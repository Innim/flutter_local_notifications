package com.dexterous.flutterlocalnotifications;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.Arrays;

public class ShownNotificationsPreferences {
  private static final String SHARED_PREFS_FILE_NAME =
      "flutter_local_notifications_plugin_shown_info";
  private final String SHOWN_NOTIFICATIONS_INFO_KEY =
      "com.dexterous.flutterlocalnotifications.SHOWN_NOTIFICATIONS_INFO_KEY";
  private final String SHOWN_NOTIFICATIONS_INFO_DIVIDER = "/";

  private final Context context;

  public ShownNotificationsPreferences(Context context) {

    this.context = context;
  }

  public void saveShownNotificationInfo(String info) {
    final SharedPreferences pref = get();
    final SharedPreferences.Editor editor = pref.edit();
    String currentInfo = pref.getString(SHOWN_NOTIFICATIONS_INFO_KEY, "");
    if (!currentInfo.isEmpty()) {
      currentInfo = currentInfo + SHOWN_NOTIFICATIONS_INFO_DIVIDER + info;
    } else {
      currentInfo = info;
    }
    editor.putString(SHOWN_NOTIFICATIONS_INFO_KEY, currentInfo);
    editor.apply();
  }

  public ArrayList<String> getShownNotificationsInfo() {
    final SharedPreferences pref = get();
    final String currentInfo = pref.getString(SHOWN_NOTIFICATIONS_INFO_KEY, "");
    if (currentInfo.isEmpty()) {
      return new ArrayList<>();
    }
    final String[] list = currentInfo.split(SHOWN_NOTIFICATIONS_INFO_DIVIDER);
    return new ArrayList<>(Arrays.asList(list));
  }

  public void clearShownNotificationsInfo() {
    final SharedPreferences pref = get();
    final SharedPreferences.Editor editor = pref.edit();
    editor.remove(SHOWN_NOTIFICATIONS_INFO_KEY);
    editor.apply();
  }

  private SharedPreferences get() {
    return context.getSharedPreferences(SHARED_PREFS_FILE_NAME, Context.MODE_PRIVATE);
  }
}
