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

/**
 * 
 */
package com.percussion.widgetbuilder.service;

import com.percussion.cms.IPSConstants;
import com.percussion.deployer.server.PSLocalDeployerClient;
import com.percussion.error.PSExceptionUtils;
import com.percussion.server.PSServer;
import com.percussion.services.notification.IPSNotificationService;
import com.percussion.services.widgetbuilder.IPSWidgetBuilderDefinitionDao;
import com.percussion.services.widgetbuilder.PSWidgetBuilderDefinition;
import com.percussion.share.service.IPSSystemProperties;
import com.percussion.widgetbuilder.data.PSWidgetBuilderDefinitionData;
import com.percussion.widgetbuilder.data.PSWidgetBuilderDefinitionDataList;
import com.percussion.widgetbuilder.data.PSWidgetBuilderFieldsListData;
import com.percussion.widgetbuilder.data.PSWidgetBuilderResourceListData;
import com.percussion.widgetbuilder.data.PSWidgetBuilderSummaryData;
import com.percussion.widgetbuilder.data.PSWidgetBuilderSummaryDataList;
import com.percussion.widgetbuilder.data.PSWidgetBuilderValidationResults;
import com.percussion.widgetbuilder.utils.PSWidgetPackageBuilder;
import com.percussion.widgetbuilder.utils.PSWidgetPackageSpec;
import com.percussion.widgetbuilder.utils.validate.PSWidgetBuilderDefinitionValidator;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.helper.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.percussion.cms.IPSConstants.SAAS_FLAG;

/**
 * @author matthewernewein
 * 
 */
@Path("/widgetbuilder")
@Component("widgetBuilderService")
@Lazy
public class PSWidgetBuilderService implements IPSWidgetBuilderService
{
    private IPSWidgetBuilderDefinitionDao dao = null;

    private IPSSystemProperties systemProps;

    /**
     * The log instance to use for this class, never <code>null</code>.
     */

    private static final Logger log = LogManager.getLogger(IPSConstants.WIDGET_BUILDER_LOG);

    @Autowired
    public PSWidgetBuilderService(final IPSWidgetBuilderDefinitionDao dao, IPSNotificationService notificationService)
    {
        Validate.notNull(dao);
        this.dao = dao;
    }

    /**
     * Set the system properties on this service. This service will always use
     * the the values provided by the most recently set instance of the
     * properties.
     * 
     * @param systemProps the system properties
     */
    @Autowired
    public void setSystemProps(IPSSystemProperties systemProps)
    {
        this.systemProps = systemProps;
    }

