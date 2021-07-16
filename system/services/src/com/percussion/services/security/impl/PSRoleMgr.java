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

package com.percussion.services.security.impl;

import com.percussion.design.objectstore.PSConditional;
import com.percussion.design.objectstore.PSSubject;
import com.percussion.design.objectstore.PSTextLiteral;
import com.percussion.error.PSExceptionUtils;
import com.percussion.security.IPSDirectoryCataloger;
import com.percussion.security.IPSGroupProvider;
import com.percussion.security.IPSInternalRoleCataloger;
import com.percussion.security.IPSPrincipalAttribute;
import com.percussion.security.IPSPrincipalAttribute.PrincipalAttributes;
import com.percussion.security.IPSRoleCataloger;
import com.percussion.security.IPSSubjectCataloger;
import com.percussion.security.IPSTypedPrincipal;
import com.percussion.security.IPSTypedPrincipal.PrincipalTypes;
import com.percussion.security.PSSecurityCatalogException;
import com.percussion.security.PSSecurityProviderPool;
import com.percussion.services.security.IPSRoleMgr;
import com.percussion.services.security.PSJaasUtils;
import com.percussion.services.security.PSRoleMgrLocator;
import com.percussion.services.security.PSTypedPrincipal;
import com.percussion.services.security.data.PSCatalogerConfig;
import com.percussion.services.security.data.PSCatalogerConfigurations;
import com.percussion.util.PSBaseBean;
import com.percussion.utils.servlet.PSServletUtils;
import com.percussion.utils.xml.PSInvalidXmlException;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xml.sax.SAXException;

import javax.security.auth.Subject;
import java.io.File;
import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Implementation of the {@link IPSRoleMgr} service.  Additionally provides
 * persistence of its Spring bean XML configuration.
 */
@PSBaseBean("sys_roleMgr")
public class PSRoleMgr implements IPSRoleMgr
{
   private static final Logger log = LogManager.getLogger(PSRoleMgr.class);

   public List<Subject> findUsers(List<String> names) 
      throws PSSecurityCatalogException
   {
      return findUsers(names, null, null);
   }
   
   public List<Subject> findUsers(List<String> names, String catalogerName, 
      String type) throws PSSecurityCatalogException
   {
      return findUsers(names, catalogerName, type, null);
   }

