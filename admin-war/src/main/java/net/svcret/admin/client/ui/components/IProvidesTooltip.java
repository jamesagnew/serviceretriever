package net.svcret.admin.client.ui.components;

import com.google.gwt.user.client.ui.Widget;

public interface IProvidesTooltip<T> {
	Widget getTooltip(T theObject);
}