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
package com.percussion.rx.publisher.jsf.nodes;

import static com.percussion.rx.publisher.jsf.nodes.PSLocationSchemeEditor.JEXL_GENERATOR;

import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.cms.objectstore.PSFolder;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.extension.IPSAssemblyLocation;
import com.percussion.extension.IPSExtension;
import com.percussion.extension.IPSExtensionManager;
import com.percussion.extension.PSExtensionRef;
import com.percussion.rx.jsf.PSNodeBase;
import com.percussion.server.PSRequest;
import com.percussion.server.PSRequestContext;
import com.percussion.server.PSServer;
import com.percussion.server.webservices.PSServerFolderProcessor;
import com.percussion.services.legacy.IPSCmsObjectMgr;
import com.percussion.services.legacy.PSCmsObjectMgrLocator;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.util.PSBaseHttpUtils;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.request.PSRequestInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.model.SelectItem;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The backing bean for the test panel of the Location Scheme Editor.
 *
 * @author yubingchen
 */
public class PSSchemeJexlTestPanel
{
   /**
    * The class log.
    */
   private final static Log ms_log = LogFactory.getLog(PSSchemeJexlTestPanel.class);

   /**
    * The backing bean for the site id. It may be <code>null</code> if has 
    * not been selected by user.
    */
   private IPSGuid m_siteId = null;
 
   /**
    * The item path, may be empty, never <code>null</code>.
    */
   private String m_itemPath = "";
   
   /**
    * The place holder for the extra parameters. 
    */
   private String m_extraParameters = "";
   
   /**
    * The place holder for the evaluated result.
    */
   private String m_evalResult = "";
   
   /**
    * The place holder for the status of the evaluated result. It can be
    * empty (initially), Success or Error.
    */
   private String m_evalStatus = "";
   
   /**
    * The parent backing bean or the Location Scheme Editor instance. It 
    * never <code>null</code> after constructor.
    */
   PSLocationSchemeEditor m_schemeEditor;
   
   /**
    * The node of all existing site. Set by {@link #getAllSites()}, never
    * <code>null</code> after that, but it may be empty.
    */
   private List<PSSiteNode> m_allSites = null;
   
   /**
    * The instance of the item browser.
    */
   private PSItemBrowser m_itemBrowser;
   
   /**
    * Constructor.
    * 
    * @param se the parent backing bean, never <code>null</code>.
    */
   PSSchemeJexlTestPanel(PSLocationSchemeEditor se)
   {
      if (se == null)
         throw new IllegalArgumentException("se may not be null");
      
      m_schemeEditor = se;
   }

   /**
    * Invokes the Item Browser.
    * @return the outcome of the Item Browser, never <code>null</code> or empty.
    */
   public String browseItem()
   {
      m_itemBrowser = new PSItemBrowser(this);
      m_itemBrowser.setPath(getStartingFolderPath());
      
      return m_itemBrowser.gotoFolder();
   }

   /**
    * @return the extra parameters, never <code>null</code>, but may be empty.
    */
   public String getExtraParameters()
   {
      return m_extraParameters;
   }
   
   /**
    *  Set the extra parameters.
    * @param ps the new value of the extra parameters. If it is 
    *    <code>null</code>, then the extra parameters will be set to empty.
    */
   public void setExtraParameters(String ps)
   {
      if (ps == null)
         m_extraParameters = "";
      else
         m_extraParameters = ps;
   }
   
   /**
    * @return the starting folder of the item browser.
    */
   private String getStartingFolderPath()
   {
      if (!StringUtils.isBlank(m_itemPath))
      {
         try
         {
            int itemId = getFolderSrv().getIdByPath(m_itemPath);
            if (itemId != -1)
            {
               List<PSLocator> locPath;
               locPath = getFolderSrv().getAncestorLocators(
                     new PSLocator(itemId));
               PSLocator parent = locPath.get(locPath.size() - 1);
               String[] paths = getFolderSrv().getItemPaths(parent);
               return paths[0];
            }
         }
         catch (Exception e)
         {
            // ignore any error, including invalid path
         }
      }
      
      if (m_siteId != null)
      {
         for (PSSiteNode s : getAllSites())
         {
            if (m_siteId.equals(s.getGUID()))
            {
               String siteRoot = s.getFolderRootPath();
               if (StringUtils.isNotBlank(siteRoot))
                  return siteRoot;
            }
         }
      }

      // default to the root of the virtual folder
      return PSFolder.PATH_SEP;
   }
   
