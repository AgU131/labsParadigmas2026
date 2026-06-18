import org.json4s._
import org.json4s.jackson.JsonMethods._
import scala.io.Source


object Main {
  // Define `Subscription` as a simple tuple type alias
  type Subscription = (String, String)

    // Pure function to read subscriptions from a JSON file
  def readSubscriptions(path: String): Option[List[Subscription]] = {
    implicit val formats: Formats = DefaultFormats

    try {
      val source = Source.fromFile(path)

      try {
        val jsonString = source.mkString

        val json = parse(jsonString)

        val subscriptions =
          json.extract[List[Map[String, Any]]]

        if (
          subscriptions.forall(sub =>
            sub.contains("name") &&
            sub.contains("url") &&
            sub.contains("count") &&
            sub.contains("before")
          )
        ) {

          Some(
            subscriptions.map { sub =>
              (
                sub("name").asInstanceOf[String],
                sub("url").asInstanceOf[String],
                sub("count").asInstanceOf[BigInt].toInt,
                sub("before").asInstanceOf[String]
              )
            }
          )

        } else {
          None
        }

      } finally {
        source.close()
      }

    } catch {
      case _: Exception => None
    }
  }
  
  def buildUrl(subscription: Subscription): String = {
    val (_, url, count, before) = subscription

    s"$url?count=$count&before=$before"
  }
  
  def readPosts(url: String): Option[List[String]] = {
  implicit val formats: Formats = DefaultFormats

  try {
    val source = Source.fromURL(url)

    try {
      val jsonContent = source.mkString

      val json = parse(jsonContent)

      val children =
        (json \ "data" \ "children")
          .extract[List[JValue]]

      Some(
        children.map { child =>
          val data = child \ "data"
          (data \ "title").extract[String]
        }
      )

    } finally {
      source.close()
    }

  } catch {
    case _: Exception => None
  }
}

  // FUNCIONES AUXILIARES PARA IMPRIMIR UN POST Y UN SUBSCRIPTION
  // PUEDEN SER MODIFICADAS PARA AGREGAR OPTION Y COMPLETAR EL EJERCICIO 3
  // def printPost(post: Post): Unit = {
  //   val content = post._2
  //   val truncatedContent = if (content.length > 80) content.take(80) else content
  //   println(s"${post._1} by **${post._3}**")
  //   println(truncatedContent)
  //   println("-----------------------")
  // }

  // def printSubscription(allPosts: (String, List[Post])): Unit = {
  //   val url = posts._1
  //   println(s"Posts from: $url")
  //   posts._2.map(printPost)
  // }

  // Main function to run
  def main(args: Array[String]): Unit = {
    val header = s"Reddit Post Parser\n${"=" * 40}"

    println("=======================")
    println("EJ1: LEER SUSCRIPCIONES")
    val subscriptions: Option[List[Subscription]] = readSubscriptions("subscriptions.json")

    // Print subscriptions read - We can use imperative for I/O
    for (subscription <- subscriptions) {
      println(subscription)
    }

    println("=======================")
    println("")
    println("=======================")
    println("EJ2: DESCARGAR POSTS")

    // Descargar y parsear los posts
    val allPosts =
    subscriptions.flatMap(subscription =>
      readPosts(subscription._2)
    )

    println("=======================")
    println("")
    println("=======================")
    println("EJ3: IMPRIMIR POSTS Y CONTEO DE PALABRAS CENSURADAS")

    // Print final results
    //allPosts.map(printSubscription)
    println("=======================")
  }
}