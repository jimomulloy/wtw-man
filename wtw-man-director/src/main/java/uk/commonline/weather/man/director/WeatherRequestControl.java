package uk.commonline.weather.man.director;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import uk.commonline.weather.model.Region;
import uk.commonline.weather.model.Weather;
import uk.commonline.weather.model.WeatherForecast;
import uk.commonline.weather.model.WeatherReport;
import uk.commonline.weather.model.WeatherReport.WeatherSourceData;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;

@Component
public class WeatherRequestControl {

    private Map<Long, ReportCacheEntry> reportCache = new HashMap<Long, ReportCacheEntry>();

    @Inject
    private WeatherManDirector director;

    public WeatherManDirector getWeatherManDirector() {
        return director;
    }

    public void setWeatherManDirector(WeatherManDirector director) {
        this.director = director;
    }

    public class ReportCacheEntry {
        public List<Requestor> requests;
        public long timeUpdate;
        public long timeAccess;
        public WeatherReport report;

    }

    public WeatherRequestControl() {
        EventBusService.$().registerSubscriber(new ReportCacheActor());
        EventBusService.$().registerSubscriber(new BaseActor());
        EventBusService.$().registerSubscriber(new StationActor());
        EventBusService.$().registerSubscriber(new RefreshActor());
    }

    public class Requestor {
        private Object synchObj = new Object();
        WeatherReport report;

        public WeatherReport getReport() {
            return report;
        }

        public void setReport(WeatherReport report) {
            this.report = report;
        }

        public void request(long region) {
            Thread t = new Thread(new Process(this, region));
            t.start();
            synchronized (synchObj) {
                try {
                    System.out.println("!!WRC Started request process " + region);
                    synchObj.wait();
                    System.out.println("!!WRC Exit request process " + region);
                } catch (InterruptedException ie) {
                    System.out.println("Exception request " + region);
                }
            }
        }

        public void reportCompleted(WeatherReport report) {
            setReport(report);
            synchronized (synchObj) {
                synchObj.notify();
            }
        }
    }

    class Process implements Runnable {
        private Requestor requestor;
        private long region;

        public Process(Requestor requestor, long region) {
            this.requestor = requestor;
            this.region = region;
        }

        public void run() {
            startSearchServices();
        }

        private void startSearchServices() {
            System.out.println("!!WRC startSearchServices region: " + region);
            EventBusService.$().postEvent(new LookupReportCacheEvent(region, requestor));
        }
    }

    @Scheduled(fixedRate = 30000)
    public void refresh() throws Exception {
        System.out.println("!!WRC refresh THIS:" + this.hashCode());
        EventBusService.$().postEvent(new RefreshReportCacheEvent());
    }

    public WeatherReport updateWeather(long region) throws Exception {
        System.out.println("!!WRC updateWeather THIS:" + this.hashCode());
        Requestor tt = new Requestor();
        tt.request(region);
        return tt.getReport();
    }

    public WeatherManDirector getDirector() {
        return director;
    }

    public void setDirector(WeatherManDirector director) {
        this.director = director;
    }

    public abstract class BaseEvent {
    }

    public abstract class StationEvent {
    }

    public abstract class ReportCacheEvent {
    }

    public class RefreshEvent {
        long region;

        public long getRegion() {
            return region;
        }

        public void setRegion(long region) {
            this.region = region;
        }

        public RefreshEvent(long region) {
            this.region = region;
        }
    }

    public class LookupReportCacheEvent extends ReportCacheEvent {
        long region;
        Requestor requestor;

        public Requestor getThreadTester() {
            return requestor;
        }

        public void setThreadTester(Requestor requestor) {
            this.requestor = requestor;
        }

        public long getRegion() {
            return region;
        }

        public void setRegion(long region) {
            this.region = region;
        }

        public LookupReportCacheEvent(long region, Requestor requestor) {
            this.region = region;
            this.requestor = requestor;
        }
    }

    public class RefreshReportCacheEvent extends ReportCacheEvent {

    }

    public class LookupBaseEvent extends BaseEvent {
        long region;

        public long getRegion() {
            return region;
        }

        public void setRegion(long region) {
            this.region = region;
        }

