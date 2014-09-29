package uk.commonline.weather.man.service;

import java.util.Set;

import uk.commonline.weather.model.WeatherReport;

public interface WeatherManDirectorService {

    Set<Long> getActiveRegions();

    WeatherReport getWeatherReport(double latitude, double longitude) throws Exception;

}
