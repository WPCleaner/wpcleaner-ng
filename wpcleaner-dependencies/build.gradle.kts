plugins {
    id("java-platform")
}

javaPlatform { allowDependencies() }

dependencies {
    api(enforcedPlatform("org.springframework.boot:spring-boot-dependencies:3.4.5"))

    constraints {
        api("com.google.code.findbugs:jsr305:3.0.2")
    }
}