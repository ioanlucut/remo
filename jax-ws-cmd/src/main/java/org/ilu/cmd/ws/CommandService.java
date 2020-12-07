package org.ilu.cmd.ws;

import org.ilu.cmd.exception.CommandFeedbackException;
import org.ilu.cmd.model.Command;
import org.ilu.cmd.model.CommandFeedback;

import javax.jws.WebService;

@WebService
public interface CommandService {

    public CommandFeedback submit(Command command) throws CommandFeedbackException;
}
