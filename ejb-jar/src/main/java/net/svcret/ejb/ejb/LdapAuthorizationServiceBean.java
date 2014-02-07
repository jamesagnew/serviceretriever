package net.svcret.ejb.ejb;

import java.util.concurrent.ConcurrentHashMap;

import javax.naming.directory.DirContext;

import net.svcret.admin.api.ProcessingException;
import net.svcret.admin.shared.enm.AuthorizationOutcomeEnum;
import net.svcret.ejb.api.IAuthorizationService.ILdapAuthorizationService;
import net.svcret.ejb.api.ICredentialGrabber;
import net.svcret.ejb.api.ISecurityService;
import net.svcret.ejb.model.entity.BasePersAuthenticationHost;
import net.svcret.ejb.model.entity.PersAuthenticationHostLdap;
import net.svcret.ejb.model.entity.PersUser;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.AuthenticatedLdapEntryContextMapper;
import org.springframework.ldap.core.LdapEntryIdentification;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.ldap.query.LdapQuery;
import org.springframework.ldap.query.LdapQueryBuilder;
import org.springframework.stereotype.Service;

@Service
public class LdapAuthorizationServiceBean extends BaseAuthorizationServiceBean<PersAuthenticationHostLdap> implements ILdapAuthorizationService {

	private ConcurrentHashMap<BasePersAuthenticationHost, MyLdapNetworkConnection> myHostToConnection = new ConcurrentHashMap<BasePersAuthenticationHost, MyLdapNetworkConnection>();

	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(LdapAuthorizationServiceBean.class);

	@Autowired
	private ISecurityService mySecuritySvc;

	@Override
	protected UserOrFailure doAuthorize(PersAuthenticationHostLdap theHost, InMemoryUserCatalog theUserCatalog, ICredentialGrabber theCredentialGrabber) throws ProcessingException {
		Validate.notNull(theHost);
		Validate.notNull(theUserCatalog);
		Validate.notNull(theCredentialGrabber);
		
		PersAuthenticationHostLdap ldapHost = theHost;

		MyLdapNetworkConnection newConnection = new MyLdapNetworkConnection(ldapHost);
		MyLdapNetworkConnection connection = myHostToConnection.putIfAbsent(theHost, newConnection);
		if (connection == null) {
			connection = newConnection;
		}

		String username = theCredentialGrabber.getUsername();
		String password = theCredentialGrabber.getPassword();

		if (ourLog.isDebugEnabled()) {
			ourLog.debug("Going to do an LDAP validation on user {} and password {}", username, StringUtils.repeat('*', password.length()));
		}

		if (StringUtils.isBlank(username)) {
			return new UserOrFailure(AuthorizationOutcomeEnum.FAILED_MISSING_USERNAME);
		}

		if (StringUtils.isBlank(password)) {
			return new UserOrFailure(AuthorizationOutcomeEnum.FAILED_MISSING_USERNAME);
		}

		PersUser user = theUserCatalog.findUser(theHost.getPid(), theCredentialGrabber.getUsername());

		if (user == null) {
			return new UserOrFailure(AuthorizationOutcomeEnum.FAILED_USER_UNKNOWN_TO_SR);
			// TODO: auto-create user if needed (maybe refactor into common
			// superclass or something?)
		}

		boolean validates = connection.validate(username, password);
		if (!validates) {
			ourLog.debug("LDAP for user {} did not validate", username);
			return new UserOrFailure(AuthorizationOutcomeEnum.FAILED_BAD_CREDENTIALS_IN_REQUEST);
		}

		ourLog.debug("LDAP for user {} validated successfully", username);

		return new UserOrFailure(user);
	}

	@Override
	protected Class<PersAuthenticationHostLdap> getConfigType() {
		return PersAuthenticationHostLdap.class;
	}

	private static class MyLdapNetworkConnection {

		private PersAuthenticationHostLdap myLdapHost;
		private String myAuthenticateFilter;

		public MyLdapNetworkConnection(PersAuthenticationHostLdap theLdapHost) {
			myLdapHost = theLdapHost;
			updateFilterFormat();
		}

		private void updateFilterFormat() {
			if (!StringUtils.equals(myAuthenticateFilter, myLdapHost.getAuthenticateFilter())) {
				myAuthenticateFilter = myLdapHost.getAuthenticateFilter();
			}
		}

		public boolean validate(String theUsername, String thePassword) throws ProcessingException {
			try {
				LdapTemplate template = getLdapTemplate();

				AuthenticatedLdapEntryContextMapper<Boolean> callback = new AuthenticatedLdapEntryContextMapper<Boolean>() {
					@Override
					public Boolean mapWithContext(DirContext theCtx, LdapEntryIdentification theLdapEntryIdentification) {
						return true;
					}
				};

				LdapQuery q = LdapQueryBuilder.query().base(myLdapHost.getAuthenticateBaseDn()).filter(myAuthenticateFilter, theUsername);
				
				ourLog.debug("Querying LDAP with filter: {}", q.toString());
				boolean authenticate = template.authenticate(q, thePassword, callback);
				ourLog.debug("LDAP authentication results: {}", authenticate);
				
				return authenticate;

			} catch (Exception e) {
				throw new ProcessingException(e);
			}
		}

		private LdapTemplate getLdapTemplate() {
			LdapContextSource src = new LdapContextSource();
			src.setUrl(myLdapHost.getUrl());
			src.setUserDn(myLdapHost.getBindUserDn());
			src.setPassword(myLdapHost.getBindPassword());
			src.setReferral("follow");
			src.afterPropertiesSet();
			return new LdapTemplate(src);
		}

	}

}
