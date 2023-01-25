/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
