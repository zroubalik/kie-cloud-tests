/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.kie.cloud.common.provider;

import java.time.Instant;

import org.kie.cloud.api.deployment.KieServerDeployment;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.KieContainerStatus;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.KieServicesConfiguration;
import org.kie.server.client.KieServicesFactory;
import org.kie.server.client.ProcessServicesClient;
import org.kie.server.client.QueryServicesClient;
import org.kie.server.client.UserTaskServicesClient;

public class KieServerClientProvider {

    private static final long KIE_SERVER_TIMEOUT = 300_000L;

    private KieServicesClient kieServerClient;

    public KieServerClientProvider(KieServerDeployment kieServerDeployment) {
        KieServicesConfiguration configuration = KieServicesFactory.newRestConfiguration(kieServerDeployment.getUrl().toString() + "/services/rest/server",
                kieServerDeployment.getUsername(), kieServerDeployment.getPassword(), KIE_SERVER_TIMEOUT);
        kieServerClient = KieServicesFactory.newKieServicesClient(configuration);
    }

    public KieServicesClient getKieServerClient() {
        return kieServerClient;
    }

    public ProcessServicesClient getProcessClient() {
        return kieServerClient.getServicesClient(ProcessServicesClient.class);
    }

    public UserTaskServicesClient getTaskClient() {
        return kieServerClient.getServicesClient(UserTaskServicesClient.class);
    }

    public QueryServicesClient getQueryClient() {
        return kieServerClient.getServicesClient(QueryServicesClient.class);
    }

    public void waitForContainerStart(String containerId) {
        Instant timeoutTime = Instant.now().plusSeconds(30);
        while (Instant.now().isBefore(timeoutTime)) {

            ServiceResponse<KieContainerResource> containerInfo = kieServerClient.getContainerInfo(containerId);
            boolean responseSuccess = containerInfo.getType().equals(ServiceResponse.ResponseType.SUCCESS);
            if(responseSuccess && containerInfo.getResult().getStatus().equals(KieContainerStatus.STARTED)) {
                return;
            }

            try {
                Thread.sleep(200L);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupted while waiting for pod to be ready.", e);
            }
        }
    }
}

