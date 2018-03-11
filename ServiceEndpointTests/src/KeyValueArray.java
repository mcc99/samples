import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

// Added here for time being until I get it into its own project

/**
 * THIS IS NOT FULLY-TESTED BUT APPEARS THAT IT WORKS AS INTENDED FOR THE MOST PART.  UNTIL A VERSION APPEARS WITHOUT
 * THIS NOTICE, USE AT OWN RISK.  FEEL FREE TO MAKE SUGGESTIONS AS WELL.
 *
 * An implementation of Collection using an ArrayList as the underlying List that supports duplicate and null keys.
 * The List holds KVPEntity arrays (KVPEntity is a class defined within this class) each with a length of 2.  For each
 * member, kvpentity[0].getObject() is the key and kvpentity[1].getObject() is the value.  KeyValueArray includes several
 * more methods of particular use aside from the methods defined in the Collection interface.  A given key/value KVPEntity
 * pair (KVPEntity[2]) is herein referred to as an 'entry' of the underlying List.
 * <p>
 * KVPEntity allows both the key and value objects to have "labels" applied to them.  This allows the user to
 * tag the keys and values of pairs with common values and use them to manage the content of an instance of this class.  For
 * example, if you populate an object instance with 100 key-value pairs, each key may be a simple number, with each value being
 * some kind of object of this or that kind.  But say you want to identify 1/3 of the entries in the collection as distinct
 * from the others, but there is no distinguishing property among the objects nor is there a pattern for the keys' values.
 * What then?  Being able to tag the entries with labels and be able to get all the entries, or object values (or key values)
 * from the collection based on one or more label values you have associated with either the key, the value, or both, would be
 * useful.  Both the key and value objects can be tagged with multiple labels (as Strings).  This allows for a very flexible
 * way to characterize the entries in the collection and to have access to them based on attributes having nothing to do
 * with either the key or the value objects in the collection entries.
 * <p>
 * Some sources have created decorated Maps that alter HashMap so that it holds a key pointing to a List of
 * objects (ie, a multi-valued 1:N key:value list).  Instead, this implementation allows keys to repeat as often as added,
 * and values likewise.  The class addresses the need for a List of key-value pairs unconstrained by typical
 * rules around keys; for example, a key value can be null, as can be a value, and occur repeatedly in the
 * underlying List.
 * <p>
 * The constructor is private.  A new instance is created only by using getInstance(), except that the class
 * also provides a static instance of itself accessible via getCacheInstance().  This instance,
 * occurring only once for a running JVM, is created only when required by a call to getCacheInstance(), and
 * can be used to share data across objects.  The cache instance as well as any instances obtained via getInstance()
 * should be wrapped when using an iterator, etc., per the Collections Javadoc advice, if planning to use the object
 * across threads.  For this reason, interface-compliant methods herein are not synchronized.  The non-interface-compliant
 * methods are, however.
 *
 * @author Matt Campbell (mcc99@hotmail.com)
 * @version 0.1
 */
public class KeyValueArray implements Collection, java.io.Serializable {

    private static final long serialVersionUID = 8683666681163488189L;

    private ArrayList<KVPEntity[]> list = new ArrayList<>();
    private static volatile KeyValueArray cache = null;

    // Each entry in the underlying List is a KVPEntity[2].  kvpentity[0].getObject() is the key, kvpentity[1].getObject()
    // the value.  The index positions are defined and constrained here.
    private enum ENTRY_ROLE {
        KEY(0),
        VALUE(1);
        private int idx;

        ENTRY_ROLE(int idx) {
            this.idx = idx;
        }

        public int index() {
            return idx;
        }
    }


    public static void main(String[] args) {

        KeyValueArray kva = KeyValueArray.getInstance();
        Object[] oo = new Object[2];
        oo[0] = new Object();
        oo[1] = new Object();
        // kva.add(new Object(), oo);
        // kva.add((Object) oo);
        String[] lbls = {"jjj", "hhh"};
        kva.addAll(oo, lbls);
        kva.addAll(oo, "jjjj");


        // p(""+ENTRY_ROLE.KEY.index());
        // p(""+ENTRY_ROLE.VALUE.index());
    }


    /**
     * Factory method returning a new instance of KeyValueArray.
     */
    @SuppressWarnings("unchecked")
    public static KeyValueArray getInstance() {
        return new KeyValueArray();
    }

    /**
     * Returns the static KeyValueArray instance associated with the class.
     */
    @SuppressWarnings("unchecked")
    public static synchronized KeyValueArray getCacheInstance() {
        if (cache == null) {
            cache = getInstance();
        }
        return cache;
    }


    /**
     * Returns an Object[] comprised of all the key objects in the underlying List.
     */
    public synchronized Object[] keys() {
        return getEntryMembers(ENTRY_ROLE.KEY);
    }

    /**
     * Returns an Object[] comprised of all the value objects in the underlying List.
     */
    public synchronized Object[] values() {
        return getEntryMembers(ENTRY_ROLE.VALUE);
    }