   // see IPSRoleMgr interface
   public List<Subject> findUsers(List<String> names, String catalogerName, 
      String type, Set<PrincipalAttributes> supportedTypes) 
         throws PSSecurityCatalogException
   {
      return findUsers(names, catalogerName, type, supportedTypes, false);
   }
   // see IPSRoleMgr interface
   public List<Subject> findUsers(List<String> names, String catalogerName, 
      String type, Set<PrincipalAttributes> supportedTypes, boolean throwCatalogerExceptions) 
         throws PSSecurityCatalogException
   {
      Set<Subject> userSet = new HashSet<>();
      List<Subject> userList = new ArrayList<>();
      
      boolean specifiedName = !StringUtils.isBlank(catalogerName);
      if (specifiedName && StringUtils.isBlank(type))
         throw new IllegalArgumentException(
            "type may not be null or empty if catalogerName is supplied");
      
      boolean matchedName = false;
      
      //query external catalogers
      if (!specifiedName || 
         IPSRoleMgr.SUBJECT_CATALOGER_TYPE.equals(type))
      {
         for (IPSSubjectCataloger cataloger : m_subjectCatalogers)
         {
            if (specifiedName && !cataloger.getName().equals(catalogerName))
               continue;
            else if (specifiedName)
               matchedName = true;
            
            boolean isSupported = true;
            if (supportedTypes != null)
            {
               for (PrincipalAttributes attrType : supportedTypes)
               {
                  if (!cataloger.supportsAttributeType(attrType))
                  {
                     isSupported = false;
                     break;
                  }
               }
            }
            
            if (isSupported)
               userSet.addAll(cataloger.findUsers(names));
            
            if (matchedName)
               break;
         }
         
         // if specified a subject cataloger, we're done.
         if (specifiedName)
         {
            if (!matchedName)
               throw new IllegalArgumentException(
                  "No matching cataloger found");
            
            userList.addAll(userSet);
            
            return userList;
         }
      }

      // query internal catalogers
      List<IPSDirectoryCataloger> catalogers = 
         PSSecurityProviderPool.getAllDirectoryCatalogers();
      for (IPSDirectoryCataloger cataloger : catalogers)
      {
         if ((specifiedName) && (!(cataloger.getName().equals(catalogerName) && 
            cataloger.getCatalogerType().equals(type))))
         {
            continue;
         }
         else if (specifiedName)
            matchedName = true;

         try
         {
         Collection<Object> psSubs = new ArrayList<>();
         
         boolean isSupported = true;
         if (supportedTypes != null)
         {
            for (PrincipalAttributes attrType : supportedTypes)
            {
               if (attrType.equals(PrincipalAttributes.SUBJECT_NAME))
                  continue;
               else if (attrType.equals(PrincipalAttributes.EMAIL_ADDRESS))
               {
                  isSupported = !StringUtils.isBlank(
                     cataloger.getEmailAddressAttributeName());
                  
               }
               else
                  isSupported = false;
               
               if (!isSupported)
                  break;
            }
         }
         
         if (isSupported)   
         {
            PSConditional[] conds = null;
            if (names != null && !names.isEmpty())
            {
               conds = new PSConditional[names.size()];
               for (int i = 0; i < conds.length; i++)
               {
                  conds[i] = new PSConditional(new PSTextLiteral(
                     cataloger.getObjectAttributeName()), 
                     PSConditional.OPTYPE_LIKE, new PSTextLiteral(
                        names.get(i)));
               }
            }
            
            psSubs.addAll(cataloger.findUsers(conds, null));            
         }


         for (Object subObj : psSubs)
         {
            PSSubject psSub = (PSSubject) subObj;
            Subject sub = PSJaasUtils.convertSubject(psSub, 
                  cataloger.getEmailAddressAttributeName());
           
            sub.getPrincipals().addAll(getUserGroups(PSJaasUtils.subjectToPrincipal(psSub)));
            userSet.add(sub);
         }
         }
         catch (Exception e)
         {
            log.error("Error finding users: {}" , PSExceptionUtils.getMessageForLog(e));
            log.debug(e);
            if(throwCatalogerExceptions)
               throw new PSSecurityCatalogException(e);
         }         
         
         if (matchedName)
            break;
      }
      
      if (specifiedName && !matchedName)
         throw new IllegalArgumentException(
            "No matching cataloger found");

      userList.addAll(userSet);
      
      return userList;
   }

   // see IPSRoleMgr interface
   public Set<IPSTypedPrincipal> getRoleMembers(String roleName)
      throws PSSecurityCatalogException
   {
      if (StringUtils.isBlank(roleName))
         throw new IllegalArgumentException(
            "roleName may not be null or empty");
      
      Set<IPSTypedPrincipal> members = new HashSet<>();
      
      // query external catalogers
      for (IPSRoleCataloger cataloger : m_roleCatalogers)
      {
         members.addAll(cataloger.getRoleMembers(roleName));
      }
      
      // query internal catalogers
      members.addAll(catalogInternalRoleMembers(roleName, 0));
      
      return members;
   }

   // see IPSRoleMgr interface   
   public Set<IPSTypedPrincipal> getRoleMembers(String roleName, 
      PrincipalTypes type) throws PSSecurityCatalogException
   {
      if (StringUtils.isBlank(roleName))
         throw new IllegalArgumentException(
            "roleName may not be null or empty");

      int subType;
      if (type.equals(IPSTypedPrincipal.PrincipalTypes.SUBJECT))
         subType = PSSubject.SUBJECT_TYPE_USER;
      else if (type.equals(IPSTypedPrincipal.PrincipalTypes.GROUP))
         subType = PSSubject.SUBJECT_TYPE_GROUP;
      else
         throw new IllegalArgumentException("Invalid type");
      
      Set<IPSTypedPrincipal> members = new HashSet<>();
      
      // query external catalogers
      for (IPSRoleCataloger cataloger : m_roleCatalogers)
      {
         for (IPSTypedPrincipal principal : cataloger.getRoleMembers(roleName))
         {
            if (principal.getPrincipalType().equals(type))
               members.add(principal);
         }
      }
      
      // query internal catalogers
      members.addAll(catalogInternalRoleMembers(roleName, subType));
      
      return members;
   }

