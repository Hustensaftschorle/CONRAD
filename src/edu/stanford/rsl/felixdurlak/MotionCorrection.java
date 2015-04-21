package edu.stanford.rsl.felixdurlak;

import java.util.ArrayList;

import javax.swing.SpinnerNumberModel;

import ij.ImageJ;
import ij.io.Opener;
import edu.stanford.rsl.conrad.utils.CONRAD;
import edu.stanford.rsl.conrad.utils.Configuration;
import edu.stanford.rsl.conrad.utils.FileUtil;
import edu.stanford.rsl.apps.gui.RawDataOpener;
import edu.stanford.rsl.apps.gui.blobdetection.AutomaticMarkerDetectionWorker;
import edu.stanford.rsl.apps.gui.blobdetection.MarkerDetectionWorker;


public class MotionCorrection {

	// default values
	private static String path = "G:\\Projektionsdaten\\WEIGHTBEARING.XA._.0004.0001.2011.09.27.17.27.22.187500.61765161.tif";
	private boolean autoDetection = true;
	private boolean refinement = true;
	private int numberOfBeads = 16;
	private double binarizationThreshold = 0.2;
	private double circularity = 3;
	private double gradientThreshold = 0;
	private double distance = 3;
	private String blobRadii = "[3]";
	private double numberOfIterations = 10;
	
	
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
	

	public static void main(String[] args) {

		MotionCorrection mc = new MotionCorrection();
		
		// load config
		Configuration config = Configuration.loadConfiguration(mc.filename);
		if (config !=null) {
			Configuration.setGlobalConfiguration(config);
		}
		
		mc.runDetection();
		
		ArrayList<ArrayList<double[]>> measuredTwoDPoints = mc.pWorker.getMeasuredTwoDPoints();
		ArrayList<ArrayList<double[]>> mergedTwoDPositions = mc.pWorker.getMergedTwoDPositions();
		ArrayList<double[]> referenceThreeDPoints = mc.pWorker.getReferenceThreeDPoints();
		
//		new ImageJ();

	}




}
