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

package com.percussion.cx;

import com.percussion.webservices.security.data.PSLocale;
import com.percussion.webservices.security.data.PSLogin;
import org.apache.log4j.Logger;

import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.Vector;


public class PSContentExplorerHeader extends JPanel
{
   static Logger log = Logger.getLogger(PSContentExplorerHeader.class);
 
   private static final long serialVersionUID = 1L;

   private PSContentExplorerApplet m_applet;
   /**
    * @param applet 
	 *
	 */
   public PSContentExplorerHeader(PSContentExplorerApplet applet)
   {
      super();
      this.setName("Top Banner");
      this.setFocusable(true);
      this.setFocusTraversalKeysEnabled(true);
      m_applet = applet;   
      init();
      
   }

   protected void init()
   {
      
      this.setFont(m_applet.getOptionsManager()
            .getDisplayOptions().getMenuFont());
     
      this.setBackground(PSCxUtil.getWindowBkgColor(m_applet));
     
      PSLogin loginInfo = PSCESessionManager.getInstance().getLoginInfo();
     
      // get the locale list, put them in an array and set
      Vector<String> model = new Vector<String>();
      
     
      for (PSLocale localeInt : loginInfo.getLocales())
      {
         model.addElement(localeInt.getLabel());
      }

      this.setLayout(new BorderLayout());
  
      PSHeaderBanner panel1 = new PSHeaderBanner(m_applet);
      panel1.setPreferredSize(new Dimension(200,70));
     
      add(panel1, BorderLayout.CENTER); // add component to the ContentPane
  

      if (m_applet.getView().equals(PSUiMode.TYPE_VIEW_CX))
      {
         JPanel panel2 = new PSHeaderInfoPanel(m_applet);
         
         panel2.setName("Header info");  
         panel2.setFocusable(true);
         add(panel2,BorderLayout.EAST); // add component to the ContentPane
      }

      addFocusListener(new FocusAdapter() {
         public void focusGained(FocusEvent e) {
            transferFocus();
         }
      });
   }
   
  
}
