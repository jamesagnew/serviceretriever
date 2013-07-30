package net.svcret.admin.client.ui.test;

import net.svcret.admin.client.AdminPortal;
import net.svcret.admin.client.ui.components.CssConstants;
import net.svcret.admin.client.ui.components.LoadingSpinner;
import net.svcret.admin.client.ui.components.PButton;
import net.svcret.admin.client.ui.components.TwoColumnGrid;
import net.svcret.admin.shared.IAsyncLoadCallback;
import net.svcret.admin.shared.Model;
import net.svcret.admin.shared.model.BaseGServiceVersion;
import net.svcret.admin.shared.model.GDomain;
import net.svcret.admin.shared.model.GDomainList;
import net.svcret.admin.shared.model.GService;
import net.svcret.admin.shared.model.GServiceVersionSingleFireResponse;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextArea;

public class ServiceVersionTestPanel extends FlowPanel {

	private PButton mySendButton;
	private Long myPid;
	private ServiceVersionTestResponsePanel myResponsePanel;
	private TextArea mySendMessageTextArea;
	private LoadingSpinner mySendSpinner;
	private boolean mySelectServiceVersion;
	private ListBox domainBox;
	private ListBox serviceBox;
	private ListBox versionBox;
	private GDomainList myDomainList;

	public ServiceVersionTestPanel() {
		this(null);
	}

	public ServiceVersionTestPanel(Long theServiceVersionPid) {
		mySelectServiceVersion = theServiceVersionPid == null;
		myPid = theServiceVersionPid;

		final FlowPanel mainPanel = new FlowPanel();
		mainPanel.setStylePrimaryName(CssConstants.MAIN_PANEL);
		add(mainPanel);

		Label titleLabel = new Label("Service Version Tester");
		titleLabel.setStyleName(CssConstants.MAIN_PANEL_TITLE);
		mainPanel.add(titleLabel);

		final FlowPanel contentPanel = new FlowPanel();
		contentPanel.addStyleName(CssConstants.CONTENT_INNER_PANEL);
		mainPanel.add(contentPanel);

		final LoadingSpinner initSpinner = new LoadingSpinner();
		contentPanel.add(initSpinner);
		initSpinner.show();

		myResponsePanel = new ServiceVersionTestResponsePanel();
		myResponsePanel.setVisible(false);
		add(myResponsePanel);

		Model.getInstance().loadDomainList(new IAsyncLoadCallback<GDomainList>() {

			@Override
			public void onSuccess(final GDomainList theDomainList) {
				myDomainList = theDomainList;
				initSpinner.hideCompletely();

				TwoColumnGrid grid = new TwoColumnGrid();
				grid.setWidth("100%");
				grid.setMaximizeSecondColumn();
				contentPanel.add(grid);

				if (mySelectServiceVersion) {
					domainBox = new ListBox(false);
					grid.addRow("Domain", domainBox);
					serviceBox = new ListBox(false);
					grid.addRow("Service", serviceBox);
					versionBox = new ListBox(false);
					grid.addRow("Version", versionBox);
					domainBox.addChangeHandler(new ChangeHandler() {
						@Override
						public void onChange(ChangeEvent theEvent) {
							handleDomainBoxChange();
						}
					});
					serviceBox.addChangeHandler(new ChangeHandler() {
						@Override
						public void onChange(ChangeEvent theEvent) {
							handleServiceBoxChange();
						}
					});
					versionBox.addChangeHandler(new ChangeHandler() {
						@Override
						public void onChange(ChangeEvent theEvent) {
							handleVersionBoxChange();
						}
					});
				} else {
					BaseGServiceVersion svcVer = theDomainList.getServiceVersionByPid(myPid);
					grid.addRow("Service", svcVer.getParentServiceName());
					grid.addRow("Version", svcVer.getId());
				}

				mySendMessageTextArea = new TextArea();
				mySendMessageTextArea.setHeight("200px");
				mySendMessageTextArea.setWidth("100%");
				grid.addRow("Message", mySendMessageTextArea);
				grid.addDescription("Enter the raw message to send here");

				HorizontalPanel controlsPanel = new HorizontalPanel();
				contentPanel.add(controlsPanel);

				mySendButton = new PButton("Send");
				mySendButton.addClickHandler(new ClickHandler() {
					@Override
					public void onClick(ClickEvent theEvent) {
						send();
					}
				});
				controlsPanel.add(mySendButton);
				if (mySelectServiceVersion) {
					for (GDomain nextDomain : theDomainList) {
						domainBox.addItem(nextDomain.getName(), Long.toString(nextDomain.getPid()));
					}
					handleDomainBoxChange();
				}

				mySendSpinner = new LoadingSpinner("Sending Message...");
				mySendSpinner.hide();
				controlsPanel.add(mySendSpinner);

			}
		});

	}

	private void send() {
		mySendSpinner.show();
		myResponsePanel.setVisible(false);

		String messageText = mySendMessageTextArea.getText();
		AdminPortal.MODEL_SVC.testServiceVersionWithSingleMessage(messageText, myPid, new AsyncCallback<GServiceVersionSingleFireResponse>() {

			@Override
			public void onFailure(Throwable theCaught) {
				mySendSpinner.showMessage(theCaught.getMessage(), false);
			}

			@Override
			public void onSuccess(GServiceVersionSingleFireResponse theResult) {
				mySendSpinner.hide();
				myResponsePanel.setVisible(true);
				myResponsePanel.setMessage(theResult);
			}
		});

	}

	private void handleServiceBoxChange() {
		versionBox.clear();
		if (serviceBox.getSelectedIndex() != -1) {
			GDomain domain = myDomainList.getDomainByPid(Long.parseLong(domainBox.getValue(domainBox.getSelectedIndex())));
			GService service = domain.getServiceList().getServiceByPid(Long.parseLong(serviceBox.getValue(serviceBox.getSelectedIndex())));
			for (BaseGServiceVersion nextVer : service.getVersionList()) {
				versionBox.addItem(nextVer.getId(), Long.toString(nextVer.getPid()));
			}
		}

		handleVersionBoxChange();
	}

	private void handleVersionBoxChange() {
		if (versionBox.getSelectedIndex() == -1) {
			myPid = null;
			mySendButton.setEnabled(false);
		} else {
			myPid = Long.parseLong(versionBox.getValue(versionBox.getSelectedIndex()));
			mySendButton.setEnabled(true);
		}
	}

	private void handleDomainBoxChange() {
		serviceBox.clear();
		if (domainBox.getSelectedIndex() != -1) {
			GDomain domain = myDomainList.getDomainByPid(Long.parseLong(domainBox.getValue(domainBox.getSelectedIndex())));
			for (GService nextSvc : domain.getServiceList()) {
				serviceBox.addItem(nextSvc.getName(), Long.toString(nextSvc.getPid()));
			}
		}
		handleServiceBoxChange();
	}

}
