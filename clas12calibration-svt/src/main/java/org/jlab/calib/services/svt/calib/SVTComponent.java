package org.jlab.calib.services.svt.calib;

import java.util.ArrayList;

import org.jlab.groot.base.GStyle;
import org.jlab.groot.fitter.DataFitter;
import org.jlab.groot.graphics.EmbeddedCanvas;
import org.jlab.groot.math.F1D;

/**
 *
 * @author gotra
 */

public abstract class SVTComponent {
	public int nSensors;
	public int nSectors;
	public static final int REDCOLOR = 2;
	public static final int GREENCOLOR = 3;
	public static final int BLUECOLOR = 4;
	public static final int LIGHTGREENCOLOR = 38;
	public static final int YELLOWCOLOR = 35;
	public static final int LIGHTBLUECOLOR = 34;
	public static final int LILACCOLOR = 39;
	public static final int BROWNCOLOR = 37;
	public static final int PALEVIOLETREDCOLOR = 36;
	public static final int GAINCOLOR = BLUECOLOR;
	public static final int ENCCOLOR = GREENCOLOR;
	public static final int THRESHOLDCOLOR = BROWNCOLOR;

	public SVTCalibHistos meanGainHisto;
	public SVTCalibHistos meanEncHisto;
	public SVTCalibHistos chanGainHisto;
	public SVTCalibHistos chanEncHisto;
	public F1D fGain;
	public F1D fEnc;
	public ArrayList<String> noisyChannelList;
	public ArrayList<String> deadChannelList;
	public ArrayList<String> openChannelList;
	public int badChannels;

	SVTComponent() {

		noisyChannelList = new ArrayList<String>();
		deadChannelList = new ArrayList<String>();
		openChannelList = new ArrayList<String>();
		meanGainHisto = new SVTCalibHistos("MeanGain", "Mean Chip Gain, mV/fC", "Entries", 150, -0.5, 149.5, GAINCOLOR);
		meanEncHisto = new SVTCalibHistos("MeanEnc", "Mean Chip ENC, e", "Entries", 100, -0.5, 2999.5, ENCCOLOR);
		chanGainHisto = new SVTCalibHistos("ChanGain", "Channel Gain, mV/fC", "Entries", 150, -0.5, 149.5, GAINCOLOR);
		chanEncHisto = new SVTCalibHistos("ChanEnc", "Channel ENC, e", "Entries", 100, -0.5, 2999.5, ENCCOLOR);
	}

	void fitHistos() {
		DataFitter.FITPRINTOUT = false;

		GStyle.getFunctionAttributes().setOptStat("1100");
		final int ENC_LOW = 1400; //1450
		final int ENC_HIGH = 1750;
		fEnc = new F1D("gaus", "[amp]*gaus(x,[mean],[sigma])", ENC_LOW, ENC_HIGH);
		fEnc.setLineColor(REDCOLOR);
		fEnc.setLineWidth(2);
		fEnc.setParLimits(1, ENC_LOW, ENC_HIGH); // 1500 1700
		int maxbin = chanEncHisto.h.getMaximumBin();
		fEnc.setParameter(0, chanEncHisto.h.getDataY(maxbin));
		fEnc.setParameter(1, chanEncHisto.h.getDataX(maxbin));
		fEnc.setParameter(2, chanEncHisto.h.getRMS() / 6.0);
		DataFitter.fit(fEnc, chanEncHisto.h, "WREQ");

		final int GAIN_LOW = 60; // 70
		final int GAIN_HIGH = 100; // 95
		fGain = new F1D("gaus", "[amp]*gaus(x,[mean],[sigma])", GAIN_LOW, GAIN_HIGH);
		fGain.setLineColor(REDCOLOR);
		fGain.setLineWidth(2);
		maxbin = chanGainHisto.h.getMaximumBin();
		fGain.setParameter(0, chanGainHisto.h.getDataY(maxbin));
		fGain.setParameter(1, chanGainHisto.h.getDataX(maxbin));
		fGain.setParameter(2, chanGainHisto.h.getRMS() * 0.5);
		DataFitter.fit(fGain, chanGainHisto.h, "WREQ");
	}

	void drawHistos(EmbeddedCanvas canvas) {
		canvas.divide(2, 2);
		canvas.setGridX(false);
		canvas.setGridY(false);
		canvas.cd(0);
		canvas.draw(meanEncHisto.h);
		canvas.cd(1);
		canvas.draw(chanEncHisto.h);
		canvas.cd(2);
		canvas.draw(meanGainHisto.h);
		canvas.cd(3);
		canvas.draw(chanGainHisto.h);
	}
}
