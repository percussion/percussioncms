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
package com.percussion.cms;

import com.percussion.cms.objectstore.PSCmsObject;
import com.percussion.cms.objectstore.PSObjectPermissions;

import org.apache.commons.lang.StringUtils;

/**
 * This object is used by the {@link PSActionVisibilityChecker} as a way to
 * extract specific state data about some object (an item or folder).
 * This class is intended to be subclassed by users with specific knowledge
 * about the environment where the checking is occurring. This class provides
 * reasonable defaults for most methods.
 * 
 * @author paulhoward
 */
public abstract class PSActionVisibilityObjectState
{
   /**
    * If this object is a folder, then the permissions of the folder are
    * returned. If this object is an item and it is in a folder, then the parent
    * folder's permissions are returned.
    * 
    * @return May be <code>null</code> if the object is not a folder and not a
    * child of a folder. The default implementation returns <code>null</code>.
    */
   public PSObjectPermissions getFolderPermissions()
   {
      return null;
   }

   /**
    * The workflow privileges for the current user on the current object.
    * 
    * @return -1 if not set, otherwise a valid value. The default implementation
    * returns -1.
    */
   public int getAssignmentType()
   {
      return -1;
   }

   /**
    * @return Always a valid type.
    */
   public abstract int getContentTypeUuid();

   /**
    * Is the object an item or a folder as defined in {@link PSCmsObject} by
    * the TYPE_XXX values.
    * 
    * @return Always a valid type id. The default implementation returns 
    * {@link PSCmsObject#TYPE_ITEM}.
    */
   public int getObjectType()
   {
      return PSCmsObject.TYPE_ITEM;
   }

   /**
    * The state of the item in regards to being checked out. If checked out,
    * either <code>Checked Out By Me</code> or <code>Checked Out</code> is
    * returned.
    * 
    * @return A string that represents the state, or an empty string for folders.
    * The default implementation always returns an empty string.
    */
   public String getCheckoutStatus()
   {
      return StringUtils.EMPTY;
   }

   /**
    * The id of the workflow to which the item  belongs.
    * 
    * @return A valid id if the object is an item. -1 if a folder. The default
    * implementation returns -1.
    */
   public int getWorkflowAppUuid()
   {
      return -1;
   }

   /**
    * One of the 'valid' flags that indicates the context of the workflow state
    * the item is in (e.g. publishable, quick edit, etc.)
    *   
    * @return Empty if the object is a folder, otherwise a single character
    * that represents the flag. The default implementation returns an empty
    * string.
    */
   public String getPublishableType()
   {
      return StringUtils.EMPTY;
   }
}
