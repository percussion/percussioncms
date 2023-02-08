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

package com.percussion.services.purge.data;

public class RevisionData {

    private int keepMinNumberOfRevs;
    private int keepRevsYoungerThanDays;
    private int deleteRevsAboveCount;
    private int deleteRevsOlderThanDays;

    public RevisionData(int keepMinNumberOfRevs, int keepRevsYoungerThanDays,
                        int deleteRevsAboveCount, int deleteRevsOlderThanDays) {
        this.keepRevsYoungerThanDays = keepRevsYoungerThanDays;
        this.keepMinNumberOfRevs = keepMinNumberOfRevs;
        this.deleteRevsAboveCount = deleteRevsAboveCount;
        this.deleteRevsOlderThanDays = deleteRevsOlderThanDays;
    }

    /**
     * CAUTION: ignores transient and static fields!
     */
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof RevisionData)) return false;
        RevisionData obj = (RevisionData)o;

        // int field "keepMinNumberOfRevs"
        if (keepMinNumberOfRevs != obj.keepMinNumberOfRevs) {
            return false;
        }
        // int field "keepRevsYoungerThanDays"
        if (keepRevsYoungerThanDays != obj.keepRevsYoungerThanDays) {
            return false;
        }
        // int field "deleteRevsAboveCount"
        if (deleteRevsAboveCount != obj.deleteRevsAboveCount) {
            return false;
        }
        // int field "deleteRevsOlderThanDays"
        if (deleteRevsOlderThanDays != obj.deleteRevsOlderThanDays) {
            return false;
        }
        return true;
    }
    /**
     * CAUTION: ignores transient and static fields!
     */
    public int hashCode() {
        int code = 13;
        // int field "keepMinNumberOfRevs"
        code = 37*code + keepMinNumberOfRevs;

        // int field "keepRevsYoungerThanDays"
        code = 37*code + keepRevsYoungerThanDays;

        // int field "deleteRevsAboveCount"
        code = 37*code + deleteRevsAboveCount;

        // int field "deleteRevsOlderThanDays"
        code = 37*code + deleteRevsOlderThanDays;

        return code;
    }
    /**
     * int field "keepMinNumberOfRevs"
     */
    public int getKeepMinNumberOfRevs() {
        return keepMinNumberOfRevs;
    }
    /**
     * int field "keepRevsYoungerThanDays"
     */
    public int getKeepRevsYoungerThanDays() {
        return keepRevsYoungerThanDays;
    }
    /**
     * int field "deleteRevsAboveCount"
     */
    public int getDeleteRevsAboveCount() {
        return deleteRevsAboveCount;
    }
    /**
     * int field "deleteRevsOlderThanDays"
     */
    public int getDeleteRevsOlderThanDays() {
        return deleteRevsOlderThanDays;
    }
    /**
     * int field "keepMinNumberOfRevs"
     */
    void setKeepMinNumberOfRevs(int newValue) {
        keepMinNumberOfRevs = newValue;
    }
    /**
     * int field "keepRevsYoungerThanDays"
     */
    void setKeepRevsYoungerThanDays(int newValue) {
        keepRevsYoungerThanDays = newValue;
    }
    /**
     * int field "deleteRevsAboveCount"
     */
    void setDeleteRevsAboveCount(int newValue) {
        deleteRevsAboveCount = newValue;
    }
    /**
     * int field "deleteRevsOlderThanDays"
     */
    void setDeleteRevsOlderThanDays(int newValue) {
        deleteRevsOlderThanDays = newValue;
    }
    /**
     * Returns string representation of this object
     */
    public String toString() {
        StringBuilder sb = new StringBuilder(100);
        sb.append("keepMinNumberOfRevs=").append(keepMinNumberOfRevs).append("; ");
        sb.append("keepRevsYoungerThanDays=").append(keepRevsYoungerThanDays).append("; ");
        sb.append("deleteRevsAboveCount=").append(deleteRevsAboveCount).append("; ");
        sb.append("deleteRevsOlderThanDays=").append(deleteRevsOlderThanDays).append("; ");
        return sb.toString();
    }

}
