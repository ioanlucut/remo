package org.ilu.pv.ws;

import org.ilu.pv.exception.ProcessValueQueryException;
import org.ilu.pv.model.ProcessValueQuery;
import org.ilu.pv.model.ProcessValueQueryFeedback;

import javax.jws.WebService;

@WebService
public interface ProcessValueService {

    public ProcessValueQueryFeedback query(ProcessValueQuery processValueQuery) throws ProcessValueQueryException;
}
