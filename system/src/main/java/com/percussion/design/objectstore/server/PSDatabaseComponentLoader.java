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
package com.percussion.design.objectstore.server;

import com.percussion.data.IPSInternalRequestHandler;
import com.percussion.data.PSInternalRequestCallException;
import com.percussion.design.objectstore.IPSDatabaseComponent;
import com.percussion.design.objectstore.IPSObjectStoreErrors;
import com.percussion.design.objectstore.PSDatabaseComponent;
import com.percussion.design.objectstore.PSDatabaseComponentCollection;
import com.percussion.design.objectstore.PSDatabaseComponentException;
import com.percussion.design.objectstore.PSRelation;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.error.PSException;
import com.percussion.security.PSAuthenticationFailedException;
import com.percussion.security.PSAuthorizationException;
import com.percussion.server.PSInternalRequest;
import com.percussion.server.PSRequest;
import com.percussion.server.PSServer;
import com.percussion.xml.PSXmlTreeWalker;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This class is used by database components to load themselves from the
 * database.  It is responsible for making broad queries to the system and 
 * caching their results.  This class should only exist for the duration of 
 * the initialization of a set of database components from the back end.
 */
public class PSDatabaseComponentLoader
{
   /**
    * Constructor.
    *
    * @param req  The request to use to access the system component application.
    * Can't be <code>null</code>
    *
    * @throws IllegalArgumentException if req is <code>null</code>
    */
   public PSDatabaseComponentLoader(PSRequest req)
   {
      if (req == null)
         throw new IllegalArgumentException("Request must be specified.");

      m_request = req;
   }

   /**
    * Convenience method.  Calls 
    * {@link #actualizeCollectionComponent(PSDatabaseComponentCollection, 
    * PSRelation)}
    * with a null relation context.
    */
   public void actualizeCollectionComponent(PSDatabaseComponentCollection c)
      throws PSDatabaseComponentException, PSUnknownNodeTypeException
   {
      actualizeCollectionComponent(c, null);
   }

   /**
    * For the component collection specified lookup the associated xml element
    * and use it to instantiate the collection.
    *
    * @param component The component to instantiate.
    * May not be <code>null</code>.
    *
    * @param relationContext  The relation context, may be <code>null</code>.
    *
    * @throws PSUnknownNodeTypeException If the component specified could
    * not recognize the supplied element.
    *
    * @throws IllegalArgumentException if c is <code>null</code>
    */
   public void actualizeCollectionComponent(
      PSDatabaseComponentCollection component, 
      PSRelation relationContext)
      throws PSUnknownNodeTypeException, PSDatabaseComponentException
   {
      if (component == null)
         throw new IllegalArgumentException("Component must be specified");
         
      // Get The resource for this component
      String resource = component.getDatabaseAppQueryDatasetName();

      Element e = lookupDatabaseXmlElement(resource, null, true);

      if (e != null)
         component.fromDatabaseXml(e, this, relationContext);
      // Otherwise the component should just be left empty...
   }

