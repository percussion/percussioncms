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
package com.percussion.pagemanagement.assembler;

import com.percussion.pagemanagement.assembler.PSAbstractAssemblyContext.EditType;
import com.percussion.pagemanagement.assembler.PSAbstractAssemblyContext.RootRenderType;
import com.percussion.pagemanagement.assembler.PSRegionResult.PSRegionResultType;
import com.percussion.pagemanagement.assembler.impl.PSAssemblyItemBridge.TemplateAndPage;
import com.percussion.pagemanagement.assembler.impl.PSProxyAssemblyTemplate;
import com.percussion.pagemanagement.assembler.impl.PSSerialRegionsAssembler;
import com.percussion.pagemanagement.dao.IPSTemplateDao;
import com.percussion.pagemanagement.data.PSPage;
import com.percussion.pagemanagement.data.PSRegionBranches;
import com.percussion.pagemanagement.data.PSRegionTree;
import com.percussion.pagemanagement.data.PSRegionTreeUtils;
import com.percussion.pagemanagement.data.PSRenderLinkContext;
import com.percussion.pagemanagement.data.PSTemplate;
import com.percussion.pagemanagement.data.PSWidgetDefinition;
import com.percussion.pagemanagement.data.PSWidgetItem;
import com.percussion.pagemanagement.service.IPSTemplateService;
import com.percussion.pagemanagement.service.IPSWidgetService;
import com.percussion.services.assembly.IPSAssemblyItem;
import com.percussion.services.assembly.IPSAssemblyResult;
import com.percussion.services.assembly.IPSAssemblyService;
import com.percussion.services.assembly.IPSAssemblyTemplate;
import com.percussion.services.assembly.PSAssemblyException;
import com.percussion.services.assembly.PSTemplateNotImplementedException;
import com.percussion.services.assembly.data.PSAssemblyTemplate;
import com.percussion.services.assembly.data.PSTemplateBinding;
import com.percussion.services.filter.PSFilterException;
import com.percussion.share.service.exception.PSDataServiceException;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.util.PSSiteManageBean;
import com.percussion.webservices.PSWebserviceUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StopWatch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.jcr.ItemNotFoundException;
import javax.jcr.RepositoryException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.text.MessageFormat.format;
import static java.util.Arrays.asList;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.Validate.notEmpty;
import static org.apache.commons.lang.Validate.notNull;

/**
 * 
 * Creates the context to be bound to velocity for page or template assembly.
 * 
 * @author adamgent
 * 
 */
@PSSiteManageBean
public class PSPageAssemblyContextFactory
{
    /**
     * The value of this assembly item parameter controls the
     * {@link PSAbstractAssemblyContext#isEditMode()} property. If the assembly
     * item has this property (accessed via
     * {@link IPSAssemblyItem#getParameterValue(String, String)} with a value of
     * "true", then the flag is set on the widget context, otherwise it is not.
     */
    public static String ASSEMBLY_PARAM_EDITMODE = "EditMode";

    /**
     * The value of this assembly item parameter controls the
     * {@link PSAbstractAssemblyContext#getEditType()} property. If the assembly
     * item has this property (accessed via
     * {@link IPSAssemblyItem#getParameterValue(String, String)} with a value of
     * "true", then the flag is set on the widget context, otherwise it is not.
     */
    public static String ASSEMBLY_PARAM_EDITTYPE = "EditType";
    
    public static String ASSEMBLY_PARAM_SCRIPTSOFF = "ScriptsOff";

    private IPSTemplateDao templateDao;
    
    private IPSWidgetService widgetService;

    private IPSAssemblyService assemblyService;
    
    private IPSRenderLinkContextFactory renderLinkContextFactory;
    
    private IPSRegionsAssembler widgetRegionsAssembler = new PSSerialRegionsAssembler();
    private IPSRegionsAssembler overridedRegionsAssembler = new PSSerialRegionsAssembler();
    
