package edu.stanford.rsl.felixdurlak;

import ij.IJ;
import edu.stanford.rsl.apps.gui.blobdetection.AutomaticMarkerDetectionWorker;
import edu.stanford.rsl.conrad.data.numeric.Grid3D;
import edu.stanford.rsl.conrad.utils.Configuration;

public class AutomaticMarkerDetection extends AutomaticMarkerDetectionWorker {

	public AutomaticMarkerDetection() {
		// TODO Auto-generated constructor stub
	}
	
	
	@Override
	public void configure() {
		config = Configuration.getGlobalConfiguration();
		image = IJ.openImage(MotionCorrection.getPath());
		configured = true;
		if (nrOfBeads > 0){
			initializeMarkerPositions(nrOfBeads);
		}
		else{
			fastRadialSymmetrySpace = FRST();
			Grid3D frst = new Grid3D(fastRadialSymmetrySpace);
			initializeMarkerPositions(frst, false);
		}
		update2Dreference();
		configured = true;
	}

}