    /**
     * Returns as a List (ArrayList) all the key objects in the underlying List.
     */
    @SuppressWarnings("unchecked")
    public synchronized List keyList() {
        return Arrays.asList(keys());
    }

    /**
     * Returns as a List (ArrayList) all the value objects in the underlying List.
     */
    @SuppressWarnings("unchecked")
    public synchronized List valueList() {
        return Arrays.asList(values());
    }


    /**
     * Adds a new entry to the underlying List using the supplied value as both the key and value for the new entry.
     * Returns true if the method succeeds, false if not.
     * Note: Do NOT use this method to add an entry (KVPEntity[2] or Object[2]) to the underlying List.  You will in essence be adding
     * an Object[] to the underlying List with value *as* a key and value.  Use addEntry(Object[]) to add a key-value pair to
     * the underlying List as a new entry.
     *
     * @param value
     * @see #addEntry(Object[])
     */
    @Override
    public boolean add(Object value) {
        return add(value, value);
    }


    /**
     * Does the same thing that add(Object) does, but associates the passed-in labels value(s) as the labels(s) tagged
     * to the new entry value.  The new entry key (the same object as the value) also has the same label(s).
     * Returns true if the method succeeds, false if not.
     *
     * @param value
     * @param labels
     * @see #add(Object)
     */
    public boolean add(Object value, String... labels) {
        KVPEntity kvpe = new KVPEntity(value, labels);
        return add(kvpe, kvpe);
    }


    /**
     * Creates a new entry (KVPEntity[2]) using the params as a key/value entry, then adds it
     * to the underlying List.  Returns true if the method succeeds, false if not.
     *
     * @param key
     * @param value
     * @see List#add(Object)
     */
    public synchronized boolean add(Object key, Object value) {
        KVPEntity[] entry = {new KVPEntity(key), new KVPEntity(value)};
        return list.add(entry);
    }

    /**
     * Creates a new entry (KVPEntity[2]) from the passed-in key and value, associating the supplied label for each to each,
     * then adds the new entry to the underlying List.  Returns true if the method succeeds, false if not.
     *
     * @param key
     * @param keyLabel
     * @param value
     * @param valueLabel
     * @see #add(Object, String[], Object, String[])
     */
    public synchronized boolean add(Object key, String keyLabel, Object value, String valueLabel) {
        KVPEntity[] entry = {new KVPEntity(key, keyLabel), new KVPEntity(value, valueLabel)};
        return list.add(entry);
    }


    /**
     * Creates a new entry (KVPEntity[2]) from the passed-in key and value, associating the supplied label(s) for each to each,
     * then adds the new entry to the underlying List.  Returns true if the method succeeds, false if not.
     *
     * @param key
     * @param keyLabels
     * @param value
     * @param valueLabels
     * @see #add(Object, String, Object, String)
     */
    public synchronized boolean add(Object key, String[] keyLabels, Object value, String[] valueLabels) {
        KVPEntity[] entry = {new KVPEntity(key, keyLabels), new KVPEntity(value, valueLabels)};
        return list.add(entry);
    }


    /**
     * Adds a new entry to the underlying List with the supplied two KVPEntity objects as key and value.
     * Returns true if the method succeeds, false if not.
     *
     * @param ek
     * @param ev
     */
    private synchronized boolean add(KVPEntity ek, KVPEntity ev) {
        KVPEntity[] entry = {ek, ev};
        return list.add(entry);
    }


    /**
     * Adds a new entry (KVPEntity[2]) to the underlying List.  The Object[] passed in must have a length of 2 or the
     * method throws an IllegalArgumentException exception.  entry[0] is the key, entry[1] the value.
     * An IllegalArgumentException is thrown if entry does not have a length of 2.  Returns true if the method
     * succeeds, false if not. (If entry is null, it returns false, otherwise, true.)
     *
     * @param entry
     * @throws IllegalArgumentException
     * @see #addEntry(Object[], String...)
     */
    public synchronized boolean addEntry(Object[] entry) throws IllegalArgumentException {
        if (entry == null) {
            return false;
        }
        if (entry.length != 2) {
            throw new IllegalArgumentException("The supplied Object[] must have a length of 2: [0] = key, [1] = value.  " +
                    "The supplied Object[] has a length of " + entry.length + ".");
        }
        KVPEntity[] kvpe = {new KVPEntity(entry[0]), new KVPEntity(entry[1])};
        return list.add(kvpe);
    }


    /**
     * Adds a new entry (KVPEntity[2]) to the underlying List.  If entry is null, returns false.  If non-null but not
     * comprised of exactly two objects, it throws an IllegalArgumentException.  entry[0] is the key, entry[1] the value.
     * labels is a single String or array thereof that act as labels to the value (entry[1]) object.  Returns true if
     * the addition succeeds, false if not.
     *
     * @param entry
     * @param labels
     * @throws IllegalArgumentException
     * @see #addEntry(Object[])
     */
    public synchronized boolean addEntry(Object[] entry, String... labels) throws IllegalArgumentException {
        if (entry == null) {
            return false;
        }
        if (entry.length != 2) {
            throw new IllegalArgumentException("The supplied Object[] must have a length of 2: [0] = key, [1] = value.  " +
                    "The supplied Object[] has a length of " + entry.length + ".");
        }
        KVPEntity[] kvpe = {new KVPEntity(entry[0], labels), new KVPEntity(entry[1], labels)};
        return list.add(kvpe);
    }


