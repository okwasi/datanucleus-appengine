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
package com.google.appengine.datanucleus;

import junit.framework.TestSuite;

import com.google.appengine.datanucleus.jdo.DatastoreJDOPersistenceManagerFactoryTest;
import com.google.appengine.datanucleus.jpa.DatastoreEntityManagerFactoryTest;
import com.google.appengine.datanucleus.jpa.DatastoreJPACallbackHandlerTest;
import com.google.appengine.datanucleus.query.JDOQLCursorTest;
import com.google.appengine.datanucleus.query.JDOQLDeleteTest;
import com.google.appengine.datanucleus.query.JDOQLQueryOwnedJoinTest;
import com.google.appengine.datanucleus.query.JDOQLQueryTest;
import com.google.appengine.datanucleus.query.JDOQLQueryUnownedJoinTest;
import com.google.appengine.datanucleus.query.JPQLCursorTest;
import com.google.appengine.datanucleus.query.JPQLDeleteTest;
import com.google.appengine.datanucleus.query.JPQLQueryOwnedJoinTest;
import com.google.appengine.datanucleus.query.JPQLQueryTest;
import com.google.appengine.datanucleus.query.JPQLQueryUnownedJoinTest;
import com.google.appengine.datanucleus.query.JoinHelperTest;
import com.google.appengine.datanucleus.query.LazyResultTest;
import com.google.appengine.datanucleus.query.SlicingIterableTest;

/**
 * All tests for the app engine datanucleus plugin.
 * This will be difficult to keep in sync but we'll do our best.
 *
 * @author Max Ross <maxr@google.com>
 */
