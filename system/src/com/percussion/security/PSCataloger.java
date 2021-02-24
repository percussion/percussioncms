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
package com.percussion.security;

import com.percussion.design.objectstore.IPSGroupProviderInstance;
import com.percussion.design.objectstore.PSAttribute;
import com.percussion.design.objectstore.PSAttributeList;
import com.percussion.design.objectstore.PSAuthentication;
import com.percussion.design.objectstore.PSDirectory;
import com.percussion.design.objectstore.PSDirectorySet;
import com.percussion.design.objectstore.PSGlobalSubject;
import com.percussion.design.objectstore.PSJndiGroupProviderInstance;
import com.percussion.design.objectstore.PSReference;
import com.percussion.design.objectstore.PSServerConfiguration;
import com.percussion.design.objectstore.PSSubject;
import com.percussion.server.PSServer;
import com.percussion.util.PSCollection;
import com.percussion.utils.exceptions.PSExceptionHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.apache.commons.lang.StringUtils;

/**
 * An abstract base class which holds all common functionality to all 
 * catalogers.
 */
@SuppressWarnings(value={"unchecked"})
public abstract class PSCataloger
{
   /**
    * Convenience constructor that calls 
    * {@link #PSCataloger(Properties, PSServerConfiguration)} with
    * <code>null<code> for the server configuration.
    */
   public PSCataloger(Properties properties)
   {
      this(properties, null);
   }

   /**
    * Constructs a new role cataloger for the supplied properties.
    * 
    * @param properties all catalogers require one property: a
    *    reference to the <code>PSDirectorySet</code> definition used as the
    *    catalog server, not <code>null</code> or empty.
    * @param config the server configuration with all directory definitions,
    *    may be <code>null<code> in which case this will get it through 
    *    <code>PSServer.getServerConfiguration()</code>.
    */
   public PSCataloger(Properties properties, PSServerConfiguration config)
   {
      if (properties == null || properties.isEmpty())
         throw new IllegalArgumentException(
            "properties cannot be null or empty");

      if (config == null)
         config = PSServer.getServerConfiguration();

      // load and validate the specified directory set
      String directorySetName =
         properties.getProperty(PSSecurityProvider.PROVIDER_NAME);
      initDirectorySet(directorySetName, config);
   }

   /**
    * Default constructor used for non directory catalogers such as
    * <code>PSBackEdnDirectoryCataloger</code>.
    *
    */
   protected PSCataloger()
   {
   }

   /**
    * Creates a new context for the supplied directory.
    * 
    * @param directory the directory for which to create the context, assumed
    *    not <code>null<code>.
    * @return a new directory context, never <code>null<code>. The caller is
    *    responsible to close it.
    * @throws NamingException if anything goes wrong creating the new context.
    */
   protected DirContext createContext(PSDirectoryDefinition directory)
      throws NamingException
   {
      return PSJndiUtils.createContext(directory);
   }

   /**
    * Create new search controls for the supplied parameters.
    * 
    * @param directory the directory for which to create the search controls
    *    for, assumed not <code>null<code>.
    * @param returnAttrs an array with all attribute names that will be 
    *    returned, <code>null<code> to return all attributes, empty to return
    *    no attributes.
    * @return a search control object, never <code>null<code>.
    */
   protected SearchControls createSearchControls(PSDirectory directory,
      String[] returnAttrs)
   {
      return PSJndiUtils.createSearchControls(directory, returnAttrs);
   }

   /**
    * Get the directory set associated with this cataloger.
    * 
    * @return the directory set used with this cataloger, may be
    *         <code>null</code> if this cataloger is not a directory
    *         cataloger.
    */
   protected PSDirectorySet getDirectorySet()
   {
      return m_directorySet;
   }

   /**
    * Get all directory definitions.
    * 
    * @return a map of <code>PSDirectoryDefinition</code> objects. The key is 
    *    the directory name as <code>String</code>. Initialized in constructor, 
    *    never <code>null</code> or changed after that.
    */
   protected Map getDirectories()
   {
      return m_directories;
   }

