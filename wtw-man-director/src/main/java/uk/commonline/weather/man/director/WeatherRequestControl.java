package uk.commonline.weather.man.director;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import uk.commonline.weather.model.Region;
import uk.commonline.weather.model.Weather;
import uk.commonline.weather.model.WeatherForecast;
import uk.commonline.weather.model.WeatherReport;
import uk.commonline.weather.model.WeatherReport.WeatherSourceData;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;

@Component
public class WeatherRequestControl {

    private static Logger log = Logger.getLogger(WeatherRequestControl.class);

    @Inject
    private WeatherManDirector director;

    private Map<Long, ReportCacheEntry> reportCache = new HashMap<Long, ReportCacheEntry>();

    public WeatherRequestControl() {
        EventBusService.$().registerSubscriber(new ReportCacheActor());
        EventBusService.$().registerSubscriber(new BaseActor());
        EventBusService.$().registerSubscriber(new StationActor());
        EventBusService.$().registerSubscriber(new RefreshActor());
    }

    public WeatherManDirector getDirector() {
        return director;
    }

    public Set<Long> getRegions() {
        return reportCache.keySet();
    }

    public WeatherManDirector getWeatherManDirector() {
        return director;
    }

    @PostConstruct
    public void init() throws Exception {
        EventBusService.$().postEvent(new InitBaseEvent());
    }

    @Scheduled(fixedRate = 120000)
    public void refresh() throws Exception {
        EventBusService.$().postEvent(new RefreshReportCacheEvent());
        //updateWeather(223054);
    }

    public void setDirector(WeatherManDirector director) {
        this.director = director;
    }

    public void setWeatherManDirector(WeatherManDirector director) {
        this.director = director;
    }

    public WeatherReport processReport(long region) throws Exception {
        Requestor tt = new Requestor();
        tt.request(region);
        return tt.getReport();
    }

