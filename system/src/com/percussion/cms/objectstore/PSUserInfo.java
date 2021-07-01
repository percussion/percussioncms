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
package com.percussion.cms.objectstore;

import com.percussion.cms.PSCmsException;
import com.percussion.design.objectstore.PSServerConfiguration;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.PSServer;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.util.PSCollection;
import com.percussion.util.PSHttpConnection;
import com.percussion.util.PSRemoteAppletRequester;
import com.percussion.util.PSXMLDomUtil;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.FileReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;


/**
 * This object represents the state of the logged in user. There are three methods
 * this object can be created, namely,
 * <ol>
 * <li>create empty object using the default constructor and call fromXml()
 * method. This method will give an object that may not indicate the current
 * state and depends on the XML element supplied.
 * </li>
 * <li>Create object using the constructor that takes the base URL. This way
 * the object makes a request to a known resource on Rhythmyx server to get the
 * user sate information. This is specifically designed to be used in the
 * context of applet. Assumes the user is already authenticated by browser.</li>
 * <li>Create object using the constructor that IPSRequestContext object. This
 * is useful when the object needs to be created within an exit. However, the
 * already has access to all the user state information this object can provide.</li>
 *
 * </ol>
 */
public class PSUserInfo implements IPSCmsComponent
{

   private static final Logger log = LogManager.getLogger(PSUserInfo.class);
   /**
    * Default constructor. Does nothing. Must be followed by call to fromXml()
    * method. This is useful only to build an object in the fly means the state
    * information might not come from the Rhythmyx server.
    */
   public PSUserInfo()
   {
   }

   /**
    * Constructor meant to be used in the context of an applet. This may not work
    * other contexts since there is no way of supplying credentials for logging
    * in.
    * @param urlBase the document or code base for the applet.
    * @throws PSCmsException if request to server to get the user info fails for
    * any reason.
    */
   public PSUserInfo(PSHttpConnection connection, URL urlBase)
      throws PSCmsException
   {
      m_RoleList.clear();
      try
      {
       // System.out.println("url base="+urlBase);
         PSRemoteAppletRequester requestor = new PSRemoteAppletRequester(connection,
            urlBase);
     //    System.out.println("getting doc");
         Document doc = requestor.getDocument("sys_psxCms/userinfo.xml", 
            new HashMap<Object, Object>());
   //      System.out.println("got doc"+doc);
 //        System.out.println("Server returned userinfo "+PSXMLDomUtil.toString(doc));
         fromXml(doc.getDocumentElement());
      }
      catch(Exception e)
      {
         log.error(e.getMessage());
         log.debug(e.getMessage(), e);
         throw new PSCmsException(1000, e.getMessage());
      }
   }

   /**
    * Constructor meant to be used from exits. Exit already can provide all the
    * information this object can provide and hence may not of great use. However,
    * it provides a useful wrapping of the user state attributes.
    * @param request IPSRequestContext object from where the user state
    * information is retrieved.
    * @throws PSCmsException if request to server to get the user info fails for
    * any reason.
    */
   public PSUserInfo(IPSRequestContext request)
      throws PSCmsException
   {
      m_RoleList.clear();
      try
      {
         m_UserName = request.getUserContextInformation(
            "User/Name", "unknown").toString();

         Object obj = request.getUserContextInformation("Roles/RoleName", null);

         if(obj!=null)
         {
            if (obj instanceof PSCollection)
            {
               PSCollection psColl = (PSCollection)obj;
               Iterator it = psColl.iterator();
               while(it.hasNext())
                  m_RoleList.add(it.next().toString());

            }
            else
            {
               m_RoleList.add(obj.toString());
            }
         }

         // get community from session objects
         obj = request.getSessionPrivateObject(
            IPSHtmlParameters.SYS_COMMUNITY);
         if(obj!=null)
            m_CommunityId = Integer.parseInt(obj.toString());
         // get Locale from session objects
         obj = request.getSessionPrivateObject(IPSHtmlParameters.SYS_LANG);
         if(obj!=null)
            m_Locale = obj.toString();

         m_SessionTimeout =
            PSServer.getServerConfiguration().getUserSessionTimeout();
      }
      catch(Exception e)
      {
         throw new PSCmsException(1000, e.getMessage());
      }
   }

   /*
    * Implementation of the interface method
    */
   public String getNodeName()
   {
      return XML_ELEM_ROOT;
   }

