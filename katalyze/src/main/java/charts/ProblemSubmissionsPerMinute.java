package charts;

import java.util.List;

import model.Contest;
import model.Submission;

import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class ProblemSubmissionsPerMinute extends ContestTimeGraph {
	
	
	public ProblemSubmissionsPerMinute() {
		super("SubsPerMinute", "Problem submissions per minute", "Count");
	}
	
    @Override
	public XYDataset getDataset(Contest contest, int currentTime) {

        XYSeries sTotal = new XYSeries("Total submissions per minute");
        XYSeries sAccepted = new XYSeries("Accepted submissions per minute");
        List<Submission> submissions = contest.getSubmissions();
        for (int time=0; time<=currentTime; time++) {
        	int total = 0;
        	int accepted = 0;
        	
        	for (Submission s : submissions) {
        		if (s.getInitialSubmission().minutesFromStart == time) {
        			total++;
        			if (s.isAccepted()) {
        				accepted++;
        			}
        		}
        	}
        	sTotal.add(time, total);
        	sAccepted.add(time, accepted);
        	
        }

        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(sTotal);
        dataset.addSeries(sAccepted);

        return dataset;
    }	

}
