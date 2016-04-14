package aau.carma;

import android.content.Context;

/**
 * Informs users about events by vibrating in specific patterns.
 */
public class Vibrator {
    /**
     * Vibrates the device to signal a significant event.
     */
    static public void significantEvent() {
        long[] vibrationPattern = {0, 500, 50, 300};
        vibrateWithPattern(vibrationPattern);
    }

    /**
     * Vibrates with the specified pattern.
     * @param vibrationPattern Pattern to vibrate in.
     */
    static private void vibrateWithPattern(long[] vibrationPattern) {
        android.os.Vibrator vibrator = (android.os.Vibrator) App.getContext().getSystemService(Context.VIBRATOR_SERVICE);
        // -1 for don't repeat
        final int indexInPatternToRepeat = -1;
        vibrator.vibrate(vibrationPattern, indexInPatternToRepeat);
    }
}
