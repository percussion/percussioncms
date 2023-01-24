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
