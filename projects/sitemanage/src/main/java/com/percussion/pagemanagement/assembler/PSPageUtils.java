/*
 *     Percussion CMS
 *     Copyright (C) 1999-2021 Percussion Software, Inc.
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
package com.percussion.pagemanagement.assembler;

import com.percussion.analytics.service.IPSAnalyticsProviderService;
import com.percussion.category.data.PSCategory;
import com.percussion.category.data.PSCategoryNode;
import com.percussion.category.extension.PSCategoryControlUtils;
import com.percussion.delivery.service.IPSDeliveryInfoService;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSNotFoundException;
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.designmanagement.service.IPSFileSystemService;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSExtensionManager;
import com.percussion.extension.IPSJexlMethod;
import com.percussion.extension.IPSJexlParam;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionRef;
import com.percussion.extension.PSJexlUtilBase;
import com.percussion.linkmanagement.service.IPSManagedLinkService;
import com.percussion.pagemanagement.assembler.PSAbstractAssemblyContext.EditType;
import com.percussion.pagemanagement.assembler.impl.PSAssemblyItemBridge;
import com.percussion.pagemanagement.assembler.impl.PSAssemblyItemBridge.TemplateAndPage;
import com.percussion.pagemanagement.dao.IPSPageDao;
import com.percussion.pagemanagement.data.PSPage;
import com.percussion.pagemanagement.data.PSRegionBranches;
import com.percussion.pagemanagement.data.PSRegionTree;
import com.percussion.pagemanagement.data.PSRenderLink;
import com.percussion.pagemanagement.data.PSRenderLinkContext;
import com.percussion.pagemanagement.data.PSTemplate;
import com.percussion.pagemanagement.data.PSTemplateSummary;
import com.percussion.pagemanagement.data.PSWidgetItem;
import com.percussion.pagemanagement.service.IPSPageCategoryService;
import com.percussion.pagemanagement.service.IPSPageService;
import com.percussion.pagemanagement.service.IPSRenderLinkService;
import com.percussion.pagemanagement.service.IPSResourceDefinitionService;
import com.percussion.pagemanagement.service.IPSTemplateService;
import com.percussion.pagemanagement.service.IPSWidgetService;
import com.percussion.pathmanagement.service.impl.PSPathUtils;
import com.percussion.pubserver.IPSPubServerService;
import com.percussion.recycle.service.IPSRecycleService;
import com.percussion.security.PSEncryptionException;
import com.percussion.security.PSEncryptor;
import com.percussion.server.PSServer;
import com.percussion.services.assembly.IPSAssemblyErrors;
import com.percussion.services.assembly.IPSAssemblyItem;
import com.percussion.services.assembly.IPSProxyNode;
import com.percussion.services.assembly.PSAssemblyException;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.contentmgr.IPSContentMgr;
import com.percussion.services.contentmgr.IPSNode;
import com.percussion.services.contentmgr.data.PSContentNode;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.guidmgr.data.PSLegacyGuid;
import com.percussion.services.linkmanagement.IPSManagedLinkDao;
import com.percussion.services.linkmanagement.data.PSManagedLink;
import com.percussion.services.memory.IPSCacheAccess;
import com.percussion.services.memory.PSCacheAccessLocator;
import com.percussion.services.pubserver.data.PSPubServer;
import com.percussion.services.relationship.IPSRelationshipService;
import com.percussion.services.relationship.data.PSRelationshipData;
import com.percussion.services.sitemgr.IPSSite;
import com.percussion.services.sitemgr.IPSSiteManager;
import com.percussion.services.workflow.IPSWorkflowService;
import com.percussion.services.workflow.PSWorkflowServiceLocator;
import com.percussion.services.workflow.data.PSWorkflow;
import com.percussion.share.dao.IPSContentItemDao;
import com.percussion.share.dao.IPSGenericDao;
import com.percussion.share.dao.PSHtmlUtils;
import com.percussion.share.dao.PSJcrNodeFinder;
import com.percussion.share.dao.impl.PSContentItem;
import com.percussion.share.dao.impl.PSContentItemDao;
import com.percussion.share.service.IPSDataService;
import com.percussion.share.service.IPSIdMapper;
import com.percussion.share.service.IPSLinkableItem;
import com.percussion.share.service.exception.PSDataServiceException;
import com.percussion.share.service.exception.PSValidationException;
import com.percussion.share.spring.PSSpringWebApplicationContextUtils;
import com.percussion.sitemanage.service.IPSSiteSectionService;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.util.PSSiteManageBean;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.io.PathUtils;
import com.percussion.utils.jsr170.PSValueFactory;
import com.percussion.utils.security.ToDoVulnerability;
import com.percussion.utils.timing.PSStopwatchStack;
import com.percussion.utils.types.PSPair;
import com.percussion.webservices.content.IPSContentWs;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.OutputDocument;
import net.htmlparser.jericho.Source;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;
import javax.xml.XMLConstants;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import static com.percussion.pagemanagement.assembler.impl.finder.PSRelationshipWidgetContentFinder.IS_MATCH_BY_NAME;
import static com.percussion.share.spring.PSSpringWebApplicationContextUtils.getWebApplicationContext;
import static com.percussion.share.spring.PSSpringWebApplicationContextUtils.injectDependencies;
import static java.text.MessageFormat.format;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.commons.lang.StringUtils.removeStart;
import static org.apache.commons.lang.Validate.isTrue;
import static org.apache.commons.lang.Validate.notEmpty;
import static org.apache.commons.lang.Validate.notNull;

/**
 * Utility class to provide various JEXL methods for the templates used by page
 * or widget.
 *
 * NOTE: That if you add JEXL methods to this class they will only be registered
 * if you update the /CMLite-Main/Packages/perc.Baseline/Extension-Java/global/percussion/system/pageutils.extension
 * file to include the method.
 *
 * @author YuBingChen
 * @author adamgent
 * @author DennisDonoghue
 */
@PSSiteManageBean
public class PSPageUtils extends PSJexlUtilBase
{

    /**
     * The timeout used when checking links for being available.
     */
    private static final int LINKCHECK_TIMEOUT=30000;
    private static final String LINKCHECK_CACHENAME="PSLinkChecker";

    private static final String WEB_RESOURCES = "web_resources";
    private static final String CATEGORY_URL = "../rx_resources/category/category.xml";

    private CacheManager cacheMgr;
    private Cache linkCache;


    @IPSJexlMethod(description = "parseHtmlFragment can be used to parse a fragment of HTML and return an Element that can be manipulated", params =
            {@IPSJexlParam(name = "htmlFragment", description = "An HTML fragment to parse.")}, returns = "An org.jsoup.nodes.Element instance representing the HTML fragment.")
    public  org.jsoup.nodes.Element parseHtmlFragment(String htmlFragment)
    {
        org.jsoup.nodes.Document doc = Jsoup.parseBodyFragment(htmlFragment);
        return doc.body();

    }


    @IPSJexlMethod(description = "Returns the Major.Minor version string for the product.", params =
            {}, returns = "String")
    public String productVersion()
    {
        return PSServer.getVersion();
    }

    @IPSJexlMethod(description = "Returns the detailed version string for the product.", params =
            {}, returns = "String")
    public String productVersionDetail()
    {
        return PSServer.getVersionString();
    }

    @IPSJexlMethod(description = "Validates that the path / url passed in resolves to a resource that has a 20X http status code.",
            params={@IPSJexlParam(description = "Link (relative or absoulte to be checked.", name = "link"),
                    @IPSJexlParam(description = "Publishing context name", name = "context"),
                    @IPSJexlParam(description = "Set to true to not cache this link. false to use caching.  By default all links are cached for 30 minutes.",name="dontCache")}, returns = "boolean")
    public boolean isLinkGood(String link, String context, boolean dontCache){

        net.sf.ehcache.Element cachedLink =null;

        try{
            log.debug("Checking link: {} in context: {} dontCache= {}",
                    link,context,dontCache);

            if(!dontCache){
                cachedLink = linkCache.get(link);
                log.debug("Got link: {} from cache.",link);
            }


            //If we didn't get a result - it's the first time through or link has expired
            if(cachedLink == null){
                try {
                    HttpURLConnection connection = (HttpURLConnection) new URL(link).openConnection();
                    connection.setConnectTimeout(LINKCHECK_TIMEOUT);
                    connection.setReadTimeout(LINKCHECK_TIMEOUT);
                    connection.setRequestMethod("HEAD");
                    int responseCode = connection.getResponseCode();

                    log.debug("Got response code of {}  for {}",responseCode ,link);

                    boolean result = (200 <= responseCode && responseCode <= 399);

                    //Cache it
                    net.sf.ehcache.Element newLink = new net.sf.ehcache.Element(link,result);
                    newLink.setTimeToIdle(1800);
                    newLink.setTimeToLive(1800);

                    if(!dontCache){
                        linkCache.put(newLink);
                        log.debug("Caching link: {} with result of {}",link,result);
                    }

                    return result;
                } catch (IOException exception) {
                    log.error(LOG_ERROR_DEFAULT,"isLinkGood", exception.getMessage());
                    log.debug(exception.getMessage(),exception);
                    return false;
                }
            }else{
                log.debug("Returning cached link result for link: {} status: {}",link , cachedLink.getObjectValue());
                return (Boolean)cachedLink.getObjectValue();
            }
        }catch(Exception e){
            log.error("Error checking link: {} Error: {}", link, e.getMessage());
            log.debug(e.getMessage(),e);
            return false;
        }

    }


    @IPSJexlMethod(description = "Renders a Link to an item using the default resource definition for that items type.", params =
            {@IPSJexlParam(name = "linkContext", description = "The link context. Use $perc.linkContext"),
                    @IPSJexlParam(name = "linkableItem", description = "An asset or page.")}, returns = "PSRenderLink")
    public PSRenderLink itemLink(PSRenderLinkContext linkContext, IPSLinkableItem linkableItem)
    {
        try{
            return renderLinkService.renderLink(linkContext, linkableItem);
        } catch (PSDataServiceException e) {
            log.error(LOG_ERROR_DEFAULT,"itemLink", e.getMessage());
            log.debug(e.getMessage(),e);
            return new PSRenderLink("#",null);
        }
    }

    @IPSJexlMethod(description = "Renders a list of javascript links", params =
            {@IPSJexlParam(name = "linkContext", description = "The link context. Use $perc.linkContext"),
                    @IPSJexlParam(name = "item", description = "the parent (page/template) assembly item")}, returns = "List of PSRenderLink")
    public List<PSRenderLink> javascriptLinks(PSRenderLinkContext linkContext, IPSAssemblyItem item)
    {
        try {
            Set<String> widgetDefIds = getWidgetDefIds(item);
            return renderLinkService.renderJavascriptLinks(linkContext, widgetDefIds);
        } catch (PSDataServiceException | RepositoryException e) {
            log.error(LOG_ERROR_DEFAULT,"javascriptLinks", e.getMessage());
            log.debug(e.getMessage(),e);
            return new ArrayList<>();
        }
    }

    @IPSJexlMethod(description = "Renders a list of css links", params =
            {@IPSJexlParam(name = "linkContext", description = "The link context. Use $perc.linkContext"),
                    @IPSJexlParam(name = "item", description = "the parent (page/template) assembly item")}, returns = "List of PSRenderLink")
    public List<PSRenderLink> cssLinks(PSRenderLinkContext linkContext, IPSAssemblyItem item)
    {
        try {
            Set<String> widgetDefIds = getWidgetDefIds(item);
            return renderLinkService.renderCssLinks(linkContext, widgetDefIds);
        } catch (PSDataServiceException | RepositoryException e) {
            log.error(LOG_ERROR_DEFAULT,"cssLinks", e.getMessage());
            log.debug(e.getMessage(),e);
            return new ArrayList<>();
        }
    }

    @IPSJexlMethod(description = "Renders a relative path to the login page", params =
            {@IPSJexlParam(name = "sitename", description = "The name of the site to load the login page property")}, returns = "String")
    public String getSiteLoginPage(String sitename)
    {
        try {
            IPSSite site = siteMgr.loadSite(sitename);
            return site.getLoginPage();
        } catch (com.percussion.services.error.PSNotFoundException e) {
            log.error(LOG_ERROR_DEFAULT,"getSiteLoginPage",e.getMessage());
            log.debug(e.getMessage(),e);
            return "";
        }
    }

    @IPSJexlMethod(description = "Renders a relative path to the registration page", params =
            {@IPSJexlParam(name = "sitename", description = "The name of the site to load the registration page property")}, returns = "String")
    public String getSiteRegistrationPage(String sitename)
    {
        try {
            IPSSite site = siteMgr.loadSite(sitename);
            return site.getRegistrationPage();
        } catch (com.percussion.services.error.PSNotFoundException e) {
            log.error(LOG_ERROR_DEFAULT,"getSiteRegistrationPage",e.getMessage());
            log.debug(e.getMessage(),e);
            return "";
        }
    }

