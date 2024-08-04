package model;

import java.util.Comparator;

public class ScoreTableComparer implements Comparator<ScoreTableEntry>{

	private static int compare(int x, int y) {
		return Integer.compare(x, y);
	}
	
	@Override
	/* (non-Javadoc)
	 * @see model.ScoreTableEntry#compareTo(model.Score)
	 */
	public int compare(ScoreTableEntry x, ScoreTableEntry y) {

		int result = -compare(x.getNumberOfSolvedProblems(), y.getNumberOfSolvedProblems());
		if (result != 0) {
			return result;
		}
		
		result = compare(x.getTimeIncludingPenalty(), y.getTimeIncludingPenalty());
		if (result != 0) {
			return result;
		}
		
		int xLastAccepted = x.getLastAcceptedSubmission();
		int yLastAccepted = y.getLastAcceptedSubmission();
		
		result = compare(xLastAccepted, yLastAccepted);

		return result;
	}

}
