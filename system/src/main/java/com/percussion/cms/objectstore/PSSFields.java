/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.percussion.cms.objectstore;

import com.percussion.cms.PSCmsException;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import org.w3c.dom.Element;

import java.util.Iterator;


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
