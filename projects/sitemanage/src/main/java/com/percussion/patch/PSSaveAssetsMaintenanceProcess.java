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

package com.percussion.patch;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.percussion.assetmanagement.data.PSAsset;
import com.percussion.assetmanagement.service.IPSAssetService;
import com.percussion.assetmanagement.service.impl.PSAssetService;
import com.percussion.category.marshaller.PSCategoryMarshaller;
import com.percussion.category.marshaller.PSCategoryUnMarshaller;
import com.percussion.cms.objectstore.PSInvalidContentTypeException;
import com.percussion.cms.objectstore.PSItemDefinition;
import com.percussion.cms.objectstore.server.PSItemDefManager;
import com.percussion.design.objectstore.PSExtensionCall;
import com.percussion.design.objectstore.PSExtensionCallSet;
import com.percussion.design.objectstore.PSField;
import com.percussion.design.objectstore.PSFieldTranslation;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.itemmanagement.service.IPSItemWorkflowService;
import com.percussion.itemmanagement.service.impl.PSItemWorkflowService;
import com.percussion.linkmanagement.service.IPSManagedLinkService;
import com.percussion.linkmanagement.service.impl.PSManagedLinkService;
import com.percussion.maintenance.service.IPSMaintenanceManager;
import com.percussion.maintenance.service.IPSMaintenanceProcess;
import com.percussion.maintenance.service.impl.PSMaintenanceManager;
import com.percussion.pagemanagement.service.IPSPageService;
import com.percussion.pagemanagement.service.impl.PSPageService;
import com.percussion.search.PSSearchIndexEventQueue;
import com.percussion.security.PSSecurityProvider;
import com.percussion.server.PSRequest;
import com.percussion.server.PSServer;
import com.percussion.services.notification.IPSNotificationListener;
import com.percussion.services.notification.IPSNotificationService;
import com.percussion.services.notification.PSNotificationEvent;
import com.percussion.services.notification.PSNotificationEvent.EventType;
import com.percussion.share.dao.impl.PSIdMapper;
import com.percussion.share.service.IPSIdMapper;
import com.percussion.tablefactory.PSJdbcDbmsDef;
import com.percussion.tablefactory.install.RxLogTables;
import com.percussion.util.PSSqlHelper;
import com.percussion.utils.PSJsoupPreserver;
import com.percussion.utils.io.PathUtils;
import com.percussion.utils.request.PSRequestInfo;
import com.percussion.utils.timing.PSTimer;
import com.percussion.utils.types.PSPair;
import com.percussion.webservices.PSWebserviceUtils;
import com.percussion.webservices.system.IPSSystemWs;
import com.percussion.webservices.system.PSSystemWsLocator;
import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;


/**
 * Maintenance task to save all the assets related to managed links
 * @author robertjohansen
 *
 */
