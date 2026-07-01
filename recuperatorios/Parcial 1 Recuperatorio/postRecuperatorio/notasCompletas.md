# 📌 Recordatorio de Scala: Colecciones y Funciones de Transformación

Guía rápida y práctica sobre los métodos esenciales de colecciones (`List`, `Seq`, `Option`, etc.) para tener a mano mientras programás.

---

## ⚡ map vs flatMap en 30 segundos

* **`map`**: Transforma cada elemento, preservando la estructura. Si tenés una lista de $N$ elementos, obtenes una lista de $N$ resultados.
```scala
List(1, 2, 3).map(x => List(x, x))
// Resultado: List(List(1,1), List(2,2), List(3,3))  ← lista de listas
```

* **`flatMap`**: Transforma cada elemento y aplana un nivel. Ideal cuando cada elemento produce una lista y querés una sola lista al final.
```scala
List(1, 2, 3).flatMap(x => List(x, x))
// Resultado: List(1, 1, 2, 2, 3, 3)  ← lista plana
```

> 💡 **Caso real de tu código:** Cada suscripción produce una `List[Post]` (muchos posts o vacía). Con `map` obtendrías una `List[List[Post]]` —inutilizable directamente—. Con `flatMap` obtenés una `List[Post]` directamente, que es lo que necesitás.

---

## 🔄 1. map
Transforma cada elemento de una colección de forma individual. 
* Devuelve una **nueva colección con el mismo tamaño** que la original.
* Cambia el contenido, pero mantiene la estructura original.

```scala
val numeros = List(1, 2, 3)
val cuadrados = numeros.map(x => x * x)
// Resultado: List(1, 4, 9)
```

---

## 🔨 2. flatten
"Aplasta" o aplana una colección de colecciones. No transforma los datos, solo cambia la estructura.

### 🔍 Uso con colecciones anidadas:
Toma una estructura de dos niveles (como una lista de listas) y une los elementos en un **solo nivel**.
```scala
val listasAnidadas = List(List(1, 2), List(3, 4))
val aplanada = listasAnidadas.flatten
// Resultado: List(1, 2, 3, 4)
```

### 🔍 Uso con `Option` (Limpieza de datos):
Es **muy usado para limpiar listas que contienen `Option`**, eliminando los `None` y extrayendo el valor de los `Some` automáticamente (como hacés en `readPosts(url).flatten`).
```scala
val postsConErrores: List[Option[Post]] = List(Some(("Titulo 1", "Alice")), None, Some(("Titulo 2", "Bob")))
val postsLimpios: List[Post] = postsConErrores.flatten
// Resultado: List(("Titulo 1", "Alice"), ("Titulo 2", "Bob"))  ← Los 'None' desaparecen
```

---

## 🧩 3. flatMap
Es la combinación exacta de hacer un `map` y después un `flatten` en un solo paso.
* Se usa cuando la función que aplicás a cada elemento devuelve otra colección o un `Option`.
* Evita que el resultado final quede con niveles anidados innecesarios.

### 🔍 Ejemplo comparativo con Strings:
```scala
val frases = List("hola mundo", "scala es genial")

// Con MAP (crea una lista de arrays/listas)
val resultadoMap = frases.map(frase => frase.split(" "))
// Resultado: List(Array("hola", "mundo"), Array("scala", "es", "genial"))

// Con FLATMAP (crea una sola lista plana con todos los elementos)
val resultadoFlatMap = frases.flatMap(frase => frase.split(" "))
// Resultado: List("hola", "mundo", "scala", "es", "genial")
```

---

## 🗂️ 4. groupBy
Agrupa los elementos de una colección según una función criterio. Devuelve un **`Map[Clave, List[Elemento]]`**.

### 🔍 Ejemplo práctico (Ranking de Autores de tu código):
Para contar cuántos posts tiene cada autor en `authorRanking`:
```scala
type Post = (String, String) // (title, author)
val posts: List[Post] = List(("Post A", "Alice"), ("Post B", "Bob"), ("Post C", "Alice"))

// 1. Agrupamos por el autor: post._2 es el autor en la tupla
val agrupados: Map[String, List[Post]] = posts.groupBy(post => post._2)
// Resultado: Map("Alice" -> List(("Post A", "Alice"), ("Post C", "Alice")), "Bob" -> List(("Post B", "Bob")))

// 2. Transformamos el Map para contar los elementos de cada lista (.mapValues)
val ranking: Map[String, Int] = agrupados.mapValues(_.length).toMap
// Resultado: Map("Alice" -> 2, "Bob" -> 1)
```

---

## 🚀 5. foreach
Aplica una función a cada elemento de la colección, pero **no devuelve nada (`Unit`)**. 
* Se usa **únicamente por sus efectos secundarios** (como un `println`, guardar en base de datos o escribir logs). No sirve para transformar datos de la lista.

```scala
posts.foreach { case (title, author) =>
  println(s"  - \({author}:\){title}")
}
// Imprime en consola, pero no genera una nueva lista.
```

---

## 🔄 6. toList
Convierte cualquier tipo de colección iterable (un `Array`, un `Set`, secuencias nativas de Java o los elementos internos de librerías como *Json4s*) en una `List` estricta de Scala. Asegura que puedas usar toda la API funcional.

```scala
val subscriptions = json.extract[List[Map[String, Any]]].map { ... }
subscriptions.toList // Asegura el tipo final rígido List[Option[Subscription]]
```

---

## 🔥 7. Bonus: collect
Permite **filtrar y transformar** elementos en un solo paso usando *Pattern Matching*. Es una alternativa súper elegante cuando querés mapear solo ciertos elementos y descartar el resto (como los `None`).

```scala
val subscriptions: List[Option[Subscription]] = readSubscriptions("subscriptions.json")

// Filtra los 'None' automáticamente y mapea solo los 'Some'
val validas = subscriptions.collect {
  case Some((name, url)) => s"Procesando \$name"
}
```

---

## 💡 Resumen Rápido para Programar
* **¿Modificar datos conservando el tamaño?** ➡️ `map`
* **¿Modificar datos y aplanar el resultado?** ➡️ `flatMap`
* **¿Quitar los `None` de una lista de `Option`?** ➡️ `flatten`
* **¿Clasificar/Agrupar elementos por una propiedad?** ➡️ `groupBy`
* **¿Hacer un `println` o efectos secundarios?** ➡️ `foreach`
