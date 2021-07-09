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

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.percussion.design.objectstore.IPSObjectStoreErrors;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.util.PSXMLDomUtil;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;

/**
 * Object representation of the request document submitted to the web services 
 * search handler.  See the {@link #toXml(Document)} method for more info.
 */
public class PSWSSearchRequest
{

   /**
    * Create a standard search request.
    * 
    * @param searchParams Specify the search criteria, may not be 
    * <code>null</code>.
    */
   public PSWSSearchRequest(PSWSSearchParams searchParams)
   {
      if (searchParams == null)
         throw new IllegalArgumentException("searchParams may not be null");

      m_searchParams = searchParams;
      m_internalSearchName = null;
      m_internalSearchParams = null;
   }
   
   /**
    * Create an internal search request.
    * 
    * @param searchName The name of an internal search, may not be 
    * <code>null</code> or empty.  Used as the name of the request page
    * to make a request to the internal search application.
    * @param params Parameters to pass with the request to the internal search 
    * application.  May be <code>null</code>.  Key is the parameter name and 
    * value is the parameter value, both as <code>String</code> objects.  A 
    * shallow copy of the map is stored in this object.
    */
   public PSWSSearchRequest(String searchName, Map params)
   {
      if (searchName == null || searchName.trim().length() == 0)
         throw new IllegalArgumentException(
            "searchName may not be null or empty");
   
      m_internalSearchName = searchName;
      if (params != null)
      {
         m_internalSearchParams = new HashMap(); 
         Iterator entries = params.entrySet().iterator();
         while (entries.hasNext())
         {
            Map.Entry entry = (Map.Entry)entries.next();
            if (!(entry.getKey() instanceof String && 
               entry.getValue() instanceof String))
            {
               throw new IllegalArgumentException("params must have" +
                  "only String objects for keys and values");
            }
            m_internalSearchParams.put(entry.getKey(), entry.getValue());
         }
      }
      else
         m_internalSearchParams = null;

      m_searchParams = null;
   }


   /**
    * Construct a search request from its XML representation.  See 
    * {@link #toXml(Document)} for details.
    * 
    * @param src The root element of the search request, may not be 
    * <code>null</code>.
    * 
    * @throws PSUnknownNodeTypeException if the <code>src</code> element does
    * not conform the required format.
    */
   public PSWSSearchRequest(Element src) throws PSUnknownNodeTypeException
   {
      if (src == null)
         throw new IllegalArgumentException("src may not be null");

      fromXml(src);
   }  
   
   /**
    * Get the search params provided during construction.
    * 
    * @return The params, <code>null</code> if this is object was constructed as
    * an internal search.
    */
   public PSWSSearchParams getSearchParams()
   {
      return m_searchParams;
   }
   
   /**
    * Get the name of the internal search to perform provided during 
    * construction.
    * 
    * @return The name, <code>null</code> if object was not constructed as an
    * internal search, never empty.
    */
   public String getInternalSearchName()
   {
      return m_internalSearchName;
   }
   
   /**
    * Get a read-only copy of the internal search params provided during 
    * construction.  
    * 
    * @return The params, may be <code>null</code> if an internal search was
    * not constructed.  Key is the parameter name and  value is the parameter 
    * value, both as <code>String</code> objects. 
    */
   public Map getInternalSearchParams()
   {
      return Collections.unmodifiableMap(m_internalSearchParams);
   }
   
   /**
    * Determines if a case-insensitive search is to be performed.
    * @param caseInsensitive <code>true</code> to perform a case-insensitive
    * search, <code>false</code> to use the database default.  If the database
    * is case-insensitive by default, then setting this value will have no 
    * effect.
    */
   public void setCaseInsensitiveSearch(boolean caseInsensitive)
   {
      m_caseInsensitiveSearch = caseInsensitive;
   }

   /**
    * See {@link #setCaseInsensitiveSearch(boolean)} for details.
    * 
    * @return <code>true</code> if it is case-insensitive, <code>false</code> if
    * not.
    */
   public boolean isCaseInsensitiveSearch()
   {
      return m_caseInsensitiveSearch;
   }
   
