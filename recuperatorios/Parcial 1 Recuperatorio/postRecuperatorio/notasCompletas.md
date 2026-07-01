# 📌 Recordatorio de Scala: Colecciones y Transformación de Datos

Guía rápida de referencia sobre métodos de colecciones (`List`, `Map`, `Option`). Diseñada con ejemplos conceptuales y casos de uso basados en el Parser de Reddit.

---

## ⚡ map vs flatMap en 30 segundos

* **`map`**: Transforma cada elemento, preservando la estructura. Si tenés una lista de $N$ elementos, obtenés una lista de $N$ resultados.
```scala
List(1, 2, 3).map(x => List(x, x))
// Resultado: List(List(1,1), List(2,2), List(3,3))  ← lista de listas
```

* **`flatMap`**: Transforma cada elemento y aplana un nivel. Ideal cuando cada elemento produce una lista y querés una sola lista al final.
```scala
List(1, 2, 3).flatMap(x => List(x, x))
// Resultado: List(1, 1, 2, 2, 3, 3)  ← lista plana
```

> 💡 **En tu código:** Cada suscripción produce una `List[Post]`. Con `map` obtendrías una `List[List[Post]]` (difícil de iterar). Con `flatMap` procesás las suscripciones y obtenés directamente una `List[Post]` unificada.

---

## 🔄 1. map
Transforma cada elemento de una colección de forma individual. Mantiene el tamaño original de la estructura.

* **Ejemplo Genérico:**
  ```scala
  List(1, 2, 3).map(x => x * x) 
  // Resultado: List(1, 4, 9)
  ```
* **En tu código (Extracción JSON):**
  Transforma una lista de mapas crudos en una lista de opciones de suscripción:
  ```scala
  val subscriptions = json.extract[List[Map[String, Any]]].map { subscriptionMap =>
    // Transforma cada mapa en un Option[(String, String)]
    Some((name, url))
  }
  ```

---

## 🔨 2. flatten
"Aplasta" colecciones anidadas eliminando un nivel de estructura. En el caso de `Option`, descarta los `None` y extrae los valores de los `Some`.

* **Ejemplo Genérico:**
  ```scala
  List(List(1, 2), List(3, 4)).flatten // Resultado: List(1, 2, 3, 4)
  List(Some("A"), None, Some("B")).flatten // Resultado: List("A", "B")
  ```
* **En tu código (Limpieza de Posts vacíos):**
  Elimina los errores de red o parseo descifrados como `None`:
  ```scala
  val posts: List[Post] = readPosts(url).flatten // List[Option[Post]] -> List[Post]
  ```

---

## 🧩 3. flatMap
Aplica un `map` y ejecuta un `flatten` inmediatamente después. Evita el anidamiento innecesario.

* **Ejemplo Genérico:**
  ```scala
  val frases = List("hola mundo", "scala")
  frases.flatMap(frase => frase.split(" ")) 
  // Resultado: List("hola", "mundo", "scala")
  ```
* **En tu código (Acumulador de posts globales):**
  Descarga posts de múltiples urls y los une a todos en una sola lista plana:
  ```scala
  val allPosts: List[Post] = subscriptions.flatMap {
    case None => List.empty
    case Some((name, url)) => readPosts(url).flatten // Retorna List[Post]
  }
  ```

---

## 🗂️ 4. groupBy
Agrupa los elementos bajo una clave generada por una función criterio. Retorna un `Map[Clave, List[Elemento]]`.

* **Ejemplo Genérico:**
  ```scala
  val palabras = List("apple", "bar", "apricot")
  palabras.groupBy(p => p.head) 
  // Resultado: Map('a' -> List("apple", "apricot"), 'b' -> List("bar"))
  ```
* **En tu código (Agrupación por Autor):**
  Agrupa los posts usando el segundo elemento de la tupla `(title, author)`:
  ```scala
  val agrupado = posts.groupBy(post => post._2) 
  // Resultado: Map("AutorNombre" -> List((Title1, AutorNombre), (Title2, AutorNombre)))
  ```

