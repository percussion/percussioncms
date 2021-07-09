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
package com.percussion.cx;

import java.awt.Color;
import java.awt.Font;
import java.awt.SystemColor;

import javax.swing.JLabel;
import javax.swing.JPopupMenu;

/**
 * This is a convenience class for UI.  It is a proxy for <code>PSOptions</code>
 *  with the category name "display".
 */
public class PSDisplayOptions
{
   /**
    * Creates a new instance of this object with the psOptions as its definition.
    *
    * @param definition must not be <code>null</code> and must have
    * it's category {@link #DISPLAY_OPTIONS_CATEGORY DISPLAY_OPTIONS_CATEGORY}.
    */
   public PSDisplayOptions(PSOptions definition)
   {
      setDefinitionOption(definition);
   }

   /**
    * Returns the background color of this class.
    *
    * @return the <code>Color</code> of this class.  This will not hold any
    * reference to the color and any modifications to the
    * <code>Color</code> will not affect this class.   May be <code>null</code>,
    * if <code>Color</code> is not found.
    */
   public Color getBackGroundColor()
   {
      return getTheColor(BACKGROUND_COLOR_OPTIONID, "0x4867a7");
   }

   /**
    * Sets the background color used by this display options. Adds this option 
    * if it does not exist.
    *
    * @param color the color to be set, must not be <code>null</code>.
    */
   public void setBackGroundColor(Color color)
   {
      setTheColor(GENERAL_CONTEXT, BACKGROUND_COLOR_OPTIONID, color);
   }

   /**
    * Returns the foreground color of this class.
    *
    * @return the <code>Color</code> of this class.  This will not hold any
    * reference to the color and any modifications to the
    * <code>Color</code> will not affect this class.   May be <code>null</code>,
    * if <code>Color</code> is not found.
    */
   public Color getForeGroundColor()
   {
      return getTheColor(FOREGROUND_COLOR_OPTIONID, "0x000000");
   }

   /**
    * Sets the foreground color used by this display options. Adds this option 
    * if it does not exist.
    *
    * @param color the color to be set, must not be <code>null</code>.
    */
   public void setForeGroundColor(Color color)
   {
      setTheColor(GENERAL_CONTEXT, FOREGROUND_COLOR_OPTIONID, color);
   }

   /**
    * Returns the foreground color of the menu. If the option does not exist, it
    * gets the foreground color from {@link #getForeGroundColor()}.
    *
    * @return the <code>Color</code> of this class.  This will not hold any
    * reference to the color and any modifications to the
    * <code>Color</code> will not affect this class.   May be <code>null</code>,
    * if <code>Color</code> is not found.
    */
   public Color getMenuForeGroundColor()
   {
      return getTheColor(MENU_FOREGROUND_COLOR_OPTIONID, "0xffffff");
   }

   /**
    * Returns the foreground color of the context menu. 
    * If the option does not exist, it gets the foreground color from 
    * {@link #getForeGroundColor()}.
    *
    * @return the <code>Color</code> of this class.  This will not hold any
    * reference to the color and any modifications to the
    * <code>Color</code> will not affect this class.   May be <code>null</code>,
    * if <code>Color</code> is not found.
    */
   public Color getContextMenuForeGroundColor()
   {
      return getTheColor(CONTEXT_MENU_FOREGROUND_COLOR_OPTIONID, "0x000000");
   }

   /**
    * Sets the foreground color used by this display options. Adds this option 
    * if it does not exist.
    *
    * @param color the color to be set, must not be <code>null</code>.
    */
   public void setMenuForeGroundColor(Color color)
   {
      setTheColor(MENU_CONTEXT, MENU_FOREGROUND_COLOR_OPTIONID, color);
   }

   /**
    * Sets the foreground color used by this display options. Adds this option 
    * if it does not exist.
    *
    * @param color the color to be set, must not be <code>null</code>.
    */
   public void setContextMenuForeGroundColor(Color color)
   {
      setTheColor(MENU_CONTEXT, CONTEXT_MENU_FOREGROUND_COLOR_OPTIONID, color);
   }

