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
        fault("Exception while showing notification.", e, intent, notificationDetails);
        return;
      }

      try {
        FlutterLocalNotificationsPlugin.scheduleNextNotification(context, notificationDetails);
      } catch (Exception e) {
        fault("Exception while preparing next notification.", e, intent, notificationDetails);
        return;
      }
    }
  }

  private void fault(String message, Intent intent) {
    fault(message, null, intent, null);
  }

  private void fault(
      String message, Exception e, Intent intent, NotificationDetails notificationDetails) {
    Bundle bundle = intent.getExtras();
    StringBuilder sb = new StringBuilder();

    if (notificationDetails != null) {
      sb.append("{\n");
      writeData(sb, "id", notificationDetails.id);
      writeData(sb, "number", notificationDetails.number);
      writeData(sb, "importance", notificationDetails.importance);
      writeData(sb, "priority", notificationDetails.priority);
      writeData(sb, "title", notificationDetails.title);
      writeData(sb, "body", notificationDetails.body);
      writeData(sb, "subText", notificationDetails.subText);
      writeData(sb, "payload", notificationDetails.payload);
      writeData(sb, "category", notificationDetails.category);
      writeData(sb, "tag", notificationDetails.tag);
      writeData(sb, "icon", notificationDetails.icon);
      writeData(sb, "style", notificationDetails.style);
      writeData(sb, "styleInformation", notificationDetails.styleInformation);
      writeData(sb, "channelId", notificationDetails.channelId);
      writeData(sb, "channelName", notificationDetails.channelName);
      writeData(sb, "channelDescription", notificationDetails.channelDescription);
      writeData(sb, "playSound", notificationDetails.playSound);
      writeData(sb, "silent", notificationDetails.silent);
      writeData(sb, "timeZoneName", notificationDetails.timeZoneName);
      writeData(
          sb,
          "scheduledNotificationRepeatFrequency",
          notificationDetails.scheduledNotificationRepeatFrequency);
      writeData(sb, "repeatTime", notificationDetails.repeatTime);
      writeData(sb, "day", notificationDetails.day);
      writeData(sb, "when", notificationDetails.when);
      writeData(sb, "showWhen", notificationDetails.showWhen);
      writeData(sb, "scheduledDateTime", notificationDetails.scheduledDateTime);
      writeData(sb, "inexactWindowLengthMillis", notificationDetails.inexactWindowLengthMillis);
      writeData(sb, "groupKey", notificationDetails.groupKey);
      writeData(sb, "setAsGroupSummary", notificationDetails.setAsGroupSummary);
      writeData(sb, "groupAlertBehavior", notificationDetails.groupAlertBehavior);
      writeData(sb, "autoCancel", notificationDetails.autoCancel);
      writeData(sb, "customLayoutLegacyName", notificationDetails.customLayoutLegacyName);
      writeData(sb, "customLayoutCollapsedName", notificationDetails.customLayoutCollapsedName);
      writeData(sb, "iconResourceId", notificationDetails.iconResourceId);
      sb.append("}\n");
    } else if (bundle != null) {
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
    msg.append(notificationDetails != null ? "NotificationDetails: " : "Intent extras: ");
    msg.append(sb);

    if (e != null) {
      StringWriter errors = new StringWriter();
      e.printStackTrace(new PrintWriter(errors));
      msg.append("\n").append("Exception Stack trace:\n").append(errors);
    }

    throw new RuntimeException(msg.toString());
  }

  private void writeData(StringBuilder sb, String fieldName, Object data) {
    sb.append(fieldName).append(" : ").append(data).append("\n");
  }
}
