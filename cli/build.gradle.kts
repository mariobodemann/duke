plugins {
    java
    application
}

java {
    sourceCompatibility = JavaVersion.VERSION_12
    targetCompatibility = JavaVersion.VERSION_12
}

dependencies {
    implementation(project(":core"))
}

val mainClass = "net.karmacoder.duke.console.Main"
application {
    mainClassName = mainClass
}
