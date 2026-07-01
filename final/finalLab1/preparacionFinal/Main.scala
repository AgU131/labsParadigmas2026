import org.json4s._
import org.json4s.jackson.JsonMethods._
import scala.io.Source

object Main {
  type Subscription = (String, String)  // (name, url)
  type Post = (String, String)          // (title, author)

  def readSubscriptions(path: String): List[Option[Subscription]] = {
    try {
      val source = Source.fromFile(path)
      val jsonString = try { source.mkString } finally { source.close() }
      implicit val formats: Formats = DefaultFormats
      val json = parse(jsonString)
      json.extract[List[Map[String, Any]]].map { item =>
        try {
          val name = item("name").toString
          val url  = item("url").toString
          Some((name, url))
        } catch {
          case _: Exception => None
        }
      }
    } catch {
      case _: Exception => List.empty
    }
  }

  def readPosts(url: String): List[Option[Post]] = {
    try {
      val source = Source.fromURL(url)
      val jsonContent = try { source.mkString } finally { source.close() }
      implicit val formats: Formats = DefaultFormats
      val json = parse(jsonContent)
      val children = (json \ "data" \ "children").extract[List[JValue]]
      children.map { child =>
        try {
          val data   = child \ "data"
          val title  = (data \ "title").extract[String]
          val author = (data \ "author").extract[String]
          Some((title, author))
        } catch {
          case _: Throwable => None
        }
      }
    } catch {
      case _: Throwable => List.empty
    }
  }

  // Ejercicio 1a
  def filterPosts(posts: List[Post], keyword: String): List[Post] = {
    posts.filter { case (title, _) =>
      title.toLowerCase.contains(keyword.toLowerCase)
    }
  }

  // Ejercicio 2a
  def wordFrequency(posts: List[Post]): Map[String, Int] = {
    posts
      .flatMap { case (title, _) => title.toLowerCase.split("\\s+").toList }
      .groupBy(word => word)
      .map { case (word, occurrences) => (word, occurrences.size) }
  }

  // Ejercicio 2b
  def topWords(freq: Map[String, Int], n: Int): List[(String, Int)] = {
    freq.toList
      .sortBy { case (_, count) => -count }
      .take(n)
  }

  // Ejercicio 3b
  def mostActiveSubscription(postsBySubscription: List[(String, List[Post])]): Option[(String, Int)] = {
    if (postsBySubscription.isEmpty) None
    else {
      val result = postsBySubscription
        .map { case (name, posts) => (name, posts.size) }
        .maxBy { case (_, count) => count }
      Some(result)
    }
  }

  def main(args: Array[String]): Unit = {
    val subscriptions = readSubscriptions("subscriptions.json")

    // Ejercicio 3a: postsBySubscription
    val postsBySubscription: List[(String, List[Post])] = subscriptions.flatMap {
      case Some((name, url)) =>
        println(s"Descargando posts de: $name")
        val posts = readPosts(url).flatten
        List((name, posts))
      case None =>
        println("Error: suscripción inválida.")
        List.empty
    }

    val allPosts: List[Post] = postsBySubscription.flatMap { case (_, posts) => posts }
    println(s"Total de posts: ${allPosts.length}")

    // Ejercicio 1b: filtrado por keyword
    val keyword = scala.io.StdIn.readLine("Ingrese palabra clave: ")
    val filtered = filterPosts(allPosts, keyword)
    println("=== Posts filtrados ===")
    filtered.foreach { case (title, author) =>
      println(s"- $author: $title")
    }

    // Ejercicio 2c: top 5 palabras
    val freq = wordFrequency(allPosts)
    val top5 = topWords(freq, 5)
    println("=== Top 5 palabras ===")
    top5.foreach { case (word, count) =>
      println(s"- $word: $count")
    }

    // Ejercicio 3c: suscripción más activa
    println("=== Suscripción más activa ===")
    mostActiveSubscription(postsBySubscription) match {
      case Some((name, count)) => println(s"$name: $count posts")
      case None                => println("No hay suscripciones cargadas.")
    }
  }
}