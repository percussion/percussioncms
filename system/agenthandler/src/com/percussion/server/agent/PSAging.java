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

package com.percussion.server.agent;

import com.percussion.cms.IPSCmsErrors;
import com.percussion.cms.PSCmsException;
import com.percussion.data.IPSInternalRequestHandler;
import com.percussion.data.IPSInternalResultHandler;
import com.percussion.data.PSExecutionData;
import com.percussion.data.PSInternalRequestCallException;
import com.percussion.error.PSException;
import com.percussion.security.PSAuthenticationFailedException;
import com.percussion.security.PSAuthorizationException;
import com.percussion.security.PSThreadRequestUtils;
import com.percussion.server.PSConsole;
import com.percussion.server.PSInternalRequest;
import com.percussion.server.PSRequest;
import com.percussion.server.PSServer;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.utils.request.PSRequestInfo;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * This class implements the interfaces <code>IPSAgent</code> and
 * <code>TimerTask</code>. The initialization process includes scheduling of
 * the aging execution (implemented completely in run() method).
 */
public class PSAging extends TimerTask implements IPSAgent
{
   /*
    * Implementation of the method from <code>IPSAgent</code>.
    */
   public void init(Element configData) throws PSAgentException
   {
      String servicetype = configData
         .getAttribute(IPSDTDAgentManagerConfig.ATTRIB_SERVICE_TYPE);

      if (servicetype.equals(IPSDTDAgentManagerConfig.SERVICE_TYPE_SCHEDULED))
      {
         NodeList nl = configData
            .getElementsByTagName(IPSDTDAgentManagerConfig.ELEM_SCHEDULE);
         // TODO: use MessageFormat for the error message
         if (nl == null || nl.getLength() < 1)
         {
            throw new PSAgentException(
               "A scheduled agent must have the element '"
                  + IPSDTDAgentManagerConfig.ELEM_SCHEDULE
                  + "' defined in the configuration");
         }

         Element schedule = (Element) nl.item(0);
         String temp = schedule
            .getAttribute(IPSDTDAgentManagerConfig.ATTRIB_DELAY);
         try
         {
            m_Delay = Integer.parseInt(temp);
         }
         catch (NumberFormatException e)
         {
            // use the default value OR
            // TODO: handle exception ???
         }

         temp = schedule.getAttribute(IPSDTDAgentManagerConfig.ATTRIB_INTERVAL);
         try
         {
            m_Interval = Integer.parseInt(temp);
         }
         catch (NumberFormatException e)
         {
            // use the default value OR
            // TODO: handle exception ???
         }
         m_Timer = new Timer();
         m_Timer.schedule(this, m_Delay * 1000, m_Interval * 1000);

         PSConsole.printMsg(PSAgentRequestHandler.HANDLER,
            "Aging agent is initialized...");
      }
   }

   /*
    * Implementation of the method from <code>IPSAgent</code>
    */
   public void terminate()
   {
      PSConsole.printMsg(PSAgentRequestHandler.HANDLER,
         "Closing aging agent...");
      m_Timer.cancel();
      m_Timer = null;
   }

   /*
    * Implementation of the method from <code>IPSAgent</code>.
    */
   public void executeAction(String action, Map params,
      IPSAgentHandlerResponse response)
   {
      response.setResponse(IPSAgentHandlerResponse.RESPONSE_TYPE_INFO,
         "No actions are implemented");
   }

