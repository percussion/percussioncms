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

import com.percussion.util.PSXMLDomUtil;
import com.percussion.xml.PSXmlTreeWalker;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Implementation for the PSXFieldSet DTD in BasicObjects.dtd.
 */
public class PSFieldSet extends PSComponent
{
   /**
    * Creates a new parent field set for the provided name.
    *
    * @param name the field set name, not <code>null</code> or empty.
    */
   public PSFieldSet(String name)
   {
      setName(name);
   }

   /**
    * Creates a new parent field set for the provided name and field.
    *
    * @param name the field set name, not <code>null</code> or empty.
    * @param field the field, not <code>null</code>.
    */
   public PSFieldSet(String name, PSField field)
   {
      this(name, TYPE_PARENT, field);
   }

   /**
    * Creates a new field set for the provided name, type and field.
    *
    * @param name the field set name, not <code>null</code>
    * @param type the field type to create.
    * @param field the field, not <code>null</code>.
    */
   public PSFieldSet(String name, int type, PSField field)
   {
      setType(type);
      setName(name);

      if (field == null)
         throw new IllegalArgumentException("field cannot be null");

      add(field);
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
   public PSFieldSet(Element sourceNode, IPSDocument parentDoc,
                     ArrayList parentComponents)
      throws PSUnknownNodeTypeException
   {
      fromXml(sourceNode, parentDoc, parentComponents);
   }

   /**
    * Copy constructor, creates a shallow copy.
    *
    * @param source the source to create a copy from, not <code>null</code>.
    */
   public PSFieldSet(PSFieldSet source)
   {
      if (source == null)
         throw new IllegalArgumentException("source cannot be null");

      copyFrom(source);
   }

   /**
    * Needed for serialization.
    */
   protected PSFieldSet()
   {
   }

   /**
    * Get the field or field set for the provided name contained by this
    * field set.
    *
    * @param name the name (key) of the field we want back, not
    *    <code>null</code>, may be empty.  TODO: Must we allow empty?
    * @return the object (PSField or PSFieldSet) found for the provided name
    *    or <code>null</code> if not found.
    */
   public Object get(String name)
   {
      if (name == null)
         throw new IllegalArgumentException("the name cannot be null");

      return m_fields.get(name);
   }


   /**
    * Get the child fieldset of the specified type containing the field matching
    * the provided name.
    *
    * @param name the name of the field whose parent fieldSet we want back, not
    *    <code>null</code> or empty.
    *
    * @param type The type of child fieldset to check for this field.  Must be
    * one of the valid fieldset TYPE_XXX types.
    *
    * @return The first child fieldset of the specified type that contains the
    * specified field, or <code>null</code> if not found.  Only the immediate
    * child fieldsets are checked.  If the name matches a child fieldset,
    * it is not returned.
    */
   public PSFieldSet getChildsFieldSet(String name, int type)
   {
      if (name == null || name.trim().length() == 0)
         throw new IllegalArgumentException("name may not be null or empty");

      if (!isValidType(type))
         throw new IllegalArgumentException("unknown type");

      PSFieldSet result = null;

      // walk this fieldset's members
      Iterator fields = m_fields.values().iterator();
      while (fields.hasNext())
      {
         Object o = fields.next();
         if (!(o instanceof PSFieldSet))
            continue;

         PSFieldSet fieldSet = (PSFieldSet)o;
         if (fieldSet.getType() != type)
            continue;

         o = fieldSet.get(name);
         if ((o != null) && (o instanceof PSField))
         {
            result = fieldSet;
            break;
         }
      }

      return result;
   }


   /**
    * Get the field matching the provided name contained by a child fieldset
    * of the specified type.
    *
    * @param name the name of the field we want back, not
    *    <code>null</code> or empty.
    *
    * @param type The type of child fieldset to check for this field.  Must be
    * one of the valid fieldset TYPE_XXX types.
    *
    * @return The first matching field found in a child fieldset of the
    * specified type, or <code>null</code> if not found.  Only the immediate
    * child fieldsets are checked.  If the name matches a child fieldset,
    * it is not returned.
    */
   public PSField getChildField(String name, int type)
   {
      if (name == null || name.trim().length() == 0)
         throw new IllegalArgumentException("name may not be null or empty");

      if (!isValidType(type))
         throw new IllegalArgumentException("unknown type");

      PSField result = null;

      // walk this fieldset's members
      Iterator fields = m_fields.values().iterator();
      while (fields.hasNext())
      {
         Object o = fields.next();
         if (!(o instanceof PSFieldSet))
            continue;

         PSFieldSet fieldSet = (PSFieldSet)o;
         if (fieldSet.getType() != type)
            continue;

         o = fieldSet.get(name);
         if ((o != null) && (o instanceof PSField))
         {
            result = (PSField)o;
            break;
         }

      }

      return result;
   }

   /**
    * Get the field matching the provided name contained by a fieldset. The
    * match is done using a case insensitive compare of the names.
    *
    * @param name   the name of the field we want back, not
    *               <code>null</code> or empty.
    *
    * @return       The first matching field found in a fieldset, or
    *               <code>null</code> if not found.
    */
   public PSField findFieldByName(String name)
   {
      return findFieldByName(name, false);
   }

   /**
    * Get the field matching the provided name contained by a fieldset. The
    * match is done using a case insensitive compare of the names.
    *
    * @param name   the name of the field we want back, not
    *               <code>null</code> or empty.
    *
    * @return       The first matching field found in a fieldset, or
    *               <code>null</code> if not found.
    */
   public PSField findFieldByName(String name, boolean systemModOnly)
   {
       if (name == null || name.trim().length() == 0)
           throw new IllegalArgumentException("name may not be null or empty");

       // walk this fieldset's members
       Iterator iter = m_fields.values().iterator();
       while (iter.hasNext())
       {
           Object o = iter.next();
           if (o instanceof PSFieldSet)
           {
               PSField field =
                     ((PSFieldSet)o).findFieldByName(name, systemModOnly);
               if (field != null)
               {
                   return field;
               }
           }
           else
           {
               PSField field = (PSField)o;
               if (isSystemModOnly(field, systemModOnly))
               {
                  if (name.equalsIgnoreCase(field.getSubmitName()))
                  {
                      return field;
                  }
               }
           }
       }
       return null;
   }


   /**
    * Gets the single simple child field found in this fieldset.  May only be
    * called against a fieldset of type {@link #TYPE_SIMPLE_CHILD}.
    *
    * @Return The field, never <code>null</code>.
    *
    * @throws IllegalStateException if this fieldset is not of the type {@link
    * #TYPE_SIMPLE_CHILD}.
    * @throws PSSystemValidationException if this fieldset contains more than one
    * field or any child fieldsets are found.
    */
   public PSField getSimpleChildField() throws PSSystemValidationException
   {
      if (getType() != PSFieldSet.TYPE_SIMPLE_CHILD)
         throw new IllegalStateException(
            "This fieldset not a Simple Child");

      Iterator fields = getAll();
      if ( !fields.hasNext())
      {
         String [] args = { getName(), ""+1, ""+1 };
         throw new PSSystemValidationException(
               IPSObjectStoreErrors.CE_INCORRECT_FIELD_COUNT, args );
      }

      Object o = fields.next();
      if ( !( o instanceof PSField ))
      {
         throw new PSSystemValidationException(
               IPSObjectStoreErrors.CE_CANNOT_HAVE_FIELDSETS, getName());
      }

      PSField field = (PSField) o;
      if ( fields.hasNext())
      {
         String [] args = { getName(), ""+0, ""+1 };
         throw new PSSystemValidationException(
               IPSObjectStoreErrors.CE_INCORRECT_FIELD_COUNT, args );
      }

      return field;
   }

   /**
    * Add the provided field to this ConcurrentHashMap.
    *
    * @param field the field to add to this ConcurrentHashMap, not <code>null</code>,
    *    must contain a valid name.
    * @return the previous value if there was one, <code>null</code>
    *    otherwise
    */
   public Object add(PSField field)
   {
      if (field == null)
         throw new IllegalArgumentException("the field cannot be null");

      String name = field.getSubmitName();
      if (name == null || name.length() == 0)
         throw new IllegalArgumentException(
            "the provided field must have a valid name");

      return m_fields.put(name, field);
   }

   /**
    * Add the provided field set to this ConcurrentHashMap.
    *
    * @param fieldSet the field set to add to this ConcurrentHashMap, not
    *    <code>null</code>, must contain a valid name.
    * @return the previous value if there was one, <code>null</code>
    *    otherwise.
    */
   public Object add(PSFieldSet fieldSet)
   {
      if (fieldSet == null)
         throw new IllegalArgumentException("the field set cannot be null");

      String name = fieldSet.getName();
      if (name == null || name.length() == 0)
         throw new IllegalArgumentException(
            "the provided field set must have a valid name");

      return m_fields.put(name, fieldSet);
   }

   /**
    * Remove the field or field set for the provided name.
    *
    * @name the field/field set name to remove, may be <code>null</code> or
    *    empty.
    * @return the object removed (PSField or PSFieldSet) or <code>null</code>
    *    if not found.
    */
   public Object remove(String name)
   {
      if (name == null)
         return null;

      return m_fields.remove(name);
   }

   /**
    * Removes all fields and fieldsets it contains.
    */
   public void removeAll()
   {
      m_fields.clear();
   }

   /**
    * Checks if the ConcurrentHashMap contains an object for the provided name.
    *
    * @param name the name of the object we are looking for, might be
    *    <code>null</code> or empty.
    * @return <code>true</code> if this contains an object for the
    *    provided name, <code>false</code> otherwise.
    */
   public boolean contains(String name)
   {
      if (StringUtils.isBlank(name))
         return false;
      return m_fields.containsKey(name);
   }

   /**
    * Get the field set name.
    *
    * @return the current field set name, never <code>null</code> or empty.
    */
   public String getName()
   {
      return m_name;
   }

   /**
    * Set the field set name.
    *
    * @param name the new field set name, not <code>null</code> or empty.
    */
   public void setName(String name)
   {
      if (name == null || name.trim().length() == 0)
         throw new IllegalArgumentException("name cannot be null or empty");

      m_name = name;
   }

   /**
    * Get the type of this field set.
    *
    * @return the field set type.
    */
   public int getType()
   {
      return m_type;
   }

   /**
    * Set the field set type.
    *
    * @param type the new field set type.
    */
   public void setType(int type)
   {
      if (!isValidType(type))
         throw new IllegalArgumentException("unknown type");

      m_type = type;
   }


   /**
    * Get the source type of this field set.
    *
    * @return the fieldset source type.
    */
   public int getSourceType()
   {
      return m_sourceType;
   }

   /**
    * Set the field set source type. Sets the type to all the fields in this
    * fieldset.
    *
    * @param type the new field set source type must be one of the
    * TYPE_XXX values of the field.
    */
   public void setSourceType(int type)
   {
      try {
         PSField.validateType( type );
      }
      catch(PSSystemValidationException e)
      {
         throw new IllegalArgumentException(e.getLocalizedMessage());
      }
      m_sourceType = type;

      Iterator fields = getAll();
      while (fields.hasNext())
      {
         Object test = fields.next();
         if (test instanceof PSField)
         {
            PSField field = (PSField) test;
            field.setType(type);
         }
         else if (test instanceof PSFieldSet)
         {
            ((PSFieldSet) test).setSourceType(type);
         }
      }
   }

   /**
    * Get the repeatability settting.
    *
    * @return the repeatability.
    */
   public int getRepeatability()
   {
      return m_repeatability;
   }

   /**
    * Set a new repeatability setting. Clears the count setting. Use setCount to
    * set the repeatability to REPEATABILITY_COUNT.
    *
    * @param repeatability the new repeatability.
    */
   public void setRepeatability(int repeatability)
   {
      if (repeatability != REPEATABILITY_ZERO_OR_MORE &&
          repeatability != REPEATABILITY_ONE_OR_MORE)
         throw new IllegalArgumentException("unknown repeatablility");

      m_repeatability = repeatability;
      m_count = 0;
   }

   /**
    * Set a new count. This will also set the repeatability to
    * REPEATABILITY_COUNT.
    *
    * @param count the new count, must be greater than 0.
    */
   public void setCount(int count)
   {
      if (count <= 0)
         throw new IllegalArgumentException("count must be greater than 0");

      m_repeatability = REPEATABILITY_COUNT;
      m_count = count;
   }

   /**
    * Get the number of rows required.
    *
    * @return the number of rows required.
    */
   public int getCount()
   {
      return m_count;
   }

   /**
    * Get all field / field set names stored in this field set.
    *
    * @return an iterator over all keys for this field set. Never
    *    <code>null</code>, may be empty.
    */
   public Iterator getNames()
   {
      return getNames(false);
   }

   /**
    * Either get all fields that are only modifiable by the system or all
    * fields except those.
    *
    * @param systemModOnly flag to determine which set of fields to return.
    *
    * @return an iterator over all keys for this field set. Never
    *    <code>null</code>, may be empty.
    */
   public Iterator getNames(boolean systemModOnly)
   {
      List<String> retList = new ArrayList<>();
      Iterator i = m_fields.keySet().iterator();
      while (i.hasNext())
      {
         String key = (String)i.next();
         PSField f = (PSField)m_fields.get(key);
         if (isSystemModOnly(f, systemModOnly))
         {
            retList.add(key);
         }
      }
      return retList.iterator();
   }

   /**
    * Convenience method equivalent to calling
    * {@link #getAll(boolean) getAll(<code>false</code>)}.
    */
   public Iterator getAll()
   {
      return getAll(false);
   }

   /**
    * Either get all fields that are only modifiable by the system or all
    * fields except those. The sets are unique, no field appears in both sets.
    *
    * @param systemModOnly flag to determine which set of fields to return.
    *
    * @return an iterator over all objects for this field set. Never
    * <code>null</code>, may be empty. Each object is either a <code>PSField
    * </code> or a <code>PSFieldSet</code>.
    */
   public Iterator getAll(boolean systemModOnly)
   {
      List<PSComponent> retList = new ArrayList<>();
      Iterator<PSComponent> i = m_fields.values().iterator();
      while (i.hasNext())
      {
         PSComponent c = i.next();
         if (c instanceof PSField)
         {
            if (isSystemModOnly((PSField)c, systemModOnly))
            {
               retList.add(c);
            }
         }
         else
         {
            retList.add(c);
         }
      }
      return retList.iterator();
   }

   /**
    * Get all field / field set objects stored in this field set, regardless of
    * the readOnly flag. This is a short cut to get both the read only and
    * regular fields /field sets.
    *
    * @return an iterator over all objects for this field set. Never
    *    <code>null</code>, may be empty.
    */
   public Iterator getEveryField()
   {
      return m_fields.values().iterator();
   }

   /**
    * Gathers all fields contained by this fieldset and all contained field sets
    * recursively.
    *
    * @return A valid array of the fields, never <code>null</code>.
    */
   public PSField[] getAllFields()
   {
      return getAllFields(false);
   }

   /**
    * Gathers all fields contained by this fieldset and all contained field sets
    * recursively.
    *
    * @param readOnly flag to determine whether to return just the readOnly
    *    fields or all the fields.
    *
    * @return A valid array of the fields, never <code>null</code>.
    */
   public PSField[] getAllFields(boolean readOnly)
   {
      Collection<PSField> fields = getAllFields(new ArrayList<>(),
            readOnly);
      PSField[] fieldArray = new PSField[fields.size()];
      return fields.toArray(fieldArray);
   }

   /**
    * Checks fields in this set and all contained field sets recursively and 
    * returns the one whose name matches the supplied name, case-insensitive. 
    * Since field names are unique within a content editor, the search stops 
    * once one is found.
    * 
    * @param fieldName The name of the field to retrieve. A case-insensitive 
    * compare is performed. If <code>null</code> or empty, <code>null</code>
    * is returned.
    * 
    * @return The <code>PSField</code> object, that has a name equal to 
    * <code>fieldName</code> (case insensitive). <code>null</code> if a match
    * isn't found.
    * 
    * @see #get(String)
    */
   public PSField getFieldByName(String fieldName)
   {
      if (null == fieldName || fieldName.trim().length() == 0)
      {
         return null;
      }
      PSField match = null;
      Iterator fields = getEveryField();
      while(fields.hasNext() && null == match)
      {
         Object o = fields.next();
         if (o instanceof PSFieldSet)
         {
            PSFieldSet childFieldSet = (PSFieldSet)o;
            match = childFieldSet.getFieldByName(fieldName);
         }
         else if (o instanceof PSField)
         {
            PSField f = (PSField)o;
            if (f.getSubmitName().equalsIgnoreCase(fieldName))
               match = f;
         }
      }
      return match;
   }

   /**
    * Either get all fields that are only modifiable by the system or all
    * fields except those.
    *
    * A utility method used to recursively collect all fields contained by
    * this fieldset and its child fieldsets.
    *
    * @param c The container for the results. The first call to this method
    *    should pass an empty one.
    *
    * @param systemModOnly flag to determine which set of fields to return.
    *
    * @return The passed collection w/ the fields added. Each member is a
    *    PSField. Never <code>null</code>.
    */
   private Collection<PSField> getAllFields(Collection<PSField> c, 
         boolean systemModOnly)
   {
      Iterator fields = getAll(systemModOnly);
      while(fields.hasNext())
      {
         Object o = fields.next();
         if (o instanceof PSFieldSet)
         {
            PSFieldSet childFieldSet = (PSFieldSet)o;
            childFieldSet.getAllFields(c, systemModOnly);
         }
         else if (o instanceof PSField)
         {
            PSField f = (PSField)o;
            if (isSystemModOnly(f, systemModOnly))
               c.add(f);
         }
      }
      return c;
   }

   /**
    * Before 5.0, all fields in the system def were used by the content
    * editors. Then we added many other fields in the content status table to
    * the system def, plus some read-only fields in support of searching. To
    * maintain compatibility, we wanted to return the same set of fields as we
    * did before this change. To do this, we added a flag param to the various
    * 'get' methods.
    *
    * @param f Assumed not <code>null</code>.
    *
    * @param systemModOnly The flag passed to the 'get' methods. If
    *    <code>false</code>, then we need to return the pre 5.0 fields,
    *    if <code>true</code>, we return all the newly added fields.
    *
    * @return <code>true</code> if systemModOnly is <code>true</code> and
    *    the supplied field should be included in returned list,
    *    <code>false</code> otherwise.
    */
   private boolean isSystemModOnly(PSField f, boolean systemModOnly)
   {
      return (!systemModOnly && 
            (f.isUserModified() || (!f.isSystemField() && f.isReadOnly())
                  || f.isSystemMandatory())
            || (systemModOnly && !f.isSystemMandatory() && 
                  (f.isSystemModified() || (f.isSystemField() && f.isReadOnly())
                        )));
   }

   /**
    * See {@link #setUserSearchable(boolean)} for details.
    * 
    * @return <code>true</code> means check the child's flag, <code>false
    * </code> means don't allow any child data to be indexed (and thus 
    * searched).
    */
   public boolean isUserSearchable()
   {
      return m_userSearchable;
   }

   /**
    * This flag determines whether all child fields and fieldsets are 
    * searchable. This value overrides individual field or complex field
    * settings except for simple children.
    * 
    * @param searchable <code>false</code> to disallow child fields from 
    * being searched. If <code>true</code>, then the field's setting is used.
    */
   public void setUserSearchable(boolean searchable)
   {
      m_userSearchable = searchable;
   }


   /**
    * Is sequencing supported.
    *
    * @return <code>true</code> if sequencing is supported,
    *    <code>false</code> otherwise.
    */
   public boolean isSequencingSupported()
   {
      return m_supportsSequencing;
   }

   /**
    * Set a new status of sequencing supported.
    *
    * @param supportsSequencing <code>true</code> to support sequencing,
    *    <code>false</code> otherwise.
    */
   public void setSequencingSupported(boolean supportsSequencing)
   {
      m_supportsSequencing = supportsSequencing;
   }

   /**
    * Performs a shallow copy of the data in the supplied component to this
    * component, also making a copy of the collection of fields and fieldsets.
    * Derived classes should implement this method for their data, calling the
    * base class method first.
    *
    * @param c a valid PSFieldSet, not <code>null</code>.
    */
   public void copyFrom(PSFieldSet c)
   {
      try
      {
         super.copyFrom(c);
      }
      catch (IllegalArgumentException e)
      {
         throw new IllegalArgumentException(e.getLocalizedMessage());
      }

      setName(c.getName());
      setType(c.getType());
      setSourceType(c.getSourceType());
      if(c.getCount() <= 0)
         setRepeatability(c.getRepeatability());
      else
         setCount(c.getCount());
      m_fields.clear();
      m_fields.putAll(c.m_fields);
      m_supportsSequencing = c.m_supportsSequencing;
      setUserSearchable((c.isUserSearchable()));
   }

   /**
    * Test if the provided object and this are equal.
    *
    * @param o the object to compare to.
    * @return <code>true</code> if this and o are equal,
    *    <code>false</code> otherwise.
    */
   @Override
   public boolean equals(Object o)
   {
      if (!(o instanceof PSFieldSet))
         return false;

      PSFieldSet t = (PSFieldSet) o;

      boolean equal = true;
      if (!compare(m_fields, t.m_fields))
         equal = false;
      else if( !equalSettings(t) )
         equal = false;

      return equal;
   }

   /**
    * Generates code of the object.
    */
   @Override
   public int hashCode()
   {
      return new HashCodeBuilder().append(m_name).append(m_count).toHashCode();
   }

   /**
    * Checks the settings of this field set are same as settings of
    * passed in component. Basically it differs from <code>equals()</code>
    * method by not checking the collection of it's fields.
    */
   public boolean equalSettings(PSFieldSet set)
   {
      boolean equal = true;
      if (!compare(m_name, set.m_name))
         equal = false;
      else if (m_repeatability != set.m_repeatability)
         equal = false;
      else if (m_repeatability == REPEATABILITY_COUNT && m_count != set.m_count)
         equal = false;
      else if (m_supportsSequencing != set.m_supportsSequencing)
         equal = false;
      else if (isUserSearchable() != set.isUserSearchable())
         equal = false;
      else if (m_type != set.m_type)
         equal = false;

      return equal;
   }

   /**
    *
    * @see IPSComponent
    */
   public void fromXml(Element sourceNode, IPSDocument parentDoc,
                       ArrayList parentComponents)
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

      String data = null;
      Element node = null;
      try
      {
         PSXmlTreeWalker tree = new PSXmlTreeWalker(sourceNode);

         // REQUIRED: get the name
         m_name = tree.getElementData(NAME_ATTR);
         if (m_name == null)
         {
            Object[] args =
            {
               XML_NODE_NAME,
               NAME_ATTR,
               "null"
            };
            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
         }

         // OPTIONAL: get the type attribute
         data = tree.getElementData(TYPE_ATTR);
         if (data != null)
         {
            boolean found = false;
            for (int i=0; i<TYPE_ENUM.length; i++)
            {
               if (TYPE_ENUM[i].equalsIgnoreCase(data))
               {
                  m_type = i;
                  found = true;
                  break;
               }
            }
            if (!found)
            {
               Object[] args =
               {
                  XML_NODE_NAME,
                  TYPE_ATTR,
                  data
               };
               throw new PSUnknownNodeTypeException(
                  IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
            }
         }

         // OPTIONAL: get the field repeatability attribute
         data = tree.getElementData(REPEATABILITY_ATTR);
         if (data != null)
         {
            boolean found = false;
            for (int i=0; i<REPEATABILITY_ENUM.length; i++)
            {
               if (REPEATABILITY_ENUM[i].equalsIgnoreCase(data))
               {
                  m_repeatability = i;
                  found = true;
                  break;
               }
            }
            if (!found)
            {
               Object[] args =
               {
                  XML_NODE_NAME,
                  REPEATABILITY_ATTR,
                  data
               };
               throw new PSUnknownNodeTypeException(
                  IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
            }
         }

         if (m_repeatability == REPEATABILITY_COUNT)
         {
            // REQUIRED: if repeatability is set to REPEATABILITY_COUNT
            data = tree.getElementData(COUNT_ATTR);
            if (data == null)
            {
               Object[] args =
               {
                  XML_NODE_NAME,
                  COUNT_ATTR,
                  "null"
               };
               throw new PSUnknownNodeTypeException(
                  IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
            }
            try
            {
               m_count = Integer.parseInt(data);
            }
            catch (NumberFormatException e)
            {
               Object[] args =
               {
                  XML_NODE_NAME,
                  COUNT_ATTR,
                  e.toString()
               };
               throw new PSUnknownNodeTypeException(
                  IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
            }
            if (m_count <= 0)
            {
               Object[] args =
               {
                  XML_NODE_NAME,
                  COUNT_ATTR,
                  Integer.toString(m_count)
               };
               throw new PSUnknownNodeTypeException(
                  IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
            }
         }

         // OPTIONAL: get the field supports sequencing attribute
         data = tree.getElementData(SUPPORTS_SEQUENCING_ATTRIBUTE);
         if (data != null)
            m_supportsSequencing =
               data.equalsIgnoreCase(BOOLEAN_ENUM[0]) ? true : false;

         m_userSearchable = PSXMLDomUtil.checkAttributeBool(sourceNode, 
               USER_SEARCHABLE_ATTR);

         // OPTIONAL: the fields
         node = tree.getNextElement(firstFlags);
         while(node != null)
         {
            String elementName = node.getTagName();

            if(elementName.equals(XML_NODE_NAME))
               add(new PSFieldSet(node, parentDoc, parentComponents));
            else if(elementName.equals(PSField.XML_NODE_NAME))
               add(new PSField(node, parentDoc, parentComponents));

            node = tree.getNextElement(nextFlags);
         }
      }
      finally
      {
         resetParentList(parentComponents, parentSize);
      }
   }

   /**
    * Recursively fixes the source type (local, shared or system) to all fields
    * and fieldsets using the supplied information.
    * <br>
    * Steps for identification.
    * <ol>
    * <li>A field is considered as system if a field with same name exists in
    * system definition and is not in the list of system field excludes</li>
    * <li>A field is considered as shared if a field with same name exists in
    * any of the shared field groups and is not in the list of shared field
    * excludes</li>
    * <li>A fieldset is considered as shared if a fieldset with same name exists
    * in any of the shared field groups and that group is in the list of
    * included shared groups list</li>
    * </ol>
    *
    * @param systemDef the content editor system definition, may not be <code>null
    * </code>
    * @param sharedDef the content editor shared definition, may not be <code>
    * null</code>
    * @param sysFieldExcludes the list of system field excludes, may not be
    * <code>null</code>, may be empty.
    * @param sharedGroupIncludes the list of shared group includes, may not be
    * <code>null</code>, may be empty.
    * @param sharedFieldExcludes the list of shared field excludes, may not be
    * <code>null</code>, may be empty.
    */
   public void fixSourceTypes(PSContentEditorSystemDef systemDef,
      PSContentEditorSharedDef sharedDef, List sysFieldExcludes,
      List sharedGroupIncludes, List sharedFieldExcludes)
   {
      if(systemDef == null)
         throw new IllegalArgumentException("systemDef may not be null");
      if(sharedDef == null)
         throw new IllegalArgumentException("sharedDef may not be null.");
      if(sysFieldExcludes == null)
         throw new IllegalArgumentException(
            "sysFieldExcludes may not be null.");
      if(sharedGroupIncludes == null)
         throw new IllegalArgumentException(
            "sharedGroupIncludes may not be null.");
      if(sharedFieldExcludes == null)
         throw new IllegalArgumentException(
            "sharedFieldExcludes may not be null.");

      if(sharedDef.isSharedFieldSet(this, sharedGroupIncludes))
      {
         setSourceType( PSField.TYPE_SHARED );
      }
      else
      {
         m_sourceType = PSField.TYPE_LOCAL;

         Iterator fields = getEveryField();
         while (fields.hasNext())
         {
            Object test = fields.next();
            if (test instanceof PSField)
            {
               PSField field = (PSField) test;
               if (field.getType() != PSField.TYPE_UNKNOWN)
                  continue;

               if (systemDef.isSystemField(field, sysFieldExcludes))
                  field.setType(PSField.TYPE_SYSTEM);
               else if (sharedDef.isSharedField(field, sharedGroupIncludes,
                  sharedFieldExcludes))
               {
                  field.setType(PSField.TYPE_SHARED);
               }
               else
                  field.setType(PSField.TYPE_LOCAL);
            }
            else if (test instanceof PSFieldSet)
            {
               ((PSFieldSet) test).fixSourceTypes(
                  systemDef, sharedDef, sysFieldExcludes,
                  sharedGroupIncludes, sharedFieldExcludes);
            }
         }
      }
   }

   /**
    * Checks whether this is a shared field set by looking its source type.
    *
    * @return <code>true</code> if it is shared field set, otherwise
    * <code>false</code>
    */
   public boolean isSharedFieldSet()
   {
      return getSourceType() == PSField.TYPE_SHARED;
   }
   
   /**
    * Tests if this field set contains system mandatory fields.
    * 
    * @return <code>true</code> if it has, <code>false</code> otherwise.
    */
   public boolean hasMandatorySystemFields()
   {
      Iterator fields = getAll();
      while (fields.hasNext())
      {
         Object test = fields.next();
         if (test instanceof PSField)
         {
            PSField field = (PSField) test;
            if (field.isSystemMandatory())
               return true;
         }
      }
      
      return false;
   }

   /**
    *
    * @see IPSComponent
    */
   public Element toXml(Document doc)
   {
      // create root and its attributes
      Element root = doc.createElement(XML_NODE_NAME);
      root.setAttribute(NAME_ATTR, m_name);
      root.setAttribute(TYPE_ATTR, TYPE_ENUM[m_type]);
      root.setAttribute(REPEATABILITY_ATTR, REPEATABILITY_ENUM[m_repeatability]);
      if (m_repeatability == REPEATABILITY_COUNT)
         root.setAttribute(COUNT_ATTR, Integer.toString(m_count));

      /* when writing back to an old server, we add this attribute anyway 
       * because the old code will ignore it
       */
      root.setAttribute(SUPPORTS_SEQUENCING_ATTRIBUTE,
            BOOLEAN_ENUM[m_supportsSequencing ? 0 : 1]);
      root.setAttribute(USER_SEARCHABLE_ATTR, 
            BOOLEAN_ENUM[m_userSearchable ? 0 : 1]);

      /*
       * TODO: the field set contains other field sets  Stop the toXml if we
       * already processed the field set (keep a map of field sets processed).
       */

      // create fields
      Iterator it = getEveryField();
      while (it.hasNext())
         root.appendChild(((IPSComponent) it.next()).toXml(doc));

      return root;
   }

   // see IPSComponent
   @Override
   public void validate(IPSValidationContext context)
      throws PSSystemValidationException
   {
      if (!context.startValidation(this, null))
         return;

      if (!isValidType(m_type))
      {
         Object[] args = { TYPE_ENUM };
         context.validationError(this,
            IPSObjectStoreErrors.UNSUPPORTED_FIELD_SET_TYPE, args);
      }

      if (m_repeatability != REPEATABILITY_ONE_OR_MORE &&
          m_repeatability != REPEATABILITY_ZERO_OR_MORE &&
          m_repeatability != REPEATABILITY_COUNT)
      {
         Object[] args = { REPEATABILITY_ENUM };
         context.validationError(this,
            IPSObjectStoreErrors.UNSUPPORTED_FIELD_SET_REPEATABILITY, args);
      }

      if (m_name == null || m_name.trim().length() == 0)
      {
         context.validationError(this,
            IPSObjectStoreErrors.INVALID_FIELD_SET_NAME, null);
      }

      // do children
      context.pushParent(this);
      try
      {
         Iterator it = getAll();
         while (it.hasNext())
            ((IPSComponent) it.next()).validate(context);
      }
      finally
      {
         context.popParent();
      }
   }

   /**
    * Checks the supplied type to make sure it is one of the allowed types.
    *
    * @param type The type to check.  Must be one of the constants of the form
    * TYPE_xxx.
    *
    * @return <code>true</code> if type is valid, <code>false</code> if not.
    */
   public static boolean isValidType(int type)
   {
      boolean result = false;

      if (type == TYPE_PARENT ||
          type == TYPE_SIMPLE_CHILD ||
          type == TYPE_MULTI_PROPERTY_SIMPLE_CHILD ||
          type == TYPE_COMPLEX_CHILD)
      {
         result = true;
      }

      return result;
   }


   /**
    * Gets a shallow copy of this fieldset recursively merged with the source.
    * If a field in the source exists in this fieldset (overridden field), then
    * the field is merged with source field keeping its non-<code>null</code>
    * properties, otherwise adds that field to merged field set. See {@link
    * PSField#merge(PSField) merge} for more information.
    *
    * @param source The source field set to merge with, may not be <code>null
    * </code>
    *
    * @return the merged field set with the added and merged source fields,
    * never <code>null</code>
    *
    * @throws PSSystemValidationException if a fieldname in the source already exists
    * in this fieldset but the objects are of different types (i.e. one is a
    * PSField object and one is a PSFieldSet object).
    */
   public PSFieldSet merge(PSFieldSet source)
      throws PSSystemValidationException
   {
      if (source == null)
         throw new IllegalArgumentException("source may not be null");

      PSFieldSet mergedSet = new PSFieldSet(this);

      // walk source and add it's contents to the target
      Iterator i = source.getNames();
      while (i.hasNext())
      {
         String name = (String)i.next();
         Object o = source.get(name);

         boolean hasMatch = false;
         if (contains(name))
         {
            if (get(name).getClass().isInstance(o) ||
                o.getClass().isInstance(get(name)))
               hasMatch = true;
            else
            {
               // throw error
               throw new PSSystemValidationException(
                  IPSObjectStoreErrors.CE_DUPLICATE_MERGED_FIELD_NAME, name );
            }
         }

         if (o instanceof PSFieldSet)
         {
            PSFieldSet childSourceSet = (PSFieldSet)o;
            //If match is found, recursively merge the child sets and add it to
            //target, otherwise add the source child field set.
            if(hasMatch)
            {
               PSFieldSet targetSet = (PSFieldSet)get(name);
               childSourceSet = targetSet.merge( childSourceSet );
            }
            mergedSet.add(childSourceSet);
         }
         else
         {
            PSField field = (PSField)o;
            //If match is found merge and add to the target, otherwise add the
            //source field.
            if (hasMatch)
            {
               PSField target = (PSField)get(name);
               field = target.merge(field);
            }
            mergedSet.add(field);
         }
      }

      return mergedSet;
   }

   /**
    * Gets a shallow copy of this fieldset recursively demerged from source.
    * If a field in the source exists in this fieldset, then the field is
    * demerged from the source field keeping the properties that differ from
    * source field. If the demerged field does not have any overridden
    * properties, then that field is not added to the demerged fieldset.
    *
    * @param source the source fieldset to demerge from, may not be <code>null
    * </code>
    *
    * @return the demerged fieldset, never <code>null</code>
    *
    * @throws PSSystemValidationException if a fieldname in the source already exists
    * in this fieldset but the objects are of different types (i.e. one is a
    * PSField object and one is a PSFieldSet object).
    */
   public PSFieldSet demerge(PSFieldSet source)
      throws PSSystemValidationException
   {
      if(source == null)
         throw new IllegalArgumentException("source may not be null");

      PSFieldSet demergedSet = new PSFieldSet(this);

      //walk the set from which we want to demerge and demerge
      //the field/fieldset when we find match
      Iterator i = source.getNames();
      while (i.hasNext())
      {
         String name = (String)i.next();
         Object o =  source.get(name);

         boolean hasMatch = false;
         if (contains(name))
         {
            if (get(name).getClass().isInstance(o) ||
                o.getClass().isInstance(get(name)))
               hasMatch = true;
            else
            {
               // throw error
               throw new PSSystemValidationException(
                  IPSObjectStoreErrors.CE_DUPLICATE_MERGED_FIELD_NAME, name );
            }
         }

         if(hasMatch)
         {
            //recursively demerge
            if (o instanceof PSFieldSet)
            {
               PSFieldSet childSource = (PSFieldSet)o;

               PSFieldSet demergedChildSet = (PSFieldSet)get(name);
               demergedChildSet = demergedChildSet.demerge( childSource );

               //If the fieldset does not have any fields remove that fieldset,
               //otherwise update the demerged fieldset with demerged child set.
               if(demergedChildSet.getAll().hasNext() ||
                  !demergedChildSet.equalSettings(childSource))
                  demergedSet.add(demergedChildSet);
               else
                  demergedSet.remove(demergedChildSet.getName());
            }
            else //demerge the field
            {
               PSField fromField = (PSField)o;

               PSField demergedField = (PSField)get(name);
               demergedField = demergedField.demerge(fromField);
               
               //If the demerged field does not have any overridden properties
               //remove that field, otherwise update the demerged field.
               if(demergedField == null)
               {
                  demergedSet.remove(name);
               }
               else
                  demergedSet.add(demergedField);
            }
         }

      }
      return demergedSet;
   }

   /**
    * Gets a copy of this fieldset with the fields that are in the supplied
    * exclude list removed.
    *
    * @param fieldExcludes The list of field reference aliases as <code>String
    * </code>s to remove from this fieldset, may not be <code>null</code>
    * @param validate If <code>true</code>, validates that the excluded fields
    * exist in the fieldset.
    *
    * @return A copy of the fieldset with the exculded fields removed.  Never
    * <code>null</code>.
    *
    * @throws PSSystemValidationException if validating and an excluded field is not
    * found in the fieldset.
    */
   public PSFieldSet removeFields(List fieldExcludes, boolean validate)
         throws PSSystemValidationException
   {
      if (fieldExcludes == null)
         throw new IllegalArgumentException("fieldExcludes may not be null");

      // if validating be sure all excludes actually exist in the field set
      if (validate)
      {
         Iterator excludes = fieldExcludes.iterator();
         while (excludes.hasNext())
         {
            String exclude = (String)excludes.next();
            if (!contains(exclude))
            {
               Object[] args = {exclude, getName()};
               throw new PSSystemValidationException(
                  IPSObjectStoreErrors.CE_EXCLUDED_FIELD_MISSING, args);
            }
         }
      }

      // first make a copy and then remove all excluded fields
      PSFieldSet resultFields = new PSFieldSet(this);
      Iterator fields = getNames();
      while (fields.hasNext())
      {
         String name = (String)fields.next();
         if (fieldExcludes.contains(name))
            resultFields.remove(name);
      }

      return resultFields;
   }



   /**
    * Walks this fieldset recursively, looking for all instances of
    * PSBackEndColumn and replaces the table in that object with the fully
    * specified table in the supplied tables list. This is necessary because
    * the tables in this context just have an alias, all other properties
    * are <code>null</code>.
    *
    * @param tables A list of all tables defined in this editor. Each key
    *    is the table alias (lowercased) and each value is the PSBackEndTable
    *    that has all properties properly specified. The Map is treated
    *    read-only.
    *
    * @throws PSSystemValidationException if there is a table ref in the fieldset
    *    that doesn't exist in the supplied map.
    *
    * @todo PSApplicationBuilder and PSCopyHandler need to be cleaned up to
    *    take advantage of this method.
    */
   public void fixupBackEndColumns(Map tables)
      throws PSSystemValidationException
   {
      try
      {
         Iterator iter = getEveryField();
         while ( iter.hasNext())
         {
            Object o = iter.next();
            if ( o instanceof PSFieldSet )
               ((PSFieldSet) o).fixupBackEndColumns(tables);
            else
            {
               PSField field = (PSField) o;
               if ( field.getLocator() instanceof PSBackEndColumn )
               {
                  PSBackEndColumn col = (PSBackEndColumn) field.getLocator();
                  PSBackEndTable table = (PSBackEndTable) tables.get(
                        col.getTable().getAlias().toLowerCase());
                  if ( null == table )
                  {
                     throw new PSSystemValidationException(
                           IPSObjectStoreErrors.BE_TABLE_NULL);
                  }
                  col.setTable( table );
               }
            }
         }
      }
      catch ( IllegalArgumentException iae )
      {
         throw new IllegalArgumentException( iae.getLocalizedMessage());
      }
   }

   /** the XML node name */
   public static final String XML_NODE_NAME = "PSXFieldSet";

   /** Child type specifier. */
   public static final int TYPE_PARENT = 0;

   /**
    * Simple child type specifier. Specifies a fieldSet that may result in
    * multiple child table rows containing only one column, and the data is
    * edited at the same time as the parent item data.
    */
   public static final int TYPE_SIMPLE_CHILD = 1;

   /**
    * Complex child type specifier.  Specifies a fieldSet that may result in
    * multiple child table rows containing multple column values.  The data is
    * edited separately from the parent item data.
    */
   public static final int TYPE_COMPLEX_CHILD = 2;

   /**
    * Multi-property simple child type specifier.  Specifies a fieldSet that
    * may result in a single child table row containing multiple column values.
    * Data is edited along with the parent item data.
    */
   public static final int TYPE_MULTI_PROPERTY_SIMPLE_CHILD = 3;

   /**
    * An array of XML attribute values for the type. They are
    * specified at the index of the specifier.
    */
   private static final String[] TYPE_ENUM =
   {
      "parent", "simpleChild", "complexChild", "multiPropertySimpleChild"
   };

   /** ZeroOrMore type specifier. */
   public static final int REPEATABILITY_ZERO_OR_MORE = 0;

   /** OneOrMore type specifier. */
   public static final int REPEATABILITY_ONE_OR_MORE = 1;

   /** Count type specifier */
   public static final int REPEATABILITY_COUNT = 2;

   /**
    * An array of XML attribute values for the repeatability. They are
    * specified at the index of the specifier.
    */
   private static final String[] REPEATABILITY_ENUM =
   {
      "zeroOrMore", "oneOrMore", "count"
   };

   /** the name for the type attribute */
   public static final String SUPPORTS_SEQUENCING_ATTRIBUTE = "supportsSequencing";
   
   private static final String USER_SEARCHABLE_ATTR = "userSearchable";

   /**
    * An array of XML attribute values for all boolean attributes. They are
    * ordered as <code>true</code>, <code>false</code>.
    */
   private static final String[] BOOLEAN_ENUM =
   {
      "yes", "no"
   };

   /** The field set name, might be <code>null</code> */
   private String m_name = null;
   
   /**
    * See {@link #setUserSearchable(boolean)} for details. Defaults to <code>true</code>.
    */
   private boolean m_userSearchable = true;

   /** The field type. */
   private int m_type = TYPE_PARENT;

   /** The repeatability setting */
   private int m_repeatability = REPEATABILITY_ZERO_OR_MORE;

   /**
    * The number of rows needed. This is only used if the repeatability type
    * is REPEATABILITY_COUNT.
    */
   private int m_count = 0;

   /** The status whether or not sequencing is supported */
   private boolean m_supportsSequencing = true;

   /**
    * An ordered map storing PSField and/or PSFieldSet objects, ordered and keyed
    * by their name. Never <code>null</code>, might be empty.
    */
   private Map<String, PSComponent> m_fields = new TreeMap<>();
   
   /** The fieldset source type. */
   private int m_sourceType = PSField.TYPE_LOCAL;

   /*
    * The following strings define all elements/attributes used to create the
    * XML output for this object. No Java documentation will be added to this.
    */
   private static final String NAME_ATTR = "name";
   private static final String TYPE_ATTR = "type";
   private static final String REPEATABILITY_ATTR = "repeatability";
   private static final String COUNT_ATTR = "count";
}

