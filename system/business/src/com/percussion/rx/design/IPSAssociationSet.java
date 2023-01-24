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
