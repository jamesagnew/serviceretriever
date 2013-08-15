package net.svcret.admin.client.ui.config.auth;

import static net.svcret.admin.client.AdminPortal.IMAGES;
import static net.svcret.admin.client.AdminPortal.MSGS;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import net.svcret.admin.client.AdminPortal;
import net.svcret.admin.client.ui.components.CssConstants;
import net.svcret.admin.client.ui.components.EditableField;
import net.svcret.admin.client.ui.components.LoadingSpinner;
import net.svcret.admin.client.ui.components.PButton;
import net.svcret.admin.client.ui.components.TwoColumnGrid;
import net.svcret.admin.shared.Model;
import net.svcret.admin.shared.enm.ThrottlePeriodEnum;
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
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.IntegerBox;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;

public abstract class BaseUserPanel extends FlowPanel {

	private FlowPanel myContentPanel;
	private LoadingSpinner myLoadingSpinner;
	private CheckBox myPasswordCheckbox;
	private TextBox myPasswordTextbox;
	private PermissionsPanel myPermissionsPanel;
	private GUser myUser;
	private TwoColumnGrid myUsernamePasswordGrid;
	private TextBox myUsernameTextBox;
	private EditableField myNotesTextBox;
	private Grid myIpsGrid;
	private CheckBox myThrottleCheckbox;
	private IntegerBox myThrottleNumberBox;
	private ListBox myThrottlePeriodCombo;
	private CheckBox myThrottleQueueCheckbox;
	private IntegerBox myThrottleQueueMaxLength;
	private EditableField myContactEmailsEditor;

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

	public void setUser(final GUser theResult, BaseGAuthHost theAuthHost) {
		myLoadingSpinner.hideCompletely();
		myUser = theResult;
		myPermissionsPanel.setPermissions(theResult);

		myUsernamePasswordGrid.clear();

		myNotesTextBox.setValue(theResult.getContactNotes());

		myUsernameTextBox = new TextBox();
		myUsernameTextBox.setValue(myUser.getUsername());
		myUsernameTextBox.addValueChangeHandler(new ValueChangeHandler<String>() {
			@Override
			public void onValueChange(ValueChangeEvent<String> theEvent) {
				theResult.setUsername(theEvent.getValue());
			}
		});
		myUsernamePasswordGrid.addRow(MSGS.editUser_Username(), myUsernameTextBox);

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
			if (this instanceof AddUserPanel) {
				myPasswordCheckbox.setValue(true);
				myPasswordCheckbox.setEnabled(false);
			}
			myUsernamePasswordGrid.addRow(myPasswordCheckbox, myPasswordTextbox);
			
			updateContactEmailEditor();

		}

		boolean throttle = myUser.getThrottle().getMaxRequests() != null;
		myThrottleCheckbox.setValue(throttle);
		if (throttle) {
			myThrottleNumberBox.setValue(myUser.getThrottle().getMaxRequests());
			myThrottlePeriodCombo.setSelectedIndex(Arrays.asList(ThrottlePeriodEnum.values()).indexOf(myUser.getThrottle().getPeriod()));
			boolean queue = myUser.getThrottle().getQueue() != null;
			myThrottleQueueCheckbox.setValue(queue);
			if (queue) {
				myThrottleQueueMaxLength.setValue(myUser.getThrottle().getQueue());
			}
		}