---

## 📉 5. sortBy
Ordena los elementos de una colección basándose en una función que genera una clave de ordenamiento (Soporta orden inverso anteponiendo el signo menos `-`).

* **Ejemplo Genérico:**
  ```scala
  val nombres = List("Ana", "Beatriz", "Camilo")
  nombres.sortBy(n => n.length) // Ordena por longitud (menor a mayor)
  ```
* **En tu código (Ranking de más activos):**
  Convierte el mapa de autores a lista de tuplas `(autor, cantidad)` y los ordena de **mayor a menor** usando el valor negativo de la cantidad (`-count`):
  ```scala
  ranking.toList.sortBy { case (_, count) => -count }
  ```

---

## ✂️ 6. take
Extrae los primeros $N$ elementos de una colección y descarta el resto. Es seguro de usar aunque el tamaño de la lista sea menor al número solicitado.

* **Ejemplo Genérico:**
  ```scala
  List(10, 20, 30, 40).take(2) // Resultado: List(10, 20)
  ```
* **En tu código (Top Autores):**
  Secciona la lista ya ordenada para conservar únicamente los primeros 3 puestos del ranking:
  ```scala
  ranking.toList.sortBy(...).take(3) // Obtiene el Top 3
  ```

---

## 🚀 7. foreach
Aplica una función a cada elemento con el único objetivo de generar un efecto secundario (Escribir en consola, guardar en base de datos, etc). Retorna `Unit` (nada).

* **Ejemplo Genérico:**
  ```scala
  List("A", "B").foreach(x => println(x))
  ```
* **En tu código (Consola logs):**
  Muestra las publicaciones en la consola sin alterar las listas de datos:
  ```scala
  posts.foreach { case (title, author) =>
    println(s"  - author: title")
  }
  ```

---

## 📏 8. length y size
Retornan la cantidad total de elementos de una colección. En la mayoría de las colecciones de Scala se pueden usar indistintamente.

* **Ejemplo Genérico:**
  ```scala
  List(1, 2, 3).length // 3
  Map("A" -> 1).size   // 1
  ```
* **En tu código (Métricas):**
  ```scala
  println(s"Total de posts descargados: \${allPosts.length}")
  // Usado en map para contar los posts de un autor: (author, posts.size)
  ```

---

## 🔄 9. toList
Fuerza la conversión de cualquier estructura iterable o colecciones externas a una `List` estructurada de Scala.

* **Ejemplo Genérico:**
  ```scala
  Set(1, 2, 2).toList // Resultado: List(1, 2)
  ```
* **En tu código (Compatibilidad de APIs):**
  ```scala
  subscriptions.toList // Asegura tipo rígido final tras el casteo del jsonString
  ranking.toList       // Transforma el Map a List[(String, Int)] para poder usar sortBy
  ```

---

## 🔥 10. Bonus Pro: collect
Filtra y transforma elementos simultáneamente usando *Pattern Matching*. Ignora cualquier elemento que no coincida con los casos definidos.

* **Ejemplo Genérico:**
  ```scala
  val datos = List(Some(10), None, Some(20))
  val duplicados = datos.collect { case Some(n) => n * 2 } 
  // Resultado: List(20, 40) (Los 'None' se ignoran automáticamente)
  ```

---

## 💡 Acordeón de Decisiones Rápidas:
* ¿Modificar datos manteniendo el mismo tamaño de lista? ➡️ **`map`**
* ¿Modificar datos de elementos que devuelven listas y unificar todo? ➡️ **`flatMap`**
* ¿Quitar los `None` o desempaquetar listas de listas? ➡️ **`flatten`**
* ¿Clasificar elementos generando categorías/grupos? ➡️ **`groupBy`**
* ¿Ordenar una lista bajo un criterio numérico o alfabético? ➡️ **`sortBy`**
* ¿Quedarse con un número fijo de elementos iniciales? ➡️ **`take`**
* ¿Hacer un `println` o registrar logs sin alterar nada? ➡️ **`foreach`**
