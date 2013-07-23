package net.svcret.admin.client.ui.components;

import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.ValueBoxBase;

public class EditableField extends FlowPanel {

	private String myValue;
	private Label myLabel;
	private ValueBoxBase<String> myEditorComponent;
	private String myEmptyTextToDisplay;

	public EditableField() {
		setLabelMode();
	}

	public void setLabelMode() {
		this.clear();
		if (myLabel == null) {
			myLabel = new Label();
			myLabel.addStyleName(CssConstants.EDITABLE_LABEL);
			myLabel.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent theEvent) {
					setEditorMode();
				}
			});
		}

		this.add(myLabel);
		if (myValue == null) {
			myLabel.setText(myEmptyTextToDisplay);
		}else {
			myLabel.setText(myValue);
		}
	}

	private void setEditorMode() {
		if (myEditorComponent == null) {
			myEditorComponent = new TextArea();
			myEditorComponent.addValueChangeHandler(new ValueChangeHandler<String>() {
				@Override
				public void onValueChange(ValueChangeEvent<String> theEvent) {
					myValue = theEvent.getValue();
				}
			});
			
			myEditorComponent.addBlurHandler(new BlurHandler() {
				@Override
				public void onBlur(BlurEvent theEvent) {
					setLabelMode();
				}
			});
		}

		this.clear();
		this.add(myEditorComponent);
		myEditorComponent.setValue(myValue, false);
		myEditorComponent.setFocus(true);
	}

	public String getValue() {
		return myValue;
	}

	public void setEmptyTextToDisplay(String theValue) {
		myEmptyTextToDisplay =theValue; 
		setLabelMode();		
	}

	public void setValue(String theValue) {
		myValue = theValue;
		setLabelMode();		
	}

}
