package net.svcret.admin.client.ui.components;

import com.google.gwt.cell.client.ButtonCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.text.shared.SimpleSafeHtmlRenderer;

/**
 * A {@link Cell} used to render a button.
 */
public class PButtonCell extends ButtonCell {

	private ImageResource myIcon;
	private String myText="";

	/**
	 * Construct a new ButtonCell that will use a {@link SimpleSafeHtmlRenderer}
	 * .
	 */
	public PButtonCell(ImageResource theIcon) {
		super();

		myIcon = theIcon;
	}

	public PButtonCell(ImageResource theIcon, String theText) {
		super();

		myIcon = theIcon;
		myText = theText;
	}

	@Override
	public void render(Context context, SafeHtml data, SafeHtmlBuilder sb) {
		ImageResource icon = myIcon;
		render(sb, icon, myText);
	}

	public static void render(SafeHtmlBuilder sb, ImageResource icon, String theText) {
		sb.appendHtmlConstant("<button type=\"button\" class=\"" + CssConstants.PUSHBUTTON + "\" tabindex=\"-1\">");
		sb.appendHtmlConstant("<img src=\"" + icon.getSafeUri().asString() + "\"/>");
		sb.appendHtmlConstant(theText);
		sb.appendHtmlConstant("</button>");
	}

	@Override
	protected void onEnterKeyDown(Context context, Element parent, String value, NativeEvent event, ValueUpdater<String> valueUpdater) {
		if (valueUpdater != null) {
			valueUpdater.update(value);
		}
	}
}
