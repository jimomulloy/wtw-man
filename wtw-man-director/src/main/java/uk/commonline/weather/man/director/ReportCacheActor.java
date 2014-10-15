package uk.commonline.weather.man.director;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import uk.commonline.weather.man.director.WeatherRequestControl.LookupReportCacheEvent;
import uk.commonline.weather.man.director.WeatherRequestControl.RefreshReportCacheEvent;
import uk.commonline.weather.man.director.WeatherRequestControl.ReportCacheEvent;
import uk.commonline.weather.man.director.WeatherRequestControl.Requestor;
import uk.commonline.weather.man.director.WeatherRequestControl.UpdateReportCacheEvent;
import uk.commonline.weather.model.WeatherReport;

import com.google.common.eventbus.Subscribe;

public class ReportCacheActor {

    static Logger log = Logger.getLogger(ReportCacheActor.class);

    private Map<Long, ReportCacheEntry> reportCache = new HashMap<Long, ReportCacheEntry>();

    private final WeatherRequestControl weatherRequestControl;

    public ReportCacheActor(WeatherRequestControl weatherRequestControl) {
        this.weatherRequestControl = weatherRequestControl;
    }

    @Subscribe
    public void handleReportCacheEvent(ReportCacheEvent event) {
        try {
            long timeNow = new Date().getTime();
            if (event instanceof LookupReportCacheEvent) {
                long region = ((LookupReportCacheEvent) event).getRegion();
                Requestor requestor = ((LookupReportCacheEvent) event).getThreadTester();
                if (reportCache.containsKey(region) && reportCache.get(region) != null) {
                    ReportCacheEntry reportCacheEntry = reportCache.get(region);
                    if (reportCacheEntry.timeUpdate > (timeNow - 60 * 60 * 1000)) {
                        requestor.reportCompleted(reportCacheEntry.report);
                    } else {
                        reportCacheEntry.requests.add(requestor);
                        WeatherRequestControl.log.debug("Added pending request for report in Lookup ReportCache region:" + region + ", time:"
                                + System.currentTimeMillis() + ", event:" + event);
                    }
                } else {
                    List<Requestor> list = new ArrayList<Requestor>();
                    list.add(requestor);
                    ReportCacheEntry reportCacheEntry = new ReportCacheEntry();
                    reportCacheEntry.requests = list;
                    reportCache.put(region, reportCacheEntry);
                    WeatherRequestControl.log.debug("Report not found in Lookup ReportCache region:" + region + ", time:"
                            + System.currentTimeMillis() + ", event:" + event);
                    EventBusService.$().postEvent(this.weatherRequestControl.new LookupBaseEvent(region));
                }
            } else if (event instanceof UpdateReportCacheEvent) {
                WeatherReport report = ((UpdateReportCacheEvent) event).getReport();

                long region = ((UpdateReportCacheEvent) event).getReport().getRegion();
                List<Requestor> list;
                if (reportCache.containsKey(region) && reportCache.get(region) != null) {
                    ReportCacheEntry reportCacheEntry = reportCache.get(region);
                    list = reportCacheEntry.requests;
                    reportCacheEntry.timeUpdate = timeNow;
                    reportCacheEntry.timeAccess = timeNow;
                    reportCacheEntry.report = report;
                    for (Requestor requestor : list) {
                        requestor.reportCompleted(report);
                    }
                    list.clear();
                }
            } else if (event instanceof RefreshReportCacheEvent) {
                ArrayList<Long> removeEntries = new ArrayList<Long>();
                for (ReportCacheEntry reportCacheEntry : reportCache.values()) {
                    if (reportCacheEntry.timeUpdate < (timeNow - 60 * 1000) && reportCacheEntry.requests.isEmpty() && reportCacheEntry.report != null) {
                        long region = reportCacheEntry.report.getRegion();
                        removeEntries.add(region);
                        EventBusService.$().postEvent(this.weatherRequestControl.new RefreshEvent(region));
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

    private static class ReportCacheEntry {
        public WeatherReport report;
        public List<Requestor> requests;
        public long timeAccess;
        public long timeUpdate;

    }

    public Set<Long> getRegions() {
        return reportCache.keySet();
    }
}