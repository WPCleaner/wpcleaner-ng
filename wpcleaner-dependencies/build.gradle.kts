plugins {
    id("java-platform")
}

javaPlatform { allowDependencies() }

dependencies {
    api(enforcedPlatform("org.springframework.boot:spring-boot-dependencies:3.2.3"))

    constraints {
        api("com.google.code.findbugs:jsr305:3.0.2")
    }
}