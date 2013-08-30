package net.svcret.admin.client.ui.config.auth;

import net.svcret.admin.client.AdminPortal;
import net.svcret.admin.client.ui.components.CssConstants;
import net.svcret.admin.client.ui.components.HtmlH1;
import net.svcret.admin.client.ui.components.LoadingSpinner;
import net.svcret.admin.client.ui.components.PButton;
import net.svcret.admin.client.ui.components.TwoColumnGrid;
import net.svcret.admin.client.ui.config.KeepRecentTransactionsPanel;
import net.svcret.admin.shared.IAsyncLoadCallback;
import net.svcret.admin.shared.Model;
import net.svcret.admin.shared.model.BaseGAuthHost;
import net.svcret.admin.shared.model.GAuthenticationHostList;
import net.svcret.admin.shared.model.GLdapAuthHost;
import net.svcret.admin.shared.model.GLocalDatabaseAuthHost;
import net.svcret.admin.shared.util.StringUtil;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.IntegerBox;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;

abstract class BaseAuthenticationHostEditPanel<T extends BaseGAuthHost> extends FlowPanel {
	private TextBox myIdTextBox;
	private TextBox myNameTextBox;
	private LoadingSpinner myLoadingSpinner;
	private CheckBox myCacheEnabledCheckBox;
	private IntegerBox myCacheMillisTextBox;
	private T myAuthHost;
	private AuthenticationHostsPanel myParent;
	private FlowPanel myContentPanel;
	private KeepRecentTransactionsPanel myKeepRecentTransactionsPanel;

	/**
	 * Constructor
	 * 
	 * @param thePanel
	 */
	@SuppressWarnings("unchecked")
	BaseAuthenticationHostEditPanel(AuthenticationHostsPanel thePanel, T theAuthHost) {
		myParent = thePanel;

		switch (theAuthHost.getType()) {
		case LOCAL_DATABASE:
			myAuthHost = (T) new GLocalDatabaseAuthHost();
			((T) myAuthHost).merge((T) theAuthHost);
			break;
		case LDAP:
			myAuthHost = (T) new GLdapAuthHost();
			((T) myAuthHost).merge((T) theAuthHost);
			break;
		}

		myContentPanel = new FlowPanel();
		myContentPanel.setStylePrimaryName(CssConstants.MAIN_PANEL);
		add(myContentPanel);

		Label titleLabel = new Label(getPanelTitle());
		titleLabel.setStyleName(CssConstants.MAIN_PANEL_TITLE);
		myContentPanel.add(titleLabel);

		myContentPanel.add(new HTML(getPanelDescription()));

		HorizontalPanel savePanel = new HorizontalPanel();
		myContentPanel.add(savePanel);

		PButton saveButton = new PButton(AdminPortal.IMAGES.iconSave(),AdminPortal.MSGS.actions_Save(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent theEvent) {
				save();
			}
		});
		savePanel.add(saveButton);
		myLoadingSpinner = new LoadingSpinner();
		savePanel.add(myLoadingSpinner);

		TwoColumnGrid idGrid = new TwoColumnGrid();
		myContentPanel.add(idGrid);

		myIdTextBox = new TextBox();
		myIdTextBox.setValue(theAuthHost.getModuleId());
		idGrid.addRow(AdminPortal.MSGS.propertyNameId(), myIdTextBox);
		myNameTextBox = new TextBox();
		myNameTextBox.setValue(theAuthHost.getModuleName());
		idGrid.addRow(AdminPortal.MSGS.propertyNameName(), myNameTextBox);

		// Cache responses 
		
		myContentPanel.add(new HtmlH1(AdminPortal.MSGS.baseAuthenticationHostEditPanel_CacheResponsesTitle()));

