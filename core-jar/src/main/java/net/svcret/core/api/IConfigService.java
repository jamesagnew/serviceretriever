package net.svcret.core.api;

import net.svcret.admin.api.UnexpectedFailureException;
import net.svcret.admin.shared.model.RetrieverNodeTypeEnum;
import net.svcret.core.model.entity.PersConfig;

public interface IConfigService {

	PersConfig getConfig() throws UnexpectedFailureException;

	PersConfig saveConfig(PersConfig theConfig) throws UnexpectedFailureException;

	void reloadConfigIfNeeded();

	RetrieverNodeTypeEnum getNodeType();

	String getNodeId();

}
