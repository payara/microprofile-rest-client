/*
 * Copyright 2020 Contributors to the Eclipse Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.eclipse.microprofile.rest.client.tck.ext;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

import org.eclipse.microprofile.rest.client.ext.ClientHeadersFactory;

import java.util.concurrent.atomic.AtomicReference;

@ApplicationScoped
public class CdiCustomClientHeadersFactory implements ClientHeadersFactory {

    public static AtomicReference<ClientHeadersFactoryState> state = new AtomicReference(new ClientHeadersFactoryState());

    @Inject
    private Counter counter;

    public MultivaluedMap<String, String> update(MultivaluedMap<String, String> incomingHeaders,
                                                 MultivaluedMap<String, String> clientOutgoingHeaders) {
        state.get().setInvoked(true);
        state.get().setIsIncomingHeadersMapNull(incomingHeaders == null);
        state.get().setIsOutgoingHeadersMapNull(clientOutgoingHeaders == null);

        if (state.get().getPassedInOutgoingHeaders() != null) {
            state.get().getPassedInOutgoingHeaders().putAll(clientOutgoingHeaders);
        }

        MultivaluedMap<String, String> returnVal = new MultivaluedHashMap<>();
        returnVal.putSingle("FactoryHeader", "factoryValue");
        clientOutgoingHeaders.forEach((k, v) -> {
            returnVal.putSingle(k, v.get(0) + "Modified"); });

        if (counter != null) {
            returnVal.putSingle("CDI_INJECT_COUNT", "" + counter.count());
        }
        return returnVal;
    }
}
