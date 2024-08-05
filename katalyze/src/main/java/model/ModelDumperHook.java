package model;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import org.apache.log4j.Logger;

import charts.ChartDumperHook;
import charts.ChartInfo;

public class ModelDumperHook implements OutputHook {
	
	final Contest contest;
	final ChartDumperHook chartDumperHook;
	
	static Logger logger = Logger.getLogger(ModelDumperHook.class);

	JsonArray getProblems() {
		JsonArray target = new JsonArray();
		for (Problem p : contest.getProblems()) {
			JsonObject obj = new JsonObject();
			obj.addProperty("problem", p.getId());
			obj.addProperty("name", p.getNameAndLabel());
			target.add(obj);
		}
		return target;
	}
	
	
	JsonArray getGraphs() {
		JsonArray target = new JsonArray();
		
		List<ChartInfo> chartInfo = chartDumperHook.getChartInfo();
		
		for (ChartInfo chart : chartInfo) {
			JsonObject obj = new JsonObject();
			obj.addProperty("description", chart.description);
			obj.addProperty("path", chart.path);
			target.add(obj);
		}
		
		return target;
	}

	JsonObject getJson(int minutesFromStart) {
		JsonObject obj = new JsonObject();
		// TODO Auto-generated method stub
		obj.add("problems", getProblems());
		obj.add("graphs", getGraphs());
		obj.addProperty("contestLength", contest.getLengthInMinutes());
		obj.addProperty("contestTime", minutesFromStart);
		return obj;
	}
	
	public ModelDumperHook(Contest contest, ChartDumperHook chartDumperHook) {
		this.contest = contest;
		this.chartDumperHook = chartDumperHook;
	}
	
	@Override
	public void execute(int minutesFromStart) {
		JsonObject json = getJson(minutesFromStart);
		FileWriter writer = null;
		try {
			writer = new FileWriter("output/viewmodel.json");
			new Gson().toJson(json, writer);
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
