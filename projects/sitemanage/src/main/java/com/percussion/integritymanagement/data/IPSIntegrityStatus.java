package com.percussion.integritymanagement.data;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;

public interface IPSIntegrityStatus extends Serializable {
    String getToken();

    void setToken(String token);

     Status getStatus();

     void setStatus(Status status);

     Date getStartTime();

     void setStartTime(Date startTime);

     Date getEndTime();

     void setEndTime(Date endTime);

     long getElapsedTime();

     Set<IPSIntegrityTask> getTasks();

     void setTasks(Set<IPSIntegrityTask> tasks);

    public static enum Status {
        RUNNING, SUCCESS, FAILED, CANCELLED;
    }
}
