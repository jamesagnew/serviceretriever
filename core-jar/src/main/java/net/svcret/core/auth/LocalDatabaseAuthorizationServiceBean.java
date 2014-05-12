package net.svcret.core.auth;

import net.svcret.admin.api.ProcessingException;
import net.svcret.admin.shared.enm.AuthorizationOutcomeEnum;
import net.svcret.core.api.ICredentialGrabber;
import net.svcret.core.api.IDao;
import net.svcret.core.api.IAuthorizationService.ILocalDatabaseAuthorizationService;
import net.svcret.core.ejb.InMemoryUserCatalog;
import net.svcret.core.model.entity.PersAuthenticationHostLocalDatabase;
import net.svcret.core.model.entity.PersUser;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.annotations.VisibleForTesting;

@Service
public class LocalDatabaseAuthorizationServiceBean extends BaseAuthorizationServiceBean<PersAuthenticationHostLocalDatabase> implements ILocalDatabaseAuthorizationService {

	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(LocalDatabaseAuthorizationServiceBean.class);

	@Autowired
	private IDao myDao;

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

	/**
	 * FOR UNIT TESTS ONLY
	 */
	@VisibleForTesting
	public void setDao(IDao theDao) {
		myDao = theDao;
	}
}