    @IPSJexlMethod(description = "Renders a relative path to the registration confirmation page", params =
            {@IPSJexlParam(name = "sitename", description = "The name of the site to load the registration confirmation page property")}, returns = "String")
    public String getSiteRegistrationConfirmationPage(String sitename)
    {
        try {
            IPSSite site = siteMgr.loadSite(sitename);
            return site.getRegistrationConfirmationPage();
        } catch (com.percussion.services.error.PSNotFoundException e) {
            log.error(LOG_ERROR_DEFAULT,"getSiteRegistrationConfirmationPage",e.getMessage());
            log.debug(e.getMessage(),e);
            return "";
        }
    }

    @IPSJexlMethod(description = "Renders a relative path to the reset page", params =
            {@IPSJexlParam(name = "sitename", description = "The name of the site to load the reset page property")}, returns = "String")
    public String getSiteResetPage(String sitename)
    {
        try {
            IPSSite site = siteMgr.loadSite(sitename);
            return site.getResetPage();
        } catch (com.percussion.services.error.PSNotFoundException e) {
            log.error(LOG_ERROR_DEFAULT,"getSiteResetPage",e.getMessage());
            log.debug(e.getMessage(),e);
            return "";
        }
    }

    @IPSJexlMethod(description = "Renders a relative path to the reset request password page", params =
            {@IPSJexlParam(name = "sitename", description = "The name of the site to load the reset request password page property")}, returns = "String")
    public String getSiteResetRequestPasswordPage(String sitename)
    {
        try {
            IPSSite site = siteMgr.loadSite(sitename);
            return site.getResetRequestPasswordPage();
        } catch (com.percussion.services.error.PSNotFoundException e) {
            log.error(LOG_ERROR_DEFAULT,"getSiteResetPage",e.getMessage());
            log.debug(e.getMessage(),e);
            return "";
        }
    }

    @IPSJexlMethod(description = "Create a clone of the current assemblyItem if an id is passed the clone will be modified to that id.", params =
            {@IPSJexlParam(name = "item", description = "e.g $sys.assemblyItem"),
                    @IPSJexlParam(name = "idObj", description = "a content id or guid to change the clone to")
            }, returns = "IPSAssemblyItem")
    public IPSAssemblyItem createCloneAssemblyItem(IPSAssemblyItem item, Object idObj)
    {
        IPSAssemblyItem it = null;
        IPSGuid guid = null;
        guid = parseGuid(idObj);
        if (guid!=null)
        {
            it = (IPSAssemblyItem) item.clone();
            it.removeParameter(IPSHtmlParameters.SYS_CONTENTID);
            it.removeParameter(IPSHtmlParameters.SYS_REVISION);
            it.removeParameter(IPSHtmlParameters.SYS_FOLDERID);
            it.setParameterValue(IPSHtmlParameters.SYS_CONTENTID,String.valueOf(guid.getUUID()));
            it.getBindings().remove("$_previewTemplate");
            it.getBindings().remove("$_previewPage");

            it.setId(guid);
        }
        return it;
    }

    @IPSJexlMethod(description = "Tries to generate an IPSGuid object from different object types", params =
            {@IPSJexlParam(name = "id", description = "The id to convert to a guid")}, returns = "IPSGuid")
    public IPSGuid parseGuid(Object idObj)
    {
        if (idObj == null)
            return null;

        IPSGuid guid;
        if (idObj instanceof IPSGuid)
        {
            guid = (IPSGuid)idObj;
        } else if (idObj instanceof PSLocator)
        {
            guid = guidManager.makeGuid((PSLocator)idObj);
        }
        else if (idObj instanceof Number) {
            guid = new PSLegacyGuid(((Number)idObj).intValue(), -1);
        } else if (NumberUtils.isNumber((String)idObj))
        {
            guid = new PSLegacyGuid(NumberUtils.toInt((String)idObj), -1);
        } else if (idObj instanceof String)
        {
            guid = guidManager.makeGuid((String)idObj);
        } else {
            throw new RuntimeException("Cannot parse guid or content id object "+idObj.toString() +" of type "+idObj.getClass().toString());
        }
        return guid;
    }

    @IPSJexlMethod(description = "Returns region information about a page.", params =
            {@IPSJexlParam(name = "item", description = "the parent (page/template) assembly item")}, returns = "PSAbstractMergedRegionTree")
    public PSAbstractMergedRegionTree getRegionTree(IPSAssemblyItem item)
    {
        notNull(item, "assemblyItem");

        PSAbstractMergedRegionTree regionTree = null;
        TemplateAndPage tp;
        try
        {
            tp = assemblyItemBridge.getTemplateAndPage(item);
            PSPage page = tp.getPage();
            PSTemplate template = tp.getTemplate();
            PSRegionTree templateRegionTree = template.getRegionTree();
            PSRegionBranches pageRegionBranches = page.getRegionBranches();
            regionTree =  new PSMergedRegionTree(widgetService ,templateRegionTree , pageRegionBranches);
        }
        catch (RepositoryException | PSDataServiceException e)
        {
            log.error("Error getting RegionTree for item ", e);
        }
        return regionTree;
    }

    @IPSJexlMethod(description = "Find a list of WidgetInstances for an assemblyItem, will filter by regionName and/or widgetName if not null ", params =
            {@IPSJexlParam(name = "item", description = "the parent (page/template) assembly item"),
                    @IPSJexlParam(name = "regionName", description = "The name of the region to filter by"),
                    @IPSJexlParam(name = "widgetName", description = "The name of the widget to filter by")}, returns = "List<PSWidgetInstance>")
    public List<PSWidgetInstance> findWidgetInstances(IPSAssemblyItem assemblyItem, String regionName, String widgetName)
    {
        return findWidgetInstances(getRegionTree(assemblyItem), regionName, widgetName);
    }

    @IPSJexlMethod(description = "Find a list of WidgetInstances for an assemblyItem, will filter by regionName and/or widgetName if not null ", params =
            {@IPSJexlParam(name = "regionTree", description = "the PSAbstractMergedRegionTree to find from"),
                    @IPSJexlParam(name = "regionName", description = "The name of the region to filter by"),
                    @IPSJexlParam(name = "widgetName", description = "The name of the widget to filter by")}, returns = "List<PSWidgetInstance>")
    public List<PSWidgetInstance> findWidgetInstances(PSAbstractMergedRegionTree regionTree, String regionName, String widgetName)
    {
        notNull(regionTree, "regionTree");

        List<PSWidgetInstance> instances = new ArrayList<>();
        Map<String, PSMergedRegion> regionMap = regionTree.getMergedRegionMap();
        if (regionName != null)
        {
            PSMergedRegion region = regionMap.get(regionName);
            if (region != null)
            {
                List<PSWidgetInstance> regionInstances = region.getWidgetInstances();
                if (regionInstances != null)
                {
                    if (widgetName!=null)
                    {
                        for (PSWidgetInstance regionInstance : regionInstances)
                        {
                            if (regionInstance.getDefinition().getId().equals(widgetName))
                                instances.add(regionInstance);
                        }
                    } else {
                        instances = regionInstances;
                    }
                }
            }
        }
        else
        {
            for (Entry<String, PSMergedRegion> entry : regionMap.entrySet())
            {
                if (entry.getValue()!=null)
                {
                    if (widgetName!=null)
                    {
                        List<PSWidgetInstance> widgetInstanceList = entry.getValue().getWidgetInstances();
                        if (widgetInstanceList != null)
                        {
                            for (PSWidgetInstance regionInstance : widgetInstanceList)
                            {
                                if (regionInstance.getDefinition().getId().equals(widgetName))
                                    instances.add(regionInstance);
                            }
                        }
                    } else {
                        instances.addAll(entry.getValue().getWidgetInstances());
                    }
                }
            }
        }
        return instances;
    }

    /**
     * Helper method to get set of widget definitions for the supplied assembly
     * item.
     *
     * @param item if <code>null</code> returns empty set.
     * @return Set of widget definition ids, never <code>null</code>, may be
     *         empty.
     */
    private Set<String> getWidgetDefIds(IPSAssemblyItem item) throws PSDataServiceException, RepositoryException {
        Set<String> widgetDefIds = new HashSet<>();
        if (item == null)
            return widgetDefIds;
            PSPage page = getAssemblyItemBridge().getTemplateAndPage(item).getPage();
            PSTemplate template = getAssemblyItemBridge().getTemplateAndPage(item).getTemplate();
            List<PSWidgetItem> widgetList = page.getWidgets(template);
            widgetList.addAll(template.getWidgets());
            for (PSWidgetItem psWidgetItem : widgetList)
            {
                widgetDefIds.add(psWidgetItem.getDefinitionId());
            }

        return widgetDefIds;
    }

    @IPSJexlMethod(description = "Strip javascript", params =
            {@IPSJexlParam(name = "souce", description = "The source that may contain javascript")}, returns = "The text without javascript")
    public String stripJavascripts(String source)
    {
        return PSHtmlUtils.stripScriptElement(source);
    }

    @IPSJexlMethod(description = "Strip canonical link", params =
            {@IPSJexlParam(name = "source", description = "The source that may contain canonical link")}, returns = "The text without canonical link")
    public String stripLinkCanonical(String source)
    {
        return PSHtmlUtils.stripLinkCanonicalElement(source);
    }

    @IPSJexlMethod(description = "Check canonical link", params =
            {@IPSJexlParam(name = "souce", description = "The source that may contain canonical link")}, returns = "true if the source contains canonical link, false otherwize")
    public boolean checkLinkCanonical(String source)
    {
        return PSHtmlUtils.checkLinkCanonicalElement(source);
    }

    @IPSJexlMethod(description = "Renders a Link of a landing page for the given navigation node.", params =
            {@IPSJexlParam(name = "linkContext", description = "The link context. Use $perc.linkContext"),
                    @IPSJexlParam(name = "navNode", description = "A navigation node.")}, returns = "PSRenderLink")
    public PSRenderLink navLink(PSRenderLinkContext linkContext, Node navNode)
    {
        PSRenderLink renderLink = new PSRenderLink("#", null);
        try
        {
            Property landingPageProperty = navNode.getProperty("nav:landingPage");


            Node landingPageNode = (landingPageProperty == null) ? null : landingPageProperty.getNode();
            if (landingPageNode != null)
            {
                String id = idMapper.getString(((IPSNode) landingPageNode).getGuid());
                if(recycleService.isNavInRecycler(id)){
                    return renderLink;
                }
                IPSLinkableItem page = PSPathUtils.getLinkableItem(id);
                renderLink = renderLinkService.renderLink(linkContext, page);
            }
            else
            {
                IPSGuid navId = ((IPSNode) navNode).getGuid();

                log.debug("Failed to find the landing page for nav node id = {}", navId);
            }
        }
        catch (RepositoryException | IllegalArgumentException | PSDataServiceException e)
        {
            IPSGuid navId = ((IPSNode) navNode).getGuid();

            log.error(LOG_ERROR_DEFAULT, "navLink",e.getMessage());
            log.debug(e.getMessage(),e);
        }
        return renderLink;
    }


    @IPSJexlMethod(description = "Renders a Link to an item", params =
            {@IPSJexlParam(name = "linkContext", description = "The link context. Use $perc.linkContext"),
                    @IPSJexlParam(name = "linkableItem", description = "An asset or page."),
                    @IPSJexlParam(name = "resourceDefinitionId", description = "the fully qualified resourceDefinitionId")}, returns = "PSRenderLink")
    public PSRenderLink itemLink(PSRenderLinkContext linkContext, IPSLinkableItem linkableItem,
                                 String resourceDefinitionId)
    {
        try {
            return renderLinkService.renderLink(linkContext, linkableItem, resourceDefinitionId);
        } catch (PSDataServiceException e) {
            log.error(LOG_ERROR_DEFAULT,"itemLink", e.getMessage());
            log.debug(e.getMessage(),e);
            return new PSRenderLink("#",null);
        }
    }

    @IPSJexlMethod(description = "Renders a Link to a folder", params =
            {@IPSJexlParam(name = "linkContext", description = "The link context. Use $perc.linkContext"),
                    @IPSJexlParam(name = "resourceDefinitionId", description = "the fully qualified resourceDefinitionId")}, returns = "PSRenderLink")
    public PSRenderLink folderLink(PSRenderLinkContext linkContext, String resourceDefinitionId)
    {
        try {
            return renderLinkService.renderLink(linkContext, resourceDefinitionId);
        } catch (PSDataServiceException e) {
            log.error(LOG_ERROR_DEFAULT,"folderLink", e.getMessage());
            log.debug(e.getMessage(),e);
            return new PSRenderLink("#",null);
        }
    }

    @IPSJexlMethod(description = "Renders a Link to a file", params =
            {@IPSJexlParam(name = "linkContext", description = "The link context. Use $perc.linkContext"),
                    @IPSJexlParam(name = "resourceDefinitionId", description = "the fully qualified resourceDefinitionId")}, returns = "PSRenderLink")
    public PSRenderLink fileLink(PSRenderLinkContext linkContext, String resourceDefinitionId)
    {
        try {
            return renderLinkService.renderLink(linkContext, resourceDefinitionId);
        } catch (PSDataServiceException e) {
            log.error(LOG_ERROR_DEFAULT,"fileLink", e.getMessage());
            log.debug(e.getMessage(),e);
            return new PSRenderLink("#",null);
        }
    }

