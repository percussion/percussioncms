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

