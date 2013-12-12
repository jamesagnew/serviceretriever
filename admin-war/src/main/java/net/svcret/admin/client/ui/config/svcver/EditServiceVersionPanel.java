package net.svcret.admin.client.ui.config.svcver;

import static net.svcret.admin.client.AdminPortal.MSGS;
import net.svcret.admin.client.AdminPortal;
import net.svcret.admin.client.nav.NavProcessor;
import net.svcret.admin.client.ui.components.PButton;
import net.svcret.admin.client.ui.components.TwoColumnGrid;
import net.svcret.admin.shared.AddServiceVersionResponse;
import net.svcret.admin.shared.IAsyncLoadCallback;
import net.svcret.admin.shared.Model;
import net.svcret.admin.shared.model.BaseDtoServiceVersion;
import net.svcret.admin.shared.model.DtoDomainList;
import net.svcret.admin.shared.model.HierarchyEnum;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;

public class EditServiceVersionPanel extends AbstractServiceVersionPanel {

	private Label myProtocolLabel;

	public EditServiceVersionPanel(final long theServiceVersionPid) {
		super();
		
		getLoadingSpinner().show();
		Model.getInstance().loadDomainList(new IAsyncLoadCallback<DtoDomainList>() {
			@Override
			public void onSuccess(DtoDomainList theResult) {

				setDomainPid(theResult.getDomainPidWithServiceVersion(theServiceVersionPid));
				setServicePid(theResult.getServicePidWithServiceVersion(theServiceVersionPid));
				
				initParents(theResult);
				lockParents();

				AdminPortal.MODEL_SVC.loadServiceVersionIntoSession(theServiceVersionPid, new AsyncCallback<BaseDtoServiceVersion>() {
					@Override
					public void onFailure(Throwable theCaught) {
						Model.handleFailure(theCaught);
					}

					@Override
					public void onSuccess(BaseDtoServiceVersion theServiceVersion) {
						setServiceVersion(theServiceVersion);
						getLoadingSpinner().hide();
						myProtocolLabel.setText(theServiceVersion.getProtocol().getNiceName());
					}
				});
			}
		});

	}

	@Override
	protected boolean isEditPanel() {
		return true;
	}

	@Override
	protected String getDialogTitle() {
		return MSGS.editServiceVersion_Title();
	}

	@Override
	protected String getDialogDescription() {
		return MSGS.editServiceVersion_Description();
	}


	@Override
	protected void handleDoneSaving(AddServiceVersionResponse theResult) {
		getLoadingSpinner().showMessage("Saved service version", false);
	}

	@Override
	protected void addActionButtons(HorizontalPanel savePanel) {
		PButton testButton = new PButton(AdminPortal.IMAGES.iconTest16(), "Test Service");
		testButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent theEvent) {
				History.newItem(NavProcessor.getTokenTestServiceVersion(getServiceVersion().getPid()));
			}
		});
		savePanel.add(testButton);

		// TODO: better icon
		PButton transactionsButton = new PButton(AdminPortal.IMAGES.iconTransactions(), AdminPortal.MSGS.actions_RecentTransactions());
		transactionsButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent theEvent) {
				History.newItem(NavProcessor.getTokenServiceVersionRecentMessages(getServiceVersion().getPid(),false));
			}
		});
		savePanel.add(transactionsButton);

		PButton msgLibButton = new PButton(AdminPortal.IMAGES.iconLibrary(), AdminPortal.MSGS.actions_MessageLibrary());
		msgLibButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent theEvent) {
				History.newItem(NavProcessor.getTokenMessageLibrary(HierarchyEnum.VERSION, getServiceVersion().getPid()));
			}
		});
		savePanel.add(msgLibButton);

		PButton statsButton = new PButton(AdminPortal.IMAGES.iconStatus(), AdminPortal.MSGS.actions_Stats());
		statsButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent theEvent) {
				History.newItem(NavProcessor.getTokenServiceVersionStats(getServiceVersion().getPid()));
			}
		});
		savePanel.add(statsButton);
		
		PButton cloneButton = new PButton(AdminPortal.IMAGES.iconClone(), AdminPortal.MSGS.actions_Clone());
		cloneButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent theEvent) {
				History.newItem(NavProcessor.getTokenCloneServiceVersion(getServiceVersion().getPid()));
			}
		});
		savePanel.add(cloneButton);
	}

	@Override
	protected void addProtocolSelectionUi(TwoColumnGrid theGrid) {
		myProtocolLabel = new Label();
		theGrid.addRow("Protocol", myProtocolLabel);
	}

}
