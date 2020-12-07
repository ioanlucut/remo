package org.ilu.common;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Java program to convert XMLGregorianCalendar to Date and inverse i.e. java.util.Date
 * to XMLGregorianCalendar. If you are using XJC to create Java classes from XML Schema
 * or XSD file, By default JAXB map XSD data types xs:date, xs:time and xs:dateTime
 * to XMLGregorianCalendar in Java.
 *
 * @author Javin Paul
 */
public class XMLCalendarToDate {

  public static XMLGregorianCalendar XMLCalendarToDate() {
    return toXMLGregorianCalendar(new Date());
  }

  /*
   * Converts java.util.Date to javax.xml.datatype.XMLGregorianCalendar
   */
  public static XMLGregorianCalendar toXMLGregorianCalendar(Date date) {
    GregorianCalendar gCalendar = new GregorianCalendar();
    gCalendar.setTime(date);
    XMLGregorianCalendar xmlCalendar = null;
    try {
      xmlCalendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(gCalendar);
    } catch (DatatypeConfigurationException ex) {
      ex.printStackTrace();
    }
    return xmlCalendar;
  }

  /*
   * Converts XMLGregorianCalendar to java.util.Date in Java
   */
  public static Date toDate(XMLGregorianCalendar calendar) {
    if (calendar == null) {
      return null;
    }
    return calendar.toGregorianCalendar().getTime();
  }

}