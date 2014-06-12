package java.uk.commonline.weather.man.jaxrs;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import uk.commonline.weather.man.service.WeatherManDirectorService;
import uk.commonline.weather.model.Location;
import uk.commonline.weather.model.Weather;

/**
 * 
 */
@Path("/forecast")
@Component
@Transactional
public class WeatherManRestService {

	@Autowired
	WeatherManDirectorService weatherManDirector;
	
	protected WeatherManDirectorService getService() {
		return weatherManDirector;
	}
		
	public void setWeatherManService(WeatherManDirectorService weatherManService) {
		this.weatherManDirector = weatherManService;
	}

	public Class<Location> getEiClass() {
		return Location.class;
	}
	
	@POST
	@Path("forecast/{zip}")
	@Produces(MediaType.APPLICATION_JSON)
	public Weather updateForecast(String zip) throws Exception {
		return weatherManDirector.updateForecast(zip);
	}

	public List<Weather> getRecentWeather(Location location) throws Exception {
		return null;
	}

	@GET
	@Path("retrieve/{zip}")
	@Produces(MediaType.APPLICATION_JSON)
	public Weather retrieveForecast(String zip) throws Exception {
		return weatherManDirector.retrieveForecast(zip);
	}
}
