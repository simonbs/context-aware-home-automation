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
package aau.carmakit.ContextEngine.Recommender.jayes.inference.jtree;

import java.util.List;

import aau.carmakit.ContextEngine.Recommender.jayes.util.Graph;
import aau.carmakit.ContextEngine.Recommender.jayes.util.OrderIgnoringPair;
import aau.carmakit.ContextEngine.Recommender.jayes.util.Pair;

public class JunctionTree {

    private List<List<Integer>> clusters;
    private List<Pair<OrderIgnoringPair<Integer>, List<Integer>>> sepSets;
    private Graph junctionTreeGraph;

    public JunctionTree() {
    }

    public List<List<Integer>> getClusters() {
        return clusters;
    }

    public void setClusters(List<List<Integer>> clusters) {
        this.clusters = clusters;
        junctionTreeGraph = new Graph(clusters.size());
    }

    public List<Pair<OrderIgnoringPair<Integer>, List<Integer>>> getSepSets() {
        return sepSets;
    }

    public void setSepSets(List<Pair<OrderIgnoringPair<Integer>, List<Integer>>> sepSets) {
        this.sepSets = sepSets;
    }

    public Graph getGraph() {
        return junctionTreeGraph;
    }
}