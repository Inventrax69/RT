package com.inventrax.rt.util;

/**
 * Created by karthik.m on 05/11/2018.
 */

public class ScanValidator {

    public static boolean IsItemScanned(String scannedData) {
        if (scannedData.startsWith("RT")) {
            return true;
        } else {
            return false;
        }
    }


    public static boolean IsLocationScanned(String scannedData) {
        if (scannedData.length() == 7 ) {
            return true;
        } else {
            return false;
        }
    }


    public static boolean isNumeric(String ValueToCheck) {

        try {
            Double result = Double.parseDouble(ValueToCheck);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

}