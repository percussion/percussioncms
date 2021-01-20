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

package com.percussion.cms.objectstore.client;

import com.percussion.cms.IPSCmsErrors;
import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.IPSRelationshipProcessor;
import com.percussion.cms.objectstore.PSComponentSummaries;
import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.cms.objectstore.PSKey;
import com.percussion.cms.objectstore.PSRelationshipFilter;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.design.objectstore.PSRelationshipSet;
import com.percussion.util.IPSRemoteRequester;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This class contains the know how to implement the necessary relationship
 * processing when operating in a different JVM than the server.
 */
public class PSRelationshipProcessor implements IPSRelationshipProcessor
{
   /**
    * Construct an instance from the given parameters. This constructor is
    * expected to be called by the proxy.
    *
    * @param conn The remote request object, knows how to communicate with
    *    the server. It may not be <code>null</code>.
    *
    * @param procConfig The parameters from the config file. This is not
    *    needed.
    */
   public PSRelationshipProcessor(IPSRemoteRequester conn, Map procConfig)
   {
      if (null == conn)
      {
         throw new IllegalArgumentException(
               "Connection information must be supplied.");
      }
      
      // avoid eclipse warning
      if (procConfig == null);

      m_conn = conn;
   }

   /**
    * See {@link IPSRelationshipProcessor#add(String, String, List, PSKey)
    * interface}
    *
    * @throws UnsupportedOperationException Always.
    */
   public void add(String componentType, String relationshipType,
         List children, PSKey targetParent)
      throws PSCmsException
   {
      throw new UnsupportedOperationException(
         "Not supported by this processor.");
   }

   /**
    * See {@link IPSRelationshipProcessor#getChildren(String, String, PSKey)
    * interface}
    */
   public PSComponentSummary[] getChildren(String type,
      String relationshipType, PSKey parent)
      throws PSCmsException
   {
      return getComponentSummaries("getChildren", type, relationshipType,
         parent);
   }

   /**
    * See {@link IPSRelationshipProcessor#getParents(String, String, PSKey)
    * interface}
    */
   public PSComponentSummary[] getParents(String type,
      String relationshipType, PSKey locator)
      throws PSCmsException
   {
      return getComponentSummaries("getParents", type, relationshipType,
         locator);
   }

   /**
    * A convenience method to get children or get parents for a given locator.
    * This is determined by the <code>method</code>.
    *
    * @param method If it is "getChildren", then get a list of children for
    *    the given locator; otherwise get a list of parents. Assume
    *    it is either "getChildren" or "getParents".
    *
    * @param type The component type. It must be <code>COMPONENT_TYPE</code>.
    *
    * @param relationshipType The type of the requested relationship, it may
    *    not be <code>null</code> or empty.
    *
    * @param locator The locator of a parent if the <code>method</code> is
    *    "getChildren", or it is a locator of a child otherwise.
    *
    * @return Array of component summaries, which may be a list of children or
    *    parents. Never <code>null</code>, but may be empty.
    *
    * @throws PSCmsException if an error occurs.
    */
   private PSComponentSummary[] getComponentSummaries(String method,
      String type, String relationshipType, PSKey locator)
      throws PSCmsException
   {
      validateComponentType(type);
      if (relationshipType == null || relationshipType.trim().length() == 0)
         throw new IllegalArgumentException(
            "relationshipType may not be null or empty");


      PSLocator pslocator = (PSLocator) locator;
      PSComponentSummaries summaries = null;

      Map params = new HashMap();
      params.put(PARAM_ID, Integer.toString(pslocator.getId()));
      params.put(PARAM_REVISION, Integer.toString(pslocator.getRevision()));
      params.put(PARAM_METHOD, method);
      params.put(PARAM_RELATIONSHIP_TYPE, relationshipType);

      Document doc = null;

      try
      {
         doc = m_conn.getDocument("sys_ceFieldsCataloger/Relationship", params);

         Element respEl = doc.getDocumentElement();
         summaries = new PSComponentSummaries(respEl);
      }
      catch (Exception e)
      {
         Object[] args = new Object[]{e.getLocalizedMessage()};
         throw new PSCmsException(IPSCmsErrors.UNEXPECTED_ERROR, args);
      }

      return summaries.toArray();
   }

