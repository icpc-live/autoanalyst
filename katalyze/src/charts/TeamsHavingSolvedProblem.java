package charts;

import java.util.ArrayList;

import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import model.Contest;
import model.Problem;
import model.Score;
import model.Standings;

public class TeamsHavingSolvedProblem extends TimeLineGraph {

	
	public TeamsHavingSolvedProblem() {
		super(String.format("TeamsSolved"), String.format("Accepted Solutions"), "Count");
	}
	
	class ProblemSeries {
		public Problem problem;
		public XYSeries series;
		public int solvedCount;
	}

	@Override
	public XYSeriesCollection getDataset(Contest contest, int currentTime) {
		XYSeriesCollection dataset = new XYSeriesCollection();
		ArrayList<ProblemSeries> problems = new ArrayList<ProblemSeries>();
		for (Problem p : contest.getProblems()) {
			ProblemSeries ps = new ProblemSeries();
			ps.series = new XYSeries(String.format("%s", p.getLetter()));
			ps.solvedCount = 0;
			ps.problem = p;
			problems.add(ps);
			dataset.addSeries(ps.series);
		}
		        
        for (int time=0; time<=currentTime; time++) {
        	int submissionsAtTime = contest.getSubmissionsAtTime(time);
        	Standings standings = contest.getStandings(submissionsAtTime);
        	
        	
    		for (ProblemSeries ps : problems) {
    			int solvedCount = 0;
    			for (Score s : standings) {
            		if (s.isSolved(ps.problem)) {
            			solvedCount++;
            		}
        		}
        		ps.series.add(time, solvedCount);
        	}
        }
                
        return dataset;
    }	

}
