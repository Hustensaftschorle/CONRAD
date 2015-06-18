package edu.stanford.rsl.felixdurlak;

import java.util.ArrayList;

import ij.IJ;
import edu.stanford.rsl.apps.gui.blobdetection.MarkerDetectionWorker;
import edu.stanford.rsl.conrad.data.numeric.Grid3D;
import edu.stanford.rsl.conrad.utils.Configuration;
import edu.stanford.rsl.conrad.utils.FileUtil;
import edu.stanford.rsl.conrad.utils.XmlUtils;

public class MarkerDetection extends MarkerDetectionWorker {


	public void configure() throws Exception {
		config = Configuration.getGlobalConfiguration();
		image = IJ.openImage(MotionCorrection.getPath());
		if (filenamePriors == null)
			filenamePriors = FileUtil.myFileChoose(".xml", false);
		twoDPosReal = (ArrayList<ArrayList<double[]>>) XmlUtils.importFromXML(filenamePriors);
		this.fastRadialSymmetrySpace = null;
		this.allDetectedBeads = null;
		update3Dreference();
		update2Dreference();
		configured = true;
	}

}
