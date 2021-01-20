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

package com.percussion.extension;

import com.percussion.design.objectstore.PSExtensionParamDef;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Handles serialization of {@link PSExtensionDef} objects to and from their XML
 * representation.
 */
public class PSExtensionDefFactory implements IPSExtensionDefFactory
{
   /* (non-Javadoc)
    * @see IPSExtensionDefFactory#toXml(Element, IPSExtensionDef)
    */
   public Element toXml(Element root, IPSExtensionDef def)
   {
      return toXml(root, def, false);
   }

   /* (non-Javadoc)
    * @see IPSExtensionDefFactory#toXml(Element, IPSExtensionDef, boolean)
    */
   public Element toXml(Element root, IPSExtensionDef def, 
      boolean excludeMethods)
   {
      if (root == null)
         throw new IllegalArgumentException("root cannot be null");

      if (def == null)
         throw new IllegalArgumentException("def cannot be null");

      Document doc = root.getOwnerDocument();
      Element e = PSXmlDocumentBuilder.addEmptyElement(doc,
         root, "Extension");

      e.setAttribute("name", def.getRef().getExtensionName());
      e.setAttribute("context", def.getRef().getContext());
      e.setAttribute("handler", def.getRef().getHandlerName());
      e.setAttribute("categorystring", def.getRef().getCategory());
      e.setAttribute("deprecated", def.isDeprecated() ? "yes" : "no");
      e.setAttribute("restoreRequestParamsOnError", 
         def.isRestoreRequestParamsOnError() ? "yes" : "no");

      for (Iterator params = def.getInitParameterNames(); params.hasNext();)
      {
         String paramName = params.next().toString();
         String paramValue = def.getInitParameter(paramName);
         Element paramEl = PSXmlDocumentBuilder.addElement(doc,
            e, "initParam", paramValue);

         paramEl.setAttribute("name", paramName);
      }

      for (Iterator URLs = def.getResourceLocations(); URLs.hasNext();)
      {
         URL u = (URL)(URLs.next());

         Element resEl = PSXmlDocumentBuilder.addEmptyElement(doc,
            e, "resource");

         resEl.setAttribute("href", u.toString());
      }

      for (Iterator interfaces = def.getInterfaces(); interfaces.hasNext();)
      {
         String iface = (String) interfaces.next();

         Element resEl = PSXmlDocumentBuilder.addEmptyElement(doc,
            e, "interface");

         resEl.setAttribute("name", iface );
      }

      for (Iterator rParams = def.getRuntimeParameterNames(); 
         rParams.hasNext();)
      {
         IPSExtensionParamDef paramDef =
            def.getRuntimeParameter((String)rParams.next());

         paramDef.toXml(e);
      }

      Iterator resources = def.getSuppliedResources();
      if (resources != null)
      {
         Element resourcesEl = PSXmlDocumentBuilder.addEmptyElement(doc,
            e, "suppliedResources");
         while (resources.hasNext())
         {
            URL u = (URL)resources.next();
            Element resEl = PSXmlDocumentBuilder.addEmptyElement(doc,
               resourcesEl, "suppliedResource");

            resEl.setAttribute("href", u.toExternalForm());
         }
      }
      
      Iterator apps = def.getRequiredApplications();
      if (apps.hasNext())
      {
         Element appsEl = PSXmlDocumentBuilder.addEmptyElement(doc,
            e, "requiredApplications");
         while (apps.hasNext())
         {
            String appName = (String) apps.next();
            Element appEl = PSXmlDocumentBuilder.addEmptyElement(doc,
               appsEl, "requiredApplication");

            appEl.setAttribute("name", appName);
         }
      }
      
      if (!excludeMethods)
      {
         Iterator<PSExtensionMethod> methods = def.getMethods();
         if (methods.hasNext())
         {
            Element methodsElem = PSXmlDocumentBuilder.addEmptyElement(doc, e, 
               METHODS_ELEM);
            while (methods.hasNext())
               methodsElem.appendChild(methods.next().toXML(doc));
         }
      }

      return e;
   }

