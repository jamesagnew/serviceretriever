package net.svcret.admin.client.ui.config.monitor;

import static net.svcret.admin.client.AdminPortal.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.svcret.admin.client.AdminPortal;
import net.svcret.admin.client.MyResources;
import net.svcret.admin.client.nav.NavProcessor;
import net.svcret.admin.client.ui.components.CssConstants;
import net.svcret.admin.client.ui.components.EditableField;
import net.svcret.admin.client.ui.components.HtmlH1;
import net.svcret.admin.client.ui.components.LoadingSpinner;
import net.svcret.admin.client.ui.components.PButton;
import net.svcret.admin.client.ui.components.PButtonCell;
import net.svcret.admin.client.ui.components.PCellTable;
import net.svcret.admin.client.ui.components.PSelectionCell;
import net.svcret.admin.client.ui.components.TwoColumnGrid;
import net.svcret.admin.client.ui.components.VersionPickerPanel;
import net.svcret.admin.client.ui.components.VersionPickerPanel.ChangeListener;
import net.svcret.admin.client.ui.config.auth.DomainTreePanel.ITreeStatusModel;
import net.svcret.admin.client.ui.config.svcver.NullColumn;
import net.svcret.admin.shared.HtmlUtil;
import net.svcret.admin.shared.IAsyncLoadCallback;
import net.svcret.admin.shared.Model;
import net.svcret.admin.shared.enm.MonitorRuleTypeEnum;
import net.svcret.admin.shared.enm.ResponseTypeEnum;
import net.svcret.admin.shared.enm.ThrottlePeriodEnum;
import net.svcret.admin.shared.model.BaseGMonitorRule;
import net.svcret.admin.shared.model.BaseGServiceVersion;
import net.svcret.admin.shared.model.DtoLibraryMessage;
import net.svcret.admin.shared.model.DtoMonitorRuleActive;
import net.svcret.admin.shared.model.DtoMonitorRuleActiveCheck;
import net.svcret.admin.shared.model.GDomain;
import net.svcret.admin.shared.model.GDomainList;
import net.svcret.admin.shared.model.GMonitorRuleList;
import net.svcret.admin.shared.model.GMonitorRulePassive;
import net.svcret.admin.shared.model.GService;
import net.svcret.admin.shared.model.GServiceMethod;
import net.svcret.admin.shared.util.StringUtil;

import com.google.gwt.cell.client.CompositeCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.HasCell;
import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.cell.client.SelectionCell;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.IdentityColumn;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.IntegerBox;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.view.client.ListDataProvider;

public abstract class BaseMonitorRulePanel extends FlowPanel {

	private static final String FIRE_1_ONLY_IF_ALL_BACKING_UR_LS_ARE_FAILING = "Fire only if ALL backing URLs are failing";
	private static final String FIRE_0_IF_ANY_BACKING_URL_IS_FAILING = "Fire if ANY backing URL is failing";

	private RuleAppliesToTree myPassiveAppliesToTree;
	private IntegerBox myPassiveLatencyBox;
	private CheckBox myPassiveLatencyEnabledCheck;
	private IntegerBox myPassiveLatencyOverMinsBox;
	private CheckBox myPassiveLatencyOverMinsEnabledCheck;
	private LoadingSpinner myLoadingSpinner;
	private BaseGMonitorRule myRule;
	private CheckBox myRuleActiveCheckBox;
	private TextBox myRuleNameTextBox;
	private CheckBox myPassiveUrlUnavailableCheckbox;
	private ListBox myPassiveUrlUnavailableTypeCombo;
	private EditableField myNotificationEditor;
	private FlowPanel myCriteriaPanel;
	private ListDataProvider<DtoMonitorRuleActiveCheck> myActiveChecksDataProvider;
	private FlowPanel myPassiveAppliesToOuterPanel;
	private GDomainList myDomainList;
	private ListBox myActiveAddMessagePickerBox;
	private HTML myActiveAddMessagePickerDescription;
	private PButton myAddActiveCheckButton;
	private Long myActiveSelectedServiceVersionPid;

	public BaseMonitorRulePanel() {
		initTopPanel();

		myCriteriaPanel = new FlowPanel();
		myCriteriaPanel.setStylePrimaryName(CssConstants.MAIN_PANEL);
		add(myCriteriaPanel);

		initNotifyPanel();
		initAppliesToPanel();
	}

