package net.svcret.admin.client.ui.config.auth;

import static net.svcret.admin.client.AdminPortal.IMAGES;
import static net.svcret.admin.client.AdminPortal.MSGS;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import net.svcret.admin.client.AdminPortal;
import net.svcret.admin.client.ui.components.CssConstants;
import net.svcret.admin.client.ui.components.EditableField;
import net.svcret.admin.client.ui.components.LoadingSpinner;
import net.svcret.admin.client.ui.components.PButton;
import net.svcret.admin.client.ui.components.ThrottleEditorGrid;
import net.svcret.admin.client.ui.components.TwoColumnGrid;
import net.svcret.admin.client.ui.config.KeepRecentTransactionsPanel;
import net.svcret.admin.shared.IAsyncLoadCallback;
import net.svcret.admin.shared.Model;
import net.svcret.admin.shared.model.BaseDtoAuthenticationHost;
import net.svcret.admin.shared.model.DtoAuthenticationHostList;
import net.svcret.admin.shared.model.GUser;
import net.svcret.admin.shared.util.StringUtil;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.TextBox;

public abstract class BaseUserPanel extends FlowPanel {

	private TabPanel myBottomTabPanel;
	private EditableField myContactEmailsEditor;
	private FlowPanel myContentPanel;
	private Grid myIpsGrid;
	private FlowPanel myKeepRecentTransactionsContainerPanel;
	private KeepRecentTransactionsPanel myKeepRecentTransactionsPanel;
	private int myKeepRecentTransactionsTabIndex;
	private LoadingSpinner myLoadingSpinner;
	private EditableField myNotesTextBox;
	private CheckBox myPasswordCheckbox;
	private TextBox myPasswordTextbox;
	private PermissionsPanel myPermissionsPanel;
	private ThrottleEditorGrid myThrottleControlGrid;
	private GUser myUser;
	private TwoColumnGrid myUsernamePasswordGrid;

	private TextBox myUsernameTextBox;

	public BaseUserPanel() {
		FlowPanel listPanel = new FlowPanel();
		listPanel.setStylePrimaryName(CssConstants.MAIN_PANEL);
		add(listPanel);

		Label titleLabel = new Label(getPanelTitle());
		titleLabel.setStyleName(CssConstants.MAIN_PANEL_TITLE);
		listPanel.add(titleLabel);

		myContentPanel = new FlowPanel();
		myContentPanel.addStyleName(CssConstants.CONTENT_INNER_PANEL);
		listPanel.add(myContentPanel);

		myLoadingSpinner = new LoadingSpinner();
		myContentPanel.add(myLoadingSpinner);

		myLoadingSpinner.show();

	}
	
	protected abstract String getPanelTitle();

	protected void initContents() {
		myUsernamePasswordGrid = new TwoColumnGrid();
		myContentPanel.add(myUsernamePasswordGrid);

		PButton saveButton = new PButton(AdminPortal.IMAGES.iconSave(), AdminPortal.MSGS.actions_Save(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent theEvent) {
				save();
			}
		});
		myContentPanel.add(saveButton);
		myContentPanel.add(myLoadingSpinner);

		/*
		 * Contact
		 */

		FlowPanel contactPanel = new FlowPanel();
		contactPanel.setStylePrimaryName(CssConstants.MAIN_PANEL);
		add(contactPanel);

		Label contactTitleLabel = new Label(MSGS.editUser_ContactTitle());
		contactTitleLabel.setStyleName(CssConstants.MAIN_PANEL_TITLE);
		contactPanel.add(contactTitleLabel);

		TwoColumnGrid contactGrid = new TwoColumnGrid();
		contactPanel.add(contactGrid);

		myNotesTextBox = new EditableField();
		myNotesTextBox.setMultiline(true);
		myNotesTextBox.addValueChangeHandler(new ValueChangeHandler<String>() {
			@Override
			public void onValueChange(ValueChangeEvent<String> theEvent) {
				myUser.setContactNotes(myNotesTextBox.getValue());
			}
		});
		contactGrid.addRow(MSGS.editUser_ContactNotes(), myNotesTextBox);
		contactGrid.setMaximizeSecondColumn();

