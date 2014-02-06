package net.svcret.ejb.model.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import net.svcret.admin.shared.enm.InvocationStatsIntervalEnum;
import net.svcret.ejb.model.entity.BasePersStats.IStatsVisitor;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.validator.constraints.NotBlank;

@Embeddable
public class PersNodeStatsPk extends BasePersStatsPk<PersNodeStatsPk, PersNodeStats> {

	private static final long serialVersionUID = 1L;

	@Column(name="NODE_ID", length=PersNodeStatus.NODEID_MAXLENGTH,nullable=false)
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
	public <T> T accept(IStatsVisitor<T> theVisitor) {
		return theVisitor.visit(null, this);
	}

	@Override
	public Class<PersNodeStats> getStatType() {
		return PersNodeStats.class;
	}

	@Override
	protected boolean doEquals(PersNodeStatsPk theObj) {
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

	@Override
	public PersNodeStatsPk newPk(InvocationStatsIntervalEnum theInterval, Date theStartTime) {
		return new PersNodeStatsPk(theInterval, theStartTime, getNodeId());
	}

}
