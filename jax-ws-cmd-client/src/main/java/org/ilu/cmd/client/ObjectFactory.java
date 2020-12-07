
package org.ilu.cmd.client;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each
 * Java content interface and Java element interface
 * generated in the org.ilu.cmd.client package.
 * <p>An ObjectFactory allows you to programatically
 * construct new instances of the Java representation
 * for XML content. The Java representation of XML
 * content can consist of schema derived interfaces
 * and classes representing the binding of schema
 * type definitions, element declarations and model
 * groups.  Factory methods for each of these are
 * provided in this class.
 */
@XmlRegistry
public class ObjectFactory {

  private final static QName _Command_QNAME = new QName("http://ws.cmd.ilu.org/", "command");
  private final static QName _SubmitResponse_QNAME = new QName("http://ws.cmd.ilu.org/", "submitResponse");
  private final static QName _Submit_QNAME = new QName("http://ws.cmd.ilu.org/", "submit");
  private final static QName _CommandFeedback_QNAME = new QName("http://ws.cmd.ilu.org/", "commandFeedback");
  private final static QName _CommandFeedbackException_QNAME = new QName("http://ws.cmd.ilu.org/", "CommandFeedbackException");

  /**
   * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.ilu.cmd.client
   */
  public ObjectFactory() {
  }

  /**
   * Create an instance of {@link CommandFeedbackException }
   */
  public CommandFeedbackException createCommandFeedbackException() {
    return new CommandFeedbackException();
  }

  /**
   * Create an instance of {@link Submit }
   */
  public Submit createSubmit() {
    return new Submit();
  }

  /**
   * Create an instance of {@link CommandFeedback }
   */
  public CommandFeedback createCommandFeedback() {
    return new CommandFeedback();
  }

  /**
   * Create an instance of {@link Command }
   */
  public Command createCommand() {
    return new Command();
  }

  /**
   * Create an instance of {@link SubmitResponse }
   */
  public SubmitResponse createSubmitResponse() {
    return new SubmitResponse();
  }

  /**
   * Create an instance of {@link JAXBElement }{@code <}{@link Command }{@code >}}
   */
  @XmlElementDecl(namespace = "http://ws.cmd.ilu.org/", name = "command")
  public JAXBElement<Command> createCommand(Command value) {
    return new JAXBElement<Command>(_Command_QNAME, Command.class, null, value);
  }

  /**
   * Create an instance of {@link JAXBElement }{@code <}{@link SubmitResponse }{@code >}}
   */
  @XmlElementDecl(namespace = "http://ws.cmd.ilu.org/", name = "submitResponse")
  public JAXBElement<SubmitResponse> createSubmitResponse(SubmitResponse value) {
    return new JAXBElement<SubmitResponse>(_SubmitResponse_QNAME, SubmitResponse.class, null, value);
  }

  /**
   * Create an instance of {@link JAXBElement }{@code <}{@link Submit }{@code >}}
   */
  @XmlElementDecl(namespace = "http://ws.cmd.ilu.org/", name = "submit")
  public JAXBElement<Submit> createSubmit(Submit value) {
    return new JAXBElement<Submit>(_Submit_QNAME, Submit.class, null, value);
  }

  /**
   * Create an instance of {@link JAXBElement }{@code <}{@link CommandFeedback }{@code >}}
   */
  @XmlElementDecl(namespace = "http://ws.cmd.ilu.org/", name = "commandFeedback")
  public JAXBElement<CommandFeedback> createCommandFeedback(CommandFeedback value) {
    return new JAXBElement<CommandFeedback>(_CommandFeedback_QNAME, CommandFeedback.class, null, value);
  }

  /**
   * Create an instance of {@link JAXBElement }{@code <}{@link CommandFeedbackException }{@code >}}
   */
  @XmlElementDecl(namespace = "http://ws.cmd.ilu.org/", name = "CommandFeedbackException")
  public JAXBElement<CommandFeedbackException> createCommandFeedbackException(CommandFeedbackException value) {
    return new JAXBElement<CommandFeedbackException>(_CommandFeedbackException_QNAME, CommandFeedbackException.class, null, value);
  }

}
