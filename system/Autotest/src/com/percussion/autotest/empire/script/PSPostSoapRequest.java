/******************************************************************************
 *
 * [ PSPostSoapRequest.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
 
package com.percussion.autotest.empire.script;

import com.percussion.HTTPClient.ModuleException;
import com.percussion.test.http.HttpHeaders;
import com.percussion.test.soap.PSSoapRequest;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Iterator;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;

import org.apache.soap.Body;
import org.apache.soap.Constants;
import org.apache.soap.Envelope;
import org.apache.soap.Header;
import org.apache.soap.util.xml.XMLParserUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * The helper class for script interpreter to handle soap requests processing.
 */
class PSPostSoapRequest extends PSSoapRequest
{
   /**
    * Constructs soap request object from the supplied request element. The 
    * expected format of the request element is 
    * <pre><code>
    * 
    * &lt;!ELEMENT PostSOAPRequest (SOAPHeader*, SOAPBody)>
    * &lt;!ATTLIST PostSOAPRequest 
    *    href CDATA #REQUIRED >
    * &lt;-- anyHeaderElement is an element that represents soap header entry 
    * -->
    * &lt;!ELEMENT SOAPHeader(anyHeaderElement*)>
    * &lt;-- anyBodyRequestElement is an element that represents request element 
    * required for the specified 'action'. May not be required if the 
    * 'postContentHref' is specified in which case it gets data from the 
    * specified url.
    * -->
    * &lt;!ELEMENT SOAPBody(anyBodyRequestElement?, PostContentHeaders?)>
    * &lt;!ATTLIST SOAPBody
    *    action CDATA #REQUIRED
    *    service CDATA #REQUIRED
    *    postContentHref CDATA #IMPLIED >
    *    
    * </code></pre>
    * 
    * @param soapRequestElem the soap request element, may not be <code>null
    * </code> and must be <PostSOAPRequest> element.
    * @param scriptInterpreter the interpreter to use to process the request
    * element, may not be <code>null</code>
    * 
    * @throws IllegalArgumentException if any parameter is invalid.
    * @throws ScriptTestErrorException if the required attributes or child 
    * elements are missing or invalid.
    * @throws ScriptInterruptedException if the extracting content for request 
    * from an url is interrupted.
    * @throws ScriptTestFailedException if the extracting content for request 
    * from an url has failed.
    * @throws ScriptTimeoutException if the extracting content for request 
    * from an url is timed out.
    * @throws SAXException if the extracting content element from stream fails.
    * @throws IOException if any io error happens.
    */
   public PSPostSoapRequest(Element soapRequestElem, 
      EmpireScriptInterpreter scriptInterpreter) 
      throws ScriptTestErrorException, ScriptInterruptedException, 
      ScriptTestFailedException, ScriptTimeoutException, IOException, 
      SAXException, ModuleException
   {
      if(soapRequestElem == null)
         throw new IllegalArgumentException("soapRequestElem may not be null.");
         
      if(!soapRequestElem.getNodeName().equals(XML_NODE_NAME))
         throw new IllegalArgumentException("Expecting <" + XML_NODE_NAME + 
         "> element, got " + soapRequestElem.getNodeName());
         
      if(scriptInterpreter == null)
         throw new IllegalArgumentException(
            "scriptInterpreter may not be null.");
            
      m_interpreter = scriptInterpreter;
         
      String reqUrl = m_interpreter.getAttribute(soapRequestElem, 
         EmpireScriptInterpreter.REQ_URL_REF_ATTR);
         
      if(reqUrl == null || reqUrl.trim().length() == 0)
         throw new ScriptTestErrorException(
            "href is required attribute for soap request");
         
      Envelope env = createSoapEnvelope(soapRequestElem);
      
      setRequestContent(reqUrl, env);
   }
   
