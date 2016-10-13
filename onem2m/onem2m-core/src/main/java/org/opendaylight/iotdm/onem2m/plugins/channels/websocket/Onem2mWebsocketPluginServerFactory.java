/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.iotdm.onem2m.plugins.channels.websocket;

import org.opendaylight.iotdm.onem2m.plugins.IotdmPluginConfigurationBuilder;
import org.opendaylight.iotdm.onem2m.plugins.Onem2mPluginManager;
import org.opendaylight.iotdm.onem2m.plugins.channels.Onem2mBaseCommunicationChannel;
import org.opendaylight.iotdm.onem2m.plugins.channels.Onem2mPluginChannelFactory;
import org.opendaylight.iotdm.onem2m.plugins.registry.Onem2mLocalEndpointRegistry;

import static org.opendaylight.iotdm.onem2m.plugins.channels.Onem2mBaseCommunicationChannel.CommunicationChannelType.SERVER;
import static org.opendaylight.iotdm.onem2m.plugins.channels.Onem2mBaseCommunicationChannel.TransportProtocol.TCP;

/**
 * Factory class implementing instantiation of Websocket HTTP servers.
 */
public class Onem2mWebsocketPluginServerFactory extends Onem2mPluginChannelFactory {

    public Onem2mWebsocketPluginServerFactory() {
        super(Onem2mPluginManager.ProtocolWebsocket, SERVER, TCP);
    }


    public Onem2mBaseCommunicationChannel createInstance(String ipAddress, int port, IotdmPluginConfigurationBuilder configBuilder,
                                                         Onem2mLocalEndpointRegistry registry) {
        if (null != configBuilder) {
            // configuration must be null
            return null;
        }

        Onem2mWebsocketPluginServer server = new Onem2mWebsocketPluginServer(ipAddress, port, registry);
        if (server.init()) {
            // return the new instance of the Websocket HTTP server
            return server;
        }

        return null;
    }
}
