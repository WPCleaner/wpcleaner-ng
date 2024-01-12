import java.nio.charset.StandardCharsets

plugins {
    id("java-library")
}

java {
    targetCompatibility = JavaVersion.VERSION_17
    sourceCompatibility = JavaVersion.VERSION_17
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<JavaCompile> {
    options.encoding = StandardCharsets.UTF_8.name()
    options.compilerArgs.addAll(
        listOf(
            "-parameters",
            "-Xlint:all,-processing",
            "-Werror"
        )
    )
}
