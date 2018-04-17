package rules;

import model.EventImportance;
import model.LoggableEvent;

public class CriterionRule extends StateComparingRuleBase implements StandingsUpdatedEvent {

    private final StandingsCriterion criterion;

    public CriterionRule(StandingsCriterion criterion) {
        this.criterion = criterion;
    }

    @Override
    public void onStandingsUpdated(StandingsTransition transition) {
        boolean fulfilledBefore = criterion.isFulfilled(transition.before);
        if (!fulfilledBefore) {
            boolean fulfilledAfter = criterion.isFulfilled(transition.after);
            if (fulfilledAfter) {
                notify(transition.createEvent(criterion.message(), EventImportance.Breaking));
            }
        }
    }

    public String toString() {
        return "Criterion_"+criterion.toString();
    }
}
