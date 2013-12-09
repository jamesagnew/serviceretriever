package net.svcret.admin.client.ui.log;

import static net.svcret.admin.client.AdminPortal.MSGS;
import net.svcret.admin.client.AdminPortal;
import net.svcret.admin.client.MyResources;
import net.svcret.admin.client.nav.NavProcessor;
import net.svcret.admin.client.ui.components.CssConstants;
import net.svcret.admin.client.ui.components.HtmlPre;
import net.svcret.admin.client.ui.components.LoadingSpinner;
import net.svcret.admin.client.ui.components.PButton;
import net.svcret.admin.client.ui.components.TwoColumnGrid;
import net.svcret.admin.shared.DateUtil;
import net.svcret.admin.shared.IAsyncLoadCallback;
import net.svcret.admin.shared.Model;
import net.svcret.admin.shared.model.BaseDtoServiceVersion;
import net.svcret.admin.shared.model.DtoDomain;
import net.svcret.admin.shared.model.DtoDomainList;
import net.svcret.admin.shared.model.GRecentMessage;
import net.svcret.admin.shared.model.GService;
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
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.ScrollPanel;

public abstract class BaseViewRecentMessagePanel extends FlowPanel {

	private boolean myHideRequest;
	private FlowPanel myReqPanel;
	private HtmlPre myReqPre;
	private FlowPanel myRespPanel;
	private HtmlPre myRespPre;
	private TwoColumnGrid theGrid;
	private LoadingSpinner myTopLoadingSpinner;
	private FlowPanel myTopPanel;

	public BaseViewRecentMessagePanel() {
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

		theGrid = new TwoColumnGrid();
		myTopPanel.add(topContentPanel);

		topContentPanel.add(theGrid);

	}

