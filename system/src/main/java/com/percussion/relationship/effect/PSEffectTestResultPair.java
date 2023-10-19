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
package com.percussion.relationship.effect;

import com.percussion.design.objectstore.PSConditionalEffect;
import com.percussion.relationship.PSTestResult;

/**
 * This class represents a pair of effect, and it's test result. The effect
 * processing can make use of this class to gather all effects their test 
 * results. The analysis of test
 * results is done later before running the attempt() methods of the effects.
 * 
 * @author RammohanVangapalli
 */
public class PSEffectTestResultPair 
{
   /**
    * Constructor. Takes the effect and its test result.
    * @param effect effect tested, assumed not <code>null</code>.
    * @param result result of the test, assumed not <code>null</code>
    */
   PSEffectTestResultPair(PSConditionalEffect effect, PSTestResult result)
   {
      m_effect = effect;
      m_result = result;
   }

   /**
    * Access method for effect.
    * @return effect in the pair, not <code>null</code>
    */
   public PSConditionalEffect getEffect()
   {
      return m_effect;
   }

   /**
    * Access method for the result.
    * @return result in the pair, not <code>null</code>.
    */
   public PSTestResult getResult()
   {
      return m_result;
   }

   /**
    * Effect object, initialized in the ctor and never <code>null</code>
    * after that.
    */
   private PSConditionalEffect m_effect = null;

   /**
    * Result object, initialized in the ctor and never <code>null</code>
    * after that.
    */
   private PSTestResult m_result = null;
}
