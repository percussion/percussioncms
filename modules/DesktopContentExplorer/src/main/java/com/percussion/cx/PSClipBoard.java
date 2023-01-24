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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * The class to imitate the clip board for copy and paste and also to support
 * drag and drop.
 */
public class PSClipBoard
{
   /**
    * The default constructor.
    */
   public PSClipBoard()
   {
   }

   /**
    * Sets the list of nodes that are to remember or cache in
    * clip board. Overwrites any content (nodes) before in the clip board. The
    * drag clip board should be used with a 'Drop' or 'Drop-Paste', 'Drop-Move'
    * actions. This cache should be cleared once drop actions are completed by
    * calling {@link #clearDragClip}.
    *
    * @param type the type of clip board, must be one of the <code>TYPE_DRAG
    * </code> or <code>TYPE_COPY</code>
    * @param clip the clip that contains the current selection with parent of
    * clipped nodes, may not be <code>null</code>
    *
    * @throws IllegalArgumentException if any parameter is invalid.
    */
   public void setClip(int type, PSSelection clip)
   {
      checkType(type);

      if(clip == null)
         throw new IllegalArgumentException("clip may not be null.");

      m_clip.put(new Integer(type), clip);
   }

   /**
    * Clears the content in drag clipboard.
    */
   public void clearDragClip()
   {
      Integer key = new Integer(TYPE_DRAG);
      m_clip.remove(key);
   }

   /**
    * Gets the nodes in the supplied type of clip board.
    *
    * @param type the type of clip, must be one of the <code>TYPE_DRAG</code> or
    * <code>TYPE_COPY</code> values.
    * @return the clipped/cached nodes, may be <code>null</code> if the clip is
    * not set or cleared, never empty.
    *
    * @throws IllegalArgumentException if type is invalid.
    */
   public Iterator getClip(int type)
   {
      checkType(type);

      PSSelection clip = (PSSelection)m_clip.get(new Integer(type));
      Iterator clippedNodes = null;
      if(clip != null)
          clippedNodes = clip.getNodeList();

      return clippedNodes;
   }

   /**
    * Gets the selection in the supplied type of clip board.
    *
    * @param type the type of clip, must be one of the <code>TYPE_DRAG</code> or
    * <code>TYPE_COPY</code> values.
    * @return the clipped/cached selection, may be <code>null</code> if the clip is
    * not set or cleared.
    *
    * @throws IllegalArgumentException if type is invalid.
    */
   public PSSelection getClipSelection(int type)
   {
      checkType(type);

      return (PSSelection)m_clip.get(new Integer(type));
   }

   /**
    * Gets the source/parent of the nodes in the supplied type of clip board.
    *
    * @param type the type of clip, must be one of the <code>TYPE_DRAG</code> or
    * <code>TYPE_COPY</code> values.
    *
    * @return the clipped nodes parent, may be <code>null</code> if the clip is
    * not set or cleared.
    *
    * @throws IllegalArgumentException if type is invalid.
    */
   public PSNode getClipSource(int type)
   {
      checkType(type);

      PSSelection clip = (PSSelection)m_clip.get(new Integer(type));
      PSNode clipSource = null;
      if(clip != null)
          clipSource = clip.getParent();

      return clipSource;
   }

   /**
    * Checks the type as one of the clipboard TYPE_xxx values.
    *
    * @param type the clip board type.
    *
    * @throws IllegalArgumentException if type is invalid.
    */
   private void checkType(int type)
   {
      if(!(type == TYPE_DRAG || type == TYPE_COPY))
         throw new IllegalArgumentException("type is invalid.");
   }

   /**
    * The clip that contains entries for supported type of clips. The key is an
    * <code>Integer</code> indicating the type of clip and value is the clip
    * selection (<code>PSSelection</code>).
    */
   private Map m_clip = new HashMap();

   /**
    * The constant to indicate 'Drag' clip board.
    */
   public static final int TYPE_DRAG = 0;

   /**
    * The constant to indicate 'Copy' clip board.
    */
   public static final int TYPE_COPY = 1;
}