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

package aau.carma.ThreeDOneCentGestureRecognizer.recognizer;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import org.json.JSONException;

import java.util.ArrayList;

import aau.carma.ThreeDOneCentGestureRecognizer.datatype.ThreeDNNRTemplate;
import aau.carma.ThreeDOneCentGestureRecognizer.datatype.ThreeDLabeledStroke;
import aau.carma.ThreeDOneCentGestureRecognizer.datatype.ThreeDPoint;
import aau.carma.ThreeDOneCentGestureRecognizer.datatype.ThreeDStroke;
import aau.carma.ThreeDOneCentGestureRecognizer.util.ThreeDTemplatesDataSource;
import aau.carma.ThreeDOneCentGestureRecognizer.util.ThreeDOneDimensionalRepresentation;

/**
 * This class implements a simple nearest neighbor search described in our SBIM 2012 paper. Converts strokes
 * to a simple 1D representation and then finds the matching template using a simple 1NN search.
 * @author jhero
 * james.thomas.herold@gmail.com
 * 4.29.2012
 */


public class ThreeDOneCentRecognizer extends ThreeDTemplateBasedRecognizer{

    /** The resample size */
    private int n;

    /** The templates that constitute the recognizer's "training" data*/
    private ArrayList<ThreeDNNRTemplate> trainingTemplates;

    /** The DAO (Data Access Object) */
    private ThreeDTemplatesDataSource templatesDataSource;

    /** The default resample size */
    private static final int RESAMPLE_AMOUNT = 64;

