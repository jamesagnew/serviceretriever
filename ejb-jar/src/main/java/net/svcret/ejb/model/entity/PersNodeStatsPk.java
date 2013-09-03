package net.svcret.ejb.model.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.validator.constraints.NotBlank;

@Embeddable
public class PersNodeStatsPk extends BasePersInvocationStatsPk {

	private static final long serialVersionUID = 1L;

	@Column(name="NODE_ID", length=20,nullable=false)
	@NotBlank
	private String myNodeId;
	
	public PersNodeStatsPk() {
		super();
	}

	public PersNodeStatsPk(InvocationStatsIntervalEnum theInterval, Date theStartTime, String theNodeId) {
		super(theInterval, theStartTime);
		myNodeId=theNodeId;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public PersNodeStats newObjectInstance() {
		return new PersNodeStats(this);
	}

	@Override
	protected boolean doEquals(BasePersInvocationStatsPk theObj2) {
		PersNodeStatsPk theObj = (PersNodeStatsPk) theObj2;
		return getInterval().equals(theObj.getInterval()) // -
				&& getNodeId() == (theObj.getNodeId()) // -
				&& getStartTime().equals(theObj.getStartTime()); // -
	}


	public String getNodeId() {
		return myNodeId;
	}

	@Override
	protected void doHashCode(HashCodeBuilder theB) {
		theB.append(myNodeId);
	}

}
