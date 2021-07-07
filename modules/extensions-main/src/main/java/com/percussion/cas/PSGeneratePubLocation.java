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

import static com.percussion.util.IPSHtmlParameters.SYS_OVERWRITE_PREVIEW_URL_GEN;

import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.data.PSConversionException;
import com.percussion.data.PSDatabaseMetaData;
import com.percussion.data.PSInternalRequestCallException;
import com.percussion.design.objectstore.PSExtensionParamValue;
import com.percussion.design.objectstore.PSNotFoundException;
import com.percussion.design.objectstore.PSTextLiteral;
import com.percussion.extension.IPSAssemblyLocation;
import com.percussion.extension.IPSExtension;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSExtensionErrors;
import com.percussion.extension.IPSExtensionManager;
import com.percussion.extension.IPSUdfProcessor;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionParams;
import com.percussion.extension.PSExtensionRef;
import com.percussion.extension.PSSimpleJavaUdfExtension;
import com.percussion.security.SecureStringUtils;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.PSServer;
import com.percussion.services.assembly.IPSAssemblyService;
import com.percussion.services.assembly.jexl.PSExtensionWrapper;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.filter.IPSFilterService;
import com.percussion.services.filter.IPSItemFilter;
import com.percussion.services.filter.PSFilterException;
import com.percussion.services.filter.PSFilterServiceLocator;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.services.guidmgr.PSGuidUtils;
import com.percussion.services.legacy.IPSCmsObjectMgr;
import com.percussion.services.legacy.PSCmsObjectMgrLocator;
import com.percussion.services.sitemgr.IPSLocationScheme;
import com.percussion.services.sitemgr.IPSPublishingContext;
import com.percussion.services.sitemgr.IPSSiteManager;
import com.percussion.services.sitemgr.PSSiteManagerLocator;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.util.PSHtmlParameters;
import com.percussion.util.PSPreparedStatement;
import com.percussion.util.PSSqlHelper;
import com.percussion.util.PSUrlUtils;
import com.percussion.utils.exceptions.PSExceptionHelper;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.jdbc.PSConnectionDetail;
import com.percussion.utils.jdbc.PSConnectionHelper;
import com.percussion.utils.security.PSSecurityUtility;
import com.percussion.utils.timing.PSStopwatchStack;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.NamingException;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This generic UDF generates public locations for all contexts. The preview
 * generator is hardcoded here while the generators for all other contexts are
 * specified in the system table RXLOCATIONSCHEME.
 */
public class PSGeneratePubLocation extends PSSimpleJavaUdfExtension
{
   /**
    * Commons logging logger for this class
    */
   private static final Logger log = LogManager.getLogger(PSGeneratePubLocation.class);

   /**
    * Overwrite the base class to save the extension definition used to report
    * errors.
    * <p>
    * See {@link IPSExtension#init(IPSExtensionDef, File) init}for details.
    */
   public void init(IPSExtensionDef def, File codeRoot)
         throws PSExtensionException
   {
      m_def = def;
   }

