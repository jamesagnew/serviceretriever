package net.svcret.ejb.model.entity;

import java.util.Date;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.Index;

@org.hibernate.annotations.Table(indexes = { @Index(columnNames = { "START_TIME" }, name = "IDX_PIS_START_TIME") }, appliesTo = "PX_INVOC_STATS")
@Table(name = "PX_INVOC_STATS")
@Entity()
public class PersInvocationStats extends BasePersInvocationStats {

	@EmbeddedId
	private PersInvocationStatsPk myPk;

	public PersInvocationStats() {
		// nothing
	}

	public PersInvocationStats(PersInvocationStatsPk thePk) {
		myPk = thePk;
	}

	public PersInvocationStats(InvocationStatsIntervalEnum theInterval, Date theStartTime, PersServiceVersionMethod theMethod) {
		myPk = new PersInvocationStatsPk(theInterval, theStartTime, theMethod);
	}

	public PersInvocationStatsPk getPk() {
		return myPk;
	}


}
