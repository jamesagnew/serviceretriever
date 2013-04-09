package net.svcret.admin.client.rpc;

import java.io.Serializable;

import net.svcret.admin.shared.ServiceFailureException;
import net.svcret.admin.shared.model.AddServiceVersionResponse;
import net.svcret.admin.shared.model.BaseGAuthHost;
import net.svcret.admin.shared.model.GAuthenticationHostList;
import net.svcret.admin.shared.model.GDomain;
import net.svcret.admin.shared.model.GHttpClientConfig;
import net.svcret.admin.shared.model.GHttpClientConfigList;
import net.svcret.admin.shared.model.GLocalDatabaseAuthHost;
import net.svcret.admin.shared.model.GPartialUserList;
import net.svcret.admin.shared.model.GService;
import net.svcret.admin.shared.model.GSoap11ServiceVersion;
import net.svcret.admin.shared.model.GUser;
import net.svcret.admin.shared.model.ModelUpdateRequest;
import net.svcret.admin.shared.model.ModelUpdateResponse;
import net.svcret.admin.shared.model.PartialUserListRequest;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * The client side stub for the RPC service.
 */
@RemoteServiceRelativePath("modelupdate")
public interface ModelUpdateService extends RemoteService {

	GDomain addDomain(String theId, String theName) throws ServiceFailureException;

	GService addService(long theDomainPid, String theId, String theName, boolean theActive) throws ServiceFailureException;

	AddServiceVersionResponse addServiceVersion(Long theExistingDomainPid, String theCreateDomainId, Long theExistingServicePid, String theCreateServiceId, GSoap11ServiceVersion theVersion) throws ServiceFailureException;

	GSoap11ServiceVersion createNewSoap11ServiceVersion(Long theUncommittedId);

	GHttpClientConfigList deleteHttpClientConfig(long thePid) throws ServiceFailureException;

	ModelUpdateResponse loadModelUpdate(ModelUpdateRequest theRequest) throws ServiceFailureException;

	GSoap11ServiceVersion loadWsdl(GSoap11ServiceVersion theService, String theWsdlUrl) throws ServiceFailureException;

	GAuthenticationHostList removeAuthenticationHost(long thePid) throws ServiceFailureException;

	void reportClientError(String theMessage, Throwable theException);

	GAuthenticationHostList saveAuthenticationHost(GLocalDatabaseAuthHost theAuthHost) throws ServiceFailureException;

	GDomain saveDomain(long thePid, String theId, String theName);

	GHttpClientConfig saveHttpClientConfig(boolean theCreate, GHttpClientConfig theConfig) throws ServiceFailureException;

	void saveServiceVersionToSession(GSoap11ServiceVersion theServiceVersion);

	GPartialUserList loadUsers(PartialUserListRequest theRequest);

	UserAndAuthHost loadUser(long theUserPid) throws ServiceFailureException;

	public static class UserAndAuthHost implements Serializable {
		private static final long serialVersionUID = 1L;
		
		private GUser myUser;
		private BaseGAuthHost myAuthHost;

		/**
		 * Constructor
		 */
		public UserAndAuthHost() {
			super();
		}

		/**
		 * Constructor
		 */
		public UserAndAuthHost(GUser theUser, BaseGAuthHost theAuthHost) {
			super();
			myUser = theUser;
			myAuthHost = theAuthHost;
		}

		/**
		 * @return the user
		 */
		public GUser getUser() {
			return myUser;
		}

		/**
		 * @param theUser
		 *            the user to set
		 */
		public void setUser(GUser theUser) {
			myUser = theUser;
		}

		/**
		 * @return the authHost
		 */
		public BaseGAuthHost getAuthHost() {
			return myAuthHost;
		}

		/**
		 * @param theAuthHost
		 *            the authHost to set
		 */
		public void setAuthHost(BaseGAuthHost theAuthHost) {
			myAuthHost = theAuthHost;
		}
	}

	void saveUser(GUser theUser);

}
