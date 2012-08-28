package com.eucalyptus.webui.client.chart;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.*;

import com.googlecode.gchart.client.GChart;
import com.googlecode.gchart.client.HoverUpdateable;

/**
 * Defines a traditional "quarterly revenues" grouped bar-chart.
 * <p>
 * 
 * Illustrates how GWT-Widget-based tick labels and annotations (the
 * various clickable buttons on the chart) can be used to let users
 * interact with the chart.  <p>
 *
 * Also uses a Widget-based hover annotation (new w 2.4) to make a
 * detail pie chart fly across the chart along with your mouse (well, in
 * Chrome it flies. In IE it kind of meanders). 
 *
 * 
 */
public class GChartExample02 extends GChart {

   final Button[] groupLabels = {  // x-axis tick-labels
      new Button("2007"),
      new Button("2008"),
      new Button("2009")};
   final String[] barLabels = {    // legend labels
         "Q1", "Q2", "Q3", "Q4"};
  final Button[][] barButtons = {  // bar annotations 
     {new Button("1"), new Button("1"), new Button("1")},
     {new Button("2"), new Button("2"), new Button("2")},
     {new Button("3"), new Button("3"), new Button("3")},
     {new Button("4"), new Button("4"), new Button("4")}
  };
  final String[] barColors = {
     "#F55", "#55F", "#5F5", "silver"};
  final int MAX_REVENUE = 1000; 
  final int WIDTH = 400;
  final int HEIGHT = 280;  
  final String SOURCE_CODE_LINK =
"<a href='GChartExample02.txt' target='_blank'>Source code</a>";
  final Button updateButton = new Button("Update");
  final Label updateTimeMsg = new Label();

