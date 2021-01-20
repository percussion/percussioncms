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
package com.percussion.content;

import com.percussion.assetmanagement.data.PSAsset;
import com.percussion.assetmanagement.web.service.PSAssetServiceRestClient;
import com.percussion.content.data.AssetDef;
import com.percussion.content.data.CM1DataDef;
import com.percussion.content.data.CM1DataDef.AssetFolder;
import com.percussion.content.data.CM1DataDef.AutoGen;
import com.percussion.content.data.CM1DataDef.AutoGen.Assets;
import com.percussion.content.data.CM1DataDef.AutoGen.Assets.AssetGroup;
import com.percussion.content.data.CM1DataDef.AutoGen.Folders;
import com.percussion.content.data.CM1DataDef.AutoGen.Pages;
import com.percussion.content.data.CM1DataDef.AutoGen.Pages.PageGroup;
import com.percussion.content.data.CM1DataDef.SectionDefs;
import com.percussion.content.data.CM1DataDef.SectionDefs.SectionDef;
import com.percussion.content.data.CM1DataDef.SiteDefs.SiteDef;
import com.percussion.content.data.CM1DataDef.SiteFolder;
import com.percussion.content.data.CM1DataDef.SiteFolder.PageDef;
import com.percussion.content.data.TemplateDef;
import com.percussion.pagemanagement.data.PSPage;
import com.percussion.pagemanagement.data.PSTemplate;
import com.percussion.pathmanagement.data.PSDeleteFolderCriteria;
import com.percussion.pathmanagement.data.PSPathItem;
import com.percussion.pathmanagement.web.service.PSPathServiceRestClient;
import com.percussion.sitemanage.data.PSSite;
import com.percussion.sitemanage.data.PSSiteSummary;
import com.percussion.sitemanage.web.service.PSSiteRestClient;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

/**
 * This class takes an xml file that conforms to the CM1DataDef schema and
 * generates all the design objects and content as specified. It can generate
 * sites, sections, templates, assets and pages. It can also automatically
 * generate all of these objects based on 'templates' of each type, generating
 * unique names in the process. This latter capability is useful for performance
 * testing.
 * <p>
 * Accepts paths relative to /Sites/... or /Assets/..., transforming them as
 * required by each interface. This is to expose a consistent model to the user
 * of this class.
 * <p>
 * There are 2 ways to use this functionality 
 * <ol>
 *  <li>Call it from the command line. (See the main method for arg description.)</li>
 *  <li>Call it programmatically using something like the following call sequence
 * <pre>    PSContentGenerator gen = new PSContentGenerator("http://server:port", "Admin", "demo")
 *          String defFileName = "c:\...\def.xml";
 *          Object o = gen.cleanup(defFilename);
 *          gen.generateContent(o);
 * </pre>
 * If you cleanup after the tests, you may not need to run cleanup before generating,
 * but you may want to just to be safe.    
 * 
 * @author paulhoward
 */
public class PSContentGenerator
{
    //private static final Log log = LogFactory.getLog(PSContentGenerator.class);

    /* Connection info for cm1 server. Url should be of the form http://server:port */
    private String url;
    private String uid;
    private String pw;
    
    /**
     * The file from which the definition was loaded. Set by the {@link #load(String)} method.
     */
    File sourceFile;
    
    /*
     * These generators are used by multiple methods, so they are created once
     * in the ctor and shared so the login only has to happen once.
     */
    private PSFolderGenerator folderGen;
    private PSAssetGenerator assetGen;
    private PSPageGenerator pageGen;
    private PSSiteGenerator siteGen;
    private PSSiteRestClient siteClient;
   
    /**
     * Track objects that need to be deleted so a delete file can be generated
     * at the end of processing. No need to track objects associated with a site
     * if that site was generated by this class as they will automatically be
     * deleted when the site is deleted.
     * <p>
     * Set in ctor, then never <code>null</code>.
     */
    private Properties generatedObjects;
    
    /**
     * The leading part of the path for nodes  that appear under the 
     * Assets tree in the cm1 ui.
     */
    private static final String ASSETS_PATH_PREFIX = "/Assets";

