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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.data;

import com.percussion.error.PSExceptionUtils;
import com.percussion.security.xml.PSCatalogResolver;
import com.percussion.server.PSServer;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.StringTokenizer;

/**
 * Class used for caching style sheet in the form of <code>Templates</code>
 * object. If the style sheet file is modified, it's cached with new file.
 **/
public class PSCachedStylesheet
{
   /**
    * Constructor for initializing all variables.
    *
    * @param ssUrl the style sheet url, may not be <code>null</code>.
    *
    * @throws IllegalArgumentException when url is <code>null</code>.
    * @throws IOException when file can not be created from passed in url.
    * @throws TransformerFactoryConfigurationError when new instance of
    * transformer factory can not be created. This happens when the class
    * of a transformation factory specified in the system properties cannot
    * be found or instantiated.
    **/
   public PSCachedStylesheet(URL ssUrl)
      throws IOException, TransformerFactoryConfigurationError
    {
      if(ssUrl == null)
         throw new IllegalArgumentException("Url to cache can not be null");

      // Fix the URL if it is file relative to make it be relative to the
      // root
      if (ssUrl.getProtocol().equals("file") && ssUrl.getFile().startsWith("/") == false)
      {
         String rxFile = PSServer.getRxFile(ssUrl.getFile());
         ssUrl = new URL("file", ssUrl.getHost(), rxFile);
      }
      
      m_ssUrl = ssUrl;
      m_cachedTime = 0;
      m_ssTemplate = null;
      m_listener = new PSTransformErrorListener();

      m_transformFactory =  TransformerFactory.newInstance();

      PSCatalogResolver cr = new PSCatalogResolver();
      cr.setInternalRequestURIResolver(new PSInternalRequestURIResolver());
      m_transformFactory.setURIResolver(cr);

      if("file".equalsIgnoreCase(m_ssUrl.getProtocol()))
         m_ssFile = new File(m_ssUrl.getFile());        
      else
         m_ssFile = null;
   }
   /**
    * Convienience method for {@link #getStylesheetTemplate(String)}
    **/
   public Templates getStylesheetTemplate()
      throws IOException, SAXException, TransformerConfigurationException
   {
      return getStylesheetTemplate(null);
   }
   
   /**
    * Style sheet template is created if it is <code>null</code> or the url is
    * modified and returns style sheet template.
    * 
    * @param encoding the character encoding that will be used for 
    * transformation output. If this parameter is not <code>null</code>
    * then the stylesheet will have it's xsl:output element and meta tags
    * overridden to use the specified encoding. May be <code>null<code>
    * or empty in which case no modifications to the XSl template will be
    * made. 
    *
    * @return style sheet template, may not be <code>null</code>.
    *
    * @throws IOException if it can not get stream from url.
    * @throws SAXException if there is error in building Document from style
    * sheet url.
    * @throws TransformerConfigurationException if templates object can not be
    * created because of parser errors in style sheet.
    */
   public Templates getStylesheetTemplate(String encoding)
      throws IOException, SAXException, TransformerConfigurationException
   {
      encoding = encoding == null ? "" : encoding;
      // we can calculate modified time on files, so see if it changed
      if ( (m_ssTemplate == null) || (!m_lastEncoding.equals(encoding)) ||
           ((m_ssFile != null) && (m_ssFile.lastModified() > m_cachedTime)) )
      {
         synchronized(this)
         {
            if ((m_ssFile != null) && (m_ssFile.lastModified() > m_cachedTime) ||
               (!m_lastEncoding.equals(encoding)))
            {
               m_ssTemplate = null;
               m_cachedTime = m_ssFile.lastModified();
            }
            if (m_ssTemplate == null)
            {
               Logger l = LogManager.getLogger(getClass());
               Document doc = null;
               try
               {
                  //Reason for using DOMSource is creating document using
                  //PSXMLDocumentBuilder is giving better error message than
                  //using StreamSource with inputstream of stylesheet URL.

                  InputStream urlStream = m_ssUrl.openStream();
                  doc = PSXmlDocumentBuilder.createXmlDocument(
                           new InputSource(urlStream),
                           false);
                  urlStream.close();
               if(encoding.trim().length() > 0)
               {
                  // override char encoding
                  m_lastEncoding = encoding;
                  overrideXSLCharacterEncoding(doc, encoding);
               }
                  PSCatalogResolver cr = new PSCatalogResolver();
                  cr.setInternalRequestURIResolver(new PSInternalRequestURIResolver());
                  m_transformFactory.setURIResolver(cr);
                  m_transformFactory.setErrorListener(m_listener);

                  m_ssTemplate =
                     m_transformFactory.newTemplates(
                        new DOMSource(doc, m_ssUrl.toString()));
               }
               catch (SAXException | IOException| TransformerConfigurationException e)
               {
                  l.error("Error loading: {} Error: {}" , m_ssUrl,
                          PSExceptionUtils.getMessageForLog(e));
                  throw e;
               }
            }
         }
      }

      return m_ssTemplate;
   }

   /**
    * Gets error listener set on transformer factory for the stylesheet parsing.
    * This will be called to get errors happened while getting style sheet
    * template.
    *
    * @return error listener, never <code>null</code>.
    **/
   public ErrorListener getErrorListener()
   {
      return m_listener;
   }

