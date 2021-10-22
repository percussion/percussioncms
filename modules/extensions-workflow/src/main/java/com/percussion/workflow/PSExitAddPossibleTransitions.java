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

package com.percussion.workflow;

import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSExtensionErrors;
import com.percussion.extension.IPSResultDocumentProcessor;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.i18n.PSI18nUtils;
import com.percussion.server.IPSRequestContext;
import com.percussion.services.legacy.IPSCmsObjectMgr;
import com.percussion.services.legacy.PSCmsObjectMgrLocator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class PSExitAddPossibleTransitions implements IPSResultDocumentProcessor
{
   /**
    * This is an inner class to encapsulate the parameters. We cannot keep
    * these  as class variables due to threading issues. We instantiate this
    * object in the main processrequest method (called by server) and pass
    * around the  methods. This is meant for convenience only.
    */
   private class Params
   {
      public Connection m_connection = null;
      public String m_userName = null;
      public String m_checkoutUserName = null;
      public String m_statusDocElementName = null;
      public String m_contentIDNodeName = null;
      public String m_contentIDName = null;
      public String m_roleNameList = "";
   }

   /**
    * The fully qualified name of this extension.
    */
   static private String m_fullExtensionName = "";

   static private String ms_actionElementName = null;
   static private String ms_actionListElementName = null;
   static private String ms_actionTriggerName = "";

   public Document processResultDocument(Object[] params,
      IPSRequestContext request, Document resDoc)
      throws PSParameterMismatchException, PSExtensionProcessingException
   {
      PSConnectionMgr connectionMgr = null;
      try
      {
         if(null == request)
         {
            throw new PSExtensionProcessingException(
               m_fullExtensionName,
               new IllegalArgumentException("The request must not be null"));
         }
         String lang = (String)request.getSessionPrivateObject(
          PSI18nUtils.USER_SESSION_OBJECT_SYS_LANG);
         if (lang == null)
            lang =   PSI18nUtils.DEFAULT_LANG;

         if(null == params)
            return resDoc; //no parameters exit with peace!

         int nParamCount = params.length;

         Params localParams = new Params();

         try
         {
            if(ms_correctParamCount != nParamCount)
            {
               String[] exParams =
               {new Integer(ms_correctParamCount).toString(),
                String.valueOf(nParamCount)};
               throw new PSInvalidNumberOfParametersException(lang,
                IPSExtensionErrors.INVALID_PARAM_NUM, exParams);
            }

            if(null == params[0] || 0 == params[0].toString().trim().length())
            {
               return resDoc;
               /*
                * Request by James Shultz. There may be situations with user
                * name empty and still user gets the result document, of
                * course without any workflow info.
                */
//               throw new PSInvalidParameterTypeException(new String(
//                  "The user name must not be empty"));
            }

            localParams.m_userName = params[0].toString();
            localParams.m_userName = PSWorkFlowUtils.filterUserName(
               localParams.m_userName);

            if(null == params[1] ||
               0 == params[1].toString().trim().length())
            {
               throw new PSInvalidParameterTypeException(lang,
                IPSExtensionErrors.STATUS_DOC_EMPTY);
            }
            localParams.m_statusDocElementName = params[1].toString();

            if(null == params[2] ||
               0 == params[2].toString().trim().length())
            {
                  throw new PSInvalidParameterTypeException(lang,
                   IPSExtensionErrors.CONTENTID_NODENAME_EMPTY);
            }
            localParams.m_contentIDNodeName = params[2].toString();
         }
         catch(PSInvalidNumberOfParametersException | PSInvalidParameterTypeException ne)
         {
            String language = ne.getLanguageString();
            if (language == null)
               language = PSI18nUtils.DEFAULT_LANG;
            throw new PSExtensionProcessingException(language,
             m_fullExtensionName, ne);
         }

         try
         {
            localParams.m_roleNameList = request.getUserContextInformation(
               "Roles/RoleName", "").toString();
         }
         catch(Exception e)
         {
            localParams.m_roleNameList = "";
         }

         //Get the connection
         try
         {
            connectionMgr = new PSConnectionMgr();
            localParams.m_connection = connectionMgr.getConnection();
         }
         catch(Exception e)
         {
            throw new PSExtensionProcessingException(m_fullExtensionName, e);
         }

         Element element = null;
         NodeList nodes = resDoc.getElementsByTagName(
            localParams.m_statusDocElementName);

         for(int i=0; i<nodes.getLength(); i++)
         {
            element = (Element)nodes.item(i);
            try
            {
               addWorkflowInfo(resDoc, element, localParams, lang, request);
            }
            catch(PSXMLNodeMissingException xe)
            {
               request.printTraceMessage(xe.getMessage());
            }
            catch(SQLException se)
            {
               request.printTraceMessage(se.getMessage());
            }
            catch (PSRoleException e)
            {
               request.printTraceMessage(e.getMessage());
            }
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
         }
      }
      return resDoc;
   }

   private void addWorkflowInfo(Document doc,
                                Element elemParent,
                                Params localParams, String lang, 
                                IPSRequestContext req)
      throws SQLException, PSXMLNodeMissingException, PSRoleException
   {
      int contentID = 0;
      localParams.m_contentIDName = null;
      String sContentID = null;

      if(localParams.m_contentIDNodeName.startsWith("@"))
      {
         localParams.m_contentIDName =
               localParams.m_contentIDNodeName.substring(1);

         sContentID = elemParent.getAttribute(localParams.m_contentIDName);
         if(null != sContentID)
            sContentID = sContentID.trim();
         if(null == sContentID || sContentID.length() < 1)
            throw new PSXMLNodeMissingException(lang,
             IPSExtensionErrors.CONTENTID_NODE_MISSING_EMPTY);
         contentID = Integer.parseInt(sContentID);
      }
      else
      {
         NodeList nodes = elemParent.getElementsByTagName(
            localParams.m_contentIDNodeName);

         if(null == nodes || nodes.getLength() < 1)
            throw new PSXMLNodeMissingException(lang,
             IPSExtensionErrors.CONTENTID_NODE_MISSING_EMPTY);
         Element elem = (Element)nodes.item(0);
         localParams.m_contentIDName = elem.getNodeName();
         sContentID = ((Text)(elem.getFirstChild())).getData();
         if(null != sContentID)
            sContentID = sContentID.trim();
         if(null == sContentID || sContentID.length() < 1)
            throw new PSXMLNodeMissingException(lang,
             IPSExtensionErrors.CONTENTID_NODE_MISSING_EMPTY);
         contentID = Integer.parseInt(sContentID);
      }

      if(contentID<1)
         throw new PSXMLNodeMissingException(lang,
          IPSExtensionErrors.CONTENTID_NODE_MISSING);

      sContentID = new Integer(contentID).toString();

      IPSWorkflowAppsContext wac = null;
      PSContentStatusContext csc = null;
      int nWorkFlowAppID = -1;
      IPSCmsObjectMgr cms = PSCmsObjectMgrLocator.getObjectManager();
      try
      {
         csc = new PSContentStatusContext(localParams.m_connection, contentID);
         nWorkFlowAppID = csc.getWorkflowID();
         csc.close();
         
         wac = cms.loadWorkflowAppContext(nWorkFlowAppID);
      }
      catch(PSEntryNotFoundException e)
      {
         return;
      }
      csc.close(); //release the JDBC resources

//Add workflow info element and set required attributes
      Element elemWorkflowInfo = doc.createElement(ELEMENT_WORKFLOWINFO);
      elemWorkflowInfo = (Element)elemParent.appendChild(elemWorkflowInfo);
      elemWorkflowInfo.setAttribute(this.ATTRIB_CONTENTID, sContentID);
      elemWorkflowInfo.setAttribute(this.ATTRIB_WORKFLOWID,
                                    Integer.toString(nWorkFlowAppID));

      elemWorkflowInfo.setAttribute(this.ATTRIB_WORKFLOWNAME,
                                    wac.getWorkFlowAppName());
//
//Add state information element and attributes
      Element elemState = doc.createElement(this.ELEMENT_CURRENTSTATE);
      elemState = (Element)elemWorkflowInfo.appendChild(elemState);
      elemState.setAttribute(this.ATTRIB_STATEID, Integer.toString(
         csc.getContentStateID()));

      IPSStatesContext sc;
      sc = cms.loadWorkflowState(nWorkFlowAppID, csc.getContentStateID());

      String sPublishable = (sc.getIsValid()) ? "Y" : "N";
      elemState.setAttribute(this.ATTRIB_PUBLISHABLE, sPublishable);

      Text text = doc.createTextNode(sc.getStateName());
      elemState.appendChild(text);
//

//Add checkout status information element and attributes
      localParams.m_checkoutUserName = csc.getContentCheckedOutUserName();
      if(null == localParams.m_checkoutUserName)
         localParams.m_checkoutUserName = "";
      else
         localParams.m_checkoutUserName =
               localParams.m_checkoutUserName.trim();

      Element elemCheckoutStatus = doc.createElement(
         this.ELEMENT_CHECKOUTSTATUS);

      elemCheckoutStatus = (Element)elemWorkflowInfo.appendChild(
         elemCheckoutStatus);

      elemCheckoutStatus.setAttribute(this.ATTRIB_CHECKOUTUSERNAME,
                                      localParams.m_checkoutUserName);

      int nCheckoutStatus = PSWorkFlowUtils.CHECKOUT_STATUS_NONE;
      if(null == localParams.m_checkoutUserName ||
         localParams.m_checkoutUserName.length() < 1)
      {
         //Default above
      }
      else if(localParams.m_checkoutUserName.trim().
              equalsIgnoreCase(localParams.m_userName))
      {
         nCheckoutStatus = PSWorkFlowUtils.CHECKOUT_STATUS_CURRENT_USER;
      }
      else
      {
         nCheckoutStatus = PSWorkFlowUtils.CHECKOUT_STATUS_OTHER;
      }
      text = doc.createTextNode(Integer.toString(nCheckoutStatus));
      elemCheckoutStatus.appendChild(text);

//

//Add user info element and attributes
      Element elemUserName = doc.createElement(this.ELEMENT_USERNAME);
      elemUserName = (Element)elemWorkflowInfo.appendChild(elemUserName);

      int nAssignmentType =
            PSExitAddPossibleTransitionsEx.getAssignmentType(
               nWorkFlowAppID,
               contentID,
               localParams.m_connection,
               csc.getContentStateID(),
               localParams.m_userName,
               localParams.m_roleNameList,
               req);

      elemUserName.setAttribute(this.ATTRIB_ASSIGNMENTTYPE,
                                Integer.toString(nAssignmentType));

      text = doc.createTextNode(localParams.m_userName);
      elemUserName.appendChild(text);

//

//Add assigned roles information
      addAssignedRolesInfo(doc, elemWorkflowInfo, nWorkFlowAppID,
                           csc.getContentStateID(), localParams.m_connection);
//

//Add action list
      addActions(doc, elemWorkflowInfo, contentID, csc, localParams);
//
   }

   private void addAssignedRolesInfo(Document doc,
                                     Element elemParent,
                                     int nWorkflowAppID,
                                     int stateid,
                                     Connection connection)
      throws SQLException, PSRoleException
   {
      Element elemAssignedRoles =
            doc.createElement(this.ELEMENT_ASSIGNEDROLES);
      elemAssignedRoles = (Element)elemParent.appendChild(elemAssignedRoles);
      Element elemAssignedRole = null;

      PSStateRolesContext src = null;

      try
      {
         src = new PSStateRolesContext(nWorkflowAppID,
                                       connection,
                                       stateid,
                                       PSWorkFlowUtils.ASSIGNMENT_TYPE_NONE);
      }
       // No info is added if the context does not exist
      catch(PSEntryNotFoundException enfe)
      {
         return;
      }

      if ( null == src )
      {
         return;
      }

      if(!src.isEmpty())
      {
         Text text = null;
         Map stateRoleNameMap = src.getStateRoleNameMap();
         Map assignmentTypeMap = src.getStateRoleAssignmentTypeMap();
         List stateRoleIDs = src.getStateRoleIDs();
         Iterator iter = stateRoleIDs.iterator();
         Integer roleID = null;
         Integer assignmentType = null;
         String roleName = null;

         while (iter.hasNext())
         {
            roleID = (Integer) iter.next();
            roleName = (String) stateRoleNameMap.get(roleID);
            assignmentType = (Integer)assignmentTypeMap.get(roleID);
            elemAssignedRole = doc.createElement(ELEMENT_ASSIGNEDROLE);

            elemAssignedRole.setAttribute(ATTRIB_ASSIGNMENTTYPE,
                                          assignmentType.toString());

            elemAssignedRole.setAttribute(ATTRIB_ROLEID, roleID.toString());

            text = doc.createTextNode(roleName);
            elemAssignedRole.appendChild(text);

            elemAssignedRoles.appendChild(elemAssignedRole);

         }
      }
   }

   private void addActions(Document doc, Element elemParent, int contentID,
                           PSContentStatusContext csc, Params localParams)
      throws SQLException
   {
      String sContentID = new Integer(contentID).toString();

      PSContentTypesContext ctc = null;
      String sUpdateRequest = null;
      String sQueryRequest = null;
      try
      {
         ctc = new PSContentTypesContext(localParams.m_connection,
                                         csc.getContentTypeID());
         sUpdateRequest = ctc.getContentTypeUpdateRequest();
         sQueryRequest = ctc.getContentTypeQueryRequest();
         ctc.close();
      }
      catch(PSEntryNotFoundException e)
      {
      }

      Element elemActionList = doc.createElement(ms_actionListElementName);
      elemActionList = (Element) elemParent.appendChild(elemActionList);

      Element elemAction = null;
      Text text = null;
      String sRequestName = PSWorkFlowUtils.properties.getProperty(
         PSWorkFlowUtils.REQUEST_NAME, PSWorkFlowUtils.DEFAULT_REQUEST_NAME);

      String sParamSeparator = "?";

      if(null!=sQueryRequest)
      {
         elemAction = doc.createElement(
            PSWorkFlowUtils.VIEW_ACTION_ELEMENT_NAME);
         elemAction.setAttribute(ms_actionTriggerName, "view");
         sParamSeparator = (-1 == sQueryRequest.indexOf("?")) ? "?" : "&";

         elemAction.setAttribute(sRequestName,
                                 sQueryRequest + sParamSeparator +
                                 localParams.m_contentIDName + "=" +
                                 sContentID);

         elemAction.setAttribute(localParams.m_contentIDName, sContentID);
         text = doc.createTextNode("View");
         elemAction.appendChild(text);
         elemActionList.appendChild(elemAction);
      }
      if(null!=sUpdateRequest)
      {
         elemAction = doc.createElement(
            PSWorkFlowUtils.EDIT_ACTION_ELEMENT_NAME);
         elemAction.setAttribute(ms_actionTriggerName, "edit");
         sParamSeparator = (-1 == sUpdateRequest.indexOf("?")) ? "?" : "&";

         elemAction.setAttribute(sRequestName,
                                 sUpdateRequest + sParamSeparator +
                                 localParams.m_contentIDName + "=" +
                                 sContentID);

         elemAction.setAttribute(localParams.m_contentIDName, sContentID);
         text = doc.createTextNode("Edit");
         elemAction.appendChild(text);
         elemActionList.appendChild(elemAction);
      }

      boolean bCheckedOut = ((null != localParams.m_checkoutUserName) &&
                             (localParams.m_checkoutUserName.length() > 0));

      if(bCheckedOut)
      {
         elemAction = doc.createElement(
            PSWorkFlowUtils.CHECKINOUT_ACTION_ELEMENT_NAME);

         elemAction.setAttribute(ms_actionTriggerName,
                                 PSWorkFlowUtils.properties.getProperty(
                                    PSWorkFlowUtils.TRIGGER_CHECK_IN));

         elemAction.setAttribute(localParams.m_contentIDName, sContentID);
         text = doc.createTextNode("Check-In");
         elemAction.appendChild(text);
         elemActionList.appendChild(elemAction);
      }
      else
      {
         elemAction = doc.createElement(
            PSWorkFlowUtils.CHECKINOUT_ACTION_ELEMENT_NAME);

         elemAction.setAttribute(ms_actionTriggerName,
                                 PSWorkFlowUtils.properties.getProperty(
                                    PSWorkFlowUtils.TRIGGER_CHECK_OUT));

         elemAction.setAttribute(localParams.m_contentIDName, sContentID);
         text = doc.createTextNode("Check-Out");
         elemAction.appendChild(text);
         elemActionList.appendChild(elemAction);
      }

      PSTransitionsContext tc = null;

      try
      {
         tc = new PSTransitionsContext(csc.getWorkflowID(),
                                       localParams.m_connection,
                                       csc.getContentStateID());
      }
      catch(PSEntryNotFoundException e)
      {
         return;
      }

      while(true)
      {
         // Don't show buttons for aging transitions
         if (!tc.isAgingTransition())
         {
            elemAction = doc.createElement(ms_actionElementName);
            elemAction.setAttribute(ms_actionTriggerName,
                                    tc.getTransitionActionTrigger());

            elemAction.setAttribute(localParams.m_contentIDName, sContentID);
            elemAction.setAttribute(this.ATTRIB_TRANSITIONID,
                                    Integer.toString(tc.getTransitionID()));

            text = doc.createTextNode(tc.getTransitionLabel());
            elemAction.appendChild(text);
            elemActionList.appendChild(elemAction);
         }

         if(false == tc.moveNext())
            break;
      }
      tc.close();

      return;
   }

   public boolean canModifyStyleSheet()
   {
      return true;
   }

   /**
    * This is used as a flag to indicate that the class hasn't been init'd
    * yet. There are certain cases where init can be called more than once
    * on the same loaded instance of a class.
    */
   static private final int UNINITIALIZED = -1;
   static private int ms_correctParamCount = UNINITIALIZED;

   public void init(IPSExtensionDef extensionDef, 
         @SuppressWarnings("unused") File file)
      throws PSExtensionException
   {
      if ( UNINITIALIZED != ms_correctParamCount )
         return;

      ms_correctParamCount = 0;
      ms_actionTriggerName = PSWorkFlowUtils.properties.getProperty(
         PSWorkFlowUtils.ACTION_TRIGGER_NAME,
         PSWorkFlowUtils.DEFAULT_ACTION_TRIGGER_NAME);

      ms_actionListElementName = PSWorkFlowUtils.properties.getProperty(
         PSWorkFlowUtils.ACTION_LIST_ELEMENT_NAME,
         PSWorkFlowUtils.DEFAULT_ACTION_LIST_ELEMENT_NAME);

      ms_actionElementName = PSWorkFlowUtils.properties.getProperty(
         PSWorkFlowUtils.ACTION_ELEMENT_NAME,
         PSWorkFlowUtils.DEFAULT_ACTION_ELEMENT_NAME);

      Iterator iter = extensionDef.getRuntimeParameterNames();
      while(iter.hasNext())
      {
         iter.next();
         ms_correctParamCount++;
      }
      m_fullExtensionName = extensionDef.getRef().toString();
   }

/**
 * Element and attribute names for the workflow information node.
 */
   static public final String ELEMENT_WORKFLOWINFO = "workflowinfo";
   static public final String ATTRIB_CONTENTID = "contentid";
   static public final String ATTRIB_WORKFLOWID = "workflowid";
   static public final String ATTRIB_WORKFLOWNAME = "workflowname";

   static public final String ATTRIB_TRANSITIONID = "transitionid";

   static public final String ELEMENT_USERNAME = "username";
   static public final String ATTRIB_ASSIGNMENTTYPE = "assignmenttype";

   static public final String ELEMENT_CURRENTSTATE = "currentstate";
   static public final String ATTRIB_STATEID = "stateid";
   static public final String ATTRIB_PUBLISHABLE = "publishable";

   static public final String ELEMENT_CHECKOUTSTATUS = "checkoutstatus";
   static public final String ATTRIB_CHECKOUTUSERNAME = "checkoutusername";

   static public final String ELEMENT_ASSIGNEDROLES = "assignedroles";
   static public final String ELEMENT_ASSIGNEDROLE = "assignedrole";
   static public final String ATTRIB_ROLEID = "roleid";
}