   /**
    * See {@link IPSRelationshipProcessor#getChildren(String, PSKey) interface}
    *
    * @throws UnsupportedOperationException Always.
    */
   public PSComponentSummary[] getChildren(String type, PSKey parent)
      throws PSCmsException
   {
      throw new UnsupportedOperationException(
            "Not supported by this processor.");
   }

   /**
    * See {@link IPSRelationshipProcessor#move(String, PSKey, PSKey [], PSKey)
    * interface}
    *
    * @throws UnsupportedOperationException Always.
    */
   public void move(String relationshipType, PSKey sourceParent, List children,
      PSKey targetParent) throws PSCmsException
   {
      throw new UnsupportedOperationException(
         "Not supported by this processor.");
   }

   /**
    * See {@link IPSRelationshipProcessor#copy(String, PSKey, PSKey [])
    * interface}
    *
    * @throws UnsupportedOperationException Always.
    */
   public void copy(String relationshipType, List children, PSKey parent)
      throws PSCmsException
   {
      throw new UnsupportedOperationException(
         "Not supported by this processor.");
   }

   /**
    * See {@link IPSRelationshipProcessor#delete(String, PSKey, List) interface}
    *
    * @throws UnsupportedOperationException Always.
    */
   public void delete(String type, PSKey parent, List children)
      throws PSCmsException
   {
      throw new UnsupportedOperationException(
         "Not supported by this processor.");
   }

   /**
    * See {@link IPSRelationshipProcessor#getSummaryByPath(String, String, 
    * String) interface}
    *
    * @throws UnsupportedOperationException Always.
    */
   public PSComponentSummary getSummaryByPath(String type, String path, 
     String relationshipTypeName) throws PSCmsException 
   {
      throw new UnsupportedOperationException(
         "Not supported by this processor.");
   }

   /**
    * Validates a specified type. It must by <code>COMPONENT_TYPE</code>.
    *
    * @param type The to be validated component type.
    *
    */
   private void validateComponentType(String type)
   {
      if (type == null || type.trim().length() == 0)
         throw new IllegalArgumentException("type may not be null or empty");

      if (! type.equalsIgnoreCase(COMPONENT_TYPE))
         throw new IllegalArgumentException("type cannot be " + type + ". " +
            "It must be " + COMPONENT_TYPE);
   }

   /**
    * See {@link IPSRelationshipProcessor#add(String, List, PSLocator) 
    * interface}
    *
    * @throws UnsupportedOperationException Always.
    */
   public void add(String relationshipType, List children, PSLocator targetParent) throws PSCmsException
   {
      throw new UnsupportedOperationException(
         "getRelationships() is not implemented in : "
            + this.getClass().getName());
   }

   /**
    * See {@link IPSRelationshipProcessor#getRelationshipOwnerPaths(String, 
    * PSLocator, String) interface}
    *
    * @throws UnsupportedOperationException Always.
    */
   public PSRelationshipSet getRelationships(String relationshipType, PSLocator locator, boolean owner) throws PSCmsException
   {
      throw new UnsupportedOperationException(
         "getRelationships() is not implemented in : "
            + this.getClass().getName());
   }

   /**
    * See {@link IPSRelationshipProcessor#move(String, PSLocator, List, 
    * PSLocator) interface}
    *
    * @throws UnsupportedOperationException Always.
    */
   public void move(String relationshipType, PSLocator sourceParent, List children, PSLocator targetParent) throws PSCmsException
   {
      throw new UnsupportedOperationException(
         "move() is not implemented in : "
            + this.getClass().getName());
   }

   /**
    * See {@link IPSRelationshipProcessor#getRelationships(PSRelationshipFilter) 
    * interface}
    *
    * @throws UnsupportedOperationException Always.
    */
   public PSRelationshipSet getRelationships(PSRelationshipFilter filter) throws PSCmsException
   {
      throw new UnsupportedOperationException(
         "getRelationships() is not implemented in : "
            + this.getClass().getName());
   }

