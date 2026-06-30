import org.json4s._
import org.json4s.jackson.JsonMethods._
import scala.io.Source


object Main {
  // Define `Subscription` as a simple tuple type alias
  type Subscription = (String, String)
  type Post = (String, String)

    // Pure function to read subscriptions from a JSON file
  def readSubscriptions(path: String): List[Option[Subscription]] = {
    val source = Source.fromFile(path)
    val jsonString = try {source.mkString} finally {source.close}
    implicit val formats: Formats = DefaultFormats

    val json = parse(jsonString)
    val subscriptions = json.extract[List[Map[String, Any]]].map { subscriptionMap =>
      try {
        val name = subscriptionMap("name").toString
        val url = subscriptionMap("url").toString
        val before = subscriptionMap("before").toString
        val count = subscriptionMap("count").toString.toInt

        Some((name, s"$url?count=$count&before=$before"))
      } catch {
        case _: Exception => None
      }
    }
    subscriptions.toList
  }

  def readPosts(url: String): List[Option[Post]] = {
    try {
      val source = Source.fromURL(url)
      val jsonContent = try {source.mkString} finally {source.close}
      implicit val formats: Formats = DefaultFormats

      val json = parse(jsonContent)
      val children = (json \ "data" \ "children").extract[List[JValue]]

      children.map { child =>
        try {
          val data = child \ "data"
          val title = (data \ "title").extract[String]
          val author = (data \ "author").extract[String]
          Some(title, author)
        }
        catch {
          case _: Throwable => None
        }
      }
    }
    catch {
      case _: Throwable => List.empty
    }
  }

  // Main function to run
  def main(args: Array[String]): Unit = {
    val header = s"Reddit Post Parser\n${"=" * 40}"

    val subscriptions: List[Option[Subscription]] = readSubscriptions("subscriptions.json")

    var allPosts: List[Post] = List.empty

    for (subscription <- subscriptions) {
      subscription match {
        case (Some(subscription)) =>
          val subscriptionName = subscription._1
          val subscriptionUrl = subscription._2

          println(s"Descargando posts de: $subscriptionName")
          var postsFromSubscription: List[Option[Post]] = readPosts(subscriptionUrl)

          for (post <- postsFromSubscription) {
            post match {
              case None =>
              case Some(post) => {
                allPosts = allPosts :+ post
                println(s"  - ${post._2}: ${post._1}")
              }
            }
          }
        case _ =>
      }
    }

    println("=======================")
    println(s"Total de posts descargados: ${allPosts.length}")
    println("=======================")
  }
}