   /*
    * Implementation of the interface method
    */
   public Object clone()
   {
      PSUserInfo clone = null;
      try
      {
         clone = (PSUserInfo)super.clone();
         clone.m_SessionId = m_SessionId;
         clone.m_UserName = m_UserName;
         clone.m_CommunityId = m_CommunityId;
         clone.m_Locale = m_Locale;

         for(int i=0; i<m_RoleList.size(); i++)
            clone.m_RoleList.add(m_RoleList.get(i));
         clone.m_SessionTimeout = m_SessionTimeout ;
      }
      catch(CloneNotSupportedException e)
      {
         //TODO: Fix ME ????
      }
      return clone;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof PSUserInfo)) return false;
      PSUserInfo that = (PSUserInfo) o;
      return m_CommunityId == that.m_CommunityId &&
              m_SessionTimeout == that.m_SessionTimeout &&
              Objects.equals(m_SessionId, that.m_SessionId) &&
              Objects.equals(m_UserName, that.m_UserName) &&
              Objects.equals(m_Locale, that.m_Locale) &&
              Objects.equals(m_RoleList, that.m_RoleList);
   }

   @Override
   public int hashCode() {
      return Objects.hash(m_SessionId, m_UserName, m_CommunityId, m_Locale, m_RoleList, m_SessionTimeout);
   }

   /*
    * Implementation of the interface method
    */
   public void fromXml(Element elemRoot)
      throws PSUnknownNodeTypeException
   {
      m_RoleList.clear();

      PSXMLDomUtil.checkNode(elemRoot, XML_ELEM_ROOT);

      Element el = PSXMLDomUtil.getFirstElementChild(elemRoot, XML_ELEM_SESSIONID);
      m_SessionId = PSXMLDomUtil.getElementData(el);

      el = PSXMLDomUtil.getNextElementSibling(el, XML_ELEM_USERNAME);
      m_UserName = PSXMLDomUtil.getElementData(el);

      el = PSXMLDomUtil.getNextElementSibling(el, XML_ELEM_COMMUNITYID);
      String temp = PSXMLDomUtil.getElementData(el);
      try
      {
         m_CommunityId = Integer.parseInt(temp);
      }catch(NumberFormatException e){}

      el = PSXMLDomUtil.getNextElementSibling(el, XML_ELEM_LOCALE);
      m_Locale = PSXMLDomUtil.getElementData(el);

      el = PSXMLDomUtil.getNextElementSibling(el, XML_ELEM_ROLES);

      // get all Roles
      Element elChild = PSXMLDomUtil.getFirstElementChild(el);
      String value = "";
      while (elChild != null && elChild.getNodeName().equals(XML_ELEM_ROLE))
      {
         value = PSXMLDomUtil.getElementData(elChild);
         if(value != null)
            m_RoleList.add(value);
         elChild = PSXMLDomUtil.getNextElementSibling(elChild);
      }
      el = PSXMLDomUtil.getNextElementSibling(el, XML_ELEM_SESSIONTIMEOUT);
      m_SessionTimeout = Integer.parseInt(PSXMLDomUtil.getElementData(el));
   }

   /**
    * Serializes this object into an xml element.
    * It will conform to the following dtd:
    * <p>
    * &lt;!ELEMENT PSXUserInfo (SessionId, UserName, CommunityId, Locale, Roles )>
    * &lt;!ELEMENT Role (#PCDATA)>
    * &lt;!ELEMENT Roles (Role+ )>
    * &lt;!ELEMENT Locale (#PCDATA)>
    * &lt;!ELEMENT CommunityId (#PCDATA)>
    * &lt;!ELEMENT UserName (#PCDATA)>
    * &lt;!ELEMENT SessionId (#PCDATA)>
    * &lt;!ATTLIST  PSXUserInfo SecurityProviderInstance CDATA #REQUIRED>
    * &lt;!ATTLIST  PSXUserInfo SecurityProviderTypeId CDATA #REQUIRED>
    * &lt;!ATTLIST  PSXUserInfo SecurityProvider CDATA #REQUIRED>
    * <p>
    *
    * @param doc Used to generate the element. May not be <code>null</code>.
    *
    * @return the generated element, never <code>null</code>.
    *
    * @throws IllegalArgumentException if <code>doc</code> is <code>null</code>
    */
   public Element toXml(Document doc)
   {
      if (null == doc)
         throw new IllegalArgumentException("doc may not be null");

      Element elem  = PSXmlDocumentBuilder.createRoot(doc, XML_ELEM_ROOT);

      PSXmlDocumentBuilder.addElement(doc, elem,
         XML_ELEM_SESSIONID, m_SessionId);
      PSXmlDocumentBuilder.addElement(doc, elem, XML_ELEM_USERNAME, m_UserName);
      PSXmlDocumentBuilder.addElement(doc, elem, XML_ELEM_COMMUNITYID,
         String.valueOf(m_CommunityId));
      PSXmlDocumentBuilder.addElement(doc, elem, XML_ELEM_LOCALE, m_Locale);

      Element rolesElem = PSXmlDocumentBuilder.addElement(doc, elem,
         XML_ELEM_ROLES, null);
      for(int i=0; i<m_RoleList.size(); i++)
      {
         PSXmlDocumentBuilder.addElement(doc, rolesElem,
            XML_ELEM_ROLE, m_RoleList.get(i).toString());
      }
      PSXmlDocumentBuilder.addElement(doc, elem, XML_ELEM_SESSIONTIMEOUT,
         String.valueOf(m_SessionTimeout));
      return elem;
   }


   /**
    * User name. Normally not <code>null</code>. May be <code>null</code>
    * or <code>empty</code> if the user is not logged into Rhythmyx properly.
    * @return user name
    */
   public String getUserName()
   {
      return m_UserName;
   }

   /**
    * User Comunity ID . Normally not <code>null</code>. May be <code>null</code>
    * or <code>empty</code> if the user is not logged into Rhythmyx properly.
    * @return communty id
    */
   public int getCommunityId()
   {
      return m_CommunityId;
   }

   /**
    * User Locale string in the standard syntax, i.e. en-us. Normally not
    * <code>null</code>. May be <code>null</code> or <code>empty</code> if the
    * user is not logged into Rhythmyx properly.
    * @return user Locale
    */
   public String getLocale()
   {
      return m_Locale;
   }

   /**
    * An iterator of list of user roles. Never <code>null</code> and rarely be
    * <code>empty</code>
    * @return list of user roles.
    */
   public Iterator getRoles()
   {
      return m_RoleList.iterator();
   }

   /**
    * Session id of the user, never <code>null</code> or <code>empty</code>.
    * @return user's sessionid.
    */
   public String getSessionId()
   {
      return m_SessionId;
   }

   /**
    * User session timeout period in seconds as returned by
    * <code>PSServer.getServerConfiguration().getUserSessionTimeout()</code>
    * @return User session timeout period in seconds.
    * @see PSServerConfiguration
    */
   public int getSessionTimeout()
   {
      return m_SessionTimeout;
   }

   /**
    * main() method for testing purpose.
    * @param args
    */
   static public void main(String[] args)
   {
      try
      {
         Document doc = PSXmlDocumentBuilder.createXmlDocument(
            new FileReader("c:/PSXUserInfo.xml"), false);
         PSUserInfo userInfo = new PSUserInfo();
         userInfo.fromXml(doc.getDocumentElement());
         Element res = userInfo.toXml(PSXmlDocumentBuilder.createXmlDocument());
         PSXmlDocumentBuilder.write(res, System.out);
      }
      catch(Exception e)
      {
         log.error(e.getMessage());
         log.debug(e.getMessage(), e);
      }
   }

   /**
    * User's sessionid, standard Rhythmyx sessionid string. Never <code>null</code>
    * if the object is constructed properly.
    */
   private String m_SessionId;

   /**
    * User's name. Never <code>null</code> if the user is authenticated
    * previously.
    */
   private String m_UserName;

   /**
    * User's logged in communityid. Never <code>null</code> if the user logged
    * into a community.
    */
   private int m_CommunityId;

   /**
    * User's logged in Locale. Never <code>null</code> if the user logged
    * into a Locale.
    */
   private String m_Locale;


   /**
    * User's role list, Never <code>null</code>  if the user is logged
    * authenticated previously and if the object is constructed properly.
    */
   private List<String> m_RoleList = new ArrayList<String>();


   /**
    * User session timeout in seconds. Taken from the server configuration.
    */
   private int m_SessionTimeout = 0;

   /**
    * DTD string constants for this object
    */
   static public final String XML_ELEM_ROOT = "PSXUserInfo";
   static public final String XML_ELEM_USERNAME = "UserName";
   static public final String XML_ELEM_COMMUNITYID = "CommunityId";
   static public final String XML_ELEM_LOCALE = "Locale";
   static public final String XML_ELEM_ROLES = "Roles";
   static public final String XML_ELEM_ROLE= "Role";
   static public final String XML_ELEM_SESSIONID = "SessionId";
   static public final String XML_ELEM_SESSIONTIMEOUT = "SessionTimeOut";
}
