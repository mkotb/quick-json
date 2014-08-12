Quick-JSON
============

quick-json is a Java library that can be used to convert Java Objects into their JSON representation. It can also be used to convert a JSON string to an equivalent Java object. quick-json can work with any arbitrary Java objects.

Compiling
=========

- Download & Install [Maven 3](http://maven.apache.org/download.html)
- Clone the repository: `git clone https://github.com/mazentheamazin/quick-json`
- Compile and create the plugin package using Maven: `mvn clean install`

Maven will download all required dependencies and build a ready-for-use plugin package!

Adding Quick-JSON as a Maven Dependency
=========================================

Adding Quick-JSON as a Maven dependency is easy, lets go over how to do it.

First, add the Quick-JSON repo.

``` xml

<repository>
    <id>quick-json-repo</id>
    <url>https://raw.github.com/mazentheamazin/quick-json/mvn-repo/</url>
</repository>

```

Now, we just need to add the dependency:

``` xml

<dependency>
    <groupId>com.json</groupId>
    <artifactId>quick-json</artifactId>
    <scope>compile</scope>
    <version>1.0.2.3</version>
</dependency>

```