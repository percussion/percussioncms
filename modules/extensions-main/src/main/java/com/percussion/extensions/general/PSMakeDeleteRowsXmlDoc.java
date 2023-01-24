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

import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSRequestPreProcessor;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.PSConsole;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

/**
 * This extension builds a content item list for deletion by the Rhythmyx
 * update resource. The DTD for the document is:
 * <p>
 * &lt;!ELEMENT deleterows (row*) &gt;
 * &lt;!ELEMENT row (#PCDATA) &gt;
 * &lt;!ATTLIST row pkey CDATA #IMPLIED &gt;
 * &lt;!ATTLIST row pkey1 CDATA #IMPLIED &gt;
 * &lt;!ATTLIST row pkey2 CDATA #IMPLIED &gt;
 * <p>
 * This exit shall typically placed on an Rx update resource that deletes the
 * rows from one or more backend tables. The XML element pkey must be mapped to
 * the primary key in the backed table(s).
 * <p>
 * This exit supports primary key with 3 columns maximum. However the support
 * for multiple column values for primary key works only if the value
 * of the first parameter is not an instance of
 * <code>java.util.Collection</code>. This implies that if multiple rows are
 * being deleted at a time, then only the first parameter should be used.
 * The second and third parameters are ignored in this case.
 */
public class PSMakeDeleteRowsXmlDoc implements IPSRequestPreProcessor
{
   /*
    * implementation of the method in the interface IPSRequestPreProcessor
    */
   public void init(IPSExtensionDef extensionDef, File file)
      throws PSExtensionException
   {
      ms_fullExtensionName = extensionDef.getRef().toString();
   }

   /*
    * implementation of the method in the interface IPSRequestPreProcessor
    */
   public void preProcessRequest(Object[] params, IPSRequestContext request)
      throws PSExtensionProcessingException
   {
      if (request == null)
         return; //should never happen

      if (params == null || params.length < 1)
      {
         throw new PSExtensionProcessingException(ms_fullExtensionName,
            new Exception("You must supply the name of the html parameter" +
            " that has the key values"));
      }
      String paramKeyName = params[0].toString().trim();
      if (paramKeyName.length() < 1)
      {
         throw new PSExtensionProcessingException(ms_fullExtensionName,
            new Exception("You must supply the name of the html parameter" +
            " that has the key values"));
      }

      Map<String,Object> htmlParams = request.getParameters();
      if (htmlParams == null)
         return;

      try
      {
         Document doc = PSXmlDocumentBuilder.createXmlDocument();
         Element root =
            PSXmlDocumentBuilder.createRoot(doc, "deleterows");

         Object obj = htmlParams.get(paramKeyName);
         Element elem = null;

         if (obj instanceof Collection)
         {
            Collection col = (Collection)obj;
            Iterator it = col.iterator();
            while (it.hasNext())
            {
               Object tempObj = it.next();
               if (tempObj == null)
                  continue;
               String val = tempObj.toString().trim();
               if (val.length() < 1)
                  continue;

               elem = PSXmlDocumentBuilder.addElement(doc, root, "row", null);
               elem.setAttribute("pkey", val);
               elem.setAttribute("pkey1", "");
               elem.setAttribute("pkey2", "");
            }
         }
         else
         {
            String paramKeyName1 = null;
            if ((params.length > 1) &&
               (params[1].toString().trim().length() > 0))
            {
               paramKeyName1 = params[1].toString().trim();
            }

            String paramKeyName2 = null;
            if ((params.length > 2) &&
               (params[2].toString().trim().length() > 0))
            {
               paramKeyName1 = params[2].toString().trim();
            }

            Object obj1 =
               (paramKeyName1 == null) ? null : htmlParams.get(paramKeyName1);
            Object obj2 =
               (paramKeyName2 == null) ? null : htmlParams.get(paramKeyName2);

            String val = (obj == null) ? "" : obj.toString().trim();
            String val1 = (obj1 == null) ? "" : obj1.toString().trim();
            String val2 = (obj2 == null) ? "" : obj2.toString().trim();

            if ((val.length() > 0) || (val1.length() > 0) ||
            (val2.length() > 0))
            {
               elem = PSXmlDocumentBuilder.addElement(doc, root, "row", null);
               elem.setAttribute("pkey", val);
               elem.setAttribute("pkey1", val1);
               elem.setAttribute("pkey2", val2);
            }
         }

         request.setInputDocument(doc);
         htmlParams.put("DBActionType", "DELETE");
      }
      catch(Exception e)
      {
         PSConsole.printMsg("Exit:" + ms_fullExtensionName, e);
      }
   }

   /**
    * The fully qualified name of this extension.
    */
   static private String ms_fullExtensionName = "";
}

