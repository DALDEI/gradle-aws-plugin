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
import com.amazonaws.services.rds.model.DBInstanceNotFoundException;
import com.amazonaws.services.rds.model.DeleteDBInstanceRequest;

public class AmazonRDSDeleteDBInstanceTask extends ConventionTask {

  @Getter
  @Setter
  private String dbInstanceIdentifier;

  @Getter
  @Setter
  private boolean skipFinalSnapshot;

  @Getter
  @Setter
  private String finalDBSnapshotIdentifier;

  @Getter
  private DBInstance dbInstance;

  public AmazonRDSDeleteDBInstanceTask() {
    setDescription("Delete RDS instance.");
    setGroup("AWS");
  }

  @TaskAction
  public void deleteDBInstance() {
    // to enable conventionMappings feature
    String dbInstanceIdentifier = getDbInstanceIdentifier();

    if (dbInstanceIdentifier == null)
      throw new GradleException("dbInstanceIdentifier is required");

    AmazonRDSPluginExtension ext = getProject().getExtensions().getByType(AmazonRDSPluginExtension.class);
    AmazonRDS rds = ext.getClient();

    try {
      DeleteDBInstanceRequest request = new DeleteDBInstanceRequest()
          .withDBInstanceIdentifier(dbInstanceIdentifier)
          .withSkipFinalSnapshot(isSkipFinalSnapshot())
          .withFinalDBSnapshotIdentifier(getFinalDBSnapshotIdentifier());
      dbInstance = rds.deleteDBInstance(request);
      getLogger().info("Delete RDS instance requested: {}", dbInstance.getDBInstanceIdentifier());
    } catch (DBInstanceNotFoundException e) {
      getLogger().warn(e.getMessage());
    }
  }
}
