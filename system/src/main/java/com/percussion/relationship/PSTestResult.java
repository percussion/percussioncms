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
