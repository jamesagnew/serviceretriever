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

	private ListBox myTypeComboBox;

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

	@Override
	protected String getDialogDescription() {
		return AdminPortal.MSGS.addServiceVersion_Description();
	}

	@Override
	protected String getDialogTitle() {
		return "Add Service Version";
	}

	@Override
	protected void handleDoneSaving(AddServiceVersionResponse theResult) {
		String token = NavProcessor.getTokenAddServiceVersionStep2(getDomainPid(), getServicePid(), theResult.getNewServiceVersion().getPid());
		History.newItem(token);
	}

	void handleTypeChange() {
		ServiceProtocolEnum protocol = ServiceProtocolEnum.valueOf(myTypeComboBox.getValue(myTypeComboBox.getSelectedIndex()));
		if (getBottomContents() == null || getBottomContents().getProtocol() != protocol) {

			getLoadingSpinner().show();

			AsyncCallback<BaseGServiceVersion> callback = new AsyncCallback<BaseGServiceVersion>() {

				@Override
				public void onFailure(Throwable theCaught) {
					Model.handleFailure(theCaught);
				}

				@Override
				public void onSuccess(BaseGServiceVersion theResult) {
					setServiceVersion(theResult);
					setUncommittedSessionId(theResult.getUncommittedSessionId());
					String navToken = NavProcessor.getTokenAddServiceVersion(true, getDomainPid(), getServicePid(), getUncommittedSessionId());
					History.newItem(navToken, false);
				}
			};

			AdminPortal.MODEL_SVC.createNewServiceVersion(protocol, getDomainPid(), getServicePid(), getUncommittedSessionId(), callback);

		}

	}

}
