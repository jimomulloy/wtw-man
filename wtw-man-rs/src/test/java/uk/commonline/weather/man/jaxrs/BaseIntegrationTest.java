package uk.commonline.weather.man.jaxrs;

import javax.inject.Inject;

import junit.framework.TestCase;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import uk.commonline.weather.model.Weather;
import uk.commonline.weather.persist.WeatherDAO;
import uk.commonline.weather.station.client.jaxrs.WeatherStationClient;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:applicationContext.xml")
public class BaseIntegrationTest extends TestCase {

    @Autowired
    WeatherDAO weatherDAO;

    public void setWeatherDAO(WeatherDAO weatherDAO) {
	this.weatherDAO = weatherDAO;
    }

    @Inject
    private WeatherStationClient weatherStationClient;

    @Test
    public void dummy() throws Exception {

    }

    // @Test
    public void test() throws Exception {

	Weather weather = new Weather();
	weather.setSource("Hi2");
	weather.setRegion(12);
	weather = weatherDAO.create(weather);
	System.out.println("!!Created weather:" + weather.getId());
    }
}