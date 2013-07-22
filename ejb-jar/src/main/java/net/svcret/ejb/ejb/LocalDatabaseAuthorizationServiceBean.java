package net.svcret.ejb.ejb;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import net.svcret.ejb.api.IAuthorizationService.ILocalDatabaseAuthorizationService;
import net.svcret.ejb.api.ICredentialGrabber;
import net.svcret.ejb.api.IDao;
import net.svcret.ejb.ex.ProcessingException;
import net.svcret.ejb.model.entity.IThrottleable;
import net.svcret.ejb.model.entity.PersAuthenticationHostLocalDatabase;
import net.svcret.ejb.model.entity.PersUser;

@Stateless
public class LocalDatabaseAuthorizationServiceBean extends BaseAuthorizationServiceBean<PersAuthenticationHostLocalDatabase> implements ILocalDatabaseAuthorizationService {

	@Override
	protected IThrottleable doAuthorize(PersAuthenticationHostLocalDatabase theHost, InMemoryUserCatalog theUserCatalog, ICredentialGrabber theCredentialGrabber) throws ProcessingException {
		PersUser user = theUserCatalog.findUser(theHost.getPid(), theCredentialGrabber.getUsername());
		if (user == null) {
			return null;
		}
		if (!user.checkPassword(theCredentialGrabber.getPassword())) {
			return null;
		}
		
		return user;
	}

	@Override
	protected Class<PersAuthenticationHostLocalDatabase> getConfigType() {
		return PersAuthenticationHostLocalDatabase.class;
	}

	@Override
	protected boolean shouldCache() {
		return false;
	}

	@EJB
	private IDao myDao;

	/**
	 * FOR UNIT TESTS ONLY
	 */
	void setDao(IDao theDao) {
		myDao = theDao;
	}
}
