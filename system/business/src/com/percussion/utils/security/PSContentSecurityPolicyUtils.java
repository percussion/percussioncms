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

package com.percussion.utils.security;

import com.percussion.delivery.data.PSDeliveryInfo;

import java.util.List;

public class PSContentSecurityPolicyUtils {
    public static String editContentSecurityPolicy(List<PSDeliveryInfo> psDeliveryInfoList, String contentSecurityString ) {


        StringBuilder serverString=new StringBuilder();

        for(PSDeliveryInfo psDeliveryInfo : psDeliveryInfoList)
        {
            serverString.append(psDeliveryInfo.getUrl());
            serverString.append(" ");
            serverString.append(psDeliveryInfo.getUrl()).append("/*");
            serverString.append(" ");
        }
        if(contentSecurityString.contains("frame-src")) {

            contentSecurityString=contentSecurityString.replace("frame-src", "frame-src "+" "+serverString.toString());

        }else {
            if(contentSecurityString.endsWith(";")) {
                contentSecurityString=contentSecurityString+" frame-src 'self' "+" "+serverString+";";
            }else {
                contentSecurityString=contentSecurityString+"; frame-src 'self' "+" "+serverString+";";

            }

        }

        return contentSecurityString;
    }
}
