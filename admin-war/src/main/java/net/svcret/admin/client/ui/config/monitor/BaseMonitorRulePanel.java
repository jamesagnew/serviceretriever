package net.svcret.admin.client.ui.config.monitor;

import net.svcret.admin.client.AdminPortal;
import net.svcret.admin.client.ui.components.CssConstants;
import net.svcret.admin.client.ui.components.HtmlH1;
import net.svcret.admin.client.ui.components.LoadingSpinner;
import net.svcret.admin.client.ui.components.PButton;
import net.svcret.admin.client.ui.components.TwoColumnGrid;
import net.svcret.admin.client.ui.config.auth.DomainTreePanel.ITreeStatusModel;
import net.svcret.admin.shared.HtmlUtil;
import net.svcret.admin.shared.IAsyncLoadCallback;
import net.svcret.admin.shared.Model;
import net.svcret.admin.shared.model.BaseGServiceVersion;
import net.svcret.admin.shared.model.GDomain;
import net.svcret.admin.shared.model.GDomainList;
import net.svcret.admin.shared.model.GMonitorRule;
import net.svcret.admin.shared.model.GService;
import net.svcret.admin.shared.model.GServiceMethod;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IntegerBox;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;

public abstract class BaseMonitorRulePanel extends FlowPanel {

	private static final String FIRE_1_ONLY_IF_ALL_BACKING_UR_LS_ARE_FAILING = "Fire only if ALL backing URLs are failing";
	private static final String FIRE_0_IF_ANY_BACKING_URL_IS_FAILING = "Fire if ANY backing URL is failing";

	private RuleAppliesToTree myAppliesToTree;

	private IntegerBox myLatencyBox;
	private CheckBox myLatencyEnabledCheck;
	private IntegerBox myLatencyOverMinsBox;
	private CheckBox myLatencyOverMinsEnabledCheck;
	private LoadingSpinner myLoadingSpinner;
	private GMonitorRule myRule;
	private CheckBox myRuleActiveCheckBox;
	private TextBox myRuleNameTextBox;
	private CheckBox myUrlUnavailableCheckbox;
	private ListBox myUrlUnavailableTypeCombo;
	private GDomainList myDomainList;

	public BaseMonitorRulePanel() {
		initTopPanel();
		initCriteriaPanel();
		initAppliesToPanel();
	}

	private void initAppliesToPanel() {
		FlowPanel introPanel = new FlowPanel();
		introPanel.setStylePrimaryName("mainPanel");
		add(introPanel);

		Label titleLabel = new Label("Rule Application");
		titleLabel.setStyleName("mainPanelTitle");
		introPanel.add(titleLabel);

		FlowPanel contentPanel = new FlowPanel();
		contentPanel.addStyleName("contentInnerPanel");
		introPanel.add(contentPanel);

		contentPanel.add(new Label("Rules may apply to entire domains, or to all versions " + "of a service, but may also be applied to individual versions. Select the " + "entries in the service catalog that this rule should apply to from the " + "tree below."));

		myAppliesToTree = new RuleAppliesToTree();
		contentPanel.add(myAppliesToTree);

	}

