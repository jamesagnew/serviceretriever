package net.svcret.ejb.model.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Table(name = "PX_SVC_VER_THROTTLE")
@Entity()
public class PersServiceVersionThrottle extends BasePersObject {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "PID")
	private Long myPid;

	@OneToOne()
	@Column(name="SVC_VER_PID")
	private BasePersServiceVersion mySvcVer;
	
	@Column(name="PER_USER", nullable=false)
	private boolean myApplyPerUser;

	@Column(name="PROP_CAP_NAME", nullable=true)
	private String myApplyPropCapName;

	@Override
	public Long getPid() {
		return myPid;
	}

	public void loadAllAssociations() {
		// nothing
	}

}
