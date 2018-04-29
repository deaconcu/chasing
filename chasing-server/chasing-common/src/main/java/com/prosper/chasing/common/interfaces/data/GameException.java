/**
 * Autogenerated by Thrift Compiler (0.10.0)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
package com.prosper.chasing.common.interfaces.data;

@SuppressWarnings({"cast", "rawtypes", "serial", "unchecked", "unused"})
@javax.annotation.Generated(value = "Autogenerated by Thrift Compiler (0.10.0)", date = "2018-04-09")
public class GameException extends org.apache.thrift.TException implements org.apache.thrift.TBase<GameException, GameException._Fields>, java.io.Serializable, Cloneable, Comparable<GameException> {
  private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("GameException");

  private static final org.apache.thrift.protocol.TField EX_CODE_FIELD_DESC = new org.apache.thrift.protocol.TField("exCode", org.apache.thrift.protocol.TType.I32, (short)1);
  private static final org.apache.thrift.protocol.TField DESC_FIELD_DESC = new org.apache.thrift.protocol.TField("desc", org.apache.thrift.protocol.TType.STRING, (short)2);

  private static final org.apache.thrift.scheme.SchemeFactory STANDARD_SCHEME_FACTORY = new GameExceptionStandardSchemeFactory();
  private static final org.apache.thrift.scheme.SchemeFactory TUPLE_SCHEME_FACTORY = new GameExceptionTupleSchemeFactory();

  public int exCode; // required
  public java.lang.String desc; // required

  /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
  public enum _Fields implements org.apache.thrift.TFieldIdEnum {
    EX_CODE((short)1, "exCode"),
    DESC((short)2, "desc");

    private static final java.util.Map<java.lang.String, _Fields> byName = new java.util.HashMap<java.lang.String, _Fields>();

    static {
      for (_Fields field : java.util.EnumSet.allOf(_Fields.class)) {
        byName.put(field.getFieldName(), field);
      }
    }

    /**
     * Find the _Fields constant that matches fieldId, or null if its not found.
     */
    public static _Fields findByThriftId(int fieldId) {
      switch(fieldId) {
        case 1: // EX_CODE
          return EX_CODE;
        case 2: // DESC
          return DESC;
        default:
          return null;
      }
    }

    /**
     * Find the _Fields constant that matches fieldId, throwing an exception
     * if it is not found.
     */
    public static _Fields findByThriftIdOrThrow(int fieldId) {
      _Fields fields = findByThriftId(fieldId);
      if (fields == null) throw new java.lang.IllegalArgumentException("Field " + fieldId + " doesn't exist!");
      return fields;
    }

    /**
     * Find the _Fields constant that matches name, or null if its not found.
     */
    public static _Fields findByName(java.lang.String name) {
      return byName.get(name);
    }

    private final short _thriftId;
    private final java.lang.String _fieldName;

    _Fields(short thriftId, java.lang.String fieldName) {
      _thriftId = thriftId;
      _fieldName = fieldName;
    }

    public short getThriftFieldId() {
      return _thriftId;
    }

    public java.lang.String getFieldName() {
      return _fieldName;
    }
  }

  // isset id assignments
  private static final int __EXCODE_ISSET_ID = 0;
  private byte __isset_bitfield = 0;
  public static final java.util.Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;
  static {
    java.util.Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new java.util.EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
    tmpMap.put(_Fields.EX_CODE, new org.apache.thrift.meta_data.FieldMetaData("exCode", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I32)));
    tmpMap.put(_Fields.DESC, new org.apache.thrift.meta_data.FieldMetaData("desc", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
    metaDataMap = java.util.Collections.unmodifiableMap(tmpMap);
    org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(GameException.class, metaDataMap);
  }

  public GameException() {
  }

  public GameException(
    int exCode,
    java.lang.String desc)
  {
    this();
    this.exCode = exCode;
    setExCodeIsSet(true);
    this.desc = desc;
  }

  /**
   * Performs a deep copy on <i>other</i>.
   */
  public GameException(GameException other) {
    __isset_bitfield = other.__isset_bitfield;
    this.exCode = other.exCode;
    if (other.isSetDesc()) {
      this.desc = other.desc;
    }
  }

  public GameException deepCopy() {
    return new GameException(this);
  }

  @Override
  public void clear() {
    setExCodeIsSet(false);
    this.exCode = 0;
    this.desc = null;
  }

  public int getExCode() {
    return this.exCode;
  }

  public GameException setExCode(int exCode) {
    this.exCode = exCode;
    setExCodeIsSet(true);
    return this;
  }

  public void unsetExCode() {
    __isset_bitfield = org.apache.thrift.EncodingUtils.clearBit(__isset_bitfield, __EXCODE_ISSET_ID);
  }

  /** Returns true if field exCode is set (has been assigned a value) and false otherwise */
  public boolean isSetExCode() {
    return org.apache.thrift.EncodingUtils.testBit(__isset_bitfield, __EXCODE_ISSET_ID);
  }

  public void setExCodeIsSet(boolean value) {
    __isset_bitfield = org.apache.thrift.EncodingUtils.setBit(__isset_bitfield, __EXCODE_ISSET_ID, value);
  }

  public java.lang.String getDesc() {
    return this.desc;
  }

  public GameException setDesc(java.lang.String desc) {
    this.desc = desc;
    return this;
  }

  public void unsetDesc() {
    this.desc = null;
  }

  /** Returns true if field desc is set (has been assigned a value) and false otherwise */
  public boolean isSetDesc() {
    return this.desc != null;
  }

  public void setDescIsSet(boolean value) {
    if (!value) {
      this.desc = null;
    }
  }

  public void setFieldValue(_Fields field, java.lang.Object value) {
    switch (field) {
    case EX_CODE:
      if (value == null) {
        unsetExCode();
      } else {
        setExCode((java.lang.Integer)value);
      }
      break;

    case DESC:
      if (value == null) {
        unsetDesc();
      } else {
        setDesc((java.lang.String)value);
      }
      break;

    }
  }

  public java.lang.Object getFieldValue(_Fields field) {
    switch (field) {
    case EX_CODE:
      return getExCode();

    case DESC:
      return getDesc();

    }
    throw new java.lang.IllegalStateException();
  }

  /** Returns true if field corresponding to fieldID is set (has been assigned a value) and false otherwise */
  public boolean isSet(_Fields field) {
    if (field == null) {
      throw new java.lang.IllegalArgumentException();
    }

    switch (field) {
    case EX_CODE:
      return isSetExCode();
    case DESC:
      return isSetDesc();
    }
    throw new java.lang.IllegalStateException();
  }

  @Override
  public boolean equals(java.lang.Object that) {
    if (that == null)
      return false;
    if (that instanceof GameException)
      return this.equals((GameException)that);
    return false;
  }

  public boolean equals(GameException that) {
    if (that == null)
      return false;
    if (this == that)
      return true;

    boolean this_present_exCode = true;
    boolean that_present_exCode = true;
    if (this_present_exCode || that_present_exCode) {
      if (!(this_present_exCode && that_present_exCode))
        return false;
      if (this.exCode != that.exCode)
        return false;
    }

    boolean this_present_desc = true && this.isSetDesc();
    boolean that_present_desc = true && that.isSetDesc();
    if (this_present_desc || that_present_desc) {
      if (!(this_present_desc && that_present_desc))
        return false;
      if (!this.desc.equals(that.desc))
        return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int hashCode = 1;

    hashCode = hashCode * 8191 + exCode;

    hashCode = hashCode * 8191 + ((isSetDesc()) ? 131071 : 524287);
    if (isSetDesc())
      hashCode = hashCode * 8191 + desc.hashCode();

    return hashCode;
  }

  @Override
  public int compareTo(GameException other) {
    if (!getClass().equals(other.getClass())) {
      return getClass().getName().compareTo(other.getClass().getName());
    }

    int lastComparison = 0;

    lastComparison = java.lang.Boolean.valueOf(isSetExCode()).compareTo(other.isSetExCode());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetExCode()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.exCode, other.exCode);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = java.lang.Boolean.valueOf(isSetDesc()).compareTo(other.isSetDesc());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetDesc()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.desc, other.desc);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    return 0;
  }

  public _Fields fieldForId(int fieldId) {
    return _Fields.findByThriftId(fieldId);
  }

  public void read(org.apache.thrift.protocol.TProtocol iprot) throws org.apache.thrift.TException {
    scheme(iprot).read(iprot, this);
  }

  public void write(org.apache.thrift.protocol.TProtocol oprot) throws org.apache.thrift.TException {
    scheme(oprot).write(oprot, this);
  }

  @Override
  public java.lang.String toString() {
    java.lang.StringBuilder sb = new java.lang.StringBuilder("GameException(");
    boolean first = true;

    sb.append("exCode:");
    sb.append(this.exCode);
    first = false;
    if (!first) sb.append(", ");
    sb.append("desc:");
    if (this.desc == null) {
      sb.append("null");
    } else {
      sb.append(this.desc);
    }
    first = false;
    sb.append(")");
    return sb.toString();
  }

  public void validate() throws org.apache.thrift.TException {
    // check for required fields
    // check for sub-struct validity
  }

  private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
    try {
      write(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(out)));
    } catch (org.apache.thrift.TException te) {
      throw new java.io.IOException(te);
    }
  }

  private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, java.lang.ClassNotFoundException {
    try {
      // it doesn't seem like you should have to do this, but java serialization is wacky, and doesn't call the default constructor.
      __isset_bitfield = 0;
      read(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(in)));
    } catch (org.apache.thrift.TException te) {
      throw new java.io.IOException(te);
    }
  }

  private static class GameExceptionStandardSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {
    public GameExceptionStandardScheme getScheme() {
      return new GameExceptionStandardScheme();
    }
  }

  private static class GameExceptionStandardScheme extends org.apache.thrift.scheme.StandardScheme<GameException> {

    public void read(org.apache.thrift.protocol.TProtocol iprot, GameException struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TField schemeField;
      iprot.readStructBegin();
      while (true)
      {
        schemeField = iprot.readFieldBegin();
        if (schemeField.type == org.apache.thrift.protocol.TType.STOP) { 
          break;
        }
        switch (schemeField.id) {
          case 1: // EX_CODE
            if (schemeField.type == org.apache.thrift.protocol.TType.I32) {
              struct.exCode = iprot.readI32();
              struct.setExCodeIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 2: // DESC
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.desc = iprot.readString();
              struct.setDescIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          default:
            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
        }
        iprot.readFieldEnd();
      }
      iprot.readStructEnd();

      // check for required fields of primitive type, which can't be checked in the validate method
      struct.validate();
    }

    public void write(org.apache.thrift.protocol.TProtocol oprot, GameException struct) throws org.apache.thrift.TException {
      struct.validate();

      oprot.writeStructBegin(STRUCT_DESC);
      oprot.writeFieldBegin(EX_CODE_FIELD_DESC);
      oprot.writeI32(struct.exCode);
      oprot.writeFieldEnd();
      if (struct.desc != null) {
        oprot.writeFieldBegin(DESC_FIELD_DESC);
        oprot.writeString(struct.desc);
        oprot.writeFieldEnd();
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }

  }

  private static class GameExceptionTupleSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {
    public GameExceptionTupleScheme getScheme() {
      return new GameExceptionTupleScheme();
    }
  }

  private static class GameExceptionTupleScheme extends org.apache.thrift.scheme.TupleScheme<GameException> {

    @Override
    public void write(org.apache.thrift.protocol.TProtocol prot, GameException struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TTupleProtocol oprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
      java.util.BitSet optionals = new java.util.BitSet();
      if (struct.isSetExCode()) {
        optionals.set(0);
      }
      if (struct.isSetDesc()) {
        optionals.set(1);
      }
      oprot.writeBitSet(optionals, 2);
      if (struct.isSetExCode()) {
        oprot.writeI32(struct.exCode);
      }
      if (struct.isSetDesc()) {
        oprot.writeString(struct.desc);
      }
    }

    @Override
    public void read(org.apache.thrift.protocol.TProtocol prot, GameException struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TTupleProtocol iprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
      java.util.BitSet incoming = iprot.readBitSet(2);
      if (incoming.get(0)) {
        struct.exCode = iprot.readI32();
        struct.setExCodeIsSet(true);
      }
      if (incoming.get(1)) {
        struct.desc = iprot.readString();
        struct.setDescIsSet(true);
      }
    }
  }

  private static <S extends org.apache.thrift.scheme.IScheme> S scheme(org.apache.thrift.protocol.TProtocol proto) {
    return (org.apache.thrift.scheme.StandardScheme.class.equals(proto.getScheme()) ? STANDARD_SCHEME_FACTORY : TUPLE_SCHEME_FACTORY).getScheme();
  }
}

