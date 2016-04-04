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
import com.amazonaws.services.route53.model.Change;
import com.amazonaws.services.route53.model.ChangeAction;
import com.amazonaws.services.route53.model.ChangeBatch;
import com.amazonaws.services.route53.model.ChangeResourceRecordSetsRequest;
import com.amazonaws.services.route53.model.RRType;
import com.amazonaws.services.route53.model.ResourceRecord;
import com.amazonaws.services.route53.model.ResourceRecordSet;

public class AmazonRoute53ChangeRecordSetTask extends ConventionTask {

  @Getter
  @Setter
  private String hostedZoneId;

  @Getter
  @Setter
  private String rrsName;

  @Getter
  @Setter
  private String resourceRecord;

  public AmazonRoute53ChangeRecordSetTask() {
    setDescription("Create/Migrate Route53 Record.");
    setGroup("AWS");
  }

  @TaskAction
  public void changeResourceRecordSets() {
    // to enable conventionMappings feature
    String hostedZoneId = getHostedZoneId();
    String rrsName = getRrsName();
    String resourceRecord = getResourceRecord();

    AmazonRoute53PluginExtension ext = getProject().getExtensions().getByType(AmazonRoute53PluginExtension.class);
    AmazonRoute53 route53 = ext.getClient();

    route53.changeResourceRecordSets(new ChangeResourceRecordSetsRequest()
        .withHostedZoneId(hostedZoneId)
        .withChangeBatch(new ChangeBatch()
            .withChanges(new Change(ChangeAction.CREATE, new ResourceRecordSet(rrsName, RRType.CNAME)
                .withResourceRecords(new ResourceRecord(resourceRecord))))));
    getLogger().info("change {} requested", hostedZoneId);
  }
}
