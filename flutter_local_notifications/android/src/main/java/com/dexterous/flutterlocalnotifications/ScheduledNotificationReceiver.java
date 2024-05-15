package com.dexterous.flutterlocalnotifications;

import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.os.Bundle;

import androidx.annotation.Keep;
import androidx.core.app.NotificationManagerCompat;

import com.dexterous.flutterlocalnotifications.models.NotificationDetails;
import com.dexterous.flutterlocalnotifications.utils.StringUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** Created by michaelbui on 24/3/18. */
@Keep
public class ScheduledNotificationReceiver extends BroadcastReceiver {

  private static final String TAG = "ScheduledNotifReceiver";

  @Override
  @SuppressWarnings("deprecation")
  public void onReceive(final Context context, Intent intent) {
    String notificationDetailsJson =
        intent.getStringExtra(FlutterLocalNotificationsPlugin.NOTIFICATION_DETAILS);
    if (StringUtils.isNullOrEmpty(notificationDetailsJson)) {
      // This logic is needed for apps that used the plugin prior to 0.3.4

      Notification notification;
      int notificationId = intent.getIntExtra("notification_id", 0);

      if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
        notification = intent.getParcelableExtra("notification", Notification.class);
      } else {
        notification = intent.getParcelableExtra("notification");
      }

      if (notification == null) {
        // This means the notification is corrupt
        FlutterLocalNotificationsPlugin.removeNotificationFromCache(context, notificationId);
        Log.e(TAG, "Failed to parse a notification from  Intent. ID: " + notificationId);
        fault("Notification is null - invalid data.", intent);
        return;
      }

      notification.when = System.currentTimeMillis();
      NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
      notificationManager.notify(notificationId, notification);
      boolean repeat = intent.getBooleanExtra("repeat", false);
      if (!repeat) {
        FlutterLocalNotificationsPlugin.removeNotificationFromCache(context, notificationId);
      }
    } else {
      Gson gson = FlutterLocalNotificationsPlugin.buildGson();
      Type type = new TypeToken<NotificationDetails>() {}.getType();
      NotificationDetails notificationDetails = gson.fromJson(notificationDetailsJson, type);

      if (notificationDetails == null) {
        fault("NotificationDetails is null - gson.fromJson can't parse it.", intent);
        return;
      }

      try {
        FlutterLocalNotificationsPlugin.showNotification(context, notificationDetails);
      } catch (Exception e) {

        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

        LocalDateTime scheduledDateTime =
            LocalDateTime.parse(notificationDetails.scheduledDateTime, formatter);

        LocalDateTime currentDateTime = LocalDateTime.now();

        // TODO: временное решение, удалить как все починим!
        // Проблема: у части давних пользователей остались очень древние оповещения,
        // которые планировались по старому методу, который сейчас вызывает ошибку при показе.
        // Если мы ловим ошибку при показе именно такого старого оповещения, то отменяем его.
        if (scheduledDateTime.isBefore(currentDateTime.minusYears(1))) {
          FlutterLocalNotificationsPlugin.cancelByNotificationDetails(context, notificationDetails);
          fault("Wrong notification! Date older than a year.", intent);
        } else {
          fault("Exception while showing notification.", e, intent);
        }
        return;
      }

      try {
        FlutterLocalNotificationsPlugin.scheduleNextNotification(context, notificationDetails);
      } catch (Exception e) {
        fault("Exception while preparing next notification.", e, intent);
        return;
      }
    }
  }

  private void fault(String message, Intent intent) {
    fault(message, null, intent);
  }

  private void fault(String message, Exception e, Intent intent) {
    Bundle bundle = intent.getExtras();
    StringBuilder sb = new StringBuilder();
    if (bundle != null) {
      sb.append("{\n");
      for (String key : bundle.keySet()) {
        sb.append("\t")
            .append(key)
            .append(" : ")
            .append(bundle.get(key) != null ? bundle.get(key) : "NULL");
        sb.append("\n");
      }
      sb.append("}");
    } else {
      sb.append("NULL");
    }

    StringBuilder msg = new StringBuilder(message);
    msg.append("\n");
    if (e != null) {
      msg.append("Exception: ").append(e).append("\n");
    }
    msg.append("Intent extras: ");
    msg.append(sb);

    if (e != null) {
      StringWriter errors = new StringWriter();
      e.printStackTrace(new PrintWriter(errors));
      msg.append("\n").append("Exception Stack trace:\n").append(errors);
    }

    throw new RuntimeException(msg.toString());
  }
}
