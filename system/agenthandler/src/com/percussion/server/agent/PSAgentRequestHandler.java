/*
 *     Percussion CMS
 *     Copyright (C) 1999-2021 Percussion Software, Inc.
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
package com.percussion.server.agent;

import com.percussion.conn.PSServerException;
import com.percussion.data.PSXslStyleSheetMerger;
import com.percussion.server.*;
import com.percussion.tools.Base64;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;

/**
 * Class to handle the agent HTTP requests from the clients. Standard
 * clients are:
 * <br>
 * <ul>
 * <li>Agent Manager Web client </li>
 * <li>Command line runner, potentially from a scheduler, and </li>
 * <li>Workflow action that does ad hoc publishing </li>
 * </ul>
 * <br>
 * The HTTP request shall be of the form:
 * <br><br>
 * <strong>http://<emp>server</emp>:<emp>port</emp>/Rhythmyx/AgentManager/
 * agentmanager.htm?rxagent=publish&rxagentaction=execute</strong>
 * <br>
 * TODO:
 *
 */
public class PSAgentRequestHandler implements IPSLoadableRequestHandler
{
   /* ************ IPSLoadableRequestHandler Interface Implementation ********/

   /**
    * Server thread calls this method during initialization. Any initialization
    * required to be done based on the data from the config file shall be done
    * here.
    */
   public void init(Collection requestRoots, InputStream cfgFileIn)
      throws PSServerException
   {
      PSConsole.printMsg(HANDLER, "Initializing request handler...");

      ms_requestRoots = requestRoots;

      if(null == ms_Server)
      {
         ms_Server = PSServer.getHostAddress();
         try
         {
            ms_Port = Integer.toString(PSServer.getListenerPort());
         }
         catch(NumberFormatException e)
         {
            ms_Port = "0";
         }
      }
      /*
      * First try loading the resource file, if fails it is fatal for the
      * application
      */
      try
      {
         ms_Res = PSUtils.getRes();
      }
      catch(MissingResourceException e)
      {
         PSConsole.printMsg(HANDLER,
            "Failed to load agent request handler resources");
         throw new PSServerException(e);
      }
      /*
       * Initialize the product version description.
       */
      try
      {

         ResourceBundle resourceBundle = ResourceBundle.getBundle(
            "com.percussion.server.agent." + "Version", Locale.getDefault());

         ms_ProductVersion += resourceBundle.getString("majorVersion") + "." +
               resourceBundle.getString("minorVersion") +
               "; Build:" + resourceBundle.getString("buildNumber");
      }
      catch(MissingResourceException e)
      {
         PSConsole.printMsg(HANDLER, "Failed to load Version.properties");
         throw new PSServerException(e);
      }
      /*
      * Now read the configuration file and do initialization if required. We
      * have nothing for now!
      */
      try
      {
         readConfigDoc(cfgFileIn);
         m_AgentManager = new PSAgentManager(m_ConfigDoc);
      }
      //Any exception here is fatal
      catch(Exception e)
      {
         System.out.println(
            "Failed to initialize agent manager request handler");
         throw new PSServerException(e);
      }
      finally
      {
         if (cfgFileIn != null)
         {
            try{cfgFileIn.close();}catch (Exception e){}
         }
      }
      PSConsole.printMsg(HANDLER, "Request handler initialization completed");
   }

   /**
    * Helper function that reads the configuration XML document from the input
    * stream.
    * @param is the input stream
    * @throws IOException
    * @throws SAXException
    */
   private void readConfigDoc(InputStream is)
      throws SAXException, IOException
   {
      if(is == null)
      {
         throw new IOException(
            "The input stream for configuration document must not be empty");
      }

      InputSource isource = null;
      try
      {
         DocumentBuilder db = PSUtils.getDocumentBuilder();
         isource = new InputSource(is);
         m_ConfigDoc = db.parse(isource);
      }
      finally
      {
         isource = null;
      }
   }

   /* ************ IPSLoadableRequestHandler Interface Implementation ****** */