    /**
     * Takes Object[] and adds all its members as values to the underlying List as entries (KVPEntity[2]).  The key for each
     * entry is the value itself.  Returns true if the underlying List was mutated, false if not.  (false is returned when
     * the supplied values parameter is null or has a length of 0.)
     *
     * @param values
     * @see #add(Object)
     */
    public synchronized boolean addAll(Object[] values) {
        if (values == null || values.length == 0) {
            return false;
        }
        Arrays.stream(values).forEach(this::add);
        return true;
    }

    // -----------------------------------------------

    /**
     * Adds all objects in the values array to the underlying List as entries (KVPEntity[2]).  The value
     * objects are also used as the keys.  The labels passed in are also associated with the objects
     * used as both keys and values.  Returns true if the method succeeds, false if not (i.e.,
     * if the underlying List was not mutated).
     *
     * @param values
     * @param labels
     * @see #addAll(Object[])
     */
    public synchronized boolean addAll(Object[] values, String... labels) {
        if (values == null || values.length == 0) {
            return false;
        }
        Arrays.stream(values).forEach(o -> {
            add(o, labels);
        });
        return true;
    }


    /**
     * Takes a Collection of Object or Object[].  If passing in a Collection of Object[], all must have
     * a length of 2: key, value.  If any member of the Collection is an Object[], it assumes all are meant to be
     * of type Object[].  If any are not, an IllegalArgumentException is thrown and further if any
     * Object[] members are not a length of 2, the exception is thrown.  If all members are Object, then each one is
     * added to the underlying List by a call to add(Object).  Otherwise, each Object[2] is added via call to
     * add(Object, Object).  Returns true if the underlying List was mutated, false if not (ie, if supplied c was null
     * or a length of 0).
     *
     * @param c
     * @throws IllegalArgumentException
     * @see #add(Object)
     * @see #add(Object, Object)
     */
    @Override
    @SuppressWarnings("unchecked")
    public boolean addAll(Collection c) throws IllegalArgumentException {
        if (c == null || c.size() == 0) {
            return false;
        }
        if (c.stream().anyMatch(o -> o instanceof Object[] && ((Object[]) o).length != 2)) {
            throw new IllegalArgumentException("All members of the supplied Collection must be Object[] with a length of 2 if any are of type Object[].");
        }
        if (c.stream().allMatch(Object[].class::isInstance)) {
            c.forEach(o -> {
              Object[] arr = (Object[]) o;
              add(arr[0], arr[1]);
            });
            return true;
        }
        if (c.stream().noneMatch(Object[].class::isInstance)) {
            c.forEach(this::add);
            return true;
        }
        throw new IllegalArgumentException("All members of the supplied Collection must be either Object[] with a length of 2, or Object.");
    }


    /**
     * Does as addAll(Collection) does only it also associates the supplied label(s) with the value objects found in c, the objects in
     * c being considered values if each member of c is an Object or key-value pairs if each member of c is an Object[] with a
     * size of 2.  The exception is thrown based on the same criteria described in addAll(Collection).
     *
     * @param c
     * @param labels
     * @throws IllegalArgumentException
     * @see #addAll(Collection)
     */
    @SuppressWarnings("unchecked")
    public boolean addAll(Collection c, String... labels) throws IllegalArgumentException {
        if (c == null || c.size() == 0) {
            return false;
        }
        if (c.stream().anyMatch(entry -> entry instanceof Object[] && ((Object[]) entry).length != 2)) {
            throw new IllegalArgumentException("All members of the supplied Collection must be Object[] with a length of 2 if any are of type Object[].");
        }
        if (c.stream().allMatch(Object[].class::isInstance)) {
            c.forEach(entry -> {
                addEntry((Object[]) entry, labels);
            });
            return true;
        }
        if (c.stream().noneMatch(Object[].class::isInstance)) {
            c.forEach(o -> {
                add(o, labels);
            });
            return true;
        }
        throw new IllegalArgumentException("All members of the supplied Collection must be either Object[] with a length of 2, or Object.");
    }


    /**
     * Clears the underlying List of all KVPEntity[2] entries.
     */
    @Override
    public void clear() {
        list.clear();
    }


    /**
     * Returns true if among the KVPEntity[2] entries in the underling List, the Object at kvpentity[1].getObject() (the value) equals o
     * either by == or .equals() comparison.  If not, it returns false.
     *
     * @param o
     */
    @Override
    public boolean contains(Object o) {
        return Arrays.stream(values()).anyMatch(value -> isEqual(o, value));
    }

