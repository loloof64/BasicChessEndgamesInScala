enablePlugins(AndroidApp)
android.useSupportVectors

versionCode := Some(1)
versionName := Some("1.0.8")
platformTarget := "android-27"
minSdkVersion in Android :="16"

libraryDependencies ++= Seq(
    "com.android.support" % "appcompat-v7" % "27.0.2"
)

