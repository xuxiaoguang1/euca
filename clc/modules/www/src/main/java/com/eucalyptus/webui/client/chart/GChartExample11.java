package com.eucalyptus.webui.client.chart;

import java.util.HashMap;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.*;
import com.google.gwt.user.client.ui.*;

import com.googlecode.gchart.client.GChart;
import com.googlecode.gchart.client.HoverUpdateable;

/** Combination chart containing pie, line, and bar (area)-based curves.
 *  <p>
 *
 *  Also illustrates use of single-sided point selection brushes,
 *  fixed-chart-location hover annotations, and the use of a simple list
 *  box hover widget (on the pie) for quickly changing a single chart property.
 *
 */
public class GChartExample11 extends GChart {
  final double INITIAL_PRICE = 100;
  final double MAX_MONTHLY_RELATIVE_CHANGE = 0.2;
  final int N_FORCASTED_MONTHS = 13;
  final int MIN = 0;
  final int MAX = 1;
  final int AVG = 2;
  final int STD = 3; // estimated standard deviation
  final int N_STATS = 4;
  final int PRICE = 2;  // curve id of simple price curve
  final int BDIF = 3;   // curve id of backward difference curve
  double[] prices = new double[N_FORCASTED_MONTHS];
  double[] stats = new double[N_STATS];
  String[] statLabels = {"min: ", "max: ", "avg: ", "std: ",}; 
  String[] shortStatLabels = {"min", "max", "avg", "std",};

  
  final String SOURCE_CODE_LINK =
"<a href='GChartExample11.txt' target='_blank'>Source code</a>";
  final Button updateButton = new Button("<small><b>Update</b></small>");
  final Label updateTimeMsg = new Label();
  ListBox tensionChooser = new ListBox();  // 'curvyness' selector

  HoverUpdateableListBox[] hoverList = {new HoverUpdateableListBox(MIN),
                                        new HoverUpdateableListBox(MAX)};
    
  int[] iStat = {MIN, MAX}; // ids of stats mapped to the two pie slices

  HashMap<String, Double> curveData = new HashMap<String, Double>();
  
  void updateStatSlices(int iStat0, int iStat1) {
    iStat[0] = iStat0;
    iStat[1] = iStat1;
    updateStatSlices();
  }

  void updateStatSlices() {   
     getCurve(0).getSymbol().setPieSliceSize(
       stats[iStat[0]]/(stats[iStat[0]]+stats[iStat[1]]));
     getCurve(1).getSymbol().setPieSliceSize(
       stats[iStat[1]]/(stats[iStat[0]]+stats[iStat[1]]));
     getCurve(0).getPoint().setAnnotationText(
        shortStatLabels[iStat[0]]);
     getCurve(1).getPoint().setAnnotationText(
        shortStatLabels[iStat[1]]);
  }

  // pop-up-on-hover-over list box that shows, lets user
  // switch, statistic mapped to each pie slice
  class HoverUpdateableListBox extends ListBox
        implements HoverUpdateable {

     HoverUpdateableListBox(int iStat) {
        for (int i = 0; i < N_STATS; i++)
           addItem(statLabels[i] + stats[i]);
        setItemSelected(iStat,true);
        setVisibleItemCount(stats.length);
        addChangeHandler(new ChangeHandler() {
           public void onChange(ChangeEvent event) {
              updateStatSlices(hoverList[0].getSelectedIndex(),
                               hoverList[1].getSelectedIndex());
              update(TouchedPointUpdateOption.TOUCHED_POINT_LOCKED);
           }
        });

     }

     // The two HoverUpdateable interface methods follow
     public void hoverCleanup(Curve.Point hoveredAwayFrom) {}
     public void hoverUpdate(Curve.Point hoveredOver) {
        for (int i = 0; i < N_STATS; i++) // update list text
           setItemText(i, statLabels[i] +
              getYAxis().formatAsTickLabel(Math.round(stats[i])));
        // highlight statistic pie slice is now displaying
        setSelectedIndex(iStat[getCurveIndex(getTouchedCurve())]);
     }

  }
  
  // Returns Grid with code link, update button, & timing message
  // shown at the bottom of Client-side GChart live-demo
  // charts (just demo infrastructure, not GChart-specific code)
  
