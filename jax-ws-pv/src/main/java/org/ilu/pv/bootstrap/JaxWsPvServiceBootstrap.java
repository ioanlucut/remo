package org.ilu.pv.bootstrap;

import org.ilu.pv.ws.ProcessValueService;
import org.ilu.pv.ws.impl.ProcessValueServiceImpl;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;

import javax.xml.ws.Endpoint;

public class JaxWsPvServiceBootstrap {

  public static final String ADDRESS = "http://localhost:9998/ProcessValueService";

  public static void main(String[] args) {

    Weld weld = new Weld();
    WeldContainer weldContainer = weld.initialize();

    ProcessValueService processValueService = weldContainer.instance().select(ProcessValueServiceImpl.class).get();

    Endpoint.publish(ADDRESS, processValueService);
  }
}
