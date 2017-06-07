# BuildRadiator Ant Listener

This Ant Listener goes hand in hand with a [Build Radiator](//github.com//paul-hammant/buildradiator) site.

A build is a number of conceptual steps. Steps starting, passing and failing are events that this
extension can pass to the build radiator (build cancellation is is outside the control of this tech).

## Setting up the listener for your build

In order to do the step updates on buildradiator.org ...

Go and get the JAR from [Maven Central](https://repo.maven.apache.org/maven2/com/paulhammant/buildradiatorantlistener/)

Copy that into Ant's `lib/` folder.  This has to happen on the Ant that the CI infrastructure uses. It may be a problem 
for the online services.

Then configure your build script to do:

```
ant -listener com.paulhammant.buildradiatorantlistener.BuildRadiatorAntListener jar
```

## Configuring Steps 

At the top of your project's build.xml, you need to identify project/target combinations where a step starts:

```
<property name="buildradiator.0" value="Compile=*" />
<property name="buildradiator.0" value="Unit_Tests=projName/test<" />
<property name="buildradiator.0" value="Functional_Tests=projName/funcTest<" />
<property name="buildradiator.0" value="Package=*" />
```

If you mis-type a project/target to the right-hand side of a `=`, then the build will look like
this after it has finished:

![](https://cloud.githubusercontent.com/assets/82182/26393757/ce22ad8c-4038-11e7-8878-5d3b1be0cbf0.png)

It will also look like that if you mis-type a step-name on the left-hand side of a `=`.

## Targeting a radiator other than buildradiator.org

If you have hosted your own build radiator server:

```
<property name="buildradiator.baseurl" value="Compile=*" />
```

Be sure to get the `http` vs `https` right.

## Important environment variables 

You need to set these for each CI initiated build, before Maven is launched:

```
export buildId=<the build number from Jenkin or the commit hash etc>
export radiatorCode=<radiator code from when you created the radiator>
export radiatorSecret=<radiator secret from when you created the radiator>
```

Don't do these on your dev workstation, because updating the build radiator is the business of your CI daemon.

## Researching where to make step changes

In your project's build.xml:

```
<property name="buildradiator.trace" value="true" />
```
