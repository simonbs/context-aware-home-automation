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
package aau.carmakit.ContextEngine.Recommender.jayes.sampling;

import java.util.Map;

import aau.carmakit.ContextEngine.Recommender.jayes.BayesNet;
import aau.carmakit.ContextEngine.Recommender.jayes.BayesNode;

public interface ISampler {

    /**
     * @deprecated still here for compatibility reasons, but for the sake of uniformity, setNetwork should be used
     */
    @Deprecated
    void setBN(BayesNet net);

    void setNetwork(BayesNet net);

    void setEvidence(Map<BayesNode, String> evidence);

    Map<BayesNode, String> sample();

    void seed(long seed);
}
