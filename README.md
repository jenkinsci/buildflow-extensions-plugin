Jenkins Build Flow Concurrent Extensions Plugin
================================================

This plugin provides DSL extensions to the BuildFlow plugin to allow better orchestration of concurrent jobs.

[![Build Status](https://buildhive.cloudbees.com/job/jenkinsci/job/build-flow-concurrent-extension-plugin/badge/icon)]
(https://buildhive.cloudbees.com/job/jenkinsci/job/build-flow-concurrent-extension-plugin/)

## Sample Build Flow Content ##
    def conc = x.foobar()

     build("quickJob")
     block("block1") {
         build("slowerjob")
     }
     block("block2") {
         build("reallyLongJob")
     }
    )

See the documentation and release notes at [Build Flow Concurrent Extensions Plugin](https://wiki.jenkins-ci
.org/display/JENKINS/Build+Flow+Concurrent+Extensions+Plugin) on the Jenkins Wiki for more information.

Other informations:
* Bug Tracker for known issues and expectations : [Jenkins Build Flow Concurrent Extensions Component](https://issues
.jenkins-ci.org/browse/JENKINS/component/NNNN)
* Discussions on this plugin are hosted on  [jenkins-user mailing list](https://wiki.jenkins-ci.org/display/JENKINS/Mailing+Lists)