   /**
    * Set whether the external search engine should be used or not.
    * 
    * @param useExternalSearchEngine <code>true</code> to use the external
    *    search engine, <code>false</code> otherwise.
    */
   public void setUseExternalSearchEngine(boolean useExternalSearchEngine)
   {
      m_useExternalSearchEngine = useExternalSearchEngine;
   }
   
   /**
    * Is the external search engine used?
    * 
    * @return <code>true</code> if the external search engine is used, 
    *    <code>false</code> otherwise.
    */
   public boolean useExternalSearchEngine()
   {
      return m_useExternalSearchEngine;
   }

   /**
    * Serializes this object to its XML representation.  See the 
    * sys_SearchParameters.xsd schema for details and the required format.
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
      
      root.setAttribute(ATTR_USE_DATABASE_CASE, m_caseInsensitiveSearch ? 
         XML_FALSE : XML_TRUE);
      
      root.setAttribute(ATTR_USE_EXTERNAL_SEARCH_ENGINE, 
         m_useExternalSearchEngine ? XML_TRUE : XML_FALSE);
      
      // either a named internal search or a standard search
      if (m_internalSearchName != null)
      {
         // named search, set name and params if any
         PSXmlDocumentBuilder.addElement(doc, root, EL_SEARCHNAME, 
            m_internalSearchName);
         
         if (m_internalSearchParams != null)
         {
            Iterator params = m_internalSearchParams.entrySet().iterator();
            while (params.hasNext())
            {
               Map.Entry param = (Map.Entry)params.next();
               Element elParam = PSXmlDocumentBuilder.addElement(doc, root, 
                  EL_REQ_PARAM, (String)param.getValue());
               elParam.setAttribute(ATTR_NAME, (String)param.getKey());
            }
         }
      }
      else
      {
         // standard search
         root.appendChild(m_searchParams.toXml(doc));
      }
      
      return root;
   }
   
   /**
    * Restores a search request from its XML representation.  
    * 
    * @param src The root element of the request, assumed not <code>null</code>.
    */
   private void fromXml(Element src) throws PSUnknownNodeTypeException
   {
      // be sure to handle namespace aware
      PSXMLDomUtil.checkNode(src, XML_NODE_NAME);
      
      // default to case-sensitive if not specified
      m_caseInsensitiveSearch = !PSXMLDomUtil.getBooleanData(
         src.getAttribute(ATTR_USE_DATABASE_CASE));
      
      m_useExternalSearchEngine = false;
      String test = src.getAttribute(ATTR_USE_EXTERNAL_SEARCH_ENGINE);
      if (test != null)
         m_useExternalSearchEngine = PSXMLDomUtil.getBooleanData(test);
      
      // ensure one child
      PSXmlTreeWalker walker = new PSXmlTreeWalker(src);
      Element child = walker.getNextElement(
         PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);
      if (child == null)
      {
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_NULL, 
               PSWSSearchParams.XML_NODE_NAME);
      }
      
      boolean internalSearch = false;
      
      // see if internal search
      if (PSXMLDomUtil.getUnqualifiedNodeName(child).equals(EL_SEARCHNAME))
      {      
         m_internalSearchName = PSXMLDomUtil.getElementData(child);
         if (m_internalSearchName.trim().length() > 0)
         {
            internalSearch = true;
         }
         else
         {
            internalSearch = false;
            // Move to param element - next sibling
            child = walker.getNextElement(
               PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS);
         }
      }
      
