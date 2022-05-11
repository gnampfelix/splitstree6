package splitstree6.algorithms.distances.distances2network.distances2splits.neighbornet;

import jloda.util.CanceledException;
import jloda.util.progress.ProgressListener;
import splitstree6.data.parts.ASplit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;

import static java.lang.Math.*;
import static splitstree6.algorithms.distances.distances2network.distances2splits.neighbornet.SquareArrays.*;

public class NeighborNetSplitWeights {

    public static class NNLSParams {

        public NNLSParams(int ntax) {
            cgIterations = max(ntax,10);
            outerIterations = max(ntax,10);
        }

        static public int PROJ_GRAD = 0;
        static public int ACTIVE_SET = 1;

        public int nnlsAlgorithm = PROJ_GRAD;
        public double tolerance = 1e-6; //Approximate tolerance in split weights
        public boolean greedy = false;
        public boolean useInsertionAlgorithm = true; //Use taxon insertion algorithm for the initial split weights
        public int cgIterations; //Max number of iterations on the calls to conjugate gradients.
        public int outerIterations; //Max number of iterations through the outer loop
        public boolean collapseMultiple = false; //Collapse multiple negative splits (ST4 only)
        public double fractionNegativeToKeep = 0.4; //Propostion of negative splits to collapse (ST4 only)
        public double kktBound = tolerance/100;
    }


    static public ArrayList<ASplit> compute(int[] cycle, double[][] distances, NNLSParams params, ProgressListener progress) throws CanceledException {

        int n = cycle.length - 1;  //Number of taxa

        //Handle cases for n<3 directly.
        if (n == 1) {
            return new ArrayList<>();
        }
        if (n == 2) {
            final ArrayList<ASplit> splits = new ArrayList<>();
            float d_ij = (float) distances[cycle[1] - 1][cycle[2] - 1];
            if (d_ij > 0.0) {
                final BitSet A = new BitSet();
                A.set(cycle[1]);
                splits.add(new ASplit(A, 2, d_ij));
            }
            return splits;
        }

        //Set up square array of distances
        double[][] d = new double[n+1][n+1];
        for(int i=1;i<=n;i++)
            for (int j=i+1;j<=n;j++)
                d[i][j] = d[j][i] = distances[cycle[i]-1][cycle[j]-1];

        //Initial value for x given by insertion algorithm (for ST4, this should be all ones???)
        double[][] x;
        if (params.useInsertionAlgorithm)
            x = IncrementalFitting.incrementalFitting(d,params.tolerance/100);
        else
            x =  ones(n);

        optimizeFit(x,d,params,progress);

        final ArrayList<ASplit> splitList = new ArrayList<>();

        double cutoff = params.tolerance/10;
        for (int i = 1; i <= n; i++) {
            final BitSet A = new BitSet();
            for (int j = i + 1; j <= n; j++) {
                A.set(cycle[j - 1]);
                if (x[i][j] > cutoff)
                    splitList.add(new ASplit(A, n, (float) (x[i][j])));

            }
        }
        return splitList;
    }

    static private void optimizeFit(double[][] x, double[][] d, NNLSParams params, ProgressListener progress) throws CanceledException {

        double fx_old = evalf(x,d);
        int n=x.length-1;
        boolean[][] activeSet = getZeroElements(x);

        for (int k = 1; k <= params.outerIterations; k++) {
            boolean optimalForFace = searchFace(x, d, activeSet, params);
            double fx = evalf(x,d);
            if (optimalForFace || fx_old-fx<params.tolerance) {
                if (params.greedy)
                    return;
                boolean finished = checkKKT(x, d, activeSet,params);
                if (finished)
                    return;
            }
            fx_old = fx;
            progress.checkForCancel();
        }
        System.err.println("NNLS algorithm failed to converge");
    }


