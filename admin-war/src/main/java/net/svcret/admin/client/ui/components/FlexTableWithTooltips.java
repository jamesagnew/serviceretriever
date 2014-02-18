package net.svcret.admin.client.ui.components;

import java.util.ArrayList;
import java.util.List;

import net.svcret.admin.client.ui.components.TooltipListener.Tooltip;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Widget;

public class FlexTableWithTooltips<T> extends FlexTable {
	private List<T> myBackingList;

	public FlexTableWithTooltips(List<T> theBackingList) {
		sinkEvents(Event.ONMOUSEOVER | Event.ONMOUSEOUT);
		myBackingList=theBackingList;
	}

	private List<List<IProvidesTooltip<T>>> myTooltipProviders = new ArrayList<>();

	public void setTooltipProvider(int theRow, int theCol, IProvidesTooltip<T> theTooltipProvider) {
		ensureTooltipRow(theRow);
		ensureTooltipCol(theRow, theCol);
		myTooltipProviders.get(theRow).set(theCol, theTooltipProvider);
	}

	public void clearTooltipRow(int theRow) {
		ensureTooltipRow(theRow);
		for (int i = 0; i < myTooltipProviders.get(theRow).size(); i++) {
			myTooltipProviders.get(theRow).set(i, null);
		}
	}

	private void ensureTooltipCol(int theRow, int theCol) {
		while (myTooltipProviders.get(theRow).size() <= theCol) {
			myTooltipProviders.get(theRow).add(null);
		}
	}

	private void ensureTooltipRow(int theRow) {
		while (myTooltipProviders.size() <= theRow) {
			myTooltipProviders.add(new ArrayList< IProvidesTooltip<T>>());
		}
	}

	private int myCurrentTooltipRow;
	private int myCurrentTooltipCol;
	private Tooltip myCurrentTooltip;

	@Override
	public void onBrowserEvent(Event theEvent) {
		super.onBrowserEvent(theEvent);

		Element td = getEventTargetCell(theEvent);
		if (td == null || !"TD".equalsIgnoreCase(td.getTagName())) {
			return;
		}

		Element tr = (Element) td.getParentNode();
		if (tr==null || !"TR".equalsIgnoreCase(tr.getTagName())) {
			return;
		}
		
		int rowStr = indexWithinParent(tr);
		int colStr = indexWithinParent(td);

		switch (DOM.eventGetType(theEvent)) {
		case Event.ONMOUSEOVER: {
			GWT.log("Mouseover row " + rowStr + " col " + colStr);
			if (rowStr == myCurrentTooltipRow && colStr == myCurrentTooltipCol) {
				return;
			}
			if (myCurrentTooltip != null) {
				myCurrentTooltip.hideTooltip();
				myCurrentTooltip = null;
			}

			if (myTooltipProviders.size() > rowStr) {
				List< IProvidesTooltip<T>> cols = myTooltipProviders.get(rowStr);
				if (cols.size() > colStr) {
					 IProvidesTooltip<T> col = cols.get(colStr);
					if (col != null) {
						int index = rowStr-1;
						T model = myBackingList.get(index);
						Widget tooltipContents = col.getTooltip(model);
						if (tooltipContents != null) {
							GWT.log("Showing tooltip for row " + rowStr + " col " + colStr);
							Tooltip tooltip = new Tooltip(td, tooltipContents);
							tooltip.displayPopup();
							myCurrentTooltip = tooltip;
						}
					}
				}
			}

			myCurrentTooltipCol = colStr;
			myCurrentTooltipRow = rowStr;
			break;
		}
		case Event.ONMOUSEOUT: {
			GWT.log("Mouseout  row " + rowStr + " col " + colStr);
			if (rowStr == myCurrentTooltipRow && colStr == myCurrentTooltipCol) {
				if (myCurrentTooltip != null) {
					GWT.log("Hiding tooltip for row " + rowStr + " col " + colStr);
					myCurrentTooltip.hideTooltip();
					myCurrentTooltip = null;
					myCurrentTooltipCol=-1;
					myCurrentTooltipRow=-1;
				}
			}
			break;
		}
		}

	}

	private static int indexWithinParent(Element theElement) {
		Element parent = (Element)theElement.getParentNode();
		for (int i = 0; i < parent.getChildCount(); i++) {
			if (parent.getChild(i).equals(theElement)) {
				return i;
			}
		}
		return 0;
	}
}