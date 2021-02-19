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
package com.percussion.services.publisher.impl;

import com.percussion.services.assembly.IPSAssemblyTemplate;
import com.percussion.services.assembly.IPSAssemblyTemplate.OutputFormat;
import com.percussion.services.assembly.IPSAssemblyTemplate.PublishWhen;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.publisher.IPSPublisherServiceErrors;
import com.percussion.services.publisher.PSPublisherException;
import com.percussion.services.sitemgr.IPSSite;
import com.percussion.services.sitemgr.IPSSiteManager;
import com.percussion.services.sitemgr.PSSiteManagerLocator;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.utils.guid.IPSGuid;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The site template expander adds the appropriate always publish page templates
 * for a given site to the content list.
 * 
 * @author dougrand
 */
public class PSSiteTemplateExpander extends PSBaseTemplateExpander
{

   private enum DefaultTemplateType {
      /**
       * No publish when default templates should be chosen
       */
      NONE,
      /**
       * Only default templates pointing using the dispatch plugin should be
       * chosen
       */
      DISPATCH,
      /**
       * All publish when default templates should be chosen (default behavior)
       */
      ALL;
   }

   /**
    * This parameter specifies the behavior with regards to publish when default
    * templates
    */
   private static final String DEFAULT_TEMPLATE = "default_template";

   @Override
   protected List<IPSGuid> getCandidateTemplates(Map<String, String> parameters)
         throws PSPublisherException
   {
      String siteid = parameters.get(IPSHtmlParameters.SYS_SITEID);
      if (StringUtils.isBlank(siteid))
      {
         throw new IllegalArgumentException("siteid is a required parameter");
      }
      String dt = parameters.get(DEFAULT_TEMPLATE);
      DefaultTemplateType dtype = DefaultTemplateType.ALL;
      if (StringUtils.isNotBlank(dt))
      {
         dt = dt.toUpperCase();
         dtype = DefaultTemplateType.valueOf(dt);
      }
      List<IPSGuid> candidates = new ArrayList<>();
      IPSSiteManager smgr = PSSiteManagerLocator.getSiteManager();
      IPSGuid siteg = new PSGuid(PSTypeEnum.SITE, siteid);
      IPSSite site = null;
      try
      {
         site = smgr.loadUnmodifiableSite(siteg);
      }
      catch (PSNotFoundException e)
      {
         throw new PSPublisherException(IPSPublisherServiceErrors.SITE_LOAD, e,
               siteg);
      }

      Log log = LogFactory.getLog(getClass()); 
      if (log.isDebugEnabled())
      {
         log.debug("Site template expander found "
            + site.getAssociatedTemplates().size()
            + " associated templates before filtering");
      }
      for (IPSAssemblyTemplate t : site.getAssociatedTemplates())
      {
         if ((t.getOutputFormat().equals(OutputFormat.Page) || 
               t.getOutputFormat().equals(OutputFormat.Binary)))
         {
            PublishWhen pw = t.getPublishWhen();
            String plugin = t.getAssembler();
            // Always include pw always
            // Default is included if the parameter is all or
            // if the parameter is dispatch and the plugin is dispatch
            if (pw.equals(PublishWhen.Always)
                  || (pw.equals(PublishWhen.Default) && 
                        (dtype.equals(DefaultTemplateType.ALL) || 
                              (dtype.equals(DefaultTemplateType.DISPATCH) 
                                    && plugin.endsWith("dispatchAssembler")))))
            {
               candidates.add(t.getGUID());
            }
         }
      }
      if (log.isDebugEnabled())
      {
         log.debug("Site template expander retained "
            + candidates.size()
            + " templates after filtering");
      }
      return candidates;
   }
}
