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

package com.percussion.design.objectstore.server;

import com.percussion.server.PSRequest;
import com.percussion.server.PSServer;

import java.lang.reflect.Method;

/**
 *
 *      This class defines a "secured" method which must check a specified
 *      user's access level to ensure the selected operation can be executed.
 *
 * @author:      David Gennaco
 *   @version:   1.0
 * @since      1.0
 *
 ***************************************************************************/
class PSSecuredMethod
{

   /**
    *
    * Secured method constructor.  Get the method and access level
    * required to be allowed to invoke it
    * this object.
    *
    *   @param      method               The method
    *
    *   @param      requiredAccessLevel   The access level needed to invoke
    *
    */
   public PSSecuredMethod(Method method, int requiredAccessLevel)
   {
      m_method = method;
      m_requiredAccessLevel = requiredAccessLevel;
   }

   /**
    *
    * This function invokes the member method if the user authentication
    * defined in the request meets the required access level criteria of
    * this object.
    *
    *   @param      req      The current request
    *
    *   @param      currentObject   The object containing the method.
    *
    *   @param      args      The arguments to supply to the specified method
    *
    * @return      the return value of the invoked method
    *
    */
   public Object invoke(PSRequest req,
                        Object currentObject, Object[] args)
      throws   com.percussion.security.PSAuthorizationException, 
               com.percussion.security.PSAuthenticationFailedException,
               com.percussion.security.PSAuthenticationRequiredException,
               java.lang.reflect.InvocationTargetException,
               java.lang.IllegalAccessException
   {
      PSServer.checkAccessLevel(req, m_requiredAccessLevel);

      return m_method.invoke(currentObject, args);
   }

   Method   m_method;
   int      m_requiredAccessLevel;
}
