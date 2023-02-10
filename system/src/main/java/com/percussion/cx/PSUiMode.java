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

/**
 * The class to represent a view with its mode in UI.
 */
public class PSUiMode  
{
   /**
    * Constructs the view mode of UI based on the supplied parameters.
    * 
    * @param view the current view, must be one of the TYPE_VIEW_xx values.
    * @param mode the mode of the current view, must be one of the TYPE_MODE_xx
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
