package edu.stanford.rsl.felixdurlak;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.JFrame;
import javax.swing.SpinnerNumberModel;

import org.math.plot.Plot2DPanel;

import ij.ImageJ;
import ij.io.Opener;
import edu.mines.jtk.util.Array;
import edu.stanford.rsl.conrad.geometry.splines.UniformCubicBSpline;
import edu.stanford.rsl.conrad.utils.CONRAD;
import edu.stanford.rsl.conrad.utils.Configuration;
import edu.stanford.rsl.conrad.utils.FileUtil;
import edu.stanford.rsl.conrad.utils.XmlUtils;
import edu.stanford.rsl.jpop.FunctionOptimizer;
import edu.stanford.rsl.jpop.FunctionOptimizer.OptimizationMode;
import edu.stanford.rsl.apps.gui.RawDataOpener;
import edu.stanford.rsl.apps.gui.blobdetection.AutomaticMarkerDetectionWorker;
import edu.stanford.rsl.apps.gui.blobdetection.MarkerDetectionWorker;


public class MotionCorrection {

	// default values
	private static String path = "G:\\Projektionsdaten\\WEIGHTBEARING.XA._.0004.0001.2011.09.27.17.27.22.187500.61765161.tif";
	private boolean autoDetection = true;
	private boolean refinement = true;
	private int numberOfBeads = 16;
	private double binarizationThreshold = 0.08;
	private double circularity = 3;
	private double gradientThreshold = 0;
	private double distance = 200;
	private String blobRadii = "[3]";
	private double numberOfIterations = 10;
	private static ArrayList<ArrayList<double[]>> measuredTwoDPoints = null;
	private static ArrayList<double[]> referenceThreeDPoints = null;

	private int numberOfProjections = 248;


	private String filename = "G:\\Projektionsdaten\\CONRAD.xml";



	private MarkerDetectionWorker pWorker;

	public MotionCorrection () {

		// setup pWorker
		pWorker = new MarkerDetectionWorker();
		updateParameters();

		//		// show projections
		//		Opener op = new Opener();
		//		op.openImage(path).show();
	}

	protected static String getPath() {
		return path;
	}

	protected static ArrayList<double[]> getReferenceThreeDPoints() {
		return referenceThreeDPoints;
	}

	protected static ArrayList<ArrayList<double[]>> getMeasuredTwoDPoints() {
		return measuredTwoDPoints;
	}

	private void updateParameters(){
		if (autoDetection){
			pWorker = new AutomaticMarkerDetection();
			((AutomaticMarkerDetection)pWorker).setParameters(binarizationThreshold, circularity,
					gradientThreshold, distance, blobRadii, refinement, numberOfBeads);
		}
		else{
			pWorker = new MarkerDetection();
			pWorker.setParameters(binarizationThreshold, circularity,
					gradientThreshold, distance, blobRadii);
		}

	}

	private void runDetection(){
		// run Detection numberOfIterations times
		try {
			for (int j = 0; j < numberOfIterations; j++) {
				pWorker.run();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public double[] optimize(){
		FunctionOptimizer fo = new FunctionOptimizer();
		fo.setDimension(6*numberOfProjections);
		fo.setOptimizationMode(OptimizationMode.Function);
		fo.setConsoleOutput(false);
		double[] x = new double[6*numberOfProjections];
		fo.setInitialX(x);
		double min = -10;
		double max = 10;
		double[] minArray = new double[6*numberOfProjections];
		double[] maxArray = new double[6*numberOfProjections];
		Arrays.fill(minArray, min);
		Arrays.fill(maxArray, max);
		fo.setMinima(minArray);
		fo.setMaxima(maxArray);
		//		fo.setNdigit(5);


		Optimization optimizationFunction = new Optimization();
		double [] result = fo.optimizeFunction(optimizationFunction);

		//		// convert values in result from rad to degree
		//		for (int i = 0; i < result.length; i=i+6){
		//			result[i] *= 180/Math.PI;
		//			result[i+1] *= 180/Math.PI;
		//			result[i+2] *= 180/Math.PI;
		//		}

		return result;		
	}


	@SuppressWarnings("unchecked")
	public static void main(String[] args) {

		MotionCorrection mc = new MotionCorrection();

		// load config
		Configuration config = Configuration.loadConfiguration(mc.filename);
		if (config !=null) {
			Configuration.setGlobalConfiguration(config);
		}

		//		mc.runDetection();

		try {
			measuredTwoDPoints = (ArrayList<ArrayList<double[]>>) XmlUtils.importFromXML("G:/Projektionsdaten/measured2D_13_06_15.xml");
			referenceThreeDPoints = (ArrayList<double[]>) XmlUtils.importFromXML("G:/Projektionsdaten/reference3D_13_06_15.xml");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//		measuredTwoDPoints = mc.pWorker.getMeasuredTwoDPoints();
		//		referenceThreeDPoints = mc.pWorker.getReferenceThreeDPoints();

		//		try {
		//			XmlUtils.exportToXML(measuredTwoDPoints);
		//			XmlUtils.exportToXML(mc.pWorker.getMergedTwoDPositions());
		//			XmlUtils.exportToXML(mc.pWorker.getReferenceThreeDPoints());
		//		} catch (Exception e) {
		//			// TODO Auto-generated catch block
		//			e.printStackTrace();
		//		}

		//		ArrayList<ArrayList<double[]>> mergedTwoDPositions = mc.pWorker.getMergedTwoDPositions();


		@SuppressWarnings("unused")
		double [] result = mc.optimize();
		
		
		/* 						debugging stuff                          */

		//		for (int i = 0; i<16; i++){
		//			System.out.println(measuredTwoDPoints.get(i).size());					
		//		}

		//		ArrayList<double[]> xPointsList = new ArrayList<double[]>();
		//		ArrayList<double[]> yPointsList = new ArrayList<double[]>();

		//		// create your PlotPanel
		//		Plot2DPanel plot = new Plot2DPanel();
		//
		//		
		//		double[] xValues = result;
		//		
		//		
		//		double[] xValuesTest = new double[mc.numberOfProjections];
		//		double[] yValuesTest  = new double[mc.numberOfProjections];
		//		
		//		
		//		for (int i = 0; i < measuredTwoDPoints.size(); i++){
		////			double[] xValues = new double[mc.numberOfProjections];
		//			double[] yValues = new double[mc.numberOfProjections];
		//			for (int j = 0; j < measuredTwoDPoints.get(i).size(); j++){
		//				xValues[(int) measuredTwoDPoints.get(i).get(j)[2]] = measuredTwoDPoints.get(i).get(j)[0];
		//				yValues[(int) measuredTwoDPoints.get(i).get(j)[2]] = measuredTwoDPoints.get(i).get(j)[1];
		//			}
		//			
		////			// just for testing
		////			if (i == 0){
		////				xValuesTest = Array.copy(xValues);
		////				yValuesTest = Array.copy(yValues);
		////			}
		//					
		//			xPointsList.add(xValues);
		//			yPointsList.add(yValues);
		//			
		//		}
		//
		//		// add a line plot to the PlotPanel
		//		plot.addScatterPlot("xTest", xPointsList.get(0));
		//		// put the PlotPanel in a JFrame, as a JPanel
		//		JFrame frame = new JFrame("a plot panel");
		//		frame.setSize(600, 600);
		//		frame.setContentPane(plot);
		//		frame.setVisible(true);


		//		UniformCubicBSpline cspline = new UniformCubicBSpline(spline.getControlPoints(), spline.getKnots());




	}




}
