package com.percussion.integritymanagement.data;

import java.io.Serializable;
import java.util.Set;

public interface IPSIntegrityTask extends Serializable {
     long getTaskId();

     void setTaskId(long taskId);

     String getToken();

    void setToken(String token);

     String getName() ;

      void setName(String name);

      String getType();

     void setType(String type);

     TaskStatus getStatus();

     void setStatus(TaskStatus status);

     String getMessage();

     void setMessage(String message);

    Set<PSIntegrityTaskProperty> getTaskProperties();

   void setTaskProperties(Set<PSIntegrityTaskProperty> taskProperties);

    public static enum TaskStatus {
        SUCCESS, FAILED;
    }
}