   /**
    * Returns the foreground color of the headings in the main display. If the 
    * option does not exist, it gets the foreground color from {@link 
    * #getForeGroundColor()}.
    *
    * @return the <code>Color</code> of this class.  This will not hold any
    * reference to the color and any modifications to the
    * <code>Color</code> will not affect this class.   May be <code>null</code>,
    * if <code>Color</code> is not found.
    */
   public Color getHeadingForeGroundColor()
   {
      return getTheColor(HEAD_FOREGROUND_COLOR_OPTIONID, "0xffffff");
   }

   /**
    * Sets the foreground color for the headings specified by these options. 
    * Adds this option  if it does not exist.
    *
    * @param color the color to be set, must not be <code>null</code>.
    * 
    * @throws IllegalArgumentException color is <code>null</code>.
    */
   public void setHeadingForeGroundColor(Color color)
   {
      setTheColor(GENERAL_CONTEXT, HEAD_FOREGROUND_COLOR_OPTIONID, color);
   }

   /**
    * Returns the highlight color of this class.
    *
    * @return the <code>Color</code> of this class.  This will not hold any
    * reference to the color and any modifications to the
    * <code>Color</code> will not affect this class.   May be <code>null</code>,
    * if <code>Color</code> is not found.
    */
   public Color getHighlightColor()
   {
      return getTheColor(HIGHLIGHT_COLOR_OPTIONID, "0x4867a7");
   }
   
   /**
    * Returns the highlight text color of this class.
    *
    * @return the <code>Color</code> of this class.  This will not hold any
    * reference to the color and any modifications to the
    * <code>Color</code> will not affect this class.   May be <code>null</code>,
    * if <code>Color</code> is not found.
    */
   public Color getHighlightTextColor()
   {
      return getTheColor(HIGHLIGHT_TEXT_COLOR_OPTIONID, "0xFFFFFF");
   }   

   /**
    * Sets the highlight color used by this display options. Adds this option 
    * if it does not exist.
    *
    * @param color the color to be set, must not be <code>null</code>.
    */
   public void setHighlightColor(Color color)
   {
      setTheColor(GENERAL_CONTEXT, HIGHLIGHT_COLOR_OPTIONID, color);
   }
   
   /**
    * Sets the highlight text color used by this display options. Adds this option 
    * if it does not exist.
    *
    * @param color the color to be set, must not be <code>null</code>.
    */
   public void setHighlightTextColor(Color color)
   {
      setTheColor(GENERAL_CONTEXT, HIGHLIGHT_TEXT_COLOR_OPTIONID, color);
   }   

   /**
    * Returns the focus color of this class.
    *
    * @return the <code>Color</code> of this class.  This will not hold any
    * reference to the color and any modifications to the
    * <code>Color</code> will not affect this class.   May be <code>null</code>,
    * if <code>Color</code> is not found.
    */   
   public Color getFocusColor()
   {
      return getTheColor(FOCUS_COLOR_OPTIONID, "0xFF0000");
   }
 
   /**
    * Sets the focus color used by this display options. Adds this option 
    * if it does not exist.
    *
    * @param color the color to be set, must not be <code>null</code>.
    */     
   public void setFocusColor(Color color)
   {
      setTheColor(GENERAL_CONTEXT, FOCUS_COLOR_OPTIONID, color);
   }

   /**
    * Returns a <code>java.awt.Font</code> object of the font used by this
    * display options.  This value is not owned by this object and any
    * modifications to it will not be referenced by this class.
    *
    * @return may be <code>null</code>, if font is not found.
    */
   public Font getFont()
   {
      return getTheFont(FONT_OPTIONID);
   }

   /**
    * Sets the <code>java.awt.Font</code> object of the font to be used by this
    * display option. Adds this option if it does not exist.
    *
    * @param font must not be <code>null</code>.
    */
   public void setFont(Font font)
   {
      setTheFont(GENERAL_CONTEXT, FONT_OPTIONID, font);
   }

   /**
    * Returns a <code>java.awt.Font</code> object of the font used by this
    * display options.  This value is not owned by this object and any
    * modifications to it will not be referenced by this class.
    *
    * @return may be <code>null</code>, if font is not found.
    */
   public Font getMenuFont()
   {
      return getTheFont(MENU_FONT_OPTIONID);
   }

   /**
    * Sets the <code>java.awt.Font</code> object of the font to be used by this
    * display option. Adds this option if it does not exist.
    *
    * @param font must not be <code>null</code>.
    */
   public void setMenuFont(Font font)
   {
      setTheFont(MENU_CONTEXT, MENU_FONT_OPTIONID, font);
   }