    private IPSRegionAssembler widgetRegionAssembler = new PSWidgetRegionAssembler();
    
    private IPSRegionAssembler overridedRegionAssembler = new PSOverridedRegionAssembler();
    
    /**
     * Logger
     */

    private static final Logger log = LogManager.getLogger(PSPageAssemblyContextFactory.class);

    @Autowired
    public PSPageAssemblyContextFactory(IPSAssemblyService assemblyService,
            IPSTemplateDao templateDao,
            IPSWidgetService widgetService,
            IPSRenderLinkContextFactory renderLinkContextFactory)
    {
        super();
        this.assemblyService = assemblyService;
        this.templateDao = templateDao;
        this.widgetService = widgetService;
        this.renderLinkContextFactory = renderLinkContextFactory;
    }


    /**
     * The velocity context has the page context bound to it:
     * <p>
     * <code>$perc</code>
     * <p>
     * The Region results are bound:
     * <p>
     * <code>$perc.regions['region_name']</code> = list of strings : rendering of sub regions
     * or widgets.
     * <p>
     * The list of strings allows templates to loop through regions or widgets
     * with out knowing if they are a sub region or a widget
     * <pre>
     * &lt;div id="MyRegion" class="perc_region"&gt;
     * #foreach( $sub in $perc.regions.get('MyRegion')
     * &lt;span&gt;
     * $sub
     * &lt;/span&gt;
     * #end
     * &lt;/div&gt;
     * </pre>
     * <p>
     * Its up to the template designer to put the correct html for regions. It
     * should be noted that a template designer can only control the immediate
     * looping of template region contents and not user defined page regions.
     *
     * @param assemblyItem    never <code>null</code>.
     * @param templateAndPage never <code>null</code>.
     * @param isHtml          <code>true</code> if the rendered result is in HTML; otherwise the rendered result is in XML.
     * @return the context to be bound for assembling the page.
     * @throws ItemNotFoundException
     * @throws PSAssemblyException
     * @throws PSFilterException
     * @throws RepositoryException
     * @throws PSTemplateNotImplementedException
     */
    public PSPageAssemblyContext createContext(IPSAssemblyItem assemblyItem, TemplateAndPage templateAndPage, boolean isHtml)
            throws ItemNotFoundException, PSAssemblyException, PSFilterException, RepositoryException,
            PSTemplateNotImplementedException, PSDataServiceException {

        StopWatch sw = new StopWatch("#createContext");
        /*
         * Immediately clone the assembly item for thread safety while
         * we are still single threaded.
         */
        IPSAssemblyItem clonedAssemblyItem = (IPSAssemblyItem) assemblyItem.clone();

        PSPage page = templateAndPage.getPage();
        PSTemplate template = templateAndPage.getTemplate();

        notNull(clonedAssemblyItem, "assemblyItem");
        notNull(page, "page");
        notNull(template, "template");

        /*
         * TODO: This should go in the assembly item bridge.
         */
        if (clonedAssemblyItem.getUserName() == null && PSWebserviceUtils.getUserName() != null) {
            clonedAssemblyItem.setUserName(PSWebserviceUtils.getUserName());
        }

        sw.start("mergeTree");
        /*
         * First we create our top level assembly context.
         */
        PSPageAssemblyContext context = new PSPageAssemblyContext();

        context.setPage(page);
        context.setTemplate(template);
        context.setEditMode(assemblyItem.getParameterValue(ASSEMBLY_PARAM_EDITMODE, "false").equals("true"));
        context.setScriptsOff(assemblyItem.getParameterValue(ASSEMBLY_PARAM_SCRIPTSOFF, "false").equals("true"));
        PSRenderLinkContext linkContext = renderLinkContextFactory.create(clonedAssemblyItem, page);
        context.setLinkContext(linkContext);
        context.setPreviewMode(assemblyItem.getDeliveryContext() == 0);
        switch (templateAndPage.getItemType()) {
            case PAGE:
                context.setRootRenderType(RootRenderType.PAGE);
                break;
            case TEMPLATE:
                context.setRootRenderType(RootRenderType.TEMPLATE);
                break;
        }
        String editType = assemblyItem.getParameterValue(ASSEMBLY_PARAM_EDITTYPE, EditType.PAGE.name());
        if (editType.equals(EditType.TEMPLATE.name())) {
            context.setEditType(EditType.TEMPLATE);
        }
        else {
            context.setEditType(EditType.PAGE);
        }

        /*
         * Should we be doing the template parsing in the template service
         * to validate correct templates on save?
         *
         * For now we parse here!
         */

        //PSTemplateRegionParser.parseTemplate(template);

        /*
         * Now we merge the page region branches into the templates regions
         * tree.
         */

        PSRegionTree templateRegionTree = template.getRegionTree();

        PSRegionBranches pageRegionBranches = page.getRegionBranches();
        PSAbstractMergedRegionTree tree = new PSMergedRegionTree(widgetService, templateRegionTree, pageRegionBranches);

        Map<String, List<PSRegionResult>> regions = new ConcurrentHashMap<>();
        context.setRegions(regions);

        /*
         * We now load all the widget instances for the Merged Regions.
         */
        Collection<PSMergedRegion> widgetRegions = tree.getWidgetRegions();

        sw.stop();
        sw.start("assembleWidgetRegions");

        /*
         * We save the results of those regions to be bound to the pages
         * velocity context.
         */

        getWidgetRegionsAssembler().assembleRegions(widgetRegionAssembler, clonedAssemblyItem, context, widgetRegions);
        sw.stop();

        /*
         * Assemble page region overrides that contain regions.
         * We do it after we assemble widgets because the page
         * regions might contain widgets.
         */
        sw.start("assembleRegionOverrides");
        Collection<PSMergedRegion> pageRegions = tree.getOverriddenRegions();

        /*
         * TODO (ychen) Need to verify the following when we get a chance later:
         * We should be able to use "widgetRegionAssembler" to render the page regions as well as template regions
         */
        if (isHtml) {
            getOverridedRegionsAssembler().assembleRegions(overridedRegionAssembler, clonedAssemblyItem, context, pageRegions);
        }
        else {
            getOverridedRegionsAssembler().assembleRegions(widgetRegionAssembler, clonedAssemblyItem, context, pageRegions);
        }

        sw.stop();

        /*
         * Set our custom template to the assembly item.
         */
        sw.start("regionToTree");
        String html = PSRegionTreeUtils.treeToString(templateRegionTree.getRootRegion());

        IPSAssemblyTemplate at = getAssemblyTemplate(template.getSourceTemplateName(), html);
        /*
         * Set our template back to the original assembly item.
         * TODO should we be doing this here?
         */
        assemblyItem.setTemplate(at);

        /*
         * Run velocity assembler with our bindings (super.assembleSingle).
         */
        sw.stop();

        log.debug(sw.prettyPrint());
        return context;
    }
    