    /**
     * Gets the system properties used by this service.
     * 
     * @return The properties
     */
    public IPSSystemProperties getSystemProps()
    {
        return systemProps;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.percussion.widgetbuilder.service.IPSWidgetBuilderService#
     * isWidgetBuilderEnabled()
     */
    @Override
    @GET
    @Path("/active")
    @Produces(MediaType.TEXT_PLAIN)
    public boolean isWidgetBuilderEnabled()
    {
        return Boolean.parseBoolean(getSystemProps().getProperty("isWidgetBuilderActive"));
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.percussion.widgetbuilder.service.IPSWidgetBuilderService#
     * isWidgetDefinitionDeployed(java.lang.String)
     */
    @Override
    @GET
    @Path("/deployed/{definitionId}")
    @Produces(MediaType.TEXT_PLAIN)
    public boolean isWidgetDefinitionDeployed(@PathParam("definitionId")
    final long definitionId)
    {
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.percussion.widgetbuilder.service.IPSWidgetBuilderService#
     * deleteWidgetBuilderDefinition(java.lang.String)
     */
    @Override
    @DELETE
    @Path("/definition/{definitionId}")
    public void deleteWidgetBuilderDefinition(@PathParam("definitionId")
    final long definitionId)
    {
        try
        {
            dao.delete(definitionId);
        }
        catch (Exception e)
        {
            throw new RuntimeException("Failed to delete widget definition: " + definitionId);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.percussion.widgetbuilder.service.IPSWidgetBuilderService#getBuiltWidgets
     * ()
     */
    @Override
    @GET
    @Path("/definitions")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public List<PSWidgetBuilderDefinitionData> loadAll()
    {
        try
        {
            List<PSWidgetBuilderDefinition> definitions = dao.getAll();
            List<PSWidgetBuilderDefinitionData> returnResults = new ArrayList<>();
            if (definitions != null)
            {
                for (PSWidgetBuilderDefinition definition : definitions)
                {
                    returnResults.add(new PSWidgetBuilderDefinitionData(definition));
                }
            }
            return new PSWidgetBuilderDefinitionDataList(returnResults);
        }
        catch (Exception e)
        {
            log.error("Failed to load widget definitions. Error: {}",PSExceptionUtils.getMessageForLog( e));
            log.debug(PSExceptionUtils.getDebugMessageForLog(e));
            throw new RuntimeException("Failed to load widget definitions",e);
        }
    }

    @Override
    @GET
    @Path("/summaries")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public List<PSWidgetBuilderSummaryData> loadAllSummaries()
    {
        try
        {
            List<PSWidgetBuilderDefinition> definitions = dao.getAll();
            List<PSWidgetBuilderSummaryData> returnResults = new ArrayList<>();
            if (definitions != null)
            {
                for (PSWidgetBuilderDefinition definition : definitions)
                {
                    returnResults.add(new PSWidgetBuilderSummaryData(definition));
                }
            }
            return new PSWidgetBuilderSummaryDataList(returnResults);
        }
        catch (Exception e)
        {
            log.error("Failed to load widget definitions. Error: {}", PSExceptionUtils.getMessageForLog(e));
            log.debug(PSExceptionUtils.getDebugMessageForLog(e));
            throw new RuntimeException("Failed to load widget definitions",e);
        }
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * com.percussion.widgetbuilder.service.IPSWidgetBuilderService#loadBuiltWidget
     * (java.lang.String)
     */
    @Override
    @GET
    @Path("/definition/{definitionId}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public PSWidgetBuilderDefinitionData loadWidgetDefinition(@PathParam("definitionId")
    final long definitionId)
    {
        PSWidgetBuilderDefinitionData returnData = null;
        PSWidgetBuilderDefinition daoObject = null;
        daoObject = dao.find(definitionId);
        if (daoObject != null)
        {
            returnData = new PSWidgetBuilderDefinitionData(daoObject);
        }
        return returnData;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.percussion.widgetbuilder.service.IPSWidgetBuilderService#
     * createWidgetBuilderDefinition
     * (com.percussion.services.widgetbuilder.PSWidgetBuilderDefinition)
     */
    @Override
    @POST
    @Path("/definition/")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public PSWidgetBuilderValidationResults saveWidgetBuilderDefinition(PSWidgetBuilderDefinitionData definition)
    {
        try
        {
            // first validate
            PSWidgetBuilderValidationResults results = validate(definition);
            if (!results.getResults().isEmpty()) {
                return results;
            }
            // modify the path for image icon if present for make it OS compatible
            if(StringUtils.isNotBlank(definition.getWidgetTrayCustomizedIconPath())){
                definition.setWidgetTrayCustomizedIconPath(definition.getWidgetTrayCustomizedIconPath().replaceAll("\\\\","/"));
                //validate the path
                File imagePath = new File(PSServer.getRxDir(), definition.getWidgetTrayCustomizedIconPath());
                if (!imagePath.exists())
                {
                    log.warn("No valid path found for widget tray icon: {}" , imagePath);
                    definition.setWidgetTrayCustomizedIconPath("");//reset it
                }
            }
            if(StringUtils.isNotBlank(definition.getDescription())){
                definition.setDescription(definition.getDescription().replace("\"","'"));
            }

            PSWidgetBuilderDefinitionData returnData = null;
            PSWidgetBuilderDefinition daoObject = null;
            daoObject = dao.save(PSWidgetBuilderDefinitionData.createDaoObject(definition));
            if (daoObject != null)
            {
                returnData = new PSWidgetBuilderDefinitionData(daoObject);
            }

            if(returnData != null){
            	results.setDefinitionId(Long.parseLong(returnData.getId()));
            }
            
            return results;
        }
        catch (Exception e)
        {
            log.error("Error saving Widget Builder Widget Definition. Error: {}", PSExceptionUtils.getMessageForLog(e));
            log.debug(PSExceptionUtils.getDebugMessageForLog(e));
            throw new RuntimeException("Failed to save widget definition");
        }
    }
    
    
    @Override
    @POST
    @Path("/validate/")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @SuppressFBWarnings("DLS_DEAD_LOCAL_STORE")
    public PSWidgetBuilderValidationResults validate(PSWidgetBuilderDefinitionData definition)
    {
        Validate.notNull(definition);
        
        PSWidgetBuilderValidationResults results = new PSWidgetBuilderValidationResults();
        
        PSWidgetBuilderDefinitionValidator validator = new PSWidgetBuilderDefinitionValidator();
        results.setResults(validator.validate(definition, loadAll()));
        
        return results;
    }
    

    @Override
    @POST
    @Path("/deploy/{definitionId}")
    public void deployWidget(@PathParam("definitionId") final long definitionId)
    {
        File srcFile = new File(PSServer.getRxDir(), "sys_resources/widgetbuilder/percWidgetTemplate.zip");
        File tmpDir = new File(PSServer.getRxDir(), "rx_resources/widgets_generated/temp");
        File tgtDir = new File(PSServer.getRxDir(), "rx_resources/widgets_generated");
        if (!tgtDir.exists())
        {
        	try {
				tgtDir.mkdirs();
			} catch (Exception e) {
				log.error("Unable to create target widget directory: {}" , tgtDir.getAbsolutePath());
				throw new RuntimeException("Unable to create target widget directory: " + tgtDir.getAbsoluteFile(),e);
			}
        }
        
        if (!tmpDir.exists())
        {
        	try {
        		tmpDir.mkdirs();
			} catch (Exception e) {
				log.error("Unable to create temp widget directory: {}" , tmpDir.getAbsolutePath());
				throw new RuntimeException("Unable to create temp widget directory: " + tmpDir.getAbsoluteFile(), e);
			}
        }
        
        try
        {
            PSWidgetBuilderDefinition definition = this.dao.find(definitionId);
            Validate.notNull(definition);
            
            PSWidgetPackageBuilder builder = new PSWidgetPackageBuilder(srcFile, tmpDir);

            PSWidgetPackageSpec spec = new PSWidgetPackageSpec(definition.getPrefix(), definition.getPublisherUrl(),
                    definition.getLabel(), definition.getDescription(), definition.getVersion(), PSServer.getVersion());
            spec.setResponsive(definition.isResponsive());
            if (!StringUtils.isBlank(definition.getWidgetTrayCustomizedIconPath())) {
                spec.setWidgetTrayCustomizedIconPath(definition.getWidgetTrayCustomizedIconPath());
            }
            if (!StringUtils.isBlank(definition.getToolTipMessage())) {
                spec.setTooTipMessage(definition.getToolTipMessage());
            }
            if (!StringUtils.isBlank(definition.getFields())) {
                spec.setFields(PSWidgetBuilderFieldsListData.fromXml(definition.getFields()).getFields());
            }
            
            if (!StringUtils.isBlank(definition.getCssFiles())) {
                spec.setCssFiles(PSWidgetBuilderResourceListData.fromXml(definition.getCssFiles()).getResourceList());
            }
            
            if (!StringUtils.isBlank(definition.getJsFiles())) {
                spec.setJsFiles(PSWidgetBuilderResourceListData.fromXml(definition.getJsFiles()).getResourceList());
            }
            
            spec.setWidgetHtml(definition.getWidgetHtml());
            
            File result = builder.generatePackage(tgtDir, spec);
            if (result.exists())
            {
                PSLocalDeployerClient client = new PSLocalDeployerClient();
                Date started = new Date();
                try
                {
	                client.installPackage(result);
	                copyWidgetMutables(result);
                }
                catch (Exception e)
                {
                	log.error(PSExceptionUtils.getMessageForLog(e));
                    log.debug(PSExceptionUtils.getDebugMessageForLog(e));
                	throw new RuntimeException("Failed to install package.", e);
                }
            }
            else
            {
                throw new RuntimeException("Failed to generate package for widget definition: " + Long.toString(definitionId));
            }
        }
        catch (Exception e)
        {
            log.error("WidgetBuilder Error: {}",PSExceptionUtils.getMessageForLog(e));
            log.debug(PSExceptionUtils.getDebugMessageForLog(e));

            if (e instanceof RuntimeException)
            {
            	throw (RuntimeException) e;
            }
            throw new RuntimeException("Failed to build package",e);
        }
    }
    
    /**
     * Copy new ppkg file and all object store psx_ce files to mutable directory for persistence 
     * in docker SAAS installation.
     * 
     * @param ppkg Package file to be copied. Null value will result in a <code>RuntimeException</code> thrown
     */
    private void copyWidgetMutables(File ppkg) {
        //Only copy the mutables if this is a SAAS install 
        if(PSServer.getServerProps() != null 
        		&& StringUtils.equalsIgnoreCase(PSServer.getServerProps().getProperty(SAAS_FLAG), "true")) {
	        File mutableDir = new File(PSServer.getRxDir(), "var");
	        File mutableWidgetDir = new File(mutableDir, "widgets_generated");
	        File objectStoreDir = new File(PSServer.getRxDir(), "ObjectStore");
	        File mutableObjectStoreDir = new File(mutableDir, "ObjectStore");
	    	
	        if (!mutableWidgetDir.exists()) {
	        	try {
	        		mutableWidgetDir.mkdirs();
				} catch (Exception e) {
					log.error("Unable to create mutable widget directory: {}, Error: {} ", mutableWidgetDir.getAbsolutePath(),
                            PSExceptionUtils.getMessageForLog(e));
                    log.debug(e.getMessage(),e);
					throw new RuntimeException("Unable to create mutable widget directory: " + mutableWidgetDir.getAbsoluteFile(),e);
				}
	        }
	        
	        if (!mutableObjectStoreDir.exists()) {
	        	try {
	        		mutableObjectStoreDir.mkdirs();
				} catch (Exception e) {
					log.error("Unable to create mutable object store directory: {}. Error: {} ", mutableObjectStoreDir.getAbsolutePath(), PSExceptionUtils.getMessageForLog(
                            e));
                    log.debug(PSExceptionUtils.getDebugMessageForLog(e));
					throw new RuntimeException("Unable to create mutable object store directory: " + mutableObjectStoreDir.getAbsoluteFile(),e);
				}
	        }
	        
	        try {
		        if(ppkg.exists()) {
		        	FileUtils.copyFileToDirectory(ppkg, mutableWidgetDir);
		    	}
		    	else {
		    		throw new RuntimeException("Widget ppkg file must exist for copy to mutable directory.");
		    	}
			    FileFilter ceFilter = FileFilterUtils.prefixFileFilter("psx_ce");
			    FileUtils.copyDirectory(objectStoreDir, mutableObjectStoreDir, ceFilter);
	        }
		    catch (Exception e) {
	            log.error("An unexpected Exception occurred while saving the widget definition. Error: {}",
                        PSExceptionUtils.getMessageForLog(e));
                log.debug(PSExceptionUtils.getDebugMessageForLog(e));
	            if (e instanceof RuntimeException) {
	            	throw (RuntimeException) e;
	            }
	            throw new RuntimeException("Failed to copy widget package mutables.", e);
	        }
        }
    }
}
