<a href="https://opensource.newrelic.com/oss-category/#new-relic-experimental"><picture><source media="(prefers-color-scheme: dark)" srcset="https://github.com/newrelic/opensource-website/raw/main/src/images/categories/dark/Experimental.png"><source media="(prefers-color-scheme: light)" srcset="https://github.com/newrelic/opensource-website/raw/main/src/images/categories/Experimental.png"><img alt="New Relic Open Source experimental project banner." src="https://github.com/newrelic/opensource-website/raw/main/src/images/categories/Experimental.png"></picture></a>


![GitHub forks](https://img.shields.io/github/forks/newrelic-experimental/newrelic-java-thrift?style=social)
![GitHub stars](https://img.shields.io/github/stars/newrelic-experimental/newrelic-java-thrift?style=social)
![GitHub watchers](https://img.shields.io/github/watchers/newrelic-experimental/newrelic-java-thrift?style=social)

![GitHub all releases](https://img.shields.io/github/downloads/newrelic-experimental/newrelic-java-thrift/total)
![GitHub release (latest by date)](https://img.shields.io/github/v/release/newrelic-experimental/newrelic-java-thrift)
![GitHub last commit](https://img.shields.io/github/last-commit/newrelic-experimental/newrelic-java-thrift)
![GitHub Release Date](https://img.shields.io/github/release-date/newrelic-experimental/newrelic-java-thrift)


![GitHub issues](https://img.shields.io/github/issues/newrelic-experimental/newrelic-java-thrift)
![GitHub issues closed](https://img.shields.io/github/issues-closed/newrelic-experimental/newrelic-java-thrift)
![GitHub pull requests](https://img.shields.io/github/issues-pr/newrelic-experimental/newrelic-java-thrift)
![GitHub pull requests closed](https://img.shields.io/github/issues-pr-closed/newrelic-experimental/newrelic-java-thrift)


# New Relic Java Instrumentation for Apache Thrift

Provides instrumentation of both the client and server sides of Apache Thrift.  This includes support for distributed tracing between the client and the server.

## Installation

Since there is existing instrumentation for Thrift in the New Relic Java Agent it is recommended that you disable that instrumentation to avoid conflicts between it and this instrumentation.  Instructions for disabling are below.

This use this instrumentation.   
1. Download the latest release.    
2. In the New Relic Java Agent directory (directory containing newrelic.jar), create a directory named extensions if it doe not already exist.   
3. Copy the jars into the extensions directory.   
4. Restart the application.   

## Disabling New Relic Java Agent instrumentation

To avoid conflicts disable the Java Agent instrumentation as follows:

1. Edit newrelic.yml and find the following:    

 class_transformer:
    # This instrumentation reports the name of the user principal returned from 
    # HttpServletRequest.getUserPrincipal() when servlets and filters are invoked.
    com.newrelic.instrumentation.servlet-user:
      enabled: false

    com.newrelic.instrumentation.spring-aop-2:
      enabled: false

 2. Add the following lines, be sure to have 4 spaces at the beginning of the first line and 6 spaces at the beginning of the second line.

     com.newrelic.instrumentation.thrift-0.8:
      enabled: false
 3. Save newrelic.yml

## Getting Started

After deployment, you should be able to client calls showing up as external services on the Thrift Client application and Thrift transactions on the Thrift server.  Additionally in the dirstributed tracing view in the New Relic UI, you should see the two transaction tied together.  Note that on the client side the external calls will only show up if there is an active transaction.    

## Support

New Relic has open-sourced this project. This project is provided AS-IS WITHOUT WARRANTY OR DEDICATED SUPPORT. Issues and contributions should be reported to the project here on GitHub.

>We encourage you to bring your experiences and questions to the [Explorers Hub](https://discuss.newrelic.com) where our community members collaborate on solutions and new ideas.

## Contributing

We encourage your contributions to improve Salesforce Commerce Cloud for New Relic Browser! Keep in mind when you submit your pull request, you'll need to sign the CLA via the click-through using CLA-Assistant. You only have to sign the CLA one time per project. If you have any questions, or to execute our corporate CLA, required if your contribution is on behalf of a company, please drop us an email at opensource@newrelic.com.

**A note about vulnerabilities**

As noted in our [security policy](../../security/policy), New Relic is committed to the privacy and security of our customers and their data. We believe that providing coordinated disclosure by security researchers and engaging with the security community are important means to achieve our security goals.

If you believe you have found a security vulnerability in this project or any of New Relic's products or websites, we welcome and greatly appreciate you reporting it to New Relic through [HackerOne](https://hackerone.com/newrelic).

## License

New Relic Java Instrumentation for Apache Thrift is licensed under the [Apache 2.0](http://apache.org/licenses/LICENSE-2.0.txt) License.

>[If applicable: [Project Name] also uses source code from third-party libraries. You can find full details on which libraries are used and the terms under which they are licensed in the third-party notices document.]
