/*
 *     Percussion CMS
 *     Copyright (C) 1999-2020 Percussion Software, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     Mailing Address:
 *
 *      Percussion Software, Inc.
 *      PO Box 767
 *      Burlington, MA 01803, USA
 *      +01-781-438-9900
 *      support@percussion.com
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.xmldom;

import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.util.PSPurgableTempFile;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.Iterator;

import javax.xml.parsers.DocumentBuilder;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.w3c.tidy.Tidy;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;


/**
 * A set of utility routines for the XML document processing extensions.
 *
 * All of these utils are static methods designed for use only within the
 * com.percussion.xmldom package.
 **/
public class PSXmlDomUtils
{

   /**
    * this class should never be instantiated.
    **/
   private PSXmlDomUtils()
   {
      // don't construct this
   }

   /**
    * Load the XML document from a File.  The file optionally run through Tidy
    * to clean up all of the non-XML compliant structures and then
    * ServerPageTags to remove other non-essential "junk" added by certain
    * applications (e.g. Word 2000)
    *
    * @param cx the PSXmlDomContext for this request.
    * @param incomingFile The file which contains the XML/HTML document.
    * @return the parsed Document as an XML tree; may be <code>null</code>
    * or empty.
    *
    * @throws IOException
    * @throws Exception
    */
   public static Document loadXmlDocument(PSXmlDomContext cx, File incomingFile) throws IOException, PSExtensionProcessingException, SAXException {
      String HTMLString = readInFile(incomingFile);

      return loadXmlDocument(cx, HTMLString);
   }

   /**
    * Load the XML document from a File.  The file is optionally run through
    * Tidy to clean up all of the non-XML compliant structures, and then
    * ServerPageTags to remove other non-essential "junk" added by certain
    * applications (e.g. Word 2000)
    *
    *
    * @param      cx      the PSXmlDomContext for this request.
    *
    * @param      incomingFile    The file which contains the XML/HTML document.
    *
    * @param      encoding    the character encoding to use
    *
    * @return    the parsed Document as an XML tree.
    *
    **/
   public static Document loadXmlDocument(PSXmlDomContext cx, File incomingFile,
                                          String encoding) throws IOException, PSExtensionProcessingException, SAXException {
      String HTMLString = readInFile(incomingFile, encoding);

      return loadXmlDocument(cx, HTMLString);
   }

   /**
    * Parses an XML Document from the given String. Depending on the settings
    * in the supplied context, the String will first be run through Tidy and
    * server page tags processing.  The context also determines if a
    * validating or non-validating parser will be used.
    *
    * @param cx the PSXmlDomContext for this request; cannot be <code>null
    * </code>
    * @param HTMLString the source HTML or XML document as a String; may be
    * <code>null</code> or empty.
    *
    * @return the parsed org.w3c.dom.Document.  Will be <code>null</code> if
    * the source string is empty.
    *
    * @throws IOException if an IO error occurs while "tidy" the source HTML.
    * @throws SAXException if error occurs while creating DOM from string.
    * @throws PSExtensionProcessingException if an error occurs from tidy.
    */
   public static Document loadXmlDocument(PSXmlDomContext cx, String HTMLString)
         throws IOException, SAXException, PSExtensionProcessingException
   {
      Document resultDoc = null;
      if (null == HTMLString || HTMLString.trim().length() == 0)
      {
         return null;
      }
      if (null == cx)
         throw new IllegalArgumentException("PSXmlDomContext cannot be null");

      String tidiedHTML = tidyInput(cx, HTMLString);
      String tidiedOutput;

      /*
       * the doctypeHeader contains the entity definitions; these are required
       * if the incoming file uses common entities such as
       * <code>&amp;nbsp;</code>
       */
      String doctypeHeader = "<?xml version='1.0'?>" + NEWLINE;
      doctypeHeader += "<!DOCTYPE html [" +
            getDefaultEntities(cx.getServerRoot()) + "]>" + NEWLINE + NEWLINE;

      if (hasXMLHeaders(tidiedHTML))
      {
         tidiedOutput = tidiedHTML;         
      }
      else
      {
         tidiedOutput = doctypeHeader + tidiedHTML;
      }
      if (cx.isLogging())
      {
         cx.printTraceMessage("writing trace file xmldocbeforeparse.doc");
         try(FileOutputStream beforeParse =
               new FileOutputStream("xmldocbeforeparse.doc")) {
            beforeParse.write(tidiedOutput.getBytes(DEBUG_ENCODING));

         }
      }

      //add all provisioned namespaces to the 'body' tag
      tidiedOutput = addNameSpaces(tidiedOutput, "body", cx);

      InputSource is = new InputSource((Reader) new StringReader(tidiedOutput));

      StringWriter errString = new StringWriter();
      PrintWriter pWriter = new PrintWriter(errString, true);
      try
      {
         resultDoc = createXmlDocument(is, cx.isValidate(), pWriter);
      }
      catch (SAXException e)
      {
         cx.printTraceMessage("XML Parser Errors occurred \n" +
                              errString.toString());
         throw e;
      }

      if (cx.isLogging())
      {
         if (errString.toString().length() > 0)
         {
            cx.printTraceMessage("XML Parser Errors/Warnings: \n" +
                                 errString.toString());
         }
         cx.printTraceMessage("writing trace file xmldocparsedout.doc");
         try(FileOutputStream parsedOutput =
               new FileOutputStream("xmldocparsedout.doc")) {

            PSXmlTreeWalker walk = new PSXmlTreeWalker(resultDoc);
            try(OutputStreamWriter osr = new OutputStreamWriter(parsedOutput, ENCODING)) {
               try(BufferedWriter br = new BufferedWriter(osr)) {
                  walk.write(br, true);
               }
            }

         }
      }

      return resultDoc;
   }


