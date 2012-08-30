package com.eucalyptus.webui.client.chart;

import com.googlecode.gchart.client.GChart;

public class GChartExample00 extends GChart {
	   public GChartExample00() {
	     setChartTitle("<b>x<sup>2</sup> vs x</b>");
	     setChartSize(150, 150);
	     addCurve();
	     for (int i = 0; i < 10; i++) 
	       getCurve().addPoint(i,i*i);
	     getCurve().setLegendLabel("x<sup>2</sup>");
	     getXAxis().setAxisLabel("x");
	     getYAxis().setAxisLabel("x<sup>2</sup>");
	  }
	}