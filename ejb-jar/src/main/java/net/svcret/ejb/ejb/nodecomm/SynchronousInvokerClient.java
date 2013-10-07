package net.svcret.ejb.ejb.nodecomm;

import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

public class SynchronousInvokerClient extends Service {

	public SynchronousInvokerClient(String theWsdlUrl) throws MalformedURLException {
		super(new URL(theWsdlUrl), new QName(SynchronousListener.SVC_NS, SynchronousListener.SVC_SVCNAME));
	}
	
	public ISynchronousNodeIpcClient getClient() {
		return super.getPort(ISynchronousNodeIpcClient.class);
	}
	
	
	public static void main(String[] args) throws MalformedURLException {
		ISynchronousNodeIpcClient cl = new SynchronousInvokerClient("http://uhn.ca").getClient();
		cl.invokeFlushRuntimeStatus();
	}
	
}