   /* (non-Javadoc)
    * @see IPSExtensionDefFactory#fromXml(Element)
    */
   @SuppressWarnings("unchecked")
   public IPSExtensionDef fromXml(Element defElement)
      throws PSExtensionException
   {
      if (defElement == null)
         throw new IllegalArgumentException("defElement cannot be null");

      // create a walker so we can iterate over subelements
      final int firstFlag = PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN
         | PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;
      final int nextFlag = PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS
         | PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;

      PSXmlTreeWalker tree = new PSXmlTreeWalker(defElement);
      if (!defElement.getTagName().equals("Extension"))
      {
         // search for first Extension element
         defElement = tree.getNextElement("Extension", firstFlag);
         if (defElement == null)
         {
            // TODO: i18n and code
            throw new PSExtensionException(0, "Extension element not found.");
         }
      }

      // get the name of the extension
      PSExtensionRef ref = null;
      try
      {
         ref = new PSExtensionRef(defElement.getAttribute("categorystring"),
            defElement.getAttribute("handler"), 
            defElement.getAttribute("context"), 
            defElement.getAttribute("name"));
      }
      catch (IllegalArgumentException iae)
      {
         // TODO: i18n and code
         throw new PSExtensionException(0, iae.toString());
      }

      // get the deprecation setting
      boolean isDeprecated = false;
      String strDep = defElement.getAttribute("deprecated");
      if (strDep != null)
         isDeprecated = strDep.trim().equalsIgnoreCase("yes") ? true : false;

      //get the restore on error setting
      boolean isRestoreRequestParamsOnError = false;
      String strRestore = defElement.getAttribute(
         "restoreRequestParamsOnError");
      if (strRestore != null && strRestore.trim().equalsIgnoreCase("yes"))
      {
         isRestoreRequestParamsOnError = true;
      }

      // for this extension: get all resource URLs
      Collection resURLs = new LinkedList();
      try
      {
         // reset so we don't have to worry about order
         tree.setCurrent( defElement );
         Element resEl = tree.getNextElement("resource", firstFlag);
         while (resEl != null)
         {
            String resURL = resEl.getAttribute("href");
            resURLs.add(new URL(resURL));
            resEl = tree.getNextElement("resource", nextFlag);
         }
      }
      catch (MalformedURLException mfue)
      {
         // TODO: i18n and code
         throw new PSExtensionException(0, mfue.toString());
      }

      // for this extension: get all init params
      Properties initParams = new Properties();
      // reset so we don't have to worry about order
      tree.setCurrent( defElement );
      Element paramEl = tree.getNextElement("initParam", firstFlag);
      while (paramEl != null)
      {
         String paramName = paramEl.getAttribute("name");
         if (paramName == null)
         {
            // TODO: i18n and code
            throw new PSExtensionException(0,
               "init param name cannot be null");
         }
         String paramVal  = PSXmlTreeWalker.getElementData( paramEl );
         initParams.setProperty(paramName, paramVal);
         paramEl = tree.getNextElement("initParam", nextFlag);
      }

      // for this extension: get all implemented interfaces
      Collection interfaces = new LinkedList();
      // reset so we don't have to worry about order
      tree.setCurrent( defElement );
      Element interfaceEl = tree.getNextElement("interface", firstFlag);
      while (interfaceEl != null)
      {
         String ifaceName = interfaceEl.getAttribute("name");
         if (ifaceName == null)
         {
            // TODO: i18n and code
            throw new PSExtensionException(0,
               "interface name cannot be null");
         }
         interfaces.add(ifaceName);
         interfaceEl = tree.getNextElement("interface", nextFlag);
      }

      // for this extension: get all required runtime params
      Collection rParams = new LinkedList();

      // reset so we don't have to worry about order
      tree.setCurrent( defElement );
      Element rParamEl =
         tree.getNextElement(PSExtensionParamDef.ms_NodeType, firstFlag);
      while (rParamEl != null)
      {
         try
         {
            IPSExtensionParamDef pDef = new PSExtensionParamDef(
               rParamEl, null, null);

            rParams.add(pDef);

            rParamEl = tree.getNextElement(PSExtensionParamDef.ms_NodeType,
               nextFlag);
         }
         catch (PSUnknownNodeTypeException e)
         {
            // TODO: i18n and code
            throw new PSExtensionException(0, e.toString());
         }
      }

      /* Get all supplied resources.  These are the class and jar files used
       * by the extension.
       */
      ArrayList resources = null;
      try
      {
         // reset so we don't have to worry about order
         tree.setCurrent( defElement );
         Element resourcesEl = tree.getNextElement("suppliedResources", 
            firstFlag);
         if (resourcesEl != null)
         {
            resources = new ArrayList();
            Element resEl = tree.getNextElement("suppliedResource", firstFlag);
            while (resEl != null)
            {
               String resURL = resEl.getAttribute("href");
               resources.add(new URL(resURL));
               resEl = tree.getNextElement("suppliedResource", nextFlag);
            }
         }
      }
      catch (MalformedURLException mfue)
      {
         // TODO: i18n and code
         throw new PSExtensionException(0, mfue.toString());
      }

      /* Get all required apps.  
       */
      List apps = new ArrayList();

      // reset so we don't have to worry about order
      tree.setCurrent( defElement );
      Element appsEl = tree.getNextElement("requiredApplications", 
         firstFlag);
      if (appsEl != null)
      {
         
         Element appEl = tree.getNextElement("requiredApplication", firstFlag);
         while (appEl != null)
         {
            String appName = appEl.getAttribute("name");
            if (appName != null && appName.trim().length() > 0)
               apps.add(appName);
            appEl = tree.getNextElement("requiredApplication", nextFlag);
         }
      }
      
      PSExtensionDef def = new PSExtensionDef(
         ref,
         interfaces.iterator(),
         resURLs.iterator(),
         initParams,
         rParams.iterator(),
         (resources != null) ? resources.iterator() : null,
         isDeprecated,
         isRestoreRequestParamsOnError);
      
      def.setRequiredApplications(apps.iterator()); 

      // reset so we don't have to worry about order
      tree.setCurrent(defElement);
      Element methodsElem = tree.getNextElement(METHODS_ELEM, firstFlag);
      if (methodsElem != null)
      {
         Element methodElem = tree.getNextElement(PSExtensionMethod.XML_NAME, 
            firstFlag);
         while (methodElem != null)
         {
            def.addExtensionMethod(new PSExtensionMethod(methodElem));
            methodElem = tree.getNextElement(PSExtensionMethod.XML_NAME, 
               nextFlag);
         }
      }
      
      return def;
   }
   
   @Override
   public boolean equals(Object b)
   {
      return EqualsBuilder.reflectionEquals(this, b);
   }

   @Override
   public int hashCode()
   {
      return HashCodeBuilder.reflectionHashCode(this);
   }
   
   // constants used for XML serialization
   private static final String METHODS_ELEM = "Methods";
}

