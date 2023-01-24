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
package com.percussion.pagemanagement.assembler;

/**
 * This is the rendering result of a something in a region
 * which can be one of two things: <p>
 * {@link PSRegionResultType#WIDGET} or {@link PSRegionResultType#SUBREGION}.
 * <p>
 * <em>Note: The rendering results of an entire region are a list of these objects.</em>
 * @author adamgent
 *
 */
public class PSRegionResult {
    
    private String result;
    private PSRegionResult.PSRegionResultType type = PSRegionResultType.WIDGET;
    private boolean publishMode;
    
    /**
     * @see #getWidget()
     */
    private PSWidgetInstance widget;
    /**
     * maybe <code>null</code>.
     */
    private Throwable errorCause;
    

    /**
     * If this part of the region failed to render because an exception was thrown
     * the exception can be retrieved here.
     * @return maybe <code>null</code> if no exception thrown.
     */
    public Throwable getErrorCause()
    {
        return errorCause;
    }


    public void setErrorCause(Throwable errorCause,boolean publishMode)
    {
        this.errorCause = errorCause;
        this.publishMode = publishMode;
    }


    /**
     * @return maybe <code>null</code> if {@link #getType()} is {@link PSRegionResultType#SUBREGION}.
     */
    public PSWidgetInstance getWidget()
    {
        return widget;
    }


    public void setWidget(PSWidgetInstance widget)
    {
        this.widget = widget;
    }


    public String getResult()
    {
        return result;
    }


    public void setResult(String result)
    {
        this.result = result;
    }


    public PSRegionResult.PSRegionResultType getType()
    {
        return type;
    }


    public void setType(PSRegionResult.PSRegionResultType type)
    {
        this.type = type;
    }


    /**
     * Dictates whether or not this region result
     * is a widget rendering or a subregion.
     * @author adamgent
     *
     */
    public enum PSRegionResultType {
        WIDGET,SUBREGION
    }


    /**
     * Overrided for ease of use in velocity.
     * @return rendered result.
     */
    @Override
    public String toString()
    {
        if (getErrorCause() != null) {
            if(!publishMode) {
                return "Error Displaying Contents. See logs for more details";
            }
            else {
                return "";
            }
        }
        return result;
    }
    
    
}
