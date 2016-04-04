/*
 * Copyright 2015-2016 David A. Lee. <dlee@calldei.com>
 *
 * This is a derived work from Classmethods, Inc.
 *
 * Current License Terms - Dual licensed under the following :
 *
 * "Simplified BSD License" included in license.txt
 *
 * The original work (derived from) license terms:  Apache License, Version 2.0
 * included in copyright.origin/LICENSE.TXT
 */
/*
 * TOTALLY BOGUS
 * Stupid
 */
package org.xmlsh.aws.gradle;

import java.lang.reflect.Constructor;

import lombok.Getter;
import lombok.Setter;

import org.gradle.api.Project;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.AmazonWebServiceClient;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSCredentialsProviderChain;
import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.auth.SystemPropertiesCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.RegionUtils;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagement;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient;
import com.amazonaws.services.identitymanagement.model.GetUserResult;
import com.google.common.base.Strings;

public class AwsPluginExtension {

  public static final String NAME = "aws";

  private static final AWSCredentialsProvider EMPTY = new AWSCredentialsProvider() {

    public void refresh() {
    }

    public AWSCredentials getCredentials() {
      return null;
    }
  };

  @Getter
  @Setter
  private Project project;

  @Getter
  @Setter
  private String profileName = "default";

  @Getter
  @Setter
  private String region = Regions.US_EAST_1.getName();

  @Setter
  private String proxyHost;

  @Setter
  private int proxyPort = -1;

  public AwsPluginExtension(Project project) {
    this.project = project;
  }

  public AWSCredentialsProvider newCredentialsProvider(String profileName) {
    return new AWSCredentialsProviderChain(
        new EnvironmentVariableCredentialsProvider(),
        new SystemPropertiesCredentialsProvider(),
        Strings.isNullOrEmpty(profileName) == false ? new ProfileCredentialsProvider(profileName) : EMPTY,
        new ProfileCredentialsProvider(this.profileName),
        new InstanceProfileCredentialsProvider());
  }

  public <T extends AmazonWebServiceClient> T createClient(Class<T> serviceClass, String profileName) {
    return createClient(serviceClass, profileName, null);
  }

  public <T extends AmazonWebServiceClient> T createClient(Class<T> serviceClass, String profileName,
      ClientConfiguration config) {
    if (profileName == null) {
      if (this.profileName == null) {
        throw new IllegalStateException("default profileName is null");
      }
      profileName = this.profileName;
    }

    AWSCredentialsProvider credentialsProvider = newCredentialsProvider(profileName);
    if (this.proxyHost != null && this.proxyPort > 0) {
      config.setProxyHost(this.proxyHost);
      config.setProxyPort(this.proxyPort);
    }
    return createClient(serviceClass, credentialsProvider, config);
  }

  private static <T extends AmazonWebServiceClient> T createClient(Class<T> serviceClass,
      AWSCredentialsProvider credentials, ClientConfiguration config) {
    Constructor<T> constructor;
    T client;
    try {
      if (credentials == null && config == null) {
        constructor = serviceClass.getConstructor();
        client = constructor.newInstance();
      }
      else if (credentials == null) {
        constructor = serviceClass.getConstructor(ClientConfiguration.class);
        client = constructor.newInstance(config);
      }
      else if (config == null) {
        constructor = serviceClass.getConstructor(AWSCredentialsProvider.class);
        client = constructor.newInstance(credentials);
      }
      else {
        constructor = serviceClass.getConstructor(AWSCredentialsProvider.class, ClientConfiguration.class);
        client = constructor.newInstance(credentials, config);
      }

      return client;
    } catch (Exception e) {
      throw new RuntimeException("Couldn't instantiate instance of " + serviceClass, e);
    }
  }

  public Region getActiveRegion(String clientRegion) {
    if (clientRegion != null) {
      return RegionUtils.getRegion(clientRegion);
    }
    if (this.region == null) {
      throw new IllegalStateException("default region is null");
    }
    return RegionUtils.getRegion(region);
  }

  public String getActiveProfileName(String clientProfileName) {
    if (clientProfileName != null) {
      return clientProfileName;
    }
    if (this.profileName == null) {
      throw new IllegalStateException("default profileName is null");
    }
    return profileName;
  }

  public String getAccountId() {
    String arn = getUserArn(); // ex. arn:aws:iam::123456789012:user/division_abc/subdivision_xyz/Bob
    return arn.split(":")[4];
  }

  public String getUserArn() {
    AmazonIdentityManagement iam = createClient(AmazonIdentityManagementClient.class, profileName);
    try {
      GetUserResult getUserResult = iam.getUser();
      return getUserResult.getUser().getArn();
    } catch (AmazonServiceException e) {
      if (e.getErrorCode().equals("AccessDenied") == false) {
        throw e;
      }
      String msg = e.getMessage();
      int arnIdx = msg.indexOf("arn:aws");
      if (arnIdx == -1) {
        throw e;
      }
      int arnSpace = msg.indexOf(" ", arnIdx);
      return msg.substring(arnIdx, arnSpace);
    }
  }
}
