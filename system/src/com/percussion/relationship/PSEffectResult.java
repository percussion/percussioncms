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
