package net.svcret.ejb.model.entity;

import java.util.Date;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.Index;

@org.hibernate.annotations.Table(indexes = { @Index(columnNames = { "START_TIME" }, name = "IDX_PISA_START_TIME") }, appliesTo = "PX_INVOC_STATS_ANON")
@Table(name = "PX_INVOC_STATS_ANON")
@Entity()
public class PersInvocationAnonStats extends BasePersInvocationStats {

	private static final long serialVersionUID = 1L;
	
	@EmbeddedId
	private PersInvocationAnonStatsPk myPk;

	public PersInvocationAnonStats() {
		// nothing
	}

	public PersInvocationAnonStats(PersInvocationAnonStatsPk thePk) {
		myPk = thePk;
	}

	public PersInvocationAnonStats(InvocationStatsIntervalEnum theInterval, Date theStartTime, PersServiceVersionMethod theMethod) {
		myPk = new PersInvocationAnonStatsPk(theInterval, theStartTime, theMethod);
	}

	public PersInvocationAnonStatsPk getPk() {
		return myPk;
	}

}
