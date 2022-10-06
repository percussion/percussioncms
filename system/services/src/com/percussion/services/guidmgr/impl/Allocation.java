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

package com.percussion.services.guidmgr.impl;

import java.util.function.BiFunction;


/**
 * A thread safe class that allocates numbers in a range. When the range is
 * exceeded an exception is thrown. Essentially this is a numeric iterator
 *
 * @author dougrand
 */
class Allocation
{

    private BiFunction<Integer,Long,Long> nextBlockFunction;



    /**
     * The next id for the allocation
     */
    private volatile long mi_nextId;

    /**
     * The last available id for the allocation
     */
    private volatile long mi_last;

    private int blockSize;

    public Allocation(int blockSize, BiFunction<Integer,Long,Long> nextBlockFunction)
    {
        this.blockSize = blockSize;
        this.nextBlockFunction = nextBlockFunction;
    }

    public synchronized void setIds(long nextId, long last) {
            // set next id first. allocation will unblock when last is greater than
            // next
            this.mi_nextId = nextId;
            this.mi_last = last;
    }

    /**
     * Ctor
     *
     * @param first the initial value in the range
     * @param last  the last value in the range - this value will not be
     *              returned by
     */
    public Allocation(long first, long last) {
            this.mi_nextId = first;
            this.mi_last = last;

    }

    /**
     * Obtain the next id for the given allocation and update the state
     *
     * @return the next id
     */
    public synchronized long next() {
            getNewBlockIfNeeded();
            return mi_nextId++;
    }

    // Expects to be within a readlock and will relock it.
    private synchronized void getNewBlockIfNeeded() {
        if (mi_last <= 0 || mi_nextId > mi_last) {
            // nextid = 10 last = 19 with blocksize=10  first and last inclusive
            this.mi_nextId = nextBlockFunction.apply(this.blockSize, -1l);
            this.mi_last = this.mi_nextId + blockSize - 1;
        }
    }

    public synchronized long peek() {
            getNewBlockIfNeeded();
            return mi_nextId;
    }


    public synchronized  int fix(long value) {
        getNewBlockIfNeeded();
        long origResult = this.mi_nextId;
        // max value may be smaller than allocated values so increase if this is the case.
        if (mi_nextId>0 && value < mi_nextId)
            value=mi_nextId;

        this.mi_nextId = nextBlockFunction.apply(this.blockSize, value);
        this.mi_last = this.mi_nextId + blockSize - 1;


        return (int) origResult;
    }

}