    @IPSJexlMethod(description = "Renders a Link to a theme CSS file", params =
            {@IPSJexlParam(name = "linkContext", description = "The link context. Use $perc.linkContext"),
                    @IPSJexlParam(name = "theme", description = "theme name")}, returns = "PSRenderLink")
    public PSRenderLink themeLink(PSRenderLinkContext linkContext, String theme)
    {
        try {
            theme = isNotBlank(theme) ? theme : "percussion";
            return renderLinkService.renderLink(linkContext, "theme." + theme);
        } catch (PSDataServiceException e) {
            log.error(LOG_ERROR_DEFAULT,"themeLink", e.getMessage());
            log.debug(e.getMessage(),e);
            return new PSRenderLink("#",null);
        }
    }

    @IPSJexlMethod(description = "Renders a Link to the region's CSS file of a theme", params =
            {@IPSJexlParam(name = "linkContext", description = "The link context. Use $perc.linkContext"),
                    @IPSJexlParam(name = "theme", description = "theme name"),
                    @IPSJexlParam(name = "isEdit", description = "is edit mode"),
                    @IPSJexlParam(name = "editType", description = "edit type")}, returns = "PSRenderLink")
    public PSRenderLink themeRegionCssLink(PSRenderLinkContext linkContext, String theme, Boolean isEdit,
                                           EditType editType)
    {
        try {
            theme = isNotBlank(theme) ? theme : "percussion";
            return renderLinkService.renderLinkThemeRegionCSS(linkContext, theme, isEdit, editType);
        } catch (IPSDataService.PSThemeNotFoundException | PSValidationException | IPSResourceDefinitionService.PSResourceDefinitionInvalidIdException e) {
            log.error(LOG_ERROR_DEFAULT,"themeRegionCssLink", e.getMessage());
            log.debug(e.getMessage(),e);
            return new PSRenderLink("#",null);
        }
    }



    /**
     * Gets the contents (or items) of a widget that are relate to the specified
     * page and/or template.
     * <p>
     * Note, if the specified item is a page , then the returned items are
     * relate to the page's template if there is any; otherwise return the items
     * that are relate to the page itself.
     *
     * @param item the page or template item, never <code>null</code>.
     * @param widget the ID of the widget, never blank.
     * @param finderName the name of the widget content finder. It defaults to
     *            {@link #DEFAULT_WIDGET_CONTENT_FINDER}. if the finder name is
     *            not specified.
     * @param params extra parameters for the finder, may be <code>null</code>
     *            or empty.
     *
     * @return a list of asset items.
     *
     */
    @IPSJexlMethod(description = "Gets the items of a widget that are relate to the specified page and/or template.  If not found it will look for same widget on landing page and then ancestor landing pages", params =
            {@IPSJexlParam(name = "item", description = "the parent (page/template) assembly item"),
                    @IPSJexlParam(name = "widget", description = "the widget or the widget ID"),
                    @IPSJexlParam(name = "finder", description = "the fully qualified name of the content finder"),
                    @IPSJexlParam(name = "params", description = "extra parameters to the process"),
                    @IPSJexlParam(name = "regionName", description = "Optional regionName to find widget")}, returns = "list of assembly items")
    public List<PSRenderAsset> firstAncestorWidgetContents(IPSAssemblyItem item, Object widget, String finderName,
                                                           Map<String, Object> params, String regionName)
    {
        PSWidgetInstance instance = getWidgetInstance(widget);

        String widgetName = instance.getDefinition().getId();
        // first try and find the widget in the regular way
        List<PSRenderAsset> widgetContents = widgetContents(item, widget, finderName, params, false);
        if (CollectionUtils.isEmpty(widgetContents))
        {
            // Find the Current Navigation node.
            String finder = "Java/global/percussion/widgetcontentfinder/perc_NavWidgetContentFinder";
            List<PSRenderAsset> navSelfList = widgetContents(item,widget,finder,null,true);
            if (CollectionUtils.isEmpty(navSelfList))
                return new ArrayList<>();

            IPSProxyNode selfNode = (IPSProxyNode)navSelfList.get(0).getNode();

            List<Node> ancestors = selfNode.getAncestors();

            ancestors.add(selfNode);

            IPSAssemblyItem landingAssemblyItem = (IPSAssemblyItem )item.clone();

            for (int i = ancestors.size() - 1; i >= 0; i--)
            {
                Node navNode = ancestors.get(i);
                PSContentNode langingPageNode = null;
                try
                {
                    // Try to get landing page from current node.
                    Property landingPageProp = navNode.getProperty("nav:landingPage");
                    langingPageNode = (PSContentNode)landingPageProp.getNode();
                    // If the original page was a landing page we don't need to recheck.
                    if (langingPageNode.getGuid() == item.getId())
                        break;
                }
                catch (PathNotFoundException e)
                {
                    log.debug("Cannot get landing page, skipping", e);

                }
                catch (RepositoryException e)
                {
                    log.debug("Cannot get landing page, skipping", e);

                }
                if (langingPageNode == null ) break;


                IPSGuid guid = langingPageNode.getGuid();

                landingAssemblyItem.setId(guid);
                landingAssemblyItem.removeParameterValue(IPSHtmlParameters.SYS_FOLDERID);
                landingAssemblyItem.setParameterValue(IPSHtmlParameters.SYS_CONTENTID,String.valueOf(guid.getUUID()));
                landingAssemblyItem.getBindings().remove("$_previewTemplate");
                landingAssemblyItem.getBindings().remove("$_previewPage");

                // If region name is null then all regions will be searched.
                List<PSWidgetInstance> newWidgetInstances = findWidgetInstances(landingAssemblyItem, regionName,widgetName);
                if (!CollectionUtils.isEmpty(newWidgetInstances))
                {
                    PSWidgetInstance newWidgetInstance = newWidgetInstances.get(0);
                    List<PSRenderAsset> testWidget = widgetContents(landingAssemblyItem,newWidgetInstance,null,null,false);
                    if (!CollectionUtils.isEmpty(testWidget))
                        return testWidget;
                }
            }

            return new ArrayList<>();

        }
        return widgetContents;
    }


    /**
     * Gets the contents (or items) of a widget that are relate to the specified
     * page and/or template.
     * <p>
     * Note, if the specified item is a page , then the returned items are
     * relate to the page's template if there is any; otherwise return the items
     * that are relate to the page itself.
     *
     * @param item the page or template item, never <code>null</code>.
     * @param widget the ID of the widget, never blank.
     * @param finderName the name of the widget content finder. It defaults to
     *            {@link #DEFAULT_WIDGET_CONTENT_FINDER}. if the finder name is
     *            not specified.
     * @param params extra parameters for the finder, may be <code>null</code>
     *            or empty.
     *
     * @return a list of asset items.
     *
     */
    @IPSJexlMethod(description = "Gets the items of a widget that are relate to the specified page and/or template.", params =
            {@IPSJexlParam(name = "item", description = "the parent (page/template) assembly item"),
                    @IPSJexlParam(name = "widget", description = "the widget or the widget ID"),
                    @IPSJexlParam(name = "finder", description = "the fully qualified name of the content finder"),
                    @IPSJexlParam(name = "params", description = "extra parameters to the process")}, returns = "list of assembly items")
    public List<PSRenderAsset> widgetContents(IPSAssemblyItem item, Object widget, String finderName,
                                              Map<String, Object> params)
    {
        return widgetContents(item, widget, finderName, params, false);
    }

    @IPSJexlMethod(description = "Returns if item is in recycler.", params =
            {@IPSJexlParam(name = "itemId", description = "itemId")}, returns = "boolean")
    public Boolean isInRecycler(String itemId)
    {
        if (StringUtils.isBlank(itemId))
            return Boolean.FALSE;
        try {
            List<PSRelationshipData> psRelationshipDataList = relationshipService.findByDependentIdConfigId(idMapper.getContentId(itemId), PSRelationshipConfig.ID_RECYCLED_CONTENT);
            if (!psRelationshipDataList.isEmpty()) {
                return Boolean.TRUE;
            }
        }catch (Exception e){
            //incase any exception happens because of some reason
            log.error("isInRecycle check failed for item id: {}", itemId,e);
        }
        return  Boolean.FALSE;
    }

    /**
     * If the user entered format is within the SimpleDateFormat pattern string
     * then returns the date string according to it Ex: if the format is "a"
     * then it is a valid one and return string would be AM/PM.. If other than
     * the SimpleDateFormat supported pattern string then it returns the
     * defaultFormat "EEE MMM d, yyyy 'at' hh:mm a"
     *
     * @param format
     * @param defaultFormat
     * @return
     */
    @IPSJexlMethod(description = "Verify if the entered date format is correct or not for the SimpleDateFormat. If not system returns the defulat error format", params =
            {@IPSJexlParam(name = "format", description = "Date Format entered by user"),
                    @IPSJexlParam(name = "defaultFormat", description = "System Default format")}, returns = "dateformat")
    public String parseDateFormat(String format, String defaultFormat)
    {
        String resultFormat = null;
        if (StringUtils.isNotBlank(format))
        {
            try
            {
                FastDateFormat dFormat = FastDateFormat.getInstance(format);
                dFormat.format(new Date());
                resultFormat = format;
            }
            catch (Exception e)
            {
                log.error("Failed for the user entered format : {}" , format);
            }
        }
        // If user entered format doesn't work then go to the following
        // condition to process the defaultFormat
        if (resultFormat == null && StringUtils.isNotBlank(defaultFormat))
        {
            try
            {
                FastDateFormat dFormat = FastDateFormat.getInstance(defaultFormat);
                dFormat.format(new Date());
                resultFormat = defaultFormat;
            }
            catch (Exception e)
            {
                log.error("Failed for the default format: {}" , defaultFormat);
            }
        }
        return resultFormat != null ? resultFormat : "EEE MMM d, yyyy 'at' hh:mm a";
    }

    @IPSJexlMethod(description = "Parse the metadata string into key/value pairs", params =
            {@IPSJexlParam(name = "metadata", description = "Metadata string")}, returns = "Map of key/value pairs")
    public Map<String, String> parseSoProMetadata(String metadata)
    {
        JSONObject jsonObject = (JSONObject) JSONSerializer.toJSON(metadata);
        Map<String, String> map = new Hashtable<>();

        for (Object key : jsonObject.keySet())
        {
            Object value = jsonObject.get(key);
            map.put(key.toString(), value.toString());
        }

        return map;
    }

    /**
     * Method to update the managed links during the assembly. This is a thin
     * wrapper, calls IPSManagedLinkService for actual work.
     *
     * @param linkContext The render link context assumed not <code>null</code>.
     * @param source The source String in which the links needs to be updated,
     *            may be <code>null</code> or empty. If blank returns blank.
     * @return String updated source html.
     */
    @IPSJexlMethod(description = "Updates the managed links in the given source", params =
            {
                    @IPSJexlParam(name = "linkContext", description = "The link context. Use $perc.linkContext"),
                    @IPSJexlParam(name = "source", description = "The source html in which the managed links needs to be updated."),
                    @IPSJexlParam(name = "parentId", description = "The parentId for the item under management")}, returns = "managed links updated source")
    public String updateManagedLinks(PSRenderLinkContext linkContext, String source, String parentId)
    {
        if (StringUtils.isBlank(source))
            return source;
        return managedLinkService.renderLinks(linkContext, source, Boolean.FALSE, Integer.parseInt(parentId));
    }

    /**
     * Method to update the managed links in a JSON payload during  assembly. This is a thin
     * wrapper, calls IPSManagedLinkService for actual work.
     *
     * @param linkContext The render link context assumed not <code>null</code>.
     * @param source The source String in which the links needs to be updated,
     *            may be <code>null</code> or empty. If blank returns blank.
     * @return String updated source html.
     */
    @IPSJexlMethod(description = "Updates the managed links in the given source", params =
            {
                    @IPSJexlParam(name = "linkContext", description = "The link context. Use $perc.linkContext"),
                    @IPSJexlParam(name = "source", description = "The source JSON in which the managed links needs to be updated.")}, returns = "managed links updated source")
    public String updateManagedLinksInJSON(PSRenderLinkContext linkContext, String source)
    {
        if (StringUtils.isBlank(source))
            return source;

        return managedLinkService.renderLinksInJSON(linkContext, source, Boolean.FALSE);
    }

    /**
     * Method to update the managed links during the assembly. This is a thin
     * wrapper, calls IPSManagedLinkService for actual work.
     *
     * @param linkContext The render link context assumed not <code>null</code>.
     * @param source The source String in which the links needs to be updated,
     *            may be <code>null</code> or empty. If blank returns blank.
     * @return String updated source html.
     */
    @IPSJexlMethod(description = "Updates the managed links in the given source", params =
            {
                    @IPSJexlParam(name = "linkContext", description = "The link context. Use $perc.linkContext"),
                    @IPSJexlParam(name = "source", description = "The source html in which the managed links needs to be updated."),
                    @IPSJexlParam(name = "serverId", description = "The serverId to determine whether it is a staging publishing or not."),
                    @IPSJexlParam(name = "parentId", description = "The parentId for the item under management")}, returns = "managed links updated source")
    public String updateManagedLinks(PSRenderLinkContext linkContext, String source, Long serverId, String parentId)
    {
        if (StringUtils.isBlank(source))
            return source;
        return managedLinkService.renderLinks(linkContext, source, isStagingServer(serverId), Integer.parseInt(parentId));
    }

