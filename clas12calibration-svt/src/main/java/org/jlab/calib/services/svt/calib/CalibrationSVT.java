/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.calib.services.svt.calib;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.jlab.detector.calib.utils.CalibrationConstants;
import org.jlab.detector.base.DetectorDescriptor;

import org.jlab.detector.view.DetectorShape2D;
import org.jlab.calib.services.svt.utils.CalibrationData;
import org.jlab.calib.services.svt.utils.CalibrationDataChip;
import org.jlab.calib.services.svt.utils.CalibrationStore;
import org.jlab.groot.graphics.EmbeddedCanvas;
import org.jlab.groot.fitter.DataFitter;
import org.jlab.groot.math.F1D;
import org.jlab.groot.data.GraphErrors;
import org.jlab.groot.data.H1F;
import org.jlab.detector.base.DetectorType;
import org.jlab.detector.view.DetectorListener;
import org.jlab.detector.view.DetectorPane2D;

/**
 *
 * @author gavalian, gotra
 */
public class CalibrationSVT extends JPanel implements DetectorListener,
         ActionListener {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private EmbeddedCanvas canvas = new EmbeddedCanvas();
    private EmbeddedCanvas canvas1 = new EmbeddedCanvas();
    CalibrationStore calibStore = new CalibrationStore();
    private JTabbedPane tabbedPane;
    public String[] buttonNames = {"SVT", "R1", "R2", "R3", "R4", "L1", "L2", "L3", "L4", "L5", "L6", "L7", "L8"};

    public CalibrationSVT() {
        this.initDetector();
        this.init();
    }

    public void init() {

        tabbedPane = new JTabbedPane();
        tabbedPane.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                JTabbedPane tabbedPane = (JTabbedPane) e.getSource();
            }
        });

        this.tabbedPane.add("Fitting", canvas);
        this.tabbedPane.add("Chip View", canvas1);

        JButton button = new JButton(this.buttonNames[0]);
        button.addActionListener(this);
        JButton buttonR1 = new JButton(this.buttonNames[1]);
        buttonR1.addActionListener(this);
        JButton buttonR2 = new JButton(this.buttonNames[2]);
        buttonR2.addActionListener(this);
        JButton buttonR3 = new JButton(this.buttonNames[3]);
        buttonR3.addActionListener(this);
        JButton buttonR4 = new JButton(this.buttonNames[4]);
        buttonR4.addActionListener(this);
        JButton buttonL1 = new JButton(this.buttonNames[5]);
        buttonL1.addActionListener(this);
        JButton buttonL2 = new JButton(this.buttonNames[6]);
        buttonL2.addActionListener(this);
        JButton buttonL3 = new JButton(this.buttonNames[7]);
        buttonL3.addActionListener(this);
        JButton buttonL4 = new JButton(this.buttonNames[8]);
        buttonL4.addActionListener(this);
        JButton buttonL5 = new JButton(this.buttonNames[9]);
        buttonL5.addActionListener(this);
        JButton buttonL6 = new JButton(this.buttonNames[10]);
        buttonL6.addActionListener(this);
        JButton buttonL7 = new JButton(this.buttonNames[11]);
        buttonL7.addActionListener(this);
        JButton buttonL8 = new JButton(this.buttonNames[12]);
        buttonL8.addActionListener(this);
    }

    public void initDetector() {

    	DetectorPane2D detectorView = null;
    	DetectorShape2D view = new DetectorShape2D();
	view.getDescriptor().setType(DetectorType.SVT);
	detectorView.getView().addDetectorListener(this);
    }

    public void writeCalibData() {
        CalibrationConstants  constants = new CalibrationConstants(5);
    }
    
    public void detectorSelected(DetectorDescriptor dd) {
        int sector = dd.getSector();
        int layer = dd.getLayer();
        int chipid = dd.getLayer() - 10 * layer;
        CalibrationDataChip chip = this.calibStore.getChips().get(sector, layer, 0);

        System.out.println("SELECTED : " + dd.toString());
        if (chip != null) {
            this.canvas.divide(3, 2);
            this.canvas.cd(0);
            GraphErrors graphEnc = chip.getEncGraph();
            graphEnc.setTitle(CalibrationData.histoTitle);
            graphEnc.setTitleX("Channel");
            graphEnc.setTitleY("ENC, e");
            graphEnc.setMarkerSize(6);
            this.canvas.draw(graphEnc);
            this.canvas.cd(3);
            GraphErrors graph = chip.getGainGraph();
            graph.setTitle(CalibrationData.histoTitle);
            graph.setTitleX("Channel");
            graph.setTitleY("Gain, mV/fC");
            graph.setMarkerSize(6);
            this.canvas.draw(graph);
            this.canvas.cd(1);
            this.canvas.draw(chip.getHistoGain());
            chip.getHistoGain().setLineWidth(2);
            chip.getHistoGain().setFillColor(3);
            chip.getHistoGain().setTitle(CalibrationData.histoTitle);
            chip.getHistoGain().setTitleX("Gain, mV/fC");
            chip.getHistoGain().setTitleY("Entries");
            final int GAIN_LOW = 70;
            final int GAIN_HIGH = 95;
            F1D fgain = new F1D("gaus","[amp]*gaus([mean],[sigma])", GAIN_LOW, GAIN_HIGH);
            fgain.setLineColor(2);
            fgain.setLineWidth(2);
            fgain.setParameter(0, chip.getHistoGain().getMaximumBin());
            fgain.setParameter(1, chip.getHistoGain().getMean());
            fgain.setParameter(2, chip.getHistoGain().getRMS());
            DataFitter.fit(fgain,chip.getHistoGain(),"REQ");
            this.canvas.draw(fgain, "same");

            this.canvas.cd(4);
            chip.getHistoThreshold().setLineWidth(2);
            chip.getHistoThreshold().setFillColor(3);
            this.canvas.draw(chip.getHistoThreshold());
            chip.getHistoThreshold().setTitle(CalibrationData.histoTitle);
            chip.getHistoThreshold().setTitleX("Threshold, e");
            chip.getHistoThreshold().setTitleY("Entries");
            final int Threshold_LOW = 22000;
            final int Threshold_HIGH = 27000;
            F1D fthreshold = new F1D("gaus","[amp]*gaus([mean],[sigma])", Threshold_LOW, Threshold_HIGH);
            fthreshold.setLineColor(2);
            fthreshold.setLineWidth(2);
            fthreshold.setParameter(0, chip.getHistoThreshold().getMaximumBin());
            fthreshold.setParameter(1, chip.getHistoThreshold().getMean());
            fthreshold.setParameter(2, chip.getHistoThreshold().getRMS());
            DataFitter.fit(fgain,chip.getHistoThreshold(),"REQ");
            this.canvas.draw(fthreshold, "same");
            this.canvas.cd(2);
            GraphErrors graphOffset = chip.getOffsetGraph();
            graphOffset.setTitle(CalibrationData.histoTitle);
            graphOffset.setTitleX("Channel");
            graphOffset.setTitleY("Offset, mV");
            graphOffset.setMarkerSize(6);
            this.canvas.getPad(2).setAxisRange(-1.0, 129, -100.0, 100.0);
            this.canvas.draw(graphOffset);
            this.canvas.cd(5);
            GraphErrors graphVt50 = chip.getVt50Graph();
            graphVt50.setTitle(CalibrationData.histoTitle);
            graphVt50.setTitleX("Channel");
            graphVt50.setTitleY("Vt_50, mV");
            graphVt50.setMarkerSize(6);
            this.canvas.getPad(5).setAxisRange(-1.0, 129, 0.0, 400.0);
            this.canvas.draw(graphVt50);
        }
    }

    public void readData(String file) {
        this.calibStore.readData("/Volumes/data/work/pscan/test2");
        this.calibStore.analyze();
        System.out.println("analyze complete");
        List<CalibrationDataChip> chips = this.calibStore.getChips().getList();
        DecimalFormat nf = new DecimalFormat("####");
        nf.setRoundingMode(RoundingMode.CEILING);
        for (CalibrationDataChip chip : chips) {
            double[] encData = chip.getEncGraph().getVectorY().getArray();
            int layer = chip.layer;
            int chan = chip.CHAN;
            System.out.println(layer+" "+chan);
            double[] encData50Channels = Arrays.copyOfRange(encData, 0, 49);
            double sum = 0;
            int nChannels = 0;
            for (int i = 0; i < encData50Channels.length; ++i) {
                System.out.println(i+" "+encData50Channels[i]);
                if(encData50Channels[i]>0) {
                    sum += encData50Channels[i];
                    nChannels++;
                }
            }
            if(nChannels!=0) chip.meanEnc = sum / nChannels;
            else {
                System.out.println("Array length zero, nChannels");
                return;
            }
            if (chip.meanEnc > 2000) {
            }
        }
        for (CalibrationDataChip chip : chips) {
            double[] encData = chip.getEncGraph().getVectorY().getArray();
            System.out.println("enc threshold "+1.2 * chip.meanEnc);
            for (int i = 0; i < encData.length; ++i) {
                {
                    System.out.println("R" + chip.region + "S" + chip.detectorDescriptor.getSector() + "L" + chip.layer + "U" + chip.CHIP + "N" + (i + 1) + " " + chip.CHAN + " " + Double.parseDouble(nf.format(encData[i])) + " " + Double.parseDouble(nf.format(chip.meanEnc)));
                }
            }
        }
        System.out.println(chips.size() + " chips");
    }

    public void update(DetectorShape2D dsd) {
        int sector = dsd.getDescriptor().getSector();
        int layer = dsd.getDescriptor().getLayer();
        CalibrationDataChip chip = this.calibStore.getChips().get(sector, layer, 0);
        if (chip == null) {
            dsd.setColor(180, 180, 180);
        } else if (layer % 2 == 0) {
            dsd.setColor(180, 255, 180);
        } else {
            dsd.setColor(180, 180, 255);
        }
    }

    public void entrySelected(int i, int i1, int i2) {
        System.out.println("CALLBACK selecting the data " + i + " " + i1 + " " + i2);

        CalibrationDataChip dataChip = this.calibStore.getChips().get(i, i1, 0);

        if (dataChip != null) {
            CalibrationData data = dataChip.getData(i, i1, i2);
            if (data != null) {
                this.canvas.divide(2, 2);
                for (int loop = 0; loop < 3; loop++) {
                    this.canvas.cd(loop);
                    data.getGraph(loop).setLineColor(2);
                    data.getGraph(loop).setMarkerSize(6);
                    this.canvas.draw(data.getGraph(loop));
                    data.getFunc(loop).setLineWidth(2);
                    data.getFunc(loop).setLineColor(3);
                    data.getFunc(loop).setLineStyle(2);

                    this.canvas.draw(data.getFunc(loop), "same");
                }
                this.canvas.cd(3);
                data.getResGraph().setMarkerStyle(4);

                this.canvas.draw(data.getResGraph());
                data.getResFunc().setLineColor(1);
                data.getResFunc().setLineStyle(3);
                data.getResFunc().setLineWidth(2);

                this.canvas.draw(data.getResFunc(), "same");
            } else {
                System.out.println("Oooooops this is null");
            }
        } else {
            System.out.println("----> error finding chip");
        }
    }

    public void actionPerformed(ActionEvent e) {
        int regionLayerFlag = -1;
        for (int i = 0; i < this.buttonNames.length; ++i) {
            if (e.getActionCommand().compareTo(this.buttonNames[i]) == 0) {
                regionLayerFlag = i;
            }
        }
        if (regionLayerFlag != -1) {
            H1F svtGainHisto = new H1F("hSvtGain", "", 100, 0.0, 110.0);
            svtGainHisto.setTitle("SVT Mean Chip Gain");
            svtGainHisto.setTitleX("Gain, mV/fC");
            svtGainHisto.setTitleY("Entries");
            svtGainHisto.setFillColor(4);

            H1F svtEncHisto = new H1F("hSvtEnc", "", 100, 0.0, 3000.0);
            svtEncHisto.setTitle("SVT Mean Chip ENC");
            svtEncHisto.setTitleX("ENC, e");
            svtEncHisto.setTitleY("Entries");
            svtEncHisto.setFillColor(3);

            H1F svtChanGainHisto = new H1F("hSvtChanGain", "", 100, 0.0, 110.0);
            svtChanGainHisto.setTitle("SVT Channel Gain");
            svtChanGainHisto.setTitleX("Gain, mV/fC");
            svtChanGainHisto.setTitleY("Entries");
            svtChanGainHisto.setFillColor(4);
            final int GAIN_LOW = 70;
            final int GAIN_HIGH = 95;
            F1D fgain = new F1D("gaus","[amp]*gaus([mean],[sigma])", GAIN_LOW, GAIN_HIGH);
            fgain.setLineColor(2);
            fgain.setLineWidth(2);
            fgain.setParameter(1, 85);
            fgain.setParameter(2, 2.5);

            H1F svtChanEncHisto = new H1F("hSvtChanEnc", "", 100, 0.0, 4000.0);
            svtChanEncHisto.setTitle("SVT Channel ENC");
            svtChanEncHisto.setTitleX("ENC, e");
            svtChanEncHisto.setTitleY("Entries");
            svtChanEncHisto.setFillColor(3);
            final int ENC_LOW = 1450;
            final int ENC_HIGH = 1750;
            F1D fenc = new F1D("gaus","[amp]*gaus([mean],[sigma])", ENC_LOW, ENC_HIGH);
            fenc.setLineColor(2);
            fenc.setLineWidth(2);
            fenc.setParLimits(1, 1500.0, 1700.0);

            List<CalibrationDataChip> chipList = this.calibStore.getChips().getList();
            for (CalibrationDataChip chip : chipList) {
                if (regionLayerFlag > 0 && regionLayerFlag < 5 && chip.region != regionLayerFlag) {
                    continue;
                } else if (regionLayerFlag > 4 && chip.layer != regionLayerFlag - 4) {
                    continue;
                }
                double[] dataGain = chip.chipDataGain.getVectorY().getArray();
                for (double d : dataGain) {
                    svtChanGainHisto.fill(d);
                }
                double[] dataEnc = chip.chipDataEnc.getVectorY().getArray();
                for (double d : dataEnc) {
                    svtChanEncHisto.fill(d);
                    System.out.println("ENC " + d);
                }
                System.out.println(">>> ");

                double[] channelEnc = chip.chipDataEnc.getVectorX().getArray();
                for (double d : channelEnc) {
                    System.out.println("channel " + d);
                }
               
                svtGainHisto.fill(chip.chipDataGainProj.getMean());
                svtEncHisto.fill(chip.chipDataEnc.getVectorY().getMean());
            }

            this.canvas.divide(2, 2);
            this.canvas.cd(0);
            this.canvas.draw(svtEncHisto);
            this.canvas.cd(1);
            this.canvas.draw(svtChanEncHisto);
            fenc.setParameter(0, svtChanEncHisto.getMaximumBin());
            fenc.setParameter(1, 1600);
            fenc.setParameter(2, 70);
            DataFitter.fit(fenc, svtChanEncHisto, "REQ");
            this.canvas.draw(fenc, "same");
            this.canvas.cd(2);
            this.canvas.draw(svtGainHisto);
            this.canvas.cd(3);
            this.canvas.draw(svtChanGainHisto);
            fgain.setParameter(0, svtChanGainHisto.getMaximumBin());
            fgain.setParameter(1, svtChanGainHisto.getMean());
            fgain.setParameter(2, svtChanGainHisto.getRMS());
            DataFitter.fit(fgain, svtChanGainHisto, "REQ");
            this.canvas.draw(fgain, "same");
        }
    }

    public static void main(String[] args) {

        DataFitter.FITPRINTOUT = false;
        JFrame frame = new JFrame();
        frame.setSize(1200, 700);
        CalibrationSVT calib = new CalibrationSVT();
        frame.pack();
        frame.setVisible(true);
        calib.readData("");
    }

    public void processShape(DetectorShape2D dsd) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
