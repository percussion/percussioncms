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

package com.percussion.delivery.metadata;

import com.percussion.delivery.metadata.any23.IPSDocumentSource;
import com.percussion.delivery.metadata.any23.PSReaderDocumentSource;
import com.percussion.delivery.metadata.any23.PSTripleHandler;
import com.percussion.delivery.metadata.extractor.data.PSMetadataEntry;
import com.percussion.delivery.metadata.extractor.data.PSMetadataProperty;
import org.apache.any23.Any23;
import org.apache.any23.ExtractionReport;
import org.apache.any23.configuration.Configuration;
import org.apache.any23.extractor.ExtractionParameters;
import org.apache.any23.extractor.html.JsoupUtils;
import org.apache.any23.mime.NaiveMIMETypeDetector;
import org.apache.any23.mime.TikaMIMETypeDetector;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import static org.apache.commons.lang.StringUtils.*;
import static org.apache.commons.lang.Validate.isTrue;
import static org.apache.commons.lang.Validate.notEmpty;

/**
 * Responsible for extracting the metadata from a given page and returning a
 * PSMetadataEntry with its properties (PSMetadataProperty).
 * 
 * @author miltonpividori
 * 
 */
public class PSMetadataExtractorService implements IPSMetadataExtractorService
{

    /**
     * Logger for this class.
     */
    public static Log log = LogFactory.getLog(PSMetadataExtractorService.class);

    public static final String ALTERNATIVE_PROPERTY_NAME = "dcterms:alternative";

    public static final String TYPE_PROPERTY_NAME = "dterms:type";

    private static final String APPS_SUFFIX = "apps";

    public PSMetadataExtractorService()
    {
       
    }

    /* (non-Javadoc)
     * @see com.percussion.metadata.scanner.impl.IPSMetadataExtractorService#process(java.io.File, java.io.File)
     */
    /*public IPSMetadataEntry process(File tomcatHomeDirectory, File fileToScan)
    {
        log.debug("Extracting metadata from file: " + fileToScan.getPath());

        // Get the pagepath
        String pagePath = PSPagepathUtils.processPath(fileToScan.getAbsolutePath().substring(
                tomcatHomeDirectory.getAbsolutePath().length()));
        
        // Get the site directory
        File siteDirectory = new File(tomcatHomeDirectory, pagePath.split("/")[1]);
        
        // Get folder
        String folder = getEntryFolder(siteDirectory, fileToScan);
        
        // Get site
        String site = getEntrySite(siteDirectory.getName()); 
        
        return runExtraction(new PSFileDocumentSource(fileToScan), pagePath,
                fileToScan.getName(), folder, site);
    }*/
    
    public static class PathSplit {
        private String site;
        private String folder;
        private String pageName;
        
        public PathSplit(String pathToSite, String fullPath) {
            if (pathToSite == null) {
                pathToSite = "/";
            }
            notEmpty(pathToSite);
            notEmpty(fullPath);
            pathToSite = FilenameUtils.separatorsToUnix(pathToSite);
            fullPath = FilenameUtils.separatorsToUnix(fullPath);
            isTrue(startsWith(fullPath, pathToSite), "Path to site should be in full path.");
            if (! endsWith(pathToSite, "/"))
                pathToSite = pathToSite + "/";
            
            pageName = FilenameUtils.getName(fullPath);
            notEmpty(pageName, "filename (page name) is blank");
            String subPath = removeStart(fullPath, pathToSite);
            isTrue(contains(subPath, "/"), "Either site or folder sub folder is missing");
            site = substringBefore(subPath, "/");
            notEmpty(site, "site cannot be resolved");
            folder = "/" + FilenameUtils.getPath(substringAfter(subPath, "/"));
        }
        
        public String getPagePath() {
            return "/" + getSite() + getFolder() + getPageName();
        }
        public String getSite()
        {
            return site;
        }
        public String getFolder()
        {
            return folder;
        }
        public String getPageName()
        {
            return pageName;
        }

