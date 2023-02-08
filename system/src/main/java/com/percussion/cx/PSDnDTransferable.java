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