   /**
    * Initialize the directory set for the specified name.
    * 
    * @param name the name of the directory set to be initialized, 
    *    not <code>null</code> or empty.
    * @param config the server configuration, not <code>null</code>.
    * @throws PSSecurityException if any referenced directory element cannot 
    *    be found.
    */
   protected void initDirectorySet(String name, PSServerConfiguration config)
      throws PSSecurityException
   {
      if (name == null)
         throw new IllegalArgumentException("name cannot be null");

      name = name.trim();
      if (name.length() == 0)
         throw new IllegalArgumentException("name cannot be empty");

      if (config == null)
         throw new IllegalArgumentException("config cannot be null");

      m_directorySet = config.getDirectorySet(name);
      if (m_directorySet == null)
      {
         Object[] args =
         {
            name
         };
         
         throw new PSSecurityException(
            IPSSecurityErrors.DIR_REFERENCED_DIRECTORYSET_NOT_FOUND, args);
      }

      // load and validate all referenced directories/authentications
      Iterator directoryRefs = m_directorySet.iterator();
      while (directoryRefs.hasNext())
      {
         PSReference directoryRef = (PSReference) directoryRefs.next();
         PSDirectory directory = config.getDirectory(directoryRef.getName());
         if (directory == null)
         {
            Object[] args =
            {
               directoryRef.getName()
            };
         
            throw new PSSecurityException(
               IPSSecurityErrors.DIR_REFERENCED_DIRECTORY_NOT_FOUND, args);
         }

         PSAuthentication authentication =
            config.getAuthentication(
               directory.getAuthenticationRef().getName());
         if (authentication == null)
         {
            Object[] args =
            {
               directory.getAuthenticationRef().getName()
            };
         
            throw new PSSecurityException(
               IPSSecurityErrors.DIR_REFERENCED_AUTHENTICATION_NOT_FOUND, args);
         }
         
         directory = ensureReturnAttribute(directory, getEmailAddressAttributeName());

         PSDirectoryDefinition dirDef = new PSDirectoryDefinition(
            authentication, directory);
         m_directories.put(directoryRef.getName(), dirDef);
         
         // instantiate and add any group providers
         Map<String, IPSGroupProviderInstance> groupProvInstances = 
            getGroupProviderInstances(config);
         Iterator<String> groupNames = directory.getGroupProviderNames();
         while (groupNames.hasNext())
         {
            String groupName = groupNames.next();
            final IPSGroupProviderInstance gpInst =
                  groupProvInstances.get(groupName);

            if (gpInst == null)
            {
               Object[] args = {groupName};
               throw new PSSecurityException(
                  IPSSecurityErrors.GROUP_PROVIDER_MISSING, args);
            }

            int type = gpInst.getType();
            if (type == PSSecurityProvider.SP_TYPE_DIRCONN)
            {
               IPSGroupProvider gp;
               try
               {
                  gp = new PSJndiGroupProvider(
                     (PSJndiGroupProviderInstance)gpInst, dirDef, 
                     m_directorySet.getRequiredAttributeName(
                        PSDirectorySet.OBJECT_ATTRIBUTE_KEY));
               }
               catch (NamingException e)
               {
                  throw new PSSecurityException(e.toString());
               }
               addGroupProvider(gp);
            }
            else
            {
               Object[] args = {groupName};
               throw new PSSecurityException(
                  IPSSecurityErrors.GROUP_PROVIDER_MISSING, args);
            }
         }
      }
   }

   /**
    * Ensures the supplied directory returns the specified attribute, cloning
    * the directory and adding the attribute if necessary.
    * 
    * @param directory The directory, assumed not <code>null</code>.
    * @param attrName the attribute name, if <code>null</code> or empty this
    * method simply returns the supplied directory.
    * 
    * @return The cloned directory if the attribute needed to be added, 
    * otherwise the supplied directory if the attribute as already specified.
    */
   private PSDirectory ensureReturnAttribute(PSDirectory directory, 
      String attrName)
   {
      PSDirectory result = directory;
      PSCollection attributes = directory.getAttributes();
      if (!StringUtils.isBlank(attrName))
      {
         if (attributes == null)
            attributes = new PSCollection(String.class);
         else
            attributes = new PSCollection(
               directory.getAttributes().iterator());
         attributes.add(attrName);
         result = (PSDirectory) directory.clone();
         result.setAttributes(attributes);
      }
      
      return result;
   }

