/*
 * Copyright © 2017-2019 Cask Data, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

 package io.cdap.wrangler.api.parser;

 import com.google.gson.JsonElement;
 import com.google.gson.JsonPrimitive;
 
 /**
  * A token implementation for handling time values. It parses string representations
  * of time durations (e.g., 10ms, 5sec, 2min) into numeric millisecond values.
  */
 public class TimeDuration implements Token {
   private final String rawValue;
   private final long milliseconds;
 
   /**
    * Constructs a Time token with the given string value.
    *
    * @param value The string representation of the time duration.
    */
   public TimeDuration(String value) {
     this.rawValue = value;
     this.milliseconds = parseTimeDuration(value);
   }
 
   private long parseTimeDuration(String value) {
     value = value.trim().toLowerCase();
     double number;
     if (value.endsWith("ms")) {
       number = Double.parseDouble(value.replace("ms", ""));
       return (long) number;
     } else if (value.endsWith("sec")) {
       number = Double.parseDouble(value.replace("sec", ""));
       return (long) (number * 1000);
     } else if (value.endsWith("s")) {
       number = Double.parseDouble(value.replace("s", ""));
       return (long) (number * 1000);
     } else if (value.endsWith("min")) {
       number = Double.parseDouble(value.replace("min", ""));
       return (long) (number * 60 * 1000);
     } else if (value.endsWith("hour")) {
       number = Double.parseDouble(value.replace("hour", ""));
       return (long) (number * 60 * 60 * 1000);
     } else if (value.endsWith("day")) {
       number = Double.parseDouble(value.replace("day", ""));
       return (long) (number * 24 * 60 * 60 * 1000);
     } else {
       return Long.parseLong(value); // assume raw milliseconds
     }
   }
 
   /**
    * Returns the parsed time duration in milliseconds.
    *
    * @return The time duration in milliseconds.
    */
   public long getMilliseconds() {
     return milliseconds;
   }
 
   @Override
   public Object value() {
     return rawValue;
   }
 
   @Override
   public TokenType type() {
     return TokenType.TIME_DURATION;
   }
 
   @Override
   public JsonElement toJson() {
     return new JsonPrimitive(milliseconds);
   }
 
   @Override
   public String toString() {
     return milliseconds + " ms";
   }
 }
