package aau.carmakit.ContextEngine;

import aau.carmakit.ContextEngine.Recommender.jayes.BayesNode;

public class ProvidedContextualInformation {
    /**
     * Node to be parent for the action node.
     */
    public final BayesNode actionParentNode;

    /**
     * Node to apply evidence to.
     */
    public final BayesNode evidenceNode;

    /**
     * Soft evidence to apply to evidenceNode.
     */
    public final double[] softEvidence;

    /**
     * Initializes provided contextual information.
     * @param actionParentNode Node to be parent for the action node.
     * @param evidenceNode Node to apply evidence to.
     * @param softEvidence Evidence to apply to evidenceNode.
     */
    public ProvidedContextualInformation(BayesNode actionParentNode, BayesNode evidenceNode, double[] softEvidence) {
        this.actionParentNode = actionParentNode;
        this.evidenceNode = evidenceNode;
        this.softEvidence = softEvidence;
    }
}
