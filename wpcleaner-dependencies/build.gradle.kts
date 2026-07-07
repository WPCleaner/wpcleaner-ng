plugins {
    id("java-platform")
}

javaPlatform { allowDependencies() }

dependencies {
    api(enforcedPlatform("org.springframework.boot:spring-boot-dependencies:4.1.0"))

    constraints {
        api("org.jspecify:jspecify:1.0.0")
    }
}