		myContactEmailsEditor = new EditableField();
		myContactEmailsEditor.setMultiline(true);
		myContactEmailsEditor.setWidth("200px");
		myContactEmailsEditor.setEmptyTextToDisplay("No addresses defined");
		myContactEmailsEditor.addValueChangeHandler(new ValueChangeHandler<String>() {
			@Override
			public void onValueChange(ValueChangeEvent<String> theEvent) {
				Set<String> emails = new TreeSet<>();
				for (String next : myContactEmailsEditor.getValueOrBlank().split(",| ")) {
					if (next.contains("@")) {
						emails.add(next.trim());
					}
				}
				myUser.setContactEmails(emails);
				updateContactEmailEditor();
			}
		});
		contactGrid.addRow("Email Addresses", myContactEmailsEditor);

		/*
		 * Permissions
		 */

		myBottomTabPanel = new TabPanel();
		myBottomTabPanel.addStyleName(CssConstants.CONTENT_OUTER_TAB_PANEL);
		add(myBottomTabPanel);
		
		FlowPanel permsTabPanel = new FlowPanel();
		myBottomTabPanel.add(permsTabPanel, MSGS.editUser_PermissionsTitle());
		myBottomTabPanel.selectTab(0);

		myPermissionsPanel = new PermissionsPanel();
		permsTabPanel.add(myPermissionsPanel);

		/*
		 * Firewall
		 */

		FlowPanel ipsPanel = new FlowPanel();
		myBottomTabPanel.add(ipsPanel,MSGS.editUser_IpsTitle());
		ipsPanel.add(new Label(MSGS.editUser_IpsDesc()));

		myIpsGrid = new Grid();
		// myIpsGrid.addStyleName(CssConstants.PROPERTY_TABLE);
		ipsPanel.add(myIpsGrid);

		/*
		 * Throttling
		 */
		{
		FlowPanel throttlePanel = new FlowPanel();
		myBottomTabPanel.add(throttlePanel, AdminPortal.MSGS.user_requestThrottling());

		myThrottleControlGrid = new ThrottleEditorGrid();
		throttlePanel.add(myThrottleControlGrid);
		}
		
