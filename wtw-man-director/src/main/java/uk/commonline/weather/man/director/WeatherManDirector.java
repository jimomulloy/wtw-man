package uk.commonline.weather.man.director;

import java.util.List;

import javax.inject.Inject;

import uk.commonline.weather.man.service.WeatherManDirectorService;
import uk.commonline.weather.model.Location;
import uk.commonline.weather.model.Weather;
import uk.commonline.weather.persist.WeatherDAO;

public class WeatherManDirector implements WeatherManDirectorService {

	@Inject
	private WeatherDAO weatherDAO;
	
	public WeatherDAO getWeatherDAO() {
		return weatherDAO;
	}

	public void setWeatherDAO(WeatherDAO weatherDAO) {
		this.weatherDAO = weatherDAO;
	}
	
	public WeatherManDirector() {
	}

	public Weather updateForecast(String zip) throws Exception {
		Weather weather = retrieveForecast(zip);
    	System.out.println("!Update forcast for zip:"+zip);
		weatherDAO.update(weather);
		return weather;
	}

	public List<Weather> getRecentWeather(Location location) throws Exception {
		return null;
	}

	public Weather retrieveForecast(String zip) throws Exception {
		return null;
	}

}
