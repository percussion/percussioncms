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
package com.percussion.community;

import com.percussion.design.objectstore.PSAttribute;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSRequestPreProcessor;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.server.IPSInternalRequest;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.PSConsole;
import com.percussion.server.PSServer;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * This exit authenticates the current user by means of his role-community
 * membership. The following is the list of things that happen:
 * <P>
 * <UL>
 * <LI>If the communities feature is disabled (by setting communities_enabled=no
 * in Server.properties file, the passes the authentication and the user's
 * communityid is set to 0 and stored as user's session object. Otherwise, </LI>
 * <LI>User community is obtained form the session assuming it stored
 * previously</LI>
 * <LI>If does not exist in user's session, tries to recover it from Cookies
 * assuming that the session object is lost because of server session timeout</LI>
 * <LI>Authentication fails if user has no community after the above step.</LI>
 * <LI>Exit now gets a list of user's role-communities by making an internal
 * request</LI>
 * <LI>if the list contains the user community, user authentication succeeds</LI>
 * <LI>Authentications fails, otherwise</LI>
 * <LI>If authentication is successful, the user community is stored as session
 * object to make sure it is available to all Rhythmyx applications for further
 * use</LI>
 * </UL>
 * </P
 */
public class PSAuthenticateUser implements IPSRequestPreProcessor
{
   /*
    * Implementation of the interface method
    */
   public void init(IPSExtensionDef extensionDef, File file)
      throws PSExtensionException
   {
      ms_fullExtensionName = extensionDef.getRef().toString();
   }

   /*
    * Implementation of the interface method
    */
   public void preProcessRequest(Object[] params, IPSRequestContext request)
      throws PSExtensionProcessingException
   {
      try 
      {
         PSServer.verifyCommunity(request);
      }
      catch(Exception e)
      {
         PSConsole.printMsg(ms_fullExtensionName, e);
         throw new PSExtensionProcessingException(ms_fullExtensionName, e);
      }
   }

   /**
    * This mehod retrieves the community id from
    * "sys_commSupport/communityidlookup" by their community name.
    * @param request <code>IPSRequestContext</code> object that is available in
    * the extension's process request method, assumed never <code>null</code>.
    * @param name Community name, can not be <code>null</null>
    * @return Community id.
    * @throws Exception
    */
   public static String getCommunityId(IPSRequestContext request,String name )
      throws Exception
   {
      //Backup parameters
      HashMap paramsBackup = request.getParameters();
      Document doc;
      try
      {
         request.setParameter(COMMUNITYNAME,name);
         IPSInternalRequest iReq =
            request.getInternalRequest(IREQ_COMMUNITYLOOKUP);
         try
         {
            iReq.makeRequest();
            doc = iReq.getResultDoc();
         }
         finally
         {
            if(iReq != null)
               iReq.cleanUp();
         }
      }
      finally
      {
         //restore paramaters
         request.setParameters(paramsBackup);
      }
            NodeList nl = doc.getElementsByTagName(ELEM_COMMUNITY);
            Element elem = null;
            if(null != nl)
               elem = (Element)nl.item(0);
            return elem.getAttribute(ATTR_COMMID);
   }

   /**
    * This method retrieves the list user's role-communities, viz. list of all
    * communities via his role membership.
    * @param request <code>IPSRequestContext</code> object that is available in
    * the extension's process request method, assumed never <code>null</code>.
    * @return list of user communities (community ids) as Java List object never
    * <code>null</code> may be empty.
    *
    */
   private List getUserCommunities(IPSRequestContext request)
      throws Exception
   {
         ArrayList list = new ArrayList();
         // Make an internal request to get the user roles.
         IPSInternalRequest iReq =
            request.getInternalRequest(IREQ_USERCOMMUNITIES);
         Document doc = null;
         try
         {
            iReq.makeRequest();
            doc = iReq.getResultDoc();
         }
         finally
         {
            if(iReq != null)
               iReq.cleanUp();
         }
         NodeList nl = doc.getElementsByTagName(ELEM_COMMUNITY);
         if(nl == null || nl.getLength() < 1)
            return list;

         Element elem = null;
         for(int i=0; i<nl.getLength(); i++)
         {
            elem = (Element)nl.item(i);
            list.add(elem.getAttribute(ATTR_COMMID));
         }
      return list;
   }
   /**
    * This method retrieves the default community from the first role that
    * belongs to the user. If user belongs to multiple roles, the first non-empty
    * value is considered.
    * @param request <code>IPSRequestContext</code> object that is available in
    * the extension's process request method, assumed never <code>null</code>.
    * @return community id of the
    * @throws Exception, if it cannot retrieve tha role
    * attribute for any reason.
    */
   static public String getUserDefaultCommunity(IPSRequestContext request)
   throws Exception
   {
      return getCommunityId(request,
             getUserRoleAttribute(request, SYS_DEFAULTCOMMUNITY));
   }

   /**
    * This method retrieves the value of the given attribute for the user role.
    * If user happens to be in multiple roles the first non empty value is
    * considered
    * @param request <code>IPSRequestContext</code> object that is available in
    * the extension's process request method, assumed never <code>null</code>.
    * @param srcAttrName, Name of the role attribute to retrieve, cannot be
    * <code>null</code>, if <code>null</code> the result will be <code>null</code>.
    * @return value of the given attribute, may be <code>null</code>
    * @throws Exception, if it cannot retrieve tha role
    * attribute for any reason.
    */
   static public String getUserRoleAttribute(IPSRequestContext request,
      String srcAttrName )
   throws Exception
   {
      if(srcAttrName == null)
         return null;
      String attrValue = null;
      List roles = request.getSubjectRoles();
      Object role = null;
      List roleAttribs = null;
      PSAttribute attr = null;
      List attrList = null;
      String attrName = null;
      for(int i=0; roles != null && i<roles.size(); i++)
      {
         role = roles.get(i);
         if(role == null)
            continue;
         roleAttribs = request.getRoleAttributes(role.toString().trim());
         for(int j=0; roleAttribs != null && j<roleAttribs.size(); j++)
         {
            attr = (PSAttribute)roleAttribs.get(j);
            if(attr == null)
               continue;
            attrName = attr.getName();
            if(attrName.equals(srcAttrName))
            {
               attrList = attr.getValues();
               if(attrList != null && attrList.size() > 0)
               {
                  // we take only the first attribute
                  attrValue = attrList.get(0).toString();
               }
            }
            if(attrValue != null && attrValue.length() > 0)
               return attrValue;
         }
      }
      return attrValue;
   }

   /**
    * To know if communities are enabled for the server. Communities are enabled
    * are enabled or disabled by setting the property variable
    * 'communities_enabled=yes' (or no).
    * @return <code>true</code> if communities are enabled.
    */
   static public boolean isCommunityEnabled()
   {
      return ms_communitiesEnabled;
   }

    /**
    * The fully qualified name of this extension. Nerver <code>null</code> or
    * <code>empty</code> after initialization.
    */
   static private String ms_fullExtensionName = "";

   /**
    * Initial value for the flag indicating of communities are enabled for the
    * server
    */
   static private boolean ms_communitiesEnabled = false;

   /**
    * Name of the element "Community" in the result document of the internal
    * request for user communities.
    */
   static public final String ELEM_COMMUNITY = "Community";

   /**
    * Name of the attribute of the communityid of the element "Community" in
    * the result document of the internal request for user communities.
    */
   static public final String ATTR_COMMID = "commid";

   /**
    * Value of the system default community, hardcoded to 1.
    */
   static public final String SYSTEM_COMMUNITY = "1";

   /**
    * Name of the internal request to get the user communities. This is a
    * standard Rhythmyx resource meant for internal request.
    */
   static public final String IREQ_USERCOMMUNITIES =
         "sys_commSupport/usercommunities";

   /**
    * Name of the internal request to get the community id with a c
    * community name. Requires parameter communityname=value, where value is
    * a valid community name.
    */
   static public final String IREQ_COMMUNITYLOOKUP =
      "sys_commSupport/communityidlookup";
   /**
    * Name of the parameter requires for community id lookup. This
    * paremeter is added when we lookup the community id.
    */
   static public final String COMMUNITYNAME = "communityname";

   /**
    * Name of user default community properties.
    */
   static public final String SYS_DEFAULTCOMMUNITY = "sys_defaultCommunity";

   /**
    * Initialization of the flag ms_communitiesEnabled. This is done based on
    * the value for the variable "communities_enabled" in the server
    * configuration file (i.e. server.properties). This is done only during
    * server startup which means server restart required if the property is
    * modified in the file.
    */
   static
   {
      try
      {
         Properties serverProp = PSServer.getServerProps();
         String enabled = serverProp.getProperty("communities_enabled", "no");
         ms_communitiesEnabled = false;
         if(enabled.equalsIgnoreCase("yes"))
         {
            ms_communitiesEnabled = true;
         }
      }
      catch(Throwable t) //should never happen!
      {
         PSConsole.printMsg(ms_fullExtensionName, t);
      }
   }
}