   /** @see IPSDirectoryCataloger */
   public String getEmailAddressAttributeName()
   {
      String attributeName = null;
      
      PSDirectorySet directorySet = getDirectorySet();
      if (directorySet != null)
         attributeName = directorySet.getRequiredAttributeName(
            PSDirectorySet.EMAIL_ATTRIBUTE_KEY);
      
      return attributeName;
   }

   /** @see IPSDirectoryCataloger */
   public String getObjectAttributeName()
   {
      return getDirectorySet().getRequiredAttributeName(
         PSDirectorySet.OBJECT_ATTRIBUTE_KEY);
   }

   /**
    * Creates a new <code>PSSubject</code> for the supplied search result.
    * 
    * @param subject the search result for which to create a new subject,
    *    not <code>null</code>, must be of valid object type.
    * @param requestedReturns The requested attributes to include with the 
    * subject, never <code>null</code>, may be empty.
    * @return the new created subject, never <code>null</code>.
    * @throws NamingException for any naming errors and if the supplied
    *    searcch result is not of the required object type.
    */
   protected PSSubject createSubject(SearchResult subject, 
      Set<String> requestedReturns) throws NamingException
   {
      if (subject == null)
         throw new IllegalArgumentException("subject cannot be null");
      
      if (requestedReturns == null)
         throw new IllegalArgumentException("requestedReturns may not be null");

      Attribute objectAttribute =
         subject.getAttributes().get(getObjectAttributeName());
      if (objectAttribute == null)
         throw new NamingException("invalid SearchResult subject object");

      String uid = objectAttribute.get(0).toString();

      PSAttributeList attributes = new PSAttributeList();
      NamingEnumeration attrs = null;
      NamingEnumeration attrValues = null;
      try
      {
         attrs = subject.getAttributes().getAll();
         while (attrs.hasMore())
         {
            Attribute attr = (Attribute) attrs.next();

            String attrName = attr.getID();
            if (!requestedReturns.contains(attrName))
               continue;
            PSAttribute attribute = new PSAttribute(attrName);

            List attributeValues = new ArrayList();
            attrValues = attr.getAll();
            while (attrValues.hasMore())
            {
               attributeValues.add(attrValues.next());
            }
            attrValues.close();
            attrValues = null;
            attribute.setValues(attributeValues);

            attributes.add(attribute);
         }

         return new PSGlobalSubject(uid, PSSubject.SUBJECT_TYPE_USER, 
            attributes);         
      }
      finally
      {
         if (attrValues != null)
            try {attrValues.close();} catch (NamingException e){}
         if (attrs != null)
            try {attrs.close();} catch (NamingException e){}
      }
   }

   
   /**
    * Filters the supplied set of <code>PSSubject</code> objects by subject
    * type and weeds out subjects that have no attributes if so requested.
    * 
    * @param subjects a set of <code>PSSubject</code> objects that needs to
    *    be filtered by community, not <code>null</code>, may be empty.
    * @param type one of the <code>PSSubject.SUBJECT_TYPE_xxx</code> 
    *    flags to filter the results by type, provide 0 to return all types.
    * @param includeEmpty <code>true</code> to include subjects with an empty
    *    attribute list in the result, <code>false</code> otherwise.
    * @return a by subject type filtered set of <code>PSSubject</code> objects,
    *    never <code>null</code>, may be empty.
    */
   protected Set filterByType(Set subjects, int type, boolean includeEmpty)
   {
      if (type != 0 || !includeEmpty)
      {
         List subjectList = new ArrayList(subjects);
         for (int i=0; i<subjectList.size(); i++)
         {
            PSSubject subject = (PSSubject) subjectList.get(i);
            if ((type != 0 && subject.getType() != type) ||
               (!includeEmpty && subject.getAttributes().size() == 0))
               subjects.remove(subject);
         }
      }

      return subjects;
   }

   /**
    * Add's a group provider to this directory provider.  
    *
    * @param groupProvider the provider, never <code>null</code>.
    *
    * @throws IllegalArgumentException if groupProvider is <code>null</code> or
    * of the wrong type.
    */
   private void addGroupProvider(IPSGroupProvider groupProvider)
   {
      if (groupProvider == null)
         throw new IllegalArgumentException("groupProvider may not be null");

      m_groupProviders.add(groupProvider);
   }