   /**
    * First this will check that all required HTML paramaeters are available.
    * Then the location generator is called. The generator for context 0
    * (preview) is hardcoded, while all other generators are looked up in the
    * RXLOCATIONSCHEME table making an internal request to the system support
    * application 'sys_casSupport'.
    * <p>
    * See
    * {@link IPSUdfProcessor#processUdf(Object[], IPSRequestContext) processUdf}
    * for details.
    * 
    * @param params the parameters for this extension, never <code>null</code>
    *           <table>
    *           <tr>
    *           <th>0</th>
    *           <td>the variantid for which the location url will be created.
    *           Required, not <code>null</code>.</td>
    *           </tr>
    *           <tr>
    *           <th>1</th>
    *           <td>the contentid for which the location url will be created.
    *           Optional, may be <code>null</code>. If <code>null</code> is
    *           provided, the contentid from the current request will be used.</td>
    *           </tr>
    *           <tr>
    *           <th>2</th>
    *           <td>the revision for which the location url will be created.
    *           Optional, may be <code>null</code>. If <code>null</code> is
    *           provided, the revision from the current request will be used.</td>
    *           </tr>
    *           <tr>
    *           <th>3</th>
    *           <td>context value (that can override the default one) for which
    *           the location url will be created. Optional, may be
    *           <code>null</code> or empty in which case the default value
    *           specified by {@link IPSHtmlParameters#SYS_CONTEXT}is used.</td>
    *           </tr>
    *           <tr>
    *           <th>4</th>
    *           <td>siteid parameter, if provided will be appended to the
    *           generated publication url as
    *           {@link IPSHtmlParameters#SYS_SITEID}. Optional, if
    *           <code>null</code> then the sys_siteid value from the current
    *           request will be used, if it does not exist in the current
    *           request or its value is empty then SYS_SITEID parameter will not
    *           be added.</td>
    *           </tr>
    *           <tr>
    *           <th>5</th>
    *           <td>folderid parameter, if provided will be appended to the
    *           generated publication url as
    *           {@link IPSHtmlParameters#SYS_FOLDERID}. Optional, if
    *           <code>null</code> then the folderid value from the current
    *           request will be used, if it does not exist in the current
    *           request or its value is empty then SYS_FOLDERID parameter will
    *           not be added.</td>
    *           </tr>
    *           <tr>
    *           <th>6</th>
    *           <td>authtype value (that can override the default one) for
    *           which the location url will be created. Optional, may be
    *           <code>null</code> or empty in which case the default value
    *           specified by {@link IPSHtmlParameters#SYS_AUTHTYPE}is used.
    *           <tr>
    *           <th>7</th>
    *           <td>filter name, if provided this will be passed to any preview
    *           url generated. the filter's legacy authtype (if defined) will
    *           override the authtype value (if provided). </td>
    *           </tr>
    *           <tr>
    *           <th>8</th>
    *           <td>page number, if provided this will be passed to the
    *           extension.</td>
    *           </tr>
    *           </table>
    * @param request the parameter request, never <code>null</code>
    */
   @SuppressWarnings("deprecation")
   public Object processUdf(Object[] params, IPSRequestContext request)
         throws PSConversionException
   {
      if (request == null)
      {
         throw new IllegalArgumentException("request may not be null");
      }
      String exitName = m_def.getRef().getExtensionName();
      request.printTraceMessage("Entering " + exitName + ".processUdf");
      Number contentid = null;
      Number variantid = null;
      Number revision = null;
      String urlString = "";
      Map paramsBackup = request.getParameters();
      PSExtensionParams eparams = new PSExtensionParams(params, new String[]
      {IPSHtmlParameters.SYS_VARIANTID, IPSHtmlParameters.SYS_CONTENTID,
            IPSHtmlParameters.SYS_REVISION, IPSHtmlParameters.SYS_CONTEXT,
            IPSHtmlParameters.SYS_SITEID, IPSHtmlParameters.SYS_FOLDERID,
            IPSHtmlParameters.SYS_AUTHTYPE, IPSHtmlParameters.SYS_ITEMFILTER});
      PSStopwatchStack sws = PSStopwatchStack.getStack();
      try
      {
         sws.start("PSGeneratePubLocation#processUdf");
         variantid = eparams.getNumberParam(0, null, true);
         contentid = eparams.getNumberParam(1, null, true);
         revision = eparams.getNumberParam(2, null, true);
         IPSGuid contextId = PSGuidUtils.makeGuid(eparams
               .getNumberParam(3, request.getParameter(
                     IPSHtmlParameters.SYS_CONTEXT, "0"), false).longValue(),
               PSTypeEnum.CONTEXT);
         Number siteid = eparams.getNumberParam(4, request
               .getParameter(IPSHtmlParameters.SYS_SITEID), false);
         Number folderid = eparams.getNumberParam(5, request
               .getParameter(IPSHtmlParameters.SYS_FOLDERID), false);
         String authtype = eparams.getStringParam(6, null, false);;
         String filtername = eparams.getStringParam(7, null, false);
         Number page = eparams.getNumberParam(8, null, false);
         String authtype_from_filter = null;
         if (StringUtils.isBlank(filtername) && StringUtils.isBlank(authtype))
         {
            authtype = request.getParameter(IPSHtmlParameters.SYS_AUTHTYPE);
         }
         if (StringUtils.isNotBlank(filtername))
         {
            // The filter value, get the filter and deal with the authtype
            IPSFilterService fsvc = PSFilterServiceLocator.getFilterService();
            try
            {
               IPSItemFilter filter = fsvc.findFilterByName((String) params[7]);
               filtername = filter.getName();
               if (filter.getLegacyAuthtypeId() != null)
               {
                  authtype_from_filter = filter.getLegacyAuthtypeId()
                        .toString();
               }
            }
            catch (PSFilterException f)
            {
               // Drop through
            }
         }
         if (filtername != null && authtype != null)
         {
            Object args[] =
            {"Both filter and authtype can not both be set."};
            throw new PSConversionException(
                  IPSExtensionErrors.EXT_PARAM_VALUE_INVALID, args);
         }

         authtype = authtype_from_filter;

         if (authtype == null || authtype.length() < 1)
            authtype = request
                  .getParameter(IPSHtmlParameters.SYS_AUTHTYPE, "0");

         Map htmlParams = PSHtmlParameters.createStandardParams(paramsBackup);
         request.setParameters((HashMap) htmlParams);

         /*
          * Use the parameters they provided. The variantid is always provided,
          * while the contentid and the revision are optional. We set them in
          * all cases, since its just a reset if not actually provided.
          */
         request.setParameter(IPSHtmlParameters.SYS_VARIANTID, variantid
               .toString());
         if (contentid != null)
         {
            request.setParameter(IPSHtmlParameters.SYS_CONTENTID, contentid
                  .toString());
         }
         else
         {
            contentid = 
               Integer.parseInt(request.getParameter(IPSHtmlParameters.SYS_CONTENTID));
         }
         if (revision != null)
         {
            request.setParameter(IPSHtmlParameters.SYS_REVISION, revision
                  .toString());
         }
         else
         {
            revision = 
               Integer.parseInt(request.getParameter(IPSHtmlParameters.SYS_REVISION));
         }         
         if (authtype != null)
         {
            request.setParameter(IPSHtmlParameters.SYS_AUTHTYPE, authtype);
            request.removeParameter(IPSHtmlParameters.SYS_ITEMFILTER);
         }
         else if (filtername != null)
         {
            request.setParameter(IPSHtmlParameters.SYS_ITEMFILTER, filtername);
            request.removeParameter(IPSHtmlParameters.SYS_AUTHTYPE);
         }
         if (siteid != null)
            request.setParameter(IPSHtmlParameters.SYS_SITEID, siteid);
         if (folderid != null)
            request.setParameter(IPSHtmlParameters.SYS_FOLDERID, folderid);
         if (page != null)
            request.setParameter("sys_page", page.toString());
         
         // produce the location depending on current context
         if (contextId.getUUID() == 0)
         {
            String udfGenerator = getCustomUrlGenerator(request, variantid);
            if (StringUtils.isNotBlank(udfGenerator))
            {
                urlString = generateCustomPreviewUrl(contentid, revision, udfGenerator);
            }
            else
            {
               urlString = generatePreviewLocation(request, contentid, variantid,
                     revision, paramsBackup, siteid, folderid, authtype,
                     filtername, page);
            }
         }
         else
         {
            // for contexts other than preview (0), use a generator
            urlString = generatePubLocation(contextId, request,
                  contentid, revision, variantid);
         }

         request.printTraceMessage("Public URL= " + urlString);
      }
      catch (PSExtensionException e)
      {
         log.error("Problem while generating a publishing, location for template {} and contentid {} Error: {}",variantid, contentid, e.getMessage());
         log.debug(e.getMessage(),e);
      }
      catch (Throwable e)
      {
         log.error("Problem while generating a publishing "
               + "location for template " + variantid + " and contentid "
               + contentid, PSExceptionHelper.findRootCause(e, true));
      }
      finally
      {
         // restore the original HTML parameters
         request.setParameters((HashMap) paramsBackup);
         sws.stop();
      }

      request.printTraceMessage("Leaving " + exitName + ".processUdf");
      return urlString;
   }

