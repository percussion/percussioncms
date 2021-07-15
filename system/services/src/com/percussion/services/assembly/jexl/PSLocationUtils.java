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
package com.percussion.services.assembly.jexl;

import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.cms.objectstore.server.PSRelationshipProcessor;
import com.percussion.data.PSConversionException;
import com.percussion.deploy.server.PSJexlHelper;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.extension.IPSJexlMethod;
import com.percussion.extension.IPSJexlParam;
import com.percussion.extension.PSJexlUtilBase;
import com.percussion.server.PSRequest;
import com.percussion.server.webservices.PSServerFolderProcessor;
import com.percussion.services.assembly.IPSAssemblyErrors;
import com.percussion.services.assembly.IPSAssemblyItem;
import com.percussion.services.assembly.IPSAssemblyService;
import com.percussion.services.assembly.IPSAssemblyTemplate;
import com.percussion.services.assembly.IPSAssemblyTemplate.PublishWhen;
import com.percussion.services.assembly.PSAssemblyException;
import com.percussion.services.assembly.PSAssemblyServiceLocator;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.contentmgr.IPSNode;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.guidmgr.data.PSLegacyGuid;
import com.percussion.services.legacy.IPSCmsObjectMgr;
import com.percussion.services.legacy.PSCmsObjectMgrLocator;
import com.percussion.services.publisher.data.PSContentListItem;
import com.percussion.services.sitemgr.IPSSiteManager;
import com.percussion.services.sitemgr.PSSiteManagerLocator;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.utils.codec.PSXmlEncoder;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.jexl.IPSScript;
import com.percussion.utils.jexl.PSJexlEvaluator;
import com.percussion.utils.request.PSRequestInfo;
import com.percussion.utils.timing.PSStopwatchStack;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.jcr.Node;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Functions for use by jexl to calculate locations in assembly. Note that these
 * functions use annotations for documentation instead of javadoc. The
 * annotations are read by the extensions manager and presented to the workbench
 * 
 * @author dougrand
 */
public class PSLocationUtils extends PSJexlUtilBase
{
   /**
    * An extension wrapper. Extension wrappers
    * enable the code to call the UDF in a straightforward fashion.
    */
   private PSExtensionWrapper m_genpub = new PSExtensionWrapper(
         "global/percussion/contentassembler/", "sys_casGeneratePubLocation");

   /**
    * An extension wrapper. Extension wrappers
    * enable the code to call the UDF in a straightforward fashion.
    */
   private PSExtensionWrapper m_sitebase = new PSExtensionWrapper(
         "global/percussion/assemblers/", "sys_casGetSiteBaseUrl");

   /**
    * The value for sys_mode that indicates that the generated preview
    * link should include sys_command=editrc.
    */
   public final static String AA_LINK = "AA_Link";
   
   /**
    * Static expression for page
    */
   private static final IPSScript PAGE = 
      PSJexlHelper.createStaticExpression("$sys.page");
   private static final IPSScript TITLE = PSJexlHelper.createStaticExpression("$sys.metadata.title");
   private static final IPSScript ALT = PSJexlHelper.createStaticExpression("$sys.metadata.alt");

   
   /**
    * generate a url from the parameters. The resulting url will be escaped 
    * for use in xhtml/xml.
    * 
    * @param targetItem the target assembly item, never <code>null</code>.
    * @param targetTemplate the name, ID or the object of the target template, 
    *    may not <code>null</code>.
    * @param page the page number, it may be <code>0</code> or <code>null</code>
    *    indicate no page. This is the page number of current assembled item, 
    *    which may or may not be the <code>targetItem</code>.
    * 
    * @return the generated URL.
    */
   @IPSJexlMethod(description = "generate a url from the parameters. The resulting url will be escaped for use in xhtml/xml.", params =
   {
         @IPSJexlParam(name = "targetItem", description = "the target assembly item"),
         @IPSJexlParam(name = "targetTemplateName", description = "the target tempate's name"),
         @IPSJexlParam(name = "page", description = "the page number, 0 or null indicates no page")})
   public String generate(IPSAssemblyItem targetItem, Object targetTemplate, Number page)
   {
      return generateTargetInfo(targetItem, targetTemplate, page, Boolean.FALSE).getUrl();
   }
   
