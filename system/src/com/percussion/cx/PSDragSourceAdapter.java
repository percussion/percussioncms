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
package com.percussion.cx;

import java.awt.Cursor;
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