   @Override
   public void run()
   {
      if ((!PSServer.isInitialized()))
      {
         return;
      }
      
      
      PSConsole.printMsg(AGENT_NAME, "Polling aging action at: "
         + new Date().toString() + "...");

      try
      {
         int runCount = 0;
         Document docCList = null;
         Element elemItem = null;
    
         do
         {
            PSRequest request = PSThreadRequestUtils.initServerThreadRequest();
            docCList = getDocument(null, AGING_CONTENT_RESOURCE_NAME);
            NodeList nl = docCList.getElementsByTagName("contentitem");

            if (nl == null || nl.getLength() < 1)
            {
               break;
            }
            for (int i = 0; i < nl.getLength(); i++)
            {
             
               elemItem = (Element)nl.item(i);
               int contentId = -1; // default less than 0
               int stateId = -1;
               int workflowId = -1;
               try
               {
                  contentId = Integer.parseInt(elemItem
                        .getAttribute("contentid"));
                  stateId = Integer.parseInt(elemItem.getAttribute("stateid"));
                  workflowId = Integer.parseInt(elemItem.getAttribute("workflowid"));
               }
               catch (NumberFormatException e1)
               {
                  // ignore, just skip this one
                  continue;
               }
               request = PSThreadRequestUtils.initServerThreadRequest();
               /* check if the item is still in the same state; it is possible 
                * the previous transition affected this item or even a user
                * could move an item since we got the list
                */
               if(stateId != getCurrentState(contentId))
               {
                  PSConsole.printMsg(AGENT_NAME,
                     "Skipping item " 
                     + contentId
                     + " because it was transitioned while waiting to be processed in the Aging agent queue.");
                  continue;
               }
               
               try
               {
                  String urlString = getElementData(getChildElement(elemItem,
                     "editorurl"));
                  if (urlString.trim().length() < 1)
                     continue;
                  urlString += "&" + IPSHtmlParameters.SYS_WORKFLOWID + 
                     "=" + workflowId;
                  transitionItem(request, urlString);
               }
               catch (Exception e)
               {
                  PSConsole.printMsg(PSAgentRequestHandler.HANDLER, e);
               }
            }
         }
         while (++runCount < ms_Runs);
      }
      catch (Exception e)
      {
         PSConsole.printMsg(PSAgentRequestHandler.HANDLER, e);
      }
   }

   /**
    * Lookup the current state of the supplied item.
    * 
    * @param contentId The id of the content item for which you want to find the
    * state.
    * 
    * @return -1 if an item with the supplied id is not found, otherwise the
    * id of the state the this item is in.
    * 
    * @throws PSAgentException If the resource used to obtain the information 
    * cannot be found or the request fails for any reason.
    */
   private int getCurrentState(int contentId)
      throws PSAgentException
   {
      HashMap params = new HashMap();
      params.put("sys_contentid", String.valueOf(contentId));
      Document doc = getDocument(params, ITEM_STATE_RESOURCE_NAME);
      if (doc == null)
         return -1;

      Element elemItem = doc.getDocumentElement();
      int stateId = -1;
      try
      {
         stateId = Integer.parseInt(elemItem.getAttribute("stateid"));
      }
      catch (NumberFormatException e1)
      {
         // ignore, just skip this one
      }
      
      return stateId;
   }

   /**
    * Performs an internal request to the supplied resource, sending the
    * supplied parameters to the request.
    * 
    * @param params Name/value pairs to pass to the request. May be
    * <code>null</code>.
    * 
    * @param resourceName The name of the resource, in the form
    * appName/resourceName.
    * 
    * @return The doc returned from the resource. May be <code>null</code>.
    * 
    * @throws PSAgentException If the resource can't be found or any problems
    * occur making the request.
    */
   private Document getDocument(HashMap params, String resourceName) 
      throws PSAgentException
   {
      PSExecutionData data = null;
      try
      {
         PSThreadRequestUtils.changeToInternalRequest(true);
         
         IPSInternalRequestHandler rqh = PSServer.getInternalRequestHandler(
               resourceName);
         if (null == rqh || !(rqh instanceof IPSInternalResultHandler))
         {
            throw new PSAgentException(
               "Aging resource: '" + resourceName + "' not found");
         }
         IPSInternalResultHandler rsh = (IPSInternalResultHandler) rqh;
         
         PSRequest req = PSThreadRequestUtils.getPSRequest();
        
         if (params != null)
            req.setParameters(params);
         
         data = rsh.makeInternalRequest(req);
         return rsh.getResultDoc(data);
      }
      catch (PSInternalRequestCallException irce)
      {
         throw new PSAgentException(irce.getMessage());
      }
      catch (PSAuthorizationException ae)
      {
         throw new PSAgentException(ae.getMessage());
      }
      catch (PSAuthenticationFailedException afe)
      {
         throw new PSAgentException(afe.getMessage());
      }
      finally
      {
         PSThreadRequestUtils.restoreOriginalRequest();
         if (null != data)
         {
            data.release();
         }
         data = null;
      }
   }

