package aau.carma;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.Arrays;

import aau.carma.ContextEngine.ContextRecognizer;
import aau.carma.ContextEngine.GestureContextualInformationProvider;
import aau.carma.ContextEngine.RoomContextualInformationProvider;
import aau.carma.recommenders.jayes.BayesNet;
import aau.carma.recommenders.jayes.BayesNode;
import aau.carma.recommenders.jayes.inference.SoftEvidenceInferrer;
import aau.carma.recommenders.jayes.inference.jtree.JunctionTreeAlgorithm;
import aau.carmakit.ContextEngine.ContextOutcome;
import aau.carmakit.ContextEngine.ContextRecognizerListener;
import aau.carmakit.Utilities.Logger;

public class MainActivity extends AppCompatActivity implements ContextRecognizerListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ContextRecognizer contextRecognizer = new ContextRecognizer();

        try {
            contextRecognizer.addProvider(new RoomContextualInformationProvider());
            contextRecognizer.addProvider(new GestureContextualInformationProvider());
        } catch (ContextRecognizer.IsRecognizingException e) {
            e.printStackTrace();
        }

        try {
            contextRecognizer.start(this);
        } catch (ContextRecognizer.IsRecognizingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onContextReady(ArrayList<ContextOutcome> outcomes) {

    }

    @Override
    public void onFailedRecognizingContext() {

    }

    @Override
    public void onContextRecognitionTimeout() {

    }
}
