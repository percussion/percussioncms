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

import com.percussion.cms.PSCmsException;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;

import java.util.Iterator;

import org.w3c.dom.Element;


/**
 * A simple wrapper around PSDbComponentList that simplifies use by providing
 * the class name of the class is stored in the list.
 */
public class PSSFields extends PSDbComponentList
{
   /*
    * Default constructor. See {@link com.percussion.cms.objectstore.PSDbComponentList()}
    * for more details.
    */
   public PSSFields() throws PSCmsException
   {
      super(PSSearchField.class);
   }

   /*
    * Ctor for reserializing. See {@link com.percussion.cms.objectstore.PSDbComponentList(Element)}
    * base ctor for more details.
    */
   public PSSFields(Element src) throws PSUnknownNodeTypeException
   {
      super(src);
   }

   /**
    * Get the XML node name for this class.
    *
    * @return the XML node name, never <code>null</code> or empty.
    */
   public String getNodeName()
   {
      return XML_NODE_NAME;
   }

   /**
    * Sees if this collection has a field object
    * with a matching key.
    *
    * @param field field to check, it may not be <code>null</code>.
    *
    * @return object that has the same key or <code>null</code>
    *    if one does not exist.
    */
   public PSSearchField haveThisFieldKey(PSSearchField field)
   {
      if (field == null)
         throw new IllegalArgumentException(
            "field must not be null");

      Iterator iter = iterator();
      PSKey key = field.getLocator();

      while (iter.hasNext())
      {
         PSSearchField f = (PSSearchField) iter.next();

         if (f.getLocator().equals(key))
            return f;
      }

      return null;
   }

   /**
    * Makes the supplied set of fields the only fields in this list. If any
    * field is already present and unchanged at its equivalent ordinal
    * position, it will not be replaced.
    *
    * @param fields the specified field set. If it is <code>null</code> or empty, 
    *    all fields are cleared. If the iterator contains any entries, they must
    *    be {@link com.percussion.cms.objectstore.PSSearchField} objects.
    *
    * @throws ClassCastException if the objects returned by the iterator are
    *    not PSSearchFields.
    */
   public void setFields(Iterator fields)
   {
      if (fields == null || !fields.hasNext())
      {
         clear();
         return;
      }

      for (int i = 0; fields.hasNext(); i++)
      {
         PSSearchField f = (PSSearchField) fields.next();
         if (i < size() && ((PSSearchField) get(i)).equals(f))
            continue;
         set(i, f);
      }
   }

   /**
    * Removes the supplied set of fields.
    *
    * @param fields the to be removed fields. If it is <code>null</code> or 
    *    empty no field will be removed
    *
    * @throws ClassCastException if the objects returned by the iterator are
    *    not PSSearchFields.
    */
   public void removeFields(Iterator fields)
   {
      if(fields == null)
         return;
      while(fields.hasNext())
         remove((IPSDbComponent)fields.next());
   }

   /**
    * The XML node name for this class.
    */
   public static final String XML_NODE_NAME = "PSX_FIELDS";
}
