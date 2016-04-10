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

package aau.carmakit.ThreeDOneCentGestureRecognizer.datatype;

import java.util.ArrayList;

import aau.carmakit.ThreeDOneCentGestureRecognizer.util.ThreeDOneDimensionalRepresentation;

/**
 * Simple class used to store template information, namely: stroke data, one-dimensional representation of
 * that stroke, and the label of that stroke
 * @author jhero
 */
public class ThreeDNNRTemplate {
    String label;
    ThreeDOneDimensionalRepresentation odr;
    ThreeDLabeledStroke ls;

    public ThreeDNNRTemplate(String label, ThreeDOneDimensionalRepresentation odr, ThreeDLabeledStroke ls){
        this.label = label;
        this.odr = odr;
        this.ls = ls;
    }

    public ArrayList<Double> getSeries(){
        return this.odr.getSeries();
    }

    public String getLabel(){return this.label;}
    public ThreeDOneDimensionalRepresentation getOdr(){return this.odr;}
    public ThreeDLabeledStroke getLs(){return this.ls;}
}