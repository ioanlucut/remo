package org.ilu.cmd.bootstrap;

import org.ilu.cmd.ws.CommandService;
import org.ilu.cmd.ws.CommandServiceImpl;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;

import javax.xml.ws.Endpoint;

public class JaxWsCmdServiceBootstrap {

  public static final String ADDRESS = "http://localhost:9999/OutputService";

  public static void main(String[] args) {
    Weld weld = new Weld();
    WeldContainer weldContainer = weld.initialize();
    CommandService commandService = weldContainer.instance().select(CommandServiceImpl.class).get();

    Endpoint.publish(ADDRESS, commandService);
  }
}
