package net.svcret.ejb.model.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Index;

//@formatter:off
@Entity
@Table(name = "PX_MONITOR_RULE_AC_OUTCM")
@NamedQueries(value= {
	})
@org.hibernate.annotations.Table(appliesTo= "PX_MONITOR_RULE_AC_OUTCM",indexes={
	@Index(name="IDX_MRAO_CHECK_AND_TS", columnNames= {"ACTIVE_CHECK_PID","CHK_TIMESTAMP"})
})
//@formatter:on
public class PersMonitorRuleActiveCheckOutcome extends BasePersSavedTransaction {

	private static final long serialVersionUID = 1L;

	@ManyToOne(cascade = {}, optional = false)
	@JoinColumn(name = "ACTIVE_CHECK_PID", nullable = false)
	private PersMonitorRuleActiveCheck myCheck;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "PID")
	private Long myPid;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="CHK_TIMESTAMP", nullable=false)
	private Date myTimestamp;

	@Override
	public Long getPid() {
		return myPid;
	}

}
