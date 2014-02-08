package net.svcret.core.chart;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.svcret.admin.shared.enm.InvocationStatsIntervalEnum;
import net.svcret.admin.shared.model.TimeRange;
import net.svcret.admin.shared.model.TimeRangeEnum;
import net.svcret.core.api.IConfigService;
import net.svcret.core.api.IDao;
import net.svcret.core.api.IRuntimeStatusQueryLocal;
import net.svcret.core.chart.ChartingServiceBean;
import net.svcret.core.ejb.nodecomm.IBroadcastSender;
import net.svcret.core.model.entity.BasePersServiceVersion;
import net.svcret.core.model.entity.PersConfig;
import net.svcret.core.model.entity.PersInvocationMethodSvcverStats;
import net.svcret.core.model.entity.PersInvocationMethodSvcverStatsPk;
import net.svcret.core.model.entity.PersInvocationMethodUserStats;
import net.svcret.core.model.entity.PersInvocationMethodUserStatsPk;
import net.svcret.core.model.entity.PersMethod;
import net.svcret.core.model.entity.PersUser;

import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.stubbing.defaultanswers.ReturnsDeepStubs;

public class ChartingServiceBeanTest {

	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(ChartingServiceBeanTest.class);
	private PersConfig myConfig;
	private IConfigService myConfigSvc;
	private IDao myDao;
	private SimpleDateFormat myFmt;
	private SimpleDateFormat myFmtSecs;
	private IRuntimeStatusQueryLocal myStatus;

	private ChartingServiceBean mySvc;

	@Before
	public void before() throws Exception {
		myFmt = new SimpleDateFormat("HH:mm");
		myFmtSecs = new SimpleDateFormat("HH:mm:ss");

		mySvc = new ChartingServiceBean();

		myConfigSvc = mock(IConfigService.class);
		myConfig = new PersConfig();
		myConfig.setNow(myFmt.parse("14:05").getTime());
		when(myConfigSvc.getConfig()).thenReturn(myConfig);

		mySvc.setConfigForUnitTest(myConfigSvc);

		myDao = mock(IDao.class);
		mySvc.setDaoForUnitTest(myDao);

		mySvc.setBroadcastSenderForUnitTest(mock(IBroadcastSender.class));
		myStatus = mock(IRuntimeStatusQueryLocal.class);
		mySvc.setStatusForUnitTest(myStatus);

	}

