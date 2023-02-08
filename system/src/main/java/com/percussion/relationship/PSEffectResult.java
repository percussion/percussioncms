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

import com.percussion.error.PSResult;

/**
 * This class defines abstract base for all the effect result classes.
 */
public abstract class PSEffectResult  extends PSResult
{
   /**
    * @return a state of a recurse flag.
    */
   public boolean getRecurseDependents()
   {
      return m_recurseDependents;
   }

   /**
    * Allows effect implementers to set a recursion flag. It is however up to
    * a concrete effect result class to provide an appropriate implementation.
    * This allows to have more control on this powerful feature; it basically
    * lets relationship processor, by supplying instances of different concrete
    * effect result classes, to control which of the effect's methods is allowed
    * to set this flag and which is prohibited to do so.
    *
    * For example, in a context of a 'test' method it may make sense for the
    * effect to set this flag, so the relationship engine would recurse; another
    * example is 'attempt' method where we don't want to allow to set this flag.
    * So, in the above example a concrete class PSTestResult will have a 'normal'
    * implementation of this method, while the PSAttemptResult will always throw
    * UnsupportedOperationException effectively preventing effect implementer
    * from ever using this method inside of the 'attempt' method.
    *
    * @param  recurseDependents
    * @throws UnsupportedOperationException may throw if the concrete class
    * wants to prevent a give effect from using this method.
    */
   public abstract void setRecurseDependents(boolean recurseDependents);


   /**
    * Flag indicating if the engine needs to recurse to process dependents.
    * Initilaized to <code>false</code>.
    */
   protected boolean m_recurseDependents = false;
}