   /**
    * @return the instance of the backing bean for browsing an item for testing
    * the JEXL expression, never <code>null</code>.
    */
   public PSItemBrowser getItemBrowser()
   {
      return m_itemBrowser;
   }
   
   /**
    * Determines whether the panel is empty. The panel will be empty initially.
    * However, it will not be empty if is any input or activities in the panel.
    * 
    * @return <code>true</code> the panel is empty.
    */
   public boolean isPanelEmpty()
   {
      return StringUtils.isBlank(m_evalStatus)
            && StringUtils.isBlank(m_evalResult)
            && StringUtils.isBlank(m_extraParameters)
            && StringUtils.isBlank(m_itemPath);
   }
   
   /**
    * @return the current site id, may be <code>null</code>.
    */
   public IPSGuid getSiteId()
   {
      if (m_siteId == null)
      {
         List<PSSiteNode> allSites = getAllSites();
         if (!allSites.isEmpty())
            m_siteId = allSites.get(0).getGUID();
      }
      return m_siteId;
   }

   /**
    * @return the item path, never <code>null</code>, but may be empty.
    */
   public String getItemPath()
   {
      return m_itemPath;
   }

   /**
    * Set item path.
    * @param path the new item path, may be <code>null</code> or empty.
    */
   public void setItemPath(String path)
   {
      if (path == null)
         m_itemPath = "";
      else
         m_itemPath = path;
   }
   
   /**
    * Set the site id.
    * @param id the new site id, never <code>null</code>.
    */
   public void setSiteId(IPSGuid id)
   {
      if (id == null)
         throw new IllegalArgumentException("id must not be null.");

      m_siteId = id;
   }

   /**
    * @return all available sites, never <code>null</code>, but may be empty.
    */
   public SelectItem[] getSites()
   {
      List<SelectItem> siteList = new ArrayList<>();

      for (PSSiteNode s : getAllSites())
      {
         SelectItem si = new SelectItem(s.getTitle());
         si.setValue(s.getGUID());
         siteList.add(si);
      }
      
      return siteList.toArray(new SelectItem[siteList.size()]);
   }

   /**
    * @return all site nodes, never <code>null</code>, but may be empty.
    */
   @SuppressWarnings("unchecked")
   private List<PSSiteNode> getAllSites()
   {
      if (m_allSites != null)
         return m_allSites;
      
      PSNodeBase root = m_schemeEditor.getParentNode().getParent().getParent();
      PSNodeBase sitesNode = null;
      for (PSNodeBase n : root.getChildren())
      {
         if (n.getTitle().equalsIgnoreCase("Sites"))
         {
            sitesNode = n;
            break;
         }
      }
      if (sitesNode == null)
      {
         ms_log.error("Couldn't fond Sites node.");
         throw new RuntimeException("Couldn't find Sites node.");
      }
      
      m_allSites = (List<PSSiteNode>) sitesNode.getChildren();
      return m_allSites;
   }
   
   /**
    * @return the evaluated JEXL result, never <code>null</code> or empty.
    */
   public String getEvaluateResult()
   {
      return m_evalResult;
   }
   
   /**
    * @return the error message while evaluating the JEXL expression, 
    *    never <code>null</code>, but may be empty.
    */
   public String getEvaluateStatus()
   {
      return m_evalStatus;
   }
   
   /**
    * @return the folder processor, never <code>null</code>.
    */
   PSServerFolderProcessor getFolderSrv()
   {
      if (m_folderProcessor == null)
      {
         m_folderProcessor = PSServerFolderProcessor.getInstance();
      }
      return m_folderProcessor;
   }
   
   /**
    * The place folder for the folder processor. It is set by 
    * {@link #getFolderSrv()}
    */
   private PSServerFolderProcessor m_folderProcessor = null;
      
   /**
    * Evaluates the JEXL expression. It will validate the specified site and
    * item before perform the evaluation.
    * 
    * @return the outcome of the same page, never <code>null</code> or empty.
    */
   @SuppressWarnings("unchecked")
   public String evaluateExpression()
   {
      m_evalResult = "";
      
      PSRequest req = (PSRequest) PSRequestInfo
            .getRequestInfo(PSRequestInfo.KEY_PSREQUEST);

      HashMap origParams = new HashMap(req.getParameters());
      String expression = m_schemeEditor.getExpression();
      try
      {
         PSLocator itemLoc = getItemId();
         setRequestParams(req, itemLoc, m_siteId.getUUID());
         
         PSRequestContext reqCxt = new PSRequestContext(req);
         Object[] params = new Object[] { expression };
         m_evalResult = getGenerator().createLocation(params, reqCxt);
         m_evalStatus = "Success";
      }
      catch (Exception e)
      {
         Throwable cause = e.getCause();
         m_evalResult = cause != null ? getExceptionMessage(cause)
               : getExceptionMessage(e);
         m_evalStatus = "Error";
      }
      finally
      {
         req.setParameters(origParams);
      }
      
      return perform();
   }

