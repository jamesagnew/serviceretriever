package net.svcret.ejb.model.entity;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.ThreadMXBean;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.hibernate.annotations.Index;

//@formatter:off
@org.hibernate.annotations.Table(indexes = {
	@Index(columnNames = { "START_TIME", "INTRVL" }, name = "IDX_PNS_TIME_AND_IVL")
}, appliesTo = "PX_NODE_STATS")
@Table(name = "PX_NODE_STATS")
@NamedQueries(value = {
	@NamedQuery(name=Queries.PERS_NODESTATS_FINDINTERVAL, query=Queries.PERS_NODESTATS_FINDINTERVAL_Q)
})
@Entity()
// @formatter:on
public class PersNodeStats extends BasePersStats<PersNodeStatsPk, PersNodeStats> {

	private static long ourLastCpuTime;
	private static long ourLastCpuTimeTotal;
	private static double ourLastCpuTimeValue = -1;

	public long getMemoryCommitted() {
		return myMemoryCommitted;
	}

	public void setMemoryCommitted(long theMemoryCommitted) {
		myMemoryCommitted = theMemoryCommitted;
	}

	public long getMemoryMax() {
		return myMemoryMax;
	}

	public void setMemoryMax(long theMemoryMax) {
		myMemoryMax = theMemoryMax;
	}

	public long getMemoryUsed() {
		return myMemoryUsed;
	}

	public void setMemoryUsed(long theMemoryUsed) {
		myMemoryUsed = theMemoryUsed;
	}

	public long getMethodInvocations() {
		return myMethodInvocations;
	}

	public void setMethodInvocations(int theMethodInvocations) {
		myMethodInvocations = theMethodInvocations;
	}

	public double getCpuTime() {
		return (myCpuTime / 100.0);
	}

	public void setCpuTime(double theCpuTime) {
		myCpuTime = (int) (theCpuTime*100.0);
	}

	private static final long serialVersionUID = 1L;

	@Column(name = "MEM_COMMITTED")
	private long myMemoryCommitted;

	@Column(name = "MEM_MAX")
	private long myMemoryMax;

	@Column(name = "MEM_USED")
	private long myMemoryUsed;

	@Column(name = "METHOD_INVOCS")
	private long myMethodInvocations;

	@EmbeddedId
	private PersNodeStatsPk myPk;

	@Column(name = "CPU_TIME")
	private int myCpuTime;

	public PersNodeStats() {
		// nothing
	}

	public PersNodeStats(InvocationStatsIntervalEnum theInterval, Date theStartTime, String theNodeId) {
		myPk = new PersNodeStatsPk(theInterval, theStartTime, theNodeId);
	}

	public PersNodeStats(PersNodeStatsPk thePk) {
		myPk = thePk;
	}

	public synchronized void collectMemoryStats() {

		MemoryMXBean mem = ManagementFactory.getMemoryMXBean();
		myMemoryMax = mem.getHeapMemoryUsage().getMax();
		myMemoryUsed = mem.getHeapMemoryUsage().getUsed();
		myMemoryCommitted = mem.getHeapMemoryUsage().getCommitted();

		synchronized (PersNodeStats.class) {
			long now = System.nanoTime();
			if (now + (10 * 1000000000) < ourLastCpuTime && ourLastCpuTimeValue > -1) {
				setCpuTime(ourLastCpuTimeValue);
			} else {
				long processCpuTime = 0;
				final ThreadMXBean bean = ManagementFactory.getThreadMXBean();
				final long[] ids = bean.getAllThreadIds();
				for (long id : ids) {
					processCpuTime += bean.getThreadCpuTime(id);
				}

				if (ourLastCpuTimeTotal > 0) {
					long elapsed = now - ourLastCpuTime;
					setCpuTime((double) (processCpuTime - ourLastCpuTimeTotal) / (double) elapsed);
					ourLastCpuTimeValue = myCpuTime;
				}
				ourLastCpuTimeTotal = processCpuTime;
				ourLastCpuTime = now;
			}
		}
	}

	public PersNodeStatsPk getPk() {
		return myPk;
	}

	@Override
	public synchronized void mergeUnsynchronizedEvents(PersNodeStats theNext) {
		if (getCpuTime() < theNext.getCpuTime()) {
			setCpuTime(theNext.getCpuTime());
		}
		if (myMemoryCommitted < theNext.getMemoryCommitted()) {
			myMemoryCommitted = theNext.getMemoryCommitted();
		}
		if (myMemoryUsed < theNext.getMemoryUsed()) {
			myMemoryUsed = theNext.getMemoryUsed();
		}
		if (myMemoryMax < theNext.getMemoryMax()) {
			myMemoryMax = theNext.getMemoryMax();
		}
		myMethodInvocations += theNext.getMethodInvocations();
	}

	@Override
	public String toString() {
		ToStringBuilder b = new ToStringBuilder(this);
		return b.toString();
	}

	public static void main(String[] args) {
		// getProcessCpuTime

		PersNodeStats s = new PersNodeStats();
		s.collectMemoryStats();
		System.out.println(s);
	}

	@Override
	public <T> T accept(IStatsVisitor<T> theVisitor) {
		return theVisitor.visit(this, getPk());
	}

}