    /**
     * The leading part of the path for nodes  that appear under the 
     * Sites tree in the cm1 ui.
     */
    private static final String SITES_PATH_PREFIX = "/Sites";
    
    /**
     * The ctor for programmatic access. See the <code>main</code> method for
     * param descriptions.
     */
    public PSContentGenerator(String url, String uid, String pw, File dataDef)
    {
        this.url = url;
        this.uid = uid;
        this.pw = pw;
        this.folderGen = new PSFolderGenerator(url, uid, pw);
        this.assetGen = new PSAssetGenerator(url, uid, pw, dataDef.getParentFile());
        this.pageGen = new PSPageGenerator(url, uid, pw);
        this.siteGen = new PSSiteGenerator(url, uid, pw);
        this.sourceFile = dataDef;
        generatedObjects = new Properties();
        
        siteClient = new PSSiteRestClient(url);
        siteClient.login(uid, pw);
    }
    
    public PSContentGenerator(String url, String uid, String pw)
    {
        this.url = url;
        this.uid = uid;
        this.pw = pw;
        
        siteClient = new PSSiteRestClient(url);
        siteClient.login(uid, pw);
    }
    
    public void deleteAllSites()
    {
        List<PSSiteSummary> sums = siteClient.findAll();
        for(PSSiteSummary site : sums)
        {
           siteClient.deleteSite(site.getId());
        }
    }
   
    /**
     * Usage: java -cp ... -Dlog4j.configuration=com/percussion/content/log4j.properties com.percussion.content.PSContentGenerator baseUrl uid pw dataDefFile
     * <pre>
     * Where
     *    baseUrl is the scheme, server and port, e.g. http://localhost:9992
     *    uid is a user id to use to perform the operations, generally Admin
     *    pw is the user's password, generally demo
     *    dataDef is the file path to the xml definition file. Must conform to CM1DataDef schema.
     *     
     * @param args
     * @throws JAXBException
     * @throws FileNotFoundException
     */
    public static void main(String[] args)
        throws JAXBException, FileNotFoundException
    {
        String url = args[0];
        String uid = args[1];
        String pw = args[2];
        String defFileName = args[3];
        
        PSContentGenerator gen = new PSContentGenerator(url, uid, pw, new File(defFileName));
        Object o = gen.cleanup();
        gen.generateContent(o);
    }
        
    /**
     * Creates all the sites, templates and such defined in the specified definition file. 
     * You should call <code>cleanup</code> first as this code assumes that none of the 
     * objects exist and will fail and stop if they do.
     * 
     * @param o The object returned from the {@link #cleanup(String)} method.
     * 
     * @throws JAXBException If the file is malformed according to the schema.
     * @throws FileNotFoundException If the file doesn't exist.
     */
    public void generateContent(Object o)
        throws JAXBException, FileNotFoundException
    {
     // log.info("Using CM1 server " + url + " connecting as " + uid + ":" +
        // pw);
        System.out.println("Using CM1 server " + url + " connecting as " + uid + ":" + pw);
        CM1DataDef dataDef = (CM1DataDef) o;

        if (dataDef.getAutoGen() == null)
        {
            generateAssets(dataDef.getAssetFolder());
        }

        generateSites(dataDef);

        if (dataDef.getTemplateDefs() != null)
        {
            generateTemplates(dataDef.getTemplateDefs().getTemplateDef());
        }

        generateSections(dataDef.getSectionDefs());

        if (dataDef.getAutoGen() != null)
        {
            autoGenerateContent(dataDef);
        }
        else
        {
            generatePages(dataDef.getSiteFolder());
        }
        // log.info(generatedObjects.toString());
        System.out.println(generatedObjects.toString());
        // log.info("Finished");
        System.out.println("Finished");
    }

