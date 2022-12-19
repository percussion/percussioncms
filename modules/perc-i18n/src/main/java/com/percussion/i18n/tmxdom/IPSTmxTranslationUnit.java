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
package com.percussion.i18n.tmxdom;

import java.util.Iterator;

/**
 * This interface defines specific methods to access and modify the translation
 * unit of the TMX document. As the name indicates transition variant is one
 * unit of translation that has variants for different languages. Translation
 * Variant has a unique id to locate it in the TMX document. Details can be seen
 * in the TMX 1.4 DTD at:
 * <p>
 * <a href="http://www.lisa.org/tmx/">Localisation Industry Standards Association</a>
 * </p>
 */
public interface IPSTmxTranslationUnit
   extends IPSTmxNode
{
   /**
    * Method to get the translation unit key (attribute 'tuid' of &lt;tu&gt;
    * element). This is the uniques identifier for a translation unit.
    * @return key, never <code>null</code> or <code>empty</code>.
    */
   public String getKey();

   /**
    * Method to get all note objects that are associated with the translation
    * unit. Typically, there will be at least one note in default language and
    * optionally one each for other langauge(s).
    * @return Iterator object containg a list of all {@link IPSTmxNote} objects.
    * Never <code>null</code> may be <code>empty</code>.
    */
   public Iterator getNotes();

   /**
    * Method to get all property objects that are associated with this
    * translation unit.
    * @return Iterator object containg a list of all {@link IPSTmxProperty}
    * objects. Never <code>null</code> may be <code>empty</code>.
    */
   public Iterator getProperties();

   /**
    * Method to get all translation unit variant objects that are associated with
    * this  translation unit.
    * @return Iterator object containg a list of all {@link IPSTmxTranslationUnit}
    * objects. Never <code>null</code> may be <code>empty</code>.
    */
   public Iterator getTransUnitVariants();
   
   /**
    * Find a specific translation unit by language name
    * @param lang a specific language name, must never be <code>null</code> 
    * or empty
    * @return a variant, or <code>null</code> if there is no variant for 
    * the given language.
    */
   public IPSTmxTranslationUnitVariant getTransUnitVariant(String lang);

   /**
    * Method to add supplied translation unit variant object to this node.
    * 
    * @param    tuv    translation unit variant to be added to the tree, must
    * not be <code>null</code>.
    * @param    overwrite    set to <code>true</code> in order to overwrite
    * an existing translation unit variant if found, <code>false</code>
    * otherwise.
    */
   public void addTuv(IPSTmxTranslationUnitVariant tuv, boolean overwrite);

   /**
    * Method to add supplied note object to this node.
    * @param    note   tmx note to be added to the tree, must not be
    * <code>null</code>.
    * @param    overwrite    set to <code>true</code> in order to overwrite
    * an existing note if found, <code>false</code> otherwise.
    */
   public void addNote(IPSTmxNote note, boolean overwrite);

   /**
    * Method to add supplied tmx property  object to this node only if does not
    * already exist.
    * @param prop tmx property object, must not be <code>null</code> or
    * <code>empty</code>.
    */
   public void addProperty(IPSTmxProperty prop);
}
