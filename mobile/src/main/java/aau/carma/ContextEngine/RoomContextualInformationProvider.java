package aau.carma.ContextEngine;

import java.util.Arrays;

import aau.carma.recommenders.jayes.BayesNet;
import aau.carma.recommenders.jayes.BayesNode;
import aau.carmakit.Utilities.Optional;

public class RoomContextualInformationProvider implements ContextualInformationProvider {
    private Optional<ContextualInformationListener> listener = new Optional<>();

    @Override
    public void getContext(ContextualInformationListener listener, BayesNet net) {
        this.listener = new Optional<>(listener);

        BayesNode roomNode = net.createNode("room");
        roomNode.addOutcome("living_room");
        roomNode.addOutcome("bedroom");
        roomNode.setProbabilities(1.0 / 2.0, 1.0 / 2.0);

        BayesNode roomActionNode = net.createNode("room_action");
        roomActionNode.addOutcome("music_centre_next_track");
        roomActionNode.addOutcome("music_centre_previous_track");
        roomActionNode.addOutcome("music_centre_play_pause");
        roomActionNode.addOutcome("television_next_channel");
        roomActionNode.addOutcome("television_previous_channel");
        roomActionNode.addOutcome("television_on_off");
        roomActionNode.setParents(Arrays.asList(roomNode));
        roomActionNode.setProbabilities(
                1.0 / 3.0, 1.0 / 3.0, 1.0 / 3.0, 0.0 / 3.0, 0.0 / 3.0, 0.0 / 3.0, // roomNode = living_room
                0.0 / 3.0, 0.0 / 3.0, 0.0 / 3.0, 1.0 / 3.0, 1.0 / 3.0, 1.0 / 3.0  // roomNode = bedroom
        );

        double[] softEvidence = new double[] { 0.8, 0.2 };
        ProvidedContextualInformation contextualInformation = new ProvidedContextualInformation(
                roomActionNode,
                roomNode,
                softEvidence);
        listener.onContextualInformationReady(contextualInformation);
    }

    @Override
    public void cancel() {

    }
}
