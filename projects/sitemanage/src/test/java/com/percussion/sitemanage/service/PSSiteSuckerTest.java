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
package com.percussion.sitemanage.service;

import static com.percussion.share.spring.PSSpringWebApplicationContextUtils.getWebApplicationContext;

import com.percussion.server.PSServer;
import com.percussion.share.service.exception.PSValidationException;
import com.percussion.sitemanage.data.PSSite;
import com.percussion.sitemanage.error.PSSiteImportException;
import com.percussion.sitemanage.service.impl.PSSiteDataService;
import com.percussion.test.PSServletTestCase;
import com.percussion.utils.testing.IntegrationTest;
import com.percussion.webservices.security.PSSecurityWsLocator;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.lang.StringUtils;
import org.junit.experimental.categories.Category;
import org.springframework.mock.web.MockHttpServletRequest;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Tool to create sites from urls from a file at a known location.
 *
 */
@Category(IntegrationTest.class)
public class PSSiteSuckerTest extends PSServletTestCase
{
    
    public void testSiteSucker() throws Exception
    {
        PSSecurityWsLocator.getSecurityWebservice().login(request, response, "Admin", "demo", null,
                null, null);
        
        List<PSSiteImportLogEntry> logEntries = new ArrayList<PSSiteSuckerTest.PSSiteImportLogEntry>();
        List<String> urlsList = null;
        try
        {
            File wsUrlFile = new File(WEBSITE_URLS_FILE);
            if(!wsUrlFile.exists() || !wsUrlFile.isFile())
            {
                //Log it and return...
            }
            urlsList = parseSiteUrls(wsUrlFile);
        }
        catch(Exception e)
        {
            //Log that the file is not in proper format
        }
        
        if(urlsList != null)
        {
            for (String url : urlsList)
            {
                try
                {
                    PSSite site = createSiteFromURL(url);
                    logEntries.add(createLogEntry(site));
                }
                catch(Exception e)
                {
                    logEntries.add(createLogEntry(url, e.getMessage()));
                }
            }
        }
        
        generateLogFile(logEntries);
        
    }
    
