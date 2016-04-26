/* Generated by Purgatory 2014-2016 */

package ch.epfl.data.dblab.deep.queryengine

import ch.epfl.data.sc.pardis
import pardis.ir._
import pardis.types.PardisTypeImplicits._
import pardis.effects._
import pardis.deep._
import pardis.deep.scalalib._
import pardis.deep.scalalib.collection._
import pardis.deep.scalalib.io._

import ch.epfl.data.sc.pardis.quasi.anf.{ BaseExt, BaseExtIR }
import ch.epfl.data.sc.pardis.quasi.TypeParameters.MaybeParamTag

trait GenericEngineOps extends Base with OptimalStringOps {
  // Type representation
  val GenericEngineType = GenericEngineIRs.GenericEngineType
  implicit val typeGenericEngine: TypeRep[GenericEngine] = GenericEngineType
  implicit class GenericEngineRep(self: Rep[GenericEngine]) {

  }
  object GenericEngine {
    def runQuery[T](query: => Rep[T])(implicit typeT: TypeRep[T]): Rep[T] = genericEngineRunQueryObject[T](query)(typeT)
    def dateToString(long: Rep[Int]): Rep[String] = genericEngineDateToStringObject(long)
    def dateToYear(long: Rep[Int]): Rep[Int] = genericEngineDateToYearObject(long)
    def parseDate(x: Rep[String]): Rep[Int] = genericEngineParseDateObject(x)
    def parseString(x: Rep[String]): Rep[OptimalString] = genericEngineParseStringObject(x)
  }
  // constructors

  // IR defs
  val GenericEngineRunQueryObject = GenericEngineIRs.GenericEngineRunQueryObject
  type GenericEngineRunQueryObject[T] = GenericEngineIRs.GenericEngineRunQueryObject[T]
  val GenericEngineDateToStringObject = GenericEngineIRs.GenericEngineDateToStringObject
  type GenericEngineDateToStringObject = GenericEngineIRs.GenericEngineDateToStringObject
  val GenericEngineDateToYearObject = GenericEngineIRs.GenericEngineDateToYearObject
  type GenericEngineDateToYearObject = GenericEngineIRs.GenericEngineDateToYearObject
  val GenericEngineParseDateObject = GenericEngineIRs.GenericEngineParseDateObject
  type GenericEngineParseDateObject = GenericEngineIRs.GenericEngineParseDateObject
  val GenericEngineParseStringObject = GenericEngineIRs.GenericEngineParseStringObject
  type GenericEngineParseStringObject = GenericEngineIRs.GenericEngineParseStringObject
  // method definitions
  def genericEngineRunQueryObject[T](query: => Rep[T])(implicit typeT: TypeRep[T]): Rep[T] = {
    val queryOutput = reifyBlock(query)
    GenericEngineRunQueryObject[T](queryOutput)
  }
  def genericEngineDateToStringObject(long: Rep[Int]): Rep[String] = GenericEngineDateToStringObject(long)
  def genericEngineDateToYearObject(long: Rep[Int]): Rep[Int] = GenericEngineDateToYearObject(long)
  def genericEngineParseDateObject(x: Rep[String]): Rep[Int] = GenericEngineParseDateObject(x)
  def genericEngineParseStringObject(x: Rep[String]): Rep[OptimalString] = GenericEngineParseStringObject(x)
  type GenericEngine = ch.epfl.data.dblab.queryengine.GenericEngine
}
object GenericEngineIRs extends Base {
  import OptimalStringIRs._
  // Type representation
  case object GenericEngineType extends TypeRep[GenericEngine] {
    def rebuild(newArguments: TypeRep[_]*): TypeRep[_] = GenericEngineType
    val name = "GenericEngine"
    val typeArguments = Nil
  }
  implicit val typeGenericEngine: TypeRep[GenericEngine] = GenericEngineType
  // case classes
  case class GenericEngineRunQueryObject[T](queryOutput: Block[T])(implicit val typeT: TypeRep[T]) extends FunctionDef[T](None, "GenericEngine.runQuery", List(List(queryOutput))) {
    override def curriedConstructor = (copy[T] _)
  }

  case class GenericEngineDateToStringObject(long: Rep[Int]) extends FunctionDef[String](None, "GenericEngine.dateToString", List(List(long))) {
    override def curriedConstructor = (copy _)
  }

  case class GenericEngineDateToYearObject(long: Rep[Int]) extends FunctionDef[Int](None, "GenericEngine.dateToYear", List(List(long))) {
    override def curriedConstructor = (copy _)
  }

  case class GenericEngineParseDateObject(x: Rep[String]) extends FunctionDef[Int](None, "GenericEngine.parseDate", List(List(x))) {
    override def curriedConstructor = (copy _)
  }

