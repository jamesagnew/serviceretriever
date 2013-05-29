package net.svcret.ejb.ejb;

import static org.mockito.Mockito.*;
import net.svcret.admin.shared.model.TimeRange;
import net.svcret.admin.shared.model.TimeRangeEnum;
import net.svcret.ejb.api.IRuntimeStatus;
import net.svcret.ejb.ejb.AdminServiceBean.IWithStats;
import net.svcret.ejb.model.entity.PersConfig;
import net.svcret.ejb.model.entity.PersInvocationStatsPk;
import net.svcret.ejb.model.entity.PersServiceVersionMethod;

import org.junit.Test;

public class AdminServiceBeanTest {

	@Test
	public void testDoWithStats() {
		DefaultAnswer.setDesignTime();
		
		PersConfig config = new PersConfig();
		
		TimeRange range = new TimeRange();
		range.setRange(TimeRangeEnum.ONE_MONTH);
		
		IRuntimeStatus status= mock(IRuntimeStatus.class, DefaultAnswer.INSTANCE);
		when(status.getOrCreateInvocationStatsSynchronously((PersInvocationStatsPk) any())).thenReturn(null);
		
		PersServiceVersionMethod method = mock(PersServiceVersionMethod.class, DefaultAnswer.INSTANCE);
		IWithStats operator = mock(IWithStats.class);
		
		DefaultAnswer.setRunTime();
		AdminServiceBean.doWithStatsByMinute(config, range, status, method, operator);
		
//		verify(operator, atLeastOnce());
		
		DefaultAnswer.setDesignTime();
	}
	
}
