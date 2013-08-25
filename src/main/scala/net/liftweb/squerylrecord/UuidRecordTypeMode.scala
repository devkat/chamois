package net.liftweb.squerylrecord

import net.liftweb.record.{ MandatoryTypedField, OptionalTypedField, TypedField }
import org.squeryl.{ PrimitiveTypeMode, Schema }
import org.squeryl.dsl.{ UuidExpression }
import org.squeryl.dsl.ast.{ SelectElementReference, SelectElement, ConstantExpressionNode }
import org.squeryl.internals.{ AttributeValidOnNonNumericalColumn, AttributeValidOnNumericalColumn, FieldReferenceLinker, OutMapper }
import java.util.UUID

object UuidRecordTypeMode extends UuidRecordTypeMode

trait UuidRecordTypeMode extends RecordTypeMode {

  implicit def uuid2ScalarUuid(f: MandatoryTypedField[UUID]) = fieldReference match {
    case Some(e) => new SelectElementReference[UUID](e)(createOutMapperUuidType) with UuidExpression[UUID] with SquerylRecordNonNumericalExpression[UUID]
    case None => new ConstantExpressionNode[UUID](f.is)(createOutMapperUuidType) with UuidExpression[UUID] with SquerylRecordNonNumericalExpression[UUID]
  }

  implicit def optionUuid2ScalarUuid(f: OptionalTypedField[UUID]) = fieldReference match {
      case Some(e) => new SelectElementReference[Option[UUID]](e)(createOutMapperUuidTypeOption) with UuidExpression[Option[UUID]] with SquerylRecordNonNumericalExpression[Option[UUID]]
      case None => new ConstantExpressionNode[Option[UUID]](f.is)(createOutMapperUuidTypeOption) with UuidExpression[Option[UUID]] with SquerylRecordNonNumericalExpression[Option[UUID]]
    }
  
  implicit def optionUuidField2OptionUuid(f: Option[TypedField[UUID]]) = fieldReference match {
    case Some(e) => new SelectElementReference[Option[UUID]](e)(createOutMapperUuidTypeOption) with UuidExpression[Option[UUID]] with SquerylRecordNonNumericalExpression[Option[UUID]]
    case None => new ConstantExpressionNode[Option[UUID]](getValue(f))(createOutMapperUuidTypeOption) with UuidExpression[Option[UUID]] with SquerylRecordNonNumericalExpression[Option[UUID]]
  }
  
  /**
   * Returns the field that was last referenced by Squeryl. Can also be None.
   */
  private def fieldReference = FieldReferenceLinker.takeLastAccessedFieldReference

  private def getValue[T](f: Option[TypedField[T]]): Option[T] = f match {
    case Some(field) => field.valueBox
    case None => None
  }
  
  private def getValueOrNull[T <: AnyRef](f: Option[TypedField[T]]): T = f match {
    case Some(field) => field.valueBox.openOr(null.asInstanceOf[T])
    case None => null.asInstanceOf[T]
  }
}
