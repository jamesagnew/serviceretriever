package net.svcret.admin.client.ui.components;

import com.google.gwt.dom.client.ButtonElement;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Button;

public class PButton extends Button {

	public PButton(String theText) {
		setText(theText);
		setStylePrimaryName(CssConstants.PUSHBUTTON);
	}

	public PButton(ImageResource theIcon, String theText) {
		super(createButtonElement(theIcon, theText));
		setStylePrimaryName(CssConstants.PUSHBUTTON);
	}

	private static ButtonElement createButtonElement(ImageResource theIcon, String theText) {
		ButtonElement retVal = Document.get().createPushButtonElement();

		ImageElement img = Document.get().createImageElement();
		img.setSrc(theIcon.getSafeUri().asString());
		retVal.appendChild(img);

		DivElement div = Document.get().createDivElement();
		div.setClassName(CssConstants.PUSHBUTTON_TEXT);
		div.setInnerText(theText);
		retVal.appendChild(div);
		
		return retVal;
	}

	public PButton(String theText, ClickHandler theHandler) {
		this(theText);
		addClickHandler(theHandler);
	}

}
