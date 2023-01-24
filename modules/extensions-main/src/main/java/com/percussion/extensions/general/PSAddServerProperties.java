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

//java
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSResultDocumentProcessor;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.PSConsole;
import com.percussion.server.PSServer;

import java.io.File;
import java.util.Properties;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This is a generic exit that adds a specfied list of properties from
 * server.properties file as attributes to the root element of the
 * result document. The list of properties required is specified as
 * parameters to the exit.
 * <p>If the supplied parameter does not exist as a property in the
 * server.properties file the resulting value is empty.
 * Parameters with empty names will be ignored.</p>
 */
public class PSAddServerProperties implements
              IPSResultDocumentProcessor
{
   /*
    * Implementation of the method required by the interface IPSExtension.
    */
   public void init(IPSExtensionDef extensionDef, File file)
      throws PSExtensionException
   {
      ms_fullExtensionName = extensionDef.getRef().toString();
   }

   /*
    * Implementation of the method required by the interface
    * IPSResultDocumentProcessor.
    */
   public boolean canModifyStyleSheet()
   {
      return false;
   }

   /*
    * Implementation of the method required by the interface
    * IPSResultDocumentProcessor.
    */
   public Document processResultDocument(Object[] params,
      IPSRequestContext request, Document resDoc)
         throws PSParameterMismatchException,
               PSExtensionProcessingException
   {
      if(params == null || resDoc == null)
         return resDoc;
      Element elem = resDoc.getDocumentElement();
      if(elem == null)
         return resDoc;
      try
      {
         Properties serverProp = PSServer.getServerProps();
         String propname = "";
         String propvalue = "";
         for(int i=0; i<params.length;i++)
         {
            propname = (params[i]!=null)?params[i].toString().trim():"";
            if(propname.length()<1)
               continue;
            propvalue = serverProp.getProperty(propname,"");
            elem.setAttribute(propname,propvalue);
         }
      }
      catch(Throwable t) //should never happen!
      {
         PSConsole.printMsg(ms_fullExtensionName, t);
      }

      return resDoc;
   }

   /**
    * The fully qualified name of this extension.
    */
   static private String ms_fullExtensionName = "";
}
