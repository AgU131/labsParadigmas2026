/**
 * Responsable de convertir los resultados del análisis a texto para mostrar.
 */
object Formatters {

  /**
   * Formatea el análisis NER de un post individual.
   *
   * @param postTitle título del post analizado
   * @param entities  entidades detectadas en ese post
   * @return bloque de texto con el título y las entidades encontradas
   */
  def formatNERResult(postTitle: String, entities: List[NamedEntity]): String = {
    val header = s"""Post: "$postTitle"\nEntidades detectadas:"""
    val body =
      if (entities.isEmpty) "  (sin entidades detectadas)"
      else entities.map(e => s"  ${e.describe}").mkString("\n")
    s"$header\n$body"
  }

  /**
   * Formatea un resumen de estadísticas de entidades por tipo.
   *
   * @param counts mapa de entityType → cantidad
   * @return texto con las estadísticas ordenadas por cantidad (de mayor a menor)
   */
  def formatEntityStats(counts: Map[String, Int]): String = {
    val lines = counts.toList
      .sortBy(-_._2)
      .map { case (entityType, count) => s"$entityType: $count" }
    ("=== Estadísticas de entidades ===" :: lines).mkString("\n")
  }

  // Ej3
  def formatGroupedNERResult(postTitle: String, entities: List[NamedEntity]): String = {
  val header = s"""Post: "$postTitle"\nEntidades detectadas:"""

  if (entities.isEmpty) {
    s"$header\n  (sin entidades detectadas)"
  } else {
    val grouped: Map[String, List[NamedEntity]] = entities.groupBy(_.entityType)  //lo mismo que countByType, pero en vez de quedarnos con el .size, nos quedamos con la lista completa de entidades de ese tipo.

    val groupBlocks: List[String] = grouped.toList       //convertimos el Map a List[(String, List[NamedEntity])] para poder ordenarlo 
      .sortBy { case (entityType, _) => entityType }          // orden alfabético por tipo
      .map { case (entityType, ents) =>
        val sortedEnts = ents.sortBy(_.text)                   // orden alfabético por text dentro del grupo
        val groupHeader = s"  $entityType (${sortedEnts.size}):"
        val groupBody = sortedEnts.map(e => s"    ${e.text}").mkString("\n")
        s"$groupHeader\n$groupBody"
      }

    s"$header\n${groupBlocks.mkString("\n")}"
  }
}
}
