import com.jfrog.bintray.gradle.BintrayExtension
import java.util.Date
import org.gradle.jvm.tasks.Jar
import org.jetbrains.dokka.gradle.DokkaTask

plugins {
    kotlin("jvm") version "1.3.11"

    id("com.github.kukuhyoniatmoko.buildconfigkotlin") version "1.0.5"
    id("org.jetbrains.dokka") version "0.9.17"

    `maven-publish`
    id("com.jfrog.bintray") version "1.8.4"
}

object Libs {
    val okhttpVersion = "3.12.1"
    val xpp3Version = "1.1.6"
}

repositories {
    jcenter()
}

dependencies {
    implementation(kotlin("stdlib"))

    api("com.squareup.okhttp3:okhttp:${Libs.okhttpVersion}")
    implementation("org.ogce:xpp3:${Libs.xpp3Version}")       // XmlPullParser

    testImplementation("com.squareup.okhttp3:mockwebserver:${Libs.okhttpVersion}")
}

buildConfigKotlin {
    sourceSet("main", Action {
        buildConfig(name = "okhttpVersion", value = Libs.okhttpVersion)
    })
}


group = "at.bitfire.dav4jvm"
version = "0.1"

val java: JavaPluginConvention = convention.getPluginByName("java")
val sourcesJar = task<Jar>("sourcesJar") {
    classifier = "sources"
    from(java.sourceSets.getByName("main").allSource)
}
val dokkaJar = task<Jar>("dokkaJar") {
    dependsOn("dokka")
    classifier = "javadoc"
    from((tasks.getByName("dokka") as DokkaTask).outputDirectory)
}

publishing {
    publications.invoke {
        create<MavenPublication>("mavenJava") {
            groupId = project.group.toString()
            this.artifactId = artifactId
            version = project.version.toString()

            from(components["java"])
            artifact(sourcesJar)
            artifact(dokkaJar)

            pom {
                name.set("dav4jvm")
            }
        }
    }
}

fun findProperty(s: String) = project.findProperty(s) as String?
bintray {
    user = findProperty("bintrayUser")
    key = findProperty("bintrayApiKey")
    publish = true
    setPublications("mavenJava")
    setConfigurations("archives")
    pkg(closureOf<BintrayExtension.PackageConfig> {
        repo = "dav4jvm"
        name = "dav4jvm"
        desc = "WebDAV (including CalDAV, CardDAV) library for the Java virtual machine (Java/Kotlin)"
        userOrg = "bitfireat"
        websiteUrl = "https://gitlab.com/bitfireAT/dav4jvm"
        issueTrackerUrl = "https://forums.bitfire.at/category/18/libraries"
        vcsUrl = "https://gitlab.com/bitfireAT/dav4jvm.git"
        setLicenses("MPL-2.0")
        version(closureOf<BintrayExtension.VersionConfig> {
            name = project.version.toString()
            released = Date().toString()
        })
    })
}

