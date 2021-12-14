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
