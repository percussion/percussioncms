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
package com.percussion.security;

import com.percussion.design.objectstore.PSDirectorySet;
import com.percussion.design.objectstore.PSGlobalSubject;
import com.percussion.design.objectstore.PSRoleProvider;
import com.percussion.design.objectstore.PSServerConfiguration;
import com.percussion.design.objectstore.PSSubject;
import com.percussion.server.PSConsole;
import com.percussion.server.PSServer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A role cataloger using a directory server as source.
 */
@SuppressWarnings(value={"unchecked"})
public class PSRoleCataloger extends PSCataloger 
   implements IPSInternalRoleCataloger
{
   /**
    * Logger to use, never <code>null</code>.
    */
   static private Logger log = LogManager.getLogger(PSRoleCataloger.class);

   /**
    * Convenience constructor that calls
    * {@link #PSRoleCataloger(Properties, PSServerConfiguration)} with
    * <code>null<code> for the server configuration.
    */
   public PSRoleCataloger(Properties properties) {
      this(properties, null);
   }

   /**
    * Constructs a new role cataloger for the supplied properties.
    * 
    * @param properties the role cataloger requires one property: a reference to
    *           the <code>PSDirectorySet</code> definition used as the role
    *           server, not <code>null</code> or empty.
    * @param config the server configuration with all directory definitions, may
    *           be <code>null<code> in which case this will get it from the 
    *    <code>PSServer</code>.
    * @throws PSSecurityException if the referenced role provider was not
    *    found.
    */
   public PSRoleCataloger(Properties properties, PSServerConfiguration config)
         throws PSSecurityException {
      if (properties == null || properties.isEmpty())
         throw new IllegalArgumentException(
               "properties cannot be null or empty");

      if (config == null)
         config = PSServer.getServerConfiguration();

      String roleProviderName = properties
            .getProperty(PSSecurityProvider.PROVIDER_NAME);
      if (roleProviderName == null)
         throw new IllegalArgumentException(
               "properties must specify roleProviderName");

      m_roleProvider = config.getRoleProvider(roleProviderName);
      if (m_roleProvider == null)
      {
         Object[] args =
         {roleProviderName};

         throw new PSSecurityException(
               IPSSecurityErrors.DIR_REFERENCED_ROLEPROVIDER_NOT_FOUND, args);
      }

      // load and validate the specified directory set
      if (!m_roleProvider.isBackendRoleProvider())
      {
         String directorySetName = m_roleProvider.getDirectoryRef().getName();
         initDirectorySet(directorySetName, config);
      }
   }

   /** @see IPSInternalRoleCataloger */
   public List getRoles(String subjectName, int subjectType)
   {
      List roleList = new ArrayList();
      Set roleSet = new HashSet();
      
      // groups not supported
      if (subjectType == PSSubject.SUBJECT_TYPE_GROUP)
         return roleList;
      
      PSRoleProvider provider = getProvider();
      
      if (provider.isDirectoryRoleProvider() || provider.isBothProvider())
      {
         DirContext context = null;
         NamingEnumeration results = null;
         NamingEnumeration attrs = null;
         NamingEnumeration values = null;

         try
         {
            String objectAttributeName = getDirectorySet()
                  .getRequiredAttributeName(PSDirectorySet.OBJECT_ATTRIBUTE_KEY);
            String roleAttrName = getDirectorySet().getRequiredAttributeName(
                  PSDirectorySet.ROLE_ATTRIBUTE_KEY);

            Map filterValues = new HashMap();
            if (subjectName != null)
            {
               if (subjectName.trim().length() == 0)
                  filterValues.put(objectAttributeName,
                        PSJndiUtils.WILDCARD_SEARCH);
               else
                  filterValues.put(objectAttributeName, subjectName);
            }
            filterValues.put(roleAttrName, PSJndiUtils.WILDCARD_SEARCH);

            String[] returnAttributes =
            {roleAttrName};

            Iterator directories = getDirectories().values().iterator();
            while (directories.hasNext())
            {
               PSDirectoryDefinition directory = 
                  (PSDirectoryDefinition) directories.next();

               context = createContext(directory);
               results = getAttributes(context, directory, filterValues,
                     returnAttributes);
               while (results != null && results.hasMore())
               {
                  SearchResult result = (SearchResult) results.next();
                  Attributes attributes = result.getAttributes();
                  if (attributes != null)
                  {
                     attrs = attributes.getAll();
                     while (attrs != null && attrs.hasMore())
                     {
                        Attribute attribute = (Attribute) attrs.next();
                        if (attribute.getID().equals(roleAttrName))
                        {
                           if (!m_roleProvider.isDelimited())
                              roleSet.addAll(getMultiValuedRoles(attribute));
                           else
                              roleSet.addAll(getDelimitedValuedRoles(attribute,
                                    m_roleProvider.getDelimiter()));
                        }
                     }
                     attrs.close();
                     attrs = null;
                  }
               }
               results.close();
               results = null;
               context.close();
               context = null;
            }
         }
         catch (NamingException e)
         {
            // print the error to the console and return an empty list
            Object args[] =
            {e.toString()};

            PSConsole.printMsg("Security",
                  IPSSecurityErrors.UNKNOWN_NAMING_ERROR, args);
         }
         finally
         {
            if (values != null)
               try
               {
                  values.close();
               }
               catch (NamingException e)
               {
               }
            if (attrs != null)
               try
               {
                  attrs.close();
               }
               catch (NamingException e)
               {
               }
            if (results != null)
               try
               {
                  results.close();
               }
               catch (NamingException e)
               {
               }
            if (context != null)
               try
               {
                  context.close();
               }
               catch (NamingException e)
               { /* noop */
               };
         }
      }

      roleList.addAll(roleSet);
      return roleList;
   }

   /**
    * Extract roles from a role attribute that stores values as a multi valued
    * attribute
    * 
    * @param attribute the attribute, assumed not <code>null</code>
    * @return a set of role values that can be merged into a larger role set
    * @param delimiter the delimiter string used to split the values in
    *           attribute, assumed not <code>null</code>
    * @throws NamingException
    */
   private Set getDelimitedValuedRoles(Attribute attribute, String delimiter)
         throws NamingException
   {
      String value = (String) attribute.get();
      String roles[] = value.split(delimiter);
      Set rval = new HashSet();
      for (int i = 0; i < roles.length; i++)
      {
         rval.add(roles[i]);
      }
      return rval;
   }

   /**
    * Extract roles from a role attribute that stores values as a multi valued
    * attribute
    * 
    * @param attribute the attribute, assumed not <code>null</code>
    * @return a set of role values that can be merged into a larger role set
    * @throws NamingException
    */
   private Set getMultiValuedRoles(Attribute attribute) throws NamingException
   {
      NamingEnumeration values;
      values = attribute.getAll();
      Set rval = new HashSet();
      while (values != null && values.hasMore())
      {
         String role = (String) values.next();
         if (role != null)
            rval.add(role);
      }
      values.close();
      values = null;
      return rval;
   }

   /** @see IPSInternalRoleCataloger */
   public Set getSubjects(String roleName, String subjectNameFilter)
   {
      return getSubjects(roleName, subjectNameFilter, 0, null, true);
   }

   /** @see IPSInternalRoleCataloger */
   public Set getSubjects(String roleName, String subjectNameFilter,
         int subjectType, String attributeNameFilter, boolean includeEmpty)
   {
      // groups not supported
      if (subjectType == PSSubject.SUBJECT_TYPE_GROUP)
         return new HashSet();
      
      // use treeset to prevent duplicates and enforce ordering
      Set results = new TreeSet(PSSubject.getSubjectIdentifierComparator());
      String plainRoleName = roleName;
      if (roleName!= null && m_roleProvider.isDelimited())
      {
         roleName = "*" + roleName + "*";
      }
      PSRoleProvider provider = getProvider();

      // handles all directory connection security providers
      if (provider.isDirectoryRoleProvider() || provider.isBothProvider())
      {
         PSDirectorySet directorySet = getDirectorySet();
         Map filterValues = new HashMap();
         if (!StringUtils.isBlank(roleName))
         {
            String roleAttrName = directorySet
               .getRequiredAttributeName(PSDirectorySet.ROLE_ATTRIBUTE_KEY);
            filterValues.put(roleAttrName, roleName);            
         }

         if (!StringUtils.isBlank(subjectNameFilter))
         {
            filterValues.put(getObjectAttributeName(), PSJndiUtils
               .translateSQLFilter(subjectNameFilter));
         }
         
         Set additionals = null;
         if (attributeNameFilter != null
               && attributeNameFilter.trim().length() > 0)
         {
            filterValues.put(attributeNameFilter, PSJndiUtils.WILDCARD_SEARCH);
            additionals = new HashSet();
            additionals.add(attributeNameFilter);
         }

         Iterator directories = getDirectories().values().iterator();
         while (directories.hasNext())
         {
            PSDirectoryDefinition directory = 
               (PSDirectoryDefinition) directories.next();

            if (provider.isDirectoryRoleProvider() || provider.isBothProvider())
               getSubjects(directory, filterValues, results, additionals);
         }
      }

      if (m_roleProvider.isDelimited())
      {
         results = filterResultsByRoleAttribute(plainRoleName, results);
      }

      // filter by subject type and weed out empty if requested
      results = filterByType(results, subjectType, includeEmpty);

      return results;
   }

   /**
    * Check each user in results to see that the given role is present
    * 
    * @param roleName the role name, assumed not <code>null</code>
    * @param results the original results, assumed not <code>null</code>
    * @return the new results
    */
   private Set filterResultsByRoleAttribute(String roleName, Set results)
   {
      Set rval = new HashSet();
      Iterator iter = results.iterator();
      while (iter.hasNext())
      {
         PSGlobalSubject user = (PSGlobalSubject) iter.next();
         List roles = getRoles(user.getName(), 0);
         if (roles.contains(roleName))
            rval.add(user);
      }
      return rval;
   }

   /** @see IPSInternalRoleCataloger */
   public PSRoleProvider getProvider()
   {
      return m_roleProvider;
   }

   /**
    * Get all subjects from the supplied directory.
    * 
    * @param directory the directory from which to get the subjects, assumed not
    *           <code>null</code>.
    * @param filterValues the filter values for the directory search, assumed
    *           not <code>null</code>.
    * @param results a set to collect all subjects, assumed not
    *           <code>null</code>.
    * @param additionals a set of additional attributes to be returned with each
    *           subject, may be <code>null</code>.
    */
   protected void getSubjects(PSDirectoryDefinition directory,
         Map filterValues, Set results, Set additionals)
   {
      results.addAll(getSubjects(directory, filterValues, additionals));
   }

   /**
    * Get the requestd attributes from the specified directory.
    * 
    * @param context The context to use, created using the supplied directory,
    *    assumed not <code>null</code>.
    * @param directory the directory in which to search, assumed not
    *           <code>null</code>.
    * @param filterValues a map with all filter attribute/value pairs. See
    *           {@link PSJndiUtils#buildFilter(Map)} for more info.
    * @param returnAttrs an array with attribute names specifying all attributes
    *           to be returned, <code>null</code> to return all attributes.
    * @return an enumeration with all found attributes as
    *         <code>SearchResult</code> objects, never <code>null</code>,
    *         may be empty.
    * @throws NamingException for any JNDI lookup error.
    */
   @SuppressFBWarnings("LDAP_INJECTION") //Mitigated in PSJndiUtils.buildFilter
   private NamingEnumeration getAttributes(DirContext context,
                                           PSDirectoryDefinition directory, Map filterValues, String[] returnAttrs)
   throws NamingException
   {
      context = createContext(directory);
      SearchControls searchControls = createSearchControls(directory
            .getDirectory(), returnAttrs);

      return context.search("", PSJndiUtils.buildFilter(filterValues),
            searchControls);
   }

   /**
    * The role providef used for this cataloger, initialized in constructor,
    * never <code>null<code> or never changed after that.
    */
   protected PSRoleProvider m_roleProvider = null;
}