    /**
     * Checks whether pub server corresponding to the server id is a staging
     * server or not. If server id is <code>null</code> or not valid then
     * returns <code>false</code>
     *
     * @param serverId
     * @return <code>true</code> if server id is not null and a pub server
     *         exists corresponding to the server id and it is a staging pub
     *         server.
     */
    private Boolean isStagingServer(Long serverId)
    {
        Boolean isStaging = Boolean.FALSE;
        PSPubServer pubServer = null;
        if (serverId != null)
        {
            try
            {
                pubServer = pubServerService.findPubServer(serverId);
                isStaging = pubServer != null && pubServer.getServerType().equals(PSPubServer.STAGING);
            }
            catch (Exception e)
            {
                log.warn("Error finding the server corresponding to the supplied server id (" + serverId
                        + "(, treating is not a staging server");
            }
        }
        return isStaging;
    }

    /**
     * Method to render the managed item apth during the assembly. This is a
     * thin wrapper, calls IPSManagedLinkService for actual work.
     *
     * @param linkContext The render link context assumed not <code>null</code>.
     * @param linkId The id of the link to render, if <code>null</code> or empty
     *            returns "#".
     * @return The rendered path
     */
    @IPSJexlMethod(description = "Renders the item path for the given link id", params =
            {@IPSJexlParam(name = "linkContext", description = "The link context. Use $perc.linkContext"),
                    @IPSJexlParam(name = "linkId", description = "The id of the link that needs to be updated.")}, returns = "the rendered path, or \"#\" if not rendered")
    public String renderManagedItemPath(PSRenderLinkContext linkContext, String linkId)
    {

        return managedLinkService.renderItemPath(linkContext, linkId, null);
    }

    /**
     * Method to render the managed item apth during the assembly. This is a
     * thin wrapper, calls IPSManagedLinkService for actual work.
     *
     * @param linkContext The render link context assumed not <code>null</code>.
     * @param linkId The id of the link to render, if <code>null</code> or empty
     *            returns "#".
     * @return The rendered path
     */
    @IPSJexlMethod(description = "Renders the item path for the given link id", params =
            {
                    @IPSJexlParam(name = "linkContext", description = "The link context. Use $perc.linkContext"),
                    @IPSJexlParam(name = "linkId", description = "The id of the link that needs to be updated."),
                    @IPSJexlParam(name = "serverId", description = "The serverId to determine whether it is a staging publishing or not.")}, returns = "the rendered path, or \"#\" if not rendered")
    public String renderManagedItemPath(PSRenderLinkContext linkContext, String linkId, Long serverId)
    {
        return managedLinkService.renderItemPath(linkContext, linkId, isStagingServer(serverId));
    }

    /**
     * Gets the contents (or items) of a widget that are relate to the specified
     * page and/or template.
     * <p>
     * Note, if the specified item is a page and
     * <code>processPageAssetOnly</code> is <code>false</code>, then the
     * returned items are relate to the page's template if there is any.
     *
     * @param item the page or template item, never <code>null</code>.
     * @param widgetParam the ID of the widget, never blank.
     * @param finderName the name of the widget content finder. It defaults to
     *            {@link #DEFAULT_WIDGET_CONTENT_FINDER}. if the finder name is
     *            not specified.
     * @param params extra parameters for the finder, may be <code>null</code>
     *            or empty.
     * @param processPageAssetOnly if <code>true</code>, then return items that
     *            are relate to the specified page item only; otherwise return
     *            empty list if the specified item is not a page.
     *
     * @return a list of asset items described above, never <code>null</code>,
     *         but be empty.
     *
     */
    @IPSJexlMethod(description = "Gets the items of a widget that are relate to the specified page and/or template.", params =
            {
                    @IPSJexlParam(name = "item", description = "the parent (page/template) assembly item"),
                    @IPSJexlParam(name = "widget", description = "the widget or the widget ID"),
                    @IPSJexlParam(name = "finder", description = "the fully qualified name of the content finder"),
                    @IPSJexlParam(name = "params", description = "extra parameters to the process"),
                    @IPSJexlParam(name = "processPageAssetOnly", description = "if true, then lookup lookup items relate to page only")}, returns = "list of assembly items")
    public List<PSRenderAsset> widgetContents(IPSAssemblyItem item, Object widgetParam, String finderName,
                                              Map<String, Object> params, boolean processPageAssetOnly)
    {
        notNull(item, "assemblyItem");
        PSWidgetInstance widget = getWidgetInstance(widgetParam);
        if (log.isDebugEnabled())
        {
            log.debug(format("Calling widget finder with widgetId:{0}, finderName:{1} and params:{2}", widget.getItem()
                    .getId(), finderName, params));
        }
        PSStopwatchStack sws = PSStopwatchStack.getStack();
        sws.start(getClass().getCanonicalName() + "#assemble");
        try
        {
            if (isBlank(finderName))
            {
                finderName = DEFAULT_WIDGET_CONTENT_FINDER;
                if (log.isDebugEnabled())
                {
                    log.debug("No finder defined for widget id \"" + widget.getItem().getId() + "\", defaulting to \""
                            + DEFAULT_WIDGET_CONTENT_FINDER + "\"");
                }
            }
            if (params == null)
                params = new HashMap<>();

            IPSWidgetContentFinder finder = getWidgetContentFinder(finderName);
            if (finder == null)
                throw new PSAssemblyException(IPSAssemblyErrors.MISSING_FINDER, finder);
            if (!isBlank(item.getUserName()))
            {
                // Need user name for preview filter rule
                params.put(IPSHtmlParameters.SYS_USER, item.getUserName());
            }

            String itemType = getAssemblyItemBridge().getContentType(item);
            boolean page = StringUtils.equalsIgnoreCase(IPSPageService.PAGE_CONTENT_TYPE, itemType);

            List<PSRenderAsset> widgetAssets = null;
            if (!page)
            {
                if (!processPageAssetOnly)
                    widgetAssets = toAssets(finder.find(item, widget, params));
                else
                    widgetAssets = new ArrayList<>();
            }
            else
            {
                if (!processPageAssetOnly)
                {
                    PSTemplate template = getAssemblyItemBridge().getTemplateAndPage(item).getTemplate();
                    IPSAssemblyItem templateAssembly = (IPSAssemblyItem) item.clone();
                    templateAssembly.setId(idMapper.getGuid(template.getId()));

                    params.put(IS_MATCH_BY_NAME, Boolean.FALSE);
                    widgetAssets = toAssets(finder.find(templateAssembly, widget, params));
                    params.put(IS_MATCH_BY_NAME, Boolean.TRUE);
                }

                if (widgetAssets == null || widgetAssets.isEmpty())
                {
                    widgetAssets = toAssets(finder.find(item, widget, params));
                }
            }

            return widgetAssets;
        }
        catch (Exception ae)
        {
            String errMsg = "Failed to find content for widget id=" + widget.getItem().getId()
                    + " while assemble item id=" + item.getId().toString();
            log.error(errMsg, ae);

            throw new RuntimeException(errMsg, ae);
        }
        finally
        {
            sws.stop();
            log.debug(sws);
        }
    }

    /**
     * Gets the tool-tip for the specified widget in edit mode.
     *
     * @param context the assembly page/template context, not <code>null</code>.
     * @param widgetInstance the widget instance, not <code>null</code>.
     * @param defaultTooltip the default tool-tip, this is used if not in edit
     *            mode or there is no widget name and/or description.
     *
     * @return the tool-tip, never <code>null</code> or empty.
     */
    @IPSJexlMethod(description = "Gets the tooltip of the specified widget.", params =
            {@IPSJexlParam(name = "context", description = "the assembly (page/template) context"),
                    @IPSJexlParam(name = "widget", description = "the widget instance"),
                    @IPSJexlParam(name = "defaultTooltip", description = "the default tooltip")}, returns = "tooltip")
    public String getWidgetTooltip(PSAbstractAssemblyContext context, PSWidgetInstance widgetInstance,
                                   String defaultTooltip)
    {
        if (!context.isEditMode())
            return defaultTooltip;

        PSWidgetItem widget = widgetInstance.getItem();
        if (isBlank(widget.getName()) && isBlank(widget.getDescription()))
            return defaultTooltip;

        if (isNotBlank(widget.getName()) && isNotBlank(widget.getDescription()))
            return widget.getName() + ": " + widget.getDescription();

        return isNotBlank(widget.getName()) ? widget.getName() : widget.getDescription();
    }

    // ====================================================
    /**
     * A utility method that collects the categories from the supplied list of
     * assembled pages and returns a hierarchical structure of unique categories
     * and the number of occurrence of each category. The returned object is a
     * PSCategoryTree object. The first element is the string category, the
     * second is a PSPair<Integer, Integer> which count the ocurrence of the
     * category of the current node and his childrens and the third is a list of
     * PSCategoryTree.
     *
     */
    @IPSJexlMethod(description = "Gets the processed list of categories.", params =
            {@IPSJexlParam(name = "categories", description = "String of comma separated categories")}, returns = "List of PSCategoryTree.")
    public List<PSCategoryTree> getProcessedCategories(List<PSRenderAsset> assemblyPages)
    {
        PSCategoryTree categoryTree = new PSCategoryTree("dummyRoot");
        try {

            if (assemblyPages == null)
                return categoryTree.getChildren();

            List<String> parsedCategories = new ArrayList<>();
            for (PSRenderAsset assembledPage : assemblyPages) {
                Node pageNode = assembledPage.getNode();
                if (pageNode.hasProperty("page_categories_tree")) {
                    Value[] values = pageNode.getProperty("page_categories_tree").getValues();
                    for (Value val : values) {
                        String valStr = val.getString().trim();
                        String valStrLbl = getCategoryByIdPath(valStr);
                        valStr = valStrLbl != null ? valStrLbl : valStr;
                        if (valStr.startsWith("/"))
                            valStr = valStr.substring(1);
                        processCategory(valStr, categoryTree.getChildren(), parsedCategories, "");
                    }
                    parsedCategories = new ArrayList<>();
                }
            }

            alphaOrderCategories(categoryTree);

        } catch (RepositoryException e) {
            log.error(LOG_ERROR_DEFAULT,"getProcessedCategories", e.getMessage());
            log.debug(e.getMessage(),e);
        }
        return categoryTree.getChildren();
    }

    private String getCategoryByIdPath(String path)
    {
        if (path.contains("/"))
            path = StringUtils.substringAfterLast(path, "/");
        return getCategoryMap().get(path);
    }

    @IPSJexlMethod(description = "Returns the category label path for the given category id path", params =
            {@IPSJexlParam(name = "catPath", description = "The category id path, if not found returns the idpath")}, returns = "String")
    public String getCategoryLabel(String catPath)
    {
        String labelPath = getCategoryByIdPath(catPath);
        return labelPath == null ? catPath : labelPath;
    }

    /**
     * Helper method to create category map. Loads the category tree xml file
     * and creates a map of id and label paths.
     *
     * @return categories map, never null may be empty.
     */
    private Map<String, String> getCategoryMap()
    {
        Map<String, String> catMap = new HashMap<>();
        String url = CATEGORY_URL;


        int index = url.indexOf("rx_resources");
        if (index == -1)
        {
            log.error("Category tree url location is not supported. URL:" + url);
            return catMap;
        }
        File catFile = getFileSystemService().getFile(url.substring(index + "rx_resources".length()));

        // Need to change the path for the file as the location for category xml
        // has changed along with the format.
        String correctedPath = catFile.getAbsolutePath().replace(WEB_RESOURCES, "rx_resources");
        catFile = null;
        catFile = new File(correctedPath);
        SAXBuilder builder = new SAXBuilder();
        builder.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        builder.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        Document document;
        try
        {
            document = builder.build(catFile);
            Element rootNode = document.getRootElement();
            String label = rootNode.getAttributeValue("title");
            String id = rootNode.getAttributeValue("id");
            if (StringUtils.isBlank(id))
                id = label;
            label = "/" + label;
            catMap.put(id, label);
            List<Element> children = rootNode.getChildren();
            if(!children.isEmpty()){
                Element topLevelElement =children.get(0);
                if( topLevelElement.getName()!=null && topLevelElement.getName().equalsIgnoreCase("topLevelNodes") )
                {
                    children = topLevelElement.getChildren();
                }
                for (Element elem : children)
                {
                    createCategoryMap(catMap, elem, label);
                }
            }

        }
        catch (JDOMException e)
        {
            log.error(e);
        }
        catch (IOException e)
        {
            log.error(e);
        }

        return catMap;
    }

    /**
     * A helper method to recursively build the map of id and label.
     *
     * @param map assumed not <code>null</code>, the map gets updated in
     *            recursive calls.
     * @param node assumed not <code>null</code>.
     * @param label assumed not <code>null</code>.
     */
    private void createCategoryMap(Map<String, String> map, Element node, String label)
    {
        if (node == null)
            return;
        String curLabel = node.getAttributeValue("title");
        String curId = node.getAttributeValue("id");
        if (StringUtils.isBlank(curId))
            curId = curLabel;

        label = label + "/" + curLabel;
        map.put(curId, label);
        List<Element> children = node.getChildren();
        for (Element elem : children)
        {
            createCategoryMap(map, elem, label);
        }
    }