public class PSSaveAssetsMaintenanceProcess implements Runnable,
        IPSMaintenanceProcess, IPSNotificationListener {

    private static final Logger log = LogManager.getLogger(PSSaveAssetsMaintenanceProcess.class);
    static final String MAINT_PROC_NAME = PSSaveAssetsMaintenanceProcess.class.getName();
    private IPSMaintenanceManager maintenanceManager;
    private IPSItemWorkflowService itemWorkflowService;
    private IPSAssetService assetService;
    private IPSManagedLinkService managedLinkService;
    private IPSNotificationService notificationService;
    private IPSPageService pageService;
    private IPSIdMapper idMapper;
    private Connection conn = null;
    private Set<ItemWrapper> assetListSet;
    private Set<ItemWrapper> qualifiedPages;
    private PSJdbcDbmsDef dbmsDef;
    private String processLinksBase = "upgrade" + File.separator + "processedlinks" + File.separator;
    private String savedLinksBase = processLinksBase + "savedlinks"  + File.separator;
    
    private String assetsLogFilePath = processLinksBase + "Assets.json";
    private String pagesLogFilePath = processLinksBase + "Pages.json";
    private String assetsReadFilePath = savedLinksBase + "Assets.json";
    private String pagesReadFilePath = savedLinksBase + "Pages.json";

    private boolean coreStarted = false;
    private boolean indexStarted = false;
    private boolean packageStarted = false;
    private volatile boolean hasRun = false;


    public PSSaveAssetsMaintenanceProcess(PSMaintenanceManager maintenanceManager, 
            PSAssetService assetService, PSItemWorkflowService itemWorkflowService, 
            PSManagedLinkService managedLinkService, PSIdMapper idMapper,
            PSPageService pageService)
    {
        this.maintenanceManager = maintenanceManager;
        this.assetService = assetService;
        this.itemWorkflowService = itemWorkflowService;
        this.managedLinkService = managedLinkService;
        this.idMapper = idMapper;
        this.pageService = pageService;
        assetListSet = new HashSet<>();
    }

    /**
     * constructor for unit testing purposes
     * @param maintenanceManager
     */
    public PSSaveAssetsMaintenanceProcess(IPSMaintenanceManager maintenanceManager)
    {
        this.maintenanceManager = maintenanceManager;
        assetListSet = new HashSet<>();
    }
    
    /**
     * Notify the Maintenance process chain that this fix is completed.
     */
    private void notifyComplete()
    {
        if (notificationService != null) {
            notificationService.notifyEvent(new PSNotificationEvent(EventType.SAVE_ASSETS_PROCESS_COMPLETE, null));
        }
    }
    
    /**
     * notify the manager that work has completed
     */
    private void completeMaintWork()
    {
        if (maintenanceManager != null)
        {
            maintenanceManager.workCompleted(this);
        }
    }

    /**
     * notify the manager that work has failed
     */
    private void failMaintWork()
    {
        if (maintenanceManager != null)
        {
            maintenanceManager.workFailed(this);
        }
    }

    /**
     * Notify the manager that work has begun
     */
    private void startMaintWork()
    {
        if (maintenanceManager != null)
        {
            maintenanceManager.startingWork(this);
        }
    }
  
    /**
     * Create a connection to the database
     * @return Connection Object may be null
     * @throws Exception
     */
    public Connection getConnection()
    {
        Connection connection = null;
        try
        {
            Properties repprops = PSJdbcDbmsDef.loadRxRepositoryProperties(PSServer.getRxDir().getAbsolutePath());
            dbmsDef = new PSJdbcDbmsDef(repprops);
            connection = RxLogTables.createConnection(repprops);
            log.debug("Connection Made: {}" , connection.toString());
        }
        catch(Exception e)
        {
            log.warn(e.getMessage(),e);
        }
        return connection;
    }
    
    /**
     * Close the connection to the database
     * @return boolean true if connection is closed, false if close fails
     */
    public boolean closeConnection()
    {
        if(conn !=null)
        {
            try
            {
                conn.close();
            }
            catch(SQLException e)
            {
                log.error(e.getMessage());
                log.debug(e.getMessage(), e);
                log.warn(e.getMessage());
                return false;
            }
            conn = null; 
        }
        else {
            log.warn("Connection already closed");
        }
        return true;
    }

    /**
     * Execute a sql statement against the connection and return the resultset
     * @param sqlStat Statement to be executed
     * @return ResultSet the results from the sql statement execution, may be null if no connection available
     */
    public ResultSet executeSqlStatement(Statement stat, String sqlStat)
    {
        ResultSet result = null;
        if(conn == null)
        {
            log.warn("Connection Object not available to execute against");
            return result;
        }
       
        try {
            result = stat.executeQuery(sqlStat);
        } catch (Exception e) {

            log.error("executeSqlStatement : {}" , e.getMessage());
            log.debug(e.getMessage(),e);
        } 
        return result;
    }
    
    /**
     * if logs/Assets.json exists do nothing.
     * Load Assets from database if logs/Assets.json does not exist
     * @return List<AssetWrapper> asset list, never null, may be empty
     */
    public Set<ItemWrapper> loadAssets()
    {
        File logFile = new File(PathUtils.getRxDir(null),assetsLogFilePath);
        File readFile = new File(PathUtils.getRxDir(null),assetsReadFilePath);
        if(readFile.exists())
            loadFailedAssetsFromFile(readFile);
        else if(!logFile.exists()) {
            loadAssetsFromDB();
        }
        else
        {
            log.info("Found previous assets file not processing assets.");
            assetListSet = new HashSet<>();
        }   
        return assetListSet;
    }
    
    /**
     * load content Ids from database for Types that have managed link fields
     * into the asset list.
     * 
     */
    public void loadAssetsFromDB()
    {
        PSItemDefManager defMgr = PSItemDefManager.getInstance();
        String[] allContentTypes = defMgr.getContentTypeNames(-1);
        for (String contentType : allContentTypes)
        {
            //Here we are dealing with assets, skip the page content type here
            if("percPage".equalsIgnoreCase(contentType))
            {
                continue;
            }
            if (getManagedLinkFields(contentType).size()>0)
            {
                try
                {
                    long typeId = defMgr.contentTypeNameToId(contentType);
                    addTypeAssets(typeId,contentType);
                }
                catch (PSInvalidContentTypeException e)
                {
                    log.error("Cannot load content type with name " +contentType);
                }
              
            }
        }
    }
    
    private void addTypeAssets(long contentTypeId, String typeName)
    {
        conn = getConnection();
        Statement rawSelectStat = null;
        ResultSet idresult = null;
        try
        {
            String CONTENTSTATUS = PSSqlHelper.qualifyTableName("CONTENTSTATUS");
            String typeIdSelect = "SELECT CONTENTID FROM " + CONTENTSTATUS +" WHERE CONTENTTYPEID = "+ contentTypeId;
           
            rawSelectStat = conn.createStatement();
            idresult = executeSqlStatement(rawSelectStat,typeIdSelect);
            if (idresult != null) {
                addAssets(getAssetFromResult(idresult, "CONTENTID"));
            }
        }
        catch(Exception e)
        {
            log.error("Exception loading assets for type {}", typeName);
        }
        finally
        {
            try{idresult.close();} catch(Exception e){}
            try{rawSelectStat.close();} catch(Exception e){}
            try{conn.close();} catch(Exception e){}
        }
        log.info("Finished Loading Assets for type {}", typeName);
        
    }
    
    /**
     * Read the assets from the asset log and remove the success assets
     * so that we are left with only unprocessed and failed assets to try them again
     * @param File f may not be null
     */
    @SuppressWarnings("unchecked")
    public void loadFailedAssetsFromFile(File f)
    {   
        assetListSet = new HashSet<>();
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            addAssets((Set<ItemWrapper>) objectMapper.readValue(f, 
                    objectMapper.getTypeFactory().constructCollectionType(Set.class, ItemWrapper.class)));
            
            Iterator<ItemWrapper> it = assetListSet.iterator();
            while(it.hasNext())
            {
                ItemWrapper asset = it.next();
                if(asset.getStatus() == ItemWrapper.STATUS.SUCCESS ||
                        asset.getStatus() == ItemWrapper.STATUS.NOTQUALIFIED) {
                    it.remove();
                }
            }
        } catch (Exception e) {
            log.error("Error Reading Log File : {}" , e.getMessage());
            log.debug(e.getMessage(), e);
        }
    }
    
    /**
     * Gets the pages from CT_PAGE table grouped by contentid and gets the max revision for each page 
     * whose summary is not null. From the result set builds the item wrappers for the qualified pages.
     * @throws SQLException
     */
    private void loadPagesFromDB() throws SQLException
    {
        conn = getConnection();
        Statement stat = null;
        ResultSet resultSet = null;
        try
        {
            String TABLE = PSSqlHelper.qualifyTableName("CT_PAGE", getDBDef().getDataBase(), 
                    getDBDef().getSchema(), getDBDef().getDriver());
            String stmt = "SELECT C.CONTENTID, C.PAGE_SUMMARY FROM "
                    + "(SELECT MAX(A.REVISIONID) AS REVISIONID, A.CONTENTID FROM " + TABLE
                    + " A GROUP BY A.CONTENTID) AS B INNER JOIN " + TABLE + " C ON "
                    + "B.CONTENTID = C.CONTENTID AND B.REVISIONID = C.REVISIONID AND "
                    + "C.PAGE_SUMMARY IS NOT NULL";
            stat = conn.createStatement();
            resultSet = executeSqlStatement(stat, stmt);
            qualifiedPages = getQualifiedPages(resultSet);
        }
        finally
        {
            try
            {
                if(resultSet!=null) {
                    resultSet.close();
                }
            } catch(Exception e){}
            try
            {
                if(stat!=null) {
                    stat.close();
                }
            } catch(Exception e){}
            closeConnection();
        }
    }
    

    /**
     * Returns qualified pages, means searches the page summary for anchors or images that are non managed internal links
     * @param result set assumed not <code>null</code>.
     * @return Set of item wrappers never <code>null</code> may be empty.
     */
    private Set<ItemWrapper> getQualifiedPages(ResultSet result)
    {
        Set<ItemWrapper> list = new HashSet<>();
        try
        {
            while(result.next())
            {
                int id = result.getInt("CONTENTID");
                String sum = result.getString("PAGE_SUMMARY");
                Document doc = Jsoup.parseBodyFragment(PSJsoupPreserver.formatPreserveTagsForJSoupParse(sum));
                //get all anchor links with an href attr but that does not have a perc-linkid attr
                Elements anchors = doc.select(IPSManagedLinkService.A_HREF + ":not(a["+IPSManagedLinkService.PERC_LINKID_ATTR+"])").select(":not([sys_dependentid])");
                //get all img links with an src attr but that does not have a perc-linkid attr
                Elements imgs = doc.select(IPSManagedLinkService.IMG_SRC + ":not(img["+IPSManagedLinkService.PERC_LINKID_ATTR+"])").select(":not([sys_dependentid])");
                if((anchors.size() > 0 || imgs.size() > 0) && qualifyLinkPaths(anchors,imgs))
                {
                    ItemWrapper page = new ItemWrapper(id,ItemWrapper.STATUS.UNPROCESSED);
                    list.add(page);
                }
            } 
        }
        catch(Exception e)
        {
            log.error(e.getMessage());
            log.debug(e.getMessage(), e);
        }
        return list;
    }
    
    /**
     * generic load Assets from table.colname into asset list
     * @param tableName
     * @param colName
     * @return List<Integer> asset list never null but may be empty
     * @throws SQLException 
     */
    public Set<ItemWrapper> loadAssets(String tableName,String colName) throws SQLException
    {
        String TABLE = PSSqlHelper.qualifyTableName(tableName, getDBDef().getDataBase(), 
                getDBDef().getSchema(), getDBDef().getDriver());
        String statement = "SELECT " + TABLE + "." + colName + " FROM " + TABLE;
        Statement stat = conn.createStatement();
        ResultSet result = executeSqlStatement(stat, statement);
        addAssets(getAssetFromResult(result,colName));
        return assetListSet;
    }
    
    /**
     * Pull the asset's content that could contain a link and pass it through jsoup
     * @return boolean True if asset needs to be managed, false otherwise
     */
    public boolean qualifyAsset(PSAsset asset)
    {   
        boolean qualified = false;
        PSPair<Boolean, String> prResult;
        
        List<String> managedFields = getManagedLinkFields(asset.getType());
        
        for (String field : managedFields)
        {
            if(asset.getFields().get(field)!=null)
            {
                prResult = processLinks(asset.getFields().get(field).toString());
                if (prResult.getFirst()) {
                    asset.getFields().put(field, prResult.getSecond());
                }
                qualified |= prResult.getFirst();
            }
        }
        return qualified;
    }

    /**
     * For a type name get a list of field names that use managed links.
     * @param typeName
     * @return List of field names
     */
    public List<String>  getManagedLinkFields(String type)
    {
        List<String> managedFields = new ArrayList<>();
        PSItemDefManager defMgr = PSItemDefManager.getInstance();
        try
        {
            PSItemDefinition itemDef = defMgr.getItemDef(type, -1);
           
            for (PSField field : itemDef.getFieldSet().getAllFields())
            {
                if (isManagedLinkField(field))
                {
                    managedFields.add(field.getSubmitName());
                }
            }
        }
        catch (PSInvalidContentTypeException e)
        {
           throw new IllegalArgumentException("Cannot get type definition "+type,e);
        }
        return managedFields;
    }
    
    /**
     * Does the field handle managed links.  This is found by checking for the sys_manageLinksConverter 
     * or sys_manageLinksOnUpdate input translation extensions on the field
     * 
     * @param field object
     * @return true if the field handles managed links.
     */
    private boolean isManagedLinkField(PSField field)
    {
        boolean managedLinkField = false;
        PSFieldTranslation inputTranslation = field.getInputTranslation();
        if (inputTranslation != null)
        {
            PSExtensionCallSet translations = inputTranslation.getTranslations();
            if (translations.size()>0)
            {
                for(int i=0; i<translations.size();i++)
                {
                    PSExtensionCall extCall = (PSExtensionCall)translations.get(i);
                    if (extCall!=null && (extCall.getName().equals("sys_manageLinksConverter")
                            || extCall.getName().equals("sys_manageLinksOnUpdate")))
                    {
                        managedLinkField = true;
                        break;
                    }
                }
            }
        }
        return managedLinkField;
    }
    
    /**
     * Checks the source from the asset to see if it has any links that are not already managed
     * denoted by perc-linkid
     * Uses Jsoup to parse through the content source
     * @param source
     * @return PSPair the first object is a boolean tells whether the source has any unmanaged internal links or not and the
     * second object is the updated source 
     */
    private PSPair<Boolean, String> processLinks(String source)
    {
        boolean hasUnmanagedLinks = true;
        source = PSJsoupPreserver.formatPreserveTagsForJSoupParse(source);
        Document doc = Jsoup.parseBodyFragment(source);
        //get all anchor links with an href attr but that does not have a perc-linkid attr
        Elements anchors = doc.select(IPSManagedLinkService.A_HREF + ":not(a["+IPSManagedLinkService.PERC_LINKID_ATTR+"])");
        //get all img links with an src attr but that does not have a perc-linkid attr
        Elements imgs = doc.select(IPSManagedLinkService.IMG_SRC + ":not(img["+IPSManagedLinkService.PERC_LINKID_ATTR+"])");
        if(anchors.isEmpty() && imgs.isEmpty()) {
            hasUnmanagedLinks = false;
        }
        else
        {
            hasUnmanagedLinks = qualifyLinkPaths(anchors, imgs);
        }
        
        PSPair<Boolean, String> result = new PSPair<>(hasUnmanagedLinks, doc.html());
        return result;
    }
    
    /**
     * qualify the paths in the links start with //Assets,
     * /Assets, //Sites, /Sites
     * @param anchors
     * @param imgs
     * @return boolean, true if path refers to CM1 path, otherwise false
     */
    private boolean qualifyLinkPaths(Elements anchors, Elements imgs)
    {   
        boolean result = false;
        for(Element anchor : anchors)
        {
            String sysDependant = anchor.attr("sys_dependentid");
            if(StringUtils.isEmpty(sysDependant))
            {
                String path = anchor.attr("href");
                if((path.startsWith("/Sites/") || path.startsWith("/Assets/") 
                        || path.startsWith("//Sites/") || path.startsWith("//Assets/")))
                {
                    result = true;
                }
            }
        }
        
        for(Element img : imgs)
        {
            String sysDependant = img.attr("sys_dependentid");
            if(StringUtils.isEmpty(sysDependant))
            {
                String path = img.attr("src");
                if((path.startsWith("/Sites/") || path.startsWith("/Assets/") 
                        || path.startsWith("//Sites/") || path.startsWith("//Assets/")))
                {
                    result = true;
                }
            }
        }
        
        //no qualified paths found there fore nothing to do
        return result;
    }
    
    /**
     * Checkout and load the asset throws exception must be caught when calling this method
     * @param id
     * @return PSAsset from given id can be null
     * @throws Exception 
     */
    public PSAsset checkOutAndLoadAsset(int id) throws Exception
    {
        PSAsset asset = null;
        //load the asset
        PSLocator locator = new PSLocator(id,-1);
        String guid = idMapper.getString(locator);
        asset = assetService.load(guid);
        //Check whether the asset has content with links that needs fixes.
        if(qualifyAsset(asset))
        {
            //checkout if asset is worth checking out
            //if checked out by someone force checkout...
            if(itemWorkflowService.isCheckedOutToSomeoneElse(guid)) {
                itemWorkflowService.forceCheckOut(guid);
            }
            else {
                itemWorkflowService.checkOut(guid);
            }
            PSAsset assetnew = assetService.load(guid);
            assetnew.setFields(asset.getFields());
            asset = assetnew;
        }
        else {
            asset = null;
        }
        return asset;
    }
    
    /**
     * Save and check-in the Asset
     * always check in the asset so that we do not leave content in a bad state
     * @param PSAsset asset
     * @throws Exception - Must be caught when calling this method
     */
    public void saveAsset(PSAsset asset) throws Exception
    {
        try
        {
            assetService.save(asset);
        }
        finally {
            itemWorkflowService.checkIn(asset.getId());   
        }
    }
    
    /**
     * get a Set of AssetWrappers from result
     * Assumes result contains content ids
     * @param result
     * @param colName
     * @return List<Integer> list of ids
     */
    public Set<ItemWrapper> getAssetFromResult(ResultSet result, String colName)
    {
        Set<ItemWrapper> list = new HashSet<>();
        try
        {
            while(result.next())
            {
                int id = result.getInt(colName);
                ItemWrapper asset = new ItemWrapper(id,ItemWrapper.STATUS.UNPROCESSED);
                list.add(asset);
            } 
        }
        catch(Exception e)
        {
            log.error(e.getMessage());
            log.debug(e.getMessage(), e);
        }
        return list;
    }
    
    /**
     * Get the database definition
     * @return PSJdbcDbmsDef dbmsDef may be null if connection is null
     */
    public PSJdbcDbmsDef getDBDef()
    {
        return dbmsDef;
    }
    
    /**
     * Add Asset set to the global asset Set
     * @param ids
     */
    private void addAssets(Set<ItemWrapper> assets)
    {
        for(ItemWrapper asset : assets) {
            assetListSet.add(asset);
        }
    }
    
    /**
     * get the current asset set
     * @return asset set
     */
    public Set<ItemWrapper> getAssetListSet() {
        return assetListSet;
    }
    
    /**
     * write all assets to a log file in json format
     * [{"id":011, "status":"SUCCESS"},{"id":012, "status":"FAIL"},
     * {"id":013, "status":"NOTQUALIFIED"},{"id":014, "status":"UNPROCESSED"}]
     * @throws IOException 
     * @throws JsonMappingException 
     * @throws JsonGenerationException 
     */
    public void logAssets() throws JsonGenerationException, JsonMappingException, IOException
    {
        File file = new File(PathUtils.getRxDir(null),processLinksBase);
        if(!file.exists())
        {
            file.mkdirs();
        }
        log.info("Logging Assets to {}" , assetsLogFilePath);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.writeValue(new File(PathUtils.getRxDir(null),assetsLogFilePath), assetListSet);
    }
    
    private void logPages() throws JsonGenerationException, JsonMappingException, IOException
    {
        File file = new File(PathUtils.getRxDir(null),processLinksBase);
        if(!file.exists())
        {
            file.mkdirs();
        }
        log.info("Logging Pages to {}" , pagesLogFilePath);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.writeValue(new File(PathUtils.getRxDir(null),pagesLogFilePath), qualifiedPages);
    }
    
    /**
     * This is the workhorse that opens and saves all the assets
     */
    public void processAssets()
    {   
        try
        {
            log.info("Started asset processing.");
            //load the assets into the asset list
            loadAssets();
            if(!assetListSet.isEmpty()) {
                logAssets();
            }
            int assetCount =0;
            for(ItemWrapper assetW : assetListSet)
            { 
                try
                {
                    PSAsset asset = checkOutAndLoadAsset(assetW.getId());
                    if(asset == null) {
                        assetW.setProcess(ItemWrapper.STATUS.NOTQUALIFIED);
                    }
                    else
                    {
                        saveAsset(asset);
                        assetW.setProcess(ItemWrapper.STATUS.SUCCESS);
                    }
                }
                catch(Exception e)
                {
                    log.error("Failed to process asset with id: {}  due to : {}" , assetW.getId(), e.getMessage());
                    log.debug(e.getMessage(),e);
                    assetW.setProcess(ItemWrapper.STATUS.FAIL);
                }
                assetCount += 1;
                if(assetCount%250 == 0)
                {
                    log.info("Processed {}  assets out of {}", assetCount ,  assetListSet.size());
                    try{logAssets();} catch (Exception e) {log.warn("Trouble logging assets." , e);}
                }
            }
        }
        catch(Exception e)
        {
            log.error("Could not run asset fix: {}" , e.getMessage());
            log.debug(e.getMessage(),e);
        }
        
        //log state after having run through all ids if anything in the assetList 
        if(!assetListSet.isEmpty())
        {
            try
            {
                logAssets();
            }
            catch(Exception e)
            {
                log.error("Failed to complete logging of ids.", e);
            }
        }
        
        //set the manage all property back to what it was in the server.properties
        log.info("Completed Asset Fix.");
    }    
    
    /**
     * Gives the maintenance manager an id to assign to this process
     */
    @Override
    public String getProcessId() {
        return MAINT_PROC_NAME;
    }

    @Override
    public void run() {
        try
        { 
            PSTimer timer = new PSTimer(log);
            PSRequest req = PSRequest.getContextForRequest();
            PSRequestInfo.initRequestInfo((Map<String,Object>) null);
            PSRequestInfo.setRequestInfo(PSRequestInfo.KEY_PSREQUEST, req);
            PSWebserviceUtils.setUserName(PSSecurityProvider.INTERNAL_USER_NAME);
            
            IPSSystemWs sysSvc = PSSystemWsLocator.getSystemWebservice();
            sysSvc.switchCommunity("Default");
            log.info("Started processing pages and assets - this may take a while depending on your content. We suggest a relaxing cup of tea while you wait.");
            processAssets();
            processPages();
            
            // Move Me should go into separate job somewhere or we abstract out this class so we run all the sitemanage
            // work together.
            // Adding logic to check the category xml is existing or not, if not create one here.
            // The null passed in the method calls here is for the sitename.
            // When categories implementation is modified for 'per site', there can be site name passed here.
            if(PSCategoryUnMarshaller.createCategoryFileIfNotExisting() == null) {
                PSCategoryMarshaller marshaller = new PSCategoryMarshaller();
                
                marshaller.setCategory(PSCategoryUnMarshaller.getEmptyCategory());
                marshaller.marshal();
            }
            
            notifyComplete();
            completeMaintWork();
            log.info("Completed processing of pages and assets. Hope you enjoyed your cup of tea.");
            
            timer.logElapsed("Asset Fix Time elapsed: ");
        }
        catch (Exception e)
        {
            log.error("Failed to complete Save Assets Process. To try again either delete RXRoot/logs/Assets.json to start"
                    + " over or, Copy RXRoot/logs/Assets.json to RXRoot/logs/saveassets/Assets.json to try from point of failure.", e);
            failMaintWork();
        }
    }

    /**
     * Processes pages, loads the qualified pages first and then if the qualified pages are not empty then
     * force saves the pages and then logs the update pages.
     */
    private void processPages() 
    {
        log.info("Started pages processing.");
        try 
        {
            loadPages();
            if(!qualifiedPages.isEmpty())
            {
                forceSavePages();
                logPages();
            }
        } 
        catch (SQLException e) 
        {
            log.error("Failed to load pages, processing of pages for fixing the summary links will not be completed",e);
        } 
        catch (Exception e) 
        {
            log.error("Failed to load pages, processing of pages for fixing the summary links will not be completed",e);
        } 
        log.info("Completed pages processing.");
    }
    
    /**
     * Checks whether Pages.json file exists with the path defined by {@link #pagesReadFilePath} if exists
     * loads the file and processes the previously failed or unprocessed entries from this file.
     * Otherwise tries to check whether Pages.json file exists with the path defined {@link #pagesLogFilePath}
     * if exists, assumes that the page processing is already done and doesn't load the pages and makes an empty
     * set for {@link #qualifiedPages}.
     * If the log file doesn't exist then loads the pages from database, which sets the {@link #qualifiedPages} to
     * the qualified pages. 
     *  
     * @throws SQLException
     * @throws JsonGenerationException
     * @throws JsonMappingException
     * @throws IOException
     */
    private void loadPages() throws SQLException, JsonGenerationException, JsonMappingException, IOException
    {
        File logFile = new File(pagesLogFilePath);
        File readFile = new File(pagesReadFilePath);
        if(readFile.exists())
        {
            loadPagesFromFile(readFile);
        }
        else if(logFile.exists())
        {
            qualifiedPages = new HashSet<>();
            log.info("Found previously processed pages log file, skipping them in this run.");
        }
        else
        {
            loadPagesFromDB();
            logPages();
        }   
    }
    
    /**
     * Loads the qualified pages from the file
     * @param readFile assumed not <code>null</code>
     */
    private void loadPagesFromFile(File readFile) 
    {
        qualifiedPages = new HashSet<>();
        ObjectMapper objectMapper = new ObjectMapper();
        try 
        {
            qualifiedPages.addAll((Set<ItemWrapper>) objectMapper.readValue(
                    readFile, objectMapper.getTypeFactory().constructCollectionType(
                            Set.class, ItemWrapper.class)));
            Iterator<ItemWrapper> it = qualifiedPages.iterator();
            while (it.hasNext()) 
            {
                ItemWrapper page = it.next();
                if (page.getStatus().equals(ItemWrapper.STATUS.SUCCESS)
                        || page.getStatus().equals(ItemWrapper.STATUS.NOTQUALIFIED)) {
                    it.remove();
                }
            }
        } 
        catch (Exception e) 
        {
            log.error("Error Reading Pages Log File : {}" , e.getMessage());
            log.debug(e.getMessage(),e);
        }
    }

    /**
     * Force checks out the pages and then loads the pages and saves them and then checks pages in.
     * Catches the errors and logs them and continues with the rest of the pages.
     */
    private void forceSavePages()
    {
        for (ItemWrapper qpage : qualifiedPages) 
        {
            PSLocator locator = new PSLocator(qpage.getId(),-1);
            String guid = idMapper.getString(locator);
            boolean failed = false;
            try
            {
                itemWorkflowService.forceCheckOut(guid);
                pageService.save(pageService.load(guid));
            }
            catch(Exception e)
            {
                failed = true;
                qpage.setProcess(ItemWrapper.STATUS.FAIL);
                log.error("Failed to load and save the page with ID {}" , guid);
                log.debug(e.getMessage(),e);
            }
            
            //Lets try to check in the page here.
            try
            {
                itemWorkflowService.checkIn(guid);
                if(!failed) {
                    qpage.setProcess(ItemWrapper.STATUS.SUCCESS);
                }
            }
            catch(Exception e)
            {
                log.error("Failed to check in the page after processing with ID:{}" , guid);
                log.debug(e.getMessage(),e);
            }
        }
    }
    
    /**
     * Wrapper of asset so that we can track status and id and serialize as json objects
     * @author robertjohansen
     *
     */
    public static class ItemWrapper{
        private Integer id;
        private STATUS status = STATUS.UNPROCESSED;
        private enum STATUS{
            UNPROCESSED,
            FAIL,
            SUCCESS,
            NOTQUALIFIED
        }
        
        @JsonCreator
        public ItemWrapper(@JsonProperty("id") Integer id, @JsonProperty("status") STATUS status)
        {
            this.id = id;
            this.status = status;
        }
        
        public Integer getId() {
            return id;
        }
        public void setId(Integer id) {
            this.id = id;
        }
        public STATUS getStatus() {
            return status;
        }
        public void setProcess(STATUS status) {
            this.status = status;
        }
    }
    
    /**
     * Adds the listeners for the Maintenance process chain that
     * unfortunately only relies on a chain of notifications to order
     * and start the maintenance processes. Here we wait for the core server
     * to finish initializing and for the search index queue to give its first change
     * notification.
     * @param notificationService
     */
    public void setNotificationService(IPSNotificationService notificationService)
    {
        notificationService.addListener(EventType.CORE_SERVER_POST_INIT, this);
        notificationService.addListener(EventType.SEARCH_INDEX_STATUS_CHANGE, this);
        notificationService.addListener(EventType.STARTUP_PKG_INSTALL_COMPLETE, this);
        this.notificationService = notificationService;
    }

    /**
     * Notifies the maintenance manager and register this instance as
     * a maintenance process so that we do not leave maintenance mode until
     * this process is done. This method also kicks off thread.
     */
    @Override
    public void notifyEvent(PSNotificationEvent notification) {
        if(hasRun) {
            return;
        }
        
        if (EventType.CORE_SERVER_POST_INIT == notification.getType())
        {
            startMaintWork();
           
            coreStarted = true;
        }
        if(EventType.STARTUP_PKG_INSTALL_COMPLETE == notification.getType())
        {
            packageStarted = true;
        }
        if(EventType.SEARCH_INDEX_STATUS_CHANGE == notification.getType())
        {
            PSSearchIndexEventQueue indexQueue = PSSearchIndexEventQueue.getInstance();
            if(indexQueue.getStatus().equals("Running")) {
                indexStarted = true;
            }
        }
            
        if(coreStarted && indexStarted && packageStarted)
        {
            hasRun = true;
            Thread thread = new Thread(this);
            thread.setDaemon(true);
            thread.start();
        }
            
    }

}
