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

