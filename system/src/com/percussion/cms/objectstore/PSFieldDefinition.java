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

import java.util.ArrayList;
import java.util.List;

import com.percussion.design.objectstore.PSDependency;
import com.percussion.design.objectstore.PSDisplayMapping;
import com.percussion.design.objectstore.PSField;
import com.percussion.design.objectstore.PSFieldSet;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.xml.PSXmlDocumentBuilder;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
/**
 * Convenient class to hold the field/fieldset and its dispalymapping together.
 * This is useful for the fields and fieldsets that have the displaymapping.
 * The object this class holds will be either PSField or PSFieldSet
 */
public class PSFieldDefinition
{
   /**
    * Constructs an object of this class, sets the PSFieldSet object as
    * <code>null</code>.
    * @param field, object of PSField, must not be <code>null</code>.
    * @param mapping, the mapping corresponding to the supplied field
    *   must not be <code>null</code>.
    */
   public PSFieldDefinition(PSField field, PSDisplayMapping mapping)
   {
      if (field == null)
      {
         throw new IllegalArgumentException("field must not be null");
      }
      if (mapping == null)
      {
         throw new IllegalArgumentException("mapping must not be null");
      }
      if(!mapping.getFieldRef().equals(field.getSubmitName()))
      {
         throw new IllegalArgumentException("supplied mapper does not correspond to the field.");
      }
      m_field = field;
      m_mapping = mapping;
      m_fieldSet = null;
   }

   /**
    * Constructs an object of this class, sets the PSField object as
    * <code>null</code>.
    * @param fieldSet Object of PSFieldSet, sets the PSFeild object as <code>null</code>.
    * @param mapping, the mapping corresponding to the supplied fieldset
    *   must not be <code>null</code>.
    */
   public PSFieldDefinition(PSFieldSet fieldSet, PSDisplayMapping mapping)
   {
      if (fieldSet == null)
      {
         throw new IllegalArgumentException("fieldSet must not be null");
      }
      if (mapping == null)
      {
         throw new IllegalArgumentException("mapping must not be null");
      }
      if(!mapping.getFieldRef().equals(fieldSet.getName()))
      {
         throw new IllegalArgumentException("supplied mapper does not correspond to the field.");
      }
      m_field = null;
      m_mapping = mapping;
      m_fieldSet = fieldSet;
   }
   
   /**
    * Gets the PSDisplayMapping object.
    * @return the PSDisplayMapping object  never <code>null</code>.
    */
   public PSDisplayMapping getMapping()
   {
      return m_mapping;
   }
   
   /**
    * Gets the PSField object. Call isFieldSet method before calling method,
    * to determine whether the object represents a field or field set.
    * @return the PSField object, may be <code>null</code>.
    */
   public PSField getField()
   {
      return m_field;
   }

   /**
    * Gets the PSFieldSet object. Call isFieldSet method before calling method,
    * to determine whether the object represents a field or field set.
    */
   public PSFieldSet getFieldset()
   {
      return m_fieldSet;
   }
   
   /**
    * Gets the list of control dependencies set by {@link setCtrlDependencies}.
    * returns <code>null</code>if not set.
    * @return List of control dependencies, may be <code>null</code>.
    */
   public List<PSDependency> getCtrlDependencies()
   {
      return m_ctrlDependencies;
   }

   /**
    * Sets the list of control dependencies.
    * @param dependencies List of PSDependency objects.
    */
   public void setCtrlDependencies(List<PSDependency> dependencies)
   {
      m_ctrlDependencies = dependencies;
   }
   
   /**
    * @return <code>true</code>, if the PSFieldSet object is not <code>null</code>
    * otherwise <code>false</code>.
    */
   public boolean isFieldSet()
   {
      return m_fieldSet!=null?true:false;
   }
   
   /**
    * Creates a deepcopy of this object by calling appropriate toXML methods.
    * @param fd PSFieldDefinition Object, must not be <code>null</code>.
    * @return deep copied PSFieldDefinition object.
    */
   public static PSFieldDefinition deepCopy(PSFieldDefinition fd)
   {
      if(fd == null)
         throw new IllegalArgumentException("fd must not be null");
      PSFieldDefinition fdCopy = null;
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element root = fd.getMapping().toXml(doc);
      PSDisplayMapping mapping = null;
      PSField fld = null;
      PSFieldSet fldset = null;
      try
      {
         mapping = new PSDisplayMapping(root,null,null);
      }
      catch (PSUnknownNodeTypeException e)
      {
         //We should not come here as we are constructing from xml which is
         //created by a valid field's toXml()
         throw new RuntimeException(e.getMessage());
      }
      if(fd.isFieldSet())
      {
         root = fd.getFieldset().toXml(doc);
         try
         {
            fldset = new PSFieldSet(root,null,null);
         }
         catch (PSUnknownNodeTypeException e)
         {
            //We should not come here as we are constructing from xml which is
            //created by a valid field's toXml()
            throw new RuntimeException(e.getMessage());
         }
         fdCopy = new PSFieldDefinition(fldset,mapping);
      }
      else
      {
         PSField sourceField = fd.getField();
         root = sourceField.toXml(doc);
         try
         {
            fld = new PSField(root,null,null);
            // Copy user properties which are not available via to/from xml
            for(String key : sourceField.getUserPropertyKeys())
            {
               fld.setUserProperty(key, sourceField.getUserProperty(key));
            }
         }
         catch (PSUnknownNodeTypeException e)
         {
            //We should not come here as we are constructing from xml which is
            //created by a valid field's toXml()
            throw new RuntimeException(e.getMessage());
         }
         fdCopy = new PSFieldDefinition(fld,mapping);
      }
      if(fd.getCtrlDependencies() != null)
      {
         List<PSDependency> depsCopy = new ArrayList<>();
         for(PSDependency dep:fd.getCtrlDependencies())
         {
            PSDependency depCopy = (PSDependency)dep.clone();
            depsCopy.add(depCopy);
         }
         fdCopy.setCtrlDependencies(depsCopy);
      }
      return fdCopy;
   }
   
   /**
    * PSField object, will be null if the object of this class represents
    * PSFieldSet
    */
   private PSField m_field;
   
   /**
    * PSDisplayMapping object corresponding to the field or fieldset of
    * this object.
    */
   private PSDisplayMapping m_mapping;
   
   /**
    * PSFieldSet object, will be null if the object of this class represents
    * PSField.
    */
   private PSFieldSet m_fieldSet;
   
   private List<PSDependency> m_ctrlDependencies;

}