   /**
    * Creates a <code>Color</code> object from a string representation.
    * The <code>colorString</code> must be in a format that the
    * {@link java.lang.Integer#decode(String) java.lang.Integer.decode(String)},
    * can decode.
    * @param colorString a string representation of the color, must not be
    * <code>null</code> or empty.
    */
   private Color colorFromString(String color)
   {
      if (color == null || color.trim().length() == 0)
         throw new IllegalArgumentException("color must not be null or empty.");

      Integer colorInt = Integer.decode(color);
      return new Color(colorInt.intValue());
   }

   /**
    * Gets the hex representation of the color.  This value is what is
    * set back to the <code>PSOption</code> that gets persisted.
    *
    * @param color the color to be stringed.
    * @return the hex value that will be persisted.
    */
   private String getColorString(Color color)
   {
      return "0x"
         + checkDigits(Integer.toHexString(color.getRed()))
         + checkDigits(Integer.toHexString(color.getGreen()))
         + checkDigits(Integer.toHexString(color.getBlue()));
   }

   /**
    * We use Integer.toHexString(int), to convert the color values to hex values
    * .  Because we need the the preceeding "0" in value that are less than
    *  10, we need to prepend the "0" to string.  For example if we start with
    *  "0x000000", and we create a color from this value then get red, blue
    *  and green values from that color, and convert the value to hex we will
    *  get "0x000" as the value, which is different from what we started with.
    *  So we use this method to check for this and append a "0" on the
    *  begining so that we get the correct hex string representation.
    *
    * @param s assumed not <code>null</code> and nor empty.
    * @return correct string represention.  Never <code>null</code>, should
    * not be empty.
    */
   private String checkDigits(String s)
   { //TODO: there may be a better way to get the hex value from the color than
      // this, when time that should be implemented.
      String returnString = "";
      if (s.length() == 1)
         returnString = "0" + s;
      else
         returnString = s;

      return returnString;

   }

   /**
    * Copies the disply options from the supplied object to this object.
    * 
    * @param options the options to copy, may not be <code>null</code>
    * 
    * @throws IllegalArgumentException if options is <code>null</code>
    */
   public void copyFrom(PSDisplayOptions options)
   {
      if (options == null)
         throw new IllegalArgumentException("options may not be null.");

      setBackGroundColor(options.getBackGroundColor());
      setHighlightColor(options.getHighlightColor());
      setHighlightTextColor(options.getHighlightTextColor());
      setForeGroundColor(options.getForeGroundColor());
      setFont(options.getFont());
      setMenuFont(options.getMenuFont());
      setMenuForeGroundColor(options.getMenuForeGroundColor());
      setContextMenuForeGroundColor(options.getContextMenuForeGroundColor());
      setHeadingForeGroundColor(options.getHeadingForeGroundColor());
   }

   /**
    * @see PSDisplayOptions(PSOptions) PSDisplayOptions(PSOptions)
    */
   private PSOptions getDefinitionOption()
   {
      return m_defOptions;
   }

   /**
    * @see PSDisplayOptions(PSOptions) PSDisplayOptions(PSOptions)
    */
   private void setDefinitionOption(PSOptions definition)
   {
      if (definition == null
         || (!definition.getCategory().equals(DISPLAY_OPTIONS_CATEGORY)))
         throw new IllegalArgumentException(
            "definition must not be null and be have: "
               + DISPLAY_OPTIONS_CATEGORY
               + " as its category");

      m_defOptions = definition;
   }

   private Color getTheColor(String optionName, String defaultColor)
   {
      if(getIsUseOSSettings())
         return getSystemColor(optionName);

      String theValue = defaultColor;
      PSOption option = getDefinitionOption().getOption(optionName);

      if (option != null)
         theValue = (String)option.getOptionValue();

      return colorFromString(theValue);
   }

