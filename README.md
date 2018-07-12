# Welcome to UHOMEJ


# About
Uhomej is a pure-Java implementation of uhomej protocol which bases on the Ethereum protocol. It is second developed from Ethereumj project. For high-level information about Uhome and its goals, visit [uhome.io](https://uhome.io). The [uhome white paper](https://github.com/uhomeio/wiki/wiki/White-Paper) provides a complete conceptual overview, and the [yellow paper](https://github.com/uhomeio/wiki/wiki/Yellow-Paper) provides a formal definition of the protocol.

We keep Uhomej as thin as possible. For [JSON-RPC](https://github.com/uhomeio/wiki/wiki/JSON-RPC) support and other client features check [Uhome Harmony](https://github.com/uhomeio/uhome-harmony).

# Running EthereumJ

##### Adding as a dependency to your Maven project: 

```
   <dependency>
     <groupId>org.uhome</groupId>
     <artifactId>uhomej-core</artifactId>
     <version>0.0.1-RELEASE</version>
   </dependency>
```
##### or your Gradle project: 

```
   repositories {
       mavenCentral()
       jcenter()
       maven { url "https://dl.bintray.com/uhomeio/maven/" }
   }
   compile "org.uhome:uhomej-core:0.0.1"
```

##### Building an executable JAR
```
git clone https://github.com/uhomeio/uhomej
cd uhomej
cp uhomej-core/src/main/resources/ethereumj.conf uhomej-core/src/main/resources/user.conf
vim uhomej-core/src/main/resources/user.conf # adjust user.conf to your needs
./gradlew clean fatJar
java -jar uhomej-core/build/libs/uhomej-core-*-all.jar
```

##### Running from command line:
```
> git clone https://github.com/uhomeio/uhomej
> cd uhomej
> ./gradlew run [-PmainClass=<sample class>]
```

##### Optional samples to try:
```
./gradlew run -PmainClass=org.ethereum.samples.BasicSample
./gradlew run -PmainClass=org.ethereum.samples.FollowAccount
./gradlew run -PmainClass=org.ethereum.samples.PendingStateSample
./gradlew run -PmainClass=org.ethereum.samples.PriceFeedSample
./gradlew run -PmainClass=org.ethereum.samples.PrivateMinerSample
./gradlew run -PmainClass=org.ethereum.samples.TestNetSample
./gradlew run -PmainClass=org.ethereum.samples.TransactionBomb
```

##### Importing project to IntelliJ IDEA: 
```
> git clone https://github.com/uhomeio/uhomej
> cd uhomej
> gradlew build
```
  IDEA: 
* File -> New -> Project from existing sources…
* Select uhomej/build.gradle
* Dialog “Import Project from gradle”: press “OK”
* After building run either `org.ethereum.Start`, one of `org.ethereum.samples.*` or create your own main. 

# Configuring Uhomej

For reference on all existing options, their description and defaults you may refer to the default config `ethereumj.conf` (you may find it in either the library jar or in the source tree `uhomej-core/src/main/resources`) 
To override needed options you may use one of the following ways: 
* put your options to the `<working dir>/config/ethereumj.conf` file
* put `user.conf` to the root of your classpath (as a resource) 
* put your options to any file and supply it via `-Dethereumj.conf.file=<your config>`
* programmatically by using `SystemProperties.CONFIG.override*()`
* programmatically using by overriding Spring `SystemProperties` bean 

Note that don’t need to put all the options to your custom config, just those you want to override. 