package service;

import com.sun.xml.ws.fault.ServerSOAPFaultException;
import org.ilu.common.XMLCalendarToDate;
import org.ilu.pv.client.*;
import org.ilu.pv.simulation.ProcessValueServiceConsumer;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import javax.xml.ws.WebServiceException;
import java.net.ConnectException;
import java.util.Date;

public class ProcessValueServiceConsumerTest {

    @Test(expected = WebServiceException.class)
    @Ignore
    public void testconnection() throws ProcessValueQueryException_Exception {
        ProcessValueService cmdService = null;
        try {
            cmdService = new ProcessValueServiceImplService().getProcessValueServiceImplPort();
            Assert.assertNull(cmdService);
        } catch (WebServiceException ex) {
            Assert.assertTrue(ex.getCause() instanceof ConnectException);

            throw ex;
        }
    }

    @Test(expected = ServerSOAPFaultException.class)
    public void failsIfNullIsPassed() throws ProcessValueQueryException_Exception {
        ProcessValueService cmdService = new ProcessValueServiceImplService().getProcessValueServiceImplPort();
        cmdService.query(null);
    }

    @Test(expected = ProcessValueQueryException_Exception.class)
    public void failsIfProcessIdIsNull() throws ProcessValueQueryException_Exception {
        ProcessValueService cmdService = new ProcessValueServiceImplService().getProcessValueServiceImplPort();
        cmdService.query(new ProcessValueQuery());
    }

    @Test
    public void transferFunctionIsSentBackAsConfirmation() throws ProcessValueQueryException_Exception {
        ProcessValueServiceConsumer consumer = new ProcessValueServiceConsumer();

        ProcessValueQuery processValueQuery = new ProcessValueQuery();
        processValueQuery.setProcessId(1);
        processValueQuery.setLastComputedCommand(100);
        processValueQuery.setLastReceivedProcessValue(200);
        processValueQuery.setQueryDate(XMLCalendarToDate.toXMLGregorianCalendar(new Date()));

        ProcessTransferFunction processTransferFunction = new ProcessTransferFunction();
        processTransferFunction.setDelayL(14.0);
        processTransferFunction.setGainK(15.0);
        processTransferFunction.setTimeConstantT(16.0);

        processValueQuery.setProcessTransferFunction(processTransferFunction);

        ProcessValueQueryFeedback response = consumer.getService().query(processValueQuery);
        Assert.assertNotNull(response.getReceivedTransferFunction());
    }

}