        public LookupBaseEvent(long region) {
            this.region = region;
        }
    }

    public class CompleteReportBaseEvent extends BaseEvent {
        WeatherReport report;

        public WeatherReport getReport() {
            return report;
        }

        public void setReport(WeatherReport report) {
            this.report = report;
        }

        public CompleteReportBaseEvent(WeatherReport report) {
            this.report = report;
        }
    }

    public class UpdateReportCacheEvent extends ReportCacheEvent {
        WeatherReport report;

        public WeatherReport getReport() {
            return report;
        }

        public void setReport(WeatherReport report) {
            this.report = report;
        }

        public UpdateReportCacheEvent(WeatherReport report) {
            this.report = report;
        }
    }

    public class UpdateBaseEvent extends BaseEvent {
        WeatherReport report;

        public WeatherReport getReport() {
            return report;
        }

        public void setReport(WeatherReport report) {
            this.report = report;
        }

        public UpdateBaseEvent(WeatherReport report) {
            this.report = report;
        }
    }

    public class UpdateStationEvent extends StationEvent {
        long region;

        public long getRegion() {
            return region;
        }

        public void setRegion(long region) {
            this.region = region;
        }

        public UpdateStationEvent(long region) {
            this.region = region;
        }
    }

    public class ReportCacheActor {
        @Subscribe
        public void handleReportCacheEvent(ReportCacheEvent event) {
            try {
                long timeNow = new Date().getTime();
                System.out.println("!!WRC handleReportCacheEvent: " + event.getClass().getName() + ", THIS:" + this.hashCode());
                if (event instanceof LookupReportCacheEvent) {
                    long region = ((LookupReportCacheEvent) event).getRegion();
                    Requestor requestor = ((LookupReportCacheEvent) event).getThreadTester();
                    if (reportCache.containsKey(region) && reportCache.get(region) != null) {
                        ReportCacheEntry reportCacheEntry = reportCache.get(region);
                        if (reportCacheEntry.timeUpdate > (timeNow - 60 * 60 * 1000)) {
                            System.out.println("!!LookupReportCacheEvent - Report build size:" + reportCacheEntry.report.getSourceMap().size() +", MET RECS:"+reportCacheEntry.report.getSourceMap().get("met").getRecordings().size()+", MET FORES:"+reportCacheEntry.report.getSourceMap().get("met").getForecasts().size());
                            
                            System.out.println("Send report ready in Lookup ReportCache region:" + region + ", time:" + System.currentTimeMillis()
                                    + ", event:" + event );
                            requestor.reportCompleted(reportCacheEntry.report);
                        } else {
                            reportCacheEntry.requests.add(requestor);
                            System.out.println("Added pending request for report in Lookup ReportCache region:" + region + ", time:"
                                    + System.currentTimeMillis() + ", event:" + event);
                        }
                    } else {
                        List<Requestor> list = new ArrayList<Requestor>();
                        list.add(requestor);
                        ReportCacheEntry reportCacheEntry = new ReportCacheEntry();
                        reportCacheEntry.requests = list;
                        reportCache.put(region, reportCacheEntry);
                        System.out.println("Report not found in Lookup ReportCache region:" + region + ", time:" + System.currentTimeMillis()
                                + ", event:" + event);
                        EventBusService.$().postEvent(new LookupBaseEvent(region));
                    }
                } else if (event instanceof UpdateReportCacheEvent) {
                    WeatherReport report = ((UpdateReportCacheEvent) event).getReport();
                    System.out.println("!!UpdateReportCacheEvent - Report build size:" + report.getSourceMap().size() +", MET RECS:"+report.getSourceMap().get("met").getRecordings().size()+", MET FORES:"+report.getSourceMap().get("met").getForecasts().size());
                    
                    long region = ((UpdateReportCacheEvent) event).getReport().getRegion();
                    List<Requestor> list;
                    if (reportCache.containsKey(region) && reportCache.get(region) != null) {
                        ReportCacheEntry reportCacheEntry = reportCache.get(region);
                        list = reportCacheEntry.requests;
                        reportCacheEntry.timeUpdate = timeNow;
                        reportCacheEntry.timeAccess = timeNow;
                        reportCacheEntry.report = report;
                        for (Requestor requestor : list) {
                            System.out.println("Send Report ready in UpdateReportCache region:" + region + ", time:" + System.currentTimeMillis()
                                    + ", event:" + event);
                            requestor.reportCompleted(report);
                        }
                        list.clear();
                    }
                } else if (event instanceof RefreshReportCacheEvent) {
                    ArrayList<Long> removeEntries = new ArrayList<Long>();
                    for (ReportCacheEntry reportCacheEntry : reportCache.values()) {
                        if (reportCacheEntry.timeUpdate < (timeNow - 60 * 1000) && reportCacheEntry.requests.isEmpty() && reportCacheEntry.report != null) {
                            long region = reportCacheEntry.report.getRegion();
                            System.out.println("Scheduled refresh region:" + region + ", time:" + System.currentTimeMillis() + ", event:" + event);
                            removeEntries.add(region);
                            EventBusService.$().postEvent(new RefreshEvent(region));
                        }
                    }
                    for (long region : removeEntries) {
                        reportCache.remove(region);
                    }
                }
            } catch (Exception ie) {
                ie.printStackTrace();
            }
        }
    }