   /** 
    * Retrieve the related components from the back end based on the relation
    * context.
    *
    * @param relationContext The relation context, containing information 
    * on keys which will form the search criteria.  Can't be <code>null</code>.
    * If this contains no keys with ids, all database components of the 
    * specified type will be returned.
    *
    * @param c The database component class to return.  
    * May not be <code>null</code>, and must implment 
    * {@link IPSDatabaseComponent}.
    *
    * @param componentType The type of component. May not be <code>null<code> or
    * empty.
    *
    * @return An array of database components of the specified class, never
    * <code>null</code>, may be empty if none were found.
    *
    * @throws PSUnknownNodeTypeException if a component fails to initialize
    * itself from an element returned by the system component app
    *
    * @throws PSDatabaseComponentException if a database component fails 
    *    to load for any other reason
    *
    * @throws IllegalArgumentException if we can not continue the great 
    * journey of life
    */
   public IPSDatabaseComponent[] getRelatedDatabaseComponents(
      PSRelation relationContext, Class c, String componentType)
      throws PSDatabaseComponentException, PSUnknownNodeTypeException
   {
      if (relationContext == null)
         throw new IllegalArgumentException(
            "Relation context must be supplied.");
         
      if (componentType == null || componentType.length() < 1)
         throw new IllegalArgumentException(
            "Component type must be specified.");
      
      String resourceName = relationContext.getDatabaseAppQueryDatasetName(
            componentType );
      
      List relationList = (ArrayList) m_relationListMap.get(resourceName);

      if (relationList == null)
      {
         // load the relation list
         Element relationListElement = lookupDatabaseXmlElement(
            resourceName, null, false);

         if (relationListElement == null)
            return new IPSDatabaseComponent[0];
            
         PSXmlTreeWalker walker = new PSXmlTreeWalker(relationListElement);

         relationList = new ArrayList();

         Element relationElement = walker.getNextElement(
            PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);
         while (relationElement != null)
         {
            PSRelation relation = new PSRelation();
            relation.fromDatabaseXml(relationElement, this, relationContext);
            relationList.add(relation);
            relationElement = walker.getNextElement(
               PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS);
         }

         m_relationListMap.put(resourceName, relationList);
      }

      ArrayList componentList = new ArrayList();

      Iterator i = relationList.iterator();
      while (i.hasNext())
      {
         PSRelation relation = (PSRelation) i.next();
         if (relation.isMatch(relationContext))
         {
            Element relatedComponentElement = lookupDatabaseXmlElement(
               PSDatabaseComponent.buildAppQueryDatasetName(componentType),
               relation.getValue(componentType),
               true);
                        
            if (relatedComponentElement == null)
            {
               Object[] args = {
                  relation.getDatabaseAppQueryDatasetName(),
                  componentType,
                  relation.getValue(componentType),
                  PSDatabaseComponent.buildAppQueryDatasetName(componentType) };
                  
               throw new PSDatabaseComponentException(
                  IPSObjectStoreErrors.RELATED_DB_COMPONENT_LOAD_EXCEPTION,
                  args);
            }
            
            IPSDatabaseComponent component = 
               PSDatabaseComponent.newComponentInstance(
                  c,
                  relatedComponentElement,
                  (PSRelation)relationContext.clone(),
                  this);
               
            componentList.add(component);
         }
      }
      
      return (IPSDatabaseComponent[])
         componentList.toArray(new IPSDatabaseComponent[componentList.size()]);
   }
   
   /**
    * Retrieve a specific element from one of our lists.
    *
    * @param resourceName The resource name associated with the component
    * being retrieved.  Assumed not <code>null</code>.
    *
    * @param key  The key (DbComponentId) for the element to retrieve.  If
    * <code>null</code> returns the root element containing all elements.
    *
    * @param cacheMap Cache the map of all elements returned by the map 
    * loader?  <code>true</code> indicates to cache the map, <code>false</code>
    * indicates not to cache it.
    *
    * @return The xml element, from which the associated component can be
    * instantiated.  May be <code>null</code> indicating the specified request
    * returned no results.
    *
    * @throws PSUnknownNodeTypeException if a component fails to initialize
    * itself from an element returned by the system component app
    *
    * @throws PSDatabaseComponentException if the database component fails 
    *    to load for any other reason
    */
   private Element lookupDatabaseXmlElement(String resourceName, String key,
      boolean cacheMap)
      throws PSDatabaseComponentException, PSUnknownNodeTypeException
   {
      try 
      {
         if (cacheMap || key != null)
         {
            Map elementMap = (Map) m_elementsMap.get(resourceName);
            if (elementMap == null)
            {
               elementMap = loadElementMap(resourceName);

               if (cacheMap)
                  m_elementsMap.put(resourceName, elementMap);
            }
            return (Element) elementMap.get(key);
         } else
         {  // No cache and no key, just return the root of the doc.
            return getComponentDoc(resourceName).getDocumentElement();
         }
      } catch (PSException e)
      {
         if (e instanceof PSUnknownNodeTypeException)
            throw (PSUnknownNodeTypeException) e;
            
         Object[] args = {resourceName, e.toString()};

         throw new PSDatabaseComponentException(
            IPSObjectStoreErrors.DB_COMPONENT_LOAD_EXCEPTION, args);
      }
   }

