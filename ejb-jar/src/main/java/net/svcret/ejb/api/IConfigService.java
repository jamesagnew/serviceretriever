package net.svcret.ejb.api;

import java.util.List;

import net.svcret.admin.api.UnexpectedFailureException;
import net.svcret.admin.shared.model.RetrieverNodeTypeEnum;
import net.svcret.ejb.model.entity.PersConfig;

public interface IConfigService {

	PersConfig getConfig() throws UnexpectedFailureException;

	PersConfig saveConfig(PersConfig theConfig) throws UnexpectedFailureException;

	void reloadConfigIfNeeded();

	RetrieverNodeTypeEnum getNodeType();

	List<String> getSecondaryNodeRefreshUrls();

	String getNodeId();

	String getFilesystemAuditLoggerPath();
	
}
