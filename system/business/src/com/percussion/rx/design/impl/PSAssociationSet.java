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
