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
package com.percussion.gwt.pkgmgtui.client.controls;

import com.percussion.gwt.pkgmgtui.client.PSConstants;
import com.percussion.gwt.pkgmgtui.client.PkgMgtUI;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.DragDataAction;
import com.smartgwt.client.widgets.Img;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.grid.ListGrid;
import com.smartgwt.client.widgets.grid.ListGridField;
import com.smartgwt.client.widgets.grid.ListGridRecord;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.VStack;

/**
 * This class creates a generic slush bucket panel.
 * 
 * @author bjoginipally
 * 
 */
public class PSSlushBucketPanel extends HLayout
{

   /**
    * Ctor that calls {@link #PSSlushBucketPanel(String, String)} with
    * <code>null</code> values for grid titles. In this case the default grid
    * titles are used.
    */
   public PSSlushBucketPanel()
   {
      this(null, null);
   }

   /**
    * Ctor of slush bucket panel.
    *
    * grid. May be <code>null</code> or empty.
    * @param leftGridTitle Column title of the left grid, if <code>null</code>
    * or empty, Available is shown as the title column.
    * @param rightGridTitle Column title of the right grid, if <code>null</code>
    * or empty, Selected is shown as the title column.
    */
   public PSSlushBucketPanel(String leftGridTitle, String rightGridTitle)
   {
      super();
      if (leftGridTitle != null && leftGridTitle.trim().length() > 0)
      {
         m_leftGridTitle = leftGridTitle;
      }
      if (rightGridTitle != null && rightGridTitle.trim().length() > 0)
      {
         m_rightGridTitle = rightGridTitle;
      }
      setDefaultGridProperties(m_leftGrid, m_leftGridTitle);
      setDefaultGridProperties(m_rightGrid, m_rightGridTitle);
      this.addMember(m_leftGrid);
      addTransferButtons();
      this.addMember(m_rightGrid);
      this.setWidth100();
      this.setHeight100();
      this.setAlign(Alignment.CENTER);
   }

   /**
    * 
    * @return the string array of selected data from the right grid. Never
    * <code>null</code> may be empty.
    */
   public String[] getSelectedData()
   {
      int totalRows = m_rightGrid.getTotalRows();
      String[] data = new String[totalRows];
      for (int i = 0; i < totalRows; i++)
      {
         ListGridRecord record = m_rightGrid.getRecord(i);
         data[i] = record.getAttribute(ROW_DATA);
      }
      return data;
   }

   /**
    * 
    * @return returns the ListGrid object corresponding to the left grid shown
    * in this panel. Never <code>null</code>.
    */
   public ListGrid getLeftGrid()
   {
      return m_leftGrid;
   }

   /**
    * 
    * @return returns the ListGrid object corresponding to the right grid shown
    * in this panel. Never <code>null</code>.
    */
   public ListGrid getRightGrid()
   {
      return m_rightGrid;
   }

   /**
    * Sets the default properties on the supplied grid.
    * 
    * @param grid The grid object to set the properties, assumed not
    * <code>null</code>.
    */
   private void setDefaultGridProperties(ListGrid grid, String title)
   {
      grid.setWidth("40%");
      grid.setHeight100();
      grid.setCanDragRecordsOut(true);
      grid.setCanAcceptDroppedRecords(true);
      grid.setDragDataAction(DragDataAction.MOVE);
      grid.setCanReorderRecords(true);
      grid.setLeaveScrollbarGap(false);
      ListGridField[] fields = new ListGridField[1];
      fields[0] = new ListGridField(ROW_DATA, title);
      grid.setFields(fields);
   }

   /**
    * Creates the list grid record objects and sets them as data on the supplied
    * grid.
    * 
    * @param leftData assumed not <code>null</code>.
    * @param rightData may be <code>null</code>, if <code>null</code> no data is
    * set.
    * <code>null</code>.
    */
   public void setGridData(String[] leftData, String[] rightData)
   {
      if (leftData == null)
         leftData = new String[0];
      ListGridRecord[] leftRecords = new ListGridRecord[leftData.length];
      for (int i = 0; i < leftData.length; i++)
      {
         ListGridRecord record = new ListGridRecord();
         record.setAttribute(ROW_DATA, leftData[i]);
         leftRecords[i] = record;
      }
      m_leftGrid.setData(leftRecords);
      m_leftGrid.getRecordList().sortByProperty(ROW_DATA, true);

      if (rightData == null)
         rightData = new String[0];
      ListGridRecord[] rightRecords = new ListGridRecord[rightData.length];
      for (int i = 0; i < rightData.length; i++)
      {
         ListGridRecord record = new ListGridRecord();
         record.setAttribute(ROW_DATA, rightData[i]);
         rightRecords[i] = record;
      }
      m_rightGrid.setData(rightRecords);
      m_rightGrid.getRecordList().sortByProperty(ROW_DATA, true);
   }

   /**
    * Adds the data transfer buttons to a {@link VStack} and adds the vstack as
    * a member of this class. Adds the on click handlers also to the transfer
    * buttons.
    */
   private void addTransferButtons()
   {
      Img rightArrow = new Img("icons/32/arrow_right.png", 32, 32);
      rightArrow.setLayoutAlign(Alignment.CENTER);
      rightArrow.addClickHandler(new ClickHandler()
      {
         public void onClick(ClickEvent event)
         {
            transferSelectedData(m_leftGrid, m_rightGrid);
         }
      });
      Img leftArrow = new Img("icons/32/arrow_left.png", 32, 32);
      leftArrow.setLayoutAlign(Alignment.CENTER);
      leftArrow.addClickHandler(new ClickHandler()
      {
         public void onClick(ClickEvent event)
         {
            transferSelectedData(m_rightGrid, m_leftGrid);
         }
      });
      VStack vs = new VStack();
      vs.setWidth(50);
      vs.setHeight(74);
      vs.setMembersMargin(PSConstants.getMembersMargin());
      vs.setLayoutAlign(Alignment.CENTER);
      vs.addMember(rightArrow);
      vs.addMember(leftArrow);
      this.addMember(vs);
   }

   /**
    * The list grid
    * {@link ListGrid#transferSelectedData(com.smartgwt.client.widgets.DataBoundComponent)}
    * method worked fine in smartgwt version 1.01b but failing on smartgwt1.02b
    * version. I added a simple workaround for that method. . Does nothing if
    * selected rows are empty.
    * 
    * @param source The source grid from which the selected rows needs to be
    * transfered to the target grid, assumed not <code>null</code>.
    * @param target The target grid which accepts the selected rows, assumed not
    * <code>null</code>.
    */
   private void transferSelectedData(ListGrid source, ListGrid target)
   {
      ListGridRecord[] recs = source.getSelection();
      if (recs.length < 1)
         return;
      source.removeSelectedData();
      for (ListGridRecord record : recs)
      {
         target.addData(record);
      }
   }

   /**
    * Left list grid object.
    */
   private ListGrid m_leftGrid = new ListGrid();

   /**
    * right list grid object.
    */
   private ListGrid m_rightGrid = new ListGrid();

   /**
    * Default title for the left grid.
    */
   private String m_leftGridTitle = PkgMgtUI.getMessages().availableLabel();

   /**
    * Default title for the right grid.
    */
   private String m_rightGridTitle = PkgMgtUI.getMessages().selectedlabel();

   /**
    * Constant for the row data attribute of the list grid record that are
    * created for each list grid in this panel.
    */
   private static final String ROW_DATA = "RowData";

}