	private void addResponseFormatButtons(HorizontalPanel respFunctions, String contentType, final HtmlPre respPre, final String messageBody, BaseDtoServiceVersion theSvcVer) {
		if ((contentType != null && contentType.toLowerCase().contains("xml")) || theSvcVer.getProtocol().getRequestContentType().contains("xml")) {
			respFunctions.add(new PButton(AdminPortal.IMAGES.iconFormat16(), AdminPortal.MSGS.actions_FormatXml(), new ClickHandler() {
				@Override
				public void onClick(ClickEvent theEvent) {
					respPre.setText(formatXml(messageBody));
					respPre.getElement().setClassName("brush: xml");
					syntaxHighliter();
				}
			}));
		}
		if ((contentType != null && contentType.toLowerCase().contains("json")) || theSvcVer.getProtocol().getRequestContentType().contains("json")) {
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

	
	private SafeHtml formatActionLine(String theActionLine) {
		SafeHtmlBuilder b = new SafeHtmlBuilder();

		b.appendHtmlConstant("<span class='" + MyResources.CSS.messageActionLine() + "'>");
		b.appendEscaped(theActionLine);
		b.appendHtmlConstant("</span>");

		return b.toSafeHtml();
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

	protected abstract String getPanelTitle();

	public boolean isHideRequest() {
		return myHideRequest;
	}

	public void setHideRequest(boolean theHideRequest) {
		myHideRequest = theHideRequest;
	}

	public void setMessage(final GRecentMessage theResult) {
		IAsyncLoadCallback<DtoDomainList> callback=new IAsyncLoadCallback<DtoDomainList>() {
			
			@Override
			public void onSuccess(DtoDomainList theDomainList) {
				myTopLoadingSpinner.hideCompletely();

				while (theGrid.getRowCount() > 0) {
					theGrid.removeRow(0);
				}

				
				theGrid.addHeader("Service Information");

				BaseDtoServiceVersion svcVer = theDomainList.getServiceVersionByPid(theResult.getServiceVersionPid());
 
				addServiceVersionInfoToPropertyGrid(theGrid, theDomainList,theResult.getServiceVersionPid());

				theGrid.addHeader("Transaction");

				if (theResult.getOutcomeDescription() != null) {
					theGrid.addRow("Outcome", new Label(theResult.getOutcomeDescription()));
				}
				if (theResult.getAuthorizationOutcome() != null) {
					theGrid.addRow("Authorization", new Label(theResult.getAuthorizationOutcome().getDescription()));
				}
				theGrid.addRow(MSGS.recentMessagesGrid_ColTimestamp(), new Label(DateUtil.formatTime(theResult.getTransactionTime())));
				theGrid.addRow("Latency", theResult.getTransactionMillis() + "ms");
				if (StringUtil.isNotBlank(theResult.getImplementationUrlId())) {
					theGrid.addRow(MSGS.recentMessagesGrid_ColImplementationUrl(), new Anchor(theResult.getImplementationUrlId(), theResult.getImplementationUrlHref()));
				}
				if (StringUtil.isNotBlank(theResult.getRequestHostIp())) {
					theGrid.addRow(MSGS.recentMessagesGrid_ColIp(), new Label(theResult.getRequestHostIp()));
				}
				if (StringUtil.isNotBlank(theResult.getFailDescription())) {
					theGrid.addRow(MSGS.recentMessagesGrid_ColFailDescription(), new Label(theResult.getFailDescription(), true));
				}

				/*
				 * Request Message
				 */

				if (!isHideRequest()) {
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

					FlowPanel reqContentPanel = new FlowPanel();
					reqContentPanel.addStyleName(CssConstants.CONTENT_INNER_PANEL);
					myReqPanel.add(reqContentPanel);

					Panel reqHeaderPanel = new FlowPanel();
					if (StringUtil.isNotBlank(theResult.getRequestActionLine())) {
						reqHeaderPanel.add(new HTML(formatActionLine(theResult.getRequestActionLine())));
					}
					
					for (Pair<String> next : theResult.getRequestHeaders()) {
						reqHeaderPanel.add(new HTML(formatHeader(next)));
					}
					reqContentPanel.add(reqHeaderPanel);

					myReqPre = new HtmlPre(theResult.getRequestMessage());

					HorizontalPanel reqFunctions = new HorizontalPanel();
					addResponseFormatButtons(reqFunctions, theResult.getRequestContentType(), myReqPre, theResult.getRequestMessage(), svcVer);
					reqContentPanel.add(reqFunctions);

					ScrollPanel reqMsgPanel = new ScrollPanel(myReqPre);
					reqMsgPanel.addStyleName(CssConstants.RECENT_MESSAGE_SCROLLER);
					reqContentPanel.add(reqMsgPanel);
				}

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

				FlowPanel respContentPanel = new FlowPanel();
				respContentPanel.addStyleName(CssConstants.CONTENT_INNER_PANEL);
				myRespPanel.add(respContentPanel);

				Panel respHeaderPanel = new FlowPanel();
				for (Pair<String> next : theResult.getResponseHeaders()) {
					respHeaderPanel.add(new HTML(formatHeader(next)));
				}
				respContentPanel.add(respHeaderPanel);

				myRespPre = new HtmlPre(theResult.getResponseMessage());

				HorizontalPanel respFunctions = new HorizontalPanel();
				addResponseFormatButtons(respFunctions, theResult.getResponseContentType(), myRespPre, theResult.getResponseMessage(), svcVer);

				respContentPanel.add(respFunctions);

				ScrollPanel respMsgPanel = new ScrollPanel(myRespPre);
				respMsgPanel.addStyleName(CssConstants.RECENT_MESSAGE_SCROLLER);
				respContentPanel.add(respMsgPanel);

			}

		};
		Model.getInstance().loadDomainList(callback);
		
		

	}

	public static native String formatJson(String theExisting) /*-{
		return $wnd.vkbeautify.json(theExisting);
	}-*/;

	public static native String formatXml(String theExisting) /*-{
		return $wnd.vkbeautify.xml(theExisting);
	}-*/;

	public static native void syntaxHighliter() /*-{
		$wnd.SyntaxHighlighter.highlight();
	}-*/;

	public static void addServiceVersionInfoToPropertyGrid(TwoColumnGrid theGrid, DtoDomainList theDomainList, long theServiceVersionPid) {
		DtoDomain domain = theDomainList.getDomainWithServiceVersion(theServiceVersionPid);
		GService service = theDomainList.getServiceWithServiceVersion(theServiceVersionPid);
		BaseDtoServiceVersion svcVer = theDomainList.getServiceVersionByPid(theServiceVersionPid);
		
		theGrid.addRow("Domain", domain.getName());
		theGrid.addWidgetToRight(new Hyperlink(AdminPortal.MSGS.actions_Edit(), NavProcessor.getTokenEditDomain(domain.getPid())));
		theGrid.addRow("Service", service.getName());
		theGrid.addWidgetToRight(new Hyperlink(AdminPortal.MSGS.actions_Edit(), NavProcessor.getTokenEditService(domain.getPid(), service.getPid())));
		theGrid.addRow("Version", svcVer.getId());
		theGrid.addWidgetToRight(new Hyperlink(AdminPortal.MSGS.actions_Edit(), NavProcessor.getTokenEditServiceVersion(svcVer.getPid())));
	}

}
