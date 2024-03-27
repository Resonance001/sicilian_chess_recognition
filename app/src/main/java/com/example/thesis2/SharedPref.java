package com.example.thesis2;


import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

final public class SharedPref {
    private SharedPref() {
    }

    public static final String MAIN_TIME = "Main_Time";
    public static final String BTN_INDEX = "Button_Index";
    public static final String TIME_INDEXED = "Time_Indexed%c";
    public static final String BTN_NAME = "Button_Name%c";
    public static final String INCREMENT = "Increment";

    public static final String INDEX_HOUR = "Index_Hour";
    public static final String INDEX_MINUTES = "Index_Minutes";
    public static final String INDEX_SECONDS = "Index_Seconds";
    public static final String INDEX_INCREMENT = "Index_Increment%c";

    public static final String CANNY_LOWER = "Canny_Lower_Threshold";
    public static final String CANNY_UPPER = "Canny_Upper_Threshold";

    public static final String HUE_LOWER_LIGHT = "Light_Hue_Lower_Threshold";
    public static final String HUE_UPPER_LIGHT = "Light_Hue_Upper_Threshold";
    public static final String SATURATION_LOWER_LIGHT = "Light_Saturation_Lower_Threshold";
    public static final String SATURATION_UPPER_LIGHT = "Light_Saturation_Upper_Threshold";
    public static final String VALUE_LOWER_LIGHT = "Light_Value_Lower_Threshold";
    public static final String VALUE_UPPER_LIGHT = "Light_Value_Upper_Threshold";

    public static final String HUE_LOWER_DARK = "Dark_Hue_Lower_Threshold";
    public static final String HUE_UPPER_DARK = "Dark_Hue_Upper_Threshold";
    public static final String SATURATION_LOWER_DARK = "Dark_Saturation_Lower_Threshold";
    public static final String SATURATION_UPPER_DARK = "Dark_Saturation_Upper_Threshold";
    public static final String VALUE_LOWER_DARK = "Dark_Value_Lower_Threshold";
    public static final String VALUE_UPPER_DARK = "Dark_Value_Upper_Threshold";

    public static final String RATIO_THRESHOLD_LIGHT = "Ratio_Threshold_LIGHT";
    public static final String RATIO_THRESHOLD_DARK = "Ratio_Threshold_DARK";

    public static final String HAS_MOVED = "Moved";
    public static final String PAWN_PROMOTION = "Promoted";
    public static final String PROMOTE_QUEEN = "Q";
    public static final String PROMOTE_ROOK = "R";
    public static final String PROMOTE_BISHOP = "B";
    public static final String PROMOTE_KNIGHT = "N";

    public static String getFormatName(int index){
        char btn_index = (char) index;
        return String.format(SharedPref.BTN_NAME, btn_index);
    }

    public static String getFormatKey(char index){
        return String.format(SharedPref.TIME_INDEXED, index);
    }

    public static String getFormatIncrement(char index){
        return String.format(SharedPref.INDEX_INCREMENT, index);
    }

    public static String getStrPref(Context context, String key) {
        String value = null;
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (preferences != null) {
            value = preferences.getString(key, null);
        }
        return value;
    }

    public static boolean setStrPref(Context context, String key, String value) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (preferences != null && !TextUtils.isEmpty(key)) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(key, value);
            return editor.commit();
        }
        return false;
    }

    public static float getFloatPref(Context context, String key, float defaultValue) {
        float value = defaultValue;
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (preferences != null) {
            value = preferences.getFloat(key, defaultValue);
        }
        return value;
    }

    public static boolean setFloatPref(Context context, String key, float value) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (preferences != null) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putFloat(key, value);
            return editor.commit();
        }
        return false;
    }

    public static long getLongPref(Context context, String key, long defaultValue) {
        long value = defaultValue;
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (preferences != null) {
            value = preferences.getLong(key, defaultValue);
        }
        return value;
    }

    public static boolean setLongPref(Context context, String key, long value) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (preferences != null) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putLong(key, value);
            return editor.commit();
        }
        return false;
    }

    public static int getIntPref(Context context, String key, int defaultValue) {
        int value = defaultValue;
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (preferences != null) {
            value = preferences.getInt(key, defaultValue);
        }
        return value;
    }

    public static boolean setIntPref(Context context, String key, int value) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (preferences != null) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putInt(key, value);
            return editor.commit();
        }
        return false;
    }

    public static boolean getBoolPref(Context context, String key, boolean defaultValue) {
        boolean value = defaultValue;
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (preferences != null) {
            value = preferences.getBoolean(key, defaultValue);
        }
        return value;
    }

    public static boolean setBoolPref(Context context, String key, boolean value) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (preferences != null) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean(key, value);
            return editor.commit();
        }
        return false;
    }
}