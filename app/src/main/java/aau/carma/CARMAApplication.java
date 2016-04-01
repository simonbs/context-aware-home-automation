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

        configureOpenHABClient();
    }

    /**
     * Configures the openHAB client.
     */
    private void configureOpenHABClient() {
        RequestQueue.getInstance().configure(getApplicationContext());
    }
}
