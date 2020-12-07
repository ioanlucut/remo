package org.ilu.common;

import org.apache.commons.lang3.StringEscapeUtils;
import org.w3c.dom.Node;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

/**
 * Pretty-prints xml, supplied as a string.
 * <p/>
 * eg. <code>
 * String formattedXml = new XmlPrettyFormatter().format("<tag><nested>hello</nested></tag>");
 * </code>
 */
public class XmlPrettyFormatter {

  private static final String UTF_8 = "UTF-8";

  public String format(String xml) {
    try {
      InputSource src = new InputSource(new StringReader(xml));
      Node document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(src).getDocumentElement();
      Boolean keepDeclaration = Boolean.valueOf(xml.startsWith("<?xml"));

      DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
      DOMImplementationLS impl = (DOMImplementationLS) registry.getDOMImplementation("LS");
      LSSerializer writer = impl.createLSSerializer();

      // Set this to true if the output needs to be beautified.
      writer.getDomConfig().setParameter("format-pretty-print", Boolean.TRUE);
      // Set this to true if the declaration is needed to be outputed.
      writer.getDomConfig().setParameter("xml-declaration", keepDeclaration);

      LSOutput lsOutput = impl.createLSOutput();
      lsOutput.setEncoding(UTF_8);
      Writer stringWriter = new StringWriter();
      lsOutput.setCharacterStream(stringWriter);
      writer.write(document, lsOutput);

      return stringWriter.toString();
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }

  public String formatByUnescapingHtml(String xml) {
    return StringEscapeUtils.unescapeHtml4(format(xml));
  }
}