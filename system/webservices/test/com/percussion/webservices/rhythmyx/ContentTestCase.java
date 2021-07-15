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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package com.percussion.webservices.rhythmyx;

import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.cms.objectstore.PSDateValue;
import com.percussion.cms.objectstore.PSItemChildEntry;
import com.percussion.design.objectstore.PSContentEditor;
import com.percussion.design.objectstore.PSContentEditorPipe;
import com.percussion.design.objectstore.PSDisplayMapping;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.services.guidmgr.data.PSDesignGuid;
import com.percussion.services.guidmgr.data.PSLegacyGuid;
import com.percussion.services.legacy.IPSCmsObjectMgr;
import com.percussion.services.legacy.PSCmsObjectMgrLocator;
import com.percussion.util.IOTools;
import com.percussion.util.PSBase64Encoder;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.testing.IntegrationTest;
import com.percussion.webservices.IPSWebserviceErrors;
import com.percussion.webservices.PSContentTestBase;
import com.percussion.webservices.PSTestUtils;
import com.percussion.webservices.PSWebserviceErrors;
import com.percussion.webservices.common.ObjectType;
import com.percussion.webservices.common.PSObjectSummary;
import com.percussion.webservices.common.Reference;
import com.percussion.webservices.content.AddContentRelationsRequest;
import com.percussion.webservices.content.AddFolderChildrenRequest;
import com.percussion.webservices.content.AddFolderRequest;
import com.percussion.webservices.content.AddFolderResponse;
import com.percussion.webservices.content.AddFolderTreeRequest;
import com.percussion.webservices.content.CheckinItemsRequest;
import com.percussion.webservices.content.CheckoutItemsRequest;
import com.percussion.webservices.content.ContentSOAPStub;
import com.percussion.webservices.content.CreateChildEntriesRequest;
import com.percussion.webservices.content.CreateItemsRequest;
import com.percussion.webservices.content.DeleteChildEntriesRequest;
import com.percussion.webservices.content.DeleteFoldersRequest;
import com.percussion.webservices.content.FindChildItemsRequest;
import com.percussion.webservices.content.FindFolderChildrenRequest;
import com.percussion.webservices.content.FindFolderPathRequest;
import com.percussion.webservices.content.FindItemsRequest;
import com.percussion.webservices.content.FindParentItemsRequest;
import com.percussion.webservices.content.FindPathIdsRequest;
import com.percussion.webservices.content.FolderRef;
import com.percussion.webservices.content.GetAssemblyUrlsRequest;
import com.percussion.webservices.content.LoadChildEntriesRequest;
import com.percussion.webservices.content.LoadContentRelationsRequest;
import com.percussion.webservices.content.LoadFoldersRequest;
import com.percussion.webservices.content.LoadItemsRequest;
import com.percussion.webservices.content.LoadKeywordsRequest;
import com.percussion.webservices.content.MoveFolderChildrenRequest;
import com.percussion.webservices.content.NewCopiesRequest;
import com.percussion.webservices.content.NewPromotableVersionsRequest;
import com.percussion.webservices.content.NewTranslationsRequest;
import com.percussion.webservices.content.PSAaRelationship;
import com.percussion.webservices.content.PSAaRelationshipFilter;
import com.percussion.webservices.content.PSAutoTranslation;
import com.percussion.webservices.content.PSChildEntry;
import com.percussion.webservices.content.PSContentType;
import com.percussion.webservices.content.PSContentTypeSummary;
import com.percussion.webservices.content.PSContentTypeSummaryChild;
import com.percussion.webservices.content.PSField;
import com.percussion.webservices.content.PSFieldDataType;
import com.percussion.webservices.content.PSFieldDescription;
import com.percussion.webservices.content.PSFieldDimension;
import com.percussion.webservices.content.PSFieldValue;
import com.percussion.webservices.content.PSFolder;
import com.percussion.webservices.content.PSFolderPropertiesProperty;
import com.percussion.webservices.content.PSItem;
import com.percussion.webservices.content.PSItemChildren;
import com.percussion.webservices.content.PSItemFolders;
import com.percussion.webservices.content.PSItemStatus;
import com.percussion.webservices.content.PSItemSummary;
import com.percussion.webservices.content.PSKeyword;
import com.percussion.webservices.content.PSLocale;
import com.percussion.webservices.content.PSRevision;
import com.percussion.webservices.content.PSRevisions;
import com.percussion.webservices.content.PSSearch;
import com.percussion.webservices.content.PSSearchField;
import com.percussion.webservices.content.PSSearchParams;
import com.percussion.webservices.content.PSSearchParamsFolderFilter;
import com.percussion.webservices.content.PSSearchParamsTitle;
import com.percussion.webservices.content.PSSearchResultField;
import com.percussion.webservices.content.PSSearchResults;
import com.percussion.webservices.content.PSSearchResultsFields;
import com.percussion.webservices.content.ReleaseFromEditRequest;
import com.percussion.webservices.content.RemoveFolderChildrenRequest;
import com.percussion.webservices.content.ReorderChildEntriesRequest;
import com.percussion.webservices.content.ReorderContentRelationsRequest;
import com.percussion.webservices.content.SaveChildEntriesRequest;
import com.percussion.webservices.content.SaveItemsRequest;
import com.percussion.webservices.content.ViewItemsRequest;
import com.percussion.webservices.faults.PSContractViolationFault;
import com.percussion.webservices.faults.PSErrorResultsFault;
import com.percussion.webservices.faults.PSErrorResultsFaultServiceCall;
import com.percussion.webservices.faults.PSErrorsFault;
import com.percussion.webservices.faults.PSErrorsFaultServiceCall;
import com.percussion.webservices.faults.PSInvalidSessionFault;
import com.percussion.webservices.faults.PSNotAuthorizedFault;
import com.percussion.webservices.faults.PSUnknownContentTypeFault;
import com.percussion.webservices.security.data.PSCommunity;
import com.percussion.webservices.security.data.PSLogin;
import com.percussion.webservices.system.PSAclImpl;
import com.percussion.webservices.system.PSAudit;
import com.percussion.webservices.system.PSAuditTrail;
import com.percussion.webservices.system.PSRelationship;
import com.percussion.webservices.system.PSRelationshipFilter;
import com.percussion.webservices.system.PSRelationshipFilterCategory;
import com.percussion.webservices.system.PSWorkflow;
import com.percussion.webservices.system.SwitchCommunityRequest;
import com.percussion.webservices.system.SystemSOAPStub;

import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.xml.soap.SOAPException;

import junit.framework.AssertionFailedError;

import org.apache.axis.attachments.AttachmentPart;
import org.apache.commons.beanutils.ConversionException;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static org.junit.Assert.*;

/** 
 * Test case for all public content services
 */
@Category(IntegrationTest.class)
public class ContentTestCase extends PSContentTestBase
{
   public enum BinaryFiles 
   { 
      SKIP, LARGE, SMALL
   }

   /**
    * Construct the default content test case.
    */
   public ContentTestCase()
   {}      

   public ContentTestCase(String name)
   {
      super(name);
   }

   /**
    * Construct the content test case for the supplied parameters.
    *
    * @param session the session to use, not <code>null</code> or empty.
    * @param login the login to use, not <code>null</code>.
    */
   public ContentTestCase(String session, PSLogin login)
   {
      if (StringUtils.isBlank(session))
         throw new IllegalArgumentException("session cannot be null or empty");

      if (login == null)
         throw new IllegalArgumentException("login cannot be null");

      m_session = session;
      m_login = login;
   }

   /**
    * Moves the specified item to the public state if it is in Quick-Edit state.
    * Assumes the given item is either in public or quick-edit state.
    *
    * @param binding the binding object, assumed not <code>null</code>.
    * @param contentId the id of the specified item.
    *
    * @throws Exception if any error occurs.
    */
   private void moveItemInPublicState(ContentSOAPStub binding, int contentId)
   throws Exception
   {
      PSItemStatus item = checkoutItem(binding, contentId);
      if (item.isDidCheckout() == false &&
            item.isDidTransition() == false)
      {
         item.setDidCheckout(true);
         item.setDidTransition(true);
         item.setFromState(new Reference(5, "Public"));
         item.setToState(new Reference(6, "Quick Edit"));
      }

      checkinItem(binding, item);
   }

   private PSItemStatus checkoutItem(ContentSOAPStub binding, int contentId)
      throws Exception
   {
      long[] guidId = new long[] { getLegacyGuid(contentId) };
      return binding.prepareForEdit(guidId)[0];
   }

   private void checkinItem(ContentSOAPStub binding, PSItemStatus item)
      throws Exception
   {
      ReleaseFromEditRequest req = new ReleaseFromEditRequest();
      req.setPSItemStatus(new PSItemStatus[] { item });
      binding.releaseFromEdit(req);
   }

   /**
    * Testing prepareForEdit and releaseFromEdit methods
    *
    * @throws Exception if error occurs.
    */
   @Test
   public void fix_testItemStatus() throws Exception
   {
      ContentSOAPStub binding = getBinding(null);
      PSTestUtils.setSessionHeader(binding, m_session);

      // Test operation
      try
      {
         // prepare for the test data
         switchToIECommunity();
         moveItemInPublicState(binding, 489);
         moveItemInPublicState(binding, 490);

         final String PUBLIC = "Public"; // public state name
         final String QUICK_EDIT = "Quick Edit"; // quick edit state name

         long[] ids = new long[] { getLegacyGuid(489), getLegacyGuid(490) };

         // try to load folders with inlvalid ids
         PSItemStatus[] statuses = new PSItemStatus[2];
         try
         {
            long[] invalidIds = new long[ids.length];
            for (int i=0; i<ids.length; i++)
               invalidIds[i] = ids[i];
            invalidIds[ids.length-1] = invalidIds[ids.length-1] + 10000;

            binding.prepareForEdit(invalidIds);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSErrorResultsFault e)
         {
            verifyErrorResultsFault(e, ids.length-1,
               PSItemStatus.class.getName());
            statuses[0] = e.getServiceCall(0).getResult().getPSItemStatus();
         }

         // transition from public to quick-edit and checkout
         statuses[1] = binding.prepareForEdit(
            new long[] { getLegacyGuid(490) })[0];
         assertTrue(statuses.length == 2);
         PSItemStatus status = statuses[0];
         assertTrue(status.isDidCheckout());
         assertTrue(status.isDidTransition());
         assertTrue(status.getFromState().getName().equals(PUBLIC));
         assertTrue(status.getToState().getName().equals(QUICK_EDIT));
         status = statuses[1];
         assertTrue(status.isDidCheckout());
         assertTrue(status.isDidTransition());
         assertTrue(status.getFromState().getName().equals(PUBLIC));
         assertTrue(status.getToState().getName().equals(QUICK_EDIT));

         // checkin and transition from quick-edit to public
         ReleaseFromEditRequest req = new ReleaseFromEditRequest();
         req.setPSItemStatus(statuses);
         binding.releaseFromEdit(req);

         // transition from public to quick-edit and checkout
         statuses = binding.prepareForEdit(ids);

         // check in, but skip transition, stay in quick-edit
         req.setPSItemStatus(statuses);
         req.setCheckInOnly(true);
         binding.releaseFromEdit(req);

         // check out, then it should still be in quick-edit state
         statuses = binding.prepareForEdit(ids);
         status = statuses[0];
         assertTrue(status.isDidCheckout());
         assertTrue(status.isDidTransition() == false);
         assertTrue(status.getFromState() == null);
         assertTrue(status.getToState() == null);
         status = statuses[1];
         assertTrue(status.isDidCheckout());
         assertTrue(status.isDidTransition() == false);
         assertTrue(status.getFromState() == null);
         assertTrue(status.getToState() == null);

         // don't checkin, but transition from quick-edit to public state
         status = statuses[0];
         status.setDidTransition(true);
         status.setDidCheckout(false);
         status.setFromState(new Reference(5L, PUBLIC));
         status.setToState(new Reference(6L, QUICK_EDIT));
         status = statuses[1];
         status.setDidTransition(true);
         status.setDidCheckout(false);
         status.setFromState(new Reference(5L, PUBLIC));
         status.setToState(new Reference(6L, QUICK_EDIT));
         req.setPSItemStatus(statuses);
         req.setCheckInOnly(false);
         binding.releaseFromEdit(req);
      }
      catch (PSInvalidSessionFault e1)
      {
         throw new junit.framework.AssertionFailedError(
            "InvalidSessionFault Exception caught: " + e1);
      }
      catch (PSNotAuthorizedFault e)
      {
         throw new junit.framework.AssertionFailedError(
            "NotAuthorizedFault Exception caught: " + e);
      }
      catch (RemoteException e)
      {
         throw new junit.framework.AssertionFailedError(
            "RemoteException Exception caught: " + e);
      }
   }

   /**
    * Testing Content Folder methods
    *
    * @throws Exception if error occurs.
    */
   @Test
   public void fix_testFolder() throws Exception
   {
      ContentSOAPStub binding = getBinding(null);
      PSTestUtils.setSessionHeader(binding, m_session);

      // Test operation
      try
      {
         switchToIECommunity();

         LoadFoldersRequest lreq = new LoadFoldersRequest();
         // folders do not have display format id property
         long id2 = getLegacyGuid(2);
         long id3 = getLegacyGuid(3);
         // folders do have display format id property
         long id301 = getLegacyGuid(301);
         long id302 = getLegacyGuid(302);
         long[] ids = new long[] { id2, id3, id301, id302 };
         String[] paths = new String[]
         {
            "//Sites",
            "//Folders",
            "//Sites/EnterpriseInvestments",
            "//Sites/EnterpriseInvestments/Files"
         };
         lreq.setId(ids);
         PSFolder[] folders = binding.loadFolders(lreq);

         // verify the result
         assertTrue(folders.length == 4);
         assertTrue(folders[0].getPath().equals(paths[0]));
         assertTrue(folders[0].getDisplayFormat().getName().equals("Default"));
         assertTrue(folders[0].getSecurity()[0].isPermissionRread());
         assertTrue(folders[1].getPath().equals(paths[1]));
         assertTrue(folders[1].getDisplayFormat().getName().equals("Default"));
         assertTrue(folders[1].getSecurity()[0].isPermissionRread());

         assertTrue(folders[2].getPath().equals(paths[2]));
         assertTrue(folders[2].getDisplayFormat().getName().equals("Default"));
         assertTrue(folders[2].getSecurity()[0].getName().equals("EI_Members"));
         assertTrue(folders[2].getSecurity()[0].isPermissionAdmin() == false);
         assertTrue(folders[2].getSecurity()[0].isPermissionRread());
         assertTrue(folders[2].getSecurity()[0].isPermissionWrite());

         assertTrue(folders[3].getPath().equals(paths[3]));
         assertTrue(folders[3].getDisplayFormat().getName().equals("Default"));
         assertTrue(folders[3].getSecurity()[0].getName().equals("EI_Members"));
         assertTrue(folders[3].getSecurity()[0].isPermissionAdmin() == false);
         assertTrue(folders[3].getSecurity()[0].isPermissionRread());
         assertTrue(folders[3].getSecurity()[0].isPermissionWrite());

         // try to load folders with inlvalid ids
         try
         {
            long[] invalidIds = new long[ids.length];
            for (int i=0; i<ids.length; i++)
               invalidIds[i] = ids[i];
            invalidIds[ids.length-1] = invalidIds[ids.length-1] + 10000;

            lreq = new LoadFoldersRequest();
            lreq.setId(invalidIds);
            binding.loadFolders(lreq);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSErrorResultsFault e)
         {
            verifyErrorResultsFault(e, ids.length-1, PSFolder.class.getName());
         }

         // try to load folders with inlvalid paths
         try
         {
            String[] invalidPaths = new String[paths.length];
            for (int i=0; i<paths.length; i++)
               invalidPaths[i] = paths[i];
            invalidPaths[paths.length-1] = invalidPaths[paths.length-1] + "foo";

            lreq = new LoadFoldersRequest();
            lreq.setPath(invalidPaths);
            binding.loadFolders(lreq);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSErrorResultsFault e)
         {
            verifyErrorResultsFault(e, paths.length-1, PSFolder.class.getName());
         }

         // save existing folder
         ids = binding.saveFolders(new PSFolder[] { folders[3] }).getIds();
         assertTrue(ids[0] == folders[3].getId());

         // create a new folder
         AddFolderRequest areq = new AddFolderRequest();
         areq.setPath("//Sites/EnterpriseInvestments/Files");
         areq.setName("testContentSQAPFolder"
            + java.lang.System.currentTimeMillis());
         AddFolderResponse aresp = binding.addFolder(areq);
         PSFolder folder = aresp.getPSFolder();

         // validate the folder property
         long displayId_0 = new PSDesignGuid(PSTypeEnum.DISPLAY_FORMAT, 0)
            .getValue();
         assertTrue(folder.getDisplayFormat().getId() == displayId_0);
         assertTrue(folder.getDisplayFormat().getName().equals("Default"));

         // save modified display format
         long displayId_1 = new PSDesignGuid(PSTypeEnum.DISPLAY_FORMAT, 1)
            .getValue();
         folder.setDisplayFormat(new Reference(displayId_1, "Simple"));
         PSFolderPropertiesProperty[] props = folder.getProperties();
         PSFolderPropertiesProperty[] propsNew;
         int propLength = (props == null) ? 1 : props.length + 1;
         propsNew = new PSFolderPropertiesProperty[propLength];
         PSFolderPropertiesProperty prop = new PSFolderPropertiesProperty(
            "AdamProperty", "PropValue", "Description");
         propsNew[propLength - 1] = prop;
         folder.setProperties(propsNew);
         long[] savedFolderIds = binding.saveFolders(new PSFolder[] { folder })
               .getIds();

         // load the saved folder
         lreq = new LoadFoldersRequest();
         lreq.setId(savedFolderIds);
         folders = binding.loadFolders(lreq);
         assertTrue(folders.length == 1);
         folder = folders[0];
         assertTrue(folder.getDisplayFormat().getId() == displayId_1);
         assertTrue(folder.getDisplayFormat().getName().equals("Simple"));
         // validate the saved property
         assertTrue(folder.getProperties().length == propLength);
         prop = folder.getProperties()[propLength - 1];
         assertTrue(prop.getName().equals("AdamProperty"));
         assertTrue(prop.getValue().equals("PropValue"));
         assertTrue(prop.getDescription().equals("Description"));

         // save folder tree
         AddFolderTreeRequest atreq = new AddFolderTreeRequest();
         atreq.setPath(folder.getPath());
         // add folder tree from existing folder path, no folder created
         folders = binding.addFolderTree(atreq);
         assertTrue(folders.length == 0);

         // add 2 new child folders
         atreq.setPath(folder.getPath() + "/chidFolder1/ChildFolder2");
         folders = binding.addFolderTree(atreq);
         assertTrue(folders.length == 2);

         // find folder ids from folder path
         FindPathIdsRequest ffiReq = new FindPathIdsRequest();
         ffiReq.setPath(folders[1].getPath());
         long[] idPath = binding.findPathIds(ffiReq).getIds();
         assertTrue(idPath.length == 6);
         // validate the last id
         PSLegacyGuid srcId = new PSLegacyGuid(folders[1].getId());
         PSLegacyGuid tgtId = new PSLegacyGuid(idPath[5]);
         assertTrue(srcId.getContentId() == tgtId.getContentId());
         // validate the 2nd to the last id
         srcId = new PSLegacyGuid(folders[0].getId());
         tgtId = new PSLegacyGuid(idPath[4]);
         assertTrue(srcId.getContentId() == tgtId.getContentId());

         // find folder & item ids from a known path
         ffiReq.setPath("//Sites/EnterpriseInvestments/EI Home Page");
         idPath = binding.findPathIds(ffiReq).getIds();
         assertTrue(idPath.length == 3);

         // find folder path from content id
         FindFolderPathRequest ffpReq = new FindFolderPathRequest();
         ffpReq.setId(folders[1].getId());
         paths = binding.findFolderPath(ffpReq).getPaths();
         // validate the PARENT folder path
         assertTrue(paths.length == 1);
         assertTrue(paths[0].equals(folders[0].getPath()));

         // find folder children

         // find folder children by id
         PSItemSummary[] children = findFolderChildren(binding, folders[0]
            .getId(), null, true);
         assertTrue(children.length == 1);
         assertTrue(children[0].getObjectType().getValue().equals(
            ObjectType._folder));
         assertTrue(children[0].getContentType() == null);
         assertTrue(children[0].getOperation().length == 2); // read & write

         // find folder children by path, but don't load allowed operations
         children = findFolderChildren(binding, null, folders[0].getPath(),
            false);
         assertTrue(children.length == 1);
         assertTrue(children[0].getOperation() == null);

         long parentFolderId = folders[1].getId();
         String parentFolderPath = folders[1].getPath();

         // find folder children by id
         children = findFolderChildren(binding, parentFolderId, null, false);
         assertTrue(children.length == 0);
         // find folder children by path
         children = findFolderChildren(binding, null, parentFolderPath, false);
         assertTrue(children.length == 0);

         // add folder children
         AddFolderChildrenRequest acreq = new AddFolderChildrenRequest();
         acreq.setParent(new FolderRef(parentFolderId, null));
         long childId_489 = getLegacyGuid(489);
         long childId_490 = getLegacyGuid(490);
         acreq.setChildIds(new long[] { childId_489, childId_490 });
         binding.addFolderChildren(acreq);

         // create test parent and child folders
         areq = new AddFolderRequest();
         areq.setPath("//Folders");
         areq.setName("parent_1");
         aresp = binding.addFolder(areq);
         PSFolder parent_1 = aresp.getPSFolder();
         areq.setName("parent_2");
         aresp = binding.addFolder(areq);
         PSFolder parent_2 = aresp.getPSFolder();
         areq.setPath("//Folders/parent_1");
         areq.setName("child_1");
         aresp = binding.addFolder(areq);
         PSFolder child_1 = aresp.getPSFolder();

         // try to add same folder to multiple parents
         acreq = new AddFolderChildrenRequest();
         acreq.setParent(new FolderRef(parent_2.getId(), null));
         acreq.setChildIds(new long[] { child_1.getId() });
         binding.addFolderChildren(acreq);

         // verify that a new folder was created instead
         lreq = new LoadFoldersRequest();
         lreq.setId(new long[] { parent_2.getId() });
         PSFolder[] parent_2Children = binding.loadFolders(lreq);
         assertTrue(parent_2Children.length == 1);
         //FB: RC_REF_COMPARISON NC 1-17-16
         assertTrue(!parent_2Children[0].getId().equals(child_1.getId()));

         // delete test parent and child folders
         DeleteFoldersRequest dreq = new DeleteFoldersRequest();
         dreq.setId(new long[] { parent_1.getId(), parent_2.getId() });
         binding.deleteFolders(dreq);

         // validate the inserted child item
         children = findFolderChildren(binding, parentFolderId, null, true);
         assertTrue(children.length == 2);
         for (PSItemSummary child : children)
         {
            assertTrue(child.getObjectType().getValue()
               .equals(ObjectType._item));
            assertTrue(child.getContentType().getName().equals("rffFile"));
            assertTrue(child.getOperation().length == 3); // read/write,transition,checkin/out
         }

         // remove all folder children by parent id
         RemoveFolderChildrenRequest rfcreq = new RemoveFolderChildrenRequest();
         rfcreq.setParent(new FolderRef(parentFolderId, null));
         binding.removeFolderChildren(rfcreq);
         // validate above removal
         children = findFolderChildren(binding, parentFolderId, null, false);
         assertTrue(children.length == 0);

         // add folder 2 children again, prepairing the tests below
         acreq = new AddFolderChildrenRequest();
         acreq.setParent(new FolderRef(parentFolderId, null));
         acreq.setChildIds(new long[] { childId_489, childId_490 });
         binding.addFolderChildren(acreq);

         // testing remove folder children by parent Id
         rfcreq = new RemoveFolderChildrenRequest();
         rfcreq.setParent(new FolderRef(parentFolderId, null));
         rfcreq.setChildIds(new long[] { childId_489 });
         binding.removeFolderChildren(rfcreq);
         // validate the above remove folder children by parent id
         children = findFolderChildren(binding, parentFolderId, null, false);
         assertTrue(children.length == 1);
         assertTrue(children[0].getId() == childId_490);

         // testing remove folder children by parent path
         rfcreq = new RemoveFolderChildrenRequest();
         rfcreq.setParent(new FolderRef(null, parentFolderPath));
         rfcreq.setChildIds(new long[] { childId_490 });
         binding.removeFolderChildren(rfcreq);
         // validate the above remove folder children by parent path
         children = findFolderChildren(binding, null, parentFolderPath, false);
         assertTrue(children.length == 0);

         // delete the saved folder
         dreq = new DeleteFoldersRequest();
         dreq.setId(savedFolderIds);
         binding.deleteFolders(dreq);
      }
      catch (PSInvalidSessionFault e1)
      {
         throw new junit.framework.AssertionFailedError(
            "InvalidSessionFault Exception caught: " + e1);
      }
      catch (PSNotAuthorizedFault e)
      {
         throw new junit.framework.AssertionFailedError(
            "NotAuthorizedFault Exception caught: " + e);
      }
      catch (RemoteException e)
      {
         throw new junit.framework.AssertionFailedError(
            "RemoteException Exception caught: " + e);
      }

   }

