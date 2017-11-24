package charts;

import model.Contest;

import org.jfree.chart.JFreeChart;

public interface ContestChart {
	JFreeChart createChart(Contest contest, int currentTime);

	String getName();
}
