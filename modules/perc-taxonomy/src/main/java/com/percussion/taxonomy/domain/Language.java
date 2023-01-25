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