   /**
    * Get this provider's list of IPSGroupProvider objects.
    *
    * @return an iterator over zero or more IPSGroupProvider objects.  Never
    * <code>null</code>.
    */
   public Iterator<IPSGroupProvider> getGroupProviders()
   {
      return m_groupProviders.iterator();
   }   
   
   /**
    * Retrieves a Map of <code>IPSGroupProviderInstance</code> objects from the
    * supplied config matching the specified type.
    *
    * @param config The server config that supplies the group provider instance
    * objects, assumed not <code>null</code>.
    *
    * @return A Map of zero or more <code>IPSGroupProviderInstance</code>
    * objects, never <code>null</code>.  The key is the name of the provider as
    * a String, and the value a non-<code>null IPSGroupProviderInstance</code>.
    */
   private static Map<String, IPSGroupProviderInstance> 
      getGroupProviderInstances(PSServerConfiguration config)
   {
      Map<String, IPSGroupProviderInstance> result = 
         new HashMap<>();
      Iterator i = config.getGroupProviderInstances().iterator();
      while (i.hasNext())
      {
         IPSGroupProviderInstance inst = (IPSGroupProviderInstance)i.next();
         result.put(inst.getName(), inst);
      }

      return result;
   }   

   /**
    * Get all subjects for the specified parameters.
    * 
    * @param directory the directory in which to search for the subjects,
    *    assumed not <code>null</code>.
    * @param filter a map with name/values pairs by which the search is
    *    filtered, assumed not <code>null</code>.  Value may be a 
    *    <code>String</code> or <code>List</code> of strings.
    * @param attributeNames a list with attribute names to be returned with
    *    each subject in the result, may be <code>null</code> to return all
    *    attributes.
    * @return a collection with <code>PSSubject</code> objects, each subject 
    *    with the requested attributes. Never <code>null</code>, may be empty.
    */
   protected Collection getSubjects(PSDirectoryDefinition directory, Map filter, 
      Collection attributeNames)
   {
      Collection resultList = new ArrayList();
   
      DirContext context = null;
      NamingEnumeration results = null;
   
      try
      {
         context = createContext(directory);
   
         Set additionals = new HashSet();
         if (attributeNames != null)
            additionals.addAll(attributeNames);
         Set<String> requestedReturns = directory.getReturnAttributeNames(
            additionals);
         Set<String> returns = new HashSet<>(requestedReturns);
         returns.add(getObjectAttributeName());
         String[] returnAttrs = returns.toArray(new String[returns.size()]);
   
         SearchControls searchControls = createSearchControls(
            directory.getDirectory(), returnAttrs);
   
         Map values = new HashMap();
         values.put(PSJndiProvider.OBJECT_CLASS_ATTR, 
            PSJndiProvider.OBJECT_CLASS_PERSON_VAL);
         values.putAll(filter);

         results = context.search("", PSJndiUtils.buildFilter(values),
            searchControls);
         while (results.hasMore())
         {
            resultList.add(createSubject((SearchResult) results.next(), 
               requestedReturns));
         } 
         
         return resultList;
      }
      catch (NamingException e)
      {
         Object args[] = {e.toString()};
         Throwable rootCause = PSExceptionHelper.findRootCause(e,false);
   
         throw new PSSecurityException(IPSSecurityErrors.UNKNOWN_NAMING_ERROR,
            args, rootCause);
      }
      finally
      {
         if (results != null)
            try {results.close();} catch (NamingException e) {}
         
         if (context != null)
            try {context.close();} catch (NamingException e) {}
      }
   }

   /**
    * The directory set used for this cataloger, initialized in constructor,
    * may be <code>null<code> but never changed after that.
    */
   protected PSDirectorySet m_directorySet = null;

   /**
    * A map of <code>PSDirectoryDefinition</code> objects. The key is the 
    * directory name as <code>String</code>. Initialized in constructor, never
    * <code>null</code> or changed after that.
    */
   protected Map m_directories = new HashMap();
   
   
   /**
    * List of IPSGroupProvider objects used by this security provider,
    * never <code>null</code> after construction, may be empty.
    */
   private List<IPSGroupProvider> m_groupProviders = 
      new ArrayList<>();
}
