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
package com.percussion.validate;

import com.percussion.data.PSConversionException;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSFieldValidator;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionParams;
import com.percussion.server.IPSRequestContext;
import com.percussion.utils.jexl.IPSScript;
import com.percussion.utils.jexl.PSJexlEvaluator;

import java.io.File;

/**
 * Evaluate a JEXL expression for validity. The arguments are:
 * <table>
 * <tr>
 * <th>Param</th>
 * <th>Type</th>
 * <th>Description</th>
 * </tr>
 * <tr>
 * <td>value</td>
 * <td>java.lang.Object</td>
 * <td>The value to check</td>
 * </tr>
 * <tr>
 * <td>expression</td>
 * <td>java.lang.String</td>
 * <td>The test expression, may use $value to represent the passed value</td>
 * </tr>
 * </table>
 * 
 * @author dougrand
 */
public class PSValidateJexlExpression implements IPSFieldValidator
{

   /** (non-Javadoc)
    * @see com.percussion.extension.IPSUdfProcessor#processUdf(java.lang.Object[], com.percussion.server.IPSRequestContext)
    */
   public Object processUdf(Object[] params, IPSRequestContext request)
         throws PSConversionException
   {
      PSExtensionParams ep = new PSExtensionParams(params);
      Object value = ep.getUncheckedParam(0);
      String expression = ep.getStringParam(1, null, true);

      PSJexlEvaluator eval = new PSJexlEvaluator();
      eval.bind("$value", value);
      try
      {
         IPSScript e = PSJexlEvaluator.createScript(expression);
         Object result = eval.evaluate(e);

         if (result == null || !(result instanceof Boolean))
         {
            throw new IllegalArgumentException(
                  "Expression did not evaluate to a boolean " + expression);
         }

         return (Boolean) result;
      }
      catch (Exception e1)
      {
         throw new IllegalArgumentException("Problem evaluating expression: "
               + expression, e1);
      }
   }

   /** (non-Javadoc)
    * @see com.percussion.extension.IPSExtension#init(com.percussion.extension.IPSExtensionDef, java.io.File)
    */
   public void init(IPSExtensionDef def, File codeRoot)
         throws PSExtensionException
   {
      //
   }

}
