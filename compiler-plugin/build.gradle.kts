plugins {
    java
}

dependencies {
    implementation(project(":annotation"))

    annotationProcessor("com.google.auto.service:auto-service:1.0.1")
    implementation("com.google.auto.service:auto-service-annotations:1.0.1")
}

tasks.withType(JavaCompile::class) {
    options.apply {
        compilerArgs.addAll(
            listOf(
                "--add-exports", "jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED",
                "--add-exports", "jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED",
                "--add-exports", "jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED",
                "--add-exports", "jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED",
            )
        )
    }
}
