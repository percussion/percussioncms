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
package com.percussion.webdav.objectstore;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * This class contains all configuration information, which includes all the
 * information that is specified in the RxWebdavConfig.xml and the servlet
 * parameters in the web.xml of the WebDAV servlet.
 */
public class PSWebdavConfig
{
   /**
    * Constructs an instance from a given configuration definition.
    * 
    * @param configDef
    *           The configuration definition, which contains all the information
    *           specified in the WebDAV configuration file. It may not be
    *           <code>null</code>.
    */
   public PSWebdavConfig(PSWebdavConfigDef configDef)
   {
      if (configDef == null)
         throw new IllegalArgumentException("config may not be null");

      m_defaultContentType = cloneContentType(configDef.getDefaultContentType());

      Iterator contentTypes = configDef.getContentTypes();
      PSWebdavContentType contentType = null;
      while (contentTypes.hasNext())
      {
         contentType = (PSWebdavContentType) contentTypes.next();

         contentType = cloneContentType(contentType);
         m_idMap.put(new Long(contentType.getId()), contentType);

         Iterator mimeTypes = contentType.getMimeTypes();
         while (mimeTypes.hasNext())
         {
            String mimetype = (String) mimeTypes.next();
            m_mimeTypeMap.put(mimetype, contentType);
         }

      }
      setRootPath(configDef.getRootPath());
      m_configDef = configDef;
   }

   /**
    * Set the root path from the path that is specified (from the configuration
    * file).
    * 
    * @param rootPath
    *           The path from the config file, assume not <code>null</code> or
    *           empty.
    */
   private void setRootPath(String rootPath)
   {
      if (rootPath.endsWith("/"))
      {
         // strip the end '/'
         rootPath = rootPath.substring(0, rootPath.length() - 1);
      }
      if (!rootPath.startsWith("/"))
      {
         // add '/' at the beginning
         rootPath = "/" + rootPath;
      }
      m_rootPath = rootPath;

   }

   /**
    * Get the content type from the specified mime-type.
    * 
    * @param mimetype
    *           The mime-type of the retrieved content type. It may not be
    *           <code>null</code> or empty.
    * 
    * @return the content type that is configured for the specified mime-type;
    *         it may be the default content type if there is no content type for
    *         the specified mime-type. It never <code>null</code>.
    */
   public PSWebdavContentType getContentType(String mimetype)
   {
      if (mimetype == null || mimetype.trim().length() == 0)
         throw new IllegalArgumentException("mimetype may not be null or empty");

      PSWebdavContentType contentType = (PSWebdavContentType) m_mimeTypeMap
            .get(mimetype);

      if (contentType == null)
         contentType = m_defaultContentType;

      return contentType;
   }

   /**
    * Get the normalized root path that is specified in the configuration file.
    * 
    * @return The root path, never <code>null</code> or empty.
    */
   public String getRootPath()
   {
      return m_rootPath;
   }

   /**
    * Get the community id that is defined in the configuration file.
    * 
    * @return The community id.
    */
   public int getCommunityId()
   {
      return m_configDef.getCommunityId();
   }

   /**
    * Get the locale that is specified in the configuration.
    * 
    * @return the specified locale, never <code>null</code> or empty.
    */
   public String getLocale()
   {
      return m_configDef.getLocale();
   }

   /**
    * String of allowable flags to identify a state. default: 'i'
    * 
    * @return String comma separated chars never <code>null</code> or empty
    */
   public String getQEValidTokens()
   {
      return m_configDef.getQEValidTokens();
   }

   /**
    * String of allowable flags to identify a public state. default: 'y'
    * 
    * @return String comma separated chars never <code>null</code> or empty
    */
   public String getPublicValidTokens()
   {
      return m_configDef.getPublicValidTokens();
   }

   /**
    * Indicate the behavior of the DELETE operation (in DELETE, COPY and COPY
    * methods).
    * 
    * @return <code>true</code> if the DELETE operation will purge the target
    *         component(s) (items or folders), the target items cannot be
    *         recovered afterwards; otherwise the DELETE operation will only
    *         remove the folder relationship for the target items can be
    *         recovered afterwards.
    */
   public boolean isDeleteAsPurge()
   {
      return m_configDef.isDeleteAsPurge();
   }

