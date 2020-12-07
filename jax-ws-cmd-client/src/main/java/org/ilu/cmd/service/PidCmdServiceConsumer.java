package org.ilu.cmd.service;

import org.apache.log4j.Logger;
import org.ilu.cmd.client.CommandService;
import org.ilu.cmd.client.CommandServiceImplService;
import org.ilu.handler.HeaderHandlerResolver;

import javax.xml.ws.BindingProvider;

/**
 * Default implementation of {@link PidCmdServiceConsumer}
 *
 * @author i.lucut
 */
public class PidCmdServiceConsumer {

  public static final Logger LOGGER = Logger.getLogger(PidCmdServiceConsumer.class);


  /**
   * The {@link javax.xml.ws.BindingProvider} of the {@code Giftcard44Soap}
   */
  private BindingProvider bindingProvider;

  private CommandService service;

  public PidCmdServiceConsumer() {
    this.service = buildService();
    this.bindingProvider = (BindingProvider) service;
    this.bindingProvider.getRequestContext().put(BindingProvider.SESSION_MAINTAIN_PROPERTY, Boolean.TRUE);
    this.bindingProvider.getRequestContext().put("com.sun.xml.internal.ws.request.timeout", 1000);
    this.bindingProvider.getRequestContext().put("com.sun.xml.internal.ws.connect.timeout", 1000);
  }

  private CommandService buildService() {
    CommandServiceImplService serviceImplService = new CommandServiceImplService();
    CommandService service = serviceImplService.getCommandServiceImplPort();

    HeaderHandlerResolver handlerResolver = new HeaderHandlerResolver();
    serviceImplService.setHandlerResolver(handlerResolver);

    return service;
  }

  public CommandService getService() {
    return service;
  }
}
