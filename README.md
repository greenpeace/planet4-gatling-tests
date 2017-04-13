# Planet4 Performance Testsuite

This project contains the necessary files to run a performance test against a 
[planet4 setup](https://github.com/greenpeace/planet4-base), the next generation of greenpeace.org websites built on 
Wordpress. The tests are written in  Scala and can be run using [gatling](http://gatling.io/).

## Copyright and Licence

2017 (c) Greenpeace International

GPL v2 onward, see [LICENCE](LICENCE) file.


## Running the testsuite

### Prerequisite
You will need:
- Java 8
- Gatling: http://gatling.io/
- A Planet4 site: https://github.com/greenpeace/planet4-base 

### Setup
_There are several way you can install and run gatling, we will just assume you are downloading the zip archive
and not using the maven build._

- Download the gatling zip archive and unzip it
```
wget https://repo1.maven.org/maven2/io/gatling/highcharts/gatling-charts-highcharts-bundle/2.2.4/gatling-charts-highcharts-bundle-2.2.4-bundle.zip
unzip gatling-charts-highcharts-bundle-2.2.4-bundle.zip
cd gatling-charts-highcharts-bundle-2.2.4-bundle
```
- Replace the user files with a clone of this repository
```
rm -rf user-files
git clone https://github.com/greenpeace/planet4-gatling-tests userfiles
```
_Note: you can also just point to another target directory this by altering the configuration in conf/gatling.conf._

- Create a file in conf/application.conf
This file will hold the information specific to your instance such as the baseURL or the wordpress admin credentials.
For example:
```
application {
    baseURL = "https://planet4.dev"
    adminUser =  "admin"
    adminPassword = "password"
}
```
### Running a scenario
From gatling root folder, you can run the main script as follow:
```
./bin/gatling.sh -s base.Scenario1
```

### Viewing the results
The results will be displayed in the console but also available in /results as html files.
