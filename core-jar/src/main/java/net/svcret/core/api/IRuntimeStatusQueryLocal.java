package net.svcret.core.api;

import java.util.Collection;

import net.svcret.admin.api.UnexpectedFailureException;
import net.svcret.admin.shared.model.DtoNodeStatus;
import net.svcret.admin.shared.model.DtoNodeStatusAndStatisticsList;
import net.svcret.core.model.entity.BasePersServiceCatalogItem;
import net.svcret.core.model.entity.BasePersStats;
import net.svcret.core.model.entity.BasePersStatsPk;
import net.svcret.core.model.entity.PersMethod;
import net.svcret.core.model.entity.PersServiceVersionUrl;
import net.svcret.core.model.entity.PersUser;
import net.svcret.core.status.RuntimeStatusQueryBean.StatsAccumulator;

public interface IRuntimeStatusQueryLocal {

	void extract60MinuteMethodStats(PersMethod theMethod, StatsAccumulator theAccumulator) throws UnexpectedFailureException;

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

	Collection<DtoNodeStatus> getAllNodeStatuses();

	DtoNodeStatusAndStatisticsList getAllNodeStatusesAndStatistics();

}