	@Test
	public void testGenerateUserMethodGraph() throws Exception {

		PersUser user = new PersUser();
		user.setPid(1L);

		TimeRange range = new TimeRange();
		range.setNoPresetFrom(myFmtSecs.parse("04:00:01"));
		range.setNoPresetTo(myFmtSecs.parse("14:00:01"));

		PersMethod m1 = mock(PersMethod.class, new ReturnsDeepStubs());
		when(m1.getPid()).thenReturn(11L);
		when(m1.getName()).thenReturn("MethodName123");
		when(m1.getServiceVersion().getVersionId()).thenReturn("Version1");
		when(m1.getServiceVersion().getService().getServiceId()).thenReturn("Service1");
		when(m1.getServiceVersion().getService().getDomain().getDomainId()).thenReturn("Domain1");
		PersMethod m2 = mock(PersMethod.class, new ReturnsDeepStubs());
		when(m2.getPid()).thenReturn(21L);
		when(m2.getName()).thenReturn("MethodName22");
		when(m2.getServiceVersion().getVersionId()).thenReturn("Version1");
		when(m2.getServiceVersion().getService().getServiceId()).thenReturn("Service1");
		when(m2.getServiceVersion().getService().getDomain().getDomainId()).thenReturn("Domain1");
		PersMethod m3 = mock(PersMethod.class, new ReturnsDeepStubs());
		when(m3.getPid()).thenReturn(31L);
		when(m3.getName()).thenReturn("MethodName3");
		when(m3.getServiceVersion().getVersionId()).thenReturn("Version2");
		when(m3.getServiceVersion().getService().getServiceId()).thenReturn("Service2");
		when(m3.getServiceVersion().getService().getDomain().getDomainId()).thenReturn("Domain1");

		when(myDao.getUser(eq(1L))).thenReturn(user);
		when(myDao.getServiceVersionMethodByPid(eq(11L))).thenReturn(m1);
		when(myDao.getServiceVersionMethodByPid(eq(21L))).thenReturn(m2);
		when(myDao.getServiceVersionMethodByPid(eq(31L))).thenReturn(m3);

		List<PersInvocationMethodUserStats> statsList = new ArrayList<PersInvocationMethodUserStats>();
		statsList.add(new PersInvocationMethodUserStats(new PersInvocationMethodUserStatsPk(InvocationStatsIntervalEnum.TEN_MINUTE, myFmt.parse("04:10"), m1, user)));
		statsList.get(statsList.size() - 1).addSuccessInvocation(100, 200, 300);
		statsList.add(new PersInvocationMethodUserStats(new PersInvocationMethodUserStatsPk(InvocationStatsIntervalEnum.TEN_MINUTE, myFmt.parse("04:20"), m1, user)));
		statsList.get(statsList.size() - 1).addSuccessInvocation(100, 200, 300);
		statsList.add(new PersInvocationMethodUserStats(new PersInvocationMethodUserStatsPk(InvocationStatsIntervalEnum.TEN_MINUTE, myFmt.parse("04:30"), m1, user)));
		statsList.get(statsList.size() - 1).addSuccessInvocation(100, 200, 300);
		statsList.add(new PersInvocationMethodUserStats(new PersInvocationMethodUserStatsPk(InvocationStatsIntervalEnum.TEN_MINUTE, myFmt.parse("04:30"), m2, user)));
		statsList.get(statsList.size() - 1).addSuccessInvocation(100, 200, 300);
		statsList.add(new PersInvocationMethodUserStats(new PersInvocationMethodUserStatsPk(InvocationStatsIntervalEnum.TEN_MINUTE, myFmt.parse("04:40"), m2, user)));
		statsList.get(statsList.size() - 1).addSuccessInvocation(100, 200, 300);
		statsList.add(new PersInvocationMethodUserStats(new PersInvocationMethodUserStatsPk(InvocationStatsIntervalEnum.TEN_MINUTE, myFmt.parse("04:40"), m3, user)));
		statsList.get(statsList.size() - 1).addSuccessInvocation(100, 200, 300);

		when(myDao.getUserStatsWithinTimeRange(eq(user), eq(range.getNoPresetFrom()), eq(range.getNoPresetTo()))).thenReturn(statsList);

		byte[] bytes = mySvc.renderUserMethodGraphForUser(user.getPid(), range);

		new File("target/test-pngs").mkdirs();
		FileOutputStream fos = new FileOutputStream("target/test-pngs/userMethod.png", false);
		fos.write(bytes);
		fos.close();

	}

	@Test
	public void testGenerateUserMethodGraphNoData() throws Exception {

		PersUser user = new PersUser();
		user.setPid(1L);

		TimeRange range = new TimeRange();
		range.setNoPresetFrom(myFmt.parse("04:00"));
		range.setNoPresetTo(myFmt.parse("14:00"));

		when(myDao.getUser(eq(1L))).thenReturn(user);

		List<PersInvocationMethodUserStats> statsList = new ArrayList<PersInvocationMethodUserStats>();
		when(myDao.getUserStatsWithinTimeRange(eq(user), eq(range.getNoPresetFrom()), eq(range.getNoPresetTo()))).thenReturn(statsList);

		byte[] bytes = mySvc.renderUserMethodGraphForUser(user.getPid(), range);

		new File("target/test-pngs").mkdirs();
		FileOutputStream fos = new FileOutputStream("target/test-pngs/userMethodNoData.png", false);
		fos.write(bytes);
		fos.close();

	}

