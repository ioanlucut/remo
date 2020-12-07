package org.ilu.cmd.validator;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.xml.soap.SOAPException;

public class ValidatorProducer {

  @Produces
  @ApplicationScoped
  private Validator produce() throws SOAPException {
    return Validation.buildDefaultValidatorFactory().getValidator();
  }

}
