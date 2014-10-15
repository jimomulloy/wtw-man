package uk.commonline.weather.man.service;

import java.util.Set;

import uk.commonline.weather.model.WeatherReport;

/**
 * @author Jim O'Mulloy
 *
 *         WTW Manager service API
 * 
 */
public interface WeatherManDirectorService {

    /**
     * Get Set of active Region ids.
     * 
     * @return
     */
    Set<Long> getActiveRegions();

    /**
     * Get Weather report in the Region of the given latitude, longitude
     * 
     * @param latitude
     * @param longitude
     * @return
     * @throws Exception
     */
    WeatherReport getWeatherReport(double latitude, double longitude) throws Exception;

}
