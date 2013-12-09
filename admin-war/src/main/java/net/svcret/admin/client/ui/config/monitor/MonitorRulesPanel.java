package net.svcret.admin.client.ui.config.monitor;

import static net.svcret.admin.client.AdminPortal.IMAGES;
import static net.svcret.admin.client.AdminPortal.MSGS;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.svcret.admin.client.AdminPortal;
import net.svcret.admin.client.nav.NavProcessor;
import net.svcret.admin.client.ui.components.CssConstants;
import net.svcret.admin.client.ui.components.LoadingSpinner;
import net.svcret.admin.client.ui.components.PButton;
import net.svcret.admin.client.ui.components.PButtonCell;
import net.svcret.admin.client.ui.components.PCellTable;
import net.svcret.admin.client.ui.config.svcver.NullColumn;
import net.svcret.admin.shared.DateUtil;
import net.svcret.admin.shared.IAsyncLoadCallback;
import net.svcret.admin.shared.Model;
import net.svcret.admin.shared.model.BaseDtoMonitorRule;
import net.svcret.admin.shared.model.BaseDtoServiceVersion;
import net.svcret.admin.shared.model.DtoMonitorRuleActive;
import net.svcret.admin.shared.model.DtoMonitorRuleActiveCheck;
import net.svcret.admin.shared.model.DtoMonitorRuleActiveCheckList;
import net.svcret.admin.shared.model.DtoDomain;
import net.svcret.admin.shared.model.DtoDomainList;
import net.svcret.admin.shared.model.GMonitorRuleAppliesTo;
import net.svcret.admin.shared.model.GMonitorRuleFiring;
import net.svcret.admin.shared.model.GMonitorRuleList;
import net.svcret.admin.shared.model.GMonitorRulePassive;
import net.svcret.admin.shared.model.GService;
import net.svcret.admin.shared.util.StringUtil;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.view.client.ListDataProvider;

public class MonitorRulesPanel extends FlowPanel {

	private LoadingSpinner myConfigListLoadingSpinner;
	private PCellTable<BaseDtoMonitorRule> myGrid;
	private ListDataProvider<BaseDtoMonitorRule> myDataProvider;
	private DtoDomainList myDomainList;
	private Map<Long, GMonitorRuleFiring> myRulePidToLatestMonitorRuleFiring;

	public MonitorRulesPanel() {
		initListPanel();

		AdminPortal.MODEL_SVC.loadMonitorRuleList(new AsyncCallback<GMonitorRuleList>() {
			@Override
			public void onSuccess(final GMonitorRuleList theResult) {
				Model.getInstance().loadDomainList(new IAsyncLoadCallback<DtoDomainList>() {
					@Override
					public void onSuccess(final DtoDomainList theDomainList) {
						AdminPortal.MODEL_SVC.getLatestFailingMonitorRuleFiringForRulePids(new AsyncCallback<Map<Long, GMonitorRuleFiring>>() {
							@Override
							public void onSuccess(Map<Long, GMonitorRuleFiring> theRulePidToLatestMonitorRuleFiring) {
								myDomainList = theDomainList;
								myConfigListLoadingSpinner.hideCompletely();
								setRuleList(theResult, theRulePidToLatestMonitorRuleFiring);
							}

							@Override
							public void onFailure(Throwable theCaught) {
								Model.handleFailure(theCaught);
							}
						});
					}
				});
			}

			@Override
			public void onFailure(Throwable theCaught) {
				Model.handleFailure(theCaught);
			}
		});
	}

	private void setRuleList(GMonitorRuleList theResult, Map<Long, GMonitorRuleFiring> theRulePidToLatestMonitorRuleFiring) {
		myRulePidToLatestMonitorRuleFiring = theRulePidToLatestMonitorRuleFiring;

		myAppliesTo.clear();

		myDataProvider.getList().clear();
		myDataProvider.getList().addAll(theResult.toCollection());
		myDataProvider.refresh();
	}

	private Map<Long, List<String>> myAppliesTo = new HashMap<Long, List<String>>();

	private List<String> toAppliesTo(BaseDtoMonitorRule theNext) {
		Long pid = theNext.getPidOrNull();
		if (pid != null && myAppliesTo.containsKey(pid)) {
			return myAppliesTo.get(pid);
		}

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
				DtoDomain domain = myDomainList.getDomainWithServiceVersion(next);
				GService svc = myDomainList.getServiceWithServiceVersion(next);
				BaseDtoServiceVersion svcVer = svc.getVersionList().getVersionByPid(next);
				retVal.add(domain.getName() + " / " + svc.getName() + " / " + svcVer.getId());
			}

