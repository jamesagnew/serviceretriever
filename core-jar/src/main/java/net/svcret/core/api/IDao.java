package net.svcret.core.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

import net.svcret.admin.api.ProcessingException;
import net.svcret.admin.api.UnexpectedFailureException;
import net.svcret.admin.shared.enm.InvocationStatsIntervalEnum;
import net.svcret.admin.shared.enm.ResponseTypeEnum;
import net.svcret.admin.shared.model.IThrottleable;
import net.svcret.admin.shared.model.ServiceProtocolEnum;
import net.svcret.core.log.BaseUnflushed;
import net.svcret.core.model.entity.BasePersAuthenticationHost;
import net.svcret.core.model.entity.BasePersMonitorRule;
import net.svcret.core.model.entity.BasePersSavedTransaction;
import net.svcret.core.model.entity.BasePersSavedTransactionRecentMessage;
import net.svcret.core.model.entity.BasePersServiceCatalogItem;
import net.svcret.core.model.entity.BasePersServiceVersion;
import net.svcret.core.model.entity.BasePersStats;
import net.svcret.core.model.entity.BasePersStatsPk;
import net.svcret.core.model.entity.PersAuthenticationHostLdap;
import net.svcret.core.model.entity.PersAuthenticationHostLocalDatabase;
import net.svcret.core.model.entity.PersBaseClientAuth;
import net.svcret.core.model.entity.PersBaseServerAuth;
import net.svcret.core.model.entity.PersConfig;
import net.svcret.core.model.entity.PersDomain;
import net.svcret.core.model.entity.PersEnvironment;
import net.svcret.core.model.entity.PersHttpClientConfig;
import net.svcret.core.model.entity.PersInvocationMethodSvcverStats;
import net.svcret.core.model.entity.PersInvocationMethodUserStats;
import net.svcret.core.model.entity.PersInvocationUrlStats;
import net.svcret.core.model.entity.PersLibraryMessage;
import net.svcret.core.model.entity.PersMethod;
import net.svcret.core.model.entity.PersMethodStatus;
import net.svcret.core.model.entity.PersMonitorRuleActiveCheck;
import net.svcret.core.model.entity.PersMonitorRuleActiveCheckOutcome;
import net.svcret.core.model.entity.PersMonitorRuleFiring;
import net.svcret.core.model.entity.PersNodeStats;
import net.svcret.core.model.entity.PersNodeStatus;
import net.svcret.core.model.entity.PersService;
import net.svcret.core.model.entity.PersServiceVersionRecentMessage;
import net.svcret.core.model.entity.PersServiceVersionStatus;
import net.svcret.core.model.entity.PersServiceVersionUrl;
import net.svcret.core.model.entity.PersServiceVersionUrlStatus;
import net.svcret.core.model.entity.PersStickySessionUrlBinding;
import net.svcret.core.model.entity.PersUser;
import net.svcret.core.model.entity.PersUserRecentMessage;
import net.svcret.core.model.entity.PersUserStatus;
import net.svcret.core.model.entity.soap.PersServiceVersionSoap11;

public interface IDao {

	PersStickySessionUrlBinding createOrUpdateExistingStickySessionUrlBindingInNewTransaction(PersStickySessionUrlBinding theBinding) throws UnexpectedFailureException;

	void deleteAuthenticationHost(BasePersAuthenticationHost theAuthHost);

	void deleteDomain(PersDomain theDomain);

	void deleteHttpClientConfig(PersHttpClientConfig theConfig);

	void deleteLibraryMessage(PersLibraryMessage theMsg);

	void deleteMonitorRule(BasePersMonitorRule theRule);

	void deleteMonitorRuleActiveCheckOutcomesBeforeCutoff(PersMonitorRuleActiveCheck theCheck, Date theCutoff);

	void deleteService(PersService theService);

	void deleteServiceVersion(BasePersServiceVersion theSv);

	void deleteStickySession(PersStickySessionUrlBinding theStickySession);

	void deleteUser(PersUser theUser);

	Collection<BasePersAuthenticationHost> getAllAuthenticationHosts();

	Collection<PersDomain> getAllDomains();

	Collection<PersMethodStatus> getAllMethodStatus();

	Collection<PersMonitorRuleActiveCheck> getAllMonitorRuleActiveChecks();

	List<PersMonitorRuleFiring> getAllMonitorRuleFiringsWhichAreActive();

	Collection<PersNodeStatus> getAllNodeStatuses();

	Collection<PersService> getAllServices();

	Collection<PersServiceVersionSoap11> getAllServiceVersions();

	Collection<PersStickySessionUrlBinding> getAllStickySessions();

	Collection<PersUser> getAllUsersAndInitializeThem();

	BasePersAuthenticationHost getAuthenticationHost(String theModuleId) throws ProcessingException;

