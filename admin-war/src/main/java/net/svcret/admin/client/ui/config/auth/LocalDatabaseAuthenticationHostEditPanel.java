package net.svcret.admin.client.ui.config.auth;

import static net.svcret.admin.client.AdminPortal.*;
import net.svcret.admin.client.AdminPortal;
import net.svcret.admin.client.ui.components.CssConstants;
import net.svcret.admin.client.ui.components.HtmlH1;
import net.svcret.admin.client.ui.components.LoadingSpinner;
import net.svcret.admin.client.ui.components.PButton;
import net.svcret.admin.client.ui.components.TwoColumnGrid;
import net.svcret.admin.shared.IAsyncLoadCallback;
import net.svcret.admin.shared.Model;
import net.svcret.admin.shared.model.GAuthenticationHostList;
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

public class LocalDatabaseAuthenticationHostEditPanel extends BaseAuthenticationHostEditPanel {

	private TextBox myIdTextBox;
	private TextBox myNameTextBox;
	private LoadingSpinner myLoadingSpinner;
	private GLocalDatabaseAuthHost myAuthHost;
	private CheckBox myCacheEnabledCheckBox;
	private IntegerBox myCacheMillisTextBox;
	private AuthenticationHostsPanel myParent;

	public LocalDatabaseAuthenticationHostEditPanel(AuthenticationHostsPanel thePanel, GLocalDatabaseAuthHost theAuthHost) {
		myParent =thePanel;
		myAuthHost = theAuthHost;

		FlowPanel listPanel = new FlowPanel();
		listPanel.setStylePrimaryName("mainPanel");
		add(listPanel);

		Label titleLabel = new Label(MSGS.localDatabaseAuthenticationHostEditPanel_title());
		titleLabel.setStyleName("mainPanelTitle");
		listPanel.add(titleLabel);

		
		listPanel.add(new HTML(AdminPortal.MSGS.localDatabaseAuthenticationHostEditPanel_description()));

		HorizontalPanel savePanel = new HorizontalPanel();
		listPanel.add(savePanel);

		PButton saveButton = new PButton(AdminPortal.MSGS.actions_Save(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent theEvent) {
				save();
			}
		});
		savePanel.add(saveButton);
		myLoadingSpinner = new LoadingSpinner();
		savePanel.add(myLoadingSpinner);

		TwoColumnGrid idGrid = new TwoColumnGrid();
		listPanel.add(idGrid);

		myIdTextBox = new TextBox();
		myIdTextBox.setValue(theAuthHost.getModuleId());
		idGrid.addRow(AdminPortal.MSGS.propertyNameId(), myIdTextBox);
		myNameTextBox = new TextBox();
		myNameTextBox.setValue(theAuthHost.getModuleName());
		idGrid.addRow(AdminPortal.MSGS.propertyNameName(), myNameTextBox);

		listPanel.add(new HtmlH1(AdminPortal.MSGS.baseAuthenticationHostEditPanel_CacheResponsesTitle()));
		listPanel.add(new HTML(AdminPortal.MSGS.baseAuthenticationHostEditPanel_CacheResponsesDesc()));

		TwoColumnGrid cacheGrid = new TwoColumnGrid();
		listPanel.add(cacheGrid);
		myCacheEnabledCheckBox = new CheckBox(AdminPortal.MSGS.baseAuthenticationHostEditPanel_CacheEnabledCacheForMillis());
		myCacheEnabledCheckBox.setValue(theAuthHost.getCacheSuccessesForMillis() != null);
		myCacheEnabledCheckBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> theEvent) {
				updateCacheBox();
			}
		});

		myCacheMillisTextBox = new IntegerBox();
		updateCacheBox();
		cacheGrid.addRow(myCacheEnabledCheckBox, myCacheMillisTextBox);
	}

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

		myLoadingSpinner.show();
		Model.getInstance().saveAuthenticationHost(myAuthHost, new IAsyncLoadCallback<GAuthenticationHostList>(){
			@Override
			public void onSuccess(GAuthenticationHostList theResult) {
				myLoadingSpinner.showMessage(AdminPortal.MSGS.baseAuthenticationHostEditPanel_Saved(), false);
				myParent.setHostList(theResult);
			}});
		
	}

}
