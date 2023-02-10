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