    protected class PSOverridedRegionAssembler implements IPSRegionAssembler {

        @Override
        public List<PSRegionResult> assembleRegion(IPSAssemblyItem assemblyItem, PSPageAssemblyContext context,
                PSMergedRegion mr) throws IPSTemplateService.PSTemplateException {
            return assembleRegionOverride(assemblyItem, context, mr);
        }
    
    }
    
    protected class PSWidgetRegionAssembler implements IPSRegionAssembler {

        @Override
        public List<PSRegionResult> assembleRegion(IPSAssemblyItem assemblyItem, PSPageAssemblyContext context,
                PSMergedRegion mr) throws IPSTemplateService.PSTemplateException {
            return assembleWidgetRegion(assemblyItem, context, mr);
        }
    }
    
    /**
     * Assemblys a page region override.
     * 
     * @param assemblyItem never <code>null</code>.
     * @param context context must already be setup, never <code>null</code>.
     * @param mr never <code>null</code>.
     * @return never <code>null</code>.
     */
    protected List<PSRegionResult> assembleRegionOverride(
            IPSAssemblyItem assemblyItem, 
            PSPageAssemblyContext context, 
            PSMergedRegion mr) throws IPSTemplateService.PSTemplateException {
        notNull(assemblyItem, "assemblyItem");
        notNull(mr, "mr");
        if (log.isDebugEnabled()) {
            log.debug("Assembling Region: " + mr.getRegionId());
        }
        
        IPSAssemblyItem clonedItem = (IPSAssemblyItem) assemblyItem.clone();
        String templateCode = PSRegionTreeUtils.treeToString(mr.getOriginalRegion());
        IPSAssemblyTemplate at =  getAssemblyTemplate(context.getTemplate().getSourceTemplateName(), templateCode);
        at.setAssembler("Java/global/percussion/assembly/velocityAssembler");
        clonedItem.setTemplate(at);
        clonedItem.getBindings().put("$perc", context);
        

        try
        {
            List<IPSAssemblyResult> aItems = assemblyService.assemble(asList(clonedItem));
            List<String> rs = assembleResultsToString(aItems);
            notEmpty(rs);
            PSRegionResult result = new PSRegionResult();
            result.setResult(rs.get(0));
            result.setType(PSRegionResultType.SUBREGION);
            return asList(result);
        }
        catch (Exception e)
        {
            log.error("Failed to assemble regionId: {} " , mr.getRegionId());
            throw new RuntimeException(e);
        }
        finally {
            log.debug("Finished assembling region");
        }
    }

    

