plugins { id("com.diffplug.spotless") }

spotless { kotlinGradle { ktfmt() } }

tasks.named("spotlessKotlinGradleCheck") { dependsOn(tasks.named("spotlessKotlinGradleApply")) }