   /**
    * Catalog internal role members.
    * 
    * @param roleName The rolename, assumed not <code>null</code> or empty.
    * @param subType One of the <code>PSSubject.SUBJECT_TYPE_XXX</code> values, 
    * or 0 for any type.
    * 
    * @return The members, never <code>null</code>, may be empty.
    */
   private Set<IPSTypedPrincipal> catalogInternalRoleMembers(String roleName, 
      int subType)
   {
      Set<IPSTypedPrincipal> members = new HashSet<>();
      Set<Object> subSet = new HashSet<>();
      
      for (IPSInternalRoleCataloger roleCat : 
         PSSecurityProviderPool.getAllRoleCatalogers())
      {
         subSet.addAll(roleCat.getSubjects(roleName, null, 
            subType, null, true));
      } 
      
      for (Object objSub : subSet)
      {
         members.add(PSJaasUtils.subjectToPrincipal((PSSubject)objSub));
      }
      
      return members;
   }
   

   // see IPSRoleMgr interface
   public Set<String> getUserRoles(IPSTypedPrincipal user)
      throws PSSecurityCatalogException
   {
      Set<String> roles = new HashSet<>();
      
      if (user == null)
      {
         roles.addAll(
            PSRoleMgrLocator.getBackEndRoleManager().getRhythmyxRoles());
      }
      else
      {
         // query external catalogers
         for (IPSRoleCataloger cataloger : m_roleCatalogers)
         {
            roles.addAll(cataloger.getUserRoles(user));
         }         
         
         // query internal catalogers
         for (IPSInternalRoleCataloger roleCat : 
            PSSecurityProviderPool.getAllRoleCatalogers())
         {
            roles.addAll(roleCat.getRoles(user.getName(), 
               PSJaasUtils.getSubjectType(user.getPrincipalType())));
         }
      }
      
      // filter out non-Rhythmyx roles
      roles.retainAll(getDefinedRoles());
      
      return roles;
   }

   // see IPSRoleMgr interface
   @SuppressWarnings("unused")
   public Set<String> getDefaultUserRoles(IPSTypedPrincipal user)
      throws PSSecurityCatalogException
   {
      Set<String> roles = new HashSet<>();
      IPSInternalRoleCataloger roleCat = 
         PSSecurityProviderPool.getDefaultRoleCataloger();
      roles.addAll(roleCat.getRoles(user.getName(), 0));
      
      return roles;
   }

   // see IPSRoleMgr interface
   public Set<Principal> getUserGroups(IPSTypedPrincipal user)
   {
      if (user == null)
         throw new IllegalArgumentException("user may not be null");
      
      Set<Principal> groups = new HashSet<>();
      
      // query external catalogers 
      for (IPSSubjectCataloger cataloger : m_subjectCatalogers)
      {
         if (cataloger.supportsGroups())
            groups.addAll(cataloger.getUserGroups(user));
      } 
      
      // query internal catalogers
      for (IPSDirectoryCataloger dirCat : 
         PSSecurityProviderPool.getAllDirectoryCatalogers())
      {
         Iterator<IPSGroupProvider> groupProviders = dirCat.getGroupProviders();
         while (groupProviders.hasNext())
         {
            IPSGroupProvider gp = groupProviders.next();
            Collection<String> groupNames = gp.getUserGroups(user.getName());
            for (String groupName : groupNames)
            {
               groups.add(PSTypedPrincipal.createGroup(groupName));
            }
         }
      }
      
      return groups;
   }

