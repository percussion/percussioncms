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

package com.percussion.log;



/**
 * The PSLogSubMessage class is used to store sub-messages. This contains
 * the type code of the sub-message and its associated text. Certain
 * messages, such as PSLogApplicationStatistics, log an entry for each
 * statistic (providing search capabilities). For these messages, many
 * sub-messages will be handed to the log writer.
 *
 * @author     Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSLogSubMessage {
   /**
    * Construct a log sub-message from the specified information.
    *
    * @param   type     the sub-message type code
    * @param   text     the sub-message text
    */
   public PSLogSubMessage(int type, java.lang.String text)
   {
      super();
      m_type = type;
      m_text = text;
   }

   /**
    * Get the sub-message type.
    *
    * @return  the sub-message type code
    */
   public int getType()
   {
      return m_type;
   }

   /**
    * Get the sub-messages text.
    *
    * @return  the sub-message text
    */
   public java.lang.String getText()
   {
      return m_text;
   }


   private  int               m_type;
   private  java.lang.String  m_text;
}

