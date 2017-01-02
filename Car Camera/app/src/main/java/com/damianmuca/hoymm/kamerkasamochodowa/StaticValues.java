package com.damianmuca.hoymm.kamerkasamochodowa;

/**
 * Created by Hoymm on 2016-10-20.
 */

public class StaticValues {
    private static double bitrateMultiplication = 0.096;
    static String [] mySubsettingsClassesNamesArray = null;

    private static int latLongAccuracy = 5;

    public static double getBitrateMultiplication() {
        return bitrateMultiplication;
    }

    public static int getLatLongAccuracy() {
        return latLongAccuracy;
    }
}


