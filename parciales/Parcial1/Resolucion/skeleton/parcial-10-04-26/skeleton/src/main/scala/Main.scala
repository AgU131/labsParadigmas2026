import org.json4s._
import org.json4s.jackson.JsonMethods._
import scala.io.Source


object Main {
  // Define `Subscription` as a simple tuple type alias
  type Subscription = (String, String)

  type Post = (String, String, String) // (title, selftext, autor)

  // Pure function to read subscriptions from a JSON file
  def readSubscriptions(path: String): List[Option[Subscription]] = {
    val source = Source.fromFile(path)
    val jsonString = try {source.mkString} finally {source.close()}
    implicit val formats: Formats = DefaultFormats

    val json = parse(jsonString)
    val subscriptions = json.extract[List[Map[String, Any]]].map { item =>
      try {
        val url = item("url").toString
        val name = item("name").toString
        val before = item("before").toString
        val count = item("count").toString.toInt

        Some((name, s"$url?count=$count&before=$before"))
      } catch {
        case _: Exception => None
      }
    }
    // val subscriptions = parse(jsonString).children.map {item =>
    //     try{
    //       val url = (item \ "url").extract[String]
    //       val name = (item \ "name").extract[String]
    //       val before = (item \ "before").extract[String]
    //       val count = (item \ "count").extract[Int]
    //       Some(name, (url+"?count="+count+"&before="+before))
    //     } catch {
    //       case _: Exception => None
    //     }
    //   }
    
    subscriptions.toList
  }

  def readPosts(url: String): List[Option[Post]] = {
    val source = Source.fromURL(url)
    val jsonContent = try {source.mkString} finally {source.close()}
    implicit val formats: Formats = DefaultFormats

    val json = parse(jsonContent)
    try {
      val children = (json \ "data" \ "children").extract[List[JValue]]
      children.map { child =>
        val data = child \ "data"
        val title = (data \ "title").extract[String]
        val selftext = (data \ "selftext").extract[String]
        val autor = (data \ "autor").extract[String]
        (title, selftext, autor)
      }.toList.map(Some(_))
    } catch {
      case _: Exception => List(None)
    }
  }

  // FUNCIONES AUXILIARES PARA IMPRIMIR UN POST Y UN SUBSCRIPTION
  // PUEDEN SER MODIFICADAS PARA AGREGAR OPTION Y COMPLETAR EL EJERCICIO 3
  def printPost(post: Post): Unit = {
    val content = post._2
    val truncatedContent = if (content.length > 80) content.take(80) else content
    println(s"${post._1} by **${post._3}**")
    println(truncatedContent)
    println("-----------------------")
  }
  // def printPost(post: Post): Unit = {
  //   val content = post._2
  //   val words = countCensoredWords(content)
  //   val truncatedContent = if (content.length > 80) content.take(80) else content
  //   println(s"Title: \"${post._1}\" by **${post._3}**")
  //   println(s"Content: $truncatedContent")
  //   println(s"Censored words: $words")
  //   println("-----------------------")
  // }
  
  def printSubscription(posts: (String, List[Post])): Unit = {
    val url = posts._1
    println(s"Posts from: $url")
    posts._2.map(printPost)
  }
  // def printSubscription(posts: List[Post]): Unit = {
  //   posts.map(printPost)
  // }

  // Main function to run
  def main(args: Array[String]): Unit = {
    val header = s"Reddit Post Parser\n${"=" * 40}"

    println("=======================")
    println("EJ1: LEER SUSCRIPCIONES")
    val subscriptions: List[Option[Subscription]] = readSubscriptions("subscriptions.json")

    // Print subscriptions read - We can use imperative for I/O
    for (subscription <- subscriptions) {
      println(subscription)
    }

    println("=======================")
    println("")
    println("=======================")
    println("EJ2: DESCARGAR POSTS")

    // Descargar y parsear los posts
    val allPosts: List[Post] = subscriptions.flatten.flatMap { sub =>
        println(sub)
        readPosts(sub._2).flatten
      }

    // var allPosts: List[String] = List.empty
    // for (subscription <- subscriptions) {
    //   subscription.foreach { sub =>
    //     var postTitles = readPosts(sub._2)
    //     allPosts = allPosts ++ postTitles
    //   }
    // }
    // val allPosts: List[Post] = subscriptions.flatten.flatMap { subscription =>
    //   println(subscription)
    //   readPosts(subscription._2).flatten
    // }

    println("=======================")
    println("")
    println("=======================")
    println("EJ3: IMPRIMIR POSTS Y CONTEO DE PALABRAS CENSURADAS")

    // Print final results
    allPosts.map(printPost)
    // printSubscrciption(allPosts)
    println("=======================")
  }
}