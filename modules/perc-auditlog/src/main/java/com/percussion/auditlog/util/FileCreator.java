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

package com.percussion.auditlog.util;

import com.percussion.auditlog.exception.AuditException;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FileCreator {

    public static String generateFile(String filePath, String fileName, String filePattern, String extension) {
        String finalFileName="";
        try {
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(filePattern);

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
