package net.svcret.ejb.api;

import java.util.Collection;

import javax.ejb.Local;

import net.svcret.ejb.ejb.RuntimeStatusQueryBean.StatsAccumulator;
import net.svcret.ejb.ex.UnexpectedFailureException;
import net.svcret.ejb.model.entity.BasePersServiceCatalogItem;
import net.svcret.ejb.model.entity.BasePersStats;
import net.svcret.ejb.model.entity.BasePersStatsPk;
import net.svcret.ejb.model.entity.PersNodeStatus;
import net.svcret.ejb.model.entity.PersServiceVersionMethod;
import net.svcret.ejb.model.entity.PersServiceVersionUrl;
import net.svcret.ejb.model.entity.PersUser;

@Local
public interface IRuntimeStatusQueryLocal {

	void extract60MinuteMethodStats(PersServiceVersionMethod theMethod, StatsAccumulator theAccumulator) throws UnexpectedFailureException;

	void extract60MinuteServiceVersionUrlStatistics(PersServiceVersionUrl theUrl, StatsAccumulator theAccumulator) throws UnexpectedFailureException;

	void extract60MinuteUserStats(PersUser thePersUser, StatsAccumulator theAccumulator) throws UnexpectedFailureException;

	int getCachedEmptyKeyCount();

	int getCachedPopulatedKeyCount();

	<P extends BasePersStatsPk<P, O>, O extends BasePersStats<P, O>> O getInvocationStatsSynchronously(P thePk);

	int getMaxCachedNullStatCount();

	int getMaxCachedPopulatedStatCount();

	void purgeCachedStats();

	void setMaxCachedNullStatCount(int theCount);

	void setMaxCachedPopulatedStatCount(int theCount);

	StatsAccumulator extract60MinuteStats(BasePersServiceCatalogItem theItem) throws UnexpectedFailureException;

	Collection<PersNodeStatus> getAllNodeStatuses();

}