    private void generateLogFile(List<PSSiteImportLogEntry> logEntries)
    {           
        try
        {
            //generate the log file from entries
            File outputFileName =  new File(REPORT_HTML_FILE);

            File xsltFile = new File(ENTRIES_TEMPLATE_FILE);
            
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            
            // root elements
            Document doc = docBuilder.newDocument();
            Element rootElement = doc.createElement("entries");
            doc.appendChild(rootElement);
            
            //Create a table and header row here
            for (PSSiteImportLogEntry psSiteImportLogEntry : logEntries)
            {
                // staff elements
                Element entry = doc.createElement("entry");
                rootElement.appendChild(entry);
                
                // siteUrl element
                Element siteUrl = doc.createElement("siteUrl");
                siteUrl.appendChild(doc.createTextNode(psSiteImportLogEntry.siteUrl));
                entry.appendChild(siteUrl);
                
                // importError element
                Element importError = doc.createElement("importError");
                importError.appendChild(doc.createTextNode(psSiteImportLogEntry.importError));
                entry.appendChild(importError);

                // errorlog element
                Element errorLog = doc.createElement("logUrl");
                errorLog.appendChild(doc.createTextNode(psSiteImportLogEntry.logUrl));
                entry.appendChild(errorLog);

                // preview link element
                Element previewLink = doc.createElement("previewPageUrl");
                previewLink.appendChild(doc.createTextNode(psSiteImportLogEntry.previewPageUrl));
                entry.appendChild(previewLink);
                
                // site name element
                Element siteName = doc.createElement("importedSiteName");
                siteName.appendChild(doc.createTextNode(psSiteImportLogEntry.importedSiteName));
                entry.appendChild(siteName);
                
                // remarks element
                Element remarks = doc.createElement("remarks");
                remarks.appendChild(doc.createTextNode(" "));
                entry.appendChild(remarks);
            }
            
            // write the content into xml file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(ENTRIES_XML_FILE));
         
            transformer.transform(source, result);

            // read the XML file to apply the transformation
            File xmlFile = new File(ENTRIES_XML_FILE);
            OutputStream htmlFile = new FileOutputStream(outputFileName);
            
            htmlFile.write(XmlTransform.getTransformedHtml(xmlFile, xsltFile).getBytes());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Loads the supplied file and adds all the urls to the list and returns the list.
     * @param wsUrlFile The file consisting of comma separated list of urls, assumed not <code>null</code>.
     * @return List of urls, Never <code>null</code> may be empty.
     * @throws FileNotFoundException if the file is not found.
     */
    private List<String> parseSiteUrls(File wsUrlFile) throws FileNotFoundException
    {
        List<String> urlList = new ArrayList<String>();
        Scanner scanner = new Scanner(new FileInputStream(wsUrlFile), "UTF-8");
        try 
        {
          while (scanner.hasNextLine())
          {
              String urlLine = scanner.nextLine();
              if(StringUtils.isNotBlank(urlLine))
              {
                  String[] urls = urlLine.split(",");
                  for (String url : urls)
                  {
                      if(StringUtils.isNotBlank(url))
                      {
                          urlList.add(url);
                      }
                  }
              }
          }
        }
        finally
        {
          scanner.close();
        }
        return urlList;
    }
    
    /**
     * Creates a site from the supplied url. Uses the site host name as the name of the site.
     * @param siteUrl Assumed not <code>null</code>.
     * @return The created PSSite object never null, server throws RuntimeException if there is an error creating the site.
     * @throws MalformedURLException if the supplied url is malformed.
     */
    private PSSite createSiteFromURL(String siteUrl) throws MalformedURLException, PSSiteImportException, PSValidationException {
        String siteUrlToImport = siteUrl;
        if (!siteUrl.startsWith("http://") && !siteUrl.startsWith("https://"))
        {
            siteUrlToImport = "http://" + siteUrl;
        }

        URL url = new URL(siteUrlToImport);
        PSSite pssite = new PSSite();
        pssite.setName(url.getHost());
        pssite.setBaseUrl(siteUrlToImport);
        
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("User-Agent", USER_AGENT);
        
        PSSite newSite = getSiteDataService().createSiteFromUrl(request, pssite);
        return newSite;
    }
    
    /**
     * Helper method to create a log entry for a given site.
     * @param site assumed not <code>null</code>.
     * @return PSSiteImportLogEntry never <code>null</code>
     */
    private PSSiteImportLogEntry createLogEntry(PSSite site)
    {
        PSSiteImportLogEntry logEntry = new PSSiteImportLogEntry();
        //Fill the log entry
        logEntry.siteUrl = site.getBaseUrl();
        logEntry.importError = "Yes";
        logEntry.logUrl = getLogUrl(site.getName());
        logEntry.previewPageUrl = getPreviewUrl(site.getName());
        logEntry.importedSiteName = site.getName();
        return logEntry;
    }

    /**
     * Helper method to create a log entry for a given url and error message.
     * @param url Assumed not <code>null</code>.
     * @param errorMsg Assumed not <code>null</code>.
     * @return PSSiteImportLogEntry never <code>null</code>
     */
    private PSSiteImportLogEntry createLogEntry(String url, String errorMsg)
    {
        PSSiteImportLogEntry logEntry = new PSSiteImportLogEntry();
        //Fill the log entry
        logEntry.siteUrl = url;
        logEntry.importError = "No - (see details: " + errorMsg + ")";
        return logEntry;
    }
    
    /**
     * Helper method to create the link URL to preview the imported site.
     * @param siteName the site name. Assumed not <code>null</code>.
     * @return link preview URL <code>null</code>
     */
    private String getPreviewUrl(String siteName)
    {
        String linkPreviewUrl = "http://" + PSServer.getHostName() + ":" + PSServer.getListenerPort() + "/Sites/" + siteName + "/index.html";
        return linkPreviewUrl;
    }
    
    /**
     * Helper method to create the link URL to log file.
     * @param siteName the site name. Assumed not <code>null</code>.
     * @return link to log never <code>null</code>
     */
    private String getLogUrl(String siteName)
    {
        String linkLogUrl = "http://" + PSServer.getHostName() + ":" + PSServer.getListenerPort() + "/Rhythmyx/services/sitemanage/site/importLogViewer?siteName=" + siteName;
        return linkLogUrl;
    }

    /**
     * Helper method to get the PSSiteDataService object
     * @return PSSiteDataService
     */
    private PSSiteDataService getSiteDataService()
    {
        return (PSSiteDataService) getWebApplicationContext().getBean("siteDataService");
    } 
    
    /**
     * Helper data class to hold the log entry details.
     */
    static class PSSiteImportLogEntry
    {
        public String siteUrl;
        public String importError;
        public String logUrl;
        public String previewPageUrl;
        public String importedSiteName;
    }
    
    /**
     * Helper data class to transform the XML file and get the content for the
     * HTML file.
     */
    static class XmlTransform
    {
        public static String getTransformedHtml(File xmlFile, File xsltFile) throws TransformerException
        {
            byte[] xml = getStringFromFile(xmlFile).getBytes();
            byte[] xsl = getStringFromFile(xsltFile).getBytes();
            return getTransformedHtml(xml, xsl);
        }

        public String getTransformedHtml(String xml, String xsl) throws TransformerException
        {
            return getTransformedHtml(xml.getBytes(), xsl.getBytes());
        }

        public static String getTransformedHtml(byte[] xml, byte[] xsl) throws TransformerException
        {
            Source srcXml = new StreamSource(new ByteArrayInputStream(xml));
            Source srcXsl = new StreamSource(new ByteArrayInputStream(xsl));
            StringWriter writer = new StringWriter();
            Result result = new StreamResult(writer);
            TransformerFactory tFactory = TransformerFactory.newInstance();
            Transformer transformer = tFactory.newTransformer(srcXsl);
            transformer.transform(srcXml, result);
            return writer.toString();
        }

        private static String getStringFromFile(File f)
        {
            StringBuilder sb = new StringBuilder(1000);
            try
            {
                Scanner sc = new Scanner(f);
                while (sc.hasNext())
                {
                    sb.append(sc.nextLine());
                }
            }
            catch (FileNotFoundException e)
            {
                e.printStackTrace();
            }
            return sb.toString();
        }
    }
    
    private static String WEBSITE_URLS_FILE = PSServer.getRxDir() + "/SiteImporter/websiteurls.csv"; 
    
    private static String ENTRIES_XML_FILE = PSServer.getRxDir() + "/SiteImporter/logEntries.xml"; 
    
    private static String ENTRIES_TEMPLATE_FILE = PSServer.getRxDir() + "/SiteImporter/logEntries.xsl"; 
    
    private static String REPORT_HTML_FILE = PSServer.getRxDir() + "/SiteImporter/SiteSuckerReport.html"; 
    
    private String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:12.0) Gecko/20100101 Firefox/12.0";
}
