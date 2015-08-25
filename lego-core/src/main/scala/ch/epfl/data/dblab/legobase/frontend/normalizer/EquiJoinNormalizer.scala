package ch.epfl.data
package dblab.legobase
package frontend
package normalizer

import schema.Schema
import scala.collection.mutable.ListBuffer

/**
 * Takes a select statement an pushes equijoin predicates in the WHERE
 * clause to the tables in the FROM clause. Also reorders the predicates
 * to match the order of the tables.
 */
class EquiJoinNormalizer(schema: Schema) extends Normalizer {

  override def normalizeStmt(stmt: SelectStatement): SelectStatement = {
    val stmtWithNewJoinTree = pushPredicatesToJoins(stmt)
    reorderJoinPredicates(stmtWithNewJoinTree)
  }

  private def pushPredicatesToJoins(stmt: SelectStatement): SelectStatement = {
    stmt.joinTrees match {
      case None => throw new Exception("LegoBase Frontend BUG: Couldn't find any joinTree in the select statement!")
      case Some(oldJts) => {
        /* Normalize all subqueries in the JoinTrees of this query */
        val jts = oldJts.map(normalizeJoinTree)
        val newRelations = SQLParser.extractAllRelationsFromJoinTrees(jts)

        //TODO Search recursively through all projections to find subqueries in exists operators to normalize
        //     Take a look at SQLTreeToOperatorTreeConverter.analyzeExprForSubquery method
        //TODO Reextract aliases with the SQLParser.extractAllAliasesFromProjections method

        if (jts.size == 1) {
          stmt /* Nothing to normalize */
        } else {
          /* Try to normalize to a single join tree */
          var usedPreds = new ListBuffer[Equals]()
          val (equiPreds, otherPreds) = stmt.where match { /* There should be at least one predicate in the query */
            case None =>
              throw new Exception("LegoBase limitation: Joins without a join condition are currently not supported.")
            case Some(wh) => separateEquiJoinPredicates(wh)
          }

          /* Join the relations, purge any predicates used in the process and
           * reconnect them to a single WHERE clause in the end */
          val newJoinTree = jts.reduceLeft((acc, right) => joinRelations(acc, right, equiPreds, usedPreds))
          val purgedPredicates = purgePredicates(equiPreds, usedPreds)
          val connectedPredicates = connectPredicates(purgedPredicates, otherPreds)

          SelectStatement(stmt.withs, stmt.projections, newRelations, Some(Seq(newJoinTree)), connectedPredicates,
            stmt.groupBy, stmt.having, stmt.orderBy, stmt.limit, stmt.aliases)
        }
      }
    }
  }

  private def normalizeJoinTree(rel: Relation): Relation = rel match {
    case Subquery((stmt: SelectStatement), al) => Subquery(normalizeStmt(stmt), al)
    case Join(l, r, t, c)                      => Join(normalizeJoinTree(l), normalizeJoinTree(r), t, c)
    case a                                     => a
  }

  /**
   * Separates equality predicates between two fields
   * on the top level of the predicate operator tree
   * from all the other predicates.
   */
  private def separateEquiJoinPredicates(wh: Expression): (Seq[Equals], Seq[Expression]) = wh match {
    case And(left, right) => {
      val (e1, o1) = separateEquiJoinPredicates(left)
      val (e2, o2) = separateEquiJoinPredicates(right)
      (e1 ++ e2, o1 ++ o2)
    }
    case eq @ Equals(FieldIdent(_, _, _), FieldIdent(_, _, _)) => (Seq(eq), Seq.empty)
    case o @ _ => (Seq.empty, Seq(o))
  }

  /**
   * Joins two relations by searching for predicates to match.
   * Throws an exception if no suitable predicate can be found.
   */
  private def joinRelations(left: Relation, right: Relation, predicates: Seq[Equals], usedPreds: ListBuffer[Equals]): Relation = {

    /* Could this be a predicate for the join? */
    val joinPreds = predicates.filter { eq =>
      (containsField(left, eq.left) && containsField(right, eq.right)) ||
        (containsField(left, eq.right) && containsField(right, eq.left))
    }

    if (joinPreds.size == 0)
      throw new Exception(s"LegoBase Frontend BUG: Couldn't find a suitable predicate for joining $left and $right!")

    /* We have found (possibly multiple) suitable join predicates */
    usedPreds ++= joinPreds
    val jp: Seq[Expression] = joinPreds
    val concat = jp.reduceLeft(And(_, _))
    Join(left, right, InnerJoin, concat)
  }

  /** Removes predicates that have been used up by the joins */
  private def purgePredicates(equiPreds: Seq[Equals], usedPreds: Seq[Equals]) =
    equiPreds.filter(!usedPreds.contains(_))

  /** Connects the remaining predicates using AND */
  private def connectPredicates(eq: Seq[Equals], other: Seq[Expression]): Option[Expression] =
    if (eq.size + other.size == 0)
      None
    else
      Some((eq ++ other).reduce(And(_, _)))

  /** Reorders join predicates to adhere to the order of the relations in the join tree */
  private def reorderJoinPredicates(stmt: SelectStatement): SelectStatement = {

    def reorderPreds(rel: Relation): Relation = rel match {
      case Join(left, right, tpe, eq @ Equals(leftExpr, rightExpr)) => {
        Join(reorderPreds(left), reorderPreds(right), tpe,
          if (containsField(left, leftExpr))
            eq
          else
            Equals(rightExpr, leftExpr))
      }
      case _ => rel
    }

    SelectStatement(stmt.withs.map(w => Subquery(normalizeStmt(w.subquery match {
      case stmt: SelectStatement => stmt
    }), w.alias)), stmt.projections, stmt.relations, Some(Seq(reorderPreds(stmt.joinTrees.get(0)))), stmt.where,
      stmt.groupBy, stmt.having, stmt.orderBy, stmt.limit, stmt.aliases)
  }

  /** Checks if an expression (must be a FieldIdent) is a valid field in the relation */
  private def containsField(rel: Relation, field: Expression): Boolean = field match {
    case FieldIdent(quali, fName, _) => rel match { /* Look only at FieldIdents */
      case SQLTable(tName, alias) => quali match {
        case None =>
          /* Just check if attribute exists in table */
          schema.findTable(tName).findAttribute(fName) match {
            case None    => false
            case Some(_) => true
          }
        case Some(q) => {
          alias match {
            case Some(a) => if (q != a) return false
            case _       =>
          }
          schema.findTable(tName).findAttribute(fName) match {
            case None    => false
            case Some(_) => true
          }
        }
      }
      case Subquery(node, alias) => {
        quali match {
          case Some(q) => if (q != alias) return false
          case _       =>
        }
        node match {
          case stmt: SelectStatement => stmt.projections match {
            case AllColumns() => containsField(stmt.joinTrees.get(0), field) /* Suppose that only one join tree is left */
            case ExpressionProjections(lst) => lst.exists {
              case (subExp, subAli) =>
                subExp match {
                  /* Check if there exists a field in the projections of the subquery
                  * (either identified by field name or alias) */
                  case FieldIdent(subQuali, subName, _) => subAli match {
                    case Some(subA) => fName == subA
                    case None       => fName == subName
                  }
                  case _ => false
                }
            }
          }
        }
      }
      case Join(left, right, tpe, _) => tpe match {
        /* For LeftSemi- and AntiJoin we ignore the right relation, 
         * otherwise check both relations of the join */
        case LeftSemiJoin | AntiJoin => containsField(left, field)
        case _                       => containsField(left, field) || containsField(right, field)
      }
    }
    case _ => false
  }
}