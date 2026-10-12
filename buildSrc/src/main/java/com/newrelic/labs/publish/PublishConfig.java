package com.newrelic.labs.publish;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.maven.MavenPom;
import org.gradle.api.publish.maven.MavenPublication;
import org.gradle.plugins.signing.SigningExtension;

import java.net.URI;

/**
 * Shared Maven Central publish/signing config for newrelic-java-thrift modules.
 * Mirrors the pattern used by newrelic-java-agent's PublishConfig and the
 * com.newrelic.labs OSSRH namespace used by otel-mule4-observability-agent.
 */
public class PublishConfig {

    public static void config(Project project, String name, String description, Action<? super MavenPublication> setArtifacts) {
        project.getExtensions().configure(PublishingExtension.class, ext -> {

            ext.publications(container ->
                    container.create("mavenJava", MavenPublication.class, publication -> {
                        setArtifacts.execute(publication);
                        publication.pom(pom -> {
                            pom.getName().set(name);
                            pom.getDescription().set(description);
                            configurePom(pom);
                        });

                        configureSigning(project, publication);
                    })
            );
            ext.repositories(handler ->
                    handler.maven(repo -> configureRepo(repo, project.getVersion().toString()))
            );
        });
    }

    private static void configureSigning(Project project, MavenPublication publication) {
        project.getExtensions().configure(SigningExtension.class, ext -> {
            // Shells out to the local gpg binary/agent, same as otel-mule4-observability-agent's
            // maven-gpg-plugin — no key material needs to be exported or passed in as a property.
            ext.useGpgCmd();
            ext.sign(publication);
        });
    }

    private static void configureRepo(MavenArtifactRepository repo, String projectVersion) {
        URI releasesRepoUri = URI.create("https://ossrh-staging-api.central.sonatype.com/service/local/staging/deploy/maven2/");
        URI snapshotsRepoUrl = URI.create("https://central.sonatype.com/repository/maven-snapshots/");
        repo.setUrl(projectVersion.endsWith("SNAPSHOT") ? snapshotsRepoUrl : releasesRepoUri);
        repo.credentials(creds -> {
            creds.setUsername(System.getenv("SONATYPE_USERNAME"));
            creds.setPassword(System.getenv("SONATYPE_PASSWORD"));
        });
    }

    private static void configurePom(MavenPom pom) {
        pom.getUrl().set("https://github.com/newrelic-experimental/newrelic-java-thrift");
        pom.licenses(spec -> spec.license(license -> {
            license.getName().set("The Apache License, Version 2.0");
            license.getUrl().set("http://www.apache.org/licenses/LICENSE-2.0.txt");
            license.getDistribution().set("repo");
        }));
        pom.developers(spec -> spec.developer(dev -> {
            dev.getId().set("newrelic");
            dev.getName().set("New Relic");
            dev.getEmail().set("opensource@newrelic.com");
        }));
        pom.scm(scm -> {
            scm.getUrl().set("git@github.com:newrelic-experimental/newrelic-java-thrift.git");
            scm.getConnection().set("scm:git@github.com:newrelic-experimental/newrelic-java-thrift.git");
        });
    }
}
