package org.ilu.pv.simulation;

import org.ilu.handler.HeaderHandlerResolver;
import org.ilu.pv.client.ProcessValueService;
import org.ilu.pv.client.ProcessValueServiceImplService;

import javax.xml.ws.BindingProvider;

public class ProcessValueServiceConsumer {

  private ProcessValueService service;

  public ProcessValueServiceConsumer() {
    this.service = buildService();
    BindingProvider bindingProvider = (BindingProvider) service;
    bindingProvider.getRequestContext().put(BindingProvider.SESSION_MAINTAIN_PROPERTY, Boolean.TRUE);
    bindingProvider.getRequestContext().put("com.sun.xml.internal.ws.request.timeout", 3000);
    bindingProvider.getRequestContext().put("com.sun.xml.internal.ws.connect.timeout", 3000);
  }

  private ProcessValueService buildService() {
    ProcessValueServiceImplService serviceImplService = new ProcessValueServiceImplService();
    ProcessValueService service = serviceImplService.getProcessValueServiceImplPort();

    HeaderHandlerResolver handlerResolver = new HeaderHandlerResolver();

    serviceImplService.setHandlerResolver(handlerResolver);

    return service;
  }

  public ProcessValueService getService() {
    return service;
  }
}
