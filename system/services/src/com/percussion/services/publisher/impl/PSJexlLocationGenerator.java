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
package com.percussion.services.publisher.impl;

import com.percussion.cms.PSCmsException;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.extension.IPSAssemblyLocation;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSExtensionErrors;
import com.percussion.extension.PSExtensionException;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.webservices.PSServerFolderProcessor;
import com.percussion.services.assembly.IPSAssemblyService;
import com.percussion.services.assembly.IPSAssemblyTemplate;
import com.percussion.services.assembly.PSAssemblyException;
import com.percussion.services.assembly.PSAssemblyServiceLocator;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.contentmgr.IPSContentMgr;
import com.percussion.services.contentmgr.PSContentMgrConfig;
import com.percussion.services.contentmgr.PSContentMgrLocator;
import com.percussion.services.contentmgr.PSContentMgrOption;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.guidmgr.data.PSLegacyGuid;
import com.percussion.services.sitemgr.IPSSiteManager;
import com.percussion.services.sitemgr.PSSiteHelper;
import com.percussion.services.sitemgr.PSSiteManagerException;
import com.percussion.services.sitemgr.PSSiteManagerLocator;
import com.percussion.services.utils.jexl.PSServiceJexlEvaluatorBase;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.jexl.IPSScript;
import com.percussion.utils.jexl.PSJexlEvaluator;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Generate a location using a jexl expression.
 * 
 * @author dougrand
 * 
 */
public class PSJexlLocationGenerator implements IPSAssemblyLocation
{
   private static final Logger ms_log = LogManager.getLogger(PSJexlLocationGenerator.class);

   /**
    * Options for the content manager - the options are static, so they are just
    * calculated once
    */
   public static PSContentMgrConfig ms_options = new PSContentMgrConfig();

   static
   {
      ms_options.addOption(PSContentMgrOption.LOAD_MINIMAL);
      ms_options.addOption(PSContentMgrOption.LAZY_LOAD_CHILDREN);
   }

