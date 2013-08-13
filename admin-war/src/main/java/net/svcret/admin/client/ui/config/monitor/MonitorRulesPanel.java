package net.svcret.admin.client.ui.config.monitor;

import static net.svcret.admin.client.AdminPortal.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.svcret.admin.client.AdminPortal;
import net.svcret.admin.client.nav.NavProcessor;
import net.svcret.admin.client.ui.components.CssConstants;
import net.svcret.admin.client.ui.components.LoadingSpinner;
import net.svcret.admin.client.ui.components.PButton;
import net.svcret.admin.client.ui.components.PButtonCell;
import net.svcret.admin.client.ui.components.PCellTable;
import net.svcret.admin.client.ui.config.svcver.NullColumn;
import net.svcret.admin.shared.IAsyncLoadCallback;
import net.svcret.admin.shared.Model;
import net.svcret.admin.shared.enm.MonitorRuleTypeEnum;
import net.svcret.admin.shared.model.BaseGMonitorRule;
import net.svcret.admin.shared.model.BaseGServiceVersion;
import net.svcret.admin.shared.model.DtoMonitorRuleActive;
import net.svcret.admin.shared.model.DtoMonitorRuleActiveCheck;
import net.svcret.admin.shared.model.GDomainList;
import net.svcret.admin.shared.model.GMonitorRuleAppliesTo;
import net.svcret.admin.shared.model.GMonitorRuleList;
import net.svcret.admin.shared.model.GMonitorRulePassive;
import net.svcret.admin.shared.model.GService;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.view.client.ListDataProvider;

public class MonitorRulesPanel extends FlowPanel {

	private LoadingSpinner myConfigListLoadingSpinner;
	private PCellTable<BaseGMonitorRule> myGrid;
	private ListDataProvider<BaseGMonitorRule> myDataProvider;
	private ListBox myAddTypeBox;
	private GDomainList myDomainList;

	public MonitorRulesPanel() {
		initListPanel();

		Model.getInstance().loadMonitorRuleList(new IAsyncLoadCallback<GMonitorRuleList>() {
			@Override
			public void onSuccess(final GMonitorRuleList theResult) {
				Model.getInstance().loadDomainList(new IAsyncLoadCallback<GDomainList>() {
					@Override
					public void onSuccess(GDomainList theDomainList) {
						myDomainList = theDomainList;
						myConfigListLoadingSpinner.hideCompletely();
						setRuleList(theResult);
					}
				});
			}
		});
	}

	private void setRuleList(GMonitorRuleList theResult) {
		myDataProvider.getList().clear();
		myDataProvider.getList().addAll(theResult.toCollection());
		myDataProvider.refresh();
	}

	private List<String> toAppliesTo(BaseGMonitorRule theNext) {
		ArrayList<String> retVal = new ArrayList<String>();
		switch (theNext.getRuleType()) {
		case PASSIVE: {
			for (GMonitorRuleAppliesTo nextApplies : ((GMonitorRulePassive) theNext).getAppliesTo()) {
				StringBuilder b = new StringBuilder();
				b.append(nextApplies.getDomainName());
				if (nextApplies.getServiceName() != null) {
					b.append(" / ").append(nextApplies.getServiceName());
					if (nextApplies.getVersionId() != null) {
						b.append(" / ").append(nextApplies.getVersionId());
					} else {
						b.append(" - All Versions");
					}
				} else {
					b.append(" - All Services and Versions");
				}

				retVal.add(b.toString());
			}
			break;
		}
		case ACTIVE: {
			Set<Long> svcVerPids = new HashSet<Long>();
			for (DtoMonitorRuleActiveCheck next : ((DtoMonitorRuleActive) theNext).getCheckList()) {
				svcVerPids.add(next.getServiceVersionPid());
			}

			for (Long next : svcVerPids) {
				GService svc = myDomainList.getServiceWithServiceVersion(next);
				BaseGServiceVersion svcVer = svc.getVersionList().getVersionByPid(next);
				if (svc.allVersionPidsInThisServiceAreAmongThesePids(svcVerPids)) {
					retVal.add(svc.getName() + " - All Versions");
				} else {
					retVal.add(svc.getName() + " / " + svcVer.getId());
				}
			}

			break;
		}
		}

		Collections.sort(retVal);

		return retVal;
	}

