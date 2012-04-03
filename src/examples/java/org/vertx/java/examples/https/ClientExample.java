/*
 * Copyright 2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.vertx.java.examples.https;

import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpClient;
import org.vertx.java.core.http.HttpClientResponse;
import org.vertx.java.deploy.Verticle;

public class ClientExample extends Verticle {

  public void start() {
    vertx.createHttpClient().setSSL(true).setTrustAll(true).setPort(4443).setHost("localhost").getNow("/", new Handler<HttpClientResponse>() {
      public void handle(HttpClientResponse response) {
        response.dataHandler(new Handler<Buffer>() {
          public void handle(Buffer data) {
            System.out.println(data);
          }
        });
      }
    });
  }
}
