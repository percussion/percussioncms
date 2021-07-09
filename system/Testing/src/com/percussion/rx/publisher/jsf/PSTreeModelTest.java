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
package com.percussion.rx.publisher.jsf;

import com.percussion.rx.jsf.PSCategoryNodeBase;
import com.percussion.rx.jsf.PSNavigation;
import com.percussion.rx.jsf.PSTreeModel;
import com.percussion.rx.publisher.jsf.beans.PSDesignNavigation;
import com.percussion.rx.publisher.jsf.nodes.PSDeliveryTypeNode;
import com.percussion.rx.publisher.jsf.nodes.PSSiteContainerNode;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.services.publisher.IPSDeliveryType;
import com.percussion.services.publisher.data.PSDeliveryType;
import com.percussion.utils.guid.IPSGuid;

import junit.framework.TestCase;

/**
 * Create a tree and walk the tree using the tree model.
 * 
 * @author dougrand
 */
public class PSTreeModelTest extends TestCase
{
   /**
    * Tree model used in the tests. The tests have to be run from first to last.
    */
   public static PSTreeModel ms_model = null;
   
   /**
    * 
    */
   public static IPSGuid ms_fs_guid;

   /**
    * 
    */
   public static IPSGuid ms_ftp_guid;

   /**
    * 
    */
   public static IPSGuid ms_sftp_guid;
   
   public void testDummy()
   {
      //Added a dummy test all the tests in the file have been ignored, this test needs to be removed after fixing other tests
   }
   /**
    * Create the initial model
    */
   public void ignoretestCreateTree()
   {
      IPSGuidManager gmgr = PSGuidManagerLocator.getGuidMgr();
      PSNavigation nav = new PSDesignNavigation();
      PSCategoryNodeBase root = new PSCategoryNodeBase("root", null);
      ms_model = new PSTreeModel(root, nav);
      
      root.addNode(new PSSiteContainerNode("Sites", true));
      
      ms_fs_guid = gmgr.makeGuid(100, PSTypeEnum.DELIVERY_TYPE);
      ms_ftp_guid = gmgr.makeGuid(101, PSTypeEnum.DELIVERY_TYPE);
      ms_sftp_guid = gmgr.makeGuid(102, PSTypeEnum.DELIVERY_TYPE);
      
      PSCategoryNodeBase dtypes = new PSCategoryNodeBase("Delivery Types", 
         "pub-design-delivery-types-view"); 
      root.addNode(dtypes);
      dtypes.addNode(createDeliveryTypeNode("filesystem", ms_fs_guid));
      dtypes.addNode(createDeliveryTypeNode("ftp", ms_ftp_guid));
      dtypes.addNode(createDeliveryTypeNode("sftp", ms_sftp_guid));   
      PSCategoryNodeBase contexts = new PSCategoryNodeBase("Contexts", 
         "pub-design-contexts-view"); 
      root.addNode(contexts);
   }
   
   /**
    * Creates a {@link PSDeliveryTypeNode} from given title and id.
    * @param title the title of the node, assumed not <code>null</code> or empty.
    * @param id the id of the node, assumed not <code>null</code>.
    * @return the created node, never <code>null</code>.
    */
   private PSDeliveryTypeNode createDeliveryTypeNode(String title, IPSGuid id)
   {
      IPSDeliveryType dtype = new PSDeliveryType();
      dtype.setName(title);
      dtype.setGUID(id);
      return new PSDeliveryTypeNode(dtype);
   }
   
   /**
    * The category node has a model of indexing and finding nodes by key.
    */
   public void ignoretestCategoryNode()
   {
      ms_model.setRowIndex(1); // Delivery types
      PSCategoryNodeBase dtypes = (PSCategoryNodeBase) ms_model.getRowData();
      assertTrue(dtypes.isContainer());
      assertEquals(3, dtypes.getRowCount());
      dtypes.setRowIndex(0);
      assertEquals(ms_fs_guid.toString(), dtypes.getRowKey());
      dtypes.setRowIndex(1);
      assertEquals(ms_ftp_guid.toString(), dtypes.getRowKey());
      dtypes.setRowIndex(2);
      assertEquals(ms_sftp_guid.toString(), dtypes.getRowKey());
      
      dtypes.setRowKey(ms_fs_guid);
      assertEquals(0, dtypes.getRowIndex());
      dtypes.setRowKey(ms_ftp_guid);
      assertEquals(1, dtypes.getRowIndex());
      dtypes.setRowKey(ms_sftp_guid);
      assertEquals(2, dtypes.getRowIndex());      
   }
   
   /**
    * Walk the container hierarchy and test the rowkeys
    */
   public void ignoretestEnterLeaveContainer()
   {
      ms_model.setRowKey("pub-design-contexts-view");
      assertEquals("[pub-design-contexts-view]", ms_model.getRowKey().toString());
      assertEquals(3, ms_model.getRowCount());
      assertEquals(0, ms_model.getDepth());
      ms_model.setRowKey(null);
      ms_model.setRowIndex(0);
      assertEquals("[pub-design-site-views]", ms_model.getRowKey().toString());
      ms_model.enterContainer();
      assertTrue(ms_model.getRowCount() > 0);
      ms_model.setRowIndex(0);
      assertEquals(1, ms_model.getDepth());
      ms_model.exitContainer();
   }
   
   
}
