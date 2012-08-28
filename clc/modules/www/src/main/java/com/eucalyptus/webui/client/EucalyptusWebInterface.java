package com.eucalyptus.webui.client;

import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.eucalyptus.webui.client.view.GlobalResources;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.widgetideas.graphics.client.Color;
import com.google.gwt.widgetideas.graphics.client.GWTCanvas;
import com.googlecode.gchart.client.GChart;
import com.googlecode.gchart.client.GChartCanvasFactory;
import com.googlecode.gchart.client.GChartCanvasLite;

public class EucalyptusWebInterface implements EntryPoint {

  private static final Logger LOG = Logger.getLogger( EucalyptusWebInterface.class.getName( ) );
  
  private EucalyptusApp app;
  
  @Override
  public void onModuleLoad( ) {
    // Make sure we catch any uncaught exceptions, for debugging purpose.
    GWT.setUncaughtExceptionHandler( new GWT.UncaughtExceptionHandler( ) {
      public void onUncaughtException( Throwable e ) {
        if ( e != null ) {
          LOG.log( Level.SEVERE, e.getMessage( ), e );
        }
      }
    } );
    GWT.<GlobalResources>create( GlobalResources.class ).buttonCss( ).ensureInjected( );
    // Create ClientFactory using deferred binding so we can
    // replace with different implementations in gwt.xml
    ClientFactory clientFactory = GWT.create( ClientFactory.class );
    // Start
    app = new EucalyptusApp( clientFactory );
    app.start( new AppWidget( RootLayoutPanel.get( ) ) );
  }
  
