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
package org.xmlsh.aws.gradle.identitymanagement;

import lombok.Getter;
import lombok.Setter;

import org.gradle.api.Project;
import org.xmlsh.aws.gradle.AwsPluginExtension;

import com.amazonaws.services.identitymanagement.AmazonIdentityManagement;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient;

public class AmazonIdentityManagementPluginExtension {

  public static final String NAME = "iam";

  @Getter
  @Setter
  private Project project;

  @Getter
  @Setter
  private String profileName;

  @Getter(lazy = true)
  private final AmazonIdentityManagement client = initClient();

  public AmazonIdentityManagementPluginExtension(Project project) {
    this.project = project;
  }

  private AmazonIdentityManagement initClient() {
    AwsPluginExtension aws = project.getExtensions().getByType(AwsPluginExtension.class);
    AmazonIdentityManagement client = aws.createClient(AmazonIdentityManagementClient.class, profileName);
    return client;
  }
}
