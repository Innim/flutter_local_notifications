// ignore_for_file: always_specify_types

import 'package:flutter_local_notifications_plus/flutter_local_notifications_plus.dart';
import 'package:flutter_local_notifications_plus/src/platform_specifics/darwin/mappers.dart';
import 'package:flutter_test/flutter_test.dart';

void main() {
  group('DarwinNotificationActionMapper', () {
    test('should map fields and options correctly', () {
      expect(
        DarwinNotificationAction.plain('test_id', 'Something', options: {
          DarwinNotificationActionOption.authenticationRequired,
          DarwinNotificationActionOption.destructive,
          DarwinNotificationActionOption.foreground,
        }).toMap(),
        {
          'identifier': 'test_id',
          'title': 'Something',
          'options': [1, 2, 4],
          'type': 'plain',
        },
      );
    });
  });

  group('DarwinNotificationCategoryMapper', () {
    test('should map fields and options correctly', () {
      expect(
        const DarwinNotificationCategory('test_id', options: {
          DarwinNotificationCategoryOption.customDismissAction,
          DarwinNotificationCategoryOption.allowInCarPlay,
          DarwinNotificationCategoryOption.hiddenPreviewShowTitle,
          DarwinNotificationCategoryOption.hiddenPreviewShowSubtitle,
          DarwinNotificationCategoryOption.allowAnnouncement,
        }).toMap(),
        {
          'identifier': 'test_id',
          'actions': [],
          'options': [1, 2, 4, 8, 16],
        },
      );
    });
  });
}
