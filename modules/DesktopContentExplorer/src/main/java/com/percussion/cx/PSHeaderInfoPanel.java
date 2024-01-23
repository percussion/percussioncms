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

import com.percussion.cx.guitools.UTMnemonicLabel;
import com.percussion.webservices.security.data.PSCommunity;
import com.percussion.webservices.security.data.PSLocale;
import com.percussion.webservices.security.data.PSLogin;
import com.percussion.webservices.security.data.PSRole;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class PSHeaderInfoPanel extends JPanel
{

   /**
    * 
    */
   private static final long serialVersionUID = 5452851707296139396L;
   

   private Icon updateIcon = null;

   private Map<String, Integer> communityNameMap = new HashMap<String, Integer>();

   private Map<String, String> localeNameToCodeMap = new HashMap<String, String>();

   private Map<String, String> localeCodeToNameMap = new HashMap<String, String>();

   private PSCESessionManager sessionManager;

   private PSContentExplorerApplet m_applet;
   
   private ResourceBundle m_res;
   
   static Logger log = Logger.getLogger(PSHeaderInfoPanel.class);
   
   public PSHeaderInfoPanel(PSContentExplorerApplet applet)
   {

      m_res = applet.getResources();
      
      this.getAccessibleContext().setAccessibleName(m_res.getString("headerinfo.acc.name"));
      m_applet = applet;
      sessionManager = PSCESessionManager.getInstance();
      String user = sessionManager.getUserName();
      PSLogin loginInfo = sessionManager.getLoginInfo();
      
      // Create comma separated list of roles
      String roles = Arrays.stream(loginInfo.getRoles())
            .map(PSRole::getName)
            .collect(Collectors.joining(", "));

      getLocaleList();
      getCommunityList();

      updateIcon = PSImageIconLoader.loadIcon("update");

      setLayout(new GridBagLayout());
      GridBagConstraints c = new GridBagConstraints();

      // Sizing for the panel
      super.setFocusable(true);

      setBackground(PSCxUtil.getWindowBkgColor(m_applet));
      this.setMaximumSize(new Dimension(350, 70));
      this.setPreferredSize(new Dimension(350, 70));

      // Spacing to right of labels
      Insets labelInsets = new Insets(0, 0, 0, 10); // 10 pixel spacce to right

      // Add values
      LinkedHashMap<String, String> data = new LinkedHashMap<>();
      data.put("headerinfo.user", user);
      data.put("headerinfo.roles", roles);
      data.put("headerinfo.community", getCurrentCommunity());
      data.put("headerinfo.locale", getCurrentLocale());

      // Add buttons
      HashMap<String, JButton> buttons = new HashMap<>();
      buttons.put("headerinfo.user", createLogoutButton(e -> {
         int dialogButton = JOptionPane.YES_NO_OPTION;
         String message = m_res.getString("headerinfo.logout.confirm");
         int dialogResult = JOptionPane.showConfirmDialog(null,message, message, dialogButton);
         if (dialogResult == JOptionPane.YES_OPTION)
         {
            PSContentExplorerApplication.getBaseFrame().logout();
         }

      }));

      c.gridy = 0;
      c.fill = GridBagConstraints.HORIZONTAL;

      for (Entry<String, String> item : data.entrySet())
      {
         String key = item.getKey();
         
         JComponent value = null;
         
         if (key.equals("headerinfo.community"))
         {
            value = createEditButton(key,item.getValue(), communityDialog());
         }
         else if (key.equals("headerinfo.locale"))
            value = createEditButton(key,item.getValue(), localeDialog());
         else
         {
            value = new JLabel(item.getValue());
            value.setToolTipText(item.getValue());
            value.getAccessibleContext().setAccessibleName(m_res.getString(key));
            value.getAccessibleContext().setAccessibleDescription(item.getValue());
         }  
         
         value.setFocusable(true);
         
         
        
            
         // Force bold font
         UTMnemonicLabel label = new UTMnemonicLabel(m_res,key,value)
         {
            // Override set font to force to bold regardless of display options
            @Override
            public void setFont(Font f)
            {
               super.setFont(f.deriveFont(f.getStyle() | Font.BOLD));
            }
         };
         label.setFocusable(true);

         
         // Add label
         c.gridheight = 1;
         c.gridx = 0;
         c.fill = GridBagConstraints.NONE;
         c.weightx = 0;
         c.anchor = GridBagConstraints.LINE_END;

         label.setLabelFor(value);
         c.insets = labelInsets;
         add(label, c);
         // Add value

         c.weightx = 1.0;
         c.gridx = 1;
         c.anchor = GridBagConstraints.LINE_START;
         c.fill = GridBagConstraints.HORIZONTAL;
         add(value, c);
         // Add optional button
         JButton button = buttons.get(item.getKey());

         if (button != null)
         {
            c.weightx = 0;
            c.anchor = GridBagConstraints.LINE_END;
            c.fill = GridBagConstraints.NONE;
            c.gridx = 2;
            c.gridheight = 2;
            add(button, c);
         }
         c.gridy++;
      }

   }

   private ActionListener communityDialog()
   {
      return e -> {
         String[] choices = getCommunityList().toArray(new String[]
         {});
         String currentCommunity = getCurrentCommunity();
         int index = 0;
         for (int i = 0; i < choices.length; i++)
         {
            if (choices[i].equals(currentCommunity))
            {
               index = i;
               break;
            }
         }
         String message = m_applet.getResources().getString("headerinfo.changecomm");
         String input = (String) JOptionPane.showInputDialog(this, message,
               message, JOptionPane.QUESTION_MESSAGE, null, // Use
               // default
               // icon
               choices, // Array of choices
               choices[index]); // Initial choice
         changeCommunity(input);
      };
   }

   private ActionListener localeDialog()
   {
      return e -> {
         String[] choices = getLocaleList().toArray(new String[]
         {});
         String locale = getCurrentLocale();
         int index = 0;
         for (int i = 0; i < choices.length; i++)
         {
            if (localeNameToCodeMap.get(choices[i]).equals(locale))
            {
               index = i;
               break;
            }
         }
         String message = m_applet.getResources().getString("headerinfo.changeloc");
         String input = (String) JOptionPane.showInputDialog(this,message,
               message, JOptionPane.QUESTION_MESSAGE, null, // Use
               choices, // Array of choices
               choices[index]); // Initial choice
         changeLocale(localeNameToCodeMap.get(input));
      };
   }

   private JButton createLogoutButton(ActionListener al)
   {
      String mn = m_res.getString("mn_headerinfo.logout");
      String butText = m_res.getString("headerinfo.logout");
      JButton logout = new JButton(butText + " ("+mn+")");
      logout.setContentAreaFilled(false);
      logout.getAccessibleContext().setAccessibleName(butText);
      logout.getAccessibleContext().setAccessibleName(m_res.getString("headerinfo.acc.logout.description"));
      
      logout.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.BLACK, 1),
            BorderFactory.createEmptyBorder(1, 1, 1, 1)));
      logout.addActionListener(al);
      logout.setMnemonic(mn.charAt(0));

      return logout;
   }

   private JButton createEditButton(String key, String value, ActionListener al)
   {
      JButton button = new JButton(value);
      button.setToolTipText(value);
      button.getAccessibleContext().setAccessibleDescription(m_res.getString("headerinfo.edittext") +" "+ m_res.getString(key));
      button.setIcon(updateIcon);
      button.setContentAreaFilled(false);
      button.setBorder(null);
      button.setHorizontalAlignment(SwingConstants.LEFT);
      button.addActionListener(al);
      return button;
   }

  
   private ArrayList<String> getCommunityList()
   {
      PSLogin loginInfo = sessionManager.getLoginInfo();
      ArrayList<String> returnList = new ArrayList<>();

      PSCommunity[] communities = loginInfo.getCommunities();
      for (int i = 0; i < communities.length; i++)
      {
         PSCommunity community = communities[i];
         returnList.add(community.getName());
         communityNameMap.put(community.getName(), PSGuid.getId(community.getId()));
      }

      return returnList;
   }

   private ArrayList<String> getLocaleList()
   {
      PSLogin loginInfo = sessionManager.getLoginInfo();
      ArrayList<String> returnList = new ArrayList<>();

      PSLocale[] locales = loginInfo.getLocales();

      for (int i = 0; i < locales.length; i++)
      {
         PSLocale locale = locales[i];
         returnList.add(locale.getLabel());
         localeCodeToNameMap.put(locale.getCode(), locale.getLabel());
         localeNameToCodeMap.put(locale.getLabel(), locale.getCode());
      }

      return returnList;
   }

   private void changeCommunity(String community)
   {

      Integer communityid = communityNameMap.get(community);
      if (communityid != null)
      {
         try
         {
            Map<String, String> params = new HashMap<String, String>();
            params.put("updateLangCom", "true");
            Document result = m_applet.getXMLDocument("../sys_welcome/login.txt?sys_community=" + communityid, params);
            String rootNodeName = result.getDocumentElement().getNodeName();
            if (rootNodeName.equals("redirect"))
            {
               Dimension dim = new Dimension(m_applet.getWidth(), m_applet.getHeight());
               PSContentExplorerApplication.setDimension(dim);

               PSLogin loginInfo = sessionManager.getLoginInfo();
               loginInfo.setDefaultCommunity(community);

               SwingUtilities.invokeLater(() -> PSContentExplorerApplication.getBaseFrame().reload());
            }
         }
         catch (IOException | SAXException | ParserConfigurationException e)
         {
            log.error("Cannot change Community",e);
         }

      }

   }

   private void changeLocale(String localeCode)
   {

      if (localeCode != null)
      {
         try
         {
            Map<String, String> params = new HashMap<String, String>();
            params.put("updateLangCom", "true");
            Document result = m_applet.getXMLDocument("../sys_welcome/login.txt?sys_lang=" + localeCode, params);
            String rootNodeName = result.getDocumentElement().getNodeName();
            if (rootNodeName.equals("redirect"))
            {

               PSLogin loginInfo = sessionManager.getLoginInfo();
               loginInfo.setDefaultLocaleCode(localeCode);

               SwingUtilities.invokeLater(() -> PSContentExplorerApplication.getBaseFrame().reload());

            }
         }
         catch (IOException | SAXException | ParserConfigurationException e)
         {
              log.error("Cannot change Locale",e);
         }
         PSContentExplorerUtils.outputUserInfo(m_applet);
      }

   }

   private String getCurrentCommunity()
   {
      PSLogin loginInfo = sessionManager.getLoginInfo();
      return loginInfo.getDefaultCommunity();
   }

   private String getCurrentLocale()
   {
      PSLogin loginInfo = sessionManager.getLoginInfo();
      return loginInfo.getDefaultLocaleCode();
   }
}
