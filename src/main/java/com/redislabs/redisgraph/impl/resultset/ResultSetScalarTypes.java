package com.redislabs.redisgraph.impl.resultset;

import redis.clients.jedis.exceptions.JedisDataException;

enum ResultSetScalarTypes {
    VALUE_UNKNOWN,
    VALUE_NULL,
    VALUE_STRING,
    VALUE_INTEGER,  // 64 bit long.
    VALUE_BOOLEAN,
    VALUE_DOUBLE,
    VALUE_ARRAY,
    VALUE_EDGE,
    VALUE_NODE,
    VALUE_PATH,
    VALUE_MAP,
    VALUE_POINT;

    private static final ResultSetScalarTypes[] values = values();

    public static ResultSetScalarTypes getValue(int index) {
      try {
        return values[index];
      } catch(IndexOutOfBoundsException e) {
        throw new JedisDataException("Unrecognized response type");
      }
    }

}
