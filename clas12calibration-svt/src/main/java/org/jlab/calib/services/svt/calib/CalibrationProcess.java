/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.calib.services.svt.calib;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.List;
import java.util.TreeMap;

import org.jlab.calib.services.svt.utils.CalibrationDataChip;
import org.jlab.calib.services.svt.utils.CalibrationStore;

/**
 *
 * @author gotra
 */
public class CalibrationProcess {
    
    CalibrationStore calibStore = new CalibrationStore();

    public void readData(String file) {
        this.calibStore.readData("/Volumes/data/work/pscan/test2");
        this.calibStore.analyze();
        System.out.println("analyze complete");
        List<CalibrationDataChip> chips = this.calibStore.getChips().getList();
        DecimalFormat nf = new DecimalFormat("####");
        nf.setRoundingMode(RoundingMode.CEILING);
        for (CalibrationDataChip chip : chips) {
            double[] encData = chip.getEncGraph().getVectorY().getArray();
            double[] channelData = chip.getEncGraph().getVectorX().getArray();
            TreeMap<Integer, Double> encMap = new TreeMap<Integer, Double>();
            for (int i = 0; i < chip.getEncGraph().getVectorX().getSize(); ++i) {
            encMap.put((int) channelData[i], encData[i]);
            }
            int layer = chip.layer;
            int chan = chip.CHAN;
            System.out.println(layer + " " + chan);
//            double[] encData50Channels = Arrays.copyOfRange(encData, 0, 49);
            double[] encData50Channels = new double[50];
            for (int i = 0; i < 50; ++i) encData50Channels[i] = encMap.get(i + 1);
            double sum = 0;
            int nChannels = 0;
            for (int i = 0; i < encData50Channels.length; ++i) {
                System.out.println(i + " " + encData50Channels[i]);
                if (encData50Channels[i] > 0) {
                    sum += encData50Channels[i];
                    nChannels++;
                }
            }
            if (nChannels != 0) {
                chip.meanEnc = sum / nChannels;
            } else {
                System.out.println("Array length zero, nChannels");
                return;
            }
            if (chip.meanEnc > chip.HIGHENCTHRESHOLD) {
//                System.out.println("High noise L" + chip.layer + " S" + chip.detectorDescriptor.getSector() + " U" + chip.CHIP + " " + Double.parseDouble(nf.format(chip.meanEnc)));
            }
            System.out.println("enc threshold " + 1.2 * chip.meanEnc);
            for (int i = 0; i < encMap.size(); ++i) {
//                if (encData[i] > 1.2 * chip.meanEnc || (chip.CHIP == 2 && encData[i] < CalibrationDataChip.LOWENCTHRESHOLD && i < 125) || ((i < 70 || chip.CHIP == 1) && encData[i] < 0.4 * chip.meanEnc)) 
            	{
            		System.out.println("R" + chip.region + "S" + chip.detectorDescriptor.getSector() + "L" + chip.layer + "U" + chip.CHIP + "N" + (i + 1) + " " + chip.CHAN + " " + Double.parseDouble(nf.format(encMap.get(i + 1))) + " " + Double.parseDouble(nf.format(chip.meanEnc)));
            	}
            }
        }
        System.out.println(chips.size() + " chips");
    }
}
