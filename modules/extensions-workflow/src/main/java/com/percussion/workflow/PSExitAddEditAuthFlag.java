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
import com.percussion.util.IPSHtmlParameters;
import com.percussion.util.PSCms;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;



/**
 * This exit is used to enforce workflow security when we are in active
 * assembly. This exit determines if the user has the authority to
 * edit the current active item and its' parent item. Two flags will be
 * added to the activeitem element "editauthorized" and
 * "parenteditauthorized". The "editauthorized" flag, if set to "yes" means
 * the user can edit the activeitem. The "parenteditauthorized" flag, if set to
 * "yes" means the user can edit the parent item.
 * This exit is only meant to be used on the "sys_rcSupport/activeitem.xml"
 * resource.
 */
public class PSExitAddEditAuthFlag implements
              IPSResultDocumentProcessor
{

    private static final Logger log = LogManager.getLogger(PSExitAddEditAuthFlag.class);

   /*
    * Implementation of the method required by the interface IPSExtension.
    */
   @SuppressWarnings("unused")
   public void init(IPSExtensionDef extensionDef, File file)
      throws PSExtensionException
   {
      ms_fullExtensionName = extensionDef.getRef().toString();
   }

   /*
    * Implementation of the method required by the interface
    * IPSResultDocumentProcessor.
    */
   public boolean canModifyStyleSheet()
   {
      return false;
   }

   /*
    * Implementation of the method required by the interface
    * IPSResultDocumentProcessor.
    */
   @SuppressWarnings({"unused","deprecation"})
   public Document processResultDocument(Object[] params,
      IPSRequestContext request, Document resDoc)
         throws PSParameterMismatchException,
               PSExtensionProcessingException
   {

      // If no result document then do nothing
      if(null == resDoc)
        return resDoc;

      // Get language
      String lang = (String)request.getSessionPrivateObject(
       PSI18nUtils.USER_SESSION_OBJECT_SYS_LANG);
      if (lang == null)
         lang =   PSI18nUtils.DEFAULT_LANG;

      // Check for existance of request object
      if(null == request)
      {
         throw new PSExtensionProcessingException(
           ms_fullExtensionName,
           new IllegalArgumentException("The request must not be null"));
      }

      // Create the parameter container object
      AuthParams localParams = new AuthParams();
      localParams.m_request = request;

      PSConnectionMgr connectionMgr = null;
      Element activeItemElem = null;

      try
      {

        activeItemElem = resDoc.getDocumentElement();
        // Set content id  and revision for the active item. We get this value
        // from the result documents contentid attribute.
        try
        {
        localParams.m_contentID =
          Integer.parseInt(
            activeItemElem.getAttribute(XML_ATTRIB_CONTENTID));
        localParams.m_revision =
          Integer.parseInt(
            activeItemElem.getAttribute(XML_ATTRIB_REVISION));
        }
        catch(NumberFormatException nfe)
        {
          return resDoc;
        }

        // Set username
        if(null == params[0] ||
           params[0].toString().trim().length() == 0)
        {
               throw new PSInvalidParameterTypeException(lang,
                IPSExtensionErrors.EMPTY_USRNAME1);
        }
        localParams.m_userName = params[0].toString();
        localParams.m_userName = PSWorkFlowUtils.filterUserName(
            localParams.m_userName);
        if( 0 == localParams.m_userName.length())
        {
            throw new PSInvalidParameterTypeException(lang,
               IPSExtensionErrors.EMPTY_USRNAME2);
        }

        // Set RoleNameList
        if(null == params[1] ||
           params[1].toString().trim().length() == 0)
        {
            throw new PSInvalidParameterTypeException(lang,
               IPSExtensionErrors.EMPTY_ROLE_LIST, localParams.m_userName);
        }
        localParams.m_roleNameList = params[1].toString();

         Connection connection = null;
         //Get the connection
         try
         {
            connectionMgr = new PSConnectionMgr();
            connection = connectionMgr.getConnection();
         }
         catch(SQLException e)
         {
            throw new PSExtensionProcessingException(
               ms_fullExtensionName, e);
         }

         try
         {
            boolean canEdit = PSCms.canReadInFolders( 
               localParams.m_contentID) && canUserEditContent(connection, 
                  localParams); 
            String strCanEdit = canEdit ? "yes" : "no";
            // Set the edit authorization flag attribute
            activeItemElem.setAttribute(XML_ATTRIB_EDIT_AUTH, strCanEdit);

           // Set content id for the parent item. We get this value
           // from the result documents contentid attribute.
           try
           {
              localParams.m_contentID =
              Integer.parseInt(
              activeItemElem.getAttribute(XML_ATTRIB_PARENTCONTENTID));
           }
           catch(NumberFormatException nfe)
           {
              return resDoc;
           }
           // Set the edit authorization flag attribute for parent
           strCanEdit =
              canUserEditContent(connection, localParams) ? "yes" : "no";
           activeItemElem.setAttribute(XML_ATTRIB_PARENT_EDIT_AUTH, strCanEdit);


         }
         catch(Exception e)
         {
            PSWorkFlowUtils.printWorkflowException(request, e);
            throw new PSExtensionProcessingException(ms_fullExtensionName, e);
         }
      }
      catch(Throwable t)
      {
        System.err.println(t.getMessage());
        log.error(t.getMessage());
        log.debug(t.getMessage(), t);
      }
      finally
      {
         try
         {
            if (null != connectionMgr)
               connectionMgr.releaseConnection();
         }
         catch(SQLException sqe)
         {
            // Ignore since this is cleanup
         }
         PSWorkFlowUtils.printWorkflowMessage(request,
            "Exiting PSExitAddEditAuthFlag....");
      }

      return resDoc;
   }

   /**
    * This method verifies if the current user is allowed to
    * edit the specified content item. It uses the same authorization
    * checks used in PSExitAuthenticateUser.
    * @param connection JDBC connection passed in
    * @param localParams object containing parameters that will
    * be used to authorize the user.
    * @return <code>true</code> if the user is allowed to edit this content
    * item, else <code>false</code>.
    * @throws SQLException if there is an error with the SQL query.
    * @throws PSRoleException role not found
    * @throws Exception catches all other exceptions
    */
   private boolean canUserEditContent(Connection connection,
                                 AuthParams localParams)
      throws   SQLException, PSRoleException, Exception
   {
      PSWorkFlowUtils.printWorkflowMessage(localParams.m_request,
        "  Entering canUserEditContent");

      PSContentStatusContext csc = null;
      int contentID = localParams.m_contentID;
      String userName = localParams.m_userName;
      String roleNameList = localParams.m_roleNameList;
      int requiredAccessLevel = localParams.m_requiredAccessLevel;
      int assignmentType = localParams.m_assignmentType;
      List actorRoles = null;

      try
      {
         csc = new PSContentStatusContext(connection, contentID);
      }
      catch(PSEntryNotFoundException e)
      {
          PSWorkFlowUtils.printWorkflowMessage(localParams.m_request,
             "  No entry for this content. Exiting canUserEditContent");
         return true; //no entry for this content so proceed to transition
      }
      csc.close(); //release the JDBC resources

      int nWorkFlowAppID = csc.getWorkflowID();
      int itemCommunityID = csc.getCommunityID();
      int userCommunityID = -1;
      String usercomm = (String)localParams.m_request.getSessionPrivateObject(
         IPSHtmlParameters.SYS_COMMUNITY);
      if(usercomm != null)
         userCommunityID = Integer.parseInt(usercomm);

      IPSWorkflowAppsContext wac = null;
      IPSCmsObjectMgr cms = PSCmsObjectMgrLocator.getObjectManager();
      wac = cms.loadWorkflowAppContext(nWorkFlowAppID);
      String sAdminName = wac.getWorkFlowAdministrator();

      //if the login community and user community are different then return
      //false
      if(itemCommunityID != userCommunityID)
         return false;

      // Check whether the user is Workflow admin
     boolean isAdmin = false;

      if (PSWorkFlowUtils.isAdmin(sAdminName, userName, roleNameList))
      {
         isAdmin = true;
      }
      // Determine the checkout status and checkedout user
      // and return false if the content is checked out
      // by another user or not checked out.

      String checkedOutUser = csc.getContentCheckedOutUserName();
      if (null == checkedOutUser || checkedOutUser.trim().length() < 1)
      {
          // content item not checked out
          return false;
      }
      else
      {
         checkedOutUser = checkedOutUser.trim();
         if (!userName.equalsIgnoreCase(checkedOutUser))
         {
            // content item checked out, but not by you
            return false;
         }

      }

      //If the user is Workflow admin, there is no more to do
      if (isAdmin)
      {
         PSWorkFlowUtils.printWorkflowMessage(localParams.m_request,
             "  User is Admin, done. \n  Exiting canUserEditContent");
         return true;
      }



      PSStateRolesContext src = null;

      try
      {
         src = new PSStateRolesContext(nWorkFlowAppID,
                                       connection,
                                       csc.getContentStateID(),
                                       requiredAccessLevel);
      }

      catch(PSRoleException e)
      {
         return false;
      }

      catch (PSEntryNotFoundException e)
      {
         return false;
      }

      actorRoles = PSWorkflowRoleInfoStatic.getActorRoles(contentID,
                                                    src,
                                                    userName,
                                                    roleNameList,
                                                    connection,
                                                    true);

      if (null == actorRoles || actorRoles.isEmpty())
      {
        return false;
      }
      assignmentType = PSWorkflowRoleInfoStatic.getAssignmentType(src, actorRoles);
      if (PSWorkFlowUtils.ASSIGNMENT_TYPE_NONE == assignmentType)
      {
        return false;
      }

      PSWorkFlowUtils.printWorkflowMessage(localParams.m_request,
                                           "  Exiting canUserEditContent");
      return true;
   }


    /**
    * This is an inner class to encapsulate the parameters. We cannot keep
    * these as class variables due to threading issues. We instantiate this
    * object in the main processrequest method (called by server) and pass
    * around the methods. This is meant for convenience only.
    */
   private class AuthParams
   {
      /** The content id of the active item */
      public int     m_contentID = 0;
      /** The revision number of the active item */
      public int     m_revision = 0;
      /** The current users' username */
      public String  m_userName = null;
      /** The list of roles this user is in */
      public String  m_roleNameList = null;
      /** The access level required to edit content */
      public int     m_requiredAccessLevel =
                       PSWorkFlowUtils.ASSIGNMENT_TYPE_ASSIGNEE;
      /** The assignment type for this content */
      public int     m_assignmentType =
                       PSWorkFlowUtils.ASSIGNMENT_TYPE_NOT_IN_WORKFLOW;
      /** The request context passed in */
      public IPSRequestContext m_request = null;
   }

   /**
    * The fully qualified name of this extension.
    */
   private static String ms_fullExtensionName = "";

   /**
    * The content id XML attribute.
    */
   private static final String XML_ATTRIB_CONTENTID = "contentid";

   /**
    * The revision id XML attribute.
    */
   private static final String XML_ATTRIB_REVISION = "revision";

   /**
    * The parent content id XML attribute.
    */
   private static final String XML_ATTRIB_PARENTCONTENTID = "parentcontentid";

   /**
    * The edit authorization XML attribute.
    */
   private static final String XML_ATTRIB_EDIT_AUTH = "editauthorized";

   /**
    * The edit authorization XML attribute.
    */
   private static final String XML_ATTRIB_PARENT_EDIT_AUTH =
      "parenteditauthorized";





}
