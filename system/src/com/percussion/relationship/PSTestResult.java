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

package com.percussion.relationship;

/**
 * This class defines additional methods to pass sufficient informtion as test
 * result to relationship engine after executing the {@link IPSEffect#test()
 * method}. This information includes whether to test for dependents' processing
 * and error details if the test fails for some reason.
 */
public class PSTestResult  extends PSEffectResult
{
   /**
    * Implementation for an abstract method, that allows to set a recursion
    * flag.
    *
    * @param recurseDependents
    */
   public void setRecurseDependents(boolean recurseDependents)
   {
      m_recurseDependents = recurseDependents;
   }

   /**
    * Placeholder for ActivationEndPoint which was used to run Test.
    * @param isOwner <code>true</code> sets activation end point to owner and
    * <code>false</code> sets the activation end point to dependent.
    */
   public void setActivationEndPoint(boolean isOwner)
   {
      m_activationEndPointOwner = isOwner;
   }

   /**
    * Returns whether ActivationEndPoint which was used to run Test was the
    * owner.
    * @return
    */
   public boolean isActivationEndPointOwner()
   {
      return m_activationEndPointOwner;
   }

   private boolean m_activationEndPointOwner = false;
}
