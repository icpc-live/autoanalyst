package model;

public class JudgementType {
    final String id;
    final boolean accepted;
    final boolean penalty;

    public JudgementType(String id, boolean accepted, boolean penalty) {
        this.id = id;
        this.accepted = accepted;
        this.penalty = penalty;
    }

    public String getId() {
        return id;
    }

    public boolean isAccepted() {
        return accepted;
    }

    public boolean hasPenalty() {
        return penalty;
    }
}
