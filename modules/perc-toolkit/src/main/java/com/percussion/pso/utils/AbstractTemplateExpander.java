/*******************************************************************************
 * Copyright (c) 1999-2011 Percussion Software.
 * 
 * Permission is hereby granted, free of charge, to use, copy and create derivative works of this software and associated documentation files (the "Software") for internal use only and only in connection with products from Percussion Software. 
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL PERCUSSION SOFTWARE BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package com.percussion.pso.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.query.QueryResult;
import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.PSExtensionException;

import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.contentmgr.IPSContentMgr;
import com.percussion.services.contentmgr.IPSContentPropertyConstants;
import com.percussion.services.contentmgr.IPSNode;
import com.percussion.services.contentmgr.PSContentMgrLocator;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.services.publisher.IPSTemplateExpander;
import com.percussion.services.publisher.PSPublisherException;
import com.percussion.services.publisher.data.PSContentListItem;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.utils.guid.IPSGuid;

/**
 * An abstract base class for Template Expanders. 
 * Unlike the undocumented system class <code>PSBaseTemplateExpander</code> this class allows 
 * selection of templates at the individual item level. 
 * Subclasses must supply the {@link #findTemplates(IPSGuid, IPSGuid, IPSGuid, int, PSComponentSummary, Node, Map)} 
 * method which generates the list of templates for the content item. 
 * <p>
 * In addition, there are 2 hook methods: {@link #preprocessResults(QueryResult, Map, Map)} and 
 * {@link #postprocessContentList(List)}. These will be called before and after each content list. They
 * can be used by implementations that need to do caching or other processing at the content list level.  
 * As with all <code>IPSExtension</code> implementations, there is only one instance of each Template
 * Expander.  It is the implementers responsibility to use the hook methods in a thread-safe manner. 
 *
 * @author DavidBenua
 *
 */
public abstract class AbstractTemplateExpander implements IPSTemplateExpander
{
   Log log = LogFactory.getLog(AbstractTemplateExpander.class);
   
   
   private boolean NeedsContentNode = false; 
   
   private static IPSGuidManager gmgr = null; 
   
   private static IPSContentMgr cmgr = null; 
   
   /**
    * Initialize the service pointers
    */
   private static void initServices()
   {
      if(gmgr == null)
      {
         cmgr = PSContentMgrLocator.getContentMgr(); 
         gmgr = PSGuidManagerLocator.getGuidMgr(); 
      }
   }
   
   /**
    * 
    * @see com.percussion.extension.IPSExtension#init(com.percussion.extension.IPSExtensionDef, java.io.File)
    */
   public void init(IPSExtensionDef def, File codeRoot)
   throws PSExtensionException
   {
     
   }
   