   /**
    * Tests AddFolderTree operation.
    *
    * @throws Exception if any error occurs.
    */
   @Test
   public void testAddFolderTree() throws Exception
   {
      ContentSOAPStub binding = getBinding(null);
      PSTestUtils.setSessionHeader(binding, m_session);

      // create a test folder
      AddFolderTreeRequest atreq = new AddFolderTreeRequest();
      String path = "//Folders/TestFolder_1_"
            + java.lang.System.currentTimeMillis() + "/TestFolder_2";
      atreq.setPath(path);
      PSFolder[] folders = binding.addFolderTree(atreq);
      assertTrue(folders.length == 2);

      // delete a good & a bad folder ids
      DeleteFoldersRequest dreq = new DeleteFoldersRequest();
      PSLegacyGuid fakeId = new PSLegacyGuid(9999, 1);
      PSDesignGuid dsGuid = new PSDesignGuid(fakeId);
      dreq.setId(new long[]{folders[1].getId(), dsGuid.getValue()});
      try
      {
         binding.deleteFolders(dreq);
      }
      catch (PSErrorsFault e)
      {
         PSErrorsFaultServiceCall[] calls = e.getServiceCall();
         assertTrue(calls.length == 2);
         assertTrue(calls[0].getSuccess() != null);
         assertTrue(calls[0].getError() == null);
         assertTrue(calls[1].getSuccess() == null);
         assertTrue(calls[1].getError() != null);
      }

      // delete the test folders
      dreq = new DeleteFoldersRequest();
      dreq.setId(new long[] {folders[0].getId() });
      binding.deleteFolders(dreq);
   }

   /**
    * Testing Content Folder methods
    *
    * @throws Exception if error occurs.
    */
   @Test
   public void fix_testMoveFolderChildren() throws Exception
   {
      ContentSOAPStub binding = getBinding(null);
      PSTestUtils.setSessionHeader(binding, m_session);

      // Test operation
      try
      {

         // prepare data for the testing

         // create source folder
         String folderPath = "//Sites/EnterpriseInvestments/Files/testSourceFolder"
            + java.lang.System.currentTimeMillis();
         AddFolderTreeRequest atreq = new AddFolderTreeRequest();
         atreq.setPath(folderPath);
         PSFolder[] folders = binding.addFolderTree(atreq);
         PSFolder srcFolder = folders[0];

         // create target folder
         folderPath = "//Sites/EnterpriseInvestments/Files/testTargetFolder"
            + java.lang.System.currentTimeMillis();
         atreq = new AddFolderTreeRequest();
         atreq.setPath(folderPath);
         folders = binding.addFolderTree(atreq);
         PSFolder tgtFolder = folders[0];

         // add folder children
         AddFolderChildrenRequest acreq = new AddFolderChildrenRequest();
         acreq.setParent(new FolderRef(srcFolder.getId(), null));
         long childId_489 = getLegacyGuid(489);
         long childId_490 = getLegacyGuid(490);
         acreq.setChildIds(new long[] { childId_489, childId_490 });
         binding.addFolderChildren(acreq);

         // moving all items from source -> target folder, by id
         MoveFolderChildrenRequest mfcreq = new MoveFolderChildrenRequest();
         mfcreq.setSource(new FolderRef(srcFolder.getId(), null));
         mfcreq.setTarget(new FolderRef(tgtFolder.getId(), null));
         binding.moveFolderChildren(mfcreq);
         // validate the move
         PSItemSummary[] children = findFolderChildren(binding, srcFolder
            .getId(), null, false);
         assertTrue(children.length == 0);
         children = findFolderChildren(binding, tgtFolder.getId(), null, false);
         assertTrue(children.length == 2);

         // moving all items from target -> source folder, by path
         mfcreq = new MoveFolderChildrenRequest();
         mfcreq.setSource(new FolderRef(null, tgtFolder.getPath()));
         mfcreq.setTarget(new FolderRef(null, srcFolder.getPath()));
         binding.moveFolderChildren(mfcreq);
         // validate the move
         children = findFolderChildren(binding, srcFolder.getId(), null, false);
         assertTrue(children.length == 2);
         children = findFolderChildren(binding, tgtFolder.getId(), null, false);
         assertTrue(children.length == 0);

         // moving specified child item from source -> target
         mfcreq = new MoveFolderChildrenRequest();
         mfcreq.setSource(new FolderRef(null, srcFolder.getPath()));
         mfcreq.setTarget(new FolderRef(null, tgtFolder.getPath()));
         mfcreq.setChildId(new long[] { childId_489 });
         binding.moveFolderChildren(mfcreq);
         // validate the move
         children = findFolderChildren(binding, srcFolder.getId(), null, false);
         assertTrue(children.length == 1);
         children = findFolderChildren(binding, tgtFolder.getId(), null, false);
         assertTrue(children.length == 1);

         // delete the test folders
         DeleteFoldersRequest dreq = new DeleteFoldersRequest();
         dreq.setId(new long[] { srcFolder.getId(), tgtFolder.getId() });
         binding.deleteFolders(dreq);
      }
      catch (PSInvalidSessionFault e1)
      {
         throw new junit.framework.AssertionFailedError(
            "InvalidSessionFault Exception caught: " + e1);
      }
      catch (PSNotAuthorizedFault e)
      {
         throw new junit.framework.AssertionFailedError(
            "NotAuthorizedFault Exception caught: " + e);
      }
      catch (RemoteException e)
      {
         throw new junit.framework.AssertionFailedError(
            "RemoteException Exception caught: " + e);
      }

   }

   /**
    * Load Folder Children by either parent folder id (if specified) or by
    * parent folder path.
    *
    * @param binding the object used to communicate with server, assumed not
    *    <code>null</code>.
    * @param parentId the parent folder id, may be <code>null</code> if wanting
    *    to request by folder path.
    * @param parentPath the path of the parent folder, may be <code>null</code>
    *    if wanting to request by folder id.
    * @param isLoadOperations it is <code>true</code> if load the allowed
    *    operations for the returned summaries; otherwise, the allowed
    *    operations in the returned summaries will be <code>null</code> or
    *    empty.
    *
    * @return the child summaries of the specified folder, never
    *    <code>null</code>, may be empty.
    *
    * @throws Exception if any error occurs.
    */
   private PSItemSummary[] findFolderChildren(ContentSOAPStub binding,
      Long parentId, String parentPath, boolean isLoadOperations)
      throws Exception
   {
      FindFolderChildrenRequest fcreq = new FindFolderChildrenRequest();
      if (parentId != null)
         fcreq.setFolder(new FolderRef(parentId, null));
      else
         fcreq.setFolder(new FolderRef(null, parentPath));
      fcreq.setLoadOperations(isLoadOperations);

      return binding.findFolderChildren(fcreq);
   }

   /**
    * Tests the GetAssemblyUrl operation
    *
    * @throws Exception if any error occurs.
    */
   @Test
   public void testGetAssemblyUrl() throws Exception
   {
      ContentSOAPStub binding = getBinding(null);
      PSTestUtils.setSessionHeader(binding, m_session);

      // Test operation
      try
      {
         //\/\/\/\/\/\/\/
         // positive test
         //\/\/\/\/\/\/\/
         GetAssemblyUrlsRequest req = new GetAssemblyUrlsRequest();
         req.setContext(0);
         req.setId(new long[] { getLegacyGuid(335) });
         req.setTemplate("rffPgEiGeneric");
         req.setItemFilter("preview");

         String[] urls = binding.getAssemblyUrls(req).getUrls();
         assertTrue(urls.length == 1);

         req.setItemFilter("public");
         urls = binding.getAssemblyUrls(req).getUrls();
         assertTrue(urls.length == 1);

         // set the site
         req.setSite("Enterprise_Investments");
         urls = binding.getAssemblyUrls(req).getUrls();
         assertTrue(urls.length == 1);

         // set the folder path
         req
            .setFolderPath("//Sites/EnterpriseInvestments/AboutEnterpriseInvestments");
         urls = binding.getAssemblyUrls(req).getUrls();
         assertTrue(urls.length == 1);

         //\/\/\/\/\/\/\/
         // negative test
         //\/\/\/\/\/\/\/

         // set to a non-existence template name
         req.setTemplate("unknown");
         try
         {
            urls = binding.getAssemblyUrls(req).getUrls();
            assertTrue(false);
         }
         catch (Exception e)
         {
            // should come through here since there is no 'unknown' template
            assertTrue(true);
         }

         // set to a non-existence item filter name
         req.setItemFilter("unknown");
         try
         {
            urls = binding.getAssemblyUrls(req).getUrls();
            assertTrue(false);
         }
         catch (Exception e)
         {
            // should come through here since there is no 'unknown' item filter
            assertTrue(true);
         }

         // set to a non-existence site name
         req.setSite("unknown");
         try
         {
            urls = binding.getAssemblyUrls(req).getUrls();
            assertTrue(false);
         }
         catch (Exception e)
         {
            // should come through here since there is no 'unknown' site name
            assertTrue(true);
         }

         // set to a non-existence folder path
         req.setFolderPath("unknown");
         try
         {
            urls = binding.getAssemblyUrls(req).getUrls();
            assertTrue(false);
         }
         catch (Exception e)
         {
            // should come through here since there is no 'unknown' folder path
            assertTrue(true);
         }

         // set to a non-existing content id
         req.setId(new long[] { getLegacyGuid(99999) });
         try
         {
            urls = binding.getAssemblyUrls(req).getUrls();
            assertTrue(false);
         }
         catch (Exception e)
         {
            // should come through here since there is no content id = 99999
            assertTrue(true);
         }

      }
      catch (PSInvalidSessionFault e1)
      {
         throw new junit.framework.AssertionFailedError(
            "InvalidSessionFault Exception caught: " + e1);
      }
      catch (PSNotAuthorizedFault e)
      {
         throw new junit.framework.AssertionFailedError(
            "NotAuthorizedFault Exception caught: " + e);
      }
      catch (RemoteException e)
      {
         throw new junit.framework.AssertionFailedError(
            "RemoteException Exception caught: " + e);
      }

   }

   /**
    * Testing Content Relationship methods
    *
    * @throws Exception if error occurs.
    */
   @Test
   public void fix_testAaRelationship() throws Exception
   {
      ContentSOAPStub binding = getBinding(null);
      PSTestUtils.setSessionHeader(binding, m_session);

      // Test operation
      try
      {
         switchToIECommunity();

         // get the id & name for "rffGeneric" content type
         Reference genericCT = new Reference();
         PSContentTypeSummary[] sums = loadContentTypeSummaries("rffGeneric",
               m_session);
         assertTrue(sums.length == 1);
         genericCT.setId(sums[0].getId());
         genericCT.setName(sums[0].getName());

         // get AA relationships which do not contain site or folder ids
         PSAaRelationship[] rels = loadAaRelationshipsByOwner(binding, 335, null);
         PSAaRelationship[] rels2 = loadAaRelationshipsByOwner(binding, 335, genericCT);
         assertTrue(rels.length > 0);
         assertTrue(rels.length == rels2.length);


         // get AA relationships which contain site and folder ids
         rels = loadAaRelationshipsByOwner(binding, 634, null);
         assertTrue(rels.length > 0);
         long rid1479 = new PSDesignGuid(PSTypeEnum.RELATIONSHIP, 1479)
            .getValue();
         long rid1482 = new PSDesignGuid(PSTypeEnum.RELATIONSHIP, 1482)
            .getValue();
         long rid1483 = new PSDesignGuid(PSTypeEnum.RELATIONSHIP, 1483)
            .getValue();
         for (PSAaRelationship rel : rels)
         {
            long rid = rel.getId().longValue();
            if (rid == rid1479 || rid == rid1482 || rid == rid1483)
            {
               assertTrue(rel.getSite() != null);
               assertTrue(rel.getSite().getName() != null);

               assertTrue(rel.getFolder() != null);
               assertTrue(rel.getFolder().getName() != null);
               assertTrue(rel.getFolder().getPath() != null);
            }
         }

         // test load relationship by ID
         PSAaRelationship rel1479 = loadAaRelationshipsByRid(binding,
            rid1479, true);
         PSAaRelationshipFilter filter = new PSAaRelationshipFilter();
         filter.setId(rel1479.getId());
         filter.setOwner(rel1479.getOwnerId() + 1);
         PSAaRelationship[] rels_empty = loadAaRelationships(binding, filter);
         assertTrue(rels_empty.length == 0);

         // has to check out the owner
         PSItemStatus itemStatus335 = checkoutItem(binding, 335);
         long ownerId335 = getLegacyGuid(335);
         long dependentId2 = getLegacyGuid(460);

         // test addContentRelationships operation
         List<Long> createdRids = new ArrayList<Long>();
         // append 2 new relationships
         addContentRelationships(binding, createdRids,
            PSRelationshipConfig.TYPE_ACTIVE_ASSEMBLY, 1000);
         // append 2 new relationships
         addContentRelationships(binding, createdRids, null, -1);
         // insert 2 new relationships at position 1
         addContentRelationships(binding, createdRids, null, 1);

         // test reordering

         rels = loadAaRelationshipsByOwnerSlot(binding, 335, SLOT_NAME);
         reorderContentRelationships(binding, rels, null);

         rels = reverseAaRelationships(rels);
         reorderContentRelationships(binding, rels, -1);

         rels = reverseAaRelationships(rels);
         reorderContentRelationships(binding, rels, 1000);

         // pushes the top 2 item down one level
         PSAaRelationship[] rels_2 = new PSAaRelationship[2];
         rels_2[0] = rels[0];
         rels_2[1] = rels[1];
         reorderContentRelationships(binding, rels_2, 1);

         // testing saveContentRelations
         PSAaRelationship rel = loadAaRelationshipsByRid(binding,
            createdRids.get(0), true);
         long templateId = new PSDesignGuid(PSTypeEnum.TEMPLATE, 503)
            .getValue();
         rel.setTemplate(new Reference(templateId, ""));
         binding.saveContentRelations(new PSAaRelationship[] { rel });

         // validate the save operation on templateId
         rel = loadAaRelationshipsByRid(binding, rel.getId(), true);
         assertTrue(rel.getTemplate().getId() == templateId);

         // find child items
         FindChildItemsRequest fcreq = new FindChildItemsRequest();
         fcreq.setId(ownerId335);
         fcreq.setLoadOperations(true);
         fcreq.setPSAaRelationshipFilter(new PSAaRelationshipFilter());
         PSItemSummary[] children = binding.findChildItems(fcreq);
         assertTrue(children.length > 0);
         assertTrue(children[0].getOperation() != null);
         for (PSItemSummary child : children)
            assertTrue(child.getOperation().length == 3); // read,trans,checkout

         // specify AA name, should be the same result as above
         PSAaRelationshipFilter aaFilter = new PSAaRelationshipFilter();
         aaFilter.setConfigurations(
               new String[]{PSRelationshipConfig.TYPE_ACTIVE_ASSEMBLY,
                     PSRelationshipConfig.TYPE_ACTIVE_ASSEMBLY_MANDATORY});
         fcreq.setPSAaRelationshipFilter(aaFilter);
         PSItemSummary[] children_2 = binding.findChildItems(fcreq);
         assertTrue(children_2.length == children.length);

         // negative test
         try
         {
            // This should fail on incorrect category for the specified
            // relationship name
            aaFilter.setConfigurations(
                  new String[]{PSRelationshipConfig.TYPE_NEW_COPY});
            binding.findChildItems(fcreq);
            assertTrue(false);
         }
         catch (Exception e)
         {
            assertTrue(true);
         }

         // find parent items
         FindParentItemsRequest fpreq = new FindParentItemsRequest();
         fpreq.setId(dependentId2);
         fpreq.setPSAaRelationshipFilter(new PSAaRelationshipFilter());
         PSItemSummary[] parents = binding.findParentItems(fpreq);
         assertTrue(parents.length > 0);
         assertTrue(parents[0].getOperation() == null);

         // specify AA name, should be the same result as above
         aaFilter = new PSAaRelationshipFilter();
         aaFilter.setConfigurations(
               new String[]{PSRelationshipConfig.TYPE_ACTIVE_ASSEMBLY,
                     PSRelationshipConfig.TYPE_ACTIVE_ASSEMBLY_MANDATORY});
         fpreq.setPSAaRelationshipFilter(aaFilter);
         PSItemSummary[] parents_2 = binding.findParentItems(fpreq);
         assertTrue(parents_2.length == parents.length);

         // negative test
         try
         {
            // This should fail on incorrect category for the specified
            // relationship name
            aaFilter.setConfigurations(
                  new String[]{PSRelationshipConfig.TYPE_NEW_COPY});
            binding.findParentItems(fpreq);
            assertTrue(false);
         }
         catch (Exception e)
         {
            assertTrue(true);
         }


         // testing deleteContentRelationships
         long[] ids = new long[createdRids.size()];
         for (int i=0; i<createdRids.size(); i++)
            ids[i] = createdRids.get(i);
         binding.deleteContentRelations(ids);

         // testing deleted relationships
         assertTrue(loadAaRelationshipsByRid(binding, ids[0], false) == null);
         assertTrue(loadAaRelationshipsByRid(binding, ids[1], false) == null);

         checkinItem(binding, itemStatus335); // cleanup
      }
      catch (PSInvalidSessionFault e1)
      {
         throw new junit.framework.AssertionFailedError(
            "InvalidSessionFault Exception caught: " + e1);
      }
      catch (PSNotAuthorizedFault e)
      {
         throw new junit.framework.AssertionFailedError(
            "NotAuthorizedFault Exception caught: " + e);
      }
      catch (RemoteException e)
      {
         throw new junit.framework.AssertionFailedError(
            "RemoteException Exception caught: " + e);
      }

   }

   /**
    * Reverses the specified relationships.
    *
    * @param rels the to be reversed relationships; assumed not
    *    <code>null</code>.
    *
    * @return the reversed relationship list, never <code>null</code>.
    */
   private PSAaRelationship[] reverseAaRelationships(PSAaRelationship[] rels)
   {
      List<PSAaRelationship> aaList = Arrays.asList(rels);
      Collections.reverse(aaList);
      return aaList.toArray(rels);
   }

   /**
    * Tests the reorderContentRelationships operations.
    *
    * @param binding the binding object; assumed not <code>null</code>.
    * @param rels the to be reordered relationships; assumed not
    *    <code>null</code>.
    * @param index the new location of the reordered relationships.
    *
    * @throws Exception if any error occurs.
    */
   @Test
   private void reorderContentRelationships(ContentSOAPStub binding,
      PSAaRelationship[] rels, Integer index) throws Exception
   {
      // get ids from the relationships
      long[] rids = new long[rels.length];
      for (int i=0; i<rids.length; i++)
         rids[i] = rels[i].getId();

      // perform the re-order
      ReorderContentRelationsRequest roReq =
         new ReorderContentRelationsRequest();
      roReq.setId(rids);
      roReq.setIndex(index);

      binding.reorderContentRelations(roReq);

      int sortRank = 0;
      if (index != null && index.intValue() == 1)
         sortRank = 1;
      // validate the reverse order; the sort-rank equals the index of rid
      PSAaRelationship rel = null;
      for (int i=0; i<rids.length; i++)
      {
         rel = loadAaRelationshipsByRid(binding, rids[i], true);
         assertTrue(rel.getSortRank().intValue() == sortRank++);
      }
   }

   /**
    * The slot name used for the AA relationship operation testing
    */
   private static final String SLOT_NAME = "rffContacts";

   /**
    * The template name used for the AA relationship operation testing
    */
   private static final String TEMPLATE_NAME = "rffSnFlash";

   /**
    * Tests the addContentRelationships operation from the specified parameters.
    *
    * @param binding the binding object used to communicate with the server;
    *    assumed not <code>null</code>.
    * @param createdRids the object used to collect the newly created
    *    relationships.
    * @param index the index of the request.
    *
    * @throws Exception if an error occurs.
    */
   @Test
   private void addContentRelationships(ContentSOAPStub binding,
      List<Long> createdRids, String relationshipName, int index)
      throws Exception
   {
      PSAaRelationship[] existRels = loadAaRelationshipsByOwnerSlot(binding,
         335, SLOT_NAME);

      AddContentRelationsRequest areq = new AddContentRelationsRequest();
      //Sites/EnterpriseInvestments/AboutEnterpriseInvestments/Page - About Enterprise Investments
      long ownerId335 = getLegacyGuid(335);
      long dependentId2 = getLegacyGuid(460);
      long dependentId3 = getLegacyGuid(461);
      areq.setId(ownerId335);
      areq.setRelatedId(new long[] { dependentId2, dependentId3 });
      areq.setSlot(SLOT_NAME);
      areq.setTemplate(TEMPLATE_NAME);
      if (relationshipName != null)
         areq.setRelationshipConfig(relationshipName);
      areq.setIndex(index);
      PSAaRelationship[] rels = binding.addContentRelations(areq);

      // should have 2 additional relationships
      assertTrue(rels.length == 2);

      // save created ids, which will be used for cleanup later
      int sortRank_1 = existRels.length;
      int sortRank_2 = existRels.length + 1;
      if (index == 1)
      {
         sortRank_1 = 1;
         sortRank_2 = 2;
      }
      createdRids.add(rels[0].getId());
      createdRids.add(rels[1].getId());

      // validate the sort-rank of the 2 new relationships
      // the sort-rank(s) should be the same as its index
      PSAaRelationship rel = loadAaRelationshipsByRid(binding,
         rels[0].getId(), true);
      assertTrue(rel.getSortRank().intValue() == sortRank_1);

      rel = loadAaRelationshipsByRid(binding, rels[1].getId(), true);
      assertTrue(rel.getSortRank().intValue() == sortRank_2);
      assertTrue(rel.getSlot().getName().equals(SLOT_NAME));
      assertTrue(rel.getTemplate().getName().equals(TEMPLATE_NAME));
   }

   @Test
   private PSAaRelationship loadAaRelationshipsByRid(ContentSOAPStub binding,
      long rid, boolean isRequired) throws Exception
   {
      PSAaRelationshipFilter filter = new PSAaRelationshipFilter();
      filter.setId(rid);
      PSAaRelationship[] rels = loadAaRelationships(binding, filter);
      if (isRequired)
         assertTrue(rels.length == 1);

      return rels.length == 0 ? null : rels[0];
   }

   private PSAaRelationship[] loadAaRelationshipsByOwner(
      ContentSOAPStub binding, int contentId, Reference ownerCT)
      throws Exception
   {
      PSAaRelationshipFilter filter = new PSAaRelationshipFilter();
      filter.setOwner(getLegacyGuid(contentId));
      filter.setLimitToOwnerRevisions(true);
      if (ownerCT != null)
         filter.setOwnerContentType(ownerCT);

      return loadAaRelationships(binding, filter);
   }

   private PSAaRelationship[] loadAaRelationshipsByOwnerSlot(
            ContentSOAPStub binding, int contentId, String slotName)
            throws Exception
         {
            PSAaRelationshipFilter filter = new PSAaRelationshipFilter();
            filter.setOwner(getLegacyGuid(contentId));
            filter.setLimitToOwnerRevisions(true);
            filter.setSlot(slotName);

            return loadAaRelationships(binding, filter);
         }

   private PSAaRelationship[] loadAaRelationships(ContentSOAPStub binding,
      PSAaRelationshipFilter filter) throws Exception
   {
      LoadContentRelationsRequest lreq = new LoadContentRelationsRequest();
      lreq.setPSAaRelationshipFilter(filter);
      lreq.setLoadReferenceInfo(true);
      return binding.loadContentRelations(lreq);
   }

   @Test
   public void fix_testLoadKeywords() throws Exception
   {
      ContentSOAPStub binding = getBinding(null);

      // Test operation
      try
      {
         deleteTestKeywords();
         createTestKeywords();

         String session = m_session;

         LoadKeywordsRequest request = null;
         PSKeyword[] keywords = null;

         // try to load all keywords without rhythmyx session
         try
         {
            request = new LoadKeywordsRequest();
            request.setName(null);
            binding.loadKeywords(request);
            assertFalse("Should have thrown exception", false);
         }
         catch (PSInvalidSessionFault e)
         {
            // expected exception
            assertTrue(true);
         }

         // try to load all keywords with an invalid rhythmyx session
         PSTestUtils.setSessionHeader(binding, "foobar");
         try
         {
            request = new LoadKeywordsRequest();
            request.setName(null);
            binding.loadKeywords(request);
            assertFalse("Should have thrown exception", false);
         }
         catch (PSInvalidSessionFault e)
         {
            // expected exception
            assertTrue(true);
         }

         PSTestUtils.setSessionHeader(binding, session);

         // load all keywords
         request = new LoadKeywordsRequest();
         request.setName(null);
         keywords = binding.loadKeywords(request);
         assertTrue(keywords.length > 0);

         int count = keywords.length;

         request = new LoadKeywordsRequest();
         request.setName(" ");
         keywords = binding.loadKeywords(request);
         assertTrue(keywords != null && keywords.length == count);

         request = new LoadKeywordsRequest();
         request.setName("*");
         keywords = binding.loadKeywords(request);
         assertTrue(keywords != null && keywords.length == count);

         // try to load a non-existing keyword
         request = new LoadKeywordsRequest();
         request.setName("somekeyword");
         keywords = binding.loadKeywords(request);
         assertTrue(keywords != null && keywords.length == 0);

         // load test keywords
         request = new LoadKeywordsRequest();
         request.setName("keyword_*");
         keywords = binding.loadKeywords(request);
         assertTrue(keywords != null && keywords.length == 3);

         request = new LoadKeywordsRequest();
         request.setName("KEYWORD_*");
         keywords = binding.loadKeywords(request);
         assertTrue(keywords != null && keywords.length == 3);

         request = new LoadKeywordsRequest();
         request.setName("*_0");
         keywords = binding.loadKeywords(request);
         assertTrue(keywords != null && keywords.length == 1);
      }
      catch (PSInvalidSessionFault e1)
      {
         throw new junit.framework.AssertionFailedError(
            "InvalidSessionFault Exception caught: " + e1);
      }
      catch (PSNotAuthorizedFault e)
      {
         throw new junit.framework.AssertionFailedError(
            "NotAuthorizedFault Exception caught: " + e);
      }
      catch (RemoteException e)
      {
         throw new junit.framework.AssertionFailedError(
            "NotAuthorizedFault Exception caught: " + e);
      }
      finally
      {
         deleteTestKeywords();
      }
   }

