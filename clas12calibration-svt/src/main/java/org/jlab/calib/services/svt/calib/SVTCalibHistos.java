package org.jlab.calib.services.svt.calib;

import org.jlab.groot.base.GStyle;
import org.jlab.groot.data.H1F;

public class SVTCalibHistos {

	public String histoName;
	public String xTitle;
	public String yTitle;
	public int nXbins;
	public float xMin;
	public float xMax;
	public int fillColor;
	public H1F h; 

	SVTCalibHistos(String histoname, String xtitle, String ytitle, int nxbins, double xmin, double xmax,
			int fillcolor) {
		histoName = histoname;
		xTitle = xtitle;
		yTitle = ytitle;
		nXbins = nxbins;
		xMin = (float) xmin;
		xMax = (float) xmax;
		fillColor = fillcolor;

		GStyle.getH1FAttributes().setOptStat("111110");
		h = new H1F(histoName, xTitle, yTitle, nXbins, xMin, xMax);
		h.setFillColor(fillcolor);
	}
}
