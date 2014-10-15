package uk.commonline.weather.man.director;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import uk.commonline.weather.man.director.WeatherRequestControl.BaseEvent;
import uk.commonline.weather.man.director.WeatherRequestControl.CompleteReportBaseEvent;
import uk.commonline.weather.man.director.WeatherRequestControl.InitBaseEvent;
import uk.commonline.weather.man.director.WeatherRequestControl.LookupBaseEvent;
import uk.commonline.weather.man.director.WeatherRequestControl.UpdateBaseEvent;
import uk.commonline.weather.model.Weather;
import uk.commonline.weather.model.WeatherForecast;
import uk.commonline.weather.model.WeatherReport;
import uk.commonline.weather.model.WeatherReport.WeatherSourceData;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;

public class BaseActor {

    static Logger log = Logger.getLogger(BaseActor.class);

    final WeatherRequestControl weatherRequestControl;

    public BaseActor(WeatherRequestControl weatherRequestControl) {
        this.weatherRequestControl = weatherRequestControl;
    }

    @Subscribe
    @AllowConcurrentEvents
    public void handleBaseEvent(BaseEvent event) {
        try {
            if (event instanceof InitBaseEvent) {
                Date date = new Date();
                /**
                 * @author Jim O'Mulloy
                 * 
                 *         WTW Manager Director Service implementation that uses
                 *         the Google Guava EventBus to process Weather report
                 *         requests in a thread safe, reactive manner.
                 */
                Long minutesAgo = new Long(60);
                Date date5DaysAgo = new Date(date.getTime() - minutesAgo * 60 * 1000 * 24 * 5);
                List<Long> regions = this.weatherRequestControl.getDirector().getWeatherDAO().recentRegions(date5DaysAgo);
                for (long region : regions) {
                    this.weatherRequestControl.processReport(region);
                }
            } else if (event instanceof UpdateBaseEvent) {
                WeatherReport report = ((UpdateBaseEvent) event).getReport();
                Map<String, WeatherSourceData> sm = report.getSourceMap();

                for (WeatherSourceData wsd : sm.values()) {
                    List<Weather> ws = wsd.getRecordings();
                    for (Weather w : ws) {
                        if (w.getId() == null) {
                            w.setBackReferences();
                            w.setRegion(report.getRegion());
                            this.weatherRequestControl.getDirector().getWeatherDAO().create(w);
                        }
                    }
                    List<WeatherForecast> fs = wsd.getForecasts();
                    for (WeatherForecast f : fs) {
                        if (f.getId() == null) {
                            f.setBackReferences();
                            f.setRegion(report.getRegion());
                            this.weatherRequestControl.getDirector().getWeatherForecastDAO().create(f);
                        }
                    }
                }
                WeatherRequestControl.log.debug("Exit BaseActor region:" + report.getRegion() + ", time:" + System.currentTimeMillis() + ", event:"
                        + event);
            } else if (event instanceof LookupBaseEvent) {
                WeatherReport report;
                long region = ((LookupBaseEvent) event).getRegion();
                List<Weather> weathers = this.weatherRequestControl.getDirector().getWeatherDAO().recentForRegion(region);
                if (weathers.size() == 0) {
                    EventBusService.$().postEvent(this.weatherRequestControl.new UpdateStationEvent(region));
                } else {

                    Date date = new Date();
                    Long minutesAgo = new Long(60);
                    Date date5DaysAgo = new Date(date.getTime() - minutesAgo * 60 * 1000 * 24 * 5);

                    Date date1DayAgo = new Date(date.getTime() - minutesAgo * 60 * 1000 * 24);

                    Date date60MinAgo = new Date(date.getTime() - minutesAgo * 60 * 1000);

                    weathers.addAll(this.weatherRequestControl.getDirector().getWeatherDAO().getRange(region, date1DayAgo, 1, 24));

                    weathers.addAll(this.weatherRequestControl.getDirector().getWeatherDAO().getRange(region, date5DaysAgo, 24, 5));

                    List<WeatherForecast> forecasts = this.weatherRequestControl.getDirector().getWeatherForecastDAO().recentForRegion(region);

                    forecasts.addAll(this.weatherRequestControl.getDirector().getWeatherForecastDAO()
                            .getRetro(region, date5DaysAgo, date60MinAgo, 24, 5));

                    report = new WeatherReport();
                    report.setRegion(region);
                    report.buildReport(weathers, forecasts);

                    EventBusService.$().postEvent(this.weatherRequestControl.new UpdateReportCacheEvent(report));
                }
                WeatherRequestControl.log.debug("Exit BaseActor region:" + region + ", time:" + System.currentTimeMillis() + ", event:" + event);
            } else if (event instanceof CompleteReportBaseEvent) {
                WeatherReport report = ((CompleteReportBaseEvent) event).getReport();
                Date date = new Date();
                Long minutesAgo = new Long(60);
                Date date5DaysAgo = new Date(date.getTime() - minutesAgo * 60 * 1000 * 24 * 5);
                Date date1DayAgo = new Date(date.getTime() - minutesAgo * 60 * 1000 * 24);
                Date date60MinAgo = new Date(date.getTime() - minutesAgo * 60 * 1000);

                List<Weather> weathers = this.weatherRequestControl.getDirector().getWeatherDAO().getRange(report.getRegion(), date5DaysAgo, 24, 5);
                weathers.addAll(this.weatherRequestControl.getDirector().getWeatherDAO().getRange(report.getRegion(), date1DayAgo, 1, 24));

                List<WeatherForecast> forecasts = this.weatherRequestControl.getDirector().getWeatherForecastDAO()
                        .getRetro(report.getRegion(), date5DaysAgo, date60MinAgo, 24, 5);

                report.buildReport(weathers, forecasts);

                EventBusService.$().postEvent(this.weatherRequestControl.new UpdateBaseEvent(report));
                EventBusService.$().postEvent(this.weatherRequestControl.new UpdateReportCacheEvent(report));
                WeatherRequestControl.log.debug("Exit BaseActor region:" + report.getRegion() + ", time:" + System.currentTimeMillis() + ", event:"
                        + event);
            }

        } catch (Exception ie) {
            ie.printStackTrace();
        }
    }
}