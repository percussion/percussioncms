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
package com.percussion.search.objectstore;

import com.percussion.design.objectstore.IPSObjectStoreErrors;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.util.PSXMLDomUtil;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Class to represent the <code>SearchParams</code> element of the document
 * created from a {@link PSWSSearchRequest} object.  See 
 * {@link #toXml(Document)} for more information.
 */
public class PSWSSearchParams
{
   /**
    * Construct an empty search params object.  All members are optional, and
    * may be modified using the appropriate setter method.
    */
   public PSWSSearchParams()
   {
      
   }
   
   /**
    * Construct this object from its xml representation.
    * 
    * @param src The source XML element.  See {@link #toXml(Document)} for 
    * details on the expected format.  May not be <code>null</code>.
    * 
    * @throws PSUnknownNodeTypeException If <code>src</code> does not match the
    * expected format.
    */
   public PSWSSearchParams(Element src) throws PSUnknownNodeTypeException
   {
      if (src == null)
         throw new IllegalArgumentException("src may not be null");
      
      fromXml(src);
   }
   
   /**
    * Get the system title search field if one has been set.
    * 
    * @return The field, may be <code>null</code>, never empty.
    */
   public PSWSSearchField getTitle()
   {
      return m_title;
   }
   
   /**
    * Set the system title to search for.
    * 
    * @param title The title, <code>null</code> or empty to clear it.
    * @param op The operator, one of the 
    * <code>PSWSSearchField.OP_ATTR_XXX</code> values. Ignored if 
    * <code>title</code> is <code>null</code>.
    * @param connector The connector to use with other search field criteria,
    * one of the <code>PSWSSearchField.OP_CONN_XXX</code> values. Ignored if 
    * <code>title</code> is <code>null</code>.    * 
    */
   public void setTitle(String title, int op, int connector)
   {
      PSWSSearchField titleField = null;
      if (title != null && title.trim().length() > 0)      
      {
         titleField = new PSWSSearchField("sys_title", op, title, connector);         
      }
      m_title = titleField;
   }
   
   /**
    * Get the content type id to search for. See {@link #setContentTypeId(long)}
    * for more info.
    * 
    * @return The content type id.  A value of <code>-1</code> indicates not to
    * search on this property.
    */
   public long getContentTypeId()
   {
      return m_contentTypeId;
   }
   
   /**
    * Set the content type id to search for.
    * 
    * @param contentTypeId The content type id, must be greater than or equal
    * to <code>0</code>, or <code>-1</code> to clear it.
    */
   public void setContentTypeId(long contentTypeId)
   {
      if (contentTypeId < 0 && contentTypeId != -1)
         throw new IllegalArgumentException("invalid contentTypeId");
      
      m_contentTypeId = contentTypeId;
   }
   
   /**
    * Get the full text query string.
    * 
    * @return The query string, may be <code>null</code>, never empty.
    */
   public String getFTSQuery()
   {
      return m_FTSQuery;
   }
   
   /**
    * Set the full text query string.
    * 
    * @param query The query string, may be <code>null</code> or empty to clear
    * it.
    */
   public void setFTSQuery(String query)
   {
      if (query != null && query.trim().length() == 0)
         query = null;
      m_FTSQuery = query;
   }
   
   /**
    * Get the search criteria for all fields to search for.
    * 
    * @return A read-only list of {@link PSWSSearchField} objects, never 
    * <code>null</code>, may be empty.
    */
   public List<PSWSSearchField> getSearchFields()
   {
      return Collections.unmodifiableList(m_searchFields);
   }
   
   /**
    * Convenience method to get only internal or external search fields.  See
    * {@link PSWSSearchField#isExternal()} for more information.
    * 
    * @param external <code>true</code> to get external fields, 
    * <code>false</code> to get internal fields.
    * 
    * @return The list of <code>PSWSSearchField</code> objects, never 
    * <code>null</code>, may be emtpy.  The caller takes ownership of the list.
    */
   public List<PSWSSearchField> getSearchFieldsByType(boolean external)
   {
      List<PSWSSearchField> fieldList = new ArrayList<PSWSSearchField>();
      Iterator fields = m_searchFields.iterator();
      while (fields.hasNext())
      {
         PSWSSearchField field = (PSWSSearchField) fields.next();
         if (external ^ !field.isExternal())
            fieldList.add(field);            
      }
      
      return fieldList;
   }
   
   /**
    * Get the content types to search for as a comma separated string of 
    * content type ids.
    * 
    * @return the content type ids to search for or <code>null</code> if the
    *    search is not limited by content types.
    */
   public String getContentTypes()
   {
      Iterator<PSWSSearchField> fields = m_searchFields.iterator();
      while (fields.hasNext())
      {
         PSWSSearchField field = fields.next();
         if (field.getName().equals(IPSHtmlParameters.SYS_CONTENTTYPEID))
            return field.getValue();
      }
      
      return null;
   }
   
   /**
    * Set the search criteria for all fields to search for.
    * 
    * @param searchFields A list of {@link PSWSSearchField} objects, never 
    * <code>null</code>, may be empty.  A shallow copy of the list is stored in
    * this object.
    */
   public void setSearchFields(List searchFields)
   {
      if (searchFields == null)
         throw new IllegalArgumentException("searchFields may not be null");
      
      m_searchFields.clear();
      Iterator fields = searchFields.iterator();
      while (fields.hasNext())
      {
         Object obj = fields.next();
         if (!(obj instanceof PSWSSearchField))
            throw new IllegalArgumentException("searchFields must contian " +
               "only PSWSSearchField objects");
         
         m_searchFields.add((PSWSSearchField) obj);         
      }
   }
   
   /**
    * Get the field names to include in the search results.
    *  
    * @return A read-only collection of the field names as <code>String</code> 
    * objects, never <code>null</code>, may be empty.
    */
   public Collection<String> getResultFields()
   {
      return Collections.unmodifiableCollection(m_resultFields);
   }
   
   /**
    * Set the field names to include in the search results.
    * 
    * @param resultFields A collection of field names as <code>String</code> 
    * objects, never <code>null</code>, may be empty.  A copy of the collection
    * is stored in this object.
    */
   public void setResultFields(Collection resultFields)
   {
      if (resultFields == null)
         throw new IllegalArgumentException("resultFields may not be null");

      m_resultFields.clear();
      Iterator fields = resultFields.iterator();
      while (fields.hasNext())
      {
         Object obj = fields.next();
         if (!(obj instanceof String))
            throw new IllegalArgumentException("searchFields must contian " +
               "only String objects");
         
         m_resultFields.add((String) obj);         
      }
   }
   
   /**
    * Get the list of properties to pass thru to the search engine.  See
    * {@link #setProperties(Map)} for more info.
    * 
    * @return A read-only map of the properties.
    */
   public Map<String, String> getProperties()
   {
      return Collections.unmodifiableMap(m_props);
   }
   
   /**
    * Set the properties to pass thru to the search engine.
    * 
    * @param props The properties, where the key is the property name and the 
    * value is the property value, both as <code>String</code> objects.  May not
    * be <code>null</code>.  A shallow copy of the map is stored in this object. 
    */
   public void setProperties(Map<String, String> props)
   {
      if (props == null)
         throw new IllegalArgumentException("props may not be null");

      m_props.clear();
      Iterator entries = props.entrySet().iterator();
      while (entries.hasNext())
      {
         Map.Entry entry = (Entry)entries.next();
         if (!(entry.getKey() instanceof String && 
            entry.getValue() instanceof String))
         {
            throw new IllegalArgumentException("props must have" +
               "only String objects for keys and values");
         }
         
         m_props.put((String) entry.getKey(), (String) entry.getValue());
      }
   }
   
   /**
    * Set the start index of results to return, inclusive.  Initially 
    * <code>1</code> if never set.
    * 
    * @param start The start index, must be greater than <code>0</code> and less
    * than or equal to the end index if set (see {@link #setEndIndex(int)}).
    */
   public void setStartIndex(int start)
   {
      if (start <=0 || (m_endIndex != -1 && start > m_endIndex))
         throw new IllegalArgumentException("invalid start index");

      m_startIndex = start;
   }

   /**
    * Get the start index.  See {@link #setStartIndex(int)} for more info.
    * 
    * @return The start index.
    */
   public int getStartIndex()
   {
      return m_startIndex;
   }

   /**
    * Set the end index of results to return, inclusive.  Initially 
    * <code>-1</code> if never set, which indicates all results should be 
    * returned.
    * 
    * @param end The end index, <code>-1</code> to include all results, or else
    * must be greater than <code>0</code> and greater than or equal to the start 
    * index (see {@link #setStartIndex(int)}).
    */
   public void setEndIndex(int end)
   {
      if (end != -1 && (end <=0 || end < m_startIndex))
         throw new IllegalArgumentException("invalid end index");

      m_endIndex = end;
   }

   /**
    * Get the end index.  See {@link #setEndIndex(int)} for more info.
    * 
    * @return The end index.
    */
   public int getEndIndex()
   {
      return m_endIndex;
   }
      
   /**
    * See {@link #setFolderPathFilter(String, boolean)} for details.
    * 
    * @return Returns the value set by 
    *    {@link #setFolderPathFilter(String, boolean)}. If no path is set, 
    *    <code>null</code> is returned. Never empty.
    */
   public String getFolderPathFilter() 
   {
      return m_folderPathFilter;
   }
   
   /**
    * This property allows the caller to limit the scope of their search to a
    * folder tree. The following logical operations are performed if a path has
    * been set:
    * <ol>
    * <li>Perform the search using the standard criteria.</li>
    * <li>Get all children of the folder specified by this path. Whether to
    * recurse into sub-folders is determined by the
    * {@link #isIncludeSubFolders()} method.</li>
    * <li>The result is the intersection.</li>
    * </ol>
    * 
    * @param folderPathFilter <code>null</code> or empty to clear, otherwise a
    * path to an existing folder in the Rx server or its folder id. The path is
    * stored case-sensitive, but used case-insensitive for comparison purposes.
    * No validation is performed now, it will be validated when the search
    * request is made. Defaults to <code>null</code>. 
    * 
    * @param includeSubFolders If <code>true</code>, objects in the specified
    * folder and any of its sub-folders are allowed in the search results.
    * Otherwise, only objects in the top level folder are allowed. Defaults to
    * <code>true</code>.
    */
   public void setFolderPathFilter(String folderPathFilter, 
         boolean includeSubFolders) 
   {
      if (null != folderPathFilter && folderPathFilter.trim().length() == 0)
         folderPathFilter = null;
      m_folderPathFilter = folderPathFilter;
      m_includeSubFolders = includeSubFolders;
   }
   
   /**
    * See {@link #setFolderPathFilter(String, boolean)} for details.
    * 
    * @return Returns the value set with 
    *    {@link #setFolderPathFilter(String, boolean)}.
    */
   public boolean isIncludeSubFolders() {
      return m_includeSubFolders;
   }
   
   /**
    * See {@link #setSearchForFolders(boolean)} for details.
    * 
    * @return Returns the value set with {@link #setSearchForFolders(boolean)}.
    */
   public boolean isSearchForFolders() 
   {
      return m_searchForFolders;
   }
   
   /**
    * This property controls whether folders will be included in the search
    * results. This property works in conjunction w/ the 
    * {@link #setFolderPathFilter(String, boolean) folder path} property. In 
    * other words, if that property is set, only folders that are in the 
    * specified folder tree will be returned. 
    * 
    * @param searchForFolders If <code>true</code>, then folders that match
    * the search criteria will be included in the results, otherwise, no 
    * folder will be allowed in the result set.
    */
   public void setSearchForFolders(boolean searchForFolders) {
      m_searchForFolders = searchForFolders;
   }
   /**
    * Serializes this object to its XML representation.  See the 
    * sys_SearchParameters.xsd schema for details and the required format of the
    * <code>SearchParams</code> element.
    * 
    * @param doc The document to use, may not be <code>null</code>.
    *  
    * @return The root element of the search request, never <code>null</code>.
    */
   public Element toXml(Document doc)
   {
      if (doc == null)
         throw new IllegalArgumentException("doc may not be null");
      
      Element root = doc.createElement(XML_NODE_NAME);
      if (m_searchForFolders)
      {
         root.setAttribute(ATTR_SEARCH_FOR_FOLDERS, 
               getAttributeBool(m_searchForFolders));
      }
      
      // title
      if (m_title != null)
      {
         Element titleEl = PSXmlDocumentBuilder.addElement(doc, root, EL_TITLE, 
            m_title.getValue());
         titleEl.setAttribute(ATTR_OPERATOR, 
            PSWSSearchField.OP_ATTR_VALUES[m_title.getOperatorEnum().getOrdinal()]);
         titleEl.setAttribute(ATTR_CONNECTOR, 
            PSWSSearchField.CONN_ATTR_VALUES[m_title.getConnectorEnum().getOrdinal()]);
      }
      
      // content type
      if (m_contentTypeId != -1)
         PSXmlDocumentBuilder.addElement(doc, root, EL_CONTENT_TYPE, 
            String.valueOf(m_contentTypeId));
      
      // ftsquery
      if (m_FTSQuery != null)
         PSXmlDocumentBuilder.addElement(doc, root, EL_FULLTEXTQUERY, 
            m_FTSQuery);
      
      if (m_folderPathFilter != null)
      {
         Element folderFilter = PSXmlDocumentBuilder.addElement(doc, root, 
               EL_FOLDER_FILTER, m_folderPathFilter);
         folderFilter.setAttribute(ATTR_INCLUDE_SUBFOLDERS, 
               getAttributeBool(m_includeSubFolders));
      }
      
      // search fields
      if (!m_searchFields.isEmpty())
      {
         Element elParam = PSXmlDocumentBuilder.addEmptyElement(doc, root, 
            EL_PARAMETER);
         Iterator searchFields = m_searchFields.iterator();
         while (searchFields.hasNext())
         {
            PSWSSearchField searchField = (PSWSSearchField)searchFields.next();
            elParam.appendChild(searchField.toXml(doc));            
         }
      }
      
      // result fields and start-end index
      Element elSearchResults = null;
      if (m_startIndex > 1 || m_endIndex != -1)
      {
         elSearchResults = PSXmlDocumentBuilder.addEmptyElement(doc, 
            root, EL_SEARCH_RESULTS);
         if (m_startIndex > 1)
         {
            elSearchResults.setAttribute(ATTR_START_INDEX, 
               String.valueOf(m_startIndex));
         }
         if (m_endIndex != -1)
         {
            elSearchResults.setAttribute(ATTR_END_INDEX, 
               String.valueOf(m_endIndex));
         }
      }
      
      if (!m_resultFields.isEmpty())
      {
         if (elSearchResults == null)
            elSearchResults = PSXmlDocumentBuilder.addEmptyElement(doc, 
               root, EL_SEARCH_RESULTS);
         Iterator resultFields = m_resultFields.iterator();
         while (resultFields.hasNext())
         {
            Element elResultField = PSXmlDocumentBuilder.addEmptyElement(doc, 
               elSearchResults, EL_RESULT_FIELD);
            elResultField.setAttribute(ATTR_NAME, (String)resultFields.next());
         }
      }
      
      // props
      if (!m_props.isEmpty())
      {
         Element elProps = PSXmlDocumentBuilder.addEmptyElement(doc, root, 
            EL_PROPERTIES);
         Iterator props = m_props.entrySet().iterator();
         while (props.hasNext())
         {
            Entry entry = (Entry)props.next();
            Element elProp = PSXmlDocumentBuilder.addElement(doc, elProps, 
               EL_PROPERTY, (String)entry.getValue());
            elProp.setAttribute(ATTR_NAME, (String)entry.getKey());
         }
      }
      
      return root;
   }
   
   /**
    * Provides a string representation of the provided boolean value that is
    * suitable for use as an xml boolean attribute's value. 
    * 
    * @param boolValue The boolean for which you want a string representation.
    * 
    * @return Either 'true' or 'false'.
    */
   private String getAttributeBool(boolean boolValue) 
   {
      return boolValue ? "true" : "false";
   }

   /**
    * Restores a search request from its XML representation.  
    * 
    * @param src The root element of the request, assumed not <code>null</code>.
    * See {@link #toXml(Document)} for details on the expected format.
    */
   @SuppressWarnings("static-access")
   private void fromXml(Element src) throws PSUnknownNodeTypeException
   {
      // validate root
      PSXMLDomUtil.checkNode(src, XML_NODE_NAME);
      
      m_searchForFolders = PSXMLDomUtil.checkAttributeBool(src, 
            ATTR_SEARCH_FOR_FOLDERS, false, getAttributeBool(true));
    
      // be sure we handle doc's using namespaces  
      PSXmlTreeWalker walker = new PSXmlTreeWalker(src);
      Element child = walker.getNextElement(walker.GET_NEXT_ALLOW_CHILDREN);
      while (child != null)
      {
         String unqualifiedName = PSXMLDomUtil.getUnqualifiedNodeName(child); 
         if (unqualifiedName.equals(EL_TITLE))
         {      
            // load title
            String title = PSXMLDomUtil.getElementData(child);
            int op = PSXMLDomUtil.checkAttributeEnumerated(child, 
               ATTR_OPERATOR, PSWSSearchField.OP_ATTR_VALUES, false);
            int conn = PSXMLDomUtil.checkAttributeEnumerated(child, 
               ATTR_CONNECTOR, PSWSSearchField.CONN_ATTR_VALUES, false);
            setTitle(title, op, conn); 
         }
         else if (unqualifiedName.equals(EL_CONTENT_TYPE))
         {
            // load content type            
            String ct = PSXMLDomUtil.getElementData(child);
            if (ct.trim().length() > 0)
            {
               try
               {
                  m_contentTypeId = Integer.parseInt(ct);
               }
               catch (NumberFormatException e)
               {
                  Object[] args = {XML_NODE_NAME, EL_CONTENT_TYPE, ct};
                  throw new PSUnknownNodeTypeException(
                     IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
               }
            }
         }
         else if (unqualifiedName.equals(EL_FULLTEXTQUERY))
         {
            // load full text query            
            String ftsQuery = PSXMLDomUtil.getElementData(child);    
            m_FTSQuery = ftsQuery.trim().length() > 0 ? ftsQuery : null;
         }
         else if (unqualifiedName.equals(EL_FOLDER_FILTER))
         {
            String folderPath = PSXMLDomUtil.getElementData(child);
            if (folderPath.trim().length() > 0)
            {
               m_folderPathFilter = folderPath;
               m_includeSubFolders = PSXMLDomUtil.checkAttributeBool(child, 
                     ATTR_INCLUDE_SUBFOLDERS, true, getAttributeBool(true));
            }
         }
         else if (unqualifiedName.equals(EL_PARAMETER))
         {
            // load params
            PSXmlTreeWalker tree = new PSXmlTreeWalker(child);
            Element elSearchField = tree.getNextElement(
               PSWSSearchField.XML_NODE_NAME, tree.GET_NEXT_ALLOW_CHILDREN);
            while (elSearchField != null)
            {
               PSWSSearchField field = new PSWSSearchField(elSearchField);
               m_searchFields.add(field);
               elSearchField = tree.getNextElement(
                  PSWSSearchField.XML_NODE_NAME, tree.GET_NEXT_ALLOW_SIBLINGS);
            }
         }         
         else if (unqualifiedName.equals(EL_SEARCH_RESULTS))
         {
            // check for start and end indexes
            String sTemp;
            sTemp = child.getAttribute(ATTR_START_INDEX);
            try
            {
               m_startIndex = Integer.parseInt(sTemp);
            }
            catch (NumberFormatException e)
            {
               // not set, ignore
            }

            sTemp = child.getAttribute(ATTR_END_INDEX);
            try
            {
               m_endIndex = Integer.parseInt(sTemp);
            }
            catch (NumberFormatException e)
            {
               // not set, ignore
            }
            
            // load result fields
            PSXmlTreeWalker tree = new PSXmlTreeWalker(child);
            Element elResultField = tree.getNextElement( EL_RESULT_FIELD,
               tree.GET_NEXT_ALLOW_CHILDREN);
            while (elResultField != null)
            {
               m_resultFields.add(PSXMLDomUtil.checkAttribute(elResultField, 
                  ATTR_NAME, true));
               elResultField = tree.getNextElement( EL_RESULT_FIELD,
                  tree.GET_NEXT_ALLOW_SIBLINGS);
            }
         }
         else if (unqualifiedName.equals(EL_PROPERTIES))
         {
            // load props
            PSXmlTreeWalker tree = new PSXmlTreeWalker(child);
            Element elProp = tree.getNextElement(EL_PROPERTY, 
               tree.GET_NEXT_ALLOW_CHILDREN);
            while (elProp != null)
            {
               String name = PSXMLDomUtil.checkAttribute(elProp, ATTR_NAME, 
                  true);
               String value = PSXMLDomUtil.getElementData(elProp);
               m_props.put(name, value);
               elProp = tree.getNextElement(EL_PROPERTY, 
                  tree.GET_NEXT_ALLOW_SIBLINGS);
            }
         }
         child = walker.getNextElement(walker.GET_NEXT_ALLOW_SIBLINGS);
      }
   }

   /**
    * Overrides {@link Object#equals(Object)} to compare all member data.
    * 
    * @param obj The object to compare, may be <code>null</code>.
    * 
    * @return <code>true</code> if <code>obj</code> is an instance of 
    * {@link PSWSSearchParams} with the same member data.
    */
   @Override
   public boolean equals(Object obj)
   {
      boolean isEqual = true;
      
      if (!(obj instanceof PSWSSearchParams))
         isEqual = false;
      else if (obj != this)
      {
         PSWSSearchParams other = (PSWSSearchParams)obj;
         
         if (!compare(m_title, other.m_title))
            isEqual = false;
         else if (m_contentTypeId != other.m_contentTypeId)
            isEqual = false;
         else if (!compare(m_FTSQuery, other.m_FTSQuery))
            isEqual = false;
         else if (!compare(m_searchFields, other.m_searchFields))
            isEqual = false;
         else if (!compare(m_resultFields, other.m_resultFields))
            isEqual = false;
         else if (!compare(m_props, other.m_props))
            isEqual = false;
         else if (m_startIndex != other.m_startIndex)
            isEqual = false;
         else if (m_endIndex != other.m_endIndex)
            isEqual = false;
         else if (m_includeSubFolders != other.m_includeSubFolders)
            isEqual = false;
         else if (m_searchForFolders != other.m_searchForFolders)
            isEqual = false;
         else if (!compare(m_folderPathFilter, other.m_folderPathFilter))
         {
            if (!(null != m_folderPathFilter && null != other.m_folderPathFilter
                  && m_folderPathFilter.equalsIgnoreCase(
                     other.m_folderPathFilter)))
            {
               isEqual = false;
            }
         }
      }
      
      return isEqual;
   }

   /**
    * Overridden to properly fufill contract of {@link Object#hashCode()}.
    */
   @Override
   public int hashCode()
   {
      return hashCode(m_title) + m_searchFields.hashCode() + 
         m_resultFields.hashCode() + m_props.hashCode() + (m_contentTypeId + 
         m_startIndex + m_endIndex + m_FTSQuery + m_includeSubFolders
         + m_searchForFolders).hashCode() 
         + (null == m_folderPathFilter ? 0 
            : m_folderPathFilter.toUpperCase().hashCode());
   }

   /**
    * Compare two objects for equality.
    * @param a First object, may be <code>null</code>
    * @param b Second object, may be <code>null</code>
    * @return <code>true</code>if the objects are equal, or both null.
    */
   private boolean compare(Object a, Object b)
   {
      if (a == null && b == null)
      {
         return true;
      }

      if (a != null && a.equals(b))
      {
         return true;
      }

      return false;
   }

   /**
    * Convenience method to get hashcode of possibly <code>null</code> object.
    * 
    * @param o the object, may be <code>null</code>.
    * 
    * @return Result of calling the object's <code>hashCode()</code> method, or 
    * <code>0/code> if the supplied object is <code>null</code>.
    */
   private int hashCode(Object o)
   {
      return o == null ? 0 : o.hashCode();
   }
   
   /**
    * Constant for the root element name used to serialize this object to and
    * from its XML representation.
    */
   public static final String XML_NODE_NAME = "SearchParams";
   
   /**
    * The system title search criteria, may be <code>null</code>, never empty,
    * modified by {@link #setTitle(String, int, int)}
    */
   private PSWSSearchField m_title = null;
   
   /**
    * The content type id to search for.  See {@link #setContentTypeId(int)} for
    * more info.
    */
   private long m_contentTypeId = -1;
   
   /**
    * The full text query string to use, may be <code>null</code>, never empty.
    * Modified by {@link #setFTSQuery(String)}.
    */
   private String m_FTSQuery = null;

   /**
    * List of <code>PSWSSearchField</code> objects.  Never <code>null</code>, 
    * may be empty.  See {@link #setSearchFields(List)} for more info.
    */   
   private List<PSWSSearchField> m_searchFields = 
      new ArrayList<PSWSSearchField>();
   
   /**
    * List of result field names as <code>String</code> objects.  Never 
    * <code>null</code>, may be empty.  See {@link #setResultFields(Collection)}
    * for more info.
    */
   private Collection<String> m_resultFields = new HashSet<String>();
   
   /**
    * Map of properties to pass thru to the search engine.  See 
    * {@link #setProperties(Map)} for more info.
    */
   private Map<String, String> m_props = new HashMap<String, String>();
   
   /**
    * See {@link #setStartIndex(int)} for more info.
    */
   private int m_startIndex = 1;
   
   /**
    * See {@link #setEndIndex(int)} for more info.
    */
   private int m_endIndex = -1;
   
   /**
    * See {@link #setFolderPathFilter(String, boolean)} for details. 
    * Either <code>null </code> or non-empty. 
    */
   private String m_folderPathFilter = null;
   
   /**
    * See {@link #setFolderPathFilter(String, boolean)} for details.
    */
   private boolean m_includeSubFolders = true;
   
   /**
    * See {@link #setSearchForFolders(boolean)} for details.
    */
   private boolean m_searchForFolders = false;
   
   // private xml constants
   private static final String EL_TITLE = "Title";
   private static final String EL_CONTENT_TYPE = "ContentType";
   private static final String EL_PARAMETER = "Parameter";   
   private static final String EL_SEARCH_RESULTS = "SearchResults";
   private static final String EL_RESULT_FIELD = "ResultField";   
   private static final String EL_FULLTEXTQUERY = "FullTextQuery";
   private static final String EL_PROPERTIES = "Properties";
   private static final String EL_PROPERTY = "Property";
   private static final String ATTR_NAME = "name";
   private static final String ATTR_START_INDEX = "startIndex";
   private static final String ATTR_END_INDEX = "endIndex";
   private static final String ATTR_OPERATOR = "operator";
   private static final String ATTR_CONNECTOR = "connector";
   private static final String EL_FOLDER_FILTER = "FolderFilter";
   private static final String ATTR_INCLUDE_SUBFOLDERS = "includeSubFolders";
   private static final String ATTR_SEARCH_FOR_FOLDERS = "searchForFolders";
}
