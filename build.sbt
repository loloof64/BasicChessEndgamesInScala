enablePlugins(AndroidApp)
android.useSupportVectors

versionCode := Some(1)
versionName := Some("1.0.8")
platformTarget := "android-27"
minSdkVersion in Android :="16"
scalaVersion :="2.11.12"

resolvers += "Google Maven Repository" at "https://maven.google.com"

libraryDependencies ++= Seq(
    "com.android.support" % "appcompat-v7" % "27.0.2",
    "com.android.support" % "design" % "27.0.2",
    "com.android.support.constraint" % "constraint-layout" % "1.1.0-beta4"
)