    /**
     * Returns true if among the KVPEntity[] entries in the underling List, the Object at kvpentity[0].getObject() (the key) equals o
     * either by == or .equals() comparison.  If not, it returns false.
     *
     * @param o
     */
    public synchronized boolean containsKey(Object o) {
        return Arrays.stream(keys()).anyMatch(key -> isEqual(o, key));
    }

    /**
     * Returns number of times the object matching o in the underlying List appears as a value (kvpentity[1].getObject())
     * among the KVPEntity[2] entries in the underling List.  Equality is determined by == or .equals() comparison.
     *
     * @param o
     */
    public synchronized int containsCount(Object o) {
        return (int) Arrays.stream(values()).filter(value -> isEqual(o, value)).count();
    }

    /**
     * Returns number of times the object matching o in the underlying List appears as a key (kvpentity[0].getObject())
     * among the KVPEntity[2] entries in the underling List.  Equality is determined by == or .equals() comparison.
     *
     * @param o
     */
    public synchronized int containsKeyCount(Object o) {
        return (int) Arrays.stream(keys()).filter(key -> isEqual(o, key)).count();
    }


    /**
     * Returns true if all members of c are found among the values (kvpentity[1].getObject()) in the
     * underlying List, and false if not.  If c is null or has a size of 0, false is
     * always returned.
     *
     * @param c
     */
    @Override
    @SuppressWarnings("unchecked")
    public boolean containsAll(Collection c) {
        return c != null && c.size() != 0 && valueList().containsAll(c);
    }


    /**
     * Returns true if all members of c are found among the keys (kvpentity[0].getObject()) in the
     * underlying List, and false if not.  If c is null or has a size of 0, false is returned.
     *
     * @param c
     */
    @SuppressWarnings("unchecked")
    public synchronized boolean containsAllKeys(Collection c) {
        return c != null && c.size() != 0 && keyList().containsAll(c);
    }


    /**
     * If o is equal to this instance by == or .equals() comparison, it returns true.
     * Predictably, if the o is null, it always returns false.
     *
     * @param o
     */
    @Override
    public boolean equals(Object o) {
        return o != null && o instanceof KeyValueArray && super.equals(o);
    }


    /**
     * Returns this instance's underlying Object hash code value
     */
    @Override
    public int hashCode() {
        return super.hashCode();
    }


    /**
     * Returns true if this instance's underlying List is empty (size == 0) or false if not.
     */
    @Override
    public boolean isEmpty() {
        return size() == 0;
    }


    /**
     * Returns the Iterator for this instance's underlying List object.  Note the returned Iterator is
     * typed to KVPEntity[].
     */
    @Override
    public Iterator<KVPEntity[]> iterator() {
        return list.iterator();
    }


    /**
     * Removes the first instance of o as it is found in the value (kvpentity[1].getObject()) of the entries
     * in the underlying List.  Note any other duplicate instance references are *not* removed.  For that, use
     * removeAll(Object).  To remove an entry by key value, use removeByKey(Object).  (To remove all instances by key
     * value, use removeAllByKey(Object).)  Returns true if a removal took place, false if not.
     *
     * @param o
     * @see #removeAll(Object)
     * @see #removeByKey(Object)
     * @see #removeAllByKey(Object)
     */
    @Override
    public boolean remove(Object o) {
        Object[] values = values();
        for (int i = 0; i < values.length; i++) {
            if (isEqual(o, values[i])) {
                list.remove(i);
                return true;
            }
        }
        return false;
    }


    /**
     * Removes all instances of o as found in the value (kvpentity[1].getObject()) of the entries
     * in the underlying List.  To remove an entry by key value, use removeByKey(Object).  To remove
     * all instances by key value, use removeAllByKey(Object).  Note: Do not confuse this method with
     * removeAll(Collection), part of the Collection interface spec.  That removes *all* elements in the underlying List
     * found in the passed-in Collection, not just a single object.  Returns true if a removal took place, false if not.
     *
     * @param o
     * @see #removeByKey(Object)
     * @see #removeAllByKey(Object)
     * @see Collection#removeAll(Collection)
     */
    public synchronized boolean removeAll(Object o) {
        return list.removeIf(entry -> isEqual(o, entry[1].getObject()));
    }


    /**
     * Removes the first instance of o as it is found in the key (kvpentity[0].getObject()) of the entries
     * in the underlying List.  Note any other duplicate instance references are *not* removed.  For that, use
     * removeAllByKey(Object).  To remove an entry by value, use remove(Object).  To remove all instances by
     * value, use removeAll(Object).  Returns true if a removal took place, false if not.
     *
     * @param o
     * @see #removeAllByKey(Object)
     * @see #remove(Object)
     * @see #removeAll(Object)
     */
    public synchronized boolean removeByKey(Object o) {
        Object[] keys = keys();
        for (int i = 0; i < keys.length; i++) {
            if (isEqual(keys[i], o)) {
                list.remove(i);
                return true;
            }
        }
        return false;
    }


