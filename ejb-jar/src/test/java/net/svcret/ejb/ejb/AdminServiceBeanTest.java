package net.svcret.ejb.ejb;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import net.svcret.admin.shared.model.TimeRange;
import net.svcret.admin.shared.model.TimeRangeEnum;
import net.svcret.ejb.api.IRuntimeStatus;
import net.svcret.ejb.ejb.AdminServiceBean.IWithStats;
import net.svcret.ejb.model.entity.BasePersInvocationMethodStatsPk;
import net.svcret.ejb.model.entity.BasePersMethodInvocationStats;
import net.svcret.ejb.model.entity.BasePersInvocationStatsPk;
import net.svcret.ejb.model.entity.InvocationStatsIntervalEnum;
import net.svcret.ejb.model.entity.PersConfig;
import net.svcret.ejb.model.entity.PersInvocationStatsPk;
import net.svcret.ejb.model.entity.PersServiceVersionMethod;
import net.svcret.ejb.model.entity.BasePersMethodInvocationStats.StatsTypeEnum;

import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class AdminServiceBeanTest {
private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(AdminServiceBeanTest.class);
	@Test
	public void testDoWithStats() throws ParseException {
		DefaultAnswer.setDesignTime();
		
		PersConfig config = new PersConfig();
		
		TimeRange range = new TimeRange();
		range.setRange(TimeRangeEnum.ONE_MONTH);
		
		IRuntimeStatus status= mock(IRuntimeStatus.class, DefaultAnswer.INSTANCE);
		
		final ArgumentCaptor<PersInvocationStatsPk> captor = ArgumentCaptor.forClass(PersInvocationStatsPk.class);
		when(status.getInvocationStatsSynchronously(captor.capture())).thenReturn(new BasePersMethodInvocationStats() {
			private static final long serialVersionUID = 1L;

			@Override
			public BasePersInvocationStatsPk getPk() {
				return captor.getValue();
			}
			
			@Override
			public StatsTypeEnum getStatsType() {
				return null;
			}
		});
		
		PersServiceVersionMethod method = mock(PersServiceVersionMethod.class, DefaultAnswer.INSTANCE);
		when(method.getPid()).thenReturn(123L);

		final Set<BasePersInvocationStatsPk> all = new HashSet<BasePersInvocationStatsPk>();
		IWithStats operator = new IWithStats() {
			@Override
			public void withStats(int theIndex, BasePersMethodInvocationStats theStats) {
				ourLog.info("{} - {}", theStats.getPk().getInterval(), theStats.getPk().getStartTime());
				all.add(theStats.getPk());
			}
		}; 
		
		SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		Date end = fmt.parse("2013-07-09 16:04");
		config.setNow(fmt.parse("2013-07-09 16:02").getTime());
		AdminServiceBean.doWithStatsByMinute(config, range, status, method, operator, end);

		ourLog.info(config.getCollapseStatsToTenMinutesCutoff().toString());
		
		assertTrue(all.contains(new PersInvocationStatsPk(InvocationStatsIntervalEnum.HOUR, fmt.parse("2013-06-19 19:00:00"), method)));
		assertTrue(all.contains(new PersInvocationStatsPk(InvocationStatsIntervalEnum.MINUTE, fmt.parse("2013-07-09 14:00:00"), method)));
		assertTrue(all.contains(new PersInvocationStatsPk(InvocationStatsIntervalEnum.TEN_MINUTE, fmt.parse("2013-07-09 13:50:00"), method)));
		
	}
	
	
	
}
