package net.svcret.admin.client.ui.stats;

import static net.svcret.admin.client.AdminPortal.MSGS;

import java.util.List;

import net.svcret.admin.client.nav.NavProcessor;
import net.svcret.admin.client.ui.components.CssConstants;
import net.svcret.admin.shared.model.GRecentMessage;
import net.svcret.admin.shared.util.StringUtil;

import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Hyperlink;

public class RecentMessagesGrid extends FlowPanel {

	private static final int COL_TIMESTAMP = 0;
	private static final int COL_IP = 1;
	private static final int COL_USER = 2;
	private static final int COL_DOMAIN = 3;
	private static final int COL_SERVICE = 4;
	private static final int COL_SVCVERSION = 5;
	private static final int COL_URL = 6;
	private static final int COL_VIEW = 7;
	private static final int COL_MILLIS = 8;
	private static final int COL_AUTHORIZATION = 9;

	private Grid myGrid;

	public RecentMessagesGrid(List<GRecentMessage> theList, boolean theIsUserGrid) {
		myGrid = new Grid(theList.size() + 1, 10);
		myGrid.addStyleName(CssConstants.PROPERTY_TABLE);
		add(myGrid);

		myGrid.setText(0, COL_TIMESTAMP, MSGS.recentMessagesGrid_ColTimestamp());
		myGrid.setText(0, COL_IP, MSGS.recentMessagesGrid_ColIp());
		myGrid.setText(0, COL_URL, MSGS.recentMessagesGrid_ColImplementationUrl());
		myGrid.setText(0, COL_VIEW, MSGS.recentMessagesGrid_ColView());
		myGrid.setText(0, COL_MILLIS, MSGS.recentMessagesGrid_ColMillis());
		myGrid.setText(0, COL_AUTHORIZATION, MSGS.recentMessagesGrid_ColAuthorization());
		myGrid.setText(0, COL_USER, MSGS.recentMessagesGrid_ColUser());
		myGrid.setText(0, COL_DOMAIN, MSGS.recentMessagesGrid_ColDomain());
		myGrid.setText(0, COL_SERVICE, MSGS.recentMessagesGrid_ColService());
		myGrid.setText(0, COL_SVCVERSION, MSGS.recentMessagesGrid_ColSvcVersion());

		for (int row = 1, index = theList.size() - 1; index >= 0; index--, row++) {
			GRecentMessage next = theList.get(index);
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
			
			if (StringUtil.isNotBlank(next.getDomainName())) {
				myGrid.setWidget(row, COL_DOMAIN, new Hyperlink(next.getDomainName(), NavProcessor.getTokenEditDomain(true, next.getDomainPid())));
			}

			if (StringUtil.isNotBlank(next.getServiceName())) {
				myGrid.setWidget(row, COL_SERVICE, new Hyperlink(next.getServiceName(), NavProcessor.getTokenEditService(true, next.getDomainPid(), next.getServicePid())));
			}

			if (StringUtil.isNotBlank(next.getServiceVersionId())) {
				myGrid.setWidget(row, COL_SVCVERSION, new Hyperlink(next.getServiceVersionId(), NavProcessor.getTokenEditServiceVersion(true, next.getServiceVersionPid())));
			}
			
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
