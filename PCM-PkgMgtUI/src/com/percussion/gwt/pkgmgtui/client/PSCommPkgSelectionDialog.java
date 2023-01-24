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
package com.percussion.gwt.pkgmgtui.client;

import com.google.gwt.user.client.Window;
import com.percussion.gwt.pkgmgtui.client.controls.PSSlushBucketPanel;
import com.smartgwt.client.data.DSCallback;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.Overflow;
import com.smartgwt.client.widgets.Dialog;
import com.smartgwt.client.widgets.IButton;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.layout.HLayout;

/**
 * This class handles Community and Package two way associations.
 * 
 */
public class PSCommPkgSelectionDialog extends Dialog
{

   /**
    * ctor
    * 
    */
   public PSCommPkgSelectionDialog()
   {
      super();
      m_slushBucket.setAlign(Alignment.CENTER);
      setIsModal(true);
      setCanDragReposition(true);
      setCanDragResize(true);
      setResizeFrom(PSConstants.getDialogResizeOptions());
      setAutoCenter(true);
      setShowToolbar(true);
      
      HLayout btnLayout = new HLayout();
      btnLayout.addMember(m_cancelBtn);
      btnLayout.addMember(m_oKBtn);
      btnLayout.setWidth100();
      btnLayout.setHeight100();
      btnLayout.setAlign(Alignment.RIGHT);
      
      btnLayout.setMembersMargin(PSConstants.getMembersMargin());
      btnLayout.setAlign(Alignment.RIGHT);
      btnLayout.setHeight(m_oKBtn.getHeight());

      HLayout sbLayout = new HLayout();
      sbLayout.setMembersMargin(PSConstants.getMembersMargin());
      sbLayout.addMember(m_slushBucket);
      sbLayout.setAlign(Alignment.CENTER);
      sbLayout.setWidth100();
      sbLayout.setHeight100();
      sbLayout.setOverflow(Overflow.AUTO);
      sbLayout.setPadding(PSConstants.getMembersMargin());
      this.addItem(sbLayout);

      m_cancelBtn.addClickHandler(new ClickHandler()
      {
         public void onClick(ClickEvent event)
         {
            onCancel();
         }
      });

      m_oKBtn.addClickHandler(new ClickHandler()
      {
         public void onClick(ClickEvent event)
         {
            onOk();
         }
      });

      addItem(btnLayout);

   }

   /**
    * Hides the dialog.
    */
   private void onCancel()
   {
      this.hide();
   }

   /**
    * Calls the OK callback and it is callers responsibility to hide the dialog.
    * 
    */
   private void onOk()
   {
      m_okCallback.execute(null, getSelectedData(), null);
   }

   /**
    * Sets the dialog data and opens the dialog.
    * 
    * @param available String array of available items, may be <code>null</code>
    * or empty.
    * @param selected String array of selected items, may be <code>null</code>
    * or empty.
    * @param okCallBack the call back function that needs to be called on ok
    * button click.
    */
   public void open(String[] available, String[] selected,
         DSCallback okCallBack)
   {
      
      if (available == null)
         available = new String[0];
      if (selected == null)
         selected = new String[0];
      m_slushBucket.setGridData(available, selected);
      m_okCallback = okCallBack;
      super.show();
   }

   /**
    * Returns the selected data as a {@link PkgMgtUI#NAME_SEPARATOR} separated
    * String.
    * 
    * @return The selected data may be empty never <code>null</code>.
    */
   public String getSelectedData()
   {
      String selData = "";
      String[] data = m_slushBucket.getSelectedData();
      for (int i = 0; i < data.length; i++)
      {
         selData += data[i];
         if (i != data.length - 1)
            selData += PkgMgtUI.NAME_SEPARATOR;
      }
      return selData;
   }

   @Override
   public void show()
   {
      Window.alert("Use open method instead of show.");
   }

   // Controls of this form.

   private PSSlushBucketPanel m_slushBucket = new PSSlushBucketPanel();

   private IButton m_cancelBtn = new IButton(PkgMgtUI.getMessages().cancel());

   private IButton m_oKBtn = new IButton(PkgMgtUI.getMessages().OK());

   private DSCallback m_okCallback = null;
}