    /**
     * Removes all instances of o as it is found in the key (kvpentity[0].getObject()) of the entries
     * in the underlying List.  To remove the first instance of an entry by value, use remove(Object).  To remove
     * just the first instance of an entry by key, use removeByKey(Object).  Returns true if a removal took place,
     * false if not.
     *
     * @param o
     * @see #remove(Object)
     * @see #removeByKey(Object)
     */
    public synchronized boolean removeAllByKey(Object o) {
        return list.removeIf(entry -> isEqual(o, entry[0].getObject()));
    }


    /**
     * Removes from the underlying List all instances of whatever objects are in c also found as values
     * (kvpentity[1].getObject()) in the entries in the underlying List.  Returns true if any such instances were
     * found and removed, false if not.
     *
     * @param c
     */
    @SuppressWarnings("unchecked")
    public synchronized boolean removeAll(Collection c) {
        if (c == null || c.size() == 0) {
            return false;
        }
        int size = size();
        c.forEach(o -> list.removeIf(entry -> isEqual(o, entry[1].getObject())));
        return size() < size;
    }


    /**
     * When called, the only KVPEntity[2] instances that will remain in the underlying List will have their values
     * (kvpentity[1].getObject()) found somewhere among the objects in c.  Returns true if the underlying List was
     * modified, false if not.
     *
     * @param c
     */
    @Override
    public boolean retainAll(Collection c) {
        if (c == null || c.size() == 0) {
            return false;
        }
        int size = size();
        Arrays.stream(values()).forEach(value -> {
            if (!c.contains(value)) {
                removeAll(value);
            }
        });
        return size() < size;
    }


    /**
     * Returns the number of entries in the underlying List managed by this object instance.
     */
    @Override
    public int size() {
        return list.size();
    }


    /**
     * Returns an array of the values (kvpentity[1].getObject()) in the underlying List.  If there are no members in the underlying
     * List, an Object[] of zero length is returned.
     */
    public Object[] toArray() {
        return values();
    }

    /**
     * Delegates to toArray() returning the new Object[] array it creates if null is supplied or if a.length is less than {underlying List}.size()
     * If a.length is greater than or equal to list.size(), a[] is populated by the values of the underlying List.  If a[] is greater in length than
     * list.size(), the element immediately after the last value copied into a[] from the underlying List is set to null.  (This is the
     * same behavior as with this method in the ArrayList class.)  Critical to note that this method is mutating a[] if
     * {Underlying list}.size() is less than or equal to a.length.  However a[] is not mutated if a.length is less than
     * {Underlying list}.size().
     *
     * @param a
     * @see #toArray()
     * @see ArrayList#toArray()
     */
    @Override
    public Object[] toArray(Object[] a) {
        if (a == null || a.length < size()) {
            return toArray();
        }
        Object[] values = toArray();
        int size = values.length;
        System.arraycopy(values, 0, a, 0, values.length);
        if (a.length > size) {
            a[size] = null;
        }
        return a;
    }


    /**
     * Removes an entry (KVPEntity[2]) from the underlying List by index value.  Returns the entry removed if the list was mutated, null if not.
     *
     * @param i
     */
    public synchronized KVPEntity[] remove(int i) {
        return list.remove(i);
    }


    /**
     * Removes entries (KVPEntity[2]) by index values supplied by indexes.  Note that indexes is numbered
     * relative to the location of all entries before the first to be removed is removed.
     * That is, the user need not worry about shifts in index position as each member is removed from
     * the underlying List.  Returns true if the underlying List was mutated, false if not.
     *
     * @param indexes
     */
    public synchronized boolean remove(final int[] indexes) {
        if (indexes == null || indexes.length == 0) {
            return false;
        }
        int cnt = 0;
        int rcnt = 0;
        /* Clone orig passed-in indexes[] b/c we need to use sorted ver. of same so that index vals. are correctly tracked.
           Sorting the orig passed-in array would be mutating the array thereby altering the structure of it w/o the
           user expecting it.
         */
        int[] sorted = indexes.clone();
        Arrays.sort(sorted);
        // Keeps track of index values removed so we don't act on any repeat vals. found in indexes[]
        int[] removed = new int[sorted.length];
        // Assign all elements to -1 from 0 or else removal of element at index 0 is impossible
        IntStream.of(removed).forEach(i -> i = -1);
        int size = size();
        for (int i = 0; i < size; i++) {
            if (canRemove(cnt, sorted, removed)) {
                list.remove(i - rcnt);
                removed[rcnt++] = cnt;
            }
            cnt++;
        }
        return rcnt > 0;
    }


    /**
     * Returns true if idx is found among the values in indexes, or if idx is not found among the values in indexes, is found among
     * the values in removed.  It returns false otherwise.
     *
     * @param idx
     * @param indexes
     * @param removed
     */
    private boolean canRemove(int idx, int[] indexes, int[] removed) {
        return IntStream.of(indexes).anyMatch(i -> (i == idx)) && IntStream.of(removed).noneMatch(i -> (i == idx));
    }


