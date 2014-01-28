package net.svcret.admin.client.ui.log;

import static net.svcret.admin.client.AdminPortal.*;
import net.svcret.admin.client.AdminPortal;
import net.svcret.admin.client.MyResources;
import net.svcret.admin.client.ui.components.CssConstants;
import net.svcret.admin.client.ui.components.HtmlPre;
import net.svcret.admin.client.ui.components.LoadingSpinner;
import net.svcret.admin.client.ui.components.PButton;
import net.svcret.admin.client.ui.components.TwoColumnGrid;
import net.svcret.admin.shared.DateUtil;
import net.svcret.admin.shared.model.BaseDtoSavedTransaction;
import net.svcret.admin.shared.model.BaseDtoServiceVersion;
import net.svcret.admin.shared.model.Pair;
import net.svcret.admin.shared.util.StringUtil;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.ScrollPanel;

public abstract class BaseViewSavedTransactionPanel extends FlowPanel {

	private boolean myHideRequest;
	private FlowPanel myReqPanel;
	private HtmlPre myReqPre;
	private FlowPanel myRespPanel;
	private HtmlPre myRespPre;
	private TwoColumnGrid myTopGrid;
	private LoadingSpinner myTopLoadingSpinner;
	private FlowPanel myTopPanel;
	
	public BaseViewSavedTransactionPanel() {
		myTopPanel = new FlowPanel();
		add(myTopPanel);

		myTopPanel.setStylePrimaryName(CssConstants.MAIN_PANEL);

		Label titleLabel = new Label(getPanelTitle());
		titleLabel.setStyleName(CssConstants.MAIN_PANEL_TITLE);
		myTopPanel.add(titleLabel);

		FlowPanel topContentPanel = new FlowPanel();
		topContentPanel.addStyleName(CssConstants.CONTENT_INNER_PANEL);

		myTopLoadingSpinner = new LoadingSpinner();
		myTopLoadingSpinner.show();
		topContentPanel.add(myTopLoadingSpinner);

		myTopGrid = new TwoColumnGrid();
		myTopPanel.add(topContentPanel);

		topContentPanel.add(myTopGrid);

	}
	private FlowPanel getReqPanel() {
		return myReqPanel;
	}
	private HtmlPre getReqPre() {
		return myReqPre;
	}

	private FlowPanel getRespPanel() {
		return myRespPanel;
	}

	private HtmlPre getRespPre() {
		return myRespPre;
	}
	public TwoColumnGrid getTopGrid() {
		return myTopGrid;
	}

	public LoadingSpinner getTopLoadingSpinner() {
		return myTopLoadingSpinner;
	}

	public boolean isHideRequest() {
		return myHideRequest;
	}

	
	public void setHideRequest(boolean theHideRequest) {
		myHideRequest = theHideRequest;
	}

	
	static void addResponseFormatButtons(HorizontalPanel respFunctions, String contentType, final HtmlPre respPre, final String messageBody, BaseDtoServiceVersion theSvcVerIfKnown) {
		boolean svcVerIsXml = theSvcVerIfKnown != null && theSvcVerIfKnown.getProtocol().getRequestContentType().contains("xml");
		if ((contentType != null && contentType.toLowerCase().contains("xml")) || svcVerIsXml) {
			respFunctions.add(new PButton(AdminPortal.IMAGES.iconFormat16(), AdminPortal.MSGS.actions_FormatXml(), new ClickHandler() {
				@Override
				public void onClick(ClickEvent theEvent) {
					respPre.setText(formatXml(messageBody));
					respPre.getElement().setClassName("brush: xml");
					syntaxHighliter();
				}
			}));
		}
		
		boolean svcVerIsJson = theSvcVerIfKnown != null && theSvcVerIfKnown.getProtocol().getRequestContentType().contains("json");
		if ((contentType != null && contentType.toLowerCase().contains("json")) || svcVerIsJson) {
			respFunctions.add(new PButton(AdminPortal.IMAGES.iconFormat16(), AdminPortal.MSGS.actions_FormatJson(), new ClickHandler() {
				@Override
				public void onClick(ClickEvent theEvent) {
					respPre.setText(formatJson(messageBody));
					respPre.getElement().setClassName("brush: js");
					syntaxHighliter();
				}
			}));
		}
	}

	public static SafeHtml formatActionLine(String theActionLine) {
		SafeHtmlBuilder b = new SafeHtmlBuilder();

		b.appendHtmlConstant("<span class='" + MyResources.CSS.messageActionLine() + "'>");
		b.appendEscaped(theActionLine);
		b.appendHtmlConstant("</span>");

		return b.toSafeHtml();
	}

	public static SafeHtml formatHeader(Pair<String> theNext) {
		SafeHtmlBuilder b = new SafeHtmlBuilder();

		b.appendHtmlConstant("<span class='" + CssConstants.MESSAGE_HEADER_KEY + "'>");
		b.appendEscaped(theNext.getFirst());

		b.appendHtmlConstant(": ");

		b.appendHtmlConstant("</span><span class='" + CssConstants.MESSAGE_HEADER_VALUE + "'>");
		b.appendEscaped(theNext.getSecond());
		b.appendHtmlConstant("</span>");

		return b.toSafeHtml();
	}

