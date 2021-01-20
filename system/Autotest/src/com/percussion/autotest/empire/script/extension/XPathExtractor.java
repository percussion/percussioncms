/*[ XPathExtractor.java ]******************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.autotest.empire.script.extension;

import com.percussion.autotest.empire.script.IResponseExit;
import com.percussion.autotest.empire.script.IExecutionContext;
import com.percussion.autotest.empire.script.ScriptTestErrorException;
import com.percussion.test.http.HttpHeaders;
import com.percussion.test.io.IOTools;
import com.percussion.xml.PSXPathEvaluator;

import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Properties;
import javax.xml.transform.TransformerException;

/**
 * Assigns the string value of an XPath expression evaluated against the result 
 * stream to a macro.
 */ 
public class XPathExtractor implements IResponseExit
{
   /**
    * Assigns the string value of an XPath expression evaluated against an
    * XML document parsed from the result stream to a macro.
    *
    * @param params A set of name/value pairs that were specified in the
    * script for this exit, never <code>null</code>.  This method requires
    * two names be present:  {@link #XPATH_PARAM_NAME} and 
    * {@link #MACRO_PARAM_NAME}. If either parameter is missing, or has a
    * <code>null</code> or empty value, an exception will be generated.
    *
    * @param ctx The script interpreter context in which the current script
    * is running. A macro with the name defined by the {@link #MACRO_PARAM_NAME}
    * in <code>params</code> will be added to this contenxt containing the 
    * result of the XPath evaluation.
    *
    * @param headers The headers present in the response, never 
    * <code>null</code>.  Not modified by this method.
    *
    * @param input A stream the contains the data returned with the response.
    * A copy of this stream will be returned by this method, as this stream
    * will be parsed into an XML document.
    * 
    * @return A copy of the <code>input</code> stream, never <code>null</code>.
    *
    * @throws IllegalArgumentException If <code>ctx</code>, <code>headers</code>
    * or <code>input</code> is <code>null</code>.
    *
    * @throws ScriptTestErrorException If <code>param</code> is missing a
    * required parameter, or if any error occurs while evalutating the XPath.
    */
   public InputStream processResponse(Properties params, IExecutionContext ctx, 
                                      HttpHeaders headers, InputStream input)
      throws ScriptTestErrorException
   {
      if (ctx == null || headers == null || input == null)
         throw new IllegalArgumentException(
            "Required method parameters may not be null" );
      
      // get and validate the parameters
      if (params == null)
         throw new ScriptTestErrorException("Missing required parameters: " +
         XPATH_PARAM_NAME + " and " + MACRO_PARAM_NAME );
      String xpath = params.getProperty( XPATH_PARAM_NAME );
      String macroName = params.getProperty( MACRO_PARAM_NAME );   
      if (xpath == null || xpath.trim().length() == 0)
         throw new ScriptTestErrorException("Missing required parameter: " +
         XPATH_PARAM_NAME );
      if (macroName == null || macroName.trim().length() == 0)
         throw new ScriptTestErrorException("Missing required parameter: " +
         MACRO_PARAM_NAME );
      
      // clone the input stream, as it is consumed by the xpath evaluator
      // and we need to return the stream
      ByteArrayOutputStream ourData = new ByteArrayOutputStream();
      try
      {
         IOTools.copyStream(input, ourData);
      
         PSXPathEvaluator xp = new PSXPathEvaluator( 
            new ByteArrayInputStream( ourData.toByteArray() ) );
     
         ctx.setMacro( macroName, xp.evaluate( xpath ) );
      
         return new ByteArrayInputStream( ourData.toByteArray() );
      } catch (Exception e)
      {
         throw new ScriptTestErrorException( e.toString() );
      } 
   }
   
   /** 
    * Name of the parameter that contains the macro name that will receive
    * the string value of the XPath expression.
    */
   public static final String MACRO_PARAM_NAME = "macroName";

   /** Name of the parameter that contains the XPath expression. */
   public static final String XPATH_PARAM_NAME = "xpath";
   
}