        @Override
        public String toString()
        {
            return "PathSplit [site=" + site + ", folder=" + folder + ", pageName=" + pageName + "]";
        }
        
        
    }
    /*
     * (non-Javadoc)
     * @see com.percussion.metadata.extractor.IPSMetadataExtractorService#process(java.io.Reader, java.lang.String)
     */
    public PSMetadataEntry process(Reader reader, String mimeType, String pagePath, Map<String,Object> additional)
    {
        log.debug("Extracting metadata from Reader source");

        PathSplit ps = new PathSplit("/", pagePath);
        PSReaderDocumentSource source = null;
        PSMetadataEntry ret;
        try {
            if (reader == null) {
                //The file isn't going to be handled by the extraction tool so return the additional meta data
                ret = new PSMetadataEntry();
                if (additional != null) {
                    for (String key : additional.keySet()) {
                        ret.addProperty(new PSMetadataProperty(key, additional.get(key).toString()));
                    }
                }
            } else {

                source = new PSReaderDocumentSource(reader, mimeType);
                return runExtraction(source, ps.getPagePath(),
                        ps.getPageName(), ps.getFolder(), ps.getSite(), additional);

            }
            return ret;
        }
        catch (IOException e)
        {
          
            String message = "Error reading from the reader object";
            
            log.error(message, e);
            throw new RuntimeException(message,e);
        }
        finally {
                 if (source!=null)
                    try{source.close();}catch(Exception e){/*Ignore*/}
        }
    }
    
    /**
     * Runs an extraction process. Creates an Any23 parser with the specified
     * IPSDocumentSource and sets to the PSMetadataEntry returned the pagepath,
     * pagename, folder and site specified.
     * 
     * @param documentSource An IPSDocumentSource to use by Any23.
     * @param pagePath The pagepath of the page.
     * @param pageName The name of the page.
     * @param folder The folder of the page.
     * @param site The site of the page.
    * @param additional 
     * @return A PSMetadataEntry object with the page information along with its metadata
     * properties.
     */
   private PSMetadataEntry runExtraction(IPSDocumentSource documentSource, String pagePath, String pageName,
         String folder, String site, Map<String, Object> additional)
   {
      // Setup extractor
      Any23 runner;

      PSTripleHandler handler;
      
      try
      {
         // Create metadata entry
         PSMetadataEntry metadataEntry = new PSMetadataEntry();
         Set<IPSMetadataProperty> propSet = new HashSet<IPSMetadataProperty>();


         if (documentSource != null)
         {
            
            //  Hack to not use default saxon xslt parser.  This sets a transformer factory that can be used
            //  just by the thread in Any23 parser and does not affect the rest of the system. See ThreadLocalProperties class
            
            //System.setProperty("threadlocal.javax.xml.transform.TransformerFactory", "org.apache.xalan.processor.TransformerFactoryImpl");
            // DTS Was falling back to JRE internal transformer.  It seems the xalan version was stripping out the namespace declaration
            // preventing the any23 rdfa stylesheet from processing the meta tags properly.
            System.setProperty("threadlocal.javax.xml.transform.TransformerFactory", "com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl");
            
            runner = new Any23();
            runner.setMIMETypeDetector(new NaiveMIMETypeDetector());
            handler = new PSTripleHandler();

            // Run extraction process. In this point, PSTripleHandler will
            // collects all
            // metadata properties found in the page.
             final ExtractionParameters extractionParameters = ExtractionParameters.newDefault();

             extractionParameters.setFlag("any23.microdata.strict", false);
             extractionParameters.setFlag("any23.extraction.rdfa.programmatic", false);


             ExtractionReport report = runner.extract(extractionParameters,documentSource, handler);


            /** Redo Abstract as any23 is corrupting it. */
            Document doc=null;
            doc = Jsoup.parse(documentSource.openInputStream(), null,"/");

            documentSource.close();

            Element abstractEle = doc.select("div[property=dcterms:abstract]").first();
            String originalAbstract=null;
            if(abstractEle != null) {
                originalAbstract = abstractEle.html();
            }
            metadataEntry.setLinktext(handler.getPageLinktext());
            metadataEntry.setType(handler.getPageType());
            
            if (metadataEntry.getType() == null || metadataEntry.getType() == "") {
               log.warn("The detected type of this item is null or empty.  It is possible that the doctype of the template"
                     + " does not include the required prefix/dcterms."
                     + " The item name is: " + pageName + ". Setting default type to 'page.'");
               metadataEntry.setType("page");
            }
            // Properties
            for (PSMetadataProperty prop : handler.getProperties()) {
                if (null != originalAbstract && prop.getName().equals("dcterms:abstract")) {
                    prop.setValue(originalAbstract);
                }
                propSet.add(prop);
            }
         }

         if (additional != null)
         {
            for (Entry<String, Object> property : additional.entrySet())
            {
               Object value = property.getValue();
               if (value!=null)
               {
                  if (property.getKey().equals("linktext"))
                  {
                     metadataEntry.setLinktext(value.toString());
                  }
                  else if (property.getKey().equals("type"))
                  {
                     metadataEntry.setType(value.toString());
                  }
                  else if (value instanceof Collection)
                  {
                     Collection col = (Collection) value;
                     for (Object item : col)
                     {
                        if (item!=null)
                           propSet.add(new PSMetadataProperty(property.getKey(), item.toString()));
                     }
                  }
                  else if (property.getValue() instanceof Object[])
                  {
                     Object[] col = (Object[])value;
                     for (Object item : col)
                     {
                        if (item!=null)
                           propSet.add(new PSMetadataProperty(property.getKey(), item.toString()));
                     }
                  }
                  else
                  {
                     propSet.add(new PSMetadataProperty(property.getKey(), value
                           .toString()));
                  }
               }
            }
         }
         metadataEntry.setPagepath(pagePath);
         metadataEntry.setName(pageName);

         // Folder
         metadataEntry.setFolder(folder);

         // Site
         metadataEntry.setSite(site);

         metadataEntry.setProperties(propSet);

      
         logMetadataFields(metadataEntry);

         return metadataEntry;
      }
      catch (Exception ex)
      {
         throw new RuntimeException("Error in extracting metadata from file: ", ex);
      }
      finally
      {
         
         // Reset transformer factory to default
         System.getProperties().remove("threadlocal.javax.xml.transform.TransformerFactory");
         
         log.debug("Transformer Should be reset to saxon :"+System.getProperty("javax.xml.transform.TransformerFactory"));
         
         
         if (documentSource != null)
            documentSource.close();

         runner = null;
         handler = null;
      }
   }

