/*
Copyright (c) 2012, James Herold
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
1. Redistributions of source code must retain the above copyright
   notice, this list of conditions and the following disclaimer.
2. Redistributions in binary form must reproduce the above copyright
   notice, this list of conditions and the following disclaimer in the
   documentation and/or other materials provided with the distribution.
3. All advertising materials mentioning features or use of this software
   must display the following acknowledgement:
   This product includes software developed by the <organization>.
4. Neither the name of the <organization> nor the
   names of its contributors may be used to endorse or promote products
   derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY James Herold ''AS IS'' AND ANY
EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL James Herold BE LIABLE FOR ANY
DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/


/** 
 * A simple one dimensional representation of a Stroke. The centroid of the stroke is computed and the distance
 * from the centroid to each point is then computed.
 * @author jhero
 * james.thomas.herold@gmail.com
 * 4.29.2012
 */

package aau.carmakit.ThreeDOneCentGestureRecognizer.util;

import java.util.ArrayList;

import aau.carmakit.ThreeDOneCentGestureRecognizer.datatype.ThreeDPoint;
import aau.carmakit.ThreeDOneCentGestureRecognizer.datatype.ThreeDStroke;

public class ThreeDOneDimensionalRepresentation {
    ArrayList<Double> distances;

    /**
     * Creates a new instance using the provided distances.
     * @param distances One dimensional representation of a stroke.
     */
    public ThreeDOneDimensionalRepresentation(ArrayList<Double> distances){
        this.distances = distances;
    }

    /**
     * Transforms a stroke (typically resampled) into a 1D representation.
     * @param stroke
     */
    public ThreeDOneDimensionalRepresentation(ThreeDStroke stroke){

        this.distances = cDistance(stroke);
        this.distances = znormalize(distances);
    }

    /**
     * Returns the list of distances representing a stroke object.
     * @return
     */
    public ArrayList<Double> getSeries(){
        return distances;
    }

    /**
     * Computes the distance from each point to the centroid of the stroke and returns
     * the time ordered list of distances.
     * @param stroke
     * @return
     */
    private ArrayList<Double> cDistance(ThreeDStroke stroke){
        ArrayList<Double> distances = new ArrayList<Double>();

        ThreeDPoint c = centroid(stroke);

        for(ThreeDPoint p : stroke.getPoints()){
            double distance = ThreeDPoint.euclideanDistance(c, p);
            distances.add(distance);
        }

        return distances;
    }

    /**
     * Computes the centroid of the points of the stroke, defined as
     * <avg_x, avg_y>
     * @param stroke
     * @return
     */
    public ThreeDPoint centroid(ThreeDStroke stroke){

        double sumX = 0, sumY = 0, sumZ = 0;

        for(int i = 0; i < stroke.numPoints(); i++){
            ThreeDPoint p = stroke.getPoint(i);
            sumX += p.getX();
            sumY += p.getY();
            sumZ += p.getZ();
        }

        double mx = sumX / stroke.numPoints();
        double my = sumY / stroke.numPoints();
        double mz = sumZ / stroke.numPoints();

        return new ThreeDPoint(mx, my, mz);
    }

    /**
     * Returns a new copy of the data array after it has been z-normalized.
     * @param numbers
     * @return
     */
    private ArrayList<Double> znormalize(ArrayList<Double> numbers){
        ArrayList<Double> normalized = new ArrayList<Double>();

        double avg = avg(numbers);
        double std = std(numbers, avg);

        for(Double d : numbers){
            double z = (d - avg) / std;
            normalized.add(z);
        }

        return normalized;
    }

    /**
     * Returns the average of the list of numbers.
     * @param numbers
     * @return
     */
    private double avg(ArrayList<Double> numbers){
        double sum = 0;

        for(Double d : numbers) sum += d;

        return sum / numbers.size();
    }

    /**
     * Returns the standard deviation of the list of numbers.
     * @param numbers
     * @param avg
     * @return
     */
    private double std(ArrayList<Double> numbers, double avg){
        double sum = 0;

        for(Double d : numbers) sum += Math.pow(d - avg, 2);

        return Math.sqrt(sum/numbers.size());
    }
}
