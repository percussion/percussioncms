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