    /**
     * Creates the 'folder' value for a metadata entry, according to the site
     * directory and file given. Some special cases are considered (for example,
     * the special folder 'ROOT').
     * 
     * @param siteDirectory The site directory of the file.
     * @param fileToScan The file to extract the folder from.
     * @return A 'folder' value ready to be stored in the metadata entry.
     */
    /*private String getEntryFolder(File siteDirectory, File fileToScan)
    {
        // Remove the site path from the file path
        String folderWithFileName = PSPagepathUtils.processPath(
                fileToScan.getAbsolutePath()
                    .substring(siteDirectory.getAbsolutePath().length())
        );
        
        // Remove the ROOT folder from the folder field value
        if (folderWithFileName.startsWith("/ROOT"))
            folderWithFileName = folderWithFileName.substring(5);
        
        // Remove the file name to get the folder
        String folder = folderWithFileName
                    .substring(0, folderWithFileName.length() - fileToScan.getName().length());
        
        if (StringUtils.isEmpty(folder))
            return "/";
        
        return folder;
    }*/
    
    /**
     * Creates the 'site' value for a metadata entry, according to the site
     * directory. It removes the "apps" suffix from the site directory name.
     * 
     * @param siteDirectory A site directory name to extract the 'site'
     * field value from.
     * @return A 'site' value ready to be stored in the metadata entry.
     */
    private String getEntrySite(String siteDirectory)
    {
        if (!siteDirectory.endsWith(APPS_SUFFIX))
            return siteDirectory;

        return siteDirectory.substring(0, siteDirectory.length() - APPS_SUFFIX.length());
    }

    /**
     * Logs all fields of the given metadata entry, along with its properties.
     * 
     * @param metadataEntry A metadata object to log. Should never be
     *            <code>null</code>.
     */
    private void logMetadataFields(PSMetadataEntry metadataEntry)
    {
        if (!log.isTraceEnabled())
            return;
        
        log.trace("Metadata entry info: " +
                new ToStringBuilder(metadataEntry, ToStringStyle.SHORT_PREFIX_STYLE)
                    .append("pagepath", metadataEntry.getPagepath())
                    .append("site", metadataEntry.getSite())
                    .append("name", metadataEntry.getName())
                    .append("linktext", metadataEntry.getLinktext())
                    .append("type", metadataEntry.getType())
                    .append("folder", metadataEntry.getFolder())
                    .append("properties", metadataEntry.getProperties())
                    .toString());
    }

    /**
     * Checks if a PSMetadataEntry has the minimum required fields present.
     * 
     * @param metadataEntry A PSMetadataEntry to check. Should never be
     *            <code>null</code>.
     * @return 'true' if the metadata entry has the minimum required fields.
     *         'false' otherwise.
     */
    private boolean metadataEntryHasRequiredFields(IPSMetadataEntry metadataEntry)
    {
        return true;
    }
}
