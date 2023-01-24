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
package com.percussion.pso.legacy;

import com.percussion.data.PSConversionException;
import com.percussion.extension.IPSUdfProcessor;
import com.percussion.extension.PSExtensionParams;
import com.percussion.extension.PSSimpleJavaUdfExtension;
import com.percussion.server.IPSRequestContext;
import com.percussion.services.utils.jexl.PSServiceJexlEvaluatorBase;
import com.percussion.utils.jexl.IPSScript;
import com.percussion.utils.jexl.PSJexlEvaluator;

import java.util.Map;

/**
 * A UDF that evaluates a JEXL expression.  This can be used in XML 
 * query resources and in Default Values where no existing UDF 
 * is available to generate the required output. 
 * <p>
 * There is only one parameter to this UDF, the JEXL expression script itself,
 * which must not be blank or empty. The JEXL expression may reference
 * the following variables:
 * <ul>
 * <li>$rx - the list of system defined JEXL functions</li>
 * <li>$user - the list of user defined JEXL functions</li>
 * <li>$tools - the Velocity Tools</li>
 * <li>$params - the HTML parameters associated with the calling
 * request</li>
 * </ul> 
 * The return value of the extension is the result of the JEXL
 * script. 
 *
 * @author DavidBenua
 *
 */
public class PSOJexlEvaluatorUDF extends PSSimpleJavaUdfExtension
      implements
         IPSUdfProcessor
{
   
   @SuppressWarnings("unchecked")
   public Object processUdf(Object[] params, IPSRequestContext req)
         throws PSConversionException
   {
      PSExtensionParams ep = new PSExtensionParams(params);
      String expression = ep.getStringParam(0, null, true);
      
      PSJexlEvaluator eval = new PSServiceJexlEvaluatorBase(true);
      
      Map<String,Object> htmlParams = req.getParameters();
      eval.bind("$param" ,htmlParams);
      try
      {
         IPSScript e = PSJexlEvaluator.createScript(expression);
         return eval.evaluate(e);
      }
      catch (Exception e1)
      {
         throw new IllegalArgumentException("Problem evaluating expression: "
               + expression);
      }
   }
}
