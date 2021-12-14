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