			break;
		}
		}

		Collections.sort(retVal);

		if (pid != null) {
			myAppliesTo.put(pid, retVal);
		}

		return retVal;
	}

	private SafeHtml toTypeDescriptions(BaseDtoMonitorRule theNext) {
		SafeHtml retVal = null;

		switch (theNext.getRuleType()) {
		case ACTIVE: {

			boolean checkLatency = false;
			boolean checkResponseBody = false;
			DtoMonitorRuleActiveCheckList checkList = ((DtoMonitorRuleActive) theNext).getCheckList();

			for (DtoMonitorRuleActiveCheck next : checkList) {
				if (next.getExpectLatencyUnderMillis() != null) {
					checkLatency = true;
				}
				if (next.getExpectResponseContainsText() != null) {
					checkResponseBody = true;
				}
			}

			SafeHtmlBuilder b = new SafeHtmlBuilder();
			b.appendHtmlConstant("Active rule: Send " + checkList.size() + " message");
			if (checkList.size() != 1) {
				b.appendHtmlConstant("s");
			}
			if (checkLatency || checkResponseBody) {
				b.appendHtmlConstant(" and check ");
				if (checkLatency) {
					b.appendHtmlConstant("latency");
					if (checkResponseBody) {
						b.appendHtmlConstant(" and ");
					}
				}
				if (checkResponseBody) {
					b.appendHtmlConstant("response body");
				}
			}

			retVal = b.toSafeHtml();
			break;
		}
		case PASSIVE: {
			GMonitorRulePassive next = (GMonitorRulePassive) theNext;
			SafeHtmlBuilder b = new SafeHtmlBuilder();
			b.appendHtmlConstant("Passive rule: For any incoming requests, ");

			boolean haveSomething = false;
			if (next.isPassiveFireIfSingleBackingUrlIsUnavailable()) {
				b.appendHtmlConstant("fire if any backing URLs unavailable");
				haveSomething = true;
			} else if (!next.isPassiveFireIfAllBackingUrlsAreUnavailable()) {
				b.appendHtmlConstant("fire if all backing URLs unavailable");
				haveSomething = true;
			}

			if (next.getPassiveFireForBackingServiceLatencyIsAboveMillis() != null) {
				if (haveSomething) {
					b.appendHtmlConstant(" and ");
				}
				b.appendHtmlConstant("Fire if backing service latency exceeds " + next.getPassiveFireForBackingServiceLatencyIsAboveMillis() + "ms");
			}

			retVal = b.toSafeHtml();
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

		myGrid = new PCellTable<BaseDtoMonitorRule>();
		myGrid.setWidth("100%");
		contentPanel.add(myGrid);

		myGrid.setEmptyTableWidget(new Label("No rules defined"));

		myDataProvider = new ListDataProvider<BaseDtoMonitorRule>();
		myDataProvider.addDataDisplay(myGrid);

		ListHandler<BaseDtoMonitorRule> sortHandler = new ListHandler<BaseDtoMonitorRule>(myDataProvider.getList());
		myGrid.addColumnSortHandler(sortHandler);

		// Edit
		Column<BaseDtoMonitorRule, String> editColumn = new NullColumn<BaseDtoMonitorRule>(new PButtonCell(IMAGES.iconEdit(), MSGS.actions_Edit()));
		myGrid.addColumn(editColumn, "");
		editColumn.setFieldUpdater(new FieldUpdater<BaseDtoMonitorRule, String>() {
			@Override
			public void update(int theIndex, BaseDtoMonitorRule theObject, String theValue) {
				History.newItem(NavProcessor.getTokenEditMonitorRule(theObject.getPid()));
			}
		});
		editColumn.setCellStyleNames(CssConstants.PCELLTABLE_ACTION_COLUMN);

		// Active
		Column<BaseDtoMonitorRule, SafeHtml> enabledColumn = new Column<BaseDtoMonitorRule, SafeHtml>(new SafeHtmlCell()) {
			@Override
			public SafeHtml getValue(BaseDtoMonitorRule theObject) {
				if (theObject.isActive()) {
					return SafeHtmlUtils.fromSafeConstant("Yes");
				} else {
					return SafeHtmlUtils.fromSafeConstant("No");
				}
			}
		};
		myGrid.addColumn(enabledColumn, "Enabled");
		myGrid.getColumn(myGrid.getColumnCount() - 1).setSortable(true);
		sortHandler.setComparator(myGrid.getColumn(myGrid.getColumnCount() - 1), new Comparator<BaseDtoMonitorRule>() {
			@Override
			public int compare(BaseDtoMonitorRule theO1, BaseDtoMonitorRule theO2) {
				if ((theO1.isActive()) == (theO2.isActive())) {
					return 0;
				}
				if (theO1.isActive()) {
					return 1;
				} else {
					return -1;
				}
			}
		});

		// Applies To
		Column<BaseDtoMonitorRule, SafeHtml> appliesToColumn = new Column<BaseDtoMonitorRule, SafeHtml>(new SafeHtmlCell()) {
			@Override
			public SafeHtml getValue(BaseDtoMonitorRule theObject) {
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
		appliesToColumn.setSortable(true);
		sortHandler.setComparator(appliesToColumn, new Comparator<BaseDtoMonitorRule>() {
			@Override
			public int compare(BaseDtoMonitorRule theO1, BaseDtoMonitorRule theO2) {
				List<String> app1 = toAppliesTo(theO1);
				List<String> app2 = toAppliesTo(theO2);
				int retVal = 0;
				if (app1.isEmpty() && app2.isEmpty()) {
					retVal = 0;
				} else if (app1.isEmpty()) {
					retVal = 1;
				} else if (app2.isEmpty()) {
					retVal = -1;
				} else {
					retVal = StringUtil.compare(app1.get(0), app2.get(0));
				}

				if (retVal == 0) {
					retVal = StringUtil.compare(theO1.getName(), theO2.getName());
				}

				return retVal;
			}
		});

		// Rule Name
		Column<BaseDtoMonitorRule, SafeHtml> ruleNameColumn = new Column<BaseDtoMonitorRule, SafeHtml>(new SafeHtmlCell()) {
			@Override
			public SafeHtml getValue(BaseDtoMonitorRule theObject) {
				return SafeHtmlUtils.fromString(theObject.getName());
			}
		};
		myGrid.addColumn(ruleNameColumn, "Name");
		myGrid.getColumn(myGrid.getColumnCount() - 1).setSortable(true);
		sortHandler.setComparator(myGrid.getColumn(myGrid.getColumnCount() - 1), new Comparator<BaseDtoMonitorRule>() {
			@Override
			public int compare(BaseDtoMonitorRule theO1, BaseDtoMonitorRule theO2) {
				return StringUtil.compare(theO1.getName(), theO2.getName());
			}
		});

		// Criteria
		Column<BaseDtoMonitorRule, SafeHtml> criteriaColumn = new Column<BaseDtoMonitorRule, SafeHtml>(new SafeHtmlCell()) {
			@Override
			public SafeHtml getValue(BaseDtoMonitorRule theObject) {
				SafeHtml typeDescriptions = toTypeDescriptions(theObject);
				return typeDescriptions;
			}
		};
		myGrid.addColumn(criteriaColumn, "Criteria");

		// Current Status
		Column<BaseDtoMonitorRule, SafeHtml> currentStatusColumn = new Column<BaseDtoMonitorRule, SafeHtml>(new SafeHtmlCell()) {
			@Override
			public SafeHtml getValue(BaseDtoMonitorRule theObject) {
				GMonitorRuleFiring latestFiring = myRulePidToLatestMonitorRuleFiring.get(theObject.getPid());
				if (latestFiring == null) {
					StringBuilder b = new StringBuilder();
					b.append("<img src=\"");
					b.append(AdminPortal.IMAGES.dashMonitorOk().getSafeUri().asString());
					b.append("\" />");
					b.append("Ok");
					return SafeHtmlUtils.fromSafeConstant(b.toString());
				}

				// TODO: If the rule isn't currently firing, also include the last time it did fire

				SafeHtmlBuilder b = new SafeHtmlBuilder();
				b.appendHtmlConstant("<img src=\"" + AdminPortal.IMAGES.dashMonitorAlert().getSafeUri().asString() + "\" />");
				b.appendHtmlConstant("Failing since ");
				b.append(DateUtil.formatTimeElapsedForMessage(latestFiring.getStartDate()));
				return b.toSafeHtml();
			}
		};
		myGrid.addColumn(currentStatusColumn, "Current Status");
		myGrid.getColumn(myGrid.getColumnCount() - 1).setSortable(true);
		sortHandler.setComparator(myGrid.getColumn(myGrid.getColumnCount() - 1), new Comparator<BaseDtoMonitorRule>() {
			@Override
			public int compare(BaseDtoMonitorRule theO1, BaseDtoMonitorRule theO2) {
				GMonitorRuleFiring firing1 = myRulePidToLatestMonitorRuleFiring.get(theO1.getPid());
				GMonitorRuleFiring firing2 = myRulePidToLatestMonitorRuleFiring.get(theO2.getPid());
				if (firing1 == null && firing2 == null) {
					return 0;
				}
				if (firing1 == null) {
					return -1;
				}
				if (firing2 == null) {
					return 1;
				}
				return firing1.getStartDate().compareTo(firing2.getStartDate());
			}
		});

		myGrid.getColumnSortList().push(appliesToColumn);

		HorizontalPanel controlsPanel = new HorizontalPanel();
		contentPanel.add(controlsPanel);

		PButton addButton = new PButton(AdminPortal.IMAGES.iconAdd(), "Add Active Rule");
		addButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent theEvent) {
				History.newItem(NavProcessor.getTokenAddMonitorRule(0));
			}
		});
		controlsPanel.add(addButton);

		addButton = new PButton(AdminPortal.IMAGES.iconAdd(), "Add Passive Rule");
		addButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent theEvent) {
				History.newItem(NavProcessor.getTokenAddMonitorRule(1));
			}
		});
		controlsPanel.add(addButton);

	}

}
