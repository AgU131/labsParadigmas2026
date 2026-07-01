# 📌 Recordatorio de Scala: Transformación de Colecciones

Guía rápida sobre las funciones de transformación (`map`, `flatMap`, `groupBy`, etc.) indispensables para el día a día en Scala.

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

> 💡 **Caso real de uso:** Cada suscripción produce una `List[Post]` (muchos posts o vacía). Con `map` obtendrías una `List[List[Post]]` —inutilizable directamente—. Con `flatMap` obtenés una `List[Post]` directamente, que es lo que necesitás.

---

## 🗂️ 1. groupBy
Agrupa los elementos de una colección según una función criterio. Devuelve un **`Map[Clave, List[Elemento]]`**.

### 🔍 Ejemplo real (Ranking de Autores):
Para contar cuántos posts tiene cada autor (completando tu función `authorRanking`):
```scala
type Post = (String, String) // (title, author)
val posts: List[Post] = List(("Post A", "Alice"), ("Post B", "Bob"), ("Post C", "Alice"))

// 1. Agrupamos por el autor: post._2 es el autor en la tupla
val agrupados: Map[String, List[Post]] = posts.groupBy(post => post._2)
// Resultado: Map("Alice" -> List(("Post A", "Alice"), ("Post C", "Alice")), "Bob" -> List(("Post B", "Bob")))

// 2. Transformamos el Map para contar los elementos de cada lista con .mapValues
val ranking: Map[String, Int] = agrupados.mapValues(_.length).toMap
// Resultado: Map("Alice" -> 2, "Bob" -> 1)
```

---

## 🔨 2. flatten (con Option)
"Aplasta" estructuras anidadas. Además de listas de listas, es **muy usado para limpiar listas que contienen `Option`**, eliminando los `None` y extrayendo el valor de los `Some`.

### 🔍 Ejemplo real:
En tu función `readPosts(url).flatten` pasás de tener `List[Option[Post]]` a tener una limpia `List[Post]`:
```scala
val postsConErrores: List[Option[Post]] = List(Some(("Titulo 1", "Alice")), None, Some(("Titulo 2", "Bob")))
val postsLimpios: List[Post] = postsConErrores.flatten
// Resultado: List(("Titulo 1", "Alice"), ("Titulo 2", "Bob"))  ← Los 'None' desaparecen automáticamente
```

---

## 🚀 3. foreach
Aplica una función a cada elemento de la colección, pero **no devuelve nada (`Unit`)**. 
* Se usa **únicamente por sus efectos secundarios** (hacer un `println`, guardar en base de datos, enviar un log). No sirve para transformar datos.

```scala
posts.foreach { case (title, author) =>
  println(s"  - \({author}:\){title}")
}
// Imprime en consola, pero no genera una nueva lista.
```

---

## 🔄 4. toList
Convierte cualquier tipo de colección iterable (un `Array`, un `Set`, una secuencia de Java o los elementos internos de JSON4s) en una `List` de Scala. Es ideal para asegurar que podés usar métodos funcionales como `map` o `flatMap`.

```scala
val subscriptions = json.extract[List[Map[String, Any]]].map { ... }
subscriptions.toList // Asegura el tipo final rígido List[Option[Subscription]]
```

---

## 🔥 5. Bonus: collect (Alternativa Pro)
En tu código hacés un `.map` que devuelve `Some/None` y luego un `.flatten`. En Scala existe **`collect`**, que permite filtrar y transformar elementos en un solo paso usando *Pattern Matching*.

Si tenés una `List[Option[T]]` y solo querés los que son válidos:
```scala
val subscriptions: List[Option[Subscription]] = readSubscriptions("subscriptions.json")

// En lugar de usar flatMap + pattern matching manual:
val validas = subscriptions.collect {
  case Some((name, url)) => s"Procesando \$name"
}
// Filtra los 'None' automáticamente y mapea solo los 'Some'
```

---

## 💡 Resumen Rápido para Programar:
* **¿Modificar datos conservando el tamaño?** ➡️ `map`
* **¿Modificar datos y aplanar el resultado?** ➡️ `flatMap`
* **¿Quitar los `None` de una lista de `Option`?** ➡️ `flatten`
* **¿Clasificar/Agrupar elementos por una propiedad?** ➡️ `groupBy`
* **¿Hacer un `println` o guardar logs sin alterar la lista?** ➡️ `foreach`
