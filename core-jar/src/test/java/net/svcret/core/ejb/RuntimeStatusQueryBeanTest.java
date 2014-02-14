package net.svcret.core.ejb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.svcret.admin.shared.enm.InvocationStatsIntervalEnum;
import net.svcret.admin.shared.model.DtoNodeStatusAndStatisticsList;
import net.svcret.admin.shared.model.TimeRange;
import net.svcret.admin.shared.model.TimeRangeEnum;
import net.svcret.core.admin.AdminServiceBean;
import net.svcret.core.admin.AdminServiceBean.IWithStats;
import net.svcret.core.api.IConfigService;
import net.svcret.core.api.IDao;
import net.svcret.core.api.IRuntimeStatusQueryLocal;
import net.svcret.core.model.entity.BasePersStatsPk;
import net.svcret.core.model.entity.PersConfig;
import net.svcret.core.model.entity.PersDomain;
import net.svcret.core.model.entity.PersInvocationMethodSvcverStats;
import net.svcret.core.model.entity.PersInvocationMethodSvcverStatsPk;
import net.svcret.core.model.entity.PersMethod;
import net.svcret.core.model.entity.PersNodeStats;
import net.svcret.core.model.entity.PersNodeStatus;
import net.svcret.core.model.entity.PersService;
import net.svcret.core.model.entity.soap.PersServiceVersionSoap11;
import net.svcret.core.status.RuntimeStatusQueryBean;

import org.apache.commons.lang3.time.DateUtils;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.internal.stubbing.answers.ThrowsException;
import org.mockito.internal.stubbing.defaultanswers.ReturnsDeepStubs;
import org.mockito.internal.stubbing.defaultanswers.ReturnsSmartNulls;

public class RuntimeStatusQueryBeanTest {

	private static long ourNextPid;

	@SuppressWarnings("static-method")
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

	private DateFormat myTimeFormat = new SimpleDateFormat("HH:mm:ss");

	@Test
	public void testGetNodeStats() throws Exception {
		
		System.out.println(myTimeFormat.parse("12:13:00"));
		System.out.println(myTimeFormat.parse("12:13:00").getTime());
		System.out.println(myTimeFormat.parse("12:14:00"));
		System.out.println(myTimeFormat.parse("12:14:00").getTime());
		
		IConfigService configSvc = mock(IConfigService.class, new ReturnsDeepStubs());
		IDao dao = mock(IDao.class, new ReturnsSmartNulls());

		RuntimeStatusQueryBean svc = new RuntimeStatusQueryBean();
		svc.setConfigSvcForUnitTests(configSvc);
		svc.setDaoForUnitTests(dao);
		
		List<PersNodeStats> allStats = new ArrayList<>();
		PersNodeStats stats = new PersNodeStats(InvocationStatsIntervalEnum.MINUTE, myTimeFormat.parse("12:13:00"), "nodeid");
		stats.setCpuTime(100.0);
		stats.addMethodInvocations(1, 2, 3, 4);
		allStats.add(stats);

		stats = new PersNodeStats(InvocationStatsIntervalEnum.MINUTE, myTimeFormat.parse("12:14:00"), "nodeid");
		stats.setCpuTime(200.0);
		stats.addMethodInvocations(1, 2, 3, 4);
		allStats.add(stats);

		stats = new PersNodeStats(InvocationStatsIntervalEnum.MINUTE, myTimeFormat.parse("11:14:00"), "nodeid");
		stats.setCpuTime(200.0);
		stats.addMethodInvocations(1, 2, 3, 4);
		allStats.add(stats);

		stats = new PersNodeStats(InvocationStatsIntervalEnum.MINUTE, myTimeFormat.parse("12:14:00"), "nodeid2");
		stats.setCpuTime(200.0);
		stats.addMethodInvocations(1, 2, 3, 4);
		allStats.add(stats);

		when(dao.getNodeStatsWithinRange((Date)any(), (Date)any())).thenReturn(allStats);
		
		List <PersNodeStatus> nodeStatuses=new ArrayList<>();
		PersNodeStatus nodeStatus = new PersNodeStatus();
		nodeStatus.setNodeActiveSince(new Date());
		nodeStatus.setNodeLastTransactionIfNewer(new Date());
		nodeStatus.setStatusTimestamp(new Date());
		nodeStatus.setNodeId("nodeid");
		nodeStatuses.add(nodeStatus);

		PersNodeStatus nodeStatus2 = new PersNodeStatus();
		nodeStatus2.setNodeActiveSince(new Date());
		nodeStatus2.setNodeLastTransactionIfNewer(new Date());
		nodeStatus2.setStatusTimestamp(new Date());
		nodeStatus2.setNodeId("nodeid2");
		nodeStatuses.add(nodeStatus2);
		
		when(dao.getAllNodeStatuses()).thenReturn(nodeStatuses);
		when(configSvc.getConfig().getCollapseStatsIntervalForDate((Date)any())).thenReturn(InvocationStatsIntervalEnum.MINUTE);
		
		svc.setNowForUnitTests(myTimeFormat.parse("13:00:01"));
		DtoNodeStatusAndStatisticsList actual = svc.getAllNodeStatusesAndStatistics();
		assertEquals(100.0, actual.getNodeStatistics().get(0).getCpuTime()[14], 0.01);
		assertEquals(200.0, actual.getNodeStatistics().get(0).getCpuTime()[15], 0.01);
		
	}

	@SuppressWarnings({ "rawtypes", "unchecked", "static-method" })
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

		final Set<BasePersStatsPk> all = new HashSet<>();
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
