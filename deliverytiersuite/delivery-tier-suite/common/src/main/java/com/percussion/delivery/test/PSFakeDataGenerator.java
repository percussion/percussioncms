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
package com.percussion.delivery.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * This class is responsible for parsing test data
 * from the data files, or using in memory algorithms to generate
 * in memory lists of fake data to be used in unit tests.
 *
 * @author natechadwick
 */
public class PSFakeDataGenerator {

    public static final int Number = 0;
    public static final int Gender = 1;
    public static final int GivenName = 2;
    public static final int MiddleInitial = 3;
    public static final int Surname = 4;
    public static final int StreetAddress = 5;
    public static final int City = 6;
    public static final int State = 7;
    public static final int ZipCode = 8;
    public static final int Country = 9;
    public static final int EmailAddress = 10;
    public static final int Username = 11;
    public static final int Password = 12;
    public static final int TelephoneNumber = 13;
    public static final int MothersMaiden = 14;
    public static final int Birthday = 15;
    public static final int CCType = 16;
    public static final int CCNumber = 17;
    public static final int CVV2 = 18;
    public static final int CCExpires = 19;
    public static final int NationalID = 20;
    public static final int UPS = 21;
    public static final int Occupation = 22;
    public static final int Company = 23;
    public static final int Vehicle = 24;
    public static final int Domain = 25;
    public static final int BloodType = 26;
    public static final int Pounds = 27;
    public static final int Kilograms = 28;
    public static final int FeetInches = 29;
    public static final int Centimeters = 30;
    public static final int GUID = 31;
    public static final int Latitude = 32;
    public static final int Longitude = 33;

    /***
     * Will return up to count number of FakeRegistrant objects
     *
     * @param count The number of registrations to return, 0 for all available data.  Be careful as test datasets can be large.
     * @return A list of FakeRegistrants
     */
    public static List<FakeRegistrant> getFakeRegistrations(int count) {
        ArrayList<FakeRegistrant> ret = new ArrayList<FakeRegistrant>();

        BufferedReader br = new BufferedReader(new InputStreamReader(PSFakeDataGenerator.class.getResourceAsStream("/FakeData.csv")));
        StringTokenizer st = null;
        int lineNumber = 0, tokenNumber = 0;
        String line;

        try {
            while ((line = br.readLine()) != null) {

                //Bust out of hear if we have enough lines, if the passed in 0 we just get them all
                if (count > 0)
                    if (lineNumber > count)
                        break;

                lineNumber++;

                //Skip line 1 - it has fieldnames.
                if (lineNumber > 1) {

                    st = new StringTokenizer(line, ",");
                    FakeRegistrant data = new FakeRegistrant();
                    String token;

                    //Note this is pretty brute force - can be made more elegant
                    while (st.hasMoreTokens()) {

                        token = st.nextToken();

                        switch (tokenNumber) {
                            case Number:
                                data.setNumber(Integer.parseInt(token));
                                break;
                            case Gender:
                                data.setGender(token);
                                break;
                            case GivenName:
                                data.setGivenName(token);
                                break;
                            case MiddleInitial:
                                data.setMiddleInitial(token);
                                break;
                            case Surname:
                                data.setSurname(token);
                                break;
                            case StreetAddress:
                                data.setStreetAddress(token);
                                break;
                            case City:
                                data.setCity(token);
                                break;
                            case State:
                                data.setState(token);
                                break;
                            case ZipCode:
                                data.setZipCode(token);
                                break;
                            case Country:
                                data.setCountry(token);
                                break;
                            case EmailAddress:
                                data.setEmailAddress(token);
                                break;
                            case Username:
                                data.setUsername(token);
                                break;
                            case Password:
                                data.setPassword(token);
                                break;
                            case TelephoneNumber:
                                data.setTelephoneNumber(token);
                                break;
                            case MothersMaiden:
                                data.setMothersMaiden(token);
                                break;
                            case Birthday:
                                data.setBirthday(token);
                                break;
                            case CCType:
                                data.setCCType(token);
                                break;
                            case CCNumber:
                                data.setCCNumber(token);
                                break;
                            case CVV2:
                                data.setCVV2(token);
                                break;
                            case CCExpires:
                                data.setCCExpires(token);
                                break;
                            case NationalID:
                                data.setNationalID(token);
                                break;
                            case UPS:
                                data.setUPS(token);
                                break;
                            case Occupation:
                                data.setOccupation(token);
                                break;
                            case Company:
                                data.setCompany(token);
                                break;
                            case Vehicle:
                                data.setVehicle(token);
                                break;
                            case Domain:
                                data.setDomain(token);
                                break;
                            case BloodType:
                                data.setBloodType(token);
                                break;
                            case Pounds:
                                data.setPounds(token);
                                break;
                            case Kilograms:
                                data.setKilograms(token);
                                break;
                            case FeetInches:
                                data.setFeetInches(token);
                                break;
                            case Centimeters:
                                data.setCentimeters(token);
                                break;
                            case GUID:
                                data.setGUID(token);
                                break;
                            case Latitude:
                                data.setLatitude(token);
                                break;
                            case Longitude:
                                data.setLongitude(token);
                                break;
                            default:
                                break;
                        }
                        tokenNumber++;
                    }
                    ret.add(data);

                    //reset token number
                    tokenNumber = 0;

                }
            }

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
        }

        return ret;
    }
}
