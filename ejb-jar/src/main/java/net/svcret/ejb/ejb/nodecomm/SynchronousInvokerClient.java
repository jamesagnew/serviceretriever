package net.svcret.ejb.ejb.nodecomm;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.ws.Binding;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;
import javax.xml.ws.handler.Handler;

public class SynchronousInvokerClient extends Service {

	public SynchronousInvokerClient(String theWsdlUrl) throws MalformedURLException {
		super(new URL(theWsdlUrl), new QName(SynchronousListener.SVC_NS, SynchronousListener.SVC_SVCNAME));
	}
	
	public ISynchronousInvoker getClient() {
		return super.getPort(new QName(SynchronousListener.SVC_NS, SynchronousListener.SVC_PORT), ISynchronousInvoker.class);
	}
	
	
	@SuppressWarnings("rawtypes")
	public static void main(String[] args) throws MalformedURLException {
		ISynchronousInvoker cl = new SynchronousInvokerClient("http://uhnvesb02d.uhn.on.ca:26081/ServiceRetriever_NodeCommService/SynchronousListener?wsdl").getClient();
		
		// Add the logging handler
		BindingProvider bp = (BindingProvider) cl;
		Binding binding = bp.getBinding();

		List<Handler> handlerList = binding.getHandlerChain();
		if (handlerList == null) {
			handlerList = new ArrayList<Handler>();
		}

		SoapLoggingHandler loggingHandler = new SoapLoggingHandler();
		handlerList.add(loggingHandler);
		binding.setHandlerChain(handlerList);
		
		FlushRuntimeStatusRequest request = new FlushRuntimeStatusRequest();
		cl.flushRuntimeStatus(request);
	}
	
}