		updateIpsGrid();

	}

	protected abstract String getPanelTitle();

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
		 * Contcat
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
				Set<String> emails = new TreeSet<String>();
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

		FlowPanel permsPanel = new FlowPanel();
		permsPanel.setStylePrimaryName(CssConstants.MAIN_PANEL);
		add(permsPanel);

		Label titleLabel = new Label(MSGS.editUser_PermissionsTitle());
		titleLabel.setStyleName(CssConstants.MAIN_PANEL_TITLE);
		permsPanel.add(titleLabel);

		myPermissionsPanel = new PermissionsPanel();
		permsPanel.add(myPermissionsPanel);

		/*
		 * Allowed IPs
		 */

		FlowPanel ipsPanel = new FlowPanel();
		ipsPanel.setStylePrimaryName(CssConstants.MAIN_PANEL);
		add(ipsPanel);

		Label ipsTitleLabel = new Label(MSGS.editUser_IpsTitle());
		ipsTitleLabel.setStyleName(CssConstants.MAIN_PANEL_TITLE);
		ipsPanel.add(ipsTitleLabel);

		ipsPanel.add(new Label(MSGS.editUser_IpsDesc()));

		myIpsGrid = new Grid();
		// myIpsGrid.addStyleName(CssConstants.PROPERTY_TABLE);
		ipsPanel.add(myIpsGrid);

		/*
		 * Throttling
		 */

		FlowPanel throttlePanel = new FlowPanel();
		throttlePanel.setStylePrimaryName(CssConstants.MAIN_PANEL);
		add(throttlePanel);

		Label throttleTitleLabel = new Label(AdminPortal.MSGS.user_requestThrottling());
		throttleTitleLabel.setStyleName(CssConstants.MAIN_PANEL_TITLE);
		throttlePanel.add(throttleTitleLabel);

		FlowPanel throttleContentPanel = new FlowPanel();
		throttleContentPanel.addStyleName(CssConstants.CONTENT_INNER_PANEL);
		throttlePanel.add(throttleContentPanel);

		TwoColumnGrid throttleGrid = new TwoColumnGrid();
		throttleContentPanel.add(throttleGrid);

		myThrottleCheckbox = new CheckBox("Enable Throttling");
		myThrottleNumberBox = new IntegerBox();
		myThrottlePeriodCombo = new ListBox(false);
		for (ThrottlePeriodEnum next : ThrottlePeriodEnum.values()) {
			myThrottlePeriodCombo.addItem(next.getDescription(), next.name());
		}
		HorizontalPanel throttleCheckboxValuePanel = new HorizontalPanel();
		throttleCheckboxValuePanel.add(myThrottleNumberBox);
		throttleCheckboxValuePanel.add(new HTML("&nbsp;requests&nbsp;/&nbsp;"));
		throttleCheckboxValuePanel.add(myThrottlePeriodCombo);
		throttleGrid.addRow(myThrottleCheckbox, throttleCheckboxValuePanel);
		throttleGrid.addDescription("If enabled, requests from this user will not be permitted to proceed " + "faster than the given rate.");

		myThrottleQueueCheckbox = new CheckBox("Queue");
		myThrottleQueueMaxLength = new IntegerBox();
		throttleGrid.addRow(myThrottleQueueCheckbox, myThrottleQueueMaxLength);
		throttleGrid.addDescription("If enabled, up to the given number of requests will be allowed to queue if they arrive "
				+ "faster than the given throttle limit. After the throttle period has been passed, they will be permitted to "
				+ "proceed (and will receive a normal response). If more than the given number of requests arrive during the " + "throttle period, any excess requests will be rejected immediately.");

		myThrottleCheckbox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> theEvent) {
				myThrottlePeriodCombo.setEnabled(theEvent.getValue());
				myThrottleNumberBox.setEnabled(theEvent.getValue());
				myThrottleQueueCheckbox.setEnabled(theEvent.getValue());
				myThrottleQueueMaxLength.setEnabled(theEvent.getValue() && myThrottleQueueCheckbox.getValue());
				if (theEvent.getValue()) {
					if (myThrottleNumberBox.getValue() == null) {
						myThrottleNumberBox.setValue(1);
					}
				}
			}
		});

		myThrottleQueueCheckbox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> theEvent) {
				if (theEvent.getValue() && myThrottleQueueMaxLength.getValue() == null) {
					myThrottleQueueMaxLength.setValue(10);
				}
			}
		});

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

	protected void save() {

		if (myThrottleCheckbox.getValue()) {
			myUser.getThrottle().setMaxRequests(myThrottleNumberBox.getValue());
			myUser.getThrottle().setPeriod(ThrottlePeriodEnum.valueOf(myThrottlePeriodCombo.getValue(myThrottlePeriodCombo.getSelectedIndex())));
			if (myThrottleQueueCheckbox.getValue()) {
				myUser.getThrottle().setQueue(myThrottleQueueMaxLength.getValue());
			} else {
				myUser.getThrottle().setQueue(null);
			}
		} else {
			myUser.getThrottle().setMaxRequests(null);
			myUser.getThrottle().setPeriod(null);
			myUser.getThrottle().setQueue(null);
		}

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
}
