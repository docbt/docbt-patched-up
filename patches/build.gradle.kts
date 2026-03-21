group = "app.docbt.patched_up"

patches {
    about {
        name = "docbt-patched-up"
        description = "Revived and updated patches for Morphe. First Release: Google News"
        source = "https://github.com/docbt/docbt-patched-up"
        author = "docbt"
        contact = "https://github.com/docbt/docbt-patched-up/issues"
        website = "https://github.com/docbt/docbt-patched-up"
        license = "GPLv3"
    }
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xcontext-parameters")
    }
}

dependencies {
    // Used by JsonGenerator.
    implementation(libs.gson)
}

tasks {
    register<JavaExec>("generatePatchesList") {
        description = "Build patch with patch list"

        dependsOn(build)

        classpath = sourceSets["main"].runtimeClasspath
        mainClass.set("app.morphe.util.PatchListGeneratorKt")
    }
    // Used by gradle-semantic-release-plugin.
    publish {
        dependsOn("generatePatchesList")
    }
}

kotlin {
    compilerOptions {
        freeCompilerArgs = listOf("-Xcontext-receivers")
    }
}
