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
package com.percussion.extensions.general;

import com.percussion.extension.IPSRequestPreProcessor;
import com.percussion.extension.IPSResultDocumentProcessor;
import com.percussion.extension.PSDefaultExtension;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.server.IPSRequestContext;

import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;

/**
 * Takes a parameter that contains a delimited string list and
 * turns it into a multiple value parameter. However, do nothing if the
 * value of the parameter does not contain a delimiter and the source and
 * the destination parameters are the same; otherwise simply copy the
 * value of the source parameter to the destination parameter.
 */
public class PSParamStringListToMultiParams extends PSDefaultExtension 
   implements IPSRequestPreProcessor, IPSResultDocumentProcessor
{
   /**
    * See {@link #process(Object[], IPSRequestContext)} for the description.
    */
   public void preProcessRequest(Object[] params, IPSRequestContext request)
      throws PSParameterMismatchException
   {
      process(params, request);
   }

   /**
    * See {@link #process(Object[], IPSRequestContext)} for the description.
    * Does not change the supplied result document.
    */
   public Document processResultDocument(Object[] params, 
      IPSRequestContext request, Document resultDoc) 
      throws PSParameterMismatchException
   {
      process(params, request);
      return resultDoc;
   }

   /* (non-Javadoc)
    * @see IPSResultDocumentProcessor#canModifyStyleSheet()
    */
   public boolean canModifyStyleSheet()
   {
      return false;
   }

   /**
    * Takes a parameter that contains a delimited string list and
    * turns it into a multiple value parameter.
    * @param params takes the following four parameters:
    * <p>
    * <pre>
    *    [0] = Name of the source parameter, not <code>null</code> or empty.
    *    [1] = The delimiter, not <code>null</code>, may be empty.
    *    [2] = Name of the target parameter, not <code>null</code> or empty.
    *    [3] = Replace the target parameter if this parameter starts with
    *          'y'; otherwise append to existing target param value.
    * </pre>
    * </p>
    * @param request the current request context, assumed not <code>null</code>.
    * @throws PSParameterMismatchException for any error.
    */
   private void process(Object[] params, IPSRequestContext request) 
      throws PSParameterMismatchException
   {
      if (params == null || params.length != 4)
         throw new PSParameterMismatchException(4, (params == null ? 0
            : params.length));

      if (params[0] == null)
         throw new PSParameterMismatchException(
            "source parameter name cannot be null");
      String sourceParamName = params[0].toString().trim();

      if (params[1] == null)
         throw new PSParameterMismatchException("delimiter cannot be null");
      String delimiter = params[1].toString().trim();

      if (params[2] == null)
         throw new PSParameterMismatchException(
            "destination parameter name cannot be null");
      String destinationParamName = params[2].toString().trim();

      boolean replace = false;
      if (params[3] != null
         && params[3].toString().toLowerCase().startsWith("y"))
      {
         replace = true;
      }

      String sourceParamValue = request.getParameter(sourceParamName);
      boolean hasValue = sourceParamValue != null && 
         sourceParamValue.trim().length() > 0;
      
      // If the value contains delimiter, then converting the delimited string
      // to a list and set to the destination parameter, where the 
      // source and destination parameters may or may not be the same
      if(hasValue && sourceParamValue.indexOf(delimiter) >= 0 )
      {
         ms_log.debug("adding values from tokenized list " + sourceParamValue);
         StringTokenizer tokens =
            new StringTokenizer( sourceParamValue, delimiter );
         while (tokens.hasMoreTokens())
         {
            String value = tokens.nextToken();
            if(replace) 
            { //replace only the first time.
               ms_log.debug("replacing value " + value); 
               request.setParameter( destinationParamName, value);
               replace = false;
            }
            else 
            {            
               ms_log.debug("appending value " + value); 
               request.appendParameter( destinationParamName, value );
            }    
         }
      }
      // the value does not contain delimter, then simply copy the value of
      // the source-parameter to the destination-parameter if the 
      // 2 parameters are different
      else if (! sourceParamName.equals(destinationParamName))
      {
         // cannot use getParameter (flat) nor getParameterList (balanced), 
         // so iterate to find 'true' value
         Iterator origParams = request.getParametersIterator();
         while (origParams.hasNext())
         {
            Map.Entry param = (Map.Entry) origParams.next();
            if (param.getKey().equals(sourceParamName))
            {
               request.setParameter(destinationParamName, param.getValue());
               break;
            }
         }
      }
   }
   
   /**
    * The logger for this class
    */
   private static final Logger ms_log = LogManager.getLogger(
      PSParamStringListToMultiParams.class);
}