    /**
     * Permanently deletes objects that would be (or were) created such as sites, templates
     * and such. Note that if a site is defined in the def, everything associated with that
     * site will be deleted, even if it wasn't created by this file.
     * <p>
     * Currently, pages created in an existing site are not deleted.
     *  
     * @param defFileName Generally, the fully qualified path and name of the xml file that 
     * conforms to the cm1DataDef schema. Relative paths are probably relative to where the
     * program is run from.
     * @return This object should be passed to the {@link #generateContent(Object)} method
     * if you wish to follow the cleanup with content generation.
     * 
     * @throws JAXBException
     * @throws FileNotFoundException
     */
    public Object cleanup()
        throws JAXBException, FileNotFoundException
    {
        CM1DataDef dataDef = load(sourceFile);
        cleanup(dataDef);
        return dataDef;
    }
    
    /**
     * Attempts to load the xml file pointed to by <code>def</code> and converts it
     * to the returned data object. Throws an exception if the file doesn't conform
     * to the proper schema.
     * 
     * @param def Assumed not <code>null</code>.
     * @return Never <code>null</code>.
     */
    private CM1DataDef load(File dataDef)
        throws JAXBException, FileNotFoundException
    {
        //log.info("Using definition file: " + dataDef.getAbsolutePath());
        System.out.println("Using definition file: " + dataDef.getAbsolutePath());
        JAXBContext jc = JAXBContext.newInstance("com.percussion.content.data");
        Unmarshaller um = jc.createUnmarshaller();
        return (CM1DataDef) um.unmarshal(new FileInputStream(dataDef));
    }
    
    
    private void generateSections(List<SectionDefs> sectionDefs)
    {
        for (SectionDefs sectionGroup : sectionDefs){
            String parentPath = SITES_PATH_PREFIX + sectionGroup.getParentPath();
            for (SectionDef def : sectionGroup.getSectionDef()) {
                siteGen.createSection(def, parentPath);
            }
        }
    }


    private void generatePages(List<SiteFolder> siteFolders)
    {
        for (SiteFolder siteFolder : siteFolders)
        {
            String path = siteFolder.getPath();
            List<PageDef> pageDefs = siteFolder.getPageDef();
            if (pageDefs.isEmpty())
            {
                PSPathItem item = folderGen.createFolderPath(SITES_PATH_PREFIX + path);
            }
            else
            {
                for (PageDef pageDef : pageDefs)
                {
                    PSPage page = pageGen.createPage(pageDef, path);
                }
            }
        }
    }


    private void generateAssets(List<AssetFolder> assetFolders)
    {
        for (AssetFolder af : assetFolders)
        {
            String path = af.getPath();
            List<AssetDef> assetDefs = af.getAssetDef();
            if (assetDefs.isEmpty())
            {
                PSPathItem item = folderGen.createFolderPath(ASSETS_PATH_PREFIX + path);
            }
            else
            {
                for (AssetDef assetDef : assetDefs)
                {
                    PSAsset asset = assetGen.createAsset(assetDef, path);
                    generatedObjects.setProperty("asset." + asset.getId(), "");
                }
            }
        }
    }


    private void generateTemplates(List<TemplateDef> templateDefs)
    {
        PSTemplateGenerator templateGen = new PSTemplateGenerator(url, uid, pw);
        for (TemplateDef templateDef : templateDefs)
        {
            PSTemplate template = templateGen.createTemplate(templateDef);
        }
    }