    /**
     * 
     * @param assemblyItem never <code>null</code>.
     * @param context never <code>null</code>.
     * @param mr never <code>null</code>.
     * @return never <code>null</code>.
     */
    protected List<PSRegionResult> assembleWidgetRegion(
            IPSAssemblyItem assemblyItem, 
            PSPageAssemblyContext context,
            PSMergedRegion mr) throws IPSTemplateService.PSTemplateException {

        /*
         * Using the merged region tree we then assemble each of the regions.
         */
        /*
         * Right now we only assembly widget regions. More on that later.
         */
        PSWidgetAssemblyContext wac = new PSWidgetAssemblyContext();
        wac.setPage(context.getPage());
        wac.setRegion(mr.getOriginalRegion());
        wac.setTemplate(context.getTemplate());
        wac.setLinkContext(context.getLinkContext());
        wac.setEditMode(context.isEditMode());
        wac.setPreviewMode(context.isPreviewMode());
        wac.setRootRenderType(context.getRootRenderType());
        
        List<PSRegionResult> regionResults = assembleWidgets(assemblyItem, wac, mr.getWidgetInstances());
        mr.setResults(regionResults);
        // context.getRegions().put(mr.getRegionId(), mr.getResults());
        return regionResults;
    }

    protected List<String> assembleResultsToString(List<IPSAssemblyResult> assemblyResults)
    {
        notNull(assemblyResults, "assemblyResults");
        List<String> results = new ArrayList<>();
        for (IPSAssemblyResult ar : assemblyResults)
        {
            try
            {
                String result = IOUtils.toString(ar.getResultStream(), StandardCharsets.UTF_8);
                results.add(result);
            }
            catch (IOException e)
            {
                throw new RuntimeException("Failed to convert assembly result to string.",e);
            }

        }
        return results;
    }

