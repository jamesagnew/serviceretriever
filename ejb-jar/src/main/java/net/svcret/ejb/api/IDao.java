package net.svcret.ejb.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.ejb.Local;

import net.svcret.admin.shared.enm.ResponseTypeEnum;
import net.svcret.admin.shared.model.ServiceProtocolEnum;
import net.svcret.ejb.ejb.log.BaseUnflushed;
import net.svcret.ejb.ex.ProcessingException;
import net.svcret.ejb.model.entity.BasePersAuthenticationHost;
import net.svcret.ejb.model.entity.BasePersMonitorRule;
import net.svcret.ejb.model.entity.BasePersSavedTransaction;
import net.svcret.ejb.model.entity.BasePersSavedTransactionRecentMessage;
import net.svcret.ejb.model.entity.BasePersServiceCatalogItem;
import net.svcret.ejb.model.entity.BasePersServiceVersion;
import net.svcret.ejb.model.entity.BasePersStats;
import net.svcret.ejb.model.entity.BasePersStatsPk;
import net.svcret.ejb.model.entity.IThrottleable;
import net.svcret.ejb.model.entity.InvocationStatsIntervalEnum;
import net.svcret.ejb.model.entity.PersAuthenticationHostLdap;
import net.svcret.ejb.model.entity.PersAuthenticationHostLocalDatabase;
import net.svcret.ejb.model.entity.PersBaseClientAuth;
import net.svcret.ejb.model.entity.PersBaseServerAuth;
import net.svcret.ejb.model.entity.PersConfig;
import net.svcret.ejb.model.entity.PersDomain;
import net.svcret.ejb.model.entity.PersEnvironment;
import net.svcret.ejb.model.entity.PersHttpClientConfig;
import net.svcret.ejb.model.entity.PersInvocationMethodSvcverStats;
import net.svcret.ejb.model.entity.PersInvocationMethodUserStats;
import net.svcret.ejb.model.entity.PersInvocationUrlStats;
import net.svcret.ejb.model.entity.PersLibraryMessage;
import net.svcret.ejb.model.entity.PersMonitorRuleActiveCheck;
import net.svcret.ejb.model.entity.PersMonitorRuleActiveCheckOutcome;
import net.svcret.ejb.model.entity.PersMonitorRuleFiring;
import net.svcret.ejb.model.entity.PersNodeStats;
import net.svcret.ejb.model.entity.PersService;
import net.svcret.ejb.model.entity.PersServiceVersionMethod;
import net.svcret.ejb.model.entity.PersServiceVersionRecentMessage;
import net.svcret.ejb.model.entity.PersServiceVersionStatus;
import net.svcret.ejb.model.entity.PersServiceVersionUrl;
import net.svcret.ejb.model.entity.PersServiceVersionUrlStatus;
import net.svcret.ejb.model.entity.PersStickySessionUrlBinding;
import net.svcret.ejb.model.entity.PersStickySessionUrlBindingPk;
import net.svcret.ejb.model.entity.PersUser;
import net.svcret.ejb.model.entity.PersUserRecentMessage;
import net.svcret.ejb.model.entity.PersUserStatus;
import net.svcret.ejb.model.entity.soap.PersServiceVersionSoap11;

@Local
public interface IDao {

	void deleteAuthenticationHost(BasePersAuthenticationHost theAuthHost);

	void deleteHttpClientConfig(PersHttpClientConfig theConfig);

	void deleteMonitorRuleActiveCheckOutcomesBeforeCutoff(PersMonitorRuleActiveCheck theCheck, Date theCutoff);

	void deleteService(PersService theService);

	void deleteServiceVersion(BasePersServiceVersion theSv);

	void deleteUser(PersUser theUser);

	Collection<BasePersAuthenticationHost> getAllAuthenticationHosts();

	Collection<PersDomain> getAllDomains();

	Collection<PersMonitorRuleActiveCheck> getAllMonitorRuleActiveChecks();

	Collection<PersService> getAllServices();

	Collection<PersServiceVersionSoap11> getAllServiceVersions();

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

	Collection<BasePersMonitorRule> getMonitorRules();

	List<PersNodeStats> getNodeStatsBefore(InvocationStatsIntervalEnum theMinute, Date theHoursCutoff);

	PersAuthenticationHostLdap getOrCreateAuthenticationHostLdap(String theModuleId) throws ProcessingException;

	PersAuthenticationHostLocalDatabase getOrCreateAuthenticationHostLocalDatabase(String theModuleIdAdminAuth) throws ProcessingException;

	PersDomain getOrCreateDomainWithId(String theId) throws ProcessingException;

	PersEnvironment getOrCreateEnvironment(String theEnv) throws ProcessingException;

	PersHttpClientConfig getOrCreateHttpClientConfig(String theId);

	BasePersServiceVersion getOrCreateServiceVersionWithId(PersService theService, String theVersionId, ServiceProtocolEnum theProtocol) throws ProcessingException;

	PersService getOrCreateServiceWithId(PersDomain theDomain, String theServiceId) throws ProcessingException;

	<P extends BasePersStatsPk<P, O>, O extends BasePersStats<P, O>> O getOrCreateStats(P thePk);

