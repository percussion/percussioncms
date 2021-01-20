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
package com.percussion.deployer;

/**
 * The class to represent a source or target element in the element(id) mapping.
 * Can be used as a user object for table cell or elements in the list. It 
 * renderers the element as 'Name(id)-ParentName(id)' if it has a parent, 
 * otherwise 'Name(id)'.
 */
public class PSMappingElement implements Comparable
{
   /**
    * Constructs the mapping element with supplied parameters.
    * 
    * @param type the element type, may not be <code>null</code> or empty.
    * @param id the element id, may not be <code>null</code> or empty.
    * @param name the element name,  may not be <code>null</code> or empty.
    * 
    * @throws IllegalArgumentException if any parameter is invalid.
    */
   public PSMappingElement(String type, String id, String name)
   {
      if(type == null || type.trim().length() == 0)
         throw new IllegalArgumentException("type may not be null or empty.");         
      if(id == null || id.trim().length() == 0)
         throw new IllegalArgumentException("id may not be null or empty.");
      if(name == null || name.trim().length() == 0)
         throw new IllegalArgumentException("name may not be null or empty.");         

      m_type = type;
      m_id = id;
      m_name = name;                  
   }
   
   /**
    * Convenience constructor for derived classes to show an empty element in 
    * the combo-box. The derived classes using this constructor should override
    * all or required methods (at least <code>equals()</code> and <code>
    * toString()</code> methods) to fit its needs, otherwise using the 
    * base-class methods results in <code>java.lang.NullPointerException</code>s.
    */
   protected PSMappingElement()
   {
   }
   
   /**
    * Sets the parent element of this element.
    * 
    * @param type the parent type, may not be <code>null</code> or empty.
    * @param id the parent id, may not be <code>null</code> or empty.
    * @param name the parent name,  may not be <code>null</code> or empty.
    * 
    * @throws IllegalArgumentException if any parameter is invalid.
    */
   public void setParent(String type, String id, String name)
   {
      if(type == null || type.trim().length() == 0)
         throw new IllegalArgumentException("type may not be null or empty.");         
      if(id == null || id.trim().length() == 0)
         throw new IllegalArgumentException("id may not be null or empty.");
      if(name == null || name.trim().length() == 0)
         throw new IllegalArgumentException("name may not be null or empty.");         
         
      m_parentType = type;
      m_parentId = id;
      m_parentName = name;
   }

   /**
    * Finds whether this element has parent or not.
    * 
    * @return <code>true</code> if it has parent (if {@link 
    * #setParent(String, String, String) is called), otherwise <code>
    * false</code>
    */   
   public boolean hasParent()
   {
      return m_parentId != null;
   }

   /**
    * Gets the id of this element.
    * 
    * @return the id, never <code>null</code> or empty.
    */   
   public String getId()
   {
      return m_id;
   }
   
   /**
    * Gets the name of this element.
    * 
    * @return the name, never <code>null</code> or empty.
    */ 
   public String getName()
   {
      return m_name;
   }
   
   /**
    * Gets the type of this element.
    * 
    * @return the type, never <code>null</code> or empty.
    */    
   public String getType()
   {
      return m_type;
   }
   
   /**
    * Gets the parent id of this element.
    * 
    * @return the parent id, may be <code>null</code> if {@link 
    * #setParent(String, String, String) is not called.
    */
   public String getParentId()
   {
      return m_parentId;
   }  
   
   /**
    * Gets the parent name of this element.
    * 
    * @return the parent name, may be <code>null</code> if {@link 
    * #setParent(String, String, String) is not called.
    */
   public String getParentName()
   {
      return m_parentName;
   }   
   
   /**
    * Gets the parent type of this element.
    * 
    * @return the parent type, may be <code>null</code> if {@link 
    * #setParent(String, String, String) is not called.
    */
   public String getParentType()
   {
      return m_parentType;
   }   
   
