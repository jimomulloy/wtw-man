package uk.commonline.weather.man.client.jaxrs;

import java.util.List;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import uk.commonline.data.client.jaxrs.AbstractCrudClient;
import uk.commonline.weather.man.service.WeatherManDirectorService;
import uk.commonline.weather.model.Location;
import uk.commonline.weather.model.Weather;

/**
 */
public class WeatherManClient extends AbstractCrudClient<Weather> implements WeatherManDirectorService {

	protected String getPath() {
		return "forecast";
	}
	
	public Weather updateForecast(String zip) throws Exception {
		WebTarget target = getRestClient()
				.getClient()
				.target(getRestClient().createUrl(
						"http://localhost:8080/wtwman/webresources/"))
				.path(getPath()).path("update/{zip}").resolveTemplate("zip", zip);
		Weather weather = target
				.request()
				.post(Entity.entity(null, MediaType.APPLICATION_JSON), Weather.class);
		if (weather == null) {
			weather = newCiInstance();
		}
		return weather;
	}

	public List<Weather> getRecentWeather(Location location) throws Exception {
		return null;
	}

	public Weather retrieveForecast(String zip) throws Exception {
		Weather weather = getRestClient()
				.getClient()
				.target(getRestClient().createUrl(
						"http://localhost:8080/wtwman/webresources/"))
				.path(getPath()).path("retrieve/{zip}").resolveTemplate("zip", zip)
				.request(MediaType.APPLICATION_JSON).get(Weather.class);
		if (weather == null) {
			weather = newCiInstance();
		}
		return weather;
	}

}
