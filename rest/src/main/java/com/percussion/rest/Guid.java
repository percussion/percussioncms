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

package com.percussion.rest;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.percussion.services.guidmgr.data.PSGuid;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "Guid")
@JsonInclude(JsonInclude.Include.NON_NULL)
@ApiModel(value="Guid")
public class Guid {

    @ApiModelProperty(name="stringValue", value="String version of guid", notes="A String representation of the Guid.  Use this for storing the Guid for use in later API calls." )
    private String stringValue;
    @ApiModelProperty(name="untypedString",value="untypedString", notes= "Convert a numeric guid into a user readable form with no type integer.\nAppropriate for times where the type is implied.", readOnly = true)
    private
    String untypedString;
    @ApiModelProperty(name="hostId", value="hostId", notes= "Gets the host id, which indicates what customer installation created the\nobject this GUID references. Each customer should have a unique host id,\nwhich is an important part of keeping these identifiers globally unique.",readOnly = true)
    private
    long hostId;
    @ApiModelProperty(name="type",value="type",notes= "Return the type of the GUID, the interpretation of the type depends on\nthe context.\n",readOnly = true)
    private
    short type;
    @ApiModelProperty(name="uuid", value="uuid",notes="the uuid without host and type information", readOnly = true)
    private
    int uuid;
    @ApiModelProperty(name="longValue", value="longValue", notes= "Get the guid value in raw form. This is suitable for storage in \nserialized objects or in the database.\n<p>\n" +
            "If there is no hostid, then the GUID was constructed from an old id in \n" +
            "the database. For example, you have a template with a template id of 319. \n" +
            "When that becomes a GUID internally, it has the type added to it. If \n" +
            "longValue() (which is used when finding a template from the GUID) \n" +
            "doesn't strip everything but the UUID, the value won't match the value \n" +
            "in the database. On the other hand, if the GUID is a new GUID, then the \n" +
            "value in the database will be the complete guid, and it is appropriate \n" +
            "for longValue() to return guid.",readOnly = true)
    private
    long longValue;

    public String getUntypedString() {
        return untypedString;
    }

    public void setUntypedString(String untypedString) {
        this.untypedString = untypedString;
    }

    public long getHostId() {
        return hostId;
    }

    public void setHostId(long hostId) {
        this.hostId = hostId;
    }

    public short getType() {
        return type;
    }

    public void setType(short type) {
        this.type = type;
    }

    public int getUuid() {
        return uuid;
    }

    public void setUuid(int uuid) {
        this.uuid = uuid;
    }

    public long getLongValue() {
        return longValue;
    }

    public void setLongValue(long longValue) {
        this.longValue = longValue;
    }

    public String getStringValue() {
        return stringValue;
    }

    public void setStringValue(String stringValue) {
        this.stringValue = stringValue;
    }

    public Guid(){}

    /***
     * Initializes a Guid from a guid string
     * @param guid
     */
    public Guid(String guid){
        PSGuid temp = new PSGuid(guid);

        this.setStringValue(temp.toString());
        this.setHostId(temp.getHostId());
        this.setLongValue(temp.longValue());
        this.setType(temp.getType());
        this.setUuid(temp.getUUID());
        this.setUntypedString(temp.toStringUntyped());
    }
}