   /**
    * Get the status information for this stylesheet.
    *
    * @param doc the document for which to create the status element, not
    *    <code>null</code>.
    * @return the element containing all stylesheet status 
    *    information, never <code>null</code>.
    * @throws IllegalArgumentException if the provided document is 
    *    <code>null</code>.
    */
   public Element getStylesheetStatus(Document doc)
   {
      if (doc == null)
         throw new IllegalArgumentException("the document cannot be null");
      
      Element url = doc.createElement("Url");
      url.appendChild(doc.createTextNode(m_ssUrl.toExternalForm()));
      
      String ssFile = (m_ssFile == null) ? "null" : m_ssFile.toString();
      Element file = doc.createElement("File");
      file.appendChild(doc.createTextNode(ssFile));
      
      Element stylesheet = doc.createElement("Stylesheet");
      stylesheet.setAttribute("cachedTime", new Long(m_cachedTime).toString());
      stylesheet.appendChild(url);
      stylesheet.appendChild(file);
      
      return stylesheet;
   }
   
   /**
    * Overrides or adds the encoding attribute in the xsl:output tag to
    * match the specified character encoding. Also will change the encoding
    * in any existing meta tags with http-equiv = Content-type
    * 
    * @param doc the XML document that represents the XSL stylesheet,
    * assumed not <code>null</code>.
    * @param encoding the character encoding type that will be used, assumed
    * not <code>null</code> or empty.
    */
   private void overrideXSLCharacterEncoding(Document doc, String encoding)
   {
      NodeList nodes = null;
      Element elem = null;
      // Handle xsl:output element
      nodes = doc.getElementsByTagName("xsl:output");
      if(nodes.item(0) != null)
      {
         elem = (Element)nodes.item(0);
         elem.setAttribute("encoding", encoding);
      }
      
      // Handle Content-Type meta tag
      nodes = doc.getElementsByTagName("meta");
      int nodeCount = nodes.getLength();
      for (int i=0; i < nodeCount; i++)
      {
         boolean found = false;
         elem = (Element) nodes.item(i);

         /*
         1st try format:
         <META http-equiv="content-Type" content="text/html; charset=ISO-8859-1">
         */
         String attr = elem.getAttribute("http-equiv");
         if (attr != null && attr.equalsIgnoreCase("content-type"))
         {
            String content = elem.getAttribute("content");
            if (content != null && !content.equals(""))
            {
              
               found = (content.toLowerCase().indexOf("charset=") != -1);
               if(found)
               {
                  StringBuilder sb = new StringBuilder();
                  StringTokenizer st = 
                     new StringTokenizer(
                        elem.getAttribute("content"), "; ", true);
                  String token = null;
                  while(st.hasMoreTokens())
                  {
                     token = st.nextToken();
                     if(token.toLowerCase().startsWith("charset="))
                     {
                        sb.append("charset=");
                        sb.append(encoding);
                     }
                     else
                     {
                        sb.append(token);
                     }
                  
                  }
                  elem.setAttribute("content", sb.toString());
               }               
                
            }
         }
         /*
         2nd try format:
         <META http-equiv="charset" content="ISO-8859-1">
         */
         if (!found && (attr != null && attr.equalsIgnoreCase("charset")))
         {
            found = true;
            elem.setAttribute("content", encoding);
         }
         /*
         3rd try format:
         <META charset="ISO-8859-1">
         */
         if (!found)
         {
            attr = elem.getAttribute("charset");
            if (attr != null && !attr.equals(""))
               elem.setAttribute("charset", encoding);
         }

       
      }
      
   }
   


   /**
    * The style sheet template object, gets initialized in
    * {@link #getStylesheetTemplate()}.
    **/
   private Templates m_ssTemplate;

   /**
    * The style sheet url object, gets initialized in constructor and never
    * <code>null</code> after that.
    **/
   private URL m_ssUrl;
   
   /**
    * The last character encoding override passed in to the
    * <code>getStylesheetTemplate</code> method. Never <code>null</code> may be empty.
    * Intial value is an empty string.
    */
   private String m_lastEncoding = "";

   /**
    * The style sheet file object, gets initialized in constructor.
    * <code>Null</code> unless m_ssUrl uses a file: protocol, then never
    * <code>null</code>.
    **/
   private File m_ssFile;

   /**
    * The style sheet file object, gets initialized in constructor and set with
    * modified time of style sheet file in {@link #getStylesheetTemplate()},
    * used to check for reloading.
    **/
   private long m_cachedTime;

   /**
    * The factory instance to create templates object of style sheet.
    * Initialized in constructor and never be <code>null</code> after that.
    * <code>TransformerFactoryConfigurationError</code> will be thrown
    * if the implementation for the factory is not available or
    * cannot be instantiated.
    */
   private static TransformerFactory m_transformFactory;

   /**
    * The error listener set on transformer factory to listen to errors in the
    * process of creating templates object from style sheet url. Initialized in
    * constructor and never be <code>null</code> after that.
    **/
   private ErrorListener m_listener;
}