   /**
    * Gets the customer link generator if specified in the request.
    * 
    * @param request the request may contain the specified link generator for preview, 
    * assumed not <code>null</code>.
    * @param variantid the ID of the template, used for the link, assumed not <code>null</code>. 
    * 
    * @return the (fully qualified) name of the custom generator. 
    * It may be <code>null</code> if the request does not contain a custom generator
    * or the generator should not be used for current link.
    */
   private String getCustomUrlGenerator(IPSRequestContext request,
         Number variantid)
   {
      String[] values = (String[])request.getPrivateObject(SYS_OVERWRITE_PREVIEW_URL_GEN);
      if (values == null || values.length < 1)
         return null;
      
      int templateId = values.length >= 2 ? Integer.parseInt(values[1].toString()) : -1;
      return (variantid.intValue() == templateId) ? null : values[0].toString();
   }

   /**
    * Generate the preview location, which is always fixed and passes through
    * the assembly service. The parameters to this method, plus the command
    * information is simply concatenated into a simple URL
    * 
    * @param request the original request, assumed never <code>null</code>
    * @param contentid the content id, assumed never <code>null</code>
    * @param variantid the variant id, assumed never <code>null</code>
    * @param revision the revision, assumed never <code>null</code>
    * @param paramsBackup the original parameters before being overriden,
    *           assumed never <code>null</code>
    * @param siteid the siteid, may be <code>null</code>
    * @param folderid the folderid, may be <code>null</code>
    * @param authtype the authtype, may be <code>null</code>
    * @param filtername the filtername, may be <code>null</code>
    * @param page if not <code>null</code> and &gt; 0 then a sys_page parameter
    *     will be added to the url to allow paging through a paged content item.
    * @return the url string, never <code>null</code>
    * @throws PSConversionException
    * @throws PSInternalRequestCallException
    * @throws MalformedURLException
    */
   private String generatePreviewLocation(IPSRequestContext request,
         Number contentid, Number variantid, Number revision, Map paramsBackup,
         Number siteid, Number folderid, String authtype, String filtername, 
         Number page)
         throws PSConversionException, PSInternalRequestCallException,
         MalformedURLException
   {
      PSStopwatchStack sws = PSStopwatchStack.getStack();
      sws.start("PSGeneratePubLocation#generatePreviewLocation");
      try
      {
         Map<String, String> paramMap = new HashMap<>(5);
         paramMap.put(IPSHtmlParameters.SYS_CONTENTID, contentid.toString());
         paramMap.put(IPSHtmlParameters.SYS_REVISION, revision.toString());
         paramMap.put(IPSHtmlParameters.SYS_VARIANTID, variantid.toString());
         paramMap.put(IPSHtmlParameters.SYS_CONTEXT, PREVIEW_CONTEXT);

         if (page != null && page.intValue() > 0)
         {
            paramMap.put("sys_page", page.toString());
         }
         
         if (authtype != null)
         {
            paramMap.put(IPSHtmlParameters.SYS_AUTHTYPE, authtype);
         }
         else if (filtername != null)
         {
            paramMap.put(IPSHtmlParameters.SYS_ITEMFILTER, filtername);
         }

         if (siteid != null)
            paramMap.put(IPSHtmlParameters.SYS_SITEID, siteid.toString());
         if (folderid != null)
            paramMap.put(IPSHtmlParameters.SYS_FOLDERID, folderid.toString());
         
         Object command = paramsBackup.get(IPSHtmlParameters.SYS_COMMAND);
         if (command != null)
         {
            paramMap.put(IPSHtmlParameters.SYS_COMMAND, command.toString());
            /*
             * When sys_command='editrc', every link generated shall have
             * parameter sys_activeitemid="" so that the navigation is always in
             * active preview mode.
             */
            if (command.equals(IPSHtmlParameters.SYS_ACTIVE_ASSEMBLY))
            {
               String aamode = 
                  request.getParameter(
                     IPSHtmlParameters.SYS_ACTIVE_ASSEMBLY_MODE);
               if(StringUtils.isNotBlank(aamode))
                  paramMap.put(
                     IPSHtmlParameters.SYS_ACTIVE_ASSEMBLY_MODE, aamode);
               paramMap.put(IPSHtmlParameters.SYS_ACTIVEITEMID, "");
            }
         }

         // make a relative rhythmyx link
         String partial = PSServer.getRequestRoot() + "/"
               + IPSAssemblyService.ASSEMBLY_URL;
         String url = PSUrlUtils.createUrl(partial, paramMap
               .entrySet().iterator(), null);

         return url;
      }
      finally
      {
         sws.stop();
      }
   }

