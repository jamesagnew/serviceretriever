package net.svcret.ejb.model.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.Index;

@org.hibernate.annotations.Table(indexes = { @Index(columnNames = { "START_TIME" }, name = "IDX_SRS_START_TIME") }, appliesTo = "PX_STATIC_RES_STATS")
@Table(name = "PX_STATIC_RES_STATS")
@Entity()
public class PersStaticResourceStats extends BasePersMethodStats {

	@EmbeddedId
	private PersStaticResourceStatsPk myPk;

	@Column(name = "ACCESS_COUNT", nullable = false)
	private long myAccessCount;

	/**
	 * @return the accessCount
	 */
	public synchronized long getAccessCount() {
		return myAccessCount;
	}

	public synchronized void addAccess() {
		myAccessCount++;
	}

	public PersStaticResourceStats() {
		// nothing
	}

	public PersStaticResourceStats(PersStaticResourceStatsPk thePk) {
		myPk = thePk;
	}

	public PersStaticResourceStats(InvocationStatsIntervalEnum theInterval, Date theStartTime, PersServiceVersionResource theResource) {
		myPk = new PersStaticResourceStatsPk(theInterval, theStartTime, theResource);
	}

	public PersStaticResourceStatsPk getPk() {
		return myPk;
	}

	@Override
	public synchronized void mergeUnsynchronizedEvents(BasePersMethodStats theNext) {
		PersStaticResourceStats next = (PersStaticResourceStats) theNext;
		synchronized (next) {
			myAccessCount += next.getAccessCount();
		}
	}

}
