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

import com.percussion.data.PSInternalRequestCallException;
import com.percussion.design.objectstore.IPSObjectStoreErrors;
import com.percussion.design.objectstore.PSAttribute;
import com.percussion.design.objectstore.PSAttributeList;
import com.percussion.design.objectstore.PSGlobalSubject;
import com.percussion.design.objectstore.PSSubject;
import com.percussion.server.PSInternalRequest;
import com.percussion.server.PSRequest;
import com.percussion.server.PSServer;
import com.percussion.services.security.PSRoleMgrLocator;
import com.percussion.util.PSStringComparator;
import com.percussion.xml.PSXmlTreeWalker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Base class which holds common backend catalog functionality used in
 * security classes.
 */
@SuppressWarnings(value={"unchecked"})
public class PSBackendCataloger
{
   /**
    * Convenience method that call {@link #getRoleAttributes(String, String) 
    * getRoleAttributes(String, null)}.
    */
   public static List getRoleAttributes(String roleName)
   {
      if (roleName == null)
         throw new IllegalArgumentException("roleName cannot be null");
      
      roleName = roleName.trim();
      if (roleName.length() == 0)
         throw new IllegalArgumentException("roleName cannot be empty");
         
      return getRoleAttributes(roleName, null);
   }


   /**
    * Provides a list of subjects with attributes that match any supplied
    * filters.
    * <p>
    * This method may be slower when called with <code>includeEmptySubjects =
    * true</code> than when called with <code>false</code>.
    *
    * @param subjectNameFilter The individual whose attributes you wish.
    *    Wildcards allowed following SQL LIKE syntax. If <code>null</code> or
    *    empty, all subjects are included.
    * @param subjectType One of the PSSubject.SUBJECT_TYPE_xxx flags.
    *    Provide 0 to ignore this property.
    * @param roleName If <code>null</code> only global subject attributes are
    *    returned. Otherwise, both role specific and global attributes are
    *    returned. If an attribute occurs in both the global and role list, the
    *    role specific one will be returned. Wildcards not allowed.
    * @param attributeNameFilter  A single pattern used to select the desired
    *    attributes. Use SQL LIKE syntax. Supply empty or <code>null</code> to
    *    get all attributes.
    * @param includeEmptySubjects A flag to indicate whether subjects with
    *    no attributes should be included in the returned list. If <code>
    *    true</code>, they are included, otherwise, only subjects that
    *    have 1 or more attributes are included.
    * @return a valid list of 0 or more PSSubjects containing either 1 or more
    *    attributes (if includeEmptySubjects is <code>false</code>) or 0 or
    *    more attributes (if includeEmptySubjects is <code>true</code>),
    *    ordered in ascending alpha order by subject name. The caller
    *    takes ownership of the list.
    */
   public static List getSubjectGlobalAttributes(String subjectNameFilter,
      int subjectType, String roleName, String attributeNameFilter,
      boolean includeEmptySubjects)
   {
      Set subjects = new HashSet();
      
      HashMap filters = new HashMap();
      if (null != subjectNameFilter && subjectNameFilter.trim().length() > 0)
         filters.put(FILTER_SUBJECT_NAME, subjectNameFilter);

      if (0 != subjectType)
         filters.put("sys_subjectType", "" + subjectType);

      if (null != roleName && roleName.trim().length() > 0)
         filters.put(FILTER_ROLE_NAME, roleName);

      if (null != attributeNameFilter && 
         attributeNameFilter.trim().length() > 0)
         filters.put(FILTER_ATTRIBUTE_NAME, attributeNameFilter);

      String requestPage;
      if (includeEmptySubjects)
         // this request outer joins the attribute tables
         requestPage = "sys_catalogOuterJoinSubjectAttributes";
      else
         // this request inner joins the attribute tables
         requestPage = "sys_catalogSubjectAttributes";
         
      Document doc = getCatalogDocument(requestPage, filters);
      List results = processSubjectAttributes(doc, includeEmptySubjects);
         
      // add all subjects to the result
      subjects.addAll(results);

      return new ArrayList(subjects);
   }