   /**
    * Get OS default color for the color option requested.
    * @param optionName One of the XXX_COLOR_OPTIONID values. Assumed not 
    * <code>null</code>. 
    * @return OS color for the color id requested, never <code>null</code>. 
    * Default value of {@link SystemColor#window} if the color optionid is not 
    * one of the known ones.
    */
   private Color getSystemColor(String optionName)
   {
      if (optionName.equalsIgnoreCase(BACKGROUND_COLOR_OPTIONID))
      {
         return SystemColor.window;
      }
      else if (optionName.equalsIgnoreCase(HIGHLIGHT_COLOR_OPTIONID))
      {
         return SystemColor.textHighlight;
      }
      else if (optionName.equalsIgnoreCase(HIGHLIGHT_TEXT_COLOR_OPTIONID))
      {
         return SystemColor.textHighlightText;
      }
      else if (optionName.equalsIgnoreCase(FOREGROUND_COLOR_OPTIONID))
      {
         return SystemColor.textText;
      }
      else if (optionName.equalsIgnoreCase(HEAD_FOREGROUND_COLOR_OPTIONID))
      {
         return SystemColor.controlText;
      }
      else if (optionName.equalsIgnoreCase(MENU_FOREGROUND_COLOR_OPTIONID))
      {
         return SystemColor.textText;
      }
      else if (optionName.equalsIgnoreCase(CONTEXT_MENU_FOREGROUND_COLOR_OPTIONID))
      {
         return SystemColor.textText;
      }
      else if (optionName.equalsIgnoreCase(FOCUS_COLOR_OPTIONID))
      {
         return SystemColor.textText;
      }
      return SystemColor.window;
   }

   private void setTheColor(String context, String optionName, Color color)
   {
      if (color == null)
         throw new IllegalArgumentException("color must not be null");

      PSOption option = getDefinitionOption().getOption(optionName);

      if (option == null)
      {
         getDefinitionOption().addOption(
            context,            optionName,
            getColorString(color));
      }
      else
         option.setOptionValue(getColorString(color));
   }

   /**
    * Is to use the OS display settings?
    * @return <code>true</code> to use OS options, <code>false</code> otherwise. 
    */
   public boolean getIsUseOSSettings()
   {
      boolean theValue = false;
      PSOption option = getDefinitionOption().getOption(USE_OS_SETTINGS);

      if (option != null)
      {
         String temp = (String)option.getOptionValue();
         if(temp.equalsIgnoreCase("yes") || temp.equalsIgnoreCase("true"))
            theValue = true;
      }
      return theValue;
   }

   /**
    * Set the option for the applet to use OS display options. 
    * @param useOSSettings <code>true</code> to use OS options, <code>false</code>
    * otherwise.
    */
   public void setIsUseOSSettings(boolean useOSSettings)
   {
      String theValue = useOSSettings ? "yes" : "no";
      PSOption option = getDefinitionOption().getOption(USE_OS_SETTINGS);

      if (option == null)
      {
         getDefinitionOption().addOption(
            GENERAL_CONTEXT,
            USE_OS_SETTINGS,
            theValue);
      }
      else
         option.setOptionValue(theValue);
   }

   private Font getTheFont(String optionName)
   {
      if(getIsUseOSSettings())
      {
         //Intercept and return system option.
         return getSystemFont(optionName);
      }
      Font theFont = null;
      PSOption option = getDefinitionOption().getOption(optionName);

      if (option != null && option.getOptionValue() instanceof PSFont)
         theFont = ((PSFont)option.getOptionValue()).getFont();

      return theFont;
   }

   /**
    * This is a hack to read system font option since Swing does not provide a 
    * way.
    * @param optionName one of the XXX_FONT_OPTIONID values.
    * @return Undr the curren behavior, always System Font for popup menu. This 
    * is chosen since it gives closest effect to Windows Explorer. Defaults to 
    * Swing font for label. 
    */
   private Font getSystemFont(String optionName)
   {
      //Popmenu font seems to give a font that is comparabvle to Windows 
      //Explorer under Accessibility conditions.
      if(optionName.equalsIgnoreCase(FONT_OPTIONID))
      {
         return new JPopupMenu("").getFont();
      }
      else if(optionName.equalsIgnoreCase(MENU_FONT_OPTIONID))
      {
         return new JPopupMenu("").getFont();
      }
      return new JLabel("").getFont();
   }

