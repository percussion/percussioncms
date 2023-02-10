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
import com.percussion.design.objectstore.PSRelationship;
import com.percussion.relationship.PSTestResult;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * This class represents a set of effect test and result pairs for a
 * particular relationship. The effect processing can make use of this class 
 * to gather all relationship-effect-test results for all relationships 
 * processed. One such use is by {@link com.percussion.cms.handlers.
 * PSRelationshipEffectProcessor effect processor} which walks throw each 
 * relationship running all effects attached to it. The analysis of test 
 * results is done later before running the attempt() methods of the effects.
 * 
 * @author RammohanVangapalli
 */
public class PSRelationshipEffectTestResult
{
   /**
    * Ctor. Takes the relationship object.
    * @param relationship relationship object for which the effect test
    * results are to be stored, assumed not <code>null</code>.
    */
   public PSRelationshipEffectTestResult(PSRelationship relationship)
   {
      m_relationship = relationship;
   }

   /**
    * Add an effect and its test result.
    * @param effect the effect that was tested, assumed not <code>null</code>.
    * @param result the result of the effect test, assumed not <code>null</code>.
    */
   public void add(PSConditionalEffect effect, PSTestResult result)
   {
      m_list.add(new PSEffectTestResultPair(effect, result));
   }

   /**
    * Add effect and test result pair.
    * @param pair <code>EffectTestResultPair</code> object represent and
    * effect and its test result, assumed not <code>null</code>.
    */
   public void add(PSEffectTestResultPair pair)
   {
      m_list.add(pair);
   }

   /**
    * Access method for the test effect results object.
    * @return and iterator of <code>EffectTestResultPair</code> objects
    * representing the effect test results for the relationship,
    * never <code>null</code>
    */
   public Iterator getResults()
   {
      return m_list.iterator();
   }

   /**
    * Access method for the relationship object.
    * @return may not be <code>null</code>
    */
   public PSRelationship getRelationship()
   {
      return m_relationship;
   }
   /**
    * Reference to the relationship for which the effects are tested,
    * initialized in the ctor and never <code>null</code> after that.
    */
   private PSRelationship m_relationship = null;

   /**
    * List of {@link EffectTestResultPair objects} representing all effects
    * and results for the relationship.
    */
   private List m_list = new ArrayList();
}
