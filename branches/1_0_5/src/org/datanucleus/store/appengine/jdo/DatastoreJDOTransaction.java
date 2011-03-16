/*
 * Copyright (C) 2010 Google Inc
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
package org.datanucleus.store.appengine.jdo;

import com.google.appengine.api.datastore.Transaction;

import org.datanucleus.jdo.JDOTransaction;
import org.datanucleus.store.appengine.DatastoreServiceFactoryInternal;
import org.datanucleus.util.NucleusLogger;

import javax.jdo.PersistenceManager;

/**
 * Datastore-specific extension that aggressively starts transactions.
 * DataNucleus delays the start of the transaction until the first
 * read or write, but since task queue tasks auto-enlist in the current
 * transaction when a current transaction exists, it's important that
 * a datastore transaction actually be active once the user has called
 * pm.currentTransaction().begin().
 *
 * @author Max Ross <max.ross@gmail.com>
 */
class DatastoreJDOTransaction extends JDOTransaction {

  public DatastoreJDOTransaction(PersistenceManager pm, org.datanucleus.Transaction tx) {
    super(pm, tx);
  }

  @Override
  public void begin() {
    super.begin();
    Transaction txn = DatastoreServiceFactoryInternal.getDatastoreService().beginTransaction();
    NucleusLogger.DATASTORE.debug("Started new datastore transaction: " + txn.getId());
  }

  @Override
  public void commit() {
    super.commit();
    Transaction txn = DatastoreServiceFactoryInternal.getDatastoreService().getCurrentTransaction(null);
    if (txn == null) {
      // this is ok, it means the txn was committed via the connection
    } else {
      // ordinarily the txn gets committed in DatastoreXAResource.commit(), but
      // if the begin/commit block doesn't perform any reads or writes then
      // DatastoreXAResource.commit() won't be called.  In order to avoid
      // leaving transactions open we do the commit here.
      txn.commit();
    }
  }

  @Override
  public void rollback() {
    super.rollback();
    Transaction txn = DatastoreServiceFactoryInternal.getDatastoreService().getCurrentTransaction(null);
    if (txn == null) {
      // this is ok, it means the txn was rolled back via the connection
    } else {
      // ordinarily the txn gets aborted in DatastoreXAResource.commit(), but
      // if the begin/abort block doesn't perform any reads or writes then
      // DatastoreXAResource.rollback() won't be called.  In order to avoid
      // leaving transactions open we do the rollback here.
      txn.rollback();
    }
  }
}