package com.dibid.core.control.backing;

import org.ilu.pv.exception.ProcessValueQueryException;
import org.ilu.pv.model.ProcessValueQuery;
import org.ilu.pv.ws.ProcessValueService;
import org.ilu.pv.ws.impl.ProcessValueServiceImpl;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class WeldServiceBootstrapTest {

    private static Weld weld;
    private static WeldContainer weldContainer;
    private ProcessValueService processValueService;

    @BeforeClass
    public static void init() {
        weld = new Weld();
        weldContainer = weld.initialize();
    }

    @Before
    public void setUpBeforeTest() {
        processValueService = weldContainer.instance().select(ProcessValueServiceImpl.class).get();
    }

    @Test(expected = IllegalArgumentException.class)
    public void failsIfNullIsPassed() throws ProcessValueQueryException {
        processValueService.query(null);
    }

    @Test(expected = ProcessValueQueryException.class)
    public void failsIfProcessIdIsNull() throws ProcessValueQueryException {
        processValueService.query(new ProcessValueQuery());
    }
}
