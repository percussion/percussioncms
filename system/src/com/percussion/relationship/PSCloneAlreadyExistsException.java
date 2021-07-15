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
package com.percussion.relationship;

import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSRelationship;

/**
 * Runtime exception that is thrown when someone requests to create a clone of 
 * an item based on a relationship type and the same clone was created 
 * earlier and still exists in the system. 
 * @author RammohanVangapalli
 */
public class PSCloneAlreadyExistsException extends RuntimeException
{
   /**
    * Constructor that takes the existing relationships owner, dependent and 
    * relationship name that canb be accessible later when geenrating the 
    * message.
    * @param owner owner of the existing relationship, must not be 
    * <code>null</code>.
    * @param dependent dependent of the existing relationship, must not be 
    * <code>null</code>.
    * @param relType name of the relationship, must not be <code>null</code> 
    * or empty. 
    */
   public PSCloneAlreadyExistsException(
      PSLocator owner,
      PSLocator dependent,
      String relType)
   {
      super();
      m_owner = owner;
      m_dependent= dependent;
      m_relType = relType;
   }

   /**
    * Constructor that takes the existing relationship that can be 
    * accessible later when geenrating the message.
    * @param relationship must not be <code>null</code>. 
    */
   public PSCloneAlreadyExistsException(PSRelationship relationship)
   {
      super();
      m_relationship = relationship;
   }

   /**
    * Delegate to appropriate base class ctor
    */
   public PSCloneAlreadyExistsException()
   {
      super();
   }

   /**
    * Delegate to appropriate base class ctor
    * @param s
    */
   public PSCloneAlreadyExistsException(String s)
   {
      super(s);
   }

   /**
    * Access method for the relationship type name.
    * @return name of the existing relationship, never <code>null</code>.
    */
   public String getRelationshipType()
   {
      if(m_relationship != null)
         return m_relationship.getConfig().getName();
      return m_relType;
   }

   /**
    * Access method for the owner.
    * @return owner of the existing relationship, never <code>null</code>.
    */
   public PSLocator getOwner()
   {
      if(m_relationship != null)
         return m_relationship.getOwner();
      return m_owner;
   }

   /**
    * Access method for the dependent.
    * @return dependent of the existing relationship, never <code>null</code>.
    */
   public PSLocator getDependent()
   {
      if(m_relationship != null)
         return m_relationship.getDependent();
      return m_dependent;
   }

   /**
    * Access method for the existing relationship.
    * @return the existing relationship because of which this exception was 
    * thrown, may be<code>null</code>.
    */
   public PSRelationship getRelationship()
   {
      return m_relationship;
   }

   private PSRelationship m_relationship;
   private PSLocator m_owner = null;
   private PSLocator m_dependent = null;
   private String m_relType = null;
}
