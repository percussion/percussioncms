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

package com.percussion.auditlog.util;

import org.apache.commons.lang3.time.FastDateFormat;

import java.io.File;
import java.util.Date;

public class FileCreator {

    public static String generateFile(String filePath, String fileName, String filePattern, String extension) {
        String finalFileName="";
        try {
    FastDateFormat simpleDateFormat = FastDateFormat.getInstance(filePattern);

    String formatted = simpleDateFormat.format(new Date());
    finalFileName=filePath+File.separator+fileName+"_"+formatted+"."+extension;
            File directory = new File(filePath);
            if(!directory.exists()){
                directory.mkdir();
            }
    File file = new File(finalFileName);
    file.createNewFile();
}catch (Exception e){
            finalFileName="";
           // throw new AuditException("Exception occurred in creating of File ",e);

}

return finalFileName;
    }

}