	private void initNotifyPanel() {
		FlowPanel introPanel = new FlowPanel();
		introPanel.setStylePrimaryName(CssConstants.MAIN_PANEL);
		add(introPanel);

		Label titleLabel = new Label("Notification");
		titleLabel.setStyleName(CssConstants.MAIN_PANEL_TITLE);
		introPanel.add(titleLabel);

		FlowPanel contentPanel = new FlowPanel();
		contentPanel.addStyleName(CssConstants.CONTENT_INNER_PANEL);
		introPanel.add(contentPanel);

		TwoColumnGrid grid = new TwoColumnGrid();
		contentPanel.add(grid);

		myNotificationEditor = new EditableField();
		myNotificationEditor.setWidth("200px");
		myNotificationEditor.setEmptyTextToDisplay("No addresses defined");

		grid.addRow("Notify Email(s)", myNotificationEditor);
		grid.addDescription("Enter any emails here to notify when the rule fires");

	}

	private void initAppliesToPanel() {
		myPassiveAppliesToOuterPanel = new FlowPanel();
		myPassiveAppliesToOuterPanel.setStylePrimaryName(CssConstants.MAIN_PANEL);
		add(myPassiveAppliesToOuterPanel);

		Label titleLabel = new Label("Rule Application");
		titleLabel.setStyleName(CssConstants.MAIN_PANEL_TITLE);
		myPassiveAppliesToOuterPanel.add(titleLabel);

		FlowPanel contentPanel = new FlowPanel();
		contentPanel.addStyleName(CssConstants.CONTENT_INNER_PANEL);
		myPassiveAppliesToOuterPanel.add(contentPanel);

		contentPanel.add(new Label("Rules may apply to entire domains, or to all versions " + "of a service, but may also be applied to individual versions. Select the " + "entries in the service catalog that this rule should apply to from the " + "tree below."));

		myPassiveAppliesToTree = new RuleAppliesToTree();
		contentPanel.add(myPassiveAppliesToTree);

	}

