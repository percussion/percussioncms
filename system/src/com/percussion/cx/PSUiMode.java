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
package com.percussion.cx;

/**
 * The class to represent a view with its mode in UI.
 */
public class PSUiMode  
{
   /**
    * Constructs the view mode of UI based on the supplied parameters.
    * 
    * @param view the current view, must be one of the TYPE_VIEW_xx values.
    * @param mdoe the mode of the current view, must be one of the TYPE_MODE_xx 
    * values.
    * 
    * @throws IllegalArgumentException if any parameter is invalid.
    */
   public PSUiMode(String view, String mode)
   {
      if( !isValidView(view) )
      {
         throw new IllegalArgumentException(
            "view must be one of the following:" + TYPE_VIEW_CX + "," + 
            TYPE_VIEW_IA + "," + TYPE_VIEW_DT + "," + TYPE_VIEW_RC);
      }
      
      if( !(TYPE_MODE_NAV.equals(mode) || TYPE_MODE_MAIN.equals(mode)) )
      {
         throw new IllegalArgumentException(
            "mode must be one of the following:" + TYPE_MODE_NAV + "," + 
            TYPE_MODE_MAIN);
      }
      
      m_view = view;
      m_mode = mode;
   }

   /**
    * Gets the view of this object.
    * 
    * @return the mode, never <code>null</code> or empty and will be one of the 
    * TYPE_xxx values. 
    */
   public String getView()
   {
      return m_view;
   }

   /**
    * Gets the view mode of this object.
    * 
    * @return the mode, never <code>null</code> or empty and will be one of the 
    * TYPE_xxx values. 
    */
   public String getViewMode()
   {
      return m_view + m_mode;
   }
   
   /**
    * Finds whether the view is one of the allowed views or not.
    * 
    * @param view the view to check, may be <code>null</code> or empty.
    * 
    * @return <code>true</code> if one of the TYPE_VIEW_xxx values, otherwise
    * <code>false</code>
    */
   public static boolean isValidView(String view)
   {
      if( TYPE_VIEW_CX.equals(view) || TYPE_VIEW_IA.equals(view) || 
          TYPE_VIEW_DT.equals(view) || TYPE_VIEW_RC.equals(view))
      {
         return true;
      }
      
      return false;
   }
   
   /**
    * The current view of UI, initialized in the constructor and never <code>
    * null</code>, empty or modified after that.
    */
   private String m_view;
   
   /**
    * The represented mode of UI, initialized in the constructor and never 
    * <code>null</code>, empty or modified after that.
    */
   private String m_mode;
   
   /**
    * The constant to indicate 'Content Explorer' view.
    */
   public static final String TYPE_VIEW_CX = "CX";
   
   /**
    * The constant to indicate 'Item Assembly' view.
    */
   public static final String TYPE_VIEW_IA = "IA";
   
   /**
    * The constant to indicate 'Dependency Tree' view.
    */
   public static final String TYPE_VIEW_DT = "DT";
   
   /**
    * The constant to indicate 'Related Content Search' view.
    */
   public static final String TYPE_VIEW_RC = "RC";
   
   /**
    * The constant to indicate 'Navigational' mode.
    */
   public static final String TYPE_MODE_NAV = "NAV";
   
   /**
    * The constant to indicate 'Main Display' mode.
    */
   public static final String TYPE_MODE_MAIN = "MAIN";

   /**
    * The constant to indicate 'Navigation' mode in 'Content Explorer' view.
    */
   public static final String TYPE_CXNAV = TYPE_VIEW_CX + TYPE_MODE_NAV;
   
   /**
    * The constant to indicate 'Main Display' mode in 'Content Explorer' view.
    */
   public static final String TYPE_CXMAN = TYPE_VIEW_CX + TYPE_MODE_MAIN;
   
   /**
    * The constant to indicate 'Navigation' mode in 'Item Assembly' view.
    */
   public static final String TYPE_IANAV = TYPE_VIEW_IA + TYPE_MODE_NAV;
   
   /**
    * The constant to indicate 'Main Display' mode in 'Item Assembly' view.
    */
   public static final String TYPE_IAMAIN = TYPE_VIEW_IA + TYPE_MODE_MAIN;
   
   /**
    * The constant to indicate 'Navigation' mode in 'Dependency Tree' view.
    */
   public static final String TYPE_DTREE = TYPE_VIEW_DT + TYPE_MODE_NAV;
   
   /**
    * The constant to indicate 'Navigation' mode in 'Related Content Search' view.
    */
   public static final String TYPE_RCNAV = TYPE_VIEW_RC + TYPE_MODE_NAV;

   /**
    * The constant to indicate 'Main Display' mode in 'Related Content Search' 
    * view.
    */
   public static final String TYPE_RCMAIN = TYPE_VIEW_RC + TYPE_MODE_MAIN;
}