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
package com.percussion.gwt.pkgmgtui.client;

import com.smartgwt.client.data.DSCallback;
import com.smartgwt.client.data.DSRequest;
import com.smartgwt.client.data.DSResponse;
import com.smartgwt.client.data.DataSourceField;
import com.smartgwt.client.data.Record;
import com.smartgwt.client.data.RestDataSource;
import com.smartgwt.client.data.fields.DataSourceImageField;
import com.smartgwt.client.data.fields.DataSourceTextField;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.ListGridFieldType;
import com.smartgwt.client.types.Overflow;
import com.smartgwt.client.widgets.Dialog;
import com.smartgwt.client.widgets.IButton;
import com.smartgwt.client.widgets.events.MouseUpEvent;
import com.smartgwt.client.widgets.events.MouseUpHandler;
import com.smartgwt.client.widgets.grid.ListGrid;
import com.smartgwt.client.widgets.grid.ListGridField;
import com.smartgwt.client.widgets.grid.ListGridRecord;
import com.smartgwt.client.widgets.layout.HLayout;

public class PSConvertToSourceDialog extends Dialog
{
   /**
    * Ctor Creates the dialog with the panels set.
    * 
    */
   PSConvertToSourceDialog()
   {
      super();
      setTitle("Convert Package To Source");
      setHeight(DIALOG_HEIGHT);
      setWidth(DIALOG_WIDTH);
      setIsModal(true);
      setCanDragResize(true);
      setResizeFrom(IPSConstants.DIALOG_RESIZE_OPTIONS);
      setCanDragReposition(true);
      setAutoCenter(true);
      setShowToolbar(true);
      initMessageGrid();
      HLayout hlLabel = new HLayout();
      hlLabel.setHeight100();
      hlLabel.setWidth100();
      hlLabel.setOverflow(Overflow.AUTO);
      hlLabel.addMember(m_messageGrid);
      hlLabel.setPadding(10);
      addItem(hlLabel);
      addItem(createButtonsPanel());

   }

   /**
    * Sets the properties of message grid and adds the list grid fields also.
    */
   private void initMessageGrid()
   {
      m_messageGrid.setWidth100();
      m_messageGrid.setHeight100();
      m_messageGrid.setCanReorderRecords(true);
      m_messageGrid.setLeaveScrollbarGap(false);
      m_messageGrid.setFields(createFields());
      m_messageGrid.setCanGroupBy(false);
   }


   /**
    * Creates the button panel with OK and cancel buttons. Adds the on click
    * handlers to these buttons.
    * 
    * @return the horizontal stack with the buttons placed on it, never
    * <code>null</code>.
    */
   private HLayout createButtonsPanel()
   {
      HLayout btnStack = new HLayout();
      btnStack.setWidth100();
      btnStack.setHeight100();
      btnStack.addMember(m_cancelBtn);
      final PSConvertToSourceDialog thisWindow = this;
      m_cancelBtn.addMouseUpHandler(new MouseUpHandler()
      {
         public void onMouseUp(MouseUpEvent event)
         {
            thisWindow.hide();
         }
      });
      btnStack.addMember(m_oKBtn);
      m_oKBtn.addMouseUpHandler(new MouseUpHandler()
      {
         public void onMouseUp(MouseUpEvent event)
         {
            onOk();
         }

      });
      btnStack.setMembersMargin(IPSConstants.MEMBERS_MARGIN);
      btnStack.setAlign(Alignment.RIGHT);
      btnStack.setHeight(m_oKBtn.getHeight());
      return btnStack;
   }

   /**
    * Handles OK button click, The OK button is shown to the user with the
    * initial warning and dependency warnings if any. If the
    * {@link #m_depsChecked} is false, then calls the
    * {@link #checkDependencies()} method otherwise calls {@link #updateData()}.
    * 
    */
   private void onOk()
   {
      if(!m_isProcessed)
      {
         convertPackage();
         m_isProcessed = true;
         m_cancelBtn.hide();
      }
      else
      {
         m_finishCallBack.execute(null, null, null);
         m_cancelBtn.show();
         this.hide();
      }
   }