    public class RefreshActor {
        @Subscribe
        public void handleRefreshEvent(RefreshEvent event) {
            try {
                long region = event.getRegion();
                System.out.println("Handle refresh region:" + event.getRegion() + ", time:" + System.currentTimeMillis() + ", event:" + event);
                Requestor tt = new Requestor();
                tt.request(region);

            } catch (Exception ie) {
                ie.printStackTrace();
            }
        }
    }

    public class BaseActor {
        @Subscribe
        @AllowConcurrentEvents
        public void handleBaseEvent(BaseEvent event) {
            try {
                if (event instanceof UpdateBaseEvent) {
                    WeatherReport report = ((UpdateBaseEvent) event).getReport();
                    Map<String, WeatherSourceData> sm = report.getSourceMap();
                    for (WeatherSourceData wsd : sm.values()) {
                        List<Weather> ws = wsd.getRecordings();
                        for (Weather w : ws) {
                            w.setBackReferences();
                            director.getWeatherDAO().create(w);
                        }
                        List<WeatherForecast> fs = wsd.getForecasts();
                        for (WeatherForecast f : fs) {
                            f.setBackReferences();
                            director.getWeatherForecastDAO().create(f);
                        }
                    }
                    System.out.println("Exit BaseActor region:" + report.getRegion() + ", time:" + System.currentTimeMillis() + ", event:" + event);
                } else if (event instanceof LookupBaseEvent) {
                    WeatherReport report;
                    long region = ((LookupBaseEvent) event).getRegion();
                    System.out.println("!*!*!recentForRegion LookupBaseEvent BaseActor region:" + region + ", time:" + System.currentTimeMillis() + ", event:"
                            + event + ", THIS:" + this.hashCode());
                    List<Weather> weathers = director.getWeatherDAO().recentForRegion(region);
                    if (weathers.size() == 0) {
                        System.out.println("!*!*!recentForRegion LookupBaseEvent none");
                        EventBusService.$().postEvent(new UpdateStationEvent(region));
                    } else {
                        System.out.println("!*!*!GOT current recentForRegion BaseActor region:" + region + ", weathers size:"+weathers.size());

                        Date date = new Date();
                        Long minutesAgo = new Long(60);
                        Date date5DaysAgo = new Date(date.getTime() - minutesAgo * 60 * 1000 * 24 * 5);
                        
                        Date date60MinAgo = new Date(date.getTime() - minutesAgo * 60 * 1000);
                        
                        weathers.addAll(director.getWeatherDAO().getRange(region, date5DaysAgo, 24, 5));
                        List<WeatherForecast> forecasts = director.getWeatherForecastDAO().getRange(region, date60MinAgo, 24, 5);
                        forecasts.addAll(director.getWeatherForecastDAO().getRetro(region, date5DaysAgo, date60MinAgo, 24, 5));
                        System.out.println("!*!*!GOT forecast recentForRegion BaseActor region:" + region + ", forecasts size:"+forecasts.size());
                         
                        report = new WeatherReport();
                        report.setRegion(region);
                        report = buildReport(report, weathers, forecasts);
                        System.out.println("!!LookupBaseEvent - Report build size:" + report.getSourceMap().size() +", MET RECS:"+report.getSourceMap().get("met").getRecordings().size()+", MET FORES:"+report.getSourceMap().get("met").getForecasts().size());
                        
                        // EventBusService.$().postEvent(new
                        // UpdateBaseEvent(report));
                        EventBusService.$().postEvent(new UpdateReportCacheEvent(report));
                    }
                    System.out.println("Exit BaseActor region:" + region + ", time:" + System.currentTimeMillis() + ", event:" + event);
                } else if (event instanceof CompleteReportBaseEvent) {
                    WeatherReport report = ((CompleteReportBaseEvent) event).getReport();
                    System.out.println("Enter BaseActor region:" + report.getRegion() + ", time:" + System.currentTimeMillis() + ", event:" + event);
                    Date date = new Date();
                    Long minutesAgo = new Long(60);
                    Date date5DaysAgo = new Date(date.getTime() - minutesAgo * 60 * 1000 * 24 * 5);
                    List<Weather> weathers = director.getWeatherDAO().getRange(report.getRegion(), date5DaysAgo, 24, 5);
                    List<WeatherForecast> forecasts = director.getWeatherForecastDAO().getRange(report.getRegion(), date5DaysAgo, 24, 5);
                    report = buildReport(report, weathers, forecasts);
                    EventBusService.$().postEvent(new UpdateReportCacheEvent(report));
                    System.out.println("Exit BaseActor region:" + report.getRegion() + ", time:" + System.currentTimeMillis() + ", event:" + event);
                }

            } catch (Exception ie) {
                ie.printStackTrace();
            }
        }
    }

