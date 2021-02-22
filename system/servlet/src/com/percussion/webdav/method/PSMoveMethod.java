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

package com.percussion.webdav.method;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.IPSDbComponent;
import com.percussion.cms.objectstore.PSComponentProcessorProxy;
import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.cms.objectstore.PSDbComponent;
import com.percussion.cms.objectstore.PSFolder;
import com.percussion.cms.objectstore.PSItemField;
import com.percussion.cms.objectstore.PSKey;
import com.percussion.cms.objectstore.PSRelationshipProcessorProxy;
import com.percussion.cms.objectstore.PSTextValue;
import com.percussion.cms.objectstore.client.PSRemoteAgent;
import com.percussion.cms.objectstore.client.PSRemoteException;
import com.percussion.cms.objectstore.ws.PSClientItem;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.error.PSException;
import com.percussion.webdav.PSWebdavServlet;
import com.percussion.webdav.PSWebdavStatus;
import com.percussion.webdav.error.PSWebdavException;
import com.percussion.xml.PSXmlDocumentBuilder;


/**
 * This class implements the MOVE WebDAV method.
 */
public class PSMoveMethod
   extends PSCopyMethod
{

   /**
    * Constructs an instance from the given parameters.
    *
    * @param req   The servlet request, it may not be <code>null</code>.
    * @param resp  The servlet response, it may not be <code>null</code>.
    * @param servlet The webdav servlet, it may not be <code>null</code>.
    */
   public PSMoveMethod(
      HttpServletRequest req,
      HttpServletResponse resp,
      PSWebdavServlet servlet)
   {
      super(req, resp, servlet);
   }

   // Implements PSWebdavMethod.processRequest()
   @Override
   protected void processRequest() throws PSWebdavException, IOException
   {
      if (! preProcessRequest())
         return;
      
      try
      {
         String srcParentPath = PSWebdavUtils.getParentPath(m_sourcePath);
         String tgtParentPath =
            PSWebdavUtils.getParentPath(m_targetPath);
         if (srcParentPath.equalsIgnoreCase(tgtParentPath))
         {
            rename();
         }
         else
         {
            move();
         }
      }
      catch (PSException e)
      {
         throw new PSWebdavException(e);
      }
      
      if (m_targetSummary == null)
         setResponseStatus(PSWebdavStatus.SC_CREATED);
      else
         setResponseStatus(PSWebdavStatus.SC_NO_CONTENT);
   }

   /**
    * Rename the source to the destination
    * 
    * @throws PSException if any other error occurs.
    */
   private void rename() throws PSException, PSWebdavException
   {
      String filename = PSWebdavUtils.getFileName(m_targetPath);
      
      if (m_sourceSummary.isFolder())
      {      
         PSComponentProcessorProxy proxy = new PSComponentProcessorProxy(
            PSComponentProcessorProxy.PROCTYPE_REMOTE, getRemoteRequester());
      
         // retrieve the source folder
         String folderType = PSDbComponent.getComponentType(PSFolder.class);
         PSKey[] keys = {m_sourceSummary.getCurrentLocator()};
         Element[] folderEl = proxy.load(folderType, keys);
         PSFolder srcFolder = new PSFolder(folderEl[0]);
         
         // rename the folder
         srcFolder.setName(filename);
         IPSDbComponent[] comps = {srcFolder};
         proxy.save(comps);
      }
      else
      {
         PSRemoteAgent agent = getRemoteAgent();
         
         boolean hasTransToQuickEdit = false;
         boolean openWithCheckout = false;
         boolean checkinAfterUpdate = false;
         String checkoutUser = m_sourceSummary.getCheckoutUserName();
         // does the item checked out by any one? 
         if (checkoutUser == null || checkoutUser.trim().length() == 0)
         {
            openWithCheckout = true;
            checkinAfterUpdate = true;
         }

         try
         {
            hasTransToQuickEdit = PSWebdavUtils
                  .transitionFromPublicToQuickEdit(m_sourceSummary,
                        getConfig(), getWorkflowInfo(), getRemoteAgent(),
                        getRequest());
         }
         catch (PSRemoteException e)
         { // ignore error in case it has been checked out already
         }
         
         PSClientItem item =
            agent.openItem(m_sourceSummary.getEditLocator(), false, openWithCheckout);
         PSItemField field = item.getFieldByName(SYS_TITLE);
         field.addValue(new PSTextValue(filename));
         
         Document doc = PSXmlDocumentBuilder.createXmlDocument();
         Element itemEl = item.toMinXml(doc, true, false, false, false);
         
         agent.updateItem(itemEl, checkinAfterUpdate);
         
         // for an existing item, transition back to public if needed
         if (hasTransToQuickEdit)
         {
            // reload the summary since the state of the item is changed
            m_sourceSummary = getComponentByPath(m_targetPath);
            
            PSWebdavUtils.transitionFromQuickEditToPublic(m_sourceSummary,
                  getConfig(), getWorkflowInfo(), agent, getRequest(), true);
         }
      }
   }
   
   /**
    * Move the source to the destination.
    * 
    * @throws PSWebdavException if fail to get the source parent.
    * @throws PSCmsException if any other error occurs.
    */
   private void move() throws PSCmsException, PSWebdavException
   {
      PSRelationshipProcessorProxy proxy = getFolderProxy();
      String srcParentPath = PSWebdavUtils.getParentPath(m_sourcePath);
      PSComponentSummary srcParent = getComponentByPathRqd(srcParentPath);
      List<PSLocator> sourceList = new ArrayList<>();
      sourceList.add(m_sourceSummary.getCurrentLocator());
      proxy.move(
            PSRelationshipConfig.TYPE_FOLDER_CONTENT,
         srcParent.getCurrentLocator(),
         sourceList,
         m_targetParent.getCurrentLocator());
   }
}




