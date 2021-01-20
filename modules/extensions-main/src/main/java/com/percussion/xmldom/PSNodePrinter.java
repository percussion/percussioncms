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

package com.percussion.xmldom;

import java.io.Writer;

/**
 * This class is separated from PSXmlTreeWaker.java to fix a specific bug for
 * xmldom package. The idea is to not jeopardise other areas (as treewalker is
 * used everywhere) by making this change for xmldom. The only difference is
 * that the pretty print via indentation is completely disabled. This is to
 * avoid problems with  mixing the content between xml (html) nodes which is
 * very common with any HTML editor control like Ektron control.
 * @deprecated moved to com.percussion.xml package to avoid cyclic dependency
 * after it is being used in a class in com.percussion.cms package. 
 * This class is a deprecated class that simply extends 
 * com.percussion.xml.PSNodePrinter and is kept around for backward compatibility.
 */
public class PSNodePrinter extends com.percussion.xml.PSNodePrinter
{
   /**
    * Only constructor. Takes the print writer as the argument.
    * @param out must not be <code>null</code>.
    * @throws IllegalArgumentException
    */
   public PSNodePrinter(Writer out)
   {
      super(out);
   }   
   
}
