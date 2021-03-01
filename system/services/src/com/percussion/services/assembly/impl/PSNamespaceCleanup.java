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
package com.percussion.services.assembly.impl;

import com.percussion.data.PSStylesheetCleanupFilter;
import com.percussion.security.xml.PSSecureXMLUtils;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.sitemgr.IPSSite;
import com.percussion.services.sitemgr.IPSSiteManager;
import com.percussion.services.sitemgr.PSSiteManagerLocator;
import com.percussion.utils.jsr170.IPSPropertyInterceptor;
import com.percussion.utils.xml.PSSaxCopier;
import com.percussion.utils.xml.PSSaxHelper;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.lang.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * Cleanup namespaces declarations that are not configured for the site (if
 * specified) and the system. This will add all configured namespaces and remove
 * all other namespace declarations.
 * 
 * @author dougrand
 */
public class PSNamespaceCleanup implements IPSPropertyInterceptor
{
   /**
    * Parser factory
    */
   static final SAXParserFactory ms_fact = PSSecureXMLUtils.getSecuredSaxParserFactory(false);

   /**
    * Copier handler that strips namespaces for all but the topmost element. The
    * topmost element has the configured namespaces added, and all others
    * removed.
    * 
    * @author dougrand
    */
   public static class ContentHandler extends PSSaxCopier
   {
      /**
       * A map that connects namespace prefixes with namespace URIs. The default
       * namespace is stored under the empty string key.
       */
      private Map<String, String> mi_namespaces = new HashMap<>();

      /**
       * Track the level in the hierarchy, which allows us to add namespaces to
       * the topmost element
       */
      private int mi_level = 0;

      /**
       * Ctor
       * 
       * @param writer the output writer, assumed never <code>null</code>
       * @param namespaces the namespace map, assumed never <code>null</code>
       */
      public ContentHandler(XMLStreamWriter writer,
            Map<String, String> namespaces) {
         super(writer, new HashMap<>(), true);

         mi_namespaces = namespaces;
      }

      /*
       * (non-Javadoc)
       * 
       * @see com.percussion.utils.xml.PSSaxCopier#startElement(java.lang.String,
       *      java.lang.String, java.lang.String, org.xml.sax.Attributes)
       */
      @Override
      @SuppressWarnings("unused")
      public void startElement(String uri, String localName, String qName,
            Attributes attributes) throws SAXException
      {
         try
         {
            resetCharCount();
            m_writer.writeStartElement(qName);
            Set<String> nsattrs = new HashSet<>();
            for (int i = 0; i < attributes.getLength(); i++)
            {
               String name = attributes.getQName(i);
               // Remove all namespace declarations
               if (name.startsWith("xmlns"))
               {
                  nsattrs.add(name);
                  continue;
               }
               m_writer.writeAttribute(name, attributes.getValue(i));
            }
            if (mi_level == 0)
            {
               for (String key : mi_namespaces.keySet())
               {
                  String nsuri = mi_namespaces.get(key);
                  String prefix;
                  if (!StringUtils.isBlank(key))
                  {
                     prefix = "xmlns:" + key;
                  }
                  else
                  {
                     prefix = "xmlns";
                  }
                  if (nsattrs.contains(prefix))
                  {
                     m_writer.writeAttribute(prefix, nsuri);
                  }
               }
            }
            mi_level++;
         }
         catch (XMLStreamException e)
         {
            throw new SAXException(e);
         }
      }

      /*
       * (non-Javadoc)
       * 
       * @see com.percussion.utils.xml.PSSaxCopier#endElement(java.lang.String,
       *      java.lang.String, java.lang.String)
       */
      @Override
      public void endElement(String uri, String localName, String qName)
            throws SAXException
      {
         super.endElement(uri, localName, qName);
         mi_level--;
      }

   }

   /**
    * A map that connects namespace prefixes with namespace URIs. The default
    * namespace is stored under the empty string key.
    */
   private Map<String, String> m_namespaces = new HashMap<>();

   /**
    * Create a namespace cleanup interceptor. Does nothing if supplied siteid
    * parameter is <code>null</code> or 0. 
    * 
    * @param siteid the site, may be <code>null</code>.
    */
   public PSNamespaceCleanup(Integer siteid) throws PSNotFoundException {
      Set<String> pset = null;
      PSStylesheetCleanupFilter filter = PSStylesheetCleanupFilter
            .getInstance();

      if (siteid != null && siteid.intValue() != 0)
      {
         IPSSiteManager smgr = PSSiteManagerLocator.getSiteManager();
         IPSSite site = smgr.loadUnmodifiableSite(new PSGuid(
               PSTypeEnum.SITE, siteid.longValue()));
         String namespaces = site.getAllowedNamespaces();
         if (!StringUtils.isBlank(namespaces))
         {
            String tokens[] = namespaces.split(",");
            pset = new HashSet<>();
            for (String p : tokens)
            {
               pset.add(p);
            }
         }
      }

      Iterator<String> prefixiter = filter.getPrefixes();
      while (prefixiter.hasNext())
      {
         String prefix = prefixiter.next();
         // Filter out those not part of a site if defined
         if (pset != null && !pset.contains(prefix))
            continue;
         String uri = filter.getNSUri(prefix);
         if (!StringUtils.isBlank(uri))
         {
            m_namespaces.put(prefix, uri);
         }
      }
   }

   public Object translate(Object originalValue)
   {
      if (originalValue instanceof String)
      {
         if (StringUtils.isBlank((String) originalValue))
         {
            return originalValue;
         }
         try
         {
            return PSSaxHelper.parseWithXMLWriter((String) originalValue,
                  ContentHandler.class, m_namespaces);
         }
         catch (Exception e)
         {
            PSTrackAssemblyError
               .addProblem("Problem processing namespace cleanup", e);
            throw new RuntimeException(e);
         }
      }
      else
      {
         return originalValue;
      }
   }

}
