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
package com.percussion.cas;

import com.icl.saxon.expr.XPathException;
import com.percussion.data.PSConversionException;
import com.percussion.extension.IPSExtensionErrors;
import com.percussion.extension.PSExtensionParams;
import com.percussion.extension.PSSimpleJavaUdfExtension;
import com.percussion.server.IPSInternalRequest;
import com.percussion.server.IPSRequestContext;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.util.PSUrlUtils;
import com.percussion.xml.PSXPathEvaluator;

import java.net.MalformedURLException;
import java.util.HashMap;

import javax.xml.transform.TransformerException;

import org.w3c.dom.Document;

/**
 * This UDF is a hybrid of makeIntLink and CasGenPubLocation. This UDF will
 * query the SYS_CASSUPPORT application to get the assembly URL of the variant
 * id passed and generates the internal link (similar to the SYS_MAKEINTLINK
 * UDF).
 */
public class PSGenerateAssemblerLink extends PSSimpleJavaUdfExtension
{
   /**
    * Generates an internal URL to the assembler for the specified variant. The
    * assembler resource name is obtained by querying the sys_casSupport
    * application.
    * 
    * @param params[0] the variantid of the variant for which the location url
    *           is generated, not <code>null</code> or empty.
    * @param params[1] the contentid for which the location url is generated.
    *           Optional, but required if revisionid is passed.
    * @param params[2] the revisionid of the content for which the location url
    *           is generated. Optional, but required if contentid is passed.
    * @param params[3] the sys_authtype parameter to be added to the assembler
    *           url. Optional. If not supplied, the value will be obtained from
    *           the request context.
    *           <li>sys_context (optional)
    *           <li>sys_siteid (optional)
    *           <li>relatedItemId (optional) This parameter is included when
    *           building linkurls for an auto index while active assembly is
    *           enabled, to ensure the correct operation the Rhythmyx cache. If
    *           this parameter is not supplied, the <code>sys_command</code>
    *           parameter will not be included in the link.
    *           </ol>
    * 
    * Any optional values not supplied as parameters will be taken from the
    * request context parameters.
    * 
    * @param request The current request context, not <code>null</code>.
    * 
    * @return An internal URL including the following parameters: sys_contentid,
    *         sys_revision, sys_context, sys_variantid, sys_authtype, and
    *         pssessionid. The URL will also include the active assembly
    *         parameters sys_command, relateditemid, and activeitemid if a
    *         relatedItemId parameter has been passed to this exit, and the
    *         current request includes a sys_command parameter with a value of
    *         <code>editrc</code>. Never <code>null</code>.
    * 
    * @throws PSConversionException if supplied parameters are invalid, if the
    *            variant lookup resource cannot be found, if the xpath
    *            extraction fails, or if building the internal URL fails.
    */
   public Object processUdf(Object[] params, IPSRequestContext request)
         throws PSConversionException
   {
      PSExtensionParams ep = new PSExtensionParams(params);
      // Extract the parameters into local variables.
      // Missing or empty parameters will be converted to nulls.
      Number variantid = ep.getNumberParam(0, null, false);
      Number contentid = ep.getNumberParam(1, null, false);
      Number revisionid = ep.getNumberParam(2, null, false);
      String authType = ep.getStringParam(3, null, false);
      String context = ep.getStringParam(4, null, false);
      String siteid = ep.getStringParam(5, null, false);
      String relatedItemId = ep.getStringParam(6, null, false);

      // Validate the parameters
      if (variantid == null)
      {
         Object[] args =
         {IPSHtmlParameters.SYS_VARIANTID, "VariantId is required"};
         throw new PSConversionException(
               IPSExtensionErrors.EXT_MISSING_REQUIRED_PARAMETER_ERROR, args);
      }
      if ((contentid == null) ^ (revisionid == null))
      {
         Object[] args =
         {
               IPSHtmlParameters.SYS_CONTENTID + " / "
                     + IPSHtmlParameters.SYS_REVISION,
               "Must provide both content id and revision id."};
         throw new PSConversionException(
               IPSExtensionErrors.EXT_MISSING_REQUIRED_PARAMETER_ERROR, args);
      }

      // Obtain values from request context if not supplied explicitly
      if (contentid == null)
         contentid = Integer.parseInt(request.getParameter(
               IPSHtmlParameters.SYS_CONTENTID));
      if (revisionid == null)
         revisionid = Integer.parseInt(request.getParameter(
               IPSHtmlParameters.SYS_REVISION));
      if (authType == null)
         authType = request.getParameter(IPSHtmlParameters.SYS_AUTHTYPE, "0");
      if (context == null)
         context = request.getParameter(IPSHtmlParameters.SYS_CONTEXT, "0");
      if (siteid == null)
         siteid = request.getParameter(IPSHtmlParameters.SYS_SITEID);

      /*
       * make sure content id and revision id were obtained either as UDF
       * parameters or as request parameters
       */
      if (contentid == null || revisionid == null)
      {
         Object[] args =
         {
               IPSHtmlParameters.SYS_CONTENTID + " / "
                     + IPSHtmlParameters.SYS_REVISION,
               "Must provide both content id and revision id."};
         throw new PSConversionException(
               IPSExtensionErrors.EXT_MISSING_REQUIRED_PARAMETER_ERROR, args);
      }

      // determine the base URL for the specified variant id
      String assemblyBaseURL = getAssemblyBaseUrl(variantid.toString(), request);

      //Create a new hashmap of parameters to be passed
      //while generating the link
      HashMap paramMap = new HashMap(10);
      paramMap.put(IPSHtmlParameters.SYS_SESSIONID, request.getUserSessionId());
      paramMap.put(IPSHtmlParameters.SYS_CONTENTID, contentid.toString());
      paramMap.put(IPSHtmlParameters.SYS_REVISION, revisionid.toString());
      paramMap.put(IPSHtmlParameters.SYS_AUTHTYPE, authType);
      paramMap.put(IPSHtmlParameters.SYS_CONTEXT, context);
      paramMap.put(IPSHtmlParameters.SYS_VARIANTID, variantid.toString());
      if (siteid != null)
         paramMap.put(IPSHtmlParameters.SYS_SITEID, siteid);

      /*
       * Propagate the active-assembly parameters, if a relateditemid is
       * provided. If sys_command=editrc is passed without setting a
       * relateditemid, Rhythmyx will cache by session only.
       */
      String command = request.getParameter(IPSHtmlParameters.SYS_COMMAND);
      if (relatedItemId != null && command != null
            && command.equals(IPSHtmlParameters.SYS_ACTIVE_ASSEMBLY))
      {
         paramMap.put(IPSHtmlParameters.SYS_COMMAND, command);

         /* always include relateditemid so the cache operates correctly */
         paramMap.put(PSAddAssemblerInfo.ATTR_REALTEDITEMID, relatedItemId);

         /*
          * always include active item id to prevent auto index snippets from
          * activateing themselves
          */
         String activeItem = request.getParameter(
               IPSHtmlParameters.SYS_ACTIVEITEMID, "");
         paramMap.put(IPSHtmlParameters.SYS_ACTIVEITEMID, activeItem);
      }

      // add any extra param/value pairs supplied
      for (int i = 7; i < params.length; i += 2)
      {
         String paramName = ep.getStringParam(i, null, false);
         String paramValue = ep.getStringParam(i + 1, null, false);
         paramMap.put(paramName, paramValue);
      }

      try
      {
         return PSUrlUtils.createUrl("127.0.0.1", new Integer(request
               .getServerListenerPort()), assemblyBaseURL, paramMap.entrySet()
               .iterator(), null, request);
      }
      catch (MalformedURLException mue)
      {
         Object[] args =
         {this.getClass().getName(), mue.getLocalizedMessage()};
         throw new PSConversionException(
               IPSExtensionErrors.EXT_PROCESSOR_EXCEPTION, args);
      }
   }

