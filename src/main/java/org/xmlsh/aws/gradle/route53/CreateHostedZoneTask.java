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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

import org.gradle.api.internal.ConventionTask;
import org.gradle.api.tasks.TaskAction;

import com.amazonaws.services.route53.AmazonRoute53;
import com.amazonaws.services.route53.model.CreateHostedZoneRequest;
import com.amazonaws.services.route53.model.CreateHostedZoneResult;
import com.amazonaws.services.route53.model.HostedZoneAlreadyExistsException;
import com.amazonaws.services.route53.model.HostedZoneConfig;

public class CreateHostedZoneTask extends ConventionTask {

  @Getter
  @Setter
  private String hostedZoneName;

  @Getter
  @Setter
  private String callerReference;

  @Getter
  @Setter
  private String comment;

  // after did work

  @Getter
  private CreateHostedZoneResult createHostedZoneResult;

  @Getter
  private String hostedZoneId;

  @Getter
  private List<String> nameServers;

  @TaskAction
  public void createHostedZone() throws UnknownHostException {
    // to enable conventionMappings feature
    String hostedZoneName = getHostedZoneName();
    String callerReference = getCallerReference() != null ? getCallerReference() : InetAddress.getLocalHost()
        .getHostName();
    String comment = getComment();

    AmazonRoute53PluginExtension ext = getProject().getExtensions().getByType(AmazonRoute53PluginExtension.class);
    AmazonRoute53 route53 = ext.getClient();

    getLogger().info("callerRef = {}", callerReference);

    CreateHostedZoneRequest req = new CreateHostedZoneRequest()
        .withName(hostedZoneName)
        .withCallerReference(callerReference);
    if (comment != null) {
      req.setHostedZoneConfig(new HostedZoneConfig().withComment(comment));
    }

    try {
      createHostedZoneResult = route53.createHostedZone(req);
      nameServers = createHostedZoneResult.getDelegationSet().getNameServers();
      hostedZoneId = createHostedZoneResult.getHostedZone().getId();
      getLogger().info("HostedZone {} ({} - {})  is created.", hostedZoneId, hostedZoneName, callerReference);
      nameServers.forEach(it -> {
        getLogger().info("  NS {}", it);
      });
    } catch (HostedZoneAlreadyExistsException e) {
      getLogger().error("HostedZone {} - {} is already created.", hostedZoneName, callerReference);
    }
  }
}
