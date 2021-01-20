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
package com.percussion.i18n.tmxdom;

import java.util.Iterator;

/**
 * This interface defines specific methods to access and modify the translation
 * unit variant of the TMX document. Refer to TMX 1.4 DTD for details at:
 * <p>
 * <a href="http://www.lisa.org/tmx/">Localisation Industry Standards Association</a>
 * </p>
 */
public interface IPSTmxTranslationUnitVariant
   extends IPSTmxNode
{
   /**
    * Method to get all note objects that are associated with the translation
    * unit variant node. There may one note per each supported language in the
    * TMX document.
    * @return Iterator object containg a list of all {@link IPSTmxNote} objects.
    * Never <code>null</code> may be <code>empty</code>.
    */
   Iterator getNotes();

   /**
    * Method to get all property objects that are associated with this translation
    * unit variant node.
    * @return Iterator object containg a list of all {@link IPSTmxProperty}
    * objects. Never <code>null</code> may be <code>empty</code>.
    */
   Iterator getProperties();

   /**
    * Method to get the segment object associated with this node.
    * @return segment object, never <code>null</code>.
    */
   IPSTmxSegment getSegment();

   /**
    * Method to add supplied note object to this node only if does not already
    * exist.
    * @param    note   tmx note to be added to the tree, must not be
    * <code>null</code>.
    */
   void addNote(IPSTmxNote note);

   /**
    * Method to add supplied tmx property  object to this node only if does not
    * already exist.
    * @param property tmx property object, must not be <code>null</code> or
    * <code>empty</code>.
    */
   void addProperty(IPSTmxProperty property);

   /**
    * Method to add supplied tmx segment object to this node only if does not
    * already exist.
    * @param segment tmx object, must not be <code>null</code> or
    * <code>empty</code>.
    */
   void addSegment(IPSTmxSegment segment);

   /**
    * Returns langauge attribute of this node
    * @return language String associated with node, Never <code>null</code> or
    * <code>empty</code>.
    */
   String getLang();
}
