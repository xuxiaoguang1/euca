package com.eucalyptus.webui.client.chart;

import java.util.Arrays;

import com.googlecode.gchart.client.GChart;

public class PieStatChart extends GChart {

	static final int LABEL_COL = 0;
	static final int OBJECT_COL = 1;
	static final int N_COLS = 2;
	static final int SLICE_BORDER_WIDTH = 3;
	static final int SELECTED_BORDER_WIDTH = 5;
	static final String SLICE_BORDER_COLOR = "#FFF";
	
	public String[] color = {"#0F0", "#00F", "#F00", "#FF0", "#0FF", "#F0F"};

	public String[] label;
	public double[] percent;
	public int[] size;
	
	public String title;
	public int[] chartSize = {300, 240};
	
	private int len;

	public PieStatChart(String title, String[] label, int[] size, String[] color) {
		if(label.length != size.length){
			throw new RuntimeException("label.length != size.length");
		}
		this.title = title;
		this.label = label;
		this.size = size;
		this.color = color;
		
		len = label.length;
		this.percent = new double[len];
		int sum = 0;
		for(int i = 0; i < len; ++i){
			sum += size[i];
		}
		for(int i = 0; i < len; ++i){
			percent[i] = (double)size[i] / (double)sum;
		}
	}
	
	public PieStatChart(String title, String[] label, int[] size) {
		if(label.length != size.length){
			throw new RuntimeException("label.length != size.length");
		}
		this.title = title;
		this.label = label;
		this.size = size;
		
		len = label.length;
		this.percent = new double[len];
		int sum = 0;
		for(int i = 0; i < len; ++i){
			sum += size[i];
		}
		for(int i = 0; i < len; ++i){
			percent[i] = (double)size[i] / (double)sum;
		}
	}
	
	public void init(){
		setChartSize(chartSize[0], chartSize[1]);
		setBorderStyle("none");
		setChartTitle("<h3>" + title + "</h3>");
		
		// initial slice sizes
		for (int iCurve = 0; iCurve < len; iCurve++) {
			addCurve();
			getCurve().getSymbol().setBorderWidth(SLICE_BORDER_WIDTH);
			getCurve().getSymbol().setFillThickness(4);
			getCurve().getSymbol().setFillSpacing(0);
			getCurve().getSymbol().setBorderColor(SLICE_BORDER_COLOR);

			getCurve().getSymbol().setBrushSize(5, 5);
			getCurve().getSymbol().setSymbolType(SymbolType.PIE_SLICE_OPTIMAL_SHADING);
			getCurve().getSymbol().setModelHeight(1.0); // diameter = yMax-yMin
			getCurve().getSymbol().setModelWidth(0);
			
			getCurve().getSymbol().setBackgroundColor(color[iCurve]);
			getCurve().getSymbol().setPieSliceSize(percent[iCurve]);

			getCurve().addPoint(0.5, 0.5); // pie centered in world units
			getCurve().getPoint().setAnnotationLocation(AnnotationLocation.OUTSIDE_PIE_ARC);
			getCurve().getPoint().setAnnotationText(label[iCurve] + ": " + size[iCurve]);
			
		}
		getXAxis().setAxisMin(0); // so 0.5,0.5 (see above) centers pie
		getXAxis().setAxisMax(1);
		getYAxis().setAxisMin(0);
		getYAxis().setAxisMax(1);
		getXAxis().setHasGridlines(false); // hides axes, ticks, etc.
		getXAxis().setAxisVisible(false); // (not needed for the pie)
		getXAxis().setTickCount(0);
		getYAxis().setHasGridlines(false);
		getYAxis().setAxisVisible(false);
		getYAxis().setTickCount(0);
	}
}