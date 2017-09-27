package org.jlab.calib.services.svt.calib;

/**
 *
 * @author gotra
 */

public class SVTLayer extends SVTComponent {

	public int side;
	public int layerId;
	protected SVTSensor[] sensors;

	public SVTLayer(int sectors, int layerid, int side) {

		nSensors = sectors;
		nSectors = sectors; // layer has only single sided sensors
		this.side = side;
		layerId = layerid;
		// System.out.println("Layer: sensors " + nSensors + " layer " + layerId
		// + " side " + side);
		sensors = new SVTSensor[nSensors];

		for (int i = 0; i < nSensors; i++)
			sensors[i] = new SVTSensor(i + 1, layerId, side);
	}

	public int size() {
		return nSensors;
	}

	public SVTSensor[] getSensors() {
		return sensors;
	}

	public void setSensors(SVTSensor[] sensors) {
		this.sensors = sensors;
	}

	public SVTSensor getSensor(int sensorId) {
		return sensors[sensorId - 1]; // sensorId starts from 1
	}

}
