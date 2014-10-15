package uk.commonline.weather.man.director;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import uk.commonline.weather.model.WeatherReport;

/**
 * @author Jim O'Mulloy
 * 
 *         WTW Manager Request Control Service that uses the Google Guava
 *         EventBus to process Weather report requests in a thread safe,
 *         reactive manner.
 */
@Component
public class WeatherRequestControl {

    static Logger log = Logger.getLogger(WeatherRequestControl.class);

    @Inject
    WeatherManDirector director;

    public WeatherRequestControl() {
        EventBusService.$().registerSubscriber(new ReportCacheActor(this));
        EventBusService.$().registerSubscriber(new BaseActor(this));
        EventBusService.$().registerSubscriber(new StationActor(this));
        EventBusService.$().registerSubscriber(new RefreshActor(this));
    }

    public WeatherManDirector getDirector() {
        return director;
    }

    public WeatherManDirector getWeatherManDirector() {
        return director;
    }

    @PostConstruct
    public void init() throws Exception {
        EventBusService.$().postEvent(new InitBaseEvent());
    }

    @Scheduled(fixedRate = 120000)
    public void refresh() throws Exception {
        EventBusService.$().postEvent(new RefreshReportCacheEvent());
    }

    public void setDirector(WeatherManDirector director) {
        this.director = director;
    }

    public void setWeatherManDirector(WeatherManDirector director) {
        this.director = director;
    }

    public WeatherReport processReport(long region) throws Exception {
        Requestor requestor = new Requestor();
        requestor.request(region);
        return requestor.getReport();
    }

    public abstract class BaseEvent {
    }

    public class CompleteReportBaseEvent extends BaseEvent {
        WeatherReport report;

        public CompleteReportBaseEvent(WeatherReport report) {
            this.report = report;
        }

        public WeatherReport getReport() {
            return report;
        }

        public void setReport(WeatherReport report) {
            this.report = report;
        }
    }

    public class LookupBaseEvent extends BaseEvent {
        long region;

        public LookupBaseEvent(long region) {
            this.region = region;
        }

        public long getRegion() {
            return region;
        }

        public void setRegion(long region) {
            this.region = region;
        }
    }

    public class InitBaseEvent extends BaseEvent {

    }

    public class LookupReportCacheEvent extends ReportCacheEvent {
        long region;
        Requestor requestor;

        public LookupReportCacheEvent(long region, Requestor requestor) {
            this.region = region;
            this.requestor = requestor;
        }

        public long getRegion() {
            return region;
        }

        public Requestor getThreadTester() {
            return requestor;
        }

        public void setRegion(long region) {
            this.region = region;
        }

        public void setThreadTester(Requestor requestor) {
            this.requestor = requestor;
        }
    }

    class Process implements Runnable {
        private long region;
        private Requestor requestor;

        public Process(Requestor requestor, long region) {
            this.requestor = requestor;
            this.region = region;
        }

        @Override
        public void run() {
            startSearchServices();
        }

        private void startSearchServices() {
            EventBusService.$().postEvent(new LookupReportCacheEvent(region, requestor));
        }
    }

    public class RefreshEvent {
        long region;

        public RefreshEvent(long region) {
            this.region = region;
        }

        public long getRegion() {
            return region;
        }

        public void setRegion(long region) {
            this.region = region;
        }
    }

    public class RefreshReportCacheEvent extends ReportCacheEvent {

    }

    public abstract class ReportCacheEvent {
    }

    public class Requestor {
        private WeatherReport report;
        private Object synchObj = new Object();

        public WeatherReport getReport() {
            return report;
        }

        public void reportCompleted(WeatherReport report) {
            setReport(report);
            synchronized (synchObj) {
                synchObj.notify();
            }
        }

        public void request(long region) {
            Thread t = new Thread(new Process(this, region));
            t.start();
            synchronized (synchObj) {
                try {
                    synchObj.wait();
                } catch (InterruptedException ie) {
                    log.debug("Exception in request for region:" + region);
                }
            }
        }

        public void setReport(WeatherReport report) {
            this.report = report;
        }
    }

    public abstract class StationEvent {
    }

    public class UpdateBaseEvent extends BaseEvent {
        WeatherReport report;

        public UpdateBaseEvent(WeatherReport report) {
            this.report = report;
        }

        public WeatherReport getReport() {
            return report;
        }

        public void setReport(WeatherReport report) {
            this.report = report;
        }
    }

    public class UpdateReportCacheEvent extends ReportCacheEvent {
        WeatherReport report;

        public UpdateReportCacheEvent(WeatherReport report) {
            this.report = report;
        }

        public WeatherReport getReport() {
            return report;
        }

        public void setReport(WeatherReport report) {
            this.report = report;
        }
    }

    public class UpdateStationEvent extends StationEvent {
        long region;

        public UpdateStationEvent(long region) {
            this.region = region;
        }

        public long getRegion() {
            return region;
        }

        public void setRegion(long region) {
            this.region = region;
        }
    }

    public Set<Long> getRegions() {
        return new HashSet<Long>();  //TODO
    }

}
