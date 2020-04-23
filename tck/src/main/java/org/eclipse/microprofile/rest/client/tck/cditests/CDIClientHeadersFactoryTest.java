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

    @Test
    public void testClientHeadersFactoryInvoked() {
        CdiCustomClientHeadersFactory.state.get().setIsIncomingHeadersMapNull(true);
        CdiCustomClientHeadersFactory.state.get().setIsOutgoingHeadersMapNull(true);
        CdiCustomClientHeadersFactory.state.get().getPassedInOutgoingHeaders().clear();

        JsonObject headers = client(ReturnWithAllClientHeadersFilter.class).delete("argValue");

        assertTrue(CdiCustomClientHeadersFactory.state.get().isInvoked());
        assertFalse(CdiCustomClientHeadersFactory.state.get().isIncomingHeadersMapNull());
        assertFalse(CdiCustomClientHeadersFactory.state.get().isOutgoingHeadersMapNull());
        assertEquals(CdiCustomClientHeadersFactory.state.get().getPassedInOutgoingHeaders().getFirst("IntfHeader"), "intfValue");
        assertEquals(CdiCustomClientHeadersFactory.state.get().getPassedInOutgoingHeaders().getFirst("MethodHeader"), "methodValue");
        assertEquals(CdiCustomClientHeadersFactory.state.get().getPassedInOutgoingHeaders().getFirst("ArgHeader"), "argValue");

        assertEquals(headers.getString("IntfHeader"), "intfValueModified");
        assertEquals(headers.getString("MethodHeader"), "methodValueModified");
        assertEquals(headers.getString("ArgHeader"), "argValueModified");
        assertEquals(headers.getString("FactoryHeader"), "factoryValue");
        assertEquals(headers.getString("CDI_INJECT_COUNT"), "1");
        assertEquals(Counter.COUNT.get(), 1);
    }

    @Test(dependsOnMethods = "testClientHeadersFactoryInvoked")
    public void testApplicationScope() {
        JsonObject headers = client(ReturnWithAllClientHeadersFilter.class).delete("argValue");
        assertEquals(headers.getString("CDI_INJECT_COUNT"), "2");
        assertEquals(Counter.COUNT.get(), 2);
    }

    @Test
    public void testRequestScopeClientHeadersFactoryInvoked() {
        RequestScopedCdiCustomClientHeadersFactory.state.get().setIsIncomingHeadersMapNull(true);
        RequestScopedCdiCustomClientHeadersFactory.state.get().setIsOutgoingHeadersMapNull(true);
        RequestScopedCdiCustomClientHeadersFactory.state.get().getPassedInOutgoingHeaders().clear();

        JsonObject headers = requestScopedClient(ReturnWithAllClientHeadersFilter.class).delete("argValue");

        assertTrue(RequestScopedCdiCustomClientHeadersFactory.state.get().isInvoked());
        assertFalse(RequestScopedCdiCustomClientHeadersFactory.state.get().isIncomingHeadersMapNull());
        assertFalse(RequestScopedCdiCustomClientHeadersFactory.state.get().isOutgoingHeadersMapNull());
        assertEquals(RequestScopedCdiCustomClientHeadersFactory.state.get().getPassedInOutgoingHeaders().getFirst("IntfHeader"), "intfValue");
        assertEquals(RequestScopedCdiCustomClientHeadersFactory.state.get().getPassedInOutgoingHeaders().getFirst("MethodHeader"), "methodValue");
        assertEquals(RequestScopedCdiCustomClientHeadersFactory.state.get().getPassedInOutgoingHeaders().getFirst("ArgHeader"), "argValue");

        assertEquals(headers.getString("IntfHeader"), "intfValueModified");
        assertEquals(headers.getString("MethodHeader"), "methodValueModified");
        assertEquals(headers.getString("ArgHeader"), "argValueModified");
        assertEquals(headers.getString("FactoryHeader"), "factoryValue");
        assertEquals(headers.getString("CDI_INJECT_COUNT"), "1");
    }

    @Test(dependsOnMethods = "testRequestScopeClientHeadersFactoryInvoked")
    public void testRequestScope() {
        JsonObject headers = requestScopedClient(ReturnWithAllClientHeadersFilter.class).delete("argValue");
        assertEquals(headers.getString("CDI_INJECT_COUNT"), "1");
    }
}
