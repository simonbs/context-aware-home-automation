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
 *  Class used to represent strokes, i.e., a time-stamped sequence of points.
 * 
 * @author jhero
 * james.thomas.herold@gmail.com
 * 4.29.2012
 */

package aau.carmakit.ThreeDOneCentGestureRecognizer.datatype;

import java.util.ArrayList;


public class ThreeDStroke {
    protected ArrayList<ThreeDPoint> points;
    private ArrayList<ThreeDPoint> path;

    /**
     * Creates an empty stroke
     */
    public ThreeDStroke(){
        this.points = new ArrayList<>();
        this.path = new ArrayList<>();
    }

    /**
     * Creates a stroke with the supplied point information
     * @param points
     */
    public ThreeDStroke(ArrayList<ThreeDPoint> points){
        this.points = points;
        this.path = GeneratePath(this.points);
    }

    /**
     * Append a point to the end of this stroke
     * @param p
     */
    public void addPoint(ThreeDPoint p){
        points.add(p);
    }

    /**
     * Returns the number of points contained within this stroke
     * @return
     */
    public int numPoints(){
        return this.points.size();
    }

    /**
     * Returns the point at the specified index
     * @param index
     * @return
     */
    public ThreeDPoint getPoint(int index){
        return this.points.get(index);
    }

    /**
     * Return the entire list of points comprising this stroke
     * @return
     */
    public ArrayList<ThreeDPoint> getPoints(){
        return this.points;
    }

    /**
     * Sums the distances between all pairs of consecutive points within the stroke
     * @return
     */
    public double pathLength(){
        double length = 0;

        for(int i = 1; i < getPath().size()-1; i++)
            length += ThreeDPoint.euclideanDistance(getPath().get(i), getPath().get(i - 1));

        return length;
    }

    /** Gets the path length from the beginning up to and including the point at index i */
    public double pathLength(int index){
        double length = 0;

        for(int i = 1; i <= index; i++)
            length += ThreeDPoint.euclideanDistance(getPath().get(i), getPath().get(i - 1));

        return length;
    }

    /**
     * Returns the path of summed accelerations, generates it if it does not yet exist.
     * @return Path of summed accelerations.
     */
    public ArrayList<ThreeDPoint> getPath(){
        if (this.path == null || this.path.isEmpty()){
            this.path = GeneratePath(this.points);
        }
        return this.path;
    }

    /**
     * Generates the path of the stroke by adding the previous points acceleration data to the next.
     */
    public static ArrayList<ThreeDPoint> GeneratePath(ArrayList<ThreeDPoint> points){
        ArrayList<ThreeDPoint> path = new ArrayList<>();
        for (int i = 0; i < points.size(); i++){
            if (i == 0){
                path.add(points.get(i));
            } else {
                ThreeDPoint last = points.get(i-1);
                ThreeDPoint next = points.get(i);
                ThreeDPoint newPoint = new ThreeDPoint(last.getX() + next.getX(), last.getY() + next.getY(), last.getZ() + next.getZ(), next.getT());
                path.add(newPoint);
            }
        }
        return path;
    }

}
