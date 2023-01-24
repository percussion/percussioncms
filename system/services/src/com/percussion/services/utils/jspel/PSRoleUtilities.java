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
package com.percussion.services.utils.jspel;

import com.percussion.i18n.PSI18nUtils;
import com.percussion.server.PSRequest;
import com.percussion.server.PSUserSession;
import com.percussion.services.system.IPSSystemService;
import com.percussion.services.system.PSSystemServiceLocator;
import com.percussion.services.system.data.PSUIComponent;
import com.percussion.services.system.data.PSUIComponentProperty;
import com.percussion.utils.request.PSRequestInfo;
import com.percussion.utils.string.PSStringUtils;

import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;

/**
 * Various static methods called from el to determine a user's roles or role
 * access.
 * 
 * @author dougrand
 */
public class PSRoleUtilities
{
   /**
    * If the user's role's include the named role for the current component,
    * then return <code>true</code>. The current component is derived from
    * the current parameters.
    * 
    * @param component the component being tested, never <code>null</code> or
    *           empty
    * @param rolename the rolename, never <code>null</code> or empty
    * @return <code>true</code> if the user has the given named role from the
    *         component configuration
    */
   public static Boolean hasComponentRole(String component, String rolename)
   {
      if (StringUtils.isBlank(component))
      {
         throw new IllegalArgumentException(
               "component may not be null or empty");
      }
      if (StringUtils.isBlank(rolename))
      {
         throw new IllegalArgumentException("rolename may not be null or empty");
      }
      PSRequest req = (PSRequest) PSRequestInfo
            .getRequestInfo(PSRequestInfo.KEY_PSREQUEST);
      HttpServletRequest sreq = req.getServletRequest();

      IPSSystemService ssvc = PSSystemServiceLocator.getSystemService();
      PSUIComponent comp = ssvc.findComponentByName(component);
      if (comp != null)
      {
         for (PSUIComponentProperty prop : comp.getProperties())
         {
            if (prop.getName().equals(rolename))
            {
               if (sreq.isUserInRole(prop.getValue()))
               {
                  return true;
               }
            }
         }
      }
      return false;
   }

   /**
    * Get the current logged in user's roles as a comma separated string
    * 
    * @return the roles, never <code>null</code>
    */
   public static String getUserRoles()
   {
      PSRequest req = (PSRequest) PSRequestInfo
            .getRequestInfo(PSRequestInfo.KEY_PSREQUEST);
      PSUserSession s = req.getUserSession();
      List<String> roles = s.getUserRoles();
      return PSStringUtils.listToString(roles, ", ");
   }

   /**
    * Get the current logged in user's community
    * 
    * @return the community, never <code>null</code>
    */
   public static String getUserCurrentCommunity()
   {
      PSRequest req = (PSRequest) PSRequestInfo
            .getRequestInfo(PSRequestInfo.KEY_PSREQUEST);
      PSUserSession s = req.getUserSession();
      String community = s.getUserCurrentCommunity();
      
      return community != null ? community : "<unknown>";
   }
   
   /**
    * Get the current logged in user's locale.  Will return the System locale if user session is not active
    * 
    * @return the locale
    */
   public static String getUserCurrentLocale()
   {
      PSRequest req = (PSRequest) PSRequestInfo
            .getRequestInfo(PSRequestInfo.KEY_PSREQUEST);
      
      if(req == null){
         return PSI18nUtils.getSystemLanguage();
      }else{ 
         PSUserSession s = req.getUserSession();
         
         String locale = (String) s
               .getPrivateObject(PSI18nUtils.USER_SESSION_OBJECT_SYS_LANG);
         
         if (locale == null)
         {
            locale = Locale.getDefault().toString().toLowerCase()
                  .replace('_', '-');
         }
         
         return locale;
       }
   }   
}
