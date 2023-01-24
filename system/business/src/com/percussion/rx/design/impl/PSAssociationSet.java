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
package com.percussion.rx.design.impl;

import com.percussion.rx.design.IPSAssociationSet;

import java.util.List;

public class PSAssociationSet implements IPSAssociationSet
{

   /**
    * Ctor for the association set.
    * 
    * @param associationType, the association type must not be <code>null</code>.
    */
   public PSAssociationSet(AssociationType associationType)
   {
      if (associationType == null)
         throw new IllegalArgumentException("associationType must not be null");
      m_associationType = associationType;
   }

   /*
    * (non-Javadoc)
    * @see com.percussion.rx.design.IPSAssociationSet#getAssociationType()
    */
   public AssociationType getType()
   {
      return m_associationType;
   }

   /*
    * (non-Javadoc)
    * @see com.percussion.rx.design.IPSAssociationSet#getAssociations()
    */
   public List getAssociations()
   {
      return m_associations;
   }

   /*
    * (non-Javadoc)
    * @see com.percussion.rx.design.IPSAssociationSet#setAssociations(java.util.List)
    */
   public void setAssociations(List associations)
   {
      m_associations = associations;
   }

   /**
    * The action, default to {@link AssociationAction#REPLACE}.
    * 
    * @return the action, never <code>null</code>.
    */
   public AssociationAction getAction()
   {
      return m_action;
   }
   
   /**
    * Sets the action.
    * 
    * @param action the new action, never <code>null</code>.
    */
   public void setAction(AssociationAction action)
   {
      if (action == null)
         throw new IllegalArgumentException(
               "Association Action may not be null.");
      
      m_action = action;
   }
   
   /**
    * The list of associations set through the set method may be
    * <code>null</code>.
    */
   private List m_associations = null;

   /**
    * The association type initialized in ctor and never <code>null</code>
    * after that.
    */
   private AssociationType m_associationType;
   
   /**
    * The action, default to {@link AssociationAction#REPLACE}.
    */
   private AssociationAction m_action = AssociationAction.REPLACE;
}