   /**
    * See {@link IPSRelationshipProcessor#getSummaries(PSRelationshipFilter, 
    * boolean) interface}
    *
    * @throws PSCmsException if an error occurs.
    */
   public PSComponentSummaries getSummaries(PSRelationshipFilter filter,
      boolean owner)
   throws PSCmsException
   {
      if(filter == null)
         throw new IllegalArgumentException(
            "Relationship filter cannot be null.");
      
      PSComponentSummaries summaries = null;

      Map params = new HashMap();
      Document inputDoc = PSXmlDocumentBuilder.createXmlDocument();
      String rFilter = PSXmlDocumentBuilder.toString(filter.toXml(inputDoc));
      params.put(PARAM_OWNER, Boolean.toString(owner));
      params.put(PARAM_METHOD, "getSummaries");
      params.put(PARAM_RELATIONSHIP_FILTER, rFilter);

      Document doc = null;

      try
      {             
         doc = m_conn.getDocument(
            "sys_ceFieldsCataloger/Relationship", params);

         Element respEl = doc.getDocumentElement();
         summaries = new PSComponentSummaries(respEl);
      }
      catch (Exception e)
      {
         Object[] args = new Object[]{e.getLocalizedMessage()};
         throw new PSCmsException(IPSCmsErrors.UNEXPECTED_ERROR, args);
      }

      return summaries;
   }

   /**
    * See {@link IPSRelationshipProcessor#save(PSRelationshipSet) interface}
    *
    * @throws UnsupportedOperationException Always.
    */
   public void save(PSRelationshipSet relationships) throws PSCmsException
   {
      throw new UnsupportedOperationException(
         "save() is not implemented in : "
            + this.getClass().getName());
   }

   /**
    * See {@link IPSRelationshipProcessor#delete(PSRelationshipSet) interface}
    *
    * @throws UnsupportedOperationException Always.
    */
   public void delete(PSRelationshipSet relationships)
   {
      throw new UnsupportedOperationException(
         "delete() is not implemented in : "
            + this.getClass().getName());
   }

   /**
    * See {@link IPSRelationshipProcessor#getConfig(String) interface}
    *
    * @throws UnsupportedOperationException Always.
    */
   public PSRelationshipConfig getConfig(String relationshipTypeName)
   {
      throw new UnsupportedOperationException(
         "getConfig() is not implemented in : "
            + this.getClass().getName());
   }

   /**
    * See {@link IPSRelationshipProcessor#getRelationshipOwnerPaths(String, 
    * PSLocator, String) interface}
    *
    * @throws UnsupportedOperationException Always.
    */
   public String[] getRelationshipOwnerPaths(
      String componentType,
      PSLocator locator,
      String relationshipTypeName)
      throws PSCmsException
   {
      throw new UnsupportedOperationException(
         "getRelationshipOwnerPaths() is not implemented in : "
            + this.getClass().getName());
   }

   /**
    * See {@link IPSRelationshipProcessor#isDescendent(String, PSLocator, 
    * PSLocator, String) interface}
    *
    * @throws UnsupportedOperationException Always.
    */
   public boolean isDescendent(
      String componentType,
      PSLocator parent,
      PSLocator child,
      String relationshipTypeName)
      throws PSCmsException
   {
      throw new UnsupportedOperationException(
         "isDescendent() is not implemented in : "
            + this.getClass().getName());
   }
   
   // see interface for details
   public PSKey[] getDescendentsLocators(
            String componentType,
            String relationshipType,
            PSKey parent)
      throws PSCmsException
   {
      throw new UnsupportedOperationException(
            "Not supported by this processor.");
   }
      

   /**
    * Object used to make the requests to the server. Never <code>null</code>
    * after construction.
    */
   private IPSRemoteRequester m_conn = null;

   /**
    * This is the name of <code>CmsComponent</code> that is used in the proxy
    * config file for this processor.
    */
   public static String COMPONENT_TYPE = "Relationship";

  /**
   * A list of names for URL parameters
   */
   public static String PARAM_ID = "id";
   public static String PARAM_REVISION = "revision";
   public static String PARAM_METHOD = "method";
   public static String PARAM_RELATIONSHIP_TYPE = "relationshipType";
   public static String PARAM_OWNER = "owner";
   public static String PARAM_RELATIONSHIP_FILTER = "relationshipFilter";
}
