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
package com.percussion.workflow;

import com.percussion.data.PSConversionException;
import com.percussion.data.PSDataExtractionException;
import com.percussion.extension.PSSimpleJavaUdfExtension;
import com.percussion.i18n.PSI18nUtils;
import com.percussion.server.IPSRequestContext;

/**
 * A document in workflow can be in 1 of 3 conditions regarding its checked
 * out status:
 * <ol>
 *    <li>Not checked out</li>
 *    <li>Checked out by the current user</li>
 *    <li>Checked out by someone else</li>
 * </ol>
 * This exit analyzes the user information and returns a String that contains
 * a String representation of the value. These constants are defined in
 * PSWorkFlowUtils.
 *
 * @see PSWorkFlowUtils
 */
public class PSGetCheckOutStatusUdf extends PSSimpleJavaUdfExtension
{
   /* ************ IPSUdfProcessor Interface Implementation ************ */

   /**
    * Returns a constant indicating the checked-out state of the document.
    *
    * @param params A single parameter is expected which is the name of the
    *    user who currently has the document checked out. Typically, this
    *    is passed in by using a Backend column replacement value that gets
    *    the data from the contentstatus table. May be empty or <code>null
    *    </code>.
    *
    * @param request The current request context, never <code>null</code>.
    *
    * @return An integer as a String indicating the status.
    *
    * @throws  PSConversionException If any data extraction from the request
    *    fails.
    */
   public Object processUdf(Object[] params, IPSRequestContext request)
      throws PSConversionException
   {
      try
      {
         String status = CHECKOUT_STATUS_NONE;

         if ( null != params && params.length >= 1 && null != params[0]
               && params[0].toString().trim().length() > 0 )
         {
            String checkedOutUser = params[0].toString().trim();
            // someone has it checked out
            Object o = request.getUserContextInformation( "User/name", "" );
            String currentUser = null;
            if ( null != o )
               currentUser = o.toString().trim();
            if ( null != currentUser
                  && currentUser.equalsIgnoreCase( checkedOutUser ))
            {
               status = CHECKOUT_STATUS_CURRENT_USER;
            }
            else
               status = CHECKOUT_STATUS_OTHER;
         }
         return status;
      }
      catch ( PSDataExtractionException e )
      {
         String lang = e.getLanguageString();
         if (lang == null)
            lang = PSI18nUtils.DEFAULT_LANG;
         throw new PSConversionException( lang, e.getErrorCode(),
               e.getErrorArguments());
      }
   }

   /**
    * One of the possible constants returned by this method. It means the
    * current document is not checked out. Immutable after it is initialized
    * in the <code>init</code> method.
    */
   private static String CHECKOUT_STATUS_NONE =
         ""+PSWorkFlowUtils.CHECKOUT_STATUS_NONE;

   /**
    * One of the possible constants returned by this method. It means the
    * current document is checked out by the user making this request.
    * Immutable after it is initialized in the <code>init</code> method.
    */
   private static String CHECKOUT_STATUS_CURRENT_USER =
            ""+PSWorkFlowUtils.CHECKOUT_STATUS_CURRENT_USER;

   /**
    * One of the possible constants returned by this method. It means the
    * current document is checked out by someone other than the requestor.
    * Immutable after it is initialized in the <code>init</code> method.
    */
   private static String CHECKOUT_STATUS_OTHER =
         ""+PSWorkFlowUtils.CHECKOUT_STATUS_OTHER;
}

