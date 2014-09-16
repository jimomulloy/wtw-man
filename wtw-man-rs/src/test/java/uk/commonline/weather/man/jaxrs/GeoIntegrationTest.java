package uk.commonline.weather.man.jaxrs;

import javax.inject.Inject;

import junit.framework.TestCase;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import uk.commonline.weather.geo.client.jaxrs.GeoLocationClient;
import uk.commonline.weather.model.Region;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:applicationContext.xml")
public class GeoIntegrationTest extends TestCase {

    @Inject
    private GeoLocationClient geoLocationClient;

    @Test
    public void dummy() throws Exception {

    }

    // @Test
    public void test() throws Exception {

        long region = geoLocationClient.getRegion(0, 0);
        System.out.println("!!Region 0,0:" + region);
        assertEquals("Invalid region", 1, region);
        Region regionInfo = geoLocationClient.getRegionInfo(1);
        System.out.println("!!Region Info 1at:" + regionInfo.latitude + ", long:" + regionInfo.longitude);
        assertEquals("Invalid region", 1, regionInfo.region);
        String id = geoLocationClient.getLocationId("yahoo", 51, 0);
        System.out.println("!!Yahoo Location id:" + id);
        assertEquals("Invalid region", 1, regionInfo.region);
        id = geoLocationClient.getLocationId("met", 51, 0);
        System.out.println("!!Met Location id:" + id);
        assertEquals("Invalid region", 1, regionInfo.region);
        id = geoLocationClient.getLocationId("google", 51, 0);
        System.out.println("!!Google Location id:" + id);
        assertEquals("Invalid region", 1, regionInfo.region);

        // Valid URIs
        // assertEquals(200,
        // client.target("http://localhost:8080/wtwgeo/webresources/location/region/lat/0/long/0").request().get().getStatus());
        // assertEquals(200,
        // client.target("http://localhost:8282/customer/1234").request().get().getStatus());
        // assertEquals(200,
        // client.target("http://localhost:8282/customer?zip=75012").request().get().getStatus());
        // assertEquals(200,
        // client.target("http://localhost:8282/customer/search;firstname=Antonio;surname=Goncalves").request().get().getStatus());

        // Invalid URIs
        // assertEquals(404,
        // client.target("http://localhost:8282/customer/AGONCAL").request().get().getStatus());
        // assertEquals(404,
        // client.target("http://localhost:8282/customer/dummy/1234").request().get().getStatus());

        // Stop HTTP server
        // server.stop(0);
        // final String hello = target("hello").request().get(String.class);
        // assertEquals("Hello World!", hello);
    }
}