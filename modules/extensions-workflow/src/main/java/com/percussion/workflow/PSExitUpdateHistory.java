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

package com.percussion.workflow;

import com.percussion.cms.IPSCmsErrors;
import com.percussion.cms.IPSConstants;
import com.percussion.cms.PSApplicationBuilder;
import com.percussion.cms.PSCmsException;
import com.percussion.error.PSException;
import com.percussion.extension.*;
import com.percussion.i18n.PSI18nUtils;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.IPSServerErrors;
import com.percussion.server.PSInternalRequest;
import com.percussion.server.PSServer;
import com.percussion.services.legacy.IPSCmsObjectMgr;
import com.percussion.services.legacy.PSCmsObjectMgrLocator;
import com.percussion.utils.exceptions.PSORMException;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

/**
 * Updates the status history context for this transition or checkout or
 * checkin action.
 */
public class PSExitUpdateHistory implements IPSResultDocumentProcessor
{
   /**
    * The fully qualified name of this extension.
    */
   private String m_fullExtensionName = "";

   private String  ms_actionTriggerName = "";

   /* Set the parameter count to not initialized */
   private int ms_correctParamCount = IPSExtension.NOT_INITIALIZED;

   /**************  IPSExtension Interface Implementation ************* */
   public void init(IPSExtensionDef extensionDef, File file)
      throws PSExtensionException
   {
      ms_actionTriggerName = PSWorkFlowUtils.properties.getProperty(
         PSWorkFlowUtils.ACTION_TRIGGER_NAME,
         PSWorkFlowUtils.DEFAULT_ACTION_TRIGGER_NAME);

      if (ms_correctParamCount == IPSExtension.NOT_INITIALIZED)
      {
         ms_correctParamCount = 0;

         Iterator iter = extensionDef.getRuntimeParameterNames();
         while(iter.hasNext())
         {
            iter.next();
            ms_correctParamCount++;
         }
         m_fullExtensionName = extensionDef.getRef().toString();
      }
   }

  /* *******  IPSResultDocumentProcessor Interface Implementation ******* */

   /**
    * Return <CODE>true</CODE>, this extension can modify the style sheet.
    */
   public boolean canModifyStyleSheet()
   {
      return true;
   }

