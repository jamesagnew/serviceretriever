package net.svcret.ejb.ejb;

import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.ejb.Schedule;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import net.svcret.admin.api.ProcessingException;
import net.svcret.admin.shared.util.Validate;
import net.svcret.ejb.api.IAuthorizationService;
import net.svcret.ejb.api.ICredentialGrabber;
import net.svcret.ejb.model.entity.BasePersAuthenticationHost;
import net.svcret.ejb.model.entity.PersUser;

public abstract class BaseAuthorizationServiceBean<T extends BasePersAuthenticationHost> implements IAuthorizationService {

	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(BaseAuthorizationServiceBean.class);

	private ConcurrentMap<String, CacheBean> myCredentialHashToExpiry = new ConcurrentHashMap<String, CacheBean>();

	@SuppressWarnings("unchecked")
	@Override
	public UserOrFailure authorize(BasePersAuthenticationHost theHost, InMemoryUserCatalog theUserCatalog, ICredentialGrabber theCredentialGrabber) throws ProcessingException {
		Validate.notNull(theHost, "Host");
		Validate.notNull(theUserCatalog, "Catalog");
		Validate.notNull(theCredentialGrabber, "Grabber");

		ourLog.debug("Trying to authorize username: {}", theCredentialGrabber.getUsername());

		Integer cacheForMillis = theHost.getCacheSuccessfulCredentialsForMillis();
		if (cacheForMillis != null) {
			CacheBean userCache = checkCachedAuthorization(theCredentialGrabber);
			if (userCache != null) {
				ourLog.debug("Found cached authorization containing user PID {}", userCache.getUserPid());
				PersUser user = theUserCatalog.getUser(userCache.getAuthHostPid(), userCache.getUserPid());
				if (user != null) {
					return new UserOrFailure(user);
				}
			}
		}

		if (!getConfigType().isInstance(theHost)) {
			throw new IllegalArgumentException("Host is not instance of " + getConfigType().getSimpleName() + ": " + theHost.getClass());
		}

		UserOrFailure retVal = doAuthorize((T) theHost, theUserCatalog, theCredentialGrabber);

		if (retVal.getUser() != null && cacheForMillis != null) {
			storeAuthorization(theCredentialGrabber, cacheForMillis, retVal.getUser().getPid(), theHost.getPid());
		}

		return retVal;
	}

	private CacheBean checkCachedAuthorization(ICredentialGrabber theCredentialGrabber) throws ProcessingException {
		String hash = theCredentialGrabber.getCredentialHash();
		CacheBean entry = myCredentialHashToExpiry.get(hash);
		if (entry == null) {
			return null;
		}

		long expiry = entry.getExpiry();
		if (expiry >= System.currentTimeMillis()) {
			return entry;
		}
		return null;
	}

	/**
	 * Purge old entries from the cache
	 */
	@Schedule(second = "0", minute = "*", hour = "*")
	@TransactionAttribute(TransactionAttributeType.NEVER)
	public void clearCache() {
		try {
			long now = System.currentTimeMillis();
			int count = 0;
			for (Entry<String, CacheBean> nextEntry : new ArrayList<Entry<String, CacheBean>>(myCredentialHashToExpiry.entrySet())) {
				if (now > nextEntry.getValue().getExpiry()) {
					if (myCredentialHashToExpiry.remove(nextEntry.getKey(), nextEntry.getValue())) {
						ourLog.debug("Removing cached entry for user {}", nextEntry.getValue().getUserPid());
					} else {
						ourLog.debug("Tried to remove cached entry for user {} but it was gone", nextEntry.getValue().getUserPid());
					}
				}
				count++;
			}

			if (count > 0) {
				ourLog.info("Purged {} entries from in-memory authorization cache", count);
			}
		} catch (Exception e) {
			ourLog.error("Failed to clear credential cache", e);
		}
	}

	protected abstract UserOrFailure doAuthorize(T theHost, InMemoryUserCatalog theUserCatalog, ICredentialGrabber theCredentialGrabber) throws ProcessingException;

	protected abstract Class<T> getConfigType();

	protected boolean shouldCache() {
		return true;
	}

	private void storeAuthorization(ICredentialGrabber theCredentialGrabber, Integer theCacheForMillis, Long theUserPid, Long theAuthHostPid) throws ProcessingException {
		Validate.notNull(theAuthHostPid, "AuthHostPid");
		Validate.notNull(theUserPid, "UserPid");
		Validate.greaterThanZero(theCacheForMillis, "CacheForMillis");

		String hash = theCredentialGrabber.getCredentialHash();
		long expiry = System.currentTimeMillis() + theCacheForMillis;
		myCredentialHashToExpiry.put(hash, new CacheBean(expiry, theUserPid, theAuthHostPid));
	}

	private static class CacheBean implements Comparable<CacheBean> {
		private final long myAuthHostPid;
		private final long myExpiry;
		private final int myHashCode;
		private final long myUserPid;

		public CacheBean(long theExpiry, long theUserPid, long theAuthHostPid) {
			super();
			myExpiry = theExpiry;
			myUserPid = theUserPid;
			myAuthHostPid = theAuthHostPid;

			final int prime = 31;
			int hashCode = 1;
			hashCode = prime * hashCode + (int) (myAuthHostPid ^ (myAuthHostPid >>> 32));
			hashCode = prime * hashCode + (int) (myExpiry ^ (myExpiry >>> 32));
			hashCode = prime * hashCode + (int) (myUserPid ^ (myUserPid >>> 32));
			myHashCode = hashCode;
		}

		@Override
		public int compareTo(CacheBean theCache) {
			return this.myExpiry < theCache.myExpiry ? -1 : this.myExpiry > theCache.myExpiry ? 1 : 0;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (!(obj instanceof CacheBean)) {
				return false;
			}
			CacheBean other = (CacheBean) obj;
			if (myAuthHostPid != other.myAuthHostPid) {
				return false;
			}
			if (myExpiry != other.myExpiry) {
				return false;
			}
			if (myUserPid != other.myUserPid) {
				return false;
			}
			return true;
		}

		/**
		 * @return the authHostPid
		 */
		public long getAuthHostPid() {
			return myAuthHostPid;
		}

		/**
		 * @return the expiry
		 */
		public long getExpiry() {
			return myExpiry;
		}

		/**
		 * @return the userPid
		 */
		public long getUserPid() {
			return myUserPid;
		}

		@Override
		public int hashCode() {
			return myHashCode;
		}
	}

}