    private void processCategory(String pathCategory, List<PSCategoryTree> childrens, List<String> parsedCategories,
                                 String currentPath)
    {
        if (!pathCategory.isEmpty())
        {
            int index = (pathCategory.indexOf('/') != -1) ? pathCategory.indexOf('/') : pathCategory.length();
            String category = (pathCategory.substring(0, index).trim());
            String sep = ((currentPath != "") ? "/" : "");
            currentPath = currentPath + sep + category;
            pathCategory = (index < pathCategory.length()) ? pathCategory.substring(index + 1).trim() : "";
            PSCategoryTree categoryNode = null;
            for (PSCategoryTree node : childrens)
            {
                if (node.getCategory().equalsIgnoreCase(category))
                {
                    categoryNode = node;
                    break;
                }
            }
            if (categoryNode == null)
            {
                categoryNode = new PSCategoryTree(category);
                childrens.add(categoryNode);
            }
            if (!parsedCategories.contains(currentPath))
            {
                if (pathCategory.equals(""))
                {
                    categoryNode.getCount().setFirst(categoryNode.getCount().getFirst() + 1);
                }
                else
                {
                    categoryNode.getCount().setSecond(categoryNode.getCount().getSecond() + 1);
                }
                parsedCategories.add(currentPath);
            }

            processCategory(pathCategory, categoryNode.getChildren(), parsedCategories, currentPath);
        }
    }

    /**
     * A utility method that sorts a list of tags alphabetically case
     * insensitive.
     *
     * @param arrayElements The list of tags.
     */
    public void alphaOrderTag(Value[] arrayElements)
    {
        int n = arrayElements.length;

        for (int pass = 1; pass < n; pass++)
        {
            for (int i = 0; i < n - pass; i++)
            {
                try
                {
                    if (arrayElements[i].getString().compareToIgnoreCase(arrayElements[i + 1].getString()) > 0)
                    {
                        Value temp = arrayElements[i];
                        arrayElements[i] = arrayElements[i + 1];
                        arrayElements[i + 1] = temp;
                    }
                }
                catch (Exception e)
                {
                    log.error(e);
                    throw new RuntimeException(e);
                }
            }
        }
    }

