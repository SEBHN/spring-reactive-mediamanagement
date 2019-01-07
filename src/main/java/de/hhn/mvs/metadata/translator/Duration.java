package de.hhn.mvs.metadata.translator;

import java.util.concurrent.TimeUnit;

/**
 * Im responsible for converting durations from metadata to human representable time (eg. 00:00:00)
 */
class Duration {

    static String toHumanFromMillis(String durationInMillis) {
        String milliseconds = durationInMillis.substring(0, durationInMillis.indexOf('.'));
        long millis = Long.valueOf(milliseconds);
        return String.format("%02d:%02d:%02d",
                TimeUnit.MILLISECONDS.toHours(millis),
                TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
                TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
    }

    static String toHumanFromSeconds(String durationInSeconds) {
        String secondsString = durationInSeconds.substring(0, durationInSeconds.indexOf('.'));
        long seconds = Long.valueOf(secondsString);
        return String.format("%02d:%02d:%02d",
                TimeUnit.SECONDS.toHours(seconds),
                TimeUnit.SECONDS.toMinutes(seconds) - TimeUnit.HOURS.toMinutes(TimeUnit.SECONDS.toHours(seconds)),
                TimeUnit.SECONDS.toSeconds(seconds) - TimeUnit.MINUTES.toSeconds(TimeUnit.SECONDS.toMinutes(seconds)));
    }
}
