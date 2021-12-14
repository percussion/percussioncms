/*
 *     Percussion CMS
 *     Copyright (C) 1999-2021 Percussion Software, Inc.
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
