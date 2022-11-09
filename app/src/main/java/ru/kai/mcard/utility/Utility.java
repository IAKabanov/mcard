package ru.kai.mcard.utility;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;

import ru.kai.mcard.Constants;

/**
 * Created by akabanov on 30.08.2017.
 */

public final class Utility {
    public void checkDB(){

    }

    /**
     * Ensures that an object reference passed as a parameter to the calling method is not null.
     *
     * @param reference an object reference
     * @return the non-null reference that was validated
     * @throws NullPointerException if {@code reference} is null
     */
    public static <T> T checkNotNull(T reference) {
        if (reference == null) {
            throw new NullPointerException();
        }
        return reference;
    }

    // повороты. Если мы поворачиваем device из портрета в ландшафт, то нужно зафиксировать TAG фрагмента,
    // из которого был поворот, чтобы при обратном повороте вернуться на экран именно с этим фрагментом.
    // Так как активность при повороте пересоздается, то сохранять имя активости и состояние фрагментов будем в SharedPreferences.
    public static String getFragmentTAGByActivityName(Context context, String activityName){

        SharedPreferences mSettings = context.getSharedPreferences(Constants.APP_PREFERENCES, Context.MODE_PRIVATE);

        String prefsActName = "";
        if (mSettings.contains(Constants.TAGActivityName)) {
            prefsActName = mSettings.getString(Constants.TAGActivityName, "");
        }else return "";

        if (activityName.equals(prefsActName)){
            String prefsFragTAG = "";
            if (mSettings.contains(Constants.TAGFragmentTAG)) {
                prefsFragTAG = mSettings.getString(Constants.TAGFragmentTAG, "");
            }else return "";
            if (!prefsFragTAG.isEmpty())return prefsFragTAG;

        }else return "";

        return "";
    }

    public static void setFragmentTAGAndActivityName(Context context, String activityName, String fragmentTAG){

        SharedPreferences mSettings = context.getSharedPreferences(Constants.APP_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mSettings.edit();
        editor.putString(Constants.TAGActivityName, activityName);
        editor.putString(Constants.TAGFragmentTAG, fragmentTAG);
        editor.apply();

    }

    public static Boolean isThisOrientationPort(Context activity){
        if(activity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
            return true;
        else if (activity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
            return false;
        else
            return true;
    }


}
