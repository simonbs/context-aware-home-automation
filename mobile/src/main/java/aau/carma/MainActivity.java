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

//        BayesNet net = new BayesNet();
//
//        BayesNode roomNode = net.createNode("room");
//        roomNode.addOutcome("living_room");
//        roomNode.addOutcome("bedroom");
//        roomNode.setProbabilities(1.0 / 2.0, 1.0 / 2.0);
//
//        BayesNode gestureNode = net.createNode("gesture");
//        gestureNode.addOutcome("horizontal_line");
//        gestureNode.addOutcome("half_circle");
//        gestureNode.addOutcome("circle");
//        gestureNode.setProbabilities(1.0 / 3.0, 1.0 / 3.0, 1.0 / 3.0);
//
//        BayesNode gestureActionNode = net.createNode("gesture_action");
//        gestureActionNode.addOutcome("gesture_music_centre_next_track");
//        gestureActionNode.addOutcome("gesture_music_centre_previous_track");
//        gestureActionNode.addOutcome("gesture_music_centre_play_pause");
//        gestureActionNode.addOutcome("gesture_television_next_channel");
//        gestureActionNode.addOutcome("gesture_television_previous_channel");
//        gestureActionNode.addOutcome("gesture_television_on_off");
//        gestureActionNode.setParents(Arrays.asList(gestureNode));
//        gestureActionNode.setProbabilities(
//                0.5, 0.0, 0.0, 0.5, 0.0, 0.0, // gestureNode = horizontal_line
//                0.0, 0.5, 0.0, 0.0, 0.5, 0.0, // gestureNode = half_circle
//                0.0, 0.0, 0.5, 0.0, 0.0, 0.5  // gestureNode = circle
//        );
//
//        BayesNode roomActionNode = net.createNode("room_action");
//        roomActionNode.addOutcome("room_music_centre_next_track");
//        roomActionNode.addOutcome("room_music_centre_previous_track");
//        roomActionNode.addOutcome("room_music_centre_play_pause");
//        roomActionNode.addOutcome("room_television_next_channel");
//        roomActionNode.addOutcome("room_television_previous_channel");
//        roomActionNode.addOutcome("room_television_on_off");
//        roomActionNode.setParents(Arrays.asList(roomNode));
//        roomActionNode.setProbabilities(
//                1.0/3.0, 1.0/3.0, 1.0/3.0, 0.0/3.0, 0.0/3.0, 0.0/3.0, // roomNode = living_room
//                0.0/3.0, 0.0/3.0, 0.0/3.0, 1.0/3.0, 1.0/3.0, 1.0/3.0  // roomNode = bedroom
//        );
//
//        BayesNode actionNode = net.createNode("action");
//        actionNode.addOutcome("music_centre_next_track");
//        actionNode.addOutcome("music_centre_previous_track");
//        actionNode.addOutcome("music_centre_play_pause");
//        actionNode.addOutcome("television_next_channel");
//        actionNode.addOutcome("television_previous_channel");
//        actionNode.addOutcome("television_on_off");
//        actionNode.setParents(Arrays.asList(gestureActionNode, roomActionNode));
//        actionNode.setProbabilities(
//                // gestureActionNode == music_centre_next_track
//                2.0 / 2.0, 0.0 / 2.0, 0.0 / 2.0, 0.0 / 2.0, 0.0 / 2.0, 0.0 / 2.0, // roomActionNode = music_centre_next_track
//                1.0 / 2.0, 1.0 / 2.0, 0.0 / 2.0, 0.0 / 2.0, 0.0 / 2.0, 0.0 / 2.0, // roomActionNode = music_centre_previous_track
//                1.0 / 2.0, 0.0 / 2.0, 1.0 / 2.0, 0.0 / 2.0, 0.0 / 2.0, 0.0 / 2.0, // roomActionNode = music_centre_play_pause
//                1.0 / 2.0, 0.0 / 2.0, 0.0 / 2.0, 1.0 / 2.0, 0.0 / 2.0, 0.0 / 2.0, // roomActionNode = television_next_channel
//                1.0 / 2.0, 0.0 / 2.0, 0.0 / 2.0, 0.0 / 2.0, 1.0 / 2.0, 0.0 / 2.0, // roomActionNode = television_previous_channel
//                1.0 / 2.0, 0.0 / 2.0, 0.0 / 2.0, 0.0 / 2.0, 0.0 / 2.0, 1.0 / 2.0, // roomActionNode = television_on_off
//                // gestureActionNode == music_centre_previous_track
//                1.0 / 2.0, 1.0 / 2.0, 0.0 / 2.0, 0.0 / 2.0, 0.0 / 2.0, 0.0 / 2.0, // roomActionNode = music_centre_next_track
//                0.0 / 2.0, 2.0 / 2.0, 0.0 / 2.0, 0.0 / 2.0, 0.0 / 2.0, 0.0 / 2.0, // roomActionNode = music_centre_previous_track
//                0.0 / 2.0, 1.0 / 2.0, 1.0 / 2.0, 0.0 / 2.0, 0.0 / 2.0, 0.0 / 2.0, // roomActionNode = music_centre_play_pause
//                0.0 / 2.0, 1.0 / 2.0, 0.0 / 2.0, 1.0 / 2.0, 0.0 / 2.0, 0.0 / 2.0, // roomActionNode = television_next_channel
//                0.0 / 2.0, 1.0 / 2.0, 0.0 / 2.0, 0.0 / 2.0, 1.0 / 2.0, 0.0 / 2.0, // roomActionNode = television_previous_channel
//                0.0 / 2.0, 1.0 / 2.0, 0.0 / 2.0, 0.0 / 2.0, 0.0 / 2.0, 1.0 / 2.0, // roomActionNode = television_on_off
//                // gestureActionNode == music_centre_play_pause
//                1.0 / 2.0, 0.0 / 2.0, 1.0 / 2.0, 0.0 / 2.0, 0.0 / 2.0, 0.0 / 2.0, // roomActionNode = music_centre_next_track
//                0.0 / 2.0, 1.0 / 2.0, 1.0 / 2.0, 0.0 / 2.0, 0.0 / 2.0, 0.0 / 2.0, // roomActionNode = music_centre_previous_track
//                0.0 / 2.0, 0.0 / 2.0, 2.0 / 2.0, 0.0 / 2.0, 0.0 / 2.0, 0.0 / 2.0, // roomActionNode = music_centre_play_pause
//                0.0 / 2.0, 0.0 / 2.0, 1.0 / 2.0, 1.0 / 2.0, 0.0 / 2.0, 0.0 / 2.0, // roomActionNode = television_next_channel
//                0.0 / 2.0, 0.0 / 2.0, 1.0 / 2.0, 0.0 / 2.0, 1.0 / 2.0, 0.0 / 2.0, // roomActionNode = television_previous_channel
//                0.0 / 2.0, 0.0 / 2.0, 1.0 / 2.0, 0.0 / 2.0, 0.0 / 2.0, 1.0 / 2.0, // roomActionNode = television_on_off
//                // gestureActionNode == television_next_channel
//                1.0 / 2.0, 0.0 / 2.0, 0.0 / 2.0, 1.0 / 2.0, 0.0 / 2.0, 0.0 / 2.0, // roomActionNode = music_centre_next_track
//                0.0 / 2.0, 1.0 / 2.0, 0.0 / 2.0, 1.0 / 2.0, 0.0 / 2.0, 0.0 / 2.0, // roomActionNode = music_centre_previous_track
//                0.0 / 2.0, 0.0 / 2.0, 1.0 / 2.0, 1.0 / 2.0, 0.0 / 2.0, 0.0 / 2.0, // roomActionNode = music_centre_play_pause
//                0.0 / 2.0, 0.0 / 2.0, 0.0 / 2.0, 2.0 / 2.0, 0.0 / 2.0, 0.0 / 2.0, // roomActionNode = television_next_channel
//                0.0 / 2.0, 0.0 / 2.0, 0.0 / 2.0, 1.0 / 2.0, 1.0 / 2.0, 0.0 / 2.0, // roomActionNode = television_previous_channel
//                0.0 / 2.0, 0.0 / 2.0, 0.0 / 2.0, 1.0 / 2.0, 0.0 / 2.0, 1.0 / 2.0, // roomActionNode = television_on_off
//                // gestureActionNode == television_previous_channel
//                1.0 / 2.0, 0.0 / 2.0, 0.0 / 2.0, 0.0 / 2.0, 1.0 / 2.0, 0.0 / 2.0, // roomActionNode = music_centre_next_track
//                0.0 / 2.0, 1.0 / 2.0, 0.0 / 2.0, 0.0 / 2.0, 1.0 / 2.0, 0.0 / 2.0, // roomActionNode = music_centre_previous_track
//                0.0 / 2.0, 0.0 / 2.0, 1.0 / 2.0, 0.0 / 2.0, 1.0 / 2.0, 0.0 / 2.0, // roomActionNode = music_centre_play_pause
//                0.0 / 2.0, 0.0 / 2.0, 0.0 / 2.0, 1.0 / 2.0, 1.0 / 2.0, 0.0 / 2.0, // roomActionNode = television_next_channel
//                0.0 / 2.0, 0.0 / 2.0, 0.0 / 2.0, 0.0 / 2.0, 2.0 / 2.0, 0.0 / 2.0, // roomActionNode = television_previous_channel
//                0.0 / 2.0, 0.0 / 2.0, 0.0 / 2.0, 0.0 / 2.0, 1.0 / 2.0, 1.0 / 2.0, // roomActionNode = television_on_off
//                // gestureActionNode == television_on_off
//                1.0 / 2.0, 0.0 / 2.0, 0.0 / 2.0, 0.0 / 2.0, 0.0 / 2.0, 1.0 / 2.0, // roomActionNode = music_centre_next_track
//                0.0 / 2.0, 1.0 / 2.0, 0.0 / 2.0, 0.0 / 2.0, 0.0 / 2.0, 1.0 / 2.0, // roomActionNode = music_centre_previous_track
//                0.0 / 2.0, 0.0 / 2.0, 1.0 / 2.0, 0.0 / 2.0, 0.0 / 2.0, 1.0 / 2.0, // roomActionNode = music_centre_play_pause
//                0.0 / 2.0, 0.0 / 2.0, 0.0 / 2.0, 1.0 / 2.0, 0.0 / 2.0, 1.0 / 2.0, // roomActionNode = television_next_channel
//                0.0 / 2.0, 0.0 / 2.0, 0.0 / 2.0, 0.0 / 2.0, 1.0 / 2.0, 1.0 / 2.0, // roomActionNode = television_previous_channel
//                0.0 / 2.0, 0.0 / 2.0, 0.0 / 2.0, 0.0 / 2.0, 0.0 / 2.0, 2.0 / 2.0  // roomActionNode = television_on_off
//        );
//
//        SoftEvidenceInferrer inference = new SoftEvidenceInferrer(new JunctionTreeAlgorithm());
//        inference.setNetwork(net);
//        // Living room, bedroom
//        inference.addSoftEvidence(roomNode, new double[]{ 0.8, 0.2 });
//        // Horizontal line, half circle, circle
//        inference.addSoftEvidence(gestureNode, new double[]{0.7, 0.2, 0.1});
//
//        double[] gestureBeliefs = inference.getBeliefs(net.getNode("gesture"));
//        Logger.verbose("Gesture beliefs:");
//        for (int i = 0; i < gestureBeliefs.length; i++) {
//            Logger.verbose(" - " + net.getNode("gesture").getOutcomeName(i) + ": " + gestureBeliefs[i] * 100);
//        }
//
//        double[] roomBeliefs = inference.getBeliefs(net.getNode("room"));
//        Logger.verbose("Room beliefs:");
//        for (int i = 0; i < roomBeliefs.length; i++) {
//            Logger.verbose(" - " + net.getNode("room").getOutcomeName(i) + ": " + roomBeliefs[i] * 100);
//        }
//
//        double[] gestureActionBeliefs = inference.getBeliefs(net.getNode("gesture_action"));
//        Logger.verbose("Gesture action beliefs:");
//        for (int i = 0; i < gestureActionBeliefs.length; i++) {
//            Logger.verbose(" - " + net.getNode("gesture_action").getOutcomeName(i) + ": " + gestureActionBeliefs[i] * 100);
//        }
//
//        double[] roomActionBeliefs = inference.getBeliefs(net.getNode("room_action"));
//        Logger.verbose("Room action beliefs:");
//        for (int i = 0; i < roomActionBeliefs.length; i++) {
//            Logger.verbose(" - " + net.getNode("room_action").getOutcomeName(i) + ": " + roomActionBeliefs[i] * 100);
//        }
//
//        double[] actionBeliefs = inference.getBeliefs(net.getNode("action"));
//        Logger.verbose("Action beliefs:");
//        for (int i = 0; i < actionBeliefs.length; i++) {
//            Logger.verbose(" - " + net.getNode("action").getOutcomeName(i) + ": " + actionBeliefs[i] * 100);
//        }
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
