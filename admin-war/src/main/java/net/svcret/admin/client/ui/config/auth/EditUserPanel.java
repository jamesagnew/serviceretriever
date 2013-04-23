package net.svcret.admin.client.ui.config.auth;

import static net.svcret.admin.client.AdminPortal.*;
import net.svcret.admin.client.AdminPortal;
import net.svcret.admin.client.rpc.ModelUpdateService.UserAndAuthHost;
import net.svcret.admin.client.ui.components.CssConstants;
import net.svcret.admin.client.ui.components.LoadingSpinner;
import net.svcret.admin.client.ui.components.PButton;
import net.svcret.admin.client.ui.components.TwoColumnGrid;
import net.svcret.admin.shared.Model;
import net.svcret.admin.shared.model.BaseGAuthHost;
import net.svcret.admin.shared.model.GUser;
import net.svcret.admin.shared.util.StringUtil;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;

public class EditUserPanel extends FlowPanel {

	private LoadingSpinner myLoadingSpinner;
	private PermissionsPanel myPermissionsPanel;
	private FlowPanel myContentPanel;
	private TextBox myUsernameTextBox;
	private TwoColumnGrid myUsernamePasswordGrid;
	private GUser myUser;
	private CheckBox myPasswordCheckbox;
	private TextBox myPasswordTextbox;

	public EditUserPanel(long thePid) {
		FlowPanel listPanel = new FlowPanel();
		listPanel.setStylePrimaryName(CssConstants.MAIN_PANEL);
		add(listPanel);

		Label titleLabel = new Label(MSGS.editUser_Title());
		titleLabel.setStyleName(CssConstants.MAIN_PANEL_TITLE);
		listPanel.add(titleLabel);

		myContentPanel = new FlowPanel();
		myContentPanel.addStyleName(CssConstants.CONTENT_INNER_PANEL);
		listPanel.add(myContentPanel);

		myLoadingSpinner = new LoadingSpinner();
		myContentPanel.add(myLoadingSpinner);

		myLoadingSpinner.show();
		AdminPortal.MODEL_SVC.loadUser(thePid, new AsyncCallback<UserAndAuthHost>() {
			@Override
			public void onFailure(Throwable theCaught) {
				Model.handleFailure(theCaught);
			}

			@Override
			public void onSuccess(UserAndAuthHost theResult) {
				initContents();
				setUser(theResult.getUser(), theResult.getAuthHost());
			}
		});

	}

	private void initContents() {

		// Move the spinner to after the save button
		myLoadingSpinner.hideCompletely();
		myContentPanel.remove(myLoadingSpinner);

		myUsernamePasswordGrid = new TwoColumnGrid();
		myContentPanel.add(myUsernamePasswordGrid);

		PButton saveButton = new PButton(AdminPortal.MSGS.actions_Save(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent theEvent) {
				save();
			}
		});
		myContentPanel.add(saveButton);
		myContentPanel.add(myLoadingSpinner);

		myPermissionsPanel = new PermissionsPanel();
		myContentPanel.add(myPermissionsPanel);
	}

	protected void save() {

		myLoadingSpinner.show();
		AdminPortal.MODEL_SVC.saveUser(myUser, new AsyncCallback<Void>() {
			@Override
			public void onFailure(Throwable theCaught) {
				Model.handleFailure(theCaught);
			}

			@Override
			public void onSuccess(Void theResult) {
				myLoadingSpinner.showMessage(MSGS.editUser_DoneSaving(), false);
			}
		});

	}

	private void setUser(final GUser theResult, BaseGAuthHost theAuthHost) {
		myUser = theResult;
		myPermissionsPanel.setPermissions(theResult);

		myUsernamePasswordGrid.clear();

		myUsernameTextBox = new TextBox();
		myUsernameTextBox.setValue(myUser.getUsername());
		myUsernameTextBox.addValueChangeHandler(new ValueChangeHandler<String>() {
			@Override
			public void onValueChange(ValueChangeEvent<String> theEvent) {
				theResult.setUsername(theEvent.getValue());
			}
		});
		myUsernamePasswordGrid.addRow(MSGS.editUser_Username(), myUsernameTextBox);

		if (theAuthHost.isSupportsPasswordChange()) {
			myPasswordCheckbox = new CheckBox(MSGS.editUser_Password());
			myPasswordCheckbox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
				@Override
				public void onValueChange(ValueChangeEvent<Boolean> theEvent) {
					if (myPasswordCheckbox.getValue()) {
						myUser.setChangePassword(myPasswordTextbox.getValue());
					} else {
						myUser.setChangePassword(null);
					}
				}
			});
			myPasswordTextbox = new TextBox();
			myPasswordTextbox.addValueChangeHandler(new ValueChangeHandler<String>() {
				@Override
				public void onValueChange(ValueChangeEvent<String> theEvent) {
					myPasswordCheckbox.setValue(StringUtil.isNotBlank(myPasswordTextbox.getValue()));
					if (myPasswordCheckbox.getValue()) {
						myUser.setChangePassword(myPasswordTextbox.getValue());
					} else {
						myUser.setChangePassword(null);
					}
				}
			});
			myUsernamePasswordGrid.addRow(myPasswordCheckbox, myPasswordTextbox);
		}

	}

}
