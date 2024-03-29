/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.percussion.webservices.ui.impl;

import com.percussion.cms.IPSConstants;
import com.percussion.cms.objectstore.IPSDbComponent;
import com.percussion.cms.objectstore.PSAction;
import com.percussion.cms.objectstore.PSSearch;
import com.percussion.error.PSExceptionUtils;
import com.percussion.server.PSInternalRequest;
import com.percussion.server.PSRequest;
import com.percussion.server.PSServer;
import com.percussion.util.PSBaseBean;
import com.percussion.util.PSCharSets;
import com.percussion.utils.request.PSRequestInfo;
import com.percussion.webservices.IPSWebserviceErrors;
import com.percussion.webservices.PSErrorException;
import com.percussion.webservices.PSWebserviceErrors;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Common implementations used with the public and private ui webservices.
 */
@PSBaseBean("sys_uiWs")
public class PSUiBaseWs
{

   protected static final Logger log = LogManager.getLogger(IPSConstants.WEBSERVICES_LOG);

   private SessionFactory sessionFactory;

   public SessionFactory getSessionFactory() {
      return sessionFactory;
   }

   @Autowired
   public void setSessionFactory(SessionFactory sessionFactory) {
      this.sessionFactory = sessionFactory;
   }

   /**
    * Finds components by name and/or label.
    * 
    * @param name the component name for which to find the summaries, may 
    *    be <code>null</code> or empty, asterisk wildcards are accepted. If not 
    *    supplied or empty all component summaries will be returned.
    * @param label the component label for which to find the summaries, may 
    *    be <code>null</code> or empty, asterisk wildcards are accepted. If not 
    *    supplied or empty all component summaries will be returned.
    * @param resourcePath the resource path used to lookup the components, 
    *    not <code>null</code> or empty.
    * @param nodeName the XML node name of the component, not 
    *    <code>null</code> or empty.
    * @param objClass the class of the component, not <code>null</code>.
    * @return the search result, never <code>null</code>, may be empty.
    * @throws PSErrorException if search failed due to error.
    */
   protected List<IPSDbComponent> findComponentsByNameLabel(String name,
      String label, String resourcePath, String nodeName, Class objClass)
      throws PSErrorException
   {
      if (StringUtils.isBlank(resourcePath))
         throw new IllegalArgumentException(
            "resourcePath cannot be null or empty");

      if (StringUtils.isBlank(nodeName))
         throw new IllegalArgumentException("nodeName cannot be null or empty");

      if (objClass == null)
         throw new IllegalArgumentException("objClass cannot be null");

      // convert wildcard character, from '*' to '%'
      if (StringUtils.isBlank(name))
         name = "*";
      name = StringUtils.replaceChars(name, '*', '%');

      if (StringUtils.isBlank(label))
         label = "*";
      label = StringUtils.replaceChars(label, '*', '%');

      // preparing for making an internal request 
      PSRequest req = (PSRequest) PSRequestInfo
         .getRequestInfo(PSRequestInfo.KEY_PSREQUEST);

      Map<String, String> params = new HashMap<String, String>();
      params.put("sys_name", name);
      params.put("sys_label", label);
      PSInternalRequest iReq = PSServer.getInternalRequest(resourcePath, req,
         params, false);
      if (iReq == null)
      {
         // this is not possible
         throw new RuntimeException("Failed to create internal request for '"
            + resourcePath + "'");
      }

      try
      {
         Document doc;

         if (resourcePath.equals(FIND_SEARCHES))
         {
            iReq.makeRequest();
            doc = iReq.getResultDoc();
         }
         else
         {
            // make the internal request, has to get the merged result 
            // in case the child objects are processed by XSL
            try(ByteArrayOutputStream os = iReq.getMergedResult()) {
               try(StringReader reader = new StringReader(
                       new String(os.toByteArray(), PSCharSets.rxJavaEnc()))){
                  doc = PSXmlDocumentBuilder.createXmlDocument(reader, false);
               }
            }
         }

         // convert XML to action objects
         Element root = doc.getDocumentElement();
         NodeList nods = root.getElementsByTagName(nodeName);
         int length = nods.getLength();
         List<IPSDbComponent> actionList = new ArrayList<IPSDbComponent>();
         for (int i = 0; i < length; i++)
         {
            Element elem = (Element) nods.item(i);
            actionList.add(createComponent(elem, objClass));
         }
         return actionList;
      }
      catch (Exception e)
      {

         int code = IPSWebserviceErrors.FIND_FAILED;
         PSErrorException error = new PSErrorException(code, PSWebserviceErrors
            .createErrorMessage(code, PSAction.class.getName(), name, label, e
               .getLocalizedMessage()), ExceptionUtils.getFullStackTrace(e));
         throw error;
      }
   }

   /**
    * Gets a list of search or view objects from a list of combined search
    * and view objects.
    * 
    * @param sv a list of combined search and view objects, not 
    *   <code>null</code>.
    * @param isView <code>true</code> if select view objects only; otherwise
    *   select search objects only.
    * @return the selected objects, never <code>null</code>, may be empty.
    */
   protected List<PSSearch> getSearchOrViews(List sv, boolean isView)
   {
      if (sv == null)
         throw new IllegalArgumentException("sv cannot be null");

      List<PSSearch> result = new ArrayList<PSSearch>();
      Iterator svIt = sv.iterator();
      while (svIt.hasNext())
      {
         PSSearch s = (PSSearch) svIt.next();
         if (isView)
         {
            if (s.isView())
               result.add(s);
         }
         else
         {
            if (!s.isView())
               result.add(s);
         }
      }

      return result;
   }

   /**
    * Creates a component from its XML representation.
    * @param source the XML format of the component, not 
    *    <code>null</code>.
    * @param objClass the component class, not <code>null</code>
    * @return the created component, never <code>null</code>.
    */
   protected IPSDbComponent createComponent(Element source, Class objClass)
   {
      if (source == null)
         throw new IllegalArgumentException("source cannot be null");

      if (objClass == null)
         throw new IllegalArgumentException("objClass cannot be null");

      Constructor ctor;
      try
      {
         ctor = objClass.getConstructor(new Class[] { Element.class });
         return (IPSDbComponent)  ctor.newInstance(new Object[] { source });
      }
      catch (Exception e)
      {
         log.error(PSExceptionUtils.getMessageForLog(e));
         throw new RuntimeException("Failed to create object with type '"
            + objClass.getName() + "' from its XML: \n"
            + PSXmlDocumentBuilder.toString(source));
      }
   }

   /**
    * Resource path for finding actions by name and/or label
    */
   protected static final String FIND_ACTIONS = "sys_psxCms/QueryActions.xml";

   /**
    * Resource path for finding display format by name and/or label
    */
   protected static final String FIND_DISPLAY_FORMAT = "sys_DisplayFormats/getDisplayFormats.xml";

   /**
    * Resource path for finding searches by name and/or label
    */
   protected static final String FIND_SEARCHES = "sys_DisplayFormats/getSearches.xml";
}
