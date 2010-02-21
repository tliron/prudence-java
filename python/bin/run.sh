#!/bin/bash

here=$(readlink -f "$(dirname "$0")")
cd $here

java=/usr/bin/java
#java=/usr/libraries/jvm/java-1.5.0-sun/bin/java

main=com.threecrickets.scripturian.Scripturian

jars=\
libraries/com.mysql.jdbc.jar:\
libraries/com.sun.grizzly.jar:\
libraries/com.sun.script.velocity.jar:\
libraries/com.threecrickets.jygments.jar;\
libraries/com.threecrickets.prudence.jar:\
libraries/com.threecrickets.scripturian.jar:\
libraries/javax.script.jar:\
libraries/jep.jar:\
libraries/jython.jar:\
libraries/jython-engine.jar:\
libraries/org.apache.log4j.jar:\
libraries/org.apache.velocity.jar:\
libraries/org.codehaus.jackson.jar:\
libraries/org.codehaus.jackson.mapper.jar:\
libraries/org.json.jar:\
libraries/org.restlet.ext.grizzly.jar:\
libraries/org.restlet.ext.jackson.jar:\
libraries/org.restlet.ext.json.jar:\
libraries/org.restlet.ext.slf4j.jar:\
libraries/org.restlet.jar:\
libraries/org.slf4j.bridge.jar:\
libraries/org.slf4j.impl.jar:\
libraries/org.slf4j.jar

# We are setting java.library.path for Jepp

"$java" -cp "$jars" -Djava.library.path=/usr/local/lib -Dpython.home=libraries/python -Dpython.cachedir=cache -Dpython.verbose=warning $main instance
