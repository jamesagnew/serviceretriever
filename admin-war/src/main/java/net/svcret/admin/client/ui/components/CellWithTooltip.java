package net.svcret.admin.client.ui.components;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import net.svcret.admin.client.ui.components.TooltipListener.Tooltip;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.view.client.ListDataProvider;

public class CellWithTooltip<T> extends AbstractCell<SafeHtml> {

	private ListDataProvider<T> myListDataProvider;
	private IProvidesTooltip<T> myTooltipProvider;
	private Tooltip myTooltip;
	private int myCurrentTooltipIndex;

	public CellWithTooltip(ListDataProvider<T> theListDataProvider, IProvidesTooltip<T> theTooltipProvider) {
		super(createEvents());

		myListDataProvider = theListDataProvider;
		myTooltipProvider = theTooltipProvider;
	}

	private static Set<String> createEvents() {
		Set<String> retVal = new HashSet<String>();
		retVal.add(BrowserEvents.MOUSEOVER);
		retVal.add(BrowserEvents.MOUSEOUT);
		return retVal;
	}

	@Override
	public void onBrowserEvent(Context theContext, Element theParent, SafeHtml theValue, NativeEvent theEvent, ValueUpdater<SafeHtml> theValueUpdater) {
		super.onBrowserEvent(theContext, theParent, theValue, theEvent, theValueUpdater);
		if (theEvent.getType().equals(BrowserEvents.MOUSEOVER)) {
			GWT.log(new Date() + " - Mouseover " + theContext.getIndex() + " " + theContext.getColumn());
			int index = theContext.getIndex();
			T item = myListDataProvider.getList().get(index);

			myTooltip = new TooltipListener.Tooltip(theParent, myTooltipProvider.getTooltip(item));
			myTooltip.displayPopup();

			myCurrentTooltipIndex = index;

		} else if (theEvent.getType().equals(BrowserEvents.MOUSEOUT)) {
			GWT.log(new Date() + " - Mouseout " + theContext.getIndex() + " " + theContext.getColumn());
			int index = theContext.getIndex();

			if (myTooltip != null && index == myCurrentTooltipIndex) {
				myTooltip.hide();
				myTooltip = null;
				myCurrentTooltipIndex = -1;
			}
		}

	}

	@Override
	public void render(Context context, SafeHtml value, SafeHtmlBuilder sb) {
		if (value != null) {
			sb.append(value);
		}
	}
}
