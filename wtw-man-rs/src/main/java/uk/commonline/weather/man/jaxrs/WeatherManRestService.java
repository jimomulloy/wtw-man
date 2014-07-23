package uk.commonline.weather.man.jaxrs;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import uk.commonline.weather.man.service.WeatherManDirectorService;
import uk.commonline.weather.model.Location;
import uk.commonline.weather.model.Weather;
import uk.commonline.weather.model.WeatherReport;

/**
 * 
 */
@Path("/manager")
@Component
@Transactional
public class WeatherManRestService {

    @Autowired
    WeatherManDirectorService weatherManDirectorService;

    protected WeatherManDirectorService getService() {
	return weatherManDirectorService;
    }

    public void setWeatherManDirectorService(WeatherManDirectorService weatherManDirectorService) {
	this.weatherManDirectorService = weatherManDirectorService;
    }

    public Class<Location> getEiClass() {
	return Location.class;
    }

    @GET
    @Path("report/lat/{lat}/long/{long}")
    @Produces(MediaType.APPLICATION_JSON)
    public WeatherReport getWeatherReport(@PathParam("lat") double latitude, @PathParam("long") double longitude) throws Exception {
	return weatherManDirectorService.getWeatherReport(latitude, longitude);
    }

    @POST
    @Path("update/lat/{lat}/long/{long}")
    @Produces(MediaType.APPLICATION_JSON)
    public WeatherReport updateWeather(@PathParam("lat") double latitude, @PathParam("long") double longitude) throws Exception {
	WeatherReport report = weatherManDirectorService.updateWeather(latitude, longitude);
	System.out.println("!!Report returning size:"+report.getSourceMap().size());
	return report;
    }
}
