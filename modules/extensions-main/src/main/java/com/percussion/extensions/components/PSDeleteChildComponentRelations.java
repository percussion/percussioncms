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
