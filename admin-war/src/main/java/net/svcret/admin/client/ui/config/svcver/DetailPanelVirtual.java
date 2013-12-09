package net.svcret.admin.client.ui.config.svcver;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;

import net.svcret.admin.client.ui.components.VersionPickerPanel;
import net.svcret.admin.client.ui.components.VersionPickerPanel.ChangeListener;
import net.svcret.admin.shared.IAsyncLoadCallback;
import net.svcret.admin.shared.Model;
import net.svcret.admin.shared.model.DtoServiceVersionVirtual;
import net.svcret.admin.shared.model.DtoDomainList;
import net.svcret.admin.shared.model.ServiceProtocolEnum;

public class DetailPanelVirtual extends BaseDetailPanel<DtoServiceVersionVirtual> {

	/**
	 * Constructor
	 */
	public DetailPanelVirtual(AbstractServiceVersionPanel theParent, DtoServiceVersionVirtual theServiceVersion) {
		super(theParent, theServiceVersion);
	}

	@Override
	protected void addProtocolSpecificPanelsToTop(boolean theIsAddPanel) {
		final FlowPanel targetPanel = new FlowPanel();
		add(targetPanel, "Target");
		
		//@formatter:off
		targetPanel.add(new Label("Virtual services are an alternate access point "
				+ "to a different service defined within the catalog."));
		//@formatter:on

		Model.getInstance().loadDomainList(new IAsyncLoadCallback<DtoDomainList>() {
			@Override
			public void onSuccess(DtoDomainList theDomainList) {
				VersionPickerPanel pickerPanel = new VersionPickerPanel(theDomainList);
				pickerPanel.setAllowSelectAll(false);
				targetPanel.add(pickerPanel);

				long svcVerPid = getServiceVersion().getTargetServiceVersionPid();
				if (svcVerPid == 0) {
					getServiceVersion().setPid(pickerPanel.getSelectedDomainPid());
				} else {
					Long domainPid = theDomainList.getDomainPidWithServiceVersion(svcVerPid);
					pickerPanel.tryToSelectDomain(domainPid);

					Long servicePid = theDomainList.getServicePidWithServiceVersion(getServiceVersion().getTargetServiceVersionPid());
					pickerPanel.tryToSelectService(servicePid);

					pickerPanel.tryToSelectServiceVersion(getServiceVersion().getTargetServiceVersionPid());
				}

				pickerPanel.addVersionChangeHandler(new ChangeListener() {
					@Override
					public void onChange(Long theDomainPid, Long theServicePid, Long theServiceVersionPid) {
						getServiceVersion().setTargetServiceVersionPid(theServiceVersionPid);
					}
				});

			}
		});
	}

	@Override
	protected boolean isIncludeUrlsTab() {
		return false;
	}

	@Override
	protected boolean isIncludeClientSecurity() {
		return false;
	}

	@Override
	public ServiceProtocolEnum getProtocol() {
		return ServiceProtocolEnum.VIRTUAL;
	}

}
