package charts;

import java.awt.BasicStroke;
import java.awt.Font;

import model.Contest;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.XYSeriesCollection;

public abstract class TimeLineGraph implements ContestChart {
	
	protected String shortName;
	protected String title;
	protected String yAxisLabel;
	protected String xAxisLabel = "Minutes";
	
	public abstract XYSeriesCollection getDataset(Contest contest, int currentTime);
	
	public String getTitle() { return title; }
	public String getXAxisLabel() { return xAxisLabel; }
	public String getYAxisLabel() { return yAxisLabel; }
	public String getName() { return shortName; }
	
	public TimeLineGraph(String shortName, String chartName, String yAxisLabel) {
		this.shortName = shortName;
		this.title = chartName;
		this.yAxisLabel = yAxisLabel;
	}
	
    public JFreeChart createChart(Contest contest, int currentTime) {
    	XYSeriesCollection dataset = getDataset(contest, currentTime);
    	JFreeChart chart = ChartFactory.createXYLineChart(getTitle(), getXAxisLabel(), getYAxisLabel(), dataset, PlotOrientation.VERTICAL, true, false, false);
    	Font font = new Font("Sans-serif", Font.BOLD, 12); 
    	chart.getTitle().setFont(font);
    	XYPlot plot = (XYPlot) chart.getPlot();
    	
		XYItemRenderer renderer = plot.getRenderer();
		int i=0;
    	for (Object series : dataset.getSeries()) {
    	
//    		XYSeries xySeries = (XYSeries) series;
    		renderer.setSeriesStroke(i, new BasicStroke((float) 2.0));
    		i++;
    	}
    	
    	NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
    	rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
    	return chart;
    }
	

}
