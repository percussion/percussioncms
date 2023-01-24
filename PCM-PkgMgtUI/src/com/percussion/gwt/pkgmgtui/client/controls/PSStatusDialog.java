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
package com.percussion.gwt.pkgmgtui.client.controls;

import com.google.gwt.user.client.Window;
import com.percussion.gwt.pkgmgtui.client.PSConstants;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.Overflow;
import com.smartgwt.client.types.VerticalAlignment;
import com.smartgwt.client.widgets.Dialog;
import com.smartgwt.client.widgets.HTMLPane;
import com.smartgwt.client.widgets.IButton;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.layout.HLayout;

/**
 * A generic status dialog. Shows the message and title with OK button. Usage,
 * create an instance of this class and call open method with the message and
 * title.
 * 
 * @author bjoginipally
 * 
 */
public class PSStatusDialog extends Dialog
{
   /**
    * Ctor, Creates a label and a button and adds them to this dialog.
    * 
    */
   public PSStatusDialog()
   {
      super();
      setWidth(DIALOG_WIDTH);
      setHeight(DIALOG_HEIGHT);
      setIsModal(true);
      setCanDragReposition(true);
      setCanDragResize(true);
      setResizeFrom(PSConstants.getDialogResizeOptions());
      setAutoCenter(true);
      setShowToolbar(true);
      HLayout hlLabel = new HLayout();
      hlLabel.setHeight100();
      hlLabel.setWidth100();
      hlLabel.setOverflow(Overflow.AUTO);
      hlLabel.setBorder(MESSAGE_BORDER_STYLE);
      hlLabel.setMargin(10);
      m_label.setOverflow(Overflow.AUTO);
      m_label.setPadding(2);
      HLayout hlBtn = new HLayout();
      hlBtn.setHeight100();
      hlBtn.setWidth100();
      hlBtn.setAlign(Alignment.RIGHT);
      
      IButton okBtn = new IButton("OK");
      okBtn.addClickHandler(new ClickHandler()
      {
         public void onClick(ClickEvent event)
         {
            hide();
         }
      });
      hlLabel.addMember(m_label);
      hlBtn.addMember(okBtn);
      addItem(hlLabel);
      addItem(hlBtn);
   }

   @Override
   public void show()
   {
      Window.alert("Use open method instead of show.");
   }

   /**
    * Sets the title and message of the dialog and shows it.
    * 
    * @param title The title of the message dialog.
    * @param message The message that needs to be shown.
    */
   public void open(String title, String message)
   {
      setWidth(DIALOG_WIDTH);
      setHeight(DIALOG_HEIGHT);
      setAutoCenter(true);
      setTitle(title);
      m_label.setContents(message);
      super.show();
   }

   /**
    * The label element in which the message is shown.
    */
   private HTMLPane m_label = new HTMLPane();

   private static final String MESSAGE_BORDER_STYLE = "1px solid #D4D4D4";
   private static final int DIALOG_HEIGHT = 300;
   private static final int DIALOG_WIDTH = 500;


}
