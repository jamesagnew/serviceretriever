package net.svcret.ejb.api;

import javax.ejb.Local;

import net.svcret.ejb.model.entity.hl7.PersServiceVersionHl7OverHttp;

@Local
public interface IServiceInvokerHl7OverHttp extends IServiceInvoker<PersServiceVersionHl7OverHttp> {

}