   /**
    * Test loading locales
    *
    * @throws Exception if the test fails
    */
   @Test
   public void testLoadLocales() throws Exception
   {
      // test no session
      try
      {
         loadLocales(null, null, null);
         assertTrue("Should have thrown", false);
      }
      catch (PSInvalidSessionFault e)
      {
         // expected
      }

      // test invalid session
      try
      {
         loadLocales(null, null, "nosuchsession");
         assertTrue("Should have thrown", false);
      }
      catch (PSInvalidSessionFault e)
      {
         // expected
      }

      cleanupTestLocales();

      List<PSLocale> localesToFind = createTestLocales();
      saveLocales(localesToFind, m_session, false);
      try
      {
         PSLocale[] locales = loadLocales("", "", m_session);
         validateLocaleSummaries(locales, localesToFind);

         locales = loadLocales("", "Test*", m_session);
         validateLocaleSummaries(locales, localesToFind);

         for (PSLocale locale : localesToFind)
         {
            locales = loadLocales(locale.getCode(), null, m_session);
            assertTrue(locales.length == 1);
            assertEquals(locale, locales[0]);

            locales = loadLocales(locale.getCode(), locale.getLabel(),
               m_session);
            assertTrue(locales.length == 1);
            assertEquals(locale, locales[0]);
         }
      }
      catch (com.percussion.webservices.faults.PSInvalidSessionFault e1)
      {
         throw new AssertionFailedError(
            "InvalidSessionFault Exception caught: " + e1);
      }
      catch (Exception e2)
      {
         throw new AssertionFailedError("Unexpected Exception caught: " + e2);
      }
      finally
      {
         // delete locales
         deleteLocales(localesListToIdArray(localesToFind), m_session, false);
      }
   }

   /**
    * Validates that the supplied array of locales contains the expected locales
    * to be found.
    *
    * @param locales The locales to validate, assumed not <code>null</code>.
    * @param localesToFind The expected locales, assumed not <code>null</code>.
    */
   @Test
   private void validateLocaleSummaries(PSLocale[] locales,
      List<PSLocale> localesToFind)
   {
      Map<String, PSLocale> map = new HashMap<String, PSLocale>();
      for (PSLocale locale : locales)
      {
         map.put(locale.getCode(), locale);
      }

      for (PSLocale locale : localesToFind)
      {
         PSLocale test = map.get(locale.getCode());
         assertNotNull(test);
         assertEquals(locale, test);
      }
   }

   /**
    * Test loading and saving auto translations
    *
    * @throws Exception if the test fails
    */
   @Test
   public void testAutoTranslations() throws Exception
   {
      // test no session
      try
      {
         loadAutoTranslations(null);
         assertTrue("Should have thrown", false);
      }
      catch (PSInvalidSessionFault e)
      {
         // expected
      }

      // test invalid session
      try
      {
         loadAutoTranslations("nosuchsession");
         assertTrue("Should have thrown", false);
      }
      catch (PSInvalidSessionFault e)
      {
         // expected
      }

      List<PSAutoTranslation> ats = new ArrayList<PSAutoTranslation>();
      ats.addAll(createTestTranslations());
      List<PSAutoTranslation> curTrans;
      boolean saved = false;
      try
      {
         // load current locked in case we have some
         curTrans = loadAutoTranslations(m_session, true, null, null);

         ats.addAll(curTrans);
         saveAutoTranslations(ats, m_session, false);
         saved = true;
      }
      finally
      {
         if (!saved)
         {
            PSDesignGuid guid = new PSDesignGuid(
               com.percussion.services.content.data.PSAutoTranslation
                  .getAutoTranslationsGUID());
            PSTestUtils.releaseLocks(m_session, new long[] { guid.getValue() });
         }
      }

      try
      {
         assertEquals(loadAutoTranslations(m_session), ats);
      }
      catch (com.percussion.webservices.faults.PSInvalidSessionFault e1)
      {
         throw new AssertionFailedError(
            "InvalidSessionFault Exception caught: " + e1);
      }
      catch (Exception e2)
      {
         throw new AssertionFailedError("Unexpected Exception caught: " + e2);
      }
      finally
      {
         if (saved)
         {
            // save just what we had
            loadAutoTranslations(m_session, true, null, null);
            saveAutoTranslations(curTrans, m_session, true);
         }
      }
   }

   /**
    * Tests the loadContenTypes web service.  Creates test content types and
    * deletes them when finished, so this test relies on some of the CRUD design
    * services.
    *
    * @throws Exception If the test fails.
    */
   @Test
   public void testLoadContentTypes() throws Exception
   {
      // test no session
      try
      {
         loadContentTypeSummaries(null, null);
         assertTrue("Should have thrown", false);
      }
      catch (PSInvalidSessionFault e)
      {
         // expected
      }

      // test invalid session
      try
      {
         loadContentTypeSummaries(null, "nosuchsession");
         assertTrue("Should have thrown", false);
      }
      catch (PSInvalidSessionFault e)
      {
         // expected
      }

      List<PSContentType> typesToFind = createTestContentTypes(m_session);
      saveContentTypes(typesToFind, m_session, false);
      try
      {
         PSContentTypeSummary[] sums;

         sums = loadContentTypeSummaries(null, m_session);
         validateContentTypeSummaries(sums, typesToFind);

         sums = loadContentTypeSummaries("", m_session);
         validateContentTypeSummaries(sums, typesToFind);

         sums = loadContentTypeSummaries("test*", m_session);
         validateContentTypeSummaries(sums, typesToFind);
      }
      catch (com.percussion.webservices.faults.PSInvalidSessionFault e1)
      {
         throw new AssertionFailedError(
            "InvalidSessionFault Exception caught: " + e1);
      }
      catch (Exception e2)
      {
         throw new AssertionFailedError("Unexpected Exception caught: " + e2);
      }
      finally
      {
         deleteContentTypes(ctypesListToIdArray(typesToFind), m_session);
      }
   }

   /**
    * Validates that the summaries are in the expected types, and that the
    * fields match.
    *
    * @param sums The list of summaries to validate, assumed not
    * <code>null</code>.
    * @param typesToFind The list of expected types, assumed not
    * <code>null</code> or empty.
    */
   @SuppressWarnings( { "unchecked", "unchecked" })
   @Test
   private void validateContentTypeSummaries(PSContentTypeSummary[] sums,
      List<PSContentType> typesToFind)
   {
      Map<String, PSContentTypeSummary> sumMap = new HashMap<String, PSContentTypeSummary>();
      for (PSContentTypeSummary summary : sums)
      {
         assertTrue(summary.getFields().length > 0);

         if (summary.getName().equals("Folder"))
            validateFolderContentType(summary);

         sumMap.put(summary.getName(), summary);
      }

      for (PSContentType ctype : typesToFind)
      {
         String name = ctype.getName();
         PSContentTypeSummary sum = sumMap.get(name);
         assertNotNull(sum);
         assertEquals(ctype.getDescription(), sum.getDescription());
         PSContentEditor ce = m_testCEMap.get(name);
         assertNotNull(ce);
         Map<String, PSFieldDescription> fieldMap = new HashMap<String, PSFieldDescription>();
         PSFieldDescription[] fields = sum.getFields();
         for (PSFieldDescription field : fields)
         {
            fieldMap.put(field.getName(), field);
         }

         Iterator<Object> mappings = ((PSContentEditorPipe) ce.getPipe())
            .getMapper().getUIDefinition().getDisplayMapper().iterator();
         while (mappings.hasNext())
         {
            PSDisplayMapping mapping = (PSDisplayMapping) mappings.next();
            if (!mapping.getFieldRef().equals("dummy"))
            {
               PSFieldDescription fieldDesc = fieldMap.get(mapping
                     .getFieldRef());
               assertNotNull(fieldDesc);
            }
         }
      }
   }

   /**
    * Validates the folder content type, which contains parent and child fields.
    *
    * @param folderCT the folder content type in question; assumed not
    *    <code>null</code>.
    */
   @Test
   private void validateFolderContentType(PSContentTypeSummary folderCT)
   {
      assertTrue(folderCT.getName().equals("Folder"));
      assertEquals(folderCT.getFields().length, 8);

      PSContentTypeSummaryChild[] children = folderCT.getChildren();
      assertEquals(children.length, 2);

      for (PSContentTypeSummaryChild child : children)
      {
         assertTrue(child.getName().equals("properties") ||
               child.getName().equals("acl"));
         assertTrue(child.getChildField().length == 3);
      }
   }

   /**
    * Tests all child entry service functionality.
    *
    * @throws Exception if the test fails.
    */
   @Test
   public void testContentSOAPChildItems() throws Exception
   {
      ContentSOAPStub binding = getBinding(6000000);
      PSContentType contentType = null;
      long[] ids = null;
      List<PSAclImpl> acls = new ArrayList<PSAclImpl>();
      try
      {
         // create the test content types
         contentType = createContentType("test3", m_session, acls);

         // create 3 test items for the new content type
         PSTestUtils.setSessionHeader(binding, m_session);
         List<PSItem> items = createTestItems(contentType.getName(), 1, true,
            true, false, null, m_session, binding);

         ids = toItemIds(items);
         assertEquals(1, ids.length);

         long contentId = ids[0];
         String childName = "child_1";
         int count = 4;

         PSChildEntry[] entries = doTestCreateChildItems(contentId, childName,
            count, "field_12", "1", binding);
         entries = doTestSaveChildItems(contentId, childName, entries, binding);

         entries = doTestLoadChildItems(contentId, childName, entries);

         entries = doTestReorderChildItems(contentId, childName, entries);

         // test delete of child items
         doTestDeleteChildItems(contentId, childName, entries);
      }
      finally
      {
         // cleanup
         try
         {
            if (ids != null)
               binding.deleteItems(ids);
         }
         catch (Exception e)
         {
            java.lang.System.out
               .println("Failed to cleanup test content item: "
                  + e.getLocalizedMessage());
         }

         cleanUpContentType(m_session, contentType, acls);
      }
   }

   /**
    * Test child entry creation.
    *
    * @param contentId The item id
    * @param childName The child field name, may be <code>null</code> or empty
    * to test contract enforcement
    * @param count The number of entries to create, may be <=0 to test contract
    * enforcemnt
    * @param fieldName The name of the field to check for a default value,
    * assumed not <code>null</code> or empty.
    * @param fieldVal The expected value of the field, assumed not
    * <code>null</code>.
    * @param binding the stub used, assumed not <code>null</code>.
    * @return An array of entries, never <code>null</code> and will have the
    * specified count.
    *
    * @throws Exception
    */
   @Test
   private PSChildEntry[] doTestCreateChildItems(long contentId,
      String childName, int count, String fieldName, String fieldVal,
      ContentSOAPStub binding) throws Exception
   {
      // test no session
      try
      {
         createChildEntries(contentId, childName, 1, null);
         assertTrue("Should have thrown", false);
      }
      catch (PSInvalidSessionFault e)
      {
         // expected
      }

      // test invalid session
      try
      {
         createChildEntries(contentId, childName, 1, "nosuchsession");
         assertTrue("Should have thrown", false);
      }
      catch (PSInvalidSessionFault e)
      {
         // expected
      }

      // test bad input
      try
      {
         createChildEntries(contentId, "", 1, m_session);
         assertTrue("Should have thrown", false);
      }
      catch (PSContractViolationFault e)
      {
         // expected
      }

      // test bad input
      try
      {
         createChildEntries(contentId, childName, -1, m_session);
         assertTrue("Should have thrown", false);
      }
      catch (PSContractViolationFault e)
      {
         // expected
      }

      // test item not checked out, first ensure checked out, then check in
      ensureItemCheckedIn(contentId);

      try
      {
         createChildEntries(contentId, childName, count, m_session);
         assertTrue("Should have thrown", false);
      }
      catch (PSContractViolationFault e)
      {
         // expected
      }

      // test invalid guid
      long badId = changeRevision(contentId, 2);
      try
      {
         createChildEntries(badId, childName, count, m_session);
         assertTrue("Should have thrown", false);
      }
      catch (PSContractViolationFault e)
      {
         // expected
      }

      // now check out
      checkoutItem(contentId);

      // test bad id
      try
      {
         createChildEntries(badId, childName, count, m_session);
         assertTrue("Should have thrown", false);
      }
      catch (PSContractViolationFault e)
      {
         // expected
      }

      PSChildEntry[] entries = createChildEntries(contentId, childName, count,
         m_session);
      assertEquals(count, entries.length);

      // check default value
      populateChildRows(entries, fieldName, fieldVal, false, 1, binding);

      return entries;
   }

   /**
    * Populates field values in the supplied entries, also validating the
    * default field value if specified.
    *
    * @param entries The entries to populate, assumed not <code>null</code>.
    * @param fieldName The name of the field to validate, may be
    *    <code>null</code> or empty if validation is not required.
    * @param fieldVal The expected value, ignored if no field name is specified.
    * @param attachBinaries <code>true</code> to create attachments for binary
    *    fields, <code>false</code> to use base64 encoded data.
    * @param updateId an update identifier used to differentiate contents
    *    between updates.
    * @param binding the stub used, assumed not <code>null</code>.
    */
   @Test
   private void populateChildRows(PSChildEntry[] entries, String fieldName,
      String fieldVal, boolean attachBinaries, int updateId,
      ContentSOAPStub binding)
   {
      if (updateId <= 0)
         throw new IllegalArgumentException("updateId must be > 0");

      for (int i = 0; i < entries.length; i++)
      {
         PSChildEntry entry = entries[i];
         if (entry == null)
         {
            entry = new PSChildEntry();
            entries[i] = entry;
         }

         PSField[] fields = entry.getPSField();

         String testFieldVal = null;
         for (int j = 0; j < fields.length; j++)
         {
            PSField field = fields[j];
            PSFieldDataType dt = field.getDataType();
            PSFieldValue[] vals = field.getPSFieldValue();
            if (vals == null)
            {
               // create empty value to set later
               if (dt.equals(PSFieldDataType.text)
                  || dt.equals(PSFieldDataType.number)
                  || dt.equals(PSFieldDataType.date)
                  || dt.equals(PSFieldDataType.binary))
               {
                  vals = new PSFieldValue[1];
                  vals[0] = new PSFieldValue();
               }

               field.setPSFieldValue(vals);
            }
            else
            {
               assertEquals(vals.length, 1);

               if (!StringUtils.isBlank(fieldName)
                  && field.getName().equals(fieldName))
               {
                  testFieldVal = vals[0].getRawData();
               }
            }

            if (dt.equals(PSFieldDataType.text))
            {
               vals[0].setRawData(field.getName() + "-test." + updateId);
            }
            else if (dt.equals(PSFieldDataType.number))
            {
               vals[0].setRawData(updateId + String.valueOf(i)
                  + String.valueOf(j));
            }
            else if (dt.equals(PSFieldDataType.date))
            {
               Calendar calendar = Calendar.getInstance();
               calendar.setTime(new Date());
               calendar.set(Calendar.DAY_OF_YEAR, updateId);
               PSDateValue dateValue = new PSDateValue(calendar.getTime());

               vals[0].setRawData(dateValue.getValueAsString());
            }
            else if (dt.equals(PSFieldDataType.binary))
            {
               if (attachBinaries)
               {
                  File file = new File(RESOURCE_BASE + "attachments/",
                     "child.jpg");
                  String attachmentId = addAttachment(binding, file);
                  vals[0].setAttachmentId(attachmentId);
               }
               else
               {
                  String fieldValue = String.valueOf(i) + String.valueOf(j)
                     + String.valueOf(updateId) + ": some binary !@#$%^&*";
                  vals[0].setRawData(PSBase64Encoder.encode(fieldValue));
               }
            }
         }

         // Do not compare the default value, which does not work any more
         // with the "fix" from RX-14410.
         //if (!StringUtils.isBlank(fieldName))
         //   assertEquals(testFieldVal, fieldVal);
      }
   }

   /**
    * Change the revision of the supplied id
    *
    * @param id the id to use
    * @param revision The new revision
    *
    * @return the id with the same content id, but the specified revision.
    */
   private long changeRevision(long id, int revision)
   {
      PSLegacyGuid lguid = new PSLegacyGuid(id);
      lguid = new PSLegacyGuid(lguid.getContentId(), revision);
      long badId = lguid.longValue();
      return badId;
   }

   /**
    * Test saving child items.
    *
    * @param id The content item id.
    * @param name The child field name, assumed not <code>null</code> or empty.
    * @param entries The entries to save, assumed not <code>null</code> or
    * empty.
    * @param binding the stub used, assumed not <code>null</code>.
    * @return The loaded entries that were saved, never <code>null</code> or
    * empty.
    *
    * @throws Exception if there are any errors.
    */
   @Test
   private PSChildEntry[] doTestSaveChildItems(long id, String name,
      PSChildEntry[] entries, ContentSOAPStub binding) throws Exception
   {
      // test no session
      try
      {
         saveChildEntries(id, name, entries, null);
         assertTrue("Should have thrown", false);
      }
      catch (PSInvalidSessionFault e)
      {
         // expected
      }

      // test invalid session
      try
      {
         saveChildEntries(id, name, entries, "noshuchsession");
         assertTrue("Should have thrown", false);
      }
      catch (PSInvalidSessionFault e)
      {
         // expected
      }

      // test invalid input
      try
      {
         saveChildEntries(id, "", entries, m_session);
         assertTrue("Should have thrown", false);
      }
      catch (PSContractViolationFault e)
      {
         // expected
      }

      // test not checked out
      ensureItemCheckedIn(id);
      try
      {
         saveChildEntries(id, name, entries, m_session);
         assertTrue("Should have thrown", false);
      }
      catch (PSContractViolationFault e)
      {
         // expected
      }

      // test invalid guid
      long badId = changeRevision(id, 2);
      try
      {
         saveChildEntries(badId, name, entries, m_session);
         assertTrue("Should have thrown", false);
      }
      catch (PSContractViolationFault e)
      {
         // expected
      }

      // now check out
      checkoutItem(id);

      // test bad id
      try
      {
         saveChildEntries(badId, name, entries, m_session);
         assertTrue("Should have thrown", false);
      }
      catch (PSContractViolationFault e)
      {
         // expected
      }

      // save
      saveChildEntries(id, name, entries, m_session);

      PSChildEntry[] loaded = loadChildEntries(id, name, true, m_session);
      compareChildEntries(entries, loaded, false);

      // now modify save and reload again
      PSField field = loaded[0].getPSField()[0];
      PSFieldValue val = field.getPSFieldValue()[0];
      if (field.getDataType().getValue().equals(PSFieldDataType._text))
      {
         val.setRawData(val.getRawData() + "-test");
      }
      else if (field.getDataType().getValue().equals(PSFieldDataType._number))
      {
         String oldVal = val.getRawData();
         String newVal = StringUtils.isBlank(oldVal) ? "1" : String
            .valueOf(Integer.parseInt(oldVal) + 1);
         val.setRawData(newVal);
      }

      saveChildEntries(id, name, loaded, m_session);
      entries = loaded;
      loaded = loadChildEntries(id, name, true, m_session);
      compareChildEntries(entries, loaded, true);

      // add an entry
      PSChildEntry[] newEntries = new PSChildEntry[entries.length + 1];
      java.lang.System.arraycopy(entries, 0, newEntries, 0, entries.length);
      PSChildEntry[] createdEntries = createChildEntries(id, name, 1, m_session);
      populateChildRows(createdEntries, null, null, false, 2, binding);
      newEntries[newEntries.length - 1] = createdEntries[0];

      saveChildEntries(id, name, newEntries, m_session);
      loaded = loadChildEntries(id, name, true, m_session);
      compareChildEntries(newEntries, loaded, false);

      return loaded;
   }

   /**
    * Test reordering child entries.
    *
    * @param id The content item id.
    * @param name The child field name, assumed not <code>null</code> or empty.
    * @param entries The entries to reorder, assumed not <code>null</code> or
    * empty.
    *
    * @return The reordered entries that were saved, never <code>null</code> or
    * empty.
    *
    * @throws Exception if there are any errors.
    */
   @Test
   private PSChildEntry[] doTestReorderChildItems(long id, String name,
      PSChildEntry[] entries) throws Exception
   {
      long[] childIds = new long[entries.length];
      for (int i = 0; i < childIds.length; i++)
      {
         childIds[i] = entries[i].getId();
      }

      // test no session
      try
      {
         reorderChildEntries(id, name, childIds, null);
         assertTrue("Should have thrown", false);
      }
      catch (PSInvalidSessionFault e)
      {
         // expected
      }

      // test invalid session
      try
      {
         reorderChildEntries(id, name, childIds, "noshuchsession");
         assertTrue("Should have thrown", false);
      }
      catch (PSInvalidSessionFault e)
      {
         // expected
      }

      // test invalid input
      try
      {
         reorderChildEntries(id, "", childIds, m_session);
         assertTrue("Should have thrown", false);
      }
      catch (PSContractViolationFault e)
      {
         // expected
      }

      // test not checked out
      ensureItemCheckedIn(id);
      try
      {
         reorderChildEntries(id, name, childIds, m_session);
         assertTrue("Should have thrown", false);
      }
      catch (PSContractViolationFault e)
      {
         // expected
      }

      // test invalid guid
      long badId = changeRevision(id, 2);
      try
      {
         reorderChildEntries(badId, name, childIds, m_session);
         assertTrue("Should have thrown", false);
      }
      catch (PSContractViolationFault e)
      {
         // expected
      }

      // now check out
      checkoutItem(id);

      // test bad id
      try
      {
         reorderChildEntries(badId, name, childIds, m_session);
         assertTrue("Should have thrown", false);
      }
      catch (PSContractViolationFault e)
      {
         // expected
      }

      // reverse the order
      for (int i = 0; i < childIds.length; i++)
      {
         long childId = childIds[i];
         int next = i + 1;
         if (next < childIds.length)
         {
            childIds[i] = childIds[next];
            childIds[next] = childId;
         }
      }

      reorderChildEntries(id, name, childIds, m_session);

      PSChildEntry[] loaded;
      loaded = loadChildEntries(id, name, false, m_session);
      assertEquals(loaded.length, childIds.length);
      for (int i = 0; i < loaded.length; i++)
      {
         assertEquals(childIds[i], loaded[i].getId().longValue());
      }

      // try a subset
      int remainder = 2;
      int partialSize = entries.length - remainder;
      assertTrue("test requires more entries", partialSize > 1);
      long[] partialIds = new long[partialSize];

      java.lang.System.arraycopy(childIds, remainder, partialIds, 0,
         partialSize);
      java.lang.System.arraycopy(childIds, 0, childIds, childIds.length
         - remainder, remainder);
      java.lang.System.arraycopy(partialIds, 0, childIds, 0, partialSize);

      reorderChildEntries(id, name, partialIds, m_session);

      loaded = loadChildEntries(id, name, false, m_session);
      assertEquals(loaded.length, childIds.length);
      for (int i = 0; i < loaded.length; i++)
      {
         assertEquals(childIds[i], loaded[i].getId().longValue());
      }

      return loaded;
   }

