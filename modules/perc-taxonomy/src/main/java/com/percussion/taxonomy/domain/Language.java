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

package com.percussion.taxonomy.domain;

public class Language {

   /**
    * Taxonomy Language class to represent the language in which taxonomy can be created.
    */
    public static final int DEFAUL_LANG = 1;
    
    private int id;
    private String name;
    private String abbreviation;
    private String script;

    /**
     * Returns unique id of a Language
     * @return id - unique int id of a Langauge
     */
    public int getId() {
        return id;
    }

    /**
     * Set id unique id of a Language
     * @param id - int id of a Language
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Returns name of a Language
     * @return name - String name of a Langauge
     */
    public String getName() {
        return name;
    }

    /**
     * Set Language name
     * @param name - String name of a Language
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Return Language abbreviation
     * @return abbreviation - String Language abbreviation
     */
    public String getAbbreviation() {
        return abbreviation;
    }

    /**
     * Set Language abbreviation
     * @param abbreviation - String abbreviation of a Language
     */
    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    /**
     * Returns Language script
     * @return script - Language script
     */
    public String getScript() {
        return script;
    }

    
    /**
     * Set script of the Language
     * @param script - String script of a Language
     */
    public void setScript(String script) {
        this.script = script;
    }
    
    /**
     * Returns a String representation of the object.
     */
    public String toString(){
        return "Language ----------------------\n"+
                "ID: "+id+"\n"+
                "Name: "+name+"\n"+
                "Abbreviation: "+abbreviation+"\n"+
                "Script: "+script;
    }
}
