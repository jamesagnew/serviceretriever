package net.svcret.admin.client.ui.stats;

import static net.svcret.admin.client.AdminPortal.*;

import java.util.List;

import net.svcret.admin.client.nav.NavProcessor;
import net.svcret.admin.client.ui.components.CssConstants;
import net.svcret.admin.shared.model.GRecentMessage;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Hyperlink;

public class RecentMessagesGrid extends FlowPanel {

	private static final int COL_USER = 6;
	private static final int COL_AUTHORIZATION = 5;
	private static final int COL_MILLIS = 4;
	private static final int COL_VIEW = 3;
	private static final int COL_URL = 2;
	private static final int COL_IP = 1;
	private static final int COL_TIMESTAMP = 0;

	private Grid myGrid;
	private DateTimeFormat myDateFormat = DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_LONG);

	public RecentMessagesGrid(List<GRecentMessage> theList, boolean theIsUserGrid) {
		myGrid = new Grid(theList.size() + 1, 7);
		myGrid.addStyleName(CssConstants.PROPERTY_TABLE);
		add(myGrid);

		myGrid.setText(0, COL_TIMESTAMP, MSGS.recentMessagesGrid_ColTimestamp());
		myGrid.setText(0, COL_IP, MSGS.recentMessagesGrid_ColIp());
		myGrid.setText(0, COL_URL, MSGS.recentMessagesGrid_ColImplementationUrl());
		myGrid.setText(0, COL_VIEW, MSGS.recentMessagesGrid_ColView());
		myGrid.setText(0, COL_MILLIS, MSGS.recentMessagesGrid_ColMillis());
		myGrid.setText(0, COL_AUTHORIZATION, MSGS.recentMessagesGrid_ColAuthorization());
		myGrid.setText(0, COL_USER, MSGS.recentMessagesGrid_ColUser());

		for (int row = 1, index = theList.size() - 1; index >= 0; index--, row++) {
			GRecentMessage next = theList.get(index);
			myGrid.setText(row, COL_TIMESTAMP, myDateFormat.format(next.getTransactionTime()));
			myGrid.setText(row, COL_IP, next.getRequestHostIp());
			myGrid.setText(row, COL_URL, next.getImplementationUrl());
			myGrid.setText(row, COL_MILLIS, Long.toString(next.getTransactionMillis()));
			if (theIsUserGrid) {
				myGrid.setWidget(row, COL_VIEW, new Hyperlink(MSGS.recentMessagesGrid_View(), NavProcessor.getTokenViewUserRecentMessage(true, next.getPid())));
			} else {
				myGrid.setWidget(row, COL_VIEW, new Hyperlink(MSGS.recentMessagesGrid_View(), NavProcessor.getTokenViewServiceVersionRecentMessage(true, next.getPid())));
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