  private Grid getDemoFootnotes(String sourceCodeLink,
                                Widget updateWidget,
                                Label updateTimeMsg,
                                ListBox tensionListBox) {
        HTML sourceCode = new HTML(sourceCodeLink);
        Grid subGrid = new Grid(1, 2);
        subGrid.setWidget(0, 0, updateWidget);
        subGrid.setWidget(0, 1, tensionListBox);
        subGrid.getCellFormatter().setHorizontalAlignment(0,0, 
                HasHorizontalAlignment.ALIGN_RIGHT);
        subGrid.getCellFormatter().setHorizontalAlignment(0,0, 
                HasHorizontalAlignment.ALIGN_LEFT);
        Widget[] w = {sourceCode, subGrid, updateTimeMsg};
        String[] wWidth = {"30%", "40%", "30%"};
        Grid result = new Grid(1, w.length);
        for (int i = 0; i < w.length; i++) {
          result.setWidget(0, i, w[i]);
          result.getCellFormatter().setWidth(0,i, wWidth[i]);
          result.getCellFormatter().setHorizontalAlignment(0,i, 
             HasHorizontalAlignment.ALIGN_CENTER);
        }
        result.setWidth(getXChartSizeDecorated()+"px");
        return result;
  }

  
// updates the chart with results of a new oil price simulation  
  private void updateChart() {
    double sum = INITIAL_PRICE;  // simple sum of all prices   
    double ssq = INITIAL_PRICE*INITIAL_PRICE;  // sum of squares of prices
    stats[MIN] = INITIAL_PRICE;
    stats[MAX] = INITIAL_PRICE;
    prices[0] = INITIAL_PRICE;
    for (int i=1; i < N_FORCASTED_MONTHS; i++) {
       prices[i] = prices[i-1] *
         (1 + MAX_MONTHLY_RELATIVE_CHANGE*(2*Math.random()-1));
       stats[MIN] = Math.min(stats[MIN], prices[i]);
       stats[MAX] = Math.max(stats[MAX], prices[i]);
       sum += prices[i];
       ssq +=  prices[i]*prices[i];
    }
    stats[AVG] = sum/N_FORCASTED_MONTHS;
    // use "average of squares minus square of average"
    // formula for variance to get standard deviation. 
    stats[STD] = Math.sqrt(ssq/N_FORCASTED_MONTHS -
                           stats[AVG]*stats[AVG]);

    updateStatSlices();  // update pie slices to reflect new stats

// update backward-price-difference and price curves    
    getCurve(BDIF).clearPoints();
    getCurve(PRICE).clearPoints();
    for (int i = 0; i < N_FORCASTED_MONTHS; i++) {
      getCurve(BDIF).addPoint(i, (i == 0)?0:(prices[i]-prices[i-1]));
      getCurve(PRICE).addPoint(i,prices[i]);
    
      if (prices[i]!=stats[MIN] && prices[i]!=stats[MAX]) {
        //no min/max;
        getCurve(PRICE).getPoint().setAnnotationText(null); //no label
      }
      else {
        // label point to indicate it's at a min or max price
        getCurve(PRICE).getPoint().setAnnotationFontSize(10);
        getCurve(PRICE).getPoint().setAnnotationFontWeight("bold");
        if (prices[i]==stats[MIN]) {
          getCurve(PRICE).getPoint().setAnnotationLocation(
            AnnotationLocation.SOUTH);
          getCurve(PRICE).getPoint().setAnnotationText(shortStatLabels[MIN]);
          getCurve(PRICE).getPoint().setAnnotationFontColor("blue");
        }
        else {
          getCurve(PRICE).getPoint().setAnnotationLocation(
            AnnotationLocation.NORTH);
           getCurve(PRICE).getPoint().setAnnotationText(shortStatLabels[MAX]);
           getCurve(PRICE).getPoint().setAnnotationFontColor("blue");
        }
      }
    }
    update();
  }

