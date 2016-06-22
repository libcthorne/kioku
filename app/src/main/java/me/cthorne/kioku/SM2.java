package me.cthorne.kioku;

/**
 * Created by chris on 15/11/15.
 * Calculates values for spaced repetition.
 * Based on the SuperMemo2 algorithm.
 */
public class SM2 {

    public static final int MIN_EASINESS_FACTOR = 130;
    public static final int MAX_EASINESS_FACTOR = 250;
    public static final int DEFAULT_EASINESS_FACTOR = 250;

    public static final int EASINESS_FACTOR_INCREASE = 10;
    public static final int EASINESS_FACTOR_DECREASE = 20;

    public static final int INCORRECT_ANSWER = 0;
    public static final int CORRECT_ANSWER = 1;

    /**
     * If q=0(INCORRECT_ANSWER), EF-=incorrect delta
     * If q=1(CORRECT_ANSWER), EF+=correct delta
     * @param easinessFactor
     * @param responseQuality
     * @return
     */
    public static int calculateNewEasinessFactor(int easinessFactor, int responseQuality) {
        switch (responseQuality) {
            case INCORRECT_ANSWER:
                easinessFactor = Math.max(MIN_EASINESS_FACTOR, easinessFactor-EASINESS_FACTOR_DECREASE); // Subtract step, making sure we don't go below min
                break;
            case CORRECT_ANSWER:
                easinessFactor = Math.min(MAX_EASINESS_FACTOR, easinessFactor+EASINESS_FACTOR_INCREASE); // Add step, making sure we don't go over max
                break;
        }

        return easinessFactor;
    }

    /**
     * I(1)=1
     *  for n>1: I(n):=IM+(I(n-1)*EF)
     * @param previousInterval
     * @param easinessFactor
     * @return
     */
    public static int calculateNextInterval(int previousInterval, int easinessFactor) {
        if (previousInterval == 0)
            return 1;

        return (int)Math.max(1, Math.ceil(previousInterval*(easinessFactor/100.0)));
    }
}
