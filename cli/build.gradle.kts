plugins {
    java
    application
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
    implementation(project(":core"))
}

val mainClass = "net.karmacoder.duke.console.Main"
application {
    mainClassName = mainClass
}
