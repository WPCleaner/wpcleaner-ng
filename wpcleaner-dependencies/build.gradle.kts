plugins {
    id("java-platform")
}

javaPlatform { allowDependencies() }

dependencies {
    api(enforcedPlatform("org.springframework.boot:spring-boot-dependencies:4.1.0"))

    constraints {
        api("org.jspecify:jspecify:1.0.0")
        api("org.controlsfx:controlsfx:11.2.1")
        api("org.fxmisc.richtext:richtextfx:0.11.7")
        api("io.github.java-diff-utils:java-diff-utils:4.17")
        api("org.reactfx:reactfx:2.0-M5")
    }
}