public class AllTests {
  public static TestSuite suite() {
    TestSuite suite = new TestSuite();
    suite.addTestSuite(DatastoreFieldManagerTest.class);
    suite.addTestSuite(DatastoreJDOPersistenceManagerFactoryTest.class);
    suite.addTestSuite(DatastoreEntityManagerFactoryTest.class);
    suite.addTestSuite(JDOQLQueryTest.class);
    suite.addTestSuite(JPQLQueryTest.class);
    suite.addTestSuite(LazyResultTest.class);
    suite.addTestSuite(SerializationTest.class);
    suite.addTestSuite(SerializationManagerTest.class);
    suite.addTestSuite(JDOFetchTest.class);
    suite.addTestSuite(JDOInsertionTest.class);
    suite.addTestSuite(JDOUpdateTest.class);
    suite.addTestSuite(JDODeleteTest.class);
    suite.addTestSuite(JDOTransactionTest.class);
    suite.addTestSuite(JPAFetchTest.class);
    suite.addTestSuite(JPAInsertionTest.class);
    suite.addTestSuite(JPAUpdateTest.class);
    suite.addTestSuite(JPADeleteTest.class);
    suite.addTestSuite(JPATransactionTest.class);
    suite.addTestSuite(JDOAncestorTest.class);
    suite.addTestSuite(JPAAncestorTest.class);
    suite.addTestSuite(JDOTableAndColumnTest.class);
    suite.addTestSuite(JPATableAndColumnTest.class);
    suite.addTestSuite(JPAOneToOneTest.class);
    suite.addTestSuite(JDOOneToOneTest.class);
    suite.addTestSuite(JPAImplicitEntityGroupTest.class);
    suite.addTestSuite(JDOImplicitEntityGroupTest.class);
    suite.addTestSuite(JPAOneToManyListTest.class);
    suite.addTestSuite(JPAOneToManySetTest.class);
    suite.addTestSuite(JDOOneToManyListTest.class);
    suite.addTestSuite(JDOOneToManySetTest.class);
    // TODO(maxr) Reenable once we reenable support for arrays.
//    suite.addTestSuite(JDOOneToManyArrayTest.class);
    suite.addTestSuite(JDODataSourceConfigTest.class);
    suite.addTestSuite(JPADataSourceConfigTest.class);
    suite.addTestSuite(JDOEnumTest.class);
    suite.addTestSuite(JPAEnumTest.class);
    suite.addTestSuite(JDONullValueTest.class);
    suite.addTestSuite(JPANullValueTest.class);
    suite.addTestSuite(TypeConversionUtilsTest.class);
    suite.addTestSuite(JDOMakeTransientTest.class);
    suite.addTestSuite(JDOPrimaryKeyTest.class);
    suite.addTestSuite(JPAPrimaryKeyTest.class);
    suite.addTestSuite(JDOMetaDataValidatorTest.class);
    suite.addTestSuite(JPAMetaDataValidatorTest.class);
    suite.addTestSuite(MetaDataValidatorTest.class);
    suite.addTestSuite(EntityUtilsTest.class);
    suite.addTestSuite(JDOBytesTest.class);
    suite.addTestSuite(JPABytesTest.class);
    suite.addTestSuite(JDOConcurrentModificationTest.class);
    suite.addTestSuite(JPAConcurrentModificationTest.class);
    suite.addTestSuite(JDOEmbeddedTest.class);
    suite.addTestSuite(JPAEmbeddedTest.class);
    suite.addTestSuite(JDOFetchGroupTest.class);
    suite.addTestSuite(JDOAttachDetachTest.class);
    suite.addTestSuite(JPAAttachDetachTest.class);
    suite.addTestSuite(JDOSubclassTest.class);
    suite.addTestSuite(JPASubclassTest.class);
    suite.addTestSuite(JDOAbstractBaseClassTest.class);
    suite.addTestSuite(JPAAbstractBaseClassTest.class);
    suite.addTestSuite(JDOBidirectionalOneToOneSubclassTest.class);
    suite.addTestSuite(JPABidirectionalOneToOneSubclassTest.class);
    suite.addTestSuite(JDOBidirectionalOneToManySubclassTest.class);
    suite.addTestSuite(JPABidirectionalOneToManySubclassTest.class);
    suite.addTestSuite(JDOUnidirectionalOneToOneSubclassTest.class);
    suite.addTestSuite(JPAUnidirectionalOneToOneSubclassTest.class);
    suite.addTestSuite(JDOUnidirectionalOneToManySubclassTest.class);
    suite.addTestSuite(JPAUnidirectionalOneToManySubclassTest.class);
    suite.addTestSuite(JPALobTest.class);
    suite.addTestSuite(JDOUnindexedPropertiesTest.class);
    suite.addTestSuite(JPAUnindexedPropertiesTest.class);
    suite.addTestSuite(DatastoreJPACallbackHandlerTest.class);
    suite.addTestSuite(JDOBatchInsertTest.class);
    suite.addTestSuite(JDOBatchDeleteTest.class);
    suite.addTestSuite(BatchManagerTest.class);
    suite.addTestSuite(DatastoreManagerTest.class);
    suite.addTestSuite(JPQLDeleteTest.class);
    suite.addTestSuite(JDOQLDeleteTest.class);
    suite.addTestSuite(JPASequenceTest.class);
    suite.addTestSuite(JDOSequenceTest.class);
    suite.addTestSuite(JoinHelperTest.class);
    suite.addTestSuite(SlicingIterableTest.class);
    suite.addTestSuite(JDOQLQueryUnownedJoinTest.class);
    suite.addTestSuite(JDOQLQueryOwnedJoinTest.class);
    suite.addTestSuite(JPQLQueryUnownedJoinTest.class);
    suite.addTestSuite(JPQLQueryOwnedJoinTest.class);
    suite.addTestSuite(JDOQLCursorTest.class);
    suite.addTestSuite(JPQLCursorTest.class);
    suite.addTestSuite(JDODatastoreBridgeTest.class);
    suite.addTestSuite(JPADatastoreBridgeTest.class);
    suite.addTestSuite(JPANonWritableFieldsTest.class);
    suite.addTestSuite(JDOStorageVersionTest.class);
    suite.addTestSuite(JPAStorageVersionTest.class);
    suite.addTestSuite(JDODatastoreServiceConfigTest.class);
    suite.addTestSuite(JPADatastoreServiceConfigTest.class);
    suite.addTestSuite(JPAVersionTest.class);
    suite.addTestSuite(JDOSuperclassTableInheritanceTest.class);
    suite.addTestSuite(JPASingleTableInheritanceTest.class);
    suite.addTestSuite(JDOOneToManyPolymorphicListTest.class);
    suite.addTestSuite(JDOOneToManyPolymorphicSetTest.class);
    suite.addTestSuite(JPAOneToManyPolymorphicListTest.class);
    suite.addTestSuite(JPAOneToManyPolymorphicSetTest.class);
    
    return suite;
  }
}