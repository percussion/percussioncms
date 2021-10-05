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

package com.percussion.filetracker;


/**
 * PSFUDNodeModel is a PSFUDTreeTableModel representing a Rhythmyx FUD Manager
 * Application. Nodes in the PSFUDNodeModel are PSFUDNodes. Note that these
 * nodes are NOT derived from DOM Node and only encapsulate the DOM element.
 */

public class PSFUDNodeModel
   extends AbstractTreeTableModel
{
   public PSFUDNodeModel(Object obj)
   {
      super(obj);
   }

   //
   // The TreeModel interface
   //
   public int getChildCount(Object node)
   {
      if(!(node instanceof IPSFUDNode))
         return 0;

      Object[] children = ((IPSFUDNode)node).getChildren();
      return (children == null) ? 0 : children.length;
   }

   public Object getChild(Object node, int i)
   {
      if(!(node instanceof IPSFUDNode))
         return null;

      return ((IPSFUDNode)node).getChildren()[i];
   }

   public boolean isLeaf(Object node)
   {
      if(node instanceof PSFUDFileNode)
         return true;

      return false;
   }

   //
   //  The TreeTableNode interface.
   //

   public int getColumnCount()
   {
      return ms_cNames.length;
   }

   public String getColumnName(int column)
   {
      return ms_cNames[column];
   }

   public Class getColumnClass(int column)
   {
      return ms_cTypes[column];
   }

   public Object getValueAt(Object node, int column)
   {
      IPSFUDNode fudNode = (IPSFUDNode)node;
      switch(column)
      {
         case 0:
               return fudNode.toString();
         case 1: //Size
            if(fudNode instanceof PSFUDFileNode)
            {
               PSFUDFileNode fileNode = (PSFUDFileNode)fudNode;
               try
               {
                  return Long.valueOf(fileNode.getSize());
               }
               catch(Exception e)
               {
                  return new Long(0L);
               }
            }
            else
               return null;

         case 2: //Modified Date
            if(fudNode instanceof PSFUDFileNode)
            {
               PSFUDFileNode fileNode = (PSFUDFileNode)fudNode;
               return fileNode.getModified();
            }
            else
               return null;
      }

      return null;
   }

   // Names of the columns.
   static protected String[]  ms_cNames = {"Name", "Size", "Modified"};

   // Types of the columns.
   static protected Class[]  ms_cTypes = {PSFUDTreeTableModel.class,
                                                Integer.class, String.class};

   // The the returned file length for directories.
   public static final Integer ZERO = new Integer(0);

}


