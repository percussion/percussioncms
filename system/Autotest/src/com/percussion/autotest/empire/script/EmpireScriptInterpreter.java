/******************************************************************************
 *
 * [ EmpireScriptInterpreter.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.autotest.empire.script;

import com.percussion.HTTPClient.ModuleException;
import com.percussion.autotest.empire.EmpireTestClient;
import com.percussion.autotest.empire.IPSAutoTestJavaExtension;
import com.percussion.autotest.framework.QAObjectDescription;
import com.percussion.autotest.framework.QAObjectScope;
import com.percussion.autotest.framework.QAObjectType;
import com.percussion.autotest.framework.QAPerformanceStats;
import com.percussion.autotest.framework.QAScriptDocument;
import com.percussion.autotest.framework.QATestResults;
import com.percussion.performance.IPSPerformanceMonitor;
import com.percussion.performance.PSNtPerformanceMonitor;
import com.percussion.performance.PSPerformanceMonitorException;
import com.percussion.test.http.HttpHeaders;
import com.percussion.test.http.PSHttpRequest;
import com.percussion.test.io.IOTools;
import com.percussion.test.io.LogSink;
import com.percussion.util.PSBase64Encoder;
import com.percussion.util.PSHttpUtils;
import com.percussion.utils.tools.PSPatternMatcher;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;
import com.quiotix.html.HtmlCollector;
import com.quiotix.html.HtmlDocument;
import com.quiotix.html.HtmlFormatter;
import com.quiotix.html.HtmlScrubber;
import com.quiotix.html.parser.HtmlParser;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.rmi.RemoteException;
import java.text.FieldPosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.soap.Header;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


/**
 * The script interpreter is responsible for interpreting and
 * running a script. A client creates a script interpreter for
 * each script execution request it receives.
 *
 * @author  chad loder
 *
 * @version 1.0 1999/8/17
 *
 */
