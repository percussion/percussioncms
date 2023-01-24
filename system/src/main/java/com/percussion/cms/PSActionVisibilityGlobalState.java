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

import org.apache.commons.lang.StringUtils;

import java.util.Collection;
import java.util.Collections;

/**
 * This object is used by the {@link PSActionVisibilityChecker} as a way to
 * extract specific state data that applies to all the objects being acted upon
 * at 1 time. Most of the data supplied by this class is user oriented. This
 * class is intended to be subclassed by implementers with specific knowledge
 * about the environment where the checking is occurring. This class provides
 * reasonable defaults for most methods.
 * <p>
 * The same user state may be applied to many
 * {@link PSActionVisibilityObjectState object state} instances.
 * 
 * @author paulhoward
 */
public abstract class PSActionVisibilityGlobalState
{
   /**
    * The community to which the user attempting the action is currently logged
    * into.
    * 
    * @return A valid community id, or -1. If -1 is returned, the action will
    * not be hidden based on the community visibility settings. The default
    * implementation returns -1.
    */
   public int getCommunityUuid()
   {
      return -1;
   }
   
   /**
    * All the roles to which the user attempting the action is a member of
    * based on his login credentials.
    * 
    * @return Never <code>null</code>. The default implementation returns an
    * empty set.
    */
   public Collection<String> getRoles()
   {
      return Collections.emptySet();
   }
   
   /**
    * The locale to which the user attempting the action is currently logged
    * into.
    * 
    * @return A 2 part string of the form 'll-cc', where ll is the 2 letter
    * language code and cc is the 2 letter country code. Never <code>null</code>
    * or empty. The default implementation returns 'en-us.'
    */
   public String getLocale()
   {
      return "en-us";
   }
   
   /**
    * The 'UI' context that the user is operating in. For example, Drag and
    * Drop, Single selection, multi-selection, etc.
    * 
    * @return Never <code>null</code>, may be empty if there isn't one. The
    * default implementation returns an empty string, which will never cause the
    * action to be hidden.
    */
   public String getClientContext()
   {
      return StringUtils.EMPTY;
   }
}
