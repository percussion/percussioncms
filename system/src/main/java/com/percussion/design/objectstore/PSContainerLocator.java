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

package com.percussion.design.objectstore;

import com.percussion.server.IPSServerErrors;
import com.percussion.util.PSCollection;
import com.percussion.xml.PSXmlTreeWalker;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Implements the PSXContainerLocator DTD defined in BasicObjects.dtd.
 */
public class PSContainerLocator extends PSComponent
{
   /**
    * Create a new table locator for the provided table sets.
    *
    * @param tableSets the table sets for this locator, a collection of
    *    PSTableSet objects, not <code>null</code> or empty.
    */
   public PSContainerLocator(PSCollection tableSets)
   {
      setTableSets(tableSets);
   }

   /**
    * Construct a Java object from its XML representation.
    *
    * @param sourceNode   the XML element node to construct this object from,
    *    not <code>null</code>.
    * @param parentDoc the Java object which is the parent of this object,
    *    not <code>null</code>.
    * @param parentComponents   the parent objects of this object, not
    *    <code>null</code>.
    * @throws PSUnknownNodeTypeException if the XML element node is not of
    *    the appropriate type
    */
   public PSContainerLocator(Element sourceNode, IPSDocument parentDoc,
                             List parentComponents)
      throws PSUnknownNodeTypeException
   {
      fromXml(sourceNode, parentDoc, parentComponents);
   }

   /**
    * Needed for serialization.
    */
   protected PSContainerLocator()
   {
   }

   /**
    * Get the collection of table sets.
    *
    * @return a collection of PSTableSet objects, never
    *    <code>null</code> or empty.
    */
   public Iterator getTableSets()
   {
      return m_tableSets.iterator();
   }

   /**
    * Set the new collection of new table sets (a collection of PSTableSet
    * objects).
    *
    * @param tableSets the new collection of table sets(PSTableSet objects),
    *    never <code>null</code>, never empty.
    */
   public void setTableSets(PSCollection tableSets)
   {
      if (tableSets == null || tableSets.isEmpty())
         throw new IllegalArgumentException("the table set cannot be null or empty");

      if (!tableSets.getMemberClassName().equals(
          m_tableSets.getMemberClassName()))
         throw new IllegalArgumentException("PSTableSet collection expected");

      m_tableSets.clear();
      m_tableSets.addAll(tableSets);
   }
   
   /**
    * Merges the table sets from the supplied system and shared definitions into
    * this locator.
    * @param systemDef The system def, may not be <code>null</code>.
    * @param sharedDef The shared def, may be <code>null</code>.
    * @param sharedFieldIncludes uppercased list of shared group names whose
    * tablesets are to be merged into this locator, may be <code>null</code> only
    * if <code>sharedDef</code> is <code>null</code>, may be empty.
    * 
    * @throws PSSystemValidationException If there is a table alias collision.
    */
   public void mergeTableSets(PSContentEditorSystemDef systemDef, 
      PSContentEditorSharedDef sharedDef, Collection sharedFieldIncludes) 
         throws PSSystemValidationException
   {
      if (systemDef == null)
         throw new IllegalArgumentException("systemDef may not be null");
      
      if (sharedDef != null && sharedFieldIncludes == null)
         throw new IllegalArgumentException(
            "sharedFieldIncludes may not be null if a sharedDef is supplied");

      // Add tables from the system def to the editor.
      PSContainerLocator sysTables = systemDef.getSystemLocator();

      // need to create a map of editor table refs for alias collision checks
      Map refMap = getTableRefs();

      // add the tablesets (should just be one)
      Iterator sysTableSets = sysTables.getTableSets();
      while (sysTableSets.hasNext())
      {
         mergeTableSet((PSTableSet)sysTableSets.next(), refMap);
      }

      PSContainerLocator sysContainer = systemDef.getContainerLocator();
      if (sysContainer != null)
      {
         /* need to re-create a map of editor table refs for alias collision
          * checks since tables may have been added from the system locator
          */
         refMap = getTableRefs();

         // walk the system def tablesets
         Iterator sourceTableSets = sysContainer.getTableSets();
         while (sourceTableSets.hasNext())
         {
            mergeTableSet((PSTableSet)sourceTableSets.next(), refMap);
         }
      }

      // add tables from shared groups, only processing included groups
      if (sharedDef != null)
      {
         Iterator groups = sharedDef.getFieldGroups();
         while (groups.hasNext())
         {
            PSSharedFieldGroup group = (PSSharedFieldGroup) groups.next();
            
            PSFieldSet groupFields = group.getFieldSet();
            if (!sharedFieldIncludes.contains(group.getName().toUpperCase()) && 
               !groupFields.hasMandatorySystemFields())
               continue;

            PSContainerLocator groupLocator = group.getLocator();
            /* need to re-create a map of editor table refs for alias collision
             * checks since tables may have been added from the system def
             */
            refMap = getTableRefs();

            // walk the group locators tablesets
            Iterator sourceTableSets = groupLocator.getTableSets();
            while (sourceTableSets.hasNext())
               mergeTableSet((PSTableSet) sourceTableSets.next(), refMap);
         }
      }
   }
   
