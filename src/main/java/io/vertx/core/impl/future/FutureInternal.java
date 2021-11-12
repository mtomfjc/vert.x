/*
 * Copyright (c) 2011-2020 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */
package io.vertx.core.impl.future;

import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.impl.ContextInternal;
import io.vertx.core.impl.WorkerContext;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Expose some of the future internal stuff.
 *
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public interface FutureInternal<T> extends Future<T> {

  /**
   * @return the context associated with this promise or {@code null} when there is none
   */
  ContextInternal context();

  /**
   * Add a listener to the future result.
   *
   * @param listener the listener
   */
  void addListener(Listener<T> listener);

  default T await() throws ExecutionException, InterruptedException {
    Context ctx = Vertx.currentContext();
    if (ctx == null || ctx.isEventLoopContext()) {
      throw new IllegalStateException();
    }
    // Worker style
    ReentrantLock lock = new ReentrantLock();
    Condition cond = lock.newCondition();
    lock.lock();
    try {
      addListener(new Listener<T>() {
        @Override
        public void emitSuccess(ContextInternal context, T value) {
          lock.lock();
          try {
            cond.signal();
          } finally {
            lock.unlock();
          }
        }
        @Override
        public void emitFailure(ContextInternal context, Throwable failure) {
          lock.lock();
          try {
            cond.signal();
          } finally {
            lock.unlock();
          }
        }
      });
      cond.await();
      if (succeeded()) {
        return result();
      } else {
        throw new ExecutionException(cause());
      }
    } finally {
      lock.unlock();
    }
  }
}
