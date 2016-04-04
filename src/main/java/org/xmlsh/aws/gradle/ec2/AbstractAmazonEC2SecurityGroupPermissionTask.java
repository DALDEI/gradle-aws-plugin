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
package org.xmlsh.aws.gradle.ec2;

import groovy.lang.GString;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.gradle.api.GradleException;
import org.gradle.api.internal.ConventionTask;

import com.amazonaws.services.ec2.model.IpPermission;

abstract class AbstractAmazonEC2SecurityGroupPermissionTask extends ConventionTask {

  protected Collection<IpPermission> parse(Object e) {
    if (e instanceof IpPermission) {
      return Collections.singleton((IpPermission) e);
    }
    Collection<?> elements;
    if (e instanceof Collection) {
      elements = (Collection<?>) e;
    }
    else {
      elements = Collections.singleton(e);
    }

    return elements.stream().map(it -> {
      if (it instanceof IpPermission) {
        return (IpPermission) it;
      }
      else if (it instanceof String || it instanceof GString) {
        // "tcp/22:10.0.0.2/32"
        // "tcp/1-65535:10.0.0.2/32"
        // "tcp/22:10.0.0.2/32,10.0.0.5/32"

        String expression = it.toString();
        if (expression.contains(":") == false) {
          throw new ParseException(expression);
        }

        String protocol;
        int fromPort;
        int toPort;

        String[] expressions = expression.split(":", 2);
        String protocolAndPortExpression = expressions[0];
        String rangeExpression = expressions[1];

        if ("icmp".equalsIgnoreCase(protocolAndPortExpression)) {
          protocol = protocolAndPortExpression;
          fromPort = toPort = -1;
        }
        else if (protocolAndPortExpression.contains("/") == false) {
          protocol = protocolAndPortExpression;
          fromPort = 0;
          toPort = 65535;
        }
        else {
          String[] protocolAndPortExpressions = protocolAndPortExpression.split("/", 2);
          protocol = protocolAndPortExpressions[0];
          String portExpression = protocolAndPortExpressions[1];
          if (portExpression.contains("-")) {
            String[] ports = portExpression.split("-", 2);
            fromPort = Integer.parseInt(ports[0]);
            toPort = Integer.parseInt(ports[1]);
          }
          else {
            fromPort = toPort = Integer.parseInt(portExpression);
          }
        }

        List<String> ranges = Arrays.asList(rangeExpression.split(","));

        return new IpPermission()
            .withIpProtocol(protocol)
            .withFromPort(fromPort)
            .withToPort(toPort)
            .withIpRanges(ranges);
      }
      else {
        throw new GradleException("ipPermission type only supports IpPermission or String: " + it.getClass());
      }
    }).collect(Collectors.toList());
  }
}