   // see IPSRoleMgr interface
   public List<Principal> findGroups(String pattern, String catalogerName,
      String type) throws PSSecurityCatalogException
   {
      List<Principal> groups = new ArrayList<>();
      
      boolean specifiedName = !StringUtils.isBlank(catalogerName);
      boolean matchedName = false;
      
      // query external catalogers
      if (!specifiedName || 
         IPSRoleMgr.SUBJECT_CATALOGER_TYPE.equals(type))
      {
         for (IPSSubjectCataloger cataloger : m_subjectCatalogers)
         {
            if (specifiedName && !cataloger.getName().equals(catalogerName))
               continue;
            else if (specifiedName)
               matchedName = true;
            
            if (cataloger.supportsGroups())
               groups.addAll(cataloger.findGroups(pattern));
            
            if (matchedName)
               break;
         }
         
         // if specified a subject cataloger, we're done.
         if (specifiedName)
         {
            if (!matchedName)
               throw new IllegalArgumentException(
                  "No matching cataloger found");
            
            return groups;
         }
      }
      
      String groupFilter = StringUtils.isBlank(pattern) ? null : pattern;
      
      List<IPSDirectoryCataloger> catalogers = 
         PSSecurityProviderPool.getAllDirectoryCatalogers();
      for (IPSDirectoryCataloger cataloger : catalogers)
      {
         if ((specifiedName) && (!(cataloger.getName().equals(catalogerName) && 
            cataloger.getCatalogerType().equals(type))))
         {
            continue;
         }
         else if (specifiedName)
            matchedName = true;

         Iterator<IPSGroupProvider> groupProviders = 
            cataloger.getGroupProviders();
         if (!groupProviders.hasNext())
            continue;
         
         while (groupProviders.hasNext())
         {
            IPSGroupProvider gp = groupProviders.next();
            for (String s : gp.getGroups(groupFilter)) {
               groups.add(PSTypedPrincipal.createGroup(s));
            }
         }
         
         if (matchedName)
            break;
      }
      
      if (specifiedName && !matchedName)
         throw new IllegalArgumentException("Invalid cataloger type supplied");
      
      return groups;      
   }

   // see IPSRoleMgr interface
   public Set<IPSPrincipalAttribute> getRoleAttributes(String roleName)
   {
      return PSRoleMgrLocator.getBackEndRoleManager().getRoleAttributes(
         roleName);
   }

   // see IPSRoleMgr interface
   public List<PSCatalogerConfig> getCatalogerConfigs() 
      throws PSInvalidXmlException, IOException, SAXException
   {
      File configFile = new File(PSServletUtils.getSpringConfigDir(), 
         PSServletUtils.CATALOGER_BEANS_FILE_NAME);
      
      return PSCatalogerConfigurations.getCatalogerConfigs(configFile);
   }
   
   // see IPSRoleMgr interface
   public void saveCatalogerConfigs(List<PSCatalogerConfig> configs) 
      throws PSInvalidXmlException, IOException, SAXException
   {
      if (configs == null)
         throw new IllegalArgumentException("configs may not be null");
      
      File configFile = new File(PSServletUtils.getSpringConfigDir(), 
         PSServletUtils.CATALOGER_BEANS_FILE_NAME);
      
      PSCatalogerConfigurations.saveCatalogerConfigs(configs, configFile);
   }

   // see IPSRoleMgr interface
   public List<IPSSubjectCataloger> getSubjectCatalogers()
   {
      return new ArrayList<>(m_subjectCatalogers);
   }