    /**
     * Returns value (kvpentity[1].getObject()) of the entry in the underlying List found at the supplied idx location.
     *
     * @param idx
     */
    public synchronized Object getValue(int idx) {
        return values()[idx];
    }


    /**
     * Returns the key (kvpentity[0].getObject()) of the entry in the underlying List at the supplied idx location.
     *
     * @param idx
     */
    public synchronized Object getKey(int idx) {
        return keys()[idx];
    }


    /**
     * Returns the value (kvpentity[1].getObject()) of the first entry found in the underlying List whose key
     * (kvpentity[0].getObject()) matches the supplied key object.  If the key is not found among the key values
     * in the underlying List, an exception is thrown.
     *
     * @param key
     * @throws Exception
     */
    public synchronized Object getFirst(final Object key) throws Exception {
        return get(key).get(0);
    }


    /**
     * Returns the value (kvpentity[1].getObject()) of the last entry found in the underlying List whose key
     * (kvpentity[0].getObject()) matches the supplied key object.  If the key is not found among the key values
     * in the underlying List, an exception is thrown.
     *
     * @param key
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public synchronized Object getLast(final Object key) throws Exception {
        List l = get(key);
        return get(key).get(l.size() - 1);
    }


    /**
     * Returns a List of all values (kvpentity[1].getObject()) in the underlying List that have the specified
     * key (kvpentity[0].getObject()) supplied to the method.  If the supplied key is not found among the keys in
     * the entries in the underlying List, an Exception is thrown.
     *
     * @param key
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public synchronized List get(final Object key) throws Exception {
        if (!keyList().contains(key)) {
            throw new Exception("Key '" + key + "' not found among underlying List's key values.");
        }
        List l = new ArrayList();
        list.forEach(entry -> {
            if (isEqual(key, entry[0].getObject())) {
                l.add(entry[1].getObject());
            }
        });
        return l;
    }


    /**
     * Gets all value objects in a List from the underlying List whose label values contain any value
     * found in labels.  Always returns a valid List even if it has no members.
     *
     * @param labels
     */
    public synchronized List getByLabel(String... labels) {
        return getKVPEntityObjectsByLabel(ENTRY_ROLE.VALUE, labels);
    }

    /**
     * Gets all key objects in a List from the underlying List whose label values contain any value
     * found in labels.  Always returns a valid List even if it has no members.
     *
     * @param labels
     */
    public synchronized List getKeysByLabel(String... labels) {
        return getKVPEntityObjectsByLabel(ENTRY_ROLE.KEY, labels);
    }


    /**
     * Gets all objects (kvpentity[entryRole.index()].getObject(); entryRole.index() resolves either to 0 or 1) associated
     * with the supplied ENTRY_ROLE value in a List from the underlying List whose label values contain any value found in
     * labels.  Always returns a valid List even if it has no members.
     *
     * @param labels
     */
    private List getKVPEntityObjectsByLabel(ENTRY_ROLE entryRole, String... labels) {
        List<Object> l = new ArrayList<>();
        if (labels == null || labels.length == 0) {
            return l;
        }
        list.forEach(entry -> {
            entry[entryRole.index()].getLabels().forEachRemaining(lbl -> {
                if (Arrays.stream(labels).anyMatch(lbl1 -> isEqual(lbl, lbl1))) {
                    l.add(entry[entryRole.index()].getObject());
                }
            });
        });
        return l;
    }


    /**
     * Returns List of values (kvpentity[1].getObject()) based on the values in indexes for their corresponding entries
     * in the underlying List.
     *
     * @param indexes
     */
    @SuppressWarnings("unchecked")
    public synchronized List getValues(final int[] indexes) {
        List l = new ArrayList();
        if (indexes == null || indexes.length == 0) {
            return l;
        }
        IntStream.of(indexes).forEach(
                idx -> {
                    l.add(getValue(idx));
                }
        );
        return l;
    }


    /**
     * Returns the first index location of the entry in the underlying List whose value (kvpentity[1].getObject()) is equal
     * to the object supplied to the method.  Throws an exception if no matching object was found among the
     * underlying List entries' values.
     *
     * @param value
     * @throws Exception
     */
    public synchronized int getIndex(Object value) throws Exception {
        try {
            return getIndex(value, values());
        } catch (Exception e) {
            throw new Exception("No matching value was found among the entries in the underlying List.");
        }
    }


    /**
     * Returns the index locations of the entries in the underlying List whose values (kvpentity[1].getObject()) are equal
     * to the object supplied to the method.  Returns an empty array of int if no matching values were found.
     *
     * @param value
     */
    public synchronized int[] getIndexes(Object value) {
        return getIndexes(value, values());
    }


    /**
     * Returns the first index location of the entry in the underlying List whose key (kvpentity[0].getObject()) is equal
     * to the object supplied to the method.  Throws an exception if no matching object was found among the
     * underlying List entries' keys.
     *
     * @param key
     * @throws Exception
     */
    public synchronized int getIndexByKey(Object key) throws Exception {
        try {
            return getIndex(key, keys());
        } catch (Exception e) {
            throw new Exception("No matching key was found among the entries in the underlying List.");
        }
    }


