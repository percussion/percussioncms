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
package com.percussion.sitemanage.importer;

import com.percussion.server.IPSHttpErrors;
import com.percussion.share.dao.IPSGenericDao;
import com.percussion.share.service.IPSSystemProperties;
import com.percussion.sitemanage.data.PSPageContent;
import com.percussion.sitemanage.data.PSSiteImportCtx;
import com.percussion.sitemanage.importer.IPSSiteImportLogger.PSLogEntryType;
import com.percussion.sitemanage.importer.IPSSiteImportLogger.PSLogObjectType;
import com.percussion.sitemanage.importer.dao.IPSImportLogDao;
import com.percussion.sitemanage.importer.data.PSImportLogEntry;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Connection;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.List;

import static org.apache.commons.lang.StringUtils.equalsIgnoreCase;
import static org.apache.commons.lang.Validate.notNull;


@Component("siteImporter")
@Lazy
public class PSSiteImporter
{
    public static final String REDIRECTED_FROM_URL = "Redirect the original URL from  '{originalUrl}' to '{newUrl}'";

    private static final Logger log = LogManager.getLogger(PSSiteImporter.class);
    private static final String SITE_IMPORTER = "Site Importer";
    private static final String HTML = "html";
    private static final String HEAD = "head";
    private static final String BODY = "body";

    //FB: 
    private static volatile IPSSystemProperties systemProperties = null;

    /**
     * Gets the page in the given URL, and parses its content into a
     * PSPageContent object.
     * 
     * @param siteImportCtx must not be <code>null</code> and the site url must
     *            not be <code>null</code> either.
     * @return PSPageContent object with all fields filled in from the page
     *         found in the provided URL.
     */
    public static PSPageContent getPageContentFromSite(PSSiteImportCtx siteImportCtx) throws IOException
    {
        notNull(siteImportCtx);
        notNull(siteImportCtx.getSiteUrl());
        notNull(siteImportCtx.getUserAgent());

        URLConnectionProperties properties = null;
        
        try {
            properties = overrideConnectionProperties();
            
            Connection con = buildJsoupConnection(siteImportCtx.getSiteUrl(), true, true, siteImportCtx.getUserAgent());
            Document doc = con.get();
            
            PSPageContent pageContent = createPageContent(doc, siteImportCtx.getLogger());
            pageContent.setPath(siteImportCtx.getSiteUrl());
            return pageContent;
        }
        catch (IOException e) {
            throw e;
        }
        finally {
            restoreConnectionProperties(properties);
        }
    }

    /**
     * Given a JSoup document creates extracts the content appropriately and
     * creates PSPageContent object and returns it.
     * 
     * @param doc assumed not <code>null</code>
     * @param logger {@link IPSSiteImportLogger} to use, assumed
     *            not <code>null</code>.
     * @return PSPageContent object with all fields filled in from the supplied
     *         document.
     */
    public static PSPageContent createPageContent(Document doc, IPSSiteImportLogger logger)
    {
        PSPageContent pageContent = new PSPageContent();
        Element docHead = doc.head();
        Elements titleElems = docHead.select("title");
        String title = "";
        if (!titleElems.isEmpty())
        {
            title = titleElems.get(0).text();
        }

        // Extracts all the style script and link elements to
        Elements addHeadElems = new Elements();
        addHeadElems.addAll(docHead.select("style"));
        addHeadElems.addAll(docHead.select("link"));
        addHeadElems.addAll(docHead.select("script"));

        StringBuilder additionalHeadContent = new StringBuilder();
        for (Element element : addHeadElems)
        {
            additionalHeadContent.append(element.outerHtml());
        }
        
        String bodyContent = getBodyContent(doc, logger);
        
        pageContent.setTitle(title);
        pageContent.setHeadContent(additionalHeadContent.toString());
        pageContent.setBodyContent(bodyContent);
        pageContent.setSourceDocument(doc);
        return pageContent;
    }

    /**
     * Retrieves the body content of the document. If the document has no body
     * content, it tryies to build one using content that is outside header
     * element, but inside html element.
     * 
     * @param doc {@link Document} to get the body from, assumed not
     *            <code>null</code>.
     * @param logger {@link IPSSiteImportLogger} to use, assumed
     *            not <code>null</code>.
     * @return {@link String}, never <code>null</code> but may be empty.
     */
    private static String getBodyContent(Document doc, IPSSiteImportLogger logger)
    {
        Element body = doc.body();

        if (body != null)
        {
            return body.html();
        }

        logger.appendLogMessage(PSLogEntryType.ERROR, SITE_IMPORTER,
                "Cannot find <body> element, the imported template and page will not look the same as the original page.");

        return buildBodyFromDocument(doc).html();
    }

