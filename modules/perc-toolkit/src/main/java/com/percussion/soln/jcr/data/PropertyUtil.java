package com.percussion.soln.jcr.data;

import static com.percussion.soln.jcr.data.ValueUtil.createValueData;

public class PropertyUtil {
    public static PropertyData createPropertyData(Object obj) {
        return new PropertyData(createValueData(obj));
    }
}
