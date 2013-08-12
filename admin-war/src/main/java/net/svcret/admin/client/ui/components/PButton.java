package net.svcret.admin.client.ui.components;

import net.svcret.admin.client.AdminPortal;

import com.google.gwt.dom.client.ButtonElement;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;

public class PButton extends Button {

	public PButton(String theText) {
		super(createButtonElement(null, theText));
		setStylePrimaryName(CssConstants.PUSHBUTTON);
	}

	public PButton(ImageResource theIcon, String theText) {
		super(createButtonElement(theIcon, theText));
		setStylePrimaryName(CssConstants.PUSHBUTTON);
	}

	public PButton(ImageResource theIcon) {
		super(createButtonElement(theIcon, null));
		setStylePrimaryName(CssConstants.PUSHBUTTON);
	}

	private static ButtonElement createButtonElement(ImageResource theIcon, String theText) {
		ButtonElement retVal = Document.get().createPushButtonElement();

		if (theIcon != null) {
			ImageElement img = Document.get().createImageElement();
			img.setSrc(theIcon.getSafeUri().asString());
			retVal.appendChild(img);
		}

		if (theText != null) {
			DivElement div = Document.get().createDivElement();
			div.setClassName(CssConstants.PUSHBUTTON_TEXT);
			div.setInnerText(theText);
			retVal.appendChild(div);
		}

		return retVal;
	}

	public PButton(String theText, ClickHandler theHandler) {
		this(theText);
		addClickHandler(theHandler);
	}

	public PButton(ImageResource theIcon, String theText, ClickHandler theClickHandler) {
		this(theIcon, theText);
		addClickHandler(theClickHandler);
	}

	public Widget toForwardNavButtonPanel() {
		// HorizontalPanel retVal = new HorizontalPanel();
		// retVal.setWidth("100%");
		// retVal.add(this);

		Element elem = (Element) getElement().getChild(0);
		elem.getStyle().setWidth(100, Unit.PCT);
		elem.getStyle().setPaddingRight(16, Unit.PX);
		
		Image image = new Image(AdminPortal.IMAGES.iconNext16());
		image.getElement().getStyle().setMarginLeft(-32, Unit.PX);
		getElement().appendChild(image.getElement());

		// retVal.add(image);
		// retVal.setCellWidth(image, "24px");
		//
		return this;
	}

	public Widget toBackwardNavButtonPanel() {
//		Element elem = (Element) getElement().getChild(0);
//		elem.getStyle().setWidth(100, Unit.PCT);
//		elem.getStyle().setPaddingRight(16, Unit.PX);
		
		Image image = new Image(AdminPortal.IMAGES.iconPrev16());
		getElement().insertFirst(image.getElement());
		
		return this;
	}
}
