package uk.commonline.weather.man.director;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import uk.commonline.weather.geo.service.GeoLocationService;
import uk.commonline.weather.man.service.WeatherManDirectorService;
import uk.commonline.weather.model.Weather;
import uk.commonline.weather.model.WeatherReport;
import uk.commonline.weather.persist.WeatherDAO;
import uk.commonline.weather.persist.WeatherForecastDAO;
import uk.commonline.weather.station.service.WeatherStationService;

public class WeatherManDirector implements WeatherManDirectorService {

    @Inject
    private WeatherDAO weatherDAO;

    @Inject
    private WeatherForecastDAO weatherForecastDAO;

    @Inject
    private WeatherStationService weatherStationService;

    @Inject
    private GeoLocationService geoLocationService;

    private WeatherRequestControl weatherRequestControl;

    public WeatherDAO getWeatherDAO() {
	return weatherDAO;
    }

    public void setWeatherDAO(WeatherDAO weatherDAO) {
	this.weatherDAO = weatherDAO;
    }

    public WeatherManDirector() {
	weatherRequestControl = new WeatherRequestControl();
	weatherRequestControl.setDirector(this);
    }

    public WeatherReport updateWeather(double latitude, double longitude) throws Exception {
	WeatherReport report = new WeatherReport();
	long region = geoLocationService.getRegion(latitude, longitude);
	report = weatherRequestControl.updateWeather(region);
	return report;
    }

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
	return report;
    }

    public WeatherStationService getWeatherStationService() {
	return weatherStationService;
    }

    public void setWeatherStationService(WeatherStationService weatherStationService) {
	this.weatherStationService = weatherStationService;
    }

    public GeoLocationService getGeoLocationService() {
	return geoLocationService;
    }

    public void setGeoLocationService(GeoLocationService geoLocationService) {
	this.geoLocationService = geoLocationService;
    }

    public WeatherForecastDAO getWeatherForecastDAO() {
	return weatherForecastDAO;
    }

    public void setWeatherForecastDAO(WeatherForecastDAO weatherForecastDAO) {
	this.weatherForecastDAO = weatherForecastDAO;
    }
}
