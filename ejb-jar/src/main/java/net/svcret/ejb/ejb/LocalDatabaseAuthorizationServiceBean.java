package net.svcret.ejb.ejb;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import net.svcret.admin.api.ProcessingException;
import net.svcret.admin.shared.enm.AuthorizationOutcomeEnum;
import net.svcret.ejb.api.IAuthorizationService.ILocalDatabaseAuthorizationService;
import net.svcret.ejb.api.ICredentialGrabber;
import net.svcret.ejb.api.IDao;
import net.svcret.ejb.model.entity.PersAuthenticationHostLocalDatabase;
import net.svcret.ejb.model.entity.PersUser;

@Stateless
public class LocalDatabaseAuthorizationServiceBean extends BaseAuthorizationServiceBean<PersAuthenticationHostLocalDatabase> implements ILocalDatabaseAuthorizationService {

	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(LocalDatabaseAuthorizationServiceBean.class);
	
	@Override
	protected UserOrFailure doAuthorize(PersAuthenticationHostLocalDatabase theHost, InMemoryUserCatalog theUserCatalog, ICredentialGrabber theCredentialGrabber) throws ProcessingException {
		PersUser user = theUserCatalog.findUser(theHost.getPid(), theCredentialGrabber.getUsername());
		if (user == null) {
			return new UserOrFailure(AuthorizationOutcomeEnum.FAILED_USER_UNKNOWN_TO_SR);
		}
		if (!user.checkPassword(theCredentialGrabber.getPassword())) {
			ourLog.debug("Password does not match");
			return new UserOrFailure(AuthorizationOutcomeEnum.FAILED_BAD_CREDENTIALS_IN_REQUEST);
		}
		
		return new UserOrFailure(user);
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
