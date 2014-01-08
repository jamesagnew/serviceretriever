package net.svcret.admin.client.ui.components;

import java.util.ArrayList;
import java.util.List;

import net.svcret.admin.client.AdminPortal;
import net.svcret.admin.client.MyResources;

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
	private List<String> myStyles;
	private boolean myExecuteSpinnerOnNonNullValue;

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
		if (myExecuteSpinnerOnNonNullValue && data != null) {
			sb.appendHtmlConstant("<span class=\"" + MyResources.CSS.spinnerStatusMessageStyle() + "\">");
			sb.appendHtmlConstant("<img src=\"" + AdminPortal.IMAGES.spinner().getSafeUri().asString() + "\" />");
			sb.appendHtmlConstant("Loading...");
			sb.appendHtmlConstant("</span>");
			return;
		}
		
		ImageResource icon = myIcon;
		render(sb, icon, myText, myStyles);
	}

	public static void render(SafeHtmlBuilder sb, ImageResource icon, String theText, List<String> theStyles) {
		String onClick = null;
		
		render(sb, icon, theText, theStyles, onClick);
	}

	public static void render(SafeHtmlBuilder sb, ImageResource icon, String theText, List<String> theStyles, String theOnClick) {
		StringBuilder styles = new StringBuilder();
		if (theStyles!=null) {
			for (String nextStyle : theStyles) {
				styles.append(" ").append(nextStyle);
			}
		}
		
		String onClick;
		if (theOnClick == null) {
			onClick = "";
		}else {
			onClick = " onclick=\"" + theOnClick + "\" ";
		}
		
		sb.appendHtmlConstant("<button " + onClick + " type=\"button\" class=\"" + CssConstants.PUSHBUTTON + styles.toString() + "\" tabindex=\"-1\">");
		sb.appendHtmlConstant("<img src=\"" + icon.getSafeUri().asString() + "\"/>");
		sb.appendHtmlConstant("<span class='"+CssConstants.PUSHBUTTON_TEXT+"'>");
		sb.appendHtmlConstant(theText);
		sb.appendHtmlConstant("</span>");
		sb.appendHtmlConstant("</button>");
	}

	@Override
	protected void onEnterKeyDown(Context context, Element parent, String value, NativeEvent event, ValueUpdater<String> valueUpdater) {
		if (valueUpdater != null) {
			valueUpdater.update(value);
		}
	}

	public void addStyle(String theStyle) {
		if (myStyles==null) {
			myStyles=new ArrayList<String>();
		}
		myStyles.add(theStyle);
	}

	public Cell<String> withExecuteSpinnerOnNonNullValue() {
		myExecuteSpinnerOnNonNullValue=true;
		return this;
	}
}
