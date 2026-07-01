import org.json4s._
import org.json4s.jackson.JsonMethods._
import scala.io.Source


object Main {
  // Define `Subscription` as a simple tuple type alias
  type Subscription = (String, String)  // (name, url)
  type Post = (String, String)          // (title, author)

    // Pure function to read subscriptions from a JSON file
  def readSubscriptions(path: String): List[Option[Subscription]] = {
    try {
      val source = Source.fromFile(path)
      val jsonString = source.mkString
      source.close
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
    catch {
      case _: Exception => List.empty
    }
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

  //Funcion Auxiliar: Calcula un ranking de los autores más activos (nombre autor, cantidad posts)
  def authorRanking(posts: List[Post]): Map[String, Int] = {
    val agrupado = posts.groupBy(post => post._2)   //agrupa los posts por autor, Post=(title, author)
    agrupado.map {
      case (author, posts) =>
        (author, posts.size)
    }
  }
  //Imprimir los autores
  def printTopAuthors(ranking: Map[String, Int]): Unit = {  //solo top 3
    println("=== Top autores ===")
    ranking.toList
      .sortBy { case (_, count) => -count }
      .take(3)    //aqui sacamos el top 3
      .foreach { case (author, count) => println(s"- $author: $count") }
  }
  //con top n
  // def printTopAuthors(ranking: Map[String, Int], n: Int): Unit = {
  //   println("=== Top autores ===")
  //   ranking.toList
  //     .sortBy { case (_, count) => -count }
  //     .take(n)
  //     .foreach { case (author, count) => println(s"- $author: $count") }
  // }

  // Main function to run
  def main(args: Array[String]): Unit = {
    val header = s"Reddit Post Parser\n${"=" * 40}"

    val subscriptions: List[Option[Subscription]] = readSubscriptions("subscriptions.json")

    val allPosts: List[Post] = subscriptions.flatMap {    // flatMap hace todo en un solo paso — descarga, imprime, y acumula los posts para allPosts
      case None => List.empty
      case Some((name, url)) =>              // == Some(subscription)
        println(s"Descargando posts de: $name")
        val posts = readPosts(url).flatten          //: List[Option[Post]]
        posts.foreach {   // foreach (imprime cada post) es la herramienta correcta cuando el único propósito es un efecto secundario (imprimir) y no necesitás transformar nada
          case (title, author) =>
            println(s"  - ${author}: ${title}")
        }
        posts
    }

    println("=======================")
    println(s"Total de posts descargados: ${allPosts.length}")
    println("=======================")

    val ranking = authorRanking(allPosts)
    printTopAuthors(ranking)
    //printTopAuthors(ranking, 3)  //con top n
  }
}
