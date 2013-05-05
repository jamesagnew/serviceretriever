package net.svcret.admin.client.ui.config.svcver;

import net.svcret.admin.client.AdminPortal;
import net.svcret.admin.client.nav.NavProcessor;
import net.svcret.admin.client.ui.components.HtmlLabel;
import net.svcret.admin.shared.IAsyncLoadCallback;
import net.svcret.admin.shared.Model;
import net.svcret.admin.shared.model.AddServiceVersionResponse;
import net.svcret.admin.shared.model.GDomainList;
import net.svcret.admin.shared.model.GSoap11ServiceVersion;
import net.svcret.admin.shared.model.ProtocolEnum;

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

	@Override
	protected boolean allowTypeSelect() {
		return true;
	}

	void handleTypeChange() {
		switch (ProtocolEnum.valueOf(myTypeComboBox.getValue(myTypeComboBox.getSelectedIndex()))) {
		case SOAP11:
			if (!(getBottomContents() instanceof SoapDetailPanel)) {

				myLoadingSpinner.show();
				myBottomPanel.clear();

				AsyncCallback<GSoap11ServiceVersion> callback = new AsyncCallback<GSoap11ServiceVersion>() {

					@Override
					public void onFailure(Throwable theCaught) {
						Model.handleFailure(theCaught);
					}

					@Override
					public void onSuccess(GSoap11ServiceVersion theResult) {
						setServiceVersion(theResult);
						myUncommittedSessionId = theResult.getUncommittedSessionId();
						String navToken = NavProcessor.getTokenAddServiceVersion(true, myDomainPid, myServicePid, myUncommittedSessionId);
						History.newItem(navToken, false);
					}
				};

				AdminPortal.MODEL_SVC.createNewSoap11ServiceVersion(myDomainPid, myServicePid, myUncommittedSessionId, callback);
			}
			break;
		}
	}



	@Override
	protected void addTypeSelector() {
		HtmlLabel typeLabel = new HtmlLabel("Protocol", "cbType");
		myParentsGrid.setWidget(3, 0, typeLabel);
		myTypeComboBox = new ListBox(false);
		myTypeComboBox.getElement().setId("cbType");
		myParentsGrid.setWidget(3, 1, myTypeComboBox);

		for (ProtocolEnum next : ProtocolEnum.values()) {
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
	protected void handleDoneSaving(AddServiceVersionResponse theResult) {
		String token = NavProcessor.getTokenAddServiceVersionStep2(myDomainPid, myServicePid, theResult.getNewServiceVersion().getPid());
		History.newItem(token);
	}

}
