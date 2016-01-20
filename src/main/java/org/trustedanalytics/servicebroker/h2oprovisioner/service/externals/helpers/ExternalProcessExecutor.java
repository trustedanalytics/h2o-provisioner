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

package org.trustedanalytics.servicebroker.h2oprovisioner.service.externals.helpers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.stream.Collectors;

public class ExternalProcessExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExternalProcessExecutor.class);

    private ExternalProcessExecutor() {
    }

    public static int runCommand(String[] command) throws IOException {
        String lineToRun = Arrays.asList(command).stream().collect(Collectors.joining(" "));

        LOGGER.info("===================");
        LOGGER.info("Command to invoke:");
        LOGGER.info(lineToRun);
        LOGGER.info("===================");

        Process pr = Runtime.getRuntime().exec(command);
        BufferedReader in = new BufferedReader(new InputStreamReader(pr.getInputStream()));
        BufferedReader err = new BufferedReader(new InputStreamReader(pr.getErrorStream()));

        new Thread(getPrintOutputJob(in)).start();
        new Thread(getPrintErrorJob(err)).start();

        try {
            pr.waitFor();
        } catch (InterruptedException e) {
            LOGGER.info("Command '" + lineToRun + "' interrupted.", e);
        }

        int exitCode = pr.exitValue();
        LOGGER.info("===================");
        LOGGER.info("Exit value: " + exitCode);
        LOGGER.info("===================");
        return exitCode;
    }

    private static Runnable getPrintOutputJob(BufferedReader in) {
        return getPrintJob(in, "      ");
    }

    private static Runnable getPrintErrorJob(BufferedReader err) {
        return getPrintJob(err, "ERROR ");
    }

    private static Runnable getPrintJob(BufferedReader stream, String printPrefix) {
        return () -> {
            String line;
            try {
                try {
                    while ((line = stream.readLine()) != null) {
                        LOGGER.info(printPrefix + line);
                    }
                } catch (IOException e) {
                    LOGGER.info(printPrefix + "Error while reading process output.", e);
                } finally {
                    stream.close();
                }
            } catch (IOException e) {
                LOGGER.info(printPrefix + "Error while closing process output stream.", e);
            }
        };
    }
}