   /**
    * Creates soap envelope from the supplied soap request script element.
    * 
    * @param soapRequestElem the soap request element, assumes that it is a 
    * valid element.
    * 
    * @return the envelope, never <code>null</code>
    * 
    * @throws ScriptTestErrorException if the required attributes or child 
    * elements are missing or invalid.
    * @throws ScriptInterruptedException if the extracting content for request 
    * from a url is interrupted.
    * @throws ScriptTestFailedException if the extracting content for request 
    * from a url has failed.
    * @throws ScriptTimeoutException if the extracting content for request 
    * from a url is timed out.
    * @throws SAXException if the extracting content element from stream fails.
    * @throws IOException if any io error happens.
    */
   private Envelope createSoapEnvelope(Element soapRequestElem) 
      throws ScriptTestErrorException, ScriptInterruptedException, 
      ScriptTestFailedException, ScriptTimeoutException, IOException, 
      SAXException, ModuleException
   {
      //First replace any macros used
      replaceMacrosForSoapElements(soapRequestElem);
           
      int firstFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN |
         PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;
         
      PSXmlTreeWalker tree = new PSXmlTreeWalker(soapRequestElem);      
      Node current = tree.getCurrent();
      
      Envelope env = new Envelope();      
      Header header = new Header();
      Body body = new Body();
      env.setHeader(header);
      env.setBody(body);
      
      Vector headerEntries = new Vector();
      header.setHeaderEntries(headerEntries);
      Element headerEl = tree.getNextElement(XML_HEADER_NODE, firstFlags);
      if (headerEl != null)
      {
        if(headerEl.hasChildNodes())
         {         
            NodeList childNodes = headerEl.getChildNodes();
            for(int i=0; i<childNodes.getLength(); i++)
            {
               Node childEl = childNodes.item(i);            
               if(childEl instanceof Element)
                  headerEntries.add(childEl);
            }
         }
      }
      tree.setCurrent(current);
      
      Element bodyEl = tree.getNextElement(XML_BODY_NODE, firstFlags);      
      if(bodyEl == null)
         throw new ScriptTestErrorException("Must have <" + 
            XML_BODY_NODE + "> element");
      
      String action = bodyEl.getAttribute(XML_ACTION_ATTR);
      String service = 
         URLDecoder.decode(bodyEl.getAttribute(XML_SERVICE_ATTR));
      String postContentHref = 
         bodyEl.getAttribute(m_interpreter.POST_CONTENT_REF_ATTR);
         
      if(action == null || action.trim().length() == 0)
         throw new ScriptTestErrorException(
            "Missing Required attribute <" + 
            XML_ACTION_ATTR + "> for Soap Body");
            
      if(service == null || service.trim().length() == 0)
         throw new ScriptTestErrorException(
            "Missing Required attribute <" + 
            XML_SERVICE_ATTR + "> for Soap Body");
            
      Vector bodyEntries = new Vector();
      body.setBodyEntries(bodyEntries);
      
      Document doc = PSXmlDocumentBuilder.createXmlDocument();      
      Element actionElement = doc.createElement(action);
      actionElement.setAttribute(Constants.NS_PRE_XMLNS, service);
         
      NamedNodeMap nodeMap = bodyEl.getAttributes();
      for(int i=0; i<nodeMap.getLength(); i++)
      {
         Node attrib = nodeMap.item(i);
         String attribName = attrib.getNodeName();
         if( !(attribName.equals(XML_ACTION_ATTR) || 
            attribName.equals(XML_SERVICE_ATTR) ||
            attribName.equals(m_interpreter.POST_CONTENT_REF_ATTR)) ) 
         {
            actionElement.setAttribute(attribName, attrib.getNodeValue());
         }
      }
      bodyEntries.add(actionElement);      
      
      Element contentEl = null;
      if(postContentHref != null && postContentHref.trim().length() != 0)
      {
         try 
         {
            URL contentUrl = new URL(postContentHref);
         }
         catch(MalformedURLException e)
         {
            throw new ScriptTestErrorException(
               "Invalid URL format for " + m_interpreter.POST_CONTENT_REF_ATTR);
         }
         
         NodeList elements = bodyEl.getElementsByTagName(
            m_interpreter.POST_CONTENT_HDRS_ATTR);
      
         HttpHeaders headers = new HttpHeaders();   
         for(int i=0; i<elements.getLength(); i++)
         {
            headers.addAll(
               m_interpreter.getChildHeaders((Element)elements.item(i)));
         }
         InputStream postContentIn = 
            m_interpreter.getContentStream(postContentHref, 0L, headers);
         DocumentBuilder xdb = XMLParserUtils.getXMLDocBuilder();
         contentEl =  xdb.parse(postContentIn).getDocumentElement();            
      }
      else
      {
         PSXmlTreeWalker treeEl = new PSXmlTreeWalker(bodyEl);
         contentEl = treeEl.getNextElement(
            PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);
      }
      
      if(contentEl != null)
         bodyEntries.add(contentEl);

      return env;
   } 
   
