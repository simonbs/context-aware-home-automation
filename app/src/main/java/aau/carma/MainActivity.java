package aau.carma;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.estimote.sdk.SystemRequirementsChecker;

import java.util.ArrayList;

import aau.carma.ContextEngine.ContextOutcome;
import aau.carma.ContextEngine.ContextRecognizer;
import aau.carma.ContextEngine.ContextRecognizerListener;
import aau.carma.ContextProviders.GestureContextProvider;
import aau.carma.ContextProviders.PositionContextProvider;

public class MainActivity extends AppCompatActivity implements ContextRecognizerListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onResume() {
        super.onResume();

        SystemRequirementsChecker.checkWithDefaultDialogs(this);

        // Recognize context after some seconds
        Runnable timeoutRunnable = new Runnable() {
            @Override
            public void run() {
                recognizeContext();
            }
        };

        Handler recognizeHandler = new Handler();
        recognizeHandler.postDelayed(timeoutRunnable, (long) (20 * 1000));
    }

    /**
     * Starts the context recognizer.
     */
    private void recognizeContext() {
        Log.v(Configuration.Log, "Start recognizing context");

        try {
            CARMAContextRecognizer.getInstance().start(this);
        } catch (ContextRecognizer.IsRecognizingException e) {
            Log.e(Configuration.Log, "The context recognizer could not be started because it is already started.");
        }
    }

    @Override
    public void onContextReady(ArrayList<ContextOutcome> outcomes) {
        Log.v(Configuration.Log, "- - - - - - - - - - - - - - - - - -");
        Log.v(Configuration.Log, "Context is ready:");

        if (outcomes.size() > 0) {
            for (ContextOutcome outcome : outcomes) {
                Log.v(Configuration.Log, outcome.id + ": " + outcome.probability);
            }
        }

        Log.v(Configuration.Log, "- - - - - - - - - - - - - - - - - -");
    }

    @Override
    public void onFailedRecognizingContext() {
        Log.v(Configuration.Log, "Recognizing the context has failed");
    }

    @Override
    public void onContextRecognitionTimeout() {
        Log.v(Configuration.Log, "Recognizing the context has timed out");
    }
}
