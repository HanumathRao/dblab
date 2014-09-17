package ch.epfl.data
package legobase
package deep

import scala.reflect.runtime.universe.{ typeTag => tag }
import scala.language.implicitConversions
import pardis.utils.Utils.{ pardisTypeToString => t2s }
import pardis.types.PardisTypeImplicits._

trait ManualLiftedLegoBase extends OptionOps with SetOps with OrderingOps with ManifestOps with IntPE with RichIntOps with pardis.deep.scalalib.ByteComponent with LegoHashMap { this: DeepDSL =>
  /* TODO These methods should be lifted from scala.Predef */
  case class Println(x: Rep[Any]) extends FunctionDef[Unit](None, "println", List(List(x))) {
    override def curriedConstructor = (copy _)
  }
  def println(x: Rep[Any]): Rep[Unit] = Println(x)
  // for performance reasons printf is written like this:
  case class Printf(text: Rep[String], xs: Rep[Any]*) extends FunctionDef[Unit](None, "printf", List(text :: xs.toList)) {
    override def rebuild(children: FunctionArg*) = Printf(children(0).asInstanceOf[Rep[String]], children.drop(1).toSeq.asInstanceOf[Seq[Rep[Any]]]: _*)
  }
  def printf(text: Rep[String], xs: Rep[Any]*): Rep[Unit] = Printf(text, xs: _*)
  // printf is not written like this for the reason mentioned above
  // case class Printf(text: Rep[String], xs: Rep[Seq[Any]]) extends FunctionDef[Unit](None, "printf", List(List(text, __varArg(xs)))) {
  //   override def curriedConstructor = (copy _).curried
  // }
  // def printf(text: Rep[String], xs: Rep[Any]*): Rep[Unit] = Printf(text, __liftSeq(xs.toSeq))

  def __newException(msg: Rep[String]) = new Exception(msg.toString)

  // TODO this thing should be removed, ideally every literal should be lifted using YY
  implicit def liftInt(i: scala.Int): Rep[Int] = unit(i)

  // TODO implicit parameters should be handled in a correct way to remove this manually lifted one
  def __newTreeSet2[A](ordering: Rep[Ordering[A]])(implicit typeA: TypeRep[A]): Rep[TreeSet[A]] = TreeSetNew2[A](ordering)(typeA)
  // case classes
  case class TreeSetNew2[A](val ordering: Rep[Ordering[A]])(implicit val typeA: TypeRep[A]) extends FunctionDef[TreeSet[A]](None, "new TreeSet", List(Nil, List(ordering))) {
    override def curriedConstructor = (copy[A] _)
  }

  // this one is needed to rewrire `ArrayBuffer.apply()` to `new ArrayBuffer()`
  override def arrayBufferApplyObject[T]()(implicit typeT: TypeRep[T]): Rep[ArrayBuffer[T]] = __newArrayBuffer[T]()

  /* TODO there's a bug with the design of records which if it's solved there's no need for this manual node */
  override def aGGRecordNew[B](key: Rep[B], aggs: Rep[Array[Double]])(implicit typeB: TypeRep[B]): Rep[AGGRecord[B]] = AGGRecordNew2[B](key, aggs)
  case class AGGRecordNew2[B](key: Rep[B], aggs: Rep[Array[Double]])(implicit val typeB: TypeRep[B]) extends ConstructorDef[AGGRecord[B]](List(), "AGGRecord", List(List(key, aggs))) {
    override def curriedConstructor = (copy[B] _).curried
  }
}

// TODO should be generated automatically
trait OptionOps { this: DeepDSL =>
  implicit class OptionRep[A](self: Rep[Option[A]])(implicit typeA: TypeRep[A]) {
    def get(): Rep[A] = optionGet[A](self)(typeA)
  }
  def optionGet[A](self: Rep[Option[A]])(implicit typeA: TypeRep[A]): Rep[A] = OptionGet[A](self)
  case class OptionGet[A](self: Rep[Option[A]])(implicit typeA: TypeRep[A]) extends FunctionDef[A](Some(self), "get", List()) {
    override def curriedConstructor = copy[A] _
  }
}

trait SetOps extends pardis.deep.scalalib.collection.SetOps { this: DeepDSL =>
  case class SetNew[T: TypeRep](seq: Rep[Seq[T]]) extends FunctionDef[Set[T]](None, "Set", List(List(__varArg(seq)))) {
    override def curriedConstructor = copy[T] _
  }

