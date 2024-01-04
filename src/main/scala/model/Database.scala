package org.bpc
package model

import org.apache.commons.text.similarity.LevenshteinDistance


class Database(path: String) {
    val distance = new LevenshteinDistance()
    val data: List[String] = {
        println(f"Loading Database $path...")
        //        val string = Source.fromFile(path).mkString
        //        ujson.read(string)("title").obj.value.values.map(_.str).toList
        List("A", "B", "C", "D", "E", "F", "G", "H", "I", "J")
    }

    def find(input: String): String = {
        data.minBy(that => distance.apply(input, that))
    }
}
