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
import com.percussion.design.objectstore.PSContentEditorMapper;
import com.percussion.design.objectstore.PSContentEditorPipe;
import com.percussion.design.objectstore.PSDisplayMapper;
import com.percussion.design.objectstore.PSDisplayMapping;
import com.percussion.design.objectstore.PSField;
import com.percussion.design.objectstore.PSFieldSet;
import com.percussion.design.objectstore.PSUIDefinition;

import java.util.Iterator;

/**
 * This class extracts definition data for <code>PSCoreItem</code> object
 * population.  It parses a <code>PSContentEditor</code> object and populates a
 * <code>PSCoreItem</code> object from that definition.  This cannot be
 * instantiated explicitely by callers.  This also uses a Visitor like pattern,
 * passing itself to acceptable object methods, and those objects understand
 * to call <code>getObject()</code> on this class.
 */
public class PSItemDefExtractor implements IPSVisitor
{
   /**
    * Cannot be instantiated by outsiders.  Does any initialization then calls
    * processFieldSet();
    *
    * @param coreItem - assumed not <code>null</code>
    * @throws PSCmsException if an error occurs populating the
    * <code>coreItem</code>
    */
   private PSItemDefExtractor(PSCoreItem coreItem) throws PSCmsException
   {
      m_coreItem = coreItem;
      processFieldSet(getFieldSet(), m_coreItem, false);
   }

   /**
    * Populates the <code>PSCoreItem</code> with it's definition.  This a
    * utility method that removes this responsibility from the
    * <code>PSCoreItem</code>.  This allows high-cohesion in the
    * <code>PSCoreItem</code> and lowers the coupling between the
    * <code>PSCoreItem</code> and the <code>com.percussion.objectstore</code>
    * package.
    *
    * @param coreItem must not be <code>null</code>.
    * @throws PSCmsException if an error occurs populating the
    * <code>coreItem</code>
    */
   public static void populateItemDefinition(PSCoreItem coreItem)
      throws PSCmsException
   {
      if(coreItem == null)
         throw new IllegalArgumentException("coreItem must not be null");

      PSItemDefExtractor xtr = new PSItemDefExtractor(coreItem);
   }

   /**
    * This is the method that does the work.  Given a <code>PSFieldSet</code>
    * it will create <code>PSItemFields</code>.  If an element of the
    * <code>PSFieldSet</code> is another <code>PSFieldSet</code> and is of
    * type complex child it creates a
    * <code>PSItemChild</code> and <code>PSItemChildEntry</code> and then
    * recursively calls itself to add the <code>PSItemFields</code> to the
    * <code>PSItemChildEntry</code>.
    *
    * The PSCoreItem and the <code>PSItemChildEntry</code> are
    * <code>IPSItemAccessor</code> objects and this object is a
    * <code>IPSVisitor</code>.  This passes itself to the
    * <code>IPSItemAccessors</code> <code>accept()</code> method.  The
    * <code>IPSItemAccessor</code> then acts upon this object by
    * calling <code>getObject()</code>.
    *
    * @param fieldSet the fieldset to parse - assumed not <code>null</code>
    * @param itemAccessor the item on which to add the elements -
    * assumed not <code>null</code>
    * @param isMultiValue <code>true</code> if it is, otherwise
    * <code>false</code>.
    */
   private void processFieldSet(
      PSFieldSet fieldSet, IPSItemAccessor itemAccessor, boolean isMultiValue)
   {
      Iterator it = fieldSet.getAll();
      PSField field = null;
      PSDisplayMapping mapping = null;
      while (it.hasNext())
      {
         Object o = it.next();
         if (o instanceof PSFieldSet)
         {
            PSFieldSet childSet = (PSFieldSet)o;

            //Is it multiPropertySimpleChild
            if(childSet.getType() == PSFieldSet.TYPE_MULTI_PROPERTY_SIMPLE_CHILD)
               // just add the fields to the parent core item
               processFieldSet(childSet, m_coreItem, false);

            // Is it  simpleChild
            else if(childSet.getType() == PSFieldSet.TYPE_SIMPLE_CHILD)
               // this is a multivalue field, add to parent:
               processFieldSet(childSet, m_coreItem, true);

            else if(childSet.getType() == PSFieldSet.TYPE_COMPLEX_CHILD)
            {
               mapping = getDisplayMapping(childSet.getName());
               if (mapping == null)
                  continue; // ignore non-mapped fields

               // create child
               PSItemChild child = new PSItemChild(childSet, mapping);

               // create entry (which is item accessor) and add entry to child
               PSItemChildEntry  entry = child.createChildEntry();

               // add child to item:
               m_object = child;

               // TODO: SUPPORT CHILDREN OF CHILDREN???
               itemAccessor.accept(this);

               // null the object:
               m_object = null;

               // now let's recurse and add the fields to the entry:
               processFieldSet(childSet, entry, false);
            }
         }
         else if (o instanceof PSField)
         {
            // handle fields:
            field = (PSField)o;
            // get ui set:
            mapping = getDisplayMapping(field.getSubmitName());
            if (mapping == null)
               continue; // ignore non-mapped fields
               
            // create field and set to member
            m_object = new PSItemField(field, mapping.getUISet(), isMultiValue);
            // send this object to accessor for extraction
            itemAccessor.accept(this);
            // null the value for the next element.
            m_object = null;
         }
      }
   }

