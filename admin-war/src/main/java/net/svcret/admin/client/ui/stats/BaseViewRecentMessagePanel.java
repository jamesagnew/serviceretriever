package net.svcret.admin.client.ui.stats;

import static net.svcret.admin.client.AdminPortal.*;
import net.svcret.admin.client.ui.components.CssConstants;
import net.svcret.admin.client.ui.components.HtmlPre;
import net.svcret.admin.client.ui.components.LoadingSpinner;
import net.svcret.admin.client.ui.components.TwoColumnGrid;
import net.svcret.admin.shared.model.GRecentMessage;

import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;

public abstract class BaseViewRecentMessagePanel extends FlowPanel {

	private LoadingSpinner myTopLoadingSpinner;
	private FlowPanel myTopPanel;
	private FlowPanel myReqPanel;
	private FlowPanel myRespPanel;

	public BaseViewRecentMessagePanel(long thePid) {
		myTopPanel = new FlowPanel();
		add(myTopPanel);
		
		myTopPanel.setStylePrimaryName(CssConstants.MAIN_PANEL);

		Label titleLabel = new Label(MSGS.viewRecentMessageServiceVersion_Title());
		titleLabel.setStyleName(CssConstants.MAIN_PANEL_TITLE);
		myTopPanel.add(titleLabel);

		myTopLoadingSpinner = new LoadingSpinner();
		myTopLoadingSpinner.show();
		myTopPanel.add(myTopLoadingSpinner);

		loadMessage(thePid);
	}

	protected abstract void loadMessage(long thePid);

	protected void setMessage(GRecentMessage theResult) {
		myTopLoadingSpinner.hideCompletely();
		
		TwoColumnGrid topGrid = new TwoColumnGrid();
		myTopPanel.add(topGrid);
		
		topGrid.addRow(MSGS.recentMessagesGrid_ColTimestamp(), new Label(DateUtil.formatTime(theResult.getTransactionTime())));
		topGrid.addRow(MSGS.recentMessagesGrid_ColImplementationUrl(), new Anchor(theResult.getImplementationUrl(), theResult.getImplementationUrl()));
		topGrid.addRow(MSGS.recentMessagesGrid_ColIp(), new Label(theResult.getRequestHostIp()));

		/*
		 * Request Message
		 */
		
		myReqPanel = new FlowPanel();
		add(myReqPanel);
		
		myReqPanel.setStylePrimaryName(CssConstants.MAIN_PANEL);

		Label titleLabel = new Label(MSGS.viewRecentMessageServiceVersion_RequestMessage());
		titleLabel.setStyleName(CssConstants.MAIN_PANEL_TITLE);
		myReqPanel.add(titleLabel);
		
		myReqPanel.add(new HtmlPre(theResult.getRequestMessage()));
		
		/*
		 * Response Message
		 */
		
		myRespPanel = new FlowPanel();
		add(myRespPanel);
		
		myRespPanel.setStylePrimaryName(CssConstants.MAIN_PANEL);

		Label respTitleLabel = new Label(MSGS.viewRecentMessageServiceVersion_ResponseMessage());
		respTitleLabel.setStyleName(CssConstants.MAIN_PANEL_TITLE);
		myRespPanel.add(respTitleLabel);
		
		myRespPanel.add(new HtmlPre(theResult.getResponseMessage()));
		
	}
	
}