   /**
    * Updates the status history context for this transition or checkout or
    * checkin action after validating the input data. Add the status history
    * context ID to the workflow context private object.
    *
    * @param  params          the parameters for this extension.
    *         params[0]       the content ID
    *         params[1]       the user name
    * @param request          the context of the request associated with this
    *                         extension. if <CODE>request</CODE> does not
    *                         contain the workflow context private object
    *                         which has the key <CODE>
    *                        IPSWorkFlowContext.WORKFLOW_CONTEXT_PRIVATE_OBJECT
    *                         </CODE> no action will be taken, because this
    *                         object contains the transition ID associated
    *                         with this status history item.
    *                         The status history item ID of this private object
    *                         will be updated by this ext.
    *                         The HTML parameters used from this request:
    *                         action trigger name, transition comments.
    *
    * @param  resDoc          the result XML document
    *
    * @return                 <code>resultDoc</code> is returned unchanged
    *
    * @throws                 PSParameterMismatchException
    *                         if wrong number of parameters is supplied.
    * @throws                 PSExtensionProcessingException if
    *                         <ul>
    *                          <li><CODE>request</CODE> is <CODE>null</CODE>
    *                         </li>
    *                         <li>user name parameter is <CODE>null</CODE>
    *                         </li>
    *                         <li>a parameter is of the wrong type
    *                         </li>
    *                         <li>an SQL exception is caught
    *                         </li>
    *                         <li>a PSEntryNotFoundException is caught because
    *                         an expected data base entry does not exist
    *                         </li>
    *                         </ul>
    */
   public Document processResultDocument(Object[] params,
                                         IPSRequestContext request,
                                         Document resDoc)
      throws PSParameterMismatchException, PSExtensionProcessingException
   {
      PSWorkFlowUtils.printWorkflowMessage(request,
         "\nUpdate History: enter processResultDocument");
      PSConnectionMgr connectionMgr = null;
      PSWorkflowRoleInfo wfRoleInfo = null;
      PSWorkFlowContext wfContext = null;
      Map<String,Object> htmlParams;
      int nParamCount = 0;
      String sActionTrigger = "";
      int nContentID = 0;
      String userName = "";
      String sessionID = "";
      int transitionID = 0;
      String transitionComment = "";
      int contentstatushistoryid = 0;
      List stateAssignedRoles = null;
      Exception except = null;
      String lang = null;
      try
      {
         if(null == request)
         {
            throw new PSExtensionProcessingException(
               m_fullExtensionName,
               new IllegalArgumentException("The request must not be null"));
         }
         sessionID = request.getUserSessionId();
         htmlParams = request.getParameters();

         if(null == params || null == htmlParams)
         {
            PSWorkFlowUtils.printWorkflowMessage(request,
               "update history: no parameters - exiting");
            return resDoc; //no parameters exit with peace!
         }

         lang = (String)request.getSessionPrivateObject(
          PSI18nUtils.USER_SESSION_OBJECT_SYS_LANG);
         if (lang == null)
            lang =   PSI18nUtils.DEFAULT_LANG;

         transitionComment =
               PSWorkFlowUtils.getTransitionCommentFromHTMLParams(htmlParams);

         nParamCount = params.length;

         if(!htmlParams.containsKey(ms_actionTriggerName))
         {
            PSWorkFlowUtils.printWorkflowMessage(request,
               "update history: no action trigger in html parameters - " +
                  "exiting\n");

            return resDoc; //no html Params - no history update!
         }

         sActionTrigger = (String)htmlParams.get(ms_actionTriggerName);
         if(null == sActionTrigger)
            sActionTrigger = "";
         if(0 == sActionTrigger.trim().length())
         {
            PSWorkFlowUtils.printWorkflowMessage(request,
               "update history: no action trigger - exiting");
            return resDoc; //no action is performed no history update!
         }
         try
         {
            if(ms_correctParamCount != nParamCount)
            {
               String[] exParams = {String.valueOf(ms_correctParamCount),
                                    String.valueOf(nParamCount)};

               throw new PSInvalidNumberOfParametersException(exParams);
            }

            if(null == params[0] || 0 == params[0].toString().trim().length())
            {
               PSWorkFlowUtils.printWorkflowMessage(request,
                  "update history: no contentid - no history will be written");
               return resDoc; //no contentid so do nothing
            }
            nContentID = Integer.parseInt(params[0].toString());

            if(0 == nContentID)
            {
               PSWorkFlowUtils.printWorkflowMessage(request,
                  "update history: no contentid - no history will be written");

               return resDoc; //no contentid so do nothing
            }

            if(null == params[1] || 0 == params[1].toString().trim().length())
            {
               throw new PSInvalidParameterTypeException(lang,
                IPSExtensionErrors.EMPTY_USRNAME1);
            }
            userName = params[1].toString();
            userName = PSWorkFlowUtils.filterUserName(userName);
         }
         catch(PSInvalidNumberOfParametersException | PSInvalidParameterTypeException ne)
         {
            String language = ne.getLanguageString();
            if (language == null)
               language = lang;
            throw new PSExtensionProcessingException(language,
             m_fullExtensionName, ne);
         }

         wfContext = (PSWorkFlowContext) request.getPrivateObject
               (IPSWorkFlowContext.WORKFLOW_CONTEXT_PRIVATE_OBJECT);

         if (null == wfContext)
         {
            PSWorkFlowUtils.printWorkflowMessage(request,
               "update history: - no transition action happened - no history" +
               "will be written");
            return resDoc; //no workflow context - no history
         }

         transitionID = wfContext.getTransitionID();
         if (IPSConstants.TRANSITIONID_NO_ACTION_TAKEN == transitionID)
         {
            PSWorkFlowUtils.printWorkflowMessage(request,
               "update history: - no transition action happened - no history" +
               "will be written");
            return resDoc; //no action at all - no history
         }

         wfRoleInfo = (PSWorkflowRoleInfo) request.getPrivateObject(
            PSWorkflowRoleInfo.WORKFLOW_ROLE_INFO_PRIVATE_OBJECT);

         if (null != wfRoleInfo)
         {
            stateAssignedRoles = wfRoleInfo.getUserActingRoleNames();
         }
         else
         {
            PSWorkFlowUtils.printWorkflowMessage(request,
               "update history: - no state roles found - history" +
               "will be written");
         }

         //Get the connection
         Connection connection = null;
         try
         {
            connectionMgr = new PSConnectionMgr();
            connection = connectionMgr.getConnection();
         }
         catch(Exception e)
         {
            throw new PSExtensionProcessingException(
               m_fullExtensionName, e);
         }

         try
         {
            contentstatushistoryid =
               updateHistory(nContentID,
                                wfContext.getBaseRevisionNum(),
                                userName,
                                sessionID,
                                transitionID,
                                stateAssignedRoles,
                                transitionComment,
                                request,
                                connection);

            wfContext.setHistoryid(contentstatushistoryid);
            if (0 == contentstatushistoryid )
            {
               PSWorkFlowUtils.printWorkflowMessage(request,
                  "No status history was written.");
            }

         }
         catch(SQLException e)
         {
            except = e;
         }
         catch(PSEntryNotFoundException e)
         {
            if (e.getLanguageString() == null)
               e.setLanguageString(lang);
            except = e;
         }
                  catch (Exception e)
         {
            except = e;
         }
      }
      finally
      {
         try
         {
            if(null != connectionMgr)
               connectionMgr.releaseConnection();
         }
         catch(SQLException sqe)
         {
            // Ignore since this is cleanup
         }
         if (null != except)
         {
            PSWorkFlowUtils.printWorkflowException(request, except);
            if (except instanceof PSException)
            {
               String language = ((PSException)except).getLanguageString();
               if (language == null)
                  language = lang;
               throw new PSExtensionProcessingException(language,
               m_fullExtensionName, except);
            }
            else
            {
               PSExtensionProcessingException e =
                  new PSExtensionProcessingException(m_fullExtensionName,
                        except);
               // SQL Exceptions that occur here are serious. Make sure
               // to mark the exception being thrown so it isn't ignored
               // by PSActionSet
               if (except instanceof SQLException)
               {
                  Object args[] = new Object[] {
                        "CONTENTSTATUSHISTORY"
                  };
                  e.setArgs(IPSServerErrors.SQL_PROBLEM, args);
               }
               throw e;
            }
         }
         PSWorkFlowUtils.printWorkflowMessage(request,
         "Update History: exit processResultDocument");
      }

      return resDoc;
   }