    private void autoGenerateContent(CM1DataDef dataDef)
    {
        AutoGen autoGen = dataDef.getAutoGen();
        List<Folders> folderDefs = autoGen.getFolders();
        for (Folders f : folderDefs)
        {
            String path = f.getBasePath();
            if (f.getType().equalsIgnoreCase("sites"))
            {
                validateSite(path, "Site in autogen 'Folders' basePath does not exist");
                path = SITES_PATH_PREFIX + path;
            }
            else if (f.getType().equalsIgnoreCase("assets"))
                path = ASSETS_PATH_PREFIX + path;
            else
            {
                throw new RuntimeException("Unsupported type on Folders element (must be Sites or Assets): " 
                        + f.getType());
            }
            folderGen.createFolderPath(path);
            Collection<String> folderPathNames = generateFolderPathNames(path, f.getCount(), f.getMaxDepth(), 
                    f.getBreadthFactor());
            for (String folderPathName : folderPathNames)
            {
                PSPathItem pathItem = folderGen.createFolderPath(folderPathName);
                //log.info("Created folder " + pathItem.getPath());
                System.out.println("Created folder " + pathItem.getPath());
            }
        }
        
        
        Map<String, AssetDef> assetDefsByName = new HashMap<String, AssetDef>();
        for (AssetFolder assetFolder : dataDef.getAssetFolder())
        {
            for (AssetDef def : assetFolder.getAssetDef())
            {
                assetDefsByName.put(def.getName(), def);
            }
        }
        
        List<Assets> assets = autoGen.getAssets();
        for (Assets asset : assets )
        {
            String path = asset.getBasePath();
            if (path == null || path.isEmpty())
                path = autoGen.getBasePath();
            if (path == null || path.isEmpty())
            {
                throw new RuntimeException(
                        "The basePath attribute must be specified on either the AutoGen or Assets element.");
            }
            path = ASSETS_PATH_PREFIX + path;
            folderGen.createFolderPath(path);
            
            List<AssetGroup> assetGroups = asset.getAssetGroup();
            if (assetGroups.size() == 0)
            {
                generateRandomAssets(assetDefsByName.values(), path, asset.getCount().intValue());
            }
            else
            {
                for (AssetGroup group : assetGroups)
                {
                    List<String> names = group.getAssetDefName();
                    Collection<AssetDef> desiredAssetDefs = new HashSet<AssetDef>();
                    for (String name : names)
                    {
                        if (assetDefsByName.containsKey(name))
                            desiredAssetDefs.add(assetDefsByName.get(name));
                        else
                            throw new RuntimeException("AssetDef specified by AssetDefName not found: " + name);
                    }
                    BigInteger count = group.getCount();
                    if (count == null)
                        count = asset.getCount();
                    if (count == null)
                    {
                        throw new RuntimeException(
                                "A count attribute must be specified for either the AssetGroup or Asset element.");
                    }
                    
                    generateRandomAssets(desiredAssetDefs, path, count.intValue());
                }
            }
        }

        // get the page 'templates'
        Map<String, PageDef> pageDefsByName = new HashMap<String, PageDef>();
        for (SiteFolder pageFolder : dataDef.getSiteFolder())
        {
            for (PageDef def : pageFolder.getPageDef())
            {
                pageDefsByName.put(def.getName(), def);
            }
        }
        
        //create the pages
        List<Pages> pages = autoGen.getPages();
        for (Pages page : pages)
        {
            String path = page.getBasePath();
            if (path == null || path.isEmpty())
                path = autoGen.getBasePath();
            if (path == null || path.isEmpty())
            {
                throw new RuntimeException(
                        "The basePath attribute must be specified on either the AutoGen or Pages element.");
            }
            path = SITES_PATH_PREFIX + path;
            folderGen.createFolderPath(path);
            
            List<PageGroup> pageGroups = page.getPageGroup();
            if (pageGroups.size() == 0)
            {
                generateRandomPages(pageDefsByName.values(), path, page.getCount().intValue());
            }
            else
            {
                for (PageGroup group : pageGroups)
                {
                    List<String> names = group.getPageDefName();
                    Collection<PageDef> desiredPageDefs = new HashSet<PageDef>();
                    for (String name : names)
                    {
                        if (pageDefsByName.containsKey(name))
                            desiredPageDefs.add(pageDefsByName.get(name));
                        else
                            throw new RuntimeException("PageDef specified by PageDefName not found: " + name);
                    }
                    BigInteger count = group.getCount();
                    if (count == null)
                        count = page.getCount();
                    if (count == null)
                    {
                        throw new RuntimeException(
                                "A count attribute must be specified for either the AssetGroup or Asset element.");
                    }
                    
                    generateRandomPages(desiredPageDefs, path, count.intValue());
                }
            }
        }
    }