	BasePersAuthenticationHost getAuthenticationHostByPid(long thePid);

	PersConfig getConfigByPid(long theDefaultId);

	PersDomain getDomainById(String theDomainId);

	/**
	 * Returns <code>null</code> if not found
	 */
	PersDomain getDomainByPid(long theDomainPid);

	PersHttpClientConfig getHttpClientConfig(long thePid);

	Collection<PersHttpClientConfig> getHttpClientConfigs();

	<P extends BasePersStatsPk<P, O>, O extends BasePersStats<P, O>> O getInvocationStats(P thePk);

	List<PersInvocationMethodSvcverStats> getInvocationStatsBefore(InvocationStatsIntervalEnum theHour, Date theDaysCutoff);

	List<PersInvocationUrlStats> getInvocationUrlStatsBefore(InvocationStatsIntervalEnum theMinute, Date theHoursCutoff);

	List<PersInvocationMethodUserStats> getInvocationUserStatsBefore(InvocationStatsIntervalEnum theHour, Date theDaysCutoff);

	PersLibraryMessage getLibraryMessageByPid(long theMessagePid);

	Collection<PersLibraryMessage> getLibraryMessagesWhichApplyToDomain(long thePid);

	Collection<PersLibraryMessage> getLibraryMessagesWhichApplyToService(long thePid);

	Collection<PersLibraryMessage> getLibraryMessagesWhichApplyToServiceVersion(long theServiceVersionPid);

	BasePersMonitorRule getMonitorRule(long thePid);

	PersMonitorRuleActiveCheck getMonitorRuleActiveCheck(long thePid);

	Collection<BasePersMonitorRule> getMonitorRules();

	List<PersNodeStats> getNodeStatsBefore(InvocationStatsIntervalEnum theMinute, Date theHoursCutoff);

	List<PersNodeStats> getNodeStatsWithinRange(Date theStartInclusive, Date theEndInclusive);

	PersAuthenticationHostLdap getOrCreateAuthenticationHostLdap(String theModuleId) throws ProcessingException;

	PersAuthenticationHostLocalDatabase getOrCreateAuthenticationHostLocalDatabase(String theModuleIdAdminAuth) throws ProcessingException;

	PersDomain getOrCreateDomainWithId(String theId) throws ProcessingException;

	PersEnvironment getOrCreateEnvironment(String theEnv) throws ProcessingException;

	PersHttpClientConfig getOrCreateHttpClientConfig(String theId);

//	PersStickySessionUrlBinding getOrCreateStickySessionUrlBindingInNewTransaction(PersStickySessionUrlBindingPk theBindingPk, PersServiceVersionUrl theUrlToUseIfNoneExists);

	PersNodeStatus getOrCreateNodeStatusInNewTransaction(String theNodeId);

	BasePersServiceVersion getOrCreateServiceVersionWithId(PersService theService, String theVersionId, ServiceProtocolEnum theProtocol) throws ProcessingException;

	BasePersServiceVersion getOrCreateServiceVersionWithId(PersService theService, String theVersionId, ServiceProtocolEnum theProtocol, BasePersServiceVersion theSvcVerToUseIfCreatingNew);

	PersService getOrCreateServiceWithId(PersDomain theDomain, String theServiceId) throws ProcessingException;

	<P extends BasePersStatsPk<P, O>, O extends BasePersStats<P, O>> O getOrCreateStats(P thePk);

	PersUser getOrCreateUser(BasePersAuthenticationHost theAuthHost, String theUsername) throws ProcessingException;

	PersService getServiceById(long theDomainPid, String theServiceId);

	PersService getServiceByPid(long theServicePid);

	BasePersServiceVersion getServiceVersionByPid(long theServiceVersionPid);

	PersMethod getServiceVersionMethodByPid(long theServiceVersionMethodPid);

	List<PersServiceVersionRecentMessage> getServiceVersionRecentMessages(BasePersServiceVersion theSvcVer, ResponseTypeEnum theResponseType);

	PersServiceVersionUrl getServiceVersionUrlByPid(long theUrlPid);

	PersServiceVersionUrlStatus getServiceVersionUrlStatusByPid(Long thePid);

	long getStateCounter(String theKey);

	PersServiceVersionStatus getStatusForServiceVersionWithPid(long theServicePid);

	PersUser getUser(long thePid);

	// List<PersMonitorRuleFiring>
	// loadMonitorRuleFirings(Set<BasePersServiceVersion> theAllSvcVers, int
	// theStart);

	List<PersUserRecentMessage> getUserRecentMessages(IThrottleable theUser, ResponseTypeEnum theResponseType);

	List<PersInvocationMethodUserStats> getUserStatsWithinTimeRange(PersUser theUser, Date theStart, Date theEnd);

	long incrementStateCounter(String theKey);

	StatusesBean loadAllStatuses(PersConfig theConfig);

