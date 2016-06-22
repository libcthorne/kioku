package me.cthorne.kioku.statistics;

/**
 * Created by chris on 31/01/16.
 */
public enum GraphType {
    WORD_COUNT,
    PERFORMANCE,
    STUDY_TIME;

    @Override
    public String toString() {
        switch (this) {
            case WORD_COUNT: return "Word count";
            case PERFORMANCE: return "Performance";
            case STUDY_TIME: return "Study time";
            default: return super.toString();
        }
    }
}
