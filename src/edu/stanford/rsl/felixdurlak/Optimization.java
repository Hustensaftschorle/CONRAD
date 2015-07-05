package edu.stanford.rsl.felixdurlak;
import java.time.LocalDateTime;
import java.util.ArrayList;

import javax.swing.JFrame;

import org.math.plot.Plot2DPanel;

import edu.stanford.rsl.conrad.geometry.Projection;
import edu.stanford.rsl.conrad.geometry.Rotations;
import edu.stanford.rsl.conrad.numerics.SimpleMatrix;
import edu.stanford.rsl.conrad.numerics.SimpleOperators;
import edu.stanford.rsl.conrad.numerics.SimpleVector;
import edu.stanford.rsl.conrad.utils.Configuration;
import edu.stanford.rsl.jpop.GradientOptimizableFunction;
import edu.stanford.rsl.jpop.OptimizationOutputFunction;


public class Optimization implements GradientOptimizableFunction{

	JFrame frame = new JFrame("parameter vector x");
	Plot2DPanel plot;

	boolean firstIter = true;

	@Override
	public void setNumberOfProcessingBlocks(int number) {
		// not parallel
	}

	@Override
	public int getNumberOfProcessingBlocks() {
		return 1;
	}

	@Override
	public double evaluate(double[] x, int block) {

		// convert values in x from degree to rad
		for (int i = 0; i < x.length; i=i+6){
			x[i] *= Math.PI/180;
			x[i+1] *= Math.PI/180;
			x[i+2] *= Math.PI/180;
		}

		ArrayList<double[]> referenceThreeDPoints = MotionCorrection.getReferenceThreeDPoints();

		ArrayList<ArrayList<double[]>> measuredTwoDPoints = MotionCorrection.getMeasuredTwoDPoints();

		Projection[] projectionMatrices = Configuration.getGlobalConfiguration().getGeometry().getProjectionMatrices();

		int numberOfProjections = projectionMatrices.length;

		int [] projectionNumber = new int[measuredTwoDPoints.size()];

		int projectionCounter = 0;

		double distance = 0;

		for (int i=0; i < numberOfProjections; i++){

			// create rotationMatrix and use it to create tMatrix
			SimpleMatrix rotationMatrix = Rotations.createRotationMatrix(x[i*6], x[i*6+1], x[i*6+2]);
			SimpleMatrix tMatrix = new SimpleMatrix(4,4);
			for (int row=0; row < 3; row++){
				for (int col=0; col < 3; col++){
					tMatrix.setElementValue(row, col, rotationMatrix.getElement(row, col));
				}
			}
			tMatrix.setElementValue(0, 3, x[i*6+3]);
			tMatrix.setElementValue(1, 3, x[i*6+4]);
			tMatrix.setElementValue(2, 3, x[i*6+5]);
			tMatrix.setElementValue(3, 3, 1);


			// integrate movement from tMatrix into projectionMatrices
			Projection currProj = new Projection(projectionMatrices[i]);
			currProj.setRtValue(SimpleOperators.multiplyMatrixProd(currProj.getRt(),tMatrix));


			// measure distance between reference points and measuredTwoDPoints for each projection present for each bead and sum it up
			for (int j=0; j < measuredTwoDPoints.size(); j++){	


				// project 3D reference points into 2D
				SimpleVector output2Dpixel = new SimpleVector(2);
				SimpleVector input3Dvector = new SimpleVector(3);
				input3Dvector.setElementValue(0, referenceThreeDPoints.get(j)[0]);
				input3Dvector.setElementValue(1, referenceThreeDPoints.get(j)[1]);
				input3Dvector.setElementValue(2, referenceThreeDPoints.get(j)[2]);
				currProj.project(input3Dvector, output2Dpixel);	


				// if bead j is present in projection i => calculate distance
				if ((measuredTwoDPoints.get(j).get(projectionNumber[j]) != null) && measuredTwoDPoints.get(j).get(projectionNumber[j])[2] == i){
					distance += Math.sqrt(Math.pow((output2Dpixel.getElement(0)-measuredTwoDPoints.get(j).get(projectionNumber[j])[0]), 2) 
							+ Math.pow((output2Dpixel.getElement(1)-measuredTwoDPoints.get(j).get(projectionNumber[j])[1]), 2));
					projectionNumber[j]++;
					projectionCounter++;
				}
				
			}
		}
		double result = distance/projectionCounter;
		System.out.printf("%.12f \n", result);	
				
				// plotting stuff for debugging
				if (firstIter){
					firstIter = false;
					frame.setSize(600, 600);
					frame.setVisible(true);
				}
				plot = new Plot2DPanel();
				plot.removeAllPlots();
				plot.addLinePlot("xTest", x);
		
				frame.setContentPane(plot);

		return result;
	}

	@Override
	public double[] gradient(double[] x, int block) {
		return null;
	}

}
