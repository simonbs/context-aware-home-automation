/*******************************************************************************
 * Copyright (c) 2014 Michael Kutschke.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Michael Kutschke - initial API and implementation
 ******************************************************************************/

package aau.carma.recommenders.jayes.inference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import aau.carma.recommenders.jayes.BayesNet;
import aau.carma.recommenders.jayes.BayesNode;
import aau.carma.recommenders.jayes.util.MathUtils;
import aau.carmakit.Utilities.Logger;

/**
 * Implements an iterative soft evidence algorithm from Rong Pan, Yun Peng and Zhongli Ding
 * "Belief Update in Bayesian Networks Using Uncertain Evidence" (2006)
 *
 */
public class SoftEvidenceInferrer implements IBayesInferrer {

    private IBayesInferrer delegate;
    private boolean beliefsValid = false;
    private Map<BayesNode, double[]> softEvidence = new HashMap<BayesNode, double[]>();

    public SoftEvidenceInferrer(IBayesInferrer delegate) {
        this.delegate = delegate;

    }

    @Override
    public void setNetwork(BayesNet bayesNet) {
        delegate.setNetwork(bayesNet);
        beliefsValid = false;
    }

    @Override
    public void setEvidence(Map<BayesNode, String> evidence) {
        delegate.setEvidence(evidence);
        beliefsValid = false;
    }

    @Override
    public void addEvidence(BayesNode node, String outcome) {
        delegate.addEvidence(node, outcome);
        beliefsValid = false;
    }

    @Override
    public Map<BayesNode, String> getEvidence() {
        return delegate.getEvidence();
    }

    @Override
    public void setVirtualEvidence(Map<BayesNode, double[]> virtualEvidence) {
        delegate.setVirtualEvidence(virtualEvidence);
        beliefsValid = false;
    }

    @Override
    public void addVirtualEvidence(BayesNode node, double[] virtualEvidence) {
        delegate.addVirtualEvidence(node, virtualEvidence);
        beliefsValid = false;

    }

    @Override
    public Map<BayesNode, double[]> getVirtualEvidence() {
        return delegate.getVirtualEvidence();
    }

    public void setSoftEvidence(Map<BayesNode, double[]> softEvidence) {
        this.softEvidence = softEvidence;
    }

    public void addSoftEvidence(BayesNode node, double[] softEvidence) {
        this.softEvidence.put(node, softEvidence);
    }

    @Override
    public double[] getBeliefs(BayesNode node) {
        if (!beliefsValid) {
            updateBeliefs();
            beliefsValid = true;
        }
        return delegate.getBeliefs(node);
    }

    private void updateBeliefs() {
        List<Entry<BayesNode, double[]>> softEv = new ArrayList<Entry<BayesNode, double[]>>(softEvidence.entrySet());

        Logger.verbose("Will update beliefs");

        int noChanges = 0;
        int k = 0;
        while (noChanges < softEv.size()) {
//            Logger.verbose(noChanges + " < " + softEv.size());

            int i = k % softEv.size();

            Entry<BayesNode, double[]> se = softEv.get(i);
            double[] p = delegate.getBeliefs(se.getKey());

            double[] ve = new double[p.length];

            MathUtils.secureDivide(se.getValue(), p, ve); // P(Vi| se) / P(Vi)

            double[] factor = delegate.getVirtualEvidence().get(se.getKey());

            if (factor != null) {
                MathUtils.multiply(factor, ve, ve);
            }

            if (factor != null && Arrays.equals(factor, ve)) {
//                Logger.verbose("Increment noChanges");
                noChanges++;
            } else {
                delegate.addVirtualEvidence(se.getKey(), ve);
//                Logger.verbose("Reset noChanges");
                noChanges = 0;
            }

            k++;
        }

        Logger.verbose("Did update beliefs");
    }

}