    protected List<PSRegionResult> assembleWidgets(
            IPSAssemblyItem assemblyItem, 
            PSWidgetAssemblyContext widgetContext,
            List<PSWidgetInstance> wis) throws IPSTemplateService.PSTemplateException {
        /*
         * The leaf regions are assembled by calling the assembly service
         * #assemble on each of the widgets in the region with their respective
         * template code which comes from the widget definition. This is done
         * by:
         * 
         * The widget template code is turned into IPSAssemblyTemplate. We add
         * new bindings to the template from the widget item and we add the
         * widget instance to the bindings: $widget.item $widget.definition
         * $widget.assets
         * 
         * The pages IPSAssemblyItem is cloned for the widget to use. We set the
         * template on the assembly item to our new template.
         * 
         * We then call assemble(clonedAssemblyItem).
         * 
         * (TODO) The widgets asset finder is called to $widget.assets
         */
        List<PSRegionResult> regionResults = new ArrayList<>();
        for (PSWidgetInstance wi : wis)
        {
            PSWidgetAssemblyContext clonedContext = copyWidgetAssemblyContext(widgetContext);
            clonedContext.setWidget(wi);
            IPSAssemblyItem clonedItem = (IPSAssemblyItem) assemblyItem.clone();

            IPSAssemblyTemplate at =  getWidgetTemplate(wi);
            clonedItem.setTemplate(at);
            
            clonedItem.getBindings().put("$perc", clonedContext);
            PSRegionResult regionResult = assemblyWidget(clonedItem, wi);
            regionResults.add(regionResult);
        }

        return regionResults;
    }
    
    private PSRegionResult assemblyWidget(IPSAssemblyItem assemblyItem, PSWidgetInstance wis) {
        List<IPSAssemblyItem> widgetAssemblyItems = new ArrayList<>();
        widgetAssemblyItems.add(assemblyItem);
        PSRegionResult regionResult = new PSRegionResult();
        regionResult.setType(PSRegionResultType.WIDGET);
        regionResult.setWidget(wis);
        try
        {
            List<IPSAssemblyResult> results =  assemblyService.assemble(widgetAssemblyItems);
            List<String> renderResults = assembleResultsToString(results);
            regionResult.setResult(renderResults.get(0));
        }
        catch (Throwable e)
        {
            log.error("While running the widget there was a major error: {} " , e.getMessage());
            log.debug("While running the widget there was a major error: ", e);
            String publish = assemblyItem.getParameterValue(IPSHtmlParameters.SYS_PUBLISH, null);
            boolean publishFlag = "publish".equals(publish);
            regionResult.setErrorCause(e,publishFlag);
        }
        return regionResult;
    }

    protected PSWidgetAssemblyContext copyWidgetAssemblyContext(PSWidgetAssemblyContext context)
    {
        return context.clone();
    }

    private IPSAssemblyTemplate getAssemblyTemplate(String templateName, String templateCode) throws IPSTemplateService.PSTemplateException {
        notEmpty(templateName, "templateName");
        notEmpty(templateCode, "templateCode");        
        if (log.isDebugEnabled()) {
            log.debug("Creating assembly template with templateName:{} and templateCode:{}",
                    templateName, templateCode);
        }
        IPSAssemblyTemplate baseTpl = getProxyAssemblyTemplate(templateName);
        baseTpl.setTemplate(templateCode);
        return baseTpl;
    }

    /**
     * Gets a widget template for a given widget ID. The returned widget template
     * is an in memory template object, which is based on the "master" template
     * of the widget.
     * 
     * @param widgetInstance never <code>null</code>. 
     * 
     * 
     * @return the widget template, never <code>null</code>.
     */
    public IPSAssemblyTemplate getWidgetTemplate(PSWidgetInstance widgetInstance) throws IPSTemplateService.PSTemplateException {

       IPSAssemblyTemplate widgetBaseTmpl = 
           getProxyAssemblyTemplate(widgetService.getBaseTemplate());
       
       IPSAssemblyTemplate template = createTemplate(
               widgetInstance.getItem(), 
               widgetInstance.getDefinition(),
               widgetBaseTmpl);
       
       return template;
    }
    
    protected IPSAssemblyTemplate getProxyAssemblyTemplate(String name) throws IPSTemplateService.PSTemplateException {
        PSAssemblyTemplate template = templateDao.loadBaseTemplateByName(name);
        return new PSProxyAssemblyTemplate(template);
    }

