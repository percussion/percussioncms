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
