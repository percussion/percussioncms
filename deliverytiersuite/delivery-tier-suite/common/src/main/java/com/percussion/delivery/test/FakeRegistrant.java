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
package com.percussion.delivery.test;

/**
 * Provides a model for holding a Fake Registrant.  Provides
 * a large set of data fileds that tests can choose from. This data
 * was sourced from a fake data generation service with the fields
 * contained on this class. 
 * 
 * There are several convenience methods for converting the FakeRegistrant
 * into various CM1 specific entities. 
 * 
 * @author natechadwick
 *
 */
public class FakeRegistrant {

	private int number;
	private String gender;
	private String givenName;
	private String middleInitial;
	private String surname;
	private String streetAddress;
	private String city;
	private String state;
	private String zipCode;
	private String country;
	private String emailAddress;
	private String username;
	private String password;
	private String telephoneNumber;
	private String mothersMaiden;
	private String birthday;
	private String CCType;
	private String CCNumber;
	private String CVV2;
	private String CCExpires;
	private String nationalID;
	private String UPS;
	private String occupation;
	private String company;
	private String vehicle;
	private String domain;
	private String bloodType;
	private String pounds;
	private String kilograms;
	private String feetInches;
	private String centimeters;
	private String GUID;
	private String latitude;
	private String longitude;
	/**
	 * @return the number
	 */
	public int getNumber() {
		return number;
	}
	/**
	 * @param number the number to set
	 */
	public void setNumber(int number) {
		this.number = number;
	}
	/**
	 * @return the gender
	 */
	public String getGender() {
		return gender;
	}
	/**
	 * @param gender the gender to set
	 */
	public void setGender(String gender) {
		this.gender = gender;
	}
	/**
	 * @return the givenName
	 */
	public String getGivenName() {
		return givenName;
	}
	/**
	 * @param givenName the givenName to set
	 */
	public void setGivenName(String givenName) {
		this.givenName = givenName;
	}
	/**
	 * @return the middleInitial
	 */
	public String getMiddleInitial() {
		return middleInitial;
	}
	/**
	 * @param middleInitial the middleInitial to set
	 */
	public void setMiddleInitial(String middleInitial) {
		this.middleInitial = middleInitial;
	}
	/**
	 * @return the surname
	 */
	public String getSurname() {
		return surname;
	}
	/**
	 * @param surname the surname to set
	 */
	public void setSurname(String surname) {
		this.surname = surname;
	}
	/**
	 * @return the streetAddress
	 */
	public String getStreetAddress() {
		return streetAddress;
	}
	/**
	 * @param streetAddress the streetAddress to set
	 */
	public void setStreetAddress(String streetAddress) {
		this.streetAddress = streetAddress;
	}
	/**
	 * @return the city
	 */
	public String getCity() {
		return city;
	}
	/**
	 * @param city the city to set
	 */
	public void setCity(String city) {
		this.city = city;
	}
	/**
	 * @return the state
	 */
	public String getState() {
		return state;
	}
	/**
	 * @param state the state to set
	 */
	public void setState(String state) {
		this.state = state;
	}
	/**
	 * @return the zipCode
	 */
	public String getZipCode() {
		return zipCode;
	}
	/**
	 * @param zipCode the zipCode to set
	 */
	public void setZipCode(String zipCode) {
		this.zipCode = zipCode;
	}
	/**
	 * @return the country
	 */
	public String getCountry() {
		return country;
	}
	/**
	 * @param country the country to set
	 */
	public void setCountry(String country) {
		this.country = country;
	}
	/**
	 * @return the emailAddress
	 */
	public String getEmailAddress() {
		return emailAddress;
	}
	/**
	 * @param emailAddress the emailAddress to set
	 */
	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}
	/**
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}
	/**
	 * @param username the username to set
	 */
	public void setUsername(String username) {
		this.username = username;
	}
	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}
	/**
	 * @param password the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}
	/**
	 * @return the telephoneNumber
	 */
	public String getTelephoneNumber() {
		return telephoneNumber;
	}
	/**
	 * @param telephoneNumber the telephoneNumber to set
	 */
	public void setTelephoneNumber(String telephoneNumber) {
		this.telephoneNumber = telephoneNumber;
	}
	/**
	 * @return the mothersMaiden
	 */
	public String getMothersMaiden() {
		return mothersMaiden;
	}
	/**
	 * @param mothersMaiden the mothersMaiden to set
	 */
	public void setMothersMaiden(String mothersMaiden) {
		this.mothersMaiden = mothersMaiden;
	}
	/**
	 * @return the birthday
	 */
	public String getBirthday() {
		return birthday;
	}
	/**
	 * @param birthday the birthday to set
	 */
	public void setBirthday(String birthday) {
		this.birthday = birthday;
	}

	/**
	 * @return the cCType
	 */
	public String getCCType() {
		return CCType;
	}
	/**
	 * @param cCType the cCType to set
	 */
	public void setCCType(String cCType) {
		CCType = cCType;
	}
	/**
	 * @return the cCNumber
	 */
	public String getCCNumber() {
		return CCNumber;
	}
	/**
	 * @param cCNumber the cCNumber to set
	 */
	public void setCCNumber(String cCNumber) {
		CCNumber = cCNumber;
	}
	/**
	 * @return the cVV2
	 */
	public String getCVV2() {
		return CVV2;
	}
	/**
	 * @param cVV2 the cVV2 to set
	 */
	public void setCVV2(String cVV2) {
		CVV2 = cVV2;
	}
	/**
	 * @return the cCExpires
	 */
	public String getCCExpires() {
		return CCExpires;
	}
	/**
	 * @param cCExpires the cCExpires to set
	 */
	public void setCCExpires(String cCExpires) {
		CCExpires = cCExpires;
	}
	/**
	 * @return the nationalID
	 */
	public String getNationalID() {
		return nationalID;
	}
	/**
	 * @param nationalID the nationalID to set
	 */
	public void setNationalID(String nationalID) {
		this.nationalID = nationalID;
	}
	/**
	 * @return the uPS
	 */
	public String getUPS() {
		return UPS;
	}
	/**
	 * @param uPS the uPS to set
	 */
	public void setUPS(String uPS) {
		UPS = uPS;
	}
	/**
	 * @return the occupation
	 */
	public String getOccupation() {
		return occupation;
	}
	/**
	 * @param occupation the occupation to set
	 */
	public void setOccupation(String occupation) {
		this.occupation = occupation;
	}
	/**
	 * @return the company
	 */
	public String getCompany() {
		return company;
	}
	/**
	 * @param company the company to set
	 */
	public void setCompany(String company) {
		this.company = company;
	}
	/**
	 * @return the vehicle
	 */
	public String getVehicle() {
		return vehicle;
	}
	/**
	 * @param vehicle the vehicle to set
	 */
	public void setVehicle(String vehicle) {
		this.vehicle = vehicle;
	}
	/**
	 * @return the domain
	 */
	public String getDomain() {
		return domain;
	}
	/**
	 * @param domain the domain to set
	 */
	public void setDomain(String domain) {
		this.domain = domain;
	}
	/**
	 * @return the bloodType
	 */
	public String getBloodType() {
		return bloodType;
	}
	/**
	 * @param bloodType the bloodType to set
	 */
	public void setBloodType(String bloodType) {
		this.bloodType = bloodType;
	}
	/**
	 * @return the pounds
	 */
	public String getPounds() {
		return pounds;
	}
	/**
	 * @param pounds the pounds to set
	 */
	public void setPounds(String pounds) {
		this.pounds = pounds;
	}
	/**
	 * @return the kilograms
	 */
	public String getKilograms() {
		return kilograms;
	}
	/**
	 * @param kilograms the kilograms to set
	 */
	public void setKilograms(String kilograms) {
		this.kilograms = kilograms;
	}
	/**
	 * @return the feetInches
	 */
	public String getFeetInches() {
		return feetInches;
	}
	/**
	 * @param feetInches the feetInches to set
	 */
	public void setFeetInches(String feetInches) {
		this.feetInches = feetInches;
	}
	/**
	 * @return the centimeters
	 */
	public String getCentimeters() {
		return centimeters;
	}
	/**
	 * @param centimeters the centimeters to set
	 */
	public void setCentimeters(String centimeters) {
		this.centimeters = centimeters;
	}
	/**
	 * @return the latitude
	 */
	public String getLatitude() {
		return latitude;
	}
	/**
	 * @param latitude the latitude to set
	 */
	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}
	/**
	 * @return the gUID
	 */
	public String getGUID() {
		return GUID;
	}
	/**
	 * @param gUID the gUID to set
	 */
	public void setGUID(String gUID) {
		GUID = gUID;
	}
	/**
	 * @return the longitude
	 */
	public String getLongitude() {
		return longitude;
	}
	/**
	 * @param longitude the longitude to set
	 */
	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}
	
	
	
	
}
