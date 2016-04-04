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
package org.xmlsh.aws.gradle.elasticbeanstalk;

import groovy.lang.Closure;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Project;
import org.xmlsh.aws.gradle.AwsPluginExtension;

import com.amazonaws.services.elasticbeanstalk.AWSElasticBeanstalk;
import com.amazonaws.services.elasticbeanstalk.AWSElasticBeanstalkClient;
import com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentsRequest;
import com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentsResult;
import com.amazonaws.services.elasticbeanstalk.model.EnvironmentDescription;
import com.amazonaws.services.elasticbeanstalk.model.ListAvailableSolutionStacksResult;

public class AwsBeanstalkPluginExtension {

  public static final String NAME = "beanstalk";

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
  private final AWSElasticBeanstalk client = initClient();

  private AWSElasticBeanstalk initClient() {
    AwsPluginExtension aws = project.getExtensions().getByType(AwsPluginExtension.class);
    AWSElasticBeanstalkClient client = aws.createClient(AWSElasticBeanstalkClient.class, profileName);
    client.setRegion(aws.getActiveRegion(region));
    return client;
  }

  @Getter
  @Setter
  private String appName;

  @Getter
  @Setter
  private String appDesc = "";

  @Getter
  private EbAppVersionExtension version;

  public void version(Closure<?> closure) {
    closure.setResolveStrategy(Closure.DELEGATE_FIRST);
    closure.setDelegate(version);
    closure.call();
  }

  @Getter
  private NamedDomainObjectContainer<EbConfigurationTemplateExtension> configurationTemplates;

  public void configurationTemplates(Closure<?> closure) {
    configurationTemplates.configure(closure);
  }

  @Getter
  private EbEnvironmentExtension environment;

  public void environment(Closure<?> closure) {
    environment.configure(closure);
  }

  @Getter
  @Setter
  private Tier tier = Tier.WebServer;

  public AwsBeanstalkPluginExtension(Project project) {
    this.project = project;
    this.version = new EbAppVersionExtension();
    this.configurationTemplates = project.container(EbConfigurationTemplateExtension.class);
    this.environment = new EbEnvironmentExtension();
  }

  public String getEbEnvironmentCNAME(String environmentName) {
    DescribeEnvironmentsResult der = getClient().describeEnvironments(new DescribeEnvironmentsRequest()
        .withApplicationName(appName)
        .withEnvironmentNames(environmentName));
    EnvironmentDescription env = der.getEnvironments().get(0);
    return env.getCNAME();
  }

  public List<EnvironmentDescription> getEnvironmentDescs(List<String> environmentNames) {
    DescribeEnvironmentsRequest req = new DescribeEnvironmentsRequest().withApplicationName(appName);
    if (environmentNames.isEmpty() == false) {
      req.setEnvironmentNames(environmentNames);
    }
    DescribeEnvironmentsResult der = getClient().describeEnvironments(req);
    return der.getEnvironments();
  }

  public String getElbName(EnvironmentDescription env) {
    String elbName = env.getEndpointURL();
    elbName = elbName.substring(0, elbName.indexOf("."));
    elbName = elbName.substring(0, elbName.lastIndexOf("-"));
    return elbName;
  }

  public String latestSolutionStackName(String os, String platform) {
    ListAvailableSolutionStacksResult lassr = getClient().listAvailableSolutionStacks();
    return lassr.getSolutionStacks().stream()
        .filter(n -> n.startsWith(os) && n.contains(" running " + platform))
        .findFirst()
        .get();
  }
}