   /**
    * Makes a rest service call to the server to convert the package to source.
    * Shows the status message on the call back.
    */
   private void convertPackage()
   {
      RestDataSource restDs = new RestDataSource();
      restDs.setFields(createDsFields());
      restDs.setXmlRecordXPath("/Response");
      restDs.setRecordXPath("/Response");
      restDs.setUpdateDataURL(m_updateUrl + m_packageName);
      restDs.updateData(new ListGridRecord(), new DSCallback()
      {
         public void execute(DSResponse response, Object rawData,
               DSRequest request)
         {
            Record[] records = response.getData();
            m_messageGrid.setWrapCells(true);
            m_messageGrid.setFixedRecordHeights(false);
            m_messageGrid.setData(records);
         }
      });
      
   }
   
   /**
    * Opens the dialog and sets the initial message.
    * 
    * @param parentRecord must not be <code>null</code>.
    * @param finishCallBack the call back that needs to be called when the
    * OK button is clicked after converting the package.
    * 
    */
   public void open(ListGridRecord parentRecord, DSCallback finishCallBack)
   {
      if (parentRecord == null)
         throw new IllegalArgumentException("parentGrid must not be null");
      if (finishCallBack == null)
         throw new IllegalArgumentException("finishCallBack must not be null");
      m_isProcessed = false;
      setHeight(DIALOG_HEIGHT);
      setWidth(DIALOG_WIDTH);
      setAutoCenter(true);
      m_finishCallBack = finishCallBack;
      String selectedPkg = parentRecord.getAttribute("name");
      m_packageName = selectedPkg;
      setInitialData();
      super.show();
   }

   /**
    * Creates a list grid record with the appropriate message based on the lock
    * status of the selected package and sets that record on the message grid.
    */
   private void setInitialData()
   {
      String msg = "After converting deployed package to source package, " +
            "the package will no longer be configurable by the " +
            "configuration files, nor it can be managed by \"Package Manager\".";
      String type="Warning";
      
      ListGridRecord rec = new ListGridRecord();
      rec.setAttribute("message", msg);
      rec.setAttribute("type", type);
      ListGridRecord[] recs = { rec };
      m_messageGrid.setWrapCells(true);
      m_messageGrid.setFixedRecordHeights(false);
      m_messageGrid.setData(recs);
   }
   
   /**
    * Helper method to create the data source fields that match with the xml
    * produced by the convert call to the server. The first column shows the
    * type of message and the second column will show the message body.
    * 
    * @return array of datasource fields, never <code>null</code> or empty.
    */
   private DataSourceField[] createDsFields()
   {
      DataSourceField[] fields = new DataSourceField[2];
      DataSourceImageField typeField = new DataSourceImageField("type", "Type");
      typeField.setWidth(25);
      fields[0] = typeField;
      DataSourceTextField msgBody = new DataSourceTextField("message", "Message");
      fields[1] = msgBody;

      return fields;
   }

   /**
    * Helper method to create the list grid fields corresponding to the data
    * source fields.
    * 
    * @return list grid field array, never <code>null</code>.
    */
   private ListGridField[] createFields()
   {
      ListGridField[] fields = new ListGridField[2];
      fields[0] = new ListGridField("type", "Type", 80);
      fields[0].setAlign(Alignment.CENTER);
      fields[0].setType(ListGridFieldType.IMAGE);
      fields[0].setImageURLPrefix("icons/16/");
      fields[0].setImageURLSuffix(".png");
      fields[1] = new ListGridField("message", "Message");

      return fields;

   }
   
   /**
    * Name of the package that needs to be converted to source, initialized in
    * open method and never <code>null</code> after that.
    */
   private String m_packageName;

   /**
    * Call back function after the OK button is pressed after the conversion is
    * done.
    */
   private DSCallback m_finishCallBack = null;

   /**
    * Rest url for converting the package.
    */
   private final String m_updateUrl = PkgMgtUI.ms_serviceRoot
   + "convertPackage.xml?packageName=";

   /**
    * Flag to indicate that the conversion is processed or not.
    */
   private boolean m_isProcessed = false;

   //Height and width of the dialog
   private int DIALOG_HEIGHT = 300;
   private int DIALOG_WIDTH = 500;


   // Controls for this dialog
   private IButton m_cancelBtn = new IButton(PkgMgtUI.getMessages().cancel());
   private IButton m_oKBtn = new IButton(PkgMgtUI.getMessages().OK());
   private ListGrid m_messageGrid = new ListGrid();
   

}