   /**
    * The custom preview URL generator map, which maps the name of the generator to its UDF instance. 
    * It is default to empty and lazily loaded UDF extension as needed.
    */
    Map<String, PSExtensionWrapper> m_previewUrlGeneratorMap = new HashMap<>();

   /**
    * Generates the preview URL from a specified UDF extension.
    * 
    * @param contentid the ID of the item in question. 
    * @param revision the revision of the item.
    * @param fqnUdfName the fully qualified UDF name, assumed not blank.
    * 
    * @return the preview URL, not blank.
    * 
    * @throws PSConversionException if an error occurs.
    */
   private String generateCustomPreviewUrl(Number contentid, Number revision, String fqnUdfName)
         throws PSConversionException
   {
      PSExtensionWrapper previewUrlGenerator = m_previewUrlGeneratorMap.get(fqnUdfName); 
      if ( previewUrlGenerator == null)
      {
         int index = fqnUdfName.lastIndexOf("/");
         String context = index != -1 ? fqnUdfName.substring(0, index + 1) : "";
         String name = index != -1 ? fqnUdfName.substring(index + 1) : fqnUdfName;
         previewUrlGenerator = new PSExtensionWrapper(context, name);
         m_previewUrlGeneratorMap.put(fqnUdfName, previewUrlGenerator);
      }
      
      Object link = previewUrlGenerator.call(new Integer(contentid.intValue()), new Integer(revision.intValue()));
      return (String) link;
   }

