/**
 * Responsable de cargar colecciones de entidades nombradas desde archivos.
 *
 * Un diccionario es un archivo de texto plano donde cada línea contiene
 * el nombre de una entidad conocida del mismo tipo.
 */
object Dictionary {

  /**
   * Lee un archivo de diccionario y crea una lista de entidades del tipo indicado.
   *
   * @param filePath   ruta al archivo de diccionario (ej: "data/people.txt")
   * @param entityType tipo de entidad: "Person", "University", "ProgrammingLanguage", etc.
   * @return lista de NamedEntity del tipo correspondiente
   */
  def loadFromFile(filePath: String, entityType: String): List[NamedEntity] = {
    FileIO.readLines(filePath).map { name =>
      entityType match {
        case "Person"              => new Person(name)
        case "Organization"        => new Organization(name)
        case "University"          => new University(name)
        case "Place"               => new Place(name)
        case "Technology"          => new Technology(name)
        case "ProgrammingLanguage" => new ProgrammingLanguage(name)
      }
    }
  }

  /**
   * Carga todos los diccionarios disponibles y combina sus entidades.
   *
   * @return lista con todas las entidades de todos los diccionarios
   */
  // Ej 1 b) loadAll reemplazado por loadFromConfig
  def loadFromConfig(configPath: String): List[NamedEntity] = {
    FileIO.readLines(configPath).map { linea =>
      // val link = "(?i)(?<![a-zA-Z0-9])" + java.util.regex.Pattern.quote(linea) + "="
      // val entidad = "=" + java.util.regex.Pattern.quote(linea)
      val dir = linea.substring(0, linea.indexOf('=')).toString
      var ent = linea.substring(linea.indexOf('=') + 1).toString
      loadFromFile(dir, ent)
      // match case (dir.isDefined && ent.isDefined) {
      // }
    }.toList.flatten
  }
}