   /**
    * Do the actual work of updating the content status history context.
    *
    * @param contentID           ID of the content item
    * @param baseRevisionNum     The base revision number for the content item
    *                            <ul><li>for transitions - current revision
    *             </li>
    *              <li>for checkin - revision being checked in
    *             </li>
    *              <li>for checkout - base revision for the item
    *             being checked out:  either 1, or the revision
    *             of the item copied to  create the revision
    *                       checked out</li></ul>
    * @param userName            the user performing the transition or action
    * @param sessionID           the SessionID
    * @param transitionID        the TransitionID for content status history
    *                            entry. 0 for a checkin or checkout
    * @param transitionComment   the descriptive comment for the transition
    * @param request              the context of the request associated with
    *                            this extension. (Used to output to trace log.)
    * @return                    data base ID for this content status history
    *                            entry.
    * @throws                    SQLException if an SQL exception is caught
    * @throws                    PSEntryNotFoundException an expected data base
    *                            entry does not exist
    * @throws                    PSExtensionProcessingException
    * @throws PSORMException 
    */
   private int updateHistory(int contentID,
                             int baseRevisionNum,
                             String userName,
                             String sessionID,
                             int transitionID,
                             List stateAssignedRoles,
                             String  transitionComment,
                             IPSRequestContext request,
                             Connection connection)
      throws SQLException, PSEntryNotFoundException,
      PSExtensionProcessingException
   {
      PSWorkFlowUtils.printWorkflowMessage(request,"  Entering updateHistory");
      Integer temp = null;
      int contentstatushistoryid =  0;
      PSContentStatusContext csc = null;
      int workflowID = 0;
      String stateAssignedRole = "None";
      PSTransitionsContext tc = null;
      IPSStatesContext sc = null;
      String lang = (String)request.getSessionPrivateObject(
      PSI18nUtils.USER_SESSION_OBJECT_SYS_LANG);
      if (lang == null)
         lang =   PSI18nUtils.DEFAULT_LANG;

      try
      {
         csc = new PSContentStatusContext(connection, contentID);
      }
      catch(SQLException e)
      {
          PSWorkFlowUtils.printWorkflowMessage(request,
             "  No entry for this content item so no update history.\n" +
             "Exiting updateHistory");
         return 0; //no entry for this content item so no update history
      }
      finally
      {
         if (csc != null)
            csc.close(); //release the JDBC resources
      }

      workflowID = csc.getWorkflowID();

      stateAssignedRole = "None";

      if (null != stateAssignedRoles && !stateAssignedRoles.isEmpty())
      {
          stateAssignedRole =
                PSWorkFlowUtils.listToDelimitedString(stateAssignedRoles, ";");
    
          // CMS-3472  - Limit the size of the rolename string to the size of the column in the database 
          if(stateAssignedRole.length()>=1024){
             stateAssignedRole = stateAssignedRole.substring(0, 1024);
          }
      }
      IPSCmsObjectMgr cms = PSCmsObjectMgrLocator.getObjectManager();
      sc = cms.loadWorkflowState(workflowID, csc.getContentStateID());


      // if it's not a checkin or checkout, get the transition context

      if (IPSConstants.TRANSITIONID_CHECKINOUT != transitionID)
      {
         try
         {
            tc = new PSTransitionsContext(transitionID, workflowID,
                                          connection);
            tc.close();
         }
         catch(PSEntryNotFoundException e)
         {
            String language = e.getLanguageString();
            if (language == null)
               e.setLanguageString(lang);
            throw new PSExtensionProcessingException(language,
             m_fullExtensionName, e);
         }
      }

      temp = PSExitNextNumber.getNextNumber("CONTENTSTATUSHISTORY");
      contentstatushistoryid = temp;

      try
      {
         PSContentStatusHistoryContext csh = new PSContentStatusHistoryContext(
               contentstatushistoryid, workflowID, connection, contentID, csc,
               sc, tc, transitionComment, userName, sessionID,
               stateAssignedRole, baseRevisionNum);
         updateLastPublicRevision(csh, sc, request, csc);
      }
      catch (Exception e)
      {
         PSWorkFlowUtils.printWorkflowException(request, e);
         throw new PSExtensionProcessingException(m_fullExtensionName, e);
      }
      PSWorkFlowUtils.printWorkflowMessage(request,"  Exiting updateHistory");
      return contentstatushistoryid;
   }
   
