package charts;

import java.io.File;
import java.util.List;

import model.Contest;
import model.OutputHook;

public class ChartDumperHook implements OutputHook {
	
	final File outputDirectory;
	final Contest contest;
	//final ChartDumper chartDumper;
	
	public ChartDumperHook(Contest contest, File outputDirectory) {
		this.contest = contest;
		this.outputDirectory = outputDirectory;
		
	}
	
	ChartDumper getCharts() {
		ChartDumper chartDumper = new ChartDumper(contest, outputDirectory);

		// For now, hard-code the chart types
		chartDumper.add(new ProblemSubmissionsPerMinute());
		chartDumper.add(new SubmissionsPerProblem());
		chartDumper.add(new TeamsHavingSolvedProblem());
		
		return chartDumper;
	}
	
	public List<ChartInfo> getChartInfo() {
		return getCharts().getChartInfo();
	}
	
	public void execute(int minutesFromStart) {
		getCharts().createCharts(minutesFromStart);
	}

}
