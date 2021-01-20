/*******************************************************************************
 *
 * [ TracingTestPreProcExit.java ]
 *
 * COPYRIGHT (c) 1999 - 2005 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.extensions.testing;

import com.percussion.design.objectstore.PSExtensionParamDef;
import com.percussion.error.PSIllegalArgumentException;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSRequestPreProcessor;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.security.PSAuthorizationException;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.PSRequestValidationException;
import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;



public class TracingTestPreProcExit implements IPSRequestPreProcessor
{
   /**
    * Default constructor, as required for use by IPSExitHandler.
    */
   public TracingTestPreProcExit()
   {
      super();
   }


   public void init(IPSExtensionDef def, java.io.File f)
   {
      // nothing to do here
   }


   /**
    * Here we'll take the input
    * request object to locate the HashMap of HTML parameters and CGI variables.
    * We can then add a new one to each.  Also, we'll send a trace message
    * as well.
    *
    * @param request the current request context
    * @param params the input parameters for our call - there
    *               are 4 expected in this order:
    *
    *    String paramName: the name of the parameter to add
    *    String paramValue: the value of the parameter to add
    *    String cgiName: the name of the parameter to add
    *    String cgiValue: the value of the parameter to add
    *
    * @exception  PSParameterMismatchException  if the parameter number is incorrect
    * @exception  PSExtensionProcessingException      if any parameter is <code>null</code>
    */
   public void preProcessRequest(Object[] params, IPSRequestContext request)
      throws
         PSAuthorizationException,
         PSRequestValidationException,
         PSParameterMismatchException,
         PSExtensionProcessingException
   {

      // send out a trace message
      request.printTraceMessage("TracingTestPreProcExit is executing");

      // validate our input params
      int len = (params == null) ? 0 : params.length;
      if (len != 4)  // four parameters are required
         throw new PSParameterMismatchException(len, 4);

      for (int j=0; j < 4; j++){
         if (params[j] == null){
            String msg = "parameters must not be null to call preProcessRequest";
            IllegalArgumentException ex = new IllegalArgumentException(msg);
            throw new PSExtensionProcessingException( getClass().getName(), ex);
         }
      }


      String paramName  = params[0].toString();
      String paramValue = params[1].toString();
      String cgiName    = params[2].toString();
      String cgiValue   = params[3].toString();

      // get the params and cgi vars from the request
      HashMap htmlParams = null;
      boolean hasVars = false;
      
      if (request != null)
      {
         htmlParams = request.getParameters();
         hasVars = request.getHeaders().hasMoreElements();
      }
      if ((htmlParams == null) || (!hasVars))
         return;

      // now add the new one
      htmlParams.put(paramName, paramValue);
      request.setCgiVariable(cgiName, cgiValue);
   }
}

