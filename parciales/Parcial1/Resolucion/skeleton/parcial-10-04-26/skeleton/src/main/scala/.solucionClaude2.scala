import org.json4s._
import org.json4s.jackson.JsonMethods._
import scala.io.Source


object Main {  
  
  //EJ 1
  type Subscription = (String, String, Option[Int], Option[String])

  def parseSubscription(item: JValue): Option[Subscription] = {
    implicit val formats: Formats = DefaultFormats

    val nameOpt = (item \ "name").extractOpt[String]
    val urlOpt  = (item \ "url").extractOpt[String]
    val countOpt = (item \ "count").extractOpt[Int]
    val beforeOpt = (item \ "before").extractOpt[String]

    for {
      name <- nameOpt
      url  <- urlOpt
    } yield (name, url, countOpt, beforeOpt)
  }

  def readSubscriptions(path: String): List[Subscription] = {
    val source = Source.fromFile(path)
    val jsonString = try { source.mkString } finally { source.close() }

    implicit val formats: Formats = DefaultFormats
    val json = parse(jsonString)
    val rawList = json.extract[List[JValue]]

    rawList.flatMap(parseSubscription)
  }

  //EJ 2
  def buildFeedUrl(subscription: Subscription): String = {
    val (_, url, countOpt, beforeOpt) = subscription

    val queryParams = List(
      countOpt.map(c => s"count=$c"),
      beforeOpt.map(b => s"before=$b")
    ).flatten

    if (queryParams.isEmpty) url
    else s"$url?${queryParams.mkString("&")}"
  }
  def fetchFeed(url: String): Option[String] = {
    try {
      val source = Source.fromURL(url)
      val content = try { source.mkString } finally { source.close() }
      Some(content)
    } catch {
      case _: Exception => None
    }
  }

  //Ej 3
  //a
  type Post = (String, String, String) // (title, selftext, author)

  def readPosts(subscription: Subscription): List[Post] = {
    val url = buildFeedUrl(subscription)

    fetchFeed(url) match {
      case None => List.empty
      case Some(jsonContent) =>
        implicit val formats: Formats = DefaultFormats
        val json = parse(jsonContent)
        val children = (json \ "data" \ "children").extract[List[JValue]]

        children.map { child =>
          val data = child \ "data"
          val title = (data \ "title").extractOrElse[String]("")
          val selftext = (data \ "selftext").extractOrElse[String]("")
          val author = (data \ "author").extractOrElse[String]("unknown")
          (title, selftext, author)
        }
    }
  }

  //b
  val censoredWords = Set(
    "llm", "llms", "ai", "chatgpt", "copilot",
    "claude", "ml", "gemini", "agent", "agentic"
  )

  def countCensoredWords(text: String): Int = {
    text
      .split("\\s+")
      .map(_.toLowerCase.replaceAll("[^a-z0-9]", ""))
      .count(word => censoredWords.contains(word))
  }

  def countCensoredInPost(post: Post): Int = {
    val (title, selftext, _) = post
    countCensoredWords(title) + countCensoredWords(selftext)
  }

  //c
  def printPost(post: Post): Unit = {
    val (title, selftext, author) = post
    val truncatedContent =
      if (selftext.length > 80) selftext.take(80) else selftext
    val censored = countCensoredInPost(post)

    println(s"\t$title by **$author**")
    println(s"\tContenido: $truncatedContent")
    println(s"\tPalabras censuradas: $censored")
    println("\t-----------------------")
  }

  def printSubscription(subscription: Subscription, posts: List[Post]): Unit = {
    val url = buildFeedUrl(subscription)
    println(s"Posts from: $url")
    posts.foreach(printPost)
  }


  def main(args: Array[String]): Unit = {
    println("=======================")
    println("EJ1: LEER SUSCRIPCIONES")
    val subscriptions: List[Subscription] = readSubscriptions("subscriptions.json")
    subscriptions.foreach { sub =>
      println(buildFeedUrl(sub))
    }

    println("=======================")
    println("EJ2: DESCARGAR POSTS")
    // (este print de URLs se repite en el output esperado del parcial)
    subscriptions.foreach { sub =>
      println(buildFeedUrl(sub))
    }

    println("=======================")
    println("EJ3: IMPRIMIR POSTS Y PALABRAS CENSURADAS")
    subscriptions.foreach { sub =>
      val posts = readPosts(sub)
      printSubscription(sub, posts)
    }
    println("=======================")
  }
}