   /**
    * Returns the <code>PSDisplayMapping</code> for the specified field name.  
    * This depends on <code>getFieldSet()</code> being called first.  Which is 
    * called by the ctor.
    *
    * @param fieldName - assumed not <code>null</code> or empty,
    * is case sensitive
    * 
    * @return may be <code>null</code> as fields are not required to have a 
    * <code>PSDisplayMapping</code>
    */
   private PSDisplayMapping getDisplayMapping(String fieldName)
   {
      // get ui definition for the label:
      PSUIDefinition ceUiDef = m_parentMapper.getUIDefinition();
   
      // get the display mapper:
      PSDisplayMapper disMpr = ceUiDef.getDisplayMapper();
   
        // get the display mapping:
      PSDisplayMapping disMapping = disMpr.getMapping(fieldName);
   
      return disMapping;
   }

   /**
    * Returns an <code>Object</code>.  This is called by a
    * <code>IPSItemAccessor</code> when parsing a definition.
    * @return Object - may be <code>null</code>
    */
   public Object getObject()
   {
      return m_object;
   }

   /**
    * Gets the field set from the content editor, called by the ctor.
    * @return the parent field set, never <code>null</code>.
    */
   private PSFieldSet getFieldSet() throws PSCmsException
   {
      // get pipe:
      PSContentEditorPipe cePipe = (PSContentEditorPipe)
         m_coreItem.getItemDefinition().getContentEditor().getPipe();

      if(cePipe == null)
         throw new PSCmsException(
            IPSCmsErrors.DATA_EXTRACTION_ERROR_NULL_DATAPIPE);

      // get Mapper:
      PSContentEditorMapper ceMapper = cePipe.getMapper();

      // get field set:
      PSFieldSet ceFieldSet = ceMapper.getFieldSet();

      m_parentMapper = ceMapper;

      return ceFieldSet;
   }

   /**
    * The definition to be used to create the <code>PSCoreItem</code>, set by
    * <code>getFieldSet()</code> and should not change and should not be
    * <code>null</code>.
    */

   /**
    * The Parent Mapper.  The top most mapper of the
    * <code>PSContentEditor</code>, set by <code>getFieldSet()</code>, never
    * <code>null</code>.
    */
   private PSContentEditorMapper m_parentMapper;

   /**
    * The PSCoreItem being populated, set by the ctor, never <code>null</code>.
    */
   private PSCoreItem  m_coreItem;

   /**
    * Temporary field.  Mostly <code>null</code>.  Used in creation of objects.
    */
   private Object m_object;
}
