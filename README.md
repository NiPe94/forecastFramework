# Bachelor Thesis Nico Peter - KIT IAI 2017
Repository with the software prototype developed during my Bachelor Thesis @ KIT IAI under supervision of Andreas Bartschat and Dr.-Ing. Markus Reischl.

## Content
* General Information
* How to: use it
* How to: write a plugin for it
* Limitations

## General Information
The software prototype proposed in this repository is called `Forecast Service`. It's a webservice I created during my bachelor project phase for my thesis "Entwurf und Entwicklung eines Webservices zur Erstellung von Energieprognosen in einer Big Data Umgebung". The aim of the Forecast Service is to produce machine learning models and/or create forecasts using existing models with the help of the machine learning library ``MLlib`` from Apache Spark. Therefore a web page, generated by the server, is available to:
* Start Apache Spark locally or from a available cluster,
* Add time series data from a CSV file or a OpenTSDB database,
* Choose an provided algorithm and set its parameters to generate a new model trained with the loaded datasets,
* Produce a prediction using a already trained machine learning model

This software uses and works with following technologies and versions:

| Technology | Version |
| ---------- | ---------- |
| Java | 1.8 |
| Maven | 3.3.9 |
| Spring Boot | 1.4.3.RELEASE |
| Scala | 2.11.8 |
| Spark | 2.1.0 |

## How to: use it
1. Clone the repository
2. Import the project into IntelliJ as a Maven project
3. Run the ``ForecastFrameworkApplication`` class
4. Type ``localhost:8080`` into your browser and the following page appears:

![Screenshot](screenshots/firstPage.PNG)

5. Fore more details on how to use the service, please read my bachelor thesis or select the ``About`` tab of the web page

## How to: write a plugin for it
In this section, two implementation examples for algorithm plugins are presented. The idea is to implement an algorithm which will then be stored inside a folder, from which the webservice loads all available algorithms during runtime. The first plugin is written in Java while the second plugin is written in Scala.
The directory, from which the webservice will read the algorithms, can be configured inside [application.properties] from the server module via the parameter ``plugin.loading.path``.
To achieve the implementation of a plugin, go through the following steps. Each step with a "*" means, that there are differences between the two examples, which will be explanied below.
1. Download the [api.jar] file from my repository (click on ``View Raw``)
2. Install IntelliJ from [jetbrains]
3. Start a new Maven project with Java 1.8
4. Copy the downloaded api.jar to your project directory in main/resources
5. (*) Copy the following dependencies inside the ``pom.xml`` file:
```{maven}
<dependencies>
    <!-- *** API dependency *** -->
    <dependency>
        <groupId>org.kit.energy.api</groupId>
        <artifactId>AlgoPlugin</artifactId>
        <version>LATEST</version>
        <scope>system</scope>
        <systemPath>${project.basedir}/src/main/resources/api.jar</systemPath>
    </dependency>

    <!-- *** SPARK FRAMEWORK *** -->
    <!-- Spark 2.1.0 for Scala 2.11 -->
    <dependency>
        <groupId>org.apache.spark</groupId>
        <artifactId>spark-core_2.11</artifactId>
        <version>2.1.0</version>
    </dependency>
    <!-- Spark SQL -->
    <!-- https://mvnrepository.com/artifact/org.apache.spark/spark-sql_2.10 -->
    <dependency>
        <groupId>org.apache.spark</groupId>
        <artifactId>spark-sql_2.11</artifactId>
        <version>2.1.0</version>
    </dependency>
    <!-- Spark MLib -->
    <!-- https://mvnrepository.com/artifact/org.apache.spark/spark-mllib_2.11 -->
    <dependency>
        <groupId>org.apache.spark</groupId>
        <artifactId>spark-mllib_2.11</artifactId>
        <version>2.1.0</version>
    </dependency>
</dependencies>
```
6. (*) Implement the interface via the ``@AlgoPlugin`` annotation and its methods ``train()`` and ``apply()``
7. (Optional) Define some parameters for the algorithm via the ``@AlgoParam`` annotation
8. Run the Maven commands from ``clean`` to ``install``
9. Copy the generated .jar file from your project to the directory from which the webservice reads

