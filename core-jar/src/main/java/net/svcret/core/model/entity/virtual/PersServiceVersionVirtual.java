package net.svcret.core.model.entity.virtual;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.ForeignKey;

import net.svcret.admin.api.ProcessingException;
import net.svcret.admin.shared.model.BaseDtoServiceVersion;
import net.svcret.admin.shared.model.DtoServiceVersionVirtual;
import net.svcret.admin.shared.model.ServiceProtocolEnum;
import net.svcret.core.api.IDao;
import net.svcret.core.api.IServiceRegistry;
import net.svcret.core.model.entity.BasePersServiceVersion;

@Entity
@DiscriminatorValue("VIRTUAL")
public class PersServiceVersionVirtual extends BasePersServiceVersion {

	private static final long serialVersionUID = 1L;

	@ManyToOne(cascade= {})
	@JoinColumn(name="VIRTUAL_TARGET_PID", nullable=true)
//	@NotNull
	@ForeignKey(name = "PX_SVCVER_VIRT_TARGET")
	private BasePersServiceVersion myTarget;
	
	public PersServiceVersionVirtual() {
	}
	
	public PersServiceVersionVirtual(BasePersServiceVersion theTarget) {
		myTarget=theTarget;
	}

	@Override
	public ServiceProtocolEnum getProtocol() {
		return ServiceProtocolEnum.VIRTUAL;
	}

	public BasePersServiceVersion getTarget() {
		return myTarget;
	}

	public void setTarget(BasePersServiceVersion theTarget) {
		myTarget = theTarget;
	}

	@Override
	protected void fromDto(BaseDtoServiceVersion theDto, IDao theDao) throws ProcessingException {
		DtoServiceVersionVirtual dto = (DtoServiceVersionVirtual)theDto;
		myTarget = theDao.getServiceVersionByPid(dto.getTargetServiceVersionPid());
		if (myTarget==null) {
			throw new ProcessingException("Unknown target service version PID: " + dto.getTargetServiceVersionPid());
		}
	}

	@Override
	protected BaseDtoServiceVersion createDtoAndPopulateWithTypeSpecificEntries() {
		DtoServiceVersionVirtual retVal = new DtoServiceVersionVirtual();
		retVal.setTargetServiceVersionPid(myTarget.getPid());
		return retVal;
	}

}
