package net.svcret.admin.client.ui.components;

import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class TwoColumnGrid extends FlexTable {

	private static int ourNextId;

	private int myNextRow;
	
	public TwoColumnGrid() {
		setStylePrimaryName(CssConstants.TWO_COLUMN_PROPERTY_GRID);
		myNextRow = -1;
	}

	public void addDescription(Label theLabel) {
		myNextRow++;
		int row = myNextRow;
		theLabel.addStyleName(CssConstants.TWO_COLUMN_PROPERTY_GRID_DESC);
		setWidget(row, 1, theLabel);
	}

	public HTML addDescription(String theDescription) {
		HTML widget = createDescriptionWidget(theDescription);
		myNextRow++;
		int row = myNextRow;
		setWidget(row, 1, widget);
		return widget;
	}
	
	public void addDescriptionToRight(String theDescription) {
		addDescriptionToRight(theDescription, 1);
	}

	public void addDescriptionToRight(String theDescription, int theRowsToSpan) {
		HTML widget = createDescriptionWidget(theDescription);
		addWidgetToRight(widget, theRowsToSpan);
	}

	public void addFullWidthCell(Widget theWidget) {
		myNextRow++;
		int row = myNextRow;
		setWidget(row, 0, theWidget);
		getFlexCellFormatter().setColSpan(row, 0, 3);
	}

	public void addRow(String theLabel, String theComponent) {
		addRow(theLabel, new Label(theComponent));
	}

	public HtmlLabel addRow(String theLabel, Widget theComponent) {
		String id= "tcg_" + ourNextId++;
		HtmlLabel lbl = new HtmlLabel(theLabel, id);
		
		theComponent.getElement().setId(id);
		
		myNextRow++;
		int row = myNextRow;
		
		setWidget(row, 0, lbl);
		setWidget(row, 1, theComponent);
		
		return lbl;
	}

	public void addRow(Widget theLabel, Widget theComponent) {
		myNextRow++;
		int row = myNextRow;
		setWidget(row, 0, theLabel);
		setWidget(row, 1, theComponent);
	}

	public void addWidgetToRight(Widget theWidget) {
		addWidgetToRight(theWidget, 1);
	}

	public void addWidgetToRight(Widget theWidget, int theRowsToSpan) {
		setWidget(myNextRow, 2, theWidget);
		if (theRowsToSpan>1) {
			getFlexCellFormatter().setRowSpan(myNextRow, 2, theRowsToSpan);
		}
	}

	private HTML createDescriptionWidget(String theDescription) {
		HTML widget = new HTML(theDescription);
		widget.addStyleName(CssConstants.TWO_COLUMN_PROPERTY_GRID_DESC);
		return widget;
	}

	public void addRowDoubleWidth(String theLabel, Widget theComponent) {
		addRow(theLabel, theComponent);
		getFlexCellFormatter().setColSpan(myNextRow, 1, 2);
	}

	public void setMaximizeSecondColumn() {
		getColumnFormatter().setWidth(1, "100%");
	}
}
