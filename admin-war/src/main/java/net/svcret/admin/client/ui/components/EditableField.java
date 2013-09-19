package net.svcret.admin.client.ui.components;

import java.util.ArrayList;
import java.util.List;

import net.svcret.admin.shared.util.StringUtil;

import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.ValueBoxBase;

public class EditableField extends FlowPanel implements HasValue<String> {

	private ValueBoxBase<String> myEditorComponent;
	private String myEmptyTextToDisplay = "(Click to add a value)";
	private Label myLabel;
	private boolean myLabelIsPlainText;
	private int myMaxFieldWidth;
	private boolean myMultiline;
	private boolean myProcessHtml = true;
	private boolean myShowTooltip;
	private boolean myTransparent;
	private String myValue;
	private List<ValueChangeHandler<String>> myValueChangeHandlers = new ArrayList<ValueChangeHandler<String>>();

	public EditableField() {

	}

	@Override
	public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> theHandler) {
		myValueChangeHandlers.add(theHandler);

		return new HandlerRegistration() {
			@Override
			public void removeHandler() {
				throw new UnsupportedOperationException();
			}
		};

	}

	@Override
	public String getValue() {
		return myValue;
	}

	public void setEditorMode() {
		if (myEditorComponent == null) {

			if (myMultiline) {
				myEditorComponent = new TextArea();
			} else {
				myEditorComponent = new TextBox();
			}

			myEditorComponent.addKeyDownHandler(new KeyDownHandler() {
				@Override
				public void onKeyDown(KeyDownEvent theEvent) {
					if (!myMultiline && theEvent.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
						myEditorComponent.setFocus(false);
						setLabelMode();
					}
				}
			});
			
			myEditorComponent.addValueChangeHandler(new ValueChangeHandler<String>() {
				@Override
				public void onValueChange(ValueChangeEvent<String> theEvent) {
					String newValue = theEvent.getValue();
					if (StringUtil.equals(newValue, myValue)) {
						return;
					}
					myValue = newValue;

					for (ValueChangeHandler<String> next : myValueChangeHandlers) {
						next.onValueChange(theEvent);
					}
				}
			});

			myEditorComponent.addBlurHandler(new BlurHandler() {
				@Override
				public void onBlur(BlurEvent theEvent) {
					ValueChangeEvent.fireIfNotEqual(myEditorComponent, myValue, myEditorComponent.getValue());
					setLabelMode();
				}
			});
		}

		this.clear();
		this.add(myEditorComponent);
		myEditorComponent.setValue(myValue, false);
		myEditorComponent.setFocus(true);
	}

	public void setEmptyTextToDisplay(String theValue) {
		myEmptyTextToDisplay = theValue;
		setLabelMode();
	}

	public void setLabelIsPlainText(boolean theLabelIsPlainText) {
		myLabelIsPlainText = theLabelIsPlainText;
	}

	public void setLabelMode() {
		this.clear();
		if (myLabel == null) {
			if (myLabelIsPlainText) {
				myLabel = new Label();
				if (myMaxFieldWidth>0) {
					myLabel.getElement().getStyle().setPropertyPx("maxWidth", myMaxFieldWidth);
					myLabel.getElement().getStyle().setProperty("textOverflow", "ellipsis");
					myLabel.getElement().getStyle().setProperty("overflow", "hidden");
				}
			}else {
				myLabel = new HTML();
			}
			myLabel.addStyleName(CssConstants.EDITABLE_LABEL);
			myLabel.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent theEvent) {
					setEditorMode();
				}
			});
		}

		this.add(myLabel);
		if (StringUtil.isBlank(myValue)) {
			myLabel.setText(myEmptyTextToDisplay);
			myLabel.addStyleName(CssConstants.EDITABLE_LABEL_NOVALUE);
		} else {

			String value;
			if (myProcessHtml) {
				value = StringUtil.convertPlaintextToHtml(myValue);
			} else {
				value = myValue;
			}
			if (myLabelIsPlainText) {
				myLabel.setText(value);
			} else {
				((HTML) myLabel).setHTML(value);
			}

			myLabel.removeStyleName(CssConstants.EDITABLE_LABEL_NOVALUE);
		}

		if (myTransparent) {
			myLabel.addStyleName(CssConstants.EDITABLE_LABEL_TRANSPARENT);
		} else {
			myLabel.removeStyleName(CssConstants.EDITABLE_LABEL_TRANSPARENT);
		}
		
		if (myShowTooltip) {
			myLabel.setTitle(myValue);
		}

	}

	public void setMaxFieldWidth(int theMaxFieldWidth) {
		myMaxFieldWidth = theMaxFieldWidth;
	}

	public void setMultiline(boolean theMultiline) {
		myMultiline = theMultiline;
	}

	public void setProcessHtml(boolean theB) {
		myProcessHtml = theB;
	}

	public void setShowTooltip(boolean theShowTooltip) {
		myShowTooltip = theShowTooltip;
	}

	public void setTransparent(boolean theB) {
		myTransparent = theB;
	}

	@Override
	public void setValue(String theValue) {
		myValue = theValue;
		setLabelMode();
	}

	@Override
	public void setValue(String theValue, boolean theFireEvents) {
		boolean change = !StringUtil.equals(theValue, myValue);
		myValue = theValue;
		setLabelMode();

		if (theFireEvents && change) {
			ValueChangeEvent<String> event = new ValueChangeEvent<String>(theValue) {
			};
			for (ValueChangeHandler<String> next : myValueChangeHandlers) {
				next.onValueChange(event);
			}
		}
	}

	@Override
	protected void onLoad() {
		setLabelMode();
	}

	public String getValueOrBlank() {
		return getValue() != null ? getValue() : "";
	}

}
