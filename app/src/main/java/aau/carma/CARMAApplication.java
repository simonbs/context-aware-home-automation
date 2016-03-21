package aau.carma;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.estimote.sdk.EstimoteSDK;

import aau.carma.ContextEngine.ContextRecognizer;
import aau.carma.RESTClient.RequestQueue;

/**
 * Created by simonbs on 08/03/2016.
 */
public class CARMAApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        EstimoteSDK.initialize(getApplicationContext(), Configuration.EstimoteAppId, Configuration.EstimoteAppToken);
        EstimoteSDK.enableDebugLogging(true);

        configureContextRecognizer();
        configureOpenHABClient();
    }

    /**
     * Configures the openHAB client.
     */
    private void configureOpenHABClient() {
        RequestQueue.getInstance().configure(getApplicationContext());
    }

    /**
     * Configures the shared context recognizer.
     */
    private void configureContextRecognizer() {
        Log.v(Configuration.Log, "Will configure context recognizer");

        try {
            CARMAContextRecognizer.getInstance().addPositionContextProvider(getApplicationContext(), DummyData.getAllRooms());
        } catch (ContextRecognizer.IsRecognizingException e) {
            Log.e(Configuration.Log, "The shared context recognizer could not be configured because the recognizer is currently recognizing.");
        }

        Log.v(Configuration.Log, "Did configure context recognizer");
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
