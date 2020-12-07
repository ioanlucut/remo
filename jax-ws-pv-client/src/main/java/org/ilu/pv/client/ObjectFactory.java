
package org.ilu.pv.client;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each
 * Java content interface and Java element interface
 * generated in the org.ilu.pv.client package.
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

  private final static QName _ProcessValueQuery_QNAME = new QName("http://ws.pv.ilu.org/", "processValueQuery");
  private final static QName _ProcessValueQueryFeedback_QNAME = new QName("http://ws.pv.ilu.org/", "processValueQueryFeedback");
  private final static QName _QueryResponse_QNAME = new QName("http://ws.pv.ilu.org/", "queryResponse");
  private final static QName _ProcessValueQueryException_QNAME = new QName("http://ws.pv.ilu.org/",
      "ProcessValueQueryException");
  private final static QName _ProcessTransferFunction_QNAME = new QName("http://ws.pv.ilu.org/", "processTransferFunction");
  private final static QName _Query_QNAME = new QName("http://ws.pv.ilu.org/", "query");

  /**
   * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.ilu.pv.client
   */
  public ObjectFactory() {
  }

  /**
   * Create an instance of {@link Query }
   */
  public Query createQuery() {
    return new Query();
  }

  /**
   * Create an instance of {@link ProcessTransferFunction }
   */
  public ProcessTransferFunction createProcessTransferFunction() {
    return new ProcessTransferFunction();
  }

  /**
   * Create an instance of {@link QueryResponse }
   */
  public QueryResponse createQueryResponse() {
    return new QueryResponse();
  }

  /**
   * Create an instance of {@link ProcessValueQueryFeedback }
   */
  public ProcessValueQueryFeedback createProcessValueQueryFeedback() {
    return new ProcessValueQueryFeedback();
  }

  /**
   * Create an instance of {@link ProcessValueQueryException }
   */
  public ProcessValueQueryException createProcessValueQueryException() {
    return new ProcessValueQueryException();
  }

  /**
   * Create an instance of {@link ProcessValueQuery }
   */
  public ProcessValueQuery createProcessValueQuery() {
    return new ProcessValueQuery();
  }

  /**
   * Create an instance of {@link JAXBElement }{@code <}{@link ProcessValueQuery }{@code >}}
   */
  @XmlElementDecl(namespace = "http://ws.pv.ilu.org/", name = "processValueQuery")
  public JAXBElement<ProcessValueQuery> createProcessValueQuery(ProcessValueQuery value) {
    return new JAXBElement<>(_ProcessValueQuery_QNAME, ProcessValueQuery.class, null, value);
  }

  /**
   * Create an instance of {@link JAXBElement }{@code <}{@link ProcessValueQueryFeedback }{@code >}}
   */
  @XmlElementDecl(namespace = "http://ws.pv.ilu.org/", name = "processValueQueryFeedback")
  public JAXBElement<ProcessValueQueryFeedback> createProcessValueQueryFeedback(ProcessValueQueryFeedback value) {
    return new JAXBElement<>(_ProcessValueQueryFeedback_QNAME, ProcessValueQueryFeedback.class, null, value);
  }

  /**
   * Create an instance of {@link JAXBElement }{@code <}{@link QueryResponse }{@code >}}
   */
  @XmlElementDecl(namespace = "http://ws.pv.ilu.org/", name = "queryResponse")
  public JAXBElement<QueryResponse> createQueryResponse(QueryResponse value) {
    return new JAXBElement<>(_QueryResponse_QNAME, QueryResponse.class, null, value);
  }

  /**
   * Create an instance of {@link JAXBElement }{@code <}{@link ProcessValueQueryException }{@code >}}
   */
  @XmlElementDecl(namespace = "http://ws.pv.ilu.org/", name = "ProcessValueQueryException")
  public JAXBElement<ProcessValueQueryException> createProcessValueQueryException(ProcessValueQueryException value) {
    return new JAXBElement<>(_ProcessValueQueryException_QNAME, ProcessValueQueryException.class, null, value);
  }

  /**
   * Create an instance of {@link JAXBElement }{@code <}{@link ProcessTransferFunction }{@code >}}
   */
  @XmlElementDecl(namespace = "http://ws.pv.ilu.org/", name = "processTransferFunction")
  public JAXBElement<ProcessTransferFunction> createProcessTransferFunction(ProcessTransferFunction value) {
    return new JAXBElement<>(_ProcessTransferFunction_QNAME, ProcessTransferFunction.class, null, value);
  }

  /**
   * Create an instance of {@link JAXBElement }{@code <}{@link Query }{@code >}}
   */
  @XmlElementDecl(namespace = "http://ws.pv.ilu.org/", name = "query")
  public JAXBElement<Query> createQuery(Query value) {
    return new JAXBElement<>(_Query_QNAME, Query.class, null, value);
  }

}
