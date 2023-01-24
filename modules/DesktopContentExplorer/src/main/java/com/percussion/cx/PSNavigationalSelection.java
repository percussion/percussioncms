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

import com.percussion.cx.objectstore.PSNode;

import java.util.Iterator;

/**
 * The class that represents a selection in navigational tree.
 */
public class PSNavigationalSelection extends PSSelection
{
   /**
    * Constructs the selection with supplied parameters.
    *
    * @param uiMode the ui mode that represents the component view which is the
    * source of the selection, may not be <code>null</code>
    * @param parent the parent of the supplied node list, may be <code>null</code>
    * @param nodeList the list of <code>PSNode</code> objects that are selected,
    * may not be <code>null</code> or empty.
    *
    * @param path the selection path, may not be <code>null</code> or empty.
    */
   public PSNavigationalSelection(PSUiMode uiMode, PSNode parent,
                                  Iterator nodeList, String path)
   {
      super(uiMode, parent, nodeList);

      if(path == null || path.trim().length() == 0)
         throw new IllegalArgumentException("path may not be null or empty");

      m_path = path;
   }

   /**
    * Gets the path of this selection.
    *
    * @return the path, never <code>null</code> or empty.
    */
   public String getSelectionPath()
   {
      return m_path;
   }

   /**
    * The path of the selection, initialized in the ctor and never <code>null
    * </code> or modified after that.
    */
   private String m_path;
}