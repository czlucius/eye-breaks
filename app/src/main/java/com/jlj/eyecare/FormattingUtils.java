package com.jlj.eyecare;

public class FormattingUtils {
    public static String formatSeconds(long originalSeconds) {

        int hours   = (int) Math.floor((float)originalSeconds / 3600);
        int minutes = (int) Math.floor(((float)originalSeconds - (hours * 3600)) / 60);
        int seconds = (int) (originalSeconds - (hours * 3600) - (minutes * 60));

        return hours + " h " + minutes + " m " + seconds + "s";
    }
}
