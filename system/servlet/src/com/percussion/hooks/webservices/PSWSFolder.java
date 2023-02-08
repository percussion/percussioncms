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

package com.percussion.hooks.webservices;

import org.apache.soap.Envelope;
import org.apache.soap.SOAPException;
import org.apache.soap.rpc.SOAPContext;

/**
 * This class defines the actions associated with the Folder group
 * of the web services.
 *
 * All methods assume the Envelope and the SOAPContext objects are not
 * <code>null</code>. This is defined by the SOAP 1.1 message router
 * servlet.
 */
public class PSWSFolder extends PSWebServices
{
   public PSWSFolder()
      throws SOAPException
   {}

   /**
    * This operation is used to insert a new folder
    *
    * @param   env  the full envelope of the SOAP message being sent, the
    *               contents of the message contain a
    *               <code>CreateFolderRequest</code> element defined in
    *               the sys_FolderParameters.xsd schema file
    *
    * @param   reqCtx   the context of the message being sent
    *
    * @param   resCtx  a location for the response to be sent back to the
    *                  requestor
    *
    * @throws  SOAPException @see sendToServer for more info
    */
   public void createFolder(Envelope env,
                       SOAPContext reqCtx,
                       SOAPContext resCtx)
      throws SOAPException
   {
      sendToServer("createFolder", env, reqCtx, resCtx);
   }

   /**
    * This operation is used to delete existing folder in the system.
    *
    * @param   env  the full envelope of the SOAP message being sent, the
    *               contents of the message contain a
    *               <code>DeleteFolderRequest</code> element defined in
    *               the sys_FolderParameters.xsd schema file
    *
    * @param   reqCtx   the context of the message being sent
    *
    * @param   resCtx  a location for the response to be sent back to the
    *                   requestor
    *
    * @throws  SOAPException @see sendToServer for more info
    */
   public void deleteFolder(Envelope env,
                          SOAPContext reqCtx,
                          SOAPContext resCtx)
      throws SOAPException
   {
      sendToServer("deleteFolder", env, reqCtx, resCtx);
   }

   /**
    * This operation is used to open existing folder in the system.
    *
    * @param   env  the full envelope of the SOAP message being sent, the
    *               contents of the message contain a
    *               <code>OpenFolderRequest</code> element defined in
    *               the sys_FolderParameters.xsd schema file
    *
    * @param   reqCtx   the context of the message being sent
    *
    * @param   resCtx  a location for the response to be sent back to the
    *                   requestor
    *
    * @throws  SOAPException @see sendToServer for more info
    */
   public void openFolder(Envelope env,
                          SOAPContext reqCtx,
                          SOAPContext resCtx)
      throws SOAPException
   {
      sendToServer("openFolder", env, reqCtx, resCtx);
   }

   /**
    * This operation is used to modify existing folder in the system.
    *
    * @param   env  the full envelope of the SOAP message being sent, the
    *               contents of the message contain a
    *               <code>UpdateFolderRequest</code> element defined in
    *               the sys_FolderParameters.xsd schema file
    *
    * @param   reqCtx   the context of the message being sent
    *
    * @param   resCtx  a location for the response to be sent back to the
    *                  requestor
    *
    * @throws  SOAPException @see sendToServer for more info
    */
   public void updateFolder(Envelope env,
                          SOAPContext reqCtx,
                          SOAPContext resCtx)
      throws SOAPException
   {
      sendToServer("updateFolder", env, reqCtx, resCtx);
   }

   /**
    * This operation is used to add a list of child objects to an existing
    * folder in the system.
    *
    * @param   env  the full envelope of the SOAP message being sent, the
    *               contents of the message contain a
    *               <code>AddFolderChildrenRequest</code> element defined in
    *               the sys_FolderParameters.xsd schema file
    *
    * @param   reqCtx   the context of the message being sent
    *
    * @param   resCtx  a location for the response to be sent back to the
    *                  requestor
    *
    * @throws  SOAPException @see sendToServer for more info
    */
   public void addFolderChildren(Envelope env,
                          SOAPContext reqCtx,
                          SOAPContext resCtx)
      throws SOAPException
   {
      sendToServer("addFolderChildren", env, reqCtx, resCtx);
   }

