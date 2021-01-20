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
package com.percussion.utils.codec;

import org.apache.commons.codec.Encoder;

/**
 * Replace xml reserved characters with their entity equivalents.
 * 
 * @author dougrand
 */
public class PSXmlEncoder implements Encoder
{
   /**
    * 
    */
   public PSXmlEncoder() {
      super();
   }

   
   /**
    * (non-Javadoc)
    * 
    * @see org.apache.commons.codec.Encoder#encode(java.lang.Object)
    */
   public Object encode(Object arg0)
   {
      if(arg0 != null) {
         String input = (String) arg0;
         StringBuffer rval = new StringBuffer((int) (input.length() * 1.5));
         for (int i = 0; i < input.length(); i++) {
            char ch = input.charAt(i);
            switch (ch) {
               case '<':
                  rval.append("&lt;");
                  break;
               case '>':
                  rval.append("&gt;");
                  break;
               case '\"':
                  rval.append("&quot;");
                  break;
               case '&':
                  rval.append("&amp;");
                  break;
               case '\'':
                  rval.append("&apos;");
                  break;
               default:
                  if (ch > 127) {
                     rval.append("&#");
                     rval.append(Integer.toString(ch));
                     rval.append(';');
                  } else {
                     rval.append(ch);
                  }
            }
         }
         return rval.toString();
      }else{
         return arg0;
      }
   }
}