   /**
   * Process a Agent Manager request using the input context information and
   * data. The results will be written to the specified output stream.
   * <p>
   * The following steps are performed to handle the request:
   * <ol>
   * <li>validate the request</li>
   * <li>see if requested action can be performed</li>
   * <li>return the response</li>
   * </ol>
   *
   * @param   request      the request object containing all context
   *            data associated with the request
   */
   public void processRequest(PSRequest request)
   {
      PSResponse resp = request.getResponse();
      String page = request.getRequestPage(false);
      String cmsUserid = "";
      String cmsPassword = "";
      String sessionid = "";

      try
      {
         PSAgentHandlerResponse handlerResponse =
                                    new PSAgentHandlerResponse();

         //use the style sheet from the handler
         handlerResponse.setStyleSheet(m_StyleSheetPath);

         if(!page.equalsIgnoreCase(IPSDTDAgentHandlerResponse.HANDLER_PAGE) &&
            !page.equalsIgnoreCase(IPSDTDAgentHandlerResponse.MANAGER_PAGE)
            )
         {
            handlerResponse.setResponse(
               handlerResponse.RESPONSE_TYPE_ERROR,
               getRes().getString(handlerResponse.RESPONSE_CODE_NOPAGE),
               handlerResponse.RESPONSE_CODE_NOPAGE);
            send(request, handlerResponse);
            return;
         }

         Map<String, Object> map = request.getParameters();

         /* This is not exactly right error since the map is NULL. However it
          * is not unreasonable to say editionid is not specified when the map
          * is empty.
          */
         if(map == null)
         {
            handlerResponse.setResponse(
               handlerResponse.RESPONSE_TYPE_ERROR,
               getRes().getString(handlerResponse.RESPONSE_CODE_NOACTION),
               handlerResponse.RESPONSE_CODE_NOACTION);
            send(request, handlerResponse);
            return;
         }

         if(!map.containsKey(IPSDTDAgentHandlerResponse.HANDLER_PARAM_ACTION))
         {
            handlerResponse.setResponse(
               handlerResponse.RESPONSE_TYPE_ERROR,
               getRes().getString(handlerResponse.RESPONSE_CODE_NOACTION),
               handlerResponse.RESPONSE_CODE_NOACTION);

            send(request, handlerResponse);
            return;
         }

         sessionid = request.getUserSessionId();

         if(map.containsKey(HTML_PARAM_CMS_USERID))
         {
            cmsUserid = map.get(HTML_PARAM_CMS_USERID).toString();
         }

         if(map.containsKey(HTML_PARAM_CMS_PASSWORD))
         {
            cmsPassword = map.get(HTML_PARAM_CMS_PASSWORD).toString();
            if(cmsPassword != null && cmsPassword.length() > 0)
               cmsPassword = new String(Base64.decode(cmsPassword));
         }

         String action = map.get(
            IPSDTDAgentHandlerResponse.HANDLER_PARAM_ACTION).toString();

         Element result = null;
         if(page.equalsIgnoreCase(IPSDTDAgentHandlerResponse.HANDLER_PAGE))
         {
            handleAction(action, map, handlerResponse);
         }
         else //MANAGER_PAGE
         {
            m_AgentManager.handleAction(map, handlerResponse);
         }
         send(request, handlerResponse);
      }
      catch(Exception e)
      {
         PSConsole.printMsg("AgentMgr", e);
      }
   }

   /**
    * Most of the requested actions are meant to be handled by the Agent
    * Manager. However, there are some actions to be handled by the Request
    * Handler itself. For example, read the configuration file from the disk
    * and reinitialize the Agent Manager when modifed or status of the agent
    * manager. This method handles this kind of actions.
    */
   protected void handleAction(
      String action,
      Map params,
      PSAgentHandlerResponse handlerResponse)
   {
      //No action is currently handled by the requets handler.

      //TODO: use MessageFormat
      String msg = "Action '" + action + "' for " +
         handlerResponse.HANDLER_PAGE + " is not valid" ;
      handlerResponse.setResponse(handlerResponse.RESPONSE_TYPE_ERROR, msg);
   }

   // see IPSRootedHandler for documentation
   public String getName()
   {
      return HANDLER;
   }

   // see IPSRootedHandler for documentation
   public Iterator getRequestRoots()
   {
      return ms_requestRoots.iterator();
   }