   /**
    * Tidy the incoming document, based on the settings in the operation
    * context.
    * 
    * @param cx the PSXmlDomContext for this request, must not be
    *           <code>null</code>.
    * 
    * @param htmlInput a String containing the input to be tidied, if blank
    *           returns empty string.
    * 
    * @returns the tidied output in a String, never <code>null</code> may be
    *          empty.
    * 
    */
   public static String tidyInput(PSXmlDomContext cx, String htmlInput)
         throws IOException,PSExtensionProcessingException
   {
      if(StringUtils.isBlank(htmlInput))
      {
         return StringUtils.EMPTY;
      }
      if(cx==null)
         throw new IllegalArgumentException("cx must not be null");
      
      if (!cx.isTidyEnabled())
      {
         cx.printTraceMessage("Tidy Not Enabled");
         return htmlInput;
      }

      Tidy tidy = new Tidy();
      tidy.setConfigurationFromProps(cx.getTidyProperties());
      tidy.setInputEncoding("UTF-8");

      if (cx.isLogging())
      {
         cx.printTraceMessage("writing trace file xmldompretidy.doc");
         try(FileOutputStream preTidy = new FileOutputStream("xmldompretidy.doc")) {
            preTidy.write(htmlInput.getBytes(DEBUG_ENCODING));
         }
      }

      try(ByteArrayInputStream bystream = new ByteArrayInputStream(htmlInput.getBytes(ENCODING))){
         StringWriter tidyErrors = new StringWriter();
         tidy.setErrout(new PrintWriter((Writer) tidyErrors));
         Document TidyXML = tidy.parseDOM(bystream, null);
         /*
          * The following code adds a non-breaking space as the first body
          * node in case the body only contains comments. This is to work 
          * around a bug in eWebEditPro, which is removing all comments on load
          * if there are only comments. Adding a non-breaking space is ektrons
          * recommended workaround.
          */
         if (cx.rxCommentHandling())
         {
            NodeList nodes = TidyXML.getElementsByTagName("body");
            if (nodes != null && nodes.getLength() > 0)
            {
               Element body = (Element) nodes.item(0);
               NodeList children = body.getChildNodes();
               if (children != null)
               {
                  boolean commentOnly = true;
                  for (int i=0; commentOnly && i<children.getLength(); i++)
                  {
                     Node child = children.item(i);
                     commentOnly = child.getNodeType() == Node.COMMENT_NODE;
                  }
                  
                  if (commentOnly)
                  {
                     char nbsp = '\u00a0';
                     Text text = TidyXML.createTextNode("" + nbsp);
                     body.insertBefore(text, children.item(0));
                  }
               }
            }
         }
         
         if (tidy.getParseErrors() > 0)
         {
            cx.printTraceMessage("Tidy Errors: " + tidyErrors.toString());
            throw new PSExtensionProcessingException(0,
                                                  "Errors encoutered in Tidy" +
                                                  tidyErrors.toString());
         }

         // Write out the document element using PSNodePrinter. This removes
         // the Xml and Doctype declaration.
         StringWriter swriter = new StringWriter();
         PSNodePrinter np = new PSNodePrinter(swriter);
         np.printNode(TidyXML.getDocumentElement());
         String result = swriter.toString();

         if(cx.getUsePrettyPrint())
         {
            try(ByteArrayInputStream xmlStream = new ByteArrayInputStream(result.getBytes(ENCODING))) {
               TidyXML = tidy.parseDOM(xmlStream, null);
               try (ByteArrayOutputStream tidiedStream = new ByteArrayOutputStream()) {
                  tidy.pprint(TidyXML, (OutputStream) tidiedStream);
                  result = tidiedStream.toString(ENCODING);
               }
            }
         }
         if (cx.isLogging())
         {
            cx.printTraceMessage("writing trace file xmldomtidied.doc");
            try(FileOutputStream fs = new FileOutputStream("xmldomtidied.doc")) {
               PrintWriter pw = new PrintWriter(fs);
               pw.println(result);
               pw.flush();
               pw.close();
            }
         }
         return result;
      }


   }

