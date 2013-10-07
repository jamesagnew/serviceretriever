package net.svcret.ejb.ejb.nodecomm;

import javax.ejb.Local;

@Local
public interface ISynchronousNodeIpcClient {

	void invokeFlushRuntimeStatus();

	void invokeFlushTransactionLogs();

}