		/*
		 * Logging/Auditing
		 */
		{
			FlowPanel logPanel = new FlowPanel();
			myKeepRecentTransactionsTabIndex = myBottomTabPanel.getWidgetCount();
			myBottomTabPanel.add(logPanel, "Log/Audit");

			myKeepRecentTransactionsContainerPanel = new FlowPanel();
			logPanel.add(myKeepRecentTransactionsContainerPanel);
			
		}
		
		
	}

	protected void save() {


		if (!myKeepRecentTransactionsPanel.validateAndShowErrorIfNotValid()) {
			myBottomTabPanel.selectTab(myKeepRecentTransactionsTabIndex);
			return;
		}
		
		myKeepRecentTransactionsPanel.populateDto(myUser);
		
		myLoadingSpinner.show();
		AdminPortal.MODEL_SVC.saveUser(myUser, new AsyncCallback<GUser>() {
			@Override
			public void onFailure(Throwable theCaught) {
				Model.handleFailure(theCaught);
			}

			@Override
			public void onSuccess(GUser theResult) {
				myUser.setPid(theResult.getPid());
				myLoadingSpinner.showMessage(MSGS.editUser_DoneSaving(), false);
			}
		});

	}

	private void setAllowableIpToEdit(final int theIndex) {
		TextBox editTextBox = new TextBox();
		editTextBox.setValue(myUser.getAllowableSourceIps().get(theIndex));
		editTextBox.addValueChangeHandler(new ValueChangeHandler<String>() {
			@Override
			public void onValueChange(ValueChangeEvent<String> theEvent) {
				myUser.getAllowableSourceIps().set(theIndex, theEvent.getValue());
			}
		});
		myIpsGrid.setWidget(theIndex, 2, editTextBox);
	}
	private ListBox myAuthHostBox;

	public void setUser(final GUser theResult, final BaseDtoAuthenticationHost theAuthHost) {
		Model.getInstance().loadAuthenticationHosts(new IAsyncLoadCallback<DtoAuthenticationHostList>() {

			
			@Override
			public void onSuccess(final DtoAuthenticationHostList theAuthHostList) {

		myLoadingSpinner.hideCompletely();
		myUser = theResult;
		myPermissionsPanel.setPermissions(theResult);

		myUsernamePasswordGrid.clear();

		myNotesTextBox.setValue(theResult.getContactNotes());

		// User Name
		myUsernameTextBox = new TextBox();
		myUsernameTextBox.setValue(myUser.getUsername());
		myUsernameTextBox.addValueChangeHandler(new ValueChangeHandler<String>() {
			@Override
			public void onValueChange(ValueChangeEvent<String> theEvent) {
				theResult.setUsername(theEvent.getValue());
			}
		});
		myUsernamePasswordGrid.addRow(MSGS.editUser_Username(), myUsernameTextBox);

		// Auth Host
		myAuthHostBox = new ListBox(false);
		for (BaseDtoAuthenticationHost nextAuthHost : theAuthHostList) {
			myAuthHostBox.addItem(nextAuthHost.getModuleId());
			if (nextAuthHost.getPid()==theResult.getAuthHostPid()) {
				myAuthHostBox.setSelectedIndex(myAuthHostBox.getItemCount()-1);
			}
		}
		myAuthHostBox.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent theArg0) {
				myUser.setAuthHostPid(theAuthHostList.get(myAuthHostBox.getSelectedIndex()).getPid());
			}
		});
		myUsernamePasswordGrid.addRow(MSGS.name_AuthenticationHost(), myAuthHostBox);
		
		final EditableField descriptionTextBox = new EditableField();
		descriptionTextBox.setValue(theResult.getDescription());
		descriptionTextBox.addValueChangeHandler(new ValueChangeHandler<String>() {
			@Override
			public void onValueChange(ValueChangeEvent<String> theEvent) {
				theResult.setDescription(descriptionTextBox.getValue());
			}
		});
		myUsernamePasswordGrid.addRow("Description", descriptionTextBox);

		if (theAuthHost.isSupportsPasswordChange()) {
			myPasswordCheckbox = new CheckBox(MSGS.editUser_Password());
			myPasswordCheckbox.setStyleName(CssConstants.FORM_LABEL);
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
			if (BaseUserPanel.this instanceof AddUserPanel) {
				myPasswordCheckbox.setValue(true);
				myPasswordCheckbox.setEnabled(false);
			}
			myUsernamePasswordGrid.addRow(myPasswordCheckbox, myPasswordTextbox);
			
		}

		updateContactEmailEditor();

		myThrottleControlGrid.setThrottle(myUser);

		updateIpsGrid();

		myKeepRecentTransactionsPanel = new KeepRecentTransactionsPanel(theResult);
		myKeepRecentTransactionsContainerPanel.clear();
		myKeepRecentTransactionsContainerPanel.add(myKeepRecentTransactionsPanel);

			}
		});
		
	}

	private void updateContactEmailEditor() {
		StringBuilder b = new StringBuilder();

		HashSet<String> contactEmails = myUser.getContactEmails();
		if (contactEmails != null) {
			for (String next : contactEmails) {
				if (b.length() > 0) {
					b.append("\n");
				}
				b.append(next);
			}
		}
		myContactEmailsEditor.setValue(b.toString());
	}

	private void updateIpsGrid() {
		final List<String> allowableIps = myUser.getAllowableSourceIps();

		myIpsGrid.resize(allowableIps.size() + 1, 3);

		for (int i = 0; i < allowableIps.size(); i++) {
			final int nextIndex = i;
			final String nextValue = allowableIps.get(i);

			// Remove
			PButton removeButton = new PButton(IMAGES.iconRemove(), MSGS.actions_Remove());
			removeButton.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent theEvent) {
					allowableIps.remove(nextIndex);
					updateIpsGrid();
				}
			});
			myIpsGrid.setWidget(i, 0, removeButton);

			// Edit
			final PButton editButton = new PButton(IMAGES.iconEdit(), MSGS.actions_Edit());
			editButton.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent theBtnEvent) {
					editButton.setEnabled(false);
					setAllowableIpToEdit(nextIndex);
				}

			});
			myIpsGrid.setWidget(i, 1, editButton);

			if (StringUtil.isBlank(nextValue)) {
				setAllowableIpToEdit(nextIndex);
			} else {
				myIpsGrid.setWidget(nextIndex, 2, new Label(nextValue));
			}

		}

		PButton addButton = new PButton(IMAGES.iconAdd(), MSGS.actions_Add());
		addButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent theEvent) {
				myUser.getAllowableSourceIps().add("");
				updateIpsGrid();
			}
		});
		myIpsGrid.setWidget(allowableIps.size(), 0, addButton);

	}
	
}
