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
 package com.percussion.i18n.rxlt;

import com.percussion.i18n.tmxdom.IPSTmxDocument;

import org.w3c.dom.Element;

/**
 * This interface is implemented by all section handlers. Sections are part of
 * the action {@link IPSActionHandler#ACTIONID_GENERATE_TMX_RESOURCES}. This
 * action is split into four logical sections so that the user gets flexibility
 * of generationg TMX resources for translation from only sections that are
 * known to be modified.
 */

public interface IPSSectionHandler
{
   /**
    * This is the method that actually processes the section.
    * @param cfgData DOM element holding the data for processing the section ,
    * must not be <code>null</code>.
    * @return The newly created TMX document object that subsequently merged
    * with other sections when required, never <code>null</code>.
    * @throws IllegalArgumentException cfgData <code>null</code>)
    * @throws PSSectionProcessingException if any error occurs during processing
    */
   IPSTmxDocument process(Element cfgData)
      throws PSSectionProcessingException;

   /**
    * Section ID for CMS tables part of the Rhythmyx Content Manager
    */
   static final int SECTIONID_CMS_TABLES = 1;
   /**
    * Section ID for XSL Stylesheets of the Rhythmyx Content Manager
    */
   static final int SECTIONID_XSL_STYLESHEETS = 2;
   /**
    * Section ID for Content Editors of the Rhythmyx Content Manager
    */
   static final int SECTIONID_CONTENT_EDITORS = 3;
   /**
    * Section ID for the extension resources. These are typically uploaded to
    * the server along with the exits written by customer when the exits handle
    * the localized error messages.
    */
   static final int SECTIONID_EXTENSION_RESOURCES = 4;
   /**
    * Section ID for JSPs included with Rhythmyx.
    */
   static final int SECTIONID_JSPS = 5;
   /**
    * First Sectionid
    */
   static final int SECTIONID_FIRST = SECTIONID_CMS_TABLES;
   /**
    * Last Sectionid
    */
   static final int SECTIONID_LAST = SECTIONID_JSPS;
}
