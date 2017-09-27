package org.jlab.calib.services.svt.utils;

import java.util.List;

import org.jlab.calib.services.svt.calib.SVTCalibrationTable;
import org.jlab.calib.services.svt.decode.AbsDetectorTranslationTable;
import org.jlab.detector.base.DetectorCollection;
import org.jlab.groot.fitter.DataFitter;
import org.jlab.hipo.utils.FileUtils;

/**
 *
 * @author gavalian, gotra
 */
public class CalibrationStore {
	DetectorCollection<CalibrationDataChip> calibrationStore = new DetectorCollection<CalibrationDataChip>();
	AbsDetectorTranslationTable translationTable = new AbsDetectorTranslationTable();
	public static int nChips;
	public static int currentChip;

	public CalibrationStore() {
		translationTable.readFile("SVT123.table");
	}

	public DetectorCollection<CalibrationDataChip> getChips() {
		return this.calibrationStore;
	}

	public void readData(String directory) {
		calibrationStore = new DetectorCollection<CalibrationDataChip>();
		List<String> dirFiles = FileUtils.getFileListInDir(directory);
		// dirFiles.add("/Volumes/data/work/pscan/test2/svt1_s09_c1_u1");
		// dirFiles.add("/Volumes/data/work/pscan/test2/svt1_s09_c1_u2");
		// dirFiles.add("/Volumes/data/work/pscan/test2/svt1_s09_c1_u3");
		// dirFiles.add("/Volumes/data/work/pscan/test2/svt1_s09_c1_u4");

		nChips = dirFiles.size();
		currentChip = 0;
		System.out.println("-----> found " + nChips + " files");
		SVTCalibrationTable.println("-----> found " + nChips + " files");
		for (String f : dirFiles) {
//			System.out.println(f);
			if (f.contains("svt")) {
				CalibrationDataChip store = new CalibrationDataChip(this.translationTable);
				store.init();
				store.readData(f);
				this.calibrationStore.add(store.detectorDescriptor, store);
			}
		}
	}

	public void analyze() {
//		System.out.println("store analyze");
		List<CalibrationDataChip> Stores = this.calibrationStore.getList();
		int icounter = 1;
		for (CalibrationDataChip store : Stores) {
			currentChip = icounter;
			store.analyze();
			icounter++;
		}
	}

	public static void main(String[] args) {
		DataFitter.FITPRINTOUT = false;
		CalibrationStore sectors = new CalibrationStore();
		CalibrationDataChip chip = new CalibrationDataChip(sectors.translationTable);
		chip.readData("/Volumes/data/work/pscan/test2/svt1_s09_c1_u1");
		sectors.readData("/Volumes/data/work/pscan/test2");
		sectors.analyze();
		List<CalibrationDataChip> chipList = sectors.getChips().getList();
		System.out.println("Chip list size " + chipList.size());
	}
}
