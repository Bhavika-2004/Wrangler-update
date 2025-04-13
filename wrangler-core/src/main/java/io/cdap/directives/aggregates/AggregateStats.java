/*
 * Copyright © 2025 Cask Data, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */

 package io.cdap.directives.aggregates;

 import io.cdap.cdap.api.annotation.Description;
 import io.cdap.cdap.api.annotation.Name;
 import io.cdap.cdap.api.annotation.Plugin;
 import io.cdap.wrangler.api.Arguments;
 import io.cdap.wrangler.api.Directive;
 import io.cdap.wrangler.api.DirectiveExecutionException;
 import io.cdap.wrangler.api.DirectiveParseException;
 import io.cdap.wrangler.api.ExecutorContext;
 import io.cdap.wrangler.api.Row;
import io.cdap.wrangler.api.TransientVariableScope;
import io.cdap.wrangler.api.parser.ColumnName;
 import io.cdap.wrangler.api.parser.TokenType;
 import io.cdap.wrangler.api.parser.UsageDefinition;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Aggregates statistics (total, average) for byte sizes and time durations.
  */
 @Plugin(type = Directive.TYPE)
 @Name("aggregate-stats")
 @Description("Aggregates statistics (total, average) for byte sizes and time durations.")
 public class AggregateStats implements Directive {
 
   private String sizeColumn;
   private String timeColumn;
   private String targetSizeColumn;
   private String targetTimeColumn;
   private String aggregationType;
   private String outputSizeUnit;
   private String outputTimeUnit;
 
   private double totalBytes;
   private double totalMillis;
   private int rowCount;
 
   @Override
   public UsageDefinition define() {
     UsageDefinition.Builder builder = UsageDefinition.builder("aggregate-stats");
     builder.define("size-column", TokenType.COLUMN_NAME);
     builder.define("time-column", TokenType.COLUMN_NAME);
     builder.define("target-size-column", TokenType.COLUMN_NAME);
     builder.define("target-time-column", TokenType.COLUMN_NAME);
     builder.define("aggregation", TokenType.IDENTIFIER, false);
     builder.define("size-unit", TokenType.IDENTIFIER, false);
     builder.define("time-unit", TokenType.IDENTIFIER, false);
     return builder.build();
   }
 
   @Override
   public void initialize(Arguments arguments) throws DirectiveParseException {
     sizeColumn = ((ColumnName) arguments.value("size-column")).value();
     timeColumn = ((ColumnName) arguments.value("time-column")).value();
     targetSizeColumn = ((ColumnName) arguments.value("target-size-column")).value();
     targetTimeColumn = ((ColumnName) arguments.value("target-time-column")).value();
 
     if (arguments.contains("aggregation")) {
       aggregationType = arguments.value("aggregation").value().toString().toLowerCase();
     }
     if (arguments.contains("size-unit")) {
       outputSizeUnit = arguments.value("size-unit").value().toString().toUpperCase();
     }
     if (arguments.contains("time-unit")) {
       outputTimeUnit = arguments.value("time-unit").value().toString().toLowerCase();
     }
   }
 
   @Override
   public List<Row> execute(List<Row> rows, ExecutorContext context) throws DirectiveExecutionException {
     // 👇 Only run once using context flag
     if (context != null && context.getTransientStore().get("aggregate-stats-executed") != null) {
      return new ArrayList<>();
    }
    if (context != null) {
      context.getTransientStore().set(TransientVariableScope.GLOBAL, "aggregate-stats-executed", true);
    }
    
    
 
     totalBytes = 0;
     totalMillis = 0;
     rowCount = 0;
 
     for (Row row : rows) {
       Object sizeValue = row.getValue(sizeColumn);
       Object timeValue = row.getValue(timeColumn);
 
       if (sizeValue != null && timeValue != null) {
         try {
           totalBytes += parseByteSize(sizeValue.toString());
           totalMillis += parseTimeDuration(timeValue.toString());
           rowCount++;
         } catch (Exception ignored) {
           // skip malformed values
         }
       }
     }
 
     double sizeResult = (aggregationType != null && aggregationType.equalsIgnoreCase("avg"))
                           ? totalBytes / rowCount : totalBytes;
     double timeResult = (aggregationType != null && aggregationType.equalsIgnoreCase("avg"))
                           ? totalMillis / rowCount : totalMillis;
 
     sizeResult = convertBytes(sizeResult, outputSizeUnit);
     timeResult = convertTime(timeResult, outputTimeUnit);
 
     Row result = new Row(targetSizeColumn, sizeResult);
     result.add(targetTimeColumn, timeResult);
 
     List<Row> output = new ArrayList<>();
     output.add(result);
     return output;
   }
 
   private double parseByteSize(String value) {
     value = value.trim().toUpperCase();
     if (value.endsWith("PB")) {
       return Double.parseDouble(value.replace("PB", "").trim()) * Math.pow(1024, 5);
     } else if (value.endsWith("TB")) {
       return Double.parseDouble(value.replace("TB", "").trim()) * Math.pow(1024, 4);
     } else if (value.endsWith("GB")) {
       return Double.parseDouble(value.replace("GB", "").trim()) * Math.pow(1024, 3);
     } else if (value.endsWith("MB")) {
       return Double.parseDouble(value.replace("MB", "").trim()) * Math.pow(1024, 2);
     } else if (value.endsWith("KB")) {
       return Double.parseDouble(value.replace("KB", "").trim()) * 1024;
     } else if (value.endsWith("B")) {
       return Double.parseDouble(value.replace("B", "").trim());
     }
     return Double.parseDouble(value);
   }
 
   private double parseTimeDuration(String value) {
     value = value.trim().toLowerCase();
     if (value.endsWith("ms")) {
       return Double.parseDouble(value.replace("ms", ""));
     } else if (value.endsWith("sec")) {
       return Double.parseDouble(value.replace("sec", "")) * 1000;
     } else if (value.endsWith("s")) {
       return Double.parseDouble(value.replace("s", "")) * 1000;
     } else if (value.endsWith("min")) {
       return Double.parseDouble(value.replace("min", "")) * 60 * 1000;
     } else if (value.endsWith("hour")) {
       return Double.parseDouble(value.replace("hour", "")) * 60 * 60 * 1000;
     } else if (value.endsWith("day")) {
       return Double.parseDouble(value.replace("day", "")) * 24 * 60 * 60 * 1000;
     }
     return Double.parseDouble(value);
   }
 
   private double convertBytes(double bytes, String unit) {
     if (unit == null) {
       return bytes;
     }
     switch (unit) {
       case "GB":
         return bytes / Math.pow(1024, 3);
       case "MB":
         return bytes / Math.pow(1024, 2);
       case "KB":
         return bytes / 1024;
       default:
         return bytes;
     }
   }
 
   private double convertTime(double millis, String unit) {
     if (unit == null) {
       return millis;
     }
     switch (unit) {
       case "sec":
         return millis / 1000;
       case "min":
         return millis / (60 * 1000);
       case "hour":
         return millis / (60 * 60 * 1000);
       case "day":
         return millis / (24 * 60 * 60 * 1000);
       default:
         return millis;
     }
   }
 
   @Override
   public void destroy() {
     // No-op
   }
 }
 