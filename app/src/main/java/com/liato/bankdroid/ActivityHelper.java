package com.liato.bankdroid;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.ContextWrapper;

public class ActivityHelper {

    public static void dismissDialog(Dialog dialog) {
        if(dialog.isShowing()) { //check if dialog is showing.

            //get the Context object that was used to great the dialog
            Context context = ((ContextWrapper) dialog.getContext()).getBaseContext();

            //if the Context used here was an activity AND it hasn't been finished
            //then dismiss it
            if(context instanceof Activity) {
                if(!((Activity)context).isFinishing()) {
                    dialog.dismiss();
                }
            } else { //if the Context used wasnt an Activity, then dismiss it too
                dialog.dismiss();
            }
        }
    }

}