### Differences for the Java plugin
6. * Create a java class inside the java directory of your project. This will be the class in which the algorithm will be implemented
    * Implement the plugin interface from the api.jar for example like the following:
```{java}
public class ExampleAlgo implements AlgoPlugin {

    @AlgoParam(name = "parameterName",value = "0.9")
    public String myParam = "";

    public Transformer train(Dataset<Row> dataset) {
        LinearRegression linearRegression = new LinearRegression().setRegParam(Double.parseDouble(myParam));
        LinearRegressionModel linearModel = linearRegression.fit(dataset);
        return linearModel;
    }

    public Dataset<Row> applyModel(Dataset<Row> dataset, Transformer transformer) {
        Dataset<Row> predictedData = transformer.transform(dataset).select("prediction");
        return predictedData;
    }
}
```

### Differences for the Scala plugin
5. * For the plugin written in Scala, you also need the following maven entries:
```{maven}
...
<!-- https://mvnrepository.com/artifact/org.scala-lang/scala-library -->
    <dependency>
        <groupId>org.scala-lang</groupId>
        <artifactId>scala-library</artifactId>
        <version>2.12.3</version>
    </dependency>
</dependencies>

<build>
    <plugins>
        <plugin>
            <groupId>org.scala-tools</groupId>
            <artifactId>maven-scala-plugin</artifactId>
            <version>2.15.2</version>
            <executions>
                <execution>
                    <id>compile</id>
                    <goals>
                        <goal>compile</goal>
                    </goals>
                    <phase>compile</phase>
                </execution>
                <execution>
                    <id>test-compile</id>
                    <goals>
                        <goal>testCompile</goal>
                    </goals>
                    <phase>test-compile</phase>
                </execution>
                <execution>
                    <phase>process-resources</phase>
                    <goals>
                        <goal>compile</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```
6. * Create a directory called ``scala`` in your project inside ``src/main``
    * Click with the right mouse button on the created ``scala`` directory -> Select ``Mark Directory As`` -> Select ``Sources Root``
    * Click with the right mouse button on the directory with the project name (the first/root directory) -> Select ``Add Framework Support`` -> Activate ``Scala``
    * Create a Scala class inside the created ``scala`` directory. This will be the class in which the algorithm will be implemented
    * Implement the plugin interface from the api.jar for example like the following:
```{scala}
class ScalaAlgorithm extends AlgoPlugin{
  
  @AlgoParam(name="Elastic Net Parameter", value="1")
  var elasticNet:String = _

  def train(dataset: Dataset[Row]): Transformer = {

    val lrModelDefinition = new LinearRegression().setElasticNetParam(elasticNet.toDouble)
    var lrModelTrained = lrModelDefinition.fit(dataset)

    return lrModelTrained
  }

  def applyModel(dataset: Dataset[Row], transformer: Transformer): Dataset[Row] = {
    val transformedData = transformer.transform(dataset).select("prediction")

    return transformedData
  }

}
```

## Limitations
* No unit tests
* Spark Cluster support not tested
* The forecast [horizon] for predictions with [ar] models, is  not adjustable (set to 1)
* Not tested with algorithms from other machine learning libraries like H2O ([Sparkling Water])
* New predicted data will only be saved as a CSV file

[//]: # (These are reference links used in the body of this note and get stripped out when the markdown processor does its job. There is no need to format nicely because it shouldn't be seen. Thanks SO - http://stackoverflow.com/questions/4823468/store-comments-in-markdown-syntax)

   [api.jar]: <https://github.com/NiPe94/forecastFramework/blob/ChangesForBachelor/api.jar>
   [jetbrains]: <https://www.jetbrains.com/idea/documentation/>
   [application.properties]: <https://github.com/NiPe94/forecastFramework/blob/ChangesForBachelor/server/src/main/resources/application.properties>
   [horizon]: <http://blog.drhongtao.com/2014/09/forecasting-backcasting.html>
   [ar]: <https://www.otexts.org/fpp/8/3>
   [Sparkling Water]: <http://docs.h2o.ai/h2o/latest-stable/h2o-docs/faq/sparkling-water.html>
