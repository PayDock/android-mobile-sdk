plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
}

dependencies {
    // This allows us to use Version Catalog in sub-gradle scripts
    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
    api(libs.detekt.plugin)
}