   /**
    * Gets the string representation of this element's parent in the form 
    * 'Name{id)' if it has a parent.
    * 
    * @return the string, may be <code>null</code> if it does not have a parent,
    * never empty.
    */
   public String getParentDisplayString()
   {
      if(hasParent())
         return  m_parentName + "(" + m_parentId + ")";
      else
         return null;
   }

   /**
    * Gets the string representation of this element as 
    * 'Name(id)-ParentName(id)' if it has a parent, otherwise as 'Name(id)'.
    * 
    * @return the string, never <code>null</code> or empty.
    */
   public String toString()
   {      
      String displayString = m_name + "(" + m_id + ")";
      if(hasParent())
      {
         displayString += "-" + m_parentName + "(" + m_parentId + ")";
      }
      
      return displayString;
   }

   //overrides the equals method   
   public boolean equals(Object object)
   {
      boolean isEqual = true;
      if(object instanceof PSMappingElement)
      {
         PSMappingElement element = (PSMappingElement)object;
         if(!getId().equals(element.getId()) )
            isEqual = false;
         else if(!getName().equals(element.getName()))
            isEqual = false;
         else if(!getType().equals(element.getType()))
            isEqual = false;
         else if(hasParent() && !element.hasParent())
            isEqual = false;
         else if(!hasParent() && element.hasParent())
            isEqual = false;
         else if(hasParent())
         {
            if(!m_parentId.equals(element.m_parentId))
               isEqual = false;
            else if(!m_parentName.equals(element.m_parentName))
               isEqual = false;
            else if(!m_parentType.equals(element.m_parentType))
               isEqual = false;
         }
      }
      else
         isEqual = false;
         
      return isEqual;
   }

   /**
    * Generates hash code corresponding to {@link #equals(Object)}.
    */
   @Override
   public int hashCode()
   {
      int hashCode =
            hashCodeOr0(getId())
            + hashCodeOr0(getName())
            + hashCodeOr0(getType())
            + Boolean.valueOf(hasParent()).hashCode();
      if (hasParent())
      {
         hashCode +=
            hashCodeOr0(m_parentId)
            + hashCodeOr0(m_parentName)
            + hashCodeOr0(m_parentType);
      }
      return hashCode;
   }

   /**
    * Hash code of the provided object or 0 if the object is <code>null</code>.
    */
   private int hashCodeOr0(Object object)
   {
      return object == null ? 0 : object.hashCode();
   }

   /**
    * Compare's this object <code>toString()</code> representation 
    * lexicographically ignoring case. See {@link 
    * java.lang.Comparable#compareTo(Object) } for more information.
    * 
    * @throws IllegalArgumentException if obj is <code>null</code>
    * @throws ClassCastException if obj is not an instance of <code>
    * PSMappingElement</code>
    */
   public int compareTo(Object obj)
   {
      if(obj == null)
         throw new IllegalArgumentException("obj may not be null.");
         
      PSMappingElement element = (PSMappingElement)obj;
      return toString().compareToIgnoreCase(element.toString());
   }

   /**
    * The element id, initialized in the constructor and never <code>null</code>
    * or empty or modified after that.
    */
   private String m_id = null;
   
   /**
    * The element name, initialized in the constructor and never <code>null
    * </code> or empty or modified after that.
    */
   private String m_name;
   
   /**
    * The element type, initialized in the constructor and never <code>null
    * </code> or empty or modified after that.
    */
   private String m_type;   
   
   /**
    * The element's parent id, may be <code>null</code> if {@link 
    * #setParent(String, String, String) is not called and modified through the
    * same call.
    */
   private String m_parentId = null;
   
   /**
    * The element's parent name, may be <code>null</code> if {@link 
    * #setParent(String, String, String) is not called and modified through the
    * same call.
    */
   private String m_parentName = null;
   
   /**
    * The element's parent type, may be <code>null</code> if {@link 
    * #setParent(String, String, String) is not called and modified through the
    * same call.
    */
   private String m_parentType = null;
}
