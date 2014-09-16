package uk.commonline.weather.man.client.jaxrs;

import java.util.Set;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;

import uk.commonline.data.client.jaxrs.AbstractCrudClient;
import uk.commonline.data.client.jaxrs.RestClient;
import uk.commonline.weather.man.service.WeatherManDirectorService;
import uk.commonline.weather.model.Weather;
import uk.commonline.weather.model.WeatherListMessenger;
import uk.commonline.weather.model.WeatherMessenger;
import uk.commonline.weather.model.WeatherReport;
import uk.commonline.weather.model.WeatherReportMessenger;

/**
 */
public class WeatherManClient extends AbstractCrudClient<Weather> implements WeatherManDirectorService {

    @Override
    public Set<Long> getActiveRegions() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected String getPath() {
        return "manager";
    }

    @Override
    public WeatherReport getWeatherReport(double latitude, double longitude) throws Exception {
        WeatherReport report = getRestClient().getClient().register(WeatherReportMessenger.class)
                .target(getRestClient().createUrl("http://localhost:8080/wtwman/webresources/")).path(getPath()).path("report/lat/{lat}/long/{long}")
                .resolveTemplate("lat", latitude).resolveTemplate("long", longitude).request(MediaType.APPLICATION_JSON).get(WeatherReport.class);
        return report;
    }

    @Override
    public void setRestClient(RestClient restClient) {
        super.setRestClient(restClient);
        restClient.registerProvider(WeatherListMessenger.class);
        restClient.registerProvider(WeatherReportMessenger.class);
        restClient.registerProvider(WeatherMessenger.class);
        restClient.resetClient();
    }

    @Override
    public WeatherReport updateWeather(double latitude, double longitude) throws Exception {
        WeatherReport report = getRestClient().getClient().register(WeatherReportMessenger.class)
                .target(getRestClient().createUrl("http://localhost:8080/wtwman/webresources/")).path(getPath()).path("update/lat/{lat}/long/{long}")
                .resolveTemplate("lat", latitude).resolveTemplate("long", longitude).request()
                .post(Entity.entity(null, MediaType.APPLICATION_JSON), WeatherReport.class);
        return report;
    }

}
