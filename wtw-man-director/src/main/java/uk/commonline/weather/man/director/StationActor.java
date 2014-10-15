package uk.commonline.weather.man.director;

import uk.commonline.weather.man.director.WeatherRequestControl.StationEvent;
import uk.commonline.weather.man.director.WeatherRequestControl.UpdateStationEvent;
import uk.commonline.weather.model.Region;
import uk.commonline.weather.model.WeatherReport;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;

public class StationActor {
  
    private final WeatherRequestControl weatherRequestControl;

    public StationActor(WeatherRequestControl weatherRequestControl) {
        this.weatherRequestControl = weatherRequestControl;
    } 

    @Subscribe
    @AllowConcurrentEvents
    public void handleStationEvent(StationEvent event) {
        try {
            WeatherReport report = new WeatherReport();
            long region = ((UpdateStationEvent) event).getRegion();
            report.setRegion(region);
            Region regionInfo = this.weatherRequestControl.director.getGeoLocationService().getRegionInfo(region);

            report = this.weatherRequestControl.director.getWeatherStationService().getWeatherReport(regionInfo.latitude, regionInfo.longitude);
            report.setRegion(region);
            EventBusService.$().postEvent(this.weatherRequestControl.new CompleteReportBaseEvent(report));
            WeatherRequestControl.log.debug("Exit StationActor region:" + report.getRegion() + ", time:" + System.currentTimeMillis() + ", event:"
                    + event);
        } catch (Exception ie) {
            ie.printStackTrace();
        }
    }
}