    /**
     * A utility method that gets the labels for list of categories and then
     * sorts, by the category without the whole path, alphabetically case
     * insensitive. For example if the values are [/A/B/C, /A/B] it compares C
     * and B.
     *
     * @param arrayElements The list of categories contemplating the whole path.
     */
    public void alphaOrderCategory(Value[] arrayElements)
    {
        int n = arrayElements.length;
        for (int i = 0; i < n; i++)
        {
            try
            {
                arrayElements[i] = PSValueFactory.createValue((Object) getCategoryLabel(arrayElements[i].getString()));
            }
            catch (Exception e)
            {
                // This should not happen as getCategoryLabels returns the
                // supplied string if it doesn't find a label
                log.error(e);
            }
        }
        for (int pass = 1; pass < n; pass++)
        {
            for (int i = 0; i < n - pass; i++)
            {
                try
                {
                    if (arrayElements[i]
                            .getString()
                            .substring(arrayElements[i].getString().lastIndexOf('/') + 1,
                                    arrayElements[i].getString().length())
                            .compareToIgnoreCase(
                                    arrayElements[i + 1].getString().substring(
                                            arrayElements[i + 1].getString().lastIndexOf('/') + 1,
                                            arrayElements[i + 1].getString().length())) > 0)
                    {
                        Value temp = arrayElements[i];
                        arrayElements[i] = arrayElements[i + 1];
                        arrayElements[i + 1] = temp;
                    }
                }
                catch (Exception e)
                {
                    log.error(e);
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public void alphaOrderCategories(PSCategoryTree categoryTree)
    {
        // Sort the children
        alphaOrderChildrens(categoryTree.getChildren());

        // Sort the children of the sorted children
        for (PSCategoryTree children : categoryTree.getChildren())
        {
            alphaOrderCategories(children);
        }
    }

    private void alphaOrderChildrens(List<PSCategoryTree> categoryTree)
    {
        int n = categoryTree.size();

        for (int pass = 1; pass < n; pass++)
        {
            for (int i = 0; i < n - pass; i++)
            {
                if (categoryTree.get(i).getCategory().compareToIgnoreCase(categoryTree.get(i + 1).getCategory()) > 0)
                {
                    PSCategoryTree temp = categoryTree.get(i);
                    categoryTree.set(i, categoryTree.get(i + 1));
                    categoryTree.set(i + 1, temp);
                }
            }
        }
    }

    /**
     * A utility method that collects the years and months from the supplied
     * list of assembled pages and returns a structure of unique months
     * categorized by year and the number of blogs of each year/month. Return a
     * list of PSBlogYear objects. Each PSBlogYear is formed with an string
     * element the string year, the count for the year and the list of months
     * with the number of blogs.
     *
     */
    @IPSJexlMethod(description = "Gets the processed list of blogs per month organized by year.", params =
            {@IPSJexlParam(name = "assemblyPages", description = "assembly pages")}, returns = "List of PSBlogArchive.")
    public List<PSBlogYear> getProcessedBlogs(List<PSRenderAsset> assemblyPages)
    {
        PSBlogEntry blogs = new PSBlogEntry();

        if (assemblyPages == null) {
            log.error(LOG_ERROR_DEFAULT,"itemLink", "assemblyPages pages must not be null");
            return new ArrayList<>();
        }

        for (PSRenderAsset assembledPage : assemblyPages)
        {
            try {
                Node pageNode = assembledPage.getNode();
                Calendar date = Calendar.getInstance();
                if (pageNode.hasProperty("sys_contentpostdate")
                        && pageNode.getProperty("sys_contentpostdate").getValue() != null) {
                    date = pageNode.getProperty("sys_contentpostdate").getValue().getDate();
                }
                PSBlogYear selectedYear = null;
                PSBlogMonth selectedMonth = null;
                Integer currentYear = date.get(Calendar.YEAR);
                String currentMonth = date.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault());
                for (PSBlogYear year : blogs.getYears()) {
                    if (year.getYear().equals(currentYear)) {
                        selectedYear = year;
                        break;
                    }
                }
                if (selectedYear == null) {
                    selectedYear = new PSBlogYear(date.get(Calendar.YEAR));
                }
                if (selectedYear != null) {
                    for (PSBlogMonth month : selectedYear.getMonths()) {
                        if (month.getMonth().equals(currentMonth)) {
                            selectedMonth = month;
                            break;
                        }
                    }
                    if (selectedMonth != null) {
                        selectedYear.setYearCount(selectedYear.getYearCount() + 1);
                        selectedMonth.setCount(selectedMonth.getCount() + 1);
                    }
                }

                blogs.getYears().add(selectedYear);
            } catch (RepositoryException e) {
                log.error(LOG_ERROR_DEFAULT,"getProcessedBlogs", e.getMessage());
                log.debug(e.getMessage(),e);
            }
        }

        List<PSBlogYear> blogYears = new ArrayList<>();

        blogYears.addAll(blogs.getYears());

        Comparator<PSBlogYear> comp = new YearOrderBlogsComparator();
        Collections.sort(blogYears, comp);

        return blogYears;
    }

    /**
     * A utility method that collects the pages collected from the system, that
     * match with the calendar name passed as parameter. Return a JSONArray
     * object that have a list of fields, with the values read from the list of
     * pages found, such as title, start date, end date and summary.
     *
     * @throws RepositoryException
     * @throws IllegalStateException
     * @throws PathNotFoundException
     * @throws ValueFormatException
     * @throws ParseException
     */
    @IPSJexlMethod(description = "Gets the processed list of pages that have set supplied calendar.", params =
            {@IPSJexlParam(name = "calendarName", description = "The name of the calendar")}, returns = "JSONArray object")
    public JSONArray getPagesForCalendar(String calendarName) throws  RepositoryException, ParseException {
        try {
            JSONArray pagesForCal = new JSONArray();
            List<Integer> ids = pageDao.getPageIdsByFieldNameAndValue("page_calendar", calendarName);

            // Convert input string into a date
            FastDateFormat inputFormat = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss.S");

            // don't output time-zone as the application is not time-zone aware
            // fullCalendar.js lib supports ISO-8601 formatted date/time values
            FastDateFormat outputFormat = FastDateFormat.getInstance("yyyy-MM-dd'T'HH:mm:ss");

            for (Integer id : ids) {
                PSLegacyGuid guid = new PSLegacyGuid(id, -1);
                String sid = idMapper.getString(guid);

                // Find the content item for the given id
                PSContentItem contentItem = getContentItemDao().find(sid);
                Map<String, Object> fields = contentItem.getFields();

                // Find the path for the item and strip the site from the url
                String[] paths = getItemPath(sid).split("/");
                String pageUrl = StringUtils.EMPTY;
                // Starts from three because the array has the format [, Sites,
                // TestSite, page1] and has to remove the site name
                for (int i = 3; i < paths.length; i++) {
                    pageUrl = pageUrl + "/" + paths[i];
                }
                // Get the values from the fields
                String title = StringUtils.isNotBlank((String) fields.get("resource_link_title")) ? (String) fields
                        .get("resource_link_title") : "";
                String summary = StringUtils.isNotBlank((String) fields.get("page_summary")) ? (String) fields
                        .get("page_summary") : "";

                Date startDate = null;
                if (fields.get("page_start_date") != null) {
                    startDate = inputFormat.parse((String) fields.get("page_start_date"));
                }

                Date endDate = null;
                if (fields.get("page_end_date") != null) {
                    endDate = inputFormat.parse((String) fields.get("page_end_date"));
                }
                JSONObject pageCalItem = new JSONObject();
                pageCalItem.put("title", title);
                pageCalItem.put("summary", summary);
                pageCalItem.put("start", (startDate == null) ? StringUtils.EMPTY : outputFormat.format(startDate));
                if (endDate != null) {
                    pageCalItem.put("end", outputFormat.format(endDate));
                }
                pageCalItem.put("url", pageUrl);
                pageCalItem.put("allDay", false);
                pageCalItem.put("textColor", StringUtils.EMPTY);
                pageCalItem.put("textBackground", StringUtils.EMPTY);
                pagesForCal.add(pageCalItem);
            }

            return pagesForCal;
        } catch (PSDataServiceException e) {
            log.error(e.getMessage());
            log.debug(e.getMessage(),e);
           return new JSONArray();
        }
    }

    // ====================================================
    /**
     * A utility method that collects the tags from the supplied list of
     * assembled pages and returns a list of unique tags and the number of
     * occurrence of each tag. The returned object is a list of PSPair objects,
     * the first object is the String tag and the second object is Integer
     * count. The returned list is sorted by the supplied sortOption, either
     * alpha and count, defaults to alpha and any value other than count is
     * treated as alpha.
     *
     * @throws RepositoryException
     * @throws IllegalStateException
     * @throws PathNotFoundException
     * @throws ValueFormatException
     */
    @IPSJexlMethod(description = "Gets the processed list of tags.", params =
            {
                    @IPSJexlParam(name = "tags", description = "String of comma separated tags"),
                    @IPSJexlParam(name = "sortOption", description = "either alpha and count, defaults to alpha and any value other "
                            + "than count is treated as alpha")}, returns = "List of PSPair of String and Integers.")
    public List<PSPair<String, Integer>> getProcessedTags(List<PSRenderAsset> assemblyPages, String sortOption)
            throws RepositoryException
    {
        if (assemblyPages == null)
            throw new IllegalArgumentException("assemblyPages pages must not be null");
        List<String> tags = new ArrayList<>();
        for (PSRenderAsset assembledPage : assemblyPages)
        {
            Node node = assembledPage.getNode();
            if (node.hasProperty("page_tags"))
            {
                Value[] values = node.getProperty("page_tags").getValues();
                for (Value val : values)
                {
                    tags.add(val.getString());
                }
            }
        }
        return collapseStrings(tags, sortOption);
    }

    /**
     * Helper method that takes a list of non unique strings and returns a list
     * of unique strings along with the number of occurrence of each string as
     * PSPair objects with the first object as String and second one as Integer.
     * The returned list is sorted based on the sortOption parameter. For
     * example, if the incoming list is [foo, bar, foo, cat, bar, dog, bar] and
     * sortOption is alpha. Then the returned list will be like
     * [bar(3),cat(1),dog(1),foo(2)]. If the sortOption is count then the
     * returned list will be like [bar(3),foo(2),cat(1),dog(1)].
     *
     * @param tags List of non-unique strings, must not be <code>null</code>.
     * @param sortOption
     * @return
     */
    private List<PSPair<String, Integer>> collapseStrings(List<String> tags, String sortOption)
    {
        Map<String, Integer> tagMap = new HashMap<>();
        for (String tag : tags)
        {
            if (tagMap.containsKey(tag))
            {
                tagMap.put(tag, tagMap.get(tag) + 1);
            }
            else
            {
                tagMap.put(tag, new Integer(1));
            }
        }
        List<PSPair<String, Integer>> tagResultList = new ArrayList<>();
        for (Entry<String, Integer> entry : tagMap.entrySet())
        {
            tagResultList.add(new PSPair<>(entry.getKey(), entry.getValue()));
        }
        Comparator<PSPair<String, Integer>> comp = new AlphaOrderTagComparator();
        if (sortOption.equalsIgnoreCase("count"))
        {
            comp = new CountOrderTagComparator();
        }
        Collections.sort(tagResultList, comp);
        return tagResultList;
    }

    class AlphaOrderTagComparator implements Comparator<PSPair<String, Integer>>
    {
        @Override
        public int compare(PSPair<String, Integer> o1, PSPair<String, Integer> o2)
        {
            return o1.getFirst().compareTo(o2.getFirst());
        }
    }

    class CountOrderTagComparator implements Comparator<PSPair<String, Integer>>
    {
        @Override
        public int compare(PSPair<String, Integer> o1, PSPair<String, Integer> o2)
        {
            return o1.getSecond().equals(o2.getSecond()) ? o1.getFirst().compareTo(o2.getFirst()) : o2.getSecond()
                    .compareTo(o1.getSecond());
        }
    }

    class YearOrderBlogsComparator implements Comparator<PSBlogYear>
    {
        @Override
        public int compare(PSBlogYear o1, PSBlogYear o2)
        {
            return o2.getYear().compareTo(o1.getYear());
        }
    }

    /**
     * Returns the list of template names for the given template ids
     *
     * @param templateIds comma separated template ids, and id is expected in
     *            guid format never <code>null</code>, but be empty.
     * @return a list template names, never <code>null</code>, but be empty.
     */
    @IPSJexlMethod(description = "gets list of template names", params =
            {@IPSJexlParam(name = "templateIds", description = "String of comma separated template ids")}, returns = "List of template names")
    public List<String> templateNames(String templateIds)
    {
        try {
            List<String> templateNames = new ArrayList<>();
            String[] ids = StringUtils.split(templateIds, ',');
            for (String id : ids) {
                PSTemplateSummary summary = templateService.find(id);
                if (summary != null) {
                    templateNames.add(summary.getName());
                }
            }
            return templateNames;
        } catch (PSDataServiceException e) {
            log.error(LOG_ERROR_DEFAULT,"templateNames",e.getMessage());
            log.debug(e.getMessage(),e);
            return new ArrayList<>();
        }
    }

    /**
     * Find a specified workflow from its ID
     *
     * @param workflowId the ID of the specified workflow, not <code>null</code>
     *            .
     * @return the workflow. It may be <code>null</code> if the workflow does
     *         not exist.
     */
    @IPSJexlMethod(description = "find a workflow from ID", params =
            {@IPSJexlParam(name = "workflowId", description = "the ID of the workflow")}, returns = "the workflow. It may be null if not exist.")
    public PSWorkflow findWorkflow(Integer workflowId)
    {
        IPSGuid id = new PSGuid(PSTypeEnum.WORKFLOW, workflowId);
        IPSWorkflowService srv = PSWorkflowServiceLocator.getWorkflowService();
        return srv.loadWorkflow(id);
    }

    /**
     * Gets the blog post template id for the specified blog.
     *
     * @param path of the blog, never blank.
     *
     * @return id of the blog post template, <code>null</code> if the path does
     *         not represent a blog.
     *
     */
    @IPSJexlMethod(description = "Gets the blog post template id for the specified blog.", params =
            {@IPSJexlParam(name = "path", description = "the folder path of the blog")}, returns = "id of the blog post template")
    public String getBlogPostTemplateId(String path)
    {
        notEmpty(path);

        return siteSectionService.getBlogPostTemplateId(path);
    }

    /**
     * Gets the dependent content id of the specified link
     *
     * @param linkId the id of the link, may be <code>null<code/> or empty.
     *
     * @return The item content id, or an empty string if the supplied linkId is
     *         not valid
     */
    @IPSJexlMethod(description = "Gets the dependent content id of the specified link.", params =
            {@IPSJexlParam(name = "linkId", description = "the id of the link")}, returns = "The dependent item content id, or an empty string if the supplied linkId is not valid")
    public String getManagedLinkDependentId(String linkId)
    {
        long linkIdVal = NumberUtils.toLong(linkId, -1);

        String depId = "";
        if (linkIdVal == -1)
            return depId;

        PSManagedLink link = managedLinkDao.findLinkByLinkId(linkIdVal);
        if (link != null)
            depId = String.valueOf(link.getChildId());

        return depId;
    }

    @IPSJexlMethod(description = "Find an item and return specified field values.", params =
            {@IPSJexlParam(name = "type", description = "the content type name of the item"),
                    @IPSJexlParam(name = "fields", description = "A comma delimited list of fields to return"),
                    @IPSJexlParam(name = "contentId", description = "The content id of the item to find")}, returns = "A map of the specified field names and their values as strings, values are escaped for html")
    public Map<String, String> findItemFieldValues(String type, String fields, String contentId) {

            notEmpty(type);
            notEmpty(fields);
            notEmpty(contentId);

            List<String> selectFields = Arrays.asList(fields.split("\\s*,\\s*"));

            PSJcrNodeFinder nodeFinder = new PSJcrNodeFinder(contentMgr, type, IPSHtmlParameters.SYS_CONTENTID);
            Map<String, String> result = nodeFinder.find(selectFields, contentId);
            for (Map.Entry<String, String> entry : result.entrySet()) {
                entry.setValue(StringEscapeUtils.escapeHtml(entry.getValue()));
            }

            return result;

    }


    @IPSJexlMethod(description = "Get a list of child categories from a root path, or top level categories if not specified", params =
            {@IPSJexlParam(name = "siteName", description = "Tne sitename of all sites if null or empty"),
                    @IPSJexlParam(name = "rootPath", description = "The root path, can find by title or guid,  if relative path first match will be returned")}, returns = "A list of PSCategoryNode objects")
    public List<PSCategoryNode> getCategoryNodes(String site, String rootPath)
    {
        try {
            PSCategory category = PSCategoryControlUtils.getCategories(site, rootPath, false, true);
            return category.getTopLevelNodes();
        } catch (PSDataServiceException e) {
            log.error(LOG_ERROR_DEFAULT,"getCategoryNodes",e.getMessage());
            log.debug(e.getMessage(),e);
            return new ArrayList<>();
        }
    }

    @IPSJexlMethod(description = "Get item's category json string value and prepare the information in a map and return.", params =
            {@IPSJexlParam(name = "fieldValue", description = "the category drop down value that is to be displayed"),
                    @IPSJexlParam(name = "fieldName", description = "the name of the category drop down field"),
                    @IPSJexlParam(name = "siteName", description = "the name of the site")}, returns = "A map of the category drop down values, where is key is the drop down fieldname appended with a count and value is a list of PSCategoryNode. "
            + "This list has PSCategoryNode that belong to the specific dropdown. The selected categories are marked as selected true in their object.")
    public Map<String, List<PSCategoryNode>> getCategoryDropDownValues(String fieldValue, String fieldName,
                                                                       String siteName)
    {
        try {
            notEmpty(fieldValue);
            notEmpty(fieldName);
            // Prepare the data Structure that needs to be returned.
            // This List contains the category nodes with one/more marked as
            // selected.
            List<PSCategoryNode> nodeList;
            // This is the map which will be returned back to the template. This
            // will have key as the drop down number and value will be the combo
            // map.
            Map<String, List<PSCategoryNode>> templateMap = new HashMap<>();
            PSCategoryNode parentNode = null;
            PSCategoryNode prevParentNode = null;
            int fieldCounter = 0;

            // Get the persisted drop down field value.

            JSONArray jsonArray = JSONArray.fromObject(fieldValue);

            // Get the categories from the category xml, so that the relevant
            // map can be populated.
            PSCategory category = PSCategoryControlUtils.getCategories(siteName, null, false, true);

            if (category.getTopLevelNodes() != null && !category.getTopLevelNodes().isEmpty()) {
                List<PSCategoryNode> nodes = category.getTopLevelNodes();
                for (int n = 0; n < jsonArray.size(); n++) {
                    JSONObject jObj = jsonArray.getJSONObject(n);

                    for (Object key : jObj.keySet()) {
                        if (((String) key).equalsIgnoreCase("id")) {
                            parentNode = getParentNode(nodes, jObj.getString((String) key), parentNode);
                            if (parentNode != null) {
                                nodes = parentNode.getChildNodes();
                                if (!parentNode.equals(prevParentNode))
                                    fieldCounter = fieldCounter + 1;
                                prevParentNode = parentNode;
                            }
                            nodeList = getCategoryList(nodes, jObj.getString((String) key));
                            templateMap.put(fieldName + fieldCounter, nodeList);
                        }
                    }
                }
            }
            return new TreeMap<>(templateMap);
        } catch (PSDataServiceException e) {
            log.error(LOG_ERROR_DEFAULT,"getCategoryDropDownValues",e.getMessage());
            log.debug(e.getMessage(),e);
            return new TreeMap<>();
        }
    }

    @IPSJexlMethod(description = "Find a category node by the category path", params = {
            @IPSJexlParam(name = "categoryPath", description = "the category path")
    },
            returns = "A PSCategoryNode object")
    public PSCategoryNode getCategoryByPath(String categoryPath)
    {
        if (StringUtils.isEmpty(categoryPath))
            return null;
        return  PSCategoryControlUtils.findCategoryNode(null, categoryPath, false, true);

    }


    /**
     * The pages and widgets are assembled as a chain of assembly items. When a
     * sub item is assembled a clone of the assembly item is created and the
     * original item is made available from getCloneParentItem() if this value
     * returns null then we are at the top most original assembly items This
     * means the item that is being directly assembled, e.g. the page. By
     * getting hold of this object the widgets can modify the bindings of the
     * page. This can be used to modify the metadata map that is extracted when
     * delivering the page. No assumption can be made about the order that the
     * widgets are rendered in and the widgets may be assembled concurrently.
     *
     *
     * @param asmItem
     * @return  The root AssemblyItem
     */
    @SuppressWarnings("unchecked")
    @IPSJexlMethod(description = "Get the Root Assembly Item,  e.g. the page being rendered", params =
            {@IPSJexlParam(name = "asmItem", description = "The current assemblyItem usually $sys.assemblyItem")}, returns = "The top level IPSAssemblyItem")
    public IPSAssemblyItem getRootAssemblyItem(IPSAssemblyItem asmItem)
    {
        notNull(asmItem);
        IPSAssemblyItem currItem = asmItem;
        while (currItem.getCloneParentItem() != null)
        {
            currItem = currItem.getCloneParentItem();
        }
        return currItem;
    }

    /**
     * We check for the $sys.metadata binding on the parent assembly item. e.g.
     * page if we find it we return the object that individual widgets can add
     * to. If we do not find it we add it.
     *
     * @param param
     * @return The Metadata Map. The key is a string and the value must be a
     *         String or List of Strings.
     */
    @SuppressWarnings("unchecked")
    @IPSJexlMethod(description = "Get the page metadata map that a widget can add to.", params =
            {@IPSJexlParam(name = "asmItem", description = "The current assemblyItem usually $sys.assemblyItem")}, returns = "A Map.  The key is a String of the metada key.  The value is an Object but should only be populated with a String or List of Strings ")
    public Map<String, Object> getMetadataMap(Object param)
    {
        if ( !(param instanceof IPSAssemblyItem))
        {
            throw new IllegalArgumentException("Expecting $sys.assemblyItem");
        }
        IPSAssemblyItem asmItem = (IPSAssemblyItem)param;

        IPSAssemblyItem rootItem = getRootAssemblyItem(asmItem);
        Object sysObj = rootItem.getBindings().get("$sys");
        notNull(sysObj);
        if (!(sysObj instanceof Map<?, ?>))
        {
            throw new IllegalArgumentException("$sys is not a Map it is " + sysObj.getClass());
        }

        Map<String, Object> sysMap = (Map<String, Object>) sysObj;
        Object metaObj = sysMap.get("metadata");
        if (metaObj == null)
        {
            // double checked locking just in case multiple widgets accessing
            // page
            // Asynchronously
            synchronized (metalock)
            {
                metaObj = sysMap.get("metadata");

                if (metaObj == null)
                {
                    metaObj = new HashMap<String, Integer>();
                    sysMap.put("metadata", metaObj);
                }
            }
        }

        return (Map<String, Object>) metaObj;

    }

    private List<PSCategoryNode> getCategoryList(List<PSCategoryNode> nodes, String selectedId)
    {

        List<PSCategoryNode> nodeList = new ArrayList<>();

        for (PSCategoryNode node : nodes)
        {
            if (node.getId().equals(selectedId))
                node.setSelected(true);
            nodeList.add(node);
        }

        return nodeList;
    }

    private PSCategoryNode getParentNode(List<PSCategoryNode> nodes, String selectedId, PSCategoryNode pNode)
    {

        PSCategoryNode parentNode = pNode;
        boolean foundInList = false;

        for (PSCategoryNode node : nodes)
        {
            if (node.getId() != null && node.getId().equals(selectedId))
            {
                foundInList = true;
            }
        }

        if (!foundInList)
        {
            parentNode = null;
            for (PSCategoryNode node : nodes)
            {
                if (node.getChildNodes() != null && !node.getChildNodes().isEmpty())
                {
                    parentNode = getParentNode(node.getChildNodes(), selectedId, node);
                    if (parentNode != null)
                        break;
                }
            }
        }

        return parentNode;
    }

    private PSWidgetInstance getWidgetInstance(Object widget)
    {
        notNull(widget, "widget");
        if (!(widget instanceof PSWidgetInstance))
            throw new IllegalArgumentException(
                    "Must pass widget instance, e.g. $perc.widget, but cannot be  $perc.widget.item or $perc.widget.item.id");

        return (PSWidgetInstance) widget;
    }

    public List<PSRenderAsset> toAssets(List<IPSAssemblyItem> assemblyItems)
    {
        List<PSRenderAsset> list = new ArrayList<>();
        for (IPSAssemblyItem ai : assemblyItems)
        {
            list.add(toAsset(ai));
        }
        return list;
    }

    public PSRenderAsset toAsset(IPSAssemblyItem assemblyItem)
    {
        try {
            return assemblyItemBridge.createRenderAsset(assemblyItem);
        } catch (PSDataServiceException e) {
            log.error(LOG_ERROR_DEFAULT,"toAsset", e.getMessage());
            log.debug(e.getMessage(),e);
            return new PSRenderAsset();
        }
    }

    /**
     * Finds the specified widget content finder.
     *
     * @param finder the fully qualified extension name of the finder in
     *            question. It is default to
     *            {@link #DEFAULT_WIDGET_CONTENT_FINDER} if it is blank.
     *
     * @return the specified content finder, never <code>null</code>.
     */
    public static IPSWidgetContentFinder getWidgetContentFinder(String finder)
    {
        if (isBlank(finder))
            finder = DEFAULT_WIDGET_CONTENT_FINDER;

        IPSExtensionManager emgr = PSServer.getExtensionManager(null);
        try
        {
            PSExtensionRef ref = new PSExtensionRef(finder);
            return (IPSWidgetContentFinder) emgr.prepareExtension(ref, null);
        } catch (PSExtensionException | PSNotFoundException e) {
                log.error(LOG_ERROR_DEFAULT,"getWidgetContentFinder", e.getMessage());
                log.debug(e.getMessage(),e);
        }
        return null;
        }


        /**
     * Gets html content from a field and escapes the fields contents to html if
     * needed.
     * <p>
     * If the field is an html field then the contents will not be escaped. If
     * the field is not found an exception will be thrown.
     *
     * @param item never <code>null</code>.
     * @param fields never <code>null</code> or empty.
     * @return never <code>null</code>.
     * @see #html(Object, String, Object)
     */
    public String html(Object item, String fields)
    {
        return html(item, fields, null);
    }

    /**
     *
     * Gets html content from a field and escapes the fields contents to html if
     * needed.
     * <p>
     * If the field is an html field then the contents will not be escaped.
     *
     * @param item either a map, assembly item or a jcr node.
     * @param fields a list of comma separated fields tried in order.
     * @param defaultValue if there are no fields matching then this parameter
     *            will be returned. If this field is <code>null</code> and no
     *            property is found an exception will be thrown.
     * @return never <code>null</code>.
     */
    public String html(Object item, String fields, Object defaultValue)
    {
        notNull(item, "item cannot be null");
        notEmpty(fields, "field cannot be empty.");
        try
        {
            Node node = null;
            if (item instanceof Node)
            {
                node = (Node) item;
            }
            else if (item instanceof IPSAssemblyItem)
            {
                node = ((IPSAssemblyItem) item).getNode();
            }
            else if (item instanceof PSRenderAsset)
            {
                node = ((PSRenderAsset) item).getNode();
            }
            else if (item instanceof Map<?, ?>)
            {
                NameAndValue nv = getProperty((Map<?, ?>) item, fields);
                if (nv == null && defaultValue == null)
                    handleNoFieldFound(item, fields);
                return nv == null ? null : StringEscapeUtils.escapeHtml(nv.value);
            }
            else
            {
                throw new IllegalArgumentException("Item must be either a map, assembly item, or node");
            }

            /*
             * Get the fields that the content type has.
             */
            List<String> filteredFields = getFields(node, fields);

            /*
             * If we have no fields that belong to the content type and default
             * value is null throw an exception.
             */
            if (filteredFields.isEmpty() && defaultValue == null)
            {
                handleNoFieldFound(item, fields);
            }

            Property prop = getProperty(node, filteredFields);
            defaultValue = defaultValue == null ? "" : defaultValue;

            /*
             * Use the default value if the propery is null
             */
            if (prop == null)
                return StringEscapeUtils.escapeHtml(defaultValue.toString());

            String field = removeStart(prop.getName(), "rx:");
            isTrue(node != null, "Could not get node from item: ", item);
            String contentType = assemblyItemBridge.getContentType(node);
            boolean isHtml = assemblyItemBridge.isHtmlField(contentType, field);

            if (isHtml)
            {
                return prop.getString();
            }

            return StringEscapeUtils.escapeHtml(prop.getString());
        }
        catch (RepositoryException e)
        {
            log.error(LOG_ERROR_DEFAULT,"html", e.getMessage());
            log.debug(e.getMessage(),e);
            return "";
        }

    }

    /**
     * Gets the path of the specified item. This is the finder path, including
     * the item name.
     *
     * @param id of the item, never <code>null</code> or empty.
     * @return the path, may be empty if the item does not have a path (this
     *         will be true for local content).
     */
    public String getItemPath(String id)
    {
        notEmpty(id);

        IPSGuid guid = idMapper.getGuid(id);
        String[] paths = contentWs.findItemPaths(guid);
        return (paths.length > 0) ? PSPathUtils.getFinderPath(paths[0]) : "";
    }

    /**
     *
     * @param jsonString
     * @return
     */
    @IPSJexlMethod(description = "createJsonObject can be used to convert a JSON string into a JSONObject.", params =
            {@IPSJexlParam(name = "jsonString", description = "A valid JSON string")},
            returns = "A net.sf.json.JSONObject instance ")
    public JSONObject createJsonObject(String jsonString)
    {
        JSONObject jsonObj = null;
        try
        {
            jsonObj = (JSONObject) JSONSerializer.toJSON(jsonString);
        }
        catch (Exception e)
        {
            log.error("Error processing json string: {}" ,jsonString);
            log.debug(e.getMessage(),e);

        }
        return jsonObj;
    }

    /**
     *
     * @param jsonObj - A valid JSON object
     * @param name - The name of the array
     * @return
     */
    @IPSJexlMethod(description = "createJsonArray can be used to convert a JSON Object into a JSONArray.", params =
            {@IPSJexlParam(name = "jsonObj", description = "A valid net.sf.json.JSONObject"),
                    @IPSJexlParam(name = "name", description = "The name of the array")},
            returns = "A valid net.sf.json.JSONArray instance, may be empty.")
    public JSONArray createJsonArray(JSONObject jsonObj, String name)
    {
        if(name==null || name.trim().equals(""))
            throw new IllegalArgumentException("name is required");

        JSONArray ret = new JSONArray();

        if(jsonObj != null) {
            try {
                ret = jsonObj.getJSONArray(name);
            } catch (Exception e) {
                log.error("Error processing json string: {}",e.getMessage());
                log.debug(e);
            }
        }
        return ret;
    }


    public String encryptString(String str){
        try {
            return PSEncryptor.getInstance("AES",
                    PathUtils.getRxDir().getAbsolutePath().concat(PSEncryptor.SECURE_DIR)
            ).encrypt(str);
        } catch (PSEncryptionException e) {
            log.error("Error encrypting string: {}",e.getMessage());
            log.debug(e);
            return "";
        }
    }

    /**
     * Find or edit html source doc as a string
     * and do something with it. Test Method.
     *
     * @param src the source string, may be empty but never <code>null</code>
     * @return the edited or concatenated result string, never <code>null</code>
     */
    public String processForm(IPSAssemblyItem item, Object widget, String src)
    {
        if (src == null)
        {
            log.error(LOG_ERROR_DEFAULT,"processForm","src may not be null");
            return "";
        }

        Source processSRC = new Source(src);
        OutputDocument processOUT = new OutputDocument(processSRC);
        List<net.htmlparser.jericho.Element> hiddenContentType = processSRC.getAllElements(HTMLElementName.INPUT);
        for(net.htmlparser.jericho.Element element : hiddenContentType){
            String typeName = element.getAttributeValue("data-type");
            if(typeName!=null){
                String query=element.getAttributeValue("value"); //build query string with content name
                processOUT.replace(element, createDDDropdown(item, widget, query));
            }
        }

        return processOUT.toString();
    }

    @ToDoVulnerability
    private String createDDDropdown(IPSAssemblyItem item, Object widget, String query)
    {
        String finderName = "Java/global/percussion/widgetcontentfinder/perc_AutoWidgetContentFinder";
        StringBuilder dddString = new StringBuilder("<select name=\"perc_EmailFormTo\" id=\"email-to\" />");
        Map<String, Object> params = new HashMap<>();
        params.put("query", "select rx:personFirstName, rx:personLastName, rx:personEmail, jcr:path from rx:" + query + " where jcr:path like '//Folders/$System$/Assets/%'");
        List<PSRenderAsset> results = widgetContents(item, widget, finderName, params);
        if(results.size() > 0){
            TreeMap<String, PSRenderAsset> resultsTree = new TreeMap<>();
            for(PSRenderAsset element : results){
                try {
                    Node node = element.getNode();
                    String temp = node.getProperty("sys_contentid").getString();
                    resultsTree.put(temp,element);
                }
                catch (ValueFormatException e) {
                    log.error("Error retrieving name property as string {}",e.getMessage());
                }
                catch (PathNotFoundException e) {
                    log.error("Error finding path  to retrieve name property as string {}",e.getMessage());
                }
                catch (RepositoryException e) {
                    log.error("Error querying form repository {}",e.getMessage());
                }
            }
            for(Entry<String, PSRenderAsset> entry : resultsTree.entrySet()){
                try {
                    PSRenderAsset element = entry.getValue();
                    Node node = element.getNode();
                    String first = node.getProperty("personFirstName").getString();
                    String last = node.getProperty("personLastName").getString();
                    String email = node.getProperty("personEmail").getString();

                    String encryptEmail = "";
                    try {
                        encryptEmail = PSEncryptor.getInstance("AES",
                                PathUtils.getRxDir().getAbsolutePath().concat(PSEncryptor.SECURE_DIR)
                        ).encrypt(email);
                    } catch (PSEncryptionException e) {
                       log.error("Error encrypting email address: {}", e.getMessage());
                    }

                    dddString.append("<option data-personName=\"").append(
                            first).append( "-" ).append(last).append("\" value=\"").append( encryptEmail).append(
                                    "\">").append(first).append(" ").append(last).append("</option>");


                }
                catch (ValueFormatException e) {
                    log.error("Error retrieving name property as string",e);
                }
                catch (PathNotFoundException e) {
                    log.error("Error finding path  to retrieve name property as string",e);
                }
                catch (RepositoryException e) {
                    log.error("Error querying form repository",e);
                }
            }
        } else{
            dddString.append("<option value=\"empty\">Select an Option</option>");
        }
        dddString.append("</select>");
        return dddString.toString();
    }

    private void handleNoFieldFound(Object item, String fields) throws RepositoryException {

        throw new RepositoryException(format(
                "For item type: {0} failed to find a property and no default value provided for fields: {1}",
                item.getClass(), fields));
    }

    private NameAndValue getProperty(Map<?, ?> item, String fields)
    {
        notNull(fields);
        String[] fs = fields.split(",");
        for (String name : fs)
        {
            if (item.containsKey(name))
            {
                NameAndValue nv = new NameAndValue();
                nv.name = name;
                nv.value = MapUtils.getString(item, name);
                return nv;
            }
        }
        return null;
    }

    private static class NameAndValue
    {
        @SuppressWarnings("unused")
        private String name;

        private String value;
    }

    /**
     * Determines which fields the node has from a CSV of fields.
     *
     * @param node never <code>null</code>.
     * @param fields csv of fields never <code>null</code>.
     * @return never <code>null</code>, maybe empty.
     * @throws RepositoryException
     */
    private List<String> getFields(Node node, String fields) throws RepositoryException
    {
        notNull(fields);
        notNull(node);
        String[] fs = fields.split(",");
        String contentType = assemblyItemBridge.getContentType(node);
        List<String> rvalue = new ArrayList<>();
        for (String name : fs)
        {
            name = removeStart(name, "rx:");
            /*
             * Has property has does not work correctly so we have to also check
             * the content editor if the node has the field. See CML-2111 and
             * RX-12824
             */
            if (node.hasProperty(name) || assemblyItemBridge.hasField(contentType, name))
            {
                rvalue.add(name);
            }
        }
        return rvalue;
    }

    private Property getProperty(Node node, List<String> fields) throws
            RepositoryException
    {
        notNull(fields);
        for (String name : fields)
        {
            if (node.hasProperty(name))
            {
                return node.getProperty(name);
            }
        }
        return null;
    }

    /**
     * The fully qualified name of the default widget content finder.
     */
    public static final String DEFAULT_WIDGET_CONTENT_FINDER = "Java/global/percussion/widgetcontentfinder/perc_DefaultWidgetContentFinder";


    @Autowired
    private IPSRecycleService recycleService;

    @Autowired
    private IPSRelationshipService relationshipService;

    /**
     * The site manager service. It is assumed to be auto wired by
     * {@link PSSpringWebApplicationContextUtils}
     */
    @Autowired
    private IPSSiteManager siteMgr;

    /**
     * The page service. It is assumed to be auto wired by
     * {@link PSSpringWebApplicationContextUtils}
     */
    @Autowired
    private IPSPageService pageService;

    /**
     * The page dao service. It is assumed to be auto wired by
     * {@link PSSpringWebApplicationContextUtils}
     */
    @Autowired
    private IPSPageDao pageDao;

    /**
     * The template service. It is assumed to be auto wired by
     * {@link PSSpringWebApplicationContextUtils}
     */
    @Autowired
    private IPSTemplateService templateService;

    /**
     * The content service. It is assumed to be auto wired by
     * {@link PSSpringWebApplicationContextUtils}.
     */
    @Autowired
    private IPSContentWs contentWs;

    /**
     * The GUID manager. It is assumed to be auto wired by
     * {@link PSSpringWebApplicationContextUtils}.
     */
    @Autowired
    private IPSGuidManager guidManager;

    /**
     * The widget service, assumed to auto wired by
     * {@link PSSpringWebApplicationContextUtils}
     */
    @Autowired
    private IPSWidgetService widgetService;

    @Autowired
    private IPSIdMapper idMapper;

    @Autowired
    private IPSRenderLinkService renderLinkService;

    @Autowired
    private PSAssemblyItemBridge assemblyItemBridge;

    @Autowired
    private IPSContentItemDao contentItemDao;

    @Autowired
    private IPSManagedLinkService managedLinkService;

    @Autowired
    private IPSManagedLinkDao managedLinkDao;

    @Autowired
    private IPSContentMgr contentMgr;

    private IPSFileSystemService fileSystemService;

    @Autowired
    private IPSPageCategoryService pageCategoryService;

    @Autowired
    private IPSPubServerService pubServerService;

    /**
     * Analytics provider service auto wired by
     * {@link PSSpringWebApplicationContextUtils}
     */
    @Autowired
    private IPSAnalyticsProviderService analyticsProviderService;

    /**
     * The delivery info service to get location of delivery server. auto wired
     * by {@link PSSpringWebApplicationContextUtils}
     */
    @Autowired
    private IPSDeliveryInfoService deliveryInfoService;

    /**
     * The delivery info service to get location of delivery server. auto wired
     * by {@link PSSpringWebApplicationContextUtils}
     */
    @Autowired
    private IPSSiteSectionService siteSectionService;

    @Override
    public void init(IPSExtensionDef def, File codeRoot)
    {

        injectDependencies(this);

        IPSCacheAccess cache = PSCacheAccessLocator.getCacheAccess();
        this.cacheMgr = cache.getManager();

        if(!cacheMgr.cacheExists(LINKCHECK_CACHENAME)){
            cacheMgr.addCache(LINKCHECK_CACHENAME);
        }

        linkCache = cacheMgr.getCache(LINKCHECK_CACHENAME);


    }

    /**
     * Gets the page service, which is auto "wired" by
     * {@link PSSpringWebApplicationContextUtils}
     *
     * @return the page service, never <code>null</code>.
     */
    public IPSPageService getPageService()
    {
        return pageService;
    }

    /**
     * Sets the page service, which is auto "wired" by
     * {@link PSSpringWebApplicationContextUtils}
     *
     * @param srv the page service, never <code>null</code>.
     */
    public void setPageService(IPSPageService srv)
    {
        pageService = srv;
    }

    /**
     * Gets the page dao, which is auto "wired" by
     * {@link PSSpringWebApplicationContextUtils}
     *
     * @return the page service, never <code>null</code>.
     */
    public IPSPageDao getPageDao()
    {
        return pageDao;
    }

    /**
     * Sets the page dao, which is auto "wired" by
     * {@link PSSpringWebApplicationContextUtils}
     *
     * @param pageDao the page dao, never <code>null</code>.
     */
    public void setPageDao(IPSPageDao pageDao)
    {
        this.pageDao = pageDao;
    }

    /**
     * Gets the page service, which is auto "wired" by
     * {@link PSSpringWebApplicationContextUtils}
     *
     * @return the site manager service, never <code>null</code>.
     */
    public IPSSiteManager getSiteMgrService()
    {
        return siteMgr;
    }

    /**
     * Sets the site manager service, which is auto "wired" by
     * {@link PSSpringWebApplicationContextUtils}
     *
     * @param srv the site manager service, never <code>null</code>.
     */
    public void setSiteMgrService(IPSSiteManager srv)
    {
        siteMgr = srv;
    }

    /**
     * Gets the template service, which is auto "wired" by
     * {@link PSSpringWebApplicationContextUtils}
     *
     * @return the template service, never <code>null</code>.
     */
    public IPSTemplateService getTemplateService()
    {
        return templateService;
    }

    /**
     * Sets the template service, which is auto "wired" by
     * {@link PSSpringWebApplicationContextUtils}
     *
     * @param srv the template service, never <code>null</code>.
     */
    public void setTemplateService(IPSTemplateService srv)
    {
        templateService = srv;
    }

    /**
     * Gets the content service, which is auto "wired" by
     * {@link PSSpringWebApplicationContextUtils}.
     *
     * @return content service, never <code>null</code>.
     */
    public IPSContentWs getContentWs()
    {
        return contentWs;
    }

    /**
     * Sets the content service, which is auto "wired" by
     * {@link PSSpringWebApplicationContextUtils}.
     *
     * @param contentWs the content service, never <code>null</code>.
     */
    public void setContentWs(IPSContentWs contentWs)
    {
        this.contentWs = contentWs;
    }

    /**
     * Gets the GUID manager, which is auto "wired" by
     * {@link PSSpringWebApplicationContextUtils}.
     *
     * @return GUID manager, never <code>null</code>.
     */
    public IPSGuidManager getGuidManager()
    {
        return guidManager;
    }

    /**
     * Sets the GUID manager, which is auto "wired" by
     * {@link PSSpringWebApplicationContextUtils}.
     *
     * @param guidManager the GUID manager, never <code>null</code>.
     */
    public void setGuidManager(IPSGuidManager guidManager)
    {
        this.guidManager = guidManager;
    }

    /**
     * Gets the widget service, which is auto "wired" by
     * {@link PSSpringWebApplicationContextUtils}
     *
     * @return the widget service, never <code>null</code>.
     */
    public IPSWidgetService getWidgetService()
    {
        return widgetService;
    }

    /**
     * Sets the widget service, which is auto "wired" by
     * {@link PSSpringWebApplicationContextUtils}
     *
     * @param srv the widget service, never <code>null</code>.
     */
    public void setWidgetService(IPSWidgetService srv)
    {
        widgetService = srv;
    }

    public IPSIdMapper getIdMapper()
    {
        return idMapper;
    }

    public void setIdMapper(IPSIdMapper idMapper)
    {
        this.idMapper = idMapper;
    }

    /*public IPSRecycleService getRecycleService() {
        return recycleService;
    }

    public void setRecycleService(IPSRecycleService recycleService) {
        this.recycleService = recycleService;
    }*/

    public IPSRenderLinkService getRenderLinkService()
    {
        return renderLinkService;
    }

    public void setRenderLinkService(IPSRenderLinkService renderLinkService)
    {
        this.renderLinkService = renderLinkService;
    }

    public PSAssemblyItemBridge getAssemblyItemBridge()
    {
        return assemblyItemBridge;
    }

    public void setAssemblyItemBridge(PSAssemblyItemBridge assemblyItemBridge)
    {
        this.assemblyItemBridge = assemblyItemBridge;
    }

    public IPSContentItemDao getContentItemDao()
    {
        return contentItemDao;
    }

    public void setContentItemDao(PSContentItemDao contentItemDao)
    {
        this.contentItemDao = contentItemDao;
    }

    public IPSManagedLinkService getManagedLinkService()
    {
        return managedLinkService;
    }

    public void setManagedLinkService(IPSManagedLinkService managedLinkService)
    {
        this.managedLinkService = managedLinkService;
    }

    /**
     * Gets the 'web property ID' for the specified site. This will only exist
     * if Google Analytics was set-up and a profile assigned to the site. So the
     * presence of the web property indicates that the site has analytics
     * enabled.
     *
     * @param sitename may be <code>null</code> or empty in which case no web
     *            property will be returned.
     * @return the web property ID. It may be empty if it is not configured for
     *         the site.
     */
    public String getWebPropertyId(String sitename) throws IPSGenericDao.LoadException {
        if (StringUtils.isBlank(sitename))
            return "";
        String webPropertyId = analyticsProviderService.getWebPropertyId(sitename);
        if (webPropertyId != null)
        {
            return webPropertyId;
        }
        return "";
    }

    /**
     * Gets the google API key for the specified site.
     *
     * @param sitename the name of the site.
     * @return the API key. It may be empty if the site name is blank or the
     *         google API key is not configure for the site.
     */
    public String getGoogleApiKey(String sitename) throws IPSGenericDao.LoadException {
        if (StringUtils.isBlank(sitename))
            return "";

        String apiKey = analyticsProviderService.getGoogleApiKey(sitename);
        if (apiKey != null)
        {
            return apiKey;
        }
        return "";
    }

    /**
     * @return the url of the delivery server as defined in
     *         <code>config/delivery-servers.xml</code>. Never <code>null</code>
     *         , may be empty if no entry was found.
     */
    public String getDeliveryServer()
    {
        // TODO: Right now there is only one entry not specific to any site
        // or type of service. If this changes the code will need to be
        // refactored
        // to support it.
        return getDeliveryServer(null);
    }

    public String getDeliveryServer(Long serverId)
    {
        String serverBase = "";
        PSPubServer pubServer = null;
        if (serverId != null) {
            try {
                pubServer = pubServerService.findPubServer(serverId);
            } catch (IPSPubServerService.PSPubServerServiceException e) {
                log.error(e.getMessage());
                log.debug(e.getMessage(),e);
            }
        }
        if (serverId != null && pubServer != null && pubServer.getServerType().equals(PSPubServer.STAGING))
        {
            serverBase = deliveryInfoService.findBaseByServerType(PSPubServer.STAGING);
        }
        else
        {
            serverBase = deliveryInfoService.findBaseByServerType(null);
        }
        return serverBase;
    }

    /**
     * @return the deliveryInfoService
     */
    public IPSDeliveryInfoService getDeliveryInfoService()
    {
        return deliveryInfoService;
    }

    /**
     * @param deliveryInfoService the deliveryInfoService to set
     */
    public void setDeliveryInfoService(IPSDeliveryInfoService deliveryInfoService)
    {
        this.deliveryInfoService = deliveryInfoService;
    }

    /**
     * @return the analyticsProviderService
     */
    public IPSAnalyticsProviderService getAnalyticsProviderService()
    {
        return analyticsProviderService;
    }

    /**
     * @param analyticsProviderService the analyticsProviderService to set
     */
    public void setAnalyticsProviderService(IPSAnalyticsProviderService analyticsProviderService)
    {
        this.analyticsProviderService = analyticsProviderService;
    }

    /**
     * @return the siteSectionService
     */
    public IPSSiteSectionService getSiteSectionService()
    {
        return siteSectionService;
    }

    /**
     * @param siteSectionService the siteSectionService to set
     */
    public void setSiteSectionService(IPSSiteSectionService siteSectionService)
    {
        this.siteSectionService = siteSectionService;
    }

    public IPSManagedLinkDao getManagedLinkDao()
    {
        return managedLinkDao;
    }

    public void setManagedLinkDao(IPSManagedLinkDao managedLinkDao)
    {
        this.managedLinkDao = managedLinkDao;
    }

    public IPSContentMgr getContentMgr()
    {
        return contentMgr;
    }

    public void setContentMgr(IPSContentMgr contentMgr)
    {
        this.contentMgr = contentMgr;
    }

    public IPSFileSystemService getFileSystemService()
    {
        if (fileSystemService == null)
            fileSystemService = (IPSFileSystemService) getWebApplicationContext().getBean("webResourcesService");
        return fileSystemService;
    }

    public IPSPageCategoryService getPageCategoryService()
    {
        return pageCategoryService;
    }

    public void setPageCategoryService(IPSPageCategoryService pageCategoryService)
    {
        this.pageCategoryService = pageCategoryService;
    }

    public IPSPubServerService getPubServerService()
    {
        return pubServerService;
    }

    public void setPubServerService(IPSPubServerService pubServerService)
    {
        this.pubServerService = pubServerService;
    }

    public PSPageUtils(){
        //default ctor
    }

    private static Object metalock = new Object();

}
