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

import com.percussion.UTComponents.UTBrowseButton;
import com.percussion.UTComponents.UTFixedButton;
import com.percussion.border.PSFocusBorder;
import com.percussion.guitools.PSDialog;
import com.percussion.guitools.PSPropertyPanel;
import com.percussion.guitools.UTStandardCommandPanel;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EtchedBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


/**
 * The dialog to modify display options.
 */
public class PSDisplayOptionsDialog extends PSDialog
{
   /**
    * Constructs the dialog with supplied options.
    * Not checking for <code>null</code> applet because of the super
    *
    * @param parent the parent frame of the dialog, may be <code>null</code>
    * @param userOptions the user display options, may not be <code>null</code>
    * @param defaultOptions the default display options, may not be <code>null
    * </code>
    *
    * @throws IllegalArgumentException if any parameter is invalid.
    */
   public PSDisplayOptionsDialog(Frame parent, PSDisplayOptions userOptions,
      PSDisplayOptions defaultOptions, PSContentExplorerApplet applet)
   {
      super(parent, applet.getResourceString(
         PSDisplayOptionsDialog.class, "Display Options"));
    
      if(userOptions == null)
         throw new IllegalArgumentException("userOptions may not be null.");

      if(defaultOptions == null)
         throw new IllegalArgumentException("defaultOptions may not be null.");

      m_defaultOptions = defaultOptions;
      m_userOptions = userOptions;
      m_applet = applet;

      initDialog();
   }


   
   /**
    * Initializes the dialog framework and sets initial state.
    */
   private void initDialog()
   {
      String[] labels_1 = {"Bold", "Italic", "Text color:", "Font:"};
      String[] labels_2 = {"Bold", "Italic", "Main Menu Text color:", "Font:", "Context Menu Text Color"};
      char[] mnemonics_1 = {'B', 'I', 'T', 'F'};
      char[] mnemonics_2 = {'L', 'A', 'E', 'N','U'};
      
      PSPropertyPanel commonOptionsPanel = new PSPropertyPanel();

      // setup Background color control
      m_bgColorPanel = new ColorPanel();
      String bgLabel = m_applet.getResourceString(
            getClass(), "@Background color:");
      char bgMnemonic = PSContentExplorerApplet.getResourceMnemonic(
            getClass(), "@Background color:", 'K');
      String bgTooltip = PSContentExplorerApplet.getResourceTooltip(
            getClass(), "@Background color:");
      commonOptionsPanel.addPropertyRow(
         bgLabel,
         m_bgColorPanel,
         m_bgColorPanel.m_brButton,
         bgMnemonic, 
         bgTooltip);
      JPanel optsPanel = new JPanel(new BorderLayout());
      optsPanel.add(commonOptionsPanel, BorderLayout.WEST);

      m_mainDisplayPanel = new OptionsPanel("Main Display Options", labels_1,
            mnemonics_1,"MD");
      m_mainDisplayPanel.setBorder(
         BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                  m_applet.getResourceString(
                  getClass(),
                  "@Main Display Options")),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)));

      // setup Heading text color control
      m_headingTextColorPanel = new ColorPanel();
      String htLabel = m_applet.getResourceString(getClass(),
            "@Heading text color:");
      char htMnemonic = PSContentExploreAppletUtils.getResourceMnemonic(getClass(),
            "@Heading text color:", 'X');
      String htTooltip = PSContentExploreAppletUtils.getResourceTooltip(getClass(),
            "@Heading text color:");
      m_mainDisplayPanel.addPropertyRow(
         htLabel,
         m_headingTextColorPanel,
         m_headingTextColorPanel.m_brButton,
         htMnemonic,
         htTooltip);
      
      // setup Highlight color control
      m_hltColorPanel = new ColorPanel();
      String hltLabel = m_applet.getResourceString(getClass(),
            "@Highlight color:");
      char hltMnemonic = PSContentExploreAppletUtils.getResourceMnemonic(getClass(),
            "@Highlight color:", 'R');
      String hltTooltip = PSContentExploreAppletUtils.getResourceTooltip(getClass(),
            "@Highlight color:");
      m_mainDisplayPanel.addPropertyRow(
         hltLabel,
         m_hltColorPanel,
         m_hltColorPanel.m_brButton,
         hltMnemonic,
         hltTooltip);
      
      // setup Highlight text color control
      m_hltTextColorPanel = new ColorPanel();
      String hltTextLabel = m_applet.getResourceString(getClass(),
            "@Highlight text color:");
      char hltTextMnemonic = PSContentExploreAppletUtils.getResourceMnemonic(getClass(),
            "@Highlight text color:", 'H');
      String hltTextTooltip = PSContentExploreAppletUtils.getResourceTooltip(getClass(),
            "@Highlight text color:");
      m_mainDisplayPanel.addPropertyRow(
         hltTextLabel,
         m_hltTextColorPanel,
         m_hltTextColorPanel.m_brButton,
         hltTextMnemonic,
         hltTextTooltip);      
      
      // setup Focus color control
      m_focusColorPanel = new ColorPanel();
      String fcLabel = m_applet.getResourceString(getClass(),
            "@Focus color:");
      char fcMnemonic = PSContentExploreAppletUtils.getResourceMnemonic(getClass(),
            "@Focus color:", 'S');
      String fcTooltip = PSContentExploreAppletUtils.getResourceTooltip(getClass(),
            "@Focus color:");
      m_mainDisplayPanel.addPropertyRow(
         fcLabel,
         m_focusColorPanel,
         m_focusColorPanel.m_brButton,
         fcMnemonic,
         fcTooltip);

      m_menuPanel = new OptionsPanel("Menu Options", labels_2, mnemonics_2,"MN");
      m_menuPanel.setBorder(
         BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                  m_applet.getResourceString(
                  getClass(),
                  "@Menu Options")),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)));

      //add command panel
      m_defaultButton =
         new UTFixedButton(
               m_applet.getResourceString(getClass(), "@Default"));
      
      m_defaultButton.setMnemonic(
         PSContentExploreAppletUtils.getResourceMnemonic(getClass(), "@Default", 'D'));

      m_systemDefaultCheck =
         new JCheckBox(
               m_applet.getResourceString(
               getClass(), "@Use OS Settings"));
      m_systemDefaultCheck.setMnemonic(
         PSContentExploreAppletUtils.getResourceMnemonic(
            getClass(), "@Use OS Settings", 'U'));
      m_defaultButton.addActionListener(new ActionListener()
      {
         //Applies default options to the dialog.
         public void actionPerformed(ActionEvent e)
         {
            m_systemDefaultCheck.setSelected(false);
            applyOptions(m_defaultOptions);
         }
      });
      m_systemDefaultCheck.setAlignmentX(RIGHT_ALIGNMENT);
      m_systemDefaultCheck.addActionListener(new ActionListener()
      {
         //Applies system options to the dialog.
         public void actionPerformed(ActionEvent e)
         {
            if(e.getSource() instanceof JCheckBox)
            {
               JCheckBox cb = (JCheckBox) e.getSource();
               m_userOptions.setIsUseOSSettings(cb.isSelected());
               applyOptions(m_userOptions);
            }
         }
      });

      UTStandardCommandPanel defCommandPanel = new UTStandardCommandPanel(
         this, SwingConstants.HORIZONTAL, true);
      defCommandPanel.setAlignmentX(RIGHT_ALIGNMENT);
      JPanel systemSettingsPanel = new JPanel(new BorderLayout());
      systemSettingsPanel.add(m_systemDefaultCheck);

      JPanel bottomPanel = new JPanel(new BorderLayout());
      
      // Since this is moved out of the defCommandPanel, we have no choice but
      // to mimic the layout of UTStandardCommandPanel
      Box defButtonBox = new Box(BoxLayout.Y_AXIS);
      defButtonBox.add(Box.createVerticalGlue());
      defButtonBox.add(m_defaultButton);
      defButtonBox.add(Box.createVerticalStrut(5));
      
      JPanel defCmdPanel = new JPanel(new BorderLayout());
      defCmdPanel.add(defCommandPanel, BorderLayout.SOUTH);

      
      bottomPanel.add(defButtonBox, BorderLayout.WEST);
      bottomPanel.add(defCmdPanel, BorderLayout.EAST);

      JPanel panel = new JPanel();
      panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
      panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
      panel.add(optsPanel);
      
      panel.add(Box.createVerticalStrut(10));
      panel.add(m_mainDisplayPanel);
      panel.add(Box.createVerticalStrut(10));
      panel.add(m_menuPanel);
      panel.add(Box.createVerticalStrut(10));
      panel.add(systemSettingsPanel);
      panel.add(Box.createVerticalStrut(10));
      //panel.add(defCommandPanel);
      panel.add(bottomPanel);

      setContentPane(panel);

      //Update dialog state from user options initially.
      applyOptions(m_userOptions);
      m_systemDefaultCheck.setSelected(m_userOptions.getIsUseOSSettings());
            

      Dimension dim = getSize();
      // Not picking up size of bottom panel.

      // Add focus highlights
      PSDisplayOptions dispOptions =
         (PSDisplayOptions)UIManager.getDefaults().get(
            PSContentExplorerConstants.DISPLAY_OPTIONS);
      PSFocusBorder focusBorder = new PSFocusBorder(1, dispOptions);
      focusBorder.addToAllNavigable(panel);

      pack();
      center();
      setResizable(true);
   }

   /**
    * Enable or Disable all components recursively except the command button 
    * panel.
    * @param comp top level {@link Container container} to disable. All child 
    * components will be disabled except {@link UTStandardCommandPanel} if it 
    * exists as a child. Assumed not <code>null</code>.
    * @param disable <code>true</code> to enable and <code>false</code> to 
    * disable.
    */
   protected void disableComponent(Component comp, boolean disable)
   {
      if (comp instanceof UTStandardCommandPanel)
         return;
      if (comp instanceof Container)
      {
         Component[] comps = ((Container) comp).getComponents();
         for (int i = 0; i < comps.length; i++)
            disableComponent(comps[i], disable);
      }
      comp.setEnabled(!disable);
   }

   /**
    * Applies supplied options to the dialog controls.
    *
    * @param options the options to apply, assumed not <code>null</code>
    */
   private void applyOptions(PSDisplayOptions options)
   {
      if (options.getBackGroundColor() != null)
         m_bgColorPanel.setColor(options.getBackGroundColor());

      if (options.getHighlightColor() != null)
         m_hltColorPanel.setColor(options.getHighlightColor());
      
      if (options.getHighlightTextColor() != null)
         m_hltTextColorPanel.setColor(options.getHighlightTextColor());

      if (options.getFocusColor() != null)
         m_focusColorPanel.setColor(options.getFocusColor());

      if (options.getForeGroundColor() != null)
         m_mainDisplayPanel.setTextColor(options.getForeGroundColor());

      if (options.getFont() != null)
         m_mainDisplayPanel.setDisplayFont(options.getFont());

      if (options.getMenuForeGroundColor() != null)
         m_menuPanel.setTextColor(options.getMenuForeGroundColor());

      if (options.getContextMenuForeGroundColor() != null)
         m_menuPanel.setCMTextColor(options.getContextMenuForeGroundColor());

      if (options.getMenuFont() != null)
         m_menuPanel.setDisplayFont(options.getMenuFont());

      if (options.getHeadingForeGroundColor() != null)
         m_headingTextColorPanel.setColor(options.getHeadingForeGroundColor());

      disableComponent(getContentPane(), options.getIsUseOSSettings());
      
      /**
       * Always enabled
       */
      m_systemDefaultCheck.setEnabled(true);
      m_defaultButton.setEnabled(true);
   }

   /**
    * Gets the display options from the dialog controls and updates user display
    * options. Hides and disposes the dialog.
    */
   public void onOk()
   {
      m_userOptions.setIsUseOSSettings(m_systemDefaultCheck.isSelected());
      m_userOptions.setBackGroundColor(m_bgColorPanel.getColor());
      m_userOptions.setForeGroundColor(m_mainDisplayPanel.getTextColor());
      m_userOptions.setHighlightColor(m_hltColorPanel.getColor());
      m_userOptions.setHighlightTextColor(m_hltTextColorPanel.getColor());
      m_userOptions.setFocusColor(m_focusColorPanel.getColor());
      m_userOptions.setHeadingForeGroundColor(
         m_headingTextColorPanel.getColor());

      m_userOptions.setFont(m_mainDisplayPanel.getDisplayFont());

      m_userOptions.setMenuForeGroundColor(m_menuPanel.getTextColor());
      m_userOptions.setContextMenuForeGroundColor(m_menuPanel.getCMTextColor());
      m_userOptions.setMenuFont(m_menuPanel.getDisplayFont());

      super.onOk();
   }

   /**
    * The panel that represents a text field and a button to choose different
    * colors for various options.
    */
   private class ColorPanel extends JPanel
   {
      /**
       * Constructs the panel with a text field and a browse button to choose
       * color and set that color as background for the text field.
       */
      public ColorPanel()
      {
         setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

         Dimension dim = new Dimension(50, 20);
         m_colorPanel.setPreferredSize(dim);
         m_colorPanel.setMinimumSize(dim);
         m_colorPanel.setMaximumSize(dim);
         m_colorPanel.setBorder(new EtchedBorder());

         //UTBrowseButton brButton = new UTBrowseButton();
         m_brButton.addActionListener(new ActionListener()
         {
            //Sets the user chosen color as background of the text field.
            public void actionPerformed(ActionEvent e)
            {
               Color color =
                  JColorChooser.showDialog(
                     PSDisplayOptionsDialog.this,
                     m_applet.getResourceString(
                        getClass(),
                        "@Choose color"),
                     m_colorPanel.getBackground());
               if (color != null)
               {
                  m_colorPanel.setBackground(color);
               }
            }
         });

         add(m_colorPanel);
         add(Box.createHorizontalStrut(5));
         add(m_brButton);
      }

      /**
       * Gets the color represented by this panel's text field.
       *
       * @return the color, never <code>null</code>
       */
      public Color getColor()
      {
         return m_colorPanel.getBackground();
      }

      /**
       * Sets the color supplied as background of this panel's text field.
       *
       * @param color the color, may not be <code>null</code>
       *
       * @throws IllegalArgumentException if color is <code>null</code>
       */
      public void setColor(Color color)
      {
         if(color == null)
            throw new IllegalArgumentException("color may not be null.");

         m_colorPanel.setBackground(color);
      }

      /**
       * The panel that shows currently picked color, never <code>null</code>.
       */
      private JPanel m_colorPanel = new JPanel();
      
      /**
       * The select color button within the color panel, never <code>null</code>
       */
      private UTBrowseButton m_brButton = new UTBrowseButton();
   }

   /**
    * Constructs the panel to choose text color, font with style and size.
    */
   private class OptionsPanel extends PSPropertyPanel
   {
      /**
       * Initializes the panel with all controls.
       */
      public OptionsPanel(String category, String[] labels, char[] mnemonics, String panel)
      {
         if (category == null)
            category = "";
         if (category.length() > 0)
         {
            category = category.replace(' ', '_');
            if (!category.startsWith("."))
               category = "." + category;
         }
         m_txtColorPanel = new ColorPanel();
         m_cmTxtColorPanel = new ColorPanel();
         m_fontNameField = new JComboBox();
         m_fontNameField.setPreferredSize(new Dimension(150, 20));
         m_fontNameField.setMaximumSize(new Dimension(150, 20));
         m_fontNameField.setEditable(false);

         //add all available font names
         Font[] availFonts =
            GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();

         for (int i = 0; i < availFonts.length; i++)
            m_fontNameField.addItem(availFonts[i].getName());

         m_fontSizeField = new JComboBox();
         m_fontSizeField.setPreferredSize(new Dimension(40, 20));
         m_fontSizeField.setMinimumSize(new Dimension(40, 20));
         m_fontSizeField.setMaximumSize(new Dimension(70, 20));
         for (int i = 1; i < 30; i++)
            m_fontSizeField.addItem(new Integer(i));

         m_boldFontCheck = new JCheckBox(m_applet
               .getResourceString(getClass(), "@" + labels[0]));
         m_boldFontCheck.setMnemonic(
            PSContentExploreAppletUtils.getResourceMnemonic(
               getClass().getName() + category,
               labels[0], mnemonics[0]));

         m_italicFontCheck =
            new JCheckBox(
                  m_applet.getResourceString(
                  getClass(),
                  "@" + labels[1]));
         m_italicFontCheck.setMnemonic(
            PSContentExploreAppletUtils.getResourceMnemonic(
               getClass().getName() + category,
               labels[1], mnemonics[1]));
         JPanel fontPanel = new JPanel();
         fontPanel.setLayout(new BoxLayout(fontPanel, BoxLayout.X_AXIS));
         fontPanel.add(m_fontNameField);
         fontPanel.add(Box.createHorizontalStrut(5));
         fontPanel.add(m_fontSizeField);
         
         // setup Text color control
         String tcLabel = m_applet.getResourceString(
               getClass(), "@" + labels[2]);
         char tcMnemonic = PSContentExploreAppletUtils.getResourceMnemonic(
               getClass().getName() + category, labels[2], mnemonics[2]);
         String tcTooltip = PSContentExploreAppletUtils.getResourceTooltip(
               getClass(), "@" + category.substring(1) + "." + labels[2]);
         
         addPropertyRow(
            tcLabel,
            m_txtColorPanel,
            m_txtColorPanel.m_brButton,
            tcMnemonic,
            tcTooltip);
         
         // setup Text color control
         if(panel.equalsIgnoreCase("MN"))
         {
            String cmtcLabel = m_applet.getResourceString(
                  getClass(), "@" + labels[4]);
            char cmtcMnemonic = PSContentExploreAppletUtils.getResourceMnemonic(
                  getClass().getName() + category, labels[4], mnemonics[4]);
            String cmtcTooltip = PSContentExploreAppletUtils.getResourceTooltip(
                  getClass(), "@" + category.substring(1) + "." + labels[4]);
            
            addPropertyRow(
                  cmtcLabel,
               m_cmTxtColorPanel,
               m_cmTxtColorPanel.m_brButton,
               cmtcMnemonic,
               cmtcTooltip);
         }
         // setup Font combo box
         String fontLabel = m_applet.getResourceString(
               getClass(), "@" + labels[3]);
         char fontMnemonic = PSContentExploreAppletUtils.getResourceMnemonic(
               getClass().getName() + category, labels[3], mnemonics[3]);
         String fontTooltip = PSContentExploreAppletUtils.getResourceTooltip(
               getClass(), "@" + category.substring(1) + "." + labels[3]);
         addPropertyRow(
            fontLabel,
            fontPanel,
            m_fontNameField,
            fontMnemonic,
            fontTooltip);
         
         // setup Style panel
         addPropertyRow(
               m_applet.getResourceString(getClass(), "@Style:"),
            new JComponent[] { m_boldFontCheck, m_italicFontCheck });
      }

      /**
       * Updates the controls state from the supplied font.
       *
       * @param font the font to set, may not be <code>null</code>
       *
       * @throws IllegalArgumentException if font is <code>null</code>
       */
      public void setDisplayFont(Font font)
      {
         if(font == null)
            throw new IllegalArgumentException("font may not be null.");


         m_fontNameField.setSelectedItem(font.getName());
         m_fontSizeField.setSelectedItem(new Integer(font.getSize()));
         m_boldFontCheck.setSelected(font.isBold());
         m_italicFontCheck.setSelected(font.isItalic());
      }

      /**
       * Gets the user selected font.
       *
       * @return the font, never <code>null</code>
       */
      public Font getDisplayFont()
      {
         int style = Font.PLAIN;
         if(m_boldFontCheck.isSelected())
            style = Font.BOLD;

         if(m_italicFontCheck.isSelected())
            style = Font.ITALIC;

         if(m_boldFontCheck.isSelected() && m_italicFontCheck.isSelected())
            style = Font.BOLD | Font.ITALIC;

         String str = m_fontSizeField.getSelectedItem().toString();
         int size = (new Integer(str)).intValue();

         return new Font((String)m_fontNameField.getSelectedItem(), style, size);
      }

      /**
       * Sets the color to be shown in text panel.
       *
       * @param color the color to set, may not be <code>null</code>
       *
       * @throws IllegalArgumentException if color is <code>null</code>
       */
      public void setTextColor(Color color)
      {
         m_txtColorPanel.setColor(color);
      }

      /**
       * Gets the background color of the text panel.
       *
       * @return the color, never <code>null</code>
       */
      public Color getTextColor()
      {
         return m_txtColorPanel.getColor();
      }

      /**
       * Sets the color to be shown in text panel.
       *
       * @param color the color to set, may not be <code>null</code>
       *
       * @throws IllegalArgumentException if color is <code>null</code>
       */
      public void setCMTextColor(Color color)
      {
         m_cmTxtColorPanel.setColor(color);
      }

      /**
       * Gets the background color of the text panel.
       *
       * @return the color, never <code>null</code>
       */
      public Color getCMTextColor()
      {
         return m_cmTxtColorPanel.getColor();
      }

      /**
       * The combo-box to choose font name, initialized in <code>initDialog()
       * </code> and never <code>null</code> or modified after that. The user
       * options font name will be selected initially.
       */
      private JComboBox m_fontNameField;

      /**
       * The combo-box to choose font size, initialized in <code>initDialog()
       * </code> and never <code>null</code> or modified after that. The user
       * options font size will be selected initially.
       */
      private JComboBox m_fontSizeField;

      /**
       * The check-box to choose bold style, initialized in <code>initDialog()
       * </code> and never <code>null</code> or modified after that. Initially it
       * will be checked if the user options font style is 'BOLD'.
       */
      private JCheckBox m_boldFontCheck;

      /**
       * The check-box to choose italic style, initialized in <code>initDialog()
       * </code> and never <code>null</code> or modified after that. Initially it
       * will be checked if the user options font style is 'ITALIC'.
       */
      private JCheckBox m_italicFontCheck;

      /**
       * The panel that contains a text field and a button to choose foreground
       * color,  initialized in <code>initDialog()</code> and never <code>null
       * </code> or modified after that. The user options foreground color will be
       * set as background of text field initially.
      */
      private ColorPanel m_txtColorPanel;

      /**
       * The panel that contains a text field and a button to choose foreground
       * color,  initialized in <code>initDialog()</code> and never <code>null
       * </code> or modified after that. The user options text foreground color
       *  will be set as background of text field initially.
      */
      private ColorPanel m_cmTxtColorPanel;
   }


   /**
    * The panel that contains a text field and a button to choose background
    * color,  initialized in <code>initDialog()</code> and never <code>null
    * </code> or modified after that. The user options background color will be
    * set as background of text field initially.
    */
   private ColorPanel m_bgColorPanel;

   /**
    * The panel that contains a text field and a button to choose highlight
    * (selection) color,  initialized in <code>initDialog()</code> and never
    * <code>null</code> or modified after that. The user options hightlight
    * color will be set as background of text field initially.
    */
   private ColorPanel m_hltColorPanel;
   
   /**
    * The panel that contains a text field and a button to choose highlight
    * text (selection) color,  initialized in <code>initDialog()</code> and never
    * <code>null</code> or modified after that. The user options hightlight
    * color will be set as background of text field initially.
    */
   private ColorPanel m_hltTextColorPanel;   

   /**
    * The panel that contains a text field and a button to choose heading text
    * color in main display panel, initialized in <code>initDialog()</code> and
    * never <code>null</code> or modified after that. The user options heading
    * text color will be set as background of text field initially.
    */
   private ColorPanel m_headingTextColorPanel;

   /**
    * The panel that contains a text field and a button to choose focus
    * color in main display panel, initialized in <code>initDialog()</code> and
    * never <code>null</code> or modified after that. The user options focus
    * color will be set whenever focus is on a particular control.
    */
   private ColorPanel m_focusColorPanel;

   /**
    * The panel that contains controls to select a font and text color for main display,
    * initialized in <code>initDialog()</code> and never <code>null</code> or
    * modified after that.
    */
   private OptionsPanel m_mainDisplayPanel;

   /**
    * The panel that contains controls to select a font and text color for menu,
    * initialized in <code>initDialog()</code> and never <code>null</code> or
    * modified after that.
    */
   private OptionsPanel m_menuPanel;

   /**
    * The default display options, initialized in the ctor and never <code>null
    * </code> or modified after that.
    */
   private PSDisplayOptions m_defaultOptions;

   /**
    * The user display options, initialized in the ctor and modified in <code>
    * onOk()</code>. Never <code>null</code>.
    */
   private PSDisplayOptions m_userOptions;

   /**
    * The check-box to choose OS default settings, initialized in 
    * <code>initDialog()</code> and never <code>null</code> or modified 
    * after that. Initially it will be un checked.
    */
   private JCheckBox m_systemDefaultCheck;
   
   /**
    * The reset button to get back to Rx defaults
    */
   private UTFixedButton m_defaultButton;
   
   /**
    * A reference back to the applet that initiated the action manager.
    */
   private PSContentExplorerApplet m_applet;
}