   /**
    * Load an element map from the back end, caching all its
    * elements using the internal sys components application.
    * The root element of the document returned by the back end
    * will be stored with the key <code>null</code>.
    *
    * @param resourceName The name of the resource which will return
    * the desired elements.  Assumed not <code>null</code> or empty.
    *
    * @return A map containing the elements, keyed by database component id.
    * Never <code>null</code>.
    *
    * @throws PSUnknownNodeTypeException if a component fails to initialize
    * itself from an element returned by the system component app
    *
    * @throws PSAuthorizationException if the user making the call does not
    *    have permission to access the associated application
    *
    * @throws PSAuthenticationException if the user making the call fails to
    *    authenticate
    *
    * @throws PSInternalRequestCallException if an error occurs processing the
    *    internal request call
    */
   private Map loadElementMap(String resourceName)
      throws   PSInternalRequestCallException,
               PSAuthorizationException, 
               PSAuthenticationFailedException,
               PSUnknownNodeTypeException
   {
      HashMap elementMap = new HashMap();
      
      Document componentDoc = getComponentDoc(resourceName);      

      Element root = componentDoc.getDocumentElement();

      elementMap.put(null, root);

      PSXmlTreeWalker walker = new PSXmlTreeWalker(root);

      Element e = 
         walker.getNextElement(PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);
         
      while (e != null)
      {
         elementMap.put(PSDatabaseComponent.getDatabaseComponentId(e), e);
         e = walker.getNextElement(PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS);
      }
      
      return elementMap;
   }

   /**
    * Call the system component application to retrieve database components
    * using the specified resource.
    *
    * @param resourceName The name of the resource which will return
    * the desired elements.  Assumed not <code>null</code> or empty.
    *
    * @return The document returned by the call to the sys components app.
    * Never <code>null</code>.
    *
    * @throws PSAuthorizationException if the user making the call does not
    *    have permission to access the associated application
    *
    * @throws PSAuthenticationException if the user making the call fails to
    *    authenticate
    *
    * @throws PSInternalRequestCallException if an error occurs processing the
    *    internal request call
    */
   private Document getComponentDoc(String resourceName)
      throws   PSInternalRequestCallException,
               PSAuthorizationException, 
               PSAuthenticationFailedException
   {
      String request = COMPONENT_HANDLER_APP_NAME + "/" + resourceName;

      IPSInternalRequestHandler irh = 
         PSServer.getInternalRequestHandler(request);

      if (irh == null)
      {
         Object[] args = {resourceName, 
            "The internal request handler could not be loaded."+
              "  It may have failed to start"};
         throw new PSInternalRequestCallException(
            IPSObjectStoreErrors.DB_COMPONENT_LOAD_EXCEPTION,
            args);
      }
      
      PSInternalRequest req = new PSInternalRequest(m_request, irh);
      return req.getResultDoc();
   }

   /**
    * The request used to make internal calls to the system component
    * application.  Initialized at construct time, never <code>null</code>
    * after that.
    */
   private PSRequest m_request = null;
   
   /**
    * The elements map is a map with a key based on a component resource
    * name and a value which is a Map of DbComponentId - xml Element pairs.
    * Never <code>null</code>, may be empty.  Initialized lazily as requests
    * for Db element definitions are made.
    */
   private HashMap m_elementsMap = new HashMap();

   /**
    * The relation list map is a map with a key based on a component resource
    * name and a value which is a list of associated relations.
    * Never <code>null</code>, may be empty.  Initialized lazily as requests
    * for relations are made.
    */
   private HashMap m_relationListMap = new HashMap();
   
   /**
    * The name of the application used to retrieve DB components and relations.
    */
   public static final String COMPONENT_HANDLER_APP_NAME = "sys_components";
}
