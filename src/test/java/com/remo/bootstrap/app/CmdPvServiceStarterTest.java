package com.remo.bootstrap.app;

import org.junit.Before;
import org.junit.Test;

/**
 * User: Ionel
 * Date: 7/3/14
 * Time: 8:54 PM
 */
public class CmdPvServiceStarterTest {

  @Before
  public void setUp() throws Exception {

  }

  @Test
  public void testStartService() throws Exception {
    CmdPvServiceStarter starter = new CmdPvServiceStarter();
    starter.startService(false);

    starter.stopService();
  }

  @Test
  public void testStopService() throws Exception {

  }
}