    public class BaseActor {
        @Subscribe
        @AllowConcurrentEvents
        public void handleBaseEvent(BaseEvent event) {
            try {
                if (event instanceof InitBaseEvent) {
                    Date date = new Date();
                    Long minutesAgo = new Long(60);
                    Date date5DaysAgo = new Date(date.getTime() - minutesAgo * 60 * 1000 * 24 * 5);
                    List<Long> regions = director.getWeatherDAO().recentRegions(date5DaysAgo);
                    for(long region: regions){
                        System.out.println("!!**!!??InitBaseEvent for region:"+region);
                        processReport(region);
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
                                director.getWeatherDAO().create(w);
                            }
                        }
                        List<WeatherForecast> fs = wsd.getForecasts();
                        for (WeatherForecast f : fs) {
                            if (f.getId() == null) {
                                f.setBackReferences();
                                f.setRegion(report.getRegion());
                                director.getWeatherForecastDAO().create(f);
                            }
                        }
                    }
                    log.info("Exit BaseActor region:" + report.getRegion() + ", time:" + System.currentTimeMillis() + ", event:" + event);
                } else if (event instanceof LookupBaseEvent) {
                    WeatherReport report;
                    long region = ((LookupBaseEvent) event).getRegion();
                    List<Weather> weathers = director.getWeatherDAO().recentForRegion(region);
                    if (weathers.size() == 0) {
                        EventBusService.$().postEvent(new UpdateStationEvent(region));
                    } else {

                        Date date = new Date();
                        Long minutesAgo = new Long(60);
                        Date date5DaysAgo = new Date(date.getTime() - minutesAgo * 60 * 1000 * 24 * 5);

                        Date date1DayAgo = new Date(date.getTime() - minutesAgo * 60 * 1000 * 24);

                        Date date60MinAgo = new Date(date.getTime() - minutesAgo * 60 * 1000);

                        weathers.addAll(director.getWeatherDAO().getRange(region, date1DayAgo, 1, 24));

                        weathers.addAll(director.getWeatherDAO().getRange(region, date5DaysAgo, 24, 5));

                        List<WeatherForecast> forecasts = director.getWeatherForecastDAO().recentForRegion(region);

                        forecasts.addAll(director.getWeatherForecastDAO().getRetro(region, date5DaysAgo, date60MinAgo, 24, 5));

                        report = new WeatherReport();
                        report.setRegion(region);
                        report.buildReport(weathers, forecasts);

                        EventBusService.$().postEvent(new UpdateReportCacheEvent(report));
                    }
                    log.info("Exit BaseActor region:" + region + ", time:" + System.currentTimeMillis() + ", event:" + event);
                } else if (event instanceof CompleteReportBaseEvent) {
                    WeatherReport report = ((CompleteReportBaseEvent) event).getReport();
                    Date date = new Date();
                    Long minutesAgo = new Long(60);
                    Date date5DaysAgo = new Date(date.getTime() - minutesAgo * 60 * 1000 * 24 * 5);

                    Date date1DayAgo = new Date(date.getTime() - minutesAgo * 60 * 1000 * 24);

                    Date date60MinAgo = new Date(date.getTime() - minutesAgo * 60 * 1000);

                    List<Weather> weathers = director.getWeatherDAO().getRange(report.getRegion(), date5DaysAgo, 24, 5);
                    weathers.addAll(director.getWeatherDAO().getRange(report.getRegion(), date1DayAgo, 1, 24));

                    List<WeatherForecast> forecasts = director.getWeatherForecastDAO()
                            .getRetro(report.getRegion(), date5DaysAgo, date60MinAgo, 24, 5);

                    report.buildReport(weathers, forecasts);

                    EventBusService.$().postEvent(new UpdateBaseEvent(report));
                    EventBusService.$().postEvent(new UpdateReportCacheEvent(report));
                    log.info("Exit BaseActor region:" + report.getRegion() + ", time:" + System.currentTimeMillis() + ", event:" + event);
                }

            } catch (Exception ie) {
                ie.printStackTrace();
            }
        }
    }

    public abstract class BaseEvent {
    }

    public class CompleteReportBaseEvent extends BaseEvent {
        WeatherReport report;

        public CompleteReportBaseEvent(WeatherReport report) {
            this.report = report;
        }

        public WeatherReport getReport() {
            return report;
        }

        public void setReport(WeatherReport report) {
            this.report = report;
        }
    }

    public class LookupBaseEvent extends BaseEvent {
        long region;

        public LookupBaseEvent(long region) {
            this.region = region;
        }

        public long getRegion() {
            return region;
        }

        public void setRegion(long region) {
            this.region = region;
        }
    }

    public class InitBaseEvent extends BaseEvent {

    }

    public class LookupReportCacheEvent extends ReportCacheEvent {
        long region;
        Requestor requestor;

        public LookupReportCacheEvent(long region, Requestor requestor) {
            this.region = region;
            this.requestor = requestor;
        }

        public long getRegion() {
            return region;
        }

        public Requestor getThreadTester() {
            return requestor;
        }

        public void setRegion(long region) {
            this.region = region;
        }

        public void setThreadTester(Requestor requestor) {
            this.requestor = requestor;
        }
    }

    class Process implements Runnable {
        private long region;
        private Requestor requestor;

        public Process(Requestor requestor, long region) {
            this.requestor = requestor;
            this.region = region;
        }

        @Override
        public void run() {
            startSearchServices();
        }

        private void startSearchServices() {
            EventBusService.$().postEvent(new LookupReportCacheEvent(region, requestor));
        }
    }

    public class RefreshActor {
        @Subscribe
        public void handleRefreshEvent(RefreshEvent event) {
            try {
                long region = event.getRegion();
                Requestor tt = new Requestor();
                tt.request(region);

            } catch (Exception ie) {
                ie.printStackTrace();
            }
        }
    }

    public class RefreshEvent {
        long region;

        public RefreshEvent(long region) {
            this.region = region;
        }

        public long getRegion() {
            return region;
        }

        public void setRegion(long region) {
            this.region = region;
        }
    }

    public class RefreshReportCacheEvent extends ReportCacheEvent {

    }

    public class ReportCacheActor {
        @Subscribe
        public void handleReportCacheEvent(ReportCacheEvent event) {
            try {
                long timeNow = new Date().getTime();
                if (event instanceof LookupReportCacheEvent) {
                    long region = ((LookupReportCacheEvent) event).getRegion();
                    Requestor requestor = ((LookupReportCacheEvent) event).getThreadTester();
                    if (reportCache.containsKey(region) && reportCache.get(region) != null) {
                        ReportCacheEntry reportCacheEntry = reportCache.get(region);
                        if (reportCacheEntry.timeUpdate > (timeNow - 60 * 60 * 1000)) {
                            requestor.reportCompleted(reportCacheEntry.report);
                        } else {
                            reportCacheEntry.requests.add(requestor);
                            log.info("Added pending request for report in Lookup ReportCache region:" + region + ", time:"
                                    + System.currentTimeMillis() + ", event:" + event);
                        }
                    } else {
                        List<Requestor> list = new ArrayList<Requestor>();
                        list.add(requestor);
                        ReportCacheEntry reportCacheEntry = new ReportCacheEntry();
                        reportCacheEntry.requests = list;
                        reportCache.put(region, reportCacheEntry);
                        log.info("Report not found in Lookup ReportCache region:" + region + ", time:" + System.currentTimeMillis() + ", event:"
                                + event);
                        EventBusService.$().postEvent(new LookupBaseEvent(region));
                    }
                } else if (event instanceof UpdateReportCacheEvent) {
                    WeatherReport report = ((UpdateReportCacheEvent) event).getReport();

                    long region = ((UpdateReportCacheEvent) event).getReport().getRegion();
                    List<Requestor> list;
                    if (reportCache.containsKey(region) && reportCache.get(region) != null) {
                        ReportCacheEntry reportCacheEntry = reportCache.get(region);
                        list = reportCacheEntry.requests;
                        reportCacheEntry.timeUpdate = timeNow;
                        reportCacheEntry.timeAccess = timeNow;
                        reportCacheEntry.report = report;
                        for (Requestor requestor : list) {
                            requestor.reportCompleted(report);
                        }
                        list.clear();
                    }
                } else if (event instanceof RefreshReportCacheEvent) {
                    ArrayList<Long> removeEntries = new ArrayList<Long>();
                    for (ReportCacheEntry reportCacheEntry : reportCache.values()) {
                        if (reportCacheEntry.timeUpdate < (timeNow - 60 * 1000) && reportCacheEntry.requests.isEmpty()
                                && reportCacheEntry.report != null) {
                            long region = reportCacheEntry.report.getRegion();
                            removeEntries.add(region);
                            EventBusService.$().postEvent(new RefreshEvent(region));
                        }
                    }
                    for (long region : removeEntries) {
                        reportCache.remove(region);
                    }
                }
            } catch (Exception ie) {
                ie.printStackTrace();
            }
        }
    }

    public class ReportCacheEntry {
        public WeatherReport report;
        public List<Requestor> requests;
        public long timeAccess;
        public long timeUpdate;

    }

    public abstract class ReportCacheEvent {
    }

    public class Requestor {
        WeatherReport report;
        private Object synchObj = new Object();

        public WeatherReport getReport() {
            return report;
        }

        public void reportCompleted(WeatherReport report) {
            setReport(report);
            synchronized (synchObj) {
                synchObj.notify();
            }
        }

        public void request(long region) {
            Thread t = new Thread(new Process(this, region));
            t.start();
            synchronized (synchObj) {
                try {
                    synchObj.wait();
                } catch (InterruptedException ie) {
                    log.info("Exception request " + region);
                }
            }
        }

        public void setReport(WeatherReport report) {
            this.report = report;
        }
    }

    public class StationActor {
        @Subscribe
        @AllowConcurrentEvents
        public void handleStationEvent(StationEvent event) {
            try {
                WeatherReport report = new WeatherReport();
                long region = ((UpdateStationEvent) event).getRegion();
                report.setRegion(region);
                Region regionInfo = director.getGeoLocationService().getRegionInfo(region);

                report = director.getWeatherStationService().getWeatherReport(regionInfo.latitude, regionInfo.longitude);
                report.setRegion(region);
                EventBusService.$().postEvent(new CompleteReportBaseEvent(report));
                log.info("Exit StationActor region:" + report.getRegion() + ", time:" + System.currentTimeMillis() + ", event:" + event);
            } catch (Exception ie) {
                ie.printStackTrace();
            }
        }
    }

    public abstract class StationEvent {
    }

    public class UpdateBaseEvent extends BaseEvent {
        WeatherReport report;

        public UpdateBaseEvent(WeatherReport report) {
            this.report = report;
        }

        public WeatherReport getReport() {
            return report;
        }

        public void setReport(WeatherReport report) {
            this.report = report;
        }
    }

    public class UpdateReportCacheEvent extends ReportCacheEvent {
        WeatherReport report;

        public UpdateReportCacheEvent(WeatherReport report) {
            this.report = report;
        }

        public WeatherReport getReport() {
            return report;
        }

        public void setReport(WeatherReport report) {
            this.report = report;
        }
    }

    public class UpdateStationEvent extends StationEvent {
        long region;

        public UpdateStationEvent(long region) {
            this.region = region;
        }

        public long getRegion() {
            return region;
        }

        public void setRegion(long region) {
            this.region = region;
        }
    }

}