   /**
    * Test deleting child entries.
    *
    * @param id The content item id.
    * @param name The child field name, assumed not <code>null</code> or empty.
    * @param entries The entries to delete, assumed not <code>null</code> or
    * empty.
    *
    * @throws Exception if there are any errors.
    */
   @Test
   private void doTestDeleteChildItems(long id, String name,
      PSChildEntry[] entries) throws Exception
   {
      long[] childIds = new long[entries.length];
      for (int i = 0; i < childIds.length; i++)
      {
         childIds[i] = entries[i].getId();
      }

      // test no session
      try
      {
         deleteChildEntries(id, name, childIds, null);
         assertTrue("Should have thrown", false);
      }
      catch (PSInvalidSessionFault e)
      {
         // expected
      }

      // test invalid session
      try
      {
         deleteChildEntries(id, name, childIds, "noshuchsession");
         assertTrue("Should have thrown", false);
      }
      catch (PSInvalidSessionFault e)
      {
         // expected
      }

      // test invalid input
      try
      {
         deleteChildEntries(id, "", childIds, m_session);
         assertTrue("Should have thrown", false);
      }
      catch (PSContractViolationFault e)
      {
         // expected
      }

      // test not checked out
      ensureItemCheckedIn(id);
      try
      {
         deleteChildEntries(id, name, childIds, m_session);
         assertTrue("Should have thrown", false);
      }
      catch (PSContractViolationFault e)
      {
         // expected
      }

      // test invalid guid
      long badId = changeRevision(id, 2);
      try
      {
         deleteChildEntries(badId, name, childIds, m_session);
         assertTrue("Should have thrown", false);
      }
      catch (PSContractViolationFault e)
      {
         // expected
      }

      // now check out
      checkoutItem(id);

      // test bad id
      try
      {
         deleteChildEntries(badId, name, childIds, m_session);
         assertTrue("Should have thrown", false);
      }
      catch (PSContractViolationFault e)
      {
         // expected
      }

      // delete one
      int delIndex = childIds.length - 1;
      long[] delIds = new long[] { childIds[delIndex] };
      deleteChildEntries(id, name, delIds, m_session);

      PSChildEntry[] remainder = new PSChildEntry[delIndex];
      java.lang.System.arraycopy(entries, 0, remainder, 0, delIndex);
      compareChildEntries(remainder, loadChildEntries(id, name, false,
         m_session), true);

      // delete all, pass non-existant id as well (should be ignored)
      deleteChildEntries(id, name, childIds, m_session);
      assertEquals(0, loadChildEntries(id, name, false, m_session).length);
   }

   /**
    * Test loading child items
    * @param id The content item id.
    * @param name The child field name, assumed not <code>null</code> or empty.
    * @param entries The entries to save, assumed not <code>null</code> or
    * empty.
    *
    * @return The loaded entries, neer <code>null</code>
    *
    * @throws Exception if there are any errors.
    */
   @Test
   private PSChildEntry[] doTestLoadChildItems(long id, String name,
      PSChildEntry[] entries) throws Exception
   {
      // test no session
      try
      {
         loadChildEntries(id, name, false, null);
         assertTrue("Should have thrown", false);
      }
      catch (PSInvalidSessionFault e)
      {
         // expected
      }

      // test invalid session
      try
      {
         loadChildEntries(id, name, false, "noshuchsession");
         assertTrue("Should have thrown", false);
      }
      catch (PSInvalidSessionFault e)
      {
         // expected
      }

      // test invalid input
      try
      {
         loadChildEntries(id, "", false, m_session);
         assertTrue("Should have thrown", false);
      }
      catch (PSContractViolationFault e)
      {
         // expected
      }

      // test not checked out
      ensureItemCheckedIn(id);

      // should load readonly
      compareChildEntries(entries, loadChildEntries(id, name, true, m_session),
         true);

      // test invalid guid
      long badId = changeRevision(id, 2);
      try
      {
         loadChildEntries(badId, name, false, m_session);
         assertTrue("Should have thrown", false);
      }
      catch (PSContractViolationFault e)
      {
         // expected
      }

      // now check out
      checkoutItem(id);

      // test bad id
      try
      {
         loadChildEntries(badId, name, false, m_session);
         assertTrue("Should have thrown", false);
      }
      catch (PSContractViolationFault e)
      {
         // expected
      }

      try
      {
         long invalidId = id + 10000;
         loadChildEntries(invalidId, name, true, m_session);
         assertFalse("Should have thrown exception", true);
      }
      catch (PSErrorResultsFault e)
      {
         verifyErrorResultsFault(e, -1, PSChildEntry.class.getName());
      }

      PSChildEntry[] loaded = loadChildEntries(id, name, true, m_session);
      compareChildEntries(entries, loaded, true);

      return loaded;
   }

   /**
    * Compare the two entry arrays.
    *
    * @param srcEntries The source entries, assumed not <code>null</code>.
    * @param tgtEntries The target entries, assumed not <code>null</code>.
    * @param compareIds <code>true</code> to compare ids, <code>false</code> to
    * compare only other values (used to compare a new entry with what is loaded
    * after it is saved).
    */
   @Test
   private void compareChildEntries(PSChildEntry[] srcEntries,
      PSChildEntry[] tgtEntries, boolean compareIds)
   {
      assertEquals(srcEntries.length, tgtEntries.length);
      for (int i = 0; i < srcEntries.length; i++)
      {
         PSChildEntry src = srcEntries[i];
         PSChildEntry tgt = tgtEntries[i];
         if (compareIds)
            assertEquals(src, tgt);
         else
         {
            PSField[] srcFields = src.getPSField();
            PSField[] tgtFields = tgt.getPSField();
            assertEquals(srcFields.length, tgtFields.length);
            for (int j = 0; j < srcFields.length; j++)
            {
               assertEquals(srcFields[j], tgtFields[j]);
            }
         }
      }
   }

   /**
    * Create the specified child entries
    * @param id The item id
    * @param name The child field name, may be <code>null</code> or empty to
    * test contract enforcement
    * @param count The number of entries to create, may be <=0 to test
    * contract enforcemnt
    * @param session The session, may be <code>null</code> or empty to test
    * authorization
    *
    * @return The entries, never <code>null</code>.
    *
    * @throws Exception If there are any errors.
    */
   private PSChildEntry[] createChildEntries(long id, String name, int count,
      String session) throws Exception
   {
      ContentSOAPStub binding = getBinding(null);

      if (session != null)
         PSTestUtils.setSessionHeader(binding, session);

      CreateChildEntriesRequest req = new CreateChildEntriesRequest();
      req.setId(id);
      req.setCount(count);
      req.setName(name);

      return binding.createChildEntries(req);
   }

   /**
    * Ensures the specified item is checked in.
    *
    * @param id The content id.
    *
    * @throws Exception if there are any errors.
    */
   @Test
   private void ensureItemCheckedIn(long id) throws Exception
   {
      long[] idarr = new long[] { id };
      ContentSOAPStub binding = getBinding(null);
      PSTestUtils.setSessionHeader(binding, m_session);
      PSItemStatus[] statusarr = binding.prepareForEdit(idarr);
      assertEquals(statusarr.length, 1);

      // if it wasn't checked out, we need to say it was so it will check in
      statusarr[0].setDidCheckout(true);
      ReleaseFromEditRequest releaseReq = new ReleaseFromEditRequest();
      releaseReq.setCheckInOnly(true);
      releaseReq.setPSItemStatus(statusarr);
      binding.releaseFromEdit(releaseReq);
   }

   /**
    * Checks out the specified item
    *
    * @param id The content id
    *
    * @return the resulting item status, never <code>null</code>.
    */
   private PSItemStatus[] checkoutItem(long id) throws Exception
   {
      long[] idarr = new long[] { id };
      ContentSOAPStub binding = getBinding(null);
      PSTestUtils.setSessionHeader(binding, m_session);
      return binding.prepareForEdit(idarr);
   }

   /**
    * Save the supplied child entries.
    *
    * @param id The item id
    * @param name The child field name, may be <code>null</code> or empty to
    * test contract enforcement
    * @param entries The entires to save, assumed not <code>null</code> or
    * empty.
    * @param session The session, may be <code>null</code> or empty to test
    * authorization
    *
    * @throws Exception if there are any errors.
    */
   private void saveChildEntries(long id, String name, PSChildEntry[] entries,
      String session) throws Exception
   {
      ContentSOAPStub binding = getBinding(600000);

      if (session != null)
         PSTestUtils.setSessionHeader(binding, session);

      SaveChildEntriesRequest req = new SaveChildEntriesRequest();
      req.setId(id);
      req.setName(name);
      req.setPSChildEntry(entries);

      binding.saveChildEntries(req);
   }

   /**
    * Loads the specified child entries;
    * @param id The item id
    * @param name The child field name, may be <code>null</code> or empty to
    * test contract enforcement.
    * @param includeBinary <code>true</code> to load binary fields,
    * <code>false</code> otherwise.
    * @param session The session, may be <code>null</code> or empty to test
    * authorization
    *
    * @return The loaded entries, never <code>null</code>.
    *
    * @throws Exception If there are any errors.
    */
   private PSChildEntry[] loadChildEntries(long id, String name,
      boolean includeBinary, String session) throws Exception
   {
      ContentSOAPStub binding = getBinding(null);

      if (session != null)
         PSTestUtils.setSessionHeader(binding, session);

      LoadChildEntriesRequest req = new LoadChildEntriesRequest();
      req.setId(id);
      req.setIncludeBinaries(includeBinary);
      req.setName(name);

      return binding.loadChildEntries(req);
   }

   /**
    * Reorders child entries.
    *
    * @param id The item id
    * @param name The child field name, may be <code>null</code> or empty to
    * test contract enforcement.
    * @param childIds The new order of child ids, may only specify a subset of
    * ids, see {@link ReorderChildEntriesRequest#setChildId(long[])} for more
    * info.
    * @param session the session to use, may be <code>null</code> or empty to
    * test authorization.
    *
    * @throws Exception If there are any errors.
    */
   private void reorderChildEntries(long id, String name, long[] childIds,
      String session) throws Exception
   {
      ContentSOAPStub binding = getBinding(null);

      if (session != null)
         PSTestUtils.setSessionHeader(binding, session);

      ReorderChildEntriesRequest req = new ReorderChildEntriesRequest();
      req.setId(id);
      req.setName(name);
      req.setChildId(childIds);

      binding.reorderChildEntries(req);
   }

   /**
    * Deletes child entries.
    *
    * @param id The item id
    * @param name The child field name, may be <code>null</code> or empty to
    * test contract enforcement.
    * @param childIds The child ids to delete, assumed not <code>null</code>.
    * @param session the session to use, may be <code>null</code> or empty to
    * test authorization.
    *
    * @throws Exception If there are any errors.
    */
   private void deleteChildEntries(long id, String name, long[] childIds,
      String session) throws Exception
   {
      ContentSOAPStub binding = getBinding(null);

      if (session != null)
         PSTestUtils.setSessionHeader(binding, session);

      DeleteChildEntriesRequest req = new DeleteChildEntriesRequest();
      req.setChildId(childIds);
      req.setId(id);
      req.setName(name);

      binding.deleteChildEntries(req);
   }

   /**
    * Test the item creation service.
    *
    * @throws Exception for any error.
    */
   @Test
   public void testCreateContentItems() throws Exception
   {
      ContentSOAPStub binding = getBinding(6000000);

      String session = m_session;

      PSContentType contentType = null;
      List<PSAclImpl> acls = new ArrayList<PSAclImpl>();

      try
      {
         // create the test content types
         contentType = createContentType("test3", session, acls);

         CreateItemsRequest request = null;
         PSItem[] items = null;

         // try create without rhythmyx session
         try
         {
            request = new CreateItemsRequest();
            request.setContentType(contentType.getName());
            binding.createItems(request);
            assertFalse("Should have thrown exception", false);
         }
         catch (PSInvalidSessionFault e)
         {
            // expected exception
            assertTrue(true);
         }

         // try create with an invalid rhythmyx session
         PSTestUtils.setSessionHeader(binding, "foobar");
         try
         {
            request = new CreateItemsRequest();
            request.setContentType(contentType.getName());
            binding.createItems(request);
            assertFalse("Should have thrown exception", false);
         }
         catch (PSInvalidSessionFault e)
         {
            // expected exception
            assertTrue(true);
         }

         PSTestUtils.setSessionHeader(binding, session);

         // try create with invalid content type
         try
         {
            request = new CreateItemsRequest();
            request.setContentType(null);
            binding.createItems(request);
            assertFalse("Should have thrown exception", false);
         }
         catch (RemoteException e)
         {
            // expected exception
            assertTrue(true);
         }

         // try create with invalid content type
         try
         {
            request = new CreateItemsRequest();
            request.setContentType(" ");
            binding.createItems(request);
            assertFalse("Should have thrown exception", false);
         }
         catch (PSContractViolationFault e)
         {
            // expected exception
            assertTrue(true);
         }

         // try create with unknown content type
         try
         {
            request = new CreateItemsRequest();
            request.setContentType("somecontenttype");
            binding.createItems(request);
            assertFalse("Should have thrown exception", false);
         }
         catch (PSUnknownContentTypeFault e)
         {
            // expected exception
            assertTrue(true);
         }

         // try create with invalid count
         try
         {
            request = new CreateItemsRequest();
            request.setContentType(contentType.getName());
            request.setCount(0);
            binding.createItems(request);
            assertFalse("Should have thrown exception", false);
         }
         catch (PSContractViolationFault e)
         {
            // expected exception
            assertTrue(true);
         }

         // by default we create 1 item
         request = new CreateItemsRequest();
         request.setContentType(contentType.getName());
         items = binding.createItems(request);
         assertTrue(items != null && items.length == 1);

         // create 3 items
         request = new CreateItemsRequest();
         request.setContentType(contentType.getName());
         request.setCount(3);
         items = binding.createItems(request);
         assertTrue(items != null && items.length == 3);
      }
      catch (PSInvalidSessionFault e)
      {
         throw new junit.framework.AssertionFailedError("Invalid session: " + e);
      }
      catch (PSNotAuthorizedFault e)
      {
         throw new junit.framework.AssertionFailedError("Not authorized: " + e);
      }
      catch (PSUnknownContentTypeFault e)
      {
         throw new junit.framework.AssertionFailedError(
            "Unknown content type: " + e);
      }
      catch (RemoteException e)
      {
         throw new junit.framework.AssertionFailedError("Unexpected error: "
            + e);
      }
      finally
      {
         // cleanup
         cleanUpContentType(session, contentType, acls);
      }
   }

   /**
    * Test the item save service.
    *
    * @throws Exception for any error.
    */
   @Test
   public void FIXME_testSaveContentItems() throws Exception
   {
      ContentSOAPStub binding = getBinding(600000000);

      String session = m_session;

      PSContentType contentType = null;
      List<PSAclImpl> acls = new ArrayList<PSAclImpl>();
      List<PSItem> items = new ArrayList<PSItem>();
      boolean cleanupFolders = false;

      try
      {
         // cleanup the content type which may be left from previous test run
         cleanUpContentType(session, "test3");

         // create the test content types
         contentType = createContentType("test3", session, acls);

         // create test items for each content type
         PSTestUtils.setSessionHeader(binding, session);
         int rows = 3;
         items.addAll(createTestItems(contentType.getName(), rows, true, false,
            false, null, session, binding));

         SaveItemsRequest request = null;

         // try without rhythmyx session
         binding.clearHeaders();
         try
         {
            request = new SaveItemsRequest();
            request.setPSItem(toArray(items));
            binding.saveItems(request);
            assertFalse("Should have thrown exception", false);
         }
         catch (PSInvalidSessionFault e)
         {
            // expected exception
            assertTrue(true);
         }

         // try an invalid rhythmyx session
         PSTestUtils.setSessionHeader(binding, "foobar");
         try
         {
            request = new SaveItemsRequest();
            request.setPSItem(toArray(items));
            binding.saveItems(request);
            assertFalse("Should have thrown exception", false);
         }
         catch (PSInvalidSessionFault e)
         {
            // expected exception
            assertTrue(true);
         }

         PSTestUtils.setSessionHeader(binding, session);

         // try with invalid items
         try
         {
            request = new SaveItemsRequest();
            request.setPSItem(null);
            binding.saveItems(request);
            assertFalse("Should have thrown exception", false);
         }
         catch (PSContractViolationFault e)
         {
            // expected exception
            assertTrue(true);
         }

         // try with invalid items
         try
         {
            request = new SaveItemsRequest();
            request.setPSItem(new PSItem[0]);
            binding.saveItems(request);
            assertFalse("Should have thrown exception", false);
         }
         catch (PSContractViolationFault e)
         {
            // expected exception
            assertTrue(true);
         }

         int updateId = 0;
         int childRows = -1;

         // insert the new items with default fields only
         request = new SaveItemsRequest();
         request.setPSItem(toArray(items));
         long[] ids = saveItems(request, binding);
         assertTrue(ids != null && ids.length == items.size());
         items = validateSave(items, rows, childRows, true, binding);

         BinaryFiles binaryFile = BinaryFiles.LARGE;
         boolean attachBinaries = true;

         // populate all local fields and update the items
         populateTestFields(items, -1, updateId++, binaryFile, session,
            binding);
         request = new SaveItemsRequest();
         request.setPSItem(toArray(items));
         ids = saveItems(request, binding);
         assertTrue(ids != null && ids.length == items.size());
         items = validateSave(items, rows, childRows, attachBinaries, binding);

         // populate all children with 3 rows and update the items
         childRows = 3;
         populateTestFields(items, childRows, updateId++, binaryFile,
            session, binding);
         request = new SaveItemsRequest();
         request.setPSItem(toArray(items));
         ids = saveItems(request, binding);
         assertTrue(ids != null && ids.length == items.size());
         items = validateSave(items, rows, childRows, attachBinaries, binding);

         // add 1 row to all children and update the items
         childRows = 4;
         populateTestFields(items, childRows, updateId++, binaryFile,
            session, binding);
         request = new SaveItemsRequest();
         request.setPSItem(toArray(items));
         ids = saveItems(request, binding);
         assertTrue(ids != null && ids.length == items.size());
         items = validateSave(items, rows, childRows, attachBinaries, binding);

         // remove 1 row from all children and update the items
         childRows = 3;
         populateTestFields(items, childRows, updateId++, binaryFile,
            session, binding);
         request = new SaveItemsRequest();
         request.setPSItem(toArray(items));
         ids = saveItems(request, binding);
         assertTrue(ids != null && ids.length == items.size());
         items = validateSave(items, rows, childRows, attachBinaries, binding);

         /* Comment out the following to avoid timeout failure from time to time
          *
         // add all items to these folders
         String[] folderPaths = { "//Folders/Tests/SaveItems_1"};
         setupFolderPaths(items, folderPaths);
         request = new SaveItemsRequest();
         request.setPSItem(toArray(items));
         ids = saveItems(request, binding);
         cleanupFolders = true;
         assertTrue(ids != null && ids.length == items.size());
         items = validateSave(items, folderPaths, true, binding);

         // add all items to an additional folder

        String[] folderPaths2 = { "//Folders/Tests/SaveItems_1",
            "//Folders/Tests/SaveItems_2"};
         setupFolderPaths(items, folderPaths2);
         request = new SaveItemsRequest();
         request.setPSItem(toArray(items));
         ids = saveItems(request, binding);
         assertTrue(ids != null && ids.length == items.size());

         // remove all items from a folder
         String[] folderPaths3 = { "//Folders/Tests/SaveItems_1"};
         setupFolderPaths(items, folderPaths3);
         request = new SaveItemsRequest();
         request.setPSItem(toArray(items));
         ids = saveItems(request, binding);
         assertTrue(ids != null && ids.length == items.size());
      */

         // try to save an item with unknown field
         PSItem item0 = items.get(0);
         PSField[] originalFields = item0.getFields();
         String unknownFieldName = "someField";
         try
         {
            List<PSField> fields = new ArrayList<PSField>();
            PSField someField = new PSField();
            someField.setName(unknownFieldName);
            someField.setDimension(PSFieldDimension.optional);
            fields.add(someField);
            item0.setFields(fields.toArray(new PSField[fields.size()]));
            SaveItemsRequest saveRequest = new SaveItemsRequest();
            saveRequest.setPSItem(new PSItem[] {item0});
            binding.saveItems(saveRequest);
            assertFalse("Should have thrown exception", false);
         }
         catch (RemoteException e)
         {
            PSLegacyGuid guid = new PSLegacyGuid(item0.getId());
            ConversionException expectedException = new ConversionException(
               PSWebserviceErrors.createErrorMessage(
                  IPSWebserviceErrors.UNKNOWN_FIELD_NAME, guid.toString(),
                  unknownFieldName));
            assertTrue(e.getLocalizedMessage().indexOf(
               expectedException.getLocalizedMessage()) >= 0);
         }
         item0.setFields(originalFields);

         // save and checkin all items
         request = new SaveItemsRequest();
         request.setPSItem(toArray(items));
         request.setCheckin(true);
         ids = saveItems(request, binding);
         assertTrue(ids != null && ids.length == items.size());

         // try to save items not checked out
         try
         {
            request = new SaveItemsRequest();
            request.setPSItem(toArray(items));
            request.setCheckin(true);
            ids = saveItems(request, binding);
            assertFalse("Should have thrown exception", false);
         }
         catch (Exception e)
         {
            // expected exception
            assertTrue(true);
         }
      }
      catch (PSInvalidSessionFault e)
      {
		 e.printStackTrace();
         throw new junit.framework.AssertionFailedError("Invalid session: " + e);
      }
      catch (PSNotAuthorizedFault e)
      {
		 e.printStackTrace();
         throw new junit.framework.AssertionFailedError("Not authorized: " + e);
      }
      catch (PSUnknownContentTypeFault e)
      {
		 e.printStackTrace();
         throw new junit.framework.AssertionFailedError(
            "Unknown content type: " + e);
      }
      catch (Throwable e)
      {
		 e.printStackTrace();
         throw new junit.framework.AssertionFailedError("Unexpected error: "
            + e);
      }
      finally
      {
         // cleanup
         cleanUpContentType(session, contentType, acls);
         if (cleanupFolders)
            cleanUpFolders("//Folders/Tests", session);
      }
   }

   /**
    * Add the supplied attachment to the provided stub and return the
    * attachment id.
    *
    * @param binding the stub to which to add the attachement, assumed not
    *    <code>null</code>.
    * @param attachment the file to attach, assumed not <code>null</code>.
    * @return the attachment id needed on the server to map this to the
    *    appropriate field, never <code>null</code> or empty.
    */
   private String addAttachment(ContentSOAPStub binding, File attachment)
   {
      DataHandler handler = new DataHandler(new FileDataSource(attachment));

      AttachmentPart part = new AttachmentPart(handler);
      binding.addAttachment(part);

      return part.getContentId();
   }

   /**
    * Setup the folder paths for all supplied items. All supplied folders
    * will be added, updated or removed. Supply <code>null</code> to ignore
    * folder processing. Supply empty to remove all folders.
    *
    * @param items the items for which to seup the folder paths, assumed
    *    not <code>null</code>.
    * @param folderPaths the folder paths, may be <code>null</code> or empty.
    */
   private void setupFolderPaths(List<PSItem> items, String[] folderPaths)
   {
      PSItemFolders[] folders = null;
      if (folderPaths != null)
      {
         folders = new PSItemFolders[folderPaths.length];
         for (int i = 0; i < folderPaths.length; i++)
            folders[i] = new PSItemFolders(folderPaths[i]);
      }

      for (PSItem item : items)
         item.setFolders(folders);
   }

   /**
    * Reloads all supplied items with everything included. Then this verifies
    * that the reloaded items contain only the suppliedd folder paths.
    *
    * @param items the items to reload and verify, assumed not
    *    <code>null</code>.
    * @param paths the expected patrhs to verify all items for, may be
    *    <code>null</code> or empty.
    * @param attachedBinaries <code>true</code> to validate binaries as
    *    attachments, <code>false</code> to validate them base64 encoded.
    * @param binding the stub used, assumed not <code>null</code>.
    * @return a list with all reloaded items, never <code>null</code>, may be
    *    empty.
    * @throws RemoteException for any error.
    */
   @Test
   private List<PSItem> validateSave(List<PSItem> items, String[] paths,
      boolean attachedBinaries, ContentSOAPStub binding) throws RemoteException
   {
      // reload the items
      LoadItemsRequest loadRequest = new LoadItemsRequest(toItemIds(items),
         null, true, attachedBinaries, true, null, true, null, true);
      List<PSItem> loadedItems = Arrays.asList(binding.loadItems(loadRequest));

      List<String> pathList = null;
      if (paths != null)
         pathList = Arrays.asList(paths);
      for (PSItem item : loadedItems)
      {
         PSItemFolders[] folders = item.getFolders();
         if (pathList != null)
         {
            if (folders == null)
               assertTrue(pathList.isEmpty());
            else
            {
               assertTrue(pathList.size() == folders.length);
               for (PSItemFolders folder : folders)
                  assertTrue(pathList.contains(folder.getPath()));
            }
         }
         else
            assertTrue(folders == null || folders.length == 0);
      }

      return loadedItems;
   }

   /**
    * Reloads all supplied items with everything included. Then the supplied
    * items are verified against the reloaded ones to make sure that a previous
    * save was successful.
    *
    * @param items the items to reload and validate, assumed not
    *    <code>null</code>, may be empty.
    * @param rows the number of expected items, -1 to ignore this parameter.
    * @param childRows the number of expected child entries, -1 to ignore
    *    this parameter.
    * @param attachedBinaries <code>true</code> to validate binaries as
    *    attachments, <code>false</code> to validate them base64 encoded.
    * @param binding the stub used, assumed not <code>null</code>.
    * @return a list with all reloaded items, never <code>null</code>, may be
    *    empty.
    * @throws RemoteException for any error.
    */
   @Test
   private List<PSItem> validateSave(List<PSItem> items, int rows,
      int childRows, boolean attachedBinaries, ContentSOAPStub binding)
      throws RemoteException
   {
      // reload the items
      LoadItemsRequest loadRequest = new LoadItemsRequest(toItemIds(items),
         null, true, attachedBinaries, true, null, true, null, true);
      List<PSItem> loadedItems = Arrays.asList(binding.loadItems(loadRequest));

      // validate parent row count
      if (rows > 0)
         assertTrue(loadedItems.size() == rows);

      // validate child row count
      for (PSItem item : loadedItems)
      {
         if (childRows > 0)
         {
            PSItemChildren[] children = item.getChildren();
            for (PSItemChildren child : children)
               assertTrue(child.getPSChildEntry().length == childRows);
         }
      }

      // validate parent and child content
      assertTrue(compareTestItems(items, loadedItems, binding));

      return loadedItems;
   }

