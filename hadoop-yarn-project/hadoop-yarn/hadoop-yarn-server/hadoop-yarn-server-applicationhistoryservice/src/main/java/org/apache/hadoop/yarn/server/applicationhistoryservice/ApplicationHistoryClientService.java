/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.hadoop.yarn.server.applicationhistoryservice;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.classification.InterfaceAudience.Private;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.ipc.Server;
import org.apache.hadoop.service.AbstractService;
import org.apache.hadoop.yarn.api.ApplicationHistoryProtocol;
import org.apache.hadoop.yarn.api.protocolrecords.CancelDelegationTokenRequest;
import org.apache.hadoop.yarn.api.protocolrecords.CancelDelegationTokenResponse;
import org.apache.hadoop.yarn.api.protocolrecords.GetApplicationAttemptReportRequest;
import org.apache.hadoop.yarn.api.protocolrecords.GetApplicationAttemptReportResponse;
import org.apache.hadoop.yarn.api.protocolrecords.GetApplicationAttemptsRequest;
import org.apache.hadoop.yarn.api.protocolrecords.GetApplicationAttemptsResponse;
import org.apache.hadoop.yarn.api.protocolrecords.GetApplicationReportRequest;
import org.apache.hadoop.yarn.api.protocolrecords.GetApplicationReportResponse;
import org.apache.hadoop.yarn.api.protocolrecords.GetApplicationsRequest;
import org.apache.hadoop.yarn.api.protocolrecords.GetApplicationsResponse;
import org.apache.hadoop.yarn.api.protocolrecords.GetContainerReportRequest;
import org.apache.hadoop.yarn.api.protocolrecords.GetContainerReportResponse;
import org.apache.hadoop.yarn.api.protocolrecords.GetContainersRequest;
import org.apache.hadoop.yarn.api.protocolrecords.GetContainersResponse;
import org.apache.hadoop.yarn.api.protocolrecords.GetDelegationTokenRequest;
import org.apache.hadoop.yarn.api.protocolrecords.GetDelegationTokenResponse;
import org.apache.hadoop.yarn.api.protocolrecords.RenewDelegationTokenRequest;
import org.apache.hadoop.yarn.api.protocolrecords.RenewDelegationTokenResponse;
import org.apache.hadoop.yarn.api.records.ApplicationAttemptReport;
import org.apache.hadoop.yarn.api.records.ApplicationId;
import org.apache.hadoop.yarn.api.records.ApplicationReport;
import org.apache.hadoop.yarn.api.records.ContainerReport;
import org.apache.hadoop.yarn.conf.YarnConfiguration;
import org.apache.hadoop.yarn.exceptions.YarnException;
import org.apache.hadoop.yarn.ipc.YarnRPC;

public class ApplicationHistoryClientService extends AbstractService {
  private static final Log LOG = LogFactory
      .getLog(ApplicationHistoryClientService.class);
  private ApplicationHistoryManager history;
  private ApplicationHistoryProtocol protocolHandler;
  private Server server;
  private InetSocketAddress bindAddress;

  public ApplicationHistoryClientService(ApplicationHistoryManager history) {
    super("ApplicationHistoryClientService");
    this.history = history;
    this.protocolHandler = new ApplicationHSClientProtocolHandler();
  }

  protected void serviceStart() throws Exception {
    Configuration conf = getConfig();
    YarnRPC rpc = YarnRPC.create(conf);
    InetSocketAddress address = conf.getSocketAddr(
        YarnConfiguration.AHS_ADDRESS, YarnConfiguration.DEFAULT_AHS_ADDRESS,
        YarnConfiguration.DEFAULT_AHS_PORT);

    server = rpc.getServer(ApplicationHistoryProtocol.class, protocolHandler,
        address, conf, null, conf.getInt(
            YarnConfiguration.AHS_CLIENT_THREAD_COUNT,
            YarnConfiguration.DEFAULT_AHS_CLIENT_THREAD_COUNT));

    server.start();
    this.bindAddress = conf.updateConnectAddr(YarnConfiguration.AHS_ADDRESS,
        server.getListenerAddress());
    LOG.info("Instantiated ApplicationHistoryClientService at "
        + this.bindAddress);

    super.serviceStart();
  }

  @Override
  protected void serviceStop() throws Exception {
    if (server != null) {
      server.stop();
    }
    super.serviceStop();
  }

  @Private
  public ApplicationHistoryProtocol getClientHandler() {
    return this.protocolHandler;
  }

  @Private
  public InetSocketAddress getBindAddress() {
    return this.bindAddress;
  }

  private class ApplicationHSClientProtocolHandler implements
      ApplicationHistoryProtocol {

    @Override
    public CancelDelegationTokenResponse cancelDelegationToken(
        CancelDelegationTokenRequest request) throws YarnException, IOException {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public GetApplicationAttemptReportResponse getApplicationAttemptReport(
        GetApplicationAttemptReportRequest request) throws YarnException,
        IOException {
      GetApplicationAttemptReportResponse response = GetApplicationAttemptReportResponse
          .newInstance(history.getApplicationAttempt(request
              .getApplicationAttemptId()));
      return response;
    }

    @Override
    public GetApplicationAttemptsResponse getApplicationAttempts(
        GetApplicationAttemptsRequest request) throws YarnException,
        IOException {
      GetApplicationAttemptsResponse response = GetApplicationAttemptsResponse
          .newInstance(new ArrayList<ApplicationAttemptReport>(history
              .getApplicationAttempts(request.getApplicationId()).values()));
      return response;
    }

    @Override
    public GetApplicationReportResponse getApplicationReport(
        GetApplicationReportRequest request) throws YarnException, IOException {
      ApplicationId applicationId = request.getApplicationId();
      GetApplicationReportResponse response = GetApplicationReportResponse
          .newInstance(history.getApplication(applicationId));
      return response;
    }

    @Override
    public GetApplicationsResponse getApplications(
        GetApplicationsRequest request) throws YarnException, IOException {
      GetApplicationsResponse response = GetApplicationsResponse
          .newInstance(new ArrayList<ApplicationReport>(history
              .getAllApplications().values()));
      return response;
    }

    @Override
    public GetContainerReportResponse getContainerReport(
        GetContainerReportRequest request) throws YarnException, IOException {
      GetContainerReportResponse response = GetContainerReportResponse
          .newInstance(history.getContainer(request.getContainerId()));
      return response;
    }

    @Override
    public GetContainersResponse getContainers(GetContainersRequest request)
        throws YarnException, IOException {
      GetContainersResponse response = GetContainersResponse
          .newInstance(new ArrayList<ContainerReport>(history.getContainers(
              request.getApplicationAttemptId()).values()));
      return response;
    }

    @Override
    public GetDelegationTokenResponse getDelegationToken(
        GetDelegationTokenRequest request) throws YarnException, IOException {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public RenewDelegationTokenResponse renewDelegationToken(
        RenewDelegationTokenRequest request) throws YarnException, IOException {
      // TODO Auto-generated method stub
      return null;
    }
  }
}
