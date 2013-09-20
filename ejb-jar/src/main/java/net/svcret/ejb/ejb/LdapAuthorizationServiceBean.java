package net.svcret.ejb.ejb;

import java.text.MessageFormat;
import java.util.concurrent.ConcurrentHashMap;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.naming.directory.DirContext;

import net.svcret.admin.shared.model.AuthorizationOutcomeEnum;
import net.svcret.ejb.api.IAuthorizationService.ILdapAuthorizationService;
import net.svcret.ejb.api.ICredentialGrabber;
import net.svcret.ejb.api.ISecurityService;
import net.svcret.ejb.ex.ProcessingException;
import net.svcret.ejb.model.entity.BasePersAuthenticationHost;
import net.svcret.ejb.model.entity.PersAuthenticationHostLdap;
import net.svcret.ejb.model.entity.PersUser;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.springframework.ldap.core.AuthenticatedLdapEntryContextCallback;
import org.springframework.ldap.core.LdapEncoder;
import org.springframework.ldap.core.LdapEntryIdentification;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;

@Stateless
public class LdapAuthorizationServiceBean extends BaseAuthorizationServiceBean<PersAuthenticationHostLdap> implements ILdapAuthorizationService {

	private ConcurrentHashMap<BasePersAuthenticationHost, MyLdapNetworkConnection> myHostToConnection = new ConcurrentHashMap<BasePersAuthenticationHost, MyLdapNetworkConnection>();

	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(LdapAuthorizationServiceBean.class);

	@EJB
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
		private MessageFormat myFilterFormat;
		private String myAuthenticateFilter;

		public MyLdapNetworkConnection(PersAuthenticationHostLdap theLdapHost) {
			myLdapHost = theLdapHost;
			updateFilterFormat();
		}

		private void updateFilterFormat() {
			if (!StringUtils.equals(myAuthenticateFilter, myLdapHost.getAuthenticateFilter())) {
				myAuthenticateFilter = myLdapHost.getAuthenticateFilter();
				myFilterFormat = new MessageFormat(myAuthenticateFilter);
			}
		}

		public boolean validate(String theUsername, String thePassword) throws ProcessingException {
			try {
				LdapTemplate template = getLdapTemplate();

				AuthenticatedLdapEntryContextCallback callback = new AuthenticatedLdapEntryContextCallback() {

					@Override
					public void executeWithContext(DirContext theDc, LdapEntryIdentification theId) {
						// try {
						// NamingEnumeration<NameClassPair> list =
						// theDc.list(theId.getAbsoluteDn());
						// while (list.hasMore()) {
						// NameClassPair next = list.next();
						// System.out.println("nexT: " + next.getName());
						// }
						//
						// Attributes attrList =
						// theDc.getAttributes(theId.getAbsoluteDn(), new
						// String[]{"memberOf"});
						// NamingEnumeration<? extends Attribute> attrEnum =
						// attrList.getAll();
						// while (attrEnum.hasMore()) {
						// Attribute next = attrEnum.next();
						// NamingEnumeration<?> allValues = next.getAll();
						// while (allValues.hasMore()) {
						// System.out.println("Attr: "+
						// allValues.next().toString());
						// }
						// }
						//
						// } catch (NamingException e) {
						// e.printStackTrace();
						// }
						//
					}
				};

				String usernameEncoded = LdapEncoder.filterEncode(theUsername);
				String filter = myFilterFormat.format(new Object[] {usernameEncoded});
				
				ourLog.debug("Querying LDAP with filter: {}", filter);
				boolean authenticate = template.authenticate(myLdapHost.getAuthenticateBaseDn(), filter, thePassword, callback);
				ourLog.debug("LDAP authentication results: {}", authenticate);
				
				return authenticate;

			} catch (Exception e) {
				throw new ProcessingException(e);
			}
		}

		private LdapTemplate getLdapTemplate() throws Exception {
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