		TwoColumnGrid cacheGrid = new TwoColumnGrid();
		myContentPanel.add(cacheGrid);
		myCacheEnabledCheckBox = new CheckBox(AdminPortal.MSGS.baseAuthenticationHostEditPanel_CacheResponsesEnabled());
		myCacheEnabledCheckBox.setValue(theAuthHost.getCacheSuccessesForMillis() != null);
		myCacheEnabledCheckBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> theEvent) {
				updateCacheBox();
			}
		});

		myCacheMillisTextBox = new IntegerBox();
		updateCacheBox();
		
		HorizontalPanel cacheHp =new HorizontalPanel();
		cacheHp.add(myCacheEnabledCheckBox);
		cacheHp.add(myCacheMillisTextBox);
		
		cacheGrid.addRow(AdminPortal.MSGS.baseAuthenticationHostEditPanel_CacheResponses(), cacheHp);
		cacheGrid.addDescription(AdminPortal.MSGS.baseAuthenticationHostEditPanel_CacheResponsesDesc());
		
		myKeepRecentTransactionsPanel = new KeepRecentTransactionsPanel(theAuthHost);
		myContentPanel.add(myKeepRecentTransactionsPanel);
		
	}

	protected FlowPanel getContentPanel() {
		return myContentPanel;
	}

	protected T getAuthHost() {
		return myAuthHost;
	}

	protected void save() {
		myIdTextBox.removeStyleName(CssConstants.TEXTBOX_WITH_ERR);
		if (StringUtil.isBlank(myIdTextBox.getValue())) {
			myLoadingSpinner.showMessage(AdminPortal.MSGS.baseAuthenticationHostEditPanel_errorNoId(), false);
			myIdTextBox.addStyleName(CssConstants.TEXTBOX_WITH_ERR);
			return;
		}
		myAuthHost.setModuleId(myIdTextBox.getValue());

		myNameTextBox.removeStyleName(CssConstants.TEXTBOX_WITH_ERR);
		if (StringUtil.isBlank(myNameTextBox.getValue())) {
			myLoadingSpinner.showMessage(AdminPortal.MSGS.baseAuthenticationHostEditPanel_errorNoName(), false);
			myNameTextBox.addStyleName(CssConstants.TEXTBOX_WITH_ERR);
			return;
		}
		myAuthHost.setModuleName(myNameTextBox.getValue());

		if (myCacheEnabledCheckBox.getValue()) {
			myCacheMillisTextBox.removeStyleName(CssConstants.TEXTBOX_WITH_ERR);
			Integer cacheMillis = myCacheMillisTextBox.getValue();
			if (cacheMillis == null || cacheMillis <= 0) {
				myCacheMillisTextBox.addStyleName(CssConstants.TEXTBOX_WITH_ERR);
				myLoadingSpinner.showMessage(AdminPortal.MSGS.baseAuthenticationHostEditPanel_ErrorNoCacheValue(), false);
				return;
			}
			myAuthHost.setCacheSuccessesForMillis(cacheMillis);
		} else {
			myAuthHost.setCacheSuccessesForMillis(null);
		}

		if (!myKeepRecentTransactionsPanel.validateAndShowErrorIfNotValid()) {
			return;
		}
		
		applySettingsFromUi();
		
		myKeepRecentTransactionsPanel.populateDto(myAuthHost);
		
		myLoadingSpinner.show();
		Model.getInstance().saveAuthenticationHost(myAuthHost, new IAsyncLoadCallback<GAuthenticationHostList>() {
			@Override
			public void onSuccess(GAuthenticationHostList theResult) {
				Model.getInstance().setAuthenticationHostList(theResult);
				myLoadingSpinner.showMessage(AdminPortal.MSGS.baseAuthenticationHostEditPanel_Saved(), false);
				myParent.setHostList(theResult);
			}
		});

	}

	protected abstract void applySettingsFromUi();

	protected abstract String getPanelDescription();

	protected abstract String getPanelTitle();

	private void updateCacheBox() {
		if (myCacheEnabledCheckBox.getValue()) {
			if (myAuthHost.getCacheSuccessesForMillis() != null) {
				myCacheMillisTextBox.setValue(myAuthHost.getCacheSuccessesForMillis(), false);
			} else {
				myCacheMillisTextBox.setValue(60000);
			}
			myCacheMillisTextBox.setEnabled(true);
		} else {
			myCacheMillisTextBox.setEnabled(false);
		}
	}

}
