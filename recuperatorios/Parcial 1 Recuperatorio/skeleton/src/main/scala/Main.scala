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
    val jsonString = try {source.mkString} finally {source.close()}  //manejamos error de que no abra el json
    implicit val formats: Formats = DefaultFormats

    val json = parse(jsonString)
    val subscriptions = json.extract[List[Map[String, Any]]].map { sub =>
      try {
        val name = sub("name").toString
        val url = sub("url").toString
        val before = sub("before").toString
        val count = sub("count").toString.toInt

        Some(name, (s"$url?count=$count&before=$before"))
      } catch {
        case _: Exception => None  // caso de que no exista alguno de sos campos requeridos
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
  // def printPost(post: Post): Unit = {
  //   println(s"  - ${post._2}: ${post._1}")
  // }
  
  // Main function to run
  def main(args: Array[String]): Unit = {
    val header = s"Reddit Post Parser\n${"=" * 40}"

    val subscriptions: List[Option[Subscription]] = readSubscriptions("subscriptions.json")

    // var allPosts: List[Post] = List.empty
    val allPosts: List[Post] = subscriptions.flatten.flatMap { sub =>
      println(sub)
      readPosts(sub._2).flatten
    }
    // for (subscription <- subscriptions) { 
    //   println(subscription)
    // }
    for (subscription <- subscriptions) {
      subscription match {
        case Some(subscription) =>
          val subscriptionName = subscription._1
          val subscriptionUrl = subscription._2
        
          println(s"Descargando posts de: $subscriptionName")
          var postsFromSubscription: List[Option[Post]] = readPosts(subscriptionUrl)

          for (post <- postsFromSubscription) {
            post match {
              case None =>
              case Some(post) => {
                println(s"  - ${post._2}: ${post._1}")
              }
            }
          }  
        case _ : Throwable => println("no hay sub aca bro: 404")
      }
    }

    println("=======================")
    println(s"Total de posts descargados: ${allPosts.length}")
    println("=======================")
    // allPosts.map(printPost)
  }
}
