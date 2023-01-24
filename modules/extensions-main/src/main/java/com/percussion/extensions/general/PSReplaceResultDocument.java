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

import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSResultDocumentProcessor;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.server.IPSInternalRequest;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.PSConsole;

import java.io.File;
import java.util.HashMap;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * This exit can be used where you need to execute condtional internal requests.
 * Rhythmyx server does not support internal requests based on the resource names
 * and needs the pipe name or internal request names today. To overcome this
 * restriction one can use this exit. To use this exit one needs to do the
 * following:
 * <P>
 * <OL>
 * <LI>Create other resource that need to be executed conditionally by probably
 * cloning the original and modifying suitably and give proper internal names
 * (pipe names)</LI>
 * <LI>Place this exit on the original resource</LI>
 * <LI>Give the name of the default resource name to execute as the first
 * parameter</LI>
 * <LI>Second parameter must be the value of the condition to choose the request
 * from the following choices</LI>
 * <LI>Third, fifth, seventh etc.. should be the option values for the conditions</LI>
 * <LI>Fourth, sixth, eighth etc.. should be the corresponding request names.
 * These are the resources you created in the workbench based on the original
 * resource</LI>
 * </OL>
 * <P>
 * One can a give an empty value for the resource name corresponding to a choice
 * in which case the original document shall not be touched. If the value
 * corresponding to the second parameter is available in the options,
 * corresponding resource is executed, otherwise default request (first
 * parameter) is executed and the result document's root element is replaced
 * with that of the result of the execution of the internal request.
 */
public class PSReplaceResultDocument implements IPSResultDocumentProcessor
{
   // see IPSResultDocumentProcessor#canModifyStyleSheet()
   public boolean canModifyStyleSheet()
   {
      return false;
   }

   // see IPSExtensionDef#init(IPSExtensionDef, File)
   public void init(IPSExtensionDef extensionDef, File file)
      throws PSExtensionException
   {
      ms_fullExtensionName = extensionDef.getRef().toString();
   }

/* Implementation of the method from the interface <code>IPSResultDocumentProcessor</code> */
   public Document processResultDocument(Object[] params,
      IPSRequestContext request, Document resultDoc)
         throws PSParameterMismatchException, PSExtensionProcessingException
   {
      request.printTraceMessage(ms_fullExtensionName + ": entering");

      if(params == null || params.length < 1)
      {
         //No parameters are specified, keep the original result document
         request.printTraceMessage( ms_fullExtensionName +
            ": Insufficient number of parameters exit not executed");
         return resultDoc;
      }

      Object defaultRequest = params[0];
      HashMap reqMap = new HashMap();
      try
      {
         Object condValue, requestName;
         //Store the option-value pairs in a hashmap.
         for(int i=2; i<params.length; i+=2)
         {
            condValue = params[i];
            if(condValue == null || condValue.toString().trim().length() < 1)
               continue;

            requestName = null;
            if(params.length > i+1)
               requestName = params[i+1];

            reqMap.put(condValue.toString().trim(), requestName);
         }
         //See if the condition specified is in the option list
         if(params.length > 1 && params[1] != null)
         {
            String key = params[1].toString().trim();
            if(reqMap.containsKey(key))
               defaultRequest = reqMap.get(key);
         }
         /* default request is forced to be null, do not execute any request
          * and keep the original result document
          */
         if(defaultRequest == null ||
            defaultRequest.toString().trim().length() < 1)
         {
            request.printTraceMessage( ms_fullExtensionName +
               "Internal request name is null, no change in the result document");
            return resultDoc;
         }

         IPSInternalRequest iReq =
            request.getInternalRequest(defaultRequest.toString().trim());
         if (iReq == null)
            throw new PSExtensionProcessingException(0,
               "Unable to locate handler for default request: " +
                  defaultRequest.toString());

         Document doc = null;
         try
         {
            iReq.makeRequest();
            doc = iReq.getResultDoc();
         }
         catch(Exception e)
         {
            PSConsole.printMsg(ms_fullExtensionName, e);
            throw new PSExtensionProcessingException(ms_fullExtensionName, e);
         }
         finally
         {
            if(iReq != null)
               iReq.cleanUp();
         }

         if (resultDoc != null)
         {
            Node resultRoot = resultDoc.getDocumentElement();
            // see if we got an empty doc from the internal request
            if (doc != null)
            {
               Node docRoot = doc.getDocumentElement();
               if (docRoot != null)
               {
                  Node importRoot = resultDoc.importNode(
                        docRoot, true);
                  if(resultRoot != null)
                  {
                     // replace result doc root with our result
                     resultDoc.replaceChild(importRoot, resultRoot);
                  }
                  else
                  {
                     // add our result as result doc root
                     resultDoc.appendChild(importRoot);
                  }
               }
               else if (resultRoot != null)
               {
                  // no result, be sure to return empty doc
                  resultDoc.removeChild(resultRoot);
               }
            }
            else if (resultRoot != null)
            {
               // we had a problem, need to be sure to return an empty doc
               resultDoc.removeChild(resultRoot);
            }
         }
      }
      catch(Exception e)
      {
         PSConsole.printMsg(ms_fullExtensionName, e);
         throw new PSExtensionProcessingException(ms_fullExtensionName, e);
      }
      request.printTraceMessage(ms_fullExtensionName + ": leaving");

      return resultDoc;
   }

   /**
    * The fully qualified name of this extension.
    */
   static private String ms_fullExtensionName = "";
}
