// Copyright 2008 Google Inc. All Rights Reserved.
package org.datanucleus.store.appengine;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import static com.google.appengine.api.datastore.FetchOptions.Builder.withLimit;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;

import org.datanucleus.ClassLoaderResolver;
import org.datanucleus.ObjectManager;
import org.datanucleus.StateManager;
import org.datanucleus.exceptions.NucleusDataStoreException;
import org.datanucleus.metadata.AbstractClassMetaData;
import org.datanucleus.metadata.AbstractMemberMetaData;
import org.datanucleus.metadata.NullValue;
import org.datanucleus.metadata.Relation;
import org.datanucleus.store.appengine.query.DatastoreQuery;
import org.datanucleus.store.exceptions.NotYetFlushedException;
import org.datanucleus.store.mapped.DatastoreClass;
import org.datanucleus.store.mapped.mapping.EmbeddedPCMapping;
import org.datanucleus.store.mapped.mapping.InterfaceMapping;
import org.datanucleus.store.mapped.mapping.JavaTypeMapping;
import org.datanucleus.store.mapped.mapping.MappingCallbacks;
import org.datanucleus.store.mapped.mapping.PersistenceCapableMapping;
import org.datanucleus.store.mapped.mapping.SerialisedPCMapping;
import org.datanucleus.store.mapped.mapping.SerialisedReferenceMapping;

import java.util.List;

/**
 * @author Max Ross <maxr@google.com>
 */
class DatastoreRelationFieldManager {

  // Needed for relation management in datanucleus.
  private static final int[] NOT_USED = {0};
  static final int IS_ANCESTOR_VALUE = -1;
  private static final int[] IS_ANCESTOR_VALUE_ARR = {IS_ANCESTOR_VALUE};
  static final String ANCESTOR_KEY_PROPERTY = "____ANCESTOR_KEY____";

  public static final int IS_FK_VALUE = -2;
  private static final int[] IS_FK_VALUE_ARR = {IS_FK_VALUE};

  private final DatastoreFieldManager fieldManager;

  // Events that know how to store relations.
  private final List<StoreRelationEvent> storeRelationEvents = Utils.newArrayList();

  DatastoreRelationFieldManager(DatastoreFieldManager fieldManager) {
    this.fieldManager = fieldManager;
  }

  /**
   * Applies all the relation events that have been built up.
   */
  void storeRelations(KeyRegistry keyRegistry) {
    if (storeRelationEvents.isEmpty()) {
      return;
    }
    if (fieldManager.getEntity().getKey() != null) {
      keyRegistry.registerKey(getStoreManager(), getStateManager(), fieldManager);
    }
    for (StoreRelationEvent event : storeRelationEvents) {
      event.apply();
    }
    storeRelationEvents.clear();
  }

  private DatastoreManager getStoreManager() {
    return fieldManager.getStoreManager();
  }