	List<PersLibraryMessage> loadLibraryMessages();

	PersMonitorRuleActiveCheckOutcome loadMonitorRuleActiveCheckOutcome(long thePid);

	List<PersMonitorRuleFiring> loadMonitorRuleFirings(Set<? extends BasePersServiceVersion> theAllSvcVers, int theStart);

	PersServiceVersionRecentMessage loadRecentMessageForServiceVersion(long thePid);

	PersUserRecentMessage loadRecentMessageForUser(long thePid);

	BasePersAuthenticationHost saveAuthenticationHost(BasePersAuthenticationHost theHost);

	PersBaseClientAuth<?> saveClientAuth(PersBaseClientAuth<?> theNextPers);

	PersConfig saveConfigInNewTransaction(PersConfig theConfig);

	PersDomain saveDomain(PersDomain theDomain);

	PersHttpClientConfig saveHttpClientConfigInNewTransaction(PersHttpClientConfig theConfig);

	void saveStatsInNewTransaction(Collection<? extends BasePersStats<?, ?>> theStats);

	void saveStatsInNewTransaction(Collection<? extends BasePersStats<?, ?>> theStats, List<? extends BasePersStats<?, ?>> theStatsToDelete);

	PersLibraryMessage saveLibraryMessage(PersLibraryMessage theMessage);

	void saveMethodStatuses(List<PersMethodStatus> theMethodStatuses);

	PersMonitorRuleActiveCheckOutcome saveMonitorRuleActiveCheckOutcome(PersMonitorRuleActiveCheckOutcome theRecentOutcome);

	PersMonitorRuleFiring saveMonitorRuleFiring(PersMonitorRuleFiring theFiring);

	BasePersMonitorRule saveMonitorRuleInNewTransaction(BasePersMonitorRule theRule);

	void saveNodeStatusInNewTransaction(PersNodeStatus theNodeStatus);

	<T extends BasePersMonitorRule> T saveOrCreateMonitorRule(T theRule);

	ByteDelta saveRecentMessagesAndTrimInNewTransaction(BaseUnflushed<? extends BasePersSavedTransactionRecentMessage> theNextTransactions);

	PersBaseServerAuth<?, ?> saveServerAuth(PersBaseServerAuth<?, ?> theNextPers);

	BasePersServiceCatalogItem saveServiceCatalogItem(BasePersServiceCatalogItem theItem);

	PersService saveServiceInNewTransaction(PersService theService);

	PersUser saveServiceUser(PersUser theUser);

	BasePersServiceVersion saveServiceVersionInNewTransaction(BasePersServiceVersion theVersion) throws ProcessingException;

	void saveServiceVersionRecentMessage(PersServiceVersionRecentMessage theMsg);

	void saveServiceVersionStatuses(ArrayList<PersServiceVersionStatus> theServiceVersionStatuses);

	void saveServiceVersionUrlStatusInNewTransaction(List<PersServiceVersionUrlStatus> theList);

	void saveStickySessionUrlBindingInNewTransaction(PersStickySessionUrlBinding theBinding);

	void saveUserRecentMessage(PersUserRecentMessage theMsg);

	void saveUserStatus(Collection<PersUserStatus> theStatus);

	long trimServiceVersionRecentMessages(BasePersServiceVersion theVersion, ResponseTypeEnum theType, int theNumberToTrimTo);

	void trimUserRecentMessages(PersUser theUser, ResponseTypeEnum theType, int theNumberToTrimTo);

	public static class ByteDelta {
		private long myAdded;
		private long myRemoved;

		public ByteDelta() {
			// nothing
		}

		public ByteDelta(long theAdded, long theRemoved) {
			super();
			myAdded = theAdded;
			myRemoved = theRemoved;
		}

		public void add(ByteDelta theByteDelta) {
			myAdded += theByteDelta.getAdded();
			myRemoved += theByteDelta.getRemoved();
		}

		public void addAdded(int theAddedBytes) {
			myAdded += theAddedBytes;
		}

		public void addRemoved(long theRemovedBytes) {
			myRemoved += theRemovedBytes;
		}

		public long getAdded() {
			return myAdded;
		}

		public long getRemoved() {
			return myRemoved;
		}
	}

	public static class RecentMessagesAndMaxToKeep {
		private List<BasePersSavedTransaction> myMessages = new ArrayList<>();
		private int myNumToKeep;

		public void addMessages(Collection<? extends BasePersSavedTransaction> theMessages) {
			myMessages.addAll(theMessages);
		}

		/**
		 * @return the numToKeep
		 */
		public int getNumToKeep() {
			return myNumToKeep;
		}

		/**
		 * @param theNumToKeep
		 *            the numToKeep to set
		 */
		public void setNumToKeep(int theNumToKeep) {
			myNumToKeep = theNumToKeep;
		}

	}

}
