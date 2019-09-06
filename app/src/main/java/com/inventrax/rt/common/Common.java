package com.inventrax.rt.common;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;

import com.google.gson.Gson;
import com.inventrax.rt.R;
import com.inventrax.rt.util.DialogUtils;
import com.inventrax.rt.util.SoundUtils;

/**
 * Created by Prasanna.ch on 06/14/2018.
 */

public class Common {

    private Gson gson;
    String userId = null;

    private SoundUtils soundUtils;
    public static boolean isPopupActive() {
        return isPopupActive;
    }

    public static void setIsPopupActive(boolean isPopupActive) {
        Common.isPopupActive = isPopupActive;
    }

    public static boolean isPopupActive;




    public void showUserDefinedAlertType(String errorCode, Activity activity, Context context,String alerttype) {

        soundUtils = new SoundUtils();
        if (alerttype.equals("Critical Error")) {
            setIsPopupActive(true);
            soundUtils.alertCriticalError(activity, context);
            DialogUtils.showAlertDialog(activity, "Critical Error", errorCode, R.drawable.link_break, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            setIsPopupActive(false);
                            break;
                    }
                }
            });

            return;
        }
        if (alerttype.equals("Error")) {
            setIsPopupActive(true);
            soundUtils.alertError(activity, context);
            DialogUtils.showAlertDialog(activity, "Error", errorCode, R.drawable.cross_circle, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            setIsPopupActive(false);
                            break;
                    }
                }
            });

            return;
        }
        if (alerttype.equals("Warning")) {
            setIsPopupActive(true);
            soundUtils.alertWarning(activity, context);
            DialogUtils.showAlertDialog(activity, "Warning", errorCode, R.drawable.warning_img,new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            setIsPopupActive(false);
                            break;
                    }
                }
            });

            return;
        }
        if (alerttype.equals("Success")) {
            setIsPopupActive(true);
            soundUtils.alertSuccess(activity, context);
            DialogUtils.showAlertDialog(activity, "Success", errorCode, R.drawable.success,new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            setIsPopupActive(false);
                            break;
                    }
                }
            });
            return;
        }

    }


}