package aau.carmakit.ContextEngine;

import android.util.Log;

import java.util.ArrayList;

import aau.carmakit.Utilities.Logger;

/**
 * Creates probabilities for a set of states.
 * Probabilities are crated as the amount of states
 * that have the same value.
 */
public class CrossedStatesCalculator {
    /**
     * States to create probabilities from.
     */
    private final ArrayList<String> states;

    /**
     * Indices of current values in each node.
     */
    private ArrayList<Integer> indices;

    /**
     * Initializes a constructor or creating probabilities for a set of states.
     * @param states States to create probabilities for.
     * @param nodeCount Number of nodes to involve in the computation.
     */
    public CrossedStatesCalculator(ArrayList<String> states, int nodeCount) {
        ArrayList<Integer> indicies = new ArrayList<>();
        for (int n = 0; n < nodeCount; n++) {
            indicies.add(0);
        }

        this.states = states;
        this.indices = indicies;
    }

    /**
     * Calculates the probabilities.
     * @return Calculated probabilities.
     */
    public double[] calculateProbabilities() {
        int nodeCount = indices.size();
        int statesCount = states.size();
        int rowCount = (int) Math.pow(statesCount, nodeCount);
        int probabilitiesCount = rowCount * statesCount;
        double[] probabilities = new double[probabilitiesCount];
        // For each row
        for (int r = 0; r < rowCount; r++) {
            // For each column
            for (int c = 0; c < statesCount; c++) {
                String columnState = states.get(c);
                int nodesEqualColumnState = 0;
                // For each node.
                for (int n = 0; n < indices.size(); n++) {
                    if (states.get(indices.get(n)).equals(columnState)) {
                        nodesEqualColumnState += 1;
                    }
                }

                probabilities[r * statesCount + c] = (double)nodesEqualColumnState / (double)nodeCount;
            }

            ArrayList<Integer> newIndices = new ArrayList<>();
            boolean didIncrementValue = false;
            for (int i = indices.size() - 1; i >= 0; i--) {
                Integer value = indices.get(i);
                if (didIncrementValue) {
                    newIndices.add(0, value);
                } else  if (value == statesCount - 1) {
                    newIndices.add(0, 0);
                } else {
                    newIndices.add(0, value + 1);
                    didIncrementValue = true;
                }
            }

            indices = newIndices;
        }

        // DEBUG: Logs the entire conditional probability table.
//        for (int n = 0; n < statesCount; n++) {
//            for (int i = 0; i < statesCount; i++) {
//                String rowStr = "";
//                for (int c = 0; c < statesCount; c++) {
//                    rowStr += probabilities[n * statesCount * statesCount + i * statesCount + c] + " ";
//                }
//
//                Logger.verbose(rowStr);
//            }
//
//            Logger.verbose("- - - - - - - - - - - - - - - -");
//        }
//
//        for (double probability : probabilities) {
//            Logger.verbose("" + probability);
//        }

        return probabilities;

        // n1 = [A, B]
        // n2 = [A, B]
        // n3 = [A, B]
        // n4 = [A, B]

        // n1 = A
            // n2 = A
                // n4_A = 3 , n4_B = 0 // n3 = A
                // n4_A = 2 , n4_B = 1 // n3 = B
            // n2 = B
                // n4_A = 2 , n4_B = 1 // n3 = A
                // n4_A = 1 , n4_B = 2 // n3 = B
        // n1 = B
            // n2 = A
                // n4_A = 2 , n4_B = 1 // n3 = A
                // n4_A = 1 , n4_B = 2 // n3 = B
            // n2 = B
                // n4_A = 1 , n4_B = 2 // n3 = A
                // n4_A = 0 , n4_B = 3 // n3 = B
    }
}