    /**
     * Creates the assembly template by cloning the widgets master template.
     * Sets the bindings on the template.
     * 
     * @param widgetItem The instance item of the widget assumed not
     * <code>null</code>.
     * @param widget The instance of the widget definition assumed not
     * <code>null</code>.
     * @param widgetBaseTmpl The widgets base template assumed not
     * <code>null</code>.
     * @return The assembly template, never <code>null</code>.
     */
    private IPSAssemblyTemplate createTemplate(PSWidgetItem widgetItem, PSWidgetDefinition widget,
            IPSAssemblyTemplate widgetBaseTmpl)
    {
        notNull(widgetItem, "widgetItem");
        notNull(widget, "widget");
        notNull(widgetBaseTmpl, "widgetBaseTmpl");
        
        if (StringUtils.isBlank(widgetBaseTmpl.getLabel())) {
            widgetBaseTmpl.setLabel(widgetBaseTmpl.getName());
        }
        IPSAssemblyTemplate tmpl = (IPSAssemblyTemplate) widgetBaseTmpl.clone();

        String templateContent = widget.getContent()  == null ? "" :  widget.getContent().getValue();
        
        if (isBlank(templateContent)) {
            log.error("Widget definition does not have template code. WidgetId: {} " , widget.getId());
        }
        
        String templateCode = widget.getCode() == null ? ";" : widget.getCode().getValue();
        
        if(log.isTraceEnabled()) {
            log.trace("Template Code: {} " , templateCode);
        }
       
        tmpl.setName(widget.getId());
        tmpl.setBindings(createJexlScriptBindings(templateCode));
        tmpl.setTemplate(templateContent);
        return tmpl;
    }
    
    /*
     * Total hack
     */
    private List<PSTemplateBinding> createJexlScriptBindings(String script)
    {
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        map.put("$_script", createJexlScript(script));
        return createBindings(map, 0);
    }
    
    /*
     * Total hack
     */
    private String createJexlScript(String script) {
        return  "if ( 1 == 1 ) {" + script + "} else { ; }";
    }

    /**
     * Creates template bindings for the specified variable-expression pairs.
     * 
     * @param bindings map of binding variables to expressions.
     * @param startingOrder for the bindings.  A starting order < 1 will be adjusted to 1.
     * @return list of template binding objects, never <code>null</code>, may be empty.  These objects will not have
     * valid id values and therefore should not be persisted to the database in their current state.
     */
    private List<PSTemplateBinding> createBindings(LinkedHashMap<String, String> bindings, int startingOrder)
    {
        if (startingOrder < 1) {
            startingOrder = 1;
        }

        List<PSTemplateBinding> tempBindings = new ArrayList<>();

        for (Map.Entry<String, String> entry : bindings.entrySet())
        {
            if (StringUtils.isBlank(entry.getKey())) {
                throw new IllegalArgumentException(
                        "the key of the bindings map must not be blank.");
            }
            PSTemplateBinding tempBinding = new PSTemplateBinding();
            tempBinding.setExecutionOrder(startingOrder++);
            tempBinding.setExpression(entry.getValue());
            tempBinding.setVariable(entry.getKey());
            tempBindings.add(tempBinding);
        }

        return tempBindings;
    }

    public IPSRegionsAssembler getWidgetRegionsAssembler()
    {
        return widgetRegionsAssembler;
    }

    @Autowired
    public void setWidgetRegionsAssembler(IPSRegionsAssembler widgetRegionsAssembler)
    {
        this.widgetRegionsAssembler = widgetRegionsAssembler;
    }

    public IPSRegionsAssembler getOverridedRegionsAssembler()
    {
        return overridedRegionsAssembler;
    }

    public void setOverridedRegionsAssembler(IPSRegionsAssembler overridedRegionsAssembler)
    {
        this.overridedRegionsAssembler = overridedRegionsAssembler;
    }

}