	private List<SafeHtml> toTypeDescriptions(BaseGMonitorRule theNext) {
		ArrayList<SafeHtml> retVal = new ArrayList<SafeHtml>();

		switch (theNext.getRuleType()) {
		case ACTIVE:
			for (DtoMonitorRuleActiveCheck next : ((DtoMonitorRuleActive) theNext).getCheckList()) {
				SafeHtmlBuilder b = new SafeHtmlBuilder();
				b.appendHtmlConstant("Send message every " + next.getCheckFrequencyNum() + " " + next.getCheckFrequencyUnit().getFriendlyName(next.getCheckFrequencyNum()).toLowerCase() + ": \"");
				b.appendEscaped(next.getMessageDescription());
				b.appendHtmlConstant("\"");
				b.appendHtmlConstant("<ul>");
				b.appendHtmlConstant("<li>Expects response type: " + next.getExpectResponseType().getFriendlyName() + "</li>");
				if (next.getExpectLatencyUnderMillis() != null) {
					b.appendHtmlConstant("<li>Expects latency under " + next.getExpectLatencyUnderMillis() + "ms/call</li>");
				}
				if (next.getExpectResponseContainsText() != null) {
					b.appendHtmlConstant("<li>Expects response to contain text: \"");
					b.appendEscaped(next.getExpectResponseContainsText());
					b.appendHtmlConstant("\"</li>");
				}
				b.appendHtmlConstant("</ul>");
				retVal.add(b.toSafeHtml());
			}
			break;
		case PASSIVE: {
			GMonitorRulePassive next = (GMonitorRulePassive) theNext;
			if (next.isPassiveFireIfSingleBackingUrlIsUnavailable()) {
				retVal.add(SafeHtmlUtils.fromSafeConstant("Fire if any backing URLs unavailable"));
			} else if (!next.isPassiveFireIfAllBackingUrlsAreUnavailable()) {
				retVal.add(SafeHtmlUtils.fromSafeConstant("Fire if all backing URLs unavailable"));
			}

			if (next.getPassiveFireForBackingServiceLatencyIsAboveMillis() != null) {
				retVal.add(SafeHtmlUtils.fromSafeConstant("Fire is backing service latency exceeds " + next.getPassiveFireForBackingServiceLatencyIsAboveMillis() + "ms"));
			}
			break;
		}
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

		// myRulesGrid = new Grid();
		// myRulesGrid.addStyleName(CssConstants.PROPERTY_TABLE);
		// contentPanel.add(myRulesGrid);

		myGrid = new PCellTable<BaseGMonitorRule>();
		myGrid.setWidth("100%");
		contentPanel.add(myGrid);

		myGrid.setEmptyTableWidget(new Label("No rules defined"));

		// Edit
		Column<BaseGMonitorRule, String> editColumn = new NullColumn<BaseGMonitorRule>(new PButtonCell(IMAGES.iconEdit(), MSGS.actions_Edit()));
		myGrid.addColumn(editColumn, "");
		editColumn.setFieldUpdater(new FieldUpdater<BaseGMonitorRule, String>() {
			@Override
			public void update(int theIndex, BaseGMonitorRule theObject, String theValue) {
				History.newItem(NavProcessor.getTokenEditMonitorRule(true, theObject.getPid()));
			}
		});
		editColumn.setCellStyleNames(CssConstants.PCELLTABLE_ACTION_COLUMN);

		// Active
		Column<BaseGMonitorRule, SafeHtml> enabledColumn = new Column<BaseGMonitorRule, SafeHtml>(new SafeHtmlCell()) {
			@Override
			public SafeHtml getValue(BaseGMonitorRule theObject) {
				if (theObject.isActive()) {
					return SafeHtmlUtils.fromSafeConstant("Yes");
				} else {
					return SafeHtmlUtils.fromSafeConstant("No");
				}
			}
		};
		myGrid.addColumn(enabledColumn, "Enabled");

		// Active
		Column<BaseGMonitorRule, SafeHtml> typeColumn = new Column<BaseGMonitorRule, SafeHtml>(new SafeHtmlCell()) {
			@Override
			public SafeHtml getValue(BaseGMonitorRule theObject) {
				return SafeHtmlUtils.fromTrustedString(theObject.getRuleType().getFriendlyName());
			}
		};
		myGrid.addColumn(typeColumn, "Type");

		// Criteria
		Column<BaseGMonitorRule, SafeHtml> criteriaColumn = new Column<BaseGMonitorRule, SafeHtml>(new SafeHtmlCell()) {
			@Override
			public SafeHtml getValue(BaseGMonitorRule theObject) {
				List<SafeHtml> typeDescriptions = toTypeDescriptions(theObject);
				if (typeDescriptions.size() == 0) {
					return SafeHtmlUtils.fromSafeConstant("No triggers defined");
				} else if (typeDescriptions.size() == 1) {
					return typeDescriptions.get(0);
				} else {
					SafeHtmlBuilder b = new SafeHtmlBuilder();
					b.appendHtmlConstant("<ul>");
					for (SafeHtml string : typeDescriptions) {
						b.appendHtmlConstant("<li>");
						b.append(string);
						b.appendHtmlConstant("</li>");
					}
					b.appendHtmlConstant("</ul>");
					return b.toSafeHtml();
				}
			}
		};
		myGrid.addColumn(criteriaColumn, "Criteria");

		// Rule Name
		Column<BaseGMonitorRule, SafeHtml> ruleNameColumn = new Column<BaseGMonitorRule, SafeHtml>(new SafeHtmlCell()) {
			@Override
			public SafeHtml getValue(BaseGMonitorRule theObject) {
				return SafeHtmlUtils.fromString(theObject.getName());
			}
		};
		myGrid.addColumn(ruleNameColumn, "Name");

		// Applies To
		Column<BaseGMonitorRule, SafeHtml> appliesToColumn = new Column<BaseGMonitorRule, SafeHtml>(new SafeHtmlCell()) {
			@Override
			public SafeHtml getValue(BaseGMonitorRule theObject) {
				List<String> appliesTos = toAppliesTo(theObject);
				if (appliesTos.size() == 0) {
					return SafeHtmlUtils.fromSafeConstant("No triggers defined");
				} else if (appliesTos.size() == 1) {
					return SafeHtmlUtils.fromString(appliesTos.get(0));
				} else {
					SafeHtmlBuilder b = new SafeHtmlBuilder();
					b.appendHtmlConstant("<ul>");
					for (String string : appliesTos) {
						b.appendHtmlConstant("<li>");
						b.appendEscaped(string);
						b.appendHtmlConstant("</li>");
					}
					b.appendHtmlConstant("</ul>");
					return b.toSafeHtml();
				}
			}
		};
		myGrid.addColumn(appliesToColumn, "Applies To");

		myDataProvider = new ListDataProvider<BaseGMonitorRule>();
		myDataProvider.addDataDisplay(myGrid);

		HorizontalPanel controlsPanel = new HorizontalPanel();
		contentPanel.add(controlsPanel);

		PButton addButton = new PButton(AdminPortal.IMAGES.iconAdd(), AdminPortal.MSGS.actions_Add());
		addButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent theEvent) {
				History.newItem(NavProcessor.getTokenAddMonitorRule(true, myAddTypeBox.getSelectedIndex()));
			}
		});
		controlsPanel.add(addButton);

		myAddTypeBox = new ListBox(false);
		for (MonitorRuleTypeEnum next : MonitorRuleTypeEnum.values()) {
			myAddTypeBox.addItem(next.getFriendlyName());
		}
		myAddTypeBox.setSelectedIndex(0);

	}

}
