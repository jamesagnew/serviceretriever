package net.svcret.admin.client.ui.config.monitor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.svcret.admin.client.AdminPortal;
import net.svcret.admin.client.nav.NavProcessor;
import net.svcret.admin.client.ui.components.CssConstants;
import net.svcret.admin.client.ui.components.HtmlList;
import net.svcret.admin.client.ui.components.HtmlList.ListType;
import net.svcret.admin.client.ui.components.LoadingSpinner;
import net.svcret.admin.client.ui.components.PButton;
import net.svcret.admin.shared.IAsyncLoadCallback;
import net.svcret.admin.shared.Model;
import net.svcret.admin.shared.model.GMonitorRule;
import net.svcret.admin.shared.model.GMonitorRuleAppliesTo;
import net.svcret.admin.shared.model.GMonitorRuleList;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;

public class MonitorRulesPanel extends FlowPanel {

	private LoadingSpinner myConfigListLoadingSpinner;
	private Grid myRulesGrid;

	private static final int COL_ACTIONS = 0;
	private static final int COL_ACTIVE = 1;
	private static final int COL_RULE_NAME = 2;
	private static final int COL_TYPE = 3;
	private static final int COL_APPLIES_TO = 4;
	private static final int NUM_COLS = 5;

	public MonitorRulesPanel() {
		initListPanel();

		Model.getInstance().loadMonitorRuleList(new IAsyncLoadCallback<GMonitorRuleList>() {
			@Override
			public void onSuccess(GMonitorRuleList theResult) {
				myConfigListLoadingSpinner.hideCompletely();
				setRuleList(theResult);
			}
		});
	}