   /**
    * Compares the two item lists. If the size of the supplied lists is
    * different, the only the number of items for the size of the smaller list
    * will be compared.
    *
    * @param a the first list to compare, assumed not <code>null</code>.
    * @param b the second list to compare, assumed not <code>null</code> and
    *    of the same order as the first list.
    * @param binding the stub used, assumed not <code>null</code>.
    * @return <code>true</code> if all local fields and all childrens rows
    *    are equal, <code>false</code> otherwise.
    */
   private boolean compareTestItems(List<PSItem> a, List<PSItem> b,
      ContentSOAPStub binding)
   {
      int itemCount = a.size() > b.size() ? b.size() : a.size();

      /*
       * IMPORTANT: getAttachments clears all attachments so that they
       * can only be retrieved once!
       */
      Object[] attachments = binding.getAttachments();

      for (int i = 0; i < itemCount; i++)
      {
         PSItem itemA = a.get(i);
         PSItem itemB = b.get(i);

         if (compareObjects(itemA, itemB) && itemA != null)
         {
            // compare fields
            for (int j = 0; j < itemA.getFields().length; j++)
            {
               PSField fieldA = itemA.getFields()[j];
               PSField fieldB = itemB.getFields()[j];

               // skip the hibernate version field
               if (fieldA.getName().equals("sys_hibernateVersion"))
                  continue;

               if (compareObjects(fieldA, fieldB))
               {
                  if (!compareTestFields(fieldA, fieldB, attachments))
                     return false;
               }
            }

            // compare children
            for (int j = 0; j < itemA.getChildren().length; j++)
            {
               PSItemChildren childA = itemA.getChildren(j);
               PSItemChildren childB = itemB.getChildren(j);

               if (compareObjects(childA, childB) && childA != null)
               {
                  PSChildEntry[] childEntriesA = childA.getPSChildEntry();
                  PSChildEntry[] childEntriesB = childB.getPSChildEntry();

                  if (compareObjects(childEntriesA, childEntriesB)
                     && childEntriesA != null)
                  {
                     // only compare fields for the smaller or the 2 arrays
                     int childEntryCount = childEntriesA.length > childEntriesB.length ? childEntriesB.length
                        : childEntriesA.length;
                     for (int n = 0; n < childEntryCount; n++)
                     {
                        PSField[] fieldsA = childEntriesA[n].getPSField();
                        PSField[] fieldsB = childEntriesB[n].getPSField();

                        for (int m = 0; m < fieldsA.length; m++)
                        {
                           if (!compareTestFields(fieldsA[m], fieldsB[m],
                              attachments))
                              return false;
                        }
                     }
                  }
               }
            }
         }
      }

      return true;
   }

   /**
    * Compares the contents of the two supplied values.
    *
    * @param a value a to compare, assumed not <code>null</code>.
    * @param b value b to compare, assumed not <code>null</code> and that the
    *    value arrays of both fields have the same length.
    * @param attachments an array with all response attachments, assumed not
    *    <code>null</code>, may be empty.
    * @return <code>true</code> if the contents or the 2 fields are equal,
    *    <code>false</code> otherwise.
    */
   private boolean compareTestFields(PSField a, PSField b, Object[] attachments)
   {
      PSFieldValue[] valuesA = a.getPSFieldValue();
      PSFieldValue[] valuesB = b.getPSFieldValue();

      if (compareObjects(valuesA, valuesB) && valuesA != null)
      {
         for (int i = 0; i < valuesA.length; i++)
         {
            if (!StringUtils.isBlank(valuesA[i].getAttachmentId()))
            {
               // attached binary
               if (StringUtils.isBlank(valuesB[i].getAttachmentId()))
                  return false;

               boolean found = false;
               for (Object attachment : attachments)
               {
                  AttachmentPart part = (AttachmentPart) attachment;

                  if (part.getContentId().equals(valuesB[i].getAttachmentId()))
                  {
                     try
                     {
                        int size = part.getSize();
                        if (size == 0)
                           return false;
                     }
                     catch (SOAPException e)
                     {
                        return false;
                     }
                     found = true;
                     break;
                  }
               }
               if (!found)
                  return false;
            }
            else
            {
               // non-binary or base64 encoded binary
               if (!StringUtils.equals(valuesA[i].getRawData(), valuesB[i]
                  .getRawData()))
                  return false;
            }
         }
      }

      return true;
   }

   /**
    * Compares the two supplied objects to test if both are <code>null</code>
    * or not.
    *
    * @param a the first test object, may be <code>null</code>.
    * @param b the second test object, may be <code>null</code>.
    * @return <code>true</code> if both object are <code>null</code> or both
    *    are not <code>null</code>, <code>false</code> otherwise.
    */
   private boolean compareObjects(Object a, Object b)
   {
      return ((a == null && b == null) || (a != null && b != null));
   }

