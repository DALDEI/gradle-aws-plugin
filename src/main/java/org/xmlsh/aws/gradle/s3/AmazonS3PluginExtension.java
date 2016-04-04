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
package org.xmlsh.aws.gradle.s3;

import lombok.Getter;
import lombok.Setter;

import org.gradle.api.Project;
import org.xmlsh.aws.gradle.AwsPluginExtension;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.regions.RegionUtils;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;

public class AmazonS3PluginExtension {

  public static final String NAME = "s3";

  @Getter
  @Setter
  private Project project;

  @Getter
  @Setter
  private String profileName;

  @Getter
  @Setter
  private String region;

  @Getter
  @Setter
  private Integer maxErrorRetry = -1;

  @Getter(lazy = true)
  private final AmazonS3 client = initClient();

  public AmazonS3PluginExtension(Project project) {
    this.project = project;
  }

  private AmazonS3 initClient() {
    AwsPluginExtension aws = project.getExtensions().getByType(AwsPluginExtension.class);

    ClientConfiguration clientConfiguration = new ClientConfiguration();
    if (maxErrorRetry > 0)
      clientConfiguration.setMaxErrorRetry(maxErrorRetry);

    AmazonS3Client client = aws.createClient(AmazonS3Client.class, profileName, clientConfiguration);
    if (region != null) {
      client.setRegion(RegionUtils.getRegion(region));
    }
    return client;
  }
}