  void storeRelationField(final AbstractClassMetaData acmd,
                          final AbstractMemberMetaData ammd, final Object value,
                          final boolean isInsert, final InsertMappingConsumer consumer) {
    StoreRelationEvent event = new StoreRelationEvent() {
      public void apply() {
        DatastoreTable table = getStoreManager().getDatastoreClass(
            ammd.getAbstractClassMetaData().getFullClassName(),
            fieldManager.getClassLoaderResolver());

        StateManager sm = getStateManager();
        int fieldNumber = ammd.getAbsoluteFieldNumber();
        // Based on ParameterSetter
        try {
          JavaTypeMapping mapping = table.getMemberMappingInDatastoreClass(ammd);
          if (mapping instanceof EmbeddedPCMapping ||
              mapping instanceof SerialisedPCMapping ||
              mapping instanceof SerialisedReferenceMapping ||
              mapping instanceof PersistenceCapableMapping ||
              mapping instanceof InterfaceMapping) {
            setObjectViaMapping(mapping, table, value, sm, fieldNumber);
            sm.wrapSCOField(fieldNumber, value, false, true, true);
          } else {
            if (isInsert) {
              runPostInsertMappingCallbacks(consumer, value);
            } else {
              runPostUpdateMappingCallbacks(consumer);
            }
          }
        } catch (NotYetFlushedException e) {
          if (acmd.getMetaDataForManagedMemberAtAbsolutePosition(fieldNumber).getNullValue() == NullValue.EXCEPTION) {
            throw e;
          }
          sm.updateFieldAfterInsert(e.getPersistable(), fieldNumber);
        }
      }

      private void setObjectViaMapping(JavaTypeMapping mapping, DatastoreTable table, Object value,
                                       StateManager sm, int fieldNumber) {
        Entity entity = fieldManager.getEntity();
        mapping.setObject(
            fieldManager.getObjectManager(),
            entity,
            table.isParentKeyProvider(ammd) ? IS_ANCESTOR_VALUE_ARR : IS_FK_VALUE_ARR,
            value,
            sm,
            fieldNumber);

        // If the field we're setting is the one side of an owned many-to-one,
        // its pk needs to be the ancestor of the key of the entity we're
        // currently populating.  We look for a magic property that tells
        // us if this change needs to be made.  See
        // DatastoreFKMapping.setObject for all the gory details.
        Object ancestorKey = entity.getProperty(ANCESTOR_KEY_PROPERTY);
        if (ancestorKey != null) {
          entity.removeProperty(ANCESTOR_KEY_PROPERTY);
          String ancestorKeyStr = ancestorKey instanceof Key ?
                                  KeyFactory.keyToString((Key) ancestorKey) : (String) ancestorKey;
          fieldManager.recreateEntityWithAncestor(ancestorKeyStr);
        }
      }
    };

    // If the related object can't exist without the parent it should be part
    // of the parent's entity group.  In order to be part of the parent's
    // entity group we need the parent's key.  In order to get the parent's key
    // we must save the parent before we save the child.  In order to avoid
    // saving the child until after we've saved the parent we register an event
    // that we will apply later.  Note that for one-to-many we need to apply
    // the event after the parent has been saved whether the relationship is
    // dependent or not because we need the parent key in the child table.
    // TODO(maxr) Support storing child keys in the parent table as a list
    // property.
    storeRelationEvents.add(event);
  }

  private void runPostInsertMappingCallbacks(InsertMappingConsumer consumer, Object value) {
    StateManager sm = getStateManager();
    for (MappingCallbacks callback : consumer.getMappingCallbacks()) {
      callback.postInsert(sm);
    }
  }

  private void runPostUpdateMappingCallbacks(InsertMappingConsumer consumer) {
    StateManager sm = getStateManager();
    for (MappingCallbacks callback : consumer.getMappingCallbacks()) {
      callback.postUpdate(sm);
    }
  }

