package uk.commonline.weather.man.jaxrs;

import javax.inject.Inject;

import junit.framework.TestCase;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import uk.commonline.weather.model.WeatherReport;
import uk.commonline.weather.station.client.jaxrs.WeatherStationClient;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:applicationContext.xml")
public class StationIntegrationTest extends TestCase {

    @Inject
    private WeatherStationClient weatherStationClient;

    @Test
    public void dummy() throws Exception {

    }

    // @Test
    public void test() throws Exception {
	System.out.println("!!Before Weather 50,0:");
	WeatherReport report = weatherStationClient.getWeatherReport(50, 0);
	System.out.println("!!Weather 50,0 region:" + report.getRegion());
	// assertEquals("Invalid region", 1, region);
    }
}