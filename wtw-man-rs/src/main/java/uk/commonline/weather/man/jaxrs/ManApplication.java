package uk.commonline.weather.man.jaxrs;

import java.util.logging.Logger;

import javax.ws.rs.ApplicationPath;

import org.glassfish.jersey.filter.LoggingFilter;
import org.glassfish.jersey.server.ResourceConfig;

/**
 * @author Jim O'Mulloy
 * 
 * WTW Manager Service JAXRS Application configuration.
 *
 */
@ApplicationPath("webresources")
public class ManApplication extends ResourceConfig {
    public ManApplication() {
        packages("uk.commonline.weather.man.jaxrs;uk.commonline.weather.model;org.codehaus.jackson.jaxrs");

        // Enable LoggingFilter & output entity.
        //registerInstances(new LoggingFilter(Logger.getLogger(ManApplication.class.getName()), true));

    }
}