	private void initActiveCriteriaPanel() {
		Label titleLabel = new Label("Rule Criteria");
		titleLabel.setStyleName(CssConstants.MAIN_PANEL_TITLE);
		myCriteriaPanel.add(titleLabel);

		FlowPanel contentPanel = new FlowPanel();
		contentPanel.addStyleName(CssConstants.CONTENT_INNER_PANEL);
		myCriteriaPanel.add(contentPanel);

		contentPanel.add(new Label("This rule is active, meaning that it manually sends messages to proxied " + "service implementations on a periodic basis and then checks the outcome"));

		PCellTable<DtoMonitorRuleActiveCheck> grid = new PCellTable<DtoMonitorRuleActiveCheck>();
		grid.setEmptyTableWidget(new Label("No checks have been defined for this rule."));
		contentPanel.add(grid);

		// Remove Button
		
		Column<DtoMonitorRuleActiveCheck, String> removeColumn = new NullColumn<DtoMonitorRuleActiveCheck>(new PButtonCell(IMAGES.iconRemove(), MSGS.actions_Remove()));
		grid.addColumn(removeColumn, "");
		removeColumn.setFieldUpdater(new FieldUpdater<DtoMonitorRuleActiveCheck, String>() {
			@Override
			public void update(int theIndex, DtoMonitorRuleActiveCheck theObject, String theValue) {
				((DtoMonitorRuleActive)myRule).getCheckList().remove(theObject);
				initActiveValues((DtoMonitorRuleActive) myRule);
			}
		});
		removeColumn.setCellStyleNames(CssConstants.PCELLTABLE_ACTION_COLUMN);

		// Frequency

		List<HasCell<DtoMonitorRuleActiveCheck, ?>> frequencyCells = new ArrayList<HasCell<DtoMonitorRuleActiveCheck, ?>>();
		Column<DtoMonitorRuleActiveCheck, SafeHtml> frequencyEveryColumn = new Column<DtoMonitorRuleActiveCheck, SafeHtml>(new SafeHtmlCell()) {
			@Override
			public SafeHtml getValue(DtoMonitorRuleActiveCheck theObject) {
				return SafeHtmlUtils.fromSafeConstant("Every ");
			}
		};
		frequencyCells.add(frequencyEveryColumn);
		Column<DtoMonitorRuleActiveCheck, String> frequencyNumColumn = new Column<DtoMonitorRuleActiveCheck, String>(new PEditTextCell()) {
			@Override
			public String getValue(DtoMonitorRuleActiveCheck theObject) {
				return Integer.toString(theObject.getCheckFrequencyNum());
			}
		};
		frequencyNumColumn.setFieldUpdater(new FieldUpdater<DtoMonitorRuleActiveCheck, String>() {
			@Override
			public void update(int theIndex, DtoMonitorRuleActiveCheck theObject, String theValue) {
				if (theValue.matches("^[0-9]+$")) {
					theObject.setCheckFrequencyNum(Integer.parseInt(theValue));
				}
			}
		});
		frequencyCells.add(frequencyNumColumn);
		Column<DtoMonitorRuleActiveCheck, String> frequencyUnitColumn = new Column<DtoMonitorRuleActiveCheck, String>(new SelectionCell(ThrottlePeriodEnum.getDescriptions())) {
			@Override
			public String getValue(DtoMonitorRuleActiveCheck theObject) {
				return theObject.getCheckFrequencyUnit().getDescription();
			}
		};
		frequencyUnitColumn.setFieldUpdater(new FieldUpdater<DtoMonitorRuleActiveCheck, String>() {
			@Override
			public void update(int theIndex, DtoMonitorRuleActiveCheck theObject, String theValue) {
				theObject.setCheckFrequencyUnit(ThrottlePeriodEnum.forDescription(theValue));
			}
		});
		frequencyCells.add(frequencyUnitColumn);
		CompositeCell<DtoMonitorRuleActiveCheck> frequencyCell = new CompositeCell<DtoMonitorRuleActiveCheck>(frequencyCells);
		Column<DtoMonitorRuleActiveCheck, ?> frequencyColumn = new IdentityColumn<DtoMonitorRuleActiveCheck>(frequencyCell);
		grid.addColumn(frequencyColumn, "Frequency");

		// Message

		Column<DtoMonitorRuleActiveCheck, SafeHtml> msgColumn = new Column<DtoMonitorRuleActiveCheck, SafeHtml>(new SafeHtmlCell()) {
			@Override
			public SafeHtml getValue(DtoMonitorRuleActiveCheck theObject) {
				SafeHtmlBuilder b = new SafeHtmlBuilder();
				b.appendHtmlConstant("<div class=\"" + MyResources.CSS.monitorRuleActiveTargetKey() + "\">Message</div>");
				b.appendHtmlConstant("<div class=\"" + MyResources.CSS.monitorRuleActiveTargetValue() + "\">");
				b.appendEscaped(theObject.getMessageDescription());
				b.appendHtmlConstant("</div>");

				b.appendHtmlConstant("<div class=\"" + MyResources.CSS.monitorRuleActiveTargetKey() + "\">Target</div>");
				b.appendHtmlConstant("<div class=\"" + MyResources.CSS.monitorRuleActiveTargetValue() + "\">");

				GService service = myDomainList.getServiceWithServiceVersion(theObject.getServiceVersionPid());
				BaseGServiceVersion svcVer = service.getVersionList().getVersionByPid(theObject.getServiceVersionPid());

				b.appendEscaped(service.getName());
				b.appendHtmlConstant(" / ");
				b.appendEscaped(svcVer.getId());

				b.appendHtmlConstant("</div>");

				return b.toSafeHtml();
			}
		};
		grid.addColumn(msgColumn, "Message & Target");

		// Expect Response

		List<HasCell<DtoMonitorRuleActiveCheck, ?>> expectCells = new ArrayList<HasCell<DtoMonitorRuleActiveCheck, ?>>();
		Column<DtoMonitorRuleActiveCheck, SafeHtml> expectHeaderCellColumn = new Column<DtoMonitorRuleActiveCheck, SafeHtml>(new SafeHtmlCell()) {
			@Override
			public SafeHtml getValue(DtoMonitorRuleActiveCheck theObject) {
				return SafeHtmlUtils.fromSafeConstant("<div class=\"" + MyResources.CSS.monitorRuleActiveTargetKey() + "\">Expected Response</div>");
			}
		};
		expectCells.add(expectHeaderCellColumn);
		Column<DtoMonitorRuleActiveCheck, String> expectResponseColumn = new Column<DtoMonitorRuleActiveCheck, String>(new PSelectionCell(ResponseTypeEnum.backendStatusIndexes(), ResponseTypeEnum.backendStatusDescriptions())) {
			@Override
			public String getValue(DtoMonitorRuleActiveCheck theObject) {
				return theObject.getCheckFrequencyUnit().getDescription();
			}
		};
		expectResponseColumn.setFieldUpdater(new FieldUpdater<DtoMonitorRuleActiveCheck, String>() {
			@Override
			public void update(int theIndex, DtoMonitorRuleActiveCheck theObject, String theValue) {
				theObject.setExpectResponseType(ResponseTypeEnum.forBackendStatusIndex(theValue));
			}
		});
		expectCells.add(expectResponseColumn);
		Column<DtoMonitorRuleActiveCheck, SafeHtml> containingTextColumn = new Column<DtoMonitorRuleActiveCheck, SafeHtml>(new SafeHtmlCell()) {
			@Override
			public SafeHtml getValue(DtoMonitorRuleActiveCheck theObject) {
				return SafeHtmlUtils.fromSafeConstant("<div class=\"" + MyResources.CSS.monitorRuleActiveTargetKey() + "\">Containing Text</div>");
			}
		};
		expectCells.add(containingTextColumn);
		Column<DtoMonitorRuleActiveCheck, String> expectContainsTextColumn = new Column<DtoMonitorRuleActiveCheck, String>(new PEditTextCell("(Do not test response)")) {
			@Override
			public String getValue(DtoMonitorRuleActiveCheck theObject) {
				String retVal = theObject.getExpectResponseContainsText();
				if (StringUtil.isBlank(retVal)) {
					retVal = PEditTextCell.NO_VALUE_STRING;
				}
				return retVal;
			}
		};
		expectContainsTextColumn.setFieldUpdater(new FieldUpdater<DtoMonitorRuleActiveCheck, String>() {
			@Override
			public void update(int theIndex, DtoMonitorRuleActiveCheck theObject, String theValue) {
				if (theValue == null || theValue.trim().length() == 0) {
					theObject.setExpectResponseContainsText(null);
				} else {
					theObject.setExpectResponseContainsText(theValue);
				}
			}
		});
		expectCells.add(expectContainsTextColumn);
		Column<DtoMonitorRuleActiveCheck, SafeHtml> latencyHeaderColumn = new Column<DtoMonitorRuleActiveCheck, SafeHtml>(new SafeHtmlCell()) {
			@Override
			public SafeHtml getValue(DtoMonitorRuleActiveCheck theObject) {
				return SafeHtmlUtils.fromSafeConstant("<div class=\"" + MyResources.CSS.monitorRuleActiveTargetKey() + "\">With Latency Under (ms)</div>");
			}
		};
		expectCells.add(latencyHeaderColumn);
		Column<DtoMonitorRuleActiveCheck, String> expectLatencyTextColumn = new Column<DtoMonitorRuleActiveCheck, String>(new PEditTextCell("(Do not check latency)")) {
			@Override
			public String getValue(DtoMonitorRuleActiveCheck theObject) {
				Long retVal = theObject.getExpectLatencyUnderMillis();
				if (retVal==null) {
					return PEditTextCell.NO_VALUE_STRING;
				}
				return Long.toString(retVal);
			}
		};
		expectLatencyTextColumn.setFieldUpdater(new FieldUpdater<DtoMonitorRuleActiveCheck, String>() {
			@Override
			public void update(int theIndex, DtoMonitorRuleActiveCheck theObject, String theValue) {
				if (theValue.matches("^[0-9]+$")) {
					theObject.setExpectLatencyUnderMillis(Long.parseLong(theValue));
				}
			}
		});
		expectCells.add(expectLatencyTextColumn);
		CompositeCell<DtoMonitorRuleActiveCheck> expectCell = new CompositeCell<DtoMonitorRuleActiveCheck>(expectCells);
		Column<DtoMonitorRuleActiveCheck, ?> expectColumn = new IdentityColumn<DtoMonitorRuleActiveCheck>(expectCell);
		grid.addColumn(expectColumn, "Expect");

		myActiveChecksDataProvider = new ListDataProvider<DtoMonitorRuleActiveCheck>();
		myActiveChecksDataProvider.addDataDisplay(grid);

		contentPanel.add(new HtmlH1("Add Check"));
		
		final VersionPickerPanel versionPicker = new VersionPickerPanel(myDomainList);
		contentPanel.add(versionPicker);
		
		myActiveAddMessagePickerBox = new ListBox(false);
		versionPicker.addRow("Message", myActiveAddMessagePickerBox);
		myActiveAddMessagePickerDescription = versionPicker.addDescription("");
		
		myAddActiveCheckButton = new PButton(AdminPortal.IMAGES.iconAdd(),AdminPortal.MSGS.actions_Add());
		contentPanel.add(myAddActiveCheckButton);
		
		myAddActiveCheckButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent theEvent) {
				long messagePid = Long.parseLong(myActiveAddMessagePickerBox.getValue(myActiveAddMessagePickerBox.getSelectedIndex()));
				String messageDesc = myActiveAddMessagePickerBox.getItemText(myActiveAddMessagePickerBox.getSelectedIndex());
				DtoMonitorRuleActiveCheck check = new DtoMonitorRuleActiveCheck();
				check.setCheckFrequencyNum(1);
				check.setCheckFrequencyUnit(ThrottlePeriodEnum.MINUTE);
				check.setExpectResponseType(ResponseTypeEnum.SUCCESS);
				check.setMessagePid(messagePid);
				check.setMessageDescription(messageDesc);
				check.setServiceVersionPid(myActiveSelectedServiceVersionPid);
				((DtoMonitorRuleActive)myRule).getCheckList().add(check);
				initActiveValues((DtoMonitorRuleActive) myRule);
			}
		});
		
		
		updateActiveAddMessagePickerBox(versionPicker);
		versionPicker.addVersionChangeHandler(new ChangeListener() {
			@Override
			public void onChange(Long theSelectedPid) {
				updateActiveAddMessagePickerBox(versionPicker);
			}
		});
			
	}

	private void updateActiveAddMessagePickerBox(final VersionPickerPanel theVersionPicker) {
		final Long versionPid = theVersionPicker.getSelectedVersionPid();
		myActiveSelectedServiceVersionPid = versionPid;
		if (versionPid == null) {
			myActiveAddMessagePickerDescription.setHTML("Select a service with at least one version, then select a message");
			myActiveAddMessagePickerBox.clear();
			myAddActiveCheckButton.setEnabled(false);
			return;
		}
		
		myActiveAddMessagePickerDescription.setHTML("Loading messages...");
		AdminPortal.MODEL_SVC.loadLibraryMessagesForServiveVersion(versionPid, new AsyncCallback<Collection<DtoLibraryMessage>>() {
			@Override
			public void onFailure(Throwable theCaught) {
				Model.handleFailure(theCaught);
			}

			@Override
			public void onSuccess(Collection<DtoLibraryMessage> theResult) {
				Long foundVersionPid = theVersionPicker.getSelectedVersionPid();
				if (!versionPid.equals(foundVersionPid)) {
					return;
				}
				
				myActiveAddMessagePickerBox.clear();
				if (theResult.isEmpty()) {
					handleActiveAddMessagePickerBoxChange(versionPid);
					return;
				}
				
				for (DtoLibraryMessage next : theResult) {
					myActiveAddMessagePickerBox.addItem(next.getDescription(), next.getPid().toString());
				}
			
				if (myActiveAddMessagePickerBox.getItemCount()>0) {
					myActiveAddMessagePickerBox.setSelectedIndex(0);
				}
				handleActiveAddMessagePickerBoxChange(versionPid);
			}

		});
		
	}

	private void handleActiveAddMessagePickerBoxChange(long versionPid) {
		boolean enabled = myActiveAddMessagePickerBox.getItemCount()>0;
		myAddActiveCheckButton.setEnabled(enabled);
		if (enabled) {
			myActiveAddMessagePickerDescription.setHTML("");
		}else {
			myActiveAddMessagePickerDescription.setHTML("This version has no messages in the <a href=\"" + NavProcessor.getTokenServiceVersionMessageLibrary(true,versionPid)+"\">message library</a> for this service version. Add a message before adding active monitor checks for this version to this rule.");
		}
	}
	
	private void initPassiveCriteriaPanel() {

		Label titleLabel = new Label("Passive Rule Criteria");
		titleLabel.setStyleName(CssConstants.MAIN_PANEL_TITLE);
		myCriteriaPanel.add(titleLabel);

		FlowPanel contentPanel = new FlowPanel();
		contentPanel.addStyleName(CssConstants.CONTENT_INNER_PANEL);
		myCriteriaPanel.add(contentPanel);

		contentPanel.add(new Label("This rule is passive, meaning that it examines the outcomes of normal requests " + "which arrive from outside clients."));

		TwoColumnGrid grid = new TwoColumnGrid();
		contentPanel.add(grid);

		// Backing URL Failures

		HtmlH1 h1 = new HtmlH1("Backing Service Failure");
		grid.addFullWidthCell(h1);

		myPassiveUrlUnavailableCheckbox = new CheckBox("Backing URLs");
		myPassiveUrlUnavailableCheckbox.setValue(true);
		myPassiveUrlUnavailableTypeCombo = new ListBox(false);

		// NB careful reordering these...
		myPassiveUrlUnavailableTypeCombo.addItem(FIRE_0_IF_ANY_BACKING_URL_IS_FAILING);
		myPassiveUrlUnavailableTypeCombo.addItem(FIRE_1_ONLY_IF_ALL_BACKING_UR_LS_ARE_FAILING);

		grid.addRow(myPassiveUrlUnavailableCheckbox, myPassiveUrlUnavailableTypeCombo);
		grid.addDescription("If enabled, the rule will fire upon detecting failures when " + "accessing the backing service implementations. Failures generally occur when " + "the backing server can't be reached, returns HTTP 500, etc.");
		myPassiveUrlUnavailableCheckbox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> theEvent) {
				myPassiveUrlUnavailableTypeCombo.setEnabled(myPassiveUrlUnavailableCheckbox.getValue());
			}
		});

		// Backing Service Latency

		h1 = new HtmlH1("Backing Service Latency");
		grid.addFullWidthCell(h1);

		myPassiveLatencyBox = new IntegerBox();
		myPassiveLatencyEnabledCheck = new CheckBox(HtmlUtil.toSafeHtml("Latency exceeds: "));
		myPassiveLatencyEnabledCheck.setEnabled(true);
		myPassiveLatencyEnabledCheck.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> theEvent) {
				updateLatencyBoxes();
			}

		});
		grid.addRow(myPassiveLatencyEnabledCheck, myPassiveLatencyBox);
		grid.addDescription("If enabled, this rule will fire if the backing service latency (in other words, " + "the average amount of time that the backing service is taking to respond to requests) " + "exceeds the threshold set here (in milliseconds).");

		myPassiveLatencyOverMinsBox = new IntegerBox();
		myPassiveLatencyOverMinsEnabledCheck = new CheckBox(HtmlUtil.toSafeHtml("Over Minutes"));
		myPassiveLatencyOverMinsEnabledCheck.setEnabled(true);
		myPassiveLatencyOverMinsEnabledCheck.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> theEvent) {
				updateLatencyBoxes();
			}
		});
		grid.addRow(myPassiveLatencyOverMinsEnabledCheck, myPassiveLatencyOverMinsBox);
		grid.addDescription("If enabled, the latency text will look at the average over a number of " + "minutes instead of simply checking the latest latency number.");

	}

	private void initTopPanel() {
		FlowPanel introPanel = new FlowPanel();
		introPanel.setStylePrimaryName(CssConstants.MAIN_PANEL);
		add(introPanel);

		Label titleLabel = new Label(getPanelTitle());
		titleLabel.setStyleName(CssConstants.MAIN_PANEL_TITLE);
		introPanel.add(titleLabel);

		FlowPanel contentPanel = new FlowPanel();
		contentPanel.addStyleName(CssConstants.CONTENT_INNER_PANEL);
		introPanel.add(contentPanel);

		TwoColumnGrid grid = new TwoColumnGrid();
		contentPanel.add(grid);

		myRuleNameTextBox = new TextBox();
		grid.addRow("Rule Name", myRuleNameTextBox);

		myRuleActiveCheckBox = new CheckBox();
		grid.addRow("Enabled", myRuleActiveCheckBox);

		HorizontalPanel controlsPanel = new HorizontalPanel();
		contentPanel.add(controlsPanel);

		PButton saveButton = new PButton(AdminPortal.IMAGES.iconSave(), AdminPortal.MSGS.actions_Save());
		saveButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent theEvent) {
				save();
			}
		});
		controlsPanel.add(saveButton);

		myLoadingSpinner = new LoadingSpinner();
		myLoadingSpinner.show();
		controlsPanel.add(myLoadingSpinner);

	}

	private void updateLatencyBoxes() {
		Boolean value = myPassiveLatencyEnabledCheck.getValue();
		myPassiveLatencyBox.setEnabled(value);
		if (value && myPassiveLatencyBox.getValue() == null) {
			myPassiveLatencyBox.setValue(1000);
		}

		myPassiveLatencyOverMinsEnabledCheck.setEnabled(value);

		value = value && myPassiveLatencyOverMinsEnabledCheck.getValue();
		myPassiveLatencyOverMinsBox.setEnabled(value);
		if (value && myPassiveLatencyOverMinsBox.getValue() == null) {
			myPassiveLatencyOverMinsBox.setValue(1000);
		}

	}

	protected abstract String getPanelTitle();

	protected void save() {
		myRule.setActive(myRuleActiveCheckBox.getValue());
		myRule.setName(myRuleNameTextBox.getValue());

		if (myRule.getRuleType() == MonitorRuleTypeEnum.PASSIVE) {
			myPassiveLatencyBox.removeStyleName(CssConstants.TEXTBOX_WITH_ERR);
			applyPassiveValues((GMonitorRulePassive) myRule);
		}

		myLoadingSpinner.showMessage("Saving Rule...", true);

		AdminPortal.MODEL_SVC.saveMonitorRule(myRule, new AsyncCallback<GMonitorRuleList>() {

			@Override
			public void onFailure(Throwable theCaught) {
				Model.handleFailure(theCaught);
			}

			@Override
			public void onSuccess(GMonitorRuleList theResult) {
				myLoadingSpinner.showMessage("Rule has been saved.", false);
				Model.getInstance().setMonitorRuleList(theResult);
			}
		});

	}

	private void applyPassiveValues(GMonitorRulePassive theRule) {
		Integer latency = null;
		Integer latencyOverMins = null;
		if (myPassiveLatencyEnabledCheck.getValue()) {
			latency = myPassiveLatencyBox.getValue();
			if (latency == null || latency < 1) {
				myPassiveLatencyBox.addStyleName(CssConstants.TEXTBOX_WITH_ERR);
				Window.alert("Latency alert is enabled, but minimum latency has not been specified.");
				myPassiveLatencyBox.setFocus(true);
				return;
			}

			if (myPassiveLatencyOverMinsEnabledCheck.getValue()) {
				latencyOverMins = myPassiveLatencyOverMinsBox.getValue();
				if (latencyOverMins == null || latencyOverMins < 1) {
					myPassiveLatencyOverMinsBox.addStyleName(CssConstants.TEXTBOX_WITH_ERR);
					Window.alert("Latency average over minutes is enabled, but no time has been specified.");
					myPassiveLatencyOverMinsBox.setFocus(true);
					return;
				}
			}
		}

		theRule.setPassiveFireForBackingServiceLatencyIsAboveMillis(latency);
		theRule.setPassiveFireForBackingServiceLatencySustainTimeMins(latencyOverMins);

		theRule.setPassiveFireIfAllBackingUrlsAreUnavailable(false);
		theRule.setPassiveFireIfSingleBackingUrlIsUnavailable(false);
		if (myPassiveUrlUnavailableCheckbox.getValue()) {
			if (myPassiveUrlUnavailableTypeCombo.getSelectedIndex() == 0) {
				theRule.setPassiveFireIfSingleBackingUrlIsUnavailable(true);
			} else if (myPassiveUrlUnavailableTypeCombo.getSelectedIndex() == 1) {
				theRule.setPassiveFireIfAllBackingUrlsAreUnavailable(true);
			}
		}

	}

	protected void setRule(final BaseGMonitorRule theRule) {
		Model.getInstance().loadDomainList(new IAsyncLoadCallback<GDomainList>() {

			@Override
			public void onSuccess(GDomainList theResult) {
				myDomainList = theResult;
				myRule = theRule;

				switch (theRule.getRuleType()) {
				case PASSIVE:
					initPassiveCriteriaPanel();
					initPassiveValues((GMonitorRulePassive) theRule, theResult);
					myPassiveAppliesToOuterPanel.setVisible(true);
					break;
				case ACTIVE:
					initActiveCriteriaPanel();
					initActiveValues((DtoMonitorRuleActive) theRule);
					myPassiveAppliesToOuterPanel.setVisible(false);
					break;
				}

				myLoadingSpinner.hideCompletely();
			}
		});
	}

	private void initActiveValues(DtoMonitorRuleActive theRule) {
		myActiveChecksDataProvider.getList().clear();
		myActiveChecksDataProvider.getList().addAll(theRule.getCheckList().toCollection());
		myActiveChecksDataProvider.refresh();
	}

	private void initPassiveValues(GMonitorRulePassive theRule, GDomainList theDomainList) {
		boolean latencyText = theRule.getPassiveFireForBackingServiceLatencyIsAboveMillis() != null;
		if (latencyText) {
			myPassiveLatencyBox.setValue(theRule.getPassiveFireForBackingServiceLatencyIsAboveMillis());
			myPassiveLatencyEnabledCheck.setValue(latencyText);
			myPassiveLatencyOverMinsBox.setValue(theRule.getPassiveFireForBackingServiceLatencySustainTimeMins());
			myPassiveLatencyOverMinsEnabledCheck.setValue(theRule.getPassiveFireForBackingServiceLatencySustainTimeMins() != null);
		}

		myRuleActiveCheckBox.setValue(theRule.isActive());

		myRuleNameTextBox.setValue(theRule.getName());
		myPassiveUrlUnavailableCheckbox.setValue(theRule.isPassiveFireIfAllBackingUrlsAreUnavailable() || theRule.isPassiveFireIfSingleBackingUrlIsUnavailable());
		if (theRule.isPassiveFireIfAllBackingUrlsAreUnavailable()) {
			myPassiveUrlUnavailableTypeCombo.setSelectedIndex(1);
		} else {
			myPassiveUrlUnavailableTypeCombo.setSelectedIndex(0);
		}

		myPassiveAppliesToTree.setModel(theDomainList, new MyTreeStatusModel(theRule));

	}

	public static class MyTreeStatusModel implements ITreeStatusModel {

		private GMonitorRulePassive myRule;

		public MyTreeStatusModel(GMonitorRulePassive theRule) {
			myRule = theRule;
		}

		@Override
		public boolean isEntireDomainChecked(GDomain theDomain) {
			return myRule.appliesTo(theDomain);
		}

		@Override
		public boolean isEntireServiceChecked(GDomain theDomain, GService theService) {
			return myRule.appliesTo(theService);
		}

		@Override
		public boolean isEntireServiceVersionChecked(GDomain theDomain, GService theService, BaseGServiceVersion theServiceVersion) {
			return myRule.appliesTo(theServiceVersion);
		}

		@Override
		public boolean isMethodChecked(GDomain theDomain, GService theService, BaseGServiceVersion theSvcVer, GServiceMethod theMethod) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void setEntireDomainChecked(GDomain theDomain, boolean theValue) {
			myRule.applyTo(theDomain, theValue);
		}

		@Override
		public void setEntireServiceChecked(GDomain theDomain, GService theService, boolean theValue) {
			myRule.applyTo(theDomain, theService, theValue);
		}

		@Override
		public void setEntireServiceVersionChecked(GDomain theDomain, GService theService, BaseGServiceVersion theServiceVersion, boolean theValue) {
			myRule.applyTo(theDomain, theService, theServiceVersion, theValue);
		}

		@Override
		public void setMethodChecked(GDomain theDomain, GService theService, BaseGServiceVersion theSvcVer, GServiceMethod theMethod, Boolean theValue) {
			throw new UnsupportedOperationException();
		}

	}

}