    /**
     * Builds the body element and puts inside it the tags that are inside the
     * html element, and outside the header.
     * 
     * @param doc {@link Document} to get the body from, assumed not
     *            <code>null</code>.
     * @return {@link Element}, never <code>null</code>.
     */
    private static Element buildBodyFromDocument(Document doc)
    {
        addBodyToDocument(doc);

        Element body = doc.body();
        Element html = doc.getElementsByTag(HTML).get(0);

        Elements htmlChildren = html.children();
        for (Element element : htmlChildren)
        {
            if (equalsIgnoreCase(element.nodeName(), HEAD) || equalsIgnoreCase(element.nodeName(), BODY))
            {
                continue;
            }
            body.appendChild(element);
        }
        return body;
    }

    /**
     * Adds the body element to the document, as a child of the html element.
     * 
     * @param doc {@link Document} to add the element to, assumed not
     *            <code>null</code>.
     */
    private static void addBodyToDocument(Document doc)
    {
        // this should add the body if it does not exist
        doc.normalise();

        // check just in case the document could not be normalised
        if (doc.body() == null)
        {
            Element html = doc.getElementsByTag(HTML).get(0);
            html.appendElement("body");
        }
    }

    /**
     * Gets the redirected url for the given site url. It finds out if the given
     * site url gets redirected, in which case it will follow those redirections
     * and return the correct url (the final one). If the url is not redirected,
     * the same site url is returned.
     * 
     * @param siteUrl {@link String} with the site url, must not be
     *            <code>null</code>.
     * @param logger {@link IPSSiteImportLogger} with the logger to use. Must
     *            not be <code>null</code>.
     * * @param userAgent {@link String} with the user agent to set. Must
     *            not be <code>null</code>.
     * @return {@link String} with the final url, never <code>null</code> nor
     *         empty.
     * @throws IOException if an error occurs when trying to reach the url for
     *             the site.
     */
    public static String getRedirectedUrl(String siteUrl, IPSSiteImportLogger logger, String userAgent) throws IOException
    {
        notNull(siteUrl);
        notNull(logger);
        notNull(userAgent);
        
        URLConnectionProperties properties = null;
        
        try {
            properties = overrideConnectionProperties();
            
            // make the first request but don't follow redirects
            Connection conn = buildJsoupConnection(siteUrl, true, false, userAgent);
            conn.get();
            Response response = conn.response();
            
            if (response.statusCode() != IPSHttpErrors.HTTP_MOVED_TEMPORARILY
                    && response.statusCode() != IPSHttpErrors.HTTP_MOVED_PERMANENTLY)
            {
                return siteUrl;
            }
    
            // we need to find the final url and replace the old one
            Connection redirectedConn = buildJsoupConnection(siteUrl, true, true, userAgent);
            redirectedConn.get();
            URL newUrl = redirectedConn.response().url();
            
            // log the redirection
            logger.appendLogMessage(PSLogEntryType.STATUS, SITE_IMPORTER,
                    REDIRECTED_FROM_URL.replace("{originalUrl}", siteUrl).replace("{newUrl}", newUrl.toString()));
            
            return newUrl.toString();
        }
        catch (IOException e) {
            throw e;
        }
        finally {
            restoreConnectionProperties(properties);
        }
    }

    /**
     * Generates a {@link Connection} using the given parameters.
     * 
     * @param url {@link String} with the url to use in the connection.
     * @param ignoreContentType if <code>true</code>, the connection will ignore
     *            content types.
     * @param followRedirects if <code>true</code>, the connection will follow
     *            all redirections.
     * @param userAgent {@link String} the user agent to set in the request,
     *            never<code>null</code> or empty.
     * @return a {@link Connection} object, never <code>null</code>.
     */
    public static Connection buildJsoupConnection(String url, boolean ignoreContentType, boolean followRedirects,
            String userAgent)
    {
        Connection conn = Jsoup.connect(url);
        conn.ignoreContentType(ignoreContentType);
        conn.followRedirects(followRedirects);
        conn.userAgent(userAgent);
        int timeOut = getImportTimeout();
        if (timeOut > 0)
            conn.timeout(timeOut);
        
        return conn;
    }
    
