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

package com.percussion.services.assembly.impl;

import com.percussion.server.IPSRequestContext;
import com.percussion.server.PSServer;

public class AssemblerInfoUtils {

    /**
     * Add the {@link #ASSEMBLY_LEVEL assembly level} parameter to the supplied
     * variant URL. Does not validate the URL. Just appends the parameter
     * (name=value) at the end of the URL string with an appropriate parameter
     * separator(? or &amp;). Does not check if the parameter already exists in
     * the URL. The new assembly parameter value is the current assembly level
     * value read from the request (see
     * {@link #readCurrentAssemblyLevel(IPSRequestContext)}) incremented by 1.
     *
     * @param request request context, must not be <code>null</code>.
     * @param assemblyUrlString the unencoded URL string to append the assembly
     * level parameter, may be <code>null</code> or empty.
     * @return the assembly URL value after appending the new assembly level as a
     * special parameter. May be <code>null</code> or empty if supplied URL
     * sting is <code>null</code> or empty.
     */
    public static String appendNewAssemblyLevelParam(IPSRequestContext request,
                                                     String assemblyUrlString)
    {
        if (request == null)
        {
            throw new IllegalArgumentException("request must nopt be null");
        }
        if (assemblyUrlString != null && assemblyUrlString.length() > 0)
        {
            char separator = '&';
            if (assemblyUrlString.indexOf('?') == -1)
            {
                separator = '?';
            }
            assemblyUrlString += separator + ASSEMBLY_LEVEL + "="
                    + (readCurrentAssemblyLevel(request) + 1);
        }
        return assemblyUrlString;
    }

    /**
     * Read and return the assembly depth by reading the special HTML parameter
     * {@link #ASSEMBLY_LEVEL} from the request context.
     *
     * @param request request context object, assumed not <code>null</code>
     * @return assembly depth which will be 0 (default) or higher.
     */
    public static int readCurrentAssemblyLevel(IPSRequestContext request)
    {
        String sDepth = request.getParameter(ASSEMBLY_LEVEL);
        sDepth = (sDepth == null || sDepth.length() == 0) ? "0" : sDepth;
        int depth = 0;
        try
        {
            depth = Integer.parseInt(sDepth);
        }
        catch(NumberFormatException e){}

        return depth;
    }

    /**
     * String constant for the special parameter name that holds the value of the
     * assmbly recursion depth. The value of this parameter is passed to child
     * snipptes after incrementing by one during assembly process.
     */
    public static final String ASSEMBLY_LEVEL = "sys_assemblylevel";

    /**
     * Return a correct HRef override based on user Preference in server.properties
     */

    public static String getBrokenLinkOverrideValue(String originalValue){
        String returnValue = null;
        String userPrefBrokenLinkBhvr = PSServer.getBrokenLinkBehavior();
        if(userPrefBrokenLinkBhvr == null){
            userPrefBrokenLinkBhvr = PSServer.BROKEN_MANAGED_LINK_BEHAVIOR_DEADLINK;
        }else{
            if(userPrefBrokenLinkBhvr.toLowerCase().equals(PSServer.BROKEN_MANAGED_LINK_BEHAVIOR_DEADLINK.toLowerCase())){
                returnValue = "#";
            }else if(userPrefBrokenLinkBhvr.toLowerCase().equals(PSServer.BROKEN_MANAGED_LINK_BEHAVIOR_REMOVELINK.toLowerCase())){
                returnValue = "";
            }else if(userPrefBrokenLinkBhvr.toLowerCase().equals(PSServer.BROKEN_MANAGED_LINK_BEHAVIOR_LEAVELINK.toLowerCase())){
                //don't do anything, as they want broken link as broken
                returnValue = originalValue;
            }
        }
        return returnValue;
    }
}