	PersStickySessionUrlBinding getOrCreateStickySessionUrlBinding(PersStickySessionUrlBindingPk theBindingPk, PersServiceVersionUrl theUrlToUseIfNoneExists) throws ProcessingException;

	PersStickySessionUrlBinding getOrCreateStickySessionUrlBindingInNewTransaction(PersStickySessionUrlBindingPk theBindingPk, PersServiceVersionUrl theUrlToUseIfNoneExists);

	PersUser getOrCreateUser(BasePersAuthenticationHost theAuthHost, String theUsername) throws ProcessingException;

	PersService getServiceById(long theDomainPid, String theServiceId);

	PersService getServiceByPid(long theServicePid);

	BasePersServiceVersion getServiceVersionByPid(long theServiceVersionPid);

	PersServiceVersionMethod getServiceVersionMethodByPid(long theServiceVersionMethodPid);

	List<PersServiceVersionRecentMessage> getServiceVersionRecentMessages(BasePersServiceVersion theSvcVer, ResponseTypeEnum theResponseType);

	PersServiceVersionUrl getServiceVersionUrlByPid(long theUrlPid);

	PersServiceVersionUrlStatus getServiceVersionUrlStatusByPid(Long thePid);

	long getStateCounter(String theKey);

	PersServiceVersionStatus getStatusForServiceVersionWithPid(long theServicePid);

	PersUser getUser(long thePid);

	List<PersUserRecentMessage> getUserRecentMessages(IThrottleable theUser, ResponseTypeEnum theResponseType);

	List<PersInvocationMethodUserStats> getUserStatsWithinTimeRange(PersUser theUser, Date theStart, Date theEnd);

	long incrementStateCounter(String theKey);

	StatusesBean loadAllStatuses();

	List<PersLibraryMessage> loadLibraryMessages();

	List<PersMonitorRuleFiring> loadMonitorRuleFirings(Set<? extends BasePersServiceVersion> theAllSvcVers, int theStart);

	List<PersMonitorRuleFiring> loadMonitorRuleFiringsWhichAreActive();

	PersServiceVersionRecentMessage loadRecentMessageForServiceVersion(long thePid);

	PersUserRecentMessage loadRecentMessageForUser(long thePid);

	void removeDomain(PersDomain theDomain);

	void removeServiceVersion(long thePid) throws ProcessingException;

	void saveAuthenticationHost(BasePersAuthenticationHost theHost);

	PersBaseClientAuth<?> saveClientAuth(PersBaseClientAuth<?> theNextPers);

	PersConfig saveConfig(PersConfig theConfig);

	// List<PersMonitorRuleFiring> loadMonitorRuleFirings(Set<BasePersServiceVersion> theAllSvcVers, int theStart);

	PersDomain saveDomain(PersDomain theDomain);

	PersHttpClientConfig saveHttpClientConfig(PersHttpClientConfig theConfig);

	void saveInvocationStats(Collection<? extends BasePersStats<?, ?>> theStats);

	void saveInvocationStats(Collection<? extends BasePersStats<?, ?>> theStats, List<? extends BasePersStats<?, ?>> theStatsToDelete);

	PersLibraryMessage saveLibraryMessage(PersLibraryMessage theMessage);

	BasePersMonitorRule saveMonitorRule(BasePersMonitorRule theRule);

	PersMonitorRuleActiveCheckOutcome saveMonitorRuleActiveCheckOutcome(PersMonitorRuleActiveCheckOutcome theRecentOutcome);

	PersMonitorRuleFiring saveMonitorRuleFiring(PersMonitorRuleFiring theFiring);

	<T extends BasePersMonitorRule> T saveOrCreateMonitorRule(T theRule);

	void saveRecentMessagesAndTrimInNewTransaction(BaseUnflushed<? extends BasePersSavedTransactionRecentMessage> theNextTransactions);

	PersBaseServerAuth<?, ?> saveServerAuth(PersBaseServerAuth<?, ?> theNextPers);

	void saveService(PersService theService);

	BasePersServiceCatalogItem saveServiceCatalogItem(BasePersServiceCatalogItem theItem);

	PersUser saveServiceUser(PersUser theUser);

	BasePersServiceVersion saveServiceVersion(BasePersServiceVersion theVersion) throws ProcessingException;

	void saveServiceVersionRecentMessage(PersServiceVersionRecentMessage theMsg);

	void saveServiceVersionStatuses(ArrayList<PersServiceVersionStatus> theServiceVersionStatuses);

	void saveServiceVersionUrlStatus(List<PersServiceVersionUrlStatus> theList);

	void saveUserRecentMessage(PersUserRecentMessage theMsg);

	void saveUserStatus(Collection<PersUserStatus> theStatus);

	void trimServiceVersionRecentMessages(BasePersServiceVersion theVersion, ResponseTypeEnum theType, int theNumberToTrimTo);

	void trimUserRecentMessages(PersUser theUser, ResponseTypeEnum theType, int theNumberToTrimTo);

	public static class RecentMessagesAndMaxToKeep {
		private List<BasePersSavedTransaction> myMessages = new ArrayList<BasePersSavedTransaction>();
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

	void saveStickySessionUrlBinding(PersStickySessionUrlBinding theBinding);

}
