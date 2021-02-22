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

import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.services.assembly.IPSAssemblyService;
import com.percussion.services.assembly.IPSAssemblyTemplate;
import com.percussion.services.assembly.PSAssemblyException;
import com.percussion.services.assembly.PSAssemblyServiceLocator;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.contentmgr.IPSContentPropertyConstants;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.guidmgr.data.PSLegacyGuid;
import com.percussion.services.publisher.IPSPublisherServiceErrors;
import com.percussion.services.publisher.IPSTemplateExpander;
import com.percussion.services.publisher.PSPublisherException;
import com.percussion.services.publisher.data.PSContentListItem;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.utils.guid.IPSGuid;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.query.QueryResult;
import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;

import org.apache.commons.lang.StringUtils;

/**
 * The base template expander services as a base class for writing template
 * expanders. Subclasses can simply implement
 * {@link #getCandidateTemplates(Map)}
 * 
 * @author dougrand
 */
public abstract class PSBaseTemplateExpander implements IPSTemplateExpander
{
   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.extension.IPSExtension#init(com.percussion.extension.IPSExtensionDef,
    *      java.io.File)
    */
   @SuppressWarnings("unused")
   public void init(IPSExtensionDef def, File codeRoot)
   {
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.services.publisher.IPSTemplateExpander#expand(javax.jcr.query.QueryResult,
    *      java.util.Map, java.util.Map)
    */
   public List<PSContentListItem> expand(QueryResult results,
         Map<String, String> parameters,
         Map<Integer, PSComponentSummary> summaryMap)
         throws PSPublisherException
   {
      if (results == null)
      {
         throw new IllegalArgumentException("results may not be null");
      }
      if (parameters == null || parameters.size() == 0)
      {
         throw new IllegalArgumentException(
               "parameters may not be null or empty");
      }
      String siteid = parameters.get(IPSHtmlParameters.SYS_SITEID);
      IPSGuid siteg = null;
      if (!StringUtils.isBlank(siteid))
      {
         siteg = new PSGuid(PSTypeEnum.SITE, siteid);
      }

      List<PSContentListItem> clist = new ArrayList<>();

      String ctx = parameters.get(IPSHtmlParameters.SYS_CONTEXT);
      String deliveryctx = parameters
            .get(IPSHtmlParameters.SYS_DELIVERY_CONTEXT);
      int context = 0;
      if (deliveryctx != null)
         context = Integer.parseInt(deliveryctx);
      else if (ctx != null)
         context = Integer.parseInt(ctx);
      else
         throw new RuntimeException(
               "Either sys_context or sys_delivery_context must be specified");
      try
      {
         List<IPSGuid> candidates = getCandidateTemplates(parameters);
         if (candidates.isEmpty())
            return clist;
         RowIterator riter = results.getRows();
         Map<IPSGuid, List<IPSGuid>> cache = new HashMap<>();
         while (riter.hasNext())
         {
            Row r = riter.nextRow();
            List<IPSGuid> templateids = findTemplates(r, candidates, cache,
                  parameters);
            int cid = (int) r.getValue(
                  IPSContentPropertyConstants.RX_SYS_CONTENTID).getLong();
            PSComponentSummary sum = summaryMap.get(cid);
            IPSGuid cguid = new PSLegacyGuid(cid, sum
                  .getPublicOrCurrentRevision());
            Value folderid = r.getValue("rx:sys_folderid");
            IPSGuid fid = null;
            if (folderid != null)
            {
               fid = new PSLegacyGuid((int) folderid.getLong(), 0);
            }
            for (IPSGuid tid : templateids)
            {
               clist.add(new PSContentListItem(cguid, fid, tid, siteg, context));
            }
         }
      }
      catch (RepositoryException e)
      {
         throw new PSPublisherException(
               IPSPublisherServiceErrors.RUNTIME_ERROR, e,
                  e.getLocalizedMessage());
      }

      return clist;
   }

   /**
    * Find the templates that may be used in the expander.
    * 
    * @param parameters the parameters for the expander, never <code>null</code>
    *           or empty. <code>siteid</code> must be present.
    * @return a list of candidates, never <code>null</code>, may be empty.
    * @throws PSPublisherException
    */
   protected abstract List<IPSGuid> getCandidateTemplates(
         Map<String, String> parameters) throws PSPublisherException;

   /**
    * Find the templates from the candidates that match the content type for the
    * given row
    * 
    * @param publishablerow the row, never <code>null</code>
    * @param candidates the candidate templates, never <code>null</code> or
    *           empty
    * @param cache a cache to use across calls that associates the content type
    *           with a particular set of template guids to use, never
    *           <code>null</code>.
    * @param parameters the parameters for the expander, never <code>null</code>
    *           or empty.
    * @return a list of templates to use in the expansion
    * @throws PSPublisherException
    */
   protected List<IPSGuid> findTemplates(Row publishablerow,
         List<IPSGuid> candidates, Map<IPSGuid, List<IPSGuid>> cache,
         Map<String, String> parameters) throws PSPublisherException
   {
      if (publishablerow == null)
      {
         throw new IllegalArgumentException("publishablerow may not be null");
      }
      if (parameters == null || parameters.size() == 0)
      {
         throw new IllegalArgumentException(
               "parameters may not be null or empty");
      }
      if (candidates == null || candidates.size() == 0)
      {
         throw new IllegalArgumentException(
               "candidates may not be null or empty");
      }
      if (cache == null)
      {
         throw new IllegalArgumentException("cache may not be null");
      }
      List<IPSGuid> rval = null;

      try
      {
         IPSGuid ctype = new PSGuid(PSTypeEnum.NODEDEF, publishablerow
               .getValue("rx:sys_contenttypeid").getLong());
         rval = cache.get(ctype);
         if (rval == null)
         {
            rval = new ArrayList<>();
            IPSAssemblyService asm = PSAssemblyServiceLocator
                  .getAssemblyService();
            for (IPSAssemblyTemplate t : asm.findTemplatesByContentType(ctype))
            {
               if (candidates.contains(t.getGUID()))
               {
                  rval.add(t.getGUID());
               }
            }
            cache.put(ctype, rval);
         }
      }
      catch (RepositoryException e)
      {
         throw new PSPublisherException(
               IPSPublisherServiceErrors.RUNTIME_ERROR, e,
                  e.getLocalizedMessage());
      }
      catch (PSAssemblyException e)
      {
         throw new PSPublisherException(
               IPSPublisherServiceErrors.RUNTIME_ERROR, e,
                  e.getLocalizedMessage());
      }

      return rval;
   }

}
