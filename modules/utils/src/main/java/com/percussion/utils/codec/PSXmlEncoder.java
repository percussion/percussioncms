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
         StringBuilder rval = new StringBuilder((int) (input.length() * 1.5));
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
               default:  //TODO: Will this break unicode?
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