   /**
    * Test the item load service.
    *
    * @throws Exception for any error.
    */
   @Test
   public void testLoadContentItems() throws Exception
   {
      ContentSOAPStub binding = getBinding(6000000);

      String session = m_session;

      PSContentType contentType = null;
      List<PSAclImpl> acls = new ArrayList<PSAclImpl>();
      List<PSItem> items = new ArrayList<PSItem>();

      try
      {
         // create the test content types
         contentType = createContentType("test3", session, acls);

         // create 3 test items for the new content type
         PSTestUtils.setSessionHeader(binding, session);
         items.addAll(createTestItems(contentType.getName(), 3, true, true,
            true, "//Folders/TestLoadContentItems", session, binding));
         long[] ids = toItemIds(items);

         // populate all local fields and update the items
         int updateId = 0;
         int childRows = -1;
         BinaryFiles binaryFile = BinaryFiles.SMALL;
         populateTestFields(items, childRows, updateId++, binaryFile, session,
            binding);
         SaveItemsRequest saveRequest = new SaveItemsRequest();
         saveRequest.setPSItem(toArray(items));
         ids = saveItems(saveRequest, binding);
         assertTrue(ids != null && ids.length == items.size());

         // populate all children with 3 rows and update the items
         childRows = 3;
         populateTestFields(items, childRows, updateId++, binaryFile, session,
            binding);
         saveRequest = new SaveItemsRequest();
         saveRequest.setPSItem(toArray(items));
         ids = saveItems(saveRequest, binding);
         assertTrue(ids.length == items.size());

         LoadItemsRequest request = null;

         // try load without rhythmyx session
         binding.clearHeaders();
         try
         {
            request = new LoadItemsRequest();
            request.setId(ids);
            binding.loadItems(request);
            assertFalse("Should have thrown exception", false);
         }
         catch (PSInvalidSessionFault e)
         {
            // expected exception
            assertTrue(true);
         }

         // try load with an invalid rhythmyx session
         PSTestUtils.setSessionHeader(binding, "foobar");
         try
         {
            request = new LoadItemsRequest();
            request.setId(ids);
            binding.loadItems(request);
            assertFalse("Should have thrown exception", false);
         }
         catch (PSInvalidSessionFault e)
         {
            // expected exception
            assertTrue(true);
         }

         PSTestUtils.setSessionHeader(binding, session);

         // try load with invalid ids
         try
         {
            request = new LoadItemsRequest();
            request.setId(null);
            binding.loadItems(request);
            assertFalse("Should have thrown exception", false);
         }
         catch (PSContractViolationFault e)
         {
            // expected exception
            assertTrue(true);
         }

         // try load with invalid ids
         try
         {
            request = new LoadItemsRequest();
            request.setId(new long[0]);
            binding.loadItems(request);
            assertFalse("Should have thrown exception", false);
         }
         catch (PSContractViolationFault e)
         {
            // expected exception
            assertTrue(true);
         }

         // try to load items with inlvalid ids
         try
         {
            long[] invalidIds = new long[ids.length];
            for (int i=0; i<ids.length; i++)
               invalidIds[i] = ids[i];
            invalidIds[ids.length-1] = invalidIds[ids.length-1] + 10000;

            request = new LoadItemsRequest();
            request.setId(invalidIds);
            binding.loadItems(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSErrorResultsFault e)
         {
            verifyErrorResultsFault(e, ids.length-1, PSItem.class.getName());
         }

         // load all test items without binaries, children, related and folders
         request = new LoadItemsRequest();
         request.setId(ids);
         PSItem[] loadedItems = binding.loadItems(request);
         assertTrue(loadedItems != null && loadedItems.length == ids.length);
         validateBinaryField(loadedItems, "field_2", 
                                          false, BinaryFiles.SKIP, binding);
         validateRequestParts(loadedItems, false, false, false);

         // load all test items with binaries base64 encoded but without
         // children, related and folders
         request = new LoadItemsRequest();
         request.setId(ids);
         request.setIncludeBinary(true);
         loadedItems = binding.loadItems(request);
         assertTrue(loadedItems != null && loadedItems.length == ids.length);
         validateBinaryField(loadedItems, "field_2", 
                                          true, BinaryFiles.SKIP, binding);
         validateRequestParts(loadedItems, false, false, false);

         // load all test items with binaries attached but without
         // children, related and folders
         request = new LoadItemsRequest();
         request.setId(ids);
         request.setIncludeBinary(true);
         request.setAttachBinaries(true);
         loadedItems = binding.loadItems(request);
         assertTrue(loadedItems != null && loadedItems.length == ids.length);
         validateBinaryField(loadedItems, "field_2", 
                             true, BinaryFiles.SMALL, binding);
         validateRequestParts(loadedItems, false, false, false);

         // load all test items without binaries, related and folders but with
         // children
         request = new LoadItemsRequest();
         request.setId(ids);
         request.setIncludeChildren(true);
         loadedItems = binding.loadItems(request);
         assertTrue(loadedItems != null && loadedItems.length == ids.length);
         validateBinaryField(loadedItems, "field_2", 
                             false, BinaryFiles.SKIP, binding);
         validateRequestParts(loadedItems, true, false, false);

         // load all test items without binaries, children and folders but with
         // related
         request = new LoadItemsRequest();
         request.setId(ids);
         request.setIncludeRelated(true);
         loadedItems = binding.loadItems(request);
         assertTrue(loadedItems != null && loadedItems.length == ids.length);
         validateBinaryField(loadedItems, "field_2", 
                             false, BinaryFiles.SKIP, binding);
         validateRequestParts(loadedItems, false, true, false);

         // load all test items without binaries, children and related but with
         // folders
         request = new LoadItemsRequest();
         request.setId(ids);
         request.setIncludeFolderPath(true);
         loadedItems = binding.loadItems(request);
         assertTrue(loadedItems != null && loadedItems.length == ids.length);
         validateBinaryField(loadedItems, "field_2", 
                             false, BinaryFiles.SKIP, binding);
         validateRequestParts(loadedItems, false, false, true);
      }
      catch (PSInvalidSessionFault e)
      {
         throw new junit.framework.AssertionFailedError("Invalid session: " + e);
      }
      catch (PSNotAuthorizedFault e)
      {
         throw new junit.framework.AssertionFailedError("Not authorized: " + e);
      }
      catch (PSUnknownContentTypeFault e)
      {
         throw new junit.framework.AssertionFailedError(
            "Unknown content type: " + e);
      }
      catch (RemoteException e)
      {
         throw new junit.framework.AssertionFailedError("Unexpected error: "
            + e);
      }
      finally
      {
         // cleanup
         cleanUpContentType(session, contentType, acls);
      }
   }

   /**
    * Validate if the supplied items contain / miss the requested / not
    * requested parts.
    *
    * @param items the items to verify, assumed not <code>null</code>.
    * @param includeChildren <code>true</code> if children were requested,
    *    <code>false</code> otherwise.
    * @param includeSlots <code>true</code> if slots were requested,
    *    <code>false</code> otherwise.
    * @param includeFolders <code>true</code> if folders were requested,
    *    <code>false</code> otherwise.
    */
   @Test
   private void validateRequestParts(PSItem[] items, boolean includeChildren,
      boolean includeSlots, boolean includeFolders)
   {
      for (PSItem item : items)
      {
         if (includeChildren)
            assertTrue("children requested but not returned",
               item.getChildren() != null);
         else
            assertTrue("children not requested but returned",
               item.getChildren() == null);

         if (includeSlots)
            assertTrue("slots requested but not returned",
               item.getSlots() == null);
         else
            assertTrue("slots not requested but returned",
               item.getSlots() == null);

         if (includeFolders)
            assertTrue("folders requested but not returned",
               item.getFolders() != null);
         else
            assertTrue("folders not requested but returned",
               item.getFolders() == null);
      }
   }

   /**
    * Validate that the identified binary field contains the expected data.
    *
    * @param items the items for which to validate the identified binary field,
    *    assumed not <code>null</code>.
    * @param fieldName the name of the binary field to validate, assumed not
    *    <code>null</code> or empty.
    * @param loaded <code>true</code> if the identified binary field must be
    *    loaded, <code>false</code> otherwise.
    * @param binaryFile <code>LARGE</code> or <code>SMALL</code> if the 
    *    identified binary field is expected as attachment, <code>SKIP</code> 
    *    if it is expected base64 encoded.
    * @param binding the binding used, assumed not <code>null</code>.
    * @throws Exception for any error.
    */
   @Test
   private void validateBinaryField(PSItem[] items, String fieldName,
      boolean loaded, BinaryFiles binaryFile, ContentSOAPStub binding)
      throws Exception
   {
      /*
       * IMPORTANT: getAttachments clears all attachments so
       * that they can only be retrieved once!
       */
      Object[] attachments = binding.getAttachments();

      int itemIndex = 0;
      for (PSItem item : items)
      {
         for (PSField field : item.getFields())
         {
            if (!field.getName().equals(fieldName))
               continue;

            if (!loaded)
            {
               // validate that binary field is not loaded
               assertTrue(field.getPSFieldValue() == null);
            }
            else
            {
               for (PSFieldValue value : field.getPSFieldValue())
               {
                  if (binaryFile != BinaryFiles.SKIP)
                  {
                     // validate that binary field is attacched
                     assertTrue(value.getRawData() == null);
                     assertTrue(value.getAttachmentId() != null);

                     boolean found = false;
                     for (Object attachment : attachments)
                     {
                        AttachmentPart part = (AttachmentPart) attachment;
                        if (part.getContentId().equals(value.getAttachmentId()))
                        {
                           /*
                            * Compare the loaded binary attachment with the
                            * originally attached file.
                            */
                           InputStreamReader contentReader =
                              new InputStreamReader(
                                 (InputStream) part.getContent());
                           FileReader originalReader = new FileReader(
                              getAttachmentFile(itemIndex, binaryFile));

                           assertTrue(IOTools.compareReaders(contentReader,
                              originalReader));

                           found = true;
                           break;
                        }
                     }
                     assertTrue(found);
                  }
                  else
                  {
                     // validate that binary field is base64 encoded
                     assertTrue(value.getRawData() != null);
                     assertTrue(value.getAttachmentId() == null);
                  }
               }
            }
         }

         itemIndex++;
      }
   }

   /**
    * Get the attachment file based on the item index.
    * 
    * @param index the item index for which to get the attachment file.
    * @param binaryFile <code>LARGE</code> or <code>SMALL</code> if the
    *    identified binary field is expected as attachment, <code>SKIP</code>
    *    if it is expected base64 encoded.
    * @return the attachment file, never <code>null</code>.
    */
   private File getAttachmentFile(int index, BinaryFiles binaryFile)
   {
      String directory = RESOURCE_BASE + "attachments/";

      if ((index + 1) % 2 == 0)
         return new File(directory, "attachment_0.jpg");
      else if ((index + 1) % 3 == 0)
      {
         if(binaryFile == BinaryFiles.SMALL)
         {
            return new File(directory, "attachment_1.doc");            
         }
         else
         {
            return new File(directory, "attachment_large_1.doc");
         }
      }

      return new File(directory, "attachment_2.pdf");
   }

   /**
    * Test the item view service.
    *
    * @throws Exception for any error.
    */
   @Test
   public void testViewContentItems() throws Exception
   {
      ContentSOAPStub binding = getBinding(6000000);

      String session = m_session;

      PSContentType contentType = null;
      List<PSAclImpl> acls = new ArrayList<PSAclImpl>();
      List<PSItem> items = new ArrayList<PSItem>();

      int itemCount = 3;
      int revisionCount = 3;

      try
      {
         // cleanup the content type which may be left from previous test run
         cleanUpContentType(session, "test3");

         // create the test content type
         contentType = createContentType("test3", session, acls);

         // create test items for the new content type
         PSTestUtils.setSessionHeader(binding, session);
         items.addAll(createTestItems(contentType.getName(), itemCount, true,
            true, true, null, session, binding));
         long[] ids = toItemIds(items);

         // create 3 revisions
         createItemRevisions(items, revisionCount, binding);

         // verify that all revisions are created
         PSRevisions[] revisions = binding.findRevisions(ids);
         assertTrue(revisions.length == itemCount);
         for (PSRevisions itemRevisions : revisions)
            assertTrue(itemRevisions.getRevisions().length == revisionCount);

         ViewItemsRequest request = null;

         // try without rhythmyx session
         binding.clearHeaders();
         try
         {
            request = new ViewItemsRequest();
            request.setId(ids);
            binding.viewItems(request);
            assertFalse("Should have thrown exception", false);
         }
         catch (PSInvalidSessionFault e)
         {
            // expected exception
            assertTrue(true);
         }

         // try with an invalid rhythmyx session
         PSTestUtils.setSessionHeader(binding, "foobar");
         try
         {
            request = new ViewItemsRequest();
            request.setId(ids);
            binding.viewItems(request);
            assertFalse("Should have thrown exception", false);
         }
         catch (PSInvalidSessionFault e)
         {
            // expected exception
            assertTrue(true);
         }

         PSTestUtils.setSessionHeader(binding, session);

         // try with invalid ids
         try
         {
            request = new ViewItemsRequest();
            request.setId(null);
            binding.viewItems(request);
            assertFalse("Should have thrown exception", false);
         }
         catch (PSContractViolationFault e)
         {
            // expected exception
            assertTrue(true);
         }

         // try with invalid ids
         try
         {
            request = new ViewItemsRequest();
            request.setId(new long[0]);
            binding.viewItems(request);
            assertFalse("Should have thrown exception", false);
         }
         catch (PSContractViolationFault e)
         {
            // expected exception
            assertTrue(true);
         }

         // try to view items with inlvalid ids
         try
         {
            long[] invalidIds = new long[ids.length];
            for (int i=0; i<ids.length; i++)
               invalidIds[i] = ids[i];
            invalidIds[ids.length-1] = invalidIds[ids.length-1] + 10000;

            request = new ViewItemsRequest();
            request.setId(invalidIds);
            binding.viewItems(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSErrorResultsFault e)
         {
            verifyErrorResultsFault(e, ids.length-1, PSItem.class.getName());
         }

         // view all current test items
         request = new ViewItemsRequest();
         request.setId(ids);
         PSItem[] viewItems = binding.viewItems(request);
         assertTrue(viewItems != null && viewItems.length == ids.length);

         // get the 2nd revision for all items
         int viewRevision = 2;
         long[] revisionIds = new long[revisions.length];
         int index = 0;
         for (PSRevisions itemRevisions : revisions)
            revisionIds[index++] = itemRevisions
               .getRevisions()[viewRevision - 1].getId();
         request = new ViewItemsRequest();
         request.setId(revisionIds);
         viewItems = binding.viewItems(request);
         assertTrue(viewItems != null && viewItems.length == ids.length);
         verifyRevisions(Arrays.asList(viewItems), viewRevision);
      }
      catch (PSInvalidSessionFault e)
      {
         throw new junit.framework.AssertionFailedError("Invalid session: " + e);
      }
      catch (PSNotAuthorizedFault e)
      {
         throw new junit.framework.AssertionFailedError("Not authorized: " + e);
      }
      catch (PSUnknownContentTypeFault e)
      {
         throw new junit.framework.AssertionFailedError(
            "Unknown content type: " + e);
      }
      catch (RemoteException e)
      {
         throw new junit.framework.AssertionFailedError("Unexpected error: "
            + e);
      }
      finally
      {
         // cleanup
         cleanUpContentType(session, contentType, acls);
      }
   }

   /**
    * Verifies that the supplied items are all for the revision supplied.
    * Assumes that the <code>field_1</code> fiels all represent the items
    * revosion.
    *
    * @param items all items to verify, assumed not <code>null</code>, may
    *    be empty.
    * @param revision the revision to verify.
    * @throws Exception for any error.
    */
   @Test
   private void verifyRevisions(List<PSItem> items, int revision)
      throws Exception
   {
      for (PSItem item : items)
      {
         PSField[] fields = item.getFields();
         for (PSField field : fields)
         {
            if (field.getName().equalsIgnoreCase("field_1"))
            {
               PSFieldValue[] values = field.getPSFieldValue();
               for (PSFieldValue value : values)
                  assertTrue(value.getRawData().equalsIgnoreCase(
                     String.valueOf(revision)));
            }
         }
      }
   }

   /**
    * Test the item delete service.
    *
    * @throws Exception for any error.
    */
   @Test
   public void testDeleteContentItems() throws Exception
   {
      ContentSOAPStub binding = getBinding(6000000);

      String session = m_session;

      PSContentType contentType = null;
      List<PSAclImpl> acls = new ArrayList<PSAclImpl>();
      List<PSItem> items = new ArrayList<PSItem>();

      try
      {
         // create the test content type
         contentType = createContentType("test3", session, acls);

         // create 3 test items for the new content type
         PSTestUtils.setSessionHeader(binding, session);
         items.addAll(createTestItems(contentType.getName(), 3, true, true,
            false, null, session, binding));
         long[] ids = toItemIds(items);

         long[] request = null;

         // try without rhythmyx session
         binding.clearHeaders();
         try
         {
            request = ids;
            binding.deleteItems(request);
            assertFalse("Should have thrown exception", false);
         }
         catch (PSInvalidSessionFault e)
         {
            // expected exception
            assertTrue(true);
         }

         // try with an invalid rhythmyx session
         PSTestUtils.setSessionHeader(binding, "foobar");
         try
         {
            request = ids;
            binding.deleteItems(request);
            assertFalse("Should have thrown exception", false);
         }
         catch (PSInvalidSessionFault e)
         {
            // expected exception
            assertTrue(true);
         }

         PSTestUtils.setSessionHeader(binding, session);

         // try with invalid ids
         try
         {
            request = null;
            binding.deleteItems(request);
            assertFalse("Should have thrown exception", false);
         }
         catch (PSContractViolationFault e)
         {
            // expected exception
            assertTrue(true);
         }

         // try with invalid ids
         try
         {
            request = new long[0];
            binding.deleteItems(request);
            assertFalse("Should have thrown exception", false);
         }
         catch (PSContractViolationFault e)
         {
            // expected exception
            assertTrue(true);
         }

         // veify that the items are there
         LoadItemsRequest loadRequest = new LoadItemsRequest();
         loadRequest.setId(ids);
         PSItem[] loadedItems = binding.loadItems(loadRequest);
         assertTrue(loadedItems != null && loadedItems.length == ids.length);

         // delete the created items
         request = ids;
         binding.deleteItems(request);

         // veify that the items were deleted
         try
         {
            loadRequest = new LoadItemsRequest();
            loadRequest.setId(ids);
            loadedItems = binding.loadItems(loadRequest);
            assertFalse("Should have thrown exception", false);
         }
         catch (PSErrorResultsFault e)
         {
            PSErrorResultsFaultServiceCall[] calls = e.getServiceCall();
            for (int i = 0; i < ids.length; i++)
            {
               assertTrue(calls[i].getResult() == null);
               assertTrue(calls[i].getError() != null);
            }
         }
      }
      catch (PSInvalidSessionFault e)
      {
         throw new junit.framework.AssertionFailedError("Invalid session: " + e);
      }
      catch (PSNotAuthorizedFault e)
      {
         throw new junit.framework.AssertionFailedError("Not authorized: " + e);
      }
      catch (PSUnknownContentTypeFault e)
      {
         throw new junit.framework.AssertionFailedError(
            "Unknown content type: " + e);
      }
      catch (RemoteException e)
      {
         throw new junit.framework.AssertionFailedError("Unexpected error: "
            + e);
      }
      finally
      {
         // cleanup
         cleanUpContentType(session, contentType, acls);
      }
   }

   /**
    * Test the find items webservice.
    *
    * @throws Exception for any error.
    */
   @Test
   public void testFindContentItems() throws Exception
   {
      ContentSOAPStub binding = getBinding(null);

      String session = m_session;

      PSContentType contentType = null;
      List<PSAclImpl> acls = new ArrayList<PSAclImpl>();
      List<PSItem> items = new ArrayList<PSItem>();

      try
      {
         // create the test content type
         contentType = createContentType("test3", session, acls);

         // create 3 test items for the new content type
         PSTestUtils.setSessionHeader(binding, session);
         items.addAll(createTestItems(contentType.getName(), 3, true, true,
            false, "//folders/testFindContentItems", session, binding));

         FindItemsRequest request = null;

         PSSearch search = new PSSearch();
         search.setPSSearchParams(new PSSearchParams());

         // try without rhythmyx session
         binding.clearHeaders();
         try
         {
            request = new FindItemsRequest();
            request.setPSSearch(search);
            binding.findItems(request);
            assertFalse("Should have thrown exception", false);
         }
         catch (PSInvalidSessionFault e)
         {
            // expected exception
            assertTrue(true);
         }

         // try with an invalid rhythmyx session
         PSTestUtils.setSessionHeader(binding, "foobar");
         try
         {
            request = new FindItemsRequest();
            request.setPSSearch(search);
            binding.findItems(request);
            assertFalse("Should have thrown exception", false);
         }
         catch (PSInvalidSessionFault e)
         {
            // expected exception
            assertTrue(true);
         }

         PSTestUtils.setSessionHeader(binding, session);

         // try with invalid ids
         try
         {
            request = new FindItemsRequest();
            request.setPSSearch(null);
            binding.findItems(request);
            assertFalse("Should have thrown exception", false);
         }
         catch (RemoteException e)
         {
            // expected exception
            assertTrue(true);
         }

         // find by sys_contentid = 335
         PSSearchParams searchParams = new PSSearchParams();
         PSSearchField field = new PSSearchField();
         field.setName("sys_contentid");
         field.setValue("335");
         searchParams.setParameter(new PSSearchField[] {field});

         // ask for specific fields back
         PSSearchResultField resfields[] = {
            new PSSearchResultField("sys_workflowname"),
            new PSSearchResultField("sys_contenttypename"),
            new PSSearchResultField("sys_communityname")
            };
         searchParams.setSearchResults(resfields);

         search = new PSSearch();
         search.setUseExternalSearchEngine(false);
         search.setPSSearchParams(searchParams);
         request = new FindItemsRequest();
         request.setPSSearch(search);
         PSSearchResults[] results = binding.findItems(request);
         assertTrue(results.length == 1);

         // validate the requested fields were returned with values
         PSSearchResultsFields[] retFields = results[0].getFields();
         assertTrue(retFields.length > 0);
         Map<String, PSSearchResultsFields> fieldMap =
            new HashMap<String, PSSearchResultsFields>();
         for (PSSearchResultsFields retField : retFields)
         {
            assertNotNull(retField.get_value());
            fieldMap.put(retField.getName(), retField);
         }

         for (PSSearchResultField resField : resfields)
         {
            PSSearchResultsFields retField = fieldMap.get(resField.getName());
            assertNotNull("expected field not found in results", retField);
            assertTrue("expected field value is empty", !StringUtils.isBlank(
               retField.get_value()));
         }

         // find test item 0
         PSSearchParamsTitle title = new PSSearchParamsTitle();
         title.setValue("item_0.field_0");
         searchParams = new PSSearchParams();
         searchParams.setContentType(contentType.getName());
         searchParams.setTitle(title);
         search = new PSSearch();
         search.setUseExternalSearchEngine(false);
         search.setPSSearchParams(searchParams);
         request = new FindItemsRequest();
         request.setPSSearch(search);
         results = binding.findItems(request);
         assertTrue(results.length == 1);

         // find all items in folder //folders/testFindContentItems
         PSSearchParamsFolderFilter folderFilter =
            new PSSearchParamsFolderFilter();
         folderFilter.set_value("//folders/testFindContentItems");
         searchParams = new PSSearchParams();
         searchParams.setContentType(contentType.getName());
         searchParams.setFolderFilter(folderFilter);
         search = new PSSearch();
         search.setUseExternalSearchEngine(false);
         search.setPSSearchParams(searchParams);
         request = new FindItemsRequest();
         request.setPSSearch(search);
         results = binding.findItems(request);
         assertTrue(results.length == 3);
      }
      catch (PSInvalidSessionFault e)
      {
         throw new junit.framework.AssertionFailedError("Invalid session: " + e);
      }
      catch (PSNotAuthorizedFault e)
      {
         throw new junit.framework.AssertionFailedError("Not authorized: " + e);
      }
      catch (PSUnknownContentTypeFault e)
      {
         throw new junit.framework.AssertionFailedError(
            "Unknown content type: " + e);
      }
      catch (RemoteException e)
      {
         throw new junit.framework.AssertionFailedError("Unexpected error: "
            + e);
      }
      finally
      {
         // cleanup
         cleanUpContentType(session, contentType, acls);
      }
   }

   /**
    * Test the find items webservice.
    *
    * @throws Exception for any error.
    */
   public void testDeleteFolderItem() throws Exception
   {
      ContentSOAPStub binding = getBinding(null);
      PSTestUtils.setSessionHeader(binding, m_session);
      String session = m_session;

      PSContentType contentType = null;
      List<PSAclImpl> acls = new ArrayList<PSAclImpl>();
      List<PSItem> items = new ArrayList<PSItem>();

      try
      {
         // create the test content type
         contentType = createContentType("test3", session, acls);

         // create 3 test items for the new content type
         items.addAll(createTestItems(contentType.getName(), 1, true, true,
            false, null, session, binding));

         // create a new folder
         AddFolderRequest areq = new AddFolderRequest();
         areq.setPath("//Sites/EnterpriseInvestments/Files");
         areq.setName("testContentSQAPFolder_1_"
            + java.lang.System.currentTimeMillis());
         AddFolderResponse aresp = binding.addFolder(areq);
         PSFolder folder_1 = aresp.getPSFolder();

         areq.setName("testContentSQAPFolder_2_"
               + java.lang.System.currentTimeMillis());
         aresp = binding.addFolder(areq);
         PSFolder folder_2 = aresp.getPSFolder();

         // add folder children
         AddFolderChildrenRequest acreq = new AddFolderChildrenRequest();
         acreq.setParent(new FolderRef(folder_1.getId(), null));
         acreq.setChildIds(new long[] { items.get(0).getId() });
         binding.addFolderChildren(acreq);

         acreq.setParent(new FolderRef(folder_2.getId(), null));
         binding.addFolderChildren(acreq);

         DeleteFoldersRequest deleteRequest = new DeleteFoldersRequest();
         deleteRequest.setId(new long[]{folder_1.getId(), folder_2.getId()});
         deleteRequest.setPurgItems(true);
         binding.deleteFolders(deleteRequest);
      }
      finally
      {
         // cleanup
         cleanUpContentType(session, contentType, acls);
      }
   }

   /**
    * Test the newCopies service.
    *
    * @throws Exception for any error.
    */
   @Test
   public void testCreateNewCopies() throws Exception
   {
      ContentSOAPStub binding = getBinding(6000000);

      String session = m_session;

      PSContentType contentType = null;
      List<PSAclImpl> acls = new ArrayList<PSAclImpl>();
      List<PSItem> items = new ArrayList<PSItem>();

      try
      {
         // create the test content type
         contentType = createContentType("test3", session, acls);

         // create 3 test items for the new content type
         PSTestUtils.setSessionHeader(binding, session);
         items.addAll(createTestItems(contentType.getName(), 3, true, true,
            false, null, session, binding));

         long[] ids = toItemIds(items);
         String[] paths = { "//Folders/Tests/Copy_1", "//Folders/Tests/Copy_2",
            "//Folders/Tests/Copy_3" };
         String[] nullPaths = { null, null, null };
         String[] invalidPaths = { "//Folders/Tests/Copy_1",
            "//Folders/Tests/Copy_3" };
         String relationshipType = PSRelationshipConfig.TYPE_NEW_COPY;
         String[] onePaths = { "//Folders/Tests/Copy_One"};
         String[] one3Paths = { "//Folders/Tests/Copy_One",
               "//Folders/Tests/Copy_One", "//Folders/Tests/Copy_One"};

         NewCopiesRequest request = null;

         // try without rhythmyx session
         binding.clearHeaders();
         try
         {
            request = new NewCopiesRequest();
            request.setIds(ids);
            request.setPaths(paths);
            request.setType(relationshipType);
            request.setEnableRevisions(false);
            binding.newCopies(request);
            assertFalse("Should have thrown exception", false);
         }
         catch (PSInvalidSessionFault e)
         {
            // expected exception
            assertTrue(true);
         }

         // try with an invalid rhythmyx session
         PSTestUtils.setSessionHeader(binding, "foobar");
         try
         {
            request = new NewCopiesRequest();
            request.setIds(ids);
            request.setPaths(paths);
            request.setType(relationshipType);
            request.setEnableRevisions(false);
            binding.newCopies(request);
            assertFalse("Should have thrown exception", false);
         }
         catch (PSInvalidSessionFault e)
         {
            // expected exception
            assertTrue(true);
         }

         PSTestUtils.setSessionHeader(binding, session);

         // try with invalid ids
         try
         {
            request = new NewCopiesRequest();
            request.setIds(null);
            request.setPaths(paths);
            request.setType(relationshipType);
            request.setEnableRevisions(false);
            binding.newCopies(request);
            assertFalse("Should have thrown exception", false);
         }
         catch (PSContractViolationFault e)
         {
            // expected exception
            assertTrue(true);
         }

         // try with invalid ids
         try
         {
            request = new NewCopiesRequest();
            request.setIds(new long[0]);
            request.setPaths(paths);
            request.setType(relationshipType);
            request.setEnableRevisions(false);
            binding.newCopies(request);
            assertFalse("Should have thrown exception", false);
         }
         catch (PSContractViolationFault e)
         {
            // expected exception
            assertTrue(true);
         }

         // try with invalid paths = null
         try
         {
            request = new NewCopiesRequest();
            request.setIds(ids);
            request.setPaths(null);
            request.setType(relationshipType);
            request.setEnableRevisions(false);
            binding.newCopies(request);
            assertFalse("Should have thrown exception", false);
         }
         catch (PSContractViolationFault e)
         {
            // expected exception
            assertTrue(true);
         }

         // try with invalid paths = EMPTY
         try
         {
            request = new NewCopiesRequest();
            request.setIds(ids);
            request.setPaths(new String[0]);
            request.setType(relationshipType);
            request.setEnableRevisions(false);
            binding.newCopies(request);
            assertFalse("Should have thrown exception", false);
         }
         catch (PSContractViolationFault e)
         {
            // expected exception
            assertTrue(true);
         }

         // try with invalid paths, ids.length != paths.length
         try
         {
            request = new NewCopiesRequest();
            request.setIds(ids);
            request.setPaths(invalidPaths);
            request.setType(relationshipType);
            request.setEnableRevisions(false);
            binding.newCopies(request);
            assertFalse("Should have thrown exception", false);
         }
         catch (PSContractViolationFault e)
         {
            // expected exception
            assertTrue(true);
         }

         // try with invalid relationship type
         try
         {
            request = new NewCopiesRequest();
            request.setIds(ids);
            request.setPaths(invalidPaths);
            request.setType("sometype");
            request.setEnableRevisions(false);
            binding.newCopies(request);
            assertFalse("Should have thrown exception", false);
         }
         catch (PSContractViolationFault e)
         {
            // expected exception
            assertTrue(true);
         }

         // create new copies
         request = new NewCopiesRequest();
         request.setIds(ids);
         request.setPaths(paths);
         request.setType(relationshipType);
         request.setEnableRevisions(false);
         PSItem[] newCopies = binding.newCopies(request);
         assertTrue(newCopies != null && newCopies.length == ids.length);
         verifyRevisionLocks(newCopies, false);
         verifyFolders(newCopies, paths, false, binding);

         // create new copies and enable revisions
         request = new NewCopiesRequest();
         request.setIds(ids);
         request.setPaths(nullPaths);     // array of null 's
         request.setType(relationshipType);
         request.setEnableRevisions(true);
         newCopies = binding.newCopies(request);
         assertTrue(newCopies != null && newCopies.length == ids.length);
         verifyRevisionLocks(newCopies, true);
         verifyFolders(newCopies, paths, true, binding);

         // same as array of null's
         request.setPaths(new String[]{null});
         newCopies = binding.newCopies(request);
         assertTrue(newCopies != null && newCopies.length == ids.length);
         verifyFolders(newCopies, paths, true, binding);

         // array of the same path
         request.setPaths(onePaths);
         newCopies = binding.newCopies(request);
         assertTrue(newCopies != null && newCopies.length == ids.length);
         verifyFolders(newCopies, one3Paths, false, binding);
      }
      catch (PSInvalidSessionFault e)
      {
         throw new junit.framework.AssertionFailedError("Invalid session: " + e);
      }
      catch (PSNotAuthorizedFault e)
      {
         throw new junit.framework.AssertionFailedError("Not authorized: " + e);
      }
      catch (PSUnknownContentTypeFault e)
      {
         throw new junit.framework.AssertionFailedError(
            "Unknown content type: " + e);
      }
      catch (Throwable e)
      {
         e.printStackTrace();
         throw new junit.framework.AssertionFailedError("Unexpected error: "
            + e);
      }
      finally
      {
         // cleanup
         cleanUpContentType(session, contentType, acls);
         cleanUpFolders("//Folders/Tests", session);
      }
   }

   /**
    * Test the newPromotableVersions service.
    *
    * @throws Exception for any error.
    */
   @Test
   public void testCreateNewPromotableVersions() throws Exception
   {
      ContentSOAPStub binding = getBinding(6000000);

      String session = m_session;

      PSContentType contentType = null;
      List<PSAclImpl> acls = new ArrayList<PSAclImpl>();
      List<PSItem> items = new ArrayList<PSItem>();

      try
      {
         // create the test content type
         contentType = createContentType("test3", session, acls);

         // create 3 test items for the new content type
         PSTestUtils.setSessionHeader(binding, session);
         items.addAll(createTestItems(contentType.getName(), 3, true, true,
            false, null, session, binding));

         long[] ids = toItemIds(items);
         String[] paths = { "//Folders/Tests/NewPromotableVersion_1",
            "//Folders/Tests/NewPromotableVersion_2",
            "//Folders/Tests/NewPromotableVersion_3" };
         String[] nullPaths = { null, null, null };
         String[] invalidPaths = { "//Folders/Tests/NewPromotableVersion_1",
            "//Folders/Tests/NewPromotableVersion_3" };
         String relationshipType = PSRelationshipConfig.TYPE_PROMOTABLE_VERSION;
         String[] onePaths = {"//Folders/Tests/NewPromotableVersion_1"};
         String[] one3Paths = {"//Folders/Tests/NewPromotableVersion_1",
               "//Folders/Tests/NewPromotableVersion_1",
               "//Folders/Tests/NewPromotableVersion_1"};

         NewPromotableVersionsRequest request = null;

         // try without rhythmyx session
         binding.clearHeaders();
         try
         {
            request = new NewPromotableVersionsRequest();
            request.setIds(ids);
            request.setPaths(paths);
            request.setType(relationshipType);
            request.setEnableRevisions(false);
            binding.newPromotableVersions(request);
            assertFalse("Should have thrown exception", false);
         }
         catch (PSInvalidSessionFault e)
         {
            // expected exception
            assertTrue(true);
         }

         // try with an invalid rhythmyx session
         PSTestUtils.setSessionHeader(binding, "foobar");
         try
         {
            request = new NewPromotableVersionsRequest();
            request.setIds(ids);
            request.setPaths(paths);
            request.setType(relationshipType);
            request.setEnableRevisions(false);
            binding.newPromotableVersions(request);
            assertFalse("Should have thrown exception", false);
         }
         catch (PSInvalidSessionFault e)
         {
            // expected exception
            assertTrue(true);
         }

         PSTestUtils.setSessionHeader(binding, session);

         // try with invalid ids
         try
         {
            request = new NewPromotableVersionsRequest();
            request.setIds(null);
            request.setPaths(paths);
            request.setType(relationshipType);
            request.setEnableRevisions(false);
            binding.newPromotableVersions(request);
            assertFalse("Should have thrown exception", false);
         }
         catch (PSContractViolationFault e)
         {
            // expected exception
            assertTrue(true);
         }

         // try with invalid ids
         try
         {
            request = new NewPromotableVersionsRequest();
            request.setIds(new long[0]);
            request.setPaths(paths);
            request.setType(relationshipType);
            request.setEnableRevisions(false);
            binding.newPromotableVersions(request);
            assertFalse("Should have thrown exception", false);
         }
         catch (PSContractViolationFault e)
         {
            // expected exception
            assertTrue(true);
         }

         // try with invalid paths = null
         try
         {
            request = new NewPromotableVersionsRequest();
            request.setIds(ids);
            request.setPaths(null);
            request.setType(relationshipType);
            request.setEnableRevisions(false);
            binding.newPromotableVersions(request);
            assertFalse("Should have thrown exception", false);
         }
         catch (PSContractViolationFault e)
         {
            // expected exception
            assertTrue(true);
         }

         // try with invalid paths = EMPTY
         try
         {
            request = new NewPromotableVersionsRequest();
            request.setIds(ids);
            request.setPaths(new String[0]);
            request.setType(relationshipType);
            request.setEnableRevisions(false);
            binding.newPromotableVersions(request);
            assertFalse("Should have thrown exception", false);
         }
         catch (PSContractViolationFault e)
         {
            // expected exception
            assertTrue(true);
         }

         // try with invalid paths with ids.length != paths.length
         try
         {
            request = new NewPromotableVersionsRequest();
            request.setIds(ids);
            request.setPaths(invalidPaths);
            request.setType(relationshipType);
            request.setEnableRevisions(false);
            binding.newPromotableVersions(request);
            assertFalse("Should have thrown exception", false);
         }
         catch (PSContractViolationFault e)
         {
            // expected exception
            assertTrue(true);
         }

         // try with invalid relationship type
         try
         {
            request = new NewPromotableVersionsRequest();
            request.setIds(ids);
            request.setPaths(invalidPaths);
            request.setType("sometype");
            request.setEnableRevisions(false);
            binding.newPromotableVersions(request);
            assertFalse("Should have thrown exception", false);
         }
         catch (PSContractViolationFault e)
         {
            // expected exception
            assertTrue(true);
         }

         // create new promotable versions
         request = new NewPromotableVersionsRequest();
         request.setIds(ids);
         request.setPaths(paths);
         request.setType(relationshipType);
         request.setEnableRevisions(false);
         PSItem[] newCopies = binding.newPromotableVersions(request);
         assertTrue(newCopies != null && newCopies.length == ids.length);
         verifyRevisionLocks(newCopies, false);
         verifyFolders(newCopies, paths, false, binding);

         // create new promotable versions and enable revisions
         request = new NewPromotableVersionsRequest();
         request.setIds(ids);
         request.setPaths(nullPaths);
         request.setType(relationshipType);
         request.setEnableRevisions(true);
         newCopies = binding.newPromotableVersions(request);
         assertTrue(newCopies != null && newCopies.length == ids.length);
         verifyRevisionLocks(newCopies, true);
         verifyFolders(newCopies, paths, true, binding);
         removeItems(binding, newCopies);

         // same as array of null
         request.setPaths(new String[]{null});
         newCopies = binding.newPromotableVersions(request);
         assertTrue(newCopies != null && newCopies.length == ids.length);
         verifyFolders(newCopies, paths, true, binding);
         removeItems(binding, newCopies);

         // same as array of the same path
         request.setPaths(onePaths);
         newCopies = binding.newPromotableVersions(request);
         assertTrue(newCopies != null && newCopies.length == ids.length);
         verifyFolders(newCopies, one3Paths, false, binding);
         removeItems(binding, newCopies);
      }
      catch (PSInvalidSessionFault e)
      {
         throw new junit.framework.AssertionFailedError("Invalid session: " + e);
      }
      catch (PSNotAuthorizedFault e)
      {
         throw new junit.framework.AssertionFailedError("Not authorized: " + e);
      }
      catch (PSUnknownContentTypeFault e)
      {
         throw new junit.framework.AssertionFailedError(
            "Unknown content type: " + e);
      }
      catch (RemoteException e)
      {
         throw new junit.framework.AssertionFailedError("Unexpected error: "
            + e);
      }
      finally
      {
         // cleanup
         cleanUpContentType(session, contentType, acls);
         cleanUpFolders("//Folders/Tests", session);
      }
   }

   /**
    * Removes the specified items.
    *
    * @param binding the proxy object, assumed not <code>null</code>.
    * @param items the to be removed items, assumed not <code>null</code>.
    *
    * @throws Exception if any error occurs.
    */
   private void removeItems(ContentSOAPStub binding, PSItem[] items)
      throws Exception
   {
      long[] ids = new long[items.length];
      for (int i=0; i<ids.length; i++)
         ids[i] = items[i].getId();

      if (ids.length != 0)
         binding.deleteItems(ids);
   }

   /**
    * Attempts to create a translation of an item twice. Verify that no item was
    * created the 2nd time and that the original translation was not modified.
    * <p>
    * This test depends on the presence of a specific FastForward item, #335.
    * This item must not have any existing translations.
    *
    * @throws Exception
    */
   @Test
   public void FIXME_testCreateTranslationAgain() throws Exception
   {
      ContentSOAPStub binding = getBinding(6000000);
      PSTestUtils.setSessionHeader(binding, m_session);
      SystemSOAPStub sysBinding = getSystemSOAPStub(null);
      PSTestUtils.setSessionHeader(sysBinding, m_session);

      List<PSLocale> locales = new ArrayList<PSLocale>();
      PSItem[] newCopies = null;
      PSItem[] newCopies2 = null;
      IPSGuid originalItemGuid = null;
      boolean done = false;
      try
      {
         locales = createTestLocales(m_session);
         {
            List<PSAutoTranslation> autoTranslations =
               createTestAutoTranslations(locales);
            long[] ids = new long[1];
            //335 is the item id for 'Page - About Enterprise Investments'
            IPSGuidManager gmgr = PSGuidManagerLocator.getGuidMgr();
            originalItemGuid = gmgr.makeGuid(new PSLocator(335));
            ids[0] = originalItemGuid.longValue();

            PSRelationshipFilter rFilter = new PSRelationshipFilter();
            rFilter.setCategory(PSRelationshipFilterCategory.translation);
            rFilter.setOwner(ids[0]);
            PSRelationship[] rels = loadRelationships(sysBinding, rFilter);
            assertTrue(
               "Item 335 must not contain any translations for this test to run.",
               rels.length == 0);

            PSAaRelationshipFilter relFilter = new PSAaRelationshipFilter();
            relFilter.setOwner(new PSLegacyGuid(originalItemGuid.getUUID(),
                  getCurrentRevision(originalItemGuid)).longValue());
            relFilter.setLimitToOwnerRevisions(true);
            PSAaRelationship[] origAaRels = loadAaRelationships(binding,
                  relFilter);

            final NewTranslationsRequest request = createTranslationRequest(
                  ids, new PSAutoTranslation[] { autoTranslations.get(0) });
            newCopies = binding.newTranslations(request);
            assertEquals(newCopies.length, ids.length);
            PSRelationship[] transRels = loadRelationships(sysBinding, rFilter);
            assertTrue(transRels.length == 1);

            relFilter.setOwner(newCopies[0].getId());
            PSAaRelationship[] transAaRels = loadAaRelationships(binding,
                  relFilter);
            assertEquals(origAaRels.length, transAaRels.length);

            // translate again and verify the item and rels were not duped
            newCopies2 = binding.newTranslations(request);
            PSRelationship[] transRels2 = loadRelationships(sysBinding,
                  rFilter);
            assertTrue(newCopies[0].getId() == newCopies2[0].getId());
            assertTrue(transRels2.length == 1);
            assertTrue(transRels[0].getDependentId() == transRels2[0]
                  .getDependentId());

            relFilter.setOwner(newCopies2[0].getId());
            PSAaRelationship[] transAaRels2 = loadAaRelationships(binding,
                  relFilter);
            assertEquals(origAaRels.length, transAaRels2.length);
            done = true;
         }

      }
      catch (Throwable e)
      {
         e.printStackTrace();
         throw new junit.framework.AssertionFailedError("Unknown exception: " + e);
      }
      finally
      {
         try
         {
            // cleanup
            if (newCopies != null && newCopies.length > 0)
            {
               purgeTranslations(binding, sysBinding, originalItemGuid, true,
                     new ArrayList<Long>());
            }
            deleteLocales(localesListToIdArray(locales), m_session, true);
         }
         catch (Exception e)
         {
            // to leave the original exception alone in case of failure
            if (done)
            {
               throw e;
            }
         }

      }

   }

   /**
    * If <code>original</code> is <code>true</code>, looks for a
    * translation relationship on the item identified by the supplied guid. If
    * found, the dependent item is processed recursively. If
    * <code>original</code> is <code>false</code>, all AA rels are followed
    * and the dependent items are processed recursively. If the item's lang is
    * not en-us, it is purged.
    *
    * @param binding Assumed not <code>null</code>.
    *
    * @param itemGuid The id of the item to process. This item is deleted after
    * processing all relationships unless the <code>original</code> flag is
    * <code>true</code>. Assumed not <code>null</code>.
    *
    * @param original <code>true</code> if <code>itemGuid</code> is for the
    * untranslated item, <code>false</code> if it is one of the translated
    * items in the translation graph.
    *
    * @param idsToDelete Supply an empty set on the first call. Used for
    * recursive processing.
    */
   private void purgeTranslations(ContentSOAPStub binding,
         SystemSOAPStub sysBinding, IPSGuid itemGuid, boolean original,
         List<Long> idsToDelete)
      throws Exception
   {
      if (original)
      {
         PSRelationshipFilter rFilter = new PSRelationshipFilter();
         rFilter.setCategory(PSRelationshipFilterCategory.translation);
         rFilter.setOwner(itemGuid.longValue());
         PSRelationship[] rels = loadRelationships(sysBinding, rFilter);

         if (rels.length == 0)
            return;
         for (PSRelationship r : rels)
         {
            purgeTranslations(binding, sysBinding, new PSLegacyGuid(
               r.getDependentId()), false, idsToDelete);
         }
         long[] ids = new long[idsToDelete.size()];
         for (int i = 0; i < ids.length; i++)
         {
            ids[i] = idsToDelete.get(i);
         }
         binding.deleteItems(ids);
         return;
      }

      //process translated item
      PSRelationshipFilter rFilter = new PSRelationshipFilter();
      rFilter.setCategory(PSRelationshipFilterCategory.activeassembly);
      rFilter.setOwner(itemGuid.longValue());
      PSRelationship[] rels = loadRelationships(sysBinding, rFilter);

      IPSCmsObjectMgr omgr = PSCmsObjectMgrLocator.getObjectManager();
      for (PSRelationship r : rels)
      {
         PSComponentSummary sum = omgr.loadComponentSummary((int) r.getOwnerId());
         if (!sum.getLocale().equals("en-us"))
         {
            purgeTranslations(binding, sysBinding, new PSLegacyGuid(
               r.getDependentId()), false, idsToDelete);
         }
      }
      PSComponentSummary sum = omgr.loadComponentSummary(itemGuid.getUUID());
      if (!sum.getLocale().equals("en-us"))
      {
         idsToDelete.add(itemGuid.longValue());
      }
   }

   /**
    *
    * param g Assumed not null and that it is of type
    * {@link PSTypeEnum#LEGACY_CONTENT}.
    *
    * @return -1 if no item is found with the supplied id, otherwise, the
    * current revision of that item.
    */
   private int getCurrentRevision(IPSGuid g)
   {
      IPSCmsObjectMgr omgr = PSCmsObjectMgrLocator.getObjectManager();
      PSComponentSummary sum = omgr.loadComponentSummary(g.getUUID());
      return sum == null ? -1 : sum.getCurrentLocator().getRevision();
   }

   /**
    * Attempt to translate the EI site tree logged in as the admin1 user in the
    * EI Admin community. Verify that the cloned site looks like the original
    * site, with all folders and items translated, regardless of object
    * community.
    *
    * @throws Exception
    */
   public void testCreateTranslationOfFolder() throws Exception
   {

   }

   /* left here for testing purposes, easy way to disable all tests except what
    * is needed when creating new tests or fixing existing
    */
//   public static Test suite()
//   {
//      TestSuite suite = new TestSuite();
//      suite.addTest(new ContentTestCase("testCreateNewTranslations"));
//      suite.addTest(new ContentTestCase("testCreateTranslationOfFolder"));
//      suite.addTest(new ContentTestCase("testCreateTranslationAgain"));
//      return suite;
//   }


   /**
    * Test the newTranslations service.
    *
    * @throws Exception for any error.
    */
   @Test
   public void testCreateNewTranslations() throws Exception
   {
      ContentSOAPStub binding = getBinding(6000000);

      List<PSLocale> locales = new ArrayList<PSLocale>();
      PSContentType contentType = null;
      List<PSAclImpl> acls = new ArrayList<PSAclImpl>();

      boolean done = false;
      try
      {
         // create the test content type
         contentType = createContentType("test3", m_session, acls);

         // create test locales
         locales = createTestLocales(m_session);

         // create test auto translations for the first workflow found
         final PSAutoTranslation[] autoTranslations =
               createTestAutoTranslations(locales).toArray(
                     new PSAutoTranslation[0]);

         // create 3 test items for the new content type
         PSTestUtils.setSessionHeader(binding, m_session);
         final List<PSItem> items =
               createTestItems(contentType.getName(), 3, true, true, false,
                     null, m_session, binding);
         final long[] ids = toItemIds(items);

         // try without rhythmyx session
         binding.clearHeaders();
         try
         {
            binding.newTranslations(
                  createTranslationRequest(ids, autoTranslations));
            fail("Should have thrown exception");
         }
         catch (PSInvalidSessionFault expected)
         {
         }

         // try with an invalid rhythmyx session
         PSTestUtils.setSessionHeader(binding, "foobar");
         try
         {
            binding.newTranslations(
                  createTranslationRequest(ids, autoTranslations));
            fail("Should have thrown exception");
         }
         catch (PSInvalidSessionFault expected)
         {
         }

         // set valid rhythmyx session
         PSTestUtils.setSessionHeader(binding, m_session);

         // try with invalid ids
         try
         {
            binding.newTranslations(
                  createTranslationRequest(null, autoTranslations));
            fail("Should have thrown exception");
         }
         catch (PSContractViolationFault expected)
         {
         }

         // try with invalid ids
         try
         {
            binding.newTranslations(
                  createTranslationRequest(new long[0], autoTranslations));
            fail("Should have thrown exception");
         }
         catch (PSContractViolationFault expected)
         {
         }

         // try with invalid relationship type
         try
         {
            final NewTranslationsRequest request =
                  createTranslationRequest(ids, autoTranslations);
            request.setType("sometype");
            binding.newTranslations(request);
            fail("Should have thrown exception");
         }
         catch (PSContractViolationFault expected)
         {
         }

         // try with invalid locale
         try
         {
            final PSAutoTranslation autoTranslation = autoTranslations[0];
            final NewTranslationsRequest request = createTranslationRequest(ids,
                  new PSAutoTranslation[] {autoTranslation});
            final String oldLocale = autoTranslation.getLocale();
            autoTranslation.setLocale("zz-zz");
            try
            {
               binding.newTranslations(request);
            }
            finally
            {
               autoTranslation.setLocale(oldLocale);
            }
            fail("Should have thrown exception");
         }
         catch (PSContractViolationFault expected)
         {
         }

         // try to convert to the item's locale
         try
         {
            final PSAutoTranslation autoTranslation = autoTranslations[0];
            final NewTranslationsRequest request = createTranslationRequest(ids,
                  new PSAutoTranslation[] {autoTranslation});
            final String oldLocale = autoTranslation.getLocale();
            autoTranslation.setLocale(items.get(0).getDataLocale());
            try
            {
               binding.newTranslations(request);
            }
            finally
            {
               autoTranslation.setLocale(oldLocale);
            }
            fail("Should have thrown exception");
         }
         catch (PSContractViolationFault expected)
         {
         }

         // all the copies created during this test
         List<PSItem> allCopies = new ArrayList<PSItem>();

         // create new translations for autoTranslations[0]
         {
            final NewTranslationsRequest request = createTranslationRequest(ids,
                  new PSAutoTranslation[] {autoTranslations[0]});
            final PSItem[] newCopies = binding.newTranslations(request);
            assertEquals(newCopies.length, ids.length);
            verifyRevisionLocks(newCopies, false);
            allCopies.addAll(Arrays.asList(newCopies));
         }

         // try to translate a translation
         try
         {
            final NewTranslationsRequest request = createTranslationRequest(
                  new long[] {allCopies.get(0).getId()},
                  new PSAutoTranslation[] {autoTranslations[0]});
            binding.newTranslations(request);
            fail("Should have thrown exception");
         }
         catch (PSContractViolationFault expected)
         {
         }

         // create new translations for autoTranslations[1]
         {
            final NewTranslationsRequest request = createTranslationRequest(ids,
                  new PSAutoTranslation[] {autoTranslations[1]});
            request.setEnableRevisions(true);
            final PSItem[] newCopies = binding.newTranslations(request);
            assertEquals(newCopies.length, ids.length);
            verifyRevisionLocks(newCopies, true);
            allCopies.addAll(Arrays.asList(newCopies));
         }

         // recreate the translations for all test locales
         {
            final NewTranslationsRequest request = createTranslationRequest(ids,
                  autoTranslations);
            final PSItem[] newCopies = binding.newTranslations(request);
            assertEquals(newCopies.length,
                  ids.length * autoTranslations.length);
            // order - first all copies for the first locale,
            // then - for the second, etc
            for (int i = 0; i < newCopies.length; i++)
            {
               assertEquals(allCopies.get(i), newCopies[i]);
            }
         }

         // recreate the translations for all locales available on the system
         {
            final NewTranslationsRequest request =
                  createTranslationRequest(ids, null);
            final PSItem[] newCopies = binding.newTranslations(request);
            // there could be some other locales besides the test ones
            final PSLocale[] allLocales = loadLocales(null, null, m_session);
            assertEquals(newCopies.length, ids.length * (allLocales.length - 1));
            // at least it should contain items of the test locales
            // created earlier
            for (final PSItem copy : allCopies)
            {
               assertTrue(ArrayUtils.contains(newCopies, copy));
            }
         }
         done = true;
      }
      finally
      {
         try
         {
            // cleanup
            cleanUpContentType(m_session, contentType, acls);
            deleteLocales(localesListToIdArray(locales), m_session, true);
         }
         catch (Exception e)
         {
            // to leave the original exception alone in case of failure
            if (done)
            {
               throw e;
            }
         }
      }
      assertTrue(done);
   }

   /**
    * Creates a basic translation request for testing.
    * @param ids the items to translate. Can be <code>null</code>.
    * @param autoTranslations the translations storing the locales
    * to translate to. Can be <code>null</code>.
    * @return the new translation request of the translation type,
    * with enable revisions is set to "false".
    */
   private NewTranslationsRequest createTranslationRequest(
         final long[] ids, PSAutoTranslation[] autoTranslations)
   {
      final NewTranslationsRequest request = new NewTranslationsRequest();
      request.setIds(ids);
      request.setType(PSRelationshipConfig.TYPE_TRANSLATION);
      request.setEnableRevisions(false);
      request.setAutoTranslations(autoTranslations);
      return request;
   }

   /**
    * Verifies that all supplied items have the specified revision lock value.
    *
    * @param items the items to verify, assumed not <code>null</code>, may be
    *    empty.
    * @param enabled <code>true</code> if the revision locks should be enabled,
    *    <code>false</code> otherwise.
    */
   @Test
   private void verifyRevisionLocks(PSItem[] items, boolean enabled)
   {
      List<Integer> ids = new ArrayList<Integer>();
      for (PSItem item : items)
         ids.add(new PSLegacyGuid(item.getId()).getContentId());

      IPSCmsObjectMgr mgr = PSCmsObjectMgrLocator.getObjectManager();
      List<PSComponentSummary> summaries = mgr.loadComponentSummaries(ids);
      for (PSComponentSummary summary : summaries)
      {
         if (enabled)
            assertTrue(summary.isRevisionLock());
         else
            assertFalse(summary.isRevisionLock());
      }
   }

   /**
    * Verifies that the supplied items are attached to the paths specified in
    * the same order.
    *
    * @param items the items to verify, assumed not <code>null</code>, may be
    *    empty.
    * @param paths the folder paths to verify, assumed not <code>null</code>
    *    and of same length as items.
    * @param none <code>true</code> to specify that none of the items should
    *    be attached to a folder, <code>false</code> otherwise.
    * @param binding the stub to do the lookup, assumed not <code>null</code>.
    * @throws Exception for any error.
    */
   @Test
   private void verifyFolders(PSItem[] items, String[] paths, boolean none,
      ContentSOAPStub binding) throws Exception
   {
      for (int i = 0; i < items.length; i++)
      {
         PSItem item = items[i];
         String path = paths[i];

         FindFolderPathRequest request = new FindFolderPathRequest();
         request.setId(item.getId());
         String[] foundPaths = binding.findFolderPath(request).getPaths();
         if (none)
         {
            assertTrue(foundPaths.length == 0);
         }
         else
         {
            boolean found = false;
            for (String foundPath : foundPaths)
            {
               if (foundPath.equals(path))
               {
                  found = true;
                  break;
               }
            }
            assertTrue(found);
         }
      }
   }

   /**
    * Test the findRevisions service.  Requires that revisions exist for fixed
    * items (currently 360 and 471).
    *
    * @throws Exception if the test fails
    */
   @Test
   public void testContentSOAPFindRevisions() throws Exception
   {
      ContentSOAPStub binding = getBinding(6000000);
      long[] guids = new long[0];

      // try no session
      try
      {
         binding.findRevisions(guids);
         assertFalse("should have thrown", true);
      }
      catch (PSInvalidSessionFault e)
      {
         // expected
      }

      // try invalid session
      PSTestUtils.setSessionHeader(binding, "nosuchsession");
      try
      {
         binding.findRevisions(guids);
         assertFalse("should have thrown", true);
      }
      catch (PSInvalidSessionFault e)
      {
         // expected
      }

      // try bad input
      PSTestUtils.setSessionHeader(binding, m_session);
      try
      {
         binding.findRevisions(guids);
      }
      catch (PSContractViolationFault e)
      {
         // expected
      }

      guids = new long[] { 2 };
      try
      {
         binding.findRevisions(guids);
      }
      catch (PSContractViolationFault e)
      {
         // expected
      }

      int[] ids = new int[] { 360, 471 };
      guids = new long[ids.length];
      for (int i = 0; i < ids.length; i++)
      {
         guids[i] = getLegacyGuid(ids[i]);
      }

      PSRevisions[] revisionsList = binding.findRevisions(guids);
      assertEquals(revisionsList.length, guids.length);
      for (int i = 0; i < revisionsList.length; i++)
      {
         PSRevisions revisions = revisionsList[i];
         PSRevision[] revList = revisions.getRevisions();
         assertEquals(revList.length, 3);
         int editRev = revisions.getEditRevision();
         assertEquals(editRev, -1);
         int curRev = revisions.getCurrentRevision();
         assertEquals(curRev, 3);
         for (int j = 0; j < revList.length; j++)
         {
            PSRevision rev = revList[i];
            assertEquals(rev.isIsCurrentRevision(), rev.getRevision() == curRev);
            assertEquals(rev.isIsEditRevision(), rev.getRevision() == editRev);
            PSLegacyGuid guid = new PSLegacyGuid(rev.getId());
            assertEquals(ids[i], guid.getContentId());
            assertEquals(rev.getRevision(), guid.getRevision());
            assertTrue(!StringUtils.isBlank(rev.getCreator()));
         }
      }

      // testing items with empty or null revision list, in other words
      // there is no entries in the contentstatus history table since
      // the item has not been checked in after they are created.
      guids = new long[] { getLegacyGuid(2), getLegacyGuid(3) };
      revisionsList = binding.findRevisions(guids);
      for (PSRevisions rev : revisionsList)
      {
         assertTrue(rev.getRevisions() == null
               || rev.getRevisions().length == 0);
      }

      // negative test: should get RemoteException for non-existing id
      try
      {
         guids = new long[] {getLegacyGuid(9999)};
         binding.findRevisions(guids);
         assertFalse("should have thrown", true);
      }
      catch (RemoteException e)
      {
         // expected exception
      }
   }

   /**
    * Test promoteRevisions web service.  Requires a revision exists for fixed
    * item (currently 335).
    *
    * @throws Exception If the test fails
    */
   @Test
   public void fix_testPromoteRevision() throws Exception
   {
      ContentSOAPStub binding = getBinding(null);

      int contentId = 335;
      long[] promoteReq = new long[] { getLegacyGuid(contentId) };
      switchToIECommunity();

      // test no session
      try
      {
         binding.promoteRevisions(promoteReq);
         assertFalse("Should have thrown", true);
      }
      catch (PSInvalidSessionFault e)
      {
         // expected
      }

      // test invalid session
      PSTestUtils.setSessionHeader(binding, "nosuchsession");
      try
      {
         binding.promoteRevisions(promoteReq);
         assertFalse("Should have thrown", true);
      }
      catch (PSInvalidSessionFault e)
      {
         // expected
      }

      PSTestUtils.setSessionHeader(binding, m_session);

      // test no revision
      promoteReq = new long[] { getLegacyGuid(contentId) };
      try
      {
         binding.promoteRevisions(promoteReq);
         assertFalse("Should have thrown", true);
      }
      catch (PSContractViolationFault e)
      {
         // expected
      }

      // prepare test data
      moveItemInPublicState(binding, contentId);

      // find revision to promote
      long[] ids = new long[] { getLegacyGuid(contentId) };
      PSRevisions[] revisionsList = binding.findRevisions(ids);
      assertEquals(revisionsList.length, ids.length);
      PSRevisions curRevisions = revisionsList[0];
      int numRevs = curRevisions.getRevisions().length;
      assertTrue(numRevs > 2);
      long promoteId = curRevisions.getRevisions()[numRevs - 1].getId();

      // test invalid state
      promoteReq = new long[] { promoteId };
      try
      {
         binding.promoteRevisions(promoteReq);
         assertFalse("Should have thrown", true);
      }
      catch (PSErrorsFault e)
      {
         // expected
      }

      // edit the current revision to make a change
      PSItemStatus itemStatus = binding.prepareForEdit(ids)[0];
      assertTrue(itemStatus.isDidCheckout());
      long curId = new PSLegacyGuid(contentId, numRevs + 1).longValue();

      // test bad promote attempt on checked out item
      promoteReq = new long[] { curId };
      try
      {
         binding.promoteRevisions(promoteReq);
         assertFalse("Should have thrown", true);
      }
      catch (PSErrorsFault e)
      {
         // expected
      }

      LoadItemsRequest loadReq = new LoadItemsRequest();
      loadReq.setId(new long[] { curId });

      PSItem item = binding.loadItems(loadReq)[0];
      PSField modField = null;
      int modFieldIndex = 0;
      for (PSField field : item.getFields())
      {
         if (field.getDataType().getValue().equals(PSFieldDataType._text)
            && field.getName().equals("description"))
         {
            modField = field;
            break;
         }
         modFieldIndex++;
      }
      assertNotNull(modField);
      PSFieldValue[] oldVal = modField.getPSFieldValue();
      modField.setPSFieldValue(new PSFieldValue[] { new PSFieldValue(
         "test: " + new Date(), null) });
      SaveItemsRequest saveReq = new SaveItemsRequest();
      saveReq.setPSItem(new PSItem[] { item });
      saveReq.setCheckin(true);
      binding.saveItems(saveReq);

      // we should have created a new revision
      numRevs++;
      PSRevisions newRevisions = binding.findRevisions(ids)[0];
      assertEquals(newRevisions.getRevisions().length, numRevs);

      // compare the field value
      loadReq.setId(new long[] { new PSLegacyGuid(contentId, numRevs)
         .longValue() });
      item = binding.loadItems(loadReq)[0];
      PSField testField = item.getFields()[modFieldIndex];
      assertEquals(modField.getName(), testField.getName());
      assertFalse(Arrays.equals(oldVal, modField.getPSFieldValue()));

      // ok, finally, now we can promote
      numRevs++;
      promoteReq = new long[] { promoteId };
      binding.promoteRevisions(promoteReq);
      binding.releaseFromEdit(new ReleaseFromEditRequest(
         new PSItemStatus[] { itemStatus }, false));
      newRevisions = binding.findRevisions(ids)[0];
      assertEquals(newRevisions.getRevisions().length, numRevs);

      loadReq.setId(new long[] { new PSLegacyGuid(contentId, numRevs)
         .longValue() });
      item = binding.loadItems(loadReq)[0];
      int revision = new PSLegacyGuid(item.getId()).getRevision();
      assertEquals(revision, numRevs);
      testField = item.getFields()[modFieldIndex];
      assertEquals(modField.getName(), testField.getName());
      assertTrue(Arrays.equals(oldVal, testField.getPSFieldValue()));
   }

   private void switchToIECommunity() throws Exception
   {
      switchToCommunity("Enterprise_Investments");
   }

   private void switchToCommunity(String name) throws Exception
   {
      SwitchCommunityRequest req = new SwitchCommunityRequest();
      SystemSOAPStub binding = getSystemSOAPStub(null);
      PSTestUtils.setSessionHeader(binding, m_session);
      req.setName(name);
      binding.switchCommunity(req);
   }
   /**
    * Test checkin and checkout of items.  Requires a revision exists for fixed
    * item (currently 335).
    *
    * @throws Exception if the test fails.
    */
   @Test
   public void fix_testContentSOAPCheckInOut() throws Exception
   {
      long ctId = getLegacyGuid(335);
      long[] idArr = new long[] { ctId };

      ContentSOAPStub binding = getBinding(6000000);
      CheckoutItemsRequest checkoutReq = new CheckoutItemsRequest();
      checkoutReq.setId(new long[] { ctId });
      CheckinItemsRequest checkinReq = new CheckinItemsRequest();
      checkinReq.setId(idArr);

      SystemSOAPStub sysBinding = (new SystemTestCase()).getBinding(6000000);
      PSTestUtils.setSessionHeader(sysBinding, m_session);

      // get into quick edit checked out

      PSItemStatus[] itemStatus = checkoutItem(ctId);

      try
      {
         // test checkin no session
         try
         {
            binding.checkinItems(checkinReq);
            assertFalse("Should have thrown", true);
         }
         catch (PSInvalidSessionFault e)
         {
            // expected
         }

         // test invalid session
         PSTestUtils.setSessionHeader(binding, "nosuchsession");
         try
         {
            binding.checkinItems(checkinReq);
            assertFalse("Should have thrown", true);
         }
         catch (PSInvalidSessionFault e)
         {
            // expected
         }

         // test bad id
         PSTestUtils.setSessionHeader(binding, m_session);
         try
         {
            checkinReq.setId(new long[] {});
            binding.checkinItems(checkinReq);
            assertFalse("Should have thrown", true);
         }
         catch (PSContractViolationFault e)
         {
            // expected
         }

         try
         {
            checkinReq.setId(new long[] { getLegacyGuid(999999) });
            binding.checkinItems(checkinReq);
            assertFalse("Should have thrown", true);
         }
         catch (PSErrorsFault e)
         {
            // expected
         }

         // test checkin no comment
         String user = "admin1";
         checkinReq.setId(idArr);
         binding.checkinItems(checkinReq);
         assertTrue(didCheckIn(sysBinding, ctId, user, null));

         // test already checked in
         try
         {
            binding.checkinItems(checkinReq);
         }
         catch (Exception e)
         {
            assertFalse("should not have thrown: " + e.getLocalizedMessage(),
               true);
         }

         // test checkout no comment
         checkinReq.setId(idArr);
         binding.checkoutItems(checkoutReq);
         assertTrue(didCheckOut(sysBinding, ctId, user, null));

         // test checkin w/comment
         String comment = "test - " + new Date();
         checkinReq.setId(idArr);
         checkinReq.setComment(comment);
         binding.checkinItems(checkinReq);
         assertTrue(didCheckIn(sysBinding, ctId, user, comment));

         // test checkout no session
         binding = getBinding(6000000);
         try
         {
            binding.checkoutItems(checkoutReq);
            assertFalse("Should have thrown", true);
         }
         catch (PSInvalidSessionFault e)
         {
            // expected
         }

         // test invalid session
         PSTestUtils.setSessionHeader(binding, "nosuchsession");
         try
         {
            binding.checkoutItems(checkoutReq);
            assertFalse("Should have thrown", true);
         }
         catch (PSInvalidSessionFault e)
         {
            // expected
         }

         // test bad id
         PSTestUtils.setSessionHeader(binding, m_session);
         try
         {
            checkoutReq.setId(new long[] {});
            binding.checkoutItems(checkoutReq);
            assertFalse("Should have thrown", true);
         }
         catch (PSContractViolationFault e)
         {
            // expected
         }

         try
         {
            checkoutReq.setId(new long[] { getLegacyGuid(999999) });
            binding.checkoutItems(checkoutReq);
            assertFalse("Should have thrown", true);
         }
         catch (PSErrorsFault e)
         {
            // expected
         }

         // test checkout w/ comment
         checkoutReq.setId(idArr);
         comment = "test - " + new Date();
         checkoutReq.setComment(comment);
         binding.checkoutItems(checkoutReq);
         assertTrue(didCheckOut(sysBinding, ctId, user, comment));

         // test already checked out
         try
         {
            binding.checkoutItems(checkoutReq);
         }
         catch (Exception e)
         {
            assertFalse("should not have thrown: " + e.getLocalizedMessage(),
               true);
         }

         // test checkin checkedout by other user
         String sessId = PSTestUtils.login("editor1", "demo");
         PSTestUtils.setSessionHeader(binding, sessId);
         try
         {
            binding.checkinItems(checkinReq);
            assertFalse("Should have thrown", true);
         }
         catch (PSErrorsFault e)
         {
            // expected
         }

         // test checkout checkedout by other user
         try
         {
            binding.checkoutItems(checkoutReq);
            assertFalse("Should have thrown", true);
         }
         catch (PSErrorsFault e)
         {
            // expected
         }
      }
      finally
      {
         PSTestUtils.setSessionHeader(binding, m_session);
         checkinItem(binding, itemStatus[0]);
      }

   }

   /**
    * Use the audit trail to determine if the item was checked out, assumes item
    * has been public.
    *
    * @param sysBinding The binding to use, assumed not <code>null</code> and
    * to have a session set on it.
    * @param ctId The item content id.
    * @param user The actor, assumed not <code>null</code> or empty.
    * @param comment Optional comment, may be <code>null</code> or empty, used
    * to check if the audit trail comment matches.
    *
    * @return <code>true</code> if the last audit trail entry indicates the item
    * was checked out by the specified user with the specified comment,
    * <code>false</code> if not.
    *
    * @throws Exception If there are any errors.
    */
   @Test
   private boolean didCheckOut(SystemSOAPStub sysBinding, long ctId,
      String user, String comment) throws Exception
   {
      PSAuditTrail[] trails = sysBinding.loadAuditTrails(new long[] { ctId });
      assertTrue(trails.length == 1);
      PSAuditTrail trail = trails[0];
      PSAudit[] audits = trail.getAudits();
      int len = audits.length;
      assertTrue(len > 0);
      int iAudit = len - 1;
      PSAudit audit = audits[iAudit];

      boolean isCheckout = audit.getActor().equals(user)
         && audit.getTransitionId() == 0;
      // ensure previous entry had same revision, otherwise this is a checkin
      if (isCheckout && iAudit > 1)
      {
         if (audit.getRevision() != audits[iAudit - 1].getRevision())
            isCheckout = false;
         else
         {
            if (comment == null)
               comment = "";
            String trComment = audit.getTransitionComment();
            if (trComment == null)
               trComment = "";
            if (!comment.equals(trComment))
               isCheckout = false;
         }
      }
      else
      {
         isCheckout = false;
      }

      return isCheckout;
   }

   /**
    * Use the audit trail to determine if the item was checked in, assumes item
    * has been public.
    *
    * @param sysBinding The binding to use, assumed not <code>null</code> and
    * to have a session set on it.
    * @param ctId The item content id.
    * @param user The actor, assumed not <code>null</code> or empty.
    * @param comment Optional comment, may be <code>null</code> or empty, used
    * to check if the audit trail comment matches.
    *
    * @return <code>true</code> if the last audit trail entry indicates the item
    * was checked in by the specified user with the specified comment,
    * <code>false</code> if not.
    *
    * @throws Exception If there are any errors.
    */
   @Test
   private boolean didCheckIn(SystemSOAPStub sysBinding, long ctId,
      String user, String comment) throws Exception
   {
      PSAuditTrail[] trails = sysBinding.loadAuditTrails(new long[] { ctId });
      assertTrue(trails.length == 1);
      PSAuditTrail trail = trails[0];
      PSAudit[] audits = trail.getAudits();
      int len = audits.length;
      assertTrue(len > 0);
      int iAudit = len - 1;
      PSAudit audit = audits[iAudit];

      boolean isCheckin = audit.getActor().equals(user)
         && audit.getTransitionId() == 0;
      // ensure previous entry had lower revision, otherwise this is a checkout
      if (isCheckin && iAudit > 1)
      {
         if (audit.getRevision() != audits[iAudit - 1].getRevision() + 1)
            isCheckin = false;
         else
         {
            if (comment == null)
               comment = "";
            String trComment = audit.getTransitionComment();
            if (trComment == null)
               trComment = "";
            if (!comment.equals(trComment))
               isCheckin = false;
         }
      }
      else
      {
         isCheckin = false;
      }

      return isCheckin;
   }

   /**
    * Save the items supplied with the request and update the items ids after
    * a successful save.
    *
    * @param request the save request with all items and options to save,
    *    assumed not <code>null</code>.
    * @param binding the binding to use for the save operation, assumed not
    *    <code>null</code>.
    * @return the new ids of the saved items, never <code>null</code> or empty.
    * @throws Exception fof any error.
    */
   private long[] saveItems(SaveItemsRequest request, ContentSOAPStub binding)
      throws Exception
   {
      long[] ids = binding.saveItems(request).getIds();

      // update the inserted items ids
      int index = 0;
      for (PSItem item : request.getPSItem())
         item.setId(ids[index++]);

      return ids;
   }

   /**
    * Populate the field values for all required fields to make sure we are able
    * to save the items.
    *
    * @param items all items for which to fill the values of all required
    *    fields, assumed not <code>null</code>, may be empty.
    * @param communityId the community id to fill the
    *    <code>sys_communityid</code> field with, assumed not
    *    <code>null</code> or empty.
    * @param workflowId the workflow id to ffill the
    *    <code>sys_workflowid</code> field with, assumed not <code>null</code>
    *    or empty.
    * @param locale the locale to fill the <code>sys_lang</code> field with,
    *    assumed not <code>null</code> or empty.
    */
   private void populateRequiredFieldValues(List<PSItem> items,
      String communityId, String workflowId, String locale)
   {
      for (int itemIndex = 0; itemIndex < items.size(); itemIndex++)
      {
         PSItem item = items.get(itemIndex);
         int textFieldIndex = 0;
         int numberFieldIndex = 0;
         for (PSField field : item.getFields())
         {
            PSFieldDimension dimension = field.getDimension();
            if (dimension.equals(PSFieldDimension.required)
               || dimension.equals(PSFieldDimension.oneormore)
               || dimension.equals(PSFieldDimension.count))
            {
               if (field.getName().equals("sys_communityid"))
               {
                  updateField(item, field.getName(), communityId);
               }
               else if (field.getName().equals("sys_workflowid"))
               {
                  updateField(item, field.getName(), workflowId);
               }
               else if (field.getName().equals("sys_lang"))
               {
                  updateField(item, field.getName(), locale);
               }
               else if (field.getDataType().equals(PSFieldDataType.text))
               {
                  String value = "item_" + itemIndex + ".field_"
                        + textFieldIndex++;
                  updateField(item, field.getName(), value);
               }
               else if (field.getDataType().equals(PSFieldDataType.number))
               {
                  updateNumberField(item, field.getName(), numberFieldIndex++);
               }
               else if (field.getDataType().equals(PSFieldDataType.date))
               {
                  updateDateField(item, field.getName(), new Date(), 1);
               }
               else if (field.getDataType().equals(PSFieldDataType.binary))
               {
                  updateBinaryField(item, field.getName(),
                     "Some binary !@#$%^&", null, null);
               }
               else
               {
                  throw new RuntimeException("Unknown field type");
               }
            }
         }
      }
   }

   /**
    * Populates the local parent fields and all children for the test content
    * type <code>test3</code>.
    *
    * @param items the items to populate, not <code>null</code>, may be empty.
    * @param rowCount the number of rows to create for each child, <= 0 to leave
    *    all children empty.
    * @param updateId an update identifier used to differentiate contents
    *    between updates.
    * @param binaryFile <code>LARGE</code> or <code>SMALL</code> if the 
    *    identified binary field is expected as attachment, <code>SKIP</code> 
    *    if it is expected base64 encoded.
    * @param session the user session, assumed not <code>null</code> or empty.
    * @param binding the stub used, assumed not <code>null</code>.
    * @throws Exception for any error.
    */
   private void populateTestFields(List<PSItem> items, int rowCount,
      int updateId, BinaryFiles binaryFile, String session,
      ContentSOAPStub binding) throws Exception
   {
      int itemIndex = 0;
      for (PSItem item : items)
      {
         // local parent fields
         updateNumberField(item, "field_1", itemIndex + updateId);

         File file = null;
         if (binaryFile != BinaryFiles.SKIP)
         {
            file = getAttachmentFile(itemIndex, binaryFile);
            if (file == null || !file.exists())
               throw new IllegalArgumentException("attachment file must exist");
         }

         updateBinaryField(item, "field_2", itemIndex + updateId
            + ": some binary data !@#$%^^&&*(*_)+", file, binding);

         if (rowCount > 0)
         {
            // fill all children fields
            PSItemChildren[] children = item.getChildren();
            for (PSItemChildren child : children)
            {
               PSChildEntry[] childEntries = child.getPSChildEntry();
               if (childEntries == null)
               {
                  // create new child entries
                  childEntries = createChildEntries(item.getId(), child
                     .getName(), rowCount, session);
                  child.setPSChildEntry(childEntries);
               }
               else if (childEntries.length < rowCount)
               {
                  // add child entries
                  int newCount = rowCount - childEntries.length;
                  PSChildEntry[] newEntries = createChildEntries(item.getId(),
                     child.getName(), newCount, session);

                  PSChildEntry[] newChildEntries = new PSChildEntry[rowCount];

                  // copy existing
                  java.lang.System.arraycopy(childEntries, 0, newChildEntries,
                     0, childEntries.length);

                  // copy new
                  java.lang.System.arraycopy(newEntries, 0, newChildEntries,
                     childEntries.length, newEntries.length);

                  child.setPSChildEntry(newChildEntries);
               }
               else if (childEntries.length > rowCount)
               {
                  // remove child entries
                  for (int i = 0; i < childEntries.length; i++)
                  {
                     PSChildEntry entry = childEntries[i];
                     if (i >= rowCount)
                        entry.setAction(PSItemChildEntry.CHILD_ACTION_DELETE);
                  }
               }

               populateChildRows(child.getPSChildEntry(), null, null,
                     binaryFile != BinaryFiles.SKIP, updateId++, binding);
            }
         }

         itemIndex++;
      }
   }

   /**
    * Convert the supplied list of items into an array.
    *
    * @param items the items list, assumed not <code>null</code>, may be empty.
    * @return the items array, never <code>null</code>, may be empty.
    */
   private PSItem[] toArray(List<PSItem> items)
   {
      return items.toArray(new PSItem[items.size()]);
   }

   /**
    * Creates the specified number of content items for the specified content
    * type, populates all required field values and saves the items as
    * requested.
    *
    * @param contentType the content type of the new items to create,
    *    not <code>null</code> or empty.
    * @param count the number of items to create, must be > 0.
    * @param populateRequiredFields <code>true</code> to populate all
    *    required fields values, <code>false</code> otherwise.
    * @param saveItems <code>true</code> to save the newly created items to the
    *    repository, <code>false</code> to return them unsaved.
    * @param enableRevisions <code>true</code> to enable revisions for all
    *    saved items, <code>false</code> otherwise.
    * @param path the folder path to which to attach the created items, may be
    *    <code>null</code> or empty.
    * @param session the user session, not <code>null</code> or empty.
    * @param binding the stub used for the item creation, not
    *    <code>null</code>.
    * @return a list with all new items created, never <code>null</code> or
    *    empty. The returned items are not persisted yet.
    * @throws Exception for any error.
    */
   @Test
   public List<PSItem> createTestItems(String contentType, int count,
      boolean populateRequiredFields, boolean saveItems,
      boolean enableRevisions, String path, String session,
      ContentSOAPStub binding) throws Exception
   {
      if (StringUtils.isBlank(contentType))
         throw new IllegalArgumentException(
            "contentType cannot be null or empty");

      if (count <= 0)
         throw new IllegalArgumentException("count must be > 0");

      if (StringUtils.isBlank(session))
         throw new IllegalArgumentException("session cannot be null or empty");

      if (binding == null)
         throw new IllegalArgumentException("binding cannot be null");

      CreateItemsRequest request = new CreateItemsRequest();
      request.setContentType(contentType);
      request.setCount(count);

      List<PSItem> items = Arrays.asList(binding.createItems(request));

      if (populateRequiredFields)
      {
         PSCommunity defaultCommunity = getDefaultCommunity();
         int communityId = new PSDesignGuid(defaultCommunity.getId()).getUUID();

         com.percussion.webservices.security.data.PSLocale defaultLocale =
            getDefaultLocale();
         String locale = defaultLocale.getCode();

         List<PSWorkflow> workflows = catalogWorkflows(session);
         assertFalse("Need at least one workflow definition.", workflows
            .isEmpty());
         long workflowId = workflows.get(0).getId();

         populateRequiredFieldValues(items, String.valueOf(communityId), String
            .valueOf(workflowId), locale);
      }

      if (!StringUtils.isBlank(path))
      {
         PSItemFolders[] folders = new PSItemFolders[1];
         folders[0] = new PSItemFolders(path);
         for (PSItem item : items)
            item.setFolders(folders);
      }

      if (saveItems)
      {
         SaveItemsRequest saveRequest = new SaveItemsRequest();
         saveRequest.setPSItem(toArray(items));
         saveRequest.setEnableRevisions(enableRevisions);
         long[] ids = saveItems(saveRequest, binding);
         assertTrue(ids != null && ids.length == items.size());
      }

      return items;
   }

   /**
    * Creates the specified number of revisions for the supplied items, updates
    * the <code>field_1</code> for each revision. Assumes that revisions
    * are enabled for all items already.
    *
    * @param items the items to create revisions for, assumed not
    *    <code>null</code>, may be empty.
    * @param count the number of revisions to create, assumed >= 0.
    * @param binding the stub used to save the items, assumed not
    *    <code>null</code>.
    * @return the updated items, never <code>null</code>, may be empty. All
    *    items will be checked out on return.
    * @throws Exception for any error.
    */
   @Test
   private List<PSItem> createItemRevisions(List<PSItem> items, int count,
      ContentSOAPStub binding) throws Exception
   {
      for (int i = 0; i < count; i++)
      {
         // update field_1 for each item
         for (PSItem item : items)
            updateField(item, "field_1", String.valueOf(i + 1));

         // save all changed items
         SaveItemsRequest saveRequest = new SaveItemsRequest();
         saveRequest.setPSItem(toArray(items));
         long[] ids = saveItems(saveRequest, binding);
         assertTrue(ids != null && ids.length == items.size());

         // checkin all items
         CheckinItemsRequest checkinRequest = new CheckinItemsRequest();
         checkinRequest.setId(toItemIds(items));
         checkinRequest.setComment("Create revision " + i);
         binding.checkinItems(checkinRequest);

         // checkout all items
         CheckoutItemsRequest checkoutRequest = new CheckoutItemsRequest();
         checkoutRequest.setId(toItemIds(items));
         checkoutRequest.setComment("Create revision " + i);
         binding.checkoutItems(checkoutRequest);
      }

      return items;
   }

   /**
    * Convenience method to update a binary field. Base64 encodes the supplied
    * fieldValue and calls {@link #updateField(PSItem, String, String)}.
    *
    * @param attachment an attachment to update the binary field with,
    *    may be <code>null</code> in which case the supplied fieldValue
    *    will be used and base64 encoded.
    * @param binding the stub to add the attachment to, assumed not
    *    <code>null</code> if attached is <code>true</code>.
    */
   private void updateBinaryField(PSItem item, String fieldName,
      String fieldValue, File attachment, ContentSOAPStub binding)
   {
      if (attachment != null)
      {
         String attachmentId = addAttachment(binding, attachment);

         PSField[] fields = item.getFields();
         for (PSField field : fields)
         {
            if (field.getName().equalsIgnoreCase(fieldName))
            {
               PSFieldValue[] values = field.getPSFieldValue();
               if (values == null)
               {
                  values = new PSFieldValue[1];
                  field.setPSFieldValue(values);
               }

               for (PSFieldValue value : values)
               {
                  if (value == null)
                  {
                     value = new PSFieldValue();
                     values[0] = value;
                  }

                  value.setAttachmentId(attachmentId);
               }

               break;
            }
         }
      }
      else
         updateField(item, fieldName, PSBase64Encoder.encode(fieldValue));
   }

   /**
    * Convenience method to update a date field. Converts the supplied date
    * to a string and then calls {@link #updateField(PSItem, String, String)}.
    *
    * @param dayOfYear the day of the year to set for the date field,
    *    assumed > 0 and < 365.
    */
   private void updateDateField(PSItem item, String fieldName, Date fieldValue,
      int dayOfYear)
   {
      Calendar calendar = Calendar.getInstance();
      calendar.setTime(fieldValue);
      calendar.set(Calendar.DAY_OF_YEAR, dayOfYear);
      PSDateValue dateValue = new PSDateValue(calendar.getTime());

      updateField(item, fieldName, dateValue.getValueAsString());
   }

   /**
    * Convenience method to update a number field. Converts the supplied integer
    * to a string and then calls {@link #updateField(PSItem, String, String)}.
    */
   private void updateNumberField(PSItem item, String fieldName, int fieldValue)
   {
      updateField(item, fieldName, Integer.toString(fieldValue));
   }

   /**
    * Update the identified field for all supplied items.
    *
    * @param item the items to update, assumed not <code>null</code>, may
    *    be empty.
    * @param fieldName the name of the field to update, assumed not
    *    <code>null</code> or empty.
    * @param fieldValue the new field value, may be <code>null</code> or empty.
    */
   private void updateField(PSItem item, String fieldName, String fieldValue)
   {
      PSField[] fields = item.getFields();
      for (PSField field : fields)
      {
         if (field.getName().equalsIgnoreCase(fieldName))
         {
            PSFieldValue[] values = field.getPSFieldValue();
            if (values == null)
            {
               values = new PSFieldValue[1];
               field.setPSFieldValue(values);
            }

            for (PSFieldValue value : values)
            {
               if (value == null)
               {
                  value = new PSFieldValue();
                  values[0] = value;
               }

               value.setRawData(fieldValue);
            }

            break;
         }
      }
   }

   /**
    * Convert the supplied items list into an array of item ids.
    *
    * @param items the items to convert, not <code>null</code>, may be empty.
    * @return an array with all item ids, never <code>null</code>, may be empty.
    */
   public long[] toItemIds(List<PSItem> items)
   {
      if (items == null)
         throw new IllegalArgumentException("items cannot be null");

      long[] ids = new long[items.size()];
      int index = 0;
      for (PSItem item : items)
         ids[index++] = item.getId();

      return ids;
   }

   /**
    * Convert the supplied search results into an array of item ids.
    *
    * @param searchResults the search results to convert, assumed not
    *    <code>null</code>, may be empty.
    * @return an array with all items ids, never <code>null</code>, may be
    *    empty.
    */
   private long[] toItemIds(PSSearchResults[] searchResults)
   {
      long[] ids = new long[searchResults.length];
      int index = 0;
      for (PSSearchResults searchResult : searchResults)
         ids[index++] = searchResult.getId();

      return ids;
   }

   /**
    * Create the specified content type used for testing and sets up runtime
    * access for the current logins default community.
    *
    * @param name the name of the content type to create, not
    *    <code>null</code> or empty, the content editor definition must be
    *    available under <code>UnitTestResources</code>.
    * @param session a valid session, not <code>null</code> or empty.
    * @param acls a container to return all created runtime acl's for the
    *    created content type, not <code>null</code>, may be empty.
    * @return the new content type, never <code>null</code>.
    * @throws Exception for any error.
    */
   public PSContentType createContentType(String name, String session,
      List<PSAclImpl> acls) throws Exception
   {
      if (StringUtils.isBlank(name))
         throw new IllegalArgumentException("name cannot be null or empty");

      if (StringUtils.isBlank(session))
         throw new IllegalArgumentException("session cannot be null or empty");

      if (acls == null)
         throw new IllegalArgumentException("acls cannot be null");

      // create and save the test content types
      PSContentType contentType = createTestContentType(name, session);
      List<PSContentType> contentTypes = new ArrayList<PSContentType>();
      contentTypes.add(contentType);
      saveContentTypes(contentTypes, session, false);

      // setup runtime access for the users default community
      PSCommunity defaultCommunity = getDefaultCommunity();
      acls.addAll(setupCommunityAcls(contentType.getId(), defaultCommunity
         .getName(), session));

      switchToCommunity(defaultCommunity.getName());

      return contentType;
   }

   /**
    * Remove all supplied content types and acls supplied.
    *
    * @param session the user session, not <code>null</code> or empty.
    * @param contentTypes the content types to remove, may be <code>null</code>
    *    or empty.
    * @param acls the acls to remove, may be <code>null</code> or empty.
    * @throws Exception for any error.
    */
   public void cleanUpContentTypes(String session,
      List<PSContentType> contentTypes, List<PSAclImpl> acls) throws Exception
   {
      if (StringUtils.isBlank(session))
         throw new IllegalArgumentException("session cannot be null or empty");

      if (contentTypes != null)
      {
         ContentSOAPStub binding = getBinding(null);
         PSTestUtils.setSessionHeader(binding, session);

         for (PSContentType contentType : contentTypes)
         {
            try
            {
               PSSearchParams searchParams = new PSSearchParams();
               searchParams.setContentType(contentType.getName());
               PSSearch search = new PSSearch();
               search.setUseDbCaseSensitivity(true);
               search.setUseExternalSearchEngine(false);
               search.setPSSearchParams(searchParams);
               FindItemsRequest findItemsRequest = new FindItemsRequest();
               findItemsRequest.setPSSearch(search);
               PSSearchResults[] items = binding.findItems(findItemsRequest);

               long[] deleteItemsRequest = toItemIds(items);
               if (deleteItemsRequest.length != 0)
                  binding.deleteItems(deleteItemsRequest);
            }
            catch (Exception e)
            {
               // ignore and continue with the next content type
            }
         }

         deleteContentTypes(ctypesListToIdArray(contentTypes), session);
      }

      if (acls != null && !acls.isEmpty())
      {
         long[] ids = new long[acls.size()];
         int index = 0;
         for (PSAclImpl acl : acls)
            ids[index++] = acl.getId();

         deleteAcls(ids, session, true);
      }
   }

   /**
    * Convenience method that calls
    * {@link #cleanUpContentTypes(String, List, List)}.
    */
   public void cleanUpContentType(String session, PSContentType contentType,
      List<PSAclImpl> acls) throws Exception
   {
      List<PSContentType> contentTypes = null;

      if (contentType != null)
      {
         contentTypes = new ArrayList<PSContentType>();
         contentTypes.add(contentType);
      }

      cleanUpContentTypes(session, contentTypes, acls);
   }

   /**
    * Remove the specified content type and its items.
    *
    * @param session the user session, not <code>null</code> or empty.
    * @param contentTypeName the name of the content types to remove, not
    *    <code>null</code> or empty.
    * @throws Exception for any error.
    */
   public void cleanUpContentType(String session, String contentTypeName)
      throws Exception
   {
      if (StringUtils.isBlank(session))
         throw new IllegalArgumentException("session may not be null or empty.");
      if (StringUtils.isBlank(contentTypeName))
         throw new IllegalArgumentException(
               "contentTypeName may not be null or empty.");

      PSObjectSummary[] cts = findContentTypes(contentTypeName, session);
      if (cts == null || cts.length == 0)
         return;

      long[] ids = new long[cts.length];
      for (int i=0; i < ids.length; i++)
         ids[i] = cts[i].getId();

      List<PSContentType> contentTypes = loadContentTypes(ids, session, true);
      for (PSContentType ct : contentTypes)
         cleanUpContentType(session, ct, null);
   }

   /**
    * Deletes the specified folder with all its children.
    *
    * @param path the folder path to delete, may be <code>null</code> or empty.
    * @param session the user session, assumed not <code>null</code> or empty.
    * @throws Exception for any error.
    */
   private void cleanUpFolders(String path, String session) throws Exception
   {
      ContentSOAPStub binding = getBinding(null);
      PSTestUtils.setSessionHeader(binding, session);

      if (!StringUtils.isBlank(path))
      {
         LoadFoldersRequest loadRequest = new LoadFoldersRequest();
         loadRequest.setPath(new String[] { path });
         PSFolder[] folders = binding.loadFolders(loadRequest);

         long[] ids = new long[folders.length];
         int index = 0;
         for (PSFolder folder : folders)
            ids[index++] = folder.getId();
         DeleteFoldersRequest deleteRequest = new DeleteFoldersRequest();
         deleteRequest.setId(ids);

         binding.deleteFolders(deleteRequest);
      }
   }

   /**
    * Create the locales de-ch and it-ch, enables and saves them for testing.
    *
    * @param session the session user, assumed not <code>null</code>.
    * @return a list with all test locales, never <code>null</code> or
    *    empty.
    * @throws Exception for any error.
    */
   private List<PSLocale> createTestLocales(String session) throws Exception
   {
      // create locales
      List<PSLocale> locales = new ArrayList<PSLocale>();
      locales.add(createLocale("de-ch", "Swiss German", session));
      locales.add(createLocale("it-ch", "Swiss Italian", session));

      // enable all
      for (PSLocale locale : locales)
         locale.setEnabled(true);

      // and finally save them
      saveLocales(locales, session, true);

      return locales;
   }

   /**
    * Creates an auto tanslation for each supplied locale for testing.
    *
    * @param locales a list with all locales for which to create auto
    *    translations, assumed not <code>null</code>, may be empty.
    * @return a list with all created auto translations of the same size
    *    as the locales list, never <code>null</code>.
    *    These auto translations are not initialized except the locale field.
    */
   private List<PSAutoTranslation> createTestAutoTranslations(
      List<PSLocale> locales)
   {
      List<PSAutoTranslation> autoTranslations = new ArrayList<PSAutoTranslation>();

      for (PSLocale locale : locales)
      {
         PSAutoTranslation autoTranslation = new PSAutoTranslation();
         autoTranslation.setLocale(locale.getCode());
         autoTranslations.add(autoTranslation);
      }
      return autoTranslations;
   }
}