  // These are needed for rewiring `Set.apply()` to `new Set()`
  // TODO if YinYang virtualizes `:_*` correctly there's no need to lift this one manually,
  // only the reflected one should be changed to var arg one.
  override def setApplyObject1[T](seq: Rep[Seq[T]])(implicit typeT: TypeRep[T]): Rep[Set[T]] = SetNew[T](seq)
  // TODO as Set doesn't have a constructor they should be manually lifted
  override def setApplyObject2[T]()(implicit typeT: TypeRep[T]): Rep[Set[T]] = SetNew2[T]()
  case class SetNew2[T: TypeRep]() extends FunctionDef[Set[T]](None, s"Set[${t2s(implicitly[TypeRep[T]])}]", List(List())) {
    override def curriedConstructor = (x: Any) => copy[T]()
  }
}

// TODO needs lifting type classes
trait OrderingOps { this: DeepDSL =>
  object Ordering {
    def apply[T: TypeRep](comp: Rep[Function2[T, T, Int]]): Rep[Ordering[T]] = OrderingNew(comp)
  }
  case class OrderingNew[T: TypeRep](comp: Rep[Function2[T, T, Int]]) extends FunctionDef[Ordering[T]](None, "OrderingFactory", List(List(comp))) {
    override def curriedConstructor = copy[T] _
  }
}

// TODO needs lifting type classes
trait ManifestOps { this: DeepDSL =>
  object ManifestRep {
    def apply[T: TypeRep](man: Manifest[T]): Rep[Manifest[T]] = ManifestNew[T](man)
  }

  case class ManifestNew[T](man: Manifest[T])(implicit typeT: TypeRep[T]) extends FunctionDef[Manifest[T]](None, s"manifest[${t2s(typeT)}]", Nil) {
    override def curriedConstructor = copy[T] _
  }
}

// TODO needs generation of partial evaluation for every node
trait IntPE extends pardis.deep.scalalib.IntOps { this: DeepDSL =>
  case class Int$plus5PE(self: Rep[Int], x: Rep[Int]) extends FunctionDef[Int](Some(self), "+", List(List(x))) {
    override def isPure = true
    override def partialEvaluable: Boolean = true
    override def partialEvaluate(children: Any*): Int = children(0).asInstanceOf[Int] + children(1).asInstanceOf[Int]
    override def curriedConstructor = (copy _).curried
  }
  override def int$plus5(self: Rep[Int], x: Rep[Int]): Rep[Int] = Int$plus5PE(self, x)
}

trait RichIntOps extends pardis.deep.scalalib.collection.RangeComponent { this: DeepDSL =>
  def intWrapper(i: Rep[Int]): Rep[RichInt] = i

  implicit class RichIntOps(self: Rep[RichInt]) {
    def until(to: Rep[Int]): Rep[Range] = richIntUntil(self, to)
  }

  def richIntUntil(self: Rep[RichInt], to: Rep[Int]): Rep[Range] = rangeNew(self, to, unit(1))

  type RichInt = Int
}

trait LegoHashMap { this: DeepDSL =>
  def __newHashMap3[A, B](extract: Rep[B => A])(implicit typeA: TypeRep[A], typeB: TypeRep[B]): Rep[HashMap[A, ArrayBuffer[B]]] = hashMapNew3[A, B](extract)(typeA, typeB)
  def hashMapNew3[A, B](extract: Rep[B => A])(implicit typeA: TypeRep[A], typeB: TypeRep[B]): Rep[HashMap[A, ArrayBuffer[B]]] = HashMapNew3[A, B](extract)(typeA, typeB)

  case class HashMapNew3[A, B](extract: Rep[B => A])(implicit val typeA: TypeRep[A], val typeB: TypeRep[B]) extends ConstructorDef[HashMap[A, ArrayBuffer[B]]](List(typeA, ArrayBufferType(typeB)), "HashMap", List(List())) {
    override def curriedConstructor = copy[A, B] _
    override def funArgs = List(extract)
  }

  def __newHashMap4[A]()(implicit typeA: TypeRep[A]): Rep[HashMap[A, Array[Double]]] = hashMapNew4[A]()(typeA)
  def hashMapNew4[A]()(implicit typeA: TypeRep[A]): Rep[HashMap[A, Array[Double]]] = HashMapNew4[A]()(typeA)

  case class HashMapNew4[A]()(implicit val typeA: TypeRep[A]) extends ConstructorDef[HashMap[A, Array[Double]]](List(typeA, ArrayType(DoubleType)), "HashMap", List(List())) {
    override def curriedConstructor = (x: Any) => copy[A]()
    override def funArgs = List()
  }
}