   /**
    * Returns a map of all table ref aliases contained in this locator, with 
    * table ref alias uppercased as the key and the tableset containing it as 
    * the value.
    * 
    * @return The HashMap, never <code>null</code>.
    */
   private Map getTableRefs()
   {
      // build a map of refs
      Map refMap = new HashMap();

      // walk the table sets
      Iterator tableSets = getTableSets();
      while (tableSets.hasNext())
      {
         // walk the table refs
         PSTableSet tableSet = (PSTableSet)tableSets.next();
         Iterator refs = tableSet.getTableRefs();
         while (refs.hasNext())
         {
            PSTableRef ref = (PSTableRef)refs.next();
            refMap.put(ref.getAlias().toUpperCase(), tableSet);
         }
      }
      return refMap;
   }   

   /**
    * Merges fields from the source tableset into the container's
    * tableset collection.  Will check for any alias collisions (same alias,
    * different table) in a case insensitive manner.  Any duplicate table refs
    * within the same table locator will not be added.
    *
    * @param source The source tableset, assumed not <code>null</code>.
    * @param refMap A hash map of all table ref aliases from the target
    * container with the table ref alias (uppercased) as the key and the
    * tableset of that ref as the value. Used to check for alias collisions, 
    * assumed not <code>null</code>
    *
    * @throws PSSystemValidationException if there is an alias collision.
    */
   private void mergeTableSet(PSTableSet source, Map refMap) 
      throws PSSystemValidationException
   {
      // copy the source cause we're going to modify it
      PSTableSet tmpSource = new PSTableSet( source );

      /* need to be sure that there is not a conflict between any system
       * table alias and any target table alias
       */
      Iterator sourceRefs = tmpSource.getTableRefs();
      while (sourceRefs.hasNext())
      {
         PSTableRef sourceRef = (PSTableRef)sourceRefs.next();

         /* see if there is any target tableset containing this alias.  There
          * can only be one possible match as the alias should already be unique
          * across the whole container.
          */
         PSTableSet targetTableSet = (PSTableSet)refMap.get(
            sourceRef.getAlias().toUpperCase());
         String badTableName = null;
         if (targetTableSet != null)
         {
            // make sure it is not the same exact table
            PSTableLocator targetLocator = targetTableSet.getTableLocation();
            PSTableLocator sourceLocator = tmpSource.getTableLocation();
            if (sourceLocator.isSameLocation(targetLocator))
            {
               // same database, now see if the table itself matches
               boolean isDupe = false;
               Iterator refs = targetTableSet.getTableRefs();
               while (refs.hasNext())
               {
                  PSTableRef ref = (PSTableRef)refs.next();
                  if (ref.getAlias().equalsIgnoreCase(sourceRef.getAlias()))
                  {
                     if (ref.equals(sourceRef))
                     {
                        // this is just a dupe, remove it so we do not add it later
                        tmpSource.removeTableRef(sourceRef);
                        isDupe = true;
                     }
                     else
                     {
                        badTableName = ref.getName();
                     }
                     break;
                  }
               }

               if (isDupe)
                  break;
            }

            // if we are here, then this is a duplicate alias
            Object[] args = {sourceRef.getAlias(), badTableName,
               sourceRef.getName()};
            throw new PSSystemValidationException(
               IPSServerErrors.CE_TABLE_ALIAS_DUPLICATE, args);
         }
      }

      // find a matching locator to merge into, otherwise add the whole set
      PSTableLocator sourceLocator = tmpSource.getTableLocation();
      Iterator targetSets = getTableSets();
      boolean merged = false;
      while (targetSets.hasNext())
      {
         PSTableSet targetTableSet = (PSTableSet)targetSets.next();
         if (sourceLocator.isSameLocation(targetTableSet.getTableLocation()))
         {
            // merge the fields (we've already removed dupes)
            PSCollection targetTableRefs = new PSCollection(
               targetTableSet.getTableRefs());
            targetTableRefs.addAll(new PSCollection(tmpSource.getTableRefs()));
            targetTableSet.setTableRefs(targetTableRefs);

            merged = true;
            break;
         }
      }

      // add it if we haven't removed all tables as dupes
      if (!merged && tmpSource.getTableRefs().hasNext())
      {
         // add the tableset to the container's collection
         PSCollection col = new PSCollection(getTableSets());
         col.add(tmpSource);
         setTableSets(col);
      }
   }
   
