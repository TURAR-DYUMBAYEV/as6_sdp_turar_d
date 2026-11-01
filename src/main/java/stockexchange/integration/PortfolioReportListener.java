package stockexchange.integration;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import stockexchange.observer.Observer;
import stockexchange.observer.events.PortfolioReportEvent;

public final class PortfolioReportListener implements Observer<PortfolioReportEvent> {
    private final ConcurrentLinkedQueue<PortfolioReportEvent> reports = new ConcurrentLinkedQueue<>();
    public void onEvent(PortfolioReportEvent report) { reports.add(report); }
    public List<PortfolioReportEvent> reports() { return List.copyOf(reports); }
}
