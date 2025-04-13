/*
 * Copyright © 2017-2019 Cask Data, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */

 package io.cdap.wrangler.api.parser;

 import com.google.gson.JsonElement;
 import com.google.gson.JsonPrimitive;
 
 /**
  * A token implementation that parses string representations of byte sizes
  * (e.g., 1KB, 5MB, 10GB) into numeric byte values.
  */
 public class ByteSize implements Token {
   private final String rawValue;
   private final long bytes;
 
   /**
    * Constructs a SizeofByte token with the given string value.
    * @param value The string representation of the byte size.
    */
   public ByteSize(String value) {
     this.rawValue = value;
     this.bytes = parseByteSize(value);
   }
 
   private long parseByteSize(String value) {
     value = value.trim().toUpperCase();
     double number;
 
     if (value.endsWith("PB")) {
       number = Double.parseDouble(value.substring(0, value.length() - 2));
       return (long) (number * Math.pow(1024, 5));
     } else if (value.endsWith("TB")) {
       number = Double.parseDouble(value.substring(0, value.length() - 2));
       return (long) (number * Math.pow(1024, 4));
     } else if (value.endsWith("GB")) {
       number = Double.parseDouble(value.substring(0, value.length() - 2));
       return (long) (number * Math.pow(1024, 3));
     } else if (value.endsWith("MB")) {
       number = Double.parseDouble(value.substring(0, value.length() - 2));
       return (long) (number * Math.pow(1024, 2));
     } else if (value.endsWith("KB")) {
       number = Double.parseDouble(value.substring(0, value.length() - 2));
       return (long) (number * 1024);
     } else if (value.endsWith("B")) {
       number = Double.parseDouble(value.substring(0, value.length() - 1));
       return (long) number;
     } else {
       return Long.parseLong(value);
     }
   }
 
   /**
    * Returns the parsed size in bytes.
    */
   public long getBytes() {
     return bytes;
   }
 
   @Override
   public Object value() {
     return rawValue;
   }
 
   @Override
   public TokenType type() {
     return TokenType.BYTE_SIZE;
   }
 
   @Override
   public JsonElement toJson() {
     return new JsonPrimitive(bytes);
   }
 
   @Override
   public String toString() {
     return bytes + " bytes";
   }
 }
