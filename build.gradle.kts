plugins {
    id("java")
    application
}

group = "bill.jgit"
version = "v1.5.0"

sourceSets{
    main{
        java.srcDirs("src/main/java", "src/main/dev")
        resources.srcDirs("rc/main/resources")
    }
}

application {
    mainClass.set("com.google.gitiles.MainCli")
}

tasks {
    jar {
        manifest {
            attributes["Main-Class"] = "com.google.gitiles.MainCli"
        }
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    }
}

dependencies {
    implementation("commons-cli:commons-cli:1.9.0")

    implementation(files("libs/jms-1.1.jar"))
    implementation(files("libs/jmxri-1.2.1.jar"))
    implementation(files("libs/jmxtools-1.2.1.jar"))

    implementation(project(":modules:java-prettify"))
    
    implementation("com.google.errorprone:error_prone_annotations:2.22.0")
    implementation("commons-codec:commons-codec:1.16.0")
    implementation("org.apache.commons:commons-lang3:3.8.1")
    implementation("org.apache.commons:commons-text:1.2")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.google.guava:guava:32.1.2-jre")
    implementation("com.google.guava:failureaccess:1.0.1")
    implementation("com.google.code.findbugs:jsr305:3.0.1")
    implementation("org.commonmark:commonmark:0.21.0")
    implementation("org.commonmark:commonmark-ext-autolink:0.21.0")
    implementation("org.nibor.autolink:autolink:0.10.0")
    implementation("org.commonmark:commonmark-ext-gfm-strikethrough:0.21.0")
    implementation("org.commonmark:commonmark-ext-gfm-tables:0.21.0")
    implementation("javax.servlet:javax.servlet-api:3.1.0")
    implementation("com.google.truth:truth:1.1")
    implementation("com.googlecode.java-diff-utils:diffutils:1.3.0")
    implementation("com.google.template:soy:2024-01-30")
    implementation("com.google.flogger:flogger:0.7.4")
    implementation("com.google.flogger:flogger-log4j-backend:0.7.4") {
        exclude(group = "com.sun.jdmk", module = "jmxtools")
        exclude(group = "com.sun.jmx", module = "jmxri")
        exclude(group = "javax.jms", module = "jms")
    }
    implementation("com.google.flogger:google-extensions:0.7.4")
    implementation("com.google.flogger:flogger-system-backend:0.7.4")
    implementation("com.google.common.html.types:types:1.0.8")
    implementation("com.google.protobuf:protobuf-java:3.19.4")
    implementation("com.ibm.icu:icu4j:74.2")
    implementation("com.googlecode.javaewah:JavaEWAH:1.2.3")
    implementation("org.apache.commons:commons-compress:1.25.0")
    implementation("org.tukaani:xz:1.9")

    testImplementation("junit:junit:4.12")
    testImplementation("org.hamcrest:hamcrest:2.2")

    implementation("org.mockito:mockito-core:5.6.0")
    implementation("net.bytebuddy:byte-buddy:1.14.9")
    implementation("net.bytebuddy:byte-buddy-agent:1.14.9")
    implementation("org.objenesis:objenesis:2.6")
    implementation("org.slf4j:slf4j-api:1.7.36")
    implementation("org.slf4j:slf4j-simple:1.7.36")
    implementation("org.eclipse.jetty:jetty-servlet:9.4.53.v20231009")
    implementation("org.eclipse.jetty:jetty-security:9.4.53.v20231009")
    implementation("org.eclipse.jetty:jetty-server:9.4.53.v20231009")
    implementation("org.eclipse.jetty:jetty-continuation:9.4.53.v20231009")
    implementation("org.eclipse.jetty:jetty-http:9.4.53.v20231009")
    implementation("org.eclipse.jetty:jetty-io:9.4.53.v20231009")
    implementation("org.eclipse.jetty:jetty-util:9.4.53.v20231009")
    implementation("org.ow2.asm:asm:9.2")
    implementation("org.ow2.asm:asm-analysis:9.2")
    implementation("org.ow2.asm:asm-commons:9.2")
    implementation("org.ow2.asm:asm-tree:9.2")
    implementation("org.ow2.asm:asm-util:9.2")

    implementation("org.eclipse.jgit:org.eclipse.jgit.http.server:6.10.1-SNAPSHOT")
    implementation("org.eclipse.jgit:org.eclipse.jgit:6.10.1-SNAPSHOT")
    implementation("org.eclipse.jgit:org.eclipse.jgit.archive:6.10.1-SNAPSHOT")
}