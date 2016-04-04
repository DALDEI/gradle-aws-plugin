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
package org.xmlsh.aws.gradle.elasticloadbalancing;

import lombok.Getter;
import lombok.Setter;

import org.gradle.api.Project;
import org.xmlsh.aws.gradle.AwsPluginExtension;

import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancing;
import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancingClient;

public class AmazonElasticLoadBalancingPluginExtension {

  public static final String NAME = "elb";

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
  private final AmazonElasticLoadBalancing client = initClient();

  public AmazonElasticLoadBalancingPluginExtension(Project project) {
    this.project = project;
  }

  private AmazonElasticLoadBalancing initClient() {
    AwsPluginExtension aws = project.getExtensions().getByType(AwsPluginExtension.class);
    AmazonElasticLoadBalancingClient client = aws.createClient(AmazonElasticLoadBalancingClient.class, profileName);
    client.setRegion(aws.getActiveRegion(region));
    return client;
  }

}
