package aau.carma;

import android.app.Application;
import android.content.Context;

import com.estimote.sdk.EstimoteSDK;

import aau.carmakit.Configuration;
import aau.carmakit.RESTClient.RequestQueue;

/**
 * Android Wear application.
 */
public class App extends Application {
    /**
     * This context.
     */
    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        configureEstimote();
        configureOpenHABClient();
    }

    /**
     * Applications context. Makes the context accessible from anywhere.
     * @return Applications context.
     */
    public static Context getContext(){
        return context;
    }

    /**
     * Configures the Estimote SDK.
     */
    private void configureEstimote() {
        EstimoteSDK.initialize(this, Configuration.EstimoteAppId, Configuration.EstimoteAppToken);
        EstimoteSDK.enableDebugLogging(true);
    }

    /**
     * Configures the openHAB client.
     */
    private void configureOpenHABClient() {
        RequestQueue.getInstance().configure(getApplicationContext());
    }
}