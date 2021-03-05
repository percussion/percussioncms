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
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package com.percussion.extensions.general;

import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSResultDocumentProcessor;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.PSConsole;
import org.w3c.dom.*;

import java.io.File;
import java.util.Iterator;
import java.util.Map;

/**
 * This pre-exit adds all HTML parameters in the request to the specified URLs.
 * The urls are specified as the first children of the root element in the
 * result document. For example, the result document
 * &lt;root&gt;
 *  &lt;url1&gt;/Rhythmyx/sampleApp/samplePage1.htm&lt;/url1&gt;
 *  &lt;url2&gt;/Rhythmyx/sampleApp/samplePage2.htm&lt;/url2&gt;
 * &lt;/root&gt;
 *
 * shall become
 *
 * &lt;root&gt;
 *  &lt;url1&gt;/Rhythmyx/sampleApp/samplePage1.htm?param1=value1&amp;param2=value2&lt;/url1&gt;
 *  &lt;url2&gt;/Rhythmyx/sampleApp/samplePage2.htm?param1=value1&amp;param2=value2&lt;/url2&gt;
 * &lt;/root&gt;
 *
 * if the request came with the HTML parameters param1 and param2. The parameter
 * 'pssessionid' is always skipped.
 *
 * Right now its restricted in the sense that it modifies all children and grand
 * children of the root that have the element name specified. Also, it does not
 * modify the URLs if to be the attributes of an element.
 *
 */
public class PSAddAllParamsToUrl
   implements IPSResultDocumentProcessor
{
   /*
    * Implementation of the method defined by the interface
    */
   public Document processResultDocument(Object[] params,
      IPSRequestContext request, Document resultDoc)
      throws PSExtensionProcessingException
   {
      try
      {
         Map<String,Object> htmlParams = request.getParameters();
         if(htmlParams == null || htmlParams.isEmpty())
            return resultDoc;

         for(int i=0; params != null && i<params.length; i++)
         {
            Object obj = params[i];
            if(obj == null)
               continue;
            String elemName = obj.toString().trim();
            if(elemName.length() < 1)
               continue;

            NodeList nl = resultDoc.getElementsByTagName(elemName);
            if(nl == null || nl.getLength() < 1)
               continue;

            Element elem = (Element)nl.item(0);
            Node node = elem.getFirstChild();
            if(!(node instanceof Text))
               continue;

            Text text = (Text)node;   
            StringBuilder url = new StringBuilder(text.getData());
            Iterator<String> iterator = htmlParams.keySet().iterator();
            String key;
            while(iterator.hasNext())
            {
               key = iterator.next();
               //skip if the parameter happens to be 'pssessionid'
               if(key.equals(HTMLPARAM_PSSESSIONID))
                  continue;
                  
               if(url.indexOf("?") == -1)
                  url.append("?");
               else
                  url.append("&");

               url.append(key).append("=").append(htmlParams.get(key).toString());
            }
            text.setData(url.toString());
         }
      }
      catch(Exception e)
      {
         PSConsole.printMsg("Exit:" + ms_fullExtensionName, e);
      }
      return resultDoc;
   }

   /*
    * Implementation of the method defined by the interface
    */
   public void init(IPSExtensionDef extensionDef, File file)
      throws PSExtensionException
   {
      ms_fullExtensionName = extensionDef.getRef().toString();
   }

   /*
    * Implementation of the method defined by the interface
    */
   public boolean canModifyStyleSheet()
   {
      return false;
   }
   
   /**
    * The fully qualified name of this extension.
    */
   private String ms_fullExtensionName = "";

   /**
    * Name of the html parameter for session id
    */
   private static final String HTMLPARAM_PSSESSIONID = "pssessionid";
}