   /**
    * Gets the base URL for the assembler resource associated with the supplied
    * variant ID. Makes an internal request to a system query resource to obtain
    * the URL.
    * 
    * @param variantid
    * @param request
    * @return @throws PSConversionException
    */
   protected String getAssemblyBaseUrl(String variantid,
         IPSRequestContext request) throws PSConversionException
   {
      IPSInternalRequest ireq = request.getInternalRequest(VARIANT_LOOKUP_URL,
            null, false);
      if (ireq == null)
      {
         Object[] args =
         {
               this.getClass().getName(),
               "Could not find the variant lookup resource at "
                     + VARIANT_LOOKUP_URL};
         throw new PSConversionException(
               IPSExtensionErrors.EXT_PROCESSOR_EXCEPTION, args);
      }
      IPSRequestContext innerReqContext = ireq.getRequestContext();
      innerReqContext.setParameter(IPSHtmlParameters.SYS_VARIANTID, variantid);

      Document doc = ireq.getResultDoc();
      String assemblyURL = null;
      try
      {
         PSXPathEvaluator xe = new PSXPathEvaluator(doc);
         assemblyURL = xe.evaluate("/" + ASSEMBLY_URL_NODE + "/@" + URL_ATTR);
         if (assemblyURL.length() == 0)
         {
            Object[] args =
            {this.getClass().getName(),
                  "Failed to extract the assembler URL from the variant lookup."};
            throw new PSConversionException(
                  IPSExtensionErrors.EXT_PROCESSOR_EXCEPTION, args);
         }
      }
      catch (XPathException xpe)
      {
         Object[] args =
         {
               this.getClass().getName(),
               "Exception while evaluating the expression. "
                     + xpe.getLocalizedMessage()};
         throw new PSConversionException(
               IPSExtensionErrors.EXT_PROCESSOR_EXCEPTION, args);
      }
      catch (TransformerException te)
      {
         Object[] args =
         {
               this.getClass().getName(),
               "Could not instantiate a XPathEvalutor class. "
                     + te.getLocalizedMessage()};
         throw new PSConversionException(
               IPSExtensionErrors.EXT_PROCESSOR_EXCEPTION, args);
      }
      return assemblyURL;
   }

   /**
    * The url of the application that does the look up of the variants table.
    */
   private static final String VARIANT_LOOKUP_URL = "sys_casSupport/AssemblyUrl.xml";

   /**
    * The node name to look for in the XML result
    */
   private static final String ASSEMBLY_URL_NODE = "AssemblyUrl";

   /**
    * The name of the attribute that has the value of the current asssembly url
    */
   private static final String URL_ATTR = "current";
}