    /**
     * Checks if the first part of the path matches any existing site (case-insensitive) and throws an exception
     * if it doesn't.
     * 
     * @param path The path to check. Assumed to begin with the site name, e.g. /sitename/...
     * @param msg Used as main part of error msg.
     */
    private void validateSite(String path, String msg) {
        int index = path.indexOf("/", 1);
        String name = path.substring(1, index).toLowerCase();
        if (!getSites().containsKey(name))
        {
            throw new RuntimeException(msg + ": '" + name + "'");
        }
    }


    /**
     * 
     * @param desiredPageDefs
     *            The 'page templates' to use when generating pages. The
     *            collection will be iterated repeatedly until the count pages
     *            have been created.
     * @param basePath
     *            Of the form /Sites/... Created pages are about equally
     *            distributed (by count) among <code>path</code> and all of
     *            its descendants.
     * @param count
     *            Total # of pages to create
     */
    private void generateRandomPages(Collection<PageDef> desiredPageDefs, String basePath,
            int count) {
        Collection<PSPathItem> folders = folderGen.getFolderPaths(basePath);
        int itemsPerFolder = count / folders.size();
        if ((count % folders.size()) != 0)
            itemsPerFolder++;
        
        int created = 0;
        Iterator<PageDef> srcDefs = new RepeatableIterator<PageDef>(desiredPageDefs);
        for (PSPathItem pathItem : folders)
        {
            for (int i = 0; i < itemsPerFolder && created < count; i++, created++)
            {
                PageDef d = srcDefs.next();
                String originalName = d.getName();
                d.setName(originalName + nameSuffixCounter++);
                String relPath = pathItem.getPath().substring(pathItem.getPath().indexOf("/", 1));
                pageGen.createPage(d, relPath);
                d.setName(originalName);
            }
        }
    }


    /**
     * 
     * @param basePath
     *            All created folders are added as descendants of this one.
     * @param count
     *            How many folders to create. negative is treated as 0
     * @param maxDepth
     *            Guaranteed to have at least 1 node that has this many
     *            ancestors between it and basePath, unless count is less than
     *            this, in which case it will be equal to count. A value less
     *            than 1 is treated as 1.
     * @param breadthFactor
     *            A scale factor that affects the distribution of generated
     *            folders, between 0 and 1. 0 will put equal #s of folders at
     *            each level, while 1 will put all folders in basePath (except
     *            for 1 branch that has maxDepth.
     * @return Never <code>null</code>. Size = <code>count</code>
     */
    private Collection<String> generateFolderPathNames(String basePath, int count,
            int maxDepth, float breadthFactor) {
        if (count < 0)
            count = 0;
        if (maxDepth < 1)
            maxDepth = 1;
        if (maxDepth > count)
            maxDepth = count;
        if (breadthFactor > 1.0)
            breadthFactor = 1.0f;
        if (breadthFactor < 0.0)
            breadthFactor = 0.0f;

        int[] foldersPerLevel = calculateFoldersPerLevel(count, maxDepth, breadthFactor);

        Collection<String> results = new ArrayList<String>();
        int level = 1;
        List<String> parents = new ArrayList<String>();
        parents.add(basePath);
        Collection<String> levelResults = new ArrayList<String>();
        for (int folderCount : foldersPerLevel)
        {
            int[] parentFolderCount = new int[parents.size()];
            distributeNumbers(parentFolderCount, folderCount);
            for (int j = 0; j < parents.size(); j++)
            {
                for (int i = 0; i < parentFolderCount[j]; i++){
                    levelResults.add(parents.get(j) + "/Folder-L" + level + "-" + i);
                }
            }
            if (level < foldersPerLevel.length)
                parents = filter(levelResults, foldersPerLevel[level++]);
            results.addAll(levelResults);
            levelResults.clear();
        }
        
        return results;
    }

    /**
     * Randomly selects <code>count</code> values and removes the rest.
     * @param values Assumed not <code>null</code>. <code>Count</code> values are kept, the rest are removed.
     * @param count How many values to keep.
     */
    private List<String> filter(Collection<String> values, int count) {
       if (count < 0) 
           count = 0;

       Set<Integer> choices = new HashSet<Integer>();
       Random r = new Random();
       int randCount = 0;
       while (randCount == 0)
           randCount = r.nextInt(count+1);
       while (choices.size() < randCount)
       {
           choices.add(new Integer(r.nextInt(values.size()))); 
       }

       Iterator<String> iter = values.iterator();
       List<String> results = new ArrayList<String>();
       int i = 0;
       while (iter.hasNext())
       {
           String value = iter.next();
           if (choices.contains(new Integer(i)))
               results.add(value);
           i++;
       }
       return results;
    }


