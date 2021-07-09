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
package com.percussion.services.assembly.jexl;

import com.percussion.extension.IPSJexlMethod;
import com.percussion.extension.IPSJexlParam;
import com.percussion.extension.PSJexlUtilBase;
import com.percussion.server.PSRequest;
import com.percussion.server.PSRequestContext;
import com.percussion.util.PSUrlUtils;
import com.percussion.utils.request.PSRequestInfo;

import java.net.MalformedURLException;

import org.apache.commons.lang.StringUtils;

/**
 * Link manipulation utilities
 * 
 * @author dougrand
 */
public class PSLinkUtils extends PSJexlUtilBase
{
   /**
    * Process url add params
    * 
    * @param urlbase base url, never <code>null</code> or empty
    * @param strings an array of param names and values to add. Note that the
    *           names and values must match up, i.e. the array must have a
    *           length divisible by 2
    * @return the updated url, appropriately modified
    */
   private String doAddParams(String urlbase, String... strings)
   {
      if (StringUtils.isBlank(urlbase))
      {
         throw new IllegalArgumentException("urlbase may not be null or empty");
      }
      if (strings == null || strings.length == 0)
      {
         throw new IllegalArgumentException("strings may not be null or empty");
      }
      if (strings.length % 2 != 0)
      {
         throw new IllegalArgumentException(
               "strings must contain an even count");
      }
      boolean appendQ = urlbase.indexOf('?') < 0;

      StringBuilder rval = new StringBuilder(urlbase);
      for (int i = 0; i < strings.length; i += 2)
      {
         String name = strings[i];
         String val = strings[i + 1];
         if (appendQ)
            rval.append('?');
         else
            rval.append('&');
         appendQ = false;
         rval.append(name);
         rval.append('=');
         rval.append(val);
      }
      return rval.toString();
   }

   /**
    * add 1 param pair to a url
    * 
    * @param urlbase the base url to append the parameters to
    * @param name1 the name of the parameter
    * @param value1 the value of the parameter
    * @return the modified url
    */
   @IPSJexlMethod(description = "add 1 param pair to a url", params =
   {
         @IPSJexlParam(name = "urlbase", description = "the base url to append the parameters to"),
         @IPSJexlParam(name = "name1", description = "the name of the parameter"),
         @IPSJexlParam(name = "value1", description = "the value of the parameter")}, returns = "Object")
   public String addParams(String urlbase, String name1, String value1)
   {
      return doAddParams(urlbase, name1, value1);
   }

   /**
    * add 2 param pairs to a url
    * 
    * @param urlbase the base url to append the parameters to
    * @param name1 the name of the parameter
    * @param value1 the value of the parameter
    * @param name2 the name of the parameter
    * @param value2 the value of the parameter
    * @return the modified url
    */
   @IPSJexlMethod(description = "add 2 param pairs to a url", params =
   {
         @IPSJexlParam(name = "urlbase", description = "the base url to append the parameters to"),
         @IPSJexlParam(name = "name1", description = "the name of the parameter"),
         @IPSJexlParam(name = "value1", description = "the value of the parameter"),
         @IPSJexlParam(name = "name2", description = "the name of the parameter"),
         @IPSJexlParam(name = "value2", description = "the value of the parameter")}, returns = "Object")
   public String addParams(String urlbase, String name1, String value1,
         String name2, String value2)
   {
      return doAddParams(urlbase, name1, value1, name2, value2);
   }

   /**
    * add 3 param pairs to a url
    * 
    * @param urlbase the base url to append the parameters to
    * @param name1 the name of the parameter
    * @param value1 the value of the parameter
    * @param name2 the name of the parameter
    * @param value2 the value of the parameter
    * @param name3 the name of the parameter
    * @param value3 the value of the parameter
    * @return the modified url
    */
   @IPSJexlMethod(description = "add 3 param pairs to a url", params =
   {
         @IPSJexlParam(name = "urlbase", description = "the base url to append the parameters to"),
         @IPSJexlParam(name = "name1", description = "the name of the parameter"),
         @IPSJexlParam(name = "value1", description = "the value of the parameter"),
         @IPSJexlParam(name = "name2", description = "the name of the parameter"),
         @IPSJexlParam(name = "value2", description = "the value of the parameter"),
         @IPSJexlParam(name = "name3", description = "the name of the parameter"),
         @IPSJexlParam(name = "value3", description = "the value of the parameter")}, returns = "Object")
   public String addParams(String urlbase, String name1, String value1,
         String name2, String value2, String name3, String value3)
   {
      return doAddParams(urlbase, name1, value1, name2, value2, name3, value3);
   }

