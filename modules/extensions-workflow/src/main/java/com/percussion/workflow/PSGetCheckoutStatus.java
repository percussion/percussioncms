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

package com.percussion.workflow;


import com.percussion.cms.IPSConstants;
import com.percussion.data.PSConversionException;
import com.percussion.data.PSDataExtractionException;
import com.percussion.error.PSException;
import com.percussion.error.PSExceptionUtils;
import com.percussion.extension.IPSUdfProcessor;
import com.percussion.extension.PSSimpleJavaUdfExtension;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.IPSServerErrors;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.naming.NamingException;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * This class implements the UDF processor interface, so it can be used as a
 * Rhythmyx function. See {@link #processUdf(Object[], IPSRequestContext)
 * processUdf} for a description. This UDF is sepcifically designed for Content
 * Explorer, though it can be theoretically used elsewhere.
 * <p>
 * This UDF evaluates the checkout status to an image given the contentid of the
 * item and image url options. Checkout status is evaluated based on the current
 * checkout user name of the item which has the following possible values:
 * <ol>
 * <li>nobody, when checkout user name is empty</li>
 * <li>by someone else, when logged in user name does not matche with checkout
 * user name</li>
 * <li>by me, when logged in user name matches with checkout user name</li>
 * </ol>
 * The UDF takes the content id as the first parameter and the rest are the image
 * url options for each possible checkout status values above in that order.
 * <p>
 * The image urls can either be standard Applet Url in the syntax of
 * "../app/resource.gif" or the icon key. An icon key is simple text not starting
 * with "..". In the first case, the Content Explorer loads the image by executing
 * the url request. In latter case, the image is loaded from the Content Explorer
 * archive (com/percussion/cx/images/iconkey.gif). It is recommended that the
 * images match the normal icon images in the content explorer.
 */
public class PSGetCheckoutStatus extends PSSimpleJavaUdfExtension
   implements IPSUdfProcessor
{
   /**
    * Determines the image url to use.
    * 
    * @param params The request parameters, never <code>null</code>.  See class
    * description for more info.
    * @param request The current request, guaranteed not <code>null</code> by
    * the interface.
    * 
    * @return The correct image, never <code>null</code>, may be empty if all 3 
    * image params were not supplied.
    * 
    * @throws PSConversionException if there are any errors.
    */
   public Object processUdf(Object[] params, IPSRequestContext request)
      throws PSConversionException
   {
      if ( null == params || params.length < 1 || null == params[0]
         || 0 == params[0].toString().trim().length()||!StringUtils.isNumeric(params[0].toString().trim()))
         return "";


      String contentid = params[0].toString().trim();
      String result = "Default";

      PSConnectionMgr connectionMgr = null;
      PSContentStatusContext csc = null;

      try{
         connectionMgr = new PSConnectionMgr();
      } catch (SQLException e) {
         log.error("Error getting JDBC Connection Manager. Error: {}",PSExceptionUtils.getMessageForLog(e));
         throw new PSConversionException(e);
      }

      try(Connection conn = connectionMgr.getConnection())
      {
         int iContentId = Integer.parseInt(contentid);

         csc = new PSContentStatusContext(conn, iContentId);
         String checkoutuser = csc.getContentCheckedOutUserName();
         result = getCheckoutStatus(checkoutuser, params, request);
      }
      catch (PSException e)
      {
         log.error(PSExceptionUtils.getMessageForLog(e));
         throw new PSConversionException(e.getErrorCode(),
            e.getErrorArguments());
      }
      catch (SQLException e)
      {
         log.error(PSExceptionUtils.getMessageForLog(e));
         throw new PSConversionException(IPSServerErrors.SQL_PROBLEM, PSExceptionUtils.getMessageForLog(e));
      }
      catch (NamingException e)
      {
         log.error(PSExceptionUtils.getMessageForLog(e));
         throw new PSConversionException(e);
      }
      
      return result;
   }
   
   /**
    * Determine checkout status based on the name of the checked out user.
    * 
    * @param checkoutUser The name of the user that current has the item 
    * checked out, assumed not <code>null</code>, may be empty.
    * @param params The request params supplied to this udf, see 
    * <code>processUdf()</code> for more info.
    * @param request The current request context, used to determine the current
    * user, assumed not <code>null</code>.
    * 
    * @return The image to use, never <code>null</code>, may be empty.
    * 
    * @throws PSDataExtractionException if the current user cannot be 
    * determined.
    */
   protected static String getCheckoutStatus(String checkoutUser, 
      Object[] params, IPSRequestContext request) 
         throws PSDataExtractionException
   {
      String userName = request.getUserContextInformation(
         "User/Name", "unknown").toString();
      
      String somebodyImage = IPSConstants.CHECKOUT_STATUS_SOMEONEELSE;
      String nobodyImage = IPSConstants.CHECKOUT_STATUS_NOBODY;
      String myselfImage = IPSConstants.CHECKOUT_STATUS_MYSELF;
      
      if(params.length > 1 && params[1].toString().trim().length()>0)
         nobodyImage = params[1].toString();
      if(params.length > 2 && params[2].toString().trim().length()>0)
         somebodyImage = params[2].toString();
      if(params.length > 3 && params[3].toString().trim().length()>0)
         myselfImage = params[3].toString();

      String result;
      if(checkoutUser.trim().length() < 1)
         result = nobodyImage;
      else if(checkoutUser.equalsIgnoreCase(userName))
         result = myselfImage;
      else
         result = somebodyImage;
      
      return result;
   }

    protected static final Logger log = LogManager.getLogger(IPSConstants.WORKFLOW_LOG);

}

