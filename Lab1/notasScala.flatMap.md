# 📌 Recordatorio de Scala: map, flatten y flatMap

Guía rápida sobre las tres funciones de transformación de colecciones (`List`, `Seq`, `Option`, `Future`, etc.).

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

## 1. 🔄 map
Transforma cada elemento de una colección de forma individual. 
* Devuelve una **nueva colección con el mismo tamaño** que la original.
* Cambia el contenido, pero mantiene la estructura.

```scala
val numeros = List(1, 2, 3)
val cuadrados = numeros.map(x => x * x)
// Resultado: List(1, 4, 9)
```

---

## 2. 🔨 flatten
"Aplasta" o aplana una colección de colecciones.
* Toma una estructura de dos niveles (como una lista de listas) y une los elementos en un **solo nivel**.
* No transforma los datos, solo cambia la estructura.

```scala
val listasAnidadas = List(List(1, 2), List(3, 4))
val aplanada = listasAnidadas.flatten
// Resultado: List(1, 2, 3, 4)
```

---

## 3. ⚡ flatMap
Es la combinación exacta de hacer un `map` y después un `flatten` en un solo paso.
* Se usa cuando la función que aplicás a cada elemento devuelve otra colección.
* Evita que el resultado final quede con niveles anidados innecesarios.

### 🔍 Comparativa visual: map vs flatMap
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
💡 **Regla nemotécnica:** 
* ¿Querés cambiar los elementos manteniendo el contenedor? Usa **`map`**.
* ¿Tenés contenedores dentro de contenedores y querés uno solo? Usa **`flatten`**.
* ¿Vas a transformar elementos y cada uno te va a generar un nuevo contenedor? Usa **`flatMap`**.