   /**
    * Put a string into the result document, at a particular node location,
    * which can be a compound name: category/nodelevel1/nodelevel2.
    *
    * @param  cx  @see(PSXmlDomContext) for the current request
    *
    * @param  resultDoc the DOM document where the node is to be added
    *
    * @param nodeName the name of the node to add under the root document. This
    * name may be a compound name (e.g. category/nodevalue)
    *
    * @param nodeValue the string value to add
    **/
   protected static void addResultNode(PSXmlDomContext cx, Document resultDoc,
                                       String nodeName, String nodeValue)

   {

      Element docElement = resultDoc.getDocumentElement();
      PSXmlTreeWalker resWalker = new PSXmlTreeWalker(docElement);
      Element outputNode =
            resWalker.getNextElement(nodeName,
                                     PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN +
                                     PSXmlTreeWalker.GET_NEXT_RESET_CURRENT);
      if (outputNode == null)
      {
         //walker didn't find node, we must create it
         int lastslash = nodeName.lastIndexOf('/');
         if (lastslash == -1)
         {
            //no directory name, just add our element under the document root
            PSXmlDocumentBuilder.addElement(resultDoc, docElement,
                                            nodeName, nodeValue);
         }
         else
         {
            String pathPart = nodeName.substring(0, lastslash);
            String nodePart = nodeName.substring(lastslash + 1);
            //first see if we can find the path to insert under.
            Element newParent =
                  resWalker.getNextElement(
                  pathPart,
                  PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN +
                  PSXmlTreeWalker.GET_NEXT_RESET_CURRENT);
            if (null != newParent)
            {
               // we found an actual element with that name
               PSXmlDocumentBuilder.addElement(
               resultDoc,
               newParent,
               nodePart,
               nodeValue);
            }
            else
            {
               // the parent node was not found.  This probably should throw
               // an exception, but we'll just put the node under the root.
               cx.printTraceMessage(
                     "Warning: Node <" +
                     pathPart +
                     "> not found, adding to <ROOT> ");
               PSXmlDocumentBuilder.addElement(resultDoc, docElement,
                                               nodePart, nodeValue);
            }
         }
      }
      else
      {
         // The target node already exists in the document,
         // just replace the value.
         replaceText(resultDoc, outputNode, nodeValue);
      }
   }

   /**
    * Replace all of the TEXT nodes underneath a given element
    * with a single new text node.  If the node has markup, it may not
    * be preserved. Comments, CDATA sections, and other children of the
    * node are not changed.
    *
    * @param parentDoc the document that the element belongs to
    *
    * @param elementNode the element to be replaced
    *
    * @param newValue the string which contains the new value
    *
    * @return the original Element.
    **/
   public static Element replaceText(Document parentDoc,
                                     Element elementNode,
                                     String newValue)
   {
      int i;
      if (elementNode.hasChildNodes())
      {
         elementNode.normalize();
         NodeList children = elementNode.getChildNodes();
         if (children.getLength() == 1 &&
               children.item(0).getNodeType() == Node.TEXT_NODE)
         {
            // only 1 child, and it's a TEXT.  Just replace the value
            children.item(0).setNodeValue(newValue);
            return elementNode;
         }
         i = 0;
         while (i <= children.getLength())
         {
            Node tempNode = children.item(i);
            if (tempNode.getNodeType() == Node.TEXT_NODE)
            {
               // found a TEXT node, must delete it.
               elementNode.removeChild(tempNode);
            }
            else
            {
               //skip over any non TEXT nodes
               i++;
            }
         } //if
      } //while
      Node newText = parentDoc.createTextNode(newValue);
      elementNode.appendChild(newText);

      return elementNode;
   };

