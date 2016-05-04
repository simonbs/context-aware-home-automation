package aau.carma.ContextEngine;

import java.util.Arrays;

import aau.carma.recommenders.jayes.BayesNet;
import aau.carma.recommenders.jayes.BayesNode;
import aau.carmakit.Utilities.Optional;

public class GestureContextualInformationProvider implements ContextualInformationProvider {
    private Optional<ContextualInformationListener> listener = new Optional<>();

    @Override
    public void getContext(ContextualInformationListener listener, BayesNet net) {
        this.listener = new Optional<>(listener);

        BayesNode gestureNode = net.createNode("gesture");
        gestureNode.addOutcome("horizontal_line");
        gestureNode.addOutcome("half_circle");
        gestureNode.addOutcome("circle");
        gestureNode.setProbabilities(1.0 / 3.0, 1.0 / 3.0, 1.0 / 3.0);

        BayesNode gestureActionNode = net.createNode("gesture_action");
        gestureActionNode.addOutcome("music_centre_next_track");
        gestureActionNode.addOutcome("music_centre_previous_track");
        gestureActionNode.addOutcome("music_centre_play_pause");
        gestureActionNode.addOutcome("television_next_channel");
        gestureActionNode.addOutcome("television_previous_channel");
        gestureActionNode.addOutcome("television_on_off");
        gestureActionNode.setParents(Arrays.asList(gestureNode));
        gestureActionNode.setProbabilities(
                0.5, 0.0, 0.0, 0.5, 0.0, 0.0, // gestureNode = horizontal_line
                0.0, 0.5, 0.0, 0.0, 0.5, 0.0, // gestureNode = half_circle
                0.0, 0.0, 0.5, 0.0, 0.0, 0.5  // gestureNode = circle
        );

        double[] softEvidence = new double[] { 0.7, 0.2, 0.1 };
        ProvidedContextualInformation contextualInformation = new ProvidedContextualInformation(
                gestureActionNode,
                gestureNode,
                softEvidence);
        listener.onContextualInformationReady(contextualInformation);
    }

    @Override
    public void cancel() {

    }
}