   /**
    * Replaces any macros used in attributes or element text nodes of 
    * <SOAPHeader> and <SOAPBody> elements and its children of the the supplied 
    * request element with actual values. 
    * 
    * @param soapRequestElem the parent element of the above specified elements,
    * assumed not <code>null</code>
    */
   private void replaceMacrosForSoapElements(Element soapRequestElem)
   {  
      int firstFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN |
         PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;
      int nextFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS |
         PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;
         
      PSXmlTreeWalker tree = new PSXmlTreeWalker(soapRequestElem);      
      Node current = tree.getCurrent();

      Element header = tree.getNextElement(XML_HEADER_NODE, firstFlags);
      while (header != null)
      {
         m_interpreter.replaceMacros(header);

         header = tree.getNextElement(XML_HEADER_NODE, nextFlags);
      }
      tree.setCurrent(current);
      
      Element body = tree.getNextElement(XML_BODY_NODE, firstFlags);
      m_interpreter.replaceMacros(body); 
   }
   
   /**
    * Gets the status of response, will be one of the <code>STATUS_xxx</code>
    * values. This status is valid only for Rx web services api.
    *
    * @return the status code, may be <code>null</code> if the request has 
    * generated a soap fault, never empty.
    * 
    * @throws IllegalStateException if the request is not yet sent.
    */
   public String getResponseStatus()
   {
      checkRequestSent();
      
      if(m_respStatus == null && !generatedFault())
      {  
         Element resultResp = null;                
         Iterator bodyEntries = m_respBody.getBodyEntries().iterator();
         while(bodyEntries.hasNext() && resultResp == null)
         {
            Element el = (Element)bodyEntries.next();
            if(getUnqualifiedNodeName(el).equals(XML_RESULT_RESP_NODE))
               resultResp = el;
         }
         
         if(resultResp != null)
            m_respStatus = resultResp.getAttribute(XML_RESULT_RESP_TYPE_ATTR);
         else //for success there may or may not be any result response
            m_respStatus = STATUS_SUCCESS;  
      }
      
      return m_respStatus;
   }

   /**
    * Return the node's name without any namespace qualifier
    * 
    * @param node Node may not be <code>null</code>
    * 
    * @return The unqualified name
    */
   public static String getUnqualifiedNodeName(Node node)
   {
      String unqualifiedName = node.getLocalName();
      
      return (unqualifiedName == null) ? node.getNodeName() : unqualifiedName;
   }

   /**
    * The interpreter to use to construct the soap envelope from the script 
    * element, initialized in the constructor and never <code>null</code> or 
    * modified after that. 
    */
   private EmpireScriptInterpreter m_interpreter;
      
   /**
    * Holds the response status, <code>null</code> until first call to <code>
    * getResponseStatus()</code>. May be <code>null</code> if the request 
    * generated a soap fault. 
    */
   private String m_respStatus = null;
   
   /**
    * The constant to indicate successful response status as defined by Rx web 
    * services api.
    */
   public static final String STATUS_SUCCESS = "success";
   
   /**
    * The constant to indicate partially successful response status as defined 
    * by Rx web services api.
    */
   public static final String STATUS_PARTIAL = "partial";   
   
   /**
    * The constant to indicate failure response statusas defined by Rx web 
    * services api.
    */
   public static final String STATUS_FAILURE = "failure";
   
   /** XML constants that the soap script request uses. **/   
   public static final String XML_NODE_NAME = "PostSOAPRequest";
   public static final String XML_HEADER_NODE = "SOAPHeader";
   public static final String XML_BODY_NODE = "SOAPBody";
   public static final String XML_ACTION_ATTR = "action";   
   public static final String XML_SERVICE_ATTR = "service";   
   public static final String XML_RESULT_RESP_NODE = "ResultResponse";
   public static final String XML_RESULT_RESP_TYPE_ATTR = "type";  
}

