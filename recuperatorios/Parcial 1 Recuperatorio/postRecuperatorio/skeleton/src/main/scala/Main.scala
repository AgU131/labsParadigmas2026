import org.json4s._
import org.json4s.jackson.JsonMethods._
import scala.io.Source


object Main {
  // Define `Subscription` as a simple tuple type alias
  type Subscription = (String, String)  // (name, url)
  type Post = (String, String)          // (title, author)

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
      val jsonContent = source.mkString
      source.close
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

    val allPosts: List[Post] = subscriptions.flatMap {
      case None => List.empty
      case Some((name, url)) =>              // == Some(subscription)
        println(s"Descargando posts de: $name")
        val posts = readPosts(url).flatten          //: List[Option[Post]]
        posts.foreach {
          case (title, author) =>
            println(s"  - ${author}: ${title}")
        }
        posts
    }

    println("=======================")
    println(s"Total de posts descargados: ${allPosts.length}")
    println("=======================")
  }
}
