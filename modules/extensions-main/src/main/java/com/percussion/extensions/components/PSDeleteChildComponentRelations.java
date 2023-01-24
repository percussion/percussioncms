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

package com.percussion.extensions.components;

import com.percussion.extension.*;
import com.percussion.server.IPSInternalRequest;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.PSConsole;
import org.w3c.dom.Document;

import java.io.File;
import java.util.Map;

/**
 * This is a special exit for deleting the childcomponentraltions in 
 * RXSYSCOMPONENTRELATIONS table. When a component is deleted from 
 * RXSYSCOMPONENT table its parent and child relations must be deleted 
 * from RXSYSCOMPONENTRELATIONS table. A regualr resource can remove 
 * only one relation (in this case PARENTRELATION), inorder to remove the other 
 * relation this exit makes an internal request to another resource 
 * which deletes the other relation(CHILDRELATION).
 */
public class PSDeleteChildComponentRelations implements
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
      IPSInternalRequest delReq = null;
      Map<String,Object> paramsOrig = request.getParameters();

      try
      {
          request.setParameter("DBActionType","DELETE");
          delReq = request.getInternalRequest(
          "sys_cmpComponents/delchildrelations");
          delReq.makeRequest();
      }
      catch(Exception t)
      {
        PSConsole.printMsg(ms_fullExtensionName, t);
      }
      finally
      {
        if(delReq != null)
        {
          delReq.cleanUp();
        }
      }
      
      request.setParameters(paramsOrig);
       return resDoc;
   }

   /**
    * The fully qualified name of this extension.
    */
   private String ms_fullExtensionName = "";
}
