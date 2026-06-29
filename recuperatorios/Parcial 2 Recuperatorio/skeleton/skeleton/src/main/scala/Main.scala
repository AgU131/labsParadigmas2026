object Main {
  // Ej 1 c)
  def findByType(dictionary: List[NamedEntity], entityType: String): List[NamedEntity] = {
    dictionary.filter(entity => entityType.contains(entity.entityType))
  }

  def main(args: Array[String]): Unit = {
    //Ej 1 d) usar loadFromConfig y imprimir entidades x tipo
    val dictionary: List[NamedEntity] = Dictionary.loadFromConfig("data/dictionaries.conf")

    println(s"Diccionario cargado: ${dictionary.size} entidades\n")
    // val detectadas = detectEntities(dictionary)
    // println(s"Se cargaron: ${countByType(detectadas)} entidades por tipo\n")

    val subscriptions = FileIO.readSubscriptions()

    val allDetected: List[NamedEntity] = subscriptions.flatMap { url =>
      println(s"Descargando posts de: $url")
      val json   = FileIO.downloadFeed(url)
      val titles = FileIO.extractPostTitles(json)
      println(s"\n${"=" * 60}\n$url\n${"=" * 60}")
      titles.flatMap { title =>
        val entities = Analyzer.detectEntities(title, dictionary)
        // val cantDetectadas = Analyzer.countByType(entities)
        // println(Formatters.formatEntityStatsNum(cantDetectadas))
        println(Formatters.formatNERResult(title, entities))
        entities
      }
    }
  
    println(s"\n${Formatters.formatEntityStats(Analyzer.countByType(allDetected))}")
  }
}