    /**
     * Search a face of the nnls problem
     *
     * Minimizes ||Ax - d|| subject to the constraint that x_ij = 0 whenever activeSet[i][j] = true.
     *
     * Uses at most n iterations of the conjugate gradient algorithm for normal equations (cf CGNR in Saad's book)
     * When that converges, or when all iterations are completed,
     *    (1) if projectedMin is true, considers points along the projection of the line segment connecting the
     *    initial value of x and the result of the CG, where the 'projection' of a vector xt is just obtained
     *    by setting xt[i][j]=0 whenever xt[i][j]<0, otherwise
     *    (2) picks the last feasible point on the path from initial x to the final x.
     *
     * @param x   Initial point, overwritten with final point
     * @param d   SquareArray of distances
     * @param activeSet   Square array of boolean, indicating which entries are constrained to zero
     * @param params   options - search depends on nnls algorithm
     * @return boolean. True if method finishes with x as the approx minimizer of the current face.
     */
    static private boolean searchFace(double[][] x, double[][] d, boolean[][] activeSet, NNLSParams params) {
        int n=x.length-1;
        double[][] x0 = new double[n+1][n+1];
        copyArray(x,x0);

        boolean cgConverged = cgnr(x,d,activeSet,params.tolerance,params.cgIterations);
        if (params.collapseMultiple) {
            filterMostNegative(x,activeSet,params.fractionNegativeToKeep);
            cgConverged = cgnr(x,d,activeSet,params.tolerance,params.cgIterations);
        }

        if (minArray(x)<0) {
            if (params.nnlsAlgorithm == NNLSParams.PROJ_GRAD)
                //Use gradient projection to return the best projection of points on the line between x0 and x
                goldenProjection(x,x0,d,params.tolerance);
            else
                furthestFeasible(x,x0,params.tolerance);
            getZeroElements(x,activeSet);
            return false;
        }
        else
            return cgConverged;
    }


    /**
     * Implementation of the CGNR algorithm in Saad, "Iterative Methods for Sparse Linear Systems", applied to the
     * problem of minimizing ||Ax - d|| such that x_{ij} = 0 for all ij in the activeSet.
     *
     * @param x   Initial value, overwritten with final value
     * @param d   square array of distances
     * @param activeSet   square array of boolean: specifying active set.
     * @param tol  tolerance for the squared norm of the residual
     * @param maxIterations  maximum number of iterations
     * @return boolean  true if the method converged (didn't hit max number of iterations)
     */
    static private boolean cgnr(double[][] x, double[][] d, boolean[][] activeSet, double tol, int maxIterations) {
        int n = x.length-1;

        double[][] p = new double[n+1][n+1];
        double[][] r = calcAx(x);
        for(int i=1;i<=n;i++)
            for(int j=1;j<=n;j++)
                r[i][j] = d[i][j] - r[i][j];
        double[][] z = calcAtx(r);
        maskElements(z,activeSet);
        copyArray(z,p);
        double ztz=sumArraySquared(z);

        int k=1;

        while(true) {
            double[][] w = calcAx(p);
            double alpha = ztz/sumArraySquared(w);

            for(int i=1;i<=n;i++) {
                for (int j = 1; j <= n; j++) {
                    x[i][j] += alpha * p[i][j];
                    r[i][j] -= alpha * w[i][j];
                }
            }
            z = calcAtx(r);
            maskElements(z,activeSet);
            double ztz2 = sumArraySquared(z);
            double beta = ztz2/ztz;

            if (ztz2<tol || k >= maxIterations)
                break;

            for(int i=1;i<=n;i++) {
                for (int j = 1; j <= n; j++) {
                    p[i][j] = z[i][j] + beta * p[i][j];
                }
            }
            ztz = ztz2;
            k++;
        }
        return (k<maxIterations);
    }


    /**
     * Determine the most negative entries in x. Finds the largest threshold t so that a proportion of
     * fractionNegativeToKeep entries have values at least t. Those entries with value less than t are added to the
     * active set.
     * At present this runs in O(n^2 + k log k) where k is the number of negative entries. Could be made simpler?
     * This method is here in order to emulate the ST4 algorithm.
     * @param x square array of doubles
     * @param activeSet  activeset. Entries with most negative values are added to this
     * @param fractionNegativeToKeep double. Minimum fraction of the negative entries to keep.
     */
    static private void filterMostNegative(double[][] x, boolean[][] activeSet, double fractionNegativeToKeep) {
        int numNeg = 0;
        int n = x.length - 1;
        for (int i = 1; i <= n; i++)
            for (int j = i + 1; j <= n; j++)
                if (!activeSet[i][j] && x[i][j] < 0)
                    numNeg++;
        if (numNeg == 0)
            return;
        double[] vals = new double[numNeg];
        int k = 0;
        for (int i = 1; i <= n; i++)
            for (int j = i + 1; j <= n; j++)
                if (!activeSet[i][j] && x[i][j] < 0) {
                    vals[k] = x[i][j];
                    k++;
                }
        Arrays.sort(vals);
        int numToKeep = (int) ceil(numNeg * fractionNegativeToKeep);
        double threshold;
        if (numToKeep == 0)
            threshold = 0.0;
        else
            threshold = vals[numNeg - numToKeep];
        //Make active all entries with weight strictly less than the threshold.
        for (int i = 1; i <= n; i++)
            for (int j = i + 1; j <= n; j++) {
                if (!activeSet[i][j] && x[i][j] < threshold) {
                    activeSet[i][j] = true;
                    activeSet[j][i] = true;
                }
            }
    }