   /**
    * This method creates a new publishing location string using the provided
    * parameters.
    * 
    * See {@link IPSAssemblyLocation#createLocation(Object[], IPSRequestContext)
    * createLocation} for details.
    * 
    * @param params array containing two required elements
    * 
    * param[0] is required. This is the JEXL expression to use when generating
    * the output. The JEXL expression may use the item node
    */
   @SuppressWarnings("unchecked")
   public String createLocation(Object[] params, IPSRequestContext request)
         throws PSExtensionException
   {
      // Requires one parameter. The parameter must be a String, and is
      // interpreted as a JEXL expression.
      // check the number of parameters provided is correct
      if ((params.length < 1) || (params[0] == null)
            || (!(params[0] instanceof String)))
      {
         Object[] args =
         {"" + 1, "" + params.length};
         throw new PSExtensionException(
               IPSExtensionErrors.EXT_PARAM_VALUE_MISMATCH, args);
      }
      String expression = (String) params[0];
      String cidstr = request.getParameter(IPSHtmlParameters.SYS_CONTENTID);
      String revstr = request.getParameter(IPSHtmlParameters.SYS_REVISION);
      String variantid = request.getParameter(IPSHtmlParameters.SYS_VARIANTID);
      String pagestr = request.getParameter("sys_page");
      Boolean usePageSuffix = "true".equalsIgnoreCase(request.getParameter(
            IPSHtmlParameters.SYS_USE_PAGE_SUFFIX, "false"));
      if (StringUtils.isBlank(cidstr))
      {
         throw new IllegalArgumentException(
               "the sys_contentid parameter may not be null or empty");
      }
      if (StringUtils.isBlank(revstr))
      {
         throw new IllegalArgumentException(
               "the sys_revision parameter may not be null or empty");
      }
      if (StringUtils.isBlank(variantid))
      {
         throw new IllegalArgumentException(
               "variantid may not be null or empty");
      }

      PSServiceJexlEvaluatorBase jexlEvaluator = new PSServiceJexlEvaluatorBase(
            true);

      String sitestr = request.getParameter(IPSHtmlParameters.SYS_SITEID);
      String origsitestr = request
            .getParameter(IPSHtmlParameters.SYS_ORIGINALSITEID);
      String contextstr = request.getParameter(IPSHtmlParameters.SYS_CONTEXT);
      
      if (StringUtils.isNotBlank(pagestr) && StringUtils.isNumeric(pagestr))
      {
         Integer page = new Integer(pagestr);
         jexlEvaluator.bind("$sys.page", page);
         if (page > 1 && usePageSuffix)
         {
            jexlEvaluator.bind("$sys.page_suffix", "_" + pagestr);
         }
         else
         {
            jexlEvaluator.bind("$sys.page_suffix", "");
         }
      }
      else
      {
         jexlEvaluator.bind("$sys.page_suffix", "");
      }
      
      if (StringUtils.isNotBlank(origsitestr) && !origsitestr.equals(sitestr))
      {
         jexlEvaluator.bind("$sys.crossSiteLink", true);
      }
      else
      {
         jexlEvaluator.bind("$sys.crossSiteLink", false);
      }

      try
      {
         PSSiteHelper.setupSiteInfo(jexlEvaluator, sitestr, contextstr);
      }
      catch (Exception e)
      {
         throw new PSExtensionException(
               IPSExtensionErrors.EXT_PARAM_VALUE_INVALID, e);
      }

      Iterator<Map.Entry<String, Object>> iter = request
            .getParametersIterator();
      Map<String, Object> parammap = new HashMap<>();
      while (iter.hasNext())
      {
         Map.Entry<String, Object> entry = iter.next();
         parammap.put(entry.getKey(), entry.getValue());
      }

      IPSAssemblyService asm = PSAssemblyServiceLocator.getAssemblyService();
      try
      {
         IPSAssemblyTemplate template = asm.loadUnmodifiableTemplate(variantid);
         if (StringUtils.isNotBlank(template.getLocationPrefix()))
         {
            jexlEvaluator.bind("$sys.template.prefix", template
                  .getLocationPrefix());
         }
         else
         {
            jexlEvaluator.bind("$sys.template.prefix", "");
         }
         if (StringUtils.isNotBlank(template.getLocationSuffix()))
         {
            jexlEvaluator.bind("$sys.template.suffix", template
                  .getLocationSuffix());
         }
         else
         {
            jexlEvaluator.bind("$sys.template.suffix", "");
         }
      }
      catch (PSAssemblyException e1)
      {
         throw new PSExtensionException(
               IPSExtensionErrors.EXT_PARAM_VALUE_INVALID, e1
                     );
      }

      jexlEvaluator.bind("$sys.params", parammap);

      IPSContentMgr cms = PSContentMgrLocator.getContentMgr();
      int contentid = Integer.parseInt(cidstr);
      int revision = Integer.parseInt(revstr);
      IPSGuid cguid = new PSLegacyGuid(contentid, revision);
      List<IPSGuid> guids = new ArrayList<>();
      String fidstr = request.getParameter(IPSHtmlParameters.SYS_FOLDERID);
      guids.add(cguid);
      boolean set_dummy_site_paths = false;
      try
      {
         List<Node> nodes = cms.findItemsByGUID(guids, ms_options);
         if (!nodes.isEmpty())
         {
            Node thenode = nodes.get(0);
            jexlEvaluator.bind("$sys.item", thenode);
            
            if (StringUtils.isBlank(fidstr))
            {
               IPSSiteManager smgr = PSSiteManagerLocator.getSiteManager();
               IPSGuid fid = smgr.getSiteFolderId(new PSGuid(PSTypeEnum.SITE,
                     sitestr), new PSLegacyGuid(contentid, revision));
               if (fid != null)
               {
                  PSLegacyGuid flg = (PSLegacyGuid) fid;
                  fidstr = Integer.toString(flg.getContentId());
               }
            }

            if (StringUtils.isNotBlank(fidstr))
            {
               PSServerFolderProcessor proc = PSServerFolderProcessor.getInstance();
               PSLocator folderLocator = new PSLocator(fidstr);
               String[] paths = proc.getItemPaths(folderLocator);
               if (paths != null && paths.length > 0)
               {
                  jexlEvaluator.bind("$sys.path", paths[0]);
               }

               String pubPath = getPublishPath(sitestr, folderLocator);
               if (StringUtils.isNotBlank(pubPath))
               {
                  jexlEvaluator.bind("$sys.pub_path", pubPath);
               }
               else
               {
                  jexlEvaluator.bind("$sys.pub_path", "/");
               }
            }
            else
            {
               jexlEvaluator.bind("$sys.pub_path", "/");
            }
         }
      }
      catch (RepositoryException e)
      {
         throw new PSExtensionException(
               IPSExtensionErrors.EXT_PARAM_VALUE_INVALID, e);
      }
      catch (PSNotFoundException | PSCmsException | PSSiteManagerException e)
      {
         set_dummy_site_paths = true;
      }

      if (set_dummy_site_paths)
      {
         jexlEvaluator.bind("$sys.pub_path", "/unknownpath/");
         jexlEvaluator.bind("$sys.path", "/unknownpath/");
      }

      try
      {
         IPSScript parsed = PSJexlEvaluator.createScript(expression);
         Object rval = jexlEvaluator.evaluate(parsed);
         if (rval == null)
         {
            throw new PSExtensionException(
                  IPSExtensionErrors.JEXL_WRONG_RETURN_TYPE, "null");
         }
         else if (!(rval instanceof String))
         {
            throw new PSExtensionException(
                  IPSExtensionErrors.JEXL_WRONG_RETURN_TYPE, "String");
         }
         else
         {
            return (String) rval;
         }
      }
      catch (org.apache.commons.jexl3.JexlException e)
      {
         // Output bindings to console to aid in debugging
         ms_log.error("Problem in evaluating, dumping bindings: \n {}"
               , jexlEvaluator.bindingsToString());
         return "/location-jexl-error/" + contextstr + "/" + cidstr +"-" + revstr;

      }
   }

   /**
    * Get the publishing path for the specified site and folder.
    * 
    * @param sitestr the id of the specified site, assumed not <code>null</code>
    *           or empty.
    * @param folder the locator of the specified folder, assumed not
    *           <code>null</code>.
    * 
    * @return the publishing path, may be <code>null</code> if failed to get
    *         the specified path.
    * 
    * @throws PSSiteManagerException if the specified folder does not exit under
    *            the specified site.
    */
   private String getPublishPath(String sitestr, PSLocator folder)
           throws PSSiteManagerException, PSNotFoundException {
      IPSGuid siteid = new PSGuid(PSTypeEnum.SITE, sitestr);
      IPSSiteManager sitemanager = PSSiteManagerLocator.getSiteManager();
      return sitemanager.getPublishPath(siteid, new PSLegacyGuid(folder));
   }

   /**
    * (non-Javadoc)
    * 
    * @see com.percussion.extension.IPSExtension#init(com.percussion.extension.IPSExtensionDef,
    *      java.io.File)
    */
   @SuppressWarnings("unused")
   public void init(IPSExtensionDef def, File codeRoot)
   {
      // No initialization required
   }
}
