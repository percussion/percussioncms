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

package com.percussion.delivery.integrations.ems.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.percussion.delivery.integrations.ems.IPSEMSEventService;

/**
 * <Data>
    <CategoryID>2147483647</CategoryID>
    <CategoryDescription>Room Charge</CategoryDescription>
    <UseCutOff>false</UseCutOff>
    <CutOffTime>1900-01-01T00:00:00</CutOffTime>
    <CutOffDays>0</CutOffDays>
    <CutOffHours>0</CutOffHours>
    <PONumberRequired>false</PONumberRequired>
    <BillingReferenceRequired>false</BillingReferenceRequired>
    <CurrencySymbol>US Dollars</CurrencySymbol>
    <UseStates>false</UseStates>
    <DefaultStateID>0</DefaultStateID>
  </Data>
 * @author natechadwick
 *
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Category {
	
	private static Log log = LogFactory.getLog(Category.class);
	private Integer categoryID;
	private String categoryDesc;
	private boolean useCutOff;
	private Date custoffTime;
	private Integer cutOffDays;
	private Integer cutOffHours;
	private boolean poNumberRequired;
	private boolean billingReferenceRequired;
	private String currencySymbol;
	private boolean useStates;
	private Integer defaultStateId;
	public Integer getCategoryID() {
		return categoryID;
	}
	public void setCategoryID(Integer categoryID) {
		this.categoryID = categoryID;
	}
	public String getCategoryDesc() {
		return categoryDesc;
	}
	public void setCategoryDesc(String categoryDesc) {
		this.categoryDesc = categoryDesc;
	}
	public boolean isUseCutOff() {
		return useCutOff;
	}
	public void setUseCutOff(boolean useCutOff) {
		this.useCutOff = useCutOff;
	}
	public Date getCustoffTime() {
		return custoffTime;
	}
	public void setCustoffTime(String custoffTime) {
		try {
			this.custoffTime = new SimpleDateFormat(IPSEMSEventService.TIME_FORMAT_STRING).parse(custoffTime.replace("T", " " ));
		} catch (ParseException e) {
			log.error("Error setting CustoffTime with value " + custoffTime + " and format: " + IPSEMSEventService.TIME_FORMAT_STRING,e);
		}
	}
	public Integer getCutOffDays() {
		return cutOffDays;
	}
	public void setCutOffDays(Integer cutOffDays) {
		this.cutOffDays = cutOffDays;
	}
	public Integer getCutOffHours() {
		return cutOffHours;
	}
	public void setCutOffHours(Integer cutOffHours) {
		this.cutOffHours = cutOffHours;
	}
	public boolean isPoNumberRequired() {
		return poNumberRequired;
	}
	public void setPoNumberRequired(boolean poNumberRequired) {
		this.poNumberRequired = poNumberRequired;
	}
	public boolean isBillingReferenceRequired() {
		return billingReferenceRequired;
	}
	public void setBillingReferenceRequired(boolean billingReferenceRequired) {
		this.billingReferenceRequired = billingReferenceRequired;
	}
	public String getCurrencySymbol() {
		return currencySymbol;
	}
	public void setCurrencySymbol(String currencySymbol) {
		this.currencySymbol = currencySymbol;
	}
	public boolean isUseStates() {
		return useStates;
	}
	public void setUseStates(boolean useStates) {
		this.useStates = useStates;
	}
	public Integer getDefaultStateId() {
		return defaultStateId;
	}
	public void setDefaultStateId(Integer defaultStateId) {
		this.defaultStateId = defaultStateId;
	}
	
	
	
	
}