   /**
    * generate information about the target item from the parameters. 
    * Along with other details, this will provide the 'title' and 'alt' text for the inline types 'link' and 'image'. 
    * 
    * @param targetItem the target assembly item, assumed not <code>null</code>.
    * @param targetTemplate the name, ID or the object of the target template, 
    *    assumed not <code>null</code>.
    * @param page the page number, it may be <code>0</code> or <code>null</code>
    *           indicate no page. This is the page number of current assembled
    *           item, which may or may not be the <code>targetItem</code>.
    * @param usePageSuffix the binding value of $sys.usePageSuffix. It is
    *    <code>null</code> if the target item is paginated.  
    * 
    * @return the information object which has data about the target.
    */
   @IPSJexlMethod(description = "generate an information object from the parameters.", params =
       {@IPSJexlParam(name = "targetItem", description = "the target assembly item"),
             @IPSJexlParam(name = "targetTemplateName", description = "the target tempate's name"),
             @IPSJexlParam(name = "page", description = "the page number, 0 or null indicates no page"),
             @IPSJexlParam(name = "usePageSuffix", description = "should the page suffix be used")})
   public PSLinkTargetInfo generateTargetInfo(IPSAssemblyItem targetItem, Object targetTemplate, Number page, Boolean usePageSuffix)
   {

      PSStopwatchStack sws = PSStopwatchStack.getStack();
      sws.start(getClass().getName() + "#generate");
      PSLinkTargetInfo targetInfo = new PSLinkTargetInfo();
      PSRequest req = (PSRequest) PSRequestInfo
            .getRequestInfo(PSRequestInfo.KEY_PSREQUEST);
      String oldUsePageSuffix = req.getParameter(
            IPSHtmlParameters.SYS_USE_PAGE_SUFFIX, "false");
      try
      {
         if (targetItem == null)
         {
            throw new IllegalArgumentException("targetItem may not be null");
         }
         // set the node object in the targetInfo object
         targetInfo.setNode(targetItem.getNode());
         
         if (targetTemplate == null)
         {
            throw new IllegalArgumentException("targetTemplate may not be null");
         }

         long templateId = 0;
         PSLegacyGuid targetItemLG = (PSLegacyGuid) targetItem.getId();

         templateId = lookupTemplateId(targetTemplate, targetItemLG, targetItem);
         
         //set templateId in the targetInfo object
         targetInfo.setTemplateId(new PSGuid(templateId));

         PSJexlEvaluator eval = new PSJexlEvaluator(targetItem.getBindings());
         
         if (page == null)
         {
            page = (Number) eval.evaluate(PAGE);
         }
         
         if (page != null && page.intValue() == 0)
         {
            page = null;
         }

         // set page in the targetInfo object
         targetInfo.setPage(page);
         
         // set the title in the targetInfo object
         String title = (String)eval.evaluate(TITLE);
         if(title == null || title.isEmpty()) {
             if(targetItem.getNode().hasProperty("displaytitle")) {
                 targetInfo.setTitle(targetItem.getNode().getProperty("displaytitle").getValue().getString());
             } else {
                 targetInfo.setTitle("");
             }
         } else {
             targetInfo.setTitle(title);
         }
         
         // set alt text for an image in the targetInfo object
         String alt = (String)eval.evaluate(ALT);
         if(alt == null || alt.isEmpty()) {
             if(targetItem.getNode().hasProperty("img_alt")) {
                 targetInfo.setAlt(targetItem.getNode().getProperty("img_alt").getValue().getString());
             } else {
                 targetInfo.setAlt("");
             }
         } else {
             targetInfo.setAlt(alt);
         }
         
         req.setParameter(IPSHtmlParameters.SYS_USE_PAGE_SUFFIX,
               usePageSuffix ? "true" : "fales");

         IPSGuid siteId = targetItem.getSiteId();
         targetInfo.setSiteId(siteId); // set siteId in the targetInfo object
         String folderId = targetItem.getParameterValue(
               IPSHtmlParameters.SYS_FOLDERID, null);
         String context = targetItem.getParameterValue(
               IPSHtmlParameters.SYS_CONTEXT, null);
         String origSiteId = targetItem.getParameterValue(
               IPSHtmlParameters.SYS_ORIGINALSITEID, null);
         String command = targetItem.getParameterValue(
               IPSHtmlParameters.SYS_COMMAND, null);
         String mode = targetItem.getParameterValue(IPSHtmlParameters.SYS_MODE,
               null);
         if (StringUtils.isNotBlank(mode) && StringUtils.isBlank(command) &&
               mode.equals(AA_LINK))
         {
            command = IPSHtmlParameters.SYS_ACTIVE_ASSEMBLY;
         }

         if (folderId == null && siteId != null)
         {
            IPSSiteManager smgr = PSSiteManagerLocator.getSiteManager();
            IPSGuid fid = smgr.getSiteFolderId(siteId, targetItem.getId());
            if (fid != null)
            {
               PSLegacyGuid flg = (PSLegacyGuid) fid;
               folderId = Integer.toString(flg.getContentId());
            }
         }

         // set folderId in the targetInfo object
         targetInfo.setFolderId(folderId);
         PSXmlEncoder enc = new PSXmlEncoder();
         if (origSiteId != null)
         {
            req.setParameter(IPSHtmlParameters.SYS_ORIGINALSITEID, origSiteId);
         }
         if (command != null)
         {
            req.setParameter(IPSHtmlParameters.SYS_COMMAND, command);
         }
         String siteIdValue = (siteId == null ? null : Long.toString(siteId.longValue()));
         String filter = targetItem.getFilter() == null ? null : targetItem.getFilter().getName();
         String location = (String) enc.encode(m_genpub.call(req, templateId, targetItemLG
               .getContentId(), targetItemLG.getRevision(), context, siteIdValue,
               folderId, null, filter, page));
         
         targetInfo.setUrl(location);
         
         return targetInfo;
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
      finally
      {
         req.setParameter(IPSHtmlParameters.SYS_USE_PAGE_SUFFIX,
               oldUsePageSuffix);
         sws.stop();
      }
   }

   /**
    * Finds the Rhythmyx virtual path (//Site/) of JCR-170 node and picks the 
    * first one found (if the item/node exists in more than one folder).
    * 
    * @param node the JCR-170 node or the item in question, assumed not 
    *    <code>null</code>.
    * @param isFolderPath <code>true</code> if requesting a folder path for the
    *    given item; otherwise requesting the item path.
    *    
    * @return the requested path. It never <code>null</code>, but may be empty
    *    if the item does not exist or it does not exist in any folder.
    */
   private String getPath(Node node, boolean isFolderPath)
   {

      IPSNode psnode = (IPSNode) node;
      IPSGuid guid = psnode.getGuid();
      PSLegacyGuid lg_id = new PSLegacyGuid(guid.longValue());

      String path = "";

      PSRequest req = PSRequest.getContextForRequest();
      try
      {
         PSServerFolderProcessor fproc =PSServerFolderProcessor.getInstance();
         String paths[] = null;
         if (isFolderPath) {
            paths = fproc.getFolderPaths(lg_id.getLocator());
         }
         else {
            try{
            paths = fproc.getItemPaths(lg_id.getLocator());
            } catch (PSNotFoundException e) {
               ms_log.warn(e.getMessage());
               ms_log.debug(e.getMessage(),e);
            }
         }

         if (paths != null && paths.length >= 1)
         {
            path = paths[0];
         }
         
         return path;
      }
      catch (PSCmsException e)
      {
         throw new RuntimeException(e);
      }

   }

   /**
    * Finds the Rhythmyx virtual folder path for the given (JCR-170) node and 
    * picks the first one found (if the item/node exists in more than one 
    * folder). Note, the folder path is the path from the root to the parent
    * folder of the node, but does not include the node itself.
    * 
    * @param node the JCR-170 node or the item in question, never 
    *    <code>null</code>.
    *    
    * @return the requested path. It never <code>null</code>, but may be empty
    *    if the item does not exist or it does not exist in any folder.
    */
   @IPSJexlMethod(description = "get Rhythmyx folder path for a given node. The resulting path is from the root to the parent of the node, but does not include the node itself. It is empty if the node does not exist or does not exist in any folder", params =
   {
         @IPSJexlParam(name = "node", type = "javax.jcr.Node", description = "the node to get folder path from")
   })
   public String folderPath(Node node)
   {
      if (node == null)
         throw new IllegalArgumentException("node may not be null.");
      
      return getPath(node, true);
   }

   /**
    * Finds the Rhythmyx virtual path for the given (JCR-170) node and 
    * picks the first one found (if the item/node exists in more than one 
    * folder). Note, the path is the path from the root to the node itself.
    * 
    * @param node the JCR-170 node or the item in question, never 
    *    <code>null</code>.
    *    
    * @return the requested path. It never <code>null</code>, but may be empty
    *    if the item does not exist or it does not exist in any folder.
    */
   @IPSJexlMethod(description = "get Rhythmyx path for a given node. The resulting path is from the root to the node itself. It is empty if the node does not exist or does not exist in any folder", params =
   {
         @IPSJexlParam(name = "node", type = "javax.jcr.Node", description = "the node to get the path from")
   })
   public String path(Node node)
   {
      if (node == null)
         throw new IllegalArgumentException("node may not be null.");
      
      return getPath(node, false);
   }


   /**
    * Get the containing folder of the passed node
    * 
    * @param node the node is assumed not <code>null</code>
    * @return a guid of the parent folder (the first if there are multiple), or
    *         <code>null</code> if no parent folder is found
    */
   @SuppressWarnings("unchecked")
   public IPSGuid folderId(Node node)
   {
      try
      {
         if (node == null)
         {
            throw new IllegalArgumentException("node may not be null");
         }
         PSRequest req = PSRequest.getContextForRequest();
         PSRelationshipProcessor processor = PSRelationshipProcessor.getInstance();
         IPSNode cn = (IPSNode) node;
         PSLegacyGuid lg = (PSLegacyGuid) cn.getGuid();
         List<PSLocator> folders = processor.getParents(
               PSRelationshipConfig.TYPE_FOLDER_CONTENT, lg.getLocator());
         if (folders == null || folders.size() == 0)
            return null;
         else
            return new PSLegacyGuid(folders.get(0));
      }
      catch (PSCmsException e)
      {
         throw new RuntimeException(e);
      }
   }

   /**
    * Find the template id for given target information for a particular content
    * item.
    * 
    * @param targetTemplate the template info, may be a string or number, but
    *           never <code>null</code>
    * @param targetItemLG the guid of the target item, never <code>null</code>
    * @param item if known, the target assembly item, may be <code>null</code>
    * @return the template id
    * @throws PSAssemblyException
    */
   private long lookupTemplateId(Object targetTemplate,
         PSLegacyGuid targetItemLG, IPSAssemblyItem item)
         throws PSAssemblyException
   {
      if (item == null)
      {
         IPSAssemblyService asm = PSAssemblyServiceLocator.getAssemblyService();
         item = asm.getCurrentAssemblyItem();
      }

      if (targetTemplate instanceof String)
      {
         String targetTemplateName = (String) targetTemplate;
         if (StringUtils.isBlank(targetTemplateName))
         {
            throw new IllegalArgumentException(
                  "targetTemplate string may not be empty");
         }
         IPSAssemblyService asm = PSAssemblyServiceLocator.getAssemblyService();
         IPSCmsObjectMgr cms = PSCmsObjectMgrLocator.getObjectManager();
         PSComponentSummary sum = cms.loadComponentSummary(targetItemLG
               .getContentId());
         if (sum == null)
         {
            throw new IllegalStateException(
                  "No component summary found for content id "
                        + targetItemLG.getContentId());
         }
         //change findTemplateByNameAndType to findTemplateByName.
         //for new CMlite publishing changes
         //Adam/Jb/Luis
         IPSAssemblyTemplate temp = null;
         if (item != null)
         {
            temp = asm.findTemplateByName(targetTemplateName);
         }
         else
         {
            temp = asm.findTemplateByName(targetTemplateName);
         }
         if (temp != null)
         {
            return temp.getGUID().longValue();
         }
      }
      else if (targetTemplate instanceof Number)
      {
         return ((Number) targetTemplate).longValue();
      }
      else if (targetTemplate instanceof IPSAssemblyTemplate)
      {
         return ((IPSAssemblyTemplate) targetTemplate).getGUID().longValue();
      }
      throw new IllegalStateException("Could not find a template for info "
            + targetTemplate + " and content id " + targetItemLG.getContentId());
   }

   /**
    * Generate path
    * 
    * @param targetItem
    * @return a path string, from the generate pub location udf
    */
   @IPSJexlMethod(description = "generate a default template url from the target item. The resulting url will be escaped for use in xhtml/xml.", params =
   {@IPSJexlParam(name = "targetItem", description = "the target assembly item")})
   public String generate(IPSAssemblyItem targetItem)
   {
      try
      {
         return generate(targetItem, findDefaultTemplate(targetItem), 
               targetItem.getPage());
      }
      catch (PSAssemblyException e)
      {
         throw new RuntimeException(e);
      }
   }
   
   /**
    * Generate a URL/location for a particular page of a paginated item. 
    * The resulting url will be escaped for use in xhtml/xml.
    * 
    * @param paginatedItem the paginated assembly item, never <code>null</code>.
    * @param targetTemplate the name, ID or the object of the target template, 
    *    may not <code>null</code>.
    * @param page the page number of the paginated item, it may be 
    *    <code>0</code> or <code>null</code> indicate no page. 
    * 
    * @return the generated URL/location.
    */
   @IPSJexlMethod(description = "Generate a url for a particular page of a paginated item. The resulting url will be escaped for use in xhtml/xml.", params =
   {
      @IPSJexlParam(name = "paginatedItem", description = "the paginated assembly item"),
      @IPSJexlParam(name = "targetTemplateName", description = "the target tempate's name"),
      @IPSJexlParam(name = "page", description = "the page number, 0 or null indicates no page")})
   public String generateToPage(IPSAssemblyItem paginatedItem,
         Object targetTemplate, Number page)
   {
      return generateTargetInfo(paginatedItem, targetTemplate, page, Boolean.TRUE).getUrl();
   }

   /**
    * The same as {@link #generateToPage(IPSAssemblyItem, Object, Number)}, 
    * except the location is generated with the default template.
    * 
    * @param paginatedItem the paginated assembly item, never <code>null</code>.
    * @param page the page number of the paginated item, it may be 
    *    <code>0</code> or <code>null</code> indicate no page. 
    *    
    * @return a path string, from the generate pub location udf
    */
   @IPSJexlMethod(description = "Generate a default template url for a particular page of a paginated item. The resulting url will be escaped for use in xhtml/xml.", params =
   {@IPSJexlParam(name = "paginatedItem", description = "the paginated assembly item"),
         @IPSJexlParam(name = "page", description = "the page number")})
   public String generateToPage(IPSAssemblyItem paginatedItem, Number page)
   {
      try
      {
         return generateToPage(paginatedItem, findDefaultTemplate(paginatedItem),
               page);
      }
      catch (PSAssemblyException e)
      {
         throw new RuntimeException(e);
      }
   }   

   /**
    * generate a url from the parameters. The resulting url will be escaped 
    * for use in xhtml/xml.
    *  
    * @param targetItem the target assembly item, never <code>null</code>.
    * @param targetTemplate the name, ID or the object of the target template, 
    *    may not <code>null</code>.
    *    
    * @return a path string, from the generate pub location udf
    */
   @IPSJexlMethod(description = "generate a url from the parameters. The resulting url will be escaped for use in xhtml/xml.", params =
   {
         @IPSJexlParam(name = "targetItem", description = "the target assembly item"),
         @IPSJexlParam(name = "targetTemplateName", description = "the name or ID of the target tempate")})
   public String generate(IPSAssemblyItem targetItem, Object targetTemplate)
   {
      return generate(targetItem, targetTemplate, targetItem.getPage());
   }

   /**
    * Finds the default template for an assembly item. In order to maintain
    * backward compatibility and ease of use, a specific process is used to
    * determine the default template.
    * <p>
    * In general a template is default if its publishwhen attribute is set to
    * default. i.e. template.getPublishWhen() ==
    * IPSAssemblyTemplate.PublishWhen.Default
    * <p>
    * The ideal case is if the site id is provided and there is atleast one
    * default template associated with the site. If this is the case then a
    * template is chosen from this set.
    * <p>
    * If the site id is not provided then a template is chosen from the set of
    * all default templates associated with the item's content type.
    * <p>
    * If the site id is provided and there are no default templates associated
    * with the site and assembly item's content type then an Assembly exception
    * is thrown.
    * <p>
    * All other cases result in an assembly exception.
    * 
    * @param item The assembly item
    * @return The default template (IPSAssemblyTemplate)
    * @throws PSAssemblyException
    */
   @SuppressWarnings("unchecked")
   public IPSAssemblyTemplate findDefaultTemplate(IPSAssemblyItem item)
         throws PSAssemblyException
   {
      /*
       * We want to find the templates that are associated with the given
       * content type and site and publish when default.
       * 
       * To do this we take the intersection of the set templates associated
       * with a content type and the set of template associated with a site and
       * then filter out the ones that are not publish when default.
       * 
       * This may turn into a performance issue in which case Hibernate will
       * have to be used.
       * 
       */
      if (item == null)
      {
         throw new IllegalArgumentException("item may not be null");
      }

      IPSSiteManager sitem = PSSiteManagerLocator.getSiteManager();

      IPSGuid ctype = getContentTypeId(item);
      IPSGuid siteid = item.getSiteId();
      List<IPSAssemblyTemplate> ct_templates = findTemplatesByContentType(ctype);

      // Set<IPSAssemblyTemplate> ct_templates = new TreeSet
      // <IPSAssemblyTemplate> (ct_templates_list);
      /*
       * It would be nice if we could guarentee no duplicate templates but
       * IPSAssemblyTemplate does not have an equals method.
       */
      Collection<IPSAssemblyTemplate> ct_default_templates = new ArrayList<>();
      Collection<IPSAssemblyTemplate> site_templates = new ArrayList<>();
      Collection<IPSAssemblyTemplate> rvalues = null;

      if (siteid != null)
      {
         try {
            site_templates = sitem.loadUnmodifiableSite(siteid).getAssociatedTemplates();
         } catch (PSNotFoundException e) {
            ms_log.error(e.getMessage());
            ms_log.debug(e.getMessage(),e);
         }

         if (site_templates != null && !site_templates.isEmpty())
         {
            ms_log.debug("No Templates Associated with the site.");
         }
         // Copy collection to avoid modifying the underlying set
         Set<IPSAssemblyTemplate> copySet = new HashSet<>();
         copySet.addAll(site_templates);
         site_templates = copySet; // Decouple
      }

      for (IPSAssemblyTemplate c_t : ct_templates)
      {
         if (c_t.getPublishWhen().equals(PublishWhen.Default))
         {
            ct_default_templates.add(c_t);
         }
      }

      Predicate pred = new Predicate()
      {
         public boolean evaluate(Object input)
         {
            IPSAssemblyTemplate t = (IPSAssemblyTemplate) input;
            return !(t.getOutputFormat() == IPSAssemblyTemplate.OutputFormat.Snippet
                || t.getOutputFormat() == IPSAssemblyTemplate.OutputFormat.Global);
         }
      };

      /* Keep templates that are not snippets or globals */
      CollectionUtils.filter(site_templates, pred);
      CollectionUtils.filter(ct_templates, pred);
      CollectionUtils.filter(ct_default_templates, pred);

      Collection<IPSAssemblyTemplate> site_default_templates = CollectionUtils
            .intersection(ct_default_templates, site_templates);
      // Collection <IPSAssemblyTemplate> ct_site_templates =
      // CollectionUtils.intersection(ct_templates,site_templates);

      if (ct_default_templates.size() < 1)
      {
         ms_log
               .warn("No default templates associated with the item's content type.");
      }

      if (ct_templates.size() < 1)
      {
         // Should probably throw appropriate error here
         String error_msg = "no templates associated with this content type.";
         ms_log.error(error_msg);
         throw new PSAssemblyException(
               IPSAssemblyErrors.NO_DEFAULT_TEMPLATE, item.getPath(), ctype,
               error_msg);
      }
      else if (site_default_templates.size() > 0)
      {
         ms_log.debug("Using the set of default page templates associated "
               + "with the given content type and site.");
         rvalues = site_default_templates;
      }
      else if (siteid != null)
      {
         String error_msg = "no default template could be found for site id = "
               + siteid.toString();
         ms_log.error(error_msg);
         throw new PSAssemblyException(
               IPSAssemblyErrors.NO_DEFAULT_TEMPLATE, item.getPath(), ctype,
               error_msg);
      }
      else if (ct_default_templates.size() > 0)
      {
         ms_log.warn("Site Id not specified in locating default templates.");
         ms_log.debug("Using the set of default page templates "
               + "just associated with given content type.");
         rvalues = ct_default_templates;
      }
      else
      {
         String error_msg = "Error in logic occurred";
         ms_log.error(error_msg);
         throw new PSAssemblyException(
               IPSAssemblyErrors.NO_DEFAULT_TEMPLATE, item.getPath(), ctype,
               error_msg);
      }

      if (rvalues.size() > 1)
      {
         List<IPSAssemblyTemplate> sorted_templates = new ArrayList<>(
               rvalues);
         Comparator cmp = new Comparator()
         {
            public int compare(Object o1, Object o2)
            {
               IPSAssemblyTemplate t1 = (IPSAssemblyTemplate) o1;
               IPSAssemblyTemplate t2 = (IPSAssemblyTemplate) o2;
               return t1.getName().compareToIgnoreCase(t2.getName());
            }

            @Override
            public int hashCode()
            {
               return 0;
            }

            @Override
            public boolean equals(@SuppressWarnings("unused")
            Object o)
            {
               return false;
            }
         };
         Collections.sort(sorted_templates, cmp);

         rvalues = sorted_templates;
         List<String> template_names = new ArrayList<>();
         for (IPSAssemblyTemplate t : rvalues)
         {
            template_names.add(t.getName());
         }
         String choosen_template = template_names.get(0);
         ms_log.warn("Found multiple templates: " + template_names.toString()
               + " picking: " + choosen_template);
      }

      return rvalues.iterator().next();
   }

   /**
    * Get the content type id of the given assembly item's target
    * 
    * @param item the item
    * @return the guid of the content type, never <code>null</code>
    */
   private IPSGuid getContentTypeId(IPSAssemblyItem item)
   {
      PSLegacyGuid itemLG = (PSLegacyGuid) item.getId();
      IPSCmsObjectMgr cms = PSCmsObjectMgrLocator.getObjectManager();
      PSComponentSummary sum = cms.loadComponentSummary(itemLG.getContentId());
      return new PSGuid(PSTypeEnum.NODEDEF, sum.getContentTypeId());
   }

   /**
    * Logger for location utils
    */
   Logger ms_log = LogManager.getLogger(this.getClass());

   /**
    * Calculate the base site path by calling the existing UDF
    * 
    * @param siteid the siteid
    * @param modify
    * @return the path
    */
   @IPSJexlMethod(description = "get the base URL string for a given site.", params =
   {
         @IPSJexlParam(name = "siteid", description = "the target site"),
         @IPSJexlParam(name = "modify", description = "if true the url is stripped to not include the protocol, host or port")}, returns = "String")
   public String siteBase(String siteid, String modify)
   {
      try
      {
         return (String) m_sitebase.call(siteid, modify);
      }
      catch (PSConversionException e)
      {
         throw new RuntimeException(e);
      }
   }

   /**
    * General location path call
    * 
    * @param templateinfo
    * @param item
    * @param folderPath
    * @param filter
    * @param siteid
    * @param context
    * @return a path string, from the generate pub location udf
    */
   @IPSJexlMethod(description = "get the base URL string for a given site.  The resulting url will be escaped for use in xhtml/xml.", params =
   {
         @IPSJexlParam(name = "templateinfo", description = "the template name or id"),
         @IPSJexlParam(name = "item", description = "the content item"),
         @IPSJexlParam(name = "folderPath", description = "the folder path, including the immediate parent folder"),
         @IPSJexlParam(name = "filter", description = "the name of the item filter to use"),
         @IPSJexlParam(name = "siteid", type = "Number", description = "the id of the site"),
         @IPSJexlParam(name = "context", type = "Number", description = "the context id")}, returns = "String")
   public String generate(Object templateinfo, Node item, String folderPath,
         String filter, Number siteid, Number context)
   {
      if (templateinfo == null)
      {
         throw new IllegalArgumentException("templateinfo may not be null");
      }
      if (item == null)
      {
         throw new IllegalArgumentException("item may not be null");
      }
      if (context == null)
      {
         throw new IllegalArgumentException("context may not be null");
      }
      if (StringUtils.isBlank(folderPath))
      {
         throw new IllegalArgumentException(
               "folderPath may not be null or empty");
      }
      if (StringUtils.isBlank(filter))
      {
         throw new IllegalArgumentException("filter may not be null or empty");
      }
      IPSNode psnode = (IPSNode) item;
      PSLegacyGuid guid = (PSLegacyGuid) psnode.getGuid();

      PSStopwatchStack sws = PSStopwatchStack.getStack();
      sws.start(getClass().getName() + "#generate");
      try
      {
         long templateid = lookupTemplateId(templateinfo, guid, null);
         long site = siteid != null ? siteid.longValue() : 0;
         int ctx = context != null ? context.intValue() : 0;

         PSServerFolderProcessor proc = PSServerFolderProcessor.getInstance();
         PSComponentSummary folder = proc.getSummary(folderPath);
         if (folder == null)
         {
            throw new IllegalArgumentException("Specified folder " + folderPath
                  + " not found");
         }
         int folderId = folder.getContentId();

         PSXmlEncoder enc = new PSXmlEncoder();
         return (String) enc.encode(m_genpub.call(templateid, guid
               .getContentId(), guid.getRevision(), ctx, site, folderId, null,
               filter));
      }
      catch (PSConversionException e)
      {
         throw new RuntimeException(e);
      }
      catch (PSAssemblyException e)
      {
         throw new RuntimeException(e);
      }
      catch (PSCmsException e)
      {
         throw new RuntimeException(e);
      }
      finally
      {
         sws.stop();
      }
   }

   /**
    * Generate for a content list item
    * 
    * @param item
    * @return a path string, from the generate pub location udf
    */
   @IPSJexlMethod(description = "generate a url from the parameters", params =
   {@IPSJexlParam(name = "targetItem", description = "the target content list item")})
   public String generate(PSContentListItem item)
   {
      PSLegacyGuid targetItemLG = (PSLegacyGuid) item.getItemId();
      long folderId = item.getFolderId() != null ? item.getFolderId()
            .longValue() : 0;
      long siteid = item.getSiteId() != null ? item.getSiteId().longValue() : 0;

      PSStopwatchStack sws = PSStopwatchStack.getStack();
      sws.start(getClass().getName() + "#generate");
      try
      {
         return (String) m_genpub.call(item.getTemplateId().longValue(),
               targetItemLG.getContentId(), targetItemLG.getRevision(), item
                     .getContext(), siteid, folderId);
      }
      catch (PSConversionException e)
      {
         throw new RuntimeException(e);
      }
      finally
      {
         sws.stop();
      }
   }

   /**
    * Search the item's properties for the first property in the list that is
    * defined. If none are defined then return the default value
    * 
    * @param item the item, never <code>null</code>
    * @param propertylist a comma separated list of property names, never
    *           <code>null</code> or empty
    * @param defaultvalue the default value
    * @return the value of the first defined property
    */
   @IPSJexlMethod(description = "search the item's properties for the first property in the list that is defined. If none are defined then return the default value", params =
   {
         @IPSJexlParam(name = "item", description = "the item to be searched"),
         @IPSJexlParam(name = "propertylist", description = "a comma separated list of property names, never null or empty"),
         @IPSJexlParam(name = "defaultvalue", description = "the default value to return")})
   public String getFirstDefined(Node item, String propertylist,
         String defaultvalue)
   {
      String properties[] = propertylist.split(",");
      for (String prop : properties)
      {
         try
         {
            prop = prop.trim();
            if (item.hasProperty(prop))
            {
               String val = item.getProperty(prop).getString();
               if (StringUtils.isNotBlank(val))
               {
                  return val;
               }
            }
         }
         catch (Exception e)
         {
            try
            {
               ms_log.error("Problem getting property: " + prop + " on item "
                     + item.getUUID(), e);
            }
            catch (Exception e1)
            {
               ms_log.error("Serious problem while reporting "
                     + e.getLocalizedMessage(), e1);
            }
         }
      }
      return defaultvalue;
   }
   
   /**
    * Find templates that are related to a given contenttype
    * 
    * @param contenttype the contenttype, never <code>null</code>
    * @return the list of templates, never <code>null</code> but can be empty
    * @throws PSAssemblyException
    */
   public static List<IPSAssemblyTemplate> findTemplatesByContentType(
         IPSGuid contenttype) throws PSAssemblyException
   {
     IPSAssemblyService asm = PSAssemblyServiceLocator.getAssemblyService();
     return asm.findTemplatesByContentType(contenttype);
   }
}
