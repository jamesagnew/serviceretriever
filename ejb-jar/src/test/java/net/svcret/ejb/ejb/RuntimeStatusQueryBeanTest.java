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
import net.svcret.ejb.admin.AdminServiceBean;
import net.svcret.ejb.admin.AdminServiceBean.IWithStats;
import net.svcret.ejb.api.IConfigService;
import net.svcret.ejb.api.IDao;
import net.svcret.ejb.api.IRuntimeStatusQueryLocal;
import net.svcret.ejb.model.entity.BasePersStatsPk;
import net.svcret.ejb.model.entity.InvocationStatsIntervalEnum;
import net.svcret.ejb.model.entity.PersConfig;
import net.svcret.ejb.model.entity.PersDomain;
import net.svcret.ejb.model.entity.PersInvocationMethodSvcverStats;
import net.svcret.ejb.model.entity.PersInvocationMethodSvcverStatsPk;
import net.svcret.ejb.model.entity.PersService;
import net.svcret.ejb.model.entity.PersMethod;
import net.svcret.ejb.model.entity.soap.PersServiceVersionSoap11;

import org.apache.commons.lang3.time.DateUtils;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class RuntimeStatusQueryBeanTest {

	private static long ourNextPid;

	@Test
	public void testDoWithStats() throws Exception {

		PersDomain domain = new PersDomain(ourNextPid++, "d");
		PersService service = new PersService(ourNextPid++, domain, "s", "s");
		PersServiceVersionSoap11 sv = new PersServiceVersionSoap11(ourNextPid++, service, "v");
		PersMethod method = new PersMethod(ourNextPid++, sv, "m");

		PersConfig config = new PersConfig();
		config.setDefaults();
		IConfigService configSvc = mock(IConfigService.class);
		when(configSvc.getConfig()).thenReturn(config);

		IDao dao = mock(IDao.class);

		RuntimeStatusQueryBean svc = new RuntimeStatusQueryBean();
		svc.setConfigSvcForUnitTest(configSvc);
		svc.setDaoForUnitTests(dao);

		// Want now stats
		{
			Date now = InvocationStatsIntervalEnum.MINUTE.truncate(new Date());
			PersInvocationMethodSvcverStatsPk pk = new PersInvocationMethodSvcverStatsPk(InvocationStatsIntervalEnum.MINUTE, now, method);
			PersInvocationMethodSvcverStats value = new PersInvocationMethodSvcverStats(pk);
			when(dao.getInvocationStats(pk)).thenReturn(value);
			PersInvocationMethodSvcverStats stats = svc.getInvocationStatsSynchronously(pk);

			assertSame(value, stats);
			verify(dao, times(1)).getInvocationStats(pk);
		}

		// Make sure a second invocation for now goes back to the DB again
		{
			Date now = InvocationStatsIntervalEnum.MINUTE.truncate(new Date());
			PersInvocationMethodSvcverStatsPk pk = new PersInvocationMethodSvcverStatsPk(InvocationStatsIntervalEnum.MINUTE, now, method);
			PersInvocationMethodSvcverStats value = new PersInvocationMethodSvcverStats(pk);
			when(dao.getInvocationStats(pk)).thenReturn(value);
			PersInvocationMethodSvcverStats stats = svc.getInvocationStatsSynchronously(pk);

			assertSame(value, stats);
			verify(dao, times(2)).getInvocationStats(pk);
		}

		// Not let's try to minutes ago
		{
			Date now = InvocationStatsIntervalEnum.MINUTE.truncate(new Date(System.currentTimeMillis() - (10 * DateUtils.MILLIS_PER_MINUTE)));
			PersInvocationMethodSvcverStatsPk pk = new PersInvocationMethodSvcverStatsPk(InvocationStatsIntervalEnum.MINUTE, now, method);
			PersInvocationMethodSvcverStats value = new PersInvocationMethodSvcverStats(pk);
			when(dao.getInvocationStats(pk)).thenReturn(value);
			PersInvocationMethodSvcverStats stats = svc.getInvocationStatsSynchronously(pk);

			assertSame(value, stats);
			verify(dao, times(1)).getInvocationStats(pk);
		}

		// 10 minutes ago should return from cache
		{
			Date now = InvocationStatsIntervalEnum.MINUTE.truncate(new Date(System.currentTimeMillis() - (10 * DateUtils.MILLIS_PER_MINUTE)));
			PersInvocationMethodSvcverStatsPk pk = new PersInvocationMethodSvcverStatsPk(InvocationStatsIntervalEnum.MINUTE, now, method);
			PersInvocationMethodSvcverStats value = new PersInvocationMethodSvcverStats(pk);
			when(dao.getInvocationStats(pk)).thenReturn(value);
			PersInvocationMethodSvcverStats stats = svc.getInvocationStatsSynchronously(pk);

			assertNotSame(value, stats);
			verify(dao, times(1)).getInvocationStats(pk);
		}

	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void testDoWithStatsNormal() throws ParseException {
		DefaultAnswer.setDesignTime();

		PersConfig config = new PersConfig();

		TimeRange range = new TimeRange();
		range.setWithPresetRange(TimeRangeEnum.ONE_MONTH);

		IRuntimeStatusQueryLocal status = mock(IRuntimeStatusQueryLocal.class, DefaultAnswer.INSTANCE);

		final ArgumentCaptor<PersInvocationMethodSvcverStatsPk> captor = ArgumentCaptor.forClass(PersInvocationMethodSvcverStatsPk.class);
		when(status.getInvocationStatsSynchronously(captor.capture())).thenReturn(new PersInvocationMethodSvcverStats() {
			private static final long serialVersionUID = 1L;

			@Override
			public PersInvocationMethodSvcverStatsPk getPk() {
				return captor.getValue();
			}

			@Override
			public StatsTypeEnum getStatsType() {
				return null;
			}

		});

		PersMethod method = mock(PersMethod.class, DefaultAnswer.INSTANCE);
		when(method.getPid()).thenReturn(123L);

		final Set<BasePersStatsPk> all = new HashSet<BasePersStatsPk>();
		IWithStats operator = new IWithStats<PersInvocationMethodSvcverStatsPk, PersInvocationMethodSvcverStats>() {
			@Override
			public void withStats(int theIndex, PersInvocationMethodSvcverStats theStats) {
				ourLog.info("{} - {}", theStats.getPk().getInterval(), theStats.getPk().getStartTime());
				all.add(theStats.getPk());
			}
		};

		SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		range.setWithPresetRangeEndForUnitTest(fmt.parse("2013-07-09 16:04"));
		config.setNow(fmt.parse("2013-07-09 16:02").getTime());
		AdminServiceBean.doWithStatsByMinute(config, range, status, method, operator);

		ourLog.info(config.getCollapseStatsToTenMinutesCutoff().toString());

		assertTrue(all.contains(new PersInvocationMethodSvcverStatsPk(InvocationStatsIntervalEnum.HOUR, fmt.parse("2013-06-19 19:00:00"), method)));
		assertTrue(all.contains(new PersInvocationMethodSvcverStatsPk(InvocationStatsIntervalEnum.MINUTE, fmt.parse("2013-07-09 14:00:00"), method)));
		assertTrue(all.contains(new PersInvocationMethodSvcverStatsPk(InvocationStatsIntervalEnum.TEN_MINUTE, fmt.parse("2013-07-09 13:50:00"), method)));

	}

	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(RuntimeStatusQueryBeanTest.class);
}
