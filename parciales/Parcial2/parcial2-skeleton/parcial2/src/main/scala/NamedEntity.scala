/**
 * Clase base abstracta para todas las entidades nombradas.
 *
 * Una entidad nombrada es una expresión del texto que refiere a un objeto
 * del mundo real (persona, lugar, organización, tecnología, etc.).
 *
 * @param text el texto tal como aparece en el corpus
 */
abstract class NamedEntity(val text: String) {

  /**
   * Retorna el tipo de la entidad como String.
   */
  def entityType: String

  /**
   * Retorna una línea de descripción de la entidad para el informe.
   */
  def describe: String = s"[$entityType] $text"

  // def matches(text: String): Boolean = {
  //   val quoted = Regex.quote(this.text)
  //   val pattern = s"(?i)(?<![a-zA-Z0-9])$quoted(?![a-zA-Z0-9])".r
  //   pattern.findFirstIn(text).isDefined
  // }
  def matches(text: String): Boolean = {
    val pattern = "(?i)(?<![a-zA-Z0-9])" + java.util.regex.Pattern.quote(this.text) + "(?![a-zA-Z0-9])"
    pattern.r.findFirstIn(text).isDefined
  }
}

class Person(text: String) extends NamedEntity(text) {
  def entityType: String = "Person"
  override def matches(text: String): Boolean = {
    text.contains(this.text)
  }
}

class Organization(text: String) extends NamedEntity(text) {
  def entityType: String = "Organization"
}

class University(text: String) extends Organization(text) {
  override def entityType: String = "University"
}

class Place(text: String) extends NamedEntity(text) {
  def entityType: String = "Place"
}

class Technology(text: String) extends NamedEntity(text) {
  override def entityType: String = "Technology"
  override def matches(text: String): Boolean = {
    val quoted = java.util.regex.Pattern.quote(this.text)
    val pattern = s"(?<![a-zA-Z0-9])$quoted(?![a-zA-Z0-9])".r
    pattern.findFirstIn(text).isDefined
  }
}

class ProgrammingLanguage(text: String) extends Technology(text) {
  override def entityType: String = "ProgrammingLanguage"
}