   /**
    * Updates the last public revision for the supplied item if needed.
    * 
    * @param csh the to be updated item, assumed not <code>null</code>.
    * @param sc the states context for the current state of the item, assumed 
    * not <code>null</code>.
    * @param request the request context, assumed not <code>null</code>.
    * 
    * @throws PSException if an error occurs.
    */
   private void updateLastPublicRevision(PSContentStatusHistoryContext csh, 
         IPSStatesContext sc, IPSRequestContext request, PSContentStatusContext csc) throws PSException
   {
      int revision;
      if (csh.getContentIsValid())
      {   
         if (! needToUpdatePublicRevision(csc))
            return;
         
         // if public, update to current revision
         revision = csh.getRevision();
      }
      else if (sc.getIsUnpublish())
      {
         // if unpublish, clear public revision
         revision = -1;
      }
      else
      {
         // nothing to update
         return;
      }
      
      Map<String, String> params = new HashMap<>();
      params.put(PSApplicationBuilder.REQUEST_TYPE_HTML_PARAMNAME,
            PSApplicationBuilder.REQUEST_TYPE_VALUE_UPDATE);

      Document doc = PSXmlDocumentBuilder.createXmlDocument();

      Element elem = doc.createElement("putLastPublicRev");
      elem.setAttribute("contentId", String.valueOf(csh.getContentID()));
      elem.setAttribute("lastPublicRevision", 
         String.valueOf(revision));
      doc.appendChild(elem);

      PSInternalRequest ir = PSServer.getInternalRequest(PUT_LASTPUBREV_RSC,
            request, params, false, doc);

      /**
       * If the internal request is <code>null</code> now, we did not find the
       * resource.
       */
      if (ir == null)
         throw new PSCmsException(IPSCmsErrors.REQUIRED_RESOURCE_MISSING,
               PUT_LASTPUBREV_RSC);

      ir.performUpdate();
   }
   
   private boolean needToUpdatePublicRevision(PSContentStatusContext csc)
   {
      Date startDate = csc.getContentStartDate();
      
      if (startDate == null)
         return true;
      
      Date currentDate = new Date();
      
      // don't update the (last) public revision
      // if the published time has not reach the scheduled time yet.
      return currentDate.getTime() >= startDate.getTime();
   }
   
   
   /**
    * The resource used to update the CONTENTSTATUS.LAST_PUBLIC_REVISION colum
    */
   private final static String PUT_LASTPUBREV_RSC = "sys_ceSupport/putLastPublicRev"; 
}