    /**
     * Minimizes ||Ax-b|| along the projection of the line segment between x0 and x, where the projection of a point
     * is the closest point in the non-negative quadrant.
     *
     * @param x square array   final point, overwritten by optimal point
     * @param x0  square array  initial point
     * @param d  square array of distances
     * @param tolerance tolerance used for golden section search
     */

    static private void goldenProjection(double[][] x, double[][] x0,double[][] d, double tolerance) {
        //Minimize ||A \pi((1-t)x0 + tx) - d||  for t in [0,1]
        double C = (3-sqrt(5))/2.0;
        double R = 1.0 - C;

        double t0=0,t1=C,t2 = C+C*(1-C), t3 = 1.0;
        double f1 = evalfprojected(t1,x0,x,d);
        double f2 = evalfprojected(t2,x0,x,d);

        double f3 = evalfprojected(t3,x0,x,d);

        while(abs(t3-t0)>tolerance) {
            if (f2<f1) {
                t0=t1;
                t1=t2;
                t2 = R*t1 + C*t3;
                f1=f2;
                f2 = evalfprojected(t2,x0,x,d);
            } else {
                t3=t2;
                t2=t1;
                t1 = R*t2+C*t0;
                f2=f1;
                f1 = evalfprojected(t1,x0,x,d);
            }
        }
        double tmin = t1;
        if (f2<f1)
            tmin = t2;
        else if (t0==0) {  //Handle a special case so that if minimum is at the boundary t=0 then this is exactly what is returned
            double f0 = evalfprojected(t0,x0,x,d);
            if (f0<f1)
                tmin = t0;
        }
        int n=x.length-1;
        for(int i=1;i<=n;i++) {
            for(int j=1;j<=n;j++) {
                double newx_ij = max((1-tmin)*x0[i][j] + tmin*x[i][j],0);
                x[i][j] = newx_ij;
            }
        }
    }

    /**
     * Evaluates ||A x_t - d|| where x_t is the projection of (1-t)x0 + t x.
     * @param t  double between 0 and 1
     * @param x0  initial point
     * @param x   final point
     * @param d   square array of distances
     * @return  value of ||A x_t - d||
     */
    static private double evalfprojected(double t, double[][] x0, double[][] x, double[][] d) {
        int n = x.length-1;
        double[][] xt = new double[n+1][n+1];
        for(int i=1;i<=n;i++)
            for(int j=1;j<=n;j++) {
                xt[i][j] = max(x0[i][j]*(1-t) + x[i][j]*t,0.0);
            }
        return evalf(xt,d);

    }

    /**
     * Determines the point on the path from x0 to x that is furthest from x0 and still feasible.
     *
     * @param x  square array, final point
     * @param x0   square array, initial point
     * @param tolerance   tolerance. Any entry of x less than tolerance is mapped to zero.
     */
    static private void furthestFeasible(double[][] x, double[][] x0,double tolerance) {
        double tmin = 1.0;
        int n = x.length-1;

        for(int i=1;i<=n;i++) {
            for(int j=1;j<=n;j++) {
                if (x[i][j]<0)
                    tmin = min(tmin,x0[i][j]/(x0[i][j] - x[i][j]));
            }
        }

        for(int i=1;i<=n;i++) {
            for(int j=i+1;j<=n;j++) {
                double x_ij =  (1.0-tmin)*x0[i][j] + tmin*x[i][j];
                if (x_ij < tolerance)
                    x_ij = 0;
                x[i][j] = x[j][i] = x_ij;
            }
        }
    }









