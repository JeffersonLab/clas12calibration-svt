/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.calib.services.svt.calib;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.jlab.calib.services.svt.utils.CalibrationData;
import org.jlab.calib.services.svt.utils.CalibrationDataChip;
import org.jlab.calib.services.svt.utils.CalibrationStore;
import org.jlab.detector.base.DetectorType;
import org.jlab.detector.view.DetectorListener;
import org.jlab.detector.view.DetectorPane2D;
import org.jlab.detector.view.DetectorShape2D;
import org.jlab.groot.base.GStyle;
import org.jlab.groot.data.DataVector;
import org.jlab.groot.data.GraphErrors;
import org.jlab.groot.data.H1F;
import org.jlab.groot.fitter.DataFitter;
import org.jlab.groot.graphics.EmbeddedCanvas;
import org.jlab.groot.graphics.EmbeddedCanvasTabbed;
import org.jlab.groot.math.F1D;
import org.jlab.io.base.DataEvent;
import org.jlab.io.task.DataSourceProcessorPane;
import org.jlab.io.task.IDataEventListener;

/**
 *
 * @author gavalian, gotra
 */
public class CalibrationSuite extends JPanel
		implements DetectorListener, ActionListener, IDataEventListener, ChangeListener, ListSelectionListener {

	private static final long serialVersionUID = 1L;
	private EmbeddedCanvasTabbed canvas = null;
	// main panel
	JPanel pane = null;
	DetectorPane2D detectorView = null;
	JSplitPane splitPane = null;
	// event reading panel
	DataSourceProcessorPane processorPane = null;
	public final int UPDATE_RATE = 50000;
	public static CalibrationSuite calib;

	CalibrationStore calibStore = new CalibrationStore();

	int selectedSector = 1;
	int selectedLayer = 1;
	int selectedChip = 1;
	int nChipsProcessed;

	String[] buttons = { "Summary", "WriteChan", "WriteChip", "DataDir" };
	// button indices
	public final int SUMMARY = 0;
	public final int WRITECHAN = 1;
	public final int WRITECHIP = 2;
	public final int DATADIR = 3;
	public SVTDetector svtDetector;
	public final int NREGIONS = 3;
	public final int NLAYERS = NREGIONS * 2;
	public int[] nChips = new int[NREGIONS + 1];
	public int[] noisyChans = new int[NREGIONS + 1];
	public int[] deadChans = new int[NREGIONS + 1];
	public int[] openChans = new int[NREGIONS + 1];
	public int[] badChans = new int[NREGIONS + 1];
	public double[] percentOperational = new double[NREGIONS + 1];
	public H1F histoSummary;
	public GraphErrors summaryGraph = new GraphErrors();
	private SVTCalibrationTable tablePane;

	private final int SHAPER_TIME_NS = 125;
	private final int BCO_TIME_NS = 128;
	private final int BLR_MODE = 1; // 0:off, 1:on
	private final int GAIN_MODE = 0; // 0:low, 1:high
	
	private final int ADC_THRESHOLD1 = 30;
	private final int ADC_THRESHOLD2 = 45;
	private final int ADC_THRESHOLD3 = 60;
	private final int ADC_THRESHOLD4 = 75;
	private final int ADC_THRESHOLD5 = 90;
	private final int ADC_THRESHOLD6 = 105;
	private final int ADC_THRESHOLD7 = 120;
	private final int ADC_THRESHOLD8 = 135;
	
	public static final String RESET = "\u001B[0m";
	public static final String BLACK = "\u001B[30m";
	public static final String RED = "\u001B[31m";
	public static final String GREEN = "\u001B[32m";
	public static final String YELLOW = "\u001B[33m";
	public static final String BLUE = "\u001B[34m";
	public static final String PURPLE = "\u001B[35m";
	public static final String CYAN = "\u001B[36m";
	public static final String WHITE = "\u001B[37m";
//	private static String LAST_USED_FOLDER = "/Volumes/data/work/pscan/2017";
	private static String LAST_USED_FOLDER = "";
	private String dataDir = "";
	public static JTextArea log;
	private JPanel logPanel;
	private JLabel sectorLabel;
	private JLabel layerLabel;
	private JLabel regionLabel;
	private JLabel chipLabel;
	private JLabel crateLabel;
	private JLabel meanEncLabel;
	private JLabel meanGainLabel;
	private JLabel chanStatusLabel;
	private String summaryInfo = new String();

	public CalibrationSuite() {
		super();

		logPanel = new JPanel();

		this.initDetector();
		this.initUI();
		this.splitPane.setDividerLocation(600);
		svtDetector = new SVTDetector(NREGIONS);
	}

	private void init() {
		nChipsProcessed = 0;
		nChips = new int[NREGIONS + 1];
		noisyChans = new int[NREGIONS + 1];
		deadChans = new int[NREGIONS + 1];
		openChans = new int[NREGIONS + 1];
		badChans = new int[NREGIONS + 1];
		percentOperational = new double[NREGIONS + 1];
		svtDetector = new SVTDetector(NREGIONS);
		calibStore = new CalibrationStore();
	}

	private void initUI() {

		splitPane = new JSplitPane();

		canvas = new EmbeddedCanvasTabbed("Summary", "R1", "R2", "R3", "L1", "L2", "L3", "L4",
				"L5", "L6", "Channel", "Chip", "SVT");
		this.setLayout(new BorderLayout());

		// combined panel for detector view and button panel
		JPanel combined = new JPanel();
		combined.setLayout(new BorderLayout());

		JPanel butPanel = new JPanel();
		for (int i = 0; i < buttons.length; i++) {
			JButton button = new JButton(buttons[i]);
			button.addActionListener(this);
			butPanel.add(button);
		}
		JPanel infoPanel = new JPanel();
		sectorLabel = new JLabel();
		layerLabel = new JLabel();
		regionLabel = new JLabel();
		chipLabel = new JLabel();
		crateLabel = new JLabel();
		meanEncLabel = new JLabel();
		meanGainLabel = new JLabel();
		chanStatusLabel = new JLabel();
		infoPanel.add(layerLabel);
		infoPanel.add(regionLabel);
		infoPanel.add(sectorLabel);
		infoPanel.add(chipLabel);
		infoPanel.add(crateLabel);
		infoPanel.add(meanEncLabel);
		infoPanel.add(meanGainLabel);
		infoPanel.add(chanStatusLabel);

		combined.add(infoPanel, BorderLayout.NORTH);
		combined.add(detectorView, BorderLayout.CENTER);
		combined.add(butPanel, BorderLayout.PAGE_END);

		splitPane.setLeftComponent(combined);
		splitPane.setDividerLocation(0.99);
		splitPane.setResizeWeight(0.99);

		pane = new JPanel();
		pane.setLayout(new BorderLayout());
		JPanel engineView = new JPanel();
		JSplitPane enginePane = null;
		engineView.setLayout(new BorderLayout());
		enginePane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

		enginePane.setTopComponent(canvas);
		enginePane.setBottomComponent(logPanel);
		log = new JTextArea(1, 5);
		log.setEditable(false);
		logPanel.add(log);
		enginePane.setDividerLocation(0.99);
		enginePane.setResizeWeight(0.99);
		engineView.add(splitPane, BorderLayout.CENTER);

		splitPane.setRightComponent(enginePane);
		pane.add(splitPane, BorderLayout.CENTER);

		tablePane = new SVTCalibrationTable();
		tablePane.setOpaque(true);
		tablePane.getTable().setAutoCreateRowSorter(true);
		tablePane.getTable().getSelectionModel().addListSelectionListener(this);
		pane.add(tablePane, BorderLayout.PAGE_END);

		JFrame frame = new JFrame("SVT Calibration");
		frame.setSize(1800, 1000);

		frame.add(pane);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

	}

	private void initDetector() {
		System.out.println("init detector");
		detectorView = new DetectorPane2D();
		int[] sectors = new int[] { 10, 10, 14, 14, 18, 18 };
		double[] raduis = new double[] { 60.0, 65.0, 80.0, 85.0, 100, 105.0 };

		for (int layer = 0; layer < NLAYERS; layer++) {
			for (int sector = 0; sector < sectors[layer]; sector++) {
				for (int chip = 0; chip < 2; chip++) {
					DetectorShape2D shape = new DetectorShape2D(DetectorType.SVT, sector + 1, layer + 1, chip + 1);
					double rotation = sector * (360.0 / sectors[layer]) / 57.29;
					double offset = raduis[layer];
					int width = 15;
					int height = 6;
					shape.createBarXY(width, height);
					shape.getShapePath().translateXYZ(chip * width - 5.0, offset, 0.0);
					shape.getShapePath().rotateZ(rotation);
					detectorView.getView().addShape("SVT", shape);
				}
			}
		}
		this.detectorView.getView().addDetectorListener(this);
		
	}

	public void processShape(DetectorShape2D shape) {
		update(shape);
		selectedSector = shape.getDescriptor().getSector();
		selectedLayer = shape.getDescriptor().getLayer();
		selectedChip = shape.getDescriptor().getComponent();
		int selectedRegion = (selectedLayer % 2 == 0 ? selectedLayer / 2 : (selectedLayer + 1) / 2);
		int chipId = (selectedLayer % 2 == 0 ? selectedChip : selectedChip + 2);
		layerLabel.setText("L" + selectedLayer);
		regionLabel.setText(" R" + selectedRegion);
		sectorLabel.setText(" S" + selectedSector);
		chipLabel.setText(" U" + chipId);
		chanStatusLabel.setText("");
		EmbeddedCanvas canvas = this.canvas.getCanvas("Chip");
		canvas.clear();
		int layerChip = selectedLayer * 10 + selectedChip;

		System.out.println("SELECTED : " + selectedSector + " " + selectedLayer + " " + layerChip + " ");

		CalibrationDataChip chip = this.calibStore.getChips().get(selectedSector, layerChip, 0);

		if (chip == null)
			return;

		System.out.println("L" + selectedLayer + " S" + selectedSector + " U" + chipId + " (C" + chip.crate + " S"
				+ chip.slot + " C" + chip.CHAN + ")");
		meanEncLabel.setText(" ENC " + (int) chip.meanEnc);
		meanGainLabel.setText(" Gain " + (int) chip.meanGain);
		int noisyChans = svtDetector.getSensor(selectedLayer, selectedSector).noisyChannelList.size();
		int deadChans = svtDetector.getSensor(selectedLayer, selectedSector).deadChannelList.size();
		int openChans = svtDetector.getSensor(selectedLayer, selectedSector).openChannelList.size();
		crateLabel.setText(" C" + chip.crate + " S" + chip.slot + " C" + chip.CHAN + " ");
		String chanStatus = "";
		if (noisyChans > 0) {
			chanStatus = " N: " + noisyChans;
		}
		if (deadChans > 0) {
			chanStatus += " D: " + deadChans;
		}
		if (openChans > 0) {
			chanStatus += " O: " + openChans;
		}
		chanStatusLabel.setText(chanStatus);
		chanStatusLabel.setForeground(Color.red);
		crateLabel.setForeground(Color.blue);

		canvas.divide(3, 2);
		canvas.setGridX(false);
		canvas.setGridY(false);

		plotHisto(canvas, 0, chip.getEncGraph(), "Channel", "ENC, e", 0, 1.3);
		gausFit(canvas, 1, chip.getHistoGain(), "Gain, mV/fC", 70, 95, 60, 150);
		plotHisto(canvas, 2, chip.getOffsetGraph(), "Channel", "Offset, mV", -1, -1);
		plotHisto(canvas, 3, chip.getGainGraph(), "Channel", "Gain, mV/fC", 0, 1.3);
		gausFit(canvas, 4, chip.getHistoThreshold(), "Threshold, e", 22000, 27000, 20000, 40000);
		plotHisto(canvas, 5, chip.getVt50Graph(), "Channel", "Vt_50, mV", 0, 1.5);

		double[] dataEnc = chip.chipDataEnc.getVectorY().getArray();
		double[] dataChan = chip.chipDataEnc.getVectorX().getArray();
		int ich = 0;
		for (double enc : dataEnc) {
			int chan = (int) dataChan[ich] + 1;
			svtDetector.chanEncHisto.h.fill(enc);
			svtDetector.getRegion(chip.region).chanEncHisto.h.fill(enc);
			svtDetector.getLayer(chip.layer).chanEncHisto.h.fill(enc);
			if (dataEnc[ich] > chip.HIGHENCTHRESHOLD)
				System.out.println("chan " + chan + " ENC " + (int) dataEnc[ich]);
			else if ((dataEnc[ich] < chip.LOWENCTHRESHOLD1 && chip.CHIP % 2 != 0)
					|| (dataEnc[ich] < chip.LOWENCTHRESHOLD2 && chip.CHIP % 2 == 0 && chan < 100))
				System.out.println("chan " + ((int) dataChan[ich] + 1) + " ENC " + (int) dataEnc[ich]);
			ich++;
		}

		for (int i = 0; i < chip.NCHANNELS; ++i) {
			tablePane.getTable().setValueAt(chip.sector, i, 1);
			tablePane.getTable().setValueAt(chip.layer, i, 2);
			tablePane.getTable().setValueAt(chip.CHIP, i, 3);
			tablePane.getTable().setValueAt("", i, 4);
			if (chip.channelStatus[i] == chip.OPENCHANNEL)
				tablePane.getTable().setValueAt("open", i, 4);
			else if (chip.channelStatus[i] == chip.DEADCHANNEL)
				tablePane.getTable().setValueAt("dead", i, 4);
			else if (chip.channelStatus[i] == chip.NOISYCHANNEL)
				tablePane.getTable().setValueAt("noisy", i, 4);
			tablePane.getTable().setValueAt(chip.channelEnc[i], i, 5);
			tablePane.getTable().setValueAt(chip.channelGain[i], i, 6);
			tablePane.getTable().setValueAt(chip.channelOffset[i], i, 7);
			tablePane.getTable().setValueAt(chip.channelVt50[i], i, 8);
			tablePane.getTable().setValueAt(chip.channelThreshold[i], i, 9);
		}
	}

	public void plotHisto(EmbeddedCanvas canvas, int pad, GraphErrors graph, String xtitle, String ytitle, double y1,
			double y2) {
		canvas.cd(pad);
		graph.setTitleX(xtitle);
		graph.setTitleY(ytitle);
		graph.setMarkerColor(2);
		graph.setMarkerSize(3);
		canvas.draw(graph);
		double ylow;
		double yhigh;
		double y_min = graph.getVectorY().getMin();
		double y_max = graph.getVectorY().getMax();
		ylow = (y_min >= 0 ? y1 * y_min : y2 * y_min);
		yhigh = (y_max >= 0 ? y2 * y_max : y1 * y_max);
		if (y1 == -1 && y_min > -100) ylow = -100;
		if (y2 == -1 && y_max < 100) yhigh = 100;
		if(ylow > y_min) ylow = y_min;
		if(yhigh < y_max) yhigh = y_max;
		canvas.getPad(pad).getAxisY().setRange(ylow, yhigh);
		canvas.update();
	}

	public void gausFit(EmbeddedCanvas canvas, int pad, H1F histo, String xtitle, int xlow, int xhigh, int xmin,
			int xmax) {
		canvas.cd(pad);
		histo.setLineWidth(1);
		canvas.draw(histo, "E");
		histo.setTitleX(xtitle);
		histo.setTitleY("Entries");
		histo.setLineColor(SVTComponent.BLUECOLOR);
		F1D func = new F1D("gaus", "[amp]*gaus(x,[mean],[sigma])", xlow, xhigh);
		func.setLineColor(2);
		func.setLineWidth(2);
		func.setParameter(0, histo.getMaximumBin());
		func.setParameter(1, histo.getMean());
		func.setParameter(2, histo.getRMS());
		DataFitter.fit(func, histo, "WREQ");
		canvas.getPad(pad).getAxisX().setRange(xmin, xmax);
		canvas.draw(func, "same");
	}

	public void dataEventAction(DataEvent de) {
		System.out.println(" Event received with type = " + de.getType());
		detectorView.update();
	}

	public final void updateDetectorView(boolean isNew) {

//		System.out.println("update detector view");
		int[] sectors = new int[] { 10, 10, 14, 14, 18, 18 };
		double[] raduis = new double[] { 60.0, 65.0, 80.0, 85.0, 100, 105.0 };

		for (int layer = 0; layer < NLAYERS; layer++) {
			for (int sector = 0; sector < sectors[layer]; sector++) {
				for (int chip = 0; chip < 2; chip++) {
					DetectorShape2D shape = new DetectorShape2D(DetectorType.SVT, sector + 1, layer + 1, chip + 1);
					double rotation = sector * (360.0 / sectors[layer]) / 57.29;
					double offset = raduis[layer];
					int width = 15;
					int height = 6;
					shape.createBarXY(width, height);
					shape.getShapePath().translateXYZ(chip * width - 5.0, offset, 0.0);
					shape.getShapePath().rotateZ(rotation);
					detectorView.getView().addShape("SVT", shape);
					if (svtDetector.getSensor(layer + 1, sector + 1).badChans[chip] != 0) 
						shape.setColor(128, 0, 0); // maroon
					else shape.setColor(0, 128, 128); // teal (no bad channels found)
				}
			}
		}
		this.detectorView.getView().addDetectorListener(this);

//		System.out.println("shape selected = ");
		int selectedRegion = (selectedLayer % 2 == 0 ? selectedLayer / 2 : (selectedLayer + 1) / 2);
		int chipId = (selectedLayer % 2 == 0 ? selectedChip : selectedChip + 2);
		layerLabel.setText("L" + selectedLayer);
		regionLabel.setText(" R" + selectedRegion);
		sectorLabel.setText(" S" + selectedSector);
		chipLabel.setText(" U" + chipId);
		chanStatusLabel.setText("");
	}

	public void detectorSelected() {
		int sector = 1;
		int layer = 2;
		CalibrationDataChip chip = this.calibStore.getChips().get(sector, layer, 0);
		System.out.println("SELECTED : " + sector + " " + layer);
		if (chip != null) {
			this.canvas.getCanvas("Chip").divide(3, 2);
			EmbeddedCanvas canvas = this.canvas.getCanvas("Chip");

			plotHisto(canvas, 0, chip.getEncGraph(), "Channel", "ENC, e", 0, 1.3);
			gausFit(canvas, 1, chip.getHistoGain(), "Gain, mV/fC", 70, 95, 60, 150);
			plotHisto(canvas, 2, chip.getOffsetGraph(), "Channel", "Offset, mV", 0.3, 5);
			plotHisto(canvas, 3, chip.getGainGraph(), "Channel", "Gain, mV/fC", 0, 1.3);
			gausFit(canvas, 4, chip.getHistoThreshold(), "Threshold, e", 22000, 27000, 20000, 40000);
			plotHisto(canvas, 5, chip.getVt50Graph(), "Channel", "Vt_50, mV", 0, 1.5);
		}
		this.canvas.getCanvas("Chip").repaint();
	}

	public void analyzeData(List<CalibrationDataChip> chipList) {
		nChips[0] = chipList.size();
		for (CalibrationDataChip chip : chipList) {
			nChips[chip.region]++;
			double[] dataGain = chip.chipDataGain.getVectorY().getArray();
			double[] dataEnc = chip.chipDataEnc.getVectorY().getArray();
			double[] dataChan = chip.chipDataEnc.getVectorX().getArray();
			DataVector dataEnc50channels = new DataVector();
			for (int ich = 0; ich < dataChan.length; ++ich) {
				int chan = (int) (dataChan[ich]) - 1;
				double enc = dataEnc[ich];
				int chipId = (chip.layer % 2 == 0 ? chip.CHIP : chip.CHIP + 2);
				String chanInfo = "L" + chip.layer + " R" + chip.region + " S" + chip.sector + " U" + chipId + " N"
						+ chan + " ENC " + (int) enc;
				if (enc < 1) {
					chip.channelStatus[chan] = chip.DEADCHANNEL;
					chip.status[2] = 1;
					svtDetector.deadChannelList.add(chanInfo);
					svtDetector.badChannels++;
					svtDetector.getRegion(chip.region).deadChannelList.add(chanInfo);
					svtDetector.getRegion(chip.region).badChannels++;
					svtDetector.getLayer(chip.layer).deadChannelList.add(chanInfo);
					svtDetector.getLayer(chip.layer).badChannels++;
					svtDetector.getSensor(chip.layer, chip.sector).deadChannelList.add(chanInfo);
					svtDetector.getSensor(chip.layer, chip.sector).badChannels++;
					svtDetector.getSensor(chip.layer, chip.sector).badChans[chip.CHIP - 1]++;
				} else if (enc > chip.HIGHENCTHRESHOLD) {
					chip.channelStatus[chan] = chip.NOISYCHANNEL;
					chip.status[0] = 1;
					svtDetector.noisyChannelList.add(chanInfo);
					svtDetector.badChannels++;
					svtDetector.getRegion(chip.region).noisyChannelList.add(chanInfo);
					svtDetector.getRegion(chip.region).badChannels++;
					svtDetector.getLayer(chip.layer).noisyChannelList.add(chanInfo);
					svtDetector.getLayer(chip.layer).badChannels++;
					svtDetector.getSensor(chip.layer, chip.sector).noisyChannelList.add(chanInfo);
					svtDetector.getSensor(chip.layer, chip.sector).badChannels++;
					svtDetector.getSensor(chip.layer, chip.sector).badChans[chip.CHIP - 1]++;
				} else if ((enc < chip.LOWENCTHRESHOLD1 && chip.CHIP % 2 != 0)
						|| (enc < chip.LOWENCTHRESHOLD2 && chip.CHIP % 2 == 0 && chan < 100)) {
					chip.channelStatus[chan] = chip.OPENCHANNEL;
					chip.status[1] = 1;
					svtDetector.openChannelList.add(chanInfo);
					svtDetector.badChannels++;
					svtDetector.getRegion(chip.region).openChannelList.add(chanInfo);
					svtDetector.getRegion(chip.region).badChannels++;
					svtDetector.getLayer(chip.layer).openChannelList.add(chanInfo);
					svtDetector.getLayer(chip.layer).badChannels++;
					svtDetector.getSensor(chip.layer, chip.sector).openChannelList.add(chanInfo);
					svtDetector.getSensor(chip.layer, chip.sector).badChannels++;
					svtDetector.getSensor(chip.layer, chip.sector).badChans[chip.CHIP - 1]++;
				} else
					chip.channelStatus[chan] = 0;
				if (chan <= 50 && dataEnc[ich] > 1) // exclude dead channels
					dataEnc50channels.add(dataEnc[ich]);
			}
			DataVector gainVector = new DataVector();
			for (int i = 0; i < dataGain.length; ++i) {
				if (dataGain[i] > 1) // exclude dead channels
					gainVector.add(dataGain[i]);
			}
			double meanGain = gainVector.getMean();
			double meanEnc = (chip.CHIP == 1 ? chip.chipDataEnc.getVectorY().getMean() : dataEnc50channels.getMean());
			chip.meanEnc = meanEnc;
			chip.meanGain = meanGain;
		}
	}

	public void plotData(List<CalibrationDataChip> chipList) {
		for (CalibrationDataChip chip : chipList) {
			double[] dataGain = chip.chipDataGain.getVectorY().getArray();
			for (double gain : dataGain) {
				svtDetector.chanGainHisto.h.fill(gain);
				svtDetector.getRegion(chip.region).chanGainHisto.h.fill(gain);
				svtDetector.getLayer(chip.layer).chanGainHisto.h.fill(gain);
			}
			double[] dataEnc = chip.chipDataEnc.getVectorY().getArray();
			double[] dataChan = chip.chipDataEnc.getVectorX().getArray();
			DataVector dataEnc50channels = new DataVector();
			int ich = 0;
			for (double enc : dataEnc) {
				svtDetector.chanEncHisto.h.fill(enc);
				svtDetector.getRegion(chip.region).chanEncHisto.h.fill(enc);
				svtDetector.getLayer(chip.layer).chanEncHisto.h.fill(enc);
				if (dataChan[ich] <= 50)
					dataEnc50channels.add(dataEnc[ich]);
				ich++;
			}
			double meanGain = chip.chipDataGainProj.getMean();
			double rmsGain = chip.chipDataGainProj.getRMS();
			double meanEnc = (chip.CHIP == 1 ? chip.chipDataEnc.getVectorY().getMean() : dataEnc50channels.getMean());
			double rmsEnc = (chip.CHIP == 1 ? chip.chipDataEnc.getVectorY().getRMS() : dataEnc50channels.getRMS());
			chip.meanEnc = meanEnc;
			chip.meanGain = meanGain;
			chip.rmsEnc = rmsEnc;
			chip.rmsGain = rmsGain;
			svtDetector.meanGainHisto.h.fill(meanGain);
			svtDetector.meanEncHisto.h.fill(meanEnc);
			svtDetector.getRegion(chip.region).meanGainHisto.h.fill(meanGain);
			svtDetector.getRegion(chip.region).meanEncHisto.h.fill(meanEnc);
			svtDetector.getLayer(chip.layer).meanGainHisto.h.fill(meanGain);
			svtDetector.getLayer(chip.layer).meanEncHisto.h.fill(meanEnc);
		}
		svtDetector.fitHistos();
		svtDetector.drawHistos(this.canvas.getCanvas("SVT"));
		for (int r = 1; r <= NREGIONS; ++r) {
			svtDetector.getRegion(r).fitHistos();
			svtDetector.getRegion(r).drawHistos(this.canvas.getCanvas("R" + r));
		}
		for (int l = 1; l <= NLAYERS; ++l) {
			svtDetector.getLayer(l).fitHistos();
			svtDetector.getLayer(l).drawHistos(this.canvas.getCanvas("L" + l));
		}
	}

	public void prepareSummaryInfo() {
		summaryInfo = "                                                           \n";
		for (String ch : svtDetector.noisyChannelList) {
			summaryInfo += ch + " N\n";
		}
		for (String ch : svtDetector.deadChannelList) {
			summaryInfo += ch + " D\n";
		}
		for (String ch : svtDetector.openChannelList) {
			summaryInfo += ch + " O\n";
		}
		for (int r = 1; r <= NREGIONS; ++r) {
			if (nChips[r] == 0 || nChips[0] == 0)
				continue;
			summaryInfo += "======= Region " + r + " =======\n";
			noisyChans[r] = svtDetector.getRegion(r).noisyChannelList.size();
			deadChans[r] = svtDetector.getRegion(r).deadChannelList.size();
			openChans[r] = svtDetector.getRegion(r).openChannelList.size();
			badChans[r] = noisyChans[r] + deadChans[r] + openChans[r];
			if (badChans[r] != 0) {
				summaryInfo += badChans[r] + " bad channels: ";
			}
			if (noisyChans[r] != 0)
				summaryInfo += noisyChans[r] + " noisy ";
			if (deadChans[r] != 0)
				summaryInfo += deadChans[r] + " dead ";
			if (openChans[r] != 0)
				summaryInfo += openChans[r] + " open ";
			if (badChans[r] != 0)
				summaryInfo += "\n";
			percentOperational[r] = 100.0 - badChans[r] * 100.0 / (nChips[r] * 128.0);
			// System.out.println(r + " " + badChans[r] + " " + nChips[r]);
			summaryInfo += "Operational: " + String.format("%3.2f", percentOperational[r]) + "%\n";
			summaryInfo += "Mean Chip ENC: " + (int) svtDetector.getRegion(r).meanEncHisto.h.getMean() + "\n"
					+ "Mean Chip Gain: " + (int) svtDetector.getRegion(r).meanGainHisto.h.getMean() + "\n";
		}
		noisyChans[0] = svtDetector.noisyChannelList.size();
		deadChans[0] = svtDetector.deadChannelList.size();
		openChans[0] = svtDetector.openChannelList.size();
		badChans[0] = noisyChans[0] + deadChans[0] + openChans[0];
		summaryInfo += "======== SVT =========\n";
		if (badChans[0] != 0)
			summaryInfo += badChans[0] + " bad channels: ";
		if (noisyChans[0] != 0)
			summaryInfo += noisyChans[0] + " noisy ";
		if (deadChans[0] != 0)
			summaryInfo += deadChans[0] + " dead ";
		if (openChans[0] != 0)
			summaryInfo += openChans[0] + " open ";
		if (badChans[0] != 0)
			summaryInfo += "\n";
		percentOperational[0] = 100.0 - badChans[0] * 100.0 / (nChips[0] * 128.0);
		summaryInfo += "Operational: " + String.format("%3.2f", percentOperational[0]) + "%\n";
		summaryInfo += "Mean Chip ENC: " + (int) svtDetector.meanEncHisto.h.getMean() + "\n" + "Mean Chip Gain: "
				+ (int) svtDetector.meanGainHisto.h.getMean() + "\n";
		for (int r = 1; r <= NREGIONS; ++r) {
			nChipsProcessed += nChips[r];
		}
		summaryInfo += "\nChips processed: " + nChipsProcessed + "\n";
		System.out.println(summaryInfo);
		tablePane.printout(summaryInfo);

		EmbeddedCanvas canvas = this.canvas.getCanvas("Summary");
		canvas.clear();
		canvas.setGridY(false);
		GStyle.getH1FAttributes().setOptStat("0000");
		summaryGraph.setTitle("Summary");
		summaryGraph.setTitleX("Region");
		summaryGraph.setTitleY("Operational, %");
		summaryGraph.setMarkerColor(SVTComponent.GREENCOLOR);
		summaryGraph.setMarkerSize(6);
		histoSummary = new H1F("summary", "Region", "Operational Channels, %", NREGIONS + 1, -0.5, NREGIONS + 0.5);
		histoSummary.setFillColor(SVTComponent.GREENCOLOR);
		for (int r = 0; r <= NREGIONS; ++r) {
			if (nChips[r] == 0)
				continue;
			histoSummary.setBinContent(r, percentOperational[r]);
		}
		canvas.draw(histoSummary);
		canvas.getPad(0).getAxisY().setRange(99, 100);
		GraphErrors line = new GraphErrors();
		double yMin = canvas.getPad(0).getAxisY().getRange().getMin();
		double yMax = canvas.getPad(0).getAxisY().getRange().getMax();
		for (int i = 0; i < 3; ++i) {
			line.addPoint(i + 0.5, (yMax - yMin) / 2.0, 0, (yMax - yMin) / 2.0);
			line.setMarkerSize(0);
			line.setLineColor(1);
			line.setLineThickness(2);
			line.getAttributes().setLineStyle(2);
			canvas.draw(line, "same");
		}
		canvas.update();
	}

	public void timerUpdate() {
	}

	public void update(DetectorShape2D dsd) {
		System.out.println("update");

		int sector = dsd.getDescriptor().getSector();
		int layer = dsd.getDescriptor().getLayer();
		CalibrationDataChip chip = this.calibStore.getChips().get(sector, layer, 0);
		if (chip == null) {
			dsd.setColor(180, 180, 180); // grey
		} else if (layer % 2 == 0) {
			dsd.setColor(180, 255, 180); // light green
		} else {
			dsd.setColor(180, 180, 255); // lilac
		}
		if (svtDetector.getSensor(layer, sector).badChannels > 0)
			dsd.setColor(180, 140, 0); // brown
	}

	public void resetEventListener() {
	}

	public void writeCalibData() {
		// CalibrationConstants constants = new CalibrationConstants(5);
	}

	public void actionPerformed(ActionEvent e) {

		System.out.println("action " + e.getActionCommand());

		if (e.getActionCommand().compareTo(buttons[SUMMARY]) == 0) {

			JFrame summaryFrame = new JFrame("Summary");
			summaryFrame.setSize(1000, 800);
			summaryFrame.setLayout(new GridLayout());
			JTextArea textArea = new JTextArea(summaryInfo);
			System.out.println(summaryInfo);
			JScrollPane scrollPane = new JScrollPane(textArea);
			setPreferredSize(new Dimension(950, 110));
			add(scrollPane, BorderLayout.CENTER);

			summaryFrame.add(scrollPane);
			summaryFrame.pack();
			summaryFrame.setVisible(true);
			summaryFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		} else if (e.getActionCommand().compareTo(buttons[WRITECHAN]) == 0) {

			List<CalibrationDataChip> chipList = this.calibStore.getChips().getList();
			try {
				String calibFile = "svtchannelcalibration.txt";
				BufferedWriter writer = new BufferedWriter(new FileWriter(calibFile));

				for (CalibrationDataChip chip : chipList) {
					for (int i = 0; i < chip.NCHANNELS; ++i) {
						String str = chip.sector + " " + chip.layer + " " + chip.CHIP + " " + (i + 1) + " "
								+ chip.channelStatus[i] + " " + chip.channelEnc[i] + " " + chip.channelGain[i] + " "
								+ chip.channelOffset[i] + " " + chip.channelVt50[i] + " " + chip.channelThreshold[i]
								+ "\n";
						writer.write(str);
					}
				}
				writer.close();
				System.out.println("calibration constants written to the file: " + calibFile);
				System.out.println(chipList.size() + " chips processed");
				tablePane.printout("calibration constants written to the file: " + calibFile);
				tablePane.printout(chipList.size() + " chips processed");
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		} else if (e.getActionCommand().compareTo(buttons[WRITECHIP]) == 0) {

			List<CalibrationDataChip> chipList = this.calibStore.getChips().getList();
			try {
				String calibFile = "svtchipcalibration.txt";
				BufferedWriter writer = new BufferedWriter(new FileWriter(calibFile));

				for (CalibrationDataChip chip : chipList) {
					double meanOffset = chip.chipDataOffset.getVectorY().getMean();
					double rmsOffset = chip.chipDataOffset.getVectorY().getRMS();
					double meanVt50 = chip.chipDataVt50.getVectorY().getMean();
					double rmsVt50 = chip.chipDataVt50.getVectorY().getRMS();
					double rmsThreshold = chip.chipDataThresholdProj.getRMS();
					String str = chip.sector + " " + chip.layer + " " + chip.CHIP + " "
							+ String.format("%3.0f", chip.meanEnc) + " " + String.format("%3.0f", chip.rmsEnc) + " "
							+ String.format("%3.0f", chip.meanGain) + " " + String.format("%3.0f", chip.rmsGain) + " "
							+ String.format("%3.0f", meanOffset) + " " + String.format("%3.0f", rmsOffset) + " "
							+ String.format("%3.0f", meanVt50) + " " + String.format("%3.0f", rmsVt50) + " "
							+ String.format("%3.0f", rmsThreshold) + " "
							+ GAIN_MODE + " " + BLR_MODE + " " + BCO_TIME_NS + " " + SHAPER_TIME_NS + " " 
							+ ADC_THRESHOLD1 + " " + ADC_THRESHOLD2 + " " + ADC_THRESHOLD3 + " " + ADC_THRESHOLD4 + " "
							+ ADC_THRESHOLD5 + " " + ADC_THRESHOLD6 + " " + ADC_THRESHOLD7 + " " + ADC_THRESHOLD8 + " "+ "\n";
					writer.write(str);
				}
				writer.close();
				System.out.println("calibration constants written to the file: " + calibFile);
				System.out.println(chipList.size() + " chips processed");
				tablePane.printout("calibration constants written to the file: " + calibFile);
				tablePane.printout(chipList.size() + " chips processed");
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		} else if (e.getActionCommand().compareTo(buttons[DATADIR]) == 0) {
			selectDataDir();
		}
	}

	public void selectDataDir() {
		Preferences prefs = Preferences.userRoot().node(getClass().getName());
		JFileChooser fc = new JFileChooser(prefs.get(LAST_USED_FOLDER, new File("").getAbsolutePath()));
		fc.setCurrentDirectory(new java.io.File(LAST_USED_FOLDER));
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		int returnVal = fc.showOpenDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File yourFolder = fc.getSelectedFile();
			LAST_USED_FOLDER = yourFolder.getPath();
			prefs.put(LAST_USED_FOLDER, yourFolder.getPath());
			tablePane.printout(LAST_USED_FOLDER);
			calib.dataDir = LAST_USED_FOLDER;
		}
		else calib.dataDir = "";
	}

	public static void main(String[] args) {
		calib = new CalibrationSuite();
		while (true) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if (!calib.dataDir.equals("")) {
				calib.init();
				calib.calibStore.readData(LAST_USED_FOLDER);
				calib.calibStore.analyze();
				List<CalibrationDataChip> chipList = calib.calibStore.getChips().getList();
				calib.plotData(chipList);
				calib.analyzeData(chipList);
				calib.prepareSummaryInfo();
				calib.updateDetectorView(false);
				calib.dataDir = "";
			}
		}
	}

	public void stateChanged(ChangeEvent e) {
		System.out.println("stateChanged");
	}

	public void valueChanged(ListSelectionEvent e) {
		if (e.getValueIsAdjusting()) {
			return;
		}
		int chan = tablePane.getTable().getSelectedRow();
		System.out.println("channel: " + chan);

		int layerChip = selectedLayer * 10 + selectedChip;
		CalibrationDataChip chip = this.calibStore.getChips().get(selectedSector, layerChip, 0);

		int sector = selectedSector;

		CalibrationData cd = chip.getCollection().get(sector, layerChip, chan);
		EmbeddedCanvas canvas = this.canvas.getCanvas("Channel");
		canvas.clear();

		int[] amplitude = { 75, 100, 125 };
		canvas.divide(2, 2);
		for (int pad = 0; pad < 3; pad++) {
			canvas.cd(pad);
			cd.getGraph(pad).setTitle("channel " + chan + ", amplitude " + amplitude[pad] + " mV");
			cd.getGraph(pad).setMarkerColor(2);
			cd.getGraph(pad).setMarkerSize(5);
			canvas.draw(cd.getGraph(pad));
			canvas.draw(cd.getFunc(pad), "same");
		}
		canvas.cd(3);
		canvas.draw(cd.getResGraph());
		canvas.draw(cd.getResFunc(), "same");
		cd.getResGraph().setMarkerColor(2);
		cd.getResFunc().setLineColor(4);
	}
}