  /**
	 * Alternative lines to paste in if you want curvy connecting lines (the
	 * live demo uses this implementation exclusively; the simpler
	 * implementation is provided for those who don't need curvy lines or want
	 * to avoid the overhead cost/complexity of this class)
	 * <p>
	 * 
	 * GChart does not directly support curvy connecting lines (Bezier curves)
	 * even though the GWTCanvas vector graphics library does. So, to get them,
	 * we had to override the GChartCanvasLite <tt>lineTo</tt> method so that it
	 * instead invokes <tt>cubicCurveTo<tt> appropriately.
	 * Essentially, this override treats the lineTo method as if it
	 * were a more generic 'curveTo': a request to connect up successive
	 * points with a generic continuous connecting line. 
	 * <p>
	 * 
	 * Bezier curve control points are computed using a very popular and
	 * very simple (that's why I chose it) algorithm called Catmull-Rom.
	 * Cristopher Twigg wrote this <a
	 * href="http://www.cs.cmu.edu/afs/cs/academic/class/15462-f08/www/projects/assn2/assn2/catmullRom.pdf">
	 * very clear Catmull-Rom description</tt> that has most of what you need to
	 * know about Catmull-Rom to understand this code.
	 * 
	 * <p>
	 * 
	 * Here's the rest of the story: Twigg's cubic interpolation equation for
	 * Catmull-Rom is expressed in terms tension (Tau), plus the four points on
	 * the curve surrounding (two before, two after) each interpolation
	 * interval. Cubic Bezier curves are expressed in terms of the two points
	 * that immediately bracket the interpolation interval and two non-data
	 * 'control points' that generally are placed roughly between these point
	 * (and usually <i>do not</i> fall on the interpolated curve) using the
	 * following interpolation formula:
	 * 
	 * P(u) = P0*(1-u)^3 + 3*P1*(1-u)^2*u + 3*P2*(1-u)*u^2 + P3*u^3
	 * 
	 * Here u varies from 0 to 1 to produce the interpolated points, and the P0,
	 * and P3 are real data points at the end-points of the interpolated curve,
	 * and P1, P2 are the control points, (all of these points are 2-D, (x,y)
	 * vectors).
	 * <p>
	 * 
	 * Twigg's equations are also cubic in u, but are expressed in terms of the
	 * four real data points surrounding the interpolated curve segments:
	 * p(i-2), p(i-1), p(i), p(i+1) plus an additional parameter Tau (tension)
	 * that, roughly, defines how curvy the lines are. The Bezier endpoints, P0
	 * and P3 are equal to Twigg's interpolated curve segment start/end points:
	 * p(i-1) and p(i). So we just need a formula for P1 and P2 in terms of
	 * Twigg's bracketing data points and Tau. If you expand the equation above
	 * and collect all terms linear in u and then equate the coeeficient on this
	 * linear term (3*(-P0+P1)) with the coefficient on Twigg's linear-in-u term
	 * (Tau*(p(i)-p(i-2)) you can solve for P1 in terms of just known points in
	 * the data sequence and Tau. Equating the quadratic-in-u terms leads to a
	 * similar equation for P2 (the P2 equation more simply follows from
	 * symmetry with the P1 equation). The resulting equations for all the
	 * Bezier Ps are:
	 * <p>
	 * 
	 * <pre>
	 *      P0 = p(i-1)    
	 *      P1 = p(i-1) + Tau/3 * (p(i) - p(i-2))      
	 *      P2 = p(i)   - Tau/3 * (p(i+1) - p(i-1))
	 *      P3 = p(i)
	 * </pre>
	 * 
	 * <p>
	 * 
	 * Since Tau is often chosen as 0.5, this results in the (reasonably well
	 * known) "1/6th rule" for choosing Catmull-Rom cubic Bezier control points.
	 * Tau of 0 gives you linear connecting lines (the code below treats this as
	 * a special case calling <tt>lineTo</tt> directly for speed). And, as Tau
	 * increases, the connecting lines become "more curvy" (no, I don't know how
	 * to define that). Taus greater than 1.0 or less than 0 produce
	 * strange-looking curves usually best avoided (they are fun to look at,
	 * though).
	 * <p>
	 * 
	 * For the first interpolation interval on a curve there is no p(i-2), and
	 * for the last interval no p(i+1), so we execute <tt>quadraticCurveTo</tt>
	 * (which only requires a single control point, not two) in these cases.
	 * Exception: in the case of a simple two-point curve neither p(i-2) nor
	 * p(i+1) exist and thus no control points can be calculated via the above
	 * formulas...so we just use to the ordinary <tt>lineTo</tt> method.
	 * <p>
	 * 
	 * Because the above algorithm requires a single segment lookahead, actual
	 * drawing won't begin until the first three points have been seen via
	 * moveTo or lineTo invocations. Furthermore, before the curve is closed,
	 * stroked, or filled any of these "look-ahead" segments needed to be
	 * "flushed out" so they are included in the stroked or filled path. The
	 * code below overides stroke, fill, and closePath to assure that this
	 * happens.
	 * 
	 * <p>
	 * 
	 * I considered migrating this interpolation code into GChart proper (adding
	 * a 'tension' property to the curve, expanding GChartCanvasLite to include
	 * cubicCurveTo, etc.) but Googling revealed a mind-boggling number of
	 * different ways to choose the control points that govern the interpolation
	 * (there are several ways of dealing with the endpoints, plus C2 cubic
	 * splines, least squares fitting, etc.). Not one size fits all. So,
	 * especially since this is my first ever use of Bezier curves, and my main
	 * reason for selecting Catmull-Rom (not a bad reason, BTW) was it was the
	 * easiest to understand and code, I thought exposing this code so you can
	 * tweak as needed was best. If you want to refine the algorithm (it doesn't
	 * handle irregularly spaced points that well) that's great...please post
	 * your improvements/alternatives on the GChart issue tracker.
	 * 
	 */
	static final class CurvyLineCanvasLite extends GWTCanvas implements
			GChartCanvasLite {
		// Retains the latest 4 data points Catmull-Rom algorithm needs
		private final int MAX_POINTS = 4;
		private double[] x = new double[MAX_POINTS];
		private double[] y = new double[MAX_POINTS];
		private int nPts = 0; // number of points actually in buffer now

		// The tension (tau) defines the "curvyness" of interpolated lines
		public static final double STRAIGHT_LINE_TENSION = 0;
		// Classic Catmull-Rom tension: a reasonable curvy-line default
		public static final double REASONABLY_CURVY_TENSION = 0.5;
		// more than this much tension doesn't look right (too curvy)
		public static final double MAX_TENSION = 1.0;
		private final double DEFAULT_TENSION = STRAIGHT_LINE_TENSION;
		private double tau = DEFAULT_TENSION;

		// remembers a rolling window of the most recent 'lineTo' points
		private void logPoint(double xIn, double yIn) {
			if (nPts < MAX_POINTS) { // not yet full buffer
				nPts++;
			} else { // buffer is full, make room for the new point
				for (int i = 1; i < MAX_POINTS; i++) {
					x[i - 1] = x[i];
					y[i - 1] = y[i];
				}
			}
			x[nPts - 1] = xIn;
			y[nPts - 1] = yIn;
		}

		// Note: algorithm never has more than 1 final, undrawn, segment
		private void flushCurve() {
			if (nPts == 2) { // two points only determine a line
				super.lineTo(x[nPts - 1], y[nPts - 1]);
			} else if (nPts >= 3) {
				// If the very last line is vertical or horizontal,
				// it's likely a "cap-off" edge of an area chart, and
				// such non-data lines should never be curvy. Very
				// unlikely to cause problems with non-area curves.
				if ((x[nPts - 1] == x[nPts - 2] || y[nPts - 1] == y[nPts - 2])) {
					super.lineTo(x[nPts - 1], y[nPts - 1]);
				} else {
					double P1x = x[nPts - 2] + (tau / 3)
							* (x[nPts - 1] - x[nPts - 3]);
					double P1y = y[nPts - 2] + (tau / 3)
							* (y[nPts - 1] - y[nPts - 3]);
					quadraticCurveTo(P1x, P1y, x[nPts - 1], y[nPts - 1]);
				}
			}
			nPts = 0; // clears out rolling point buffer
		}

		public void beginPath() {
			super.beginPath();
			// grab the curve's setCurveData-defined custom data object
			HashMap<String, Double> curveData = (HashMap<String, Double>) GChart
					.getCurrentCurveData();
			tau = (null == curveData) ? DEFAULT_TENSION : ((Double) curveData
					.get("catmull-rom-tension")).doubleValue();
			nPts = 0; // clears out rolling point buffer
		}

		// make sure that any "look-ahead" segments get included
		public void closePath() {
			if (nPts > 0)
				flushCurve();
			super.closePath();
		}

		// make sure that any "look-ahead" segments get included
		public void stroke() {
			if (nPts > 0)
				flushCurve();
			super.stroke();
		}

		// make sure that any "look-ahead" segments get included
		public void fill() {
			if (nPts > 0)
				flushCurve();
			super.fill();
		}

		public void moveTo(double xIn, double yIn) {
			if (STRAIGHT_LINE_TENSION != tau) {
				flushCurve(); // break in continuity
				logPoint(xIn, yIn);
			}
			super.moveTo(xIn, yIn);
		}

		public void lineTo(double xIn, double yIn) {
			if (STRAIGHT_LINE_TENSION == tau) {
				super.lineTo(xIn, yIn);
			} else { // curvy connecting lines
				if (1 == nPts && (x[nPts - 1] == xIn || y[nPts - 1] == yIn)) {
					// If the very first line is vertical or horizontal,
					// it's likely a "cap-off" edge of an area chart, and
					// such non-data lines should never be curvy. Very
					// unlikely to cause problems with non-area curves.
					flushCurve();
					super.lineTo(xIn, yIn);
					logPoint(xIn, yIn);
				} else if (0 == nPts || 1 == nPts) {
					// not enough points for curvy interplotion yet
					logPoint(xIn, yIn);
				} else if (2 == nPts) { // first line + next point available
					logPoint(xIn, yIn);
					double P2x = x[1] - (tau / 3) * (x[2] - x[0]);
					double P2y = y[1] - (tau / 3) * (y[2] - y[0]);
					quadraticCurveTo(P2x, P2y, x[1], y[1]);
				} else if (3 <= nPts) { // normal case: 4 bracketing points
					logPoint(xIn, yIn);
					double P1x = x[1] + (tau / 3) * (x[2] - x[0]);
					double P1y = y[1] + (tau / 3) * (y[2] - y[0]);
					double P2x = x[2] - (tau / 3) * (x[3] - x[1]);
					double P2y = y[2] - (tau / 3) * (y[3] - y[1]);
					cubicCurveTo(P1x, P1y, P2x, P2y, x[2], y[2]);
				}
			}
		}

		// GChartCanvasLite requires CSS/RGBA color strings, but
		// GWTCanvas uses its own Color class instead, so we wrap:
		public void setStrokeStyle(String cssColor) {
			// Sharp angles of default MITER can overwrite adjacent pie slices
			setLineJoin(GWTCanvas.ROUND);
			setStrokeStyle(new Color(cssColor));
		}

		public void setFillStyle(String cssColor) {
			setFillStyle(new Color(cssColor));
		}
	} // class CurvyLineCanvasLite

	static final class CurvyLineCanvasFactory implements GChartCanvasFactory {
		public GChartCanvasLite create() {
			GChartCanvasLite result = new CurvyLineCanvasLite();
			return result;
		}
	}

	// This line "teaches" GChart how to create the canvas
	// widgets it needs to render any continuous,
	// non-rectangular, chart aspects (solid fill pie slices,
	// continously connected lines, etc.) clearly and
	// efficiently. It's generally best to do this exactly once,
	// when your entire GWT application loads.
	static {
		GChart.setCanvasFactory(new CurvyLineCanvasFactory());
	}
  
}
