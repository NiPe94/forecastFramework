import org.apache.spark.ml.Transformer
import org.apache.spark.ml.regression.LinearRegression
import org.apache.spark.sql.{Dataset, Row}
import org.kit.energy.api.{AlgoParam, AlgoPlugin}

/**
  * This class shows an implementation example for a linear regression algorithm
  * which is written in scala and will be used by the forecast service and apache spark.
  *
  * First of all, the class has to extend the "AlgoPlugin"-Interface via the "extends" keyword,
  * so the forecast service can work with it.
  *
  * Due to that, two methods called "train()" and "apply()" have to be implemented too,
  * which will be explained in the following.
  * @author Nico Peter
  */
class ScalaAlgorithm extends AlgoPlugin{
  /**
    * In most cases, an algorithm needs some input parameters´to get started.
    * For defining input parameters, you have to use the "AlgoParam"-Annotation like:
    *
    * \@AlgoParam(name="nameOfParameter", value="defaultValueOfParameter")
    * followed by: var someName:String = _
    *
    * Inside the AlgoParam you define two things. The first thing is the name of the parameter and
    * the second thing represents the default value for that parameter.
    * Both entries will be displayed on the web page of the forecast service,
    * where the default value can be changed by a user.
    *
    * So if you write e.g. value=2.3, that value will be displayed on the web page and
    * a user can either accept that value or set it e.g. to 1.8 so 1.8 will be the value used within the algorithm.
    *
    * The line below the definition of "AlgoParam" contains the definition of a scala variable.
    * For that, the name of the variable is not important for the forecast service.
    *
    * Here the elastic net parameter for a linear regression is initially set to 1,
    * which means that a Lasso regression will be performed.
    */
  @AlgoParam(name="Elastic Net Parameter", value="1")
  var elasticNet:String = _

  /**
    * This is one of two methods which have to be implemented when using the interface "AlgoPlugin".
    * This method "train()" contains the implementation for training a mathematical model.
    * For that the method will get a DataFrame object, in which the needed dataset is inside.
    * The data will be loaded from a csv file or a openTSDB database using the web page from the forecast service.
    * DataFrame is a column based collection datatype from apache spark and
    * will be used by the machine learning library called spark ml.
    *
    * The format, with which the data will be presented is:
    * |label  (double value)|features (array of double values)  | (columns)
    * |2.7                  |[4.8, 2.3, 5.0, 7.6, 2.0]          |
    * |1.0                  |[42.0, 2.45, 5.3, 13.6, 2.0]       |
    *
    * As result, a trained model of type "Transformer"  with evaluated training parameters
    * will be returned to the forecast service.
    */
  def train(dataset: Dataset[Row]): Transformer = {

    /**
      * In this example, a linear regression model will be trained.
      * The first line defines a new linear regression,
      * using the two input parameters (regParam, elasticNet) defined above via the annotation "AlgoParam".
      * For further details about the linear regression with spark, please read the documentation from spark.
      *
      * The second line uses the defined regression to train a model.
      * Such a model consists of many parameters such as coefficients, intercept and error measurements.
      *
      * Finally, the trained model will be returned. (Here, no "return" keyword is necessary)
      */
    val lrModelDefinition = new LinearRegression().setElasticNetParam(elasticNet.toDouble)
    var lrModelTrained = lrModelDefinition.fit(dataset)

    lrModelTrained
  }

  /**
    * The second method to be implemented when using the interface "AlgoPlugin" is called "applyModel".
    * Here the loaded dataset and the trained model will be used to perform a forecast.
    * The values of the prediction are stored automatically under the column "prediction".
    * This column with its data will be returned to the forecast service, which exports the new data e.g. as a csv file
    */
  def applyModel(dataset: Dataset[Row], transformer: Transformer): Dataset[Row] = {
    val transformedData = transformer.transform(dataset).select("prediction")

    transformedData
  }

}
