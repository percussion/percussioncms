package com.percussion.soln.jcr.data;

import static java.util.Collections.singletonList;

import java.io.Serializable;
import java.util.List;

public class PropertyData implements Serializable {

    /**
     * Safe to serialize
     */
    private static final long serialVersionUID = 428038730570607863L;
    
    private List<ValueData> values;
    
    private boolean multiple;
    
    private String name;
    

    /**
     * Constructor for Serializers.
     */
    public PropertyData() { }
    
    /**
     * Constructor for Single value property.
     * @param data
     */
    public PropertyData(ValueData data) {
        if ((data) == null) throw new IllegalArgumentException("Value Data cannot be null");
        multiple = false;
        values = singletonList(data);
    }
    
    
    public boolean isMultiple() {
        return multiple;
    }

    public void setMultiple(boolean multiple) {
        this.multiple = multiple;
    }

    public List<ValueData> getValues() {
        return values;
    }

    public void setValues(List<ValueData> values) {
        this.values = values;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
