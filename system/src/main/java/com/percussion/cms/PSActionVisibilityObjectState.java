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
