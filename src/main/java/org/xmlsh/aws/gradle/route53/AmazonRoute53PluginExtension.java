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
package org.xmlsh.aws.gradle.route53;

import lombok.Getter;
import lombok.Setter;

import org.gradle.api.Project;
import org.xmlsh.aws.gradle.AwsPluginExtension;

import com.amazonaws.services.route53.AmazonRoute53;
import com.amazonaws.services.route53.AmazonRoute53Client;

public class AmazonRoute53PluginExtension {

  public static final String NAME = "route53";

  @Getter
  @Setter
  private Project project;

  @Getter
  @Setter
  private String profileName;

  @Getter(lazy = true)
  private final AmazonRoute53 client = initClient();

  public AmazonRoute53PluginExtension(Project project) {
    this.project = project;
  }

  private AmazonRoute53 initClient() {
    AwsPluginExtension aws = project.getExtensions().getByType(AwsPluginExtension.class);
    return aws.createClient(AmazonRoute53Client.class, profileName);
  }
}
