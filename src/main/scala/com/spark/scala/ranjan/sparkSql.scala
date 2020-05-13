package com.spark.scala.ranjan
/**
 * Created By Ranjan Kumar,Date-28/12/2019
 */
import org.apache.spark.sql.SparkSession

object sparkSql {
  def main(args: Array[String]): Unit = {
    val spark=SparkSession.builder().master("local[*]").appName("first spark").getOrCreate()
    println("Hi I am starting spark-SQL Programming ")
    // Register temp table
    val dfTags = spark
      .read
      .option("header", "true")
      .option("inferSchema", "true")
      .csv("src/main/resources/question_tags_10K.csv")
      .toDF("id", "tag")

    dfTags.createOrReplaceTempView("so_tags")

    // List all tables in Spark's catalog
    spark.catalog.listTables().show()

    // List all tables in Spark's catalog using Spark SQL
    spark.sql("show tables").show()

    // Select columns
    spark
      .sql("select id, tag from so_tags limit 10")
      .show()

    // Filter by column value
    spark
      .sql("select * from so_tags where tag = 'php'")
      .show(10)

    // Count number of rows
    spark
      .sql(
        """select
          |count(*) as php_count
          |from so_tags where tag='php'""".stripMargin)
      .show(10)

    // SQL like
    spark
      .sql(
        """select *
          |from so_tags
          |where tag like 's%'""".stripMargin)
      .show(10)

    // SQL where with and clause
    spark
      .sql(
        """select *
          |from so_tags
          |where tag like 's%'
          |and (id = 25 or id = 108)""".stripMargin)
      .show(10)

    // SQL IN clause
    spark
      .sql(
        """select *
          |from so_tags
          |where id in (25, 108)""".stripMargin)
      .show(10)

    // SQL Group By
    spark
      .sql(
        """select tag, count(*) as count
          |from so_tags group by tag""".stripMargin)
      .show(10)

    // SQL Group By with having clause
    spark
      .sql(
        """select tag, count(*) as count
          |from so_tags group by tag having count > 5""".stripMargin)
      .show(10)

    // SQL Order by
    spark
      .sql(
        """select tag, count(*) as count
          |from so_tags group by tag having count > 5 order by tag""".stripMargin)
      .show(10)
    val dfQuestionsCSV = spark
      .read
      .option("header", "true")
      .option("inferSchema", "true")
      .option("dateFormat","yyyy-MM-dd HH:mm:ss")
      .csv("src/main/resources/questions_10K.csv")
      .toDF("id", "creation_date", "closed_date", "deletion_date", "score", "owner_userid", "answer_count")


    // cast columns to data types
    val dfQuestions = dfQuestionsCSV.select(
      dfQuestionsCSV.col("id").cast("integer"),
      dfQuestionsCSV.col("creation_date").cast("timestamp"),
      dfQuestionsCSV.col("closed_date").cast("timestamp"),
      dfQuestionsCSV.col("deletion_date").cast("date"),
      dfQuestionsCSV.col("score").cast("integer"),
      dfQuestionsCSV.col("owner_userid").cast("integer"),
      dfQuestionsCSV.col("answer_count").cast("integer")
    )

    // filter dataframe
    val dfQuestionsSubset = dfQuestions.filter("score > 400 and score < 410").toDF()

    // register temp table
    dfQuestionsSubset.createOrReplaceTempView("so_questions")

    // SQL Inner Join
    spark
      .sql(
        """select t.*, q.*
          |from so_questions q
          |inner join so_tags t
          |on t.id = q.id""".stripMargin)
      .show(10)

    // SQL Left Outer Join
    spark
      .sql(
        """select t.*, q.*
          |from so_questions q
          |left outer join so_tags t
          |on t.id = q.id""".stripMargin)
      .show(10)

    // SQL Right Outer Join
    spark
      .sql(
        """select t.*, q.*
          |from so_tags t
          |right outer join so_questions q
          |on t.id = q.id""".stripMargin)
      .show(10)

    // SQL Distinct
    spark
      .sql("""select distinct tag from so_tags""".stripMargin)
      .show(10)

    // Function to prefix a String with so_ short for StackOverflow
    def prefixStackoverflow(s: String): String = s"so_$s"

    // Register User Defined Function (UDF)
    spark
      .udf
      .register("prefix_so", prefixStackoverflow _)

    // Use udf prefix_so to augment each tag value with so_
    spark
      .sql("""select id, prefix_so(tag) from so_tags""".stripMargin)
      .show(10)
  }
}
