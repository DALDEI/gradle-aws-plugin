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

import org.gradle.api.GradleException;
import org.gradle.api.internal.ConventionTask;
import org.gradle.api.tasks.TaskAction;

import com.amazonaws.services.rds.AmazonRDS;
import com.amazonaws.services.rds.model.DBInstance;
import com.amazonaws.services.rds.model.RebootDBInstanceRequest;

public class AmazonRDSRebootDBInstanceTask extends ConventionTask {

  @Getter
  @Setter
  private String dbInstanceIdentifier;

  @Getter
  @Setter
  private Boolean forceFailover;

  @Getter
  private DBInstance dbInstance;

  public AmazonRDSRebootDBInstanceTask() {
    setDescription("Reboot RDS instance.");
    setGroup("AWS");
  }

  @TaskAction
  public void rebootDBInstance() {
    // to enable conventionMappings feature
    String dbInstanceIdentifier = getDbInstanceIdentifier();

    if (dbInstanceIdentifier == null)
      throw new GradleException("dbInstanceIdentifier is required");

    AmazonRDSPluginExtension ext = getProject().getExtensions().getByType(AmazonRDSPluginExtension.class);
    AmazonRDS rds = ext.getClient();

    RebootDBInstanceRequest request = new RebootDBInstanceRequest()
        .withDBInstanceIdentifier(dbInstanceIdentifier)
        .withForceFailover(getForceFailover());
    dbInstance = rds.rebootDBInstance(request);
    getLogger().info("Reboot RDS instance requested: {}", dbInstance.getDBInstanceIdentifier());
  }
}
