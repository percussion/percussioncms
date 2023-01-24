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
