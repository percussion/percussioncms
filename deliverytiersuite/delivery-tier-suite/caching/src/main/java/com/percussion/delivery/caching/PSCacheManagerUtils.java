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
package com.percussion.delivery.caching;

import com.percussion.delivery.caching.data.PSInvalidateRequest;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;

/**
 * @author erikserating
 *
 */
public class PSCacheManagerUtils
{
   //Private ctor to prevent class instantiation.
   private PSCacheManagerUtils(){}
   
   /**
    * Splits an invalidate request by paths, using specified maximum path integer.
    * @param request the request to be split, cannot be <code>null</code>.
    * @param max the maximum number of paths per request.
    * @return list of split requests. Never <code>null</code> and never empty.
    */
   public static List<PSInvalidateRequest> splitRequest(PSInvalidateRequest request, int max)
   {
       if(request == null)
           throw new IllegalArgumentException("request cannot be null.");
       List<PSInvalidateRequest> results = new ArrayList<>();
       int themax = Math.max(max, 1);
       List<String> paths = request.getPaths();
       if(!paths.isEmpty() && paths.size() > max)
       {
           List<List<String>> pathChunks = Lists.partition(paths, themax);
           for(List<String> chunk : pathChunks)
           {
               PSInvalidateRequest newReq = (PSInvalidateRequest)request.clone();
               newReq.setPaths(chunk);
               results.add(newReq);
           }
       }
       else
       {
           results.add(request);
       }
       return results;
   }
}
