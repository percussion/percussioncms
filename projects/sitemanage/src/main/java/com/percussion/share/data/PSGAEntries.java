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

package com.percussion.share.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author jyadav@google.com (Your Name Here)
 *
 */
@JsonRootName(value = "psmap")
public class PSGAEntries {
  PSGAEntry entries;

  public void setEntries(Map<String, String> dataMap)
  {
    /* below here simply converting the map data into following json format
    * {"psmap":{"entries":{"entry":[{"key":"122437851|UA-1500890-10","value":"Google Analytics View (Profile) All Web Site Data (UA-1500890-10)"},
    * {"key":"127433337|UA-1500890-11","value":"Google Analytics View (Profile) All Web Site Data"}]}}}
     * */

    PSGAPair p1 = null;
    List<PSGAPair> gaPairList = new ArrayList<>();
    for (Map.Entry<String, String> e : dataMap.entrySet()) {
       p1 = new PSGAPair(e.getKey(),e.getValue());
      gaPairList.add(p1);
    }
    PSGAEntry gaEntry = new PSGAEntry();
    gaEntry.setEntry(gaPairList);
    this.setEntries(gaEntry);
  }
  /**
   * @return the entries
   */
  public PSGAEntry getEntries() {
    return entries;
  }

  /**
   * @param entries the entries to set
   */
  private void setEntries(PSGAEntry entries) {
    this.entries = entries;
  }
  
}

class PSGAEntry{
  
  List<PSGAPair> entry;

  /**
   * @return the entry
   */
  public List<PSGAPair> getEntry() {
    return entry;
  }

  /**
   * @param entry the entry to set
   */
  public void setEntry(List<PSGAPair> entry) {
    this.entry = entry;
  }
  
}

class PSGAPair{
  String key;
  String value;
  
  /**
   * @param key
   * @param value
   */
  public PSGAPair(String key, String value) {
    super();
    this.key = key;
    this.value = value;
  }
  public PSGAPair() {
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