   /**
    * Generate the location from the location scheme. This method looks up the
    * correct location scheme, then uses it to run the appropriate scheme
    * generator with the parameters registered for the scheme. The scheme is
    * first chosen to match the given content type id and template id within the
    * specified context. If there's no match then the default scheme is used.
    * 
    * @param contextid the context, assumed never <code>null</code>
    * @param request the request context with the overriden parameters, assumed
    *           never <code>null</code>
    * @param contentid the content id, assumed never <code>null</code>
    * @param revision the revision, assumed never <code>null</code>
    * @param variantid the template being used, assumed never <code>null</code>
    * @return the location string, which is used for either the assembly or
    *         publishing process
    * @throws PSConversionException
    * @throws SQLException
    * @throws PSNotFoundException
    * @throws PSExtensionException
    * @throws NamingException
    */
   private String generatePubLocation(IPSGuid contextid,
         IPSRequestContext request, Number contentid, Number revision,
         Number variantid) throws PSConversionException, SQLException,
         PSNotFoundException, PSExtensionException, NamingException
   {
      PSStopwatchStack sws = PSStopwatchStack.getStack();
      sws.start("PSGeneratePubLocation#generatePubLocation");
      try
      {
         IPSCmsObjectMgr cmgr = PSCmsObjectMgrLocator.getObjectManager();
         IPSSiteManager sitemgr = PSSiteManagerLocator.getSiteManager();
         IPSGuidManager gmgr = PSGuidManagerLocator.getGuidMgr();
         IPSGuid templateid = gmgr.makeGuid(variantid.longValue(),
               PSTypeEnum.TEMPLATE);

         PSComponentSummary summary = cmgr.loadComponentSummary(contentid
               .intValue());

         IPSLocationScheme thescheme = null;
         IPSGuid ctid = summary.getContentTypeGUID();
         try
         {
            List<IPSLocationScheme> schemes = sitemgr.findSchemeByAssemblyInfo(
                  templateid, contextid, ctid);

            if (schemes == null || schemes.size() == 0)
            {
               IPSPublishingContext context = sitemgr.loadContext(contextid);
               thescheme = context.getDefaultScheme();
               if (thescheme == null)
               {
                  throw new RuntimeException(
                        "Default Location Scheme is not defined in Context id: "
                              + contextid.toString());
               }
            }
            else
            {
               thescheme = schemes.get(0);
            }
         }
         catch (Exception sme)
         {
            Object args[] = new Object[]
            {variantid, ctid.longValue(), contextid};
            PSExtensionException extExc = new PSExtensionException(
                  IPSExtensionErrors.SCHEME_CANT_BE_FOUND, sme, args);
            
            log.error(extExc.getLocalizedMessage(), sme);
            
            throw extExc;
         }

         String generatorExit = thescheme.getGenerator();

         int rev = -1;
         if (revision != null)
            rev = revision.intValue();

         // resolve any back-end parameters in the location scheme
         PSExtensionParamValue[] exitParams = getExitParameters(thescheme,
               contentid.intValue(), rev);

         PSExtensionRef ref = new PSExtensionRef(generatorExit);
         IPSExtensionManager exitMgr = PSServer.getExtensionManager(null);
         IPSExtension exit = exitMgr.prepareExtension(ref, null);
         if (!(exit instanceof IPSAssemblyLocation))
         {
            Object[] args =
            {exit.getClass().getName(), IPSAssemblyLocation.class.getName()};
            throw new PSConversionException(
                  IPSExtensionErrors.UNEXPECTED_EXT_TYPE_EXCEPTION, args);
         }

         String[] callParams = new String[exitParams.length];
         for (int i = 0; i < exitParams.length; i++)
            callParams[i] = exitParams[i].getValue().getValueText();
         IPSAssemblyLocation generator = (IPSAssemblyLocation) exit;

         String oldContext = request
               .getParameter(IPSHtmlParameters.SYS_CONTEXT);
         request.setParameter(IPSHtmlParameters.SYS_CONTEXT, contextid
               .longValue());
         String location = generator.createLocation(callParams, request);
         request.setParameter(IPSHtmlParameters.SYS_CONTEXT, oldContext);
         return location;
      }
      finally
      {
         sws.stop();
      }
   }