   private void setTheFont(String context, String optionName, Font font)
   {
      if (font == null)
         throw new IllegalArgumentException("font may not be null.");

      PSOption option = getDefinitionOption().getOption(optionName);

      if (option == null)
      {
         getDefinitionOption().addOption(context, optionName, new PSFont(font));
      }
      else if (option.getOptionValue() instanceof PSFont)
      {
         ((PSFont)option.getOptionValue()).setFont(font);
      }
      else
         throw new IllegalStateException("invalid object for " + optionName);
   }

   /**
     * Indicates whether some other object is "equal to" this one.
     * Overrides the method in {@link Object.equals(Object) Object} and adheres
     * to that contract.
     * @param obj the reference object with which to compare.
     * @return <code>true</code> if this object is the same as the
     * <code>obj</code> argument; <code>false</code> otherwise. If
     * <code>null</code> supplied or obj is not an instance of this class,
     * <code>false</code> is returned.
     */
   public boolean equals(Object obj)
   {
      if (obj == null || !(getClass().isInstance(obj)))
         return false;

      PSDisplayOptions comp = (PSDisplayOptions)obj;

      if (!PSOptionManagerConstants.compare(m_defOptions, comp.m_defOptions))
         return false;

      return true;
   }

   /**
    * Overridden to fulfill contract of this method as described in
    * {@link Object#hashCode() Object}.
    *
    * @return A hash code value for this object
    */
   public int hashCode()
   {
      int hash = 0;

      hash += m_defOptions.hashCode();

      return hash;
   }

   /**
    * The PSOPtions to which we will provide a proxy, initialized in the ctor,
    * never <code>null</code> and invariant.
    */
   private PSOptions m_defOptions = null;

   /**
    * This is the category of display options as it will appear in the
    * XML document that is constrained to the sys_Options.dtd.
    */
   public final static String DISPLAY_OPTIONS_CATEGORY = "display";

   /**
    * This is the optionid value of the display option as it will appear in the
    * XML document that is constrained to the sys_Options.dtd.
    */
   private final static String BACKGROUND_COLOR_OPTIONID = "backgroundcolor";

   /**
    * This is the optionid value of the display option as it will appear in the
    * XML document that is constrained to the sys_Options.dtd.
    */
   private final static String HIGHLIGHT_COLOR_OPTIONID = "highlightcolor";
   
   /**
    * This is the optionid value of the display option as it will appear in the
    * XML document that is constrained to the sys_Options.dtd.
    */
   private final static String HIGHLIGHT_TEXT_COLOR_OPTIONID = "highlightcolortext";

   /**
    * This is the optionid value of the display option as it will appear in the
    * XML document that is constrained to the sys_Options.dtd for text color in 
    * main display of the applet.
    */
   private final static String FOREGROUND_COLOR_OPTIONID = "foregroundcolor";

   /**
    * This is the optionid value of the display option as it will appear in the
    * XML document that is constrained to the sys_Options.dtd for heading text
    * color.
    */
   private final static String HEAD_FOREGROUND_COLOR_OPTIONID =
      "heading_foregroundcolor";

   /**
    * This is the optionid value of the display option as it will appear in the
    * XML document that is constrained to the sys_Options.dtd for focus border
    * color.
    */
   private final static String FOCUS_COLOR_OPTIONID =
      "component_focuscolor";
      
   /**
    * This is the optionid value of the display option as it will appear in the
    * XML document that is constrained to the sys_Options.dtd for font in main
    * display of the applet.
    */
   private final static String FONT_OPTIONID = "font";

   /**
    * The optionid that represents the menu text color.
    */
   private final static String MENU_FOREGROUND_COLOR_OPTIONID =
      "menu_foregroundcolor";

   /**
    * The optionid that represents the menu text color.
    */
   private final static String CONTEXT_MENU_FOREGROUND_COLOR_OPTIONID =
      "contextmenu_foregroundcolor";

   /**
    * The optionid that represents the menu font.
    */
   private final static String MENU_FONT_OPTIONID = "menu_font";

   /**
    * The context of the menu options.
    */
   private final static String MENU_CONTEXT = "menu";

   /**
    * The context of the general options for display.
    */
   private final static String GENERAL_CONTEXT = "general";

   /**
    * This is the optionid value of the display option as it will appear in the
    * XML document that is to indicate that the applet should use font and 
    * display options from the OS rather than from the server.
    */
   private final static String USE_OS_SETTINGS = "use_os_settings";
}
