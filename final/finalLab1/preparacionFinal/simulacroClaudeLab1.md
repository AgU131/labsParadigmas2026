# Recuperatorio Simulado — Lab 1 2026

## Instrucciones

* Solo podés usar las librerías del `build.sbt` del esqueleto (`json4s`, `scala.io.Source`).
* Todo el código debe ser funcional puro: sin `var`, sin mutación, sin excepciones como mecanismo principal.
* No hay resource leaks: todo `Source` que se abre, se cierra.
* Tenés 90 minutos.
* Solo entregás `Main.scala`.

---

## Contexto

El esqueleto que te dan tiene este código base ya funcionando:

```scala
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

  def main(args: Array[String]): Unit = {
    val subscriptions = readSubscriptions("subscriptions.json")

    val allPosts: List[Post] = subscriptions.flatMap {
      case Some((name, url)) =>
        println(s"Descargando posts de: $name")
        readPosts(url).flatten
      case None =>
        println("Error: suscripción inválida.")
        List.empty
    }

    println(s"Total de posts: ${allPosts.length}")
  }
}
```

Y el archivo `subscriptions.json` contiene:

```json
[
  { "name": "Scala",   "url": "http://localhost:8123/r/scala/.json" },
  { "name": "Programming", "url": "http://localhost:8123/r/programming/.json" },
  { "name": "Subreddit sin url" }
]
```

---

## Ejercicio 1 — Filtrado de posts (3 puntos)

**a)** Implementar la función:

```scala
def filterPosts(posts: List[Post], keyword: String): List[Post]
```

Que retorne solo los posts cuyo título contiene la palabra clave, sin distinguir mayúsculas/minúsculas. Debe implementarse con funciones de orden superior, sin `var`.

*Ejemplo:*
```scala
filterPosts(allPosts, "scala")
// Retorna solo los posts cuyo título contiene "scala" o "Scala" o "SCALA"
```

**b)** Modificar `main` para que después de descargar todos los posts, pregunte al usuario una palabra clave por consola, filtre los posts con `filterPosts`, e imprima los resultados con el formato:

```text
Ingrese palabra clave: scala
=== Posts filtrados ===
- petrzapletal: This week in #Scala (Apr 27, 2026)
- eed3si9n: Scala 3.7 released
```

> **Pista:** para leer input del usuario podés usar `scala.io.StdIn.readLine()`.

---

## Ejercicio 2 — Estadísticas de títulos (3 puntos)

**a)** Implementar la función auxiliar:

```scala
def wordFrequency(posts: List[Post]): Map[String, Int]
```

Que cuente la frecuencia de cada palabra que aparece en los títulos de los posts, sin distinguir mayúsculas/minúsculas. Debe implementarse exclusivamente con funciones de orden superior (`map`, `flatMap`, `groupBy`, etc.), sin `var` ni mutación.

*Ejemplo:*
```scala
// Si los títulos son: "Scala is great", "Scala 3 released"
// wordFrequency devuelve:
// Map("scala" -> 2, "is" -> 1, "great" -> 1, "3" -> 1, "released" -> 1)
```

> **Pista:** para separar un string en palabras podés usar `.split("\\s+")`.

**b)** Implementar:

```scala
def topWords(freq: Map[String, Int], n: Int): List[(String, Int)]
```

Que retorne las `n` palabras más frecuentes, ordenadas de mayor a menor. Sin `var`.

**c)** Modificar `main` para imprimir al final las 5 palabras más frecuentes en todos los títulos, con el formato:

```text
=== Top 5 palabras ===
- scala: 8
- python: 5
- how: 4
- rust: 3
- new: 3
```

---

## Ejercicio 3 — Agrupación por suscripción (4 puntos)

Actualmente el sistema acumula todos los posts en una sola lista plana (`allPosts`), perdiendo información de qué suscripción vino cada post.

**a)** Modificar `main` para que en vez de una `List[Post]`, se construya:

```scala
val postsBySubscription: List[(String, List[Post])]
```

Donde cada elemento es `(nombreSuscripcion, listaDePosts)`. Debe hacerse funcionalmente, sin `var`.

**b)** Implementar:

```scala
def mostActiveSubscription(postsBySubscription: List[(String, List[Post])]): Option[(String, Int)]
```

Que retorne la suscripción con más posts como `Some((nombre, cantidad))`, o `None` si la lista está vacía. Sin `var`, con funciones de orden superior.

**c)** Modificar `main` para imprimir el resultado con el formato:

```text
=== Suscripción más activa ===
Scala: 12 posts
```

O si no hay suscripciones:

```text
=== Suscripción más activa ===
No hay suscripciones cargadas.
```

---

## Criterios de evaluación

Igual que el recuperatorio real:

* Funciones puras, sin `var`, sin mutación.
* Manejo de errores con `Option`, no con excepciones propagadas.
* Sin resource leaks.
* Uso correcto de funciones de orden superior (`map`, `flatMap`, `filter`, `groupBy`, `sortBy`, etc.).