  Object fetchRelationField(
      ClassLoaderResolver clr, AbstractMemberMetaData ammd) {
    DatastoreClass dc = getStoreManager().getDatastoreClass(
        ammd.getAbstractClassMetaData().getFullClassName(), fieldManager.getClassLoaderResolver());
    JavaTypeMapping mapping = dc.getMemberMappingInDatastoreClass(ammd);
    // Based on ResultSetGetter
    Object value;
    if (mapping instanceof EmbeddedPCMapping ||
        mapping instanceof SerialisedPCMapping ||
        mapping instanceof SerialisedReferenceMapping) {
      value = mapping.getObject(
          fieldManager.getObjectManager(),
          fieldManager.getEntity(),
          NOT_USED,
          getStateManager(),
          ammd.getAbsoluteFieldNumber());
    } else {
      int relationType = ammd.getRelationType(clr);
      if (relationType == Relation.ONE_TO_ONE_BI || relationType == Relation.ONE_TO_ONE_UNI) {
        // Even though the mapping is 1 to 1, we model it as a 1 to many and then
        // just throw a runtime exception if we get multiple children.  We would
        // prefer to store the child id on the parent, but we can't because creating
        // a parent and child at the same time involves 3 distinct writes:
        // 1) We put the parent object in order to get a Key.
        // 2) We put the child object, which needs the Key of the parent as
        // the ancestor of its own Key so that parent and child reside in the
        // same entity group.
        // 3) We re-put the parent object, adding the Key of the child object
        // as a property on the parent.
        // The problem is that the datastore does not support multiple writes
        // to the same entity within a single transaction, so there's no way
        // to perform this sequence of events atomically, and that's a problem.

        // We have 2 scenarios here.  The first is that we're loading the parent
        // side of a 1 to 1 and we want the child.  In that scenario we're going
        // to issue an ancestor query against the child table with the expectation
        // that there is either 1 result or 0.

        // The second scearnio is that we're loading the child side of a
        // bidirectional 1 to 1 and we want the parent.  In that scenario
        // the key of the parent is part of the child's key so we can just
        // issue a fetch using the parent's key.
        DatastoreTable table = getStoreManager().getDatastoreClass(
            ammd.getAbstractClassMetaData().getFullClassName(),
            fieldManager.getClassLoaderResolver());
        if (table.isParentKeyProvider(ammd)) {
          // bidir 1 to 1 and we are the child
          value = lookupParent(ammd, mapping);
        } else {
          // bidir 1 to 1 and we are the parent
          value = lookupOneToOneChild(ammd, clr);
        }
      } else if (relationType == Relation.MANY_TO_ONE_BI) {
        value = lookupParent(ammd, mapping);
      } else {
        value = null;
      }
    }
    // Return the field value (as a wrapper if wrappable)
    return getStateManager().wrapSCOField(ammd.getAbsoluteFieldNumber(), value, false, false, false);
  }

  private Object lookupParent(AbstractMemberMetaData ammd, JavaTypeMapping mapping) {
    Key parentKey = fieldManager.getEntity().getParent();
    if (parentKey == null) {
      // unexpected
      throw new NucleusDataStoreException("Field " + ammd.getFullFieldName() + " is an ancestor "
                                          + "provider but the entity does not have a parent.");
    }
    ObjectManager om = getStateManager().getObjectManager();
    return mapping.getObject(om, parentKey, NOT_USED);
  }

  private Object lookupOneToOneChild(AbstractMemberMetaData ammd, ClassLoaderResolver clr) {
    ObjectManager om = getStateManager().getObjectManager();
    AbstractClassMetaData childClassMetaData =
        om.getMetaDataManager().getMetaDataForClass(ammd.getType(), clr);
    String kind = getStoreManager().getIdentifierFactory().newDatastoreContainerIdentifier(
        childClassMetaData).getIdentifierName();
    Entity parentEntity = fieldManager.getEntity();
    // We're going to issue a query for all entities of the given kind with
    // the parent entity's key as their ancestor.  There should be only 1.
    Query q = new Query(kind, parentEntity.getKey());
    DatastoreService datastoreService = DatastoreServiceFactoryInternal.getDatastoreService();
    List<Entity> results = datastoreService.prepare(q).asList(withLimit(2));
    Object value;
    if (results.size() > 1) {
      throw new NucleusDataStoreException(ammd.getFullFieldName() + " is mapped as a 1 to 1 "
                                          + "relationship but there is more than one enity "
                                          + "of kind " + kind + " that is a child of "
                                          + parentEntity.getKey());
    } else if (results.isEmpty()) {
      value = null;
    } else {
      // Exactly one result
      value = DatastoreQuery.entityToPojo(
          results.get(0), childClassMetaData, clr, getStoreManager(), om, false);
    }
    // TODO(maxr) Figure out how to hook this up to a StateManager!
    return value;
  }

  private StateManager getStateManager() {
    return fieldManager.getStateManager();
  }
  /**
   * Supports a mechanism for delaying the storage of a relation.
   */
  private interface StoreRelationEvent {
    void apply();
  }
}
