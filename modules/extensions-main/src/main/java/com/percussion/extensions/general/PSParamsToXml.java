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
package com.percussion.extensions.general;

import com.percussion.extension.IPSResultDocumentProcessor;
import com.percussion.extension.PSDefaultExtension;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.server.IPSRequestContext;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * This exit convertes the an array of specified HTML parameters to an XML
 * format and useful when a request comes with an array of HTML params. Each
 * set of the specified parameters will be added as children of the element
 * name specified by the first parameter. These elements in turn will be the
 * children of the root element of the document For example:
 * <p>
 * If the reqest has an array of sys_content and sys_revision parameters with
 * a size of 2, the parameters to the exit will be
 * <ol>
 * <li>contentitem</li>
 * <li>sys_contentid</li>
 * <li>sys_revision</li>
 * </ol>
 * and the result document generated after the exit will be:
 * &lt;rootElem&gt;  &lt;contentitem&gt;
 * &lt;sys_contentid&gt;301&lt;/sys_contentid&gt;
 * &lt;sys_revision&gt;1&lt;/sys_revision&gt;
 * &lt;/contentitem&gt;  &lt;contentitem&gt;
 * &lt;sys_contentid&gt;309&lt;/sys_contentid&gt;
 * &lt;sys_revision&gt;2&lt;/sys_revision&gt;
 * &lt;/contentitem&gt; &lt;/rootElem&gt;
 * <p>
 * The exit itself is generic in that it takes any number parameters though it
 * is specified as 3 now.
 */
public class PSParamsToXml extends PSDefaultExtension
   implements IPSResultDocumentProcessor
{

   /*
    * Required by the interface. This exit never modifies the stylesheet.
    * @see IPSResultDocumentProcessor#canModifyStyleSheet()
    */
   public boolean canModifyStyleSheet()
   {
      return false;
   }

   /*
    * Required by the interface. This exit never modifies the stylesheet.
    * @see IPSResultDocumentProcessor#canModifyStyleSheet()
    */
   public Document processResultDocument(Object[] params,
         IPSRequestContext request, Document doc)
      throws PSParameterMismatchException, PSExtensionProcessingException
   {
      //Two parameters is the minimum.
      if(params==null || params.length < 2)
      {
         throw new IllegalArgumentException(
            "params must not be null and should be of size greater than 1");
      }
      //Name of the unit element whose children will be the parameters.
      String unitElemName = params[0].toString();
      Map map = request.getParameters();
      String paramName = null;
      Object obj = null;
      List values = null;
      int leastSize = Integer.MAX_VALUE; //Initialize to a large value.
      Map newMap = new HashMap();
      for(int i=1; i<params.length; i++)
      {
         if(params[i] == null)
            continue;
         paramName = params[i].toString();
         obj = map.get(paramName);
         if(obj == null)
            continue;

         values = new ArrayList();

         if(obj instanceof List)
            values.addAll((List)obj);
         else
            values.add(obj.toString());
         newMap.put(paramName, values);

         if(leastSize > values.size())
            leastSize = values.size();
      }

      //Make sure not keep the huge value to be a valid one.
      if(leastSize == Integer.MAX_VALUE)
         leastSize = 0;

      Iterator keys = null;
      String key =null;
      String value = null;
      for(int i=0; i<leastSize; i++)
      {
         Element unitElem = PSXmlDocumentBuilder.addElement(
            doc, doc.getDocumentElement(), unitElemName, null);
         keys = newMap.keySet().iterator();
         while(keys.hasNext())
         {
            key = keys.next().toString();
            value = ((List)newMap.get(key)).get(i).toString();

            PSXmlDocumentBuilder.addElement(doc, unitElem, key, value);
         }
      }
      return doc;
   }
}
