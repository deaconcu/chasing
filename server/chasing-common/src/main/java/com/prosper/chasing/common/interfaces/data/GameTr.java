/**
 * Autogenerated by Thrift Compiler (0.9.3)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
package com.prosper.chasing.common.interfaces.data;

import org.apache.thrift.scheme.IScheme;
import org.apache.thrift.scheme.SchemeFactory;
import org.apache.thrift.scheme.StandardScheme;

import org.apache.thrift.scheme.TupleScheme;
import org.apache.thrift.protocol.TTupleProtocol;
import org.apache.thrift.protocol.TProtocolException;
import org.apache.thrift.EncodingUtils;
import org.apache.thrift.TException;
import org.apache.thrift.async.AsyncMethodCallback;
import org.apache.thrift.server.AbstractNonblockingServer.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.EnumMap;
import java.util.Set;
import java.util.HashSet;
import java.util.EnumSet;
import java.util.Collections;
import java.util.BitSet;
import java.nio.ByteBuffer;
import java.util.Arrays;
import javax.annotation.Generated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({"cast", "rawtypes", "serial", "unchecked"})
@Generated(value = "Autogenerated by Thrift Compiler (0.9.3)", date = "2016-05-12")
public class GameTr implements org.apache.thrift.TBase<GameTr, GameTr._Fields>, java.io.Serializable, Cloneable, Comparable<GameTr> {
  private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("GameTr");

  private static final org.apache.thrift.protocol.TField ID_FIELD_DESC = new org.apache.thrift.protocol.TField("id", org.apache.thrift.protocol.TType.I32, (short)1);
  private static final org.apache.thrift.protocol.TField METAGAME_ID_FIELD_DESC = new org.apache.thrift.protocol.TField("metagameId", org.apache.thrift.protocol.TType.STRING, (short)2);
  private static final org.apache.thrift.protocol.TField DURATION_FIELD_DESC = new org.apache.thrift.protocol.TField("duration", org.apache.thrift.protocol.TType.I32, (short)3);
  private static final org.apache.thrift.protocol.TField STATE_FIELD_DESC = new org.apache.thrift.protocol.TField("state", org.apache.thrift.protocol.TType.BYTE, (short)4);
  private static final org.apache.thrift.protocol.TField CREATOR_ID_FIELD_DESC = new org.apache.thrift.protocol.TField("creatorId", org.apache.thrift.protocol.TType.I32, (short)5);
  private static final org.apache.thrift.protocol.TField START_TIME_FIELD_DESC = new org.apache.thrift.protocol.TField("startTime", org.apache.thrift.protocol.TType.STRING, (short)6);
  private static final org.apache.thrift.protocol.TField CREATE_TIME_FIELD_DESC = new org.apache.thrift.protocol.TField("createTime", org.apache.thrift.protocol.TType.STRING, (short)7);
  private static final org.apache.thrift.protocol.TField UPDATE_TIME_FIELD_DESC = new org.apache.thrift.protocol.TField("updateTime", org.apache.thrift.protocol.TType.STRING, (short)8);

  private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();
  static {
    schemes.put(StandardScheme.class, new GameTrStandardSchemeFactory());
    schemes.put(TupleScheme.class, new GameTrTupleSchemeFactory());
  }

  public int id; // required
  public String metagameId; // required
  public int duration; // required
  public byte state; // required
  public int creatorId; // required
  public String startTime; // required
  public String createTime; // required
  public String updateTime; // required

  /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
  public enum _Fields implements org.apache.thrift.TFieldIdEnum {
    ID((short)1, "id"),
    METAGAME_ID((short)2, "metagameId"),
    DURATION((short)3, "duration"),
    STATE((short)4, "state"),
    CREATOR_ID((short)5, "creatorId"),
    START_TIME((short)6, "startTime"),
    CREATE_TIME((short)7, "createTime"),
    UPDATE_TIME((short)8, "updateTime");

    private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

    static {
      for (_Fields field : EnumSet.allOf(_Fields.class)) {
        byName.put(field.getFieldName(), field);
      }
    }

    /**
     * Find the _Fields constant that matches fieldId, or null if its not found.
     */
    public static _Fields findByThriftId(int fieldId) {
      switch(fieldId) {
        case 1: // ID
          return ID;
        case 2: // METAGAME_ID
          return METAGAME_ID;
        case 3: // DURATION
          return DURATION;
        case 4: // STATE
          return STATE;
        case 5: // CREATOR_ID
          return CREATOR_ID;
        case 6: // START_TIME
          return START_TIME;
        case 7: // CREATE_TIME
          return CREATE_TIME;
        case 8: // UPDATE_TIME
          return UPDATE_TIME;
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
      if (fields == null) throw new IllegalArgumentException("Field " + fieldId + " doesn't exist!");
      return fields;
    }

    /**
     * Find the _Fields constant that matches name, or null if its not found.
     */
    public static _Fields findByName(String name) {
      return byName.get(name);
    }

    private final short _thriftId;
    private final String _fieldName;

    _Fields(short thriftId, String fieldName) {
      _thriftId = thriftId;
      _fieldName = fieldName;
    }

    public short getThriftFieldId() {
      return _thriftId;
    }

    public String getFieldName() {
      return _fieldName;
    }
  }

  // isset id assignments
  private static final int __ID_ISSET_ID = 0;
  private static final int __DURATION_ISSET_ID = 1;
  private static final int __STATE_ISSET_ID = 2;
  private static final int __CREATORID_ISSET_ID = 3;
  private byte __isset_bitfield = 0;
  public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;
  static {
    Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
    tmpMap.put(_Fields.ID, new org.apache.thrift.meta_data.FieldMetaData("id", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I32)));
    tmpMap.put(_Fields.METAGAME_ID, new org.apache.thrift.meta_data.FieldMetaData("metagameId", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
    tmpMap.put(_Fields.DURATION, new org.apache.thrift.meta_data.FieldMetaData("duration", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I32)));
    tmpMap.put(_Fields.STATE, new org.apache.thrift.meta_data.FieldMetaData("state", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.BYTE)));
    tmpMap.put(_Fields.CREATOR_ID, new org.apache.thrift.meta_data.FieldMetaData("creatorId", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I32)));
    tmpMap.put(_Fields.START_TIME, new org.apache.thrift.meta_data.FieldMetaData("startTime", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
    tmpMap.put(_Fields.CREATE_TIME, new org.apache.thrift.meta_data.FieldMetaData("createTime", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
    tmpMap.put(_Fields.UPDATE_TIME, new org.apache.thrift.meta_data.FieldMetaData("updateTime", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
    metaDataMap = Collections.unmodifiableMap(tmpMap);
    org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(GameTr.class, metaDataMap);
  }

  public GameTr() {
  }

  public GameTr(
    int id,
    String metagameId,
    int duration,
    byte state,
    int creatorId,
    String startTime,
    String createTime,
    String updateTime)
  {
    this();
    this.id = id;
    setIdIsSet(true);
    this.metagameId = metagameId;
    this.duration = duration;
    setDurationIsSet(true);
    this.state = state;
    setStateIsSet(true);
    this.creatorId = creatorId;
    setCreatorIdIsSet(true);
    this.startTime = startTime;
    this.createTime = createTime;
    this.updateTime = updateTime;
  }

  /**
   * Performs a deep copy on <i>other</i>.
   */
  public GameTr(GameTr other) {
    __isset_bitfield = other.__isset_bitfield;
    this.id = other.id;
    if (other.isSetMetagameId()) {
      this.metagameId = other.metagameId;
    }
    this.duration = other.duration;
    this.state = other.state;
    this.creatorId = other.creatorId;
    if (other.isSetStartTime()) {
      this.startTime = other.startTime;
    }
    if (other.isSetCreateTime()) {
      this.createTime = other.createTime;
    }
    if (other.isSetUpdateTime()) {
      this.updateTime = other.updateTime;
    }
  }

  public GameTr deepCopy() {
    return new GameTr(this);
  }

  @Override
  public void clear() {
    setIdIsSet(false);
    this.id = 0;
    this.metagameId = null;
    setDurationIsSet(false);
    this.duration = 0;
    setStateIsSet(false);
    this.state = 0;
    setCreatorIdIsSet(false);
    this.creatorId = 0;
    this.startTime = null;
    this.createTime = null;
    this.updateTime = null;
  }

  public int getId() {
    return this.id;
  }

  public GameTr setId(int id) {
    this.id = id;
    setIdIsSet(true);
    return this;
  }

  public void unsetId() {
    __isset_bitfield = EncodingUtils.clearBit(__isset_bitfield, __ID_ISSET_ID);
  }

  /** Returns true if field id is set (has been assigned a value) and false otherwise */
  public boolean isSetId() {
    return EncodingUtils.testBit(__isset_bitfield, __ID_ISSET_ID);
  }

  public void setIdIsSet(boolean value) {
    __isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __ID_ISSET_ID, value);
  }

  public String getMetagameId() {
    return this.metagameId;
  }

  public GameTr setMetagameId(String metagameId) {
    this.metagameId = metagameId;
    return this;
  }

  public void unsetMetagameId() {
    this.metagameId = null;
  }

  /** Returns true if field metagameId is set (has been assigned a value) and false otherwise */
  public boolean isSetMetagameId() {
    return this.metagameId != null;
  }

  public void setMetagameIdIsSet(boolean value) {
    if (!value) {
      this.metagameId = null;
    }
  }

  public int getDuration() {
    return this.duration;
  }

  public GameTr setDuration(int duration) {
    this.duration = duration;
    setDurationIsSet(true);
    return this;
  }

  public void unsetDuration() {
    __isset_bitfield = EncodingUtils.clearBit(__isset_bitfield, __DURATION_ISSET_ID);
  }

  /** Returns true if field duration is set (has been assigned a value) and false otherwise */
  public boolean isSetDuration() {
    return EncodingUtils.testBit(__isset_bitfield, __DURATION_ISSET_ID);
  }

  public void setDurationIsSet(boolean value) {
    __isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __DURATION_ISSET_ID, value);
  }

  public byte getState() {
    return this.state;
  }

  public GameTr setState(byte state) {
    this.state = state;
    setStateIsSet(true);
    return this;
  }

  public void unsetState() {
    __isset_bitfield = EncodingUtils.clearBit(__isset_bitfield, __STATE_ISSET_ID);
  }

  /** Returns true if field state is set (has been assigned a value) and false otherwise */
  public boolean isSetState() {
    return EncodingUtils.testBit(__isset_bitfield, __STATE_ISSET_ID);
  }

  public void setStateIsSet(boolean value) {
    __isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __STATE_ISSET_ID, value);
  }

  public int getCreatorId() {
    return this.creatorId;
  }

  public GameTr setCreatorId(int creatorId) {
    this.creatorId = creatorId;
    setCreatorIdIsSet(true);
    return this;
  }

  public void unsetCreatorId() {
    __isset_bitfield = EncodingUtils.clearBit(__isset_bitfield, __CREATORID_ISSET_ID);
  }

  /** Returns true if field creatorId is set (has been assigned a value) and false otherwise */
  public boolean isSetCreatorId() {
    return EncodingUtils.testBit(__isset_bitfield, __CREATORID_ISSET_ID);
  }

  public void setCreatorIdIsSet(boolean value) {
    __isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __CREATORID_ISSET_ID, value);
  }

  public String getStartTime() {
    return this.startTime;
  }

  public GameTr setStartTime(String startTime) {
    this.startTime = startTime;
    return this;
  }

  public void unsetStartTime() {
    this.startTime = null;
  }

  /** Returns true if field startTime is set (has been assigned a value) and false otherwise */
  public boolean isSetStartTime() {
    return this.startTime != null;
  }

  public void setStartTimeIsSet(boolean value) {
    if (!value) {
      this.startTime = null;
    }
  }

  public String getCreateTime() {
    return this.createTime;
  }

  public GameTr setCreateTime(String createTime) {
    this.createTime = createTime;
    return this;
  }

  public void unsetCreateTime() {
    this.createTime = null;
  }

  /** Returns true if field createTime is set (has been assigned a value) and false otherwise */
  public boolean isSetCreateTime() {
    return this.createTime != null;
  }

  public void setCreateTimeIsSet(boolean value) {
    if (!value) {
      this.createTime = null;
    }
  }

  public String getUpdateTime() {
    return this.updateTime;
  }

  public GameTr setUpdateTime(String updateTime) {
    this.updateTime = updateTime;
    return this;
  }

  public void unsetUpdateTime() {
    this.updateTime = null;
  }

  /** Returns true if field updateTime is set (has been assigned a value) and false otherwise */
  public boolean isSetUpdateTime() {
    return this.updateTime != null;
  }

  public void setUpdateTimeIsSet(boolean value) {
    if (!value) {
      this.updateTime = null;
    }
  }

  public void setFieldValue(_Fields field, Object value) {
    switch (field) {
    case ID:
      if (value == null) {
        unsetId();
      } else {
        setId((Integer)value);
      }
      break;

    case METAGAME_ID:
      if (value == null) {
        unsetMetagameId();
      } else {
        setMetagameId((String)value);
      }
      break;

    case DURATION:
      if (value == null) {
        unsetDuration();
      } else {
        setDuration((Integer)value);
      }
      break;

    case STATE:
      if (value == null) {
        unsetState();
      } else {
        setState((Byte)value);
      }
      break;

    case CREATOR_ID:
      if (value == null) {
        unsetCreatorId();
      } else {
        setCreatorId((Integer)value);
      }
      break;

    case START_TIME:
      if (value == null) {
        unsetStartTime();
      } else {
        setStartTime((String)value);
      }
      break;

    case CREATE_TIME:
      if (value == null) {
        unsetCreateTime();
      } else {
        setCreateTime((String)value);
      }
      break;

    case UPDATE_TIME:
      if (value == null) {
        unsetUpdateTime();
      } else {
        setUpdateTime((String)value);
      }
      break;

    }
  }

  public Object getFieldValue(_Fields field) {
    switch (field) {
    case ID:
      return getId();

    case METAGAME_ID:
      return getMetagameId();

    case DURATION:
      return getDuration();

    case STATE:
      return getState();

    case CREATOR_ID:
      return getCreatorId();

    case START_TIME:
      return getStartTime();

    case CREATE_TIME:
      return getCreateTime();

    case UPDATE_TIME:
      return getUpdateTime();

    }
    throw new IllegalStateException();
  }

  /** Returns true if field corresponding to fieldID is set (has been assigned a value) and false otherwise */
  public boolean isSet(_Fields field) {
    if (field == null) {
      throw new IllegalArgumentException();
    }

    switch (field) {
    case ID:
      return isSetId();
    case METAGAME_ID:
      return isSetMetagameId();
    case DURATION:
      return isSetDuration();
    case STATE:
      return isSetState();
    case CREATOR_ID:
      return isSetCreatorId();
    case START_TIME:
      return isSetStartTime();
    case CREATE_TIME:
      return isSetCreateTime();
    case UPDATE_TIME:
      return isSetUpdateTime();
    }
    throw new IllegalStateException();
  }

  @Override
  public boolean equals(Object that) {
    if (that == null)
      return false;
    if (that instanceof GameTr)
      return this.equals((GameTr)that);
    return false;
  }

  public boolean equals(GameTr that) {
    if (that == null)
      return false;

    boolean this_present_id = true;
    boolean that_present_id = true;
    if (this_present_id || that_present_id) {
      if (!(this_present_id && that_present_id))
        return false;
      if (this.id != that.id)
        return false;
    }

    boolean this_present_metagameId = true && this.isSetMetagameId();
    boolean that_present_metagameId = true && that.isSetMetagameId();
    if (this_present_metagameId || that_present_metagameId) {
      if (!(this_present_metagameId && that_present_metagameId))
        return false;
      if (!this.metagameId.equals(that.metagameId))
        return false;
    }

    boolean this_present_duration = true;
    boolean that_present_duration = true;
    if (this_present_duration || that_present_duration) {
      if (!(this_present_duration && that_present_duration))
        return false;
      if (this.duration != that.duration)
        return false;
    }

    boolean this_present_state = true;
    boolean that_present_state = true;
    if (this_present_state || that_present_state) {
      if (!(this_present_state && that_present_state))
        return false;
      if (this.state != that.state)
        return false;
    }

    boolean this_present_creatorId = true;
    boolean that_present_creatorId = true;
    if (this_present_creatorId || that_present_creatorId) {
      if (!(this_present_creatorId && that_present_creatorId))
        return false;
      if (this.creatorId != that.creatorId)
        return false;
    }

    boolean this_present_startTime = true && this.isSetStartTime();
    boolean that_present_startTime = true && that.isSetStartTime();
    if (this_present_startTime || that_present_startTime) {
      if (!(this_present_startTime && that_present_startTime))
        return false;
      if (!this.startTime.equals(that.startTime))
        return false;
    }

    boolean this_present_createTime = true && this.isSetCreateTime();
    boolean that_present_createTime = true && that.isSetCreateTime();
    if (this_present_createTime || that_present_createTime) {
      if (!(this_present_createTime && that_present_createTime))
        return false;
      if (!this.createTime.equals(that.createTime))
        return false;
    }

    boolean this_present_updateTime = true && this.isSetUpdateTime();
    boolean that_present_updateTime = true && that.isSetUpdateTime();
    if (this_present_updateTime || that_present_updateTime) {
      if (!(this_present_updateTime && that_present_updateTime))
        return false;
      if (!this.updateTime.equals(that.updateTime))
        return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    List<Object> list = new ArrayList<Object>();

    boolean present_id = true;
    list.add(present_id);
    if (present_id)
      list.add(id);

    boolean present_metagameId = true && (isSetMetagameId());
    list.add(present_metagameId);
    if (present_metagameId)
      list.add(metagameId);

    boolean present_duration = true;
    list.add(present_duration);
    if (present_duration)
      list.add(duration);

    boolean present_state = true;
    list.add(present_state);
    if (present_state)
      list.add(state);

    boolean present_creatorId = true;
    list.add(present_creatorId);
    if (present_creatorId)
      list.add(creatorId);

    boolean present_startTime = true && (isSetStartTime());
    list.add(present_startTime);
    if (present_startTime)
      list.add(startTime);

    boolean present_createTime = true && (isSetCreateTime());
    list.add(present_createTime);
    if (present_createTime)
      list.add(createTime);

    boolean present_updateTime = true && (isSetUpdateTime());
    list.add(present_updateTime);
    if (present_updateTime)
      list.add(updateTime);

    return list.hashCode();
  }

  @Override
  public int compareTo(GameTr other) {
    if (!getClass().equals(other.getClass())) {
      return getClass().getName().compareTo(other.getClass().getName());
    }

    int lastComparison = 0;

    lastComparison = Boolean.valueOf(isSetId()).compareTo(other.isSetId());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetId()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.id, other.id);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetMetagameId()).compareTo(other.isSetMetagameId());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetMetagameId()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.metagameId, other.metagameId);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetDuration()).compareTo(other.isSetDuration());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetDuration()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.duration, other.duration);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetState()).compareTo(other.isSetState());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetState()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.state, other.state);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetCreatorId()).compareTo(other.isSetCreatorId());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetCreatorId()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.creatorId, other.creatorId);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetStartTime()).compareTo(other.isSetStartTime());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetStartTime()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.startTime, other.startTime);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetCreateTime()).compareTo(other.isSetCreateTime());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetCreateTime()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.createTime, other.createTime);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetUpdateTime()).compareTo(other.isSetUpdateTime());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetUpdateTime()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.updateTime, other.updateTime);
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
    schemes.get(iprot.getScheme()).getScheme().read(iprot, this);
  }

  public void write(org.apache.thrift.protocol.TProtocol oprot) throws org.apache.thrift.TException {
    schemes.get(oprot.getScheme()).getScheme().write(oprot, this);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("GameTr(");
    boolean first = true;

    sb.append("id:");
    sb.append(this.id);
    first = false;
    if (!first) sb.append(", ");
    sb.append("metagameId:");
    if (this.metagameId == null) {
      sb.append("null");
    } else {
      sb.append(this.metagameId);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("duration:");
    sb.append(this.duration);
    first = false;
    if (!first) sb.append(", ");
    sb.append("state:");
    sb.append(this.state);
    first = false;
    if (!first) sb.append(", ");
    sb.append("creatorId:");
    sb.append(this.creatorId);
    first = false;
    if (!first) sb.append(", ");
    sb.append("startTime:");
    if (this.startTime == null) {
      sb.append("null");
    } else {
      sb.append(this.startTime);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("createTime:");
    if (this.createTime == null) {
      sb.append("null");
    } else {
      sb.append(this.createTime);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("updateTime:");
    if (this.updateTime == null) {
      sb.append("null");
    } else {
      sb.append(this.updateTime);
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

  private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
    try {
      // it doesn't seem like you should have to do this, but java serialization is wacky, and doesn't call the default constructor.
      __isset_bitfield = 0;
      read(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(in)));
    } catch (org.apache.thrift.TException te) {
      throw new java.io.IOException(te);
    }
  }

  private static class GameTrStandardSchemeFactory implements SchemeFactory {
    public GameTrStandardScheme getScheme() {
      return new GameTrStandardScheme();
    }
  }

  private static class GameTrStandardScheme extends StandardScheme<GameTr> {

    public void read(org.apache.thrift.protocol.TProtocol iprot, GameTr struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TField schemeField;
      iprot.readStructBegin();
      while (true)
      {
        schemeField = iprot.readFieldBegin();
        if (schemeField.type == org.apache.thrift.protocol.TType.STOP) { 
          break;
        }
        switch (schemeField.id) {
          case 1: // ID
            if (schemeField.type == org.apache.thrift.protocol.TType.I32) {
              struct.id = iprot.readI32();
              struct.setIdIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 2: // METAGAME_ID
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.metagameId = iprot.readString();
              struct.setMetagameIdIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 3: // DURATION
            if (schemeField.type == org.apache.thrift.protocol.TType.I32) {
              struct.duration = iprot.readI32();
              struct.setDurationIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 4: // STATE
            if (schemeField.type == org.apache.thrift.protocol.TType.BYTE) {
              struct.state = iprot.readByte();
              struct.setStateIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 5: // CREATOR_ID
            if (schemeField.type == org.apache.thrift.protocol.TType.I32) {
              struct.creatorId = iprot.readI32();
              struct.setCreatorIdIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 6: // START_TIME
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.startTime = iprot.readString();
              struct.setStartTimeIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 7: // CREATE_TIME
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.createTime = iprot.readString();
              struct.setCreateTimeIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 8: // UPDATE_TIME
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.updateTime = iprot.readString();
              struct.setUpdateTimeIsSet(true);
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

    public void write(org.apache.thrift.protocol.TProtocol oprot, GameTr struct) throws org.apache.thrift.TException {
      struct.validate();

      oprot.writeStructBegin(STRUCT_DESC);
      oprot.writeFieldBegin(ID_FIELD_DESC);
      oprot.writeI32(struct.id);
      oprot.writeFieldEnd();
      if (struct.metagameId != null) {
        oprot.writeFieldBegin(METAGAME_ID_FIELD_DESC);
        oprot.writeString(struct.metagameId);
        oprot.writeFieldEnd();
      }
      oprot.writeFieldBegin(DURATION_FIELD_DESC);
      oprot.writeI32(struct.duration);
      oprot.writeFieldEnd();
      oprot.writeFieldBegin(STATE_FIELD_DESC);
      oprot.writeByte(struct.state);
      oprot.writeFieldEnd();
      oprot.writeFieldBegin(CREATOR_ID_FIELD_DESC);
      oprot.writeI32(struct.creatorId);
      oprot.writeFieldEnd();
      if (struct.startTime != null) {
        oprot.writeFieldBegin(START_TIME_FIELD_DESC);
        oprot.writeString(struct.startTime);
        oprot.writeFieldEnd();
      }
      if (struct.createTime != null) {
        oprot.writeFieldBegin(CREATE_TIME_FIELD_DESC);
        oprot.writeString(struct.createTime);
        oprot.writeFieldEnd();
      }
      if (struct.updateTime != null) {
        oprot.writeFieldBegin(UPDATE_TIME_FIELD_DESC);
        oprot.writeString(struct.updateTime);
        oprot.writeFieldEnd();
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }

  }

  private static class GameTrTupleSchemeFactory implements SchemeFactory {
    public GameTrTupleScheme getScheme() {
      return new GameTrTupleScheme();
    }
  }

  private static class GameTrTupleScheme extends TupleScheme<GameTr> {

    @Override
    public void write(org.apache.thrift.protocol.TProtocol prot, GameTr struct) throws org.apache.thrift.TException {
      TTupleProtocol oprot = (TTupleProtocol) prot;
      BitSet optionals = new BitSet();
      if (struct.isSetId()) {
        optionals.set(0);
      }
      if (struct.isSetMetagameId()) {
        optionals.set(1);
      }
      if (struct.isSetDuration()) {
        optionals.set(2);
      }
      if (struct.isSetState()) {
        optionals.set(3);
      }
      if (struct.isSetCreatorId()) {
        optionals.set(4);
      }
      if (struct.isSetStartTime()) {
        optionals.set(5);
      }
      if (struct.isSetCreateTime()) {
        optionals.set(6);
      }
      if (struct.isSetUpdateTime()) {
        optionals.set(7);
      }
      oprot.writeBitSet(optionals, 8);
      if (struct.isSetId()) {
        oprot.writeI32(struct.id);
      }
      if (struct.isSetMetagameId()) {
        oprot.writeString(struct.metagameId);
      }
      if (struct.isSetDuration()) {
        oprot.writeI32(struct.duration);
      }
      if (struct.isSetState()) {
        oprot.writeByte(struct.state);
      }
      if (struct.isSetCreatorId()) {
        oprot.writeI32(struct.creatorId);
      }
      if (struct.isSetStartTime()) {
        oprot.writeString(struct.startTime);
      }
      if (struct.isSetCreateTime()) {
        oprot.writeString(struct.createTime);
      }
      if (struct.isSetUpdateTime()) {
        oprot.writeString(struct.updateTime);
      }
    }

    @Override
    public void read(org.apache.thrift.protocol.TProtocol prot, GameTr struct) throws org.apache.thrift.TException {
      TTupleProtocol iprot = (TTupleProtocol) prot;
      BitSet incoming = iprot.readBitSet(8);
      if (incoming.get(0)) {
        struct.id = iprot.readI32();
        struct.setIdIsSet(true);
      }
      if (incoming.get(1)) {
        struct.metagameId = iprot.readString();
        struct.setMetagameIdIsSet(true);
      }
      if (incoming.get(2)) {
        struct.duration = iprot.readI32();
        struct.setDurationIsSet(true);
      }
      if (incoming.get(3)) {
        struct.state = iprot.readByte();
        struct.setStateIsSet(true);
      }
      if (incoming.get(4)) {
        struct.creatorId = iprot.readI32();
        struct.setCreatorIdIsSet(true);
      }
      if (incoming.get(5)) {
        struct.startTime = iprot.readString();
        struct.setStartTimeIsSet(true);
      }
      if (incoming.get(6)) {
        struct.createTime = iprot.readString();
        struct.setCreateTimeIsSet(true);
      }
      if (incoming.get(7)) {
        struct.updateTime = iprot.readString();
        struct.setUpdateTimeIsSet(true);
      }
    }
  }

}

