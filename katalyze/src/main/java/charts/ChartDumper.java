package charts;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import model.Contest;

import org.apache.log4j.Logger;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;

public class ChartDumper {

	static final Logger logger = Logger.getLogger(ChartDumper.class);
	
	Contest contest;
	ArrayList<ContestChart> charts = new ArrayList<ContestChart>();
	final File outputDirectory;
	
	public ChartDumper(Contest contest, File outputDirectory) {
		this.contest = contest;
		this.outputDirectory = outputDirectory;
	}
	
	public void add(ContestChart chart) {
		charts.add(chart);
	}
	
	private String chartPath(ContestChart chart) {
		return String.format("%s/%s/$size$.$time$.png", outputDirectory, chart.getName());
	}
	
	private void renderChart(ContestChart chart, int minutesFromStart) {
		JFreeChart chartToRender = chart.createChart(contest, minutesFromStart);
		File chartDir = new File(outputDirectory, chart.getName());

		File largeChart = new File(chartDir, String.format("large.%03d.png", minutesFromStart));

		try {
			if (!chartDir.isDirectory()) {
				boolean wasCreated = chartDir.mkdirs();
				if (!wasCreated) {
					logger.warn(String.format("Failed to create directory %s", chartDir));
					
				}
			}
			logger.debug(String.format("Saving %s", largeChart));
			ChartUtilities.saveChartAsPNG(largeChart, chartToRender, 768, 400);
		}
		catch (Exception e) {
			logger.warn(String.format("Error saving chart %s, %s", largeChart, e));
		}
		
		/* Skip rendering of small charts
		File smallChart = new File(chartDir, String.format("small.%03d.png", minutesFromStart));		

		try {
			if (!chartDir.isDirectory()) {
				boolean wasCreated = chartDir.mkdirs();
				if (!wasCreated) {
					logger.warn(String.format("Failed to create directory %s", chartDir));
					
				}
			}
			logger.debug(String.format("Saving %s", smallChart));
			ChartUtilities.saveChartAsPNG(smallChart, chartToRender, 320, 128);
		}
		catch (Exception e) {
			logger.warn(String.format("Error saving chart %s, %s", smallChart, e));
		}		
		*/
	}
	
	public List<ChartInfo> getChartInfo() {
		ArrayList<ChartInfo> target = new ArrayList<ChartInfo>();
		for (ContestChart chart : charts) {
			ChartInfo info = new ChartInfo();
			info.description = chart.getName();
			info.path = chartPath(chart);
			target.add(info);
		}
		
		return target;
	}

	
	public void createCharts(int minutesFromStart) {
		for (ContestChart chart : charts) {
			renderChart(chart, minutesFromStart);
		}
	}

}