   /**
    * This operation is used to copy a list of child objects to an existing
    * folder in the system.
    *
    * @param   env  the full envelope of the SOAP message being sent, the
    *               contents of the message contain a
    *               <code>CopyFolderChildrenRequest</code> element defined in
    *               the sys_FolderParameters.xsd schema file
    *
    * @param   reqCtx   the context of the message being sent
    *
    * @param   resCtx  a location for the response to be sent back to the
    *                  requestor
    *
    * @throws  SOAPException @see sendToServer for more info
    */
   public void copyFolderChildren(Envelope env,
                          SOAPContext reqCtx,
                          SOAPContext resCtx)
      throws SOAPException
   {
      sendToServer("copyFolderChildren", env, reqCtx, resCtx);
   }

   /**
    * This operation is used to get a list of child objects from an existing
    * folder in the system.
    *
    * @param   env  the full envelope of the SOAP message being sent, the
    *               contents of the message contain a
    *               <code>GetFolderChildrenRequest</code> element defined in
    *               the sys_FolderParameters.xsd schema file
    *
    * @param   reqCtx   the context of the message being sent
    *
    * @param   resCtx  a location for the response to be sent back to the
    *                  requestor
    *
    * @throws  SOAPException @see sendToServer for more info
    */
   public void getFolderChildren(Envelope env,
                          SOAPContext reqCtx,
                          SOAPContext resCtx)
      throws SOAPException
   {
      sendToServer("getFolderChildren", env, reqCtx, resCtx);
   }

   /**
    * This operation is used to get a parent object from an existing
    * folder in the system.
    *
    * @param   env  the full envelope of the SOAP message being sent, the
    *               contents of the message contain a
    *               <code>GetFolderParentRequest</code> element defined in
    *               the sys_FolderParameters.xsd schema file
    *
    * @param   reqCtx   the context of the message being sent
    *
    * @param   resCtx  a location for the response to be sent back to the
    *                  requestor
    *
    * @throws  SOAPException @see sendToServer for more info
    */
   public void getParentFolder(Envelope env,
                          SOAPContext reqCtx,
                          SOAPContext resCtx)
      throws SOAPException
   {
      sendToServer("getParentFolder", env, reqCtx, resCtx);
   }

   /**
    * This operation is used to move a list of child objects from their current
    * parent folder to a new folder in the system.
    *
    * @param   env  the full envelope of the SOAP message being sent, the
    *               contents of the message contain a
    *               <code>MoveFolderChildrenRequest</code> element defined in
    *               the sys_FolderParameters.xsd schema file
    *
    * @param   reqCtx   the context of the message being sent
    *
    * @param   resCtx  a location for the response to be sent back to the
    *                  requestor
    *
    * @throws  SOAPException @see sendToServer for more info
    */
   public void moveFolderChildren(Envelope env,
                          SOAPContext reqCtx,
                          SOAPContext resCtx)
      throws SOAPException
   {
      sendToServer("moveFolderChildren", env, reqCtx, resCtx);
   }

   /**
    * This operation is used to remove a list of child objects from their
    * parent folder in the system.
    *
    * @param   env  the full envelope of the SOAP message being sent, the
    *               contents of the message contain a
    *               <code>RemoveFolderChildrenRequest</code> element defined in
    *               the sys_FolderParameters.xsd schema file
    *
    * @param   reqCtx   the context of the message being sent
    *
    * @param   resCtx  a location for the response to be sent back to the
    *                  requestor
    *
    * @throws  SOAPException @see sendToServer for more info
    */
   public void removeFolderChildren(Envelope env,
                          SOAPContext reqCtx,
                          SOAPContext resCtx)
      throws SOAPException
   {
      sendToServer("removeFolderChildren", env, reqCtx, resCtx);
   }
}
