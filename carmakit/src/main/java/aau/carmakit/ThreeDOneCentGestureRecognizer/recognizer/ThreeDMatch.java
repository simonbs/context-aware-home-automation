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
 * @author jhero
 * james.thomas.herold@gmail.com
 * 4.29.2012
 * 
 */

package aau.carmakit.ThreeDOneCentGestureRecognizer.recognizer;

import aau.carmakit.ThreeDOneCentGestureRecognizer.datatype.ThreeDStroke;

public class ThreeDMatch {
	/** The stroke data of the matching stroke object */
	private ThreeDStroke t;

	/** The manual label of the matching stroke */
	private String label;

	/** The score (closeness) of the matching stroke */
	private double score;

	public ThreeDMatch(ThreeDStroke t, double score, String label){
		this.t = t;
		this.score = score;
		this.label = label;
	}
	
	public ThreeDStroke getT(){ return this.t; }
	public double getScore(){ return this.score; }
	public String getLabel(){ return this.label; }
	
	public String toString(){ return this.label; }
}
