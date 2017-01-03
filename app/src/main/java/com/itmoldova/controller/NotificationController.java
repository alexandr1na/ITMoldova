package com.itmoldova.controller;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.RingtoneManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.BigPictureStyle;
import android.support.v4.app.NotificationCompat.BigTextStyle;
import android.support.v4.app.NotificationCompat.InboxStyle;
import android.support.v4.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.itmoldova.Extra;
import com.itmoldova.R;
import com.itmoldova.detail.DetailActivity;
import com.itmoldova.list.MainActivity;
import com.itmoldova.model.Item;
import com.itmoldova.parser.ContentParser;

import java.util.List;
import java.util.Random;

/**
 * Helper class that shows a status bar notification in different styles
 * depending on the number of new articles published.
 * <p>
 * <pre>
 * One article:
 *        if the article contains an image then use {@link BigPictureStyle}
 *        otherwise use {@link BigTextStyle}.
 *
 * More than one article:
 *        display multiline notification using {@link InboxStyle}.
 * </pre>
 */
public class NotificationController {

    private static final int MULTILINE_NOTIFICATION_ID = 1;
    private static final int NOTIFICATION_IMAGE_WIDTH = 400;
    private static final int NOTIFICATION_IMAGE_HEIGHT = 250;

    private Context context;
    private NotificationManager notificationManager;

    private enum NotificationType {
        MULTILINE,
        BIG_IMAGE,
        BIG_TEXT
    }

    public NotificationController(Context context) {
        this.context = context;
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public void showNotification(List<Item> items) {
        NotificationType type = detectNotificationTypeToShow(items);
        switch (type) {
            case MULTILINE:
                showMultilineNotification(items);
                break;
            case BIG_IMAGE:
                showBigImageNotification(items);
                break;
            case BIG_TEXT:
                showBigTextNotification(items);
                break;
        }
    }

    private NotificationType detectNotificationTypeToShow(List<Item> items) {
        if (items.size() > 1) {
            return NotificationType.MULTILINE;
        }

        boolean hasImage = ContentParser.extractFirstImage(items.get(0).getContent()) != null;
        if (hasImage) {
            return NotificationType.BIG_IMAGE;
        } else {
            return NotificationType.BIG_TEXT;
        }
    }

    private void showMultilineNotification(List<Item> items) {
        Item firstItem = items.get(0);

        InboxStyle inboxStyle = new InboxStyle();
        inboxStyle.setBigContentTitle(items.size() + " " + context.getString(R.string.new_articles));
        for (Item item : items) {
            inboxStyle.addLine(item.getTitle());
        }

        NotificationCompat.Builder builder = createBaseBuilder(firstItem.getTitle(), firstItem.getDescription());
        builder.setStyle(inboxStyle);
        builder.setContentIntent(createMainActivityPendingIntent());
        notificationManager.notify(MULTILINE_NOTIFICATION_ID, builder.build());
    }

    private void showBigTextNotification(List<Item> items) {
        Item firstItem = items.get(0);

        BigTextStyle textStyle = new BigTextStyle();
        textStyle.setBigContentTitle(firstItem.getTitle());
        textStyle.bigText(firstItem.getDescription());

        NotificationCompat.Builder builder = createBaseBuilder(firstItem.getTitle(), firstItem.getDescription());
        builder.setStyle(textStyle);
        builder.setContentIntent(createDetailActivityPendingIntent(firstItem));
        notificationManager.notify(generateId(), builder.build());
    }

    private void showBigImageNotification(List<Item> items) {
        Item firstItem = items.get(0);
        Glide.with(context)
                .load(ContentParser.extractFirstImage(firstItem.getContent()))
                .asBitmap()
                .into(new SimpleTarget<Bitmap>(NOTIFICATION_IMAGE_WIDTH, NOTIFICATION_IMAGE_HEIGHT) {
                    @Override
                    public void onResourceReady(Bitmap bitmap, GlideAnimation<? super Bitmap> glideAnimation) {
                        BigPictureStyle pictureStyle = new BigPictureStyle();
                        pictureStyle.setBigContentTitle(firstItem.getTitle());
                        pictureStyle.setSummaryText(firstItem.getDescription());
                        pictureStyle.bigPicture(bitmap);

                        NotificationCompat.Builder builder = createBaseBuilder(firstItem.getTitle(), firstItem.getDescription());
                        builder.setStyle(pictureStyle);
                        builder.setContentIntent(createDetailActivityPendingIntent(firstItem));
                        notificationManager.notify(generateId(), builder.build());
                    }
                });
    }

    private NotificationCompat.Builder createBaseBuilder(String title, String description) {
        return new NotificationCompat.Builder(context)
                .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
                .setSmallIcon(R.drawable.ic_menu_gallery)
                .setContentTitle(title)
                .setContentText(description)
                .setAutoCancel(true)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
    }

    private PendingIntent createMainActivityPendingIntent() {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return PendingIntent.getActivity(context, generateId(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private PendingIntent createDetailActivityPendingIntent(Item item) {
        Intent intent = new Intent(context, DetailActivity.class);
        intent.putExtra(Extra.ITEM, item);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return PendingIntent.getActivity(context, generateId(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private int generateId() {
        return new Random().nextInt(Integer.MAX_VALUE);
    }

}
