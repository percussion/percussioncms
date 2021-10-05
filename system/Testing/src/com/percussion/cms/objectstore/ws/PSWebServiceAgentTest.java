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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.cms.objectstore.ws;

import com.percussion.cms.IPSConstants;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.error.PSExceptionUtils;
import com.percussion.util.PSStopwatch;
import com.percussion.util.PSXMLDomUtil;
import com.percussion.utils.testing.IntegrationTest;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.experimental.categories.Category;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * This is a unit test for {@link PSWebServiceAgent}. It can also be used to
 * perform some SOAP operations as a sample program.
 */
@Category(IntegrationTest.class)
public class PSWebServiceAgentTest
{

   private static final Logger log = LogManager.getLogger(IPSConstants.TEST_LOG);

   /**
    * Constructor, called by main with the loaded properties file.
    *
    * @param props the parameter properties, never <code>null</code>.
    * 
    * @throws PSUnknownNodeTypeException if fail to login.
    */
   public PSWebServiceAgentTest(Properties props)
         throws PSUnknownNodeTypeException
   {
      m_props = props;

      String protocol = m_props.getProperty("protocol");
      String host = m_props.getProperty("hostName");
      int port = Integer.parseInt(m_props.getProperty("port"));

      String username = m_props.getProperty("loginId");
      String password = m_props.getProperty("loginPw");

      m_wsAgent = new PSWebServiceAgent(protocol, host, port, username,
            password);

      login(); // first login to get the rx location and session id
   }

   /**
    * Login to get the session id and rx location.
    * 
    * @throws PSUnknownNodeTypeException
    *            if error occurs.
    */
   private void login() throws PSUnknownNodeTypeException
   {
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element msg = doc.createElement(LOGIN_REQUEST);
      Element data = sendMessage(
         LOGIN_OPERATION, XML_NS_MISC, msg, LOGIN_RESPONSE);

      if (data != null)
      {
         Element loginDataEl =
            PSXMLDomUtil.getFirstElementChild(data, "LoginData");
         m_RxLocation = loginDataEl.getAttribute("hostUrl");

         Element sessionIdEl =
            PSXMLDomUtil.getFirstElementChild(loginDataEl, "SessionId");
         m_RxSession = PSXMLDomUtil.getElementData(sessionIdEl);
      }
      else
      {
         throw new RuntimeException("Fail to login");
      }
   }

   /**
    * Perform the specified transition for the supplied item.
    * 
    * @param locator
    *           the locator of the item, assume not <code>null</code>.
    * @param transitionId
    *           the transition id.
    * @param transitionName
    *           the name of the transition, assume not <code>null</code> or
    *           empty.
    * 
    * @throws PSUnknownNodeTypeException
    *            if error occurs.
    */
   private void doTransition(PSLocator locator, String transitionId,
         String transitionName) throws PSUnknownNodeTypeException
   {
      Document doc = PSXmlDocumentBuilder.createXmlDocument();

      Element msg = doc.createElement(TRANSITIONITEM_REQUEST);

      Element el = PSXmlDocumentBuilder.addEmptyElement(doc, msg, "ContentKey");
      el.setAttribute("contentId", String.valueOf(locator.getId()));
      el.setAttribute("revision", String.valueOf(locator.getRevision()));

      PSXmlDocumentBuilder.addElement(doc, msg, "TransitionId", transitionId);

      Element data = sendMessage(TRANSITIONITEM_OPERATION, XML_NS_WORKFLOW,
            msg, TRANSITIONITEM_RESPONSE);

      Element resultEl = PSXMLDomUtil.getFirstElementChild(data,
            "ResultResponse");
      String type = resultEl.getAttribute("type");
      if (!type.equalsIgnoreCase("success"))
      {
         String errormsg = "FAILURE: transition -> " + transitionName + "("
               + transitionId + ")";
         writeToLog(errormsg);
         throw new RuntimeException(errormsg);
      }
   }
   
   /**
    * Performs a number of transitions that is specified in the properties of
    * <code>m_prop</code>.
    * 
    * @throws PSUnknownNodeTypeException
    */
   private void performTransitions() throws PSUnknownNodeTypeException
   {
      PSStopwatch watch = new PSStopwatch();
      watch.start();

      String contentId = m_props.getProperty("contentId");
      String revision = m_props.getProperty("revision");
      
      String toTransitionId = m_props.getProperty("transitionId_out");
      String toTransitionName = m_props.getProperty("transitionName_out");
      
      String fromTransitionId = m_props.getProperty("transitionId_back");
      String fromTransitionName = m_props.getProperty("transitionName_back");

      int iteration = Integer.parseInt(m_props
            .getProperty("numberOfTransitionCycles"));

      PSLocator locator = new PSLocator(Integer.parseInt(contentId), Integer
            .parseInt(revision));

      for (int i = 0; i < iteration; i++)
      {
         doTransition(locator, toTransitionId, toTransitionName);
         doTransition(locator, fromTransitionId, fromTransitionName);

         writeToLog("[" + i + "]Execution time: " + watch.toString());
      }

      watch.stop();
      writeToLog("TOTAL execution time: " + watch.toString());
   }
   
