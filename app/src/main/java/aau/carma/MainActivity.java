package aau.carma;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.estimote.sdk.SystemRequirementsChecker;

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
    }

    @Override
    public void onContextReady(ContextOutcome[] outcomes) {
        Log.v(Configuration.Log, "Did recognize the context");
    }

    @Override
    public void onFailedRecognizingContext() {
        Log.v(Configuration.Log, "Reconizing the context has failed");
    }

    @Override
    public void onContextRecognitionTimeout() {
        Log.v(Configuration.Log, "Reconizing the context has timed out");
    }
}
