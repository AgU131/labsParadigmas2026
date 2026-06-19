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

  // Ej 1
  def matches(text: String): Boolean = {
    val pattern = "(?i)(?<![a-zA-Z0-9])" + java.util.regex.Pattern.quote(this.text) + "(?![a-zA-Z0-9])"
    pattern.r.findFirstIn(text).isDefined
  }
  // def matches(text: String): Boolean = {
  //   val quoted = Regex.quote(this.text)
  //   val pattern = s"(?i)(?<![a-zA-Z0-9])$quoted(?![a-zA-Z0-9])".r
  //   pattern.findFirstIn(text).isDefined
  // }

  // Ej 2
  def isRelevant: Boolean = true

}

class Person(text: String) extends NamedEntity(text) {
  def entityType: String = "Person"
  override def matches(text: String): Boolean = {
    text.contains(this.text)
  }
}

class Organization(text: String) extends NamedEntity(text) {
  def entityType: String = "Organization"
  override def isRelevant: Boolean = false
}

class University(text: String) extends Organization(text) {
  override def entityType: String = "University"
  override def isRelevant: Boolean = true
}

class Place(text: String) extends NamedEntity(text) {
  def entityType: String = "Place"
  override def isRelevant: Boolean = false
}

class Technology(text: String) extends NamedEntity(text) {
  override def entityType: String = "Technology"
  override def matches(text: String): Boolean = {
    val quoted = java.util.regex.Pattern.quote(this.text)
    val pattern = s"(?<![a-zA-Z0-9])$quoted(?![a-zA-Z0-9])".r
    pattern.findFirstIn(text).isDefined
  }
  override def isRelevant: Boolean = false
}

class ProgrammingLanguage(text: String) extends Technology(text) {
  override def entityType: String = "ProgrammingLanguage"
  override def isRelevant: Boolean = true
}

// Ej 2
abstract class Event(text: String) extends NamedEntity(text) {
  // No implementa entityType: queda abstracto, heredado de NamedEntity
}

class Conference(text: String) extends Event(text) {
  override def entityType: String = "Conference"
  // No sobreescribe isRelevant → hereda `true` de NamedEntity
  // No sobreescribe matches → hereda el matching con boundary check, case-insensitive
}