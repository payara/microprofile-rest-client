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

package org.eclipse.microprofile.rest.client.tck.cditests;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.net.URI;

import javax.json.JsonObject;

import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.eclipse.microprofile.rest.client.tck.ext.RequestScopedCdiCustomClientHeadersFactory;
import org.eclipse.microprofile.rest.client.tck.ext.RequestScopedCounter;
import org.eclipse.microprofile.rest.client.tck.ext.ClientHeadersFactoryState;
import org.eclipse.microprofile.rest.client.tck.interfaces.CdiClientHeadersFactoryClient;
import org.eclipse.microprofile.rest.client.tck.ext.CdiCustomClientHeadersFactory;
import org.eclipse.microprofile.rest.client.tck.ext.Counter;
import org.eclipse.microprofile.rest.client.tck.interfaces.RequestScopedCdiClientHeadersFactoryClient;
import org.eclipse.microprofile.rest.client.tck.providers.ReturnWithAllClientHeadersFilter;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.testng.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.testng.annotations.Test;

public class CDIClientHeadersFactoryTest extends Arquillian {
    @Deployment
    public static Archive<?> createDeployment() {
        return ShrinkWrap.create(WebArchive.class, CDIClientHeadersFactoryTest.class.getSimpleName()+".war")
                .addClasses(CdiClientHeadersFactoryClient.class,
                        CdiCustomClientHeadersFactory.class,
                        Counter.class,
                        ReturnWithAllClientHeadersFilter.class,
                        RequestScopedCdiClientHeadersFactoryClient.class,
                        RequestScopedCdiCustomClientHeadersFactory.class,
                        RequestScopedCounter.class,
                        ClientHeadersFactoryState.class)
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    private static CdiClientHeadersFactoryClient client(Class<?>... providers) {
        try {
            RestClientBuilder builder = RestClientBuilder.newBuilder().baseUri(URI.create("http://localhost:9080/notused"));
            for (Class<?> provider : providers) {
                builder.register(provider);
            }
            return builder.build(CdiClientHeadersFactoryClient.class);
        }
        catch (Throwable t) {
            t.printStackTrace();
            return null;
        }
    }

    private static RequestScopedCdiClientHeadersFactoryClient requestScopedClient(Class<?>... providers) {
        try {
            RestClientBuilder builder = RestClientBuilder.newBuilder().baseUri(URI.create("http://localhost:9080/notused"));
            for (Class<?> provider : providers) {
                builder.register(provider);
            }
            return builder.build(RequestScopedCdiClientHeadersFactoryClient.class);
        }
        catch (Throwable t) {
            t.printStackTrace();
            return null;
        }
    }

    @Test(priority = 1)
    public void testClientHeadersFactoryInvoked() {
        ClientHeadersFactoryState state = CdiCustomClientHeadersFactory.state.get();
        state.setIsIncomingHeadersMapNull(true);
        state.setIsOutgoingHeadersMapNull(true);
        state.getPassedInOutgoingHeaders().clear();

        JsonObject headers = client(ReturnWithAllClientHeadersFilter.class).delete("argValue");

        assertHeadersState(CdiCustomClientHeadersFactory.state.get(), headers);
        assertEquals(Counter.COUNT.get(), 1);
    }

    @Test(priority = 2)
    public void testApplicationScope() {
        JsonObject headers = client(ReturnWithAllClientHeadersFilter.class).delete("argValue");
        assertEquals(headers.getString("CDI_INJECT_COUNT"), "2");
        assertEquals(Counter.COUNT.get(), 2);
    }

    @Test(priority = 3)
    public void testRequestScopeClientHeadersFactoryInvoked() {
        ClientHeadersFactoryState state = RequestScopedCdiCustomClientHeadersFactory.state.get();
        state.setIsIncomingHeadersMapNull(true);
        state.setIsOutgoingHeadersMapNull(true);
        state.getPassedInOutgoingHeaders().clear();

        JsonObject headers = requestScopedClient(ReturnWithAllClientHeadersFilter.class).delete("argValue");

        assertHeadersState(RequestScopedCdiCustomClientHeadersFactory.state.get(), headers);
    }

    @Test(priority = 4)
    public void testRequestScope() {
        JsonObject headers = requestScopedClient(ReturnWithAllClientHeadersFilter.class).delete("argValue");
        assertEquals(headers.getString("CDI_INJECT_COUNT"), "1");
    }

    private void assertHeadersState(ClientHeadersFactoryState state, JsonObject headers) {
        assertTrue(state.isInvoked());
        assertFalse(state.isIncomingHeadersMapNull());
        assertFalse(state.isOutgoingHeadersMapNull());
        assertEquals(state.getPassedInOutgoingHeaders().getFirst("IntfHeader"), "intfValue");
        assertEquals(state.getPassedInOutgoingHeaders().getFirst("MethodHeader"), "methodValue");
        assertEquals(state.getPassedInOutgoingHeaders().getFirst("ArgHeader"), "argValue");

        assertEquals(headers.getString("IntfHeader"), "intfValueModified");
        assertEquals(headers.getString("MethodHeader"), "methodValueModified");
        assertEquals(headers.getString("ArgHeader"), "argValueModified");
        assertEquals(headers.getString("FactoryHeader"), "factoryValue");
        assertEquals(headers.getString("CDI_INJECT_COUNT"), "1");
    }
}
