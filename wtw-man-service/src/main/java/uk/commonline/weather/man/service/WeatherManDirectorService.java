package uk.commonline.weather.man.service;

import uk.commonline.weather.model.WeatherReport;

public interface WeatherManDirectorService {
	
	WeatherReport updateWeather(double latitude, double longitude) throws Exception;
	
	WeatherReport getWeatherReport(double latitude, double longitude) throws Exception;
}
