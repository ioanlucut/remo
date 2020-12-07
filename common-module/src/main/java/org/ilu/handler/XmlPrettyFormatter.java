package org.ilu.handler;

import com.google.common.base.Charsets;
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

public class XmlPrettyFormatter {

  public String format(String xml) {
    try {
      InputSource src = new InputSource(new StringReader(xml));
      Node document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(src).getDocumentElement();
      Boolean keepDeclaration = xml.startsWith("<?xml");

      DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
      DOMImplementationLS impl = (DOMImplementationLS) registry.getDOMImplementation("LS");
      LSSerializer writer = impl.createLSSerializer();

      // Set this to true if the output needs to be beautified.
      writer.getDomConfig().setParameter("format-pretty-print", Boolean.TRUE);
      // Set this to true if the declaration is needed to be outputted.
      writer.getDomConfig().setParameter("xml-declaration", keepDeclaration);

      LSOutput lsOutput = impl.createLSOutput();
      lsOutput.setEncoding(Charsets.UTF_8.toString());
      Writer stringWriter = new StringWriter();
      lsOutput.setCharacterStream(stringWriter);
      writer.write(document, lsOutput);

      return stringWriter.toString();
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }
}