   /**
    * Performs a shallow copy of the data in the supplied component to this
    * component. Derived classes should implement this method for their data,
    * calling the base class method first.
    *
    * @param c a valid PSContainerLocator, not <code>null</code>.
    */
   public void copyFrom(PSContainerLocator c)
   {
      try
      {
         super.copyFrom(c);
      }
      catch (IllegalArgumentException e)
      {
         throw new IllegalArgumentException(e.getMessage());
      }

      setTableSets(c.m_tableSets);
   }

   /**
    * Builds fully defined PSBackEndTables for every table specified in this
    * locator.
    *
    * @return A map whose key is the table alias (lowercased) and whose value
    *    is a PSBackEndTable that has all properties properly specified. Never
    *    <code>null</code>, could be empty.
    */
   public Map getBackEndTables()
   {
      Map tableMap = null;
      try
      {
         tableMap = new HashMap();
         Iterator tableSets = getTableSets();
         while (tableSets.hasNext())
         {
            PSTableSet tableSet = (PSTableSet)tableSets.next();
            PSTableLocator tableLocator = tableSet.getTableLocation();
            Iterator tableRefs = tableSet.getTableRefs();
            while (tableRefs.hasNext())
            {
               PSTableRef tableRef = (PSTableRef)tableRefs.next();
               PSBackEndTable table = new PSBackEndTable(tableRef.getAlias());
               table.setInfoFromLocator(tableLocator);
               table.setTable(tableRef.getName());
               tableMap.put( table.getAlias().toLowerCase(), table );
            }
         }
      }
      catch ( IllegalArgumentException iae )
      {
         // this won't happen
         throw new IllegalArgumentException( iae.getLocalizedMessage());
      }

      return tableMap;
   }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PSContainerLocator)) return false;
        if (!super.equals(o)) return false;
        PSContainerLocator that = (PSContainerLocator) o;
        return Objects.equals(m_tableSets, that.m_tableSets);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), m_tableSets);
    }

    /**
    *
    * @see IPSComponent
    */
   public void fromXml(Element sourceNode, IPSDocument parentDoc,
                       List parentComponents)
      throws PSUnknownNodeTypeException
   {
      if (sourceNode == null)
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_NULL, XML_NODE_NAME);

      if (!XML_NODE_NAME.equals(sourceNode.getNodeName()))
      {
         Object[] args = { XML_NODE_NAME, sourceNode.getNodeName() };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_WRONG_TYPE, args);
      }

      parentComponents = updateParentList(parentComponents);
      int parentSize = parentComponents.size() - 1;

      int firstFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN |
         PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;
      int nextFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS |
         PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;

      Element node = null;
      try
      {
         PSXmlTreeWalker tree = new PSXmlTreeWalker(sourceNode);

         // REQUIRED: get the table sets
         node = tree.getNextElement(PSTableSet.XML_NODE_NAME, firstFlags);
         if (node == null)
         {
            Object[] args =
            {
               XML_NODE_NAME,
               PSTableSet.XML_NODE_NAME,
               "null"
            };
            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
         }
         while(node != null)
         {
            m_tableSets.add(new PSTableSet(node, parentDoc, parentComponents));
            node = tree.getNextElement(PSTableSet.XML_NODE_NAME, nextFlags);
         }
      }
      finally
      {
         resetParentList(parentComponents, parentSize);
      }
   }

   /**
    *
    * @see IPSComponent
    */
   public Element toXml(Document doc)
   {
      // create root and its attributes
      Element root = doc.createElement(XML_NODE_NAME);

      // REQUIRED: create the table sets
      Iterator it = getTableSets();
      while (it.hasNext())
         root.appendChild(((IPSComponent) it.next()).toXml(doc));

      return root;
   }

   // see IPSComponent
   public void validate(IPSValidationContext context)
      throws PSSystemValidationException
   {
      if (!context.startValidation(this, null))
         return;

      // do children
      context.pushParent(this);
      try
      {
         if (m_tableSets == null)
         {
            context.validationError(this,
               IPSObjectStoreErrors.INVALID_CONTAINER_LOCATOR, null);
         }
         else
         {
            Iterator it = getTableSets();
            while (it.hasNext())
               ((PSTableSet) it.next()).validate(context);
         }
      }
      finally
      {
         context.popParent();
      }
   }

   /** the XML node name */
   public static final String XML_NODE_NAME = "PSXContainerLocator";

   /**
    * A collection of PSTableSet objects, never <code>null</code> after
    * construction.
    */
   private PSCollection m_tableSets =
      new PSCollection((new PSTableSet()).getClass());
}

