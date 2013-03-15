package ca.uhn.sail.proxy.admin.client.ui.components;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Node;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.user.client.ui.Widget;

public class Sparkline extends Widget {

    private static int ourNextId = 0;
    private String myId;
    private List<Integer> myValues;
    private String myHeight = "20px";
    private String myWidth = "35px";
    
    /**
	 * @return the height
	 */
	public String getHeight() {
		return myHeight;
	}



	/**
	 * @param theHeight the height to set
	 */
	public void setHeight(String theHeight) {
		myHeight = theHeight;
	}



	/**
	 * @return the width
	 */
	public String getWidth() {
		return myWidth;
	}



	/**
	 * @param theWidth the width to set
	 */
	public void setWidth(String theWidth) {
		myWidth = theWidth;
	}



	/**
     * Constructor
     * @param theValues The numberic values for the chart
     */
    public Sparkline(List<Integer> theValues, String theText) {

        myValues = theValues;
        myId = "sparkline" + (ourNextId++);

        SpanElement rootElement = Document.get().createSpanElement();
        setElement(rootElement);
        
        SpanElement spanElement = Document.get().createSpanElement();
        spanElement.setId(myId);
        rootElement.insertFirst(spanElement);
        
        if (theText != null) {
            Node textNode = Document.get().createTextNode(theText);
            rootElement.insertAfter(textNode, spanElement);
        }
    }


    
    public Sparkline(int[] theList, String theText) {
    	this(toList(theList), theText);
	}



	private static List<Integer> toList(int[] theList) {
		ArrayList<Integer> retVal = new ArrayList<Integer>();
		for (int i : theList) {
			retVal.add(i);
		}
		return retVal;
	}



	/**
     * {@inheritDoc}
     */
    @Override
    protected void onLoad() {
        StringBuilder valuesBuilder = new StringBuilder();
        for (Iterator<Integer> iter = myValues.iterator(); iter.hasNext(); ) {
            valuesBuilder.append(iter.next());
            if (iter.hasNext()) {
                valuesBuilder.append(",");
            }
        }
        
        Element firstChild = (Element) getElement().getFirstChild();
        String valuesString = valuesBuilder.toString();
        drawSparkline(firstChild, valuesString, myHeight, myWidth);
    }
    
    private native void drawSparkline(final com.google.gwt.dom.client.Element theElement, String theValues, String theHeight, String theWidth) /*-{
        var sparkOptions = new Array();
        sparkOptions['chartRangeMin'] = 0;
        sparkOptions['height'] = theHeight;
        sparkOptions['width'] = theWidth;

        var splitValues = theValues.split(",");
        
        var values = new Array();
        for (var i = 0; i < splitValues.length; i++) {
            values[i] = parseInt( splitValues[i] );
        }
        
        $wnd.jQuery(theElement).sparkline(values, sparkOptions);
    }-*/;
    
}
