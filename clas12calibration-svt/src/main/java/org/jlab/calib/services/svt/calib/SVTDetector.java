package org.jlab.calib.services.svt.calib;

import org.jlab.groot.base.GStyle;

/**
 *
 * @author gotra
 */

public class SVTDetector extends SVTComponent {

	public int nRegions;
	public int nLayers;
	private SVTRegion[] regions;
//	private int[] sectors = { 10, 14, 18, 24 };
	private int[] sectors = { 10, 14, 18};

	public SVTDetector(int nregions) {

		nRegions = nregions;
		nLayers = 2 * nRegions;
		regions = new SVTRegion[nregions];
		nSensors = 0;
		for (int i = 0; i < nregions; ++i) {
			regions[i] = new SVTRegion(sectors[i], i + 1);
			nSectors += sectors[i];
		}
		nSensors = 2 * nSectors;
		GStyle.getH1FAttributes().setOptStat("111110");
	}

	public SVTRegion[] getRegions() {
		return regions;
	}

	public void setRegions(SVTRegion[] regions) {
		this.regions = regions;
	}

	public SVTRegion getRegion(int regionId) {
		return regions[regionId - 1]; // regionId starts from 1
	}

	public SVTSensor getSensor(int layer, int sensor) {
		int region = (layer % 2 == 0 ? layer / 2 : (layer + 1) / 2);
		return getRegion(region).getLayer(layer).getSensor(sensor);
	}

	public SVTLayer getLayer(int layer) {
		int region = (layer % 2 == 0 ? layer / 2 : (layer + 1) / 2);
		return getRegion(region).getLayer(layer);
	}
}

