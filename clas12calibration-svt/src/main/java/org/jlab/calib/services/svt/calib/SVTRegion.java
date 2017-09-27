package org.jlab.calib.services.svt.calib;

/**
 *
 * @author gotra
 */

public class SVTRegion extends SVTComponent {

	private SVTLayer[] layers;
	public int regionId;
	private final int NLAYERSPERREGION = 2;

	public SVTRegion(int sectors, int region) {

		regionId = region;
		// System.out.println("Region " + regionId + " : sensors " + nSensors);
		nSectors = sectors;
		nSensors = sectors * 2;
		layers = new SVTLayer[NLAYERSPERREGION];
		layers[0] = new SVTLayer(nSectors, (regionId * 2) - 1, 0); // bottom
		layers[1] = new SVTLayer(nSectors, regionId * 2, 1); // top
	}

	public SVTLayer[] getLayers() {
		return layers;
	}

	public void setLayers(SVTLayer[] layers) {
		this.layers = layers;
	}

	public SVTLayer getLayer(int layerId) {
		if (layerId % 2 == 0)
			return layers[1]; // layerId starts from 1
		else
			return layers[0];
	}
}