   /**
    * add 4 param pairs to a url
    * 
    * @param urlbase the base url to append the parameters to
    * @param name1 the name of the parameter
    * @param value1 the value of the parameter
    * @param name2 the name of the parameter
    * @param value2 the value of the parameter
    * @param name3 the name of the parameter
    * @param value3 the value of the parameter
    * @param name4 the name of the parameter
    * @param value4 the value of the parameter
    * @return the modified url
    */
   @IPSJexlMethod(description = "add 4 param pairs to a url", params =
   {
         @IPSJexlParam(name = "urlbase", description = "the base url to append the parameters to"),
         @IPSJexlParam(name = "name1", description = "the name of the parameter"),
         @IPSJexlParam(name = "value1", description = "the value of the parameter"),
         @IPSJexlParam(name = "name2", description = "the name of the parameter"),
         @IPSJexlParam(name = "value2", description = "the value of the parameter"),
         @IPSJexlParam(name = "name3", description = "the name of the parameter"),
         @IPSJexlParam(name = "value3", description = "the value of the parameter"),
         @IPSJexlParam(name = "name4", description = "the name of the parameter"),
         @IPSJexlParam(name = "value4", description = "the value of the parameter")}, returns = "Object")
   public String addParams(String urlbase, String name1, String value1,
         String name2, String value2, String name3, String value3,
         String name4, String value4)
   {
      return doAddParams(urlbase, name1, value1, name2, value2, name3, value3,
            name4, value4);
   }

   /**
    * add 5 param pairs to a url
    * 
    * @param urlbase the base url to append the parameters to
    * @param name1 the name of the parameter
    * @param value1 the value of the parameter
    * @param name2 the name of the parameter
    * @param value2 the value of the parameter
    * @param name3 the name of the parameter
    * @param value3 the value of the parameter
    * @param name4 the name of the parameter
    * @param value4 the value of the parameter
    * @param name5 the name of the parameter
    * @param value5 the value of the parameter
    * @return the modified url
    */
   @IPSJexlMethod(description = "add 5 param pairs to a url", params =
   {
         @IPSJexlParam(name = "urlbase", description = "the base url to append the parameters to"),
         @IPSJexlParam(name = "name1", description = "the name of the parameter"),
         @IPSJexlParam(name = "value1", description = "the value of the parameter"),
         @IPSJexlParam(name = "name2", description = "the name of the parameter"),
         @IPSJexlParam(name = "value2", description = "the value of the parameter"),
         @IPSJexlParam(name = "name3", description = "the name of the parameter"),
         @IPSJexlParam(name = "value3", description = "the value of the parameter"),
         @IPSJexlParam(name = "name4", description = "the name of the parameter"),
         @IPSJexlParam(name = "value4", description = "the value of the parameter"),
         @IPSJexlParam(name = "name5", description = "the name of the parameter"),
         @IPSJexlParam(name = "value5", description = "the value of the parameter")}, returns = "Object")
   public String addParams(String urlbase, String name1, String value1,
         String name2, String value2, String name3, String value3,
         String name4, String value4, String name5, String value5)
   {
      return doAddParams(urlbase, name1, value1, name2, value2, name3, value3,
            name4, value4, name5, value5);
   }

   /**
    * Get an absolute url to the path provided
    * 
    * @param path a partial path that will be appended to the absolute url for Rhythmyx
    * @param secure if <code>true</code> a secure link is created
    * @return the link
    * @throws MalformedURLException
    */
   @IPSJexlMethod(description = "Get an absolute url to the path provided", params =
   {
         @IPSJexlParam(name = "path", description = "a partial path that will be appended to the absolute url for Rhythmyx"),
         @IPSJexlParam(name = "secure", type = "boolean", description = "if true a secure link may be generated")})
   public String getAbsUrl(String path, boolean secure)
         throws MalformedURLException
   {
      if (StringUtils.isBlank(path))
      {
         throw new IllegalArgumentException("path may not be null or empty");
      }
      PSRequest req = (PSRequest) PSRequestInfo
            .getRequestInfo(PSRequestInfo.KEY_PSREQUEST);
      PSRequestContext ctx = new PSRequestContext(req);
      return PSUrlUtils.createUrl(null, null, path, null, null, ctx, secure)
            .toExternalForm();
   }

   /**
    * Get a relative url to the path provided
    * 
    * @param path the partial path that will be appended to the internal url 
    * for Rhythmyx
    * @return the calculated url
    * @throws MalformedURLException
    */
   @IPSJexlMethod(description = "Get a relative url to the path provided", params =
   {@IPSJexlParam(name = "path", description = "a partial path that will be appended to the internal url for Rhythmyx")})
   public String getRelUrl(String path) throws MalformedURLException
   {
      if (StringUtils.isBlank(path))
      {
         throw new IllegalArgumentException("path may not be null or empty");
      }
      PSRequest req = (PSRequest) PSRequestInfo
            .getRequestInfo(PSRequestInfo.KEY_PSREQUEST);
      PSRequestContext ctx = new PSRequestContext(req);
      return PSUrlUtils.createUrl("127.0.0.1",
            new Integer(ctx.getServerListenerPort()), path, null, null, ctx,
            false).toExternalForm();
   }
}
