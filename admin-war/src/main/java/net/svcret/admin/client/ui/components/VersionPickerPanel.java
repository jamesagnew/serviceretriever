package net.svcret.admin.client.ui.components;

import java.util.ArrayList;
import java.util.List;

import net.svcret.admin.client.AdminPortal;
import net.svcret.admin.shared.ObjectUtil;
import net.svcret.admin.shared.model.BaseDtoServiceVersion;
import net.svcret.admin.shared.model.DtoDomain;
import net.svcret.admin.shared.model.GDomainList;
import net.svcret.admin.shared.model.GService;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.ui.ListBox;

public class VersionPickerPanel extends TwoColumnGrid {

	public void setAllowSelectAll(boolean theAllowSelectAll) {
		myAllowSelectAll = theAllowSelectAll;
	}

	public static final long ALL_PID = -1;
	private static final String ALL_PID_STR = "-1";
	private boolean myAllowSelectAll;
	private ListBox myDomainBox;
	private GDomainList myDomainList;
	private ListBox myServiceBox;
	private ListBox myVersionBox;
	private List<ChangeListener> myVersionChangeHandlers = new ArrayList<VersionPickerPanel.ChangeListener>();
	private Long myLastSelectedDomainPid;
	private Long myLastSelectedServicePid;
	private Long myLastSelectedSvcVerPid;

	public VersionPickerPanel(GDomainList theDomainList) {
		initUi(theDomainList, false);
	}

	public VersionPickerPanel(GDomainList theDomainList, boolean theAllowSelectAll) {
		initUi(theDomainList, theAllowSelectAll);
	}

	public void addVersionChangeHandler(ChangeListener theHandler) {
		myVersionChangeHandlers.add(theHandler);
	}

	public long getSelectedDomainPid() {
		long selectedDomainPid = Long.parseLong(myDomainBox.getValue(myDomainBox.getSelectedIndex()));
		return selectedDomainPid;
	}

	public Long getSelectedServicePid() {
		if (myServiceBox.getSelectedIndex()==-1) {
			return null;
		}
		long selectedServicePid = Long.parseLong(myServiceBox.getValue(myServiceBox.getSelectedIndex()));
		return selectedServicePid;
	}

	public Long getSelectedVersionPid() {
		Long selectedPid = null;
		if (myVersionBox.getSelectedIndex() != -1) {
			selectedPid = Long.parseLong(myVersionBox.getValue(myVersionBox.getSelectedIndex()));
		}
		return selectedPid;
	}

	public boolean tryToSelectDomain(long domainPid) {
		for (int i = 0; i < myDomainBox.getItemCount(); i++) {
			if (myDomainBox.getValue(i).equals(Long.toString(domainPid))) {
				myDomainBox.setSelectedIndex(i);
				handleDomainChange();
				return true;
			}
		}
		return false;
	}

	public boolean tryToSelectService(Long theServicePid) {
		Long domainPid = myDomainList.getDomainPidWithService(theServicePid);
		if (domainPid == null) {
			return false;
		}
		boolean foundDomain = tryToSelectDomain(domainPid);
		if (!foundDomain) {
			return false;
		}

		for (int i = 0; i < myServiceBox.getItemCount(); i++) {
			if (myServiceBox.getValue(i).equals(theServicePid.toString())) {
				myServiceBox.setSelectedIndex(i);
				handleServiceChange();
				return true;
			}
		}

		return false;
	}

	public boolean tryToSelectServiceVersion(long theSvcVerPid) {
		Long servicePid = myDomainList.getServicePidWithServiceVersion(theSvcVerPid);
		if (servicePid == null) {
			return false;
		}

		boolean foundService = tryToSelectService(servicePid);
		if (!foundService) {
			return false;
		}

		for (int i = 0; i < myVersionBox.getItemCount(); i++) {
			if (myVersionBox.getValue(i).equals(Long.toString(theSvcVerPid))) {
				myVersionBox.setSelectedIndex(i);
				handleVersionChange();
				return true;
			}
		}

		return false;
	}

	private void handleDomainChange() {
		myServiceBox.clear();

		if (myDomainBox.getSelectedIndex() != -1) {
			long selectedDomainPid = getSelectedDomainPid();
			if (myAllowSelectAll && selectedDomainPid == ALL_PID) {
				myServiceBox.setEnabled(false);
				myVersionBox.setEnabled(false);
				handleVersionChange();
				return;
			}

			myServiceBox.setEnabled(true);

			DtoDomain domain = myDomainList.getDomainByPid(selectedDomainPid);

			if (myAllowSelectAll) {
				myServiceBox.addItem("All Services", ALL_PID_STR);
			}
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
			long selectedDomainPid = getSelectedDomainPid();
			long selectedServicePid = getSelectedServicePid();
			if (myAllowSelectAll && selectedServicePid == ALL_PID) {
				myVersionBox.setEnabled(false);
				handleVersionChange();
				return;
			}

			myVersionBox.setEnabled(true);

			GService service = myDomainList.getDomainByPid(selectedDomainPid).getServiceList().getServiceByPid(selectedServicePid);

			if (myAllowSelectAll) {
				myVersionBox.addItem("All Versions", ALL_PID_STR);
			}
			for (BaseDtoServiceVersion nextVersion : service.getVersionList()) {
				myVersionBox.addItem(nextVersion.getId(), Long.toString(nextVersion.getPid()));
			}
		}

		if (myVersionBox.getItemCount() > 0) {
			myVersionBox.setSelectedIndex(0);
		}

		handleVersionChange();
	}

	
	
	private void handleVersionChange() {
		Long selectedDomainPid = getSelectedDomainPid();
		Long selectedServicePid = selectedDomainPid != null ? getSelectedServicePid() : null;
		Long selectedSvcVerPid = selectedServicePid != null ? getSelectedVersionPid() : null;

		if (ObjectUtil.equals(myLastSelectedDomainPid, selectedDomainPid)) {
			if (ObjectUtil.equals(myLastSelectedServicePid, selectedServicePid)) {
				if (ObjectUtil.equals(myLastSelectedSvcVerPid, selectedSvcVerPid)) {
					return;
				}
			}
		}
		
		for (ChangeListener next : myVersionChangeHandlers) {
			next.onChange(selectedDomainPid, selectedServicePid, selectedSvcVerPid);
		}
		
		myLastSelectedDomainPid=selectedDomainPid;
		myLastSelectedServicePid=selectedServicePid;
		myLastSelectedSvcVerPid=selectedSvcVerPid;
	}

	private void initUi(GDomainList theDomainList, boolean theAllowSelectAll) {
		myDomainList = theDomainList;
		myAllowSelectAll = theAllowSelectAll;

		myDomainBox = new ListBox(false);
		addRow(AdminPortal.MSGS.name_Domain(), myDomainBox);

		myServiceBox = new ListBox(false);
		addRow(AdminPortal.MSGS.name_Service(), myServiceBox);

		myVersionBox = new ListBox(false);
		addRow(AdminPortal.MSGS.name_ServiceVersion(), myVersionBox);

		if (myAllowSelectAll) {
			myDomainBox.addItem("All Domains", ALL_PID_STR);
		}
		for (DtoDomain next : theDomainList) {
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

	public interface ChangeListener {
		void onChange(Long theDomainPid, Long theServicePid, Long theServiceVersionPid);
	}
}