  // Returns Grid with code link, update button, & timing message
  // shown at the bottom of Client-side GChart live-demo charts.
  private Grid getDemoFootnotes(String sourceCodeLink,
                                Widget updateWidget,
                                Label updateTimeMsg) {
        HTML sourceCode = new HTML(sourceCodeLink);
        Widget[] w = {sourceCode, updateWidget, updateTimeMsg};
        String[] wWidth = {"40%", "20%", "40%"};
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

  // Implements the pop-up pie chart that shows percentage of the total
  // earnings in the hovered over quarter that occur in each year.
  class SingleQuarterVersusYearPie extends GChart implements HoverUpdateable {
     String[] sliceLabels = {"07", "08", "09"};
     String[] sliceColors = {"rgba(0,255,0,0.5)",
                             "rgba(255,0,0,0.5)",
                             "rgba(0,0,255,0.5)"};
     String[] sliceBorderColors = {"#7F7", "#F77", "#77F"};
     SingleQuarterVersusYearPie() { 
       setChartSize(100, 100);
       setLegendVisible(false);
       setBackgroundColor("#DDD");
       setBorderColor("black");
       setBorderStyle("solid");
       setBorderWidth("1px");
       
       getXAxis().setAxisMin(0);
       getXAxis().setAxisMax(10);
       getYAxis().setAxisMin(0);
       getYAxis().setAxisMax(10);
       getXAxis().setTickLabelThickness(0);
       getXAxis().setTickLength(0);
       getXAxis().setAxisVisible(false);
       getXAxis().setTickCount(0);
       getYAxis().setTickLabelThickness(0);
       getYAxis().setTickLength(0);
       getYAxis().setAxisVisible(false);
       getYAxis().setTickCount(0);
       getY2Axis().setTickLabelThickness(0);
       getY2Axis().setTickLength(0);
       getY2Axis().setAxisVisible(false);
       getY2Axis().setTickCount(0);

       getYAxis().setTickLabelFormat("###");
       
       for (int i=0; i < sliceColors.length; i++) {
         addCurve();
         getCurve().addPoint(5,5);
         getCurve().getSymbol().setSymbolType(
           SymbolType.PIE_SLICE_OPTIMAL_SHADING);
         getCurve().getSymbol().setBorderWidth(1);
         getCurve().getSymbol().setBackgroundColor(sliceColors[i]);
         getCurve().getSymbol().setHoverSelectionBackgroundColor(
            sliceColors[i]);
         getCurve().getSymbol().setBorderColor(sliceBorderColors[i]);
         // next two lines define pie diameter as 8 x-axis model units
         getCurve().getSymbol().setModelWidth(8);
         getCurve().getSymbol().setHeight(0);
         getCurve().getSymbol().setFillSpacing(0);
//         getCurve().getSymbol().setFillThickness(3);
         getCurve().getPoint().setAnnotationLocation(
            AnnotationLocation.INSIDE_PIE_ARC);
         getCurve().getSymbol().setHoverLocation(
            AnnotationLocation.INSIDE_PIE_ARC);
         getCurve().getSymbol().setBrushSize(100,0);
       }
       addCurve();
       getCurve().getSymbol().setSymbolType(SymbolType.ANCHOR_NORTHWEST);
       getCurve().addPoint(0,0);
       getCurve().getPoint(0).setAnnotationLocation(
          AnnotationLocation.SOUTHEAST);
     }
     public void hoverUpdate(Curve.Point p) {
        double totalRevenue = 0;
        Curve c = p.getParent(); 
        // total revenue across years for hovered over quarter (curve)
        for (int i=0; i < c.getNPoints(); i++) 
           totalRevenue += c.getPoint(i).getY();
        
        for (int i=0; i < sliceLabels.length; i++) {
           getCurve(i).getSymbol().setPieSliceSize(
              c.getPoint(i).getY()/totalRevenue);
           getCurve(i).getPoint(0).setAnnotationText(
                 GChart.formatAsHovertext("<small>" + sliceLabels[i] + "</small>"));
        }
        // upper left corner title (shows quarter pie is focused on)
        getCurve().getPoint(0).setAnnotationText(
           GChart.formatAsHovertext(c.getLegendLabel()));
           
        update();
     }
	public void hoverCleanup(Curve.Point hoveredOver) {}
  }

  // updates a single bar group (year) with new simulated data
  void updateGroupValues(int iGroup) {
     for (int iCurve=0; iCurve < getNCurves(); iCurve++) {
         getCurve(iCurve).getPoint(iGroup).setY(
             Math.random()*MAX_REVENUE);
     }
  }
  // updates 1 group's data, then the chart and elapsed time messages
  void updateGroup(int iGroup) {
     long t0 = System.currentTimeMillis();
     updateGroupValues(iGroup);
     update();
     long t1 = System.currentTimeMillis();
     updateTimeMsg.setText((t1-t0)+"ms");
  }
  // updates a single curve (quarter) with new simulated data
  void updateQuarterValues(int iQuarter) {
     for (int iGroup=0; iGroup < groupLabels.length; iGroup++) {
         getCurve(iQuarter).getPoint(iGroup).setY(
             Math.random()*MAX_REVENUE);
     }
  }

  // updates 1 quarters's data, chart, and elapsed time messages
  void updateQuarter(int iQuarter) {
     long t0 = System.currentTimeMillis();
     updateQuarterValues(iQuarter);
     update();
     long t1 = System.currentTimeMillis();
     updateTimeMsg.setText((t1-t0)+"ms");
  }

  
  public GChartExample02() {
    long t0 = System.currentTimeMillis();
    setChartSize(WIDTH, HEIGHT);
    setWidth("100%");
    setChartTitle("<b style='font-size: 16px'>" +
                  "Simulated Quarterly Revenues" + 
                  "<br>&nbsp;</b>");
    setChartTitleThickness(30);
    // the main update button updates all of the groups
    updateButton.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        long t0 = System.currentTimeMillis();
        for (int iGroup=0; iGroup < groupLabels.length; iGroup++) 
          updateGroupValues(iGroup);
        update();
        long t1 = System.currentTimeMillis();
        updateTimeMsg.setText((t1-t0)+"ms");
        // hold onto focus for spacebar clickers
        updateButton.setFocus(true);
      }
    });
    // these update just a single group's (year's) data
    for (int iG = 0; iG < groupLabels.length; iG++) {
       	
       groupLabels[iG].addClickHandler(new ClickHandler() {
         public void onClick(ClickEvent event) {
           Widget w = (Widget) event.getSource();	 
           if (w == groupLabels[0])
             updateGroup(0);
           else if (w == groupLabels[1])
             updateGroup(1);
           else if (w == groupLabels[2])
             updateGroup(2);
           // hold onto focus for spacebar clickers
           ((Button) w).setFocus(true);
         }
       });
    
    // when clicked, these update the ith quarter's data on the chart
       for (int iQ = 0; iQ < barButtons.length; iQ++) {
         barButtons[iQ][iG].addClickHandler(new ClickHandler() {
           public void onClick(ClickEvent event) {
        	 Widget w = (Widget) event.getSource();  
             String lbl = ((Button) w).getText();
             if (lbl.equals("1"))
               updateQuarter(0);
             else if (lbl.equals("2"))
               updateQuarter(1);
             else if (lbl.equals("3"))
               updateQuarter(2);
            else if (lbl.equals("4"))
               updateQuarter(3);
           // hold onto focus for spacebar clickers
            ((Button) w).setFocus(true);
           }
         });
       }
    }

    for (int iCurve=0; iCurve < barLabels.length; iCurve++) { 
      addCurve();     // one curve per quarter
      getCurve().getSymbol().setSymbolType(
         SymbolType.VBAR_SOUTHWEST);
      getCurve().getSymbol().setBackgroundColor(barColors[iCurve]);
      getCurve().setLegendLabel(barLabels[iCurve]);
      getCurve().getSymbol().setModelWidth(1.0);
      getCurve().getSymbol().setBorderColor("#AAA");
      getCurve().getSymbol().setBorderWidth(1);
      // pop-up a detail pie chart when the hover over:
      getCurve().getSymbol().setHoverWidget(
          new SingleQuarterVersusYearPie());
      // pie will be 20px above mouse, but centered on the bar
      getCurve().getSymbol().setHoverAnnotationSymbolType(
         SymbolType.ANCHOR_MOUSE_SNAP_TO_X);
      getCurve().getSymbol().setHoverLocation(AnnotationLocation.NORTH);
// because bars to left of x, we need this (half a bar) shift to center pie
      getCurve().getSymbol().setHoverXShift(-15);
      getCurve().getSymbol().setHoverYShift(20);
      // tall thin brush so we can sweep mouse across at any y
      getCurve().getSymbol().setBrushSize(10, getYChartSizeDecorated());
       getCurve().getSymbol().setBrushLocation(AnnotationLocation.SOUTH);
      // hit test "closeness" determination only "sees" x
      getCurve().getSymbol().setDistanceMetric(1, 0); 
      
      
      for (int jGroup=0; jGroup < groupLabels.length; jGroup++) { 
        // the '+1' creates a bar-sized gap between groups 
        getCurve().addPoint(1+iCurve+jGroup*(barLabels.length+1),
                            Math.random()*MAX_REVENUE);
        getCurve().getPoint().setAnnotationLocation(
            AnnotationLocation.NORTH);
        getCurve().getPoint().setAnnotationYShift(5); 
        getCurve().getPoint().setAnnotationWidget(
             barButtons[iCurve][jGroup]);
      }
    }

    for (int i = 0; i < groupLabels.length; i++) {
      // center the clickable tick-label horizontally on each group: 
      getXAxis().addTick(
         barLabels.length/2. + i*(barLabels.length+1), 
         groupLabels[i]); 
    }
    getXAxis().setTickLength(0);       // eliminates tick marks
    getXAxis().setTickLabelPadding(6); // 6px between label and axis
    getXAxis().setAxisMin(0);       // keeps first bar on chart
    getXAxis().setTickLabelThickness(20); 
    
    getYAxis().setAxisMin(0);           // Based on sim revenue range
    getYAxis().setAxisMax(MAX_REVENUE); // of 0 to MAX_REVENUE.
    getYAxis().setTickCount(11);
    getYAxis().setHasGridlines(true);
    getYAxis().setTickLabelFormat("$#,###");
    setChartFootnotes(getDemoFootnotes(
             SOURCE_CODE_LINK, updateButton, updateTimeMsg));
    setChartFootnotesThickness(60);
    update();
    long t1 = System.currentTimeMillis();
    updateTimeMsg.setText((t1-t0)+"ms");        
  }
}