	private void setRuleList(GMonitorRuleList theResult) {
		myRulesGrid.resize(theResult.size() + 1, NUM_COLS);

		myRulesGrid.setText(0, COL_ACTIONS, "Actions");
		myRulesGrid.setText(0, COL_TYPE, "Criteria");
		myRulesGrid.setText(0, COL_RULE_NAME, "Rule Name");
		myRulesGrid.setText(0, COL_APPLIES_TO, "Applies To");
		myRulesGrid.setText(0, COL_ACTIVE, "Active");

		int row = 0;
		for (final GMonitorRule next : theResult) {
			row++;

			HorizontalPanel actionsPanel = new HorizontalPanel();
			
			PButton editButton = new PButton(AdminPortal.IMAGES.iconEdit(), AdminPortal.MSGS.actions_Edit());
			editButton.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent theEvent) {
					History.newItem(NavProcessor.getTokenEditMonitorRule(true, next.getPid()));
				}
			});
			actionsPanel.add(editButton);
			
			myRulesGrid.setWidget(row, COL_ACTIONS, actionsPanel);

			List<String> typeDescriptions = toTypeDescriptions(next);
			if (typeDescriptions.size() == 0) {
				myRulesGrid.setText(row, COL_TYPE, "No triggers defined");
			} else if (typeDescriptions.size() == 1) {
				myRulesGrid.setText(row, COL_TYPE, typeDescriptions.get(0));
			} else {
				HtmlList list = new HtmlList(ListType.UNORDERED);
				for (String string : typeDescriptions) {
					list.addItem(string);
				}
				myRulesGrid.setWidget(row, COL_TYPE, list);
			}

			myRulesGrid.setText(row, COL_RULE_NAME, next.getName());

			List<String> appliesTos = toAppliesTo(next);
			if (appliesTos.size() == 0) {
				myRulesGrid.setText(row, COL_APPLIES_TO, "No triggers defined");
			} else if (appliesTos.size() == 1) {
				myRulesGrid.setText(row, COL_APPLIES_TO, appliesTos.get(0));
			} else {
				HtmlList list = new HtmlList(ListType.UNORDERED);
				for (String string : appliesTos) {
					list.addItem(string);
				}
				myRulesGrid.setWidget(row, COL_APPLIES_TO, list);
			}
			myRulesGrid.setText(row, COL_ACTIVE, next.isActive() ? "Active" : "No");
		}

	}

	private List<String> toAppliesTo(GMonitorRule theNext) {
		ArrayList<String> retVal = new ArrayList<String>();
		for (GMonitorRuleAppliesTo nextApplies : theNext.getAppliesTo()) {
			StringBuilder b= new StringBuilder();
			b.append(nextApplies.getDomainName());
			if (nextApplies.getServiceName() != null) {
				b.append(" / ").append(nextApplies.getServiceName());
				if (nextApplies.getVersionId() != null) {
					b.append(" / ").append(nextApplies.getVersionId());
				}else {
					b.append(" - All Versions");
				}
			}else {
				b.append(" - All Services and Versions");
			}
			
			retVal.add(b.toString());
		}
		
		Collections.sort(retVal);
		
		return retVal;
	}

	private List<String> toTypeDescriptions(GMonitorRule theNext) {
		ArrayList<String> retVal = new ArrayList<String>();

		if (theNext.isFireIfSingleBackingUrlIsUnavailable()) {
			retVal.add("Fire if any backing URLs unavailable");
		} else if (!theNext.isFireIfAllBackingUrlsAreUnavailable()) {
			retVal.add("Fire if all backing URLs unavailable");
		}

		if (theNext.getFireForBackingServiceLatencyIsAboveMillis() != null) {
			retVal.add("Fire is backing service latency exceeds " + theNext.getFireForBackingServiceLatencyIsAboveMillis() + "ms");
		}

		return retVal;
	}


	private void initListPanel() {
		FlowPanel listPanel = new FlowPanel();
		listPanel.setStylePrimaryName("mainPanel");
		add(listPanel);

		Label titleLabel = new Label("Monitoring Rules");
		titleLabel.setStyleName("mainPanelTitle");
		listPanel.add(titleLabel);

		FlowPanel contentPanel = new FlowPanel();
		contentPanel.addStyleName("contentInnerPanel");
		listPanel.add(contentPanel);

		contentPanel.add(new HTML("The following table contains any rules which " + "have been defined for monitoring availability and performance of " + "service implementations."));

		myConfigListLoadingSpinner = new LoadingSpinner();
		myConfigListLoadingSpinner.show();
		contentPanel.add(myConfigListLoadingSpinner);

		myRulesGrid = new Grid();
		myRulesGrid.addStyleName(CssConstants.PROPERTY_TABLE);
		contentPanel.add(myRulesGrid);

		PButton addButton = new PButton(AdminPortal.IMAGES.iconAdd(), AdminPortal.MSGS.actions_Add());
		addButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent theEvent) {
				History.newItem(NavProcessor.getTokenAddMonitorRule(true));
			}
		});
		contentPanel.add(addButton);

		
		if (true) {
			return;
		}

		HorizontalPanel hPanel = new HorizontalPanel();
		// contentPanel.add(hPanel);
		//
		// VerticalPanel toolbar = new VerticalPanel();
		//
		// HorizontalPanel addPanel = new HorizontalPanel();
		// toolbar.add(addPanel);
		// myAddButton = new PButton(AdminPortal.IMAGES.iconAdd(),
		// AdminPortal.MSGS.actions_AddNewDotDotDot());
		// myAddButton.setEnabled(false);
		// myAddButton.addClickHandler(new ClickHandler() {
		// @Override
		// public void onClick(ClickEvent theEvent) {
		// addHost();
		// }
		// });
		// addPanel.add(myAddButton);
		// addPanel.add(new Label("Type:"));
		// myAddListBox = new ListBox();
		// for (AuthorizationHostTypeEnum next :
		// AuthorizationHostTypeEnum.values()) {
		// myAddListBox.addItem(next.description(), next.name());
		// }
		// myAddListBox.setSelectedIndex(0);
		// addPanel.add(myAddListBox);
		//
		// myRemoveButton = new PButton(AdminPortal.IMAGES.iconRemove(),
		// AdminPortal.MSGS.actions_RemoveSelectedDotDotDot());
		// myRemoveButton.setEnabled(false);
		// myRemoveButton.addClickHandler(new ClickHandler() {
		// @Override
		// public void onClick(ClickEvent theEvent) {
		// removeHost();
		// }
		// });
		// toolbar.add(myRemoveButton);
		//
		// myHostsListBox = new ListBox(false);
		// myHostsListBox.setVisibleItemCount(5);
		// myHostsListBox.addChangeHandler(new ChangeHandler() {
		// @Override
		// public void onChange(ChangeEvent theEvent) {
		// if (myUpdatingConfigsListBox) {
		// return;
		// }
		// mySelectedPid =
		// Long.parseLong(myHostsListBox.getValue(myHostsListBox.getSelectedIndex()));
		// updateSelectedHost();
		// }
		//
		// });
		//
		// hPanel.add(myHostsListBox);
		// hPanel.add(toolbar);
		//
		// HorizontalPanel buttonsBar = new HorizontalPanel();
		// contentPanel.add(buttonsBar);

	}

}
