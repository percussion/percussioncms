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
