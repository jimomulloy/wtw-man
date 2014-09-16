package uk.commonline.weather.man.director;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import uk.commonline.weather.geo.service.GeoLocationService;
import uk.commonline.weather.man.service.WeatherManDirectorService;
import uk.commonline.weather.model.Weather;
import uk.commonline.weather.model.WeatherReport;
import uk.commonline.weather.persist.WeatherDAO;
import uk.commonline.weather.persist.WeatherForecastDAO;
import uk.commonline.weather.station.service.WeatherStationService;

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
        long region = geoLocationService.getRegion(latitude, longitude);
        WeatherReport report = new WeatherReport();
        Map<String, List<Weather>> sourceMap = new HashMap<String, List<Weather>>();
        List<Weather> weathers = weatherDAO.recentForRegion(region);
        for (Weather weather : weathers) {
            if (!sourceMap.containsKey(weather.getSource())) {
                sourceMap.put(weather.getSource(), new ArrayList<Weather>());
            }
            sourceMap.get(weather.getSource()).add(weather);
        }
        report.setLatitude(latitude);
        report.setLongitude(longitude);
        report.setDate(new Date());
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

    @Override
    public WeatherReport updateWeather(double latitude, double longitude) throws Exception {
        WeatherReport report = new WeatherReport();
        long region = geoLocationService.getRegion(latitude, longitude);
        report = weatherRequestControl.updateWeather(region);
        report.setLatitude(latitude);
        report.setLongitude(longitude);
        report.setDate(new Date());
        return report;
    }
}
