package org.jlab.calib.services.svt.calib;

import org.jlab.detector.base.DetectorDescriptor;

public class SVTSensor extends SVTComponent {

	// private final int TOP = 1;
	// private final int BOTTOM = 0;
	public static final int NSTRIPS = 256;
	// private int[] status;
	public int[] badChans = {0, 0}; // per chip
	private int side = 1;
	private int layer = 0;
	private int region = 0;
	private int sector = 0;
	private DetectorDescriptor desc = new DetectorDescriptor();

	SVTSensor(int sector, int layer, int side) {
		nSensors = 1;
		nSectors = 1;
		// System.out.println("Sensor: sector " + sector + " layer " + layer + "
		// side " + side);
		this.desc.setSectorLayerComponent(sector, layer, 0);
		this.side = side;
		this.layer = layer;
		this.region = region(layer);
		this.sector = sector;
		// status = new int[NSTRIPS];
		badChans[0] = 0;
		badChans[1] = 0;
	}

	static int region(int layer) {
		if (layer < 1 || layer > 8)
			return -1;
		else
			return (layer % 2 == 0 ? layer / 2 : (layer + 1) / 2);
	}

	public int getSide() {
		return side;
	}

	public int getLayer() {
		return layer;
	}

	public int getRegion() {
		return region;
	}

	public int getSector() {
		return sector;
	}

}
