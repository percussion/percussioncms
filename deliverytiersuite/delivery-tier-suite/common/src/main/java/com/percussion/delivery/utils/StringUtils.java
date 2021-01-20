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

package com.percussion.delivery.utils;

public class StringUtils {

    public static String joinURL(String firstPart, String secondPart){

        String ret = null;

            if(null != firstPart && firstPart != ""){
                if(firstPart.endsWith("/")){
                    firstPart = firstPart.substring(0,firstPart.length()-1);
                }
            }else{
                firstPart="";
            }

            if(null != secondPart && "" != secondPart){

                if(secondPart.startsWith("/")){
                    secondPart = secondPart.substring(1);
                }
            }else{
                secondPart="";
            }

            return firstPart+ "/" + secondPart;

    }

}
