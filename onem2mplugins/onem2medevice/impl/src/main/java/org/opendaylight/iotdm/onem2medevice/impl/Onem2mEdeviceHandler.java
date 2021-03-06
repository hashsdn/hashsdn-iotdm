/*
 * Copyright © 2016 Cisco Systems, Inc and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.iotdm.onem2medevice.impl;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.iotdm.onem2m.core.database.transactionCore.ResourceTreeReader;
import org.opendaylight.iotdm.onem2m.core.database.transactionCore.ResourceTreeWriter;
import org.opendaylight.iotdm.onem2m.plugins.*;
import org.opendaylight.iotdm.onem2m.plugins.channels.coap.IotdmPluginCoapRequest;
import org.opendaylight.iotdm.onem2m.plugins.channels.http.IotdmPluginHttpRequest;
import org.opendaylight.iotdm.onem2medevice.impl.protocols.Onem2mCoapHandler;
import org.opendaylight.iotdm.onem2medevice.impl.protocols.Onem2mHttpHandler;
import org.opendaylight.iotdm.onem2medevice.impl.utils.Onem2mXmlUtils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.iotdm.onem2m.rev150105.Onem2mService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.iotdm.onem2m.rev150105.onem2m.resource.tree.Onem2mResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;

/**
 * @author jkosmel
 */
class Onem2mEdeviceHandler implements IotdmPlugin, IotdmPluginDbClient {

    private static final Logger LOG = LoggerFactory.getLogger(Onem2mEdeviceHandler.class);
    protected final DataBroker dataBroker;
    private Onem2mHttpHandler httpHandler;
    private Onem2mCoapHandler coapHandler;
    private Onem2mDataStoreChangeHandler onem2mDataStoreChangeHandler = null;

    Onem2mEdeviceHandler(DataBroker dataBroker, Onem2mService onem2mService) {
        this.dataBroker = dataBroker;
        httpHandler = new Onem2mHttpHandler(onem2mService);
        coapHandler = new Onem2mCoapHandler(onem2mService);

        try {
            Onem2mPluginManager.getInstance().registerDbClientPlugin(this);
        } catch (IotdmPluginRegistrationException e) {
            LOG.error("Failed to register plugin as DB API client: {}", e);
        }
    }

    @Override
    public void dbClientStart() {
        onem2mDataStoreChangeHandler = new Onem2mDataStoreChangeHandler(dataBroker);
        try {
            Onem2mPluginManager.getInstance()
                .registerPluginHttp(this, 8284, Onem2mPluginManager.Mode.Exclusive, null)
                .registerPluginCoap(this, 123, Onem2mPluginManager.Mode.Exclusive, null);
        } catch (IotdmPluginRegistrationException e) {
            LOG.error("Failed to register at PluginManager plugin: {}", e);
        }
    }

    @Override
    public void dbClientStop() {
        onem2mDataStoreChangeHandler = null;
        Onem2mPluginManager.getInstance().unregisterIotdmPlugin(this);
    }

    @Override
    public void close() throws Exception {
        this.dbClientStop();
        Onem2mPluginManager.getInstance().unregisterDbClientPlugin(this);
    }

    private class Onem2mDataStoreChangeHandler extends Onem2mDatastoreListener {

        Onem2mDataStoreChangeHandler(DataBroker dataBroker) {
            super(dataBroker);
        }

        @Override
        public void onem2mResourceCreated(String hierarchicalResourceName, Onem2mResource onem2mResource) {
            LOG.info("Onem2mExampleCustomProtocol: onem2mResourceCreated h={}, id:{}, type:{}",
                    hierarchicalResourceName,
                    onem2mResource.getResourceId(),
                    onem2mResource.getResourceType());

            // do custom protocol notification (maybe)
        }

        @Override
        public void onem2mResourceChanged(String hierarchicalResourceName, Onem2mResource onem2mResource) {
            LOG.info("Onem2mExampleCustomProtocol: onem2mResourceChanged h={}, id:{}, type:{}",
                    hierarchicalResourceName,
                    onem2mResource.getResourceId(),
                    onem2mResource.getResourceType());

            // do custom protocol notification (maybe)

        }

        @Override
        public void onem2mResourceDeleted(String hierarchicalResourceName, Onem2mResource onem2mResource) {
            LOG.info("Onem2mExampleCustomProtocol: onem2mResourceDeleted h={}, id:{}, type:{}",
                    hierarchicalResourceName,
                    onem2mResource.getResourceId(),
                    onem2mResource.getResourceType());

            // do custom protocol notification (maybe)

        }
    }

    @Override
    public String getPluginName() {
        return "edevice";
    }

    @Override
    public void handle(IotdmPluginRequest request, IotdmPluginResponse response) {
        String contentType;
        String cn;
        boolean isHttp = false;
        if(request instanceof IotdmPluginHttpRequest) { //http
            HttpServletRequest httpRequest = ((IotdmPluginHttpRequest) request).getOriginalRequest();
            contentType = httpRequest.getContentType();
            isHttp = true;
        }
        else if(request instanceof IotdmPluginCoapRequest){ //coap
            contentType = request.getContentType();
        }
        else { //should not occure
            throw new IllegalArgumentException("Only Http and Coap are supported for edevice");
        }

        cn = request.getPayLoad();
        contentType = contentType == null ? "json":contentType.toLowerCase();
        if (cn != null && !cn.contentEquals("") && contentType.contains("xml")) { //convert payload to xml and change content-type
            request.setPayLoad(Onem2mXmlUtils.xmlRequestToJson(cn));
            request.setContentType(contentType.replace("xml","json"));
        }
        else {
            request.setContentType(contentType);
        }

        if(isHttp)
            httpHandler.handle(request,response); //handle http
        else
            coapHandler.handle(request, response); //handle coap

        LOG.info("Onem2mEdeviceHandler: method: {}, url:{}, headers: {}, payload: {}",
                request.getMethod(), request.getUrl(), request.getPayLoad());
    }
}
