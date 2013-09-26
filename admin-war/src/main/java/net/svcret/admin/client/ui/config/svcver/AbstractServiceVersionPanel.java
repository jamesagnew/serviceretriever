package net.svcret.admin.client.ui.config.svcver;

import net.svcret.admin.client.AdminPortal;
import net.svcret.admin.client.ui.components.CssConstants;
import net.svcret.admin.client.ui.components.EditableField;
import net.svcret.admin.client.ui.components.HtmlLabel;
import net.svcret.admin.client.ui.components.LoadingSpinner;
import net.svcret.admin.client.ui.components.PButton;
import net.svcret.admin.client.ui.components.TwoColumnGrid;
import net.svcret.admin.shared.AddServiceVersionResponse;
import net.svcret.admin.shared.IAsyncLoadCallback;
import net.svcret.admin.shared.Model;
import net.svcret.admin.shared.model.BaseDtoServiceVersion;
import net.svcret.admin.shared.model.DtoServiceVersionHl7OverHttp;
import net.svcret.admin.shared.model.DtoServiceVersionJsonRpc20;
import net.svcret.admin.shared.model.DtoServiceVersionSoap11;
import net.svcret.admin.shared.model.DtoServiceVersionVirtual;
import net.svcret.admin.shared.model.GDomain;
import net.svcret.admin.shared.model.GDomainList;
import net.svcret.admin.shared.model.GService;
import net.svcret.admin.shared.model.GServiceList;
import net.svcret.admin.shared.util.StringUtil;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.Style.Float;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public abstract class AbstractServiceVersionPanel extends FlowPanel implements RequiresResize {

	private BaseDetailPanel<?> myBottomContents;
	private FlowPanel myContentPanel;
	private EditableField myDescriptionEditor;
	private ListBox myDomainListBox;
	private Long myDomainPid;
	private LoadingSpinner myLoadingSpinner;
	private HtmlLabel myNewDomainLabel;
	private TextBox myNewDomainNameTextBox;
	private HtmlLabel myNewServiceLabel;
	private TextBox myNewServiceNameTextBox;
	private TwoColumnGrid myParentsGrid2;
	private ListBox myServiceListBox;
	private Long myServicePid;
	private BaseDtoServiceVersion myServiceVersion;
	private FlowPanel myTopPanel;
	private Long myUncommittedSessionId;
	private boolean myUpdating;
	private HasValue<String> myVersionTextBox;

	public AbstractServiceVersionPanel() {
		this(null, null, null);
	}

	protected AbstractServiceVersionPanel(Long theDomainPid, Long theServicePid, Long theUncommittedSessionId) {

		myDomainPid = theDomainPid;
		myServicePid = theServicePid;
		myUncommittedSessionId = theUncommittedSessionId;

		myTopPanel = new FlowPanel();
		add(myTopPanel);

		myTopPanel.setStylePrimaryName(CssConstants.MAIN_PANEL);

		Label titleLabel = new Label(getDialogTitle());
		titleLabel.setStyleName(CssConstants.MAIN_PANEL_TITLE);
		myTopPanel.add(titleLabel);

		myContentPanel = new FlowPanel();
		myContentPanel.addStyleName(CssConstants.CONTENT_INNER_PANEL);
		myTopPanel.add(myContentPanel);

		Label intro = new Label(getDialogDescription());
		myContentPanel.add(intro);

		myParentsGrid2 = new TwoColumnGrid();
		myContentPanel.add(myParentsGrid2);

		/*
		 * Parents
		 */

		// Choose from existing domain
		myDomainListBox = new ListBox(false);
		myDomainListBox.getElement().setId("cbDomain");
		myDomainListBox.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent theEvent) {
				handleDomainListChange();
			}
		});
		myParentsGrid2.addRow("Domain", myDomainListBox);

		// New domain (textbox for name)
		HorizontalPanel newDomainPanel = new HorizontalPanel();
		myNewDomainLabel = new HtmlLabel(" Name:", "cbNewDomainName");
		newDomainPanel.add(myNewDomainLabel);
		myNewDomainNameTextBox = new TextBox();
		myNewDomainNameTextBox.getElement().setId("cbNewDomainName");
		myNewDomainNameTextBox.setValue("Untitled Domain");
		newDomainPanel.add(myNewDomainNameTextBox);
		myParentsGrid2.addWidgetToRight(newDomainPanel);

		// Existing Service
		myServiceListBox = new ListBox(false);
		myServiceListBox.getElement().setId("cbSvc");
		myServiceListBox.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent theEvent) {
				handleServiceListChange();
			}
		});
		myParentsGrid2.addRow("Service", myServiceListBox);

		// New Service
		HorizontalPanel newSvcPanel = new HorizontalPanel();
		myNewServiceLabel = new HtmlLabel(" Name:", "cbNewServiceName");
		newSvcPanel.add(myNewServiceLabel);
		myNewServiceNameTextBox = new TextBox();
		myNewServiceNameTextBox.getElement().setId("cbNewServiceName");
		myNewServiceNameTextBox.setValue("Untitled Service");
		newSvcPanel.add(myNewServiceNameTextBox);
		myParentsGrid2.addWidgetToRight(newSvcPanel);

		/*
		 * Type
		 */

		addProtocolSelectionUi(myParentsGrid2);

		/*
		 * Version
		 */

		if (isAddPanel()) {
			myVersionTextBox = new TextBox();
		} else {
			myVersionTextBox = new EditableField();
		}
		myVersionTextBox.setValue("1.0");
		myVersionTextBox.addValueChangeHandler(new ValueChangeHandler<String>() {
			@Override
			public void onValueChange(ValueChangeEvent<String> theEvent) {
				if (myServiceVersion != null) {
					myServiceVersion.setId(myVersionTextBox.getValue());
				}
			}
		});
		myParentsGrid2.addRow("Version", (Widget) myVersionTextBox);

		myDescriptionEditor = new EditableField();
		myDescriptionEditor.setMultiline(true);
		myParentsGrid2.addRowDoubleWidth("Description", myDescriptionEditor);

		PButton saveButton = new PButton(AdminPortal.IMAGES.iconSave(), AdminPortal.MSGS.actions_Save());
		saveButton.getElement().getStyle().setFloat(Float.LEFT);
		saveButton.addClickHandler(new SaveClickHandler());

		myLoadingSpinner = new LoadingSpinner();

		HorizontalPanel actionButtonPanel = new HorizontalPanel();
		myContentPanel.add(actionButtonPanel);
		actionButtonPanel.add(saveButton);

		if (!isAddPanel()) {
			addActionButtons(actionButtonPanel);
		}

		actionButtonPanel.add(myLoadingSpinner);

	}

	public Long getDomainPid() {
		return myDomainPid;
	}

	public LoadingSpinner getLoadingSpinner() {
		return myLoadingSpinner;
	}

	/**
	 * @return the servicePid
	 */
	public Long getServicePid() {
		return myServicePid;
	}

	public BaseDtoServiceVersion getServiceVersion() {
		return myServiceVersion;
	}

	public Long getUncommittedSessionId() {
		return myUncommittedSessionId;
	}

	public boolean isAddPanel() {
		return this instanceof AddServiceVersionPanel;
	}

	@Override
	public void onResize() {
		GWT.log("Setting width");
		if (myBottomContents != null) {
			myBottomContents.setWidth((getOffsetWidth() - 20) + "px");
		}
	}

	public void setDomainPid(Long theDomainPid) {
		myDomainPid = theDomainPid;
	}

	public void setServicePid(Long theServicePid) {
		myServicePid = theServicePid;
	}

	public void setUncommittedSessionId(Long theUncommittedSessionId) {
		myUncommittedSessionId = theUncommittedSessionId;
	}

	private void handleDomainListChange() {
		if (myUpdating) {
			return;
		}

		if (myDomainListBox.getSelectedIndex() == 0) {
			myNewDomainLabel.setVisible(true);
			myNewDomainNameTextBox.setVisible(true);
		} else {
			myNewDomainLabel.setVisible(false);
			myNewDomainNameTextBox.setVisible(false);
		}

		String domainPidString = myDomainListBox.getValue(myDomainListBox.getSelectedIndex());
		if (StringUtil.isBlank(domainPidString)) {
			myDomainPid = null;
		} else {
			myDomainPid = Long.parseLong(domainPidString);
		}

		IAsyncLoadCallback<GServiceList> callback = new IAsyncLoadCallback<GServiceList>() {
			@Override
			public void onSuccess(GServiceList theResult) {
				processServiceList(theResult);
			}

		};

		if (myDomainPid != null) {
			Model.getInstance().loadServiceList(myDomainPid, callback);
		} else {
			processServiceList(new GServiceList());
		}
	}

	private void handleServiceListChange() {
		boolean showEdit = myServiceListBox.getSelectedIndex() == 0;

		myNewServiceLabel.setVisible(showEdit);
		myNewServiceNameTextBox.setVisible(showEdit);

		int selectedIndex = myServiceListBox.getSelectedIndex();
		if (selectedIndex != -1) {
			String pidString = myServiceListBox.getValue(selectedIndex);
			if (StringUtil.isBlank(pidString)) {
				myServicePid = null;
			} else {
				myServicePid = Long.parseLong(pidString);
			}
		}
	}

	private void processServiceList(GServiceList theResult) {
		myServiceListBox.clear();
		myServiceListBox.addItem("New...", "");
		for (GService nextService : theResult) {
			Long value = nextService.getPid();
			myServiceListBox.addItem(nextService.getName(), value.toString());
			if (value.equals(myServicePid)) {
				myServiceListBox.setSelectedIndex(myServiceListBox.getItemCount() - 1);
			}
		}

		if (myServicePid == null && myServiceListBox.getItemCount() > 1) {
			myServiceListBox.setSelectedIndex(1);
		}

		handleServiceListChange();
	}

	/**
	 * Subclasses may override
	 */
	@SuppressWarnings("unused")
	protected void addActionButtons(HorizontalPanel savePanel) {
		// nothing
	}

	protected abstract void addProtocolSelectionUi(TwoColumnGrid theGrid);

	protected BaseDetailPanel<?> getBottomContents() {
		return myBottomContents;
	}

	protected void lockParents() {
		myDomainListBox.setEnabled(false);
		myServiceListBox.setEnabled(false);
	}

	protected abstract String getDialogDescription();

	protected abstract String getDialogTitle();

	protected abstract void handleDoneSaving(AddServiceVersionResponse theResult);

	@Override
	protected void onLoad() {
		super.onLoad();
		onResize();
	}

	protected void setServiceVersion(BaseDtoServiceVersion theResult) {
		myLoadingSpinner.hide();

		myServiceVersion = theResult;
		myVersionTextBox.setValue(myServiceVersion.getId(), false);

		if (myBottomContents != null) {
			remove(myBottomContents);
		}

		switch (theResult.getProtocol()) {
		case SOAP11:
			myBottomContents = new DetailPanelSoap11(this, (DtoServiceVersionSoap11) theResult);
			break;
		case JSONRPC20:
			myBottomContents = new DetailPanelJsonRpc20(this, (DtoServiceVersionJsonRpc20) theResult);
			break;
		case HL7OVERHTTP:
			myBottomContents = new DetailPanelHl7OverHttp(this, (DtoServiceVersionHl7OverHttp) theResult);
			break;
		case VIRTUAL:
			myBottomContents = new DetailPanelVirtual(this, (DtoServiceVersionVirtual) theResult);
			break;
		}

		add(myBottomContents);

		myDescriptionEditor.setValue(myServiceVersion.getDescription());

		onResize();
	}

	void initParents(GDomainList theDomainList) {
		myUpdating = true;

		myDomainListBox.clear();
		myDomainListBox.addItem("New...", "");
		for (GDomain nextDomain : theDomainList) {
			Long value = nextDomain.getPid();
			myDomainListBox.addItem(nextDomain.getName(), value.toString());
			if (value.equals(myDomainPid)) {
				myDomainListBox.setSelectedIndex(myDomainListBox.getItemCount() - 1);
			}
		}

		if (myDomainPid == null && myDomainListBox.getItemCount() > 1) {
			myDomainListBox.setSelectedIndex(1);
		}

		myUpdating = false;

		handleDomainListChange();

	}

	public class SaveClickHandler implements ClickHandler {

		@Override
		public void onClick(ClickEvent theEvent) {
			final String domainId;
			if (myDomainListBox.getSelectedIndex() == 0) {
				domainId = myNewDomainNameTextBox.getValue();
				if (StringUtil.isBlank(domainId)) {
					myLoadingSpinner.showMessage("Please select an existing Domain, or enter a name for a new one to be created.", false);
					myNewDomainNameTextBox.setFocus(true);
					return;
				}
			} else {
				domainId = null;
			}

			String serviceId;
			if (myServiceListBox.getSelectedIndex() == 0) {
				serviceId = myNewServiceNameTextBox.getValue();
				if (StringUtil.isBlank(serviceId)) {
					myLoadingSpinner.showMessage("Please select an existing Service, or enter a name for a new one to be created.", false);
					myNewDomainNameTextBox.setFocus(true);
					return;
				}
			} else {
				serviceId = null;
			}

			if (!myBottomContents.validateValuesAndApplyIfGood()) {
				return;
			}

			myServiceVersion.setDescription(myDescriptionEditor.getValue());

			AsyncCallback<AddServiceVersionResponse> callback = new AsyncCallback<AddServiceVersionResponse>() {
				@Override
				public void onFailure(Throwable theCaught) {
					Model.handleFailure(theCaught);
				}

				@Override
				public void onSuccess(AddServiceVersionResponse theResult) {
					if (theResult.getNewDomain() != null) {
						Model.getInstance().addDomain(theResult.getNewDomain());
						myDomainPid = theResult.getNewDomain().getPid();
					}
					Model.getInstance().addOrUpdateServiceVersion(myDomainPid, myServicePid, theResult.getNewServiceVersion());
					handleDoneSaving(theResult);
				}
			};

			myLoadingSpinner.show();

			AdminPortal.MODEL_SVC.addServiceVersion(myDomainPid, domainId, myServicePid, serviceId, myServiceVersion, callback);
		}

	}
}
