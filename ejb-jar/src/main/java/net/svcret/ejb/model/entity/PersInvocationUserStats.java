package net.svcret.ejb.model.entity;

import java.util.Date;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.Index;

@org.hibernate.annotations.Table(indexes = { @Index(columnNames = { "START_TIME" }, name = "IDX_PISU_START_TIME") }, appliesTo = "PX_INVOC_STATS_USR")
@Table(name = "PX_INVOC_STATS_USR")
@Entity()
public class PersInvocationUserStats extends BasePersInvocationStats {

	@EmbeddedId
	private PersInvocationUserStatsPk myPk;

	public PersInvocationUserStats() {
		// nothing
	}

	public PersInvocationUserStats(PersInvocationUserStatsPk thePk) {
		myPk = thePk;
	}

	public PersInvocationUserStats(InvocationStatsIntervalEnum theInterval, Date theStartTime, PersServiceVersionMethod theMethod, PersServiceUser theUser) {
		myPk = new PersInvocationUserStatsPk(theInterval, theStartTime, theMethod, theUser);
	}

	public PersInvocationUserStatsPk getPk() {
		return myPk;
	}


}
