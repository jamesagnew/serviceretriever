package net.svcret.ejb.model.entity.virtual;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.ForeignKey;

import net.svcret.admin.shared.model.BaseDtoServiceVersion;
import net.svcret.admin.shared.model.DtoServiceVersionVirtual;
import net.svcret.admin.shared.model.ServiceProtocolEnum;
import net.svcret.ejb.model.entity.BasePersServiceVersion;

@Entity
@DiscriminatorValue("VIRTUAL")
public class PersServiceVersionVirtual extends BasePersServiceVersion {

	private static final long serialVersionUID = 1L;

	@ManyToOne(cascade= {})
	@JoinColumn(name="VIRTUAL_TARGET_PID", nullable=true)
	@NotNull
	@ForeignKey(name = "PX_SVCVER_VIRT_TARGET")
	private BasePersServiceVersion myTarget;
	
	@Override
	public ServiceProtocolEnum getProtocol() {
		return ServiceProtocolEnum.JSONRPC20;
	}

	public BasePersServiceVersion getTarget() {
		return myTarget;
	}

	public void setTarget(BasePersServiceVersion theTarget) {
		myTarget = theTarget;
	}

	@Override
	protected BaseDtoServiceVersion createDtoAndPopulateWithTypeSpecificEntries() {
		DtoServiceVersionVirtual retVal = new DtoServiceVersionVirtual();
		retVal.setTargetServiceVersionPid(myTarget.getPid());
		return retVal;
	}

}
