/**
 * Autogenerated by Thrift Compiler (0.9.3)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
package com.prosper.chasing.common.interfaces.game;

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
@Generated(value = "Autogenerated by Thrift Compiler (0.9.3)", date = "2016-03-01")
public class GameMessageService {

  public interface Iface {

    public void send(ByteBuffer bs) throws GameException, org.apache.thrift.TException;

  }

  public interface AsyncIface {

    public void send(ByteBuffer bs, org.apache.thrift.async.AsyncMethodCallback resultHandler) throws org.apache.thrift.TException;

  }

  public static class Client extends org.apache.thrift.TServiceClient implements Iface {
    public static class Factory implements org.apache.thrift.TServiceClientFactory<Client> {
      public Factory() {}
      public Client getClient(org.apache.thrift.protocol.TProtocol prot) {
        return new Client(prot);
      }
      public Client getClient(org.apache.thrift.protocol.TProtocol iprot, org.apache.thrift.protocol.TProtocol oprot) {
        return new Client(iprot, oprot);
      }
    }

    public Client(org.apache.thrift.protocol.TProtocol prot)
    {
      super(prot, prot);
    }

    public Client(org.apache.thrift.protocol.TProtocol iprot, org.apache.thrift.protocol.TProtocol oprot) {
      super(iprot, oprot);
    }

    public void send(ByteBuffer bs) throws GameException, org.apache.thrift.TException
    {
      send_send(bs);
      recv_send();
    }

    public void send_send(ByteBuffer bs) throws org.apache.thrift.TException
    {
      send_args args = new send_args();
      args.setBs(bs);
      sendBase("send", args);
    }

    public void recv_send() throws GameException, org.apache.thrift.TException
    {
      send_result result = new send_result();
      receiveBase(result, "send");
      if (result.ex != null) {
        throw result.ex;
      }
      return;
    }

  }
  public static class AsyncClient extends org.apache.thrift.async.TAsyncClient implements AsyncIface {
    public static class Factory implements org.apache.thrift.async.TAsyncClientFactory<AsyncClient> {
      private org.apache.thrift.async.TAsyncClientManager clientManager;
      private org.apache.thrift.protocol.TProtocolFactory protocolFactory;
      public Factory(org.apache.thrift.async.TAsyncClientManager clientManager, org.apache.thrift.protocol.TProtocolFactory protocolFactory) {
        this.clientManager = clientManager;
        this.protocolFactory = protocolFactory;
      }
      public AsyncClient getAsyncClient(org.apache.thrift.transport.TNonblockingTransport transport) {
        return new AsyncClient(protocolFactory, clientManager, transport);
      }
    }

    public AsyncClient(org.apache.thrift.protocol.TProtocolFactory protocolFactory, org.apache.thrift.async.TAsyncClientManager clientManager, org.apache.thrift.transport.TNonblockingTransport transport) {
      super(protocolFactory, clientManager, transport);
    }

    public void send(ByteBuffer bs, org.apache.thrift.async.AsyncMethodCallback resultHandler) throws org.apache.thrift.TException {
      checkReady();
      send_call method_call = new send_call(bs, resultHandler, this, ___protocolFactory, ___transport);
      this.___currentMethod = method_call;
      ___manager.call(method_call);
    }

    public static class send_call extends org.apache.thrift.async.TAsyncMethodCall {
      private ByteBuffer bs;
      public send_call(ByteBuffer bs, org.apache.thrift.async.AsyncMethodCallback resultHandler, org.apache.thrift.async.TAsyncClient client, org.apache.thrift.protocol.TProtocolFactory protocolFactory, org.apache.thrift.transport.TNonblockingTransport transport) throws org.apache.thrift.TException {
        super(client, protocolFactory, transport, resultHandler, false);
        this.bs = bs;
      }

      public void write_args(org.apache.thrift.protocol.TProtocol prot) throws org.apache.thrift.TException {
        prot.writeMessageBegin(new org.apache.thrift.protocol.TMessage("send", org.apache.thrift.protocol.TMessageType.CALL, 0));
        send_args args = new send_args();
        args.setBs(bs);
        args.write(prot);
        prot.writeMessageEnd();
      }

      public void getResult() throws GameException, org.apache.thrift.TException {
        if (getState() != org.apache.thrift.async.TAsyncMethodCall.State.RESPONSE_READ) {
          throw new IllegalStateException("Method call not finished!");
        }
        org.apache.thrift.transport.TMemoryInputTransport memoryTransport = new org.apache.thrift.transport.TMemoryInputTransport(getFrameBuffer().array());
        org.apache.thrift.protocol.TProtocol prot = client.getProtocolFactory().getProtocol(memoryTransport);
        (new Client(prot)).recv_send();
      }
    }

  }

  public static class Processor<I extends Iface> extends org.apache.thrift.TBaseProcessor<I> implements org.apache.thrift.TProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(Processor.class.getName());
    public Processor(I iface) {
      super(iface, getProcessMap(new HashMap<String, org.apache.thrift.ProcessFunction<I, ? extends org.apache.thrift.TBase>>()));
    }

    protected Processor(I iface, Map<String,  org.apache.thrift.ProcessFunction<I, ? extends  org.apache.thrift.TBase>> processMap) {
      super(iface, getProcessMap(processMap));
    }

    private static <I extends Iface> Map<String,  org.apache.thrift.ProcessFunction<I, ? extends  org.apache.thrift.TBase>> getProcessMap(Map<String,  org.apache.thrift.ProcessFunction<I, ? extends  org.apache.thrift.TBase>> processMap) {
      processMap.put("send", new send());
      return processMap;
    }

    public static class send<I extends Iface> extends org.apache.thrift.ProcessFunction<I, send_args> {
      public send() {
        super("send");
      }

      public send_args getEmptyArgsInstance() {
        return new send_args();
      }

      protected boolean isOneway() {
        return false;
      }

      public send_result getResult(I iface, send_args args) throws org.apache.thrift.TException {
        send_result result = new send_result();
        try {
          iface.send(args.bs);
        } catch (GameException ex) {
          result.ex = ex;
        }
        return result;
      }
    }

  }

  public static class AsyncProcessor<I extends AsyncIface> extends org.apache.thrift.TBaseAsyncProcessor<I> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AsyncProcessor.class.getName());
    public AsyncProcessor(I iface) {
      super(iface, getProcessMap(new HashMap<String, org.apache.thrift.AsyncProcessFunction<I, ? extends org.apache.thrift.TBase, ?>>()));
    }

    protected AsyncProcessor(I iface, Map<String,  org.apache.thrift.AsyncProcessFunction<I, ? extends  org.apache.thrift.TBase, ?>> processMap) {
      super(iface, getProcessMap(processMap));
    }

    private static <I extends AsyncIface> Map<String,  org.apache.thrift.AsyncProcessFunction<I, ? extends  org.apache.thrift.TBase,?>> getProcessMap(Map<String,  org.apache.thrift.AsyncProcessFunction<I, ? extends  org.apache.thrift.TBase, ?>> processMap) {
      processMap.put("send", new send());
      return processMap;
    }

    public static class send<I extends AsyncIface> extends org.apache.thrift.AsyncProcessFunction<I, send_args, Void> {
      public send() {
        super("send");
      }

      public send_args getEmptyArgsInstance() {
        return new send_args();
      }

      public AsyncMethodCallback<Void> getResultHandler(final AsyncFrameBuffer fb, final int seqid) {
        final org.apache.thrift.AsyncProcessFunction fcall = this;
        return new AsyncMethodCallback<Void>() { 
          public void onComplete(Void o) {
            send_result result = new send_result();
            try {
              fcall.sendResponse(fb,result, org.apache.thrift.protocol.TMessageType.REPLY,seqid);
              return;
            } catch (Exception e) {
              LOGGER.error("Exception writing to internal frame buffer", e);
            }
            fb.close();
          }
          public void onError(Exception e) {
            byte msgType = org.apache.thrift.protocol.TMessageType.REPLY;
            org.apache.thrift.TBase msg;
            send_result result = new send_result();
            if (e instanceof GameException) {
                        result.ex = (GameException) e;
                        result.setExIsSet(true);
                        msg = result;
            }
             else 
            {
              msgType = org.apache.thrift.protocol.TMessageType.EXCEPTION;
              msg = (org.apache.thrift.TBase)new org.apache.thrift.TApplicationException(org.apache.thrift.TApplicationException.INTERNAL_ERROR, e.getMessage());
            }
            try {
              fcall.sendResponse(fb,msg,msgType,seqid);
              return;
            } catch (Exception ex) {
              LOGGER.error("Exception writing to internal frame buffer", ex);
            }
            fb.close();
          }
        };
      }

      protected boolean isOneway() {
        return false;
      }

      public void start(I iface, send_args args, org.apache.thrift.async.AsyncMethodCallback<Void> resultHandler) throws TException {
        iface.send(args.bs,resultHandler);
      }
    }

  }

  public static class send_args implements org.apache.thrift.TBase<send_args, send_args._Fields>, java.io.Serializable, Cloneable, Comparable<send_args>   {
    private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("send_args");

    private static final org.apache.thrift.protocol.TField BS_FIELD_DESC = new org.apache.thrift.protocol.TField("bs", org.apache.thrift.protocol.TType.STRING, (short)1);

    private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();
    static {
      schemes.put(StandardScheme.class, new send_argsStandardSchemeFactory());
      schemes.put(TupleScheme.class, new send_argsTupleSchemeFactory());
    }

    public ByteBuffer bs; // required

    /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
    public enum _Fields implements org.apache.thrift.TFieldIdEnum {
      BS((short)1, "bs");

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
          case 1: // BS
            return BS;
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
    public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;
    static {
      Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
      tmpMap.put(_Fields.BS, new org.apache.thrift.meta_data.FieldMetaData("bs", org.apache.thrift.TFieldRequirementType.DEFAULT, 
          new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING          , true)));
      metaDataMap = Collections.unmodifiableMap(tmpMap);
      org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(send_args.class, metaDataMap);
    }

    public send_args() {
    }

    public send_args(
      ByteBuffer bs)
    {
      this();
      this.bs = org.apache.thrift.TBaseHelper.copyBinary(bs);
    }

    /**
     * Performs a deep copy on <i>other</i>.
     */
    public send_args(send_args other) {
      if (other.isSetBs()) {
        this.bs = org.apache.thrift.TBaseHelper.copyBinary(other.bs);
      }
    }

    public send_args deepCopy() {
      return new send_args(this);
    }

    @Override
    public void clear() {
      this.bs = null;
    }

    public byte[] getBs() {
      setBs(org.apache.thrift.TBaseHelper.rightSize(bs));
      return bs == null ? null : bs.array();
    }

    public ByteBuffer bufferForBs() {
      return org.apache.thrift.TBaseHelper.copyBinary(bs);
    }

    public send_args setBs(byte[] bs) {
      this.bs = bs == null ? (ByteBuffer)null : ByteBuffer.wrap(Arrays.copyOf(bs, bs.length));
      return this;
    }

    public send_args setBs(ByteBuffer bs) {
      this.bs = org.apache.thrift.TBaseHelper.copyBinary(bs);
      return this;
    }

    public void unsetBs() {
      this.bs = null;
    }

    /** Returns true if field bs is set (has been assigned a value) and false otherwise */
    public boolean isSetBs() {
      return this.bs != null;
    }

    public void setBsIsSet(boolean value) {
      if (!value) {
        this.bs = null;
      }
    }

    public void setFieldValue(_Fields field, Object value) {
      switch (field) {
      case BS:
        if (value == null) {
          unsetBs();
        } else {
          setBs((ByteBuffer)value);
        }
        break;

      }
    }

    public Object getFieldValue(_Fields field) {
      switch (field) {
      case BS:
        return getBs();

      }
      throw new IllegalStateException();
    }

    /** Returns true if field corresponding to fieldID is set (has been assigned a value) and false otherwise */
    public boolean isSet(_Fields field) {
      if (field == null) {
        throw new IllegalArgumentException();
      }

      switch (field) {
      case BS:
        return isSetBs();
      }
      throw new IllegalStateException();
    }

    @Override
    public boolean equals(Object that) {
      if (that == null)
        return false;
      if (that instanceof send_args)
        return this.equals((send_args)that);
      return false;
    }

    public boolean equals(send_args that) {
      if (that == null)
        return false;

      boolean this_present_bs = true && this.isSetBs();
      boolean that_present_bs = true && that.isSetBs();
      if (this_present_bs || that_present_bs) {
        if (!(this_present_bs && that_present_bs))
          return false;
        if (!this.bs.equals(that.bs))
          return false;
      }

      return true;
    }

    @Override
    public int hashCode() {
      List<Object> list = new ArrayList<Object>();

      boolean present_bs = true && (isSetBs());
      list.add(present_bs);
      if (present_bs)
        list.add(bs);

      return list.hashCode();
    }

    @Override
    public int compareTo(send_args other) {
      if (!getClass().equals(other.getClass())) {
        return getClass().getName().compareTo(other.getClass().getName());
      }

      int lastComparison = 0;

      lastComparison = Boolean.valueOf(isSetBs()).compareTo(other.isSetBs());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetBs()) {
        lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.bs, other.bs);
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
      StringBuilder sb = new StringBuilder("send_args(");
      boolean first = true;

      sb.append("bs:");
      if (this.bs == null) {
        sb.append("null");
      } else {
        org.apache.thrift.TBaseHelper.toString(this.bs, sb);
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
        read(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(in)));
      } catch (org.apache.thrift.TException te) {
        throw new java.io.IOException(te);
      }
    }

    private static class send_argsStandardSchemeFactory implements SchemeFactory {
      public send_argsStandardScheme getScheme() {
        return new send_argsStandardScheme();
      }
    }

    private static class send_argsStandardScheme extends StandardScheme<send_args> {

      public void read(org.apache.thrift.protocol.TProtocol iprot, send_args struct) throws org.apache.thrift.TException {
        org.apache.thrift.protocol.TField schemeField;
        iprot.readStructBegin();
        while (true)
        {
          schemeField = iprot.readFieldBegin();
          if (schemeField.type == org.apache.thrift.protocol.TType.STOP) { 
            break;
          }
          switch (schemeField.id) {
            case 1: // BS
              if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
                struct.bs = iprot.readBinary();
                struct.setBsIsSet(true);
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

      public void write(org.apache.thrift.protocol.TProtocol oprot, send_args struct) throws org.apache.thrift.TException {
        struct.validate();

        oprot.writeStructBegin(STRUCT_DESC);
        if (struct.bs != null) {
          oprot.writeFieldBegin(BS_FIELD_DESC);
          oprot.writeBinary(struct.bs);
          oprot.writeFieldEnd();
        }
        oprot.writeFieldStop();
        oprot.writeStructEnd();
      }

    }

    private static class send_argsTupleSchemeFactory implements SchemeFactory {
      public send_argsTupleScheme getScheme() {
        return new send_argsTupleScheme();
      }
    }

    private static class send_argsTupleScheme extends TupleScheme<send_args> {

      @Override
      public void write(org.apache.thrift.protocol.TProtocol prot, send_args struct) throws org.apache.thrift.TException {
        TTupleProtocol oprot = (TTupleProtocol) prot;
        BitSet optionals = new BitSet();
        if (struct.isSetBs()) {
          optionals.set(0);
        }
        oprot.writeBitSet(optionals, 1);
        if (struct.isSetBs()) {
          oprot.writeBinary(struct.bs);
        }
      }

      @Override
      public void read(org.apache.thrift.protocol.TProtocol prot, send_args struct) throws org.apache.thrift.TException {
        TTupleProtocol iprot = (TTupleProtocol) prot;
        BitSet incoming = iprot.readBitSet(1);
        if (incoming.get(0)) {
          struct.bs = iprot.readBinary();
          struct.setBsIsSet(true);
        }
      }
    }

  }

  public static class send_result implements org.apache.thrift.TBase<send_result, send_result._Fields>, java.io.Serializable, Cloneable, Comparable<send_result>   {
    private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("send_result");

    private static final org.apache.thrift.protocol.TField EX_FIELD_DESC = new org.apache.thrift.protocol.TField("ex", org.apache.thrift.protocol.TType.STRUCT, (short)1);

    private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();
    static {
      schemes.put(StandardScheme.class, new send_resultStandardSchemeFactory());
      schemes.put(TupleScheme.class, new send_resultTupleSchemeFactory());
    }

    public GameException ex; // required

    /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
    public enum _Fields implements org.apache.thrift.TFieldIdEnum {
      EX((short)1, "ex");

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
          case 1: // EX
            return EX;
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
    public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;
    static {
      Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
      tmpMap.put(_Fields.EX, new org.apache.thrift.meta_data.FieldMetaData("ex", org.apache.thrift.TFieldRequirementType.DEFAULT, 
          new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRUCT)));
      metaDataMap = Collections.unmodifiableMap(tmpMap);
      org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(send_result.class, metaDataMap);
    }

    public send_result() {
    }

    public send_result(
      GameException ex)
    {
      this();
      this.ex = ex;
    }

    /**
     * Performs a deep copy on <i>other</i>.
     */
    public send_result(send_result other) {
      if (other.isSetEx()) {
        this.ex = new GameException(other.ex);
      }
    }

    public send_result deepCopy() {
      return new send_result(this);
    }

    @Override
    public void clear() {
      this.ex = null;
    }

    public GameException getEx() {
      return this.ex;
    }

    public send_result setEx(GameException ex) {
      this.ex = ex;
      return this;
    }

    public void unsetEx() {
      this.ex = null;
    }

    /** Returns true if field ex is set (has been assigned a value) and false otherwise */
    public boolean isSetEx() {
      return this.ex != null;
    }

    public void setExIsSet(boolean value) {
      if (!value) {
        this.ex = null;
      }
    }

    public void setFieldValue(_Fields field, Object value) {
      switch (field) {
      case EX:
        if (value == null) {
          unsetEx();
        } else {
          setEx((GameException)value);
        }
        break;

      }
    }

    public Object getFieldValue(_Fields field) {
      switch (field) {
      case EX:
        return getEx();

      }
      throw new IllegalStateException();
    }

    /** Returns true if field corresponding to fieldID is set (has been assigned a value) and false otherwise */
    public boolean isSet(_Fields field) {
      if (field == null) {
        throw new IllegalArgumentException();
      }

      switch (field) {
      case EX:
        return isSetEx();
      }
      throw new IllegalStateException();
    }

    @Override
    public boolean equals(Object that) {
      if (that == null)
        return false;
      if (that instanceof send_result)
        return this.equals((send_result)that);
      return false;
    }

    public boolean equals(send_result that) {
      if (that == null)
        return false;

      boolean this_present_ex = true && this.isSetEx();
      boolean that_present_ex = true && that.isSetEx();
      if (this_present_ex || that_present_ex) {
        if (!(this_present_ex && that_present_ex))
          return false;
        if (!this.ex.equals(that.ex))
          return false;
      }

      return true;
    }

    @Override
    public int hashCode() {
      List<Object> list = new ArrayList<Object>();

      boolean present_ex = true && (isSetEx());
      list.add(present_ex);
      if (present_ex)
        list.add(ex);

      return list.hashCode();
    }

    @Override
    public int compareTo(send_result other) {
      if (!getClass().equals(other.getClass())) {
        return getClass().getName().compareTo(other.getClass().getName());
      }

      int lastComparison = 0;

      lastComparison = Boolean.valueOf(isSetEx()).compareTo(other.isSetEx());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetEx()) {
        lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.ex, other.ex);
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
      StringBuilder sb = new StringBuilder("send_result(");
      boolean first = true;

      sb.append("ex:");
      if (this.ex == null) {
        sb.append("null");
      } else {
        sb.append(this.ex);
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
        read(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(in)));
      } catch (org.apache.thrift.TException te) {
        throw new java.io.IOException(te);
      }
    }

    private static class send_resultStandardSchemeFactory implements SchemeFactory {
      public send_resultStandardScheme getScheme() {
        return new send_resultStandardScheme();
      }
    }

    private static class send_resultStandardScheme extends StandardScheme<send_result> {

      public void read(org.apache.thrift.protocol.TProtocol iprot, send_result struct) throws org.apache.thrift.TException {
        org.apache.thrift.protocol.TField schemeField;
        iprot.readStructBegin();
        while (true)
        {
          schemeField = iprot.readFieldBegin();
          if (schemeField.type == org.apache.thrift.protocol.TType.STOP) { 
            break;
          }
          switch (schemeField.id) {
            case 1: // EX
              if (schemeField.type == org.apache.thrift.protocol.TType.STRUCT) {
                struct.ex = new GameException();
                struct.ex.read(iprot);
                struct.setExIsSet(true);
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

      public void write(org.apache.thrift.protocol.TProtocol oprot, send_result struct) throws org.apache.thrift.TException {
        struct.validate();

        oprot.writeStructBegin(STRUCT_DESC);
        if (struct.ex != null) {
          oprot.writeFieldBegin(EX_FIELD_DESC);
          struct.ex.write(oprot);
          oprot.writeFieldEnd();
        }
        oprot.writeFieldStop();
        oprot.writeStructEnd();
      }

    }

    private static class send_resultTupleSchemeFactory implements SchemeFactory {
      public send_resultTupleScheme getScheme() {
        return new send_resultTupleScheme();
      }
    }

    private static class send_resultTupleScheme extends TupleScheme<send_result> {

      @Override
      public void write(org.apache.thrift.protocol.TProtocol prot, send_result struct) throws org.apache.thrift.TException {
        TTupleProtocol oprot = (TTupleProtocol) prot;
        BitSet optionals = new BitSet();
        if (struct.isSetEx()) {
          optionals.set(0);
        }
        oprot.writeBitSet(optionals, 1);
        if (struct.isSetEx()) {
          struct.ex.write(oprot);
        }
      }

      @Override
      public void read(org.apache.thrift.protocol.TProtocol prot, send_result struct) throws org.apache.thrift.TException {
        TTupleProtocol iprot = (TTupleProtocol) prot;
        BitSet incoming = iprot.readBitSet(1);
        if (incoming.get(0)) {
          struct.ex = new GameException();
          struct.ex.read(iprot);
          struct.setExIsSet(true);
        }
      }
    }

  }

}