   // see IPSRoleMgr interface
   public boolean supportsGroups(String catalogerName, String type)
   {
      if (StringUtils.isBlank(catalogerName))
         throw new IllegalArgumentException(
            "catalogerName may not be null or empty");
      
      if (StringUtils.isBlank(type))
         throw new IllegalArgumentException("type may not be null or empty");
      
      boolean supportsGroups = false;
      boolean matchedName = false;
      
      // if specified a subject cataloger, we're done.
      if (type.equals(IPSRoleMgr.SUBJECT_CATALOGER_TYPE))
      {
         for (IPSSubjectCataloger cataloger : m_subjectCatalogers)
         {
            if (!cataloger.getName().equals(catalogerName))
               continue;
            
            matchedName = true;
            supportsGroups = cataloger.supportsGroups();
            break;
         }

         if (!matchedName)
            throw new IllegalArgumentException(
               "Invalid cataloger type supplied");
         
         return supportsGroups;
      }
      
      List<IPSDirectoryCataloger> catalogers = 
         PSSecurityProviderPool.getAllDirectoryCatalogers();
      for (IPSDirectoryCataloger cataloger : catalogers)
      {
         if (!(cataloger.getName().equals(catalogerName) && 
            cataloger.getCatalogerType().equals(type)))
         {
            continue;
         }
         else 
            matchedName = true;

         supportsGroups = cataloger.getGroupProviders().hasNext();
         break;
      }
      
      if (!matchedName)
         throw new IllegalArgumentException("Invalid cataloger type supplied");
      
      return supportsGroups;
   }

   // see IPSRoleMgr interface
   public List<String> getDefinedRoles()
   {
      List<String> roles = new ArrayList<>();
      roles.addAll(PSRoleMgrLocator.getBackEndRoleManager().getRhythmyxRoles());
      
      return roles;
   }

   // see IPSRoleMgr interface
   public List<IPSTypedPrincipal> getGroupMembers(
      Collection<? extends Principal> groups)
   {
      if (groups == null)
         throw new IllegalArgumentException("groups may not be null");
      
      List<IPSTypedPrincipal> members = new ArrayList<>();
      
      if (groups.isEmpty())
         return members;
      
      for (IPSSubjectCataloger cataloger : m_subjectCatalogers)
      {
         // see if processed
         if (groups.isEmpty())
            break;

         if (!cataloger.supportsGroups())
            continue;
         
         members.addAll(cataloger.getGroupMembers(groups));
      }

      
      List<IPSDirectoryCataloger> catalogers = 
         PSSecurityProviderPool.getAllDirectoryCatalogers();
      for (IPSDirectoryCataloger cataloger : catalogers)
      {
         // see if processed
         if (groups.isEmpty())
            break;
         
         Iterator<IPSGroupProvider> groupProviders = 
            cataloger.getGroupProviders();
         if (!groupProviders.hasNext())
            continue;
         
         while (groupProviders.hasNext())
         {
            IPSGroupProvider gp = groupProviders.next();
            members.addAll(gp.getGroupMembers(groups));
         }
      }      
      
      return members;  
   }
   
   // see IPSRoleMgr interface
   public List<IPSDirectoryCataloger> getDirectoryCatalogers()
   {
      return PSSecurityProviderPool.getAllDirectoryCatalogers();
   }   

   // see IPSRoleMgr interface
   public boolean isDefaultCataloger(IPSDirectoryCataloger cataloger)
   {
      if (cataloger == null)
         throw new IllegalArgumentException("cataloger may not be null");
      
      return cataloger == PSSecurityProviderPool.getDefaultDirectoryCataloger();
   }   
   
   public void setSubjectCatalogers(List<IPSSubjectCataloger> catalogers)
   {
      if (catalogers == null)
         throw new IllegalArgumentException("catalogers may not be null");
      
      m_subjectCatalogers = catalogers; 
   }
   
   public void setRoleCatalogers(List<IPSRoleCataloger> catalogers)
   {
      if (catalogers == null)
         throw new IllegalArgumentException("catalogers may not be null");
      
      m_roleCatalogers = catalogers; 
   }   

   /**
    * Collection of subject catalogers, never <code>null</code>, initially 
    * empty, modified by {@link #setSubjectCatalogers(List)}.
    */
   private Collection<IPSSubjectCataloger> m_subjectCatalogers = 
      new ArrayList<>(0);
   
   /**
    * Collection of role catalogers, never <code>null</code>, initially 
    * empty, modified by {@link #setRoleCatalogers(List)}.
    */
   private Collection<IPSRoleCataloger> m_roleCatalogers = 
      new ArrayList<>(0);
}
