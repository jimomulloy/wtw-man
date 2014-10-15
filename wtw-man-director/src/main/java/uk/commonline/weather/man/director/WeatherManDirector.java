package uk.commonline.weather.man.director;

import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import uk.commonline.weather.geo.service.GeoLocationService;
import uk.commonline.weather.man.service.WeatherManDirectorService;
import uk.commonline.weather.model.WeatherReport;
import uk.commonline.weather.model.WeatherReport.WeatherSourceData;
import uk.commonline.weather.persist.WeatherDAO;
import uk.commonline.weather.persist.WeatherForecastDAO;
import uk.commonline.weather.station.service.WeatherStationService;

/**
 * @author Jim O'Mulloy
 * 
 * WTW Manager Director Service implementation that uses the Google Guava EventBus to process Weather report requests in a thread safe, reactive manner.
 */
@Component
public class WeatherManDirector implements WeatherManDirectorService {

    private static Logger log = Logger.getLogger(WeatherManDirector.class);

    @Inject
    private WeatherDAO weatherDAO;

    @Inject
    private WeatherForecastDAO weatherForecastDAO;

    @Inject
    private WeatherStationService weatherStationService;

    @Inject
    private GeoLocationService geoLocationService;

    @Inject
    private WeatherRequestControl weatherRequestControl;

    @Override
    public Set<Long> getActiveRegions() {
        Set<Long> activeRegions = weatherRequestControl.getRegions();
        return activeRegions;
    }

    public GeoLocationService getGeoLocationService() {
        return geoLocationService;
    }

    public WeatherDAO getWeatherDAO() {
        return weatherDAO;
    }

    public WeatherForecastDAO getWeatherForecastDAO() {
        return weatherForecastDAO;
    }

    @Override
    public WeatherReport getWeatherReport(double latitude, double longitude) throws Exception {
        WeatherReport report = new WeatherReport();
        long region = geoLocationService.getRegion(latitude, longitude);
        report = weatherRequestControl.processReport(region);
        report.setLatitude(latitude);
        report.setLongitude(longitude);
        report.setDate(new Date());
       
        Map<String, WeatherSourceData> sm = report.getSourceMap();
        for (Entry<String, WeatherSourceData> wse : sm.entrySet()) {
            log.debug("Got Report for source:"+wse.getKey()+", #recordings:"+wse.getValue().getRecordings().size()+", #forecasts:"+wse.getValue().getForecasts().size());
        }
        return report;
    }

    public WeatherStationService getWeatherStationService() {
        return weatherStationService;
    }

    public void setGeoLocationService(GeoLocationService geoLocationService) {
        this.geoLocationService = geoLocationService;
    }

    public void setWeatherDAO(WeatherDAO weatherDAO) {
        this.weatherDAO = weatherDAO;
    }

    public void setWeatherForecastDAO(WeatherForecastDAO weatherForecastDAO) {
        this.weatherForecastDAO = weatherForecastDAO;
    }

    public void setWeatherStationService(WeatherStationService weatherStationService) {
        this.weatherStationService = weatherStationService;
    }

}
