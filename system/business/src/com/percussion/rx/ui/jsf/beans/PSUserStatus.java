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
package com.percussion.rx.ui.jsf.beans;

import com.percussion.i18n.PSLocaleException;
import com.percussion.i18n.PSLocaleManager;
import com.percussion.server.PSRequest;
import com.percussion.services.utils.jspel.PSRoleUtilities;
import com.percussion.utils.request.PSRequestInfo;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;

/**
 * The user status, used only for the status tag.
 * 
 * @author dougrand
 */
public class PSUserStatus
{
   /**
    * Locale of the user, never <code>null</code> or empty.
    */
   private String m_locale;
   
   /**
    * Display locale of the user, never <code>null</code> or empty.
    */
   private String m_localeDisplay;
   
   /**
    * The complete list of user roles, never <code>null</code> or empty.
    */
   private String m_fullrolestr;
   
   /**
    * An abbreviated list of user roles, never <code>null</code> or empty.
    */
   private Object m_rolestr;
   
   /**
    * The user's current community, never <code>null</code> or empty.
    */
   private String m_fullcommstr;
   
   /**
    * An abbreviated user community name, never <code>null</code> or empty.
    */
   private String m_commstr;
   
   /**
    * The login link, never <code>null</code> or empty.
    */
   private String m_rxloginurl;

   /**
    * The user, never <code>null</code> or empty.
    */
   private String m_user;

   /**
    * Ctor
    * @throws PSLocaleException
    */
   public PSUserStatus() throws PSLocaleException
   {
      PSRequest req =  (PSRequest) 
         PSRequestInfo.getRequestInfo(PSRequestInfo.KEY_PSREQUEST);
      HttpServletRequest request = (HttpServletRequest) req.getServletRequest();
      m_fullcommstr = PSRoleUtilities.getUserCurrentCommunity();
      m_commstr = StringUtils.abbreviate(m_fullcommstr, 35);
      m_fullrolestr = PSRoleUtilities.getUserRoles();
      m_rolestr = StringUtils.abbreviate(m_fullrolestr, 35);
      m_user = request.getRemoteUser();
      m_locale = PSRoleUtilities.getUserCurrentLocale();
      m_localeDisplay = PSLocaleManager.getInstance()
         .getLocale(m_locale).getDisplayName();
      String redirect = (String) request.getAttribute("sys_redirecturl");
      m_rxloginurl = "/Rhythmyx/sys_welcome/rxlogin.html?communitypage=yes";
      if (redirect != null)
      {
         m_rxloginurl += "&sys_redirecturl=" + redirect;
      }      
   }

   
   /**
    * @return the user
    */
   public String getUser()
   {
      return m_user;
   }

   /**
    * @return the locale
    */
   public String getLocale()
   {
      return m_locale;
   }

   /**
    * @return the localeDisplay
    */
   public String getLocaleDisplay()
   {
      return m_localeDisplay;
   }

   /**
    * @return the fullrolestr
    */
   public String getFullrolestr()
   {
      return m_fullrolestr;
   }

   /**
    * @return the rolestr
    */
   public Object getRolestr()
   {
      return m_rolestr;
   }

   /**
    * @return the fullcommstr
    */
   public String getFullcommstr()
   {
      return m_fullcommstr;
   }

   /**
    * @return the commstr
    */
   public String getCommstr()
   {
      return m_commstr;
   }

   /**
    * @return the rxloginurl
    */
   public String getRxloginurl()
   {
      return m_rxloginurl;
   }
}
