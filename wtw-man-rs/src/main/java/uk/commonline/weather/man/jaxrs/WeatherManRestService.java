package uk.commonline.weather.man.jaxrs;

import java.util.Set;

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
import uk.commonline.weather.model.WeatherReport;

/**
 * @author Jim O'Mulloy
 * 
 *         WTW Manager JAXRS Service
 *
 */
@Path("/manager")
@Component
public class WeatherManRestService {

    @Autowired
    WeatherManDirectorService weatherManDirectorService;

    @GET
    @Path("regions")
    @Produces(MediaType.APPLICATION_JSON)
    public Set<Long> getActiveRegions() {
        return weatherManDirectorService.getActiveRegions();
    }

    public Class<Location> getEiClass() {
        return Location.class;
    }

    protected WeatherManDirectorService getService() {
        return weatherManDirectorService;
    }

    @GET
    @Path("report/lat/{lat}/long/{long}")
    @Produces(MediaType.APPLICATION_JSON)
    public WeatherReport getWeatherReport(@PathParam("lat") double latitude, @PathParam("long") double longitude) throws Exception {
        return weatherManDirectorService.getWeatherReport(latitude, longitude);
    }

    public void setWeatherManDirectorService(WeatherManDirectorService weatherManDirectorService) {
        this.weatherManDirectorService = weatherManDirectorService;
    }
}
