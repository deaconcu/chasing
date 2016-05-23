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
@Generated(value = "Autogenerated by Thrift Compiler (0.9.3)", date = "2016-05-23")
public class MetagameDataService {

  public interface Iface {

    public List<MetagameTr> getMetagame(List<Integer> metagameIdList) throws org.apache.thrift.TException;

  }

  public interface AsyncIface {

    public void getMetagame(List<Integer> metagameIdList, org.apache.thrift.async.AsyncMethodCallback resultHandler) throws org.apache.thrift.TException;

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

    public List<MetagameTr> getMetagame(List<Integer> metagameIdList) throws org.apache.thrift.TException
    {
      send_getMetagame(metagameIdList);
      return recv_getMetagame();
    }

    public void send_getMetagame(List<Integer> metagameIdList) throws org.apache.thrift.TException
    {
      getMetagame_args args = new getMetagame_args();
      args.setMetagameIdList(metagameIdList);
      sendBase("getMetagame", args);
    }

    public List<MetagameTr> recv_getMetagame() throws org.apache.thrift.TException
    {
      getMetagame_result result = new getMetagame_result();
      receiveBase(result, "getMetagame");
      if (result.isSetSuccess()) {
        return result.success;
      }
      throw new org.apache.thrift.TApplicationException(org.apache.thrift.TApplicationException.MISSING_RESULT, "getMetagame failed: unknown result");
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

    public void getMetagame(List<Integer> metagameIdList, org.apache.thrift.async.AsyncMethodCallback resultHandler) throws org.apache.thrift.TException {
      checkReady();
      getMetagame_call method_call = new getMetagame_call(metagameIdList, resultHandler, this, ___protocolFactory, ___transport);
      this.___currentMethod = method_call;
      ___manager.call(method_call);
    }

    public static class getMetagame_call extends org.apache.thrift.async.TAsyncMethodCall {
      private List<Integer> metagameIdList;
      public getMetagame_call(List<Integer> metagameIdList, org.apache.thrift.async.AsyncMethodCallback resultHandler, org.apache.thrift.async.TAsyncClient client, org.apache.thrift.protocol.TProtocolFactory protocolFactory, org.apache.thrift.transport.TNonblockingTransport transport) throws org.apache.thrift.TException {
        super(client, protocolFactory, transport, resultHandler, false);
        this.metagameIdList = metagameIdList;
      }

      public void write_args(org.apache.thrift.protocol.TProtocol prot) throws org.apache.thrift.TException {
        prot.writeMessageBegin(new org.apache.thrift.protocol.TMessage("getMetagame", org.apache.thrift.protocol.TMessageType.CALL, 0));
        getMetagame_args args = new getMetagame_args();
        args.setMetagameIdList(metagameIdList);
        args.write(prot);
        prot.writeMessageEnd();
      }

      public List<MetagameTr> getResult() throws org.apache.thrift.TException {
        if (getState() != org.apache.thrift.async.TAsyncMethodCall.State.RESPONSE_READ) {
          throw new IllegalStateException("Method call not finished!");
        }
        org.apache.thrift.transport.TMemoryInputTransport memoryTransport = new org.apache.thrift.transport.TMemoryInputTransport(getFrameBuffer().array());
        org.apache.thrift.protocol.TProtocol prot = client.getProtocolFactory().getProtocol(memoryTransport);
        return (new Client(prot)).recv_getMetagame();
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
      processMap.put("getMetagame", new getMetagame());
      return processMap;
    }

    public static class getMetagame<I extends Iface> extends org.apache.thrift.ProcessFunction<I, getMetagame_args> {
      public getMetagame() {
        super("getMetagame");
      }

      public getMetagame_args getEmptyArgsInstance() {
        return new getMetagame_args();
      }

      protected boolean isOneway() {
        return false;
      }

      public getMetagame_result getResult(I iface, getMetagame_args args) throws org.apache.thrift.TException {
        getMetagame_result result = new getMetagame_result();
        result.success = iface.getMetagame(args.metagameIdList);
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
      processMap.put("getMetagame", new getMetagame());
      return processMap;
    }

    public static class getMetagame<I extends AsyncIface> extends org.apache.thrift.AsyncProcessFunction<I, getMetagame_args, List<MetagameTr>> {
      public getMetagame() {
        super("getMetagame");
      }

      public getMetagame_args getEmptyArgsInstance() {
        return new getMetagame_args();
      }

      public AsyncMethodCallback<List<MetagameTr>> getResultHandler(final AsyncFrameBuffer fb, final int seqid) {
        final org.apache.thrift.AsyncProcessFunction fcall = this;
        return new AsyncMethodCallback<List<MetagameTr>>() { 
          public void onComplete(List<MetagameTr> o) {
            getMetagame_result result = new getMetagame_result();
            result.success = o;
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
            getMetagame_result result = new getMetagame_result();
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

      public void start(I iface, getMetagame_args args, org.apache.thrift.async.AsyncMethodCallback<List<MetagameTr>> resultHandler) throws TException {
        iface.getMetagame(args.metagameIdList,resultHandler);
      }
    }

  }

  public static class getMetagame_args implements org.apache.thrift.TBase<getMetagame_args, getMetagame_args._Fields>, java.io.Serializable, Cloneable, Comparable<getMetagame_args>   {
    private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("getMetagame_args");

    private static final org.apache.thrift.protocol.TField METAGAME_ID_LIST_FIELD_DESC = new org.apache.thrift.protocol.TField("metagameIdList", org.apache.thrift.protocol.TType.LIST, (short)1);

    private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();
    static {
      schemes.put(StandardScheme.class, new getMetagame_argsStandardSchemeFactory());
      schemes.put(TupleScheme.class, new getMetagame_argsTupleSchemeFactory());
    }

    public List<Integer> metagameIdList; // required

    /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
    public enum _Fields implements org.apache.thrift.TFieldIdEnum {
      METAGAME_ID_LIST((short)1, "metagameIdList");

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
          case 1: // METAGAME_ID_LIST
            return METAGAME_ID_LIST;
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
      tmpMap.put(_Fields.METAGAME_ID_LIST, new org.apache.thrift.meta_data.FieldMetaData("metagameIdList", org.apache.thrift.TFieldRequirementType.DEFAULT, 
          new org.apache.thrift.meta_data.ListMetaData(org.apache.thrift.protocol.TType.LIST, 
              new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I32))));
      metaDataMap = Collections.unmodifiableMap(tmpMap);
      org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(getMetagame_args.class, metaDataMap);
    }

    public getMetagame_args() {
    }

    public getMetagame_args(
      List<Integer> metagameIdList)
    {
      this();
      this.metagameIdList = metagameIdList;
    }

    /**
     * Performs a deep copy on <i>other</i>.
     */
    public getMetagame_args(getMetagame_args other) {
      if (other.isSetMetagameIdList()) {
        List<Integer> __this__metagameIdList = new ArrayList<Integer>(other.metagameIdList);
        this.metagameIdList = __this__metagameIdList;
      }
    }

    public getMetagame_args deepCopy() {
      return new getMetagame_args(this);
    }

    @Override
    public void clear() {
      this.metagameIdList = null;
    }

    public int getMetagameIdListSize() {
      return (this.metagameIdList == null) ? 0 : this.metagameIdList.size();
    }

    public java.util.Iterator<Integer> getMetagameIdListIterator() {
      return (this.metagameIdList == null) ? null : this.metagameIdList.iterator();
    }

    public void addToMetagameIdList(int elem) {
      if (this.metagameIdList == null) {
        this.metagameIdList = new ArrayList<Integer>();
      }
      this.metagameIdList.add(elem);
    }

    public List<Integer> getMetagameIdList() {
      return this.metagameIdList;
    }

    public getMetagame_args setMetagameIdList(List<Integer> metagameIdList) {
      this.metagameIdList = metagameIdList;
      return this;
    }

    public void unsetMetagameIdList() {
      this.metagameIdList = null;
    }

    /** Returns true if field metagameIdList is set (has been assigned a value) and false otherwise */
    public boolean isSetMetagameIdList() {
      return this.metagameIdList != null;
    }

    public void setMetagameIdListIsSet(boolean value) {
      if (!value) {
        this.metagameIdList = null;
      }
    }

    public void setFieldValue(_Fields field, Object value) {
      switch (field) {
      case METAGAME_ID_LIST:
        if (value == null) {
          unsetMetagameIdList();
        } else {
          setMetagameIdList((List<Integer>)value);
        }
        break;

      }
    }

    public Object getFieldValue(_Fields field) {
      switch (field) {
      case METAGAME_ID_LIST:
        return getMetagameIdList();

      }
      throw new IllegalStateException();
    }

    /** Returns true if field corresponding to fieldID is set (has been assigned a value) and false otherwise */
    public boolean isSet(_Fields field) {
      if (field == null) {
        throw new IllegalArgumentException();
      }

      switch (field) {
      case METAGAME_ID_LIST:
        return isSetMetagameIdList();
      }
      throw new IllegalStateException();
    }

    @Override
    public boolean equals(Object that) {
      if (that == null)
        return false;
      if (that instanceof getMetagame_args)
        return this.equals((getMetagame_args)that);
      return false;
    }

    public boolean equals(getMetagame_args that) {
      if (that == null)
        return false;

      boolean this_present_metagameIdList = true && this.isSetMetagameIdList();
      boolean that_present_metagameIdList = true && that.isSetMetagameIdList();
      if (this_present_metagameIdList || that_present_metagameIdList) {
        if (!(this_present_metagameIdList && that_present_metagameIdList))
          return false;
        if (!this.metagameIdList.equals(that.metagameIdList))
          return false;
      }

      return true;
    }

    @Override
    public int hashCode() {
      List<Object> list = new ArrayList<Object>();

      boolean present_metagameIdList = true && (isSetMetagameIdList());
      list.add(present_metagameIdList);
      if (present_metagameIdList)
        list.add(metagameIdList);

      return list.hashCode();
    }

    @Override
    public int compareTo(getMetagame_args other) {
      if (!getClass().equals(other.getClass())) {
        return getClass().getName().compareTo(other.getClass().getName());
      }

      int lastComparison = 0;

      lastComparison = Boolean.valueOf(isSetMetagameIdList()).compareTo(other.isSetMetagameIdList());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetMetagameIdList()) {
        lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.metagameIdList, other.metagameIdList);
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
      StringBuilder sb = new StringBuilder("getMetagame_args(");
      boolean first = true;

      sb.append("metagameIdList:");
      if (this.metagameIdList == null) {
        sb.append("null");
      } else {
        sb.append(this.metagameIdList);
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

    private static class getMetagame_argsStandardSchemeFactory implements SchemeFactory {
      public getMetagame_argsStandardScheme getScheme() {
        return new getMetagame_argsStandardScheme();
      }
    }

    private static class getMetagame_argsStandardScheme extends StandardScheme<getMetagame_args> {

      public void read(org.apache.thrift.protocol.TProtocol iprot, getMetagame_args struct) throws org.apache.thrift.TException {
        org.apache.thrift.protocol.TField schemeField;
        iprot.readStructBegin();
        while (true)
        {
          schemeField = iprot.readFieldBegin();
          if (schemeField.type == org.apache.thrift.protocol.TType.STOP) { 
            break;
          }
          switch (schemeField.id) {
            case 1: // METAGAME_ID_LIST
              if (schemeField.type == org.apache.thrift.protocol.TType.LIST) {
                {
                  org.apache.thrift.protocol.TList _list16 = iprot.readListBegin();
                  struct.metagameIdList = new ArrayList<Integer>(_list16.size);
                  int _elem17;
                  for (int _i18 = 0; _i18 < _list16.size; ++_i18)
                  {
                    _elem17 = iprot.readI32();
                    struct.metagameIdList.add(_elem17);
                  }
                  iprot.readListEnd();
                }
                struct.setMetagameIdListIsSet(true);
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

      public void write(org.apache.thrift.protocol.TProtocol oprot, getMetagame_args struct) throws org.apache.thrift.TException {
        struct.validate();

        oprot.writeStructBegin(STRUCT_DESC);
        if (struct.metagameIdList != null) {
          oprot.writeFieldBegin(METAGAME_ID_LIST_FIELD_DESC);
          {
            oprot.writeListBegin(new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.I32, struct.metagameIdList.size()));
            for (int _iter19 : struct.metagameIdList)
            {
              oprot.writeI32(_iter19);
            }
            oprot.writeListEnd();
          }
          oprot.writeFieldEnd();
        }
        oprot.writeFieldStop();
        oprot.writeStructEnd();
      }

    }

    private static class getMetagame_argsTupleSchemeFactory implements SchemeFactory {
      public getMetagame_argsTupleScheme getScheme() {
        return new getMetagame_argsTupleScheme();
      }
    }

    private static class getMetagame_argsTupleScheme extends TupleScheme<getMetagame_args> {

      @Override
      public void write(org.apache.thrift.protocol.TProtocol prot, getMetagame_args struct) throws org.apache.thrift.TException {
        TTupleProtocol oprot = (TTupleProtocol) prot;
        BitSet optionals = new BitSet();
        if (struct.isSetMetagameIdList()) {
          optionals.set(0);
        }
        oprot.writeBitSet(optionals, 1);
        if (struct.isSetMetagameIdList()) {
          {
            oprot.writeI32(struct.metagameIdList.size());
            for (int _iter20 : struct.metagameIdList)
            {
              oprot.writeI32(_iter20);
            }
          }
        }
      }

      @Override
      public void read(org.apache.thrift.protocol.TProtocol prot, getMetagame_args struct) throws org.apache.thrift.TException {
        TTupleProtocol iprot = (TTupleProtocol) prot;
        BitSet incoming = iprot.readBitSet(1);
        if (incoming.get(0)) {
          {
            org.apache.thrift.protocol.TList _list21 = new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.I32, iprot.readI32());
            struct.metagameIdList = new ArrayList<Integer>(_list21.size);
            int _elem22;
            for (int _i23 = 0; _i23 < _list21.size; ++_i23)
            {
              _elem22 = iprot.readI32();
              struct.metagameIdList.add(_elem22);
            }
          }
          struct.setMetagameIdListIsSet(true);
        }
      }
    }

  }

  public static class getMetagame_result implements org.apache.thrift.TBase<getMetagame_result, getMetagame_result._Fields>, java.io.Serializable, Cloneable, Comparable<getMetagame_result>   {
    private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("getMetagame_result");

    private static final org.apache.thrift.protocol.TField SUCCESS_FIELD_DESC = new org.apache.thrift.protocol.TField("success", org.apache.thrift.protocol.TType.LIST, (short)0);

    private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();
    static {
      schemes.put(StandardScheme.class, new getMetagame_resultStandardSchemeFactory());
      schemes.put(TupleScheme.class, new getMetagame_resultTupleSchemeFactory());
    }

    public List<MetagameTr> success; // required

    /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
    public enum _Fields implements org.apache.thrift.TFieldIdEnum {
      SUCCESS((short)0, "success");

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
          case 0: // SUCCESS
            return SUCCESS;
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
      tmpMap.put(_Fields.SUCCESS, new org.apache.thrift.meta_data.FieldMetaData("success", org.apache.thrift.TFieldRequirementType.DEFAULT, 
          new org.apache.thrift.meta_data.ListMetaData(org.apache.thrift.protocol.TType.LIST, 
              new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, MetagameTr.class))));
      metaDataMap = Collections.unmodifiableMap(tmpMap);
      org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(getMetagame_result.class, metaDataMap);
    }

    public getMetagame_result() {
    }

    public getMetagame_result(
      List<MetagameTr> success)
    {
      this();
      this.success = success;
    }

    /**
     * Performs a deep copy on <i>other</i>.
     */
    public getMetagame_result(getMetagame_result other) {
      if (other.isSetSuccess()) {
        List<MetagameTr> __this__success = new ArrayList<MetagameTr>(other.success.size());
        for (MetagameTr other_element : other.success) {
          __this__success.add(new MetagameTr(other_element));
        }
        this.success = __this__success;
      }
    }

    public getMetagame_result deepCopy() {
      return new getMetagame_result(this);
    }

    @Override
    public void clear() {
      this.success = null;
    }

    public int getSuccessSize() {
      return (this.success == null) ? 0 : this.success.size();
    }

    public java.util.Iterator<MetagameTr> getSuccessIterator() {
      return (this.success == null) ? null : this.success.iterator();
    }

    public void addToSuccess(MetagameTr elem) {
      if (this.success == null) {
        this.success = new ArrayList<MetagameTr>();
      }
      this.success.add(elem);
    }

    public List<MetagameTr> getSuccess() {
      return this.success;
    }

    public getMetagame_result setSuccess(List<MetagameTr> success) {
      this.success = success;
      return this;
    }

    public void unsetSuccess() {
      this.success = null;
    }

    /** Returns true if field success is set (has been assigned a value) and false otherwise */
    public boolean isSetSuccess() {
      return this.success != null;
    }

    public void setSuccessIsSet(boolean value) {
      if (!value) {
        this.success = null;
      }
    }

    public void setFieldValue(_Fields field, Object value) {
      switch (field) {
      case SUCCESS:
        if (value == null) {
          unsetSuccess();
        } else {
          setSuccess((List<MetagameTr>)value);
        }
        break;

      }
    }

    public Object getFieldValue(_Fields field) {
      switch (field) {
      case SUCCESS:
        return getSuccess();

      }
      throw new IllegalStateException();
    }

    /** Returns true if field corresponding to fieldID is set (has been assigned a value) and false otherwise */
    public boolean isSet(_Fields field) {
      if (field == null) {
        throw new IllegalArgumentException();
      }

      switch (field) {
      case SUCCESS:
        return isSetSuccess();
      }
      throw new IllegalStateException();
    }

    @Override
    public boolean equals(Object that) {
      if (that == null)
        return false;
      if (that instanceof getMetagame_result)
        return this.equals((getMetagame_result)that);
      return false;
    }

    public boolean equals(getMetagame_result that) {
      if (that == null)
        return false;

      boolean this_present_success = true && this.isSetSuccess();
      boolean that_present_success = true && that.isSetSuccess();
      if (this_present_success || that_present_success) {
        if (!(this_present_success && that_present_success))
          return false;
        if (!this.success.equals(that.success))
          return false;
      }

      return true;
    }

    @Override
    public int hashCode() {
      List<Object> list = new ArrayList<Object>();

      boolean present_success = true && (isSetSuccess());
      list.add(present_success);
      if (present_success)
        list.add(success);

      return list.hashCode();
    }

    @Override
    public int compareTo(getMetagame_result other) {
      if (!getClass().equals(other.getClass())) {
        return getClass().getName().compareTo(other.getClass().getName());
      }

      int lastComparison = 0;

      lastComparison = Boolean.valueOf(isSetSuccess()).compareTo(other.isSetSuccess());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetSuccess()) {
        lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.success, other.success);
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
      StringBuilder sb = new StringBuilder("getMetagame_result(");
      boolean first = true;

      sb.append("success:");
      if (this.success == null) {
        sb.append("null");
      } else {
        sb.append(this.success);
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

    private static class getMetagame_resultStandardSchemeFactory implements SchemeFactory {
      public getMetagame_resultStandardScheme getScheme() {
        return new getMetagame_resultStandardScheme();
      }
    }

    private static class getMetagame_resultStandardScheme extends StandardScheme<getMetagame_result> {

      public void read(org.apache.thrift.protocol.TProtocol iprot, getMetagame_result struct) throws org.apache.thrift.TException {
        org.apache.thrift.protocol.TField schemeField;
        iprot.readStructBegin();
        while (true)
        {
          schemeField = iprot.readFieldBegin();
          if (schemeField.type == org.apache.thrift.protocol.TType.STOP) { 
            break;
          }
          switch (schemeField.id) {
            case 0: // SUCCESS
              if (schemeField.type == org.apache.thrift.protocol.TType.LIST) {
                {
                  org.apache.thrift.protocol.TList _list24 = iprot.readListBegin();
                  struct.success = new ArrayList<MetagameTr>(_list24.size);
                  MetagameTr _elem25;
                  for (int _i26 = 0; _i26 < _list24.size; ++_i26)
                  {
                    _elem25 = new MetagameTr();
                    _elem25.read(iprot);
                    struct.success.add(_elem25);
                  }
                  iprot.readListEnd();
                }
                struct.setSuccessIsSet(true);
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

      public void write(org.apache.thrift.protocol.TProtocol oprot, getMetagame_result struct) throws org.apache.thrift.TException {
        struct.validate();

        oprot.writeStructBegin(STRUCT_DESC);
        if (struct.success != null) {
          oprot.writeFieldBegin(SUCCESS_FIELD_DESC);
          {
            oprot.writeListBegin(new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRUCT, struct.success.size()));
            for (MetagameTr _iter27 : struct.success)
            {
              _iter27.write(oprot);
            }
            oprot.writeListEnd();
          }
          oprot.writeFieldEnd();
        }
        oprot.writeFieldStop();
        oprot.writeStructEnd();
      }

    }

    private static class getMetagame_resultTupleSchemeFactory implements SchemeFactory {
      public getMetagame_resultTupleScheme getScheme() {
        return new getMetagame_resultTupleScheme();
      }
    }

    private static class getMetagame_resultTupleScheme extends TupleScheme<getMetagame_result> {

      @Override
      public void write(org.apache.thrift.protocol.TProtocol prot, getMetagame_result struct) throws org.apache.thrift.TException {
        TTupleProtocol oprot = (TTupleProtocol) prot;
        BitSet optionals = new BitSet();
        if (struct.isSetSuccess()) {
          optionals.set(0);
        }
        oprot.writeBitSet(optionals, 1);
        if (struct.isSetSuccess()) {
          {
            oprot.writeI32(struct.success.size());
            for (MetagameTr _iter28 : struct.success)
            {
              _iter28.write(oprot);
            }
          }
        }
      }

      @Override
      public void read(org.apache.thrift.protocol.TProtocol prot, getMetagame_result struct) throws org.apache.thrift.TException {
        TTupleProtocol iprot = (TTupleProtocol) prot;
        BitSet incoming = iprot.readBitSet(1);
        if (incoming.get(0)) {
          {
            org.apache.thrift.protocol.TList _list29 = new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRUCT, iprot.readI32());
            struct.success = new ArrayList<MetagameTr>(_list29.size);
            MetagameTr _elem30;
            for (int _i31 = 0; _i31 < _list29.size; ++_i31)
            {
              _elem30 = new MetagameTr();
              _elem30.read(iprot);
              struct.success.add(_elem30);
            }
          }
          struct.setSuccessIsSet(true);
        }
      }
    }

  }

}