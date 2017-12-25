enablePlugins(AndroidApp)
android.useSupportVectors

versionCode := Some(6)
versionName := Some("1.0.8")
platformTarget := "android-27"
minSdkVersion in Android :="16"
scalaVersion :="2.11.12"

val silencerVersion = "0.6"

resolvers ++= Seq(
    "Google Maven Repository" at "https://maven.google.com",
    "JitPack" at "https://jitpack.io"
)

libraryDependencies ++= Seq(
    "com.android.support" % "appcompat-v7" % "27.0.2",
    "com.android.support" % "design" % "27.0.2",
    "com.android.support.constraint" % "constraint-layout" % "1.1.0-beta4",
    "com.github.loloof64" % "chesslib" % "master",
    compilerPlugin("com.github.ghik" %% "silencer-plugin" % silencerVersion),
    "com.github.ghik" %% "silencer-lib" % silencerVersion
)

