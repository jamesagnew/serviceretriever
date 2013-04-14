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
	
}
