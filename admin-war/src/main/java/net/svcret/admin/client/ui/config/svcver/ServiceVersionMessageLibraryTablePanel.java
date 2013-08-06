package net.svcret.admin.client.ui.config.svcver;

import java.util.Collection;
import java.util.Set;

import net.svcret.admin.client.ui.config.lib.BaseMessageLibraryTablePanel;
import net.svcret.admin.shared.model.BaseGServiceVersion;
import net.svcret.admin.shared.model.DtoLibraryMessage;
import net.svcret.admin.shared.model.GDomainList;

public class ServiceVersionMessageLibraryTablePanel extends BaseMessageLibraryTablePanel {

	private BaseGServiceVersion myServiceVersion;

	public ServiceVersionMessageLibraryTablePanel(BaseGServiceVersion theServiceVersion, GDomainList theDomainList, Collection<DtoLibraryMessage> theMessages) {
		super(theDomainList);
		
		myServiceVersion = theServiceVersion;
		setMessages(theMessages);
	}

	@Override
	protected long toSvcVerPid(Set<Long> theAppliesToServiceVersionPids) {
		return myServiceVersion.getPid();
	}

}
