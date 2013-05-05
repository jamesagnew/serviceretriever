package net.svcret.ejb.model.entity;

public class Queries {

	public static final String PERSUSER_FIND = "PersUser.find";
	public static final String PERSUSER_FIND_Q = "SELECT u FROM PersUser u WHERE " + // -
			"u.myUsername = :USERNAME AND " + // -
			"u.myAuthenticationHost = :AUTH_HOST";

	public static final String AUTHHOST_FINDALL = "BasePersAuthenticationHost.findAll";
	public static final String AUTHHOST_FINDALL_Q = "SELECT h FROM BasePersAuthenticationHost h";

	public static final String SERVICE_FIND = "PersService.find";
	public static final String SERVICE_FIND_Q = "SELECT s FROM PersService s WHERE s.myServiceId = :SERVICE_ID AND s.myDomain.myPid = :DOMAIN_PID";
	
	public static final String PERSINVOC_STATS = "PersInvocationStats.findBeforeDate";
	public static final String PERSINVOC_STATS_Q = "SELECT s FROM PersInvocationStats s WHERE s.myPk.myInterval = :INTERVAL AND s.myPk.myStartTime < :BEFORE_DATE";

	public static final String PERSINVOC_USERSTATS = "PersInvocationUserStats.findBeforeDate";
	public static final String PERSINVOC_USERSTATS_Q = "SELECT s FROM PersInvocationUserStats s WHERE s.myPk.myInterval = :INTERVAL AND s.myPk.myStartTime < :BEFORE_DATE";

	public static final String PERSINVOC_ANONSTATS = "PersInvocationAnonStats.findBeforeDate";
	public static final String PERSINVOC_ANONSTATS_Q = "SELECT s FROM PersInvocationAnonStats s WHERE s.myPk.myInterval = :INTERVAL AND s.myPk.myStartTime < :BEFORE_DATE";

	public static final String SVCVER_RECENTMSGS = "PersServiceVersionRecentMessage.find";
	public static final String SVCVER_RECENTMSGS_Q = "SELECT s FROM PersServiceVersionRecentMessage s WHERE s.myServiceVersion = :SVC_VER AND s.myResponseType = :RESP_TYPE ORDER BY myPid ASC";
	
	public static final String SVCVER_RECENTMSGS_COUNT = "PersServiceVersionRecentMessage.count";
	public static final String SVCVER_RECENTMSGS_COUNT_Q = "SELECT COUNT(s) FROM PersServiceVersionRecentMessage s WHERE s.myServiceVersion = :SVC_VER AND s.myResponseType = :RESP_TYPE";

	public static final String USER_RECENTMSGS = "PersUserRecentMessage.find";
	public static final String USER_RECENTMSGS_Q = "SELECT s FROM PersUserRecentMessage s WHERE s.myUser = :USER AND s.myResponseType = :RESP_TYPE ORDER BY myPid ASC";

	public static final String USER_RECENTMSGS_COUNT = "PersUserRecentMessage.count";
	public static final String USER_RECENTMSGS_COUNT_Q = "SELECT COUNT(s) FROM PersUserRecentMessage s WHERE s.myUser = :USER AND s.myResponseType = :RESP_TYPE";

}
