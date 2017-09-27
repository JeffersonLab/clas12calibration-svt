/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.calib.services.svt.utils;

import java.util.List;

import org.jlab.calib.services.svt.calib.CalibrationSuite;
import org.jlab.detector.base.DetectorCollection;
import org.jlab.detector.base.DetectorDescriptor;
import org.jlab.detector.base.DetectorType;
import org.jlab.groot.base.GStyle;
import org.jlab.groot.data.GraphErrors;
import org.jlab.groot.data.H1F;
import org.jlab.groot.ui.TCanvas;
import org.jlab.calib.services.svt.decode.AbsDetectorTranslationTable;

/**
 *
 * @author gavalian, gotra
 */
public class CalibrationDataChip {

	DetectorCollection<CalibrationData> collection = new DetectorCollection<CalibrationData>();
	public DetectorDescriptor detectorDescriptor = new DetectorDescriptor(DetectorType.BST);
	public AbsDetectorTranslationTable trTable = null;
	public GraphErrors chipDataOffset = new GraphErrors();
	public GraphErrors chipDataVt50 = new GraphErrors();
	public GraphErrors chipDataGain = new GraphErrors();
	public GraphErrors chipDataEnc = new GraphErrors();
	public H1F chipDataGainProj = new H1F("h1", "", 200, -0.5, 199.5);
	public H1F chipDataThresholdProj = new H1F("ht", "", 150, 20000.0, 50000.0);
	public int layer = -1;
	public int region = -1;
	public int sector = -1;
	public int crate = 0;
	public int slot = 0;
	public int CHIP = 0;
	public int CHAN = 0;
	public int SIDE = 0;
	public int NCHANNELS = 128;
	public int[] status = new int[3];
	public final int NOISYCHANNEL = 1;
	public final int OPENCHANNEL = 2;
	public final int DEADCHANNEL = 3;
	public double meanEnc = 0;
	public double meanGain = 0;
	public double rmsEnc = 0;
	public double rmsGain = 0;
	public double thresholdDispersion = 0;
	public double gainDispersion = 0;
	public int channelGain[];
	public int channelEnc[];
	public int channelOffset[];
	public int channelVt50[];
	public int channelThreshold[];
	public int channelStatus[];
	public double HIGHENCTHRESHOLD = 2050;
	public double LOWENCTHRESHOLD1 = 1000;
	public double LOWENCTHRESHOLD2 = 400;

	public CalibrationDataChip() {
		GStyle.getH1FAttributes().setOptStat("111110");
		GStyle.getFunctionAttributes().setOptStat("1100");
		chipDataOffset.setTitle(CalibrationData.histoTitle);
		chipDataOffset.setTitleX("Channel");
		chipDataOffset.setTitleY("Offset, mV");
		chipDataVt50.setTitle(CalibrationData.histoTitle);
		chipDataVt50.setTitleX("Channel");
		chipDataVt50.setTitleY("Vt_50, mV");
		chipDataGain.setTitle(CalibrationData.histoTitle);
		chipDataGain.setTitleX("Channel");
		chipDataGain.setTitleY("Gain, mV/fC");
		chipDataEnc.setTitle(CalibrationData.histoTitle);
		chipDataEnc.setTitleX("Channel");
		chipDataEnc.setTitleY("ENC, e");
		chipDataGainProj.setTitle(CalibrationData.histoTitle);
		chipDataGainProj.setTitleX("Gain, mV/fC");
		chipDataGainProj.setTitleY("Entries");
		chipDataThresholdProj.setTitle(CalibrationData.histoTitle);
		chipDataThresholdProj.setTitleX("Threshold, e");
		chipDataThresholdProj.setTitleY("Entries");
	}
	
	public void init() {
		channelGain = new int[NCHANNELS];
		channelEnc = new int[NCHANNELS];
		channelOffset = new int[NCHANNELS];
		channelVt50 = new int[NCHANNELS];
		channelThreshold = new int[NCHANNELS];
		channelStatus = new int[NCHANNELS];
		for (int i = 0; i < 3; ++i) status[i] = 0; // noisy, open, dead 
		
	}

	public CalibrationDataChip(AbsDetectorTranslationTable t) {
		this.trTable = t;
	}

	public void addData(CalibrationData data) {
		this.collection.add(data.getDescriptor(), data);
	}

	public CalibrationData getData(int sector, int layer, int component) {
		return this.collection.get(sector, layer, component);
	}

	public DetectorCollection<CalibrationData> getCollection() {
		return this.collection;
	}

