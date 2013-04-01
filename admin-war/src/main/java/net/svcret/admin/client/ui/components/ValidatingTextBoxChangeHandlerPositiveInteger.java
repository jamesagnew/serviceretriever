package net.svcret.admin.client.ui.components;

import net.svcret.admin.shared.util.StringUtil;

import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.TextBox;

public class ValidatingTextBoxChangeHandlerPositiveInteger implements ValueChangeHandler<String>, KeyPressHandler {

	public static final ValidatingTextBoxChangeHandlerPositiveInteger INSTANCE = new ValidatingTextBoxChangeHandlerPositiveInteger(1);
	public static final ValidatingTextBoxChangeHandlerPositiveInteger INSTANCE_0_OR_ABOVE = new ValidatingTextBoxChangeHandlerPositiveInteger(0);
	private int myMin;
	
	private ValidatingTextBoxChangeHandlerPositiveInteger(int theMin) {
		myMin = theMin;
	}
	
	@Override
		public void onValueChange(ValueChangeEvent<String> theEvent) {
		TextBox tb = (TextBox) theEvent.getSource();
		handleChange(tb);

	}

	private void handleChange(TextBox tb) {
		String text = tb.getValue();

		int value = 0;
		if (!StringUtil.isBlank(text)) {
			if (text.matches("^[0-9]+$")) {
				value = Integer.parseInt(text);
			}
		}

		tb.removeStyleName(CssConstants.TEXTBOX_WITH_ERR);
		if (value < myMin) {
			tb.addStyleName(CssConstants.TEXTBOX_WITH_ERR);
		}
		
	}

	@Override
	public void onKeyPress(KeyPressEvent theEvent) {
		TextBox tb = (TextBox) theEvent.getSource();
		handleChange(tb);
	}

}