	protected void setSavedTransaction(BaseDtoSavedTransaction theSavedTransaction, BaseDtoServiceVersion theServiceVersionIfKnown) {
		myReqPanel=new FlowPanel();
		myReqPre= new HtmlPre();
		myRespPanel=new FlowPanel();
		myRespPre = new HtmlPre();

		getTopLoadingSpinner().hideCompletely();

		while (getTopGrid().getRowCount() > 0) {
			getTopGrid().removeRow(0);
		}

		addToStartOfTopGrid();

		getTopGrid().addHeader("Transaction");

		addToTransactionSectionOfGrid();

		if (theSavedTransaction.getOutcomeDescription() != null) {
			getTopGrid().addRow("Outcome", new Label(theSavedTransaction.getOutcomeDescription()));
		}
		getTopGrid().addRow(MSGS.recentMessagesGrid_ColTimestamp(), new Label(DateUtil.formatTime(theSavedTransaction.getTransactionTime())));
		getTopGrid().addRow("Latency", theSavedTransaction.getTransactionMillis() + "ms");
		if (StringUtil.isNotBlank(theSavedTransaction.getImplementationUrlId())) {
			getTopGrid().addRow(MSGS.recentMessagesGrid_ColImplementationUrl(), new Anchor(theSavedTransaction.getImplementationUrlId(), theSavedTransaction.getImplementationUrlHref()));
		}
		if (StringUtil.isNotBlank(theSavedTransaction.getFailDescription())) {
			getTopGrid().addRow(MSGS.recentMessagesGrid_ColFailDescription(), new Label(theSavedTransaction.getFailDescription(), true));
		}

		/*
		 * Request Message
		 */

		if (!isHideRequest()) {
			add(getReqPanel());
			getReqPanel().clear();

			getReqPanel().setStylePrimaryName(CssConstants.MAIN_PANEL);

			Label titleLabel = new Label(MSGS.viewRecentMessageServiceVersion_RequestMessage());
			titleLabel.setStyleName(CssConstants.MAIN_PANEL_TITLE);
			getReqPanel().add(titleLabel);

			FlowPanel reqContentPanel = new FlowPanel();
			reqContentPanel.addStyleName(CssConstants.CONTENT_INNER_PANEL);
			getReqPanel().add(reqContentPanel);

			Panel reqHeaderPanel = new FlowPanel();
			if (StringUtil.isNotBlank(theSavedTransaction.getRequestActionLine())) {
				reqHeaderPanel.add(new HTML(formatActionLine(theSavedTransaction.getRequestActionLine())));
			}

			for (Pair<String> next : theSavedTransaction.getRequestHeaders()) {
				reqHeaderPanel.add(new HTML(formatHeader(next)));
			}
			reqContentPanel.add(reqHeaderPanel);

			getReqPre().setText(theSavedTransaction.getRequestMessage());

			HorizontalPanel reqFunctions = new HorizontalPanel();
			addResponseFormatButtons(reqFunctions, theSavedTransaction.getRequestContentType(), getReqPre(), theSavedTransaction.getRequestMessage(), theServiceVersionIfKnown);
			reqContentPanel.add(reqFunctions);

			ScrollPanel reqMsgPanel = new ScrollPanel(getReqPre());
			reqMsgPanel.addStyleName(CssConstants.RECENT_MESSAGE_SCROLLER);
			reqContentPanel.add(reqMsgPanel);
		}

		/*
		 * Response Message
		 */

		add(getRespPanel());
		getRespPanel().clear();

		getRespPanel().setStylePrimaryName(CssConstants.MAIN_PANEL);

		Label respTitleLabel = new Label(MSGS.viewRecentMessageServiceVersion_ResponseMessage());
		respTitleLabel.setStyleName(CssConstants.MAIN_PANEL_TITLE);
		getRespPanel().add(respTitleLabel);

		FlowPanel respContentPanel = new FlowPanel();
		respContentPanel.addStyleName(CssConstants.CONTENT_INNER_PANEL);
		getRespPanel().add(respContentPanel);

		Panel respHeaderPanel = new FlowPanel();
		for (Pair<String> next : theSavedTransaction.getResponseHeaders()) {
			respHeaderPanel.add(new HTML(formatHeader(next)));
		}
		respContentPanel.add(respHeaderPanel);

		getRespPre().setText(theSavedTransaction.getResponseMessage());

		HorizontalPanel respFunctions = new HorizontalPanel();
		addResponseFormatButtons(respFunctions, theSavedTransaction.getResponseContentType(), getRespPre(), theSavedTransaction.getResponseMessage(), theServiceVersionIfKnown);

		respContentPanel.add(respFunctions);

		ScrollPanel respMsgPanel = new ScrollPanel(getRespPre());
		respMsgPanel.addStyleName(CssConstants.RECENT_MESSAGE_SCROLLER);
		respContentPanel.add(respMsgPanel);

	}

	protected abstract void addToTransactionSectionOfGrid();
	
	protected abstract void addToStartOfTopGrid();
	
	protected abstract String getPanelTitle();


	public static native String formatJson(String theExisting) /*-{
		return $wnd.vkbeautify.json(theExisting);
	}-*/;

	public static native String formatXml(String theExisting) /*-{
		return $wnd.vkbeautify.xml(theExisting);
	}-*/;

	public static native void syntaxHighliter() /*-{
		$wnd.SyntaxHighlighter.highlight();
	}-*/;

	public void showSpinner() {
		myTopLoadingSpinner.show();
		
	}

}