   /**
    * Helper function to return the first child element with given name of the
    * parent.
    * 
    * @param parent parent element - may be <code>null</code>
    * @param child child element name may be <code>null</code>
    */
   private Element getChildElement(Element parent, String child)
   {
      if (parent == null)
         return null;

      if (child == null || child.trim().length() < 1)
         return null;

      NodeList nl = parent.getElementsByTagName(child);
      if (nl == null || nl.getLength() < 1)
         return null;
      return (Element) nl.item(0);
   }

   /**
    * Helper function to get the text data of a given element
    * 
    * @param elem - Elelemnt to extract data of - may be <code>null</code>.
    */
   private String getElementData(Element elem)
   {
      if (elem == null)
         return "";
      Node node = elem.getFirstChild();
      if (node != null && node instanceof Text)
      {
         return ((Text) node).getData();
      }
      return "";
   }

   /**
    * Performs the specified transition for the specified item. More
    * specifically this executes the supplied Rx URL that transistions the item.
    * 
    * @param request never <code>null</code>.
    * @path transition url with all required parameters to transition the item,
    * assumed not <code>null</code> and not empty.
    * 
    * @throws PSException if an error occurs during the transition.
    */
   private void transitionItem(PSRequest request, String path)
      throws PSException
   {
      PSConsole.printMsg(AGENT_NAME, "Processing Item with URL: " + path
         + "...");
      PSInternalRequest iReq = makeInternalRequest(request, path);
      checkValidationError(iReq.getRequest(), path);
   }

   /**
    * Check the validation error that is registered in the given request.
    * 
    * @param request The request that may contains the validation error, assumed
    * not <code>null</code>.
    * 
    * @param path the resource path that is used for the request, assumed not
    * <code>null</code> or empty.
    * 
    * @throws PSCmsException if an validation error has occurred.
    */
   private void checkValidationError(PSRequest request, String path)
      throws PSCmsException
   {
      String validateError = request
         .getParameter(IPSHtmlParameters.SYS_VALIDATION_ERROR);
      if (validateError != null && validateError.trim().length() > 0)
      {
         throw new PSCmsException(IPSCmsErrors.VALIDATION_ERROR, new Object[]
         {
            path, validateError
         });
      }
   }

   /**
    * Helper function to execute an internal request.
    * 
    * @param request the original request object, assumed not <code>null</code>
    * @param path the application and resource location of the action to be
    * executed by the system, assumed not <code>null</code>
    * 
    * @return PSInternalRequest the internal request that was generated, never
    * <code>null</code>, may contain a modified request object
    * 
    * @throws PSException if the internal request is not created
    */
   private PSInternalRequest makeInternalRequest(PSRequest request, String path)
      throws PSException
   {
      //Reset the validation error
      request.setParameter(IPSHtmlParameters.SYS_VALIDATION_ERROR, "");
      PSInternalRequest iReq = PSServer.getInternalRequest(path, request, null,
         true);
      if (iReq == null)
         throw new PSException(IPSCmsErrors.REQUIRED_RESOURCE_MISSING, path);

      iReq.performUpdate();

      return iReq;
   }

   /**
    * Default initial delay in seconds before the first polling of aging
    * execution happens.
    */
   private int m_Delay = 3600;

   /**
    * Default interval time in seconds between two successive aging excecution
    * after the first execution.
    */
   private int m_Interval = 3600;

   /**
    * The <code>Timer</code> object that schedules (or cancels) an aging
    * execution task. This object is created only if the agent is of type
    * scheduled.
    */
   private Timer m_Timer = null;

   /**
    * Number of runs that the agent should execute the aging action during each
    * polling. This is to make sure all the transitions are covered between the
    * last aging action and now.
    */
   private static int ms_Runs = 2;

   /**
    * The name of this agent.
    */
   private static final String AGENT_NAME = "Aging Agent";

   /**
    * The xml application name that contains the resources used by this exit 
    * (no trailing slash.)
    */
   private static final String APP_NAME = "sys_ageSupport";
   
   /**
    * appName/resourceName to make internal request to get the current state id
    * of a specified item.
    */
   private static final String ITEM_STATE_RESOURCE_NAME = APP_NAME
         + "/currentstate";

   /**
    * appName/resourceName to make internal request to get the content list for
    * aging.
    */
   private static final String AGING_CONTENT_RESOURCE_NAME = APP_NAME
         + "/agecontentlist";

   /**
    * The default timeout when making HTTP request to make transitions.
    */
   private static int DEFAULT_TIMEOUT_MILLIS = 100000;
}