    /**
     * Computes circular distances from an array of split weights.
     *
     * @param x split weights. Symmetric array. For i<j, x(i,j)  is the weight of the
     *          split {i,i+1,...,j-1} | rest.
     * @return double[][] circular metric corresponding to these split weights.
     */
    static private double[][] calcAx(double[][] x) {
        int n = x.length - 1;
        double[][] d = new double[n+1][n+1];

        for (int i=1;i<=(n-1);i++)
            d[i+1][i]=d[i][i+1] =sumSubvector(x[i+1],i+1,n)+sumSubvector(x[i+1],1,i);

        for (int i=1;i<=(n-2);i++)
            d[i+2][i]=d[i][i+2]=d[i][i+1]+d[i+1][i+2]-2*x[i+1][i+2];

        for (int k=3;k<=n-1;k++) {
            for(int i=1;i<=n-k;i++) {  //TODO. This loop can be threaded
                int j=i+k;
                d[j][i]=d[i][j] = d[i][j-1]+d[i+1][j] - d[i+1][j-1]-2*x[i+1][j];
            }
        }
        return d;
    }

    /**
     * Sum the elements in the vector over a range of indices.
     *
     * Separating this out in case we can improve efficiency with threading.
     * @param v vector
     * @param from   start index
     * @param to  end index
     * @return  \sum_{i=from}^to v(i)
     */
    static private double sumSubvector(double[] v, int from, int to) {
        double s=0.0;
        for(int i=from;i<=to;i++)
            s+=v[i];
        return s;
    }






    static private double[][]  calcAtx(double[][] x) {
        int n=x.length-1;
        double[][] p = new double[n+1][n+1];

        for(int i=1;i<=n-1;i++)
            p[i+1][i] = p[i][i+1]=sumSubvector(x[i],1,n);

        for(int i=1;i<=n-2;i++) {  //TODO This can be threaded
            p[i+2][i]=p[i][i+2] = p[i][i+1]+p[i+1][i+2]-2*x[i][i+1];
        }

        for(int k=3;k<=n-1;k++) {
            for(int i=1;i<=n-k;i++) { //TODO. This inner loop can be threaded
                p[i+k][i]=p[i][i+k]=p[i][i+k-1]+p[i+1][i+k]-p[i+1][i+k-1]-2*x[i][i+k-1];
            }
        }
        return p;
    }





    /**
     * Evaluates ||Ax - d||
     *
     * @param x  square array
     * @param d square array of distances
     * @return   value of function at x.
     */
    static private double evalf(double[][] x, double[][] d) {
        double[][] Axt = calcAx(x);
        return fro_dist(Axt,d);
    }

    /**
     * checkKKT
     *
     * Checks the KKT conditions, under the assumption that x is optimal for the current active set.
     * @param x  square array. point x
     * @param d  square array of distances, used to compute graient.
     * @param activeSet current activeSet. Assume activeSet[i][j]=true implies x[i][j]=0.
     * @param params    Params
     * @return  boolean   true if kkt conditions are (approximately) satisfied.
     */
    static private boolean checkKKT(double[][] x, double[][] d, boolean[][] activeSet, NNLSParams params) {
        int n = x.length-1;
        double[][] gradient = evalGradient(x,d);
        double mingrad = 0.0;
        int min_i=0,min_j=0;
        for(int i=1;i<=n;i++)
            for(int j=i+1;j<=n;j++) {
                double grad_ij = gradient[i][j];
                if (activeSet[i][j] && grad_ij < mingrad) {
                    mingrad = grad_ij;
                    min_i = i;
                    min_j = j;
                }
            }
        if (mingrad >= -params.kktBound)
            return true;

        //Now remove elements from the active set.
        if (params.nnlsAlgorithm==NNLSParams.ACTIVE_SET) {
            activeSet[min_i][min_j] = activeSet[min_i][min_j] = false;
        } else {
            for(int i=1;i<=n;i++)
                for(int j=i+1;j<=n;j++) {
                    double grad_ij = gradient[i][j];
                    if (activeSet[i][j] && grad_ij < -params.kktBound) {
                        activeSet[i][j] = activeSet[j][i] = false;
                    }
                }
        }
        return false;
    }



    /**
     * Compute the gradient at x of 1/2 ||Ax - d||
     * @param x square array
     * @param d square array
     * @return  square array containing the gradient.
     */
    static private double[][] evalGradient(double[][] x, double[][] d) {
        int n = x.length-1;
        double[][] res = calcAx(x);
        for(int i=1;i<=n;i++)
            for(int j=1;j<=n;j++)
                res[i][j] -= d[i][j];
        return calcAtx(res);
    }



}
