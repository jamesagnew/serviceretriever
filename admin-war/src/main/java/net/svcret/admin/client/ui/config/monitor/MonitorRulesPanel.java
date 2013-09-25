package net.svcret.admin.client.ui.config.monitor;

import static net.svcret.admin.client.AdminPortal.IMAGES;
import static net.svcret.admin.client.AdminPortal.MSGS;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
import net.svcret.admin.shared.enm.MonitorRuleTypeEnum;
import net.svcret.admin.shared.model.BaseDtoMonitorRule;
import net.svcret.admin.shared.model.BaseDtoServiceVersion;
import net.svcret.admin.shared.model.DtoMonitorRuleActive;
import net.svcret.admin.shared.model.DtoMonitorRuleActiveCheck;
import net.svcret.admin.shared.model.GDomainList;
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
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.view.client.ListDataProvider;

public class MonitorRulesPanel extends FlowPanel {

	private LoadingSpinner myConfigListLoadingSpinner;
	private PCellTable<BaseDtoMonitorRule> myGrid;
	private ListDataProvider<BaseDtoMonitorRule> myDataProvider;
	private ListBox myAddTypeBox;
	private GDomainList myDomainList;
	private Map<Long, GMonitorRuleFiring> myRulePidToLatestMonitorRuleFiring;

	public MonitorRulesPanel() {
		initListPanel();

		AdminPortal.MODEL_SVC.loadMonitorRuleList(new AsyncCallback<GMonitorRuleList>() {
			@Override
			public void onSuccess(final GMonitorRuleList theResult) {
				Model.getInstance().loadDomainList(new IAsyncLoadCallback<GDomainList>() {
					@Override
					public void onSuccess(final GDomainList theDomainList) {
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

		myDataProvider.getList().clear();
		myDataProvider.getList().addAll(theResult.toCollection());
		myDataProvider.refresh();
	}

	private List<String> toAppliesTo(BaseDtoMonitorRule theNext) {
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
				BaseDtoServiceVersion svcVer = svc.getVersionList().getVersionByPid(next);
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

	private List<SafeHtml> toTypeDescriptions(BaseDtoMonitorRule theNext) {
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

		// Active
		Column<BaseDtoMonitorRule, SafeHtml> typeColumn = new Column<BaseDtoMonitorRule, SafeHtml>(new SafeHtmlCell()) {
			@Override
			public SafeHtml getValue(BaseDtoMonitorRule theObject) {
				return SafeHtmlUtils.fromTrustedString(theObject.getRuleType().getFriendlyName());
			}
		};
		myGrid.addColumn(typeColumn, "Type");
		myGrid.getColumn(myGrid.getColumnCount() - 1).setSortable(true);
		sortHandler.setComparator(myGrid.getColumn(myGrid.getColumnCount() - 1), new Comparator<BaseDtoMonitorRule>() {
			@Override
			public int compare(BaseDtoMonitorRule theO1, BaseDtoMonitorRule theO2) {
				return theO1.getRuleType().ordinal()- theO2.getRuleType().ordinal();
			}
		});

		// Criteria
		Column<BaseDtoMonitorRule, SafeHtml> criteriaColumn = new Column<BaseDtoMonitorRule, SafeHtml>(new SafeHtmlCell()) {
			@Override
			public SafeHtml getValue(BaseDtoMonitorRule theObject) {
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

		// Current Status
		Column<BaseDtoMonitorRule, SafeHtml> currentStatusColumn = new Column<BaseDtoMonitorRule, SafeHtml>(new SafeHtmlCell()) {
			@Override
			public SafeHtml getValue(BaseDtoMonitorRule theObject) {
				GMonitorRuleFiring latestFiring = myRulePidToLatestMonitorRuleFiring.get(theObject.getPid());
				if (latestFiring == null) {
					return SafeHtmlUtils.fromSafeConstant("Ok");
				}

				// TODO: If the rule isn't currently firing, also include the last time it did fire

				SafeHtmlBuilder b = new SafeHtmlBuilder();
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
				if (firing1==null&&firing2==null) {
					return 0;
				}
				if (firing1==null) {
					return -1;
				}
				if (firing2==null) {
					return 1;
				}
				return firing1.getStartDate().compareTo(firing2.getStartDate());
			}
		});

		myGrid.getColumnSortList().push(ruleNameColumn);

		HorizontalPanel controlsPanel = new HorizontalPanel();
		contentPanel.add(controlsPanel);

		myAddTypeBox = new ListBox(false);
		for (MonitorRuleTypeEnum next : MonitorRuleTypeEnum.values()) {
			myAddTypeBox.addItem(next.getFriendlyName());
		}
		myAddTypeBox.setSelectedIndex(0);
		controlsPanel.add(myAddTypeBox);

		PButton addButton = new PButton(AdminPortal.IMAGES.iconAdd(), AdminPortal.MSGS.actions_Add());
		addButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent theEvent) {
				History.newItem(NavProcessor.getTokenAddMonitorRule(myAddTypeBox.getSelectedIndex()));
			}
		});
		controlsPanel.add(addButton);

	}

}
