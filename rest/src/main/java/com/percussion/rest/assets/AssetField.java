package com.percussion.rest.assets;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "AssetField")
@Schema(description="Represents an Asset field")
public class AssetField {

    private String name;
    private String value;

    public AssetField(){
        //Default constructor
    }
    public AssetField(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