   /**
    * Get the exclude folder properties, which should not be inherited from the
    * parent folder when creating a new folder
    * 
    * @return an iterator over zero or more <code>String</code> objects, never
    *         <code>null</code>, but may by empty.
    */
   public Iterator getExcludeFolderProperties()
   {
      return m_configDef.getExcludeFolderProperties();
   }

   /**
    * Get the content type from the specified (content type) id.
    * 
    * @param id
    *           The id of the content type.
    * 
    * @return The content type with the specified id, it may be
    *         <code>null</code> if there is no such id among the configured
    *         content types.
    */
   public PSWebdavContentType getContentType(long id)
   {
      return (PSWebdavContentType) m_idMap.get(id);
   }

   /**
    * Set the URI of the Rhythmyx servlet, which will be used to get the context
    * of Rhythmyx Servlet.
    * 
    * @param rxServletURI
    *           The uri of the servlet, it may not be <code>null</code> or
    *           empty.
    */
   public void setRxServletURI(String rxServletURI)
   {
      if (rxServletURI == null || rxServletURI.trim().length() == 0)
         throw new IllegalArgumentException(
               "rxServletURI may not be null or empty.");

      m_rxServletURI = rxServletURI;
   }

   /**
    * Get the URI of the Rhythmyx servlet, which will be used to get the context
    * of Rhythmyx Servlet.
    * 
    * @return the uri, never <code>null</code> or empty.
    */
   public String getRxServletURI()
   {
      return m_rxServletURI;
   }

   /**
    * Get the prefix of the url-pattern of Rhythmyx Servlet. For example, it is
    * '/Rhythmyx' when its url-pattern is '/Rhythmyx/*' (and it is in a web app
    * such as rxwebdav); it is empty when its url-pattern is '/*', where the
    * Rhythmyx servlet is in another.
    * 
    * @return the prefix, may be <code>null</code> or empty.
    */
   public String getRxUriPrefix()
   {
      return m_uriPrefix;
   }

   /**
    * Set the prefix, see {@link #getRxUriPrefix()}for more info.
    * 
    * @param uriPrefix
    *           The to be set prefix. It may be <code>null</code> or empty.
    */
   public void setRxUriPrefix(String uriPrefix)
   {
      m_uriPrefix = uriPrefix;
   }

   /**
    * Clone a content type from the specified one and add the default
    * property-field name mappings to the cloned object.
    * 
    * @param contentTypeObj
    *           The to be cloned content type, assume not <code>null</code>.
    * 
    * @return The cloned content type, never <code>null</code>.
    */
   private PSWebdavContentType cloneContentType(Object contentTypeObj)
   {
      if (!(contentTypeObj instanceof PSWebdavContentType))
         throw new IllegalArgumentException(
               "contentTypeObj must be an PSWebdavContentType instance");

      PSWebdavContentType ct = (PSWebdavContentType) contentTypeObj;

      PSWebdavContentType ctCloned = (PSWebdavContentType) ct.clone();
      ctCloned.addDefaultMappings();

      return ctCloned;
   }

   /**
    * The prefix of the url-pattern of Rhythmyx Servlet. See
    * {@link #getRxUriPrefix()}for more info. It may be <code>null</code> or
    * empty.
    */
   private String m_uriPrefix = null;

   /**
    * It maps the mime-type (as key in <code>String</code>) to the content
    * type (as value in <code>PSWebdavContentType</code>). It is initialized
    * by ctor, never <code>null</code>.
    */
   private Map m_mimeTypeMap = new HashMap();

   /**
    * It maps the content-id (as key in <code>Integer</code>) to the content
    * type (as value in <code>PSWebdavContentType</code>). It is initialized
    * by ctor, never <code>null</code>.
    */
   private Map m_idMap = new HashMap();

   /**
    * The default (rx) content type. This is used for all unknown mime-type or
    * the mime-type that does not have a related (rx) content-type. It is
    * initialized by ctor, never <code>null</code> after that.
    */
   private PSWebdavContentType m_defaultContentType;

   /**
    * The config definition from the RxWebdavConfig.xml, initialized by ctor,
    * never <code>null</code> after that.
    */
   private PSWebdavConfigDef m_configDef;

   /**
    * The normalized root path that is specified in the configuration file. It
    * is initialized in the ctor, never <code>null</code> or empty after that
    */
   private String m_rootPath;

   /**
    * The URI of the Rhythmyx servlet, which can be used to get the context of
    * the servlet. Default to "/Rhythmyx".
    */
   private String m_rxServletURI = "/Rhythmyx";

}
