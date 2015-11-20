/**
 * Copyright (c) 2015 Intel Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.trustedanalytics.servicebroker.h2oprovisioner.service.externals;

import com.google.common.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.trustedanalytics.servicebroker.h2oprovisioner.config.KerberosProperties;
import org.trustedanalytics.servicebroker.h2oprovisioner.service.externals.helpers.ExternalProcessExecutor;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class KinitExec {

    private static final Logger LOGGER = LoggerFactory.getLogger(KinitExec.class);

    private final KerberosProperties krb;

    public KinitExec(KerberosProperties krbProperties) throws IOException {
        this.krb = krbProperties;
        try {
            String krbConfTemplate =
                new String(Files.readAllBytes(Paths.get(krbProperties.getConfFile())));
            String krbConf = fillKrbConfTemplate(krbConfTemplate, krbProperties);
            saveKrbConfFile(krbProperties.getConfFile(), krbConf);
        } catch (IOException e) {
            LOGGER.error("Unable to read or modify kerberos config: '" + krb.getConfFile() + "'");
            throw e;
        }
    }

    @VisibleForTesting
    static String fillKrbConfTemplate(String krbConfTemplate, KerberosProperties krb) {
        return krbConfTemplate
            .replaceAll("<kdcPLACEHOLDER>", krb.getKdc())
            .replaceAll("<realmPLACEHOLDER>", krb.getRealm());
    }

    private static void saveKrbConfFile(String krbConfFile, String krbConf) throws IOException {
        FileWriter fileWriter = new FileWriter(krbConfFile);
        fileWriter.write(krbConf);
        fileWriter.flush();
        fileWriter.close();
    }

    public void loginToKerberos() throws Exception {
        System.out.println("Try to log in kerberos");
        String[] loginCmd = {
            "/bin/sh",
            "-c",
            "echo " + krb.getPassword() + " | kinit " + krb.getUser()
        };
        int kinitExitCode = ExternalProcessExecutor.runCommand(loginCmd);
        if (kinitExitCode != 0) {
            throw new Exception("kinit exited with code " + kinitExitCode);
        }
    }
}
