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
package com.percussion.gwt.pkgmgtui.client;

import com.google.gwt.user.client.Window;
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

/**
 * Dialog class for handling the uninstallation of packages.
 * 
 * @author bjoginipally
 * 
 */
public class PSUninstallPackageDialog extends
      Dialog
{
   /**
    * Ctor Creates the dialog with the panels set.
    * 
    */
   PSUninstallPackageDialog()
   {
      super();
      setTitle("Uninstall Package(s)");
      setHeight(DIALOG_HEIGHT);
      setWidth(DIALOG_WIDTH);
      setIsModal(true);
      setCanDragResize(true);
      setResizeFrom(PSConstants.getDialogResizeOptions());
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
    * 
    */
   private void initMessageGrid()
   {
      m_messageGrid.setWidth100();
      m_messageGrid.setHeight100();
      m_messageGrid.setCanReorderRecords(true);
      m_messageGrid.setLeaveScrollbarGap(false);
      m_messageGrid.setFields(createInitalListGridFields());
      m_messageGrid.setCanGroupBy(false);
   }

   private ListGridField[] createInitalListGridFields()
   {
      ListGridField[] fields = new ListGridField[1];
      fields[0] = new ListGridField("Message", "Message");
      return fields;
   }

   /**
    * Creates the button panel with three buttons, finish button is set to hide
    * and it is shown on OK method. Adds the on click handlers to all buttons.
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
      final PSUninstallPackageDialog thisWindow = this;
      m_cancelBtn.addMouseUpHandler(new MouseUpHandler()
      {
         public void onMouseUp(MouseUpEvent event)
         {
            thisWindow.hide();
         }
      });
      m_cancelBtn.setAutoFit(true);
      btnStack.addMember(m_oKBtn);
      m_oKBtn.addMouseUpHandler(new MouseUpHandler()
      {
         public void onMouseUp(MouseUpEvent event)
         {
            onOk();
         }

      });
      m_oKBtn.setAutoFit(true);
      btnStack.addMember(m_finishBtn);
      m_finishBtn.addMouseUpHandler(new MouseUpHandler()
      {
         public void onMouseUp(MouseUpEvent event)
         {
            onFinish();
         }
      });
      m_finishBtn.setAutoFit(true);
      m_finishBtn.hide();
      btnStack.setMembersMargin(PSConstants.getMembersMargin());
      btnStack.setAlign(Alignment.RIGHT);
      btnStack.setHeight(m_oKBtn.getHeight());
      return btnStack;
   }

   /**
    * Override show method so that the open method is called with the package
    * names.
    */
   @Override
   public void show()
   {
      Window.alert("Use open method instead of show.");
   }

   /**
    * Opens the dialog and sets the initial message.
    * 
    * @param parentRecord must not be <code>null</code>.
    * @param finishCallBack the call back that needs to be called when the
    * finish button is clicked.
    * 
    */
   public void open(ListGridRecord parentRecord, DSCallback finishCallBack)
   {
      if (parentRecord == null)
         throw new IllegalArgumentException("parentGrid must not be null");
      if (finishCallBack == null)
         throw new IllegalArgumentException("finishCallBack must not be null");
            
      setHeight(DIALOG_HEIGHT);
      setWidth(DIALOG_WIDTH);
      setAutoCenter(true);
      m_finishCallBack = finishCallBack;
      String selectedPkg = parentRecord.getAttribute("name");
      m_packageName = selectedPkg;
      String category = parentRecord.getAttribute("category");
      if (category.equals("System"))
      {
         m_isSystemPackage = true;
         m_cancelBtn.hide();
      }
      else
      {
         m_isSystemPackage = false;
         m_cancelBtn.show();
      }
      setInitialData();
      super.show();
   }

   /**
    * Creates a list grid record with the alert message for uninstalling the
    * packages and sets that record on the message grid.
    */
   private void setInitialData()
   {
      String msg;
      
      if (m_isSystemPackage)
      {
         msg = m_packageName + " is a system package.  System packages cannot"
            + " be uninstalled.";
      }
      else
      {
         msg = "Are you sure you want to completely remove "
            + m_packageName
            + " and all of its components and configuration files?";
      }
      
      ListGridRecord rec = new ListGridRecord();
      rec.setAttribute("Message", msg);
      ListGridRecord[] recs = { rec };
      m_messageGrid.setWrapCells(true);
      m_messageGrid.setFixedRecordHeights(false);
      m_messageGrid.setFields(createInitalListGridFields());
      m_messageGrid.setData(recs);
      m_depsChecked = false;
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
      if (m_isSystemPackage)
      {
         this.hide();
      }
      else if (!m_depsChecked)
      {
         checkDependencies();
      }
      else
      {
         updateData();
      }
   }

   /**
    * Calls the rest service to check the package dependencies, if there are
    * dependencies then shows them to the user otherwise calls
    * {@link #updateData()} to uninstall the package and its components.
    */
   private void checkDependencies()
   {
      RestDataSource restDs = new RestDataSource();
      restDs.setFields(createDsFields());
      restDs.setUpdateDataURL(m_checkDepsUrl + m_packageName);
      restDs.setXmlRecordXPath("/Messages/Message");
      restDs.updateData(new ListGridRecord(), new DSCallback()
      {
         public void execute(DSResponse response, Object rawData,
               DSRequest request)
         {
            m_depsChecked = true;
            Record[] records = response.getData();
            if (records.length < 1)
            {
               updateData();
            }
            else
            {
               m_messageGrid.setFields(createFields());
               m_messageGrid.setWrapCells(true);
               m_messageGrid.setFixedRecordHeights(false);
               m_messageGrid.setData(records);
            }
         }
      });

   }

   /**
    * Calls the rest service to process uninstall request and creates the
    * listgrid data from the returned results and sets them on the message grid.
    * Hides the OK and cancel buttons and shows the finish button.
    */
   private void updateData()
   {
      RestDataSource restDs = new RestDataSource();
      restDs.setFields(createDsFields());
      restDs.setUpdateDataURL(m_updateUrl + m_packageName);
      restDs.setXmlRecordXPath("/Messages/Message");
      restDs.updateData(new ListGridRecord(), new DSCallback()
      {
         public void execute(DSResponse response, Object rawData,
               DSRequest request)
         {
            Record[] records = response.getData();
            m_messageGrid.setFields(createFields());
            m_messageGrid.setWrapCells(true);
            m_messageGrid.setFixedRecordHeights(false);
            m_messageGrid.setData(records);
            m_cancelBtn.hide();
            m_oKBtn.hide();
            m_finishBtn.show();
         }
      });
   }

   /**
    * Helper method to create the data source fields that match with the xml
    * produced by the uninstall call to the server. The first column shows the
    * type of message, second and third column will show the package name and
    * message body respectively.
    * 
    * @return array of datasource fields, never <code>null</code> or empty.
    */
   private DataSourceField[] createDsFields()
   {
      DataSourceField[] fields = new DataSourceField[2];
      DataSourceImageField typeField = new DataSourceImageField("type", "Type");
      typeField.setWidth(25);
      fields[0] = typeField;
      DataSourceTextField msgBody = new DataSourceTextField("body", "Message");
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
      fields[1] = new ListGridField("body", "Message");

      return fields;

   }

   /**
    * Resets the buttons (shows cancel and ok buttons and hides finish button)
    * visibility and hides the dialog.
    */
   private void onFinish()
   {
      m_oKBtn.show();
      m_cancelBtn.show();
      m_finishBtn.hide();
      m_finishCallBack.execute(null, null, null);
      this.hide();
   }

   /**
    * Name of the package that needs to be uninstalled, initialized in open
    * method and never <code>null</code> after that.
    */
   private String m_packageName;

   /**
    * Indicates if the selected package is a system package, initialized in open
    * method.
    */
   private boolean m_isSystemPackage;

   // Controls for this dialog
   private IButton m_cancelBtn = new IButton(PkgMgtUI.getMessages().cancel());

   private IButton m_oKBtn = new IButton(PkgMgtUI.getMessages().OK());

   private IButton m_finishBtn = new IButton("Finish");

   private ListGrid m_messageGrid = new ListGrid();

   private DSCallback m_finishCallBack = null;

   private boolean m_depsChecked = false;

   private static final  String  m_checkDepsUrl = PkgMgtUI.ms_serviceRoot
         + "checkPackageDependencies.xml?packageName=";

   private static final  String m_updateUrl = PkgMgtUI.ms_serviceRoot
         + "uninstallPackage.xml?packageName=";

   private static final int DIALOG_HEIGHT = 300;

   private static final int DIALOG_WIDTH = 500;

}
