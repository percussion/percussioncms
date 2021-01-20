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
package com.percussion.gwt.pkgmgtui.client.controls;

import com.google.gwt.user.client.Window;
import com.percussion.gwt.pkgmgtui.client.IPSConstants;
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
      setResizeFrom(IPSConstants.DIALOG_RESIZE_OPTIONS);
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
      setToolbarButtons(hlBtn);
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
