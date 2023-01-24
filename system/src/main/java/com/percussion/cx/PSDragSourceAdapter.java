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

import java.awt.*;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceContext;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;

/**
 * The adapter class to listen on drag source events. The method implementations
 * are empty except for {@link #dragOver(DragSourceDragEvent)}. See the link for
 * default behavior.
 */
public class PSDragSourceAdapter implements DragSourceListener
{
   public void dragEnter(DragSourceDragEvent dsde)
   {
      DragSourceContext context = dsde.getDragSourceContext();
      context.setCursor(DragSource.DefaultMoveDrop);
   }
   
   public void dragExit(DragSourceEvent dse)
   {
      DragSourceContext context = dse.getDragSourceContext();
      context.setCursor(DragSource.DefaultMoveNoDrop);
   }

   /**
    * Displays copy cursor if the control key is pressed, otherwise regular move
    * cursor. See interface for some more description about this method
    */
   public void dragOver(DragSourceDragEvent dsde)
   {
//      DragSourceContext context = dsde.getDragSourceContext();
//      if( (dsde.getDropAction() & DnDConstants.ACTION_COPY) != 0)
//         context.setCursor(DragSource.DefaultCopyDrop);
//      else
//         context.setCursor(DragSource.DefaultMoveDrop);
   }

   public void dropActionChanged(DragSourceDragEvent dsde)
   {
   }
   
   public void dragDropEnd(DragSourceDropEvent dsde)
   {
      DragSourceContext context = dsde.getDragSourceContext();
      context.setCursor(Cursor.getDefaultCursor());
   }
}
