/**
 * Copyright (C) 2012 Microsoft Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.microsoft.wake.impl;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;

import com.microsoft.tang.annotations.Parameter;
import com.microsoft.wake.AbstractEStage;
import com.microsoft.wake.EventHandler;
import com.microsoft.wake.StageConfiguration.Capacity;
import com.microsoft.wake.StageConfiguration.StageHandler;
import com.microsoft.wake.StageConfiguration.StageName;

/**
 * Single thread stage that runs the event handler
 * 
 * @param <T> type
 */
public final class SingleThreadStage<T> extends AbstractEStage<T> {
  private static final Logger LOG = Logger.getLogger(SingleThreadStage.class.getName());
  
  private final BlockingQueue<T> queue;
  private final Thread thread;
  private final AtomicBoolean interrupted;
  
  /**
   * Constructs a single thread stage
   * 
   * @param handler the event handler to execute
   * @param capacity the queue capacity
   */
  @Inject
  public SingleThreadStage(final @Parameter(StageHandler.class) EventHandler<T> handler, 
      final @Parameter(Capacity.class) int capacity) {
    this(handler.getClass().getName(), handler, capacity);
  }

  /**
   * Constructs a single thread stage
   * 
   * @param name the stage name
   * @param handler the event handler to execute
   * @param capacity the queue capacity
   */
  @Inject
  public SingleThreadStage(final @Parameter(StageName.class) String name, 
      final @Parameter(StageHandler.class) EventHandler<T> handler, 
      final @Parameter(Capacity.class) int capacity) {
    super(name);
    queue = new ArrayBlockingQueue<T> (capacity);
    interrupted = new AtomicBoolean(false);
    thread = new Thread(new Producer<T>(name, queue, handler, interrupted));
    thread.setName("SingleThreadStage<" + name + ">");
    thread.start();
    StageManager.instance().register(this);
  }

  /**
   * Puts the value to the queue, which will be processed by the handler later
   * if the queue is full, IllegalStateException is thrown
   * 
   * @param value the value
   * @throws IllegalStateException
   */
  @Override
  public void onNext(T value) {
    beforeOnNext();
    queue.add(value);
  }

  /**
   * Closes the stage
   * 
   * @throws Exception
   */
  @Override
  public void close() throws Exception {
    if (closed.compareAndSet(false, true)) {
      interrupted.set(true);
      thread.interrupt();
    }
  }

  
  /**
   * Takes events from the queue and provides them to the handler
   */
  private class Producer<U> implements Runnable {

    private final String name;
    private final BlockingQueue<U> queue;
    private final EventHandler<U> handler;
    private final AtomicBoolean interrupted;

    Producer(String name, BlockingQueue<U> queue, EventHandler<U> handler, AtomicBoolean interrupted) {
      this.name = name;
      this.queue = queue;
      this.handler = handler;
      this.interrupted = interrupted;
    }

    @Override
    public void run() {
      while(true) {
        try {
          U value = queue.take();
          handler.onNext(value);
          SingleThreadStage.this.afterOnNext();
        } catch (InterruptedException e) {
          if (interrupted.get()) {
            LOG.log(Level.FINEST, name + " Closing Producer due to interruption");
            break;
          }
        } catch (Throwable t) {
          LOG.log(Level.SEVERE, name + " Exception from event handler", t);
          throw t;
        }
      }
    }
  }
  
}

