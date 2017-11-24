package stats;

import rules.SolutionSubmittedEvent;
import rules.StandingsAtSubmission;
import model.InitialSubmission;

public class LanguageStats {
	
	public final SubmissionsPerLanguage submissionsPerLanguage = new SubmissionsPerLanguage();
	
	
	public class SubmissionsPerLanguage extends CategoryStats<InitialSubmission> implements SolutionSubmittedEvent {
				
		public void addLanguage(String language) {
			final String lang = language;
			addCategory(new Filter<InitialSubmission>(language) {
				public boolean contains(InitialSubmission item) {
					return lang.equals(item.getLanguage());
				};
			});
		}

		@Override
		public void onSolutionSubmitted(StandingsAtSubmission standingsAtSubmission) {
			this.process(standingsAtSubmission.submission);
			
			
		}		
	}
	

}
