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
package com.percussion.rx.design;

import java.util.List;

/**
 * Association set interface for setting the associations on the design objects.
 * 
 * @author bjoginipally
 * 
 */
public interface IPSAssociationSet
{
   /**
    * Returns the list of associations. May be <code>null</code>.
    * @return
    */
   public List getAssociations();

   /**
    * Sets the list of associations. While setting the associations, the objects
    * in list should be of type that is supported by the design model otherwise
    * the associations are not set on the design object. If the associations not
    * set or the associations are set to <code>null</code> then the original
    * associations are not touched. If the associations are set to an empty list
    * then all the associations of this type on the design object are removed.
    * 
    * @param associations may be <code>null</code>
    */
   public void setAssociations(List associations);

   /**
    * Returns the type of association.
    * 
    * @return never <code>null</code>.
    */
   public AssociationType getType();

   /**
    * The action, default to {@link AssociationAction#REPLACE}.
    * 
    * @return the action, never <code>null</code>.
    */
   public AssociationAction getAction();
   
   /**
    * Sets the action.
    * 
    * @param action the new action, never <code>null</code>.
    */
   public void setAction(AssociationAction action);
   
   
   /**
    * Enumeration of the supported association types.
    * 
    * @author bjoginipally
    * 
    */
   public enum AssociationType
   {
      /**
       * Content type and workflow association type.
       */
      CONTENTTYPE_WORKFLOW,
      
      /**
       * Content type and template association type.
       */
      CONTENTTYPE_TEMPLATE,
      
      /**
       * Template and slot association type
       */
      TEMPLATE_SLOT,
      
      /**
       * Slot, content type and template association type. 
       */
      SLOT_CONTENTTYPE_TEMPLATE
   }
   
   /**
    * Action used when apply the configured association
    *
    * @author YuBingChen
    */
   public enum AssociationAction
   {
      /**
       * The specified association will replace the existing ones if there is
       * any. This is the default action.
       */
      REPLACE,
      
      /**
       * The specified association will be merged into the existing association.
       */
      MERGE,
      
      /**
       * The specified association will be removed from the existing association.
       */
      DELETE
   }
}
