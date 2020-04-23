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

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

public class ClientHeadersFactoryState {

    private MultivaluedMap<String, String> passedInOutgoingHeaders = new MultivaluedHashMap<>();
    private boolean isIncomingHeadersMapNull;
    private boolean isOutgoingHeadersMapNull;
    private boolean invoked;

    public boolean isIncomingHeadersMapNull() {
        return isIncomingHeadersMapNull;
    }

    public void setIsIncomingHeadersMapNull(boolean isIncomingHeadersMapNull) {
        this.isIncomingHeadersMapNull = isIncomingHeadersMapNull;
    }

    public boolean isOutgoingHeadersMapNull() {
        return isOutgoingHeadersMapNull;
    }

    public void setIsOutgoingHeadersMapNull(boolean isOutgoingHeadersMapNull) {
        this.isOutgoingHeadersMapNull = isOutgoingHeadersMapNull;
    }

    public boolean isInvoked() {
        return invoked;
    }

    public void setInvoked(boolean invoked) {
        this.invoked = invoked;
    }

    public MultivaluedMap<String, String> getPassedInOutgoingHeaders() {
        return passedInOutgoingHeaders;
    }
}
