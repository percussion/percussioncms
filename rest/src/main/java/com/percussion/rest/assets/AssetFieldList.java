package com.percussion.rest.assets;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.Collection;

@XmlRootElement(name = "AssetFieldList")
@XmlType(propOrder = {})
@ArraySchema(schema=@Schema(implementation = AssetField.class))
public class AssetFieldList extends ArrayList<AssetField> {
    public AssetFieldList(Collection<? extends AssetField> c) {
        super(c);
    }
    public AssetFieldList(){}
}
