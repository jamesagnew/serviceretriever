package net.svcret.admin.client.ui.stats;

import static net.svcret.admin.client.AdminPortal.*;

import java.util.List;

import net.svcret.admin.client.AdminPortal;
import net.svcret.admin.client.nav.NavProcessor;
import net.svcret.admin.client.ui.components.CssConstants;
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
	private static final int COL_MILLIS = 6;
	private static final int COL_AUTHORIZATION = 7;

	private Grid myGrid;

	public RecentMessagesGrid(List<GRecentMessage> theList) {
		myGrid = new Grid(theList.size() + 1, 8);
		myGrid.addStyleName(CssConstants.PROPERTY_TABLE);
		add(myGrid);

		myGrid.setText(0, COL_TIMESTAMP, MSGS.recentMessagesGrid_ColTimestamp());
		myGrid.setText(0, COL_IP, MSGS.recentMessagesGrid_ColIp());
		myGrid.setText(0, COL_URL, MSGS.recentMessagesGrid_ColImplementationUrl());
		myGrid.setText(0, COL_MILLIS, MSGS.recentMessagesGrid_ColMillis());
		myGrid.setText(0, COL_AUTHORIZATION, MSGS.recentMessagesGrid_ColAuthorization());
		myGrid.setText(0, COL_USER, MSGS.recentMessagesGrid_ColUser());
		myGrid.setText(0, COL_DOMAIN, MSGS.name_Service());

		for (int row = 1, index = theList.size() - 1; index >= 0; index--, row++) {
			final GRecentMessage next = theList.get(index);
			
			FlowPanel actionPanel = new FlowPanel();
			myGrid.setWidget(row, COL_ACTION, actionPanel);

			// View Button
			// TODO: better icon (view magnifying glass?)
			PButton viewButton = new PButton(AdminPortal.IMAGES.iconEdit(), AdminPortal.MSGS.actions_View());
			actionPanel.add(viewButton);
			viewButton.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent theEvent) {
					switch (next.getRecentMessageType()) {
					case USER:
						History.newItem(NavProcessor.getTokenViewUserRecentMessage(true, next.getPid()));
						break;
					case SVCVER:
						History.newItem(NavProcessor.getTokenViewServiceVersionRecentMessage(true, next.getPid()));
						break;
					}
				}
			});
			
			// Replay Button
			PButton replayButton = new PButton(AdminPortal.IMAGES.iconPlay16(), "Replay");
			replayButton.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent theEvent) {
					History.newItem(NavProcessor.getTokenReplayMessage(true, next.getPid()));
				}
			});
			actionPanel.add(replayButton);
			
			// Save Button
			PButton saveButton = new PButton(AdminPortal.IMAGES.iconSave(), "Save");
			actionPanel.add(saveButton);
			saveButton.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent theEvent) {
					History.newItem(NavProcessor.getTokenSaveRecentMessageToLibrary(true, next.getRecentMessageType(), next.getPid()));
				}
			});

			replayButton.addStyleName(CssConstants.RECENT_TRANSACTIONS_ACTION_BUTTON);
			saveButton.addStyleName(CssConstants.RECENT_TRANSACTIONS_ACTION_BUTTON);
			viewButton.addStyleName(CssConstants.RECENT_TRANSACTIONS_ACTION_BUTTON);
			
			myGrid.setText(row, COL_TIMESTAMP, DateUtil.formatTimeElapsedForMessage(next.getTransactionTime()));
			myGrid.setText(row, COL_IP, next.getRequestHostIp());
			
			Anchor url = new Anchor();
			url.setText(next.getImplementationUrlId());
			url.setHref(next.getImplementationUrlHref());
			myGrid.setWidget(row, COL_URL, url);
			
			myGrid.setText(row, COL_MILLIS, Long.toString(next.getTransactionMillis()));

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
