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

package com.percussion.tls;

import org.apache.commons.codec.binary.Base64;

import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;

public class TLSUtils {
    protected static String convertToPem(X509Certificate cert) throws CertificateEncodingException {

        String cert_begin = "-----BEGIN CERTIFICATE-----\n";
        String end_cert = "-----END CERTIFICATE-----";

        byte[] derCert = cert.getEncoded();
        String pemCertPre = new String( Base64.encodeBase64(derCert,true));
        String pemCert = cert_begin + pemCertPre + end_cert;
        return pemCert;
    }
}
