package uk.commonline.weather.man.director;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.commonline.weather.model.Region;
import uk.commonline.weather.model.Weather;
import uk.commonline.weather.model.WeatherForecast;
import uk.commonline.weather.model.WeatherReport;
import uk.commonline.weather.model.WeatherReport.WeatherSourceData;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;

public class WeatherRequestControl {

    private Map<Long, ReportCacheEntry> reportCache = new HashMap<Long, ReportCacheEntry>();

    private WeatherManDirector director;

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
	    EventBusService.$().postEvent(new LookupReportCacheEvent(region, requestor));
	}
    }

    public WeatherReport updateWeather(long region) throws Exception {
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
		ReportCacheEntry reportCacheEntry;
		long timeNow = new Date().getTime();
		if (event instanceof LookupReportCacheEvent) {
		    long region = ((LookupReportCacheEvent) event).getRegion();
		    Requestor requestor = ((LookupReportCacheEvent) event).getThreadTester();
		    if (reportCache.containsKey(region) && reportCache.get(region) != null) {
			reportCacheEntry = reportCache.get(region);
			if (reportCacheEntry.timeUpdate > (timeNow - 60 * 60 * 1000)) {
			    System.out.println("Send report ready in Lookup ReportCache region:" + region + ", time:" + System.currentTimeMillis()
				    + ", event:" + event);
			    requestor.reportCompleted(reportCacheEntry.report);
			} else {
			    reportCacheEntry.requests.add(requestor);
			    System.out.println("Added pending request for report in Lookup ReportCache region:" + region + ", time:"
				    + System.currentTimeMillis() + ", event:" + event);
			}
		    } else {
			List<Requestor> list = new ArrayList<Requestor>();
			list.add(requestor);
			reportCacheEntry = new ReportCacheEntry();
			reportCacheEntry.requests = list;
			reportCache.put(region, reportCacheEntry);
			System.out.println("Report not found in Lookup ReportCache region:" + region + ", time:" + System.currentTimeMillis()
				+ ", event:" + event);
			EventBusService.$().postEvent(new LookupBaseEvent(region));
		    }
		} else if (event instanceof UpdateReportCacheEvent) {
		    WeatherReport report = ((UpdateReportCacheEvent) event).getReport();
		    long region = ((UpdateReportCacheEvent) event).getReport().getRegion();
		    List<Requestor> list;
		    if (reportCache.containsKey(region) && reportCache.get(region) != null) {
			reportCacheEntry = reportCache.get(region);
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
		}
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
		    System.out.println("Enter BaseActor region:" + region + ", time:" + System.currentTimeMillis() + ", event:" + event);
		    List<Weather> weathers = director.getWeatherDAO().recentForRegion(region);
		    if (weathers.size() == 0) {
			EventBusService.$().postEvent(new UpdateStationEvent(region));
		    } else {
			report = buildReport(weathers, region);
			EventBusService.$().postEvent(new UpdateBaseEvent(report));
			EventBusService.$().postEvent(new UpdateReportCacheEvent(report));
		    }
		    System.out.println("Exit BaseActor region:" + region + ", time:" + System.currentTimeMillis() + ", event:" + event);
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
		System.out.println("!!Report build size:"+report.getSourceMap().size());
		EventBusService.$().postEvent(new UpdateBaseEvent(report));
		EventBusService.$().postEvent(new UpdateReportCacheEvent(report));
		System.out.println("Exit StationActor region:" + report.getRegion() + ", time:" + System.currentTimeMillis() + ", event:" + event);
	    } catch (Exception ie) {
		ie.printStackTrace();
	    }
	}
    }

    private WeatherReport buildReport(List<Weather> weathers, long region) {
	WeatherReport report = new WeatherReport();
	report.setRegion(region);
	Map<String, WeatherSourceData> sourceMap = report.getSourceMap();
	for (Weather weather : weathers) {
	    WeatherSourceData wsd;
	    if (!sourceMap.containsKey(weather.getSource())) {
		wsd = report.new WeatherSourceData();
		sourceMap.put(weather.getSource(), wsd);
	    }
	    wsd = sourceMap.get(weather.getSource());
	    if (weather instanceof WeatherForecast) {
		wsd.getForecasts().add((WeatherForecast)weather);
	    } else {
		wsd.getRecordings().add(weather);

	    }
	}

	report.setDate(new Date());
	return report;
    }
}