    /**
     * Templatizes and stores a stroke fro recognition.
     * @param stroke - Stroke that will constitute a training template
     */
    public ThreeDNNRTemplate AddTrainingStroke(ThreeDLabeledStroke stroke){
        ThreeDLabeledStroke resampledStroke = new ThreeDLabeledStroke(stroke.getLabel(), resample(stroke, this.n).getPoints());
        ThreeDNNRTemplate template = new ThreeDNNRTemplate(resampledStroke.getLabel(), new ThreeDOneDimensionalRepresentation(resampledStroke), resampledStroke);
        trainingTemplates.add(template);

        try {
            templatesDataSource.saveTemplate(template);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return template;
    }

    /**
     * Creates a new recognizer with the default resample size and loads stored templates if any exist.
     * @param context
     */
    public ThreeDOneCentRecognizer(Context context){
        this(context, RESAMPLE_AMOUNT);
    }

    /**
     * Creates a new recognizer with a given resample size and loads stored templates if any exist.
     * @param context
     * @param n - resample size for template strokes
     */
    public ThreeDOneCentRecognizer(Context context, int n){
        this.n = n;
        templatesDataSource = new ThreeDTemplatesDataSource(context);
        templatesDataSource.open();
        ArrayList<ThreeDNNRTemplate> templates = new ArrayList<>();
        try {
            templates = templatesDataSource.getAllTemplates();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        this.trainingTemplates = templates;
    }

    /**
     * Creates a new recognizer using the supplied training templates.
     * @param n resample size for template strokes
     * @param templates Training template set
     */
    public ThreeDOneCentRecognizer(int n, ArrayList<ThreeDNNRTemplate> templates){
        this.n = n;
        this.trainingTemplates = templates;
    }

    /**
     * Creates a new recognizer using the supplied training strokes. Training strokes are templatized and stored for recognition.
     * @param trainingStrokes - Strokes that will constitute the training template set
     * @param n - resample size for template strokes
     */
    public ThreeDOneCentRecognizer(ArrayList<ThreeDLabeledStroke> trainingStrokes, int n){
        this.trainingTemplates = new ArrayList<ThreeDNNRTemplate>();

        this.n = n;

        ArrayList<ThreeDLabeledStroke> resampled = new ArrayList<ThreeDLabeledStroke>();

        /** Resample each of the training strokes */
        for(ThreeDLabeledStroke stroke : trainingStrokes){
            ThreeDStroke r = resample(stroke, this.n);
            ThreeDLabeledStroke lsr = new ThreeDLabeledStroke(stroke.getLabel(), r.getPoints());
            resampled.add(lsr);
        }

        /** Templatize and store strokes */
        for(ThreeDLabeledStroke labeledStroke : resampled){
            ThreeDOneDimensionalRepresentation odr = new ThreeDOneDimensionalRepresentation(labeledStroke);
            ThreeDNNRTemplate template = new ThreeDNNRTemplate(labeledStroke.getLabel(), odr, labeledStroke);
            trainingTemplates.add(template);
        }
    }

    /**
     * Find the best matching template for the specified stroke.
     */
    public ThreeDMatch recognize(ThreeDStroke stroke){

        /** Convert the input stroke into a one dimensional time-series */
        stroke = resample(stroke, this.n);
        ArrayList<Double> candidateSeries = new ThreeDOneDimensionalRepresentation(stroke).getSeries();

        /** Find the closest matching training template */
        double minMatch = Double.MAX_VALUE;
        ThreeDLabeledStroke strokeMatch = null;
        for(ThreeDNNRTemplate trainingTemplate : this.trainingTemplates){
            /** Convert the template into a one dimensional time-series */
            ArrayList<Double> trainSeries = trainingTemplate.getSeries();

            /** Compute the distance between the input stroke and the training template */
            double distance = l2(candidateSeries, trainSeries);

            if(distance < minMatch){
                minMatch = distance;
                strokeMatch = trainingTemplate.getLs();
            }
        }

        return new ThreeDMatch(strokeMatch, minMatch, strokeMatch.getLabel());
    }

    /**
     * Compares a stroke with all training templates and returns a list with all matches.
     * @param stroke The stroke to compare to training templates
     * @return
     */
    public ArrayList<ThreeDMatch> getAllMatches(ThreeDStroke stroke){
        ArrayList<ThreeDMatch> matches = new ArrayList<>();

        /** Convert the input stroke into a one dimensional time-series */
        stroke = resample(stroke, this.n);
        ArrayList<Double> candidateSeries = new ThreeDOneDimensionalRepresentation(stroke).getSeries();

        for(ThreeDNNRTemplate trainingTemplate : this.trainingTemplates){
            /** Convert the template into a one dimensional time-series */
            ArrayList<Double> trainSeries = trainingTemplate.getSeries();

            /** Compute the distance between the input stroke and the training template */
            double distance = l2(candidateSeries, trainSeries);
            ThreeDMatch match = new ThreeDMatch(trainingTemplate.getLs(), distance, trainingTemplate.getLabel());
            if (matches.size() == 0) {
                matches.add(match);
                continue;
            }
            for (int i = 0; i < matches.size(); i++){
                if (distance <= matches.get(i).getScore()){
                    matches.add(i, match);
                    break;
                } else if (i == matches.size()-1){
                    matches.add(match);
                }
            }
        }

        return matches;
    }

    /**
     * Loads training templates from database
     */
    public void loadTemplates(){
        ArrayList<ThreeDNNRTemplate> templates = new ArrayList<>();
        try {
            templates = templatesDataSource.getAllTemplates();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (templates != null && !templates.isEmpty()){
            this.trainingTemplates = templates;
        }
    }

    /**
     * Computes the L2 distance between the two time series.
     * @param s
     * @param t
     * @return
     */
    private double l2(ArrayList<Double> s, ArrayList<Double> t){
        int N = s.size() < t.size() ? s.size() : t.size();

        double diff = 0;

        for(int i = 0; i < N; i++){
            diff += Math.pow(s.get(i) - t.get(i), 2);
        }

        return diff;
    }

    /**
     * Resamples the stroke into n evenly spaced points
     * @param s
     * @param n
     * @return
     */
    private ThreeDStroke resample(ThreeDStroke s, int n){
        ArrayList<ThreeDPoint> points = s.getPath();

        double I = 1.0 * s.pathLength() / (n - 1);
        double D = 0;

        ArrayList<ThreeDPoint> newPoints = new ArrayList<>();
        newPoints.add(points.get(0));

        for(int i = 1; i < points.size(); i++){
            double d = ThreeDPoint.euclideanDistance(points.get(i - 1), points.get(i));

            ThreeDPoint pi = points.get(i);
            ThreeDPoint pim1 = points.get(i-1);

            if(D + d >= I){
                double qx = pim1.getX() + ((I-D)/d) * (pi.getX() - pim1.getX());
                double qy = pim1.getY() + ((I-D)/d) * (pi.getY() - pim1.getY());
                double qz = pim1.getZ() + ((I-D)/d) * (pi.getZ() - pim1.getZ());
                ThreeDPoint q = new ThreeDPoint(qx, qy, qz);
                newPoints.add(q);
                points.add(i, q);
                D = 0;
            }

            else D = D + d;
        }

        return new ThreeDStroke(newPoints);
    }
}