	@Test
	public void testRenderSvcVerLatencyMethodGraph() throws Exception {

		TimeRange range = new TimeRange();
		range.setNoPresetFrom(myFmtSecs.parse("04:00:01"));
		range.setNoPresetTo(myFmtSecs.parse("05:00:01"));

		PersMethod m1 = mock(PersMethod.class, new ReturnsDeepStubs());
		when(m1.getPid()).thenReturn(11L);
		when(m1.getName()).thenReturn("MethodName123");
		when(m1.getServiceVersion().getVersionId()).thenReturn("Version1");
		when(m1.getServiceVersion().getService().getServiceId()).thenReturn("Service1");
		when(m1.getServiceVersion().getService().getDomain().getDomainId()).thenReturn("Domain1");
		PersMethod m2 = mock(PersMethod.class, new ReturnsDeepStubs());
		when(m2.getPid()).thenReturn(21L);
		when(m2.getName()).thenReturn("MethodName22");
		when(m2.getServiceVersion().getVersionId()).thenReturn("Version1");
		when(m2.getServiceVersion().getService().getServiceId()).thenReturn("Service1");
		when(m2.getServiceVersion().getService().getDomain().getDomainId()).thenReturn("Domain1");
		PersMethod m3 = mock(PersMethod.class, new ReturnsDeepStubs());
		when(m3.getPid()).thenReturn(31L);
		when(m3.getName()).thenReturn("MethodName3");
		when(m3.getServiceVersion().getVersionId()).thenReturn("Version2");
		when(m3.getServiceVersion().getService().getServiceId()).thenReturn("Service2");
		when(m3.getServiceVersion().getService().getDomain().getDomainId()).thenReturn("Domain1");
		PersMethod m4 = mock(PersMethod.class, new ReturnsDeepStubs());
		when(m4.getPid()).thenReturn(41L);
		when(m4.getName()).thenReturn("MethodName4");
		when(m4.getServiceVersion().getVersionId()).thenReturn("Version2");
		when(m4.getServiceVersion().getService().getServiceId()).thenReturn("Service2");
		when(m4.getServiceVersion().getService().getDomain().getDomainId()).thenReturn("Domain1");

		BasePersServiceVersion svcVer = mock(BasePersServiceVersion.class, new ReturnsDeepStubs());
		when(svcVer.getVersionId()).thenReturn("1.0");
		when(svcVer.getService().getServiceId()).thenReturn("ServiceId");
		when(svcVer.getService().getDomain().getDomainId()).thenReturn("DomainId");

		ArrayList<PersMethod> methodList = new ArrayList<PersMethod>();
		methodList.add(m1);
		methodList.add(m2);
		methodList.add(m3);
		methodList.add(m4);
		when(svcVer.getMethods()).thenReturn(methodList);

		when(myDao.getServiceVersionByPid(eq(999L))).thenReturn(svcVer);
		when(myDao.getServiceVersionMethodByPid(eq(11L))).thenReturn(m1);
		when(myDao.getServiceVersionMethodByPid(eq(21L))).thenReturn(m2);
		when(myDao.getServiceVersionMethodByPid(eq(31L))).thenReturn(m3);

		List<PersInvocationMethodSvcverStats> statsList = new ArrayList<PersInvocationMethodSvcverStats>();
		statsList.add(new PersInvocationMethodSvcverStats(new PersInvocationMethodSvcverStatsPk(InvocationStatsIntervalEnum.TEN_MINUTE, myFmt.parse("04:00"), m1)));
		statsList.get(statsList.size() - 1).addSuccessInvocation((int) (100.0 * Math.random()), (int) (100.0 * Math.random()), (int) (100.0 * Math.random()));
		statsList.add(new PersInvocationMethodSvcverStats(new PersInvocationMethodSvcverStatsPk(InvocationStatsIntervalEnum.TEN_MINUTE, myFmt.parse("04:00"), m2)));
		statsList.get(statsList.size() - 1).addSuccessInvocation((int) (100.0 * Math.random()), (int) (100.0 * Math.random()), (int) (100.0 * Math.random()));
		statsList.add(new PersInvocationMethodSvcverStats(new PersInvocationMethodSvcverStatsPk(InvocationStatsIntervalEnum.TEN_MINUTE, myFmt.parse("04:00"), m3)));
		statsList.get(statsList.size() - 1).addSuccessInvocation((int) (100.0 * Math.random()), (int) (100.0 * Math.random()), (int) (100.0 * Math.random()));
		statsList.add(new PersInvocationMethodSvcverStats(new PersInvocationMethodSvcverStatsPk(InvocationStatsIntervalEnum.TEN_MINUTE, myFmt.parse("04:10"), m1)));
		statsList.get(statsList.size() - 1).addSuccessInvocation((int) (100.0 * Math.random()), (int) (100.0 * Math.random()), (int) (100.0 * Math.random()));
		statsList.add(new PersInvocationMethodSvcverStats(new PersInvocationMethodSvcverStatsPk(InvocationStatsIntervalEnum.TEN_MINUTE, myFmt.parse("04:10"), m2)));
		statsList.add(new PersInvocationMethodSvcverStats(new PersInvocationMethodSvcverStatsPk(InvocationStatsIntervalEnum.TEN_MINUTE, myFmt.parse("04:10"), m3)));
		statsList.add(new PersInvocationMethodSvcverStats(new PersInvocationMethodSvcverStatsPk(InvocationStatsIntervalEnum.TEN_MINUTE, myFmt.parse("04:20"), m1)));
		statsList.get(statsList.size() - 1).addSuccessInvocation((int) (100.0 * Math.random()), (int) (100.0 * Math.random()), (int) (100.0 * Math.random()));
		statsList.add(new PersInvocationMethodSvcverStats(new PersInvocationMethodSvcverStatsPk(InvocationStatsIntervalEnum.TEN_MINUTE, myFmt.parse("04:20"), m2)));
		statsList.add(new PersInvocationMethodSvcverStats(new PersInvocationMethodSvcverStatsPk(InvocationStatsIntervalEnum.TEN_MINUTE, myFmt.parse("04:20"), m3)));
		statsList.add(new PersInvocationMethodSvcverStats(new PersInvocationMethodSvcverStatsPk(InvocationStatsIntervalEnum.TEN_MINUTE, myFmt.parse("04:30"), m1)));
		statsList.get(statsList.size() - 1).addSuccessInvocation((int) (100.0 * Math.random()), (int) (100.0 * Math.random()), (int) (100.0 * Math.random()));
		statsList.add(new PersInvocationMethodSvcverStats(new PersInvocationMethodSvcverStatsPk(InvocationStatsIntervalEnum.TEN_MINUTE, myFmt.parse("04:30"), m2)));
		statsList.get(statsList.size() - 1).addSuccessInvocation((int) (100.0 * Math.random()), (int) (100.0 * Math.random()), (int) (100.0 * Math.random()));
		statsList.add(new PersInvocationMethodSvcverStats(new PersInvocationMethodSvcverStatsPk(InvocationStatsIntervalEnum.TEN_MINUTE, myFmt.parse("04:30"), m3)));
		statsList.add(new PersInvocationMethodSvcverStats(new PersInvocationMethodSvcverStatsPk(InvocationStatsIntervalEnum.TEN_MINUTE, myFmt.parse("04:40"), m1)));
		statsList.add(new PersInvocationMethodSvcverStats(new PersInvocationMethodSvcverStatsPk(InvocationStatsIntervalEnum.TEN_MINUTE, myFmt.parse("04:40"), m2)));
		statsList.get(statsList.size() - 1).addSuccessInvocation((int) (100.0 * Math.random()), (int) (100.0 * Math.random()), (int) (100.0 * Math.random()));
		statsList.add(new PersInvocationMethodSvcverStats(new PersInvocationMethodSvcverStatsPk(InvocationStatsIntervalEnum.TEN_MINUTE, myFmt.parse("04:40"), m3)));
		statsList.get(statsList.size() - 1).addSuccessInvocation((int) (100.0 * Math.random()), (int) (100.0 * Math.random()), (int) (100.0 * Math.random()));
		statsList.add(new PersInvocationMethodSvcverStats(new PersInvocationMethodSvcverStatsPk(InvocationStatsIntervalEnum.TEN_MINUTE, myFmt.parse("04:50"), m1)));
		statsList.add(new PersInvocationMethodSvcverStats(new PersInvocationMethodSvcverStatsPk(InvocationStatsIntervalEnum.TEN_MINUTE, myFmt.parse("04:50"), m2)));
		statsList.add(new PersInvocationMethodSvcverStats(new PersInvocationMethodSvcverStatsPk(InvocationStatsIntervalEnum.TEN_MINUTE, myFmt.parse("04:50"), m3)));
		statsList.add(new PersInvocationMethodSvcverStats(new PersInvocationMethodSvcverStatsPk(InvocationStatsIntervalEnum.TEN_MINUTE, myFmt.parse("05:00"), m1)));
		statsList.add(new PersInvocationMethodSvcverStats(new PersInvocationMethodSvcverStatsPk(InvocationStatsIntervalEnum.TEN_MINUTE, myFmt.parse("05:00"), m2)));
		statsList.add(new PersInvocationMethodSvcverStats(new PersInvocationMethodSvcverStatsPk(InvocationStatsIntervalEnum.TEN_MINUTE, myFmt.parse("05:00"), m3)));

		statsList.add(new PersInvocationMethodSvcverStats(new PersInvocationMethodSvcverStatsPk(InvocationStatsIntervalEnum.TEN_MINUTE, myFmt.parse("04:00"), m4)));
		statsList.add(new PersInvocationMethodSvcverStats(new PersInvocationMethodSvcverStatsPk(InvocationStatsIntervalEnum.TEN_MINUTE, myFmt.parse("04:10"), m4)));
		statsList.add(new PersInvocationMethodSvcverStats(new PersInvocationMethodSvcverStatsPk(InvocationStatsIntervalEnum.TEN_MINUTE, myFmt.parse("04:20"), m4)));
		statsList.add(new PersInvocationMethodSvcverStats(new PersInvocationMethodSvcverStatsPk(InvocationStatsIntervalEnum.TEN_MINUTE, myFmt.parse("04:30"), m4)));
		statsList.add(new PersInvocationMethodSvcverStats(new PersInvocationMethodSvcverStatsPk(InvocationStatsIntervalEnum.TEN_MINUTE, myFmt.parse("04:40"), m4)));
		statsList.add(new PersInvocationMethodSvcverStats(new PersInvocationMethodSvcverStatsPk(InvocationStatsIntervalEnum.TEN_MINUTE, myFmt.parse("04:50"), m4)));
		statsList.add(new PersInvocationMethodSvcverStats(new PersInvocationMethodSvcverStatsPk(InvocationStatsIntervalEnum.TEN_MINUTE, myFmt.parse("05:00"), m4)));

		for (PersInvocationMethodSvcverStats next : statsList) {
			when(myStatus.getInvocationStatsSynchronously(next.getPk())).thenReturn(next);
		}

		// myDao.getInvo
		// when(myDao.getUserStatsWithinTimeRange(eq(user),
		// eq(range.getNoPresetFrom()),
		// eq(range.getNoPresetTo()))).thenReturn(statsList);

		new File("target/test-pngs").mkdirs();

		byte[] bytes = mySvc.renderSvcVerLatencyMethodGraph(999L, range, true);
		FileOutputStream fos = new FileOutputStream("target/test-pngs/svcVerLatency-bymethod.png", false);
		fos.write(bytes);
		fos.close();

		bytes = mySvc.renderSvcVerLatencyMethodGraph(999L, range, false);
		fos = new FileOutputStream("target/test-pngs/svcVerLatency-avg.png", false);
		fos.write(bytes);
		fos.close();

	}

