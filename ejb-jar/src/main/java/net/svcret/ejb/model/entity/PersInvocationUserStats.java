package net.svcret.ejb.model.entity;

import java.util.Date;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import org.hibernate.annotations.Index;

//@formatter:off
@org.hibernate.annotations.Table(
	indexes = { 
		@Index(columnNames = { "START_TIME", "INTRVL" }, name = "IDX_PISU_TIME_AND_IVL"),
		@Index(columnNames = { "USER_PID", "START_TIME" }, name = "IDX_PISU_USER_AND_TIME")
	}, appliesTo = "PX_INVOC_STATS_USR")
@Table(name = "PX_INVOC_STATS_USR")
@NamedQueries(value= {
		@NamedQuery(name=Queries.PERSINVOC_USERSTATS_FINDUSER, query=Queries.PERSINVOC_USERSTATS_FINDUSER_Q),
		@NamedQuery(name=Queries.PERSINVOC_USERSTATS_FINDINTERVAL, query=Queries.PERSINVOC_USERSTATS_FINDINTERVAL_Q)
	})
@Entity()
//@formatter:on
public class PersInvocationUserStats extends BasePersMethodInvocationStats {

	private static final long serialVersionUID = 1L;
	
	@EmbeddedId
	private PersInvocationUserStatsPk myPk;

	public PersInvocationUserStats() {
		// nothing
	}

	public PersInvocationUserStats(PersInvocationUserStatsPk thePk) {
		myPk = thePk;
	}

	public PersInvocationUserStats(InvocationStatsIntervalEnum theInterval, Date theStartTime, PersServiceVersionMethod theMethod, PersUser theUser) {
		myPk = new PersInvocationUserStatsPk(theInterval, theStartTime, theMethod, theUser);
	}

	public PersInvocationUserStatsPk getPk() {
		return myPk;
	}

	@Override
	public StatsTypeEnum getStatsType() {
		return StatsTypeEnum.USER;
	}


}