  case class GenericEngineParseStringObject(x: Rep[String]) extends FunctionDef[OptimalString](None, "GenericEngine.parseString", List(List(x))) {
    override def curriedConstructor = (copy _)
  }

  type GenericEngine = ch.epfl.data.dblab.queryengine.GenericEngine
}
trait GenericEngineImplicits extends GenericEngineOps {
  // Add implicit conversions here!
}
trait GenericEngineComponent extends GenericEngineOps with GenericEngineImplicits {}

trait GenericEnginePartialEvaluation extends GenericEngineComponent with BasePartialEvaluation {
  // Immutable field inlining 

  // Mutable field inlining 
  // Pure function partial evaluation
}

// QUASI GENERATED CODE:

object GenericEngineQuasiNodes extends BaseExtIR {
  import GenericEngineIRs._
  import OptimalStringQuasiNodes._
  // case classes
  case class GenericEngineRunQueryObjectExt[T](queryOutput: Block[T])(implicit val paramT: MaybeParamTag[T]) extends FunctionDef[GenericEngineRunQueryObject[T], T] {
    override def nodeUnapply(t: GenericEngineRunQueryObject[T]): Option[Product] = (GenericEngineRunQueryObject.unapply(t): Option[Product]) map { r =>
      paramT.define(t.typeT)
      r
    }
  }
  case class GenericEngineDateToStringObjectExt(long: Rep[Int]) extends FunctionDef[GenericEngineDateToStringObject, String] {
    override def nodeUnapply(t: GenericEngineDateToStringObject): Option[Product] = (GenericEngineDateToStringObject.unapply(t): Option[Product]) map { r =>
      r
    }
  }
  case class GenericEngineDateToYearObjectExt(long: Rep[Int]) extends FunctionDef[GenericEngineDateToYearObject, Int] {
    override def nodeUnapply(t: GenericEngineDateToYearObject): Option[Product] = (GenericEngineDateToYearObject.unapply(t): Option[Product]) map { r =>
      r
    }
  }
  case class GenericEngineParseDateObjectExt(x: Rep[String]) extends FunctionDef[GenericEngineParseDateObject, Int] {
    override def nodeUnapply(t: GenericEngineParseDateObject): Option[Product] = (GenericEngineParseDateObject.unapply(t): Option[Product]) map { r =>
      r
    }
  }
  case class GenericEngineParseStringObjectExt(x: Rep[String]) extends FunctionDef[GenericEngineParseStringObject, OptimalString] {
    override def nodeUnapply(t: GenericEngineParseStringObject): Option[Product] = (GenericEngineParseStringObject.unapply(t): Option[Product]) map { r =>
      r
    }
  }
  type GenericEngine = ch.epfl.data.dblab.queryengine.GenericEngine
}

trait GenericEngineExtOps extends BaseExt with OptimalStringExtOps {

  import GenericEngineQuasiNodes._
  import ch.epfl.data.sc.pardis.quasi.OverloadHackObj._
  import OptimalStringQuasiNodes._
  implicit class GenericEngineRep(self: Rep[GenericEngine]) {
  }
  object GenericEngine {
    def runQuery[T](query: => Rep[T])(implicit paramT: MaybeParamTag[T]): Rep[T] = genericEngineRunQueryObject[T](query)(paramT)
    def dateToString(long: Rep[Int]): Rep[String] = genericEngineDateToStringObject(long)
    def dateToYear(long: Rep[Int]): Rep[Int] = genericEngineDateToYearObject(long)
    def parseDate(x: Rep[String]): Rep[Int] = genericEngineParseDateObject(x)
    def parseString(x: Rep[String]): Rep[OptimalString] = genericEngineParseStringObject(x)
  }
  // constructors

  // method definitions
  def genericEngineRunQueryObject[T](query: => Rep[T])(implicit paramT: MaybeParamTag[T]): Rep[T] = {
    val queryOutput = reifyBlock(query)
    GenericEngineRunQueryObjectExt[T](queryOutput)
  }
  def genericEngineDateToStringObject(long: Rep[Int]): Rep[String] = GenericEngineDateToStringObjectExt(long)
  def genericEngineDateToYearObject(long: Rep[Int]): Rep[Int] = GenericEngineDateToYearObjectExt(long)
  def genericEngineParseDateObject(x: Rep[String]): Rep[Int] = GenericEngineParseDateObjectExt(x)
  def genericEngineParseStringObject(x: Rep[String]): Rep[OptimalString] = GenericEngineParseStringObjectExt(x)
  type GenericEngine = ch.epfl.data.dblab.queryengine.GenericEngine
}
