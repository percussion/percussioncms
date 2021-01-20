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

import com.percussion.data.PSDataExtractionException;
import com.percussion.data.PSExecutionData;
import com.percussion.data.PSExtensionRunner;
import com.percussion.data.PSRuleListEvaluator;
import com.percussion.design.objectstore.PSConditionalEffect;
import com.percussion.design.objectstore.PSExtensionCall;
import com.percussion.design.objectstore.PSNotFoundException;
import com.percussion.extension.IPSExtension;
import com.percussion.extension.IPSExtensionManager;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.relationship.IPSEffect;
import com.percussion.relationship.IPSExecutionContext;
import com.percussion.relationship.PSTestResult;
import com.percussion.server.PSServer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A utility class to run a list of effects for given execution context.
 * 
 * @author RammohanVangapalli
 */
public class PSEffectTestRunner
{
   /**
    * Make ctor private. Use the static method.
    */
   private PSEffectTestRunner()
   {
   }
   
   /**
    * Runs the supplied list of effects for the supplied execution conext.
    * @param execContext execution context must not be <code>null</code>.
    * @param effects iterator of all effects to run, must not be 
    * <code>null</code>, may be empty in which case, nothing happens.
    * @throws PSNotFoundException
    * @throws PSExtensionException
    * @throws PSExtensionProcessingException
    * @throws PSDataExtractionException
    * @throws PSParameterMismatchException
    */
   public static void run(
      IPSExecutionContext execContext,
      Iterator effects, PSExecutionData execData)
      throws
         PSNotFoundException,
         PSExtensionException,
         PSExtensionProcessingException,
         PSDataExtractionException,
         PSParameterMismatchException
   {
      if(execContext == null)
         throw new IllegalArgumentException("execContext must not be null");
      
      if(!effects.hasNext()) //No efefcts to run, simply return
         return;
               
      IPSExtensionManager manager = PSServer.getExtensionManager(null);
      PSExtensionRunner runner = null;
      PSTestResult result = null;
      List results = new ArrayList();
      while (effects.hasNext())
      {
         PSConditionalEffect effect = (PSConditionalEffect) effects.next();
         PSRuleListEvaluator evaluator =
            new PSRuleListEvaluator(effect.getConditions());
         boolean processThisEffect = evaluator.isMatch(execData);
         if (!processThisEffect)
            continue;
         PSExtensionCall call = effect.getEffect();
         IPSExtension extension =
            manager.prepareExtension(call.getExtensionRef(), null);
         if (extension instanceof IPSEffect)
         {
            runner = PSExtensionRunner.createRunner(call, extension);
            result = (PSTestResult) runner.testEffect(execData, execContext);

            results.add(new PSEffectTestResultPair(effect, result));
         }
      }
   }
}
