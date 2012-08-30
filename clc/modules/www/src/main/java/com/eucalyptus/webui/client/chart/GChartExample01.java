package com.eucalyptus.webui.client.chart;

import com.googlecode.gchart.client.GChart;

/**
 * Defines a bar-chart of x*x vs. x, with gridlines.
 */
public class GChartExample01 extends GChart {
	public GChartExample01() {
		setChartTitle("<b>x<sup>2</sup> vs x</b>");
		setChartSize(480, 360);
		addCurve();
		for (int i = 0; i < 10; i++){
			getCurve().addPoint(i, i * i);
		}
		getCurve().setLegendLabel("x<sup>2</sup>");
		getCurve().getSymbol().setSymbolType(SymbolType.VBAR_SOUTH);
		getCurve().getSymbol().setBackgroundColor("red");
		getCurve().getSymbol().setBorderColor("black");
		getCurve().getSymbol().setModelWidth(1.0);
		getXAxis().setAxisLabel("<b>x</b>");
		getXAxis().setHasGridlines(true);
		getYAxis().setAxisLabel("<b>x<sup>2</sup></b>");
		getYAxis().setHasGridlines(true);
		setLegendVisible(false);
	}
}