public class EmpireScriptInterpreter implements Runnable, LogSink,
      IExecutionContext
{

   /**
    * Construct a script interpreter that will execute the given
    * script document when run.
    *
    * @author  chadloder
    *
    * @version 1.2 1999/08/20
    *
    *
    * @param   doc
    * @param   client
    * @param   results
    * @param   logToScreen
    *
    */
   public EmpireScriptInterpreter(
      QAScriptDocument doc,
      EmpireTestClient client,
      QATestResults results,
      boolean logToScreen)
   {
      m_client = client;
      m_qadoc = doc;
      m_results = results;
      m_subDocExecStack = new HashMap();
      m_subDocs = new HashMap();
      m_logToScreen = logToScreen;
      m_vars = new HashMap();

      // get the macros that the client has defined
      Map globalMap = client.getGlobalMacros();
      if (globalMap != null)
         m_vars.putAll(globalMap);
   }


   //****************** Begin implementation of IExecutionContext ***********
   // see interface for desc
   public void setMacro( String name, String value )
   {
      if ( null == name || name.trim().length() == 0 )
      {
         throw new IllegalArgumentException(
               "macro name cannot be null or empty" );
      }
      defineMacro( name, value );
   }

   // see interface for desc
   public String getMacro( String name )
   {
      if ( null == name || name.trim().length() == 0 )
      {
         throw new IllegalArgumentException(
               "macro name cannot be null or empty" );
      }
      return (String) m_vars.get( name );
   }
   //****************** End implementation of IExecutionContext ***********


   /**
    * Compares the actual headers with the expected headers.
    * For each expected header, make sure it and its value is present
    * in the actual headers.
    *
    * @param expectedHeaders The headers we are expecting, may not be
    * <code>null</code>.
    * @param actualHeaders The actual headers to compare with, may not be
    * <code>null</code>.
    *
    * @throws IllegalArgumentException if any param is invalid
    * @throws ScriptTestFailedException if the comparison fails.
    */
   protected void compareHttpHeaders(HttpHeaders expectedHeaders,
      HttpHeaders actualHeaders)
      throws ScriptTestFailedException
   {
      if (expectedHeaders == null)
         throw new IllegalArgumentException("expectedHeaders may not be null");

      if (actualHeaders == null)
         throw new IllegalArgumentException("actualHeaders may not be null");

      // for each expected header, try to find its value in the actual headers
      Collection keySet = expectedHeaders.getHeaderNames();
      for (Iterator i = keySet.iterator(); i.hasNext(); )
      {
         String expectName = i.next().toString();
         for (Iterator j = expectedHeaders.getHeaders(expectName); j.hasNext(); )
         {
            String expectVal = j.next().toString();
            if (!hasHeaderValue(actualHeaders, expectName, expectVal))
            {
               String hdrVal = getHeaderValue(actualHeaders, expectName);
               String errVal = hdrVal == null ?
                  "but a header with this name was not found." :
                  "but found value(s): " + hdrVal;
               throw new ScriptTestFailedException(
                  "Expected header " + expectName + " with value " + expectVal
                  + " in results, " + errVal);
            }
         }
      }
   }

   /**
    * For a given header name and value, sees if the headers object contains
    * a header with this name and value.
    *
    * @author  chadloder
    *
    * @version 1.2 1999/08/20
    *
    * @param   headers
    * @param   name
    * @param   val
    *
    * @return  boolean
    */
   protected boolean hasHeaderValue(HttpHeaders headers, String name, String val)
   {
      for (Iterator i = headers.getHeaders(name); i.hasNext(); )
      {
         String hdrVal = i.next().toString();

         if (hdrVal.equals(val))
            return true;
      }
      return false;
   }

   /**
    * For a given header name, sees if the headers object contains a header with
    * this name and returns the value.
    *
    * @param headers The headers object, assumed not <code>null</code>.
    * @param name The name of the header, assumed not <code>null</code> or
    * empty.
    *
    * @return The value found for the named header, or <code>null</code>
    * if a matching header is not found.  If the header has multiple values,
    * they are returned as a single semi-colon delimited <code>String</code>.
    */
   private String getHeaderValue(HttpHeaders headers, String name)
   {
      String val = null;
      for (Iterator i = headers.getHeaders(name); i.hasNext(); )
      {
         String hdrVal = i.next().toString();
         if (val == null)
            val = hdrVal;
         else
            val += ";" + hdrVal;
      }
      return val;
   }

   /**
    * Compares http results of the expected and actual request. If the expected
    * request's protocol is 'file' and the file extension is either 'xml' or
    * 'xhtml' or 'html' then it cleans up the streams for ignorable white spaces
    * and line breaks and then compares. If the expected request's protocol is
    * 'file' and the file extension is 'txt' then it tries to parse the file as
    * an xml document for normalized comparison. If the data of 'txt' file does
    * not conform to xml, then it does the comparison on a line-by-line basis.
    *
    * @param prot protocol of expected stream, assumed neither <code>null</code>
    * nor empty and it should be in upper case.
    * @param href reference of expected request, assumed neither
    * <code>null</code> nor empty.
    * @param expectedIn expected input result, assumed not <code>null</code> and
    * the stream should be closed by caller.
    * @param expectedCharSet encoding of expected input result, assumed neither
    * <code>null</code> nor empty
    * @param actualIn actual input result, assumed not <code>null</code> and
    * the stream should be closed by caller.
    * @param actualCharSet encoding of actual input result, assumed not
    * <code>null</code> nor empty
    * @param styleSheet A file URL of a stylesheet to use to transform the
    * actual results before comparing.  Will only be used if the actual results
    * are treated as XML and <code>styleSheet</code> is not <code>null</code>
    * or empty.
    *
    * @throws Exception when error/exception happens in process of comparison.
    **/
   private void compareResults( String prot, String href,
      InputStream expectedIn, String expectedCharSet,
      InputStream actualIn, String actualCharSet, String styleSheet)
      throws Exception
   {
      /* If the protocol is 'FILE' and have one of the following extensions
       * 'txt', 'xml', 'xhtml' or 'html', get clean streams by normalizing the
       * documents and compare.
       */
      if( prot.equals("FILE") )
      {
         int index = href.lastIndexOf('.');
         if(index != -1)
         {
            String extension = href.substring(index+1).toLowerCase();

            if(extension.startsWith("txt") || extension.startsWith("xml"))
            {
               /* 
                * Even though the file extension is 'txt' the data might be in
                * the xml format, so try to get the streams as XML streams for
                * normalized comparison to ignore the ignorable white spaces or
                * new lines.
                *
                * Even though the file extension is 'xml' the data might not
                * be parsable as xml (the file could be empty, for example).
                *
                * If either the expected or actual stream is not parsable,
                * compare the original streams.
                */
               ByteArrayOutputStream expStream = new ByteArrayOutputStream();
               ByteArrayOutputStream actStream = new ByteArrayOutputStream();

               try 
               {
                  /* 
                   * Keep copy of original streams, in case of exception with
                   * document parsing, to compare with original streams.
                   */
                  IOTools.copyStream(expectedIn, expStream);
                  IOTools.copyStream(actualIn, actStream);

                  // need new input streams after the copy
                  actualIn.close();
                  actualIn = new ByteArrayInputStream(actStream.toByteArray());
                  expectedIn.close();
                  expectedIn = new ByteArrayInputStream(expStream.toByteArray());

                  if (styleSheet != null && styleSheet.trim().length() != 0)
                  {
                     actualIn = transformXmlStream(actualIn, styleSheet);
                  }

                  actualIn = getCleanXmlStream(actualIn, actualCharSet);
                  expectedIn = getCleanXmlStream(expectedIn, expectedCharSet);
               }
               catch (SAXException e)
               {
                  // either actual or expected stream is not valid XML
                  if(actualIn != null)
                  {
                     try { actualIn.close(); } catch (IOException ioe){};
                  }
                  if(expectedIn != null)
                  {
                     try { expectedIn.close(); } catch (IOException ioe){};
                  }
                  actualIn = new ByteArrayInputStream(actStream.toByteArray());
                  expectedIn = new ByteArrayInputStream(expStream.toByteArray());
               }
               finally {
                  try { actStream.close(); } catch (IOException ioe){};
                  try { expStream.close(); } catch (IOException ioe){};
               }
            }
            else if(extension.startsWith("htm") ||
               extension.startsWith("xhtml"))
            {
               actualIn = getCleanHtmlStream(actualIn, actualCharSet);
               expectedIn = getCleanHtmlStream(expectedIn, expectedCharSet);
            }
         }
      }

      compareHttpResults(expectedIn, expectedCharSet, actualIn, actualCharSet);
   }


   /**
    * Transforms the supplied input stream using the specified styleSheet.
    *
    * @param in A valid input stream to transform.  Assumed not
    * <code>null</code> and to be valid Xml.
    * @param styleSheet The stylesheet to use for the transformation.  Assumed
    * to specify a valid file URL.
    *
    * @return A new input stream to the transformed Xml.
    *
    * @throws MalformedURLException if the provided <code>styleSheet</code> is
    * not a valid file URL.
    * @throws TransformerException if there are any errors transforming the
    * stream.
    * @throws IOException if there are any errors reading from the stream.
    */
   private InputStream transformXmlStream(InputStream in, String styleSheet)
      throws MalformedURLException, TransformerException, IOException
   {
      URL ssUrl = new URL(styleSheet);
      File ssFile = new File(ssUrl.getFile());
      log("Transforming actual data using stylesheet: " + ssFile.toString());
      ByteArrayOutputStream out = new ByteArrayOutputStream();

      TransformerFactory tFactory = TransformerFactory.newInstance();
      Transformer transformer = tFactory.newTransformer(new StreamSource(
         ssFile));
      transformer.transform( new StreamSource(in), new StreamResult(out));

      in.close();
      out.flush();

      return new ByteArrayInputStream(out.toByteArray());

   }


   /**
    * Gets clean html stream from passed in html stream ignoring white spaces
    * and line breaks not in the content and reformats the html.
    *
    * @param in input html stream, assumed not <code>null</code>. Closes this
    * stream and sends the new stream with cleaned data.
    * @param charSet encoding of stream, assumed neither <code>null</code> nor
    * empty.
    *
    * @return cleaned html stream, never <code>null</code>, caller is
    * responsible for closing the stream.
    *
    * @throws Exception when error/exception happens in process of
    * parsing/cleaning.
    **/
   private InputStream getCleanHtmlStream(InputStream in, String charSet)
      throws Exception
   {
      HtmlDocument document = new HtmlParser(
         new InputStreamReader(in, charSet)).HtmlDocument();
      document.accept(new HtmlCollector());
      in.close();

      /* Changes tags and tag attributes to lowercase, strip out unnecessary
       * quotes from attribute values, and strip trailing spaces before new line
       */
      document.accept(new HtmlScrubber(HtmlScrubber.DEFAULT_OPTIONS |
         HtmlScrubber.TRIM_SPACES));

      // Dumps html ignoring ignorable line breaks.
      byte[] htmlBytes = null;
      ByteArrayOutputStream ignoreHtml = new ByteArrayOutputStream();
      document.accept(new EmpireHtmlDumper(ignoreHtml));
      htmlBytes = ignoreHtml.toByteArray();
      ignoreHtml.close();

      /* Format the document again to have new lines so when error happens
       * in comparison, it reports the exact line.
       */
      document = new HtmlParser( new InputStreamReader(
         new ByteArrayInputStream(htmlBytes), charSet) ).HtmlDocument();
      document.accept(new HtmlCollector());
      ByteArrayOutputStream cleanHtml = new ByteArrayOutputStream();
      document.accept(new HtmlFormatter(cleanHtml));
      htmlBytes = cleanHtml.toByteArray();
      cleanHtml.close();

      return new ByteArrayInputStream(htmlBytes);
   }

   /**
    * Gets clean xml stream from passed in xml stream ignoring white spaces not
    * in the content.
    *
    * @param in input xml stream, assumed not <code>null</code> and closes this
    *    stream and sends the new stream with cleaned data.
    * @param charSet encoding of stream, assumed neither <code>null</code> nor
    *    empty.
    * @return cleaned xml stream, never <code>null</code>, caller is
    *    responsible for closing the stream
    * @throws IOException when error happens in reading/writing to streams.
    * @throws SAXException when error happens while building document out from
    *    input stream
    */
   private InputStream getCleanXmlStream(InputStream in, String charSet)
      throws IOException, SAXException
   {
      /*
       * First we must read over all white space lines because they will be
       * interpreted by DOM as Text nodes.
       */
      BufferedReader reader = new BufferedReader(
         new InputStreamReader(in, charSet));
      StringBuffer buffer = new StringBuffer();
      String line = reader.readLine();
      while (line != null)
      {
         if (line.trim().length() > 0)
            buffer.append(line);
         
         line = reader.readLine();
      }
      reader.close();

      /*
       * Now clean out all additional white space through DOM and return the
       * new cleaned XML stream.
       */
      Document document = PSXmlDocumentBuilder.createXmlDocument(
         new StringReader(buffer.toString()), false);
      in.close();

      ByteArrayOutputStream cleanXml = new ByteArrayOutputStream();
      PSXmlDocumentBuilder.write(
         document, new OutputStreamWriter(cleanXml, charSet));
      byte[] cleanBytes = cleanXml.toByteArray();
      cleanXml.close();

      return new ByteArrayInputStream(cleanBytes);
   }


   /**
    * Compare the given input streams on a line-by-line basis, raising an error on the
    * first difference found. Uses character comparisons.
    *
    * @author  chadloder
    *
    * @version 1.2 1999/08/20
    *
    */
   protected void compareHttpResults(
      InputStream expectedResult,
      String expectedCharSet,
      InputStream actualResult,
      String actualCharSet)
      throws Exception
   {
      BufferedReader actualReader = new BufferedReader(
         new InputStreamReader(actualResult, actualCharSet));

      BufferedReader expectedReader = new BufferedReader(
         new InputStreamReader(expectedResult, expectedCharSet));

      int lineNum = 1;
      String expectedLine = null;
      String actualLine = null;
      String previousExpected = null;
      String previousActual = null;
      do
      {
         expectedLine = expectedReader.readLine();
         actualLine = actualReader.readLine();

         if (expectedLine == null || actualLine == null)
         {
            if (actualLine != null)
            {
               throw new ScriptTestFailedException(
                  "Expected end of data on line " + lineNum +
                     ", actually got \"" + actualLine + "\"");
            }

            if (expectedLine != null)
            {
               throw new ScriptTestFailedException(
                  "Reached end of actual data on line " + lineNum +
                     ", expected \"" + expectedLine + "\"");
            }
            else
               break; // we reached both ends at the same time, which is good
         }

         if (!expectedLine.equalsIgnoreCase(actualLine))
         {
            String nextExpected = expectedReader.readLine();
            String nextActual = actualReader.readLine();
            if (previousExpected != null && previousActual != null)
            {
               if (nextExpected != null && nextActual != null)
               {
                  throw new ScriptTestFailedException("Expected data \r\n\"" +
                     previousExpected + "\r\n" + expectedLine + "\r\n" +
                     nextExpected + "\"\r\n on line " + lineNum +
                     ", actually got \r\n\"" + previousActual + "\r\n" +
                     actualLine + "\r\n" + nextActual + "\"");
               }
               else
               {
                  throw new ScriptTestFailedException("Expected data \r\n\"" +
                     previousExpected + "\r\n" + expectedLine +
                     "\"\r\n on line " + lineNum + ", actually got \r\n\"" +
                     previousActual + "\r\n" + actualLine + "\"");
               }
            }

            if (nextExpected != null && nextActual != null)
            {
               throw new ScriptTestFailedException("Expected data \r\n\"" +
                  expectedLine + "\r\n" + nextExpected + "\"\r\n on line " +
                  lineNum + ", actually got \r\n\"" + actualLine + "\r\n" +
                  nextActual + "\"");
            }
            else
            {
               throw new ScriptTestFailedException("Expected data \r\n\"" +
                  expectedLine + "\"\r\n on line " + lineNum +
                  ", actually got \r\n\"" + actualLine + "\"");
            }
         }

         lineNum++;
         previousExpected = expectedLine;
         previousActual = actualLine;
      }
      while (true);
   }

   /**
    * A convenience method that calls (#getChildHeaders(Element,boolean)
    * getChildHeaders( e, true )}.
    */
   protected HttpHeaders getChildHeaders( Element e )
   {
      return getChildHeaders( e, true );
   }

   /**
    * For the given element, adds all of its immediate HttpHeader children
    * to a new HttpHeaders object and returns the object. If there are no
    * HttpHeader children, returns a valid but empty HttpHeaders object.
    *
    * @author  chadloder
    *
    * @version 1.2 1999/08/20
    *
    *
    * @param   e The element containing the HttpHeader children.
    *
    * @param includeAuthorization A flag to indicate whether the Authorization
    *    header variable should be included. If <code>false</code>, it is not
    *    included in the returned object.
    *
    * @return  An object that contains all of the headers defined as children
    *    in the supplied element.
    */
   protected HttpHeaders getChildHeaders(Element e,
         boolean includeAuthorization )
   {
      HttpHeaders headers = new HttpHeaders();

      if (e == null)
         return headers;

      NodeList children = e.getChildNodes();
      Node kid = null;
      for (int i = 0; i < children.getLength(); i++)
      {
         kid = children.item(i);
         if ((kid instanceof Element) && kid.getNodeName().equals("HttpHeader"))
         {
            Element el = (Element)kid;
            String headerName = getAttribute(el, "name").trim();
            String headerValue = getAttribute(el, "value").trim();
            if ( includeAuthorization ||
                  !headerName.equalsIgnoreCase("Authorization"))
            {
               if ( headerName.equalsIgnoreCase("Authorization"))
               {
                  if ( getAttribute( el, "encode" ).equals( "basic" ))
                  {
                     headerValue =
                           "Basic " + PSBase64Encoder.encode( headerValue );
                  }
               }
               headers.addHeader(headerName, headerValue);
            }
         }
      }

      return headers;
   }

   protected void defineMacro(String macroName, String macroValue)
   {
      log("Defining macro " + macroName + "=" + macroValue);
      m_vars.put(macroName, macroValue);
   }

   /**
    * Replaces any macros in this element attribute values and any child text
    * node values. Does this recursively for its children too.
    *
    * @param replaceElem the element for which macros to be replaced, may not be
    * <code>null</code>
    *
    * @throws IllegalArgumentException if replaceElem is <code>null</code>
    */
   public void replaceMacros(Element replaceElem)
   {
      if(replaceElem == null)
         throw new IllegalArgumentException("replaceElem may not be null.");

      NamedNodeMap nodeMap = replaceElem.getAttributes();

      for(int i=0; i<nodeMap.getLength(); i++)
      {
         Node attrib = nodeMap.item(i);
         attrib.setNodeValue(getAttribute(replaceElem, attrib.getNodeName()));
      }

      if(replaceElem.hasChildNodes())
      {
         NodeList childNodes = replaceElem.getChildNodes();
         for(int i=0; i<childNodes.getLength(); i++)
         {
            Node childEl = childNodes.item(i);
            if(childEl instanceof Element)
               replaceMacros((Element)childEl);
            else if(childEl instanceof Text)
            {
               Text textNode = (Text)childEl;
               textNode.replaceData(0, textNode.getData().length(),
                  replaceMacros(textNode.getData()));
            }
         }
      }
   }

   protected String getAttribute(Element el, String attributeName)
   {
      return replaceMacros(el.getAttribute(attributeName));
   }

   protected void handle(Element el) throws Exception
   {
      String nodeName = el.getNodeName();
      if (nodeName.equals(GET_REQUEST_TAG))
         handleGetRequest(el);
      else if (nodeName.equals(EXEC_BLOCK_TAG))
         handleExecBlock(el);
      else if (nodeName.equals(PAGE_BLOCK_TAG))
         handlePageExecBlock(el);
      else if (nodeName.equals(COMPUTER_STATS_TAG))
         handleComputerStats(el);
      else if (nodeName.equals(USE_COOKIES_TAG))
         handleUseCookies(el);
      else if (nodeName.equals(POST_REQUEST_TAG))
         handlePostRequest(el);
      else if (nodeName.equals(PSPostSoapRequest.XML_NODE_NAME))
         handleSoapRequest(el);
      else if (nodeName.equals("Synchronize"))
         handleSynchronize(el);
      else if (nodeName.equals("RunScript"))
         handleRunScript(el);
      else if (nodeName.equals("SyncObject"))
         handleSyncObject(el);
      else if (nodeName.equals("Definitions"))
         handleDefinitions(el);
      else if (nodeName.equals("Macro"))
         handleMacro(el);
      // Extensible support through loaded classes implementing IExtendAutotest
      else if (nodeName.equals("ExtendedJavaSupportCall"))
         handleExtendedJavaSupportCall(el);
      else if (nodeName.equalsIgnoreCase("OnCondition"))
         handleOnCondition(el);
      else if (nodeName.equalsIgnoreCase("log")) // for debugging only
         handleLog(el);
      else if (nodeName.equalsIgnoreCase("Sleep"))
         handleSleep(el);
      else
         throw new ScriptTestErrorException("unexpected element " + el.getTagName());
   }

   /**
    * A PageExecBlock is a special ExecBlock. It executes the first request.
    * After the first byte is received, all other requests in the block are
    * queued and executed by a thread pool. After the last request has
    * finished, the block is done.
    *
    * @param pageExec The PageExecBlock element. Never <code>null</code>.
    *
    * @throws IllegalArgumentException if pageExec is <code>null</code> or
    *    is not the correct type.
    *
    * @throws ScriptTestErrorException if the 'random' attribute is specified
    *    or an invalid value for threadPoolSize is specified or some other
    *    runtime error occurs during execution of the commands within this
    *    block.
    */
   protected void handlePageExecBlock(Element pageExec) throws Exception
   {
      if (null == pageExec || !pageExec.getTagName().equals(PAGE_BLOCK_TAG))
         throw new IllegalArgumentException("element is null or the wrong type");

      log(PAGE_BLOCK_TAG);

      EmpireGroupedRequestManager mgr = null; // needed in finally block
      int pageId = -1;  // invalid pageId
      try
      {
         final String stopOnError = getAttribute(pageExec,
            STOPONERR_ATTRIB_NAME);
         final boolean bStopOnError = (stopOnError != null &&
            stopOnError.equalsIgnoreCase("yes"));

         String random = getAttribute(pageExec, RANDOM_ATTRIB_NAME);
         if (random.trim().length() > 0)
            throw new ScriptTestErrorException(PAGE_BLOCK_TAG  +
               " does not support the " + RANDOM_ATTRIB_NAME + " attribute");

         int threadsInPool = 1;  // default
         String threadPoolSizeName = "threadPoolSize";
         String threadPoolSize =
            getAttribute(pageExec, threadPoolSizeName).trim();
         if (threadPoolSize.length() > 0)
         {
            threadsInPool = Integer.parseInt(threadPoolSize);
            if (threadsInPool < 1)
               throw new ScriptTestErrorException("Invalid count for " +
                  threadPoolSizeName + ". Valid values are > 0");
         }

         String groupName = getAttribute(pageExec, "name");
         pageId = m_results.startPage(groupName, threadsInPool);

         StopCondition stop = getStopCondition(pageExec);

         NodeList children = pageExec.getChildNodes();
         int numChildren = children.getLength();
         List childVec = new ArrayList(numChildren / 2);

         for (int i = 0; i < numChildren; i++)
         {
            Node kid = children.item(i);
            if (kid instanceof Element)
               childVec.add(kid);
         }

         while (!stop.shouldStop())
         {
            // build action list
            List actions = new ArrayList(childVec.size());
            Iterator iter = childVec.iterator();
            Element firstRequest = (Element) iter.next();
            if (firstRequest.getTagName().equals( USE_COOKIES_TAG))
            {
               handle(firstRequest);
               firstRequest = (Element) iter.next();
            }
            String nodeName = firstRequest.getTagName();
            if (!(nodeName.equals(GET_REQUEST_TAG) ||
               nodeName.equals(POST_REQUEST_TAG)))
            {
               throw new ScriptTestErrorException("Invalid node type found (" +
                  nodeName + "). Valid types are " + GET_REQUEST_TAG +
                  " and " + POST_REQUEST_TAG);
            }
            while (iter.hasNext())
            {
               final Element child = (Element) iter.next();
               nodeName = child.getNodeName();
               if (nodeName.equals(GET_REQUEST_TAG) ||
                  nodeName.equals(POST_REQUEST_TAG))
               {
                  actions.add(new IPSAction ()
                  {
                     public void perform() throws Exception
                     {
                        try
                        {
                           handle(child);
                        }
                        catch (ScriptInterruptedException sie)
                        {
                           throw sie;
                        }
                        catch (Exception e)
                        {
                           if (bStopOnError)
                              throw e;
                        }
                     }
                  });
               }
               else
               {
                  throw new ScriptTestErrorException("Invalid node type found" +
                     " (" + nodeName + "). Valid types are " +
                     GET_REQUEST_TAG + " and " + POST_REQUEST_TAG);
               }
            }

            // prepare the parallel requests before we start the first one
            mgr = new EmpireGroupedRequestManager(actions.iterator(),
               threadsInPool);

            /* Process the first request, then process all other requests in
               parallel. phtodo - do we need to worry about timing here?*/
            try
            {
               handle( firstRequest );
            }
            catch (Exception e)
            {
               log("***************************************************");
               log("Exception on first request of page exec block, rest of block skipped");
               log(e.toString());
               log("***************************************************");

               throw e;
            }

            // process the action list
            Iterator exceptions = mgr.process();
            if (exceptions.hasNext())
            {
               StringBuffer buf = new StringBuffer();
               while (exceptions.hasNext())
               {
                  Exception e = (Exception) exceptions.next();
                  buf.append(e.getLocalizedMessage());
                  buf.append("\r\n");
               }
               throw new ScriptTestErrorException("One or more exceptions " +
                  "occurred while processing a PageExecBlock:\r\n" +
                  buf.toString());
            }
         }
      }
      finally
      {
         if (pageId > 0)
            m_results.endPage(pageId);
         if (null != mgr)
            mgr.shutdown();
      }
   }

   /**
    * Causes counter statistics to be collected for each request made. It
    * creates a thread that collects samples on a specified interval and
    * assigns them to a QAPerformanceStatistics object. The object should
    * be assigned at the beginning of the request and cleared at the end.
    * <p>The mechanism is generic, but it currently only supports a single
    * counter that is saved as the CPU % usage for the machine.
    *
    * @param computerStats The element in the test script containing this
    *    data. Never <code>null</code>.
    *
    * @throws IllegalArgumentException if computerStats is <code>null</code>
    *    or its tag name is incorrect or this element has already appeared
    *    in the document.
    *
    * @throws NumberFormatException If the collectionInterval attribute
    *    can't be interpretted as a number.
    */
   protected void handleComputerStats( Element computerStats )
      throws Exception
   {
      if ( null == computerStats
            || !computerStats.getTagName().equals(COMPUTER_STATS_TAG))
      {
         throw new IllegalArgumentException(
               "element is null or the wrong type" );
      }
      if ( null != m_statsCollector )
      {
         throw new IllegalArgumentException( COMPUTER_STATS_TAG
               + " element can appear only once in a test script" );

      }
      String interval = getAttribute( computerStats, "collectionInterval" );
      int millis = 1000; // default to 1 sample per second
      if ( null != interval && interval.trim().length() > 0 )
         millis = Integer.parseInt( interval.trim());

      NodeList children =
            computerStats.getElementsByTagName( COUNTER_DEF_TAG );
      Element pathEl = null;
      if ( children.getLength() > 0 )
         pathEl = (Element) children.item(0);
      if ( null == pathEl )
      {
         throw new ScriptTestErrorException( "Missing child element for "
               + COMPUTER_STATS_TAG + ". Expected " + COUNTER_DEF_TAG );
      }
      String path = getValue(pathEl, true).trim();
      if ( null == path || path.length() == 0 )
      {
         throw new ScriptTestErrorException( "Missing counter path in "
               + COUNTER_DEF_TAG );
      }
      m_statsCollector = new StatsCollector( millis, path );
      m_statsCollector.start();
      QAPerformanceStats stats = new QAPerformanceStats();
      beginStatsCollection( stats );
      m_results.setCpuUsage(stats);
      log( "Performance statistics collection initialized" );
   }

   /**
    * Allows the scripter to control how cookies are handled. Cookie use can
    * be either enabled or disabled. If enabled, any cookies sent by the
    * server are saved and sent with the subsequent matching request. If
    * disabled, any cookies sent from the server are discarded before sending 
    * the next request.  If a 'clear' command is specified, then the any cookies
    * previously sent from the server are cleared just before sending the
    * next request.
    */
   protected void handleUseCookies( Element cookieCmd )
      throws Exception
   {
      if ( null == cookieCmd || !cookieCmd.getTagName().equals(USE_COOKIES_TAG))
      {
         throw new IllegalArgumentException(
               "element is null or the wrong type" );
      }

      String command = getAttribute( cookieCmd, "command" );
      log( USE_COOKIES_TAG + ": " + command );

      if ( command.equals("disable"))
      {
         m_useCookies = false;
      }
      else if ( command.equals("enable"))
      {
         m_useCookies = true;
      }
      else if ( command.equals("clear"))
      {
         m_clearCookies = true;
      }
      else
      {
         throw new ScriptTestErrorException( "Invalid command for the "
               + USE_COOKIES_TAG + " element. Valid values are disable, "
               + "enable, or clear." );
      }
   }

   /* Handle extended java support, load the specified class and call its
      runExtensionAction method  with the associated element.

      NOTE:  This method currently expands all attribute values in the specified element,
      but does NOTHING to the element data itself or any sub-elements...
   */
   protected void handleExtendedJavaSupportCall(Element javaExtensionInfo)
      throws Exception
   {
      String className = getAttribute(javaExtensionInfo, "className");

      Class c = Class.forName(className);

      IPSAutoTestJavaExtension ext = (IPSAutoTestJavaExtension) c.newInstance();

      NamedNodeMap map = javaExtensionInfo.getAttributes();
      int mapLen = 0;

      if (map != null)
         mapLen = map.getLength();

      if (mapLen > 0)
      {
         String fred[] = new String[mapLen];

         for (int i = 0; i < mapLen; i++)
         {
            fred[i] = map.item(i).getNodeName();
         }

         /* Translate each attribute value by expanding the script macros within it */
         for (int i = 0; i < mapLen; i++)
         {
            javaExtensionInfo.setAttribute( fred[i],
                                 getAttribute(javaExtensionInfo, fred[i]) );
         }
      }

      ext.runExtensionAction(javaExtensionInfo);
   }

   protected void handleOnCondition(Element onCondEl) throws Exception
   {
      String left  = getAttribute(onCondEl, "left" );
      String op    = getAttribute(onCondEl, "op"   );
      String right = getAttribute(onCondEl, "right");

      log("Handle ON Condition: " + left + op + right);

      boolean shouldHandle = false;

      if (op.equals("="))
         shouldHandle = left.equalsIgnoreCase(right);
      else if (op.equals("!="))
         shouldHandle = !left.equalsIgnoreCase(right);
      else if (op.equals(">"))
         shouldHandle = (new Double(left).compareTo(new Double(right)) > 0);
      else if (op.equals(">="))
         shouldHandle = (new Double(left).compareTo(new Double(right)) >= 0);
      else if (op.equals("<"))
         shouldHandle = (new Double(left).compareTo(new Double(right)) < 0);
      else if (op.equals("<="))
         shouldHandle = (new Double(left).compareTo(new Double(right)) <= 0);
      else if (op.equals("LIKE"))
         shouldHandle = PSPatternMatcher.SQLPatternMatcher(right).doesMatchPattern(left);

      if (shouldHandle)
      {
         log("ON condition true");
         NodeList children = onCondEl.getChildNodes();
         for (int i = 0; i < children.getLength(); i++)
         {
            Node kid = children.item(i);
            if (kid instanceof Element)
               handle((Element)kid);
         }
      }
      else
      {
         log("ON condition false");
      }
   }

   protected void handleLog(Element logElem)
   {
      log("\t" + getValue(logElem, true));
   }

   /*
    * sleep for specified interval.
    */
   protected void handleSleep(Element sleepEl) throws Exception
   {
      String interval  = getAttribute(sleepEl, "interval" );
      int sleepInt;
      try
      {
         sleepInt = Integer.parseInt(interval);
         log("Sleeping " + (sleepInt / 1000) + " seconds...");
         Thread.sleep(sleepInt);
      }
      catch (NumberFormatException e)
      {
         throw new ScriptTestErrorException("Invalid Sleep interval: " + interval);
      }
      catch (InterruptedException e)
      {
         throw new ScriptTestErrorException("Unable to sleep: " + e.toString());
      }
   }

   protected void handleDefinitions(Element defsElem) throws Exception
   {
      log("Definitions");
      NodeList children = defsElem.getChildNodes();
      for (int i = 0; i < children.getLength(); i++)
      {
         Node kid = children.item(i);
         if (kid instanceof Element)
            handle((Element)kid);
      }
   }

   protected void handleExecBlock(Element execBlockElem) throws Exception
   {
      log(EXEC_BLOCK_TAG);

      final String stopOnError = getAttribute(execBlockElem,
            STOPONERR_ATTRIB_NAME );
      final boolean bStopOnError = (stopOnError != null && stopOnError.equalsIgnoreCase("yes"));

      StopCondition stop = getStopCondition(execBlockElem);

      NodeList children = execBlockElem.getChildNodes();
      int numChildren = children.getLength();
      Vector childVec = new Vector(numChildren / 2);

      for (int i = 0; i < numChildren; i++)
      {
         Node kid = children.item(i);
         if (kid instanceof Element)
            childVec.add(kid);
      }

      Random rand = null;
      String isRandom = getAttribute(execBlockElem, RANDOM_ATTRIB_NAME);
      if (isRandom != null && (isRandom.equals("1") || isRandom.equals("yes")))
         rand = new Random();

      while(!stop.shouldStop())
      {
         int childIndex = 0;
         for (int i = 0; i < childVec.size(); i++)
         {
            if (rand != null)
               childIndex = rand.nextInt(childVec.size());
            Element child = (Element)childVec.elementAt(childIndex++);
            try
            {
               handle(child);
            }
            catch (ScriptInterruptedException sie)
            {
               throw sie;
            }
            catch (Exception e)
            {
               if (bStopOnError)
                  throw e;
            }
            if (rand != null)
               break;
         }
      }
   }

   /**
    * This class provides a simple interface for implementing the various
    * stopwhen conditions of the exec block elements with a consistent
    * interface.
    */
   abstract class StopCondition
   {
      /**
       * Derived classes must implement this method such that it behaves
       * properly for the appropriate stopwhen condition.
       *
       * @return <code>true</code> if the looping is supposed to terminate,
       *    <code>false</code> otherwise.
       */
      public abstract boolean shouldStop();
   }

   /**
    * Reads the stopwhen attribute of the supplied element and returns an
    * appropriate condition object. The returned object is used by calling
    * the <code>shouldStop</code> method in a loop within which all children
    * of the supplied element are being processed.
    *
    * @throws ScriptTestErrorException If the stopwhen condition is invalid.
    */
   private StopCondition getStopCondition(Element exec)
      throws ScriptTestErrorException
   {
      StopCondition stop;
      final String stopWhen = getAttribute(exec, "stopwhen");
      if (stopWhen == null || stopWhen.length() == 0)
      {
         /*
          * If no stopwhen attribute is supplied we execute that block exactly
          * once.
          */
         stop = new StopCondition()
         {
            public boolean shouldStop()
            {
               boolean shouldStop = m_shouldStop;
               m_shouldStop = true;
               return shouldStop;
            }

            protected boolean m_shouldStop = false;
         };
      }
      else if(Character.isDigit(stopWhen.charAt(0)))
      {
         /*
          * If the stopwhen attribute is a number, we execute that block as many
          * times as specified.
          */
         stop = new StopCondition()
         {
            public boolean shouldStop()
            {
               if (m_times < 0)
                  m_times = 0;
               return m_times-- <= 0;
            }

            protected int m_times = Integer.parseInt(stopWhen);
         };
      }
      else
      {
         /*
          * If the stopwhen is like <macro>=<value1>;<value2;...;<valueN> this
          * will set the macro to each value specified in the list and run the
          * execution block once for that macro value.
          */
         int eqPos = stopWhen.indexOf('=');
         if (eqPos < 1)
            throw new ScriptTestErrorException("Invalid stopwhen: " + stopWhen);

         final String macroName = stopWhen.substring(0, eqPos).trim();

         String rightSide = "";

         if (eqPos < stopWhen.length() - 2)
            rightSide = stopWhen.substring(eqPos + 1, stopWhen.length());

         final String macroValues = rightSide;

         stop = new StopCondition()
         {
            public boolean shouldStop()
            {
               if (m_values == null)
               {
                  m_values = new ArrayList(10);
                  StringTokenizer tok = new StringTokenizer(macroValues, ";");
                  String logString;
                  String nextTok;
                  for (logString = "Will be looping over values for " +
                     m_paramName + ": "; tok.hasMoreTokens();
                     logString = logString + nextTok + ", ")
                  {
                     nextTok = tok.nextToken();
                     if (nextTok.equals("\\")) // single backslash evaluates to an empty token
                        m_values.add("");
                     else
                        m_values.add(nextTok);
                  }

                  log(logString);
               }

               if (m_index >= m_values.size())
               {
                  return true;
               }
               else
               {
                  defineMacro(m_paramName, (String)m_values.get(m_index++));
                  return false;
               }
            }

            protected String m_paramName = macroName;
            protected List m_values = null;
            protected int m_index = 0;
         };
      }
      return stop;
   }

   /**
    * Make the HTTP request provided in the supplied request.
    *
    * @param req the HTTP request to make, assumed not <code>null</code>.
    * @throws ScriptTestFailedException if no HTTP connection could be
    *    established.
    * @throws IOException for any failed IO operation.
    */
   private void makeRequest(PSHttpRequest req) throws ScriptTestFailedException,
      IOException
   {
      try
      {
         req.sendRequest();
      }
      catch (Exception e)
      {
         throw new ScriptTestFailedException(e.getLocalizedMessage());
      }
   }

   /**
    * Handles a soap request.  Handles making a soap request, retrieving expect
    * data and expect comparisons.
    *
    * @param requestElem The element containing the Soap request.  May
    * not be <code>null</code> and must be a <code>PostSOAPRequest</code>
    * element.
    *
    * @throws IllegalArgumentException If <code>requestElem</code> is
    * <code>null</code> or of the wrong type.
    * @throws Exception If there are any unexpected errors.
    */
   protected void handleSoapRequest(Element requestElem) throws Exception
   {
      if (requestElem == null)
         throw new IllegalArgumentException("requestElem may not be null");

      if(!requestElem.getNodeName().equals(PSPostSoapRequest.XML_NODE_NAME))
         throw new IllegalArgumentException(
            "requestElem is not of expected type, expecting <" +
            PSPostSoapRequest.XML_NODE_NAME + "> element");

      // create stats up front to record CPU utilization
      QAPerformanceStats stats = new QAPerformanceStats();

      //Initilize the test case and proceed if it is not skipped.
      StringBuffer test_id = new StringBuffer();
      if(!initializeTest("SOAP", requestElem, test_id)) //skipped so return
         return;
      //Get the test id.
      int testID = Integer.parseInt(test_id.toString());

      // get the actual data, get the expected data, and compare them
      InputStream actualIn = null;
      // need a copy of actualIn stream to log to results in case of error
      ByteArrayOutputStream actualInCopy = null;
      String actualStatus = null;

      String expectHref = null;

      try
      {
         //build the soap request from request element
         PSPostSoapRequest soapRequest =
            new PSPostSoapRequest(requestElem, this);

         soapRequest.sendRequest();

         if(soapRequest.generatedFault())
            throw new ScriptTestFailedException(
               "Invalid test case/server error - " +
               soapRequest.getFaultDetail());

         log("Getting actual response stream...");
         actualIn = soapRequest.getResponseContent();
         actualStatus = soapRequest.getResponseStatus();
         Header actualHeader = soapRequest.getResponseHeader();
         log("Got actual response stream.");

         // get the expected headers/values and the URL of the expected data
         Element expectEl = getChildElement(requestElem, "Expect");

         if (expectEl != null)
         {
            expectHref  = getAttribute(expectEl, REQ_URL_REF_ATTR);

            // get expected info
            String expectStatus = getAttribute(expectEl, "soapResponseStatus");

            // test the actual response code against the expected resp code
            if (expectStatus != null && !expectStatus.equals(actualStatus))
            {
               throw new ScriptTestFailedException(
                  "Expected response status = " + expectStatus
                  + ", actually got " + actualStatus);
            }

            log("Got status <" + actualStatus +
               ">, expected <" + expectStatus + ">");

            Map exits = prepareResponseExits( expectEl );
            Iterator keys = exits.keySet().iterator();
            while ( keys.hasNext())
            {
               Object exitInstance = keys.next();
               if(!(exitInstance instanceof IPSSoapResponseExit))
                  throw new ScriptTestErrorException(
                     "The exit <" + exitInstance.getClass().getName() +
                     "> does not implement IPSSoapResponseExit");
               IPSSoapResponseExit exit = (IPSSoapResponseExit) exitInstance;
               Properties props = (Properties) exits.get( exit );
               actualIn =
                  exit.processResponse( props, this, actualHeader, actualIn );
            }

            // need to make a copy of actualIn here for comparison to use
            // so we can write the actualIn stream to the results in the case
            // of failure/error
            actualInCopy = new ByteArrayOutputStream();
            IOTools.copyStream(actualIn, actualInCopy);
            actualIn.close();
            actualIn = new ByteArrayInputStream( actualInCopy.toByteArray() );

            String actualCharSet = soapRequest.getCharacterSet();

            compareResponseBody(expectEl, actualIn, actualCharSet);

         } // end if any kind of expect is specified

         // if we made it this far, then the test passed
         m_results.passTest(testID, stats);

         if (!m_logToScreen)
         {
            synchronized (this)
            {
               m_statusCharIndex++;
               if (m_statusCharIndex >= ms_statusChars.length)
                  m_statusCharIndex = 0;

               System.out.print("\b" + ms_statusChars[m_statusCharIndex]);
            }
         }

      }
      catch(Throwable t)
      {
         handleException(t, testID, stats, actualInCopy,
            actualStatus, requestElem, expectHref);
      }
      finally
      {
         if (actualIn != null)
            { try { actualIn.close(); } catch (Throwable t) { log(t); } }
         if(actualInCopy != null)
            { try { actualInCopy.close(); } catch (Throwable t) { log(t); } }
      }
   }


   /**
    * Handles a Get or Post request.  Handles authentication, cookies,
    * retrieving expect data and handling expect comparisons.
    *
    * @param requestElem The element containing the Get or Post request.  May
    * not be <code>null</code>.
    *
    * @throws IllegalArgumentException If <code>requestElem</code> is
    * <code>null</code> or of the wrong type.
    * @throws Exception If there are any unexpected errors.
    */
   protected void handleGetOrPostRequest(Element requestElem) throws Exception
   {
      if (requestElem == null)
         throw new IllegalArgumentException("requestElem may not be null");

      // read the href and create a test case object
      String elementType = requestElem.getNodeName();
      String requestType = null;
      if (elementType.equals(POST_REQUEST_TAG))
         requestType = "POST";
      else if (elementType.equals(GET_REQUEST_TAG))
         requestType = "GET";
      else
         throw new IllegalArgumentException("invalid requestElem node type");

      // create stats up front to record CPU utilization
      QAPerformanceStats stats = new QAPerformanceStats();

      StringBuffer test_id = new StringBuffer();
      if(!initializeTest(requestType, requestElem, test_id)) //skipped so return
         return;
      //Get the test id.
      int testID = Integer.parseInt(test_id.toString());

      // get the actual data, get the expected data, and compare them
      InputStream actualIn = null;
      // need a copy of actualIn stream to log to results in case of error
      ByteArrayOutputStream actualInCopy = null;
      PSHttpRequest actualReq = null;
      int actualCode = -1;
      String actualCharSet = "ISO-8859-1";
      String expectHref = null;

      InputStream postContentIn = null;
      long postContentLength = -1L;

      long timeout = 30000L;  // 30 seconds
      byte [] contentBytes = null;

      try
      {
         // build an HTTP request for the actual data

         //Get the post content length if it is provided for a post request.
         if (requestType.equals("POST"))
         {
            // if the user has set this, then they better have gotten it correct
            String contentLength = getAttribute(requestElem,
               "postContentLength");
            if (contentLength != null && contentLength.length() > 0)
            {
               if (!contentLength.equals("auto"))
               {
                  postContentLength = Long.parseLong(contentLength);
               }
            }

            // get our post data
            String postHref = getAttribute(requestElem, POST_CONTENT_REF_ATTR);
            if (postHref != null && postHref.length() > 0)
            {
               log("Getting POST request content data from " + postHref);

               HttpHeaders postContentHeaders = new HttpHeaders();
               NodeList children = requestElem.getChildNodes();
               for (int i = 0; i < children.getLength(); i++)
               {
                  Node kid = children.item(i);
                  if ((kid instanceof Element) &&
                     kid.getNodeName().equals(POST_CONTENT_HDRS_ATTR))
                  {
                     postContentHeaders.addAll(
                        getChildHeaders((Element)kid));
                     break;
                  }
               }
               postContentIn = getContentStream(postHref, 0L,
                  postContentHeaders);

               URL postContentURL = new URL(postHref);
               if (postContentURL.getProtocol().equalsIgnoreCase("FILE") )
               {
                  File contentFile = new File(postContentURL.getFile());
                  postContentLength = contentFile.length();
               }

               /* 
                * Replace macros used in the post document and convert to a 
                * resetable stream to support resending.
                */
               if (postContentIn != null)
               {
                  Document postContentInDoc = 
                     PSXmlDocumentBuilder.createXmlDocument(postContentIn, 
                        false);
                  replaceMacros(postContentInDoc.getDocumentElement());

                  ByteArrayOutputStream bos = new ByteArrayOutputStream();
                  PSXmlDocumentBuilder.write(postContentInDoc, bos);                  

                  contentBytes = bos.toByteArray();
                  
                  /*
                   * Content length might have changed because of replaced 
                   * macros and must be reset.
                   */
                  postContentLength = contentBytes.length;

                  postContentIn = new ByteArrayInputStream(contentBytes);
               }
            }
         }

         String href = getAttribute(requestElem, REQ_URL_REF_ATTR);
         actualReq = new PSHttpRequest(href, requestType, postContentIn);

         actualReq.enableTrace(this);
         actualReq.addRequestHeaders(getChildHeaders(requestElem,
            !isAutoLoginEnabled()));

         // set the Content-Length header to our value (either calculated or
         // hard-coded)
         if (postContentLength >= 0L)
         {
            actualReq.addRequestHeader("Content-Length", "" +
               postContentLength);
         }

         if (!m_useCookies || m_clearCookies)
         {
            actualReq.clearAllCookies();
         }
         m_clearCookies = false;
         
         makeRequest(actualReq);
         // stats.setBytesSent( actualReq.getBytesSent());

         postContentIn = null; // this is closed after a request

         log("Getting actual response stream...");
         actualIn = actualReq.getResponseContent();
         log("Got actual response stream.");

         // get the expected headers/values and the URL of the expected data
         Element expectEl = getChildElement(requestElem, "Expect");

         if (expectEl != null)
         {
            expectHref  = getAttribute(expectEl, REQ_URL_REF_ATTR);

            // get the acceptable range of HTTP response codes
            int expectedHttpCodeLoRange   = 200;
            int expectedHttpCodeHiRange   = 200;

            {
               String code = getAttribute(expectEl, "httpCode");
               if (code != null && code.length() > 0)
               {
                  // see if they have given us a range or a single number
                  int dashPos = code.indexOf('-');
                  if (dashPos > 0)
                  {
                     // a range was specified
                     expectedHttpCodeLoRange =
                        Integer.parseInt(code.substring(0, dashPos));
                     expectedHttpCodeHiRange =
                        Integer.parseInt(code.substring(dashPos+1));
                  }
                  else
                  {
                     // a single number was specified
                     expectedHttpCodeLoRange = Integer.parseInt(code);
                     expectedHttpCodeHiRange = expectedHttpCodeLoRange;
                  }
               }
            }

            // check if we need to login
            actualCode = actualReq.getResponseCode();
            if (!(actualCode  >= expectedHttpCodeLoRange &&
               actualCode <= expectedHttpCodeHiRange) &&
               401 == actualCode && isAutoLoginEnabled())
            {
               // try to login once
               String cred = getLoginCredential(requestElem);
               actualReq.addRequestHeader( "Authorization", cred );
               log( "Request failed. Resending with Authorization = " + cred );
               // reset post content, if there is any
               if ( null != contentBytes )
               {
                  actualReq.setRequestContent(
                        new ByteArrayInputStream( contentBytes ));
               }

               makeRequest(actualReq);
               //stats.setBytesSent(actualReq.getBytesSent());
               actualIn = actualReq.getResponseContent();
               actualCode = actualReq.getResponseCode();
            }

            HttpHeaders actualHeaders = actualReq.getResponseHeaders();

            Map exits = prepareResponseExits( expectEl );
            Iterator keys = exits.keySet().iterator();
            while ( keys.hasNext())
            {
               Object exitInstance = keys.next();
               if(!(exitInstance instanceof IResponseExit))
                  throw new ScriptTestErrorException(
                     "The exit <" + exitInstance.getClass().getName() +
                     "> does not implement IResponseExit");
               IResponseExit exit = (IResponseExit) exitInstance;
               Properties props = (Properties) exits.get( exit );
               actualIn =
                  exit.processResponse( props, this, actualHeaders, actualIn );
            }

            long actualContentLength = 0;

            {
               String actualContentLengthHdr = actualHeaders.getHeader(
                  "Content-Length");
               if (actualContentLengthHdr != null)
               {
                  actualContentLength = Long.parseLong(actualContentLengthHdr);
               }
            }

            long sleptFor = 0;

            if (actualContentLength != 0)
            {
               log("Waiting for " + actualContentLength + " bytes...");
               // wait for the initial results to become available
               for (int waitFor = 100; actualIn.available() == 0;
                  waitFor += 1000)
               {
                  try
                  {
                     if (waitFor >= 1000)
                     {
                        log("Sleeping for " + waitFor + "ms");
                        Thread.sleep(waitFor);
                        sleptFor += waitFor;
                     }
                  }
                  catch(InterruptedException e)
                  {
                     log(e);
                     throw new ScriptInterruptedException(
                        "interrupted while waiting for socket data");
                  }

                  if (timeout != 0L && (long)sleptFor >= timeout)
                     throw new ScriptTimeoutException(
                        "timed out while waiting for socket data");
               }
            }

            // need to make a copy of actualIn here for comparison to use
            // so we can write the actualIn stream to the results in the case
            // of failure/error
            actualInCopy = new ByteArrayOutputStream();
            if (actualContentLength > 0)
            {
               IOTools.copyStream(actualIn, actualInCopy);
               try
               {
                  actualIn.close();
               }
               catch (Exception e)
               {
                  // ignore error here
               }
            }
            actualIn = new ByteArrayInputStream( actualInCopy.toByteArray() );

            // get expected info
            log("Getting expected headers...");
            HttpHeaders expectedHeaders = getChildHeaders(expectEl);
            // test the actual response code against the expected resp code
            if (! (actualCode  >= expectedHttpCodeLoRange
                  && actualCode <= expectedHttpCodeHiRange))
            {
               throw new ScriptTestFailedException(
                  "Expected response code >= " + expectedHttpCodeLoRange
                  + " and <= " + expectedHttpCodeHiRange
                  + ", actually got " + actualCode);
            }
            log("Got code " + actualCode + ", expecting "
               + expectedHttpCodeLoRange + "-" + expectedHttpCodeHiRange);


             // compare the headers
            log("Comparing the headers");
            compareHttpHeaders(expectedHeaders, actualHeaders);

            String actualContentType = actualHeaders.getHeader("Content-Type");
            if (actualContentType != null)
            {
               Map contentParams = new HashMap();
               PSHttpUtils.parseContentType(actualContentType, contentParams);
               String cs = (String)contentParams.get("charset");
               if (cs != null)
                  actualCharSet = cs;
            }

            compareResponseBody(expectEl, actualIn, actualCharSet);


            actualIn.close();
            actualIn = null;  // prevent double closure
         } // end if any kind of expect is specified

         // if we made it this far, then the test passed
         /*
         HttpRequestTimings timings = actualReq.getTimings();
         stats.setTimeOfRequest( timings.beforeConnect());
         stats.setConnectTime( timings.afterConnect());
         stats.setTimeOfFirstByte( timings.getTimeAfterFirstByte());
         stats.setTimeToLastByte( timings.afterContent());
         stats.setBytesReceived(
               (int) (timings.headerBytes() + timings.contentBytes()));
         */
         m_results.passTest(testID, stats);

      }
      catch(Throwable t)
      {
         handleException(t, testID, stats, actualInCopy,
            String.valueOf(actualCode), requestElem, expectHref);
      }
      finally
      {
         if (actualIn != null)
            { try { actualIn.close(); } catch (Throwable t) { log(t); } }
         if (actualReq != null)
            { try { actualReq.disconnect(); } catch (Throwable t) { log(t); } }
      }
   }

   /**
    * Gets the child element of the <code>requestElem</code> with specified
    * element name. The match is case sensitive.
    *
    * @param requestElem the reuest element to search, assumed not to be <code>
    * null</code>
    * @param elementName the child element to search for, assumed not <code>null
    * </code> or empty.
    *
    * @return the child element, may be <code>null</code> if not found.
    */
   private Element getChildElement(Element requestElem, String elementName)
   {
      Element expectEl = null;

      NodeList children = requestElem.getChildNodes();
      for (int i = 0; i < children.getLength(); i++)
      {
         Node kid = children.item(i);
         if ((kid instanceof Element) && kid.getNodeName().equals(
            elementName))
         {
            expectEl = (Element)kid;
            break;
         }
      }

      return expectEl;
   }

   /**
    * Initializes the test process for the supplied <code>requestElem</code> and
    * checks whether test needs to be skipped or not.
    *
    * @param requestType the request type, assumed not to be <code>null</code>
    * or empty.
    * @param requestElem the test script request element, assumed not <code>null
    * </code>
    * @param testID used to return the initialized test id, assumed not <code>
    * null</code> and empty, will not be <code>null</code> or empty after the
    * method returns, and will be populated with the string representation of an
    * <code>int</code>.
    *
    * @return <code>false</code> if the test needs to be skipped, otherwise
    * <code>true</code>
    */
   private boolean initializeTest(String requestType, Element requestElem,
      StringBuffer testID)
   {
      SimpleDateFormat formatter =
            new SimpleDateFormat( "HH:mm:ss MMM dd, yyyy" );
      log("Started processing " + requestType + " request at " +
         formatter.format( new Date(), new StringBuffer(),
         new FieldPosition(0)));
      String href = getAttribute(requestElem, REQ_URL_REF_ATTR);
      log(requestElem.getNodeName() + " : " + href);

      testID.append(m_results.createTest(requestType, href));

      // if this test should be skipped, then skip it
      String skipBecause = getAttribute(requestElem, "skipBecause");
      if (skipBecause != null && skipBecause.length() > 0)
      {
         // this test is disabled for some reason
         String message = "Skipped test because " + skipBecause;
         log(message);
         m_results.skipTest(Integer.parseInt(testID.toString()), message);
         return false;
      }
      return true;
   }

   /**
    * Gets the expected data from the supplied expected element and compares
    * with the supplied actual data if there is any expected data.
    *
    * @param expectEl the expect element, assumed not to be <code>null</code>
    * @param actualIn actual input result, assumed not <code>null</code> and
    * the stream should be closed by caller.
    * @param actualCharSet encoding of actual input result, assumed not
    * <code>null</code> nor empty.
    *
    * @throws Exception if an error occurs while performing the comparison.
    */
   private void compareResponseBody(Element expectEl, InputStream actualIn,
      String actualCharSet) throws Exception
   {
      InputStream expectedIn = null;
      try {
         log("Getting expected response...");
         HttpHeaders expectedHeaders = getChildHeaders(expectEl);
         String expectType         = getAttribute(expectEl, "type");
         String expectHref         = getAttribute(expectEl, REQ_URL_REF_ATTR);
         String expectCompareClass = getAttribute(expectEl, "compareclass");
         String expectStyleSheet = getAttribute(expectEl, "styleSheet");
         String expectTimeout = getAttribute(expectEl, "timeout");
         long timeout = 0L;
         if(expectTimeout != null && expectTimeout.trim().length() != 0)
         {
            try
            {
               timeout = Integer.parseInt(expectTimeout);
            }
            catch(NumberFormatException nfe)
            {
               throw new ScriptTestErrorException("Invalid expect timeout: "
                  + expectTimeout + " (" + nfe.toString() + ")");
            }
         }

         String prot = null;
         // only do content comparison if there is expect data
         String expectedCharSet = "ISO-8859-1";
         if (expectHref != null && expectHref.length() > 0)
         {
            log("Reading expect data from " + expectHref);
            try {
               URL expectURL = new URL(expectHref);
               prot = expectURL.getProtocol().toUpperCase();
               if(expectURL.getProtocol().toUpperCase().equals("FILE"))
                  expectedCharSet = System.getProperty("file.encoding");
            }
            catch(MalformedURLException e)
            {
               throw new ScriptTestErrorException("Invalid expect URL: " +
                  expectHref + " (" + e.toString() + ")");
            }

            NodeList expectChildren = expectEl.getChildNodes();
            HttpHeaders expectReqHeaders = new HttpHeaders();
            Node expectKid = null;
            for (int i = 0; i < expectChildren.getLength(); i++)
            {
               expectKid = expectChildren.item(i);

               if (!(expectKid instanceof Element))
                  continue;

               if (expectKid.getNodeName().equals(
                  "ExpectRequestHeaders"))
               {
                  expectReqHeaders.addAll(getChildHeaders((Element)expectKid));
               }
            }

            expectedIn = getContentStream(expectHref, timeout, expectReqHeaders);

            // replace all macros in the expected data
            ByteArrayOutputStream bos = null;
            try
            {
               Document expectedDoc = 
                  PSXmlDocumentBuilder.createXmlDocument(expectedIn, false);
               replaceMacros(expectedDoc.getDocumentElement());

               bos = new ByteArrayOutputStream();
               PSXmlDocumentBuilder.write(expectedDoc, bos);                  
               expectedIn.close();
               expectedIn = new ByteArrayInputStream(bos.toByteArray());
            }
            catch (Exception e)
            {
               e.printStackTrace();
               throw new ScriptTestErrorException(
                  "Macro replacement failed in the expected data.");
            } finally {
               if (bos!=null) 
                  try { bos.close();} catch (Exception e) {/*ignore*/ }
            }
         } // end retrieval of expect data

         String expectedContentType =
            expectedHeaders.getHeader("Content-Type");
         if (expectedContentType != null)
         {
            Map contentParams = new HashMap();
            PSHttpUtils.parseContentType(expectedContentType, contentParams);
            String cs = (String)contentParams.get("charset");
            if (cs != null)
               expectedCharSet = cs;
         }

         if (expectType == null || expectType.length() == 0 ||
            expectType.equals("content"))
         {
            if (expectedIn != null)
            {
               log("Comparing results");
               log("Comparing actual results in " + actualCharSet
                  + " with expected results in " + expectedCharSet + " ...");
               if (expectCompareClass == null ||
                  expectCompareClass.length() == 0)
               {
                  compareResults(prot, expectHref, expectedIn,
                     expectedCharSet, actualIn, actualCharSet,
                     expectStyleSheet);
               }
               else
               {
                  log("Using compareclass=" + expectCompareClass);
                  ICustomCompare compareClass = (ICustomCompare)
                     Class.forName( expectCompareClass ).newInstance();
                  compareClass.compare(expectedIn, expectedCharSet,
                     actualIn, actualCharSet);
               }
            }
         }
         else if (expectType.equals("xml"))
         {
            compareXml(expectEl, actualIn, actualCharSet);
         }
         else
         {
            throw new ScriptTestErrorException(
               "Unsupported expect type: " + expectType);
         }
      }
      catch (Exception e)
      {
         e.printStackTrace();
         throw e;
      }
      finally
      {
         if (expectedIn!=null) 
            try { expectedIn.close();} catch (Exception e) {/*ignore*/ }
      }
   }

   /**
    * Handles the exception raised by a test execution by logging and marking
    * the test as either fail or error based on type of exception.
    *
    * @param ex the exception to handle, assumed not <code>null</code>
    * @param testID the id of the test that raised the exception.
    * @param stats the stats to pass for marking the test as failed or error,
    * assumed not <code>null</code>
    * @param actualResponse the actual response to log, may be <code>null
    * </code>.
    * @param actualRespStatus the response status or code, may be <code>null
    * </code>
    * @param requestElem the request element to log, assumed not
    * <code>null</code>
    * @param expectUrl the URL of the expected xml doc., may be <code>null</code>.
    *
    * @throws ScriptInterruptedException if <code>ex</code> an instance of
    * <code>ScriptInterruptedException</code>
    * @throws ScriptExecAbortedException in any other case to indicate error or
    *
    * failure
    */
   private void handleException(Throwable ex, int testID,
      QAPerformanceStats stats, OutputStream actualResponse,
      String actualRespStatus,  Element requestElem, String expectHref)
      throws ScriptInterruptedException, ScriptExecAbortedException
   {
      if(ex instanceof ScriptInterruptedException)
      {
         throw (ScriptInterruptedException)ex;
      }
      else if (ex instanceof ScriptTimeoutException)
      {
         errorTest(testID, ex.getMessage());
      }
      else if (ex instanceof ScriptTestFailedException)
      {
         final String newLine = System.getProperty("line.separator");

         StringBuffer message = new StringBuffer();
         message.append( newLine );
         message.append( ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>" );
         message.append( newLine );

         // log the error
         message.append( "**** Failure message: " );
         message.append( ex.getLocalizedMessage() );
         message.append( newLine );

         // log the script element
         message.append( "**** Request element: " );
         message.append( PSXmlDocumentBuilder.toString(requestElem) );
         message.append( newLine );

         // log the actual response code
         message.append( "**** Actual server response code: " );
         if(actualRespStatus != null)
            message.append( actualRespStatus );
         message.append( newLine );

         // log the actual response
         message.append( "**** Actual server response: " );

         if (actualResponse != null)
         {
            String actualResult = actualResponse.toString();
            message.append( actualResult );

            saveActualResult(actualResult, expectHref);
         }

         message.append( newLine );

         // log the defined macros
         message.append( "**** Defined macros: " );
         message.append( m_vars.toString() );
         message.append( newLine );

         message.append( "<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<" );
         message.append( newLine );

         failTest(testID, message.toString(), stats);
      }
      else
      {
         final String newLine = System.getProperty("line.separator");

         StringBuffer message = new StringBuffer();
         message.append( newLine );
         message.append( ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>" );
         message.append( newLine );

         // log the error
         message.append( "**** Error message: " );
         message.append( ex.getLocalizedMessage() );

         message.append( newLine );

         // log the script element
         message.append( "**** Request element: " );
         message.append( PSXmlDocumentBuilder.toString(requestElem) );
         message.append( newLine );

         // log the actual response code
         message.append( "**** Actual server response code: " );
         if(actualRespStatus != null)
            message.append( actualRespStatus );
         message.append( newLine );

         // log the actual response
         message.append( "**** Actual server response: " );

         if (actualResponse != null)
         {
            String actualResult = actualResponse.toString();
            message.append( actualResult );

            saveActualResult(actualResult, expectHref);
         }

         message.append( newLine );

         // log the defined macros
         message.append( "**** Defined macros: " );
         message.append( m_vars.toString() );
         message.append( newLine );

         // log the exception callstack
         StringWriter cs = new StringWriter();
         ex.printStackTrace( new PrintWriter(cs) );
         message.append( "**** Callstack: " );
         message.append( cs.toString() );
         message.append( newLine );

         message.append( "<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<" );
         message.append( newLine );

         errorTest(testID, "Caught " + ex.getClass().getName() + "\r\n" +
            ex.getMessage());
      }
   }

   /**
    * Gets the content input stream by making a request to supplied reference
    * url.
    *
    * @param contentHref the valid content reference url, may not be <code>null
    * </code> or empty and must be a valid url reference.
    * @param timeout the time to wait to get the response from supplied url,
    * supply <code>0</code> to wait indefinitely.
    * @param headers the headers to send with the request, may not be <code>null
    * </code>, can be empty.
    *
    * @return the content stream, never <code>null</code>
    *
    * @throws IllegalArgumentException if any parameter is invalid.
    * @throws IOException if an io error occurs extracting data.
    * @throws ScriptTestFailedException if an exception happens making the
    * request.
    * @throws ScriptInterruptedException if interrupted while waiting to
    * retrieve the response.
    * @throws ScriptTimeoutException if the response could not be retrieved
    * before the specified timeout.
    * @throws ModuleException 
    */
   public InputStream getContentStream(String contentHref, long timeout,
      HttpHeaders headers) throws IOException, ScriptTestFailedException,
      ScriptInterruptedException, ScriptTimeoutException, ModuleException
   {
      if(contentHref == null || contentHref.trim().length() == 0)
         throw new IllegalArgumentException(
            "contentHref may not be null or empty.");

      if(headers == null)
         throw new IllegalArgumentException("headers may not be null.");

      URL contentURL = new URL(contentHref);
      String prot = contentURL.getProtocol().toUpperCase();
      InputStream contentIn;
      if (prot.equals("HTTP"))
      {
         PSHttpRequest contentReq = new PSHttpRequest(contentHref, "GET", null);
         contentReq.enableTrace(this);
         contentReq.addRequestHeaders(headers);

         // send the post content request...
         makeRequest(contentReq);
         contentIn = contentReq.getResponseContent();
         long sleptFor = 0;
         // wait for data to become available from the expected results
         for (int waitFor = 100; contentIn.available() == 0;
            waitFor += 1000)
         {
            try
            {
               if (waitFor >= 1000)
               {
                  log("Sleeping for " + waitFor + "ms");
                  Thread.sleep(waitFor);
                  sleptFor += waitFor;
               }
            }
            catch(InterruptedException e)
            {
               log(e);
               throw new ScriptInterruptedException(
                  "interrupted while waiting for data from " + contentHref);
            }
            if (timeout != 0L && sleptFor >= timeout)
               throw new ScriptTimeoutException(
                  "timed out while waiting for data from : " + contentHref +
                  ", wait time: " + waitFor);
         }
         //make a copy because when the request goes out of scope, the content
         //stream will be closed.
         ByteArrayOutputStream bos = new ByteArrayOutputStream();
         try {
            IOTools.copyStream(contentIn, bos);
            contentIn = new ByteArrayInputStream( bos.toByteArray() );
         }
         finally {
            bos.close();
         }
      }
      else if (prot.equals("FILE"))
      {
         File contentFile = new File(contentURL.getFile());
         contentIn = new FileInputStream(contentFile);
      }
      else
         throw new IllegalArgumentException("invalid content url reference");

      return contentIn;
   }

   protected void compareXml(Element expectEl, InputStream in, String charSet)
      throws Exception
   {
      // parse the input into an XML document
      InputSource inSrc = new InputSource(in);
      if (charSet != null && charSet.length() > 0)
         inSrc.setEncoding(charSet);

      log("Parsing input as XML document");
      Document inDoc = PSXmlDocumentBuilder.createXmlDocument(inSrc, false);

      String root = inDoc.getDocumentElement().getNodeName();
      log("Document parsed: root node <" + root + ">");

      Element xmlRoot = null;
      Element xmlDtd = null;
      ArrayList expectedNodes = new ArrayList();
      ArrayList expectedValues = new ArrayList();
      ArrayList isValueRegex = new ArrayList();

      {// // // // //
         NodeList expectChildren = expectEl.getChildNodes();
         Node kid = null;

         for (int i = 0; i < expectChildren.getLength(); i++)
         {
            kid = expectChildren.item(i);

            if (!(kid instanceof Element))
               continue;

            String nodeName = kid.getNodeName();

            if (xmlRoot != null && nodeName.equals("XmlRoot"))
            {
               xmlRoot = (Element)kid;
            }
            else if (xmlDtd != null && nodeName.equals("XmlDtd"))
            {
               xmlDtd = (Element)kid;
            }
            else if (nodeName.equals("XmlNode"))
            {
               expectedNodes.add(getAttribute(  (Element)kid, "name"));
               expectedValues.add(getAttribute( (Element)kid, "value"));
               String regex = getAttribute( (Element)kid, "regex");
               if (regex != null && regex.equalsIgnoreCase("yes"))
                  isValueRegex.add(Boolean.TRUE);
               else
                  isValueRegex.add(Boolean.FALSE);
            }
         }
      }// // // // //


      if (xmlRoot != null)
      {
         String expectedRoot = getAttribute(xmlRoot, "name");
         if (!expectedRoot.equals(root))
         {
            throw new ScriptTestFailedException("Expected root node <" + expectedRoot
               + ">, actually got <" + root + ">");
         }
      }

      PSXmlTreeWalker walker = new PSXmlTreeWalker(inDoc);
      final int flags = PSXmlTreeWalker.GET_NEXT_RESET_CURRENT
         | PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN
         | PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS
         | PSXmlTreeWalker.GET_NEXT_ALLOW_PARENTS;

      for (int i = 0; i < expectedNodes.size(); i++)
      {
         String expectedNode = expectedNodes.get(i).toString();
         log("Searching for element " + expectedNode);
         Element el = walker.getNextElement(expectedNode, flags);

         if (el == null)
         {
            throw new ScriptTestFailedException(
               "Could not find element " + expectedNode);
         }

         String expectedValue = (String)expectedValues.get(i);
         if (expectedValue != null)
         {
            boolean isRegex = (Boolean.TRUE.equals(isValueRegex.get(i)));
            String actualValue = walker.getElementData(el);
            if (isRegex)
            {
               PSPatternMatcher pat = PSPatternMatcher.FileWildcardMatcher(expectedValue);
               if (!pat.doesMatchPattern(actualValue))
               {
                  throw new ScriptTestFailedException(
                     "Element <" + expectedNode + ">'s value \"" + actualValue
                     + "\" does not match pattern \"" + expectedValue + "\"");
               }
            }
            else
            {
               if (!expectedValue.equals(actualValue))
               {
                  throw new ScriptTestFailedException(
                     "Element <" + expectedNode + ">'s value \"" + actualValue
                     + "\" does not equals \""  + expectedValue + "\"");
               }
            }
         }
      }

   }

   /**
    * Records that the provided test errored, and logs the provided message.
    *
    * @param testID the test that has errored.
    * @param message the text of the error result, not <code>null</code>.
    *
    * @throws IllegalArgumentException if message is <code>null</code>.
    * @throws ScriptExecAbortedException always.
    */
   protected void errorTest(int testID, String message)
      throws ScriptExecAbortedException
   {
      if (message == null)
         throw new IllegalArgumentException("message may not be null");

      log(message, true);
      m_results.errorTest(testID, message);
      throw new ScriptExecAbortedException("Abort on error: " + message);
   }

   /**
    * Records that the provided test failed, and logs the provided message.
    *
    * @param testID the test that has errored.
    * @param message the text of the error result, not <code>null</code>.
    * @param stats performance statistics of the test, may be <code>null</code>.
    *
    * @throws IllegalArgumentException if message is <code>null</code>.
    * @throws ScriptExecAbortedException always.
    */
   protected void failTest(int testID, String message, QAPerformanceStats stats)
      throws ScriptExecAbortedException
   {
      if (message == null)
         throw new IllegalArgumentException("message may not be null");

      log(message, true);
      m_results.failTest(testID, message, stats);
      throw new ScriptExecAbortedException("Abort on fail: " + message);
   }

   /**
    * Handles the GET request for the provided element.
    *
    * @param getRequestElem the element with the GET request, assumed not
    *    <code>null</code>.
    * @throws Exception if anything goes wrong.
    */
   protected void handleGetRequest(Element getRequestElem) throws Exception
   {
      handleGetOrPostRequest(getRequestElem);
      if (!m_logToScreen)
      {
         synchronized (this)
         {
            m_statusCharIndex++;
            if (m_statusCharIndex >= ms_statusChars.length)
               m_statusCharIndex = 0;

            System.out.print("\b" + ms_statusChars[m_statusCharIndex]);
         }
      }
   }

   /**
    * The character index identifying the next character to be displayed for
    * the spinning bar while logging to screen is turned off.
    */
   private int m_statusCharIndex = 0;

   /**
    * The characters used to display the spinning bar if logging to screen is
    * turned off.
    */
   private static final String[] ms_statusChars =
      { "|", "/", "-", "\\", "|", "/", "-", "\\" };

   /**
    * Handles the POST request for the provided element.
    *
    * @param postRequestElem the element with the POST request, assumed not
    *    <code>null</code>.
    * @throws Exception if anything goes wrong.
    */
   protected void handlePostRequest(Element postRequestElem) throws Exception
   {
      handleGetOrPostRequest(postRequestElem);
      if (!m_logToScreen)
      {
         synchronized (this)
         {
            m_statusCharIndex++;
            if (m_statusCharIndex >= ms_statusChars.length)
               m_statusCharIndex = 0;

            System.out.print("\b" + ms_statusChars[m_statusCharIndex]);
         }
      }
   }

   protected void handleMacro(Element el) throws Exception
   {
      String name = getAttribute(el, "name");
      String value = getAttribute(el, "value");
      defineMacro(name, value);
   }

   protected void handleRunScript(Element runScript) throws Exception
   {
      log("RunScript");
      String scriptHref = getAttribute(runScript, REQ_URL_REF_ATTR);
      if (m_subDocExecStack.get(scriptHref.toUpperCase()) != null)
         throw new ScriptTestErrorException("error: cannot recursively call " + scriptHref);
      String cache = getAttribute(runScript, "cache");
      boolean shouldCache = true;
      if (cache != null && !cache.equals("no"))
         shouldCache = false;
      Document doc = loadScript(scriptHref, shouldCache);
      handleTestScript(doc.getDocumentElement(), scriptHref);
   }

   protected void handleSyncObject(Element syncObjectElem)
   {
      log("SyncObject");
      String name = getAttribute(syncObjectElem, "name");
      String typeString = getAttribute(syncObjectElem, "type");
      String scopeString = getAttribute(syncObjectElem, "scope");
      QAObjectType type;
      if (typeString.equals("event"))
         type = QAObjectType.EVENT;
      else
         type = QAObjectType.MUTEX;

      int scope = -1;
      if (scopeString.equals("global"))
         scope = QAObjectScope.GLOBAL;
      else if (scopeString.equals("script"))
         scope = QAObjectScope.SCRIPT;
      else
         scope = QAObjectScope.INSTANCE;

      m_client.registerServerObject(new QAObjectDescription(name, type, scope));
   }

   protected void handleSynchronize(Element syncElem) throws Exception
   {
      String name = getAttribute(syncElem, "name");
      String waitFor = getAttribute(syncElem, "waitFor");
      String expiresIn = getAttribute(syncElem, "expiresIn");

      if (waitFor == null || waitFor.length() == 0)
         waitFor = "600000"; // wait for 10 minute

      if (expiresIn == null || expiresIn.length() == 0)
         expiresIn = "600000"; // 10 minutes

      log("Synchronize on " + name);

      if (!m_client.lockServerObject(name, Long.parseLong(expiresIn), Long.parseLong(waitFor)))
      {
         log("Could not obtain lock on " + name);
         throw new ScriptTimeoutException("Timed out while waiting for lock on " + name);
      }

      try
      {
         NodeList children = syncElem.getChildNodes();
         for (int i = 0; i < children.getLength(); i++)
         {
            Node kid = children.item(i);
            if (kid instanceof Element)
               handle((Element)kid);
         }
      }
      finally
      {
         // unlock
         log("Unlocking " + name);
         if (!m_client.lockServerObject(name, 0, 0))
         {
            log("Could not release lock of " + name);
         }
      }

   }

   protected void handleTestScript(Element testScriptElem, String scriptHref)
      throws RemoteException, MalformedURLException, IOException
   {
      log("");
      log("");
      log(scriptHref + ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
      log("");
      log("");
      m_subDocExecStack.put(scriptHref.toUpperCase(), new Object());
      try
      {
         // process the attributes
         String autoLogin = getAttribute(testScriptElem, "autoLogin");
         initAutoLogin(autoLogin.equalsIgnoreCase("yes"));

         NodeList allSubScripts = testScriptElem.getElementsByTagName("RunScript");
         for (int i = 0; i < allSubScripts.getLength(); i++)
         {
            Element script = (Element) allSubScripts.item(i);
            String href = getAttribute(script, REQ_URL_REF_ATTR);
            String cache = getAttribute(script, "cache");
            boolean shouldCache = true;
            if (cache != null && !cache.equals("no"))
               shouldCache = false;
            loadScript(href, shouldCache);
         }

         NodeList children = testScriptElem.getChildNodes();
         for (int i = 0; i < children.getLength(); i++)
         {
            Node kid = children.item(i);
            if (kid instanceof Element)
               handle((Element)kid);
         }

      }
      catch (ScriptExecAbortedException e)
      {
         log("Aborted script execution because of fatal error: " + e.getMessage());
      }
      catch(Throwable t)
      {
         log(t);
      }
      finally
      {
         log("");
         log("");
         log(scriptHref + "<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
         log("");
         log("");
         m_subDocExecStack.remove(scriptHref.toUpperCase());
      }
   }

   protected Document loadScript(String scriptHref, boolean keepCopy) throws Exception
   {
      Document doc = null;
      if (keepCopy)
      {
         m_subDocs.get(scriptHref.toUpperCase());
         if (doc != null)
            return doc;
      }
      URL scriptURL = new URL(scriptHref);
      URLConnection scriptConn = scriptURL.openConnection();
      Object content = scriptConn.getContent();
      BufferedInputStream in = new BufferedInputStream((InputStream)content);
      doc = PSXmlDocumentBuilder.createXmlDocument(in, false);
      in.close();
      if (keepCopy)
         m_subDocs.put(scriptHref.toUpperCase(), doc);
      return doc;
   }

   /**
    * Logs the provided message. If its an error message, a screen logging is
    * done no matter what the logToScreen property says.
    *
    * @param message the message to log, assumed not <code>null</code>.
    * @param isError <code>true</code> if its an error to log,
    *    <code>false</code> otherwise.
    */
   public void log(String message, boolean isError)
   {
      try
      {
         StringTokenizer lineTok = new StringTokenizer(message, "\r\n");
         StringBuffer msgBuf = new StringBuffer(message.length());
         /* Add the thread id because PageExecBlocks run multiple threads
            simultaneously. */
         String id = Thread.currentThread().getName();
         msgBuf.append("[" + id + "]: ");
         if (lineTok.hasMoreTokens())
         {
            for (int i = 1; i < m_subDocExecStack.size(); i++)
               msgBuf.append('\t');

            msgBuf.append(lineTok.nextToken());
         }
         for (; lineTok.hasMoreTokens(); msgBuf.append(lineTok.nextToken()))
         {
            for (int i=1; i<m_subDocExecStack.size(); i++)
               msgBuf.append("\r\n\t");

         }

         String finalMsg = msgBuf.toString();
         m_results.log(message);
         if (m_logToScreen || isError)
            System.out.println(finalMsg);
      }
      catch (Throwable t)
      {
         System.out.println("Error while logging message.");
         t.printStackTrace();
      }
   }

   public void log(String message)
   {
      log(message, false);
   }

   public String getValue(Element node, boolean doMacros)
   {
      StringBuffer ret = new StringBuffer();
      Node text;

      for (text = node.getFirstChild();
         text != null;
         text = text.getNextSibling() )
      {
         /* the item's value is in one or more text nodes which are
          * its immediate children
          */
         if (text instanceof org.w3c.dom.Text)
            ret.append(text.getNodeValue());
         else
            break;
      }

      if (doMacros)
         return replaceMacros(ret.toString(), false);
      else
         return ret.toString();
   }

   public void log(Throwable t)
   {
      t.printStackTrace( System.out );
      /* This form doesn't handle newlines correctly. Restore when have time
         to look at it
      StringWriter w = new StringWriter();
      PrintWriter p = new PrintWriter(w);
      t.printStackTrace(p);
      log(w.toString());
      */
   }

   public void log(String s, Throwable t)
   {
      if (s != null)
         log(s);
      log(t);
   }
   protected String replaceMacros(String source)
   {
      return replaceMacros(source, true);
   }

   protected String replaceMacros(String source, boolean log)
   {
      if (source == null)
         return null;

      StringBuffer buf = new StringBuffer(source);
      for (int pos = 0; pos < buf.length(); pos++)
      {
         char c = buf.charAt(pos);
         if (c == '$')
         {
            int startVar = pos;
            pos++;
            c = buf.charAt(pos);
            if (c == '(')
            {
               int endVar;
               for (endVar = pos + 1; endVar < buf.length(); endVar++)
               {
                  c = buf.charAt(endVar);
                  if (c == ')')
                     break;
               }

               if (c == ')')
               {
                  String varName = buf.substring(startVar + 2, endVar);
                  String varValue = (String)m_vars.get(varName);
                  if (varValue == null)
                     varValue = "";
                  buf.replace(startVar, endVar + 1, varValue);
                  pos = startVar - 1;
               }
            }
         }
      }

      String retVal = buf.toString();
      if (log && !retVal.equals(source))
         log("Transformed source string \"" + source + "\" to \"" + retVal + "\"");
      return retVal;
   }

   public void run()
   {
      log("Starting script");
      log("OS: " + System.getProperty("os.name") + " " +
         System.getProperty("os.version") + " " + System.getProperty("os.arch"));
      log("Java: " + System.getProperty("java.version") + " " +
         System.getProperty("java.vendor"));

      try
      {
         m_client.publishScriptStarted(m_qadoc.getName());
         handleTestScript(m_qadoc.getDocument().getDocumentElement(),
            m_qadoc.getName());
         QAPerformanceStats stats = m_results.getCpuUsage();
         endStatsCollection(stats);
         m_client.publishScriptCompleted(m_qadoc.getName());
         if (null != m_statsCollector)
            m_statsCollector.terminate();

         log("Finishing script");
         m_client.postResults(m_results);
      }
      catch(Throwable t)
      {
         log(t);
      }
      System.gc();
   }


   /**
    * Searches the first child Element of the supplied element for a
    * ResponseExitSet tag. If it is one, all of the ResponseExit children of
    * that element are processed, loading the class for each exit.
    *
    * @param The element to check. Assumed not <code>null</code>.
    *
    * @return A map where each entry has the exit as the key and its
    *    properties object as the value. Never <code>null</code>.
    *
    * @throws ScriptTestErrorException If the ResponseExit element doesn't
    *    match the dtd.
    *
    * @throws ClassNotFoundException If the specified exit could not be found
    *    by the class loader.
    *
    * @throws InstantiationException If an instance of the specified exit
    *    could not be created.
    */
   private Map prepareResponseExits( Element requestElem)
      throws Exception
   {
      Element exitSet = null;
      NodeList children = requestElem.getChildNodes();
      for (int i = 0; i < children.getLength(); i++)
      {
         Node kid = children.item(i);
         if ((kid instanceof Element))
         {
            if ( kid.getNodeName().equals( RESPONSE_EXIT_SET_TAG ))
               exitSet = (Element) kid;
            break;
         }
      }

      Map exits = new HashMap();
      if ( null != exitSet )
      {
         children = exitSet.getChildNodes();
         for (int i = 0; i < children.getLength(); i++)
         {
            Properties params = new Properties();
            Element exit = null;
            Node kid = children.item(i);
            if ( !( kid instanceof Element
                  && kid.getNodeName().equals( "ResponseExit" )))
            {
               continue;
            }

            exit = (Element) kid;
            String className = getAttribute( exit, "className" );
            if ( className.trim().length() == 0 )
            {
               throw new ScriptTestErrorException( "The className attribute"
                     + " of the ResponseExit element cannot be empty." );
            }

            Class exitClass = Class.forName( className );
            NodeList paramChildren = exit.getChildNodes();
            for (int j = 0; j < paramChildren.getLength(); j++)
            {
               Node kid2 = paramChildren.item(j);
               if ( kid2 instanceof Element
                     && kid2.getNodeName().equals( "Param" ))
               {
                  Element param = (Element) kid2;
                  String name = getAttribute( param, "name" );
                  if ( name.trim().length() == 0 )
                  {
                     throw new ScriptTestErrorException( "The name attribute"
                           + " of the Param element cannot be empty." );
                  }
                  String value =  getValue( param, true );
                  params.setProperty( name, value );
               }
            }
            exits.put( exitClass.newInstance(), params );
         }
      }
      return exits;
   }


   /**
    * This method will initialize the auto login flag and credential. If it
    * is called more than once, it has no effect on successive calls.
    * <p>If auto login is enabled, the interpreter will automatically login
    * if a HTTP code of 401 is returned and it is not the expected code.
    * See {@link #getLoginCredential(Element) getLoginCredential} for more
    * details.
    *
    * @param isEnabled Supply <code>true</code> if you want the client to
    *    automatically login when an un-authorized code is returned.
    */
   private void initAutoLogin(boolean isEnabled)
   {
      if (null != m_autoLogin)
         return;

      m_autoLogin = isEnabled ? "a" : "";
      if (!isEnabled)
      {
         m_autoLoginCredential = null;
         return;
      }

      String cred = m_client.getLoginCredential();
      if (null != cred && cred.trim().length() > 0)
         m_autoLoginCredential = cred;
      else
         m_autoLoginCredential = null;
   }

   /**
    * See {#initAutoLogin(boolean) initAutoLogin}} for a description.
    *
    * @return <code>true</code> if auto login is enabled, <code>false</code>
    *    otherwise.
    */
   private boolean isAutoLoginEnabled()
   {
      return null != m_autoLogin && m_autoLogin.length() > 0;
   }

   /**
    * If auto login is enabled, this method can be used to obtain the
    * credential, already prepared for submission via the Authorization header
    * of the http request. First checks for a child element of req called
    * HttpHeader, whose name is 'Authorization'. If one is found, its value is
    * returned. If not, the credentials obtained from the qa client are
    * returned.
    *
    * @return  If auto login is enabled, a valid, non-empty string that
    *    contains the cred in the form "Basic 'uid:pw,base64 encoded'". If
    *    auto login is not enabled, <code>null</code> is returned.
    *
    * @throws ScriptTestErrorException If no cred was specified
    *    in the element or client properties.
    */
   private String getLoginCredential(Element req)
      throws ScriptTestErrorException
   {
      if (!isAutoLoginEnabled())
         return null;

      HttpHeaders hdrs = getChildHeaders(req);
      String cred = hdrs.getHeader("Authorization");
      if (null == cred || cred.length() == 0)
      {
         if (null == m_autoLoginCredential)
         {
            throw new ScriptTestErrorException(
               "autoLogin specified, but no credentials supplied (either" +
               " an Authorization HttpHeader in the script or autoLoginUid" +
               " client property required)");
         }
         cred = "Basic " + m_autoLoginCredential;
      }

      return cred;
   }


   /**
    * Adds the supplied stats to the statistics collection thread, if there
    * is one.
    *
    * @param stats The object to recieve the samples. Assumed not <code>null
    *    </code>.
    */
   private void beginStatsCollection( QAPerformanceStats stats )
   {
      if ( null != m_statsCollector )
      {
         m_statsCollector.beginSession( stats );
      }
   }


   /**
    * Terminates a monitor in the statistics collection thread previously
    * set with {@link #beginStatsCollection}. If the id doesn't identify a
    * current session, nothing is done and no error is thrown.
    *
    * @param stats The object originally passed to the {@link
    *    #beginStatsCollection} method. If <code>null</code>, nothing is done.
    */
   private void endStatsCollection( QAPerformanceStats stats )
   {
      if ( null == stats || null == m_statsCollector )
         return;
      m_statsCollector.endSession( stats );
   }


   /**
    * Saves the actual result as a file under /at/TestResults/appName directory
    * this is helpful in cases where the actual results are correct, however the
    * expected data is outdated and so it needs to be updated in bulk.
    * @param actualResult - server retuned result, may be <code>null</code>
    * @param expectHref - href of the file that contains expected test result,
    * may be <code>null</code>.
   */
   private void saveActualResult(String actualResult, String expectHref)
   {
      if (actualResult==null || expectHref==null)
         return;

      FileOutputStream fos = null;

      try
      {
         File fCur = new File("TestResults");
         String dirPath = fCur.getAbsolutePath();

         System.out.println("dirPath: " + dirPath);

         //this variable is here to keep the rest of the code the same as Rx45.
         String expUrl = expectHref;

         System.out.println("expUrl: " + expUrl);

         /*
            expected result Url is expected in this format:
            file:TestData//ActionSets/unauthorized-user.xml
         */

         int testDataInd = expUrl.indexOf("TestData//");

         if (testDataInd < 0)
            return;

         //skip 'file:TestData//' to get 'testName/fileName' substring
         String fileSubName = expUrl.substring(testDataInd + "TestData//".length());

         //construct a full file name of a file where the actual test result will go
         String fullFilePath = dirPath + "/" + fileSubName;

         System.out.println("actualResultFilePath: " + fullFilePath);


         File rFile = new File(fullFilePath);


         //create all needed directories
         rFile.getParentFile().mkdirs();

         System.out.println("saving actual test result to: " + fullFilePath);

         //save result file
         fos = new FileOutputStream(rFile);

         fos.write(actualResult.trim().getBytes());

         fos.flush();
      }
      catch(Throwable t)
      {
         //don't want any exceptions from here
         System.out.println("exception msg: " + t.getMessage());
         t.printStackTrace();
      }
      finally
      {
         if (fos!=null)
         {
            try {
               fos.close();
            }
            catch(Throwable t1){}
         }
      }
   }


   /**
    * This class tracks 0 or more performance monitors associated with
    * requests, collecting samples and setting them on a performance
    * statistics object. It is used to implement the <code>COMPUTER_STATS_TAG
    * </code> script tag.
    */
   private class StatsCollector extends Thread
   {
      /**
       * Contains the main run loop of the thread. Sits in a loop until the
       * {@link #stop} method is called. The stop flag is checked about every
       * 100 milliseconds or faster.
       */
      public void run()
      {
         try
         {
            // max milliseconds to wait before checking for stop flag
            final int DEFAULT_INTERVAL = 100;
            int sleepInterval = m_interval < DEFAULT_INTERVAL
                  ? m_interval : DEFAULT_INTERVAL;
            int waitedSoFar = m_interval; // take a sample immediately
            while ( !m_stop )
            {
               if ( waitedSoFar >= m_interval )
               {
                  waitedSoFar = 0;
                  synchronized (this)
                  {
                     try
                     {
                        if ( m_sessions.size() > 0 )
                        {
                           Iterator iter = m_sessions.keySet().iterator();
                           m_monitor.collectSample();
                           int cpuPercent = (int) m_monitor.getFormattedData(
                                 m_counterPath );
                           while ( iter.hasNext())
                           {
                              QAPerformanceStats stats =
                                    (QAPerformanceStats) iter.next();
                              stats.addCpuTick( cpuPercent );
                           }
                        }
                     }
                     catch ( PSPerformanceMonitorException e )
                     {
                        System.out.println( e.getLocalizedMessage());
                     }
                  }
               }
               sleep( sleepInterval );
               waitedSoFar += sleepInterval;
            }
         }
         catch ( InterruptedException e )
         { /* just end */ }
         finally
         {
            try
            {
               m_monitor.shutdown();
            }
            catch (PSPerformanceMonitorException e)
            {
               System.out.println("Exception while shutting down monitor: "
                     + e.getLocalizedMessage());
            }
         }
      }

      /**
       * Provides a safe way to stop the thread. Sets a flag that is checked
       * frequently by the main run loop.
       */
      public void terminate()
      {
         m_stop = true;
      }

      /**
       * Create the performance monitor thread. Initializes the underlying
       * OS so it is ready to go. The priority of this thread is set to the
       * max. If you want to override this behavior, set the priority
       * directly.
       *
       * @param collectionInterval The sampling interval, in milliseconds.
       *
       * @param counterPath The name of the counter you wish to sample.
       *    Currently, only the % cpu usage, _Total will work correctly.
       *    Assumed not <code>null</code>.
       */
      public StatsCollector( int collectionInterval, String counterPath )
         throws PSPerformanceMonitorException
      {
         m_monitor = new PSNtPerformanceMonitor();
         m_counterPath = counterPath;
         m_monitor.addCounter( counterPath );
         /* The first sample collected is always 99, so we do the first
            sample before real collection begins to eliminate this anomalous
            data. */
         m_monitor.collectSample();
         m_interval = collectionInterval;
         setDaemon( true );
         setName( "PerformanceMonitor" );
         setPriority(MAX_PRIORITY);
      }

      /**
       * Adds the supplied stats to the list of stats. Each time a sample is
       * collected, it is added to the stats object, until {@link
       * #endSession} is called.
       *
       * <p>Note, the param should be an interface, but time does not allow
       * for a more generic solution.
       *
       * @param stats The object that will recieve the samples.
       */
      synchronized public void beginSession( QAPerformanceStats stats )
      {
         m_sessions.put( stats, null );
      }

      /**
       * Terminates collection on the stats object associated with the id. If
       * there is no session by this id, nothing is done and no error is
       * indicated.
       *
       * @param stats The object that was previously passed too the {@link
       *    #beginSession} method.
       */
      synchronized public void endSession( QAPerformanceStats stats )
      {
         m_sessions.remove( stats );
      }

      /**
       * A latch used to communicate to the main loop that a caller has
       * requested it to stop running. This flag is checked every 100
       * millis or so.
       */
      private boolean m_stop = false;

      /**
       * Contains a map of QAPerformanceStats that have been added for counter
       * stats. The key is the object, the value is <code>null</code>. Never
       * <code>null</code>.
       */
      private Map m_sessions = new HashMap();

      /**
       * What is the sampling interval, in milliseconds. Set in ctor, then
       * never changed.
       */
      private int m_interval;

      /**
       * This object is the interface to the OS statistics gathering
       * mechanism. Never <code>null</code> after construction.
       */
      private IPSPerformanceMonitor m_monitor;
      /**
       * This is the path to the counter that is being sampled. We need to
       * keep it around to get the data back for each sample. Initialized
       * in ctor, then never changed. After init, never <code>null</code> or
       * empty.
       */
      private String m_counterPath;
   }

   /**
    * A 3 valued flag to indicate whether to automatically login when an
    * un-authorized code is returned by the server. If enabled, then <code>
    * m_autoLoginCredential</code> may contain credentials obtained from the
    * client. See {@link #initAutoLogin(boolean) initAutoLogin} for more
    * details. It is <code>null</code> until initialized once. After init,
    * it is a valid string. If it is non-empty, auto login is enabled,
    * otherwise it is disabled.
    * <p>This member should only be accessed via methods shown in the see
    * tags, never directly.
    *
    * @see #initAutoLogin(boolean)
    * @see #isAutoLoginEnabled()
    */
   private String m_autoLogin = null;

   /**
    * This variable contains the auto login credentials obtained from the
    * qa client. Always <code>null</code> if <code>m_autoLogin</code> is
    * <code>false</code>. If <code>m_autoLogin</code> is <code>true</code>,
    * this value is either the credentials in the form 'uid:pw,base64 encoded'
    * or <code>null</code> if no creds were obtained from the client.
    * <p>This member should only be accessed via local methods in the see tags,
    * never directly.
    *
    * @see #initAutoLogin(boolean)
    * @see #getLoginCredential
    * @see EmpireTestClient#getLoginCredential()
    */
   private String m_autoLoginCredential = null;

   /**
    * This flag indicates whether cookies are saved and returned to the
    * server. If <code>true</code>, they are. Defaults to <code>false</code>.
    * <p>Don't access directly, use methods.
    */
   private boolean m_useCookies = false;
   
   /**
    * Flag indicates if the current cookies should be cleared before the next
    * request is sent.  Set to <code>false</code> after it is processed just
    * before sending a request. 
    */
   private boolean m_clearCookies = false;

   /**
    * If computerstats has been specified for this test, then this thread
    * will exist. To use it, set the current QAPerformanceStats object in
    * the thread at the beginning of the request and clear it at the end.
    * It can handle multiple requests simultaneously. Use the methods
    * {@link #beginStatsCollection} and {@link #endStatsCollection} rather
    * than accessing it directly.
    */
   private StatsCollector m_statsCollector;

   protected Map m_subDocs;
   protected Map m_subDocExecStack;
   protected QAScriptDocument m_qadoc;
   protected EmpireTestClient m_client;



   protected QATestResults m_results;
   protected boolean m_logToScreen;
   protected Map m_vars;


   // These are the element and attribute names for script tags
   private static final String PAGE_BLOCK_TAG = "PageExecBlock";
   private static final String EXEC_BLOCK_TAG = "ExecBlock";
   private static final String USE_COOKIES_TAG = "UseCookie";
   private static final String COMPUTER_STATS_TAG = "ComputerStats";
   private static final String COUNTER_DEF_TAG = "CounterDef";
   private static final String RESPONSE_EXIT_SET_TAG = "ResponseExitSet";
   private static final String STOPONERR_ATTRIB_NAME = "stopOnError";
   private static final String RANDOM_ATTRIB_NAME = "random";
   private static final String POST_REQUEST_TAG = "PostRequest";
   private static final String GET_REQUEST_TAG = "GetRequest";
   static final String POST_CONTENT_HDRS_ATTR = "PostContentHeaders";
   static final String POST_CONTENT_REF_ATTR = "postContentHref";
   static final String REQ_URL_REF_ATTR = "href";
}
