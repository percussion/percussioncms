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
package com.percussion.cas;

import com.percussion.cms.IPSConstants;
import com.percussion.data.PSInternalRequestCallException;
import com.percussion.error.PSExceptionUtils;
import com.percussion.extension.IPSAssemblyLocation;
import com.percussion.extension.IPSExtensionErrors;
import com.percussion.extension.PSDefaultExtension;
import com.percussion.extension.PSExtensionException;
import com.percussion.server.IPSInternalRequest;
import com.percussion.server.IPSRequestContext;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.xml.PSXmlTreeWalker;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.util.HashMap;

/**
 * Builds a delivery location by concatenating all the text nodes of the
 * XML document returned by the specified resource.
 * See {@link #createLocation(Object[], IPSRequestContext) createLocation()}
 * for the parameters supported by this exit.
 */
public class PSGenericAssembly extends PSDefaultExtension
   implements IPSAssemblyLocation
{

   private static final Logger log = LogManager.getLogger(IPSConstants.ASSEMBLY_LOG);

   /**
    * This method creates a new publishing location string using the provided
    * parameters.
    *
    * See {@link IPSAssemblyLocation#createLocation(Object[],
    * IPSRequestContext) createLocation} for details.
    *
    * @param params array containing one required element and two
    * optional elememts as described below.
    *
    * param[0] is required. It must be the URL of the resource which returns
    * an Xml document. The values of the text nodes of this Xml document will
    * be concatenated to create the location. This URL is relative to the
    * Rhythmyx root and is of the form: <i>RhythmyxApplication/Resource</i>
    * This parameter is specified using the "resource" parameter name in the
    * workbench.
    *
    * param[1] is optional. This is the content id of the item. If present,
    * this will be added as an html parameter when querying the specified
    * resource. This parameter is specified using the "contentid" parameter name
    * in the workbench.
    *
    * param[2] is optional. This is the revision id of the item. If present,
    * this will be added as an html parameter when querying the specified
    * resource. This parameter is specified using the "revision" parameter name
    * in the workbench.
    *
    * @param request the request context for this request, never
    * <code>null</code>
    *
    * @return the publishing location generated using the text nodes of the
    * XML document returned by the specified resource, never <code>null</code>,
    * may be empty
    *
    * @throws PSExtensionException if a handler for the specified
    * resource cannot be located, or an error occurs querying this resource or
    * if param[0] is missing or is <code>null</code> or empty
    */
   public String createLocation(Object[] params, IPSRequestContext request)
      throws PSExtensionException
   {
      request.printTraceMessage(ms_className + "#createLocation");
      try
      {
         // check the number of parameters provided is correct
         if ((params.length < EXPECTED_NUMBER_OF_PARAMS) ||
            (params[0] == null) ||
            (params[0].toString().trim().length() < 1))
         {
            Object[] args =
            {
               "" + EXPECTED_NUMBER_OF_PARAMS,
               "" + params.length
            };
            throw new PSExtensionException(
               IPSExtensionErrors.EXT_PARAM_VALUE_MISMATCH, args);
         }

         String resource = params[0].toString().trim();

         HashMap<String,String> newHTMLParams = new HashMap<>();
         if ((params.length >= 2) && (params[1] != null) &&
            (params[1].toString().trim().length() > 0))
         {
            newHTMLParams.put(IPSHtmlParameters.SYS_CONTENTID,
               params[1].toString().trim());
         }
         if ((params.length >= 3) && (params[2] != null) &&
            (params[2].toString().trim().length() > 0))
         {
            newHTMLParams.put(IPSHtmlParameters.SYS_REVISION,
               params[2].toString().trim());
         }

         IPSInternalRequest iReq = request.getInternalRequest(
            resource, newHTMLParams, true);
         if (iReq == null)
         {
            String msg = ms_className +
               ": Unable to locate handler for request: " + resource;
            request.printTraceMessage(msg);
            throw new PSExtensionException(ms_className, msg);
         }

         StringBuilder location = new StringBuilder();
         Document doc = iReq.getResultDoc();
         if (doc != null)
         {
            PSXmlTreeWalker walker = new PSXmlTreeWalker(doc);
            Node node = walker.getNext();
            while (node != null)
            {
               location.append(PSXmlTreeWalker.getElementData(node));
               node = walker.getNext();
            }
         }
         request.printTraceMessage(
            ms_className + "#createLocation location = " + location);

         return location.toString();
      }
      catch (PSInternalRequestCallException ex)
      {
         log.debug(PSExceptionUtils.getDebugMessageForLog(ex));
         request.printTraceMessage("Error: " + PSExceptionUtils.getMessageForLog(ex));
         throw new PSExtensionException(ms_className, ex.getLocalizedMessage());
      }
   }

   /**
    * The function name used for error handling
    */
   private static final String ms_className = "PSGenericAssembly";

   /**
    * The number of expected parameters.
    */
   private static final int EXPECTED_NUMBER_OF_PARAMS = 1;

}