   /**
    * @see com.percussion.services.publisher.IPSTemplateExpander#expand(javax.jcr.query.QueryResult, java.util.Map, java.util.Map)
    */
   public List<PSContentListItem> expand(QueryResult results,
         Map<String, String> parameters,
         Map<Integer, PSComponentSummary> summaryMap)
         throws PSPublisherException
   {
      initServices();
      if (results == null)
      {
         throw new IllegalArgumentException("results may not be null");
      }
      if (parameters == null || parameters.size() == 0)
      {
         throw new IllegalArgumentException(
               "parameters may not be null or empty");
      }
      
      results = preprocessResults(results, parameters, summaryMap);
      String siteid = parameters.get("siteid");
      IPSGuid siteg = null;
      if (!StringUtils.isBlank(siteid))
      {
         siteg = gmgr.makeGuid(siteid, PSTypeEnum.SITE);
      }
      
      List<PSContentListItem> clist = new ArrayList<PSContentListItem>();

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
         Map<IPSGuid, Node> nodeMap  = null; 
         if(isNeedsContentNode())
         {
            nodeMap = buildNodeMap(results, summaryMap); 
         }
         
         RowIterator riter = results.getRows();
         Map<IPSGuid, List<IPSGuid>> cache = new HashMap<IPSGuid, List<IPSGuid>>();
         while (riter.hasNext())
         {
            Row r =  riter.nextRow(); 
            int cid = (int) r.getValue(
                  IPSContentPropertyConstants.RX_SYS_CONTENTID).getLong();
            PSComponentSummary sum = summaryMap.get(cid);
            int rev;
            try
            {
               rev = (int) r.getValue(
                     IPSContentPropertyConstants.RX_SYS_REVISION).getLong();
            } catch (RepositoryException ex)
            {
               log.warn("Revision not found in result set, using the public or current revision");
               rev = sum.getPublicOrCurrentRevision(); 
            } 
            IPSGuid cguid = gmgr.makeGuid(new PSLocator(cid,rev)); 
               //new PSLegacyGuid(cid, sum.getPublicOrCurrentRevision());
            Value folderid = r.getValue("rx:sys_folderid");
            IPSGuid fid = null;
            if (folderid != null)
            {
               fid = gmgr.makeGuid(new PSLocator((int) folderid.getLong(), 0));
            }
            Node cnode = null; 
            if(isNeedsContentNode())
            {
               cnode = nodeMap.get(cguid); 
            }
            List<IPSGuid> templateids = findTemplates(cguid, fid, siteg, context, sum, 
                  cnode, parameters);

            for (IPSGuid tid : templateids)
            {
               clist.add(new PSContentListItem(cguid, fid, tid, siteg, context));
            }
         }
      }
      catch (RepositoryException e)
         {
            log.error("Error accessing Repository " + e.getLocalizedMessage(), e); 
            throw new PSPublisherException(0, e);
         }
      return clist; 
   }

   /**
    * Find the publishable templates for an item.  
    * @param itemGuid the item guid. Never <code>null</code>.
    * @param folderGuid the folder guid.  Never <code>null</code>.
    * @param siteGuid the site guid.  May be <code>null</code> if not using site folder publishing.
    * @param context the publishing context.  Never <code>null</code>.
    * @param summary the item summary.  Never <code>null</code>.
    * @param contentNode the content node.  Will be <code>null</code> if the <code>needsContentNode</code>
    * property is <code>false</code>.  
    * @param parameters the parameters passed to the template expander.  Never <code>null</code>. 
    * @return the template guids for this item. 
    */
   protected abstract List<IPSGuid> findTemplates(IPSGuid itemGuid, IPSGuid folderGuid, IPSGuid siteGuid, int context, PSComponentSummary summary, Node contentNode, 
         Map<String,String> parameters );
   
 
   /**
    * Preprocess the query results. This method will always be called before the first call to 
    * <code>findTemplates</code>. It may be used to replace or add columns to the result set, or as 
    * a general hook for preprocessing.  
    * @param results the query results. Never <code>null</code>
    * @param parameters the parameters passed to <code>expand</code>. 
    * Never <code>null</code> or <code>empty</code>
    * @param summaryMap the component summary map. 
    * @return the query results. Must not be <code>null</code>
    */
   protected QueryResult preprocessResults(QueryResult results, Map<String, String> parameters,
         Map<Integer, PSComponentSummary> summaryMap )
   {
      return results; 
   }
   
   /**
    * PostProcess the content list. This method will always be called after the last call to 
    * <code>findTemplates</code>. It may be used to modify the resulting list of content list items, 
    * or to perform any cleanup required after the content list is completed.
    * @param clist the content list items.  Never<code>null</code>. 
    * @return the content list items. Never<code>null</code>.
    */
   protected List<PSContentListItem> postprocessContentList(List<PSContentListItem> clist)
   {
      return clist;
   }
   
   /**
    * Builds the map of Node objects.  The nodes may be used to access the properties of the content items
    * in the content list. 
    * @param result the query result. Never <code>null</code>. Contains one row for each item in the 
    * content list. 
    * @param summaryMap the map of component summaries. Never <code>null</code>. This map is guaranteed 
    * to contain all items referenced in the query result.  
    * @return the map of Nodes. 
    * @throws RepositoryException
    */
   protected Map<IPSGuid, Node> buildNodeMap(QueryResult result, Map<Integer, PSComponentSummary> summaryMap) throws RepositoryException
   {
       initServices();
       Map<IPSGuid, Node> nodeMap = new HashMap<IPSGuid, Node>();
       //this should be as simple as: 
//       NodeIterator ntr = result.getNodes();
//       while(ntr.hasNext())
//       {
//          IPSNode node = (IPSNode) ntr.nextNode();
//          nodeMap.put(node.getGuid(), node); 
//       }
       //however, the result set does not contain the revision, so we have to do something a bit more complex:
       List<IPSGuid> itemList = new ArrayList<IPSGuid>(); 
       RowIterator rows = result.getRows();
       while(rows.hasNext())
       {
          Row r = rows.nextRow(); 
          int cid = (int) r.getValue(
                IPSContentPropertyConstants.RX_SYS_CONTENTID).getLong();
          PSComponentSummary sum = summaryMap.get(cid);
          int rev;
          try
          {
             rev = (int) r.getValue(IPSContentPropertyConstants.RX_SYS_REVISION).getLong();
          } catch (RepositoryException ex)
          {
             log.warn("Revision not found in result set, using the public or current revision");
             rev = sum.getPublicOrCurrentRevision(); 
          } 
          IPSGuid itemGuid = gmgr.makeGuid(new PSLocator(cid, rev)); 
          itemList.add(itemGuid); 
       }
       List<Node> nodes = cmgr.findItemsByGUID(itemList, null);
       for(Node node : nodes)
       {
          nodeMap.put(((IPSNode)node).getGuid(), node); 
       }
       
       
       return nodeMap;
   }
   
   /**
    * @return the needsContentNode
    */
   
   protected boolean isNeedsContentNode()
   {
      return NeedsContentNode;
   }

   /**
    * @param needsContentNode the needsContentNode to set
    */
   protected void setNeedsContentNode(boolean needsContentNode)
   {
      NeedsContentNode = needsContentNode;
   }

   /**
    * @param gmgr the gmgr to set
    */
   public static void setGmgr(IPSGuidManager gmgr)
   {
      AbstractTemplateExpander.gmgr = gmgr;
   }

   /**
    * @param cmgr the cmgr to set
    */
   public static void setCmgr(IPSContentMgr cmgr)
   {
      AbstractTemplateExpander.cmgr = cmgr;
   }
   
}
