package net.svcret.admin.client.ui.components;

import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class TwoColumnGrid extends FlexTable {

	public TwoColumnGrid() {
		setStyleName(CssConstants.TWO_COLUMN_PROPERTY_GRID);
		getColumnFormatter().addStyleName(0, CssConstants.TWO_COLUMN_PROPERTY_GRID_FIRSTCOLUMN);
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
	
	public void addDescription(String theDescription) {
		HTML widget = createDescriptionWidget(theDescription);
		setWidget(getRowCount(), 1, widget);
	}

	private HTML createDescriptionWidget(String theDescription) {
		HTML widget = new HTML(theDescription);
		widget.addStyleName(CssConstants.TWO_COLUMN_PROPERTY_GRID_DESC);
		return widget;
	}

	public void addDescription(Label theLabel) {
		int row = getRowCount();
		theLabel.addStyleName(CssConstants.TWO_COLUMN_PROPERTY_GRID_DESC);
		setWidget(row, 1, theLabel);
	}

	public void addDescriptionToRight(String theDescription) {
		HTML widget = createDescriptionWidget(theDescription);
		setWidget(getRowCount()-1, 2, widget);
	}

	public void addRow(String theLabel, String theComponent) {
		addRow(theLabel, new Label(theComponent));
	}

	public void addFullWidthCell(Widget theWidget) {
		int row = getRowCount();
		setWidget(row, 0, theWidget);
		getFlexCellFormatter().setColSpan(row, 0, 3);
	}
}