	private void initCriteriaPanel() {
		FlowPanel introPanel = new FlowPanel();
		introPanel.setStylePrimaryName("mainPanel");
		add(introPanel);

		Label titleLabel = new Label("Rule Criteria");
		titleLabel.setStyleName("mainPanelTitle");
		introPanel.add(titleLabel);

		FlowPanel contentPanel = new FlowPanel();
		contentPanel.addStyleName("contentInnerPanel");
		introPanel.add(contentPanel);

		TwoColumnGrid grid = new TwoColumnGrid();
		contentPanel.add(grid);

		// Backing URL Failures

		HtmlH1 h1 = new HtmlH1("Backing Service Failure");
		grid.addFullWidthCell(h1);

		myUrlUnavailableCheckbox = new CheckBox("Backing URLs");
		myUrlUnavailableCheckbox.setValue(true);
		myUrlUnavailableTypeCombo = new ListBox(false);

		// NB careful reordering these...
		myUrlUnavailableTypeCombo.addItem(FIRE_0_IF_ANY_BACKING_URL_IS_FAILING);
		myUrlUnavailableTypeCombo.addItem(FIRE_1_ONLY_IF_ALL_BACKING_UR_LS_ARE_FAILING);

		grid.addRow(myUrlUnavailableCheckbox, myUrlUnavailableTypeCombo);
		grid.addDescription("If enabled, the rule will fire upon detecting failures when " + "accessing the backing service implementations. Failures generally occur when " + "the backing server can't be reached, returns HTTP 500, etc.");
		myUrlUnavailableCheckbox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> theEvent) {
				myUrlUnavailableTypeCombo.setEnabled(myUrlUnavailableCheckbox.getValue());
			}
		});

		// Backing Service Latency

		h1 = new HtmlH1("Backing Service Latency");
		grid.addFullWidthCell(h1);

		myLatencyBox = new IntegerBox();
		myLatencyEnabledCheck = new CheckBox(HtmlUtil.toSafeHtml("Latency exceeds: "));
		myLatencyEnabledCheck.setEnabled(true);
		myLatencyEnabledCheck.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> theEvent) {
				updateLatencyBoxes();
			}

		});
		grid.addRow(myLatencyEnabledCheck, myLatencyBox);
		grid.addDescription("If enabled, this rule will fire if the backing service latency (in other words, " + "the average amount of time that the backing service is taking to respond to requests) " + "exceeds the threshold set here (in milliseconds).");

		myLatencyOverMinsBox = new IntegerBox();
		myLatencyOverMinsEnabledCheck = new CheckBox(HtmlUtil.toSafeHtml("Over Minutes"));
		myLatencyOverMinsEnabledCheck.setEnabled(true);
		myLatencyOverMinsEnabledCheck.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> theEvent) {
				updateLatencyBoxes();
			}
		});
		grid.addRow(myLatencyOverMinsEnabledCheck, myLatencyOverMinsBox);
		grid.addDescription("If enabled, the latency text will look at the average over a number of " + "minutes instead of simply checking the latest latency number.");

	}

	private void initTopPanel() {
		FlowPanel introPanel = new FlowPanel();
		introPanel.setStylePrimaryName("mainPanel");
		add(introPanel);

		Label titleLabel = new Label(getPanelTitle());
		titleLabel.setStyleName("mainPanelTitle");
		introPanel.add(titleLabel);

		FlowPanel contentPanel = new FlowPanel();
		contentPanel.addStyleName("contentInnerPanel");
		introPanel.add(contentPanel);

		myLoadingSpinner = new LoadingSpinner();
		myLoadingSpinner.show();
		contentPanel.add(myLoadingSpinner);

		TwoColumnGrid grid = new TwoColumnGrid();
		contentPanel.add(grid);

		myRuleNameTextBox = new TextBox();
		grid.addRow("Rule Name", myRuleNameTextBox);

		myRuleActiveCheckBox = new CheckBox();
		grid.addRow("Enabled", myRuleActiveCheckBox);

		PButton saveButton = new PButton(AdminPortal.IMAGES.iconSave(), AdminPortal.MSGS.actions_Save());
		saveButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent theEvent) {
				save();
			}
		});
		contentPanel.add(saveButton);
	}

	private void updateLatencyBoxes() {
		Boolean value = myLatencyEnabledCheck.getValue();
		myLatencyBox.setEnabled(value);
		if (value && myLatencyBox.getValue() == null) {
			myLatencyBox.setValue(1000);
		}

		myLatencyOverMinsEnabledCheck.setEnabled(value);

		value = value && myLatencyOverMinsEnabledCheck.getValue();
		myLatencyOverMinsBox.setEnabled(value);
		if (value && myLatencyOverMinsBox.getValue() == null) {
			myLatencyOverMinsBox.setValue(1000);
		}

	}

	protected abstract String getPanelTitle();

	protected void save() {
		myLatencyBox.removeStyleName(CssConstants.TEXTBOX_WITH_ERR);

		myRule.setActive(myRuleActiveCheckBox.getValue());
		Integer latency = null;
		Integer latencyOverMins = null;
		if (myLatencyEnabledCheck.getValue()) {
			latency = myLatencyBox.getValue();
			if (latency == null || latency < 1) {
				myLatencyBox.addStyleName(CssConstants.TEXTBOX_WITH_ERR);
				Window.alert("Latency alert is enabled, but minimum latency has not been specified.");
				myLatencyBox.setFocus(true);
				return;
			}

			if (myLatencyOverMinsEnabledCheck.getValue()) {
				latencyOverMins = myLatencyOverMinsBox.getValue();
				if (latencyOverMins==null||latencyOverMins<1) {
					myLatencyOverMinsBox.addStyleName(CssConstants.TEXTBOX_WITH_ERR);
					Window.alert("Latency average over minutes is enabled, but no time has been specified.");
					myLatencyOverMinsBox.setFocus(true);
				}
			}

		}

		myRule.setFireForBackingServiceLatencyIsAboveMillis(latency);
		myRule.setFireForBackingServiceLatencySustainTimeMins(latencyOverMins);

		
		// TODO: finish
	}

	protected void setRule(final GMonitorRule theRule) {
		Model.getInstance().loadDomainList(new IAsyncLoadCallback<GDomainList>() {

			@Override
			public void onSuccess(GDomainList theResult) {
				myDomainList = theResult;
				myRule = theRule;

				myLoadingSpinner.hideCompletely();

				myAppliesToTree.setModel(theResult, new MyTreeStatusModel());

				boolean latencyText = theRule.getFireForBackingServiceLatencyIsAboveMillis() != null;
				if (latencyText) {
					myLatencyBox.setValue(theRule.getFireForBackingServiceLatencyIsAboveMillis());
					myLatencyEnabledCheck.setValue(latencyText);
					myLatencyOverMinsBox.setValue(theRule.getFireForBackingServiceLatencySustainTimeMins());
					myLatencyOverMinsEnabledCheck.setValue(theRule.getFireForBackingServiceLatencySustainTimeMins() != null);
				}

				myRuleActiveCheckBox.setValue(theRule.isActive());

				myRuleNameTextBox.setValue(theRule.getName());
				myUrlUnavailableCheckbox.setValue(theRule.isFireIfAllBackingUrlsAreUnavailable() || theRule.isFireIfSingleBackingUrlIsUnavailable());
				if (theRule.isFireIfAllBackingUrlsAreUnavailable()) {
					myUrlUnavailableTypeCombo.setSelectedIndex(1);
				} else {
					myUrlUnavailableTypeCombo.setSelectedIndex(0);
				}
			}
		});
	}

	public class MyTreeStatusModel implements ITreeStatusModel {

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
