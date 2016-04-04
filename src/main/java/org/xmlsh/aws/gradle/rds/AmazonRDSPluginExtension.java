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
package org.xmlsh.aws.gradle.rds;

import lombok.Getter;
import lombok.Setter;

import org.gradle.api.Project;
import org.xmlsh.aws.gradle.AwsPluginExtension;

import com.amazonaws.services.rds.AmazonRDS;
import com.amazonaws.services.rds.AmazonRDSClient;

public class AmazonRDSPluginExtension {

  public static final String NAME = "rds";

  @Getter
  @Setter
  private Project project;

  @Getter
  @Setter
  private String profileName;

  @Getter
  @Setter
  private String region;

  @Getter(lazy = true)
  private final AmazonRDS client = initClient();

  public AmazonRDSPluginExtension(Project project) {
    this.project = project;
  }

  private AmazonRDS initClient() {
    AwsPluginExtension aws = project.getExtensions().getByType(AwsPluginExtension.class);
    AmazonRDSClient client = aws.createClient(AmazonRDSClient.class, profileName);
    client.setRegion(aws.getActiveRegion(region));
    return client;
  }
}