   /**
    * Copy the text from a specified document or node into a String.
    * If the source is a document, the entire document is converted to a string,
    * tags and all.
    * If the source is a node, the <code>Text</code> nodes that are direct
    * children of this node are concatenated to form a String. This string will
    * not contain any tags or other markup.
    *
    * @param sourceDocument the document to read from
    *
    * @param sourceNodeName the name of the node, or NULL if the entire
    * document is to be copied
    *
    * @return the text representation of the document or node
    **/
   static String copyTextFromDocument(PSXmlDomContext cx,
                                      Document sourceDocument,
                                      String sourceNodeName)
   {

      String resultText= "";
      if (sourceNodeName.trim().length() == 0 || sourceNodeName.equals("."))
      {
         //copy the entire document
         cx.printTraceMessage("returning entire document");
         try
         {
            StringWriter swriter = new StringWriter();
            PSNodePrinter np = new PSNodePrinter(swriter);
            np.printNode(sourceDocument);
            resultText = swriter.toString();
         }
         catch (IOException e)
         {
            //Can it happen??
         }
      }
      else
      {
         PSXmlTreeWalker sourceWalker = new PSXmlTreeWalker(sourceDocument);
         cx.printTraceMessage("retrieving node :" + sourceNodeName);
         resultText = sourceWalker.getElementData(sourceNodeName, true);
         cx.printTraceMessage("value is:" + resultText);
      }
      return resultText;

   }

   /**
    * check a Node to see if it contains only whitespace
    *
    * @param x  the Node to test
    *
    * @return  true if the node is a TEXT node with just whitespace in it
    **/
   static boolean isOnlyWhiteSpace(Node x)
   {
      if (x.getNodeType() != Node.TEXT_NODE)
         return false; //not a text node, can't be whitespace
      if (x.getNodeValue().trim().length() > 0)
         return false; // has text in it
      return true;
   }

   /**
    * This method resolves a stylesheet name. The general rules for stylesheet
    * names are:
    * <table>
    * <th>Prototype</th><th>Description</th>
    * <tr><td>filename.xsl</td><td> A file in the application
    * directory</td></tr>
    * <tr><td>../app_name/filename.xsl</td><td> A file in another
    * application</td></tr>
    * </table>
    *
    * @param stylesheetName - the input from the exit parameter list
    * @param appName - the application name from request context
    *
    * @returns the resolved name
    *
    * @todo migrate to PSXmlDomUtils
    *
    **/
   static String getFullStyleName(String stylesheetName, String appName)
   {
      if (stylesheetName.startsWith("../"))
      {
         return "file:" + stylesheetName.substring(3); // skip the "../"
      }
      else
      {
         return "file:" + appName + "/" + stylesheetName;
      }
   }

   /**
    * Read a file into a string, in chunks.
    *
    * @param inFile    The incoming file
    *
    * @param encoding  The standard Java name for the file's encoding method
    *
    * @throws     FileNotFoundException
    * @throws     UnsupportedEncodingException
    * @throws     java.io.IOException
    *
    * @return     The file as a <code>String</code>, never <code>null</code>
    **/
   protected static String readInFile(File inFile, String encoding)
         throws  IOException
   {
      StringBuilder buff = new StringBuilder(BUFFERSIZE);
      try(InputStreamReader rdr =
            new InputStreamReader(new FileInputStream(inFile), encoding)) {

         char arry[] = new char[BUFFERSIZE];
         int icnt = 1;

         while (rdr.ready() && icnt > 0) {
            icnt = rdr.read(arry, 0, BUFFERSIZE);
            if (icnt > 0) {
               buff.append(arry, 0, icnt);
            }
         }
      }

      return buff.toString();
   }

   /**
    * Read a file into a <code>String</code>, in chunks,
    * using the default encoding.
    *
    * @param inFile       the incoming file
    *
    * @throws     FileNotFoundException
    * @throws     UnsupportedEncodingException
    * @throws     java.io.IOException
    *
    * @return     The file as a <code>String</code>, never <code>null</code>
    **/
   protected static String readInFile(File inFile)
         throws IOException
   {
      StringBuilder buff = new StringBuilder(BUFFERSIZE);
      try(FileInputStream fis = new FileInputStream(inFile)) {
         try (InputStreamReader rdr = new InputStreamReader(fis)) {
            char arry[] = new char[BUFFERSIZE];
            int icnt = 1;

            while (rdr.ready() && icnt > 0) {
               icnt = rdr.read(arry, 0, BUFFERSIZE);
               if (icnt > 0) {
                  buff.append(arry, 0, icnt);
               }
            }
         }
      }

      return buff.toString();
   }