    /**
     * Randomly distribute <code>count</code> among the buckets in <code>results</code>.
     * 
     * @param results Each entry is either floor(count/results) or that value+1;
     * @param count total count to distribute among the array. 
     */
    private void distributeNumbers(int[] results, int count) {
        if (count < 1) {
            return;
        }
        
        Random r = new Random();
        for (int i = 0; i < count; i++)
        {
            results[r.nextInt(results.length)]++;
        }
    }


    /**
     * 
     * @param count
     *            Total # of units.
     * @param maxDepth
     *            Total # of buckets. Assumed <= count.
     * @param breadthFactor
     *            Controls distribution among the buckets. Assumed 0<= factor <=
     *            1. A value of 0 distributes units about equally among buckets, a value of
     *            1 distributes more units at the lower indexes.
     * @return size is maxDepth, values are positive.
     */
    private int[] calculateFoldersPerLevel(int count, int maxDepth, float breadthFactor) {
        int[] levels = new int[maxDepth];
        if (maxDepth == 0)
            return levels;
        
        //reserve some folders to guarantee maxDepth contract
        int available = count - (maxDepth-1);
        int totalCreated = 0;
        for (int i = 0; i < levels.length && (available - totalCreated > 0); i++) {
            levels[i] = (int) ((available - totalCreated) * breadthFactor);
            if (levels[i] == 0)
                levels[i] = 1;
            totalCreated += levels[i];
        }
        
        //distribute the remaining units among the buckets, starting at the end to guarantee maxDepth
        int avg = (count - totalCreated) / levels.length;
        
        for (int i = 0; i < levels.length; i++)
            levels[i] += avg;
        totalCreated += avg * levels.length;
        
        for (int i = maxDepth-1; totalCreated < count; i--) {
            levels[i]++;
            totalCreated++;
        }

        //debugging
        int sum = 0;
        for (int levelCount : levels)
            sum += levelCount;
        assert (sum == count);
        //end debugging
        
        //log.debug("Folders per level: " + levels);
        return levels;
    }


    /**
     * Adds the sizes of all collections in the passed list.
     * @param levels Assumed not <code>null</code>.
     * @return >= 0;
     */
    private int getSize(List<Collection<String>> levels) {
        int result = 0;
        for (Collection<String> level : levels)
            result += level.size();
        return result;
    }

    /**
     * Used as counter to generate unique folder names.
     */
    private int nameSuffixCounter = 1;
    
    private void generateSites(CM1DataDef dataDef)
    {
        if (dataDef.getSiteDefs() != null)
        {
            for (SiteDef siteDef : dataDef.getSiteDefs().getSiteDef())
            {
                PSSite site = siteGen.createSite(siteDef);
                generatedObjects.setProperty("site." + site.getId(), "");
            }
        }
    }


    /**
     * 
     * @param desiredAssetDefs
     *            The 'asset templates' to use when generating assets. The
     *            collection will be iterated repeatedly until the count assets
     *            have been created.
     * @param basePath
     *            Of the form /Assets/... Created assets are about equally
     *            distributed (by count) among <code>path</code> and all of
     *            its descendants.
     * @param count
     *            Total # of assets to create
     */
    private void generateRandomAssets(Collection<AssetDef> desiredAssetDefs, String basePath, int count)
    {
        Collection<PSPathItem> folders = folderGen.getFolderPaths(basePath);
        int itemsPerFolder = count / folders.size();
        if ((count % folders.size()) != 0)
            itemsPerFolder++;
        
        int created = 0;
        Iterator<AssetDef> srcDefs = new RepeatableIterator<AssetDef>(desiredAssetDefs);
        for (PSPathItem pathItem : folders)
        {
            for (int i = 0; i < itemsPerFolder && created < count; i++, created++)
            {
                AssetDef d = srcDefs.next();
                String originalName = d.getName();
                d.setName(originalName + nameSuffixCounter++);
                PSAsset asset = assetGen.createAsset(d, pathItem.getPath().substring(pathItem.getPath().indexOf("/", 1)));
                generatedObjects.setProperty("asset." + asset.getId(), "");
                d.setName(originalName);
            }
        }
    }
    
