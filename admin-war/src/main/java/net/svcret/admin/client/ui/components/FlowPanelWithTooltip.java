package net.svcret.admin.client.ui.components;

import net.svcret.admin.client.ui.components.TooltipListener.Tooltip;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.FlowPanel;

public class FlowPanelWithTooltip extends FlowPanel {

	private IProvidesTooltip myTooltipProvider;
	private Tooltip myTooltip;

	public FlowPanelWithTooltip() {
		this(null);
	}

	public FlowPanelWithTooltip(IProvidesTooltip theTooltipProvider) {
		sinkEvents(Event.ONMOUSEOVER);
		sinkEvents(Event.ONMOUSEOUT);
		myTooltipProvider = theTooltipProvider;
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
				myTooltip = new TooltipListener.Tooltip(this, myTooltipProvider.getTooltip());
				myTooltip.displayPopup();
			}
			break;

		}
	}

	public void setTooltipProvider(IProvidesTooltip theTooltipProvider) {
		myTooltipProvider = theTooltipProvider;
	}

}
