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
package com.percussion.server.clone;

import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.server.IPSRequestContext;
import com.percussion.util.IPSHtmlParameters;
import org.w3c.dom.Document;

import java.util.HashMap;
import java.util.Map;

/**
 * This exit adds all the slots, sites associated with the source variant to the
 * new variant clone. If the parameter <code>clonesourceid</code> is not
 * provided the exit does nothing. This exit also needs the newly created
 * variant id in the form of HTML parameter named "variantid" if it is
 * missing, the exit skips copying variant relations.
 */
public class PSCloneVariantExit extends PSCloneBase
{

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.extension.IPSResultDocumentProcessor#
    *       processResultDocument(java.lang.Object[],
    *         com.percussion.server.IPSRequestContext, org.w3c.dom.Document)
    */
   public Document processResultDocument(Object[] params,
         IPSRequestContext request, Document resultDoc)
         throws PSParameterMismatchException, PSExtensionProcessingException
   {
      int sourceVariantId = getCloneSourceId(request);
      String targetVariantId = request.getParameter("variantid","").trim();
      if(targetVariantId.length()<0)
      {
         request
         .printTraceMessage("Error: Missing variantid html parameter, " + 
               "skipped copying the variant relationships."
               + "Check the resource on which this exit is placed and make sure"
               + "it supplies variantid html parameter.");
         return resultDoc;
      }
      if (sourceVariantId > 0)
      {

         Map qrParams = new HashMap();
         qrParams.put(IPSHtmlParameters.SYS_VARIANTID, Integer
               .toString(sourceVariantId));
         Map upParams = new HashMap();
         upParams.put("DBActionType", "INSERT");

         //Call the base class cloneChildObjects method to clone the states and
         // others.
         cloneChildObjects(request, "VariantId", targetVariantId,
               ms_queryResources, ms_updateResources, qrParams, upParams);
      }
      return resultDoc;
   }

   /**
    * Array of query resource names of variant relations.
    */
   private static final String[] ms_queryResources =
   {"sys_variantsCloning/QueryVariantSlots",
         "sys_variantsCloning/QueryVariantSites"};

   /**
    * Array of update resource names of variant relations.
    */
   private static final String[] ms_updateResources =
   {"sys_variantsCloning/UpdateVariantSlots",
         "sys_variantsCloning/UpdateVariantSites"};
}