      if (internalSearch)
      {
         // get params
         m_internalSearchParams = new HashMap();
         NodeList params = src.getElementsByTagNameNS("*", EL_REQ_PARAM);
         int tot = params.getLength();
         for (int i = 0; i < tot; i++)
         {
            Element param = (Element)params.item(i);
            String name = PSXMLDomUtil.checkAttribute(param, ATTR_NAME, true);
            String value = PSXMLDomUtil.getElementData(param);
            m_internalSearchParams.put(name, value);
         }
         
         m_searchParams = null;
      }
      // check for standard search
      else if (PSXMLDomUtil.getUnqualifiedNodeName(child).equals(
         PSWSSearchParams.XML_NODE_NAME))
      {
         m_searchParams = new PSWSSearchParams(child);
         m_internalSearchName = null;
         m_internalSearchParams = null;
      }
      // bad element
      else
      {
         Object[] args = {PSWSSearchParams.XML_NODE_NAME, 
            PSXMLDomUtil.getUnqualifiedNodeName(child)};
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_WRONG_TYPE, args);
      }            
   }
   
   /**
    * Overriden to properly fufill contract of {@link Object#hashCode()}.
    */
   public int hashCode()
   {
      int hash = m_caseInsensitiveSearch ? 1 : 0;
      hash += m_useExternalSearchEngine ? 1 : 0;
      if (m_internalSearchName != null)
      {
         hash += m_internalSearchName.hashCode();
         if (m_internalSearchParams != null)
            hash += m_internalSearchParams.hashCode();
      }
      else
      {
         hash += m_searchParams.hashCode();
      }
      
      return hash;
   }
   
   /**
    * Overrides {@link Object#equals(Object)} to compare all member data.
    * 
    * @param obj The object to compare, may be <code>null</code>.
    * 
    * @return <code>true</code> if <code>obj</code> is an instance of 
    * {@link PSWSSearchField} with the same member data.
    */
   public boolean equals(Object obj)
   {
      boolean isEqual = true;
      
      if (!(obj instanceof PSWSSearchRequest))
         isEqual = false;
      else if (this != obj)
      {
         PSWSSearchRequest other = (PSWSSearchRequest)obj;
         if (m_internalSearchName == null ^ other.m_internalSearchName == null)
            isEqual = false;
         else if (m_internalSearchName != null && !m_internalSearchName.equals(
            other.m_internalSearchName))
         {
            isEqual = false;
         }
         else if (m_internalSearchParams != null && 
            !m_internalSearchParams.equals(other.m_internalSearchParams))
         {
            isEqual = false;
         }
         else if (m_searchParams != null && !m_searchParams.equals(
            other.m_searchParams))
         {
            isEqual = false;
         }
      }
      
      return isEqual;
   }
   

   /**
    * Name of root element when this object is serialized to and from its XML
    * representation.
    */
   public static final String XML_NODE_NAME = "SearchRequest";

   /**
    * Search criteria for a standard search, set during ctor, <code>null</code>
    * only if {@link #m_internalSearchName} is not, never modified after 
    * construction.
    */
   private PSWSSearchParams m_searchParams;
   
   /**
    * Name of internal named search to perform, set by ctor,  
    * <code>null</code> only if {@link #m_searchParams} is not, never empty, 
    * never modified after construction.
    */
   private String m_internalSearchName;
   
   /**
    * Optional parameters to supply with request to internal named search, 
    * may be <code>null</code>, possibly supplied by ctor only if 
    * {@link #m_internalSearchName} was supplied, never modified after that.
    */
   private Map m_internalSearchParams;
   
   /**
    * Determines if search is to be case-insentive or if dbms defaults will be
    * used. See {@link #setCaseInsensitiveSearch(boolean)} for details.
    */
   private boolean m_caseInsensitiveSearch = false;
   
   /**
    * A flag to specify whether to use the external search engine or not.
    * Defaults to <code>false</code>, changed through 
    * {@link #setUseExternalSearchEngine(boolean)}.
    */
   private boolean m_useExternalSearchEngine = false;
   
   // private xml constants
   private static final String EL_SEARCHNAME = "SearchName";
   private static final String EL_REQ_PARAM = "ReqParameter";
   private static final String ATTR_USE_DATABASE_CASE = "useDatabaseCase";
   private static final String ATTR_USE_EXTERNAL_SEARCH_ENGINE = 
      "useExternalSearchEngine";
   private static final String ATTR_NAME = "name";   
   private static final String XML_TRUE = "true";
   private static final String XML_FALSE = "false";
}
