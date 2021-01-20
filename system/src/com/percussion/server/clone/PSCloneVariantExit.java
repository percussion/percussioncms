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
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.server.clone;

import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Document;

import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.server.IPSRequestContext;
import com.percussion.util.IPSHtmlParameters;

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