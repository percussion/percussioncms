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
 * The class that holds selection source for a menu and context selection source 
 * for a context menu. 
 */
public class PSMenuSource
{   

   /**
    * Default constructor for this object that represents no selection.
    */
   public PSMenuSource()
   {
   }
   
   /**
    * Constructs the menu with the supplied selection.
    * 
    * @param selection the default (navigational) selection for the menu, may
    * not be <code>null</code>
    */
   public PSMenuSource(PSSelection selection)
   {
      setSource(selection);
   }
   
   /**
    * Sets the selection object to be used as source for the menu. 
    * 
    * @param selection the default (navigational) selection for the menu, may
    * not be <code>null</code>
    */
   public void setSource(PSSelection selection)
   {
      if(selection == null)
         throw new IllegalArgumentException("selection may not be null.");
         
      m_source = selection;
   }
   
   /**
    * Gets the current selection source.
    * 
    * @return the selection, may be <code>null</code>
    */
   public PSSelection getSource()
   {
      return m_source;   
   }
   
   /**
    * Gets the current context selection source. See {@link 
    * #setContextSource(PSSelection)} for more information.
    * 
    * @return the selection, may be <code>null</code>
    */
   public PSSelection getContextSource()
   {
      return m_contextSrc;   
   }
   
   /**
    * Sets the context selection source of the menu. Generally this should be
    * called whereever the selection changes (either in navigational or main 
    * display panel).
    * 
    * @param contextSource the context selection source, may not be <code>null
    * </code>
    */
   public void setContextSource(PSSelection contextSource)
   {
      if(contextSource == null)
         throw new IllegalArgumentException("contextSource may not be null.");
         
      m_contextSrc = contextSource;
   }
   
   /**
    * The selection for the menu actions for which this object is set as source.
    * In general this is the selection in navigational panel. <code>null</code>
    * until <code>setSource(PSSelection)</code> is called. 
    */
   private PSSelection m_source = null;
   
   /**
    * The selection for the context menu actions for which this object is set as
    * source. In general this is the recent selection of navigational panel or 
    * main display panel selections. <code>null</code> until <code>
    * setContextSource(PSSelection)</code> is called. 
    */
   private PSSelection m_contextSrc = null;
}
