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

import org.gradle.api.internal.ConventionTask;
import org.gradle.api.tasks.TaskAction;

import com.amazonaws.services.route53.AmazonRoute53;
import com.amazonaws.services.route53.model.DeleteHostedZoneRequest;

public class DeleteHostedZoneTask extends ConventionTask {

  @Getter
  @Setter
  private String hostedZoneId;

  @TaskAction
  public void createHostedZone() {
    // to enable conventionMappings feature
    String hostedZoneId = getHostedZoneId();

    AmazonRoute53PluginExtension ext = getProject().getExtensions().getByType(AmazonRoute53PluginExtension.class);
    AmazonRoute53 route53 = ext.getClient();

    route53.deleteHostedZone(new DeleteHostedZoneRequest(hostedZoneId));
    getLogger().info("HostedZone {} is deleted successfully.", hostedZoneId);
  }
}
