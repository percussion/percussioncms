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
package com.percussion.cms.objectstore;

import com.percussion.cms.IPSCmsErrors;
import com.percussion.cms.PSCmsException;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


/**
 * This is the base class for a set of processors that use applications to
 * perform their work. This class performs various processing work that is
 * independent of how the data is submitted to the server. It calls various
 * doXXX methods passing in the resource name and the processed data. This
 * class manages the configuration properties and passes on the appropriate
 * ones.
 * <p>The following properties must be specified in the processor config.
 * <table>
 *    <tr>
 *       <th>Property name</th>
 *       <th>Description</th>
 *       <th>Required?</th>
 *       <th>Type</th>
 *    </tr>
 *    <tr>
 *       <td>deleteResource</td>
 *       <td>The partial URL of the app and resource that can delete components
 *          of the associated type, in the format "app/resource.xml". The keys
 *          for all the components to be loaded will be included as multiple
 *          html params. An html param for each key part is created for each
 *          key. For example, if you have 2 simple keys whose part is "ID",
 *          the html param "ID" will appear twice, e.g., ID=3&ID=4.<td>
 *       <td>required</td>
 *       <td>standard</td>
 *    </tr>
 *    <tr>
 *       <td>saveResource</td>
 *       <td>The parital URL of the app and resource that can insert and update
 *          components of the associated type, in the format
 *          "app/resource.xml". The supplied document will have a root
 *          element whose name is that supplied in the 'rootElementName'
 *          property. Each child of this element will be the element
 *          returned from the <code>toDbXml</code> method of the associated
 *          component type. Keys will be included in the input document for
 *          inserts.</td>
 *       <td>required</td>
 *       <td>standard</td>
 *    </tr>
 *    <tr>
 *       <td>loadResource</td>
 *       <td>The partial URL of the app and resource that can supply an xml
 *          document that contains serialized versions of the requested
 *          component type, in the format "app/resource.xml". Each child of
 *          the returned document will eventually be supplied to the
 *          <code>fromXml</code> method of a component of the type requested.
 *          The keys
 *          for all the components to be loaded will be included as multiple
 *          html params. An html param for each key part is created for each
 *          key. For example, if you have 2 simple keys whose part is "ID",
 *          the html param "ID" will appear twice, e.g., ID=3&ID=4.
 *          The system may also request all objects of this type by not
 *          including any keys. The implementor will typically handle this by
 *          making 2 resources with the same name and adding selection criteria
 *          based on the presence of the key. One of the apps will use a
 *          native select using an IN clause, and the PrepareInClause exit.
 *          The other one is a simple selector w/ no WHERE clauses.</td>
 *          </td>
 *       <td>required</td>
 *       <td>standard</td>
 *    </tr>
 *    <tr>
 *       <td>relationshipResource</td>
 *       <td>The partial URL of the app and resource that will be used when
 *          processing relationship changes such as insert, update and
 *          deletes. e.g., rx_ceArticle/article.xml?command=relation</td>
 *       <td>required</td>
 *       <td>standard</td>
 *    </tr>
 *    <tr>
 *       <td>rootElementName</td>
 *       <td>The name of the doc element that will wrap the component xml,
 *          both for loading and saving.</td>
 *       <td>required</td>
 *       <td>standard</td>
 *    </tr>
 *    <tr>
 *       <td>compTable</td>
 *       <td>The name of the table containing these components. Required for
 *          deleting if deleteResource not supplied, or for re-ordering
 *          support.</td>
 *       <td>maybe</td>
 *       <td>standard</td>
 *    </tr>
 *    <tr>
 *       <td>sequenceCol</td>
 *       <td>The name of the column containing the sort-rank value. Required
 *          for deleting if deleteResource not supplied, or for re-ordering
 *          support.</td>
 *       <td>maybe</td>
 *       <td>standard</td>
 *    </tr>
 *    <tr>
 *       <td>keyCol</td>
 *       <td>The name of the column containing the primary key. Required for
 *          deleting if deleteResource not supplied, or for re-ordering
 *          support.</td>
 *       <td>maybe</td>
 *       <td>standard</td>
 *    </tr>
 *    <tr>
 *       <td></td>
 *       <td></td>
 *       <td>required</td>
 *       <td>standard</td>
 *    </tr>
 * </table>
 *
 * @author Paul Howard
 * @version 1.0
 */
