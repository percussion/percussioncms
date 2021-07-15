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
package com.percussion.services.aaclient;

import org.json.simple.ItemList;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author FangYidong<fangyidong@yahoo.com.cn>
 */
public class JSONObject extends LinkedHashMap{
    
    public String toString(){
        ItemList list=new ItemList();
        Iterator iter=entrySet().iterator();
        
        while(iter.hasNext()){
            Map.Entry entry=(Map.Entry)iter.next();
            list.add(toString(entry.getKey().toString(),entry.getValue()));
        }
        return "{"+list.toString()+"}";
    }
    
    public static String toString(String key,Object value){
        StringBuilder sb=new StringBuilder();
        
        sb.append("\"");
        sb.append(escape(key));
        sb.append("\":");
        if(value==null){
            sb.append("null");
            return sb.toString();
        }
        
        if(value instanceof String){
            sb.append("\"");
            sb.append(escape((String)value));
            sb.append("\"");
        }
        else
            sb.append(value);
        return sb.toString();
    }
    
    /**
     * " => \" , \ => \\
     * @param s
     * @return
     */
    public static String escape(String s){
        if(s==null)
            return null;
        StringBuilder sb=new StringBuilder();
        for(int i=0;i<s.length();i++){
            char ch=s.charAt(i);
            switch(ch){
            case '"':
                sb.append("\\\"");
                break;
            case '\\':
                sb.append("\\\\");
                break;
            case '\b':
                sb.append("\\b");
                break;
            case '\f':
                sb.append("\\f");
                break;
            case '\n':
                sb.append("\\n");
                break;
            case '\r':
                sb.append("\\r");
                break;
            case '\t':
                sb.append("\\t");
                break;
            case '/':
                sb.append("\\/");
                break;
            default:
                if(ch>='\u0000' && ch<='\u001F'){
                    String ss=Integer.toHexString(ch);
                    sb.append("\\u");
                    for(int k=0;k<4-ss.length();k++){
                        sb.append('0');
                    }
                    sb.append(ss.toUpperCase());
                }
                else{
                    sb.append(ch);
                }
            }
        }//for
        return sb.toString();
    }
}
