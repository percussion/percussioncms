/******************************************************************************
 *
 * [ PSTestRelationshipPreExit.java ]
 * 
 * COPYRIGHT (c) 1999 - 2004 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.extensions.testing;

import com.percussion.extension.IPSRequestPreProcessor;
import com.percussion.extension.PSDefaultExtension;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.security.PSAuthorizationException;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.PSRequestValidationException;

/**
 * This exit is used to test extension processing for relationships. See 
 * Relationship Test Plan, I-B.2 Test pre- and post- exits for more info.
 * The extension takes the following parameter (all parameter values are not 
 * case sensitive):
 * 
 * @param params[0] defines the requested exit functionality. Allowed values are
 *    'pass', 'throwException/<fully qualified exception>' and 
 *    'programmingError'. If this parameter is <code>null</code> or empty, 
 *    the exit does noting.
 */
public class PSTestRelationshipPreExit extends PSDefaultExtension
   implements IPSRequestPreProcessor
{
   /**
    * If <code>params[0]</code> is specified, the requested function is
    * executed or nothing is done if the supplied function is unknown.
    * 
    * @see IPSRequestPreProcessor#preProcessRequest(Object[], IPSRequestContext)
    */
   public void preProcessRequest(Object[] params, IPSRequestContext request)
      throws PSAuthorizationException, PSRequestValidationException,
         PSParameterMismatchException, PSExtensionProcessingException
   {
      if (params != null && params.length > 0)
      {
         if (params[0] != null)
            process(params[0].toString().trim(), request);
      }
   }

   /* (non-Javadoc)
    * @see IPSResultDocumentProcessor#canModifyStyleSheet()
    */
   public boolean canModifyStyleSheet()
   {
      return false;
   }
   
   /**
    * Process the supplied method.
    * 
    * @param method the method which should be processed, assumed not
    *    <code>null</code>. Does nothing if the supplied method name is unknown
    *    or empty. Known methods are <code>PASS</code>, 
    *    <code>THROW_EXCEPTION</code> or <code>PROGRAMMING_ERROR</code>.
    * @param request the request for which to process the exit, assumed not
    *    <code>null</code>.
    * @throws PSAuthorizationException if requested. 
    * @throws PSRequestValidationException if requested. 
    * @throws PSExtensionProcessingException if requested. 
    * @throws PSParameterMismatchException if requested or if the supplied
    *    extension name is not supported. 
    */
   private void process(String method, IPSRequestContext request) 
      throws PSAuthorizationException, PSRequestValidationException,
         PSParameterMismatchException, PSExtensionProcessingException
   {
      if (method.equalsIgnoreCase(PASS))
         pass(request);
      else if (method.startsWith(THROW_EXCEPTION))
         throwException(method.substring(THROW_EXCEPTION.length()), request);
      else if (method.equalsIgnoreCase(PROGRAMMING_ERROR))
         programmingError(request);
   }
   
   /**
    * Sets an html parameter named <code>TST_RESULT_PARAMETER_NAME</code> in 
    * the supplied request with <code>PASS</code>.
    * 
    * @param request the request for which to process the exit, assumed not
    *    <code>null</code>.
    */
   private void pass(IPSRequestContext request)
   {
      request.setParameter(TST_RESULT_PARAMETER_NAME, PASS);
   }
   
   /**
    * Throws the exception suppied with the provided method.
    * 
    * @param exception the fully qualified exception class name that must be 
    *    thrown, assumed not <code>null</code> or empty, must be either
    *    <code>PSExtensionProcessingException</code>, 
    *    <code>PSAuthorizationException</code>, 
    *    <code>PSRequestValidationException</code> or 
    *    <code>PSParameterMismatchException</code>.
    * @param request the request for which to process the exit, assumed not
    *    <code>null</code>.
    * @throws PSAuthorizationException if requested. 
    * @throws PSRequestValidationException if requested. 
    * @throws PSExtensionProcessingException if requested. 
    * @throws PSParameterMismatchException if requested or if the supplied
    *    extension name is not supported. 
    */
   private void throwException(String exception, IPSRequestContext request) 
      throws PSAuthorizationException, PSRequestValidationException,
         PSParameterMismatchException, PSExtensionProcessingException
   {
      String message = "Testing relationship exit processing: ";
      request.setParameter(TST_RESULT_PARAMETER_NAME, 
         THROW_EXCEPTION.substring(0, THROW_EXCEPTION.length() - 1));
      
      if (exception.equals(PSAuthorizationException.class.getName()))
         throw new PSAuthorizationException(0, 
            new Object[] { message + exception });
      else if (exception.equals(PSRequestValidationException.class.getName()))
         throw new PSRequestValidationException(0, message + exception);
      else if (exception.equals(PSExtensionProcessingException.class.getName()))
         throw new PSExtensionProcessingException(0, message + exception);
      else if (exception.equals(PSParameterMismatchException.class.getName()))
         throw new PSParameterMismatchException(0, message + exception);
      else
         throw new PSParameterMismatchException(0, 
            "The supplied exception is not supported");
   }
   
   /**
    * Produces a programming error. In our case we force the code to throw
    * a <code>NullPointerException</code>.
    * 
    * @param request the request for which to process the exit, assumed not
    *    <code>null</code>.
    */
   @SuppressWarnings({"null"})
   private void programmingError(IPSRequestContext request)
   {
      request.setParameter(TST_RESULT_PARAMETER_NAME, PROGRAMMING_ERROR);

      String test = null;
      test = test.trim();
   }
   
   /**
    * Parameter value to specify that the method should pass.
    */
   public static final String PASS = "pass";
   
   /**
    * Parameter value to specify that the method should throw an exception.
    */
   public static final String THROW_EXCEPTION = "throwException/";
   
   /**
    * Parameter value to specify that the method should simulate a programming
    * error.
    */
   public static final String PROGRAMMING_ERROR = "programmingError";
   
   /**
    * The parameter name used to return the exit result.
    */
   private static final String TST_RESULT_PARAMETER_NAME = 
      "tst_relationshipPreExitResult";
}