public abstract class PSProcessorCommon
   implements IPSComponentProcessor, IPSKeyGenerator
{
   /**
    * The maximum number of ids that can be pre-allocated or requested in a
    * single request.
    */
   public static final int MAX_IDS = 1000;

   /**
    * Default consructor
    */
   protected PSProcessorCommon()
   {
   }

   /**
    * Constructor that stores the supplied props for later use. Implementors
    * should not instantiate this class directly.
    *
    * @param props The set of required and optional properties for all
    *    component types, as specified in the class description. Each entry
    *    in the map has a String (component type) as the key and Map,
    *    as the value. Each map entry has a String key and String (standard
    *    property) or Element (custom property) value.
    *    <p>Properties come in 1 of 2 flavors, standard
    *    or custom. Standard properties are stored in the collection as
    *    Strings, custom properties as Elements. Never <code>null</code>.
    *    Missing properties will not be discovered until an operation is
    *    activated. A ref to this map is stored in this instance for
    *    later use, the map and the collections it contains are never modified
    *    by this class.
    *    <p>All key names should be lowercased to allow case-insensitive
    *    compares.
    */
   protected PSProcessorCommon(Map props)
   {
      if (null == props)
         throw new IllegalArgumentException("A property map must be supplied.");
      m_props = props;
   }


   /**
    * See {@link IPSComponentProcessor#save(IPSDbComponent[]) interface} for
    * general description.
    * <p>More specifically, this class performs the following steps:
    * <ol>
    *    <li>Get the component type from the component, if the type cannot
    *       be found in the props, or any required props are missing, an
    *       exception is thrown.</li>
    *    <li>Call <code>toDbXml</code> and create a document whose root
    *       element name was supplied in the props.</li>
    *    <li>Call the derived class to perform the actual save.</li>
    *    <li>If successful, call <code>setPersisted</code> on the components.
    *       </li>
    * </ol>
    */
   @SuppressWarnings("unchecked")
   public PSSaveResults save(IPSDbComponent [] components)
      throws PSCmsException
   {
      if (null == components)
         throw new IllegalArgumentException("Null array not allowed.");

      Collection groups = groupComponents(components);
      Iterator groupsIter = groups.iterator();
      PSProcessingStatistics totals = new PSProcessingStatistics(0,0);
      while (groupsIter.hasNext())
      {
         PSDbComponentCollection comps =
               (PSDbComponentCollection) groupsIter.next();
         Document inputDoc = PSXmlDocumentBuilder.createXmlDocument();

         //check for all required props up front
         String type = comps.getMemberComponentType();
         String rootName = getProperty(type, "updateRootElementName");
         String resourceName = getProperty(type, "saveResource");

         //update component version information
         updateDbComponentVersions(comps);
                 
         // build the doc
         Element root = PSXmlDocumentBuilder.createRoot(inputDoc, rootName);
         comps.toDbXml(inputDoc, root, this, null);

         /*>>>debug
         System.out.println("Doc sent to processor for save...");
         try{
         System.out.println("Input document for save:");
         PSXmlDocumentBuilder.write(inputDoc, System.out);
         }catch (Exception e) {}
         //*///<<<debug

         //save it
         PSProcessingStatistics stats = doSave(resourceName, inputDoc);
         totals = new PSProcessingStatistics(
               totals.getInsertedCount() + stats.getInsertedCount(),
               totals.getUpdatedCount() + stats.getUpdatedCount(),
               totals.getDeletedCount() + stats.getDeletedCount(),
               totals.getSkippedCount() + stats.getSkippedCount(),
               totals.getErroredCount() + stats.getErroredCount());

         comps.setPersisted();
      }
      return new PSSaveResults(components, totals);
   }


   /**
    * Derived classes add the communication to the server.
    *
    * @param resourceName Never <code>null</code> or empty. The name of the
    *    Rhythmyx resource in the format "appName/resourceName.xml". A query
    *    string is allowed.
    *
    * @param input The document ready to send to the specified resource.
    *    Never <code>null</code>.
    *
    * @throws PSCmsException If the save can't be performed for any reason,
    *    including authorization.
    */
   protected PSProcessingStatistics doSave(String resourceName, Document input)
      throws PSCmsException
   {
      throw new UnsupportedOperationException(
         "Not supported by this processor.");
   }


   /**
    * Looks up the requested property for the specified component type. If the
    * property is not found, an exception is thrown.
    *
    * @param type  Assumed to be a value returned by the {@link
    *    IPSDbComponent#getComponentType()} method.
    *
    * @param propName  Assumed not <code>null</code> or empty.
    *
    * @return The associated property, which may be empty, never
    *    <code>null</code>.
    *
    * @throws PSCmsException If the property for the requested type cannot be
    *    found in the config.
    */
   private String getProperty(String type, String propName)
      throws PSCmsException
   {
      Map values = (Map) m_props.get(type.toLowerCase());
      if ( null == values )
      {
         String [] args =
         {
            type,
            getClass().getName().substring(
                  getClass().getName().lastIndexOf('.')+1)
         };
         throw new PSCmsException(IPSCmsErrors.UNSUPPORTED_COMPONENT_TYPE,
               args);
      }

      String lcPropName = propName.toLowerCase();
      String value = (String) values.get(lcPropName);
      if (null == value && !values.containsKey(lcPropName))
      {
         String [] args =
         {
            propName,
            type
         };
         throw new PSCmsException(IPSCmsErrors.MISSING_PROPERTY, args);
      }

      return value == null ? "" : value;
   }


   /**
    * Maps the supplied array into 0 or more PSDbComponentCollection objects.
    * During processing, any <code>null</code> entries are removed. If any
    * entry is a PSDbComponentCollection, it is returned itself.
    *
    * @param comps  Assumed not <code>null</code>. <code>null</code> entries
    *    allowed in array.
    *
    * @return A set of PSDbComponentCollections. Any <code>null</code> entries
    *    in comps will not be included in the returned collections. Never
    *    <code>null</code>, may be empty.
    */
   private Collection groupComponents(IPSDbComponent[] comps)
   {
      try
      {
         Collection groups = new ArrayList();
         Map compGroups = new HashMap();
         for (int i=0; i < comps.length; i++)
         {
            if (comps[i] instanceof PSDbComponentCollection)
               groups.add(comps[i]);
            else
            {
               String type = comps[i].getComponentType();
               Collection c = (Collection) compGroups.get(type);
               if (null == c)
               {
                  c = new ArrayList();
                  compGroups.put(type, c);
               }
               c.add(comps[i]);
            }
         }

         Iterator iter = compGroups.keySet().iterator();
         while (iter.hasNext())
         {
            String type = (String) iter.next();
            Collection c = (Collection) compGroups.get(type);
            IPSDbComponent comp = (IPSDbComponent) c.iterator().next();
            PSDbComponentCollection compColl =
                  new PSDbComponentCollection(comp.getClass().getName());
            Iterator sameTypes = c.iterator();
            while (sameTypes.hasNext())
            {
               compColl.add((IPSDbComponent) sameTypes.next());
            }
            groups.add(compColl);
         }

         return groups;
      }
      catch (ClassNotFoundException cnf)
      {
         /*this should never happen because we are passing in the name
            from a Class object */
         throw new RuntimeException("Unexpected ClassNotFoundException: "
               + cnf.getLocalizedMessage());
      }
   }


   /**
    * See {@link IPSComponentProcessor#load(String,PSKey[]) interface} for
    * general description.
    * <p>More specifically, this class performs the following steps:
    * <ol>
    *    <li>Use the supplied componentType to find the required props to
    *       perform load. If the type cannot be found in the props, or any
    *       required props are missing, an exception is thrown.</li>
    *    <li>If successful, extract each element and assign it to the correct
    *       index in the Element array. If an Element is not found for a
    *       requested key, set that Element entry to <code>null</code>.</li>
    *    <li>For every key successfully loaded, set the persisted state of the
    *       key to <code>true</code>.</li>
    * </ol>
    */
   public Element [] load(String componentType, PSKey [] locators)
      throws PSCmsException
   {
      Map ids = prepareForModify(componentType, locators);

      //check for all required props up front
      String resourceName = getProperty(componentType, "loadResource");
      String rootName = getProperty(componentType, "queryRootElementName");

      Document doc = doLoad(resourceName, ids);
      if (null == doc)
         //none of the keys could be found
         return new Element[0];

      /*>>>debug
      System.out.println("Doc loaded by processor:");
      try{
      PSXmlDocumentBuilder.write(doc, System.out);
      }catch (java.io.IOException e){}
      //*///<<<debug

      PSXmlTreeWalker walker = new PSXmlTreeWalker(doc);
      Element root = walker.getNextElement(".");
      if (null == root)
         return new Element[0];

      if (!root.getNodeName().equals(rootName))
      {
         String[] args =
         {
            componentType,
            rootName,
            root.getNodeName()
         };
         throw new PSCmsException(
               IPSCmsErrors.SERIALIZED_COMPONENTS_WRONG_XML_DOC, args);
      }

      Element comp =
            walker.getNextElement(PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);

      Collection c = new ArrayList();
      while (null != comp)
      {
         c.add(comp);
         comp = walker.getNextElement(PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS);
      }

      Element[] e = new Element[c.size()];
      c.toArray(e);
      return e;
   }


   /**
    * Does work needed for updates and deletes.
    *
    * @param componentType Validates not <code>null</code>.
    *
    * @param locators Validates not <code>null</code>, that no entry is
    *    <code>null</code> and that the keys are all the same type.
    *
    * @return Never <code>null</code>. Contains at least 1 entry. There is
    *    an entry for each part in the
    *    supplied key. The key is the part name and the value is a String[]
    *    containing all the values for that part.
    *
    * @throws PSCmsException
    */
   private Map prepareForModify(String componentType, PSKey[] locators)
   {
      if ( null == componentType || componentType.trim().length() == 0)
         throw new IllegalArgumentException("Invalid component type.");
      //validate the entries
      if (null != locators)
      {
         for (int k=0; k < locators.length; k++)
         {
            if (null == locators[k])
               throw new IllegalArgumentException("Null keys not allowed.");
         }

         //verify all keys are same type
         for (int k=1; k < locators.length; k++)
         {
            if (!locators[0].isSameType(locators[k]))
            {
               throw new IllegalArgumentException(
                     "All keys must be same type.");
            }
         }
      }

      Map ids = new HashMap();
      if (null != locators && locators.length > 0)
      {
         int keyParts = locators[0].getPartCount();
         String[] keyDef = locators[0].getDefinition();
         for (int i=0; i < keyParts; i++)
         {
            String[] idSet = new String[locators.length];
            for ( int j=0; j < idSet.length; j++)
            {
               idSet[j] = locators[j].getPart(keyDef[i]);
            }
            ids.put(keyDef[i], idSet);
         }
      }
      return ids;
   }

   /**
    * Derived classes do the real work here.
    *
    * @param resourceName Never <code>null</code> or empty. The name of the
    *    Rhythmyx resource in the format "appName/resourceName.xml". A query
    *    string is allowed.
    *
    * @param ids Never <code>null</code>. Each entry is a pair whose key
    *    is a String and value is an String[]. The key is the name of the
    *    primary key part, while the value contains all desired ids for that
    *    key part. May be empty, indicating all instances are desired.
    *
    * @return A document with 1 or more serialized components.
    * <code>null</code> if no components were located.
    *
    * @throws PSCmsException If the save can't be performed for any reason,
    *    including authorization.
    */
   protected Document doLoad(String resourceName, Map ids)
      throws PSCmsException
   {
      throw new UnsupportedOperationException(
         "Not supported by this processor.");
   }


   /**
    * Derived classes do the real work here.
    *
    * @param resourceName Never <code>null</code> or empty. The name of the
    *    Rhythmyx resource in the format "appName/resourceName.xml". A query
    *    string is allowed.
    *
    * @param ids Never <code>null</code>. Each entry is a pair whose key
    *    is a String and value is an String[]. The key is the name of the
    *    primary key part, while the value contains all desired ids for that
    *    key part. May be empty, indicating all instances are desired.
    *
    * @return The number of components deleted.
    *
    * @throws PSCmsException If the delete can't be performed for any reason,
    *    including authorization.
    */
   protected int doDelete(String resourceName, Map ids)
      throws PSCmsException
   {
      throw new UnsupportedOperationException(
         "Not supported by this processor.");
   }

   /**
    * See {@link IPSComponentProcessor#delete(String,PSKey[]) interface} for
    * general description.
    * <p>More specifically, this class performs the following steps for each
    *    group of similar keys:
    * <ol>
    *    <li>Use the supplied componentType to find the required props to
    *       perform delete. If the type cannot be found in the props, or any
    *       required props are missing, an exception is thrown.</li>
    *    <li>If a deleteResource is supplied, for each key, create an html
    *       parameter whose name is the name of the key part. The # of instances
    *       of this param will be set based on how many keys are provided.</li>
    *    <li>If not supplied, then the compTable and sequenceCol and keyCol
    *       must be provided. In this case, cascading deletes are implemented
    *       using code and the foreign key relationships that exist in the
    *       db. </li>
    * </ol>
    */
   public int delete(String componentType, PSKey [] locators)
      throws PSCmsException
   {
      // TODO: implement the generic cascaded delete version.
      Map ids = prepareForModify(componentType, locators);
      //required by the resource or nothing will happen
      ids.put("DBActionType", new String[] {"DELETE"});

      String resourceName = getProperty(componentType, "deleteResource");
      return doDelete(resourceName, ids);
   }


   /**
    * See {@link IPSComponentProcessor#delete(IPSDbComponent[]) interface} for
    * general description. This method marks all supplied comps for deletion,
    * then sends them to the save method.
    */
   public int delete(IPSDbComponent[] comps)
      throws PSCmsException
   {
      for (int i=0; i < comps.length; i++)
      {
         if (null != comps[i])
            comps[i].markForDeletion();
      }
      PSSaveResults results = save(comps);
      return results.getResultStats().getDeletedCount();
   }


   //see interface for description
   public int delete(IPSDbComponent comp)
      throws PSCmsException
   {
      return delete(new IPSDbComponent[] {comp});
   }


   //see interface for description
   public int [] allocateIds(String lookup, int count)
      throws PSCmsException
   {
      if (count > MAX_IDS)
         count = MAX_IDS;
      return doAllocateIds(lookup, count);
   }


   /**
    * The derived class does the real work here. Must allocate a block of
    * numbers from the NextNumber table, using the supplied lookup as the
    * key.
    *
    * @param lookup Assumed not <code>null</code>.
    *
    * @param count Assumed > 0;
    *
    * @return An array with 0 or more entries.
    *
    * @throws PSCmsException If any problems while allocating the ids.
    */
   protected int[] doAllocateIds(String lookup, int count)
      throws PSCmsException
   {
      throw new UnsupportedOperationException(
         "Not supported by this processor.");
   }


   //see interface for description
   public int allocateId(String lookup)
      throws PSCmsException
   {
      lookup = lookup.toLowerCase();
      int id = Integer.MIN_VALUE;
      int [] cachedIds = (int[]) m_cachedIds.get(lookup);
      if (null != cachedIds)
      {
         for (int i=0; i < cachedIds.length; i++)
         {
            if (cachedIds[i] != Integer.MIN_VALUE)
            {
               id = cachedIds[i];
               if (i == cachedIds.length-1)
                  m_cachedIds.remove(lookup);
               else
                  cachedIds[i] = Integer.MIN_VALUE;
            }
         }
      }
      else
      {
         int[] ids = allocateIds(lookup, m_nextAllocationSize);
         //auto reset
         m_nextAllocationSize = 1;
         if (ids.length > 1)
         {
            m_cachedIds.put(lookup, ids);
         }
         id = ids[0];
      }
      return id;
   }


   //see interface for description
   public void setNextAllocationSize(int count)
   {
      if (count < 1)
         count = 1;
      else if (count > MAX_IDS)
         //max allowed
         count = MAX_IDS;
      m_nextAllocationSize = count;
   }

   /**
    * Updates the version of all versionable db components for the supplied
    * db component collection.  All versionable db components within any
    * collections, lists, and sets included in the collection will also be
    * updated.
    * 
    * @param comps The db component collection, assumed not <code>null</code>.
    */
   @SuppressWarnings("unchecked")
   private void updateDbComponentVersions(PSDbComponentCollection comps)
   {
      Iterator iter = comps.iterator();
      while (iter.hasNext())
      {
         Object component = iter.next();
         
         if (component instanceof PSDbComponentCollection ||
               component instanceof PSDbComponentList ||
               component instanceof PSDbComponentSet)
         {
            Iterator compIter = null;
            if (component instanceof PSDbComponentCollection)
            {
               PSDbComponentCollection dbCompColl =
                  (PSDbComponentCollection) component;
               compIter = dbCompColl.iterator();
            }
            else if (component instanceof PSDbComponentList)
            {
               PSDbComponentList dbCompList =
                  (PSDbComponentList) component;
               compIter = dbCompList.iterator();
            }
            else if (component instanceof PSDbComponentSet)
            {
               PSDbComponentSet dbCompSet =
                  (PSDbComponentSet) component;
               compIter = dbCompSet.iterator();
            }
            
            if (compIter != null)
               updateDbComponentVersions(compIter);
         }
         else if (component instanceof PSVersionableDbComponent)
         {            
            PSVersionableDbComponent dbComponent =
               (PSVersionableDbComponent) component;
            dbComponent.setVersion(dbComponent.getVersion() + 1);
         }
      }
   }
   
   /**
    * Updates the version of all versionable db components for the supplied
    * iterator.
    * 
    * @param iter The iterator, assumed not <code>null</code>.
    */
   @SuppressWarnings("unchecked")
   private void updateDbComponentVersions(Iterator iter)
   {
      while (iter.hasNext())
      {
         Object obj = iter.next();
         if (obj instanceof PSVersionableDbComponent)
         {
            PSVersionableDbComponent dbComponent =
               (PSVersionableDbComponent) obj;
            dbComponent.setVersion(dbComponent.getVersion() + 1);
         }
      }
   }
   
   /**
    * Stores the configuration information for the supported components.
    * Set in ctor, never <code>null</code> after that.
    */
   private Map m_props;

   /**
    * How many ids should be allocated the next time allocateId is called
    * (the extras are cached). Default is 1.
    * Always in range 1 - 1000 inclusive.
    */
   private int m_nextAllocationSize = 1;

   /**
    * If any ids are pre-allocated, they are stored in this map, using the
    * lookup as the key and the value is an int array of the ids. As each
    * id is allocated, it is replaced in the array with Integer.MIN_VALUE.
    * When there are no more entries in the array, the whole entry is removed
    * from the map.
    */
   private Map m_cachedIds = new HashMap();
}
