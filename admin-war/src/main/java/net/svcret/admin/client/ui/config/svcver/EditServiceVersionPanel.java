package net.svcret.admin.client.ui.config.svcver;

import static net.svcret.admin.client.AdminPortal.*;
import net.svcret.admin.client.AdminPortal;
import net.svcret.admin.client.ui.components.TwoColumnGrid;
import net.svcret.admin.shared.IAsyncLoadCallback;
import net.svcret.admin.shared.Model;
import net.svcret.admin.shared.model.AddServiceVersionResponse;
import net.svcret.admin.shared.model.BaseGServiceVersion;
import net.svcret.admin.shared.model.GDomainList;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Label;

public class EditServiceVersionPanel extends AbstractServiceVersionPanel {

	private Label myProtocolLabel;

	public EditServiceVersionPanel(final long theServiceVersionPid) {
		super();

		getLoadingSpinner().show();
		Model.getInstance().loadDomainList(new IAsyncLoadCallback<GDomainList>() {
			@Override
			public void onSuccess(GDomainList theResult) {
				
				setDomainPid(theResult.getDomainPidWithServiceVersion(theServiceVersionPid));
				setServicePid(theResult.getServicePidWithServiceVersion(theServiceVersionPid));
				
				initParents(theResult);

				AdminPortal.MODEL_SVC.loadServiceVersionIntoSession(theServiceVersionPid, new AsyncCallback<BaseGServiceVersion>() {
					@Override
					public void onFailure(Throwable theCaught) {
						Model.handleFailure(theCaught);
					}

					@Override
					public void onSuccess(BaseGServiceVersion theServiceVersion) {
						setServiceVersion(theServiceVersion);
						getLoadingSpinner().hide();
						myProtocolLabel.setText(theServiceVersion.getProtocol().getNiceName());
					}
				});
			}
		});

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
	protected void addProtocolSelectionUi(TwoColumnGrid theGrid) {
		myProtocolLabel = new Label();
		theGrid.addRow("Protocol", myProtocolLabel);
	}

}