   /**
    * Checks if an XML document string already has a
    * <code>&lt;?xml ...&gt;</code> or <code>&lt!DOCTYPE...&gt;</code> header.
    *
    * @param xmlString the XML document to check.
    * @returns <code>true</code> if an XML header is found,
    * <code>false</code> otherwise.
    */
   private static boolean hasXMLHeaders(String xmlString)
   {
       String subString = xmlString.length() > 10 
         ? xmlString.substring(0,11).toLowerCase()
         : xmlString.toLowerCase();
         
       if (subString.startsWith("<?xml"))
       {
          return true;
       }
       if (subString.startsWith("<!doctype"))
       {
          return true;
       }
       return false;
   }

   /**
    * Gets the element from the specified documement that matches the specified
    * name.
    * @param elementName name of an element within the specified document, or
    * "." to match the root element; cannot be <code>null</code> or empty
    * @param doc document to search; cannot be <code>null</code>
    * @return Element within <code>doc</code> that matches <code>elementName
    * </code>, or <code>null</code> if no matching element was found
    */
   public static Element findElement(String elementName, Document doc)
   {
      if (null == elementName || elementName.trim().length() == 0)
         throw new IllegalArgumentException
         ("elementName cannot be null or empty");
      if (null == doc)
         throw new IllegalArgumentException("Document cannot be null");

      Element found = null;
      PSXmlTreeWalker walker = new PSXmlTreeWalker(doc);
      if(elementName.equals("."))
      {
          found = (Element)walker.getCurrent();
      }
      else
      {
          found = walker.getNextElement(elementName,
                PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);
      }
      return found;
   }

   /**
    * Get a parameter from the parameter list, and return it as a trimmed
    * string.  This is a utility function intended for the
    * <code>com.percussion.xmldom</code> extensions.
    *
    * @param params array of parameter objects from the calling function.
    *
    * @param pindex the integer index into the parameters
    *
    * @param defValue the default value of the parameter (may be
    * <code>null</code>)
    *
    * @return If index'ed parameter exits, it is <code>trim</code>ed and
    * return. If that parameter is not provided, then <code>defValue</code>
    * is returned.
    *
    **/
   protected static String getParameter(Object[] params,
                                        int pindex,
                                        String defValue)
   {
      if (params.length < pindex + 1 || null == params[pindex] ||
            params[pindex].toString().trim().length() == 0)
      {
         return defValue;
      }
      else
      {
         return params[pindex].toString().trim();
      }
   }

   /**
    * Get a parameter from the parameter list, and return it as an Object.
    * The object will retain its original class type.
    * This is a utility function intended for the
    * <code>com.percussion.xmldom</code> extensions.
    *
    * @param params array of parameter objects from the calling function.
    *
    * @param pindex the integer index into the parameters
    *
    * @return the parameter as a object, unless the parameter is not present,
    * in which case the return value is <code>null</code>
    **/
   protected static Object getParameter(Object[] params, int pindex)
   {
      if (params.length < pindex + 1 || null == params[pindex])
      {
         return null;
      }
      else
      {
         return params[pindex];
      }
   }

   /**
    * Add the entity references required by the parser.  Since we are always
    * running as a server extension, the current directory is /Rhythmyx,  and
    * the DTD directory resides immediately below it.
    *
    **/
   private static String getDefaultEntities(String serverRoot)
   {
      return "\t<!ENTITY % HTMLlat1 SYSTEM \"http://" +
            serverRoot + "/DTD/HTMLlat1x.ent\">" + NEWLINE +
            "\t\t%HTMLlat1;" + NEWLINE +
            "\t<!ENTITY % HTMLsymbol SYSTEM \"http://" +
            serverRoot + "/DTD/HTMLsymbolx.ent\">" + NEWLINE +
            "\t\t%HTMLsymbol;" + NEWLINE +
            "\t<!ENTITY % HTMLspecial SYSTEM \"http://" +
            serverRoot + "/DTD/HTMLspecialx.ent\">" + NEWLINE +
            "\t\t%HTMLspecial;";
   }

   /**
    * NEWLINEs in XML are always <code>&lt;CR&gt;&lt;LF&gt;</code>,
    * even on platforms where &lt;LF&gt; is normally used
    **/
   private static final String NEWLINE = "\r\n";
   private static final int BUFFERSIZE = 20000;

