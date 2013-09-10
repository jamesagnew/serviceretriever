package net.svcret.admin.client.ui.components;

import net.svcret.admin.client.ui.components.TooltipListener.Tooltip;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.FlowPanel;

public class FlowPanelWithTooltip<T> extends FlowPanel {

	private IProvidesTooltip<T> myTooltipProvider;
	private Tooltip myTooltip;
	private T myObject;

	public FlowPanelWithTooltip(IProvidesTooltip<T> theTooltipProvider, T theObject) {
		sinkEvents(Event.ONMOUSEOVER);
		sinkEvents(Event.ONMOUSEOUT);
		myTooltipProvider = theTooltipProvider;
		myObject = theObject;
	}

	public FlowPanelWithTooltip(T theObject) {
		this(null, theObject);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onBrowserEvent(Event theArg0) {
		super.onBrowserEvent(theArg0);

		switch (DOM.eventGetType(theArg0)) {
		case Event.ONMOUSEOUT:
			if (myTooltip != null) {
				myTooltip.hideTooltip();
				myTooltip = null;
			}
			break;

		case Event.ONMOUSEOVER:
			if (myTooltip == null) {
				myTooltip = new TooltipListener.Tooltip(this, myTooltipProvider.getTooltip(myObject));
				myTooltip.displayPopup();
			}
			break;

		}
	}

	public void setTooltipProvider(IProvidesTooltip<T> theTooltipProvider) {
		myTooltipProvider = theTooltipProvider;
	}

}
