package net.svcret.admin.client.ui.config.lib;

import net.svcret.admin.shared.IAsyncLoadCallback;
import net.svcret.admin.shared.Model;
import net.svcret.admin.shared.model.BaseDtoServiceVersion;
import net.svcret.admin.shared.model.DtoLibraryMessage;
import net.svcret.admin.shared.model.GDomainList;
import net.svcret.admin.shared.model.HierarchyEnum;

public class CreateNewLibraryMessagePanel extends BaseEditLibraryMessagePanel {

	public CreateNewLibraryMessagePanel() {
		this(null, 0);
	}

	public CreateNewLibraryMessagePanel(final HierarchyEnum theType, final long thePid) {
		Model.getInstance().loadDomainList(new IAsyncLoadCallback<GDomainList>() {

			@Override
			public void onSuccess(GDomainList theDomainList) {
				DtoLibraryMessage message = new DtoLibraryMessage();
				if (theType == null) {
					BaseDtoServiceVersion svcVer = theDomainList.getFirstServiceVersion();
					if (svcVer != null) {
						message.setAppliesToServiceVersionPids(svcVer.getPid());
					}
				} else {
					switch (theType) {
					case DOMAIN:
						BaseDtoServiceVersion firstServiceVersion = theDomainList.getDomainByPid(thePid).getServiceList().getFirstServiceVersion();
						if (firstServiceVersion != null) {
							message.setAppliesToServiceVersionPids(firstServiceVersion.getPid());
						}
						break;
					case METHOD:
						throw new IllegalArgumentException();
					case SERVICE:
						firstServiceVersion = theDomainList.getDomainByPid(theDomainList.getDomainPidWithService(thePid)).getServiceList().getServiceByPid(thePid).getVersionList().getFirstServiceVersion();
						if (firstServiceVersion != null) {
							message.setAppliesToServiceVersionPids(firstServiceVersion.getPid());
						}
						break;
					case VERSION:
						message.setAppliesToServiceVersionPids(thePid);
						break;
					}
				}

				setContents(message);
				
			}
		});
	}

	@Override
	protected String getDialogTitle() {
		return "Create new Library Message";
	}

}
