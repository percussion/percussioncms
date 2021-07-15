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