	@Test
	public void testRenderSvcVerLatencyMethodGraphForLongTimerange() throws Exception {
		myConfig.setNow(new Date().getTime());

		TimeRange range = new TimeRange();
		range.setWithPresetRange(TimeRangeEnum.SIX_MONTH);

		PersMethod m1 = mock(PersMethod.class, new ReturnsDeepStubs());
		when(m1.getPid()).thenReturn(11L);
		when(m1.getName()).thenReturn("MethodName123");
		when(m1.getServiceVersion().getVersionId()).thenReturn("Version1");
		when(m1.getServiceVersion().getService().getServiceId()).thenReturn("Service1");
		when(m1.getServiceVersion().getService().getDomain().getDomainId()).thenReturn("Domain1");

		BasePersServiceVersion svcVer = mock(BasePersServiceVersion.class, new ReturnsDeepStubs());
		when(svcVer.getVersionId()).thenReturn("1.0");
		when(svcVer.getService().getServiceId()).thenReturn("ServiceId");
		when(svcVer.getService().getDomain().getDomainId()).thenReturn("DomainId");

		ArrayList<PersMethod> methodList = new ArrayList<PersMethod>();
		methodList.add(m1);
		when(svcVer.getMethods()).thenReturn(methodList);

		when(myDao.getServiceVersionByPid(eq(999L))).thenReturn(svcVer);
		when(myDao.getServiceVersionMethodByPid(eq(11L))).thenReturn(m1);

		List<PersInvocationMethodSvcverStats> statsList = new ArrayList<PersInvocationMethodSvcverStats>();
		statsList.add(new PersInvocationMethodSvcverStats(new PersInvocationMethodSvcverStatsPk(InvocationStatsIntervalEnum.TEN_MINUTE, myFmt.parse("04:00"), m1)));
		statsList.get(statsList.size() - 1).addSuccessInvocation((int) (100.0 * Math.random()), (int) (100.0 * Math.random()), (int) (100.0 * Math.random()));
		statsList.add(new PersInvocationMethodSvcverStats(new PersInvocationMethodSvcverStatsPk(InvocationStatsIntervalEnum.TEN_MINUTE, myFmt.parse("04:10"), m1)));
		statsList.get(statsList.size() - 1).addSuccessInvocation((int) (100.0 * Math.random()), (int) (100.0 * Math.random()), (int) (100.0 * Math.random()));
		statsList.add(new PersInvocationMethodSvcverStats(new PersInvocationMethodSvcverStatsPk(InvocationStatsIntervalEnum.TEN_MINUTE, myFmt.parse("04:20"), m1)));
		statsList.get(statsList.size() - 1).addSuccessInvocation((int) (100.0 * Math.random()), (int) (100.0 * Math.random()), (int) (100.0 * Math.random()));
		statsList.add(new PersInvocationMethodSvcverStats(new PersInvocationMethodSvcverStatsPk(InvocationStatsIntervalEnum.TEN_MINUTE, myFmt.parse("04:30"), m1)));
		statsList.get(statsList.size() - 1).addSuccessInvocation((int) (100.0 * Math.random()), (int) (100.0 * Math.random()), (int) (100.0 * Math.random()));
		statsList.add(new PersInvocationMethodSvcverStats(new PersInvocationMethodSvcverStatsPk(InvocationStatsIntervalEnum.TEN_MINUTE, myFmt.parse("04:40"), m1)));
		statsList.get(statsList.size() - 1).addSuccessInvocation((int) (100.0 * Math.random()), (int) (100.0 * Math.random()), (int) (100.0 * Math.random()));
		statsList.add(new PersInvocationMethodSvcverStats(new PersInvocationMethodSvcverStatsPk(InvocationStatsIntervalEnum.TEN_MINUTE, myFmt.parse("04:50"), m1)));
		statsList.add(new PersInvocationMethodSvcverStats(new PersInvocationMethodSvcverStatsPk(InvocationStatsIntervalEnum.TEN_MINUTE, myFmt.parse("05:00"), m1)));

		InvocationHandler h = new InvocationHandler() {
			@Override
			public Object invoke(Object theProxy, Method theMethod, Object[] theArgs) throws Throwable {
				if (!"getInvocationStatsSynchronously".equals(theMethod.getName())) {
					throw new Exception("Unexpected method: " + theMethod.getName());
				}
				PersInvocationMethodSvcverStatsPk pk = (PersInvocationMethodSvcverStatsPk) theArgs[0];
				return new PersInvocationMethodSvcverStats(pk);
			}
		};
		IRuntimeStatusQueryLocal p = (IRuntimeStatusQueryLocal) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class[] { IRuntimeStatusQueryLocal.class }, h);
		mySvc.setStatusForUnitTest(p);

		byte[] bytes = mySvc.renderSvcVerLatencyMethodGraph(999L, range, true);

		FileOutputStream fos = new FileOutputStream("target/test-pngs/sixmonth.png", false);
		fos.write(bytes);
		fos.close();

	}

}
