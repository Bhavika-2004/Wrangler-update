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

 import io.cdap.wrangler.TestingRig;
 import io.cdap.wrangler.api.Row;
 import org.junit.Assert;
 import org.junit.Test;
 
 import java.util.Arrays;
 import java.util.List;
 
 public class AggregateStatsTest {
 
  @Test
  public void testAggregationWorksCorrectly() throws Exception {
    String[] recipe = {
      "aggregate-stats :size :duration :total_size :total_time total MB sec"
    };
  
    // Preprocess input to ensure numerical values
    List<Row> input = Arrays.asList(
      new Row("size", 10.0).add("duration", 2.0),
      new Row("size", 0.5).add("duration", 0.5)
    );
  
    List<Row> results = TestingRig.execute(recipe, input);
  
    // Debugging: Print results
    System.out.println("Results: " + results);
  
    Assert.assertEquals(1, results.size());
  
    Row result = results.get(0);
  
    // Debugging: Print available fields
    System.out.println("Available fields: " + result.getFields());
  
    double totalSize = (double) result.getValue("total_size");
    double totalTime = (double) result.getValue("total_time");
  
    Assert.assertEquals(10.5, totalSize, 0.01);
    Assert.assertEquals(2.5, totalTime, 0.01);
  }
}
