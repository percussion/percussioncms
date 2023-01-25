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
