package net.svcret.admin.client.ui.config.svcver;

import net.svcret.admin.client.AdminPortal;
import net.svcret.admin.client.nav.NavProcessor;
import net.svcret.admin.client.ui.components.TwoColumnGrid;
import net.svcret.admin.shared.IAsyncLoadCallback;
import net.svcret.admin.shared.Model;
import net.svcret.admin.shared.model.AddServiceVersionResponse;
import net.svcret.admin.shared.model.BaseGServiceVersion;
import net.svcret.admin.shared.model.GDomainList;
import net.svcret.admin.shared.model.ServiceProtocolEnum;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ListBox;

public class AddServiceVersionPanel extends AbstractServiceVersionPanel {

	public AddServiceVersionPanel(Long theDomainPid, Long theServicePid, Long theUncommittedSessionId) {
		super(theDomainPid, theServicePid, theUncommittedSessionId);

		Model.getInstance().loadDomainList(new IAsyncLoadCallback<GDomainList>() {
			@Override
			public void onSuccess(GDomainList theResult) {
				initParents(theResult);
				handleTypeChange();
			}
		});
	}

	@Override
	protected String getDialogTitle() {
		return "Add Service Version";
	}

	@Override
	protected String getDialogDescription() {
		return AdminPortal.MSGS.addServiceVersion_Description();
	}


	void handleTypeChange() {
		ServiceProtocolEnum protocol = ServiceProtocolEnum.valueOf(myTypeComboBox.getValue(myTypeComboBox.getSelectedIndex()));
		if (getBottomContents() == null || getBottomContents().getProtocol() != protocol) {

			myLoadingSpinner.show();
			myBottomPanel.clear();

			AsyncCallback<BaseGServiceVersion> callback = new AsyncCallback<BaseGServiceVersion>() {

				@Override
				public void onFailure(Throwable theCaught) {
					Model.handleFailure(theCaught);
				}

				@Override
				public void onSuccess(BaseGServiceVersion theResult) {
					setServiceVersion(theResult);
					myUncommittedSessionId = theResult.getUncommittedSessionId();
					String navToken = NavProcessor.getTokenAddServiceVersion(true, myDomainPid, myServicePid, myUncommittedSessionId);
					History.newItem(navToken, false);
				}
			};

			AdminPortal.MODEL_SVC.createNewServiceVersion(protocol, myDomainPid, myServicePid, myUncommittedSessionId, callback);

		}

	}

	@Override
	protected void handleDoneSaving(AddServiceVersionResponse theResult) {
		String token = NavProcessor.getTokenAddServiceVersionStep2(myDomainPid, myServicePid, theResult.getNewServiceVersion().getPid());
		History.newItem(token);
	}

	@Override
	protected void addProtocolSelectionUi(TwoColumnGrid theGrid) {
		myTypeComboBox = new ListBox(false);
		theGrid.addRow("Protocol", myTypeComboBox);
		for (ServiceProtocolEnum next : ServiceProtocolEnum.getNaturalOrder()) {
			myTypeComboBox.addItem(next.getNiceName(), next.name());
			myTypeComboBox.addChangeHandler(new ChangeHandler() {
				@Override
				public void onChange(ChangeEvent theEvent) {
					handleTypeChange();
				}
			});
		}
	}

}
