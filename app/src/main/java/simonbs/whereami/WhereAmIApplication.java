package simonbs.whereami;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.EstimoteSDK;
import com.estimote.sdk.Region;

import java.util.List;

/**
 * Created by simonbs on 08/03/2016.
 */
public class WhereAmIApplication extends Application {
    private BeaconManager beaconManager;

    @Override
    public void onCreate() {
        super.onCreate();

        EstimoteSDK.initialize(getApplicationContext(), Configuration.EstimoteAppId, Configuration.EstimoteAppToken);
        EstimoteSDK.enableDebugLogging(true);

        configureBeaconManager();
    }

    private void configureBeaconManager() {
        beaconManager = new BeaconManager(getApplicationContext());

        // Monitor a set of regions
        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                // Monitor all configured regions
                for (Room room : Configuration.Rooms) {
                    Log.v("WhereAmIApplication", "Start monitoring room with identifier " + room.getIdentifier());
                    beaconManager.startMonitoring(room.toRegion());
                }
            }
        });

        // React to change in regions
        beaconManager.setMonitoringListener(new BeaconManager.MonitoringListener() {
            @Override
            public void onEnteredRegion(Region region, List<Beacon> list) {
                Log.v("WhereAmIApplication", "Did enter region with identifier " + region.getIdentifier());
                showNotification("Hello there!", "You are now in the " + region.getIdentifier() + " region!");
            }

            @Override
            public void onExitedRegion(Region region) {
                Log.v("WhereAmIApplication", "Did exit region with identifier " + region.getIdentifier());
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