    /**
     * Get the timeout to use for importing pages, files, and assets.
     * 
     * @return The timeout in milliseconds, or -1 if no timeout is set.
     */
    public static int getImportTimeout()
    {
        int timeOut = 30;
        if (systemProperties != null)
        {
            timeOut = NumberUtils.toInt(systemProperties.getProperty(IPSSystemProperties.IMPORT_TIME_OUT), timeOut);
        }
        return (timeOut * 1000);
    }
    
    @Autowired
    public void setSystemProperties(IPSSystemProperties systemProps)
    {
        systemProperties = systemProps;
    }

    /**
     * Saves the log and any error log entries
     * 
     * @param objectId The object id to use, not empty
     * @param logger the logger to use, not <code>null</code>.
     * @param logDao log dao, not <code>null</code>.
     * @param siteId The id of the site being imported into, if <code>null</code> empty, no additional error logging
     * is performed.
     * @param desc The description of the object being imported, not <code>null<code/> or empty.
     */
    public static void saveImportLog(String objectId, IPSSiteImportLogger logger, IPSImportLogDao logDao, String siteId, String desc) throws IPSGenericDao.SaveException {
        Validate.notEmpty(objectId);
        Validate.notNull(logger);
        Validate.notNull(logDao);
        Validate.notNull(desc);
        
        PSImportLogEntry entry = new PSImportLogEntry(objectId, logger.getType().name(), new Date(), logger.getLog());
        logDao.save(entry);
        
        if (!StringUtils.isBlank(siteId))
        {
            List<PSImportLogEntry> errors = logger.getErrors(PSLogObjectType.SITE_ERROR, siteId, desc);
            if (errors != null)
            {
                for (PSImportLogEntry errorLogEntry : errors)
                {
                    logDao.save(errorLogEntry);
                }
            }
        }
    }
    
    /**
     * Hold the current URL connection properties.
     * This is used to restore values after they have been overridden.
     *
     */
    public static class URLConnectionProperties
    {
        private SSLSocketFactory defaultSSLSocketFactory = null;
        private HostnameVerifier defaultHostnameVerifier = null;
        
        public SSLSocketFactory getDefaultSSLSocketFactory()
        {
            return defaultSSLSocketFactory;
        }
        public void setDefaultSSLSocketFactory(SSLSocketFactory defaultSSLSocketFactory)
        {
            this.defaultSSLSocketFactory = defaultSSLSocketFactory;
        }
        public HostnameVerifier getDefaultHostnameVerifier()
        {
            return defaultHostnameVerifier;
        }
        public void setDefaultHostnameVerifier(HostnameVerifier defaultHostnameVerifier)
        {
            this.defaultHostnameVerifier = defaultHostnameVerifier;
        }
    }
    
    /**
     * Override connection properties and install an all-trusting certificate manager.
     * @return The URL connection properties from before, to be used in restoration.
     */
    public static URLConnectionProperties overrideConnectionProperties()
    {
        TrustManager[] trustAllCerts = new TrustManager[] {
            new X509TrustManager() {
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
                public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) { }
                public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) { }
            }
        };

        // Install the all-trusting trust manager
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            
            URLConnectionProperties connectionData = new URLConnectionProperties();
            connectionData.setDefaultSSLSocketFactory(HttpsURLConnection.getDefaultSSLSocketFactory());
            connectionData.setDefaultHostnameVerifier(HttpsURLConnection.getDefaultHostnameVerifier());
            
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier( 
                new HostnameVerifier() {
                    public boolean verify(String urlHostName, SSLSession session) {
                        return true;
                    }
                });
            
            return connectionData;
        }
        catch (Exception e) {
            // We can not recover from this exception
            log.error("Error setting override certificates", e);
            return null;
        }
    }
    
    /**
     * Restore connection properties to their values from before the override.
     * @param properties Properties to restore.
     */
    public static void restoreConnectionProperties(URLConnectionProperties properties)
    {
        if (properties != null) {
            HttpsURLConnection.setDefaultSSLSocketFactory(properties.getDefaultSSLSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(properties.getDefaultHostnameVerifier());
        }
    }
}
