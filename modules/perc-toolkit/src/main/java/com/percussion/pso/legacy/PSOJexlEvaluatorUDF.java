/*******************************************************************************
 * Copyright (c) 1999-2011 Percussion Software.
 * 
 * Permission is hereby granted, free of charge, to use, copy and create derivative works of this software and associated documentation files (the "Software") for internal use only and only in connection with products from Percussion Software. 
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL PERCUSSION SOFTWARE BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package com.percussion.pso.legacy;

import java.util.Map;

import org.apache.commons.jexl.Script;

import com.percussion.data.PSConversionException;
import com.percussion.extension.IPSUdfProcessor;
import com.percussion.extension.PSExtensionParams;
import com.percussion.extension.PSSimpleJavaUdfExtension;
import com.percussion.server.IPSRequestContext;
import com.percussion.services.utils.jexl.PSServiceJexlEvaluatorBase;
import com.percussion.utils.jexl.PSJexlEvaluator;

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
         Script e = PSJexlEvaluator.createScript(expression);
         return eval.evaluate(e);
      }
      catch (Exception e1)
      {
         throw new IllegalArgumentException("Problem evaluating expression: "
               + expression);
      }
   }
}
