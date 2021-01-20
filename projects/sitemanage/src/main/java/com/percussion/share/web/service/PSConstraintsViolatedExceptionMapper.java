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

package com.percussion.share.web.service;

import com.percussion.share.validation.PSValidationErrors;
import com.percussion.share.validation.PSValidationErrors.PSFieldError;
import com.percussion.util.PSSiteManageBean;
import net.sf.oval.ConstraintViolation;
import net.sf.oval.context.MethodParameterContext;
import net.sf.oval.exception.ConstraintsViolatedException;
import org.springframework.stereotype.Component;

import javax.ws.rs.PathParam;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.lang.annotation.Annotation;

@Provider
@PSSiteManageBean("constraintsViolatedExceptionMapper")
public class PSConstraintsViolatedExceptionMapper extends PSAbstractExceptionMapper<ConstraintsViolatedException> implements ExceptionMapper<ConstraintsViolatedException> {

    @Override
    protected PSValidationErrors createErrors(ConstraintsViolatedException exception) {
        PSValidationErrors ve = new PSValidationErrors();
        convert(ve, exception);
        return ve;
    }

    protected void convert(PSValidationErrors ve, ConstraintsViolatedException ce) {
        // TODO get the method name correctly through annotations or what not.
        ve.setMethodName(ce.getLocalizedMessage());
        ConstraintViolation[] violations = ce.getConstraintViolations();
        for (ConstraintViolation cv : violations) {
            ve.getFieldErrors().add(convert(cv));
        }

    }

    protected PSFieldError convert(ConstraintViolation cv) {
        PSFieldError oew = new PSFieldError();
        oew.setCode(cv.getErrorCode());
        oew.setDefaultMessage(cv.getMessage());
        if (cv.getContext() instanceof MethodParameterContext) {
            MethodParameterContext context = (MethodParameterContext) cv.getContext();
            Annotation[][] paramA = context.getMethod().getParameterAnnotations();
            Annotation[] anots = paramA[context.getParameterIndex()];
            for (Annotation anot : anots) {
                if (anot.annotationType() == PathParam.class) {
                    PathParam pp = (PathParam) anot;
                    oew.setField(pp.value());
                }
            }
        }
        oew.setRejectedValue(cv.getInvalidValue());
        return oew;
    }

}