   /**
    * Create and send a SOAP message to the dispatcher.
    * 
    * @param operation
    *           the SOAP method to call
    * @param nameSpace
    *           the namespace where the method lives, ex. design, search
    * @param message
    *           the XML body message to send
    * @param respNodeName
    *           the response XML node for the operation
    * @return the XML body of the response from the send
    */
   private Element sendMessage(
      String operation, String nameSpace, Element message, String respNodeName)
   {
      try
      {
         return m_wsAgent.sendSoapBody(
            m_wsAgent.getSoapBodyForParams(operation, nameSpace, message),
               respNodeName);
      }
      catch (Exception ex)
      {
         writeToLog("Send SOAP Message Error - "
            + "'" + operation + "' - "
            + ex.getMessage());
         return null;
      }
   }

   /**
    * Write a message to the log file
    *
    * @param msg the message to write
    */
   private void writeToLog(String msg)
   {
      log.info(msg);
   }

   /**
    * Load the properties from <code>DEFAULT_PROPERTIES_FILE</code> file.
    * 
    * @return the loaded properties, never <code>null</code>.
    */
   private static Properties loadProperties()
   {
      InputStream in = null;
      Properties props = new Properties();
      try
      {
         in = PSWebServiceAgentTest.class
               .getResourceAsStream(DEFAULT_PROPERTIES_FILE);
         props.load(in);
      }
      catch (FileNotFoundException e)
      {
         log.error("Unable to locate file: {}" , DEFAULT_PROPERTIES_FILE);
         System.exit(-1);
      }
      catch (IOException e)
      {
         log.error(
            "Error loading properties from file ({}): {}" ,
                 DEFAULT_PROPERTIES_FILE,
                 PSExceptionUtils.getMessageForLog(e));
         System.exit(-1);
      }
      finally
      {
         if (in != null)
         {
            try
            {
                  in.close();
            }
            catch (Exception e) { /* ignore */ }
         }
      }
      
      return props;
   }
   
   /**
    * Main for this converter application.
    *
    * @param args - @see usage
    */
   public static void main(String[] args)
   {
      Properties props = loadProperties();

      try
      {
         PSWebServiceAgentTest agentTest = new PSWebServiceAgentTest(props);
         agentTest.performTransitions();
      }
      catch (Exception e)
      {
         log.error(PSExceptionUtils.getDebugMessageForLog(e));
      }

      System.exit(0);
   }

   /**
    * The parameter properties, which is loaded from 
    * <code>DEFAULT_PROPERTIES_FILE</code>. Init by ctor, never 
    * <code>null</code> after that.
    */
   private Properties m_props = new Properties();

   /**
    * The webservice agent object, init by ctor, never <code>null</code>
    * after that.
    */
   private PSWebServiceAgent m_wsAgent = null;

   /**
    * The Rhythmyx location, init by the first login, never <code>null</code> 
    * after that.
    */
   private String m_RxLocation = null;

   /**
    * The session id, init by the first login, never <code>null</code> after 
    * that.
    */
   private String m_RxSession = null;

   /**
    * A list of private constants
    */
   private static final String DEFAULT_PROPERTIES_FILE = "PSWebServiceAgentTest.properties";

   private static final String XML_ATTR_NS = "xmlns";

   private static final String XML_NS_PREFIX = "urn:www.percussion.com/webservices/";

   private static final String XML_NS_CONTENTDATA = XML_NS_PREFIX
         + "contentdata";

   private static final String XML_NS_SEARCH = XML_NS_PREFIX + "search";

   private static final String XML_NS_DESIGN = XML_NS_PREFIX + "design";

   private static final String XML_NS_MISC = XML_NS_PREFIX + "miscellaneous";

   private static final String XML_NS_WORKFLOW = XML_NS_PREFIX + "workflow";

   /**
    * Send message SOAP constants
    */
   public static final String LOGIN_OPERATION = "login";
   public static final String LOGIN_REQUEST = "LoginRequest";
   public static final String LOGIN_RESPONSE = "LoginResponse";

   public static final String CONTENTTYPELIST_OPERATION = "contentTypeList";
   public static final String CONTENTTYPELIST_REQUEST = "ContentTypeListRequest";
   public static final String CONTENTTYPELIST_RESPONSE = "ContentTypeListResponse";

   public static final String CONTENTTYPE_OPERATION = "contentType";
   public static final String CONTENTTYPE_REQUEST = "ContentTypeRequest";
   public static final String CONTENTTYPE_RESPONSE = "ItemDefData";

   public static final String SEARCH_OPERATION = "search";
   public static final String SEARCH_REQUEST = "SearchRequest";
   public static final String SEARCH_RESPONSE = "SearchResponse";

   public static final String OPENITEM_OPERATION = "openItem";
   public static final String OPENITEM_REQUEST = "OpenItemRequest";
   public static final String OPENITEM_RESPONSE = "ItemResponse";

   public static final String UPDATEITEM_OPERATION = "updateItem";
   public static final String UPDATEITEM_REQUEST = "UpdateItemRequest";
   public static final String UPDATEITEM_RESPONSE = "UpdateItemResponse";

   public static final String TRANSITIONITEM_OPERATION = "transitionItem";
   public static final String TRANSITIONITEM_REQUEST = "transitionItemRequest";
   public static final String TRANSITIONITEM_RESPONSE = "transitionItemResponse";

}
