package model;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;

import charts.ChartDumperHook;
import charts.ChartInfo;

import net.sf.json.*;

public class ModelDumperHook implements OutputHook {
	
	final Contest contest;
	final ChartDumperHook chartDumperHook;
	
	static Logger logger = Logger.getLogger(ModelDumperHook.class);

	JSONArray getProblems() {
		JSONArray target = new JSONArray();
		for (Problem p : contest.getProblems()) {
			JSONObject obj = new JSONObject();
			obj.put("problem", p.getId());
			obj.put("name", p.getNameAndLabel());
			target.add(obj);
		}
		return target;
	}
	
	
	JSONArray getGraphs() {
		JSONArray target = new JSONArray();
		
		List<ChartInfo> chartInfo = chartDumperHook.getChartInfo();
		
		for (ChartInfo chart : chartInfo) {
			JSONObject obj = new JSONObject();
			obj.put("description", chart.description);
			obj.put("path", chart.path);
			target.add(obj);
		}
		
		return target;
	}

	JSONObject getJson(int minutesFromStart) {
		JSONObject obj = new JSONObject();
		// TODO Auto-generated method stub
		obj.put("problems", getProblems());
		obj.put("graphs", getGraphs());
		obj.put("contestLength", contest.getLengthInMinutes());
		obj.put("contestTime", minutesFromStart);
		return obj;
	}
	
	public ModelDumperHook(Contest contest, ChartDumperHook chartDumperHook) {
		this.contest = contest;
		this.chartDumperHook = chartDumperHook;
	}
	
	@Override
	public void execute(int minutesFromStart) {
		JSONObject json = getJson(minutesFromStart);
		FileWriter writer = null;
		try {
			writer = new FileWriter("output/viewmodel.json");
			json.write(writer);
			writer.close();
		}
		catch (IOException e) {
			logger.error("Error while updating viewmodel.json :"+e.getMessage());
		}
		finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
					logger.error("Error while closing viewmodel.json :"+e.getMessage());
				}
			}
		}
	}

}
