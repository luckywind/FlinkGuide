/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package spendreport;

import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.table.api.Tumble;
import org.apache.flink.table.api.java.BatchTableEnvironment;
import org.apache.flink.walkthrough.common.table.SpendReportTableSink;
import org.apache.flink.walkthrough.common.table.BoundedTransactionTableSource;
import org.apache.flink.walkthrough.common.table.TruncateDateToHour;

/**
 * Skeleton code for the table walkthrough
 */
public class SpendReport {
	public static void main(String[] args) throws Exception {
		ExecutionEnvironment env   = ExecutionEnvironment.getExecutionEnvironment();
		BatchTableEnvironment tEnv = BatchTableEnvironment.create(env);

		tEnv.registerTableSource("transactions", new BoundedTransactionTableSource());
		tEnv.registerTableSink("spend_report", new SpendReportTableSink());
		tEnv.registerFunction("truncateDateToHour", new TruncateDateToHour());//注册UDF

//		tEnv
//			.scan("transactions")
//			.insertInto("spend_report");

/*		tEnv
				.scan("transactions")
				.select("accountId, timestamp.truncateDateToHour as timestamp, amount")
				.groupBy("accountId, timestamp")
				.select("accountId, timestamp, amount.sum as total")
				.insertInto("spend_report");*/


		tEnv
				.scan("transactions")
				.window(Tumble.over("1.hour").on("timestamp").as("w"))
				.groupBy("accountId, w")
				.select("accountId, w.start as timestamp, amount.sum")
				.insertInto("spend_report");
		env.execute("Spend Report");
	}
}
