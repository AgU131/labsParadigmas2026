import org.json4s._
import org.json4s.jackson.JsonMethods._
import scala.io.Source


object Main {
  // Define `Subscription` as a simple tuple type alias
  type Subscription = (String, String)

  type Post = (String, String, String)

    // Pure function to read subscriptions from a JSON file
  def readSubscriptions(path: String): List[Option[Subscription]] = {
    val source = Source.fromFile(path)
    val jsonString = source.mkString
    implicit val formats: Formats = DefaultFormats

    val subscriptions = parse(jsonString).children.map {item =>
      try{
        val url = (item \ "url").extract[String]
        val name = (item \ "name").extract[String]
        val before = (item \ "before").extract[String]
        val count = (item \ "count").extract[Int]
        Some(name, (url+"?count="+count+"&before="+before))
      } catch {
        case _: Exception => None
      }
    }
    source.close()
    subscriptions.toList
  }

  def readPosts(url: String): List[Option[Post]] = {
    try {
      val source = Source.fromURL(url)
      val jsonContent = source.mkString
      implicit val formats: Formats = DefaultFormats

      val json = parse(jsonContent)
      val children = (json \ "data" \ "children").extract[List[JValue]]
      source.close()

      children.map { child =>
        val data = child \ "data"
        val title = (data \ "title").extract[String]
        val selftext = (data \ "selftext").extract[String]
        val author = (data \ "author").extract[String]
        (title, selftext, author)
      }.toList.map(Some(_))
    } catch {
      case _: Exception => return List(None)
    }
  }

  // FUNCIONES AUXILIARES PARA IMPRIMIR UN POST Y UN SUBSCRIPTION
  // PUEDEN SER MODIFICADAS PARA AGREGAR OPTION Y COMPLETAR EL EJERCICIO 3
  def printPost(post: Post): Unit = {
    val content = post._2
    val words = countCensoredWords(content)
    val truncatedContent = if (content.length > 80) content.take(80) else content
    println(s"Title: \"${post._1}\" by **${post._3}**")
    println(s"Content: $truncatedContent")
    println(s"Censored words: $words")
    println("-----------------------")
  }

  def countCensoredWords(content: String): Int = {
    val censored = Set("llm", "llms", "ai", "chatgpt", "copilot", "claude", "ml", "gemini", "agent", "agentic")
    content.split(" ").toList.count(word => censored.contains(word.toLowerCase))
  }

  def printSubscription(posts: List[Post]): Unit = {
    posts.map(printPost)
  }

  // Main function to run
  def main(args: Array[String]): Unit = {
    val header = s"Reddit Post Parser\n${"=" * 40}"

    println("=======================")
    println("EJ1: LEER SUSCRIPCIONES")
    val subscriptions: List[Option[Subscription]] = readSubscriptions("subscriptions.json")

    // Print subscriptions read - We can use imperative for I/O
    for (subscription <- subscriptions.flatten) {
      println(subscription)
    }

    println("=======================")
    println("")
    println("=======================")
    println("EJ2: DESCARGAR POSTS")

    // Descargar y parsear los posts
    val allPosts: List[Post] = subscriptions.flatten.flatMap { subscription =>
      println(subscription)
      readPosts(subscription._2).flatten
    }

    println("=======================")
    println("")
    println("=======================")
    println("EJ3: IMPRIMIR POSTS Y CONTEO DE PALABRAS CENSURADAS")

    // Print final results
    printSubscription(allPosts)
    println("=======================")
  }
}