  void updateCurvyness() {   
    int iSelected = tensionChooser.getSelectedIndex();
    if (iSelected != -1) {
      double value = Double.parseDouble(tensionChooser.getValue(iSelected));
      curveData.put("catmull-rom-tension", value);
     // catmull-rom-tension is a developer defined parameter that GChart
     // doesn't know about: tell GChart a re-rendering is needed
      getCurve(PRICE).invalidateRendering();
      getCurve(BDIF).invalidateRendering();
      update();
    }
  }
  
  public GChartExample11() {
     long t0 = System.currentTimeMillis();
  // misc chart configuration
     setChartSize(400, 250);
     setCanvasExpansionFactors(0, 0.3);
     setClipToDecoratedChart(true);
     setWidth("100%");
     setPlotAreaBackgroundColor("#CCC");
     setLegendBackgroundColor(getPlotAreaBackgroundColor());
     setGridColor("#EEE");
// convenience methods; these properties could also have been defined
// via CSS. See the javadoc comment for GChart.USE_CSS for more info.
     setBackgroundColor("#DDF");
     setBorderColor("black");
     setBorderWidth("1px");
     setBorderStyle("outset");
// title and footnotes (w. update button)
     setChartTitle(
"<b style='font-size: 16px'>Estimated Future Oil Prices <br>" +
"<b><i style='font-size: 10px'>All results are pseudo-random. " + 
"Randomize fully before you invest.<br>&nbsp;" + 
"</i></b>");
     setChartTitleThickness(60);
     setLegendVisible(false);
              
     final Button updateButton = new Button("Update");
     updateButton.setTitle(
"Click for new totally unbiased, totally random, estimates.");
     updateButton.addClickHandler(new ClickHandler() {
        public void onClick(ClickEvent w) {
          long t0 = System.currentTimeMillis();
          updateChart();
          long t1 = System.currentTimeMillis();
          updateTimeMsg.setText((t1-t0) + "ms");
          updateButton.setFocus(true);
        }
     });

// x-axis config    
     getXAxis().setAxisLabel(
"<small>time (months from now)</small>");
     getXAxis().setAxisLabelThickness(20);
     getXAxis().setTickCount(13);
     getXAxis().setTicksPerLabel(2);
     getXAxis().setAxisMin(0);
     getXAxis().setAxisMax(N_FORCASTED_MONTHS-1);
     getXAxis().setHasGridlines(true);
// y-axis config
     getYAxis().setAxisLabel(
"<center>p<br>r<br>i<br>c<br>e</center>");
     getYAxis().setAxisLabelThickness(30);
     getYAxis().getAxisLabel().setTitle("price");
     getYAxis().setAxisMin(0);
     getYAxis().setAxisMax(200);
     getYAxis().setTickCount(5);
     getYAxis().setTickLabelFormat("$#.##");
     getYAxis().setHasGridlines(true);

     // y2-axis config
     getY2Axis().setAxisLabel(
"<center>&Delta;<br>p<br>r<br>i<br>c<br>e</center>");
     getY2Axis().setAxisLabelThickness(30);
     getY2Axis().setAxisMin(-20);
     getY2Axis().setAxisMax(20);
     getY2Axis().setTickCount(5);
     getY2Axis().setTickLabelFormat("$#.##;($#.##)");
     getY2Axis().setTickLabelThickness(30);

// show relative size of two user-selected stats in a pie

     final double X_AT_PIE_CENTER = 13.2; // in chart-model
     final double Y_AT_PIE_CENTER = 225;  // coordinates
     final int PIE_DIAMETER = 50;         // in pixels
     addCurve(); // slice representing first statistic
     getCurve().getSymbol().setSymbolType(
       SymbolType.PIE_SLICE_OPTIMAL_SHADING);
     getCurve().getSymbol().setFillSpacing(0);
     getCurve().getSymbol().setFillThickness(3);
     getCurve().getSymbol().setBorderWidth(1);
     getCurve().getSymbol().setBorderColor("white");
     getCurve().getSymbol().setBackgroundColor("rgba(255,0,0,0.3)");
     getCurve().getSymbol().setHeight(PIE_DIAMETER); 
     getCurve().getSymbol().setWidth(0);
     getCurve().addPoint(X_AT_PIE_CENTER,Y_AT_PIE_CENTER); 
     getCurve().getPoint().setAnnotationText(shortStatLabels[iStat[0]]);
     getCurve().getPoint().setAnnotationLocation(
       AnnotationLocation.INSIDE_PIE_ARC);
     getCurve().getPoint().setAnnotationFontSize(10);
     getCurve().getPoint().setAnnotationFontWeight("bold");
     getCurve().getPoint().setAnnotationFontColor("blue");
     getCurve().getSymbol().setHoverLocation(AnnotationLocation.SOUTH);
     getCurve().getSymbol().setHoverAnnotationSymbolType(
        SymbolType.BOX_CENTER);
     getCurve().getSymbol().setHoverWidget(hoverList[0]);
     getCurve().getSymbol().setBrushSize(40,0);
                                         
     addCurve(); // slice representing second statistic
     getCurve().getSymbol().setSymbolType(
       SymbolType.PIE_SLICE_OPTIMAL_SHADING);
     getCurve().getSymbol().setFillSpacing(0);
     getCurve().getSymbol().setFillThickness(3);
     getCurve().getSymbol().setBorderWidth(1);
     getCurve().getSymbol().setBorderColor("white");
     getCurve().getSymbol().setBackgroundColor("rgba(0,255,0,0.3)");
     getCurve().getSymbol().setHeight(PIE_DIAMETER); 
     getCurve().getSymbol().setWidth(0);
     getCurve().addPoint(X_AT_PIE_CENTER,Y_AT_PIE_CENTER); 
     getCurve().getPoint().setAnnotationText(shortStatLabels[iStat[1]]);
     getCurve().getPoint().setAnnotationLocation(
       AnnotationLocation.INSIDE_PIE_ARC);
     getCurve().getPoint().setAnnotationFontSize(10);
     getCurve().getPoint().setAnnotationFontWeight("bold");
     getCurve().getPoint().setAnnotationFontColor("blue");
     getCurve().getSymbol().setHoverLocation(AnnotationLocation.SOUTH);
     getCurve().getSymbol().setHoverAnnotationSymbolType(
        SymbolType.BOX_CENTER);
     getCurve().getSymbol().setHoverWidget(hoverList[1]);
     getCurve().getSymbol().setBrushSize(40,0);
      
     // line curve (on y) represents actual price
     addCurve();
     // defines amount of "curvyness"; c.f. class CurvyLineCanvasLite
     getCurve().setCurveData(curveData);
     getCurve().setLegendLabel("price");
     getCurve().getSymbol().setFillSpacing(0);
     getCurve().getSymbol().setFillThickness(6);
     getCurve().getSymbol().setBackgroundColor("rgba(0,0,255,1)");
     getCurve().getSymbol().setBorderWidth(2);
     getCurve().getSymbol().setBorderColor("rgba(0,0,255,0.5)");
     getCurve().getSymbol().setWidth(0);
     getCurve().getSymbol().setHeight(0);
     getCurve().getSymbol().setBrushSize(40, getYChartSizeDecorated());
     getCurve().getSymbol().setBrushLocation(AnnotationLocation.NORTH);
     getCurve().getSymbol().setDistanceMetric(10, 1);
     // position hover popup in plot area's top left corner
     getCurve().getSymbol().setHoverAnnotationSymbolType(
        SymbolType.ANCHOR_NORTHWEST);
     getCurve().getSymbol().setHoverLocation(
        AnnotationLocation.SOUTHEAST);
     getCurve().getSymbol().setHoverXShift(2);
     getCurve().getSymbol().setHoverYShift(-2);
// 2px external selection border
     getCurve().getSymbol().setHoverSelectionHeight(8);
     getCurve().getSymbol().setHoverSelectionWidth(8);
     getCurve().getSymbol().setHoverSelectionBorderWidth(-2);
// same color to create illusion that selected point increases in size
     getCurve().getSymbol().setHoverSelectionBorderColor("#00F");
// brighten center to make selected point easier to see
     getCurve().getSymbol().setHoverSelectionBackgroundColor("aqua");
     getCurve().getSymbol().setHovertextTemplate(
       GChart.formatAsHovertext("month=${x}<br>price=${y}"));

     // continuously filled baseline bars ==> area chart (on y2)
     // represent monthly change in prices
     addCurve();
     getCurve().setCurveData(curveData);
     getCurve().getSymbol().setSymbolType(
       SymbolType.VBAR_BASELINE_CENTER);
     // this line makes it continuously filled areas (not just bars):
     getCurve().getSymbol().setFillSpacing(0);
     getCurve().getSymbol().setBaseline(0);
     getCurve().getSymbol().setBorderWidth(1);
     getCurve().getSymbol().setBorderColor("black");
     getCurve().getSymbol().setFillThickness(2);
     getCurve().getSymbol().setBackgroundColor("rgba(255,0,0,0.5)");
     getCurve().setLegendLabel("&Delta;price");
     getCurve().getSymbol().setWidth(0);
     getCurve().getSymbol().setHovertextTemplate(
       GChart.formatAsHovertext("month=${x}<br>&Delta;price=${y}"));
     getCurve().getSymbol().setHoverYShift(-5);
     getCurve().setYAxis(Y2_AXIS);
     // position hover popup in plot area's bottom left corner
     getCurve().getSymbol().setHoverAnnotationSymbolType(
        SymbolType.ANCHOR_SOUTHWEST);
     getCurve().getSymbol().setHoverLocation(
        AnnotationLocation.NORTHEAST);
     getCurve().getSymbol().setHoverXShift(2);
     getCurve().getSymbol().setHoverYShift(2);
     // 3px external selection border around bars
     getCurve().getSymbol().setHoverSelectionBorderWidth(1);
     getCurve().getSymbol().setHoverSelectionHeight(10);
     getCurve().getSymbol().setHoverSelectionWidth(10);
     getCurve().getSymbol().setHoverSelectionSymbolType(
        SymbolType.PIE_SLICE_OPTIMAL_SHADING);
     getCurve().getSymbol().setHoverSelectionBackgroundColor("rgba(255,128,128,0.5)");
     getCurve().getSymbol().setHoverSelectionBorderColor("rgba(0,0,0,1)");
     // tall brush thick enough to at least touch 1 point.
     getCurve().getSymbol().setBrushSize(40, getYChartSizeDecorated());
     // brush south of mouse ==> selects points below it
     getCurve().getSymbol().setBrushLocation(AnnotationLocation.SOUTH);
     // x-closeness main criterion, but y can still break ties
     getCurve().getSymbol().setDistanceMetric(10, 1);



     tensionChooser.setVisibleItemCount(1); // makes it a drop-down list
     tensionChooser.addItem("Straight",  "0.0");
     tensionChooser.addItem("10% Curvy", "0.1");     
     tensionChooser.addItem("20% Curvy", "0.2");
     tensionChooser.addItem("30% Curvy", "0.3");
     tensionChooser.addItem("40% Curvy", "0.4");
     tensionChooser.addItem("50% Curvy", "0.5");
     tensionChooser.addItem("60% Curvy", "0.6");
     tensionChooser.addItem("70% Curvy", "0.7");
     tensionChooser.addItem("80% Curvy", "0.8");
     tensionChooser.addItem("90% Curvy", "0.9");
     tensionChooser.addItem("100% Curvy","1.0");
     tensionChooser.setItemSelected(3, true); // start with "30% Curvy"
     curveData.put("catmull-rom-tension", 0.30);
     tensionChooser.addChangeHandler(new ChangeHandler() {
        public void onChange(ChangeEvent event) {
           updateCurvyness();
        }
     });
     // forces an auto-update with arrow-based curvyness changes
     tensionChooser.addKeyUpHandler(new KeyUpHandler() {
        public void onKeyUp(KeyUpEvent event) {
           if (event.isUpArrow() || event.isLeftArrow() ||
               event.isDownArrow() || event.isRightArrow()) {
              updateCurvyness();
           }
        }
     });
     
     
     setChartFootnotes(getDemoFootnotes(
             SOURCE_CODE_LINK, updateButton, updateTimeMsg, tensionChooser));
     setChartFootnotesThickness(40);
     
     updateChart();
     long t1 = System.currentTimeMillis();
     updateTimeMsg.setText((t1-t0) + "ms");
     
// button must be rendered in browser before it can accept focus
     DeferredCommand.addCommand(new Command() {
       public void execute() {
          updateButton.setFocus(true);
       }
     });

   }

}