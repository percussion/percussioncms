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
package com.percussion.analytics.data;

import com.fasterxml.jackson.annotation.JsonRootName;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Simple data class to represent the analytics provider config
 * @author erikserating
 *
 */
@JsonRootName(value = "providerConfig")
public class PSAnalyticsProviderConfig
{   

   /**
    * @param userid cannot be <code>null</code> or empty.
    * @param password cannot be <code>null</code> or empty.
    * @param isEncrypted flag indicating that the password is encrypted.
    * @param extraParamsMap The extra params used by the analytics provider to further define
    * access to the account. May be <code>null</code> or empty.
    */
   public PSAnalyticsProviderConfig(String userid, String password,
            boolean isEncrypted, Map<String, String> extraParamsMap)
   {
      if(StringUtils.isBlank(userid))
         throw new IllegalArgumentException("userid cannot be null or empty.");
      if(StringUtils.isBlank(password))
         throw new IllegalArgumentException("password cannot be null or empty.");
      this.userid = userid;
      this.password = password;
      this.isEncrypted = isEncrypted;
      this.extraParamsMap = extraParamsMap;
      this.uid=userid;

      //below is the code to create inner class objects based on the extraParamsMap map value.
       PSGAPairConfig p1 = null;
       List<PSGAPairConfig> PSGAPairList = new ArrayList<>();
       if(this.extraParamsMap !=null) {
           for (Map.Entry<String, String> e : extraParamsMap.entrySet()) {
               p1 = new PSGAPairConfig(e.getKey(), e.getValue());
               PSGAPairList.add(p1);
           }
       }
       ExtraParamsClass extraParamsClass = new ExtraParamsClass();
       extraParamsClass.setEntry(PSGAPairList);
       this.setExtraParams(extraParamsClass);

   }

   public PSAnalyticsProviderConfig()
   {

   }

   /**
    * @return the userid
    */
   public String getUserid()
   {
      return userid;
   }

   /**
    * @return the password
    */
   public String getPassword()
   {
      return password;
   }

   /**
    * @return the isEncrypted
    */
   public boolean isEncrypted()
   {
      return isEncrypted;
   }

   /**
    * @param userid the userid to set
    */
   public void setUserid(String userid)
   {
      this.userid = userid;
   }

   /**
    * @param password the password to set
    */
   public void setPassword(String password)
   {
      this.password = password;
   }

   /**
    * @param isEncrypted the isEncrypted to set
    */
   public void setEncrypted(boolean isEncrypted)
   {
      this.isEncrypted = isEncrypted;
   }


   /**
    * Analytics provider account user id. Never <code>null</code>
    * or empty.
    */
   private String userid;

    public Map<String, String> getParams() {
        return params;
    }

    public void setParams(Map<String, String> params) {
        this.params = params;
    }

    private Map<String, String> params;
    private String uid;


    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }


   
   /**
    * Analytics provider account password, may be 
    * encrypted, if so then <code>isEncrypted</code> will return
    * <code>true</code>. Never <code>null</code> or empty.
    */
   private String password;
   
   /**
    * Flag indicating if the password is encrypted.
    */
   private boolean isEncrypted;

    Map<String, String> extraParamsMap;

    public Map<String, String> getExtraParamsMap() {
        Map<String, String> map = new HashMap<>(); // create the map and return
            ExtraParamsClass extraParamsClass = this.getExtraParams();
            if(extraParamsClass !=null){
               List<PSGAPairConfig> dataList =  extraParamsClass.getEntry();
               if(dataList !=null && dataList.size()>0){
                   for (PSGAPairConfig t : dataList) {
                       map.put(t.getKey(),t.getValue());
                   }
               }
            }


        return map;
    }

    public void setExtraParamsMap(Map<String, String> extraParamsMap) {
        this.extraParamsMap = extraParamsMap;
    }

    /**
     * The extra params used by the analytics provider to further define
     * access to the account. May be <code>null</code> or empty.
     */
    private ExtraParamsClass extraParams;
    /**
     * @return the extraParams
     */
    public ExtraParamsClass getExtraParams() {
        return extraParams;
    }

    /**
     * @param extraParams to set
     */
    public void setExtraParams(ExtraParamsClass extraParams) {
        this.extraParams = extraParams;
    }
    private static final long serialVersionUID = 1L;

}

class ExtraParamsClass{

    List<PSGAPairConfig> entry;

    /**
     * @return the entry
     */
    public List<PSGAPairConfig> getEntry() {
        return entry;
    }

    /**
     * @param entry the entry to set
     */
    public void setEntry(List<PSGAPairConfig> entry) {
        this.entry = entry;
    }

}

class PSGAPairConfig{
    String key;
    String value;

    /**
     * @param key
     * @param value
     */
    public PSGAPairConfig(String key, String value) {
        super();
        this.key = key;
        this.value = value;
    }
    public PSGAPairConfig() {
        super();

    }
    /**
     * @return the key
     */
    public String getKey() {
        return key;
    }
    /**
     * @param key the key to set
     */
    public void setKey(String key) {
        this.key = key;
    }
    /**
     * @return the value
     */
    public String getValue() {
        return value;
    }
    /**
     * @param value the value to set
     */
    public void setValue(String value) {
        this.value = value;
    }

}
