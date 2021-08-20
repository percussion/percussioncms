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
  @JsonIgnore
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
