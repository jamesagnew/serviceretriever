package net.svcret.admin.client.ui.config.svcver;

import net.svcret.admin.client.AdminPortal;
import net.svcret.admin.client.nav.NavProcessor;
import net.svcret.admin.client.ui.components.TwoColumnGrid;
import net.svcret.admin.shared.AddServiceVersionResponse;
import net.svcret.admin.shared.IAsyncLoadCallback;
import net.svcret.admin.shared.Model;
import net.svcret.admin.shared.model.BaseDtoServiceVersion;
import net.svcret.admin.shared.model.DtoDomainList;

import com.google.gwt.user.client.History;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Label;

public class CloneServiceVersionPanel extends AbstractServiceVersionPanel {

	private Label myProtocolLabel;

	public CloneServiceVersionPanel(final long thePidToClone) {
		getLoadingSpinner().show();

		Model.getInstance().loadDomainList(new IAsyncLoadCallback<DtoDomainList>() {
			@Override
			public void onSuccess(DtoDomainList theResult) {
				initParents(theResult);
				setDomainPid(theResult.getDomainPidWithServiceVersion(thePidToClone));
				setServicePid(theResult.getServicePidWithServiceVersion(thePidToClone));
				AdminPortal.MODEL_SVC.cloneServiceVersion(thePidToClone, new AsyncCallback<BaseDtoServiceVersion>() {
					@Override
					public void onFailure(Throwable theCaught) {
						Model.handleFailure(theCaught);
					}
					
					@Override
					public void onSuccess(BaseDtoServiceVersion theServiceVersion) {
						setServiceVersion(theServiceVersion);
						setUncommittedSessionId(theServiceVersion.getUncommittedSessionId());
						myProtocolLabel.setText(theServiceVersion.getProtocol().getNiceName());
						getLoadingSpinner().hide();
					}
				});
			}
		});
		

	}

	@Override
	protected void addProtocolSelectionUi(TwoColumnGrid theGrid) {
		myProtocolLabel = new Label();
		theGrid.addRow("Protocol", myProtocolLabel);
	}

	@Override
	protected String getDialogDescription() {
		return "Cloning creates a copy of an existing version of a service (including all "
				+ "configuration). Generally this would be done in order to add a new version which "
				+ "works similarly to an existing version. The version can be added to another "
				+ "service though, in order to reuse similar configuration.";
	}

	@Override
	protected String getDialogTitle() {
		return AdminPortal.MSGS.cloneServiceVersion_Title();
	}

	@Override
	protected void handleDoneSaving(AddServiceVersionResponse theResult) {
		History.newItem(NavProcessor.getTokenAddServiceVersionStep2(theResult.getNewServiceVersion().getPid()));
	}

}