   /**
    *ENCODING is always ISO 8859-1 for all Word HTML files.
    **/
   public static final String ENCODING = "UTF8";

   /**
    *Default name for all Private Objects is "XMLDOM"
    **/
   public static final String DEFAULT_PRIVATE_OBJECT = "XMLDOM";

   /**
    *The encoding for all "debugging" files
    **/
   public static final String DEBUG_ENCODING = "UTF8";

   /**
    * This section is copied directly from PSXmlDocumentBuilder.  We need to
    * print out the Parser errors (and warnings), but the default routine
    * does not do this.  We cannot overload this because of Obfuscation;
    * we must copy it here and change it.
    **/
   private static Document createXmlDocument(
         InputSource in,
         boolean validate,
         PrintWriter errorLog)
         throws java.io.IOException, org.xml.sax.SAXException
   {
      Document doc = null;
      DocumentBuilder db = PSXmlDocumentBuilder.getDocumentBuilder(validate);
      PSSaxErrorHandler errHandler = new PSSaxErrorHandler(errorLog);
      errHandler.throwOnFatalErrors(false);
      db.setErrorHandler(errHandler);
      doc = db.parse(in);
      errorLog.flush();

      /* We want to handle XML files without the <?xml ...> preamble. These
       * files cause IBM's parser to report "Invalid document structure."
       * (at least on a US english version).
       *
       * Therefore, if more than one exception is thrown (which there
       * will be for real errors) we'll throw the first exception
       * encountered.
       */
      if (errHandler.numFatalErrors() > 1)
      {
         Iterator<SAXParseException> errors = errHandler.fatalErrors();
         SAXParseException ex =  errors.next();
         throw ex;
      }
      return doc;
   }


   /**
    * Determines which character encoding to use for the given file.
    * 1. from the file
    * 2. from the exit parameters
    * 3. from the platform default
    *
    * @param contxt encapsulation of the logging functions; assumed not
    *        <code>null</code>
    * @param inputSourceFile file to determine character encoding; assumed not
    *        <code>null</code>
    * @param encodingDefault name of character encoding to use if it cannot be
    *        determined from the file (not used if <code>null</code> or empty)
    *
    * @return name of the character encoding to use, or <code>null</code> if
    *         the platform default should be used.
    */
   static String determineCharacterEncoding(PSXmlDomContext contxt,
                                            PSPurgableTempFile inputSourceFile,
                                            String encodingDefault)
   {
      String encoding = null;
      if (inputSourceFile.getCharacterSetEncoding() != null)
      {
         encoding = inputSourceFile.getCharacterSetEncoding();
         contxt.printTraceMessage("Encoding (from file) is " + encoding);
      }
      else if (encodingDefault != null && encodingDefault.length() != 0)
      {
         encoding = encodingDefault;
         contxt.printTraceMessage("Encoding (from exit) is " + encoding);
      }
      else
      {
         contxt.printTraceMessage("Encoding is plaform default");
      }
      return encoding;
   }

   /**
    * Adds all the namespaces, defined in the TidyProperties file
    * under 'add-namespaces' key, to the given htmlTagName.
    * @param htmlString source string containing an html document,
    * never <code>null</code>.
    * @param htmlTagName tag name to which the namespaces will be added,
    * never <code>null</code> or <code>empty</code>.
    * @param contxt current XmlDomContext, never <code>null</code>.
    * @return
    */
   static private String addNameSpaces(String htmlString, String htmlTagName,
      PSXmlDomContext contxt)
   {
      if (htmlString==null)
         throw new IllegalArgumentException("htmlString may not be null or empty");

      if (htmlTagName==null || htmlTagName.trim().length()<=0)
         throw new IllegalArgumentException("htmlTagName may not be null or empty");

      if (contxt==null)
         throw new IllegalArgumentException("contxt may not be null");

      int bodyLoc = htmlString.indexOf("<" + htmlTagName);

      if(bodyLoc < 0)
         return htmlString;

      //Add all namespaces defined in the properties file.
      String nsString =
         contxt.getTidyProperties().getProperty(ADD_NAMESPACE_LIST,"");

      if (nsString==null || nsString.trim().length()<=0)
         return htmlString;

      StringBuilder sb = new StringBuilder(htmlString);
      bodyLoc += ("<" + htmlTagName).length();

      sb.insert(bodyLoc, " " + nsString);

      return sb.toString();
   }

   /**
    * a tag name for a list of namespaces that would be added to the body.
    */
   public static final String ADD_NAMESPACE_LIST = "add-namespaces";
}
