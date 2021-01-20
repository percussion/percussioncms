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

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;

/**
 * The class to represent a dummy <code>Transferable</code> object to use with 
 * drag and drop. Applet uses {@link #PSClipBoard} for actual data transfer.
 */
public class PSDnDTransferable implements Transferable
{      
   /**
    * Constructs the transferable object with the dummy data.
    * 
    * @param data the transferable data, may be <code>null</code>. 
    */      
   public PSDnDTransferable(Object data)
   {
      m_data = data;
   }   
   
   /**
    * Convenience constructor for {@link #PSDnDTransferable(Object) 
    * PSDnDTransferable(null) }.
    */
   public PSDnDTransferable()
   {
      this(null);
   }   

   //implements interface method 
   public DataFlavor[] getTransferDataFlavors()
   {
      return flavors;
   }

   //implements interface method to return true always.
   public boolean isDataFlavorSupported(DataFlavor flavor)
   {      
      return true;
   }
   
   //implements interface method
   public Object getTransferData(DataFlavor flavor)
   {
      return m_data;
   }
   
   /**
    * The transferable object, initialized in the ctor and never modified after 
    * that. May be <code>null</code>.
    */
   private Object m_data;

   /**
    * The supported data flavors, supports any kind of object, it was a problem 
    * with drag and drop defining the <code>Object.class</code> as the class for
    * data flavor, so uses <code>String.class</code>.
    */   
   private static final DataFlavor ms_flavor =  
      new DataFlavor(String.class, "Any object");
      
   /**
    * The list of supported flavors.
    */
   private static final DataFlavor[] flavors = new DataFlavor[] {ms_flavor};
}
