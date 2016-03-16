package aau.carma;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.estimote.sdk.EstimoteSDK;

/**
 * Created by simonbs on 08/03/2016.
 */
public class CARMAApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        EstimoteSDK.initialize(getApplicationContext(), Configuration.EstimoteAppId, Configuration.EstimoteAppToken);
        EstimoteSDK.enableDebugLogging(true);

        configureRoomsManager();
    }

    private void configureRoomsManager() {
        RoomsManager.getInstance().configureWithRooms(new Room[]{
                new Room("kitchen",
                        "Kitchen",
                        new Beacon[]{
                                new Beacon(Configuration.BeaconIce2Namespace, Configuration.BeaconIce2Instance)
                        }),
                new Room("desk",
                        "Desk",
                        new Beacon[]{
                                new Beacon(Configuration.BeaconBlueberry3Namespace, Configuration.BeaconBlueberry3Instance)
                        }),
                new Room("living_room",
                        "Living Room",
                        new Beacon[]{
                                new Beacon(Configuration.BeaconMint3Namespace, Configuration.BeaconMint3Instance)
                        }),
                new Room("bathroom",
                        "Bathroom",
                        new Beacon[]{
                                new Beacon(Configuration.BeaconIce3Namespace, Configuration.BeaconIce3Instance)
                        })
        });

        RoomsManager.getInstance().startMonitoring(getApplicationContext(), new RoomsManager.EventListener() {
            @Override
            public void onDidEnterRoom(Room room) {
                Intent intent = new Intent(Notifications.DidEnterRoom);
                intent.putExtra(Notifications.Extras.Room, room);
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
            }

            @Override
            public void onDidLeaveRoom() {
                Intent intent = new Intent(Notifications.DidLeaveRoom);
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
            }
        });
    }

    public void showNotification(String title, String message) {
        Intent notifyIntent = new Intent(this, MainActivity.class);
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivities(this, 0, new Intent[] { notifyIntent }, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification = new Notification.Builder(this)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build();
        notification.defaults |= Notification.DEFAULT_SOUND;
        NotificationManager notificationManager =  (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, notification);
    }
}