	public void analyze() {
//		System.out.println("chip analyze");
		String str = CalibrationStore.currentChip + " / " + CalibrationStore.nChips;
//		System.out.println(str);
		CalibrationSuite.log.setText(str);

		List<CalibrationData> dataList = this.collection.getList();
//		System.out.println("chip data size " + dataList.size());
		for (CalibrationData data : dataList) {
			data.analyze();
		}

		for (CalibrationData data : dataList) {
			int channel = data.getDescriptor().getComponent() + 1;
			double offset = data.getResFunc().getParameter(0);
			channelOffset[channel - 1] = (int)offset;
			double vt50 = data.getFunc(1).getParameter(2) * CalibrationData.MVDAC;
			channelVt50[channel - 1] = (int)vt50;
			double gain = data.getResFunc().getParameter(1);
			channelGain[channel - 1] = (int)gain;
			double enc = (gain < 1 ? 0 : CalibrationData.MVDAC * CalibrationData.EFC * data.getFunc(1).getParameter(3)
					/ gain);
			channelEnc[channel - 1] = (int)enc;
			double threshold = data.getFunc(1).getParameter(2) * CalibrationData.MVDAC
					* CalibrationData.EFC / data.getResFunc().getParameter(1);
//			System.out.println("chan " + channel + " enc " + enc + " gain " + gain);
			channelThreshold[channel - 1] = (int)threshold;
			this.chipDataOffset.addPoint(channel, offset, 0, 0);
			this.chipDataVt50.addPoint(channel, vt50, 0, 0);
			this.chipDataGain.addPoint(channel, gain, 0, 0);
			this.chipDataEnc.addPoint(channel, (enc < 0 ? 0 : enc), 0, 0); // fix
																			// negative
																			// ENC
																			// for
																			// dead
																			// channels
			this.chipDataGainProj.fill(gain);
			this.chipDataThresholdProj.fill(threshold);
		}
	}

	public GraphErrors getGainGraph() {
		return this.chipDataGain;
	}

	public GraphErrors getEncGraph() {
		return this.chipDataEnc;
	}

	public GraphErrors getOffsetGraph() {
		return this.chipDataOffset;
	}

	public GraphErrors getVt50Graph() {
		return this.chipDataVt50;
	}

	public H1F getHistoGain() {
		return this.chipDataGainProj;
	}

	public H1F getHistoThreshold() {
		return this.chipDataThresholdProj;
	}

	public void updateDescriptor(String str) {
		String[] tokens = new String[4];
		int index = str.length() - 11;
		tokens[0] = str.substring(index, index + 1); // crate
		tokens[1] = str.substring(index + 3, index + 5); // slot
		tokens[2] = str.substring(index + 7, index + 8); // vscm channel
		tokens[3] = str.substring(index + 10, index + 11); // chip

		this.CHIP = Integer.parseInt(tokens[3]);
		this.SIDE = (this.CHIP > 2 ? 1 : 0);
		this.CHIP = (this.CHIP > 2 ? this.CHIP - 2 : this.CHIP); // convert from
																	// 1-4 to
																	// 1-2
																	// numbering
		this.CHAN = Integer.parseInt(tokens[2]) - 1; // convert from 1,2 to 0,1
														// (as in the
														// translation table)
		this.crate = Integer.parseInt(tokens[0].trim());
		this.slot = Integer.parseInt(tokens[1].trim());
		this.detectorDescriptor.setCrateSlotChannel(
				this.crate, this.slot,
				this.CHAN);
	}

	public void readData(String file) {
//		System.out.println("reading chip data " + file);
		CalibrationFile cf = new CalibrationFile();
		this.updateDescriptor(file);

		cf.read(file);
		int crate = this.detectorDescriptor.getCrate();
		int slot = this.detectorDescriptor.getSlot();
		int sector = this.trTable.getSector(crate, slot, this.CHAN);
		int region = this.trTable.getLayer(crate, slot, 0);
		this.sector = sector;
		this.slot = slot;
		this.crate = crate;
		this.region = region;
		int layer = 2 * (region) - (this.SIDE == 0 ? 0 : 1);
		this.layer = layer;
		layer = layer * 10 + this.CHIP; // kludge to the detector layout view
										// convention
		this.detectorDescriptor.setSectorLayerComponent(sector, layer, 0);
		for (CalibrationData data : cf.getDataList()) {
			data.getDescriptor().setType(DetectorType.BST);
			data.getDescriptor().setSectorLayerComponent(sector, layer, data.getDescriptor().getComponent());
			this.collection.add(data.getDescriptor(), data);
		}
	}

	public static void main(String[] args) {
		AbsDetectorTranslationTable tr = new AbsDetectorTranslationTable();
		tr.readFile("SVT123.table");

		CalibrationDataChip store = new CalibrationDataChip(tr);
		store.readData("/Volumes/data/work/pscan/2017/20170412_1215/svt2_s03_c2_u4");

		TCanvas canvas = new TCanvas("c1", 800, 900);
		canvas.divide(2, 2);

		int sector = (Integer) store.getCollection().getSectors().toArray()[0];
		int layer = (Integer) store.getCollection().getLayers(sector).toArray()[0];
//		System.out.println("sector " + sector + " layer " + layer);

		int anachan = 81;
		for (int chan = 0; chan < 128; ++chan) {
			if (chan != anachan) continue;
			CalibrationData cd = store.getCollection().get(sector, layer, chan);
			cd.analyze();
			for (int pad = 0; pad < 3; pad++) {
				canvas.cd(pad);
				cd.getGraph(pad).setTitle("channel " + chan);
				canvas.draw(cd.getGraph(pad));
				canvas.draw(cd.getFunc(pad), "same");
			}
			canvas.cd(3);
			canvas.draw(cd.getResGraph());
			canvas.draw(cd.getResFunc(), "same");
//			System.out.println("channel " + chan + " " + String.format("%3.1f", cd.getFunc(1).getParameter(3)));
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
