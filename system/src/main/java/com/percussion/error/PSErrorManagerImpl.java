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

package com.percussion.error;

import com.percussion.i18n.PSI18nUtils;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.PSRequest;
import com.percussion.server.PSRequestContext;

import java.util.Locale;


/**
 * The PSErrorManager class is used to load the error string resources and
 * default error pages based upon the E2 server's locale.
 * <p>
 * Error messages are broken down into ranges, assigned to the various
 * components. The ranges we are using are as follows:
 * <table border="1">
 *    <tr><th>Range</th>      <th>Component</th></tr>
 *    <tr><td>0001 - 1000</td><td>HTML - this is HTML's range of errors</td></tr>
 *    <tr><td>1001 - 2000</td><td>Server Core (including hooks)</td></tr>
 *    <tr><td>2001 - 3000</td><td>Object Store</td></tr>
 *    <tr><td>3001 - 4000</td><td>Connectivity</td></tr>
 *    <tr><td>4001 - 5000</td><td>Cataloger</td></tr>
 *    <tr><td>5001 - 6000</td><td>Back-end (DBMS) Data Processing</td></tr>
 *    <tr><td>6001 - 7000</td><td>XML Data Processing</td></tr>
 *    <tr><td>7001 - 8000</td><td>Exit Processing</td></tr>
 *    <tr><td>8001 - 9000</td><td>Server Admin</td></tr>
 * </table>
 * <p>
 * All error messages are stored using the format defined in
 * the java.text.MessageFormat class. The message string contains curly
 * braces around parameters, which are 0 based. The error manager provides
 * two utility methods which take advantage of the MessageFormat.format
 * method. The following example uses an array of arguments to generate the
 * appropriate string:
 * <pre><code>
 *    String msg = PSErrorManager.getErrorText(999);
 *
 *    // let's assume the returned message is:
 *    //    "param 1={0}, param 2 date={1,date}, param 2 time={1,time}"
 *
 *    Object[] args = { new Integer(1), new Date() };
 *
 *    String displayMsg = PSErrorManager.createMessage(msg, args);
 *
 *    // displayMsg is returned containing:
 *    //    "param1=1, param 2 date=Jan 6, 1999, param 2 time=4:50 PM"
 * </code></pre>
 *
 * This model is excellent for internationalization as the position of the
 * parameters may change based upon the target language.
 *
 * @author     Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSErrorManagerImpl extends PSErrorManagerDefaultImpl {

 @Override
 public String getErrorText( int code,
                             boolean nullNotFound,
                             String language) {

    String key = null;
    try
    {
       key = Integer.toString(code);
    }
    catch(Exception e)
    {
       //do nothing
    }
    if(key == null)
       return null;
    //Get the string from TMX resource bundle
    String result = PSI18nUtils.getString(key, language);

    if(result.equals(key) && nullNotFound) { //resource not found}
       return super.getErrorText(code,
       nullNotFound,
       language);
    }

    return result;
 }

    @Override
   public String getErrorText( int code,
                               boolean nullNotFound,
                               Locale loc) {
          //first look in the i18n resource
         PSRequest req = null;

         if (ms_isServerSide) {
            String lang = null;
            if (req != null) {
               IPSRequestContext ctx = new PSRequestContext(req);
               lang = ctx.getUserLocale();
            }

            if (null == lang) {
               lang = PSI18nUtils.DEFAULT_LANG;
            }

            String key = String.valueOf(code);
            String i18nMsg = PSI18nUtils.getString(key, lang);
            if (i18nMsg != null && !i18nMsg.equals(key))
               return i18nMsg;
         }

         return super.getErrorText(code,
                 nullNotFound,
                 loc);
      }




}

