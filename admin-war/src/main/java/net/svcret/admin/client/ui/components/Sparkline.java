package net.svcret.admin.client.ui.components;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.LabelElement;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.user.client.ui.Widget;

public class Sparkline extends Widget {

	private static int ourNextId = 0;
	private boolean myBar;
	private String myHeight = "20px";
	private String myId;
	private List<String> myTimes;
	private List<Integer> myValues;
	private String myWidth = "35px";
	private DateTimeFormat ourTimeFormat = DateTimeFormat.getFormat(PredefinedFormat.TIME_SHORT);

	public Sparkline(int[] theList, List<Long> theDates, String theText) {
		this(toList(theList), theDates, theText);
	}

	@Deprecated
	public Sparkline(int[] theList, String theText) {
		this(toList(theList), theText);
	}

	/**
	 * Constructor
	 * 
	 * @param theValues
	 *            The numberic values for the chart
	 */
	@Deprecated
	public Sparkline(List<Integer> theValues) {
		this(theValues, (String)null);
	}
	
	public Sparkline(List<Integer> theSuccess, List<Long> theDates) {
		this(theSuccess, theDates, null);
	}

	/**
	 * Constructor
	 * 
	 * @param theValues
	 *            The numberic values for the chart
	 */
	public Sparkline(List<Integer> theValues, List<Long> theDates, String theText) {
		this(theValues, theText);
		
		if (theDates != null) {
			myTimes = new ArrayList<String>();
			for (Long next : theDates) {
				myTimes.add(ourTimeFormat.format(new Date(next)));
			}
		}
		
	}

	/**
	 * Constructor
	 * 
	 * @param theValues
	 *            The numberic values for the chart
	 */
	@Deprecated
	public Sparkline(List<Integer> theValues, String theText) {

		if (theValues != null) {
			myValues = theValues;
		} else {
			myValues = Collections.emptyList();
		}

		myId = "sparkline" + (ourNextId++);

		SpanElement rootElement = Document.get().createSpanElement();
		setElement(rootElement);

		SpanElement spanElement = Document.get().createSpanElement();
		spanElement.setId(myId);
		rootElement.insertFirst(spanElement);

		if (theText != null) {
			Element textNode = Document.get().createElement("nobr");
			textNode.setInnerText(theText);
			rootElement.insertAfter(textNode, spanElement);
		}else {
			LabelElement textNode = Document.get().createLabelElement();
			textNode.setInnerHTML("&nbsp;");
			rootElement.insertAfter(textNode, spanElement);
		}
	}

	public Sparkline asBar(boolean theBar) {
		setBar(theBar);
		return this;
	}

	/**
	 * @return the height
	 */
	public String getHeight() {
		return myHeight;
	}

	/**
	 * @return the width
	 */
	public String getWidth() {
		return myWidth;
	}

	public void setBar(boolean theBar) {
		myBar = theBar;
	}

	/**
	 * @param theHeight
	 *            the height to set
	 */
	@Override
	public void setHeight(String theHeight) {
		myHeight = theHeight;
	}

	/**
	 * @param theWidth
	 *            the width to set
	 */
	@Override
	public void setWidth(String theWidth) {
		myWidth = theWidth;
	}
	
	/**
	 * @param theWidth
	 *            the width to set
	 * @return
	 */
	public Sparkline withWidth(String theWidth) {
		myWidth = theWidth;
		return this;
	}

	public String getNativeInvocation(String theElementId) {
		StringBuilder b = new StringBuilder();
		b.append("jsDrawSparkline('#");
		b.append(theElementId);
		b.append("', '");
		b.append(createValuesString());
		b.append("', '");
		b.append(myHeight);
		b.append("', '");
		b.append(myWidth);
		b.append("', '");
		b.append(createTypeString());
		b.append("', '");
		b.append(createEimelinesString());
		b.append("');");
		return b.toString();
	}
	
	private native void drawSparkline(final com.google.gwt.dom.client.Element theElement, String theValues, String theHeight, String theWidth, String theType, String theTimelines) /*-{
		var sparkOptions = new Array();
		sparkOptions['chartRangeMin'] = 0;
		sparkOptions['height'] = theHeight;
		sparkOptions['width'] = theWidth;
		sparkOptions['type'] = theType;
		sparkOptions['tooltipFormat'] = '{{offset:names}} - {{offset:values}}';
		if (theType == 'bar') {
			sparkOptions['barWidth'] = 2;
			sparkOptions['barSpacing'] = 0;
			sparkOptions['barColor'] = '#AAD';
			sparkOptions['zeroColor'] = '#CCF';
		}

		if (theTimelines != null) {
			var rangeNames = new Object();
			sparkOptions['tooltipValueLookups'] = rangeNames;
			rangeNames.names = new Array();
			rangeNames.values = new Array();

			var splitValues = theValues.split(",");
			var splitTimes = theTimelines.split(",");
			for ( var i = 0; i < splitTimes.length; i++) {
				rangeNames.names[i] = splitTimes[i];
				rangeNames.values[i] = splitValues[i];
			}
			
		}

		var splitValues = theValues.split(",");
		var values = new Array();
		for ( var i = 0; i < splitValues.length; i++) {
			values[i] = parseInt(splitValues[i]);
		}

		
		$wnd.jQuery(theElement).sparkline(values, sparkOptions);
	}-*/;

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onLoad() {
		
		String valuesString = createValuesString();
		String timelines = createEimelinesString();
		String type = createTypeString();
		
		Element firstChild = (Element) getElement().getFirstChild();
		drawSparkline(firstChild, valuesString, myHeight, myWidth, type, timelines);
	}

	private String createTypeString() {
		String type = myBar ? "bar" : "line";
		return type;
	}

	private String createEimelinesString() {
		StringBuilder tBuilder = new StringBuilder();
		if (myTimes != null) {
			for (String next : myTimes) {
				if (tBuilder.length()>0) {
					tBuilder.append(",");
				}
				tBuilder.append(next);
			}
		}
		String timelines = tBuilder.length() > 0 ? tBuilder.toString() : null;
		return timelines;
	}

	private String createValuesString() {
		StringBuilder valuesBuilder = new StringBuilder();
		for (Iterator<Integer> iter = myValues.iterator(); iter.hasNext();) {
			valuesBuilder.append(iter.next());
			if (iter.hasNext()) {
				valuesBuilder.append(",");
			}
		}
		String valuesString = valuesBuilder.toString();
		return valuesString;
	}

	private static List<Integer> toList(int[] theList) {
		ArrayList<Integer> retVal = new ArrayList<Integer>();
		for (int i : theList) {
			retVal.add(i);
		}
		return retVal;
	}

	public String getId() {
		return myId;
	}

}
