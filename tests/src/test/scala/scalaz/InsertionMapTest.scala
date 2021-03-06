package scalaz

import scalacheck.ScalazArbitrary._
import scalacheck.ScalazProperties._
import std.AllInstances._

class InsertionMapTest extends Spec {
  checkAll(equal.laws[InsertionMap[Int, String]])
  checkAll(functor.laws[({type λ[α] = InsertionMap[Int, α]})#λ])
  checkAll(traverse.laws[({type λ[α] = InsertionMap[Int, α]})#λ])

  "equality order independence" ! prop {
    (k1: Int, v1: String, k2: Int, v2: String, a: InsertionMap[Int, String]) =>
      (k1 != k2) ==>
        ((a ^+^ (k1, v1) ^+^ (k2, v2)) == (a ^+^ (k2, v2) ^+^ (k1, v1)))
  }

  "isEmpty == keys.isEmpty" ! prop {
    (a: InsertionMap[Int, String]) => a.isEmpty == a.keys.isEmpty
  }

  "isEmpty == (size == 0)" ! prop {
    (a: InsertionMap[Int, String]) => a.isEmpty == (a.size == 0)
  }

  "insert contains" ! prop {
    (k: Int, v: String, a: InsertionMap[Int, String]) => a ^+^ (k, v) contains k
  }

  "insert get" ! prop {
    (k: Int, v: String, a: InsertionMap[Int, String]) =>
      (a ^+^ (k, v) get k) == Some(v)
  }

  "insert in order" ! prop {
    (k: Int, v: String, a: InsertionMap[Int, String]) =>
      (a ^+^ (k, v)).keys.last == k
  }

  "double insert in order" ! prop {
    (k1: Int, v1: String, k2: Int, v2: String, a: InsertionMap[Int, String]) => {
      val q = (a ^+^ (k1, v1) ^+^ (k2, v2)).toList
      if (k1 == k2)
        q.last == (k2, v2)
      else
        q.reverse match {
          case r::s::_ => r == (k2, v2) && s == (k1, v1)
          case _ => false
        }
    }
  }

  "insert remove" ! prop {
    (k: Int, v: String, a: InsertionMap[Int, String]) =>
      (a ^+^ (k, v) ^-^ k).toList == (a ^-^ k).toList
  }

  "remove not get" ! prop {
    (k: Int, a: InsertionMap[Int, String]) =>
      ((a ^-^ k) get k) == None
  }

  "get getOr" ! prop {
    (k: Int, z: String, a: InsertionMap[Int, String]) =>
      (a getOr (k, z)) == (a get k getOrElse z)
  }

  "remove pair get" ! prop {
    (k: Int, a: InsertionMap[Int, String]) => {
      val (q, _) = a @- k
      q == (a get k)
    }
  }

  "remove pair removes" ! prop {
    (k: Int, a: InsertionMap[Int, String]) => {
      val (_, r) = a @- k
      r.toList == (a ^-^ k).toList
    }
  }

  "no stack overflow" in {
    import syntax.foldable._
    val list = (1 to 100000).map{i => i -> i}.toList
    val values = list.map(_._2)
    val map = InsertionMap(list: _*)

    "foldLeft" in {
      map.foldLeft(List[Int]())((a, b) => b :: a) must be_===(values.foldLeft(List[Int]())((a, b) => b :: a))
    }
    "foldRight" in {
      map.foldRight(List[Int]())(_ :: _) must be_===(Foldable[List].foldRight(values, List[Int]())(_ :: _))
    }
    "foldMap" in {
      map.foldMap(identity) must be_===(values.foldMap(identity))
    }
  }

}