   /**
    * Helper method to send the response to the client back. If the requested
    * page extension starts with "htm", we merge the response with the style
    * sheet, otherwise we send the XML document.
    *
    * @param request, the PSRequest object, must not be <code>null</code>.
    *
    * @param handlerResponse, the agent manager handler's response object,
    * must not be <code>null</code>.
    *
    * @throws IOException, if the response could not be sent to the client.
    *
    */
   protected void send(PSRequest request,
      PSAgentHandlerResponse handlerResponse)
   throws IOException
   {
      String ext = request.getRequestPageExtension();
      if(null != ext &&
         ext.toLowerCase().startsWith(".htm") &&
         handlerResponse.getStyleSheet() != null
         )
      {
         URL styleSheetURL = null;
         try
         {
            //No caching of the style sheet. Can be a future task.
            styleSheetURL = new URL("file:" + handlerResponse.getStyleSheet());
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            new PSXslStyleSheetMerger().merge(
               request,
               handlerResponse.getDocument(),
               out,
               styleSheetURL,
               null);
            request.getResponse().setContent(new ByteArrayInputStream(
               out.toByteArray()), out.size(), "text/html");
         }
         /* Can throw MalformedURLException, PSUnsupportedConversionException
          * or PSConversionException
          */
         catch(Exception e)
         {
            PSConsole.printMsg(HANDLER,
               "Error merging response document and stylesheet: "
               + e.getMessage());
            //Send at least XML document!
            request.getResponse().setContent(handlerResponse.getDocument());
         }
      }
      else
      {
         request.getResponse().setContent(handlerResponse.getDocument());
      }
   }

   /*********** IPSLoadableRequestHandler Interface Implementation ***********/
   /**
    * Shutdown the request handler, freeing any associated resources. This is
    * called by server during server shut down.
    */
   public void shutdown()
   {
      PSConsole.printMsg(HANDLER, "Closing Agent Manager...");
      m_AgentManager.close();
   }

   /**
    * Get the program resources.
    *
    * @return resource bundle as ResourceBundle never <code>null</code> as it
    * is initialized in the init() method which makes sure it is not
    * <code>null</code>
    *
    */
   public static ResourceBundle getRes()
   {
      if(null != ms_Res)
         return ms_Res;

      try
      {
         ms_Res = PSUtils.getRes();
      }
      catch(MissingResourceException mre)
      {
         //Should not happen!!!
      }
      return ms_Res;
   }

   /**
    * <code>PSAgentManager</code> object that processes all requests from
    * the clients. Manages all workers.
    */
   private PSAgentManager m_AgentManager = null;

   /**
    * DOM Document representing the configuration document for all agents
    * registered.
    */
   private Document m_ConfigDoc = null;
   /**
    * Program resources
    */
   private static ResourceBundle ms_Res = null;

   /**
    * Product version string to start with. Init will append the version and
    * build info to this.
    */
    public static String ms_ProductVersion = "Rhythmyx Agent Manager ";

   /**
    * Server name IP Address to use. Set these to <code>null</code> to get it
    * from PSServer class, which must be the normal case. Use other than
    * <code>null</code> only for testing.
    */
    public static String ms_Server = null;

   /**
    * Server port number string to use. Set these to <code>null</code> to get
    * it from PSServer class, which must be the normal case. Use other than
    * <code>null</code> only for testing.
    */
    public static String ms_Port = null;

    /**
     * Storage for the request roots, initialized in init() call, never
     * <code>null</code> or empty after that. A list of String objects.
     */
    public static Collection ms_requestRoots = null;

    /**
     * Name of the subsystem used to dump messages to server console.
     */
    public static final String HANDLER = "AgentMgr";

    /**
     * Name of the optional HTML parameter User ID in the request to publish.
     */
    public static final String HTML_PARAM_CMS_USERID = "userid";

    /**
     * Name of the optional HTML parameter Password in the request to publish.
     */
    public static final String HTML_PARAM_CMS_PASSWORD = "password";

   /**
    * Location of the style sheet to be used for the response document.
    */
    static private String m_StyleSheetPath =
         "sys_resources/stylesheets/agenthandlerresponse.xsl";

    /**
     * main method for testing purpose
     */
    public static void main(String[] args)
    {
      PSAgentRequestHandler handler = new PSAgentRequestHandler();

      try
      {
         handler.handleAction("test", null, null);
      }
      catch(Exception e)
      {
         e.printStackTrace();
      }
   }

}