   /**
    * Check if the location is to be generated for a cross site link. This is
    * evaluated by comparing the current siteid and the originating siteid from
    * the request.
    * 
    * @param request request context object, must not be <code>null</code>.
    * @param currentSiteId current siteid for the link to be generated, may be
    *           <code>null</code>
    * @return <code>true</code> if the link is across sites,
    *         <code>false</code> otherwise.
    */
   public static boolean isCrossSite(IPSRequestContext request,
         String currentSiteId)
   {
      if (request == null)
         throw new IllegalArgumentException("request must not be null");

      if (currentSiteId == null)
         currentSiteId = "";

      String originalSitedId = request.getParameter(
            IPSHtmlParameters.SYS_ORIGINALSITEID, "");
      if (originalSitedId != null)
         originalSitedId = originalSitedId.trim();

      if (originalSitedId.length() > 0
            && !originalSitedId.equals(currentSiteId))
         return true;

      return false;
   }

   /**
    * Get all exit parameters from the provided generator element.
    * 
    * @param scheme the scheme to get the parameters from, assumed never
    *           <code>null</code>
    * @param contentid the contentid for which to get the parameters for. This
    *           is only used for BackendColumn parameter types.
    * @param revision the revision for which to get the parameters for. This is
    *           only used for BackendColumn parameter types.
    * 
    * @return an array of PSExtensionParamValue objects with all parameters
    *         found, never <code>null</code>, might be empty.
    * @throws SQLException if any SQL operation fails.
    * @throws NamingException
    */
   @SuppressWarnings("unchecked")
   protected PSExtensionParamValue[] getExitParameters(
         IPSLocationScheme scheme, int contentid, int revision)
         throws SQLException, NamingException
   {
      Connection connection = null;
      PreparedStatement statement = null;
      ResultSet rs = null;
      String driver = null;
      String db = null;
      String schema = null;
      try
      {
         ArrayList parameters = new ArrayList();
         for (String pname : scheme.getParameterNames())
         {
            String type = scheme.getParameterType(pname);
            String value = scheme.getParameterValue(pname);
            if (value == null)
               value = "";

            if (type.equalsIgnoreCase(IPSAssemblyLocation.TYPE_STRING))
            {
               PSTextLiteral literal = new PSTextLiteral(value);
               parameters.add(new PSExtensionParamValue(literal));
            }
            else if (type.equalsIgnoreCase("variant-based")
                  || type
                        .equalsIgnoreCase(IPSAssemblyLocation.TYPE_BACKEND_COLUMN))
            {
               if (connection == null)
               {
                  // This lazy retrieval keeps us from unnecessarily 
                  // opening a connection for location generators that
                  // aren't using back end columns
                  connection = PSConnectionHelper.getDbConnection();
                  PSConnectionDetail detail = 
                     PSConnectionHelper.getConnectionDetail();
                  driver = detail.getDriver();
                  db = detail.getDatabase();
                  schema = detail.getOrigin();
               }
               String backend = value.trim();
               int pos = backend.indexOf('.');
               if (pos > 0)
               {
                  String table = backend.substring(0, pos);
                  table = PSSqlHelper.qualifyTableName(table, db, schema,
                        driver);
                  String column = backend.substring(pos + 1);

                  if (type.equalsIgnoreCase("variant-based"))
                  {
                     throw new UnsupportedOperationException(
                           "Variant based properties were never properly "
                                 + "implemented, reimplement using the JEXL location "
                                 + "scheme generator");
                  }
                  else
                     statement = prepareContentStatement(table, column,
                           contentid, revision, connection);

                  rs = statement.executeQuery();
                  if (rs.next())
                  {
                     ResultSetMetaData metaData = rs.getMetaData();
                     PSTextLiteral literal = null;
                     int dataType = metaData.getColumnType(1);
                     Map typeMap = PSDatabaseMetaData.loadNativeDataTypeMap(
                           connection, connection.getMetaData());
                     Object nativeType = typeMap.get(metaData
                           .getColumnTypeName(1));
                     if (nativeType != null)
                        dataType = ((Short) nativeType).intValue();
                     switch (dataType)
                     {
                        case Types.CHAR :
                           // needs trim
                           literal = new PSTextLiteral(rs.getString(1).trim());
                           break;

                        default :
                           // try to get this as string
                           literal = new PSTextLiteral(rs.getString(1));
                           break;
                     }
                     parameters.add(new PSExtensionParamValue(literal));
                  }
                  /*
                   * 20020219 PH: TODO This is a quick fix for now to get the
                   * customer out of a jam. We should revisit this and either
                   * use the prepared statement properly or not use a prepared
                   * statement.
                   */
                  rs.close();
                  // if the close throws, it will just try to close it again
                  // below
                  rs = null;
                  statement.close();
                  statement = null;
               }
            }
            else if (type.trim().equalsIgnoreCase(
                  IPSAssemblyLocation.TYPE_PASSTHROUGH))
            {
               parameters.add(value);
            }
         }

         PSExtensionParamValue[] paramValues = new PSExtensionParamValue[parameters
               .size()];
         paramValues = (PSExtensionParamValue[]) parameters
               .toArray(paramValues);
         return paramValues;
      }
      finally
      {
         // release the database connection and any resources allocated
         if (connection != null)
         {
            try
            {
               connection.close(); // This will release the connection back
            }
            catch (SQLException e)
            {
               log.error(e);
            }
         }
      }
   }

