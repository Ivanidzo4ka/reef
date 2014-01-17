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
package com.microsoft.wake.remote.transport.exception;

/**
 * Transport runtime exception 
 */
public class TransportRuntimeException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  /**
   * Constructs a new runtime transport exception with the specified detail message and cause
   * @param s the detailed message
   * @param e the cause
   */
  public TransportRuntimeException(String s, Throwable e) {
    super(s, e);
  }

  /**
   * Constructs a new runtime transport exception with the specified detail message
   * @param s the detailed message
   */
  public TransportRuntimeException(String s) {
    super(s);
  }

  /**
   * Constructs a new runtime transport exception with the specified cause
   * @param e the cause
   */
  public TransportRuntimeException(Throwable e) {
    super(e);
  }
}