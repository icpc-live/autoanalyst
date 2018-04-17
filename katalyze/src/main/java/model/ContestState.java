package model;

public class ContestState {
    public static final ContestState BeforeStart = new ContestState(0,0,0,0,0);

    final long startedMillis;
    final long endedMillis;
    final long frozenMillis;
    final long finalizedMillis;
    final long thawedMillis;

    public boolean isRunning() {
        return startedMillis > 0 && endedMillis == 0;
    }

    public boolean isFrozen() {
        return frozenMillis > 0 && !(frozenMillis < thawedMillis);
    }

    public boolean isEnded() {
        return endedMillis > 0;
    }

    public boolean notStartedYet() {
        return startedMillis == 0;
    }

    public ContestState(long startedMillis, long endedMillis, long frozenMillis,
            long finalizedMillis, long thawedMillis) {
        this.startedMillis = startedMillis;
        this.endedMillis = endedMillis;
        this.frozenMillis = frozenMillis;
        this.finalizedMillis = finalizedMillis;
        this.thawedMillis = thawedMillis;
    }
}
