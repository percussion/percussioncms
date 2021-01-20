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
package com.percussion.relationship.effect;

import com.percussion.design.objectstore.PSConditionalEffect;
import com.percussion.relationship.PSTestResult;

/**
 * This class represents a pair of effect and it's test result. The effect 
 * processing can make use of this class to gather all effects their test 
 * results. One such use is by {@link com.percussion.cms.handlers.
 * PSRelationshipEffectProcessor effect processor} which walks throw each 
 * relationship running all effects attached to it. The analysis of test 
 * results is done later before running the attempt() methods of the effects.
 * 
 * @author RammohanVangapalli
 */
public class PSEffectTestResultPair 
{
   /**
    * Constructor. Takes the effect and its test result.
    * @param effect efefct tested, assumed not <code>null</code>.
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
