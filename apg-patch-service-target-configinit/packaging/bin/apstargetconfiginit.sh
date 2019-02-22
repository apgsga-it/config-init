#!/usr/bin/env bash
java -Dspring.profiles.active=default -Dlogback.statusListenerClass=ch.qos.logback.core.status.NopStatusListener -jar /opt/apg-patch-target-configinit/bin/apg-patch-target-configinit.jar $@
#-DappPropertiesFile=file:/etc/opt/apg-patch-target-configinit/application.properties