package net.svcret.admin.client.ui.stats;

import static net.svcret.admin.client.AdminPortal.MSGS;

import java.util.List;

import net.svcret.admin.client.AdminPortal;
import net.svcret.admin.client.nav.NavProcessor;
import net.svcret.admin.client.ui.components.CssConstants;
import net.svcret.admin.client.ui.components.HtmlBr;
import net.svcret.admin.client.ui.components.PButton;
import net.svcret.admin.shared.model.GRecentMessage;
import net.svcret.admin.shared.util.StringUtil;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Hyperlink;

public class RecentMessagesGrid extends FlowPanel {

	private static final int COL_ACTION = 0;
	private static final int COL_TIMESTAMP = 1;
	private static final int COL_IP = 2;
	private static final int COL_USER = 3;
	private static final int COL_DOMAIN = 4;
	private static final int COL_URL = 5;
	private static final int COL_VIEW = 6;
	private static final int COL_MILLIS = 7;
	private static final int COL_AUTHORIZATION = 8;

	private Grid myGrid;

	public RecentMessagesGrid(List<GRecentMessage> theList, boolean theIsUserGrid) {
		myGrid = new Grid(theList.size() + 1, 9);
		myGrid.addStyleName(CssConstants.PROPERTY_TABLE);
		add(myGrid);

		myGrid.setText(0, COL_TIMESTAMP, MSGS.recentMessagesGrid_ColTimestamp());
		myGrid.setText(0, COL_IP, MSGS.recentMessagesGrid_ColIp());
		myGrid.setText(0, COL_URL, MSGS.recentMessagesGrid_ColImplementationUrl());
		myGrid.setText(0, COL_VIEW, MSGS.recentMessagesGrid_ColView());
		myGrid.setText(0, COL_MILLIS, MSGS.recentMessagesGrid_ColMillis());
		myGrid.setText(0, COL_AUTHORIZATION, MSGS.recentMessagesGrid_ColAuthorization());
		myGrid.setText(0, COL_USER, MSGS.recentMessagesGrid_ColUser());
		myGrid.setText(0, COL_DOMAIN, MSGS.recentMessagesGrid_ColService());

		for (int row = 1, index = theList.size() - 1; index >= 0; index--, row++) {
			final GRecentMessage next = theList.get(index);
			
			FlowPanel actionPanel = new FlowPanel();
			PButton replayButton = new PButton(AdminPortal.IMAGES.iconPlay16(), "Replay");
			replayButton.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent theEvent) {
					History.newItem(NavProcessor.getTokenReplayMessage(true, next.getPid()));
				}
			});
			actionPanel.add(replayButton);
			myGrid.setWidget(row, COL_ACTION, actionPanel);
			
			
			myGrid.setText(row, COL_TIMESTAMP, DateUtil.formatTimeElapsedForMessage(next.getTransactionTime()));
			myGrid.setText(row, COL_IP, next.getRequestHostIp());
			
			Anchor url = new Anchor();
			url.setText(next.getImplementationUrlId());
			url.setHref(next.getImplementationUrlHref());
			myGrid.setWidget(row, COL_URL, url);
			
			myGrid.setText(row, COL_MILLIS, Long.toString(next.getTransactionMillis()));
			if (theIsUserGrid) {
				myGrid.setWidget(row, COL_VIEW, new Hyperlink(MSGS.recentMessagesGrid_View(), NavProcessor.getTokenViewUserRecentMessage(true, next.getPid())));
			} else {
				myGrid.setWidget(row, COL_VIEW, new Hyperlink(MSGS.recentMessagesGrid_View(), NavProcessor.getTokenViewServiceVersionRecentMessage(true, next.getPid())));
			}

			FlowPanel servicePanel=new FlowPanel();
			if (StringUtil.isNotBlank(next.getDomainName())) {
				servicePanel.add(new Hyperlink(next.getDomainName(), NavProcessor.getTokenEditDomain(true, next.getDomainPid())));
			}
			if (StringUtil.isNotBlank(next.getServiceName())) {
				servicePanel.add(new Hyperlink(next.getServiceName(), NavProcessor.getTokenEditService(true, next.getDomainPid(), next.getServicePid())));
			}
			if (StringUtil.isNotBlank(next.getServiceVersionId())) {
				servicePanel.add(new Hyperlink(next.getServiceVersionId(), NavProcessor.getTokenEditServiceVersion(true, next.getServiceVersionPid())));
			}
			myGrid.setWidget(row, COL_DOMAIN, servicePanel);
			
			if (next.getRequestUsername()!=null) {
				Anchor user = new Anchor();
				user.setText(next.getRequestUsername());
				user.setHref("#"+NavProcessor.getTokenEditUser(true, next.getRequestUserPid()));
				myGrid.setWidget(row, COL_USER, user);				
			}
			
			if (next.getAuthorizationOutcome()!=null) {
				myGrid.setText(row, COL_AUTHORIZATION, next.getAuthorizationOutcome().getDescription());				
			}
			
		}

	}

}