    /**
     * Repeatedly cycles thru a list of objects.
     * 
     * @author paulhoward
     */
    class RepeatableIterator<E> implements Iterator<E>
    {
        
        private Collection<E> src;
        private Iterator<E> current;
        
        public RepeatableIterator(Collection<E> src)
        {
            this.src = src;
            current = src.iterator();
        }
        
        
        public boolean hasNext()
        {
            return src.size() > 0;
        }

        public E next()
        {
            if (!current.hasNext())
                current = src.iterator();
            return current.next();
        }

        public void remove()
        {
            throw new UnsupportedOperationException();
        }
        
    }
    
    /**
     * Cache of existing sites. Key is site name, lower-cased, value is id for that site, in string form.
     */
    private Map<String, String> siteNames;
    
    synchronized private Map<String, String> getSites()
    {
        if (siteNames == null)
        {
            List<PSSiteSummary> siteSummaries = siteClient.findAll();
            siteNames = new HashMap<String, String>();
            for (PSSiteSummary sum : siteSummaries)
            {
                siteNames.put(sum.getName().toLowerCase(), sum.getId());
            }
        }
        return siteNames;
    }
    
    /**
     * Deletes all sites and assets specified in the def by name.
     * 
     * @param dataDef Assumed not <code>null</code>.
     */
    private void cleanup(CM1DataDef dataDef)
    {
        if (dataDef.getSiteDefs() != null)
        {
            Map<String, String> siteNames = getSites();
            for (SiteDef siteDef : dataDef.getSiteDefs().getSiteDef())
            {
                String name = siteDef.getName().toLowerCase();
                if (siteNames.containsKey(name))
                {
                    siteClient.delete(siteNames.get(name));
                    //log.info("Deleted site " + siteDef.getName());
                    System.out.println("Deleted site " + siteDef.getName());
                }
            }
        }
        
        AutoGen autoGen = dataDef.getAutoGen(); 
        if (autoGen != null)
        {
            PSPathServiceRestClient pathClient = new PSPathServiceRestClient(url);
            pathClient.login(uid, pw);
            
            List<Assets> assets = autoGen.getAssets();
            for (Assets assetGroup : assets)
            {
                String path = assetGroup.getBasePath();
                if (path == null || path.isEmpty())
                    path = autoGen.getBasePath();
                path = ASSETS_PATH_PREFIX + path;
                PSDeleteFolderCriteria criteria = new PSDeleteFolderCriteria();
                criteria.setPath(path);
                try {
                    pathClient.deleteFolder(criteria);
                    //log.info("Deleted folder " + path);
                    System.out.println("Deleted folder " + path);
                } catch (RuntimeException e) {
                    //ignore if not there
                }
            }
        }
        else
        {
            List<AssetFolder> assetDefs = dataDef.getAssetFolder();
            PSAssetServiceRestClient assetClient = new PSAssetServiceRestClient(url);
            assetClient.login(uid, pw);
            PSPathServiceRestClient pathClient = new PSPathServiceRestClient(url);
            pathClient.login(uid, pw);
            for (AssetFolder folder : assetDefs)
            {
                for (AssetDef def : folder.getAssetDef())
                {
                    String path = ASSETS_PATH_PREFIX + folder.getPath() + "/" + def.getName();
                    try
                    {
                        PSPathItem item = pathClient.find(path);
                        assetClient.delete(item.getId());
                        //log.info("Deleted asset: " + path);
                        System.out.println("Deleted asset: " + path);
                    }
                    catch (RuntimeException e)
                    { /* ignore - could be smarter and check for existence rather than depend on exception*/}
                }
            }
        }
    }
}
