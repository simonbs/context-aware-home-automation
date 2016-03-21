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
 * 
 * This class represents a single point of a handwritten stroke, comprising a triple (x,y,t).
 * 
 * @author jhero
 * james.thomas.herold@gmail.com
 * 
 * 4.29.2012
 */

package aau.carma.ThreeDOneCentGestureRecognizer.datatype;

public class ThreeDPoint {

    protected double x, y, z, t;

    /**
     * Creates a point with specified x-y cartesian coordinate and timestamp t
     * @param x
     * @param y
     * @param z
     * @param t
     */
    public ThreeDPoint(double x, double y, double z, double t){
        this.x = x;
        this.y = y;
        this.z = z;
        this.t = t;
    }

    /**
     * Creates a point with specified x-y cartesian coordinate and no timestamp t, default t = 0
     * @param x
     * @param y
     */
    public ThreeDPoint(double x, double y, double z){
        this(x,y, z,0);
    }


    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() { return z; }

    public double getT() {
        return t;
    }

    public String toString(){
        return "[" + this.x + "," + this.y +  "," + this.z + "]";
    }

    /**
     * Computes the Euclidean distance between two points
     * @param p1
     * @param p2
     * @return
     */
    public static double euclideanDistance(ThreeDPoint p1, ThreeDPoint p2){
        return Math.sqrt(Math.pow(p1.x - p2.x, 2) + Math.pow(p1.y - p2.y, 2) + Math.pow(p1.z - p2.z, 2));
    }

}
