package com.inventrax.rt.util;


import com.inventrax.rt.application.AbstractApplication;

public class MeasureUtils {

    public static int pxToDp(int px) {
        final float scale = AbstractApplication.get().getResources().getDisplayMetrics().density;
        return (int) ((px * scale) + 0.5f);
    }

}
