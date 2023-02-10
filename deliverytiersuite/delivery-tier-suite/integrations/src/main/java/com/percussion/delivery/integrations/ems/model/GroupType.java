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

package com.percussion.delivery.integrations.ems.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/*
 * <GroupTypes>
  <Data>
    <Description>(none)</Description>
    <ID>0</ID>
    <AvailableOnWeb>true</AvailableOnWeb>
  </Data>
  <Data>
    <Description>Staff</Description>
    <ID>60</ID>
    <AvailableOnWeb>true</AvailableOnWeb>
  </Data>
  <Data>
    <Description>Campus Department</Description>
    <ID>61</ID>
    <AvailableOnWeb>true</AvailableOnWeb>
  </Data>
  <Data>
    <Description>Student Organization</Description>
    <ID>62</ID>
    <AvailableOnWeb>true</AvailableOnWeb>
  </Data>
  <Data>
    <Description>Off Campus</Description>
    <ID>63</ID>
    <AvailableOnWeb>true</AvailableOnWeb>
  </Data>
  <Data>
    <Description>On Campus</Description>
    <ID>64</ID>
    <AvailableOnWeb>true</AvailableOnWeb>
  </Data>
  <Data>
    <Description>Student Union</Description>
    <ID>65</ID>
    <AvailableOnWeb>true</AvailableOnWeb>
  </Data>
  <Data>
    <Description>Greek Student Organization</Description>
    <ID>66</ID>
    <AvailableOnWeb>true</AvailableOnWeb>
  </Data>
  <Data>
    <Description>Individual - Off Campus</Description>
    <ID>67</ID>
    <AvailableOnWeb>false</AvailableOnWeb>
  </Data>
  <Data>
    <Description>Individual - CSUDH</Description>
    <ID>68</ID>
    <AvailableOnWeb>false</AvailableOnWeb>
  </Data>
  <Data>
    <Description>Foundation</Description>
    <ID>69</ID>
    <AvailableOnWeb>false</AvailableOnWeb>
  </Data>
</GroupTypes>

 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class GroupType {
	
	private int id;
	private String description;
	private boolean availableOnWeb;

	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	public boolean isAvailableOnWeb() {
		return availableOnWeb;
	}
	
	public void setAvailableOnWeb(boolean availableOnWeb) {
		this.availableOnWeb = availableOnWeb;
	}
	

}
