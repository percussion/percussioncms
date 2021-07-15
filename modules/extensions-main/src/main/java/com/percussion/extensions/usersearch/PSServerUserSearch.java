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

package com.percussion.extensions.usersearch;
import com.percussion.data.IPSDataErrors;
import com.percussion.data.PSInternalRequestCallException;
import com.percussion.design.objectstore.PSSubject;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSResultDocumentProcessor;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.security.PSRoleManager;
import com.percussion.server.IPSInternalRequest;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.PSConsole;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.util.PSXMLDomUtil;
import com.percussion.workflow.PSWorkFlowUtils;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

 /**
  * This exit can modify the result document by adding search results for the
  * following cases:
  * 1. Given the HTML parameter sys_command=GetRoles, it produces the list of
  * server roles with the DTD
  * &lt;root&gt;&lt;role&gt;role1&lt;/role&gt;&lt;role&gt;role2&lt;/role&gt;&lt;/root&gt;.
  * 1.1. If the document element has an attribute "fromRoles", the roles added
  * shall be the intersection of the this list and the server roles. "fromRoles"
  * attribute must be a ';' separated list of roles.
  * 1.2. If the "fromRoles" attribute is empty or <code>null</code>, no filtering
  * is done, i.e. all the server roles are added to the result document.
  *
  * 2. Given the HTML parameters sys_command=GetUsers and sys_role=roleName, it
  * produces the list of users that are members of the role roleName with
  * the DTD
  * &lt;root&gt;&lt;role&gt;roleName&lt;user&gt;user1&lt;/user&gt;&lt;user&gt;role2&lt;/user&gt;&lt;/role&gt;&lt;/root&gt;.
  *
  * The element root indicates any Document Element of the result document.
  */
   public class PSServerUserSearch
      implements IPSResultDocumentProcessor
{
   /*
    * Implementation of the method defined by the interface
    */
   public Document processResultDocument(Object[] params,
      IPSRequestContext request, Document resultDoc)
      throws PSExtensionProcessingException
   {
      try
      {

         String command = request.getParameter(HTMLPARAM_COMMAND_NAME);
         String contentId = request.getParameter(IPSHtmlParameters.SYS_CONTENTID);
         if(command == null)
         {
            return resultDoc;
         }
         command = command.trim();
         Element roleElem = null;

         ArrayList fromRoleList = new ArrayList();
         ArrayList adHocTypeList = new ArrayList();
         String fromRoles =
            resultDoc.getDocumentElement().getAttribute(ATTR_FROM_ROLES);

         extractFromRolesParam(fromRoles, fromRoleList, adHocTypeList);

         boolean containsAnonymousAdhoc =
            adHocTypeList.contains(""+PSWorkFlowUtils.ADHOC_ANONYMOUS);

         boolean containsAdhocEnabled =
            adHocTypeList.contains(""+PSWorkFlowUtils.ADHOC_ENABLED);

         if(command.equals(HTMLPARAM_COMMAND_GETROLES))
         {

            List roleList = request.getRoles();
            String role = null;
            String adHocType = null;

            for(int i=0; roleList != null && i<roleList.size(); i++)
            {
               role = roleList.get(i).toString();
               /*
                * 1. Add all roles if fromRoles is empty
                * 2. If fromRoles is not empty, add only the intersection
                * 3. If adHocType contains PSWorkFlowUtils.ADHOC_ANONYMOUS
                *    add all roles.
                * 4. If adHocType contains PSWorkFlowUtils.ADHOC_ENABLED
                *    add only the roles that have PSWorkFlowUtils.ADHOC_ENABLED
                *    NOTE: 3 overrides 4.
                */
               if(!containsAnonymousAdhoc)
               {
                  if(!fromRoleList.isEmpty() && !fromRoleList.contains(role))
                     continue;

                  if(containsAdhocEnabled)
                  {
                     // get the adhoc type of the role:
                     adHocType = (String)adHocTypeList.get(
                        fromRoleList.indexOf(role));

                     if(!adHocType.equalsIgnoreCase(
                        ""+PSWorkFlowUtils.ADHOC_ENABLED))
                        continue;
                  }
               }
               roleElem = PSXmlDocumentBuilder.addElement(resultDoc,
                           resultDoc.getDocumentElement(),
                           ELEM_ROLE, null);
               roleElem.setAttribute(ATTR_NAME, role);
            }
         }
         else if(command.equals(HTMLPARAM_COMMAND_GETUSERS))
         {
            String role = request.getParameter(HTMLPARAM_COMMAND_ROLE);
            String filter = request.getParameter(HTMLPARAM_NAMEFILTER);

            // only instantiated when containsAnonymousAdhoc is false.
            List communityRoleList = null;
            PSRoleManager mgr = null;

            if(filter != null && filter.trim().length() < 1)
               filter = null;
            //The filter is expected to come without any SQL filter pattern
            if(filter != null)
               filter  = "%" + filter + "%";
            if(role != null && role.trim().length() > 0)
            {
               roleElem = PSXmlDocumentBuilder.addElement(
                  resultDoc, resultDoc.getDocumentElement(), ELEM_ROLE, null);
               roleElem.setAttribute(ATTR_NAME, role);
               List subjectList = request.getRoleSubjects(
                  role, PSSubject.SUBJECT_TYPE_USER, filter);

               // get the communityid from the item:
               if(!containsAnonymousAdhoc)
               {
                  int itemCom = getItemCommunity(request, contentId);
                  communityRoleList = retrieveRoleList(request, itemCom);
                  mgr = PSRoleManager.getInstance();

               }

               // once we have the subject list lets check to see if they're
               // a part of the same community as the item.
               for(int i=0; subjectList != null && i<subjectList.size(); i++)
               {
                  PSSubject subject = (PSSubject)subjectList.get(i);

                  // any role is anonymous adhoc return all users for
                  // for that role
                  if(containsAnonymousAdhoc)
                     PSXmlDocumentBuilder.addElement(resultDoc, roleElem,
                        ELEM_USER, subject.getName());
                  else
                  {
                     // else return only users that are in the same community
                     // as the item.
                     Iterator it = communityRoleList.iterator();
                     while(it.hasNext())
                     {
                        if(mgr.isMemberOfRole(subject.getName(),
                           (String)it.next()))
                        {
                           PSXmlDocumentBuilder.addElement(resultDoc, roleElem,
                              ELEM_USER, subject.getName());
                           break;
                        }
                     }
                  }
               }
            }
         }
      }
      catch(Exception e)
      {
         PSXmlDocumentBuilder.addElement(resultDoc,
                  resultDoc.getDocumentElement(),
                  "error", e.getMessage());
         PSConsole.printMsg("Exit:" + ms_fullExtensionName, e);
      }
      return resultDoc;
   }

   /**
    * This takes the from roles param out and adds them to the lists.
    *
    * @param fromRoles - the fromRoles param, assumed not <code>null</code> or
    * empty.
    * @param fromRoleList - the roles in the fromRoles param, assumed not
    * <code>null</code>
    * @param adHocTypeList - the ad hoc types in the fromRoles param, assumed
    * not <code>null</code>
    */
   private void extractFromRolesParam(String fromRoles, ArrayList fromRoleList,
      ArrayList adHocTypeList)
   {
      if(fromRoles.length() > 0)
      {
         StringTokenizer tokenizer = new StringTokenizer(fromRoles,
         PSWorkFlowUtils.ADHOC_USER_LIST_DELIMITER);

         String temp = null;
         while(tokenizer.hasMoreTokens())
         {
         temp = tokenizer.nextToken();
         fromRoleList.add(temp.substring( 0,
            temp.indexOf(PSWorkFlowUtils.ADHOC_USER_ROLE_TYPE_SEP)));

         adHocTypeList.add(temp.substring(
            temp.indexOf(
               PSWorkFlowUtils.ADHOC_USER_ROLE_TYPE_SEP) + 1,
               temp.length()));
         }
      }

   }

   /**
    * This method gets the communityid of the item that has as its contentid
    * <code>contentid</code>
    *
    * @param contentid
    * @return the community id of the item, if -1 item cannot be found.
    */
   private int getItemCommunity(IPSRequestContext request, String contentId)
      throws PSInternalRequestCallException
   {
      Map params = new HashMap();

      params.put(IPSHtmlParameters.SYS_CONTENTID, contentId);
      Document doc =
         makeRequest(CMS_LOOKUP_CONTENTSTATUS, request,params, true);

      PSXmlTreeWalker tree = new PSXmlTreeWalker(doc);
      Element el = tree.getNextElement("CommunityId");
      String ret = tree.getElementData(el);

      if (ret == null || ret.trim().length() == 0)
         throw new PSInternalRequestCallException(
            IPSDataErrors.INTERNAL_REQUEST_CALL_EXCEPTION,
            CMS_LOOKUP_CONTENTSTATUS);

      return Integer.parseInt(ret);
   }

   /**
    * Method takes the community and the request context to make a request to
    * the  COMMUNITY_ROLE_LOOKUP_URL app, it gets the document back then
    * returns the list of roles.
    *
    * @param contentid
    * @return
    */
   private List retrieveRoleList(IPSRequestContext request, int communityid)
      throws PSInternalRequestCallException
   {
      Map params = new HashMap();
      //TODO: this is not sys_community or sys_communityid, the app expects
      // something entirely different.  This should be fixed when time.

      params.put("communityid", new Integer(communityid));
      Document doc =
         makeRequest(COMMUNITY_ROLE_LOOKUP_URL, request,params, true);

      return extractRoleListFromDoc(doc);
   }

   /**
    * @todo: this should be replaced once the CMS objects for community are in
    * place.
    *
    * Given a document conforming to the dtd returned by a request to the
    * COMMUNITY_ROLE_LOOKUP_URL app.
    *
    * @param doc assumed not <code>null</code> and that it is properly
    * constrained.
    * @return a list of role ids.  Never <code>null</code> may be empty.
    */
   private List extractRoleListFromDoc(Document doc)
   {
      NodeList nl = doc.getElementsByTagName("name");
      List idList = new ArrayList();
      Node theNode = null;
      for(int i=0; i < nl.getLength(); i++)
      {
         theNode = nl.item(i);
         idList.add(PSXMLDomUtil.getElementData(theNode));
      }

      return idList;
   }

   /**
    * Called throughout this class to make internal requests.
    *
    * @param path   assumed not <code>null</code>
    * @param request   assumed not <code>null</code>
    * @param params   assumed not <code>null</code>
    * @param inherit
    * @return Document the result document, never <code>null</code>
    */
   private Document makeRequest(
      String path, IPSRequestContext request, Map params, boolean inherit)
   throws PSInternalRequestCallException
   {
      IPSInternalRequest iReq =
         request.getInternalRequest(path, params, inherit);

      if (iReq == null)
         throw new PSInternalRequestCallException(
            IPSDataErrors.INTERNAL_REQUEST_CALL_EXCEPTION,
            COMMUNITY_ROLE_LOOKUP_URL);

      Document doc = null;

      // never null:
      doc = iReq.getResultDoc();

      return doc;
   }

   /*
    * Implementation of the method defined by the interface
    */
   public void init(IPSExtensionDef extensionDef, File file)
      throws PSExtensionException
   {
      ms_fullExtensionName = extensionDef.getRef().toString();
   }

   /*
    * Implementation of the method defined by the interface
    */
   public boolean canModifyStyleSheet()
   {
      return false;
   }

   /**
    * The fully qualified name of this extension.
    */
   static private String ms_fullExtensionName = "";

   /**
    * Name of the html parameter for sys_command
    */
   static private String HTMLPARAM_COMMAND_NAME = "sys_command";

   /**
    * Name of the html parameter for sys_command=GetRoles
    */
   static private String HTMLPARAM_COMMAND_GETROLES = "GetRoles";

   /**
    * Name of the html parameter for sys_command=GetUsers
    */
   static private String HTMLPARAM_COMMAND_GETUSERS = "GetUsers";

   /**
    * Name of the html parameter specifying the role to get members of
    */
   static private String HTMLPARAM_COMMAND_ROLE = "sys_role";

   /**
    * Name of the html parameter specifying the name filter to get the users
    */
   static private String HTMLPARAM_NAMEFILTER = "namefilter";

   /**
    * String constnt for the element 'role'
    */
   static private String ELEM_ROLE = "role";

   /**
    * String constnt for the element 'user'
    */
   static private String ELEM_USER = "user";

   /**
    * String constnt for the attribute 'name'
    */
   static private String ATTR_NAME = "name";

   /**
    * String constnt for the attribute 'fromRoles'
    */
   static private String ATTR_FROM_ROLES = "fromRoles";

   /**
    * This is the url that is used in the request to return a doc with roles
    * for each community.
    */
   static private String COMMUNITY_ROLE_LOOKUP_URL =
      "sys_commSupport/rolelookup.xml";

   /**
    * Name of the content type id lookup resource.
    */
   private static final String CMS_LOOKUP_CONTENTSTATUS =
      "sys_psxCms/contentStatus.xml";

}