    public class StationActor {
        @Subscribe
        @AllowConcurrentEvents
        public void handleStationEvent(StationEvent event) {
            try {
                WeatherReport report = new WeatherReport();
                long region = ((UpdateStationEvent) event).getRegion();
                report.setRegion(region);
                System.out.println("Enter StationActor region:" + report.getRegion() + ", time:" + System.currentTimeMillis() + ", event:" + event);
                Region regionInfo = director.getGeoLocationService().getRegionInfo(region);

                report = director.getWeatherStationService().getWeatherReport(regionInfo.latitude, regionInfo.longitude);
                report.setRegion(region);
                System.out.println("!!handleStationEvent - Report build size:" + report.getSourceMap().size() +", MET RECS:"+report.getSourceMap().get("met").getRecordings().size()+", MET FORES:"+report.getSourceMap().get("met").getForecasts().size());
                EventBusService.$().postEvent(new UpdateBaseEvent(report));
                EventBusService.$().postEvent(new CompleteReportBaseEvent(report));
                // EventBusService.$().postEvent(new UpdateBaseEvent(report));
                // EventBusService.$().postEvent(new
                // UpdateReportCacheEvent(report));
                System.out.println("Exit StationActor region:" + report.getRegion() + ", time:" + System.currentTimeMillis() + ", event:" + event);
            } catch (Exception ie) {
                ie.printStackTrace();
            }
        }
    }

    private WeatherReport buildReport(WeatherReport report, List<Weather> weathers, List<WeatherForecast> forecasts) {
        Map<String, WeatherSourceData> sourceMap = report.getSourceMap();
        for (Weather weather : weathers) {
            WeatherSourceData wsd;
            if (!sourceMap.containsKey(weather.getSource())) {
                wsd = report.new WeatherSourceData();
                sourceMap.put(weather.getSource(), wsd);
            }
            wsd = sourceMap.get(weather.getSource());
            if (weather instanceof WeatherForecast) {
                wsd.getForecasts().add((WeatherForecast) weather);
            } else {
                wsd.getRecordings().add(weather);

            }
        }
        for (WeatherForecast forecast : forecasts) {
            WeatherSourceData wsd;
            if (!sourceMap.containsKey(forecast.getSource())) {
                wsd = report.new WeatherSourceData();
                sourceMap.put(forecast.getSource(), wsd);
            }
            wsd = sourceMap.get(forecast.getSource());
            wsd.getForecasts().add(forecast);
        }
        report.setDate(new Date());
        return report;
    }

}