    /**
     * Returns the index locations of the entries in the underlying List whose keys (kvpentity[0].getObject()) are equal
     * to the object supplied to the method.  Returns an empty array of int if no matching keys were found.
     *
     * @param key
     */
    public synchronized int[] getIndexesByKey(Object key) {
        return getIndexes(key, keys());
    }


    /**
     * Returns a key-value entry (Object[2]) from the underlying List whose index location is equal to idx.
     * kvpentity[0] is the key, kvpentity[1] is the value.
     *
     * @param idx
     */
    @SuppressWarnings("UnnecessaryLocalVariable")
    public synchronized Object[] getKeyValueObjectPair(int idx) {
        KVPEntity[] kvpe = list.get(idx);
        Object[] entry = {kvpe[0], kvpe[1]};
        return entry;
    }


    /**
     * Returns a key-value entry (Object[2]) from the underlying List whose index location is equal to idx
     * This method is synonymous with getKeyValueObjectPair(int).
     *
     * @param idx
     * @see #getKeyValueObjectPair(int)
     */
    public synchronized Object[] getEntry(int idx) {
        return getKeyValueObjectPair(idx);
    }


    /**
     * Returns a key-value entry (KVPEntity[2]) from the underlying List whose index location is equal to idx.
     *
     * @param idx
     * @see List#get(int)
     */
    public synchronized KVPEntity[] getKVPEntry(int idx) {
        return list.get(idx);
    }


    /**
     * Returns the key-value pair objects in the underlying List as a List<Object[]>,
     * object[0] of each entry in the List being the key (kvpentity[0].getObject()) and object[1]
     * being the value (kvpentity[1].getObject()).
     *
     * @see #getEntries()
     */
    @SuppressWarnings("unchecked")
    public synchronized List<Object[]> getEntriesList() {
        List<Object[]> l = new ArrayList<>();
        Arrays.stream(getEntries()).forEach(entry ->
        {
         l.add((Object[]) entry);
        });
        return l;
    }


    /**
     * Returns the underlying List as an Object[] comprised of Object[2] arrays, with object[0] of each member as
     * the key (kvpentity[0].getObject()) and object[1] of each member as the value (kvpentity[1].getObject()).
     *
     * @see #getEntriesList()
     */
    public synchronized Object[] getEntries() {
        Object[] retn = new Object[list.size()];
        MutableInteger idx = new MutableInteger();
        list.forEach(kvpe ->
         {
           Object[] entry = {kvpe[0].getObject(), kvpe[1].getObject()};
           retn[idx.increment()] = entry;
         });
        return retn;
    }


    /**
     * Returns the underlying List as a List of KVPEntity[2], kvpentity[0].getObject() of each entry is
     * the key, kvpentity[1].getObject() is the value.
     *
     */
    public synchronized List<KVPEntity[]> getKVPEntries() {
        return list;
    }


    /**
     * Returns an Object[] holding all the elements at a specific index position (entryRole.index(), a value either of 0
     * or 1) from among the underlying List entry members (KVPEntity[2]).
     *
     * @param entryRole
     */
    private Object[] getEntryMembers(ENTRY_ROLE entryRole) {
        final int entryRoleIndex = entryRole.index();
        Object[] retn = new Object[size()];
        MutableInteger idx = new MutableInteger();
        list.forEach(entry -> {
            retn[idx.increment()] = entry[entryRoleIndex].getObject();
        });
        return retn;
    }


    /**
     * Returns the index location of the member of array that is equal to target.
     * Throws an exception if no matching object was found among array's members.
     *
     * @param target
     * @param array
     * @throws Exception
     */
    private synchronized int getIndex(Object target, Object[] array) throws Exception {
        int cnt = 0;
        for (Object o : array) {
            if (isEqual(o, target)) {
                return cnt;
            }
            cnt++;
        }
        throw new Exception("No match found in supplied array for supplied target.");
    }


    /**
     * Returns the index locations of the members of array that are equal to target.
     * Throws an exception if no matching objects were found among array's members.
     *
     * @param target
     * @param array
     */
    private synchronized int[] getIndexes(Object target, Object[] array) {
        MutableInteger idx = new MutableInteger(0);
        List<Integer> indexes = new ArrayList<>();
        Arrays.stream(array).forEach(o -> {
                    if (isEqual(o, target)) {
                     indexes.add(idx.value());
                    }
                    idx.increment();
                }
        );
        int[] retn = new int[indexes.size()];
        idx.value(-1);
        indexes.forEach(i -> {
            retn[idx.increment()] = i;
        });
        return retn;
    }


    /**
     * Checks for equality between o and o1.  If by == or .equals() comparison the two objects are equal,
     * it returns true, false if not.
     *
     * @param o
     * @param o1
     */
    private boolean isEqual(Object o, Object o1) {
        return o == o1 || (o != null && o1 != null && o.equals(o1));
    }

