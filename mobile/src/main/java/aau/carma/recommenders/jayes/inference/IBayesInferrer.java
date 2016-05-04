/**
 * Copyright (c) 2011 Michael Kutschke.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Michael Kutschke - initial API and implementation.
 */
package aau.carma.recommenders.jayes.inference;

import java.util.Map;

import aau.carma.recommenders.jayes.BayesNet;
import aau.carma.recommenders.jayes.BayesNode;

public interface IBayesInferrer {

    void setNetwork(BayesNet bayesNet);

    void setEvidence(Map<BayesNode, String/* outcome */> evidence);

    void addEvidence(BayesNode node, String outcome);

    Map<BayesNode, String> getEvidence();

    void setVirtualEvidence(Map<BayesNode, double[]> virtualEvidence);

    void addVirtualEvidence(BayesNode node, double[] virtualEvidence);

    Map<BayesNode, double[]> getVirtualEvidence();

    double[] getBeliefs(BayesNode node);
}
