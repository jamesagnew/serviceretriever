package net.svcret.admin.client.ui.stats;

import static net.svcret.admin.client.AdminPortal.*;
import net.svcret.admin.client.ui.components.CssConstants;
import net.svcret.admin.client.ui.components.HtmlPre;
import net.svcret.admin.client.ui.components.LoadingSpinner;
import net.svcret.admin.client.ui.components.PButton;
import net.svcret.admin.client.ui.components.TwoColumnGrid;
import net.svcret.admin.shared.model.GRecentMessage;
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

public abstract class BaseViewRecentMessagePanel extends FlowPanel {

	private LoadingSpinner myTopLoadingSpinner;
	private FlowPanel myTopPanel;
	private FlowPanel myReqPanel;
	private FlowPanel myRespPanel;
	private HtmlPre myReqPre;
	private HtmlPre myRespPre;
	private TwoColumnGrid myTopGrid;

	public BaseViewRecentMessagePanel() {
		myTopPanel = new FlowPanel();
		add(myTopPanel);

		myTopPanel.setStylePrimaryName(CssConstants.MAIN_PANEL);

		Label titleLabel = new Label(getPanelTitle());
		titleLabel.setStyleName(CssConstants.MAIN_PANEL_TITLE);
		myTopPanel.add(titleLabel);

		myTopLoadingSpinner = new LoadingSpinner();
		myTopLoadingSpinner.show();
		myTopPanel.add(myTopLoadingSpinner);

		myTopGrid = new TwoColumnGrid();
		myTopPanel.add(myTopGrid);

	}

	protected abstract String getPanelTitle();

	public void setMessage(final GRecentMessage theResult) {
		myTopLoadingSpinner.hideCompletely();

		myTopGrid.clear();
		if (theResult.getOutcomeDescription() != null) {
			myTopGrid.addRow("Outcome", new Label(theResult.getOutcomeDescription()));
		}
		if (theResult.getAuthorizationOutcome() != null) {
			myTopGrid.addRow("Authorization", new Label(theResult.getAuthorizationOutcome().getDescription()));
		}
		myTopGrid.addRow(MSGS.recentMessagesGrid_ColTimestamp(), new Label(DateUtil.formatTime(theResult.getTransactionTime())));
		
		if (StringUtil.isNotBlank(theResult.getImplementationUrlId())) {
			myTopGrid.addRow(MSGS.recentMessagesGrid_ColImplementationUrl(), new Anchor(theResult.getImplementationUrlId(), theResult.getImplementationUrlHref()));
		}
		
		if (StringUtil.isNotBlank(theResult.getRequestHostIp())) {
			myTopGrid.addRow(MSGS.recentMessagesGrid_ColIp(), new Label(theResult.getRequestHostIp()));
		}

		/*
		 * Request Message
		 */

		if (myReqPanel == null) {
			myReqPanel = new FlowPanel();
			add(myReqPanel);
		} else {
			myReqPanel.clear();
		}

		myReqPanel.setStylePrimaryName(CssConstants.MAIN_PANEL);

		Label titleLabel = new Label(MSGS.viewRecentMessageServiceVersion_RequestMessage());
		titleLabel.setStyleName(CssConstants.MAIN_PANEL_TITLE);
		myReqPanel.add(titleLabel);

		Panel reqHeaderPanel = new FlowPanel();
		for (Pair<String> next : theResult.getRequestHeaders()) {
			reqHeaderPanel.add(new HTML(formatHeader(next)));
		}
		myReqPanel.add(reqHeaderPanel);

		HorizontalPanel reqFunctions = new HorizontalPanel();
		if (theResult.getRequestContentType() != null && theResult.getRequestContentType().toLowerCase().contains("xml")) {
			reqFunctions.add(new PButton("Format XML", new ClickHandler() {
				@Override
				public void onClick(ClickEvent theEvent) {
					myReqPre.setText(formatXml(theResult.getRequestMessage()));
				}
			}));
		}
		;
		myReqPanel.add(reqFunctions);

		myReqPre = new HtmlPre(theResult.getRequestMessage());
		ScrollPanel reqMsgPanel = new ScrollPanel(myReqPre);
		reqMsgPanel.addStyleName(CssConstants.RECENT_MESSAGE_SCROLLER);
		myReqPanel.add(reqMsgPanel);

		/*
		 * Response Message
		 */

		if (myRespPanel == null) {
			myRespPanel = new FlowPanel();
			add(myRespPanel);
		} else {
			myRespPanel.clear();
		}

		myRespPanel.setStylePrimaryName(CssConstants.MAIN_PANEL);

		Label respTitleLabel = new Label(MSGS.viewRecentMessageServiceVersion_ResponseMessage());
		respTitleLabel.setStyleName(CssConstants.MAIN_PANEL_TITLE);
		myRespPanel.add(respTitleLabel);

		Panel respHeaderPanel = new FlowPanel();
		for (Pair<String> next : theResult.getResponseHeaders()) {
			respHeaderPanel.add(new HTML(formatHeader(next)));
		}
		myRespPanel.add(respHeaderPanel);

		HorizontalPanel respFunctions = new HorizontalPanel();
		if (theResult.getResponseContentType() != null && theResult.getResponseContentType().toLowerCase().contains("xml")) {
			respFunctions.add(new PButton("Format XML", new ClickHandler() {
				@Override
				public void onClick(ClickEvent theEvent) {
					myRespPre.setText(formatXml(theResult.getResponseMessage()));
				}
			}));
		}
		;
		myRespPanel.add(respFunctions);

		myRespPre = new HtmlPre(theResult.getResponseMessage());
		ScrollPanel respMsgPanel = new ScrollPanel(myRespPre);
		respMsgPanel.addStyleName(CssConstants.RECENT_MESSAGE_SCROLLER);
		myRespPanel.add(respMsgPanel);

	}

	private SafeHtml formatHeader(Pair<String> theNext) {
		SafeHtmlBuilder b = new SafeHtmlBuilder();

		b.appendHtmlConstant("<span class='" + CssConstants.MESSAGE_HEADER_KEY + "'>");
		b.appendEscaped(theNext.getFirst());

		b.appendHtmlConstant(": ");

		b.appendHtmlConstant("</span><span class='" + CssConstants.MESSAGE_HEADER_VALUE + "'>");
		b.appendEscaped(theNext.getSecond());
		b.appendHtmlConstant("</span>");

		return b.toSafeHtml();
	}

	private native String formatXml(String theExisting) /*-{
		return $wnd.vkbeautify.xml(theExisting);
	}-*/;

}
