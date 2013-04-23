package net.svcret.ejb.api;

import javax.ejb.Local;

import net.svcret.ejb.ex.ProcessingException;
import net.svcret.ejb.model.entity.PersConfig;

@Local
public interface IConfigService {

	PersConfig getConfig() throws ProcessingException;

	PersConfig saveConfig(PersConfig theConfig) throws ProcessingException;

	void reloadConfigIfNeeded();

}
