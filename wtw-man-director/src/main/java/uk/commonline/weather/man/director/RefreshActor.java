package uk.commonline.weather.man.director;

import org.apache.log4j.Logger;

import uk.commonline.weather.man.director.WeatherRequestControl.RefreshEvent;
import uk.commonline.weather.man.director.WeatherRequestControl.Requestor;

import com.google.common.eventbus.Subscribe;

public class RefreshActor {

    static Logger log = Logger.getLogger(RefreshActor.class);

    private final WeatherRequestControl weatherRequestControl;

    public RefreshActor(WeatherRequestControl weatherRequestControl) {
        this.weatherRequestControl = weatherRequestControl;
    }

    @Subscribe
    public void handleRefreshEvent(RefreshEvent event) {
        try {
            long region = event.getRegion();
            Requestor tt = weatherRequestControl.new Requestor();
            tt.request(region);

        } catch (Exception ie) {
            ie.printStackTrace();
        }
    }
}