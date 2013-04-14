package net.svcret.admin.client.ui.components;

import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Widget;

public class TwoColumnGrid extends FlexTable {

	public TwoColumnGrid() {
		setStyleName(CssConstants.TWO_COLUMN_PROPERTY_GRID);
	}
	
	private static int ourNextId;

	public void addRow(Widget theLabel, Widget theComponent) {
		int row = getRowCount();
		setWidget(row, 0, theLabel);
		setWidget(row, 1, theComponent);
	}

	public void addRow(String theLabel, Widget theComponent) {
		String id= "tcg_" + ourNextId++;
		HtmlLabel lbl = new HtmlLabel(theLabel, id);
		
		theComponent.getElement().setId(id);
		
		int row = getRowCount();
		setWidget(row, 0, lbl);
		setWidget(row, 1, theComponent);
	}
}