   /**
    * Get the error message from a given exception object.
    * @param e the exception, assumed not <code>null</code>.
    * @return the error message, never <code>null</code> or empty.
    */
   private String getExceptionMessage(Throwable e)
   {
      if (StringUtils.isBlank(e.getLocalizedMessage()))
         return "Caught exception: " + e.getClass().getName();
      else
         return e.getLocalizedMessage();
   }
   
   /**
    * @return the outcome of the Location Scheme page, never <code>null</code>
    *    or empty.
    */
   String perform()
   {
      return m_schemeEditor.perform();
   }
   
   /**
    * @return the instance of <code>sys_JexlAssemblyLocation</code> extension,
    *    never <code>null</code>.
    *    
    * @throws Exception if couldn't find the extension.
    */
   @SuppressWarnings("unchecked")
   private IPSAssemblyLocation getGenerator() throws Exception
   {
      
      PSExtensionRef ref = new PSExtensionRef(JEXL_GENERATOR);
      IPSExtensionManager exitMgr = PSServer.getExtensionManager(null);
      IPSExtension exit = exitMgr.prepareExtension(ref, null);
      return (IPSAssemblyLocation) exit;
   }
   
   /**
    * Set all necessary request parameters before run the evaluator of the
    * Location Generator.
    * 
    * @param req the request instance, assumed not <code>null</code>.
    * @param itemLoc the locator of the item that the evaluator will run 
    *    against with. It may be <code>null</code> if item path is not defined.
    * @param siteId the site id that the evaluator will run against with, 
    *    assumed it is an valid site id.
    *    
    * @throws PSCmsException if failed to get the parent folder of the item.
    */
   private void setRequestParams(PSRequest req, PSLocator itemLoc,
         int siteId) throws PSCmsException
   {
      int folderId = getParentFolderId(itemLoc);
      
      // set parameters
      req.setParameter(IPSHtmlParameters.SYS_VARIANTID,
            m_schemeEditor.getEditedScheme().getTemplateId());
      req.setParameter(IPSHtmlParameters.SYS_SITEID, siteId);
      req.setParameter(IPSHtmlParameters.SYS_CONTEXT,
            m_schemeEditor.getParentNode().getContext().getGUID().getUUID());
  
      if (itemLoc != null)
      {
         req.setParameter(IPSHtmlParameters.SYS_CONTENTID, itemLoc.getId());
         req.setParameter(IPSHtmlParameters.SYS_REVISION, itemLoc
               .getRevision());
      }

      if (!StringUtils.isBlank(m_extraParameters))
      {
         Map<String, Object> params = PSBaseHttpUtils
               .parseQueryParamsString(m_extraParameters);
         req.putAllParameters(params);
      }
      req.setParameter(IPSHtmlParameters.SYS_FOLDERID, folderId);
   }

   /**
    * Get the parent folder id for the given item.
    * @param itemLoc the locator of the item, assumed not <code>null</code>.
    * @return the folder id.
    * @throws PSCmsException if error.
    */
   private int getParentFolderId(PSLocator itemLoc) throws PSCmsException
   {
      List<PSLocator> locPath = null;
      locPath =getFolderSrv().getAncestorLocators(itemLoc);
      PSLocator loc = locPath.get(locPath.size()-1);
      return loc.getId();
   }
   
   /**
    * Validates the item path. Make sure it does exist and under the specified
    * site. Throws exception if encounter any invalid value.
    *  
    * @return the locator of the specified item, never <code>null</code>. 
    * 
    * @throws Exception if any error occurs.
    */
   private PSLocator getItemId() throws Exception
   {
      if (StringUtils.isBlank(m_itemPath))
         return null;
      
      int itemId = getFolderSrv().getIdByPath(m_itemPath);
      if (itemId == -1)
         return null;
      
      IPSCmsObjectMgr cms = PSCmsObjectMgrLocator.getObjectManager();
      List<Integer> ids = Collections.singletonList(itemId);
      List<PSComponentSummary> summarylist = cms.loadComponentSummaries(ids);

      return summarylist.get(0).getCurrentLocator();
   }
   
}
