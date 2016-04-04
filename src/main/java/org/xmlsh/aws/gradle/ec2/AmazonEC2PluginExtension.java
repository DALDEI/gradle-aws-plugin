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
package org.xmlsh.aws.gradle.ec2;

import lombok.Getter;
import lombok.Setter;

import org.gradle.api.Project;
import org.xmlsh.aws.gradle.AwsPluginExtension;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;

public class AmazonEC2PluginExtension {

  public static final String NAME = "ec2";

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
  private final AmazonEC2 client = initClient();

  public AmazonEC2PluginExtension(Project project) {
    this.project = project;
  }

  private AmazonEC2 initClient() {
    AwsPluginExtension aws = project.getExtensions().getByType(AwsPluginExtension.class);
    AmazonEC2Client client = aws.createClient(AmazonEC2Client.class, profileName);
    client.setRegion(aws.getActiveRegion(region));
    return client;
  }
}
