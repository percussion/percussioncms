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

import com.percussion.design.objectstore.IPSReplacementValue;
import com.percussion.design.objectstore.PSAttributeList;
import com.percussion.design.objectstore.PSConditional;
import com.percussion.design.objectstore.PSDirectory;
import com.percussion.design.objectstore.PSLiteral;
import com.percussion.design.objectstore.PSRelativeSubject;
import com.percussion.design.objectstore.PSServerConfiguration;
import com.percussion.design.objectstore.PSSubject;
import com.percussion.error.PSErrorManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.apache.commons.lang.StringUtils;

/**
 * An abstact base class providing default implementations and helper
 * functionality for <code>IPSDirectoryCataloger</code> implementations.
 */
public abstract class PSDirectoryCataloger extends PSCataloger 
   implements IPSDirectoryCataloger
{
   /** @see PSCataloger */
   public PSDirectoryCataloger(Properties properties)
   {
      super(properties);
   }
   
   /** @see PSCataloger */
   public PSDirectoryCataloger(Properties properties, 
      PSServerConfiguration config)
   {
      super(properties, config);
   }
   
   /** @see PSCataloger */
   protected PSDirectoryCataloger()
   {
   }
   
   /** @see IPSDirectoryCataloger */
  public void setName(String name)
   {
      if (StringUtils.isBlank(name))
         throw new IllegalArgumentException("name may not be null or empty");
      
      m_name = name;
   }

  /** @see IPSDirectoryCataloger */
   public String getName()
   {
      return m_name;
   }

   /** @see IPSDirectoryCataloger */
   public String getEmailAddress(String userName)
   {
      return getEmailAddress(createSubject(userName));
   }

   /** @see IPSDirectoryCataloger */
   public String getEmailAddress(PSSubject user)
   {
      String emailAttributeName = getEmailAddressAttributeName();
      if (emailAttributeName == null)
         throw new UnsupportedOperationException(PSErrorManager.getErrorText(
            IPSSecurityErrors.NO_EMAIL_ATTRIBUTE_NAME));

      return getAttribute(user, emailAttributeName);
   }

   /** @see IPSDirectoryCataloger */
   public String getAttribute(String userName, String attributeName)
   {
      return getAttribute(createSubject(userName), attributeName);
   }

   /** @see IPSDirectoryCataloger */
   public PSSubject getAttributes(String userName, Collection attributeNames)
   {
      return getAttributes(createSubject(userName), attributeNames);
   }
   
   /** @see IPSDirectoryCataloger */
   public PSSubject getAttributes(String userName)
   {
      return getAttributes(createSubject(userName));
   }
   
   /**
    * Creates a new <code>PSSubject</code> of type
    * <code>PSSubject.SUBJECT_TYPE_USER</code> for the supplied parameters.
    *
    * @param userName the user name to create the subject for, not
    *    <code>null</code> or empty.
    * @return the requested subject as <code>PSRelativeSubject</code> object
    *    with an empty attribute list, never <code>null</code>.
    */
   protected static PSSubject createSubject(String userName)
   {
      if (userName == null)
         throw new IllegalArgumentException("userName cannot be null");

      userName = userName.trim();
      if (userName.length() == 0)
         throw new IllegalArgumentException("userName cannot be empty");

      PSAttributeList attributes = new PSAttributeList();
      PSSubject subject = new PSRelativeSubject(userName,
         PSSubject.SUBJECT_TYPE_USER, attributes);

      return subject;
   }
   
   /**
    * Creates a filter map for the supplied criteria.
    * 
    * @param criteria the conditional to create the filter map for. Only 
    *    <code>PSLiteral</code> types are allowed for variable and value and
    *    only the <code>OPTYPE_EQUALS</code> and <code>OPTYPE_LIKE</code>  
    *    operators are allowed.
    * @return a map with a filter value, the criteria variable as key and the
    *    criteria value as value. Never <code>null</code>, may be empty.
    */
   protected Map<String, String> createFilter(PSConditional criteria)
   {
      String variable = null;
      String value = null;
      if (criteria != null)
      {
         IPSReplacementValue var = criteria.getVariable();
         if (!(var instanceof PSLiteral))
            throw new IllegalArgumentException(
               "criteria variable must be of type PSLiteral");
         variable = var.getValueText();
         
         IPSReplacementValue val = criteria.getValue();
         if (!(val instanceof PSLiteral))
            throw new IllegalArgumentException(
               "criteria variable must be of type PSLiteral");
         value = val.getValueText();
         
         String operator = criteria.getOperator();
         if (!operator.equals(PSConditional.OPTYPE_EQUALS) &&
            !operator.equals(PSConditional.OPTYPE_LIKE))
            throw new IllegalArgumentException(
               "criteria operator must be OPTYPE_EQUALS or OPTYPE_LIKE");
      }
      
      Map<String, String> filter = new HashMap<>();
      if (variable != null)
         filter.put(variable, value);
         
      return filter;
   }

   /**
    * Get the first attribute found for the supplied attribute name and user
    * information from the provided directory.
    * 
    * @param directory the directory in which to search for the requested
    *    attribute, assumed not <code>null<code>.
    * @param user the name of the user for which to search the attribute,
    *    assumed not <code>null<code>.
    * @param userAttributeName the attribute name that holds the user name
    *    as supplied, assumed not <code>null<code>.
    * @param attributeName the name of the attribute to look for, assumed
    *    not <code>null<code>.
    * @return the first attribute value if found, <code>null<code> otherwise.
    */
   protected String getAttribute(PSDirectoryDefinition directory, String user, 
      String userAttributeName, String attributeName)
   {
      String resultString = null;
      DirContext context = null;
      NamingEnumeration results = null;
      try
      {
         context = createContext(directory);

         String[] returnAttrs = new String[1];
         returnAttrs[0] = attributeName;
         SearchControls searchControls = createSearchControls(
            directory.getDirectory(), returnAttrs);

         Map values = new HashMap();
         values.put(userAttributeName, user);
         values.put(attributeName, null);

         results = context.search("", PSJndiUtils.buildFilter(values), 
            searchControls);
         while (results.hasMore())
         {
            SearchResult result = (SearchResult) results.next();
            
            Attribute attr = 
               (Attribute) result.getAttributes().get(attributeName);
            if (attr != null)
            {
               resultString = attr.get(0).toString();
               break;
            }
         }
      }
      catch (NamingException e)
      {
         Object args[] = {e.toString()};
         throw new PSSecurityException(IPSSecurityErrors.UNKNOWN_NAMING_ERROR,
            args);
      }
      finally
      {
         if (results != null)
            try { results.close(); } catch (NamingException e) { /* noop */ };
         
         if (context != null)
            try { context.close(); } catch (NamingException e) { /* noop */ };
      }
      
      return resultString;
   }

   /**
    * Get the first attribute found for the supplied attribute name and user
    * information from the provided directory.
    * 
    * @param directory the directory in which to search for the requested
    *    attributes, assumed not <code>null<code>.
    * @param user the name of the user for which to search the attributes,
    *    assumed not <code>null<code>.
    * @param userAttributeName the attribute name that holds the user name
    *    as supplied, assumed not <code>null<code>.
    * @param returnAttrs an array with all attribute names for which to
    *    return the values, <code>null<code> or empty to return all known
    *    attributes.
    * @param searchResults a map in which all search results will be collected,
    *    assumed not <code>null<code>.
    */
   protected void getAttributes(PSDirectoryDefinition directory, String user, 
      String userAttributeName, String[] returnAttrs, Map searchResults)
   {
      DirContext context = null;
      NamingEnumeration results = null;
      NamingEnumeration attributes = null;
      NamingEnumeration values = null;
      try
      {
         context = createContext(directory);

         PSDirectory dir = directory.getDirectory();
         SearchControls searchControls = createSearchControls(dir, returnAttrs);

         Map filterValues = new HashMap();
         filterValues.put(userAttributeName, user);

         results = context.search("",
            PSJndiUtils.buildFilter(filterValues), searchControls);
            
         while (results.hasMore())
         {
            SearchResult result = (SearchResult) results.next();
            attributes = result.getAttributes().getAll();
            while (attributes.hasMore())
            {
               Attribute attribute = (Attribute) attributes.next();
               
               List resultValues = (List) searchResults.get(attribute.getID());
               if (resultValues == null)
               {
                  resultValues = new ArrayList();
                  searchResults.put(attribute.getID(), resultValues);
               }
               values = attribute.getAll();
               while (values.hasMore())
               {
                  Object value = values.next();
                  if (value != null)
                     resultValues.add(value.toString());
               }
               values.close();
               values = null;
            }
            attributes.close();
            attributes = null;
         }
      }
      catch (NamingException e)
      {
      Object args[] =
      {
         "Ldap Authentication="+directory.getAuthentication().getName() +" userAttribute="+userAttributeName+" error="+e.toString()
      };
      
      throw new PSSecurityException(IPSSecurityErrors.UNKNOWN_NAMING_ERROR, 
         args);
      }
      finally
      {
         if (values != null)
            try { values.close(); } catch (NamingException e) { /* noop */ };
            
         if (attributes != null)
            try { attributes.close(); } catch (NamingException e) { /* noop */ };
            
         if (results != null)
            try { results.close(); } catch (NamingException e) { /* noop */ };
            
         if (context != null)
            try { context.close(); } catch (NamingException e) { /* noop */ };
      }
   }
   
   /**
    * Name set by {@link #setName(String)}, may be <code>null</code> if
    * not set, never empty.
    */
   private String m_name;
}
