/**********************************************************************
 Copyright (c) 2009 Google Inc.

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 **********************************************************************/
package org.datanucleus.store.appengine.query;

import com.google.appengine.api.datastore.Entity;

import org.datanucleus.store.appengine.Utils.Supplier;

import java.util.Iterator;

/**
 * {@link Iterator} implementation that catches runtime exceptions thrown by
 * the datastore api and translates them to the appropriate JDO or JPA
 * exception.  DataNucleus is supposed to do this for us, but they miss runtime
 * exceptions that are thrown while iterating.
 *
 * @author Max Ross <maxr@google.com>
 */
class RuntimeExceptionWrappingIterator implements Iterator<Entity> {

  final Iterator<Entity> inner;
  final Supplier<Boolean> hasNextSupplier;
  final Supplier<Entity> nextSupplier;
  final Supplier<Void> removeSupplier;

  RuntimeExceptionWrappingIterator(final Iterator<Entity> inner,
                                   boolean isJPA, final RuntimeExceptionObserver exceptionObserver) {
    if (inner == null) {
      throw new NullPointerException("inner cannot be null");
    }
    this.inner = inner;
    Supplier<Boolean> datastoreHasNextSupplier = QueryExceptionWrappers.datastoreToDataNucleus(
        new Supplier<Boolean>() {
          public Boolean get() {
            boolean success = false;
            try {
              boolean result = inner.hasNext();
              success = true;
              return result;
            } finally {
              if (!success) {
                exceptionObserver.onException();
              }
            }
          }
        });

    Supplier<Entity> datastoreNextSupplier = QueryExceptionWrappers.datastoreToDataNucleus(
        new Supplier<Entity>() {
          public Entity get() {
            boolean success = false;
            try {
              Entity result = inner.next();
              success = true;
              return result;
            } finally {
              if (!success) {
                exceptionObserver.onException();
              }
            }
          }
        });

    Supplier<Void> datastoreRemoveSupplier = QueryExceptionWrappers.datastoreToDataNucleus(
          new Supplier<Void>() {
            public Void get() {
              boolean success = false;
              try {
                inner.remove();
                success = true;
                return null;
              } finally {
                if (!success) {
                  exceptionObserver.onException();
                }
              }
            }
          });

    if (isJPA) {
      hasNextSupplier = QueryExceptionWrappers.dataNucleusToJPA(datastoreHasNextSupplier);
      nextSupplier = QueryExceptionWrappers.dataNucleusToJPA(datastoreNextSupplier);
      removeSupplier = QueryExceptionWrappers.dataNucleusToJPA(datastoreRemoveSupplier);
    } else {
      hasNextSupplier = QueryExceptionWrappers.dataNucleusToJDO(datastoreHasNextSupplier);
      nextSupplier = QueryExceptionWrappers.dataNucleusToJDO(datastoreNextSupplier);
      removeSupplier = QueryExceptionWrappers.dataNucleusToJDO(datastoreRemoveSupplier);
    }
  }

  public boolean hasNext() {
    return hasNextSupplier.get();
  }

  public Entity next() {
    return nextSupplier.get();
  }

  public void remove() {
    removeSupplier.get();
  }
}