   /**
    * Gets only the role specific attributes for a set of subjects. A subject
    * will not be included in the returned list if it does not have a role
    * attribute that matches the <code>attributeNameFilter</code>.
    * 
    * @param subjectNameFilter the subject name filter for which to get the
    *    role attributes, may be <code>null</code> or empty. This filter is
    *    ignored if <code>null</code> or empty.
    * @param subjectType One of the <code>PSSubject.SUBJECT_TYPE_xxx</code> 
    *    flags. Provide 0 to ignore this property. If o is provided, the
    *    type <code>PSSubject.SUBJECT_TYPE_USER</code> is used as default.
    * @param roleName the name of the role for which to filter the attributes,
    *    not <code>null</code> or empty.
    * @param attributeNameFilter an attribute name based on which to filter the
    *    results, may be <code>null</code> or empty in which case this filter
    *    is ignored.
    * @return the subject specific role attributes in a list of 
    *    <code>PSSubject</code> objects, never <code>null</code>, may be empty.
    */
   public static List getSubjectRoleAttributes(String subjectNameFilter, 
      int subjectType, String roleName, String attributeNameFilter)
   {
      if (roleName == null)
         throw new IllegalArgumentException("roleName cannot be null");
         
      roleName = roleName.trim();
      if (roleName.length() == 0)
         throw new IllegalArgumentException("roleName cannot be empty");
         
      HashMap filters = new HashMap();
      if (null != subjectNameFilter && subjectNameFilter.trim().length() > 0)
         filters.put(FILTER_SUBJECT_NAME, subjectNameFilter);

      filters.put(FILTER_ROLE_NAME, roleName);

      if (0 == subjectType)
      {
         subjectType = PSSubject.SUBJECT_TYPE_USER;
      }
      filters.put("sys_subjectType", "" + subjectType);

      if (null != attributeNameFilter && 
         attributeNameFilter.trim().length() > 0)
      {
         filters.put(FILTER_ATTRIBUTE_NAME, attributeNameFilter);
      }

      Document doc = getCatalogDocument("sys_catalogRoleSubjectAttributes",
         filters);
      List subjects = processSubjectAttributes(doc, false);
      if (null == subjects)
         subjects = new ArrayList();
         
      return subjects;
   }
   
   /**
    * Dynamically gets the attributes for the specified role. If the role
    * doesn't exist, an empty collection is returned.
    *
    * @param roleName The name of the role for which you want attributes.
    *    Not <code>null</code> or empty.
    * @param attributeNameFilter an attribute name on which to filter the
    *    results, may be <code>null</code> or empty.
    * @return a valid list of 0 or more <code>PSAttribute</code> objects. They 
    *    are ordered in ascending alpha order by attribute name. The caller 
    *    takes ownership of the list.
    */
   protected static List getRoleAttributes(String roleName, String attributeNameFilter)
   {
      if (roleName == null || roleName.trim().length() == 0)
         throw new IllegalArgumentException("roleName cannot be null or empty");
         
      HashMap filters = new HashMap();
      filters.put(FILTER_ROLE_NAME, roleName);

      if (null != attributeNameFilter && 
         attributeNameFilter.trim().length() > 0)
         filters.put(FILTER_ATTRIBUTE_NAME, attributeNameFilter);

      Document doc = getCatalogDocument("sys_catalogRoleAttributes", filters);

      PSXmlTreeWalker tree = new PSXmlTreeWalker(doc);

      List results = new ArrayList();
      if (null == tree.getCurrent())
         return results;

      // we only expect a single role back, so no for loop
      Element el = tree.getNextElement("Role", ms_firstFlags);
      if (null == el)
      {
         Object[] args = { "Roles", "Role", "Element is missing" };
         throw new PSSecurityException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
      }

      PSAttributeList attribs = getAttributes(el);
      Iterator iter = attribs.iterator();
      while (iter.hasNext())
         results.add(iter.next());
         
      return results;
   }

   /**
    * Scans the supplied element for nodes matching the DTD specified in the
    * sys_rolesCataloger document for attributes. It creates objects out of any
    * that it finds and returns them in a list.
    *
    * @param parent The element containing the attribute nodes. Not
    *    <code>null</code>.
    * @return An object containing all the found attributes as a
    *    <code>PSAttributeList</code>. It will contain 0 or more attributes. 
    *    Never <code>null</code>.
    * @throws PSSecurityException If the attribute name is not found.
    */
   protected static PSAttributeList getAttributes(Element parent)
   {
      if (parent == null)
         throw new IllegalArgumentException("parent cannot be null");
         
      PSAttributeList results = new PSAttributeList();
      PSXmlTreeWalker tree = new PSXmlTreeWalker(parent);

      Element el = tree.getNextElement("Attribute", ms_firstFlags);

      if (null == el)
         return results;

      for (; null!=el; el=tree.getNextElement("Attribute", ms_nextFlags))
      {
         String name = tree.getElementData("@name", false);
         if ((name == null) || (name.trim().length() == 0))
         {
            Object[] args = { "Attribute", "@name", "null" };
            throw new PSSecurityException(
                  IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
         }

         List values = getValues(el);
         results.setAttribute(name, values);
      }

      return results;
   }

   /**
    * Scans the supplied element for nodes matching the DTD specified in the
    * sys_rolesCataloger document for attribute values, then builds a list
    * containing all of the values as Strings.
    *
    * @param parent The element containing the attribute values. Not
    *    <code>null</code>.
    * @return A list of all the Value nodes found as children of the supplied
    *    parent, as Strings. If no values are found, <code>null</code> is
    *    returned. The entries may be <code>null</code> or empty.
    */
   protected static List getValues(Element parent)
   {
      if (parent == null)
         throw new IllegalArgumentException("parent cannot be null");
         
      List results = null;
      PSXmlTreeWalker tree = new PSXmlTreeWalker(parent);

      Element el = tree.getNextElement("Value", ms_firstFlags);

      if (null == el)
         return results;

      results = new ArrayList();
      for (; null!=el; el=tree.getNextElement("Value", ms_nextFlags))
      {
         String value = PSXmlTreeWalker.getElementData(el);
         results.add(value);
      }

      return results;
   }

   /**
    * Makes an internal request to the resource whose dataset name is
    * supplied. Every entry in the filter is added as an Http parameter.
    *
    * @param datasetName The internal name of the resource that the query
    *    will be made against. The name must be for a query resource or an
    *    exception is thrown. Not <code>null</code> or empty.
    * @param filters Each entry in this map will be added to the request's
    *    html parameter map. This method takes ownership of the map.
    *    Note: I use a HashMap instead of a Map because that's what the
    *    underlying object requires and I don't want to copy all of the entries
    *    to a new one. May be <code>null</code> or empty.
    * @return The document containing all of the objects that meet the filter
    *    criteria. If no objects match, an empty (no elements) document is
    *    returned.
    * @throws PSSecurityException If a handler for the specified
    *    resource could not be found, it is not a query resource, security
    *    on the app failed (the app is corrupted) or the actual request failed.
    */
   protected static Document getCatalogDocument(String datasetName, 
      HashMap filters)
   {
      if (datasetName == null || datasetName.trim().length() == 0)
         throw new IllegalArgumentException(
            "datasetName cannot be null or empty");
            
      try
      {
         String path = CATALOGER_APPNAME + "/" + datasetName;
         PSInternalRequest ir = PSServer.getInternalRequest(path, 
            PSRequest.getContextForRequest(), filters, false);
         
         return ir.getResultDoc();
      } 
      catch (PSInternalRequestCallException e)
      {
         throw new PSSecurityException(e);
      } 
   }
   
   /**
    * Returns a list with all roles to which the supplied subject belongs.
    *
    * @param subjectName the name of the subject to search for. If
    *    <code>null</code>, all roles are returned regardless of whether they
    *    contain any subjects, ignoring the other parameter values when
    *    searching. If empty, this property is ignored, but roles are still
    *    searched using the other parameter values.
    * @param subjectType one of the PSSubject.SUBJECT_TYPE_xxx types, or 0 to
    *    ignore this property.
    * @return a valid list of 0 or more <code>Strings</code>, each naming a 
    *    role. The list will not contain duplicates.
    */
   public static List getRhythmyxRoles(String subjectName, int subjectType)
   {
      // use a set to remove duplicates
      Set<String> resultSet = new HashSet<String>(); 
      resultSet.addAll(
         PSRoleMgrLocator.getBackEndRoleManager().getRhythmyxRoles(subjectName, 
            subjectType));

      // make sure we return an alpha ordered list
      List resultList =  new ArrayList(resultSet);
      Collections.sort(resultList, new PSStringComparator(
         PSStringComparator.SORT_CASE_INSENSITIVE_ASC));
      
      return resultList;
   }



   /**
    * Takes the document returned for sys_catalogSubjectAttributes or
    * sys_catalogSubjectRoleAttributes and builds the corresponding objects
    * from it.
    *
    * @param doc The document returned from the catalog, not 
    *    <code>null</code>.
    * @param includeEmptySubjects A flag to indicate whether subjects with
    *    no attributes should be included in the returned list. If <code>
    *    true</code>, they are included, otherwise, only subjects that
    *    have 1 or more attributes are included.
    * @return A valid list with 0 or more PSSubjects, each containing any
    *    attributes they have.
    * @throws PSSecurityException If the document is improperly formed.
    */
   protected static List processSubjectAttributes(Document doc, 
      boolean includeEmptySubjects)
   {
      if (doc == null)
         throw new IllegalArgumentException("doc cannot be null");
         
      Element el = doc.getDocumentElement();
      if (null == el)
         return new ArrayList();

      if (!el.getTagName().equals("Subjects"))
      {
         Object[] args = { "Subjects", el.getTagName() };
         throw new PSSecurityException(
               IPSObjectStoreErrors.XML_ELEMENT_WRONG_TYPE, args);
      }

      return new ArrayList(processCatalogedSubjects(el, includeEmptySubjects));
   }

   /**
    * Builds the appropriate objects from the data in the supplied document.
    * The document must meet the DTD as defined for the sys_roleCatalog app,
    * sys_catalogSubjects or sys_catalogSubjectAttributes resources. The
    * returned list can include subjects that only include attributes or all
    * subjects, based in the supplied flag.
    *
    * @param parent The XML element containing the Subject elements. If empty,
    *    <code>null</code> is returned. Otherwise, a list of PSSubjects
    *    are constructed to contain the attributes. Not <code>null
    *    </code>.
    * @param includeEmptySubjects A flag to indicate whether subjects with
    *    no attributes should be included in the returned list. If <code>
    *    true</code>, they are included, otherwise, only subjects that
    *    have 1 or more attributes are included.
    * @return A set with 0 or more PSSubject objects. Never <code>null
    *    </code>.
    * @throws PSSecurityException If any of a Subject's attributes are not
    *    valid.
    */
   protected static Set processCatalogedSubjects(Element parent, 
      boolean includeEmptySubjects)
   {
      if (parent == null)
         throw new IllegalArgumentException("parent cannot be null");
         
      // use set to remove dups introduced with the fix for Rx-01-10-0104
      TreeSet results = new TreeSet(PSSubject.getSubjectIdentifierComparator());

      Map<String, PSSubject> subjectMap = 
         new LinkedHashMap<String, PSSubject>();
      
      PSXmlTreeWalker tree = new PSXmlTreeWalker(parent);

      Element el = tree.getNextElement("Subject", ms_firstFlags);

      if (null == el)
         return results;

      for (; null!=el; el=tree.getNextElement("Subject", ms_nextFlags))
      {
         String name = tree.getElementData("@name", false);
         if ((name == null) || (name.trim().length() == 0))
         {
            Object[] args = { "Subject", "@name", "null" };
            throw new PSSecurityException(
                  IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
         }

         String type = tree.getElementData("@type", false);
         if ((type == null) || (type.trim().length() == 0) || 
            !(type.compareToIgnoreCase("" + PSSubject.SUBJECT_TYPE_USER) == 0 || 
            type.compareToIgnoreCase("" + PSSubject.SUBJECT_TYPE_GROUP) == 0))
         {
            Object[] args = { "Subject", "@type",
               "Invalid node in catalog doc (" + type + "),"
                  + " expected " + PSSubject.SUBJECT_TYPE_USER + " or "
                  + PSSubject.SUBJECT_TYPE_GROUP };
            throw new PSSecurityException(
                  IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
         }
         
         int subjectType = Integer.parseInt(type);

         PSAttributeList attrs = getAttributes(el);
         if (attrs.size() > 0 || includeEmptySubjects)
         {
            String key = name + ":" + subjectType;
            PSSubject curSub = subjectMap.get(key);
            if (curSub != null)
               curSub.getAttributes().mergeAttributes(attrs);
            else
            {
               subjectMap.put(key, new PSGlobalSubject(name, subjectType, 
                  attrs));
            }
         }
      }
      
      // now build ordered result set
      for (PSSubject subject : subjectMap.values())
      {
         results.add(subject);
      }

      return results;
   }
   
   /**
    * Retrieves a set of subjects for the supplied filters with the requested
    * attributes. Attributes which are not found will be available with a 
    * <code>null</code> value.
    * 
    * @param filters a map with all filters to be applied, not <code>null</code>
    *    may be empty.
    * @param attributeNames a list of attributes to be returned with each
    *    subject. Attributes that are not available are returned as 
    *    <code>null</code> value. Supply <code>null</code> or empty to get
    *    all attributes.
    * @return a set of <code>PSSubject</code> objects with the requested 
    *    attributes.
    */
   protected static Set getSubjects(HashMap filters, Collection attributeNames)
   {
      if (filters == null)
         throw new IllegalArgumentException("filters cannot be null");
         
      // use treeset to prevent duplicates and enforce ordering
      TreeSet sortedSet = new TreeSet(
         PSSubject.getSubjectIdentifierComparator());
   
      Document doc = doc = getCatalogDocument(
         "sys_catalogOuterJoinSubjectAttributes", filters);
      sortedSet.addAll(processSubjectAttributes(doc, true));
      
      if (attributeNames != null && attributeNames.size() > 0)
      {
         Iterator subjects = sortedSet.iterator();
         while (subjects.hasNext())
         {
            PSSubject subject = (PSSubject) subjects.next();
            PSAttributeList attributes = subject.getAttributes();
            
            // remove not requested attributes
            Iterator attrs = attributes.iterator();
            while (attrs.hasNext())
            {
               PSAttribute attribute = (PSAttribute) attrs.next();
               if (!attributeNames.contains(attribute.getName()))
                  attributes.remove(attribute);
            }
            
            // add missing attributes
            Iterator names = attributeNames.iterator();
            while (names.hasNext())
            {
               String name = (String) names.next();
               PSAttribute attribute = attributes.getAttribute(name);
               if (attribute == null)
                  attributes.setAttribute(name, null);
            }
         }
      }
      
      return sortedSet;
   }

   /**
    * The flags used to find the first element in an XML document.
    */
   protected static final int ms_firstFlags = 
      PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN | 
      PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;
      
   /**
    * The flags used to find the next element in an XML document.
    */
   protected static final int ms_nextFlags = 
      PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS | 
      PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;

   /**
    * The name of the application that contains the resources used for
    * cataloging.
    */
   protected static final String CATALOGER_APPNAME = "sys_roleCataloger";
   
   /**
    * Name of the parameter passed to the resources in the Rhythmyx app
    * CATALOGER_APPNAME for filtering on role name.
    * The syntax for this filter are the same as a SQL LIKE clause.
    */
   protected static final String FILTER_ROLE_NAME = "sys_roleNameFilter";
   
   /**
    * Name of the parameter passed to the resources in the Rhythmyx app
    * CATALOGER_APPNAME for filtering on attribute.
    * The syntax for this filter are the same as a SQL LIKE clause.
    */
   protected static final String FILTER_ATTRIBUTE_NAME = 
      "sys_attributeNameFilter";

   /**
    * Name of the parameter passed to the resources in the Rhythmyx app
    * CATALOGER_APPNAME for filtering on subject name.
    * The syntax for this filter are the same as a SQL LIKE clause.
    */
   protected static final String FILTER_SUBJECT_NAME = "sys_subjectNameFilter";

   /**
    * Name of the parameter passed to the resources in the Rhythmyx app
    * <code>CATALOGER_APPNAME</code> ("sys_roleCataloger") for filtering on
    * community id.
    */
   protected static final String FILTER_COMMUNITY_ID = "sys_communityIdFilter";

   /**
    * Constant for the "sys_getRoleCommunitySubjects" resource in
    * "sys_roleCataloger" application. This returns the subjects belonging to
    * specified role and community.
    */
   protected static final String CATALOG_ROLE_COMMUNITY_SUBJECTS_RESOURCE_URL =
      "sys_getRoleCommunitySubjects";
}