   /**
    * Prepare the SQL statement to lookup the provided column.
    * 
    * @param table the from which to lookup the column, assumed not
    *           <code>null</code> and to have been properly qualified.
    * @param column the column to lookup, assumed not <code>null</code>.
    * @param contentid the id of the content to lookup the column for.
    * @param revision the revision of the content to lookup the column for.
    * @param connection the connection to use for all database lookups, assumed
    *           valid.
    * @return the prepared statement, never <code>null</code>.
    * @throws SQLException if any SQL operation fails.
    */
   private PreparedStatement prepareContentStatement(String table,
         String column, int contentid, int revision, Connection connection)
         throws SQLException
   {
      // get the unqualified table name
      int sep = table.lastIndexOf('.');
      String unqualTable = sep == -1 ? table : table.substring(sep + 1);

      if(!SecureStringUtils.isValidTableOrColumnName(column))
         throw new IllegalArgumentException("Invalid column name.");

      if(!SecureStringUtils.isValidTableOrColumnName(unqualTable))
         throw new IllegalArgumentException("Invalid table name.");

      boolean useRevision = !unqualTable.equalsIgnoreCase("CONTENTSTATUS");
      String query = "SELECT " + column + " FROM " + table
            + " WHERE (CONTENTID=?";
      if (useRevision)
         query += " AND REVISIONID=?)";
      else
         query += ")";

      PreparedStatement statement = PSPreparedStatement.getPreparedStatement(
            connection, query);
      statement.clearParameters();
      statement.setInt(1, contentid);
      if (useRevision)
         statement.setInt(2, revision);

      return statement;
   }

   /**
    * '0' is reserved as the preview context, never <code>null</code>.
    */
   private static final String PREVIEW_CONTEXT = "0";
}
