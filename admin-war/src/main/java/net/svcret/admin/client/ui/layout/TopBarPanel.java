package net.svcret.admin.client.ui.layout;

import java.util.Date;
import java.util.List;

import net.svcret.admin.client.AdminPortal;
import net.svcret.admin.client.MyResources;
import net.svcret.admin.shared.DateUtil;
import net.svcret.admin.shared.model.DtoNodeStatus;

import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;

public class TopBarPanel extends DockLayoutPanel {

	private static TopBarPanel ourInstance;
	private FlowPanel myNodesContentPanel;

	private TopBarPanel() {
		super(Unit.PX);
		setStylePrimaryName("outerLayoutTopBar");

		Image titleBanner = new Image("images/banner.png");
		titleBanner.setStyleName("");
		titleBanner.getElement().getStyle().setPosition(Position.ABSOLUTE);
		titleBanner.getElement().getStyle().setLeft(0, Unit.PX);
		titleBanner.getElement().getStyle().setTop(0, Unit.PX);
		RootPanel.getBodyElement().appendChild(titleBanner.getElement());

		FlowPanel nodesPanel = new FlowPanel();
		addEast(nodesPanel, 200);
		myNodesContentPanel = new FlowPanel();
		myNodesContentPanel.addStyleName(MyResources.CSS.topPanelNodeStatusLabelContents());
		nodesPanel.add(myNodesContentPanel);

	}

	public static TopBarPanel getInstance() {
		if (ourInstance == null) {
			ourInstance = new TopBarPanel();
		}
		return ourInstance;
	}

	public void setNodeStatuses(List<DtoNodeStatus> theNodeStatuses) {
		myNodesContentPanel.clear();
		for (DtoNodeStatus nextStatus : theNodeStatuses) {

			String text = null;
			switch (nextStatus.getStatus()) {
			case ACTIVE:
				text = AdminPortal.MSGS.topPanel_NodeActive(nextStatus.getNodeId(), nextStatus.getTransactionsSuccessfulPerMinute());
				break;
			case NO_REQUESTS:
				if (nextStatus.getTimeElapsedSinceLastTx() != null) {
					text = AdminPortal.MSGS.topPanel_NoRequests(nextStatus.getNodeId(), DateUtil.formatTimeElapsedForLastInvocation(new Date(System.currentTimeMillis() - nextStatus.getTimeElapsedSinceLastTx())));
				} else {
					text = AdminPortal.MSGS.topPanel_NoRequestsYes(nextStatus.getNodeId());
				}
				break;
			case RECENTLY_STARTED:
				text = AdminPortal.MSGS.topPanel_RecentlyRestarted(nextStatus.getNodeId());
				break;
			case DOWN:
				text = AdminPortal.MSGS.topPanel_NodeDown(nextStatus.getNodeId(), DateUtil.formatTimeElapsedForLastInvocation(new Date(System.currentTimeMillis() - nextStatus.getTimeElapsedSinceDown())));
				break;
			}

			Label nodeStatusLabel = new Label(text);
			nodeStatusLabel.addStyleName(MyResources.CSS.topPanelNodeStatusLabel());
			myNodesContentPanel.add(nodeStatusLabel);

		}
	}

}
