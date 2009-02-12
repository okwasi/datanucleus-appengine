// Copyright 2008 Google Inc. All Rights Reserved.
package org.datanucleus.store.appengine;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

import org.datanucleus.test.Book;
import org.datanucleus.test.HasMultiValuePropsJPA;

/**
 * @author Max Ross <maxr@google.com>
 */
public class JPAFetchTest extends JPATestCase {

  @Override
  protected EntityManagerFactoryName getEntityManagerFactoryName() {
    return EntityManagerFactoryName.nontransactional_ds_non_transactional_ops_allowed;
  }

  public void testSimpleFetch_Id() {
    Key key = ldth.ds.put(Book.newBookEntity("max", "47", "yam"));

    String keyStr = KeyFactory.keyToString(key);
    Book book = em.find(Book.class, keyStr);
    assertNotNull(book);
    assertEquals(keyStr, book.getId());
    assertEquals("max", book.getAuthor());
    assertEquals("47", book.getIsbn());
    assertEquals("yam", book.getTitle());
  }

  public void testSimpleFetchWithNonTransactionalDatasource() {
    switchDatasource(EntityManagerFactoryName.nontransactional_ds_non_transactional_ops_not_allowed);
    Key key = ldth.ds.put(Book.newBookEntity("max", "47", "yam"));

    String keyStr = KeyFactory.keyToString(key);
    Book book = em.find(Book.class, keyStr);
    assertNotNull(book);
    assertEquals(keyStr, book.getId());
    assertEquals("max", book.getAuthor());
    assertEquals("47", book.getIsbn());
    assertEquals("yam", book.getTitle());
  }

  public void testSimpleFetch_Id_LongIdOnly() {
    Key key = ldth.ds.put(Book.newBookEntity("max", "47", "yam"));

    Book book = em.find(Book.class, key.getId());
    assertNotNull(book);
    String keyStr = KeyFactory.keyToString(key);
    assertEquals(keyStr, book.getId());
    assertEquals("max", book.getAuthor());
    assertEquals("47", book.getIsbn());
    assertEquals("yam", book.getTitle());
  }

  public void testSimpleFetch_Id_LongIdOnly_NotFound() {
    assertNull(em.find(Book.class, -1));
  }

  public void testSimpleFetch_Id_IntIdOnly() {
    Key key = ldth.ds.put(Book.newBookEntity("max", "47", "yam"));

    Book book = em.find(Book.class, Long.valueOf(key.getId()).intValue());
    assertNotNull(book);
    String keyStr = KeyFactory.keyToString(key);
    assertEquals(keyStr, book.getId());
    assertEquals("max", book.getAuthor());
    assertEquals("47", book.getIsbn());
    assertEquals("yam", book.getTitle());
  }

  public void testSimpleFetchWithNamedKey() {
    Key key = ldth.ds.put(Book.newBookEntity("named key", "max", "47", "yam"));

    String keyStr = KeyFactory.keyToString(key);
    Book book = em.find(Book.class, keyStr);
    assertNotNull(book);
    assertEquals(keyStr, book.getId());
    assertEquals("max", book.getAuthor());
    assertEquals("47", book.getIsbn());
    assertEquals("yam", book.getTitle());
    assertEquals("named key", KeyFactory.stringToKey(book.getId()).getName());
  }

  public void testSimpleFetchWithNamedKey_NameOnly() {
    Key key = ldth.ds.put(Book.newBookEntity("named key", "max", "47", "yam"));

    Book book = em.find(Book.class, key.getName());
    assertNotNull(book);
    String keyStr = KeyFactory.keyToString(key);
    assertEquals(keyStr, book.getId());
    assertEquals("max", book.getAuthor());
    assertEquals("47", book.getIsbn());
    assertEquals("yam", book.getTitle());
    assertEquals("named key", KeyFactory.stringToKey(book.getId()).getName());
  }

  public void testSimpleFetchWithNamedKey_NameOnly_NotFound() {
    assertNull(em.find(Book.class, "does not exist"));
  }

  public void testFetchNonExistent() {
    Key key = ldth.ds.put(Book.newBookEntity("max", "47", "yam"));
    ldth.ds.delete(key);
    String keyStr = KeyFactory.keyToString(key);
    assertNull(em.find(Book.class, keyStr));
  }
  public void testFetchSet() {
    Entity e = new Entity(HasMultiValuePropsJPA.class.getSimpleName());
    e.setProperty("strSet", Utils.newArrayList("a", "b", "c"));
    ldth.ds.put(e);

    HasMultiValuePropsJPA pojo = em.find(HasMultiValuePropsJPA.class, e.getKey().getId());
    assertEquals(Utils.newHashSet("a", "b", "c"), pojo.getStrSet());
  }

  public void testFetchArrayList() {
    Entity e = new Entity(HasMultiValuePropsJPA.class.getSimpleName());
    e.setProperty("strArrayList", Utils.newArrayList("a", "b", "c"));
    ldth.ds.put(e);

    HasMultiValuePropsJPA pojo = em.find(HasMultiValuePropsJPA.class, e.getKey().getId());
    assertEquals(Utils.newArrayList("a", "b", "c"), pojo.getStrArrayList());
  }
  public void testFetchLinkedList() {
    Entity e = new Entity(HasMultiValuePropsJPA.class.getSimpleName());
    e.setProperty("strLinkedList", Utils.newArrayList("a", "b", "c"));
    ldth.ds.put(e);

    HasMultiValuePropsJPA pojo = em.find(HasMultiValuePropsJPA.class, e.getKey().getId());
    assertEquals(Utils.newLinkedList("a", "b", "c"), pojo.getStrLinkedList());
  }

  public void testFetchHashSet() {
    Entity e = new Entity(HasMultiValuePropsJPA.class.getSimpleName());
    e.setProperty("strHashSet", Utils.newArrayList("a", "b", "c"));
    ldth.ds.put(e);

    HasMultiValuePropsJPA pojo = em.find(HasMultiValuePropsJPA.class, e.getKey().getId());
    assertEquals(Utils.newHashSet("a", "b", "c"), pojo.getStrHashSet());
  }
  public void testFetchTreeSet() {
    Entity e = new Entity(HasMultiValuePropsJPA.class.getSimpleName());
    e.setProperty("strTreeSet", Utils.newArrayList("a", "b", "c"));
    ldth.ds.put(e);

    HasMultiValuePropsJPA pojo = em.find(HasMultiValuePropsJPA.class, e.getKey().getId());
    assertEquals(Utils.newTreeSet("a", "b", "c"), pojo.getStrTreeSet());
  }
}
