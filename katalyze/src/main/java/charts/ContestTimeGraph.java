package charts;

import java.awt.Font;

import model.Contest;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYDataset;

public abstract class ContestTimeGraph implements ContestChart {
	
	protected String shortName;
	protected String title;
	protected String yAxisLabel;
	protected String xAxisLabel = "Minutes";
	
	public abstract XYDataset getDataset(Contest contest, int currentTime);
	
	public String getTitle() { return title; }
	public String getXAxisLabel() { return xAxisLabel; }
	public String getYAxisLabel() { return yAxisLabel; }
	public String getName() { return shortName; }
	
	public ContestTimeGraph(String shortName, String chartName, String yAxisLabel) {
		this.shortName = shortName;
		this.title = chartName;
		this.yAxisLabel = yAxisLabel;
	}
	
    public JFreeChart createChart(Contest contest, int currentTime) {
    	JFreeChart chart = ChartFactory.createXYAreaChart(getTitle(), getXAxisLabel(), getYAxisLabel(), getDataset(contest, currentTime), PlotOrientation.VERTICAL, true, false, false);
    	Font font = new Font("Sans-serif", Font.BOLD, 12); 
    	chart.getTitle().setFont(font);
    	XYPlot plot = (XYPlot) chart.getPlot();
    	
    	NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
    	rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
    	return chart;
    }


}
