package com.raaveinm.picasso

/** Verifies the desktop launch classpath, including JBR's possible bundled JCEF module. */
fun main() {
    val cefAppClass: Class<*> = Class.forName("org.cef.CefApp", false, Thread.currentThread().contextClassLoader)
    check(cefAppClass.module.name != "jcef") {
        "JBR's bundled JCEF shadows the application dependency. Check the desktop JVM --limit-modules argument."
    }
    checkNotNull(cefAppClass.getResource("/build_meta.json")) {
        "The JCEF API cannot access jcefmaven's build metadata."
    }
    val buildInfoClass: Class<*> = Class.forName("me.friwi.jcefmaven.CefBuildInfo")
    val buildInfo: Any = buildInfoClass.getMethod("fromClasspath").invoke(null)
    val releaseTag: String = buildInfoClass.getMethod("getReleaseTag").invoke(buildInfo) as String
    check(releaseTag.isNotBlank()) { "The JCEF release metadata is empty." }
    println("JCEF runtime verified: $releaseTag (module=${cefAppClass.module.name ?: "classpath"})")
}
