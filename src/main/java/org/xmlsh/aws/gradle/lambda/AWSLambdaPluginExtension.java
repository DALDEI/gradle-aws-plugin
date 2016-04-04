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
package org.xmlsh.aws.gradle.lambda;

import lombok.Getter;
import lombok.Setter;

import org.gradle.api.Project;
import org.xmlsh.aws.gradle.AwsPluginExtension;

import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.AWSLambdaClient;

public class AWSLambdaPluginExtension {

  public static final String NAME = "lambda";

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
  private final AWSLambda client = initClient();

  public AWSLambdaPluginExtension(Project project) {
    this.project = project;
  }

  private AWSLambda initClient() {
    AwsPluginExtension aws = project.getExtensions().getByType(AwsPluginExtension.class);
    AWSLambda client = aws.createClient(AWSLambdaClient.class, profileName);
    client.setRegion(aws.getActiveRegion(region));
    return client;
  }
}