    /**
     * Delegates to System.out.println(), printing the supplied String.  Used for debug only.
     *
     * @param s
     */
    private static void p(String s) {
        System.out.println(s);
    }


    /**
     * Not to be confused with MutableInt found in Apache Commons.  (MutableInt in Apache Commons is
     * not used as to avoid the external dependency.)  This class is used to avoid the heavy AtomicInteger
     * class that is meant for parallel stream operations.  Note this class is deliberately private,
     * unlike class KVPEntity defined herein.
     *
     * Note the default value for a new instances is -1.  This is because increment() increments the current
     * value by 1, then returns that new value.  Most current usages of an instance in KeyValueArray begin with invoking
     * increment() as an array counter in a loop, so {instance}.increment() returns 0 vs. -1 to start.  The starting value
     * can be set to a different value by using the MutableInteger constructor that takes an int as a parameter.
     */
    static class MutableInteger {

        private int value = -1;

        MutableInteger() {
        }

        MutableInteger(int i) {
            value(i);
        }

        int value() {
            return value;
        }

        void value(int i) {
            value = i;
        }

        int increment() {
            return ++value;
        }

        int decrement() {
            return --value;
        }
    }

    /*
    This structure violates the principle of Inversion of Control.  It does so in the name of
    keeping KeyValueArray modular, free of references to any more interfaces or 3rd-party classes.
    */
    public class KVPEntity {
        private Object o = null;
        private Set<String> labels = null;

        KVPEntity() {
        }

        KVPEntity(Object o) {
            this.o = o;
        }

        KVPEntity(Object o, String... labels) {
            this.o = o;
            addLabels(labels);
        }

        /**
         * Adds all the values in labels that are not null and have a length > 0 to the 'labels' Set collection
         * associated with this KVPEntity instance.  Since 'labels' is a Set, no duplicates will be retained.
         * Returns true if the 'labels' Set membership was changed as a result of the method, false if not.
         *
         * @param labels
         */
        boolean addLabels(String... labels) {
            if (!isUsableStringSet(labels)) {
             return false;
            }
            ensureLabelSet();
            int start = this.labels.size();
            Arrays.stream(labels).filter(label -> label != null && label.length() > 0)
                    .forEach(label -> this.labels.add(label));
            return this.labels.size() > start;
        }

        /**
         * Adds label to the Set of label values ('labels') associated with this instance.
         * Returns true if the 'labels' Set was modified as a result, false if not.
         *
         * @param label
         * @see #addLabels(String...)
         */
        boolean addLabel(String label) {
            return addLabels(label);
        }


        /**
         * Removes label from the 'labels' Set associated with this instance.
         * Returns true if the 'labels' Set was modified as a result, false if not.
         *
         * @param label
         * @see #removeLabels(String...)
         */
        boolean removeLabel(String label) {
            return removeLabels(label);
        }


        /**
         * Removes all values found in labels from the 'labels' Set associated with this instance.
         * Returns true if the 'labels' Set was modified as a result, false if not.
         *
         * @param labels
         * @see Set#removeAll(Collection)
         */
        boolean removeLabels(String... labels) {
            return isUsableStringSet(labels) &&
                    this.labels != null &&
                    labels.length > 0 &&
                    this.labels.removeAll(Arrays.asList(labels));
        }


        /**
         * Returns an Iterator of the labels associated with this instance.  The Set 'labels' is not directly
         * accessible from outside this class to protect the underlying Set from external tampering. addXXX()
         * and removeXXX() methods should be utilized instead.
         *
         */
        public Iterator<String> getLabels() {
            return labels.iterator();
        }

        /**
         * Returns the labels associated with the instances as a List.  If no labels are specified,
         * an empty List is returned.
         *
         */
        public List<String> getLabelsList() {
            return Collections.list(Collections.enumeration(labels));
        }

        public String[] getLabelsArray() {
            Object[] arr = labels.toArray();
            String[] retn = new String[arr.length];
            MutableInteger i = new MutableInteger();
            Stream.of(arr).forEach(label -> retn[i.increment()] = (String) label);
            return retn;
        }

        /**
         * Sets the Object value for this instance.
         *
         * @param o
         */
        void setObject(Object o) {
            this.o = o;
        }


        /**
         * Gets the Object value for this instance.
         *
         */
        public Object getObject() {
            return o;
        }

        /**
         * Checks if the 'labels' Set associated with this KVPEntity instance is not null.  If so, it
         * assigns an empty HashSet to it.
         *
         */
        private void ensureLabelSet() {
            if (labels == null) {
                labels = new HashSet<>();
            }
        }

        /**
         * Returns true if strings is not null and if at least one member of strings is non-null
         * with a length > 0.
         *
         * @param strings
         */
        private boolean isUsableStringSet(String... strings) {
            return strings != null &&
                    Arrays.stream(strings).anyMatch(string -> string != null && string.length() > 0);
        }

    }

} // class


