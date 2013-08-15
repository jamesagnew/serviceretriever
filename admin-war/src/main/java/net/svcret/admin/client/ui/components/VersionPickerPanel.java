package net.svcret.admin.client.ui.components;

import java.util.ArrayList;
import java.util.List;

import net.svcret.admin.client.AdminPortal;
import net.svcret.admin.shared.model.BaseGServiceVersion;
import net.svcret.admin.shared.model.GDomain;
import net.svcret.admin.shared.model.GDomainList;
import net.svcret.admin.shared.model.GService;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.ui.ListBox;

public class VersionPickerPanel extends TwoColumnGrid {

	private ListBox myDomainBox;
	private ListBox myServiceBox;
	private ListBox myVersionBox;
	private List<ChangeListener> myVersionChangeHandlers=new ArrayList<VersionPickerPanel.ChangeListener>();
	private GDomainList myDomainList;

	public void addVersionChangeHandler(ChangeListener theHandler) {
		myVersionChangeHandlers.add(theHandler);
	}

	public VersionPickerPanel(GDomainList theDomainList) {
		initUi(theDomainList);
	}

	private void initUi(GDomainList theDomainList) {
		myDomainList = theDomainList;

		myDomainBox = new ListBox(false);
		addRow(AdminPortal.MSGS.name_Domain(), myDomainBox);

		myServiceBox = new ListBox(false);
		addRow(AdminPortal.MSGS.name_Service(), myServiceBox);

		myVersionBox = new ListBox(false);
		addRow(AdminPortal.MSGS.name_ServiceVersion(), myVersionBox);

		for (GDomain next : theDomainList) {
			myDomainBox.addItem(next.getName(), Long.toString(next.getPid()));
		}
		if (myDomainBox.getItemCount() > 0) {
			myDomainBox.setSelectedIndex(0);
		}
		handleDomainChange();

		myDomainBox.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent theEvent) {
				handleDomainChange();
			}
		});
		myServiceBox.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent theEvent) {
				handleServiceChange();
			}
		});
		myVersionBox.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent theEvent) {
				handleVersionChange();
			}
		});
	}

	private void handleDomainChange() {
		myServiceBox.clear();

		if (myDomainBox.getSelectedIndex() != -1) {
			long selectedDomainPid = Long.parseLong(myDomainBox.getValue(myDomainBox.getSelectedIndex()));
			GDomain domain = myDomainList.getDomainByPid(selectedDomainPid);
			for (GService nextService : domain.getServiceList()) {
				myServiceBox.addItem(nextService.getName(), Long.toString(nextService.getPid()));
			}
		}

		if (myServiceBox.getItemCount() > 0) {
			myServiceBox.setSelectedIndex(0);
		}

		handleServiceChange();
	}

	private void handleServiceChange() {
		myVersionBox.clear();

		if (myServiceBox.getSelectedIndex() != -1) {
			long selectedDomainPid = Long.parseLong(myDomainBox.getValue(myDomainBox.getSelectedIndex()));
			long selectedServicePid = Long.parseLong(myServiceBox.getValue(myServiceBox.getSelectedIndex()));
			GService service = myDomainList.getDomainByPid(selectedDomainPid).getServiceList().getServiceByPid(selectedServicePid);
			for (BaseGServiceVersion nextVersion : service.getVersionList()) {
				myVersionBox.addItem(nextVersion.getId(), Long.toString(nextVersion.getPid()));
			}
		}

		if (myVersionBox.getItemCount() > 0) {
			myVersionBox.setSelectedIndex(0);
		}

		handleVersionChange();
	}

	private void handleVersionChange() {
		Long selectedPid = getSelectedVersionPid();

		for (ChangeListener next : myVersionChangeHandlers) {
			next.onChange(selectedPid);
		}
	}

	public Long getSelectedVersionPid() {
		Long selectedPid = null;
		if (myVersionBox.getSelectedIndex() != -1) {
			selectedPid = Long.parseLong(myVersionBox.getValue(myVersionBox.getSelectedIndex()));
		}
		return selectedPid;
	}

	public interface ChangeListener {
		void onChange(Long theSelectedPid);
	}

	public void tryToSelectServiceVersion(long theSvcVerPid) {
		Long domainPid = myDomainList.getDomainPidWithServiceVersion(theSvcVerPid);
		Long servicePid = myDomainList.getServicePidWithServiceVersion(theSvcVerPid);
		if (domainPid!=null && servicePid!=null) {
			for (int i = 0; i < myDomainBox.getItemCount(); i++) {
				if (myDomainBox.getValue(i).equals(domainPid.toString())) {
					myDomainBox.setSelectedIndex(i);
				}
			}
			for (int i = 0; i < myServiceBox.getItemCount(); i++) {
				if (myServiceBox.getValue(i).equals(servicePid.toString())) {
					myServiceBox.setSelectedIndex(i);
				}
			}
			for (int i = 0; i < myVersionBox.getItemCount(); i++) {
				if (myVersionBox.getValue(i).equals(Long.toString(theSvcVerPid))) {
					myVersionBox.setSelectedIndex(i);
					handleVersionChange();
				}
			}
		}
		
		
	}
}
