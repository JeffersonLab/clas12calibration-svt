/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.calib.services.svt.utils;

import java.util.ArrayList;
import java.util.List;
import org.jlab.detector.base.DetectorDescriptor;
import org.jlab.groot.math.F1D;
import org.jlab.groot.base.GStyle;
import org.jlab.groot.data.GraphErrors;
import org.jlab.groot.fitter.*;

/**
 *
 * @author gavalian, gotra
 */
public class CalibrationData {

	DetectorDescriptor desc = new DetectorDescriptor();

	private List<GraphErrors> graphs = new ArrayList<GraphErrors>();
	private List<Integer> amplitudes = new ArrayList<Integer>();
	private List<F1D> functions = new ArrayList<F1D>();
	private GraphErrors resGraph = null;
	private F1D resFunc = new F1D("p1", "[a]+[b]*x", 2.0, 6.0);
	public static final double MVDAC = 3.5;
	public static final double MVFC = 25.0;
	public static final double EFC = 6250.0;
	public static final double MAX_COUNTS = 3058.0;
	public static final String histoTitle = "BCO 128 ns, BLR on, low gain, 125 ns";

	public CalibrationData(int sector, int layer, int component) {
		GStyle.getH1FAttributes().setOptStat("111110");
//		GStyle.getFunctionAttributes().setOptStat("1100");
		GStyle.getGraphErrorsAttributes().setOptStat("011000");
		this.desc.setSectorLayerComponent(sector, layer, component);
	}

	public DetectorDescriptor getDescriptor() {
		return this.desc;
	}

	public void addGraph(int amplitude, int offset, double[] points) {
		double[] xp = new double[points.length];
		double[] yp = new double[points.length]; // normalized occupancy
		double[] ex = new double[points.length];
		double[] ey = new double[points.length];
		for (int loop = 0; loop < points.length; loop++) {
			xp[loop] = offset + loop;
			yp[loop] = points[loop] / MAX_COUNTS;
			ex[loop] = 0.5;
			ey[loop] = 0.03 * yp[loop];
		}

		GraphErrors graph = new GraphErrors("g", xp, yp, ex, ey);
		graph.setTitle(histoTitle);
		graph.setTitleX("DAC threshold");
		graph.setTitleY("Occupancy");
		this.graphs.add(graph);
		final int shift = 7;
		F1D f1 = new F1D("erf", "[a]+[b]*erf(x,[c],[d])", offset + shift, points.length + offset);
		this.functions.add(f1);
		this.amplitudes.add(amplitude);
	}

	public void analyze() {
//		System.out.println("data analyze " + this.graphs.size());
		for (int loop = 0; loop < this.graphs.size(); loop++) {
			F1D func = this.functions.get(loop);
			double[] dataY = this.graphs.get(loop).getVectorY().getArray();
			double[] dataX = this.graphs.get(loop).getVectorX().getArray();
			double delta = 2;
			int index = 0;
			for (int i = 0; i < dataX.length; i++) {
//				System.out.println(loop + " " + i + " " + dataX[i] + " " + dataY[i]);
				double currentDelta = Math.abs(dataY[i] - 0.5);
				if (currentDelta < delta) {
					delta = currentDelta;
					index = i;
				}
			}
			func.setParameter(2, dataX[index]);
			double spread = 5;
			func.setParLimits(2, dataX[index] - spread, dataX[index] + spread);
			func.setParameter(0, 0.0);
			func.setParameter(1, 0.5);
			func.setParameter(3, 7.0);
			func.setParLimits(0, -0.2, 0.2);
			func.setParLimits(1, 0.4, 0.6);
			func.setParLimits(3, 1.0, 10.0);
			DataFitter.fit(this.functions.get(loop), this.graphs.get(loop), "NQ");
			if(dataY.length < 5) {// treat dead channels
				func.setParameter(2, 0.0);
				func.setParameter(3, 0.0);
			}
		}

		this.resFunc.setRange(2.0, 6.0);

		this.resGraph = new GraphErrors();
		this.resGraph.setTitle(histoTitle);
		this.resGraph.setTitleX("Input charge, fC");
		this.resGraph.setTitleY("Output mean, mV");

		for (int loop = 0; loop < 3; loop++) {
			double response = this.functions.get(loop).getParameter(2) * MVDAC;
			resGraph.addPoint(this.amplitudes.get(loop) / MVFC, response, 0, 0);
		}
		DataFitter.fit(resFunc, resGraph, "WQ");
	}

	public GraphErrors getGraph(int index) {
		return this.graphs.get(index);
	}

	public Integer getAmplitude(int index) {
		return this.amplitudes.get(index);
	}

	public F1D getFunc(int index) {
		return this.functions.get(index);
	}

	public GraphErrors getResGraph() {
		return this.resGraph;
	}

	public F1D getResFunc() {
		return this.resFunc;
	}
}
