import org.junit.Assert;
import org.junit.jupiter.api.Test;
import java.util.*;

// Here for now until I can get it into its own project

public class TestKeyValueArray {

    private int answer = -1;
    private final boolean bChatter = true; // Change to false to stop seeing diagnostic values printed to stdout.
    private static KeyValueArray kva = KeyValueArray.getInstance();


    @Test
    public void testKeys() {
        populateKva();
        Object[] keys = kva.keys();
        MutableInteger atIntIdx = new MutableInteger(-1);
        MutableInteger atIntCnt = new MutableInteger();
        Arrays.stream(kva.keyList().toArray()).forEach(key -> {
            if (isEqual(key, "key" + atIntIdx.increment())) {
                atIntCnt.increment();
            }
        });
        Assert.assertEquals(atIntCnt.value(), 10);
    }


    @Test
    public void testValues() {
        populateKva();
        MutableInteger atIntIdx = new MutableInteger(-1);
        MutableInteger atIntCnt = new MutableInteger(0);
        Arrays.stream(kva.valueList().toArray()).forEach(value -> {
            if (isEqual(value, atIntIdx.increment())) {
                atIntCnt.increment();
            }
        });
        Assert.assertEquals(atIntCnt.value(), 10);
    }

    @Test
    public void testKeyList() {
        populateKva();
        MutableInteger atIntIdx = new MutableInteger(-1);
        MutableInteger atIntCnt = new MutableInteger(0);
        Arrays.stream(kva.keyList().toArray()).forEach(key -> {
            if (isEqual(key, "key" + atIntIdx.increment())) {
                atIntCnt.increment();
            }
        });
        Assert.assertEquals(atIntCnt.value(), 10);
    }


    @Test
    public void testValueList() {
        populateKva();
        MutableInteger atIntIdx = new MutableInteger(-1);
        MutableInteger atIntCnt = new MutableInteger(0);
        Arrays.stream(kva.valueList().toArray()).forEach(value -> {
            if (isEqual(value, atIntIdx.increment())) {
                atIntCnt.increment();
            }
        });
        Assert.assertEquals(atIntCnt.value(), 10);
    }


    @Test
    public void testAdd_object() {
        kva.clear();
        Assert.assertTrue(kva.size() == 0);

        kva.add(0);
        Assert.assertTrue(kva.size() == 1);
        Object[] entries = kva.getEntries();
        Assert.assertTrue(entries.length == 1);
        Object[] added = (Object[]) entries[0];
        Assert.assertTrue(isEqual(added[0], 0));
        Assert.assertTrue(isEqual(added[1], 0));

        // Duplicate keys allowed
        kva.add(0);
        Assert.assertTrue(kva.size() == 2);
        entries = kva.getEntries();
        Assert.assertTrue(entries.length == 2);
        added = (Object[]) entries[1];
        Assert.assertTrue(isEqual(added[0], 0));
        Assert.assertTrue(isEqual(added[1], 0));

        // null is allowed as key and value
        kva.add(null);
        Assert.assertTrue(kva.size() == 3);
        entries = kva.getEntries();
        Assert.assertTrue(entries.length == 3);
        added = (Object[]) entries[2];
        Assert.assertTrue(isEqual(added[0], null));
        Assert.assertTrue(isEqual(added[1], null));

        // null is allowed as key and value > 1
        kva.add(null);
        Assert.assertTrue(kva.size() == 4);
        entries = kva.getEntries();
        Assert.assertTrue(entries.length == 4);
        added = (Object[]) entries[3];
        Assert.assertTrue(isEqual(added[0], null));
        Assert.assertTrue(isEqual(added[1], null));
    }

    @Test
    public void testAdd_object_labels() {
        kva.clear();
        Assert.assertTrue(kva.size() == 0);
        kva.add("1", "red", "blue");
        KeyValueArray.KVPEntity[] entity = kva.getKVPEntry(0);
        List labels = entity[0].getLabelsList();
        Assert.assertTrue(labels.size() == 2);
        Assert.assertTrue(labels.contains("red"));
        Assert.assertTrue(labels.contains("blue"));
        labels = entity[1].getLabelsList();
        Assert.assertTrue(labels.size() == 2);
        Assert.assertTrue(labels.contains("red"));
        Assert.assertTrue(labels.contains("blue"));
    }


    @Test
    public void testAdd_object_object() {
        kva.clear();
        Assert.assertTrue(kva.size() == 0);

        kva.add("key0", 0);
        Assert.assertTrue(kva.size() == 1);
        Object[] entries = kva.getEntries();
        Assert.assertTrue(entries.length == 1);
        Object[] added = (Object[]) entries[0];
        Assert.assertTrue(isEqual(added[0], "key0"));
        Assert.assertTrue(isEqual(added[1], 0));

        // Duplicate keys allowed
        kva.add("key0", 0);
        Assert.assertTrue(kva.size() == 2);
        entries = kva.getEntries();
        Assert.assertTrue(entries.length == 2);
        added = (Object[]) entries[1];
        Assert.assertTrue(isEqual(added[0], "key0"));
        Assert.assertTrue(isEqual(added[1], 0));

        // null is allowed as key and value
        kva.add(null, null);
        Assert.assertTrue(kva.size() == 3);
        entries = kva.getEntries();
        Assert.assertTrue(entries.length == 3);
        added = (Object[]) entries[2];
        Assert.assertTrue(isEqual(added[0], null));
        Assert.assertTrue(isEqual(added[1], null));

        // null is allowed as key and value > 1
        kva.add(null, null);
        Assert.assertTrue(kva.size() == 4);
        entries = kva.getEntries();
        Assert.assertTrue(entries.length == 4);
        added = (Object[]) entries[3];
        Assert.assertTrue(isEqual(added[0], null));
        Assert.assertTrue(isEqual(added[1], null));
    }


    @Test
    public void testAdd_object_string_object_string() {
       kva.clear();
       kva.add("key1", "red", "val1", "blue");
       KeyValueArray.KVPEntity[] entry = kva.getKVPEntry(0);
       String[] labels = entry[0].getLabelsArray();
       Assert.assertTrue(entry[0].getObject().equals("key1"));
       Assert.assertTrue(labels.length == 1);
       Assert.assertTrue(isEqual(labels[0], "red"));
       labels = entry[1].getLabelsArray();
       Assert.assertTrue(entry[1].getObject().equals("val1"));
       Assert.assertTrue(labels.length == 1);
       Assert.assertTrue(isEqual(labels[0], "blue"));
    }


    @Test
    public void testAdd_object_stringArray_object_stringArray() {
        kva.clear();
        String[] labels = {"red","blue"};
        String[] labels1 = {"red1","blue1"};
        kva.add("key1", labels, "val1", labels1);
        KeyValueArray.KVPEntity[] entry = kva.getKVPEntry(0);
        String[] lbls = entry[0].getLabelsArray();
        Assert.assertTrue(entry[0].getObject().equals("key1"));
        Assert.assertTrue(lbls.length == 2);
        Assert.assertTrue(isEqual(lbls[0], "red"));
        Assert.assertTrue(isEqual(lbls[1], "blue"));
        lbls = entry[1].getLabelsArray();
        Assert.assertTrue(entry[1].getObject().equals("val1"));
        Assert.assertTrue(lbls.length == 2);
        Assert.assertTrue(isEqual(lbls[0], "blue1"));
        Assert.assertTrue(isEqual(lbls[1], "red1"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testAddEntry_ObjectArray() {
        kva.clear();
        Object[] toadd = {"key1","value1"};
        kva.addEntry(toadd);
        List<String> values = null;
        try {
            values = kva.get("key1");
        } catch (Exception e) {
          e.printStackTrace();
        }
        Assert.assertTrue(values.size() == 1);
        Assert.assertTrue(values.get(0).equals("value1"));
    }


    @Test
    @SuppressWarnings("unchecked")
    public void testAddEntry_ObjectArray_StringArray() {
        kva.clear();
        Object[] toadd = {"key1","value1"};
        String[] labels = {"red","white"};

        kva.addEntry(toadd, labels);
        List<String> values = null;
        try {
            values = kva.get("key1");
        } catch (Exception e) {
            e.printStackTrace();
        }
        Assert.assertTrue(values.size() == 1);
        Assert.assertTrue(values.get(0).equals("value1"));
        KeyValueArray.KVPEntity[] entities = kva.getKVPEntry(0);
        String[] keyLabels = (String[]) entities[0].getLabelsArray();
        String[] valueLabels = (String[]) entities[1].getLabelsArray();
        Assert.assertTrue(keyLabels.length==2);
        Assert.assertTrue(valueLabels.length==2);

        Assert.assertTrue(keyLabels[0].equals("red"));
        Assert.assertTrue(keyLabels[1].equals("white"));
        Assert.assertTrue(valueLabels[0].equals("red"));
        Assert.assertTrue(valueLabels[1].equals("white"));
    }


    @Test
    public void testAddAll_object_array() {
        kva.clear();
        Assert.assertTrue(kva.size() == 0);

        Object[] toAdd = new Object[5];
        for (int i = 0; i < toAdd.length; i++) {
            toAdd[i] = i;
        }
        kva.addAll(toAdd);
        Assert.assertTrue(kva.size() == 5);
        Object[] entries = kva.getEntries();
        Assert.assertTrue(entries.length == 5);

        MutableInteger atInt = new MutableInteger(-1);
        Arrays.stream(entries).forEach(entry ->
                Assert.assertTrue(
                        isEqual((int) ((Object[]) entry)[0], atInt.increment()) && (int) ((Object[]) entry)[1] == atInt.value()
                )
        );
    }







    // ---------------------------------------

    @Test
    public void testAddEntry() {
        kva.clear();
        Assert.assertTrue(kva.size() == 0);

        Object[] toAdd = {"key0", 0};
        kva.addEntry(toAdd);
        Assert.assertTrue(kva.size() == 1);
        Object[] entries = kva.getEntries();
        Assert.assertTrue(entries.length == 1);
        Object[] added = (Object[]) entries[0];
        Assert.assertTrue(isEqual(added[0], "key0"));
        Assert.assertTrue(isEqual(added[1], 0));

        // Duplicate keys allowed
        kva.addEntry(toAdd);
        Assert.assertTrue(kva.size() == 2);
        entries = kva.getEntries();
        Assert.assertTrue(entries.length == 2);
        added = (Object[]) entries[1];
        Assert.assertTrue(isEqual(added[0], "key0"));
        Assert.assertTrue(isEqual(added[1], 0));

        // null is allowed as key and value
        toAdd = new Object[2];
        toAdd[0] = null;
        toAdd[1] = null;
        kva.addEntry(toAdd);
        Assert.assertTrue(kva.size() == 3);
        entries = kva.getEntries();
        Assert.assertTrue(entries.length == 3);
        added = (Object[]) entries[2];
        Assert.assertTrue(isEqual(added[0], null));
        Assert.assertTrue(isEqual(added[1], null));

        // null is allowed as key and value > 1
        kva.addEntry(toAdd);
        Assert.assertTrue(kva.size() == 4);
        entries = kva.getEntries();
        Assert.assertTrue(entries.length == 4);
        added = (Object[]) entries[3];
        Assert.assertTrue(isEqual(added[0], null));
        Assert.assertTrue(isEqual(added[1], null));

        toAdd = new Object[3];
        boolean bCaught = false;
        try {
            kva.addEntry(toAdd);
        } catch (IllegalArgumentException e) {
            bCaught = true;
        }
        Assert.assertTrue(bCaught);
        Assert.assertTrue(kva.size() == 4);

        toAdd = new Object[1];
        bCaught = false;
        try {
            kva.addEntry(toAdd);
        } catch (IllegalArgumentException e) {
            bCaught = true;
        }
        Assert.assertTrue(bCaught);
        Assert.assertTrue(kva.size() == 4);
    }



    @Test
    @SuppressWarnings("unchecked")
    public void testAddAll_collection() {
        kva.clear();
        Assert.assertTrue(kva.size() == 0);

        Collection toAdd = new ArrayList();
        for (int i = 0; i < 5; i++) {
            toAdd.add(i);
        }
        kva.addAll(toAdd);
        Assert.assertTrue(kva.size() == 5);
        Object[] entries = kva.getEntries();
        MutableInteger atInt = new MutableInteger(-1);
        Arrays.stream(entries).forEach(entry ->
                Assert.assertTrue(
                        isEqual(((Object[]) entry)[0], ((Object[]) entry)[1]) &&
                                ((int) ((Object[]) entry)[1]) == atInt.increment()
                )
        );

        Object[] kvp;
        toAdd.clear();
        kva.clear();

        for (int i = 0; i < 5; i++) {
            kvp = new Object[2];
            kvp[0] = "key" + i;
            kvp[1] = i;
            toAdd.add(kvp);
        }

        kva.addAll(toAdd);
        Assert.assertTrue(kva.size() == 5);
        entries = kva.getEntries();
        atInt.value(-1);
        Arrays.stream(entries).forEach(entry ->
                Assert.assertTrue(
                        isEqual(((Object[]) entry)[0], "key" + atInt.increment()) &&
                                ((int) ((Object[]) entry)[1]) == atInt.value()
                )
        );

        toAdd.clear();
        kva.clear();

        for (int i = 0; i < 5; i++) {
            toAdd.add(i);
        }

        kvp = new Object[2];
        kvp[0] = "key6";
        kvp[1] = 6;
        toAdd.add(kvp);
        boolean bExcThrown = false;
        try {
            kva.addAll(toAdd);
        } catch (IllegalArgumentException e) {
            bExcThrown = true;
        }
        Assert.assertTrue(bExcThrown);

        bExcThrown = false;
        toAdd.clear();
        kva.clear();

        for (int i = 0; i < 5; i++) {
            kvp = new Object[2];
            kvp[0] = "key" + i;
            kvp[1] = i;
            toAdd.add(kvp);
        }
        toAdd.add(6);
        try {
            kva.addAll(toAdd);
        } catch (IllegalArgumentException e) {
            bExcThrown = true;
        }
        Assert.assertTrue(bExcThrown);
    }


    @Test
    public void testClear() {
        for (int i = 0; i < 10; i++) {
            kva.add(i);
        }
        Assert.assertTrue(kva.size() > 0);
        kva.clear();
        Assert.assertTrue(kva.size() == 0);
    }


    @Test
    public void testContains() {
        populateKva();
        Assert.assertTrue(kva.contains(0) && !kva.contains(45));
    }


    @Test
    public void testContainsKey() {
        populateKva();
        Assert.assertTrue(
                Arrays.stream(kva.keys()).anyMatch(key -> kva.containsKey(key)) &&
                        !kva.containsKey("key45")
        );
    }


    @Test
    public void testContainsCount() {
        populateKva();
        kva.add(null, 33);
        kva.add(null, 33);
        kva.add(null, 33);
        Assert.assertTrue(
                Arrays.stream(kva.values()).filter(value -> isEqual(1, value)).toArray().length == kva.containsCount(1) &&
                        Arrays.stream(kva.values()).filter(value -> isEqual(33, value)).toArray().length == kva.containsCount(33)
        );
    }


    @Test
    public void testContainsKeyCount() {
        populateKva();
        kva.add(null, 33);
        kva.add(null, 33);
        kva.add(null, 33);
        Assert.assertTrue(
                Arrays.stream(kva.keys()).filter(key -> isEqual("key1", key)).toArray().length == kva.containsKeyCount("key1") &&
                        Arrays.stream(kva.keys()).filter(key -> isEqual(null, key)).toArray().length == kva.containsKeyCount(null)
        );
    }


    @Test
    public void testContainsAll() {
        populateKva();
        Assert.assertTrue(kva.containsAll(kva.valueList()));
        List<Object> vals = new ArrayList<>();
        vals.add(1);
        vals.add(5);
        vals.add(6);
        Assert.assertTrue(kva.containsAll(vals));
        vals.add(11);
        Assert.assertTrue(!kva.containsAll(vals));
        Assert.assertTrue(!kva.containsAll(null));
        vals.add(null);
        Assert.assertTrue(!kva.containsAll(null));
        kva.clear();
        vals.clear();
        kva.add(null);
        vals.add(null);
        Assert.assertTrue(kva.containsAll(vals));
        vals.add(555);
        Assert.assertTrue(!kva.containsAll(vals));
        vals.add(null);
        Assert.assertTrue(!kva.containsAll(vals));
    }


    @Test
    public void testContainsAllKeys() {
        populateKva();
        Assert.assertTrue(kva.containsAllKeys(kva.keyList()));
        List<Object> keys = new ArrayList<>();
        keys.add("key1");
        keys.add("key5");
        keys.add("key6");
        Assert.assertTrue(kva.containsAllKeys(keys));
        keys.add("key11");
        Assert.assertTrue(!kva.containsAllKeys(keys));
        Assert.assertTrue(!kva.containsAllKeys(null));
        keys.add(null);
        Assert.assertTrue(!kva.containsAllKeys(null));
        kva.clear();
        keys.clear();
        kva.add(null);
        keys.add(null);
        Assert.assertTrue(kva.containsAllKeys(keys));
        keys.add(555);
        Assert.assertTrue(!kva.containsAllKeys(keys));
        keys.add(null);
        Assert.assertTrue(!kva.containsAllKeys(keys));
    }


    @Test
    public void testEquals() {
        Object o = KeyValueArray.getInstance();
        Assert.assertTrue(!kva.equals(o));
        Assert.assertTrue(kva.equals(kva));
    }


    @Test
    public void testHashCode() {
        Assert.assertTrue(kva.hashCode() != KeyValueArray.getInstance().hashCode());
    }


    @Test
    public void testIsEmpty() {
        populateKva();
        Assert.assertTrue(!kva.isEmpty());
        kva.clear();
        Assert.assertTrue(kva.isEmpty());
    }


    @Test
    public void testIterator() {
        populateKva();
        Object iter = kva.iterator();
        Assert.assertTrue(iter instanceof Iterator);
        Assert.assertTrue(((Iterator) iter).hasNext());
        Iterator iter2 = kva.iterator();
        Assert.assertTrue(iter2.next() instanceof Object[]);
    }


    @Test
    public void testRemove() {
        populateKva();
        try {
            Object o = kva.getFirst("key0");
            Assert.assertTrue((Integer) o == 0);
            kva.remove(o);
            Assert.assertTrue(!kva.valueList().contains(o));
            populateKva();
            kva.add("key0", 0);
            o = kva.get("key0");
            Assert.assertTrue(((List) o).size()==2);
            kva.remove(0);
            o = kva.get("key0");
            Assert.assertTrue(((List) o).size() == 1);
            Assert.assertTrue((Integer) ((List) o).get(0) == 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Test
    public void testRemoveAll() {
        populateKva();
        try {
            Object o = kva.getFirst("key0");
            kva.add("key0", o);
            o = kva.get("key0");
            Assert.assertTrue(((List) o).size() == 2);
            o = kva.getLast("key0");
            Assert.assertTrue(((Integer) o) == 0);
            kva.removeAll(o);
            Assert.assertTrue(!kva.valueList().contains(o));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Test
    public void testRemoveByKey() {
        populateKva();
        boolean b = kva.removeByKey("key0");
        Assert.assertTrue(b);
        b = kva.removeByKey("key99");
        Assert.assertTrue(!b);
        Assert.assertTrue(!kva.keyList().contains("key0"));
    }


    @Test
    public void testRemoveAllByKey() {
        populateKva();
        boolean b = kva.removeAllByKey("key0");
        Assert.assertTrue(b);
        b = kva.removeAllByKey("key99");
        Assert.assertTrue(!b);
        Assert.assertTrue(!kva.keyList().contains("key0"));
        Object[] entry = {"key0", 0};
        kva.addEntry(entry);
        kva.addEntry(entry);
        List l = null;
        try {
            l = kva.get("key0");
        } catch (Exception e) {
            e.printStackTrace();
        }
        Assert.assertTrue(l.size() == 2);
        kva.removeAllByKey("key0");
        Assert.assertTrue(!kva.keyList().contains("key0"));
    }


    @Test
    public void testRemoveAll_Collection() {
        populateKva();
        kva.add(3);
        kva.add(6);
        List<Integer> l = new ArrayList<>();
        l.add(3);
        l.add(6);
        Assert.assertTrue(kva.removeAll(l));
        Assert.assertTrue(!kva.removeAll(l));
        Assert.assertTrue(!kva.valueList().contains(3));
        Assert.assertTrue(!kva.valueList().contains(6));
    }


    @Test
    public void testRetainAll() {
        populateKva();
        List<Integer> l = new ArrayList<>();
        l.add(3);
        l.add(6);
        Assert.assertTrue(kva.retainAll(l));
        Assert.assertTrue(!kva.retainAll(l));
        Assert.assertTrue(kva.valueList().contains(3));
        Assert.assertTrue(kva.valueList().contains(6));
        Assert.assertTrue(!kva.valueList().contains(0));
        Assert.assertTrue(kva.size() == 2);
    }


    @Test
    public void testSize() {
        populateKva();
        Assert.assertTrue(kva.size() == 10);
    }


    @Test
    public void testToArray() {
        populateKva();
        Object[] arr = kva.toArray();
        MutableInteger atInt = new MutableInteger(-1);
        Arrays.stream(arr).forEach(o -> Assert.assertTrue(o.equals(atInt.increment())));
    }


    @Test
    public void testToArray_Array() {
        populateKva();
        Object[] arr1 = new Object[12];
        arr1[10] = "val";
        arr1[11] = "val2";
        Object[] arr = kva.toArray(arr1);
        Assert.assertTrue(((Integer) arr[9]) == 9);
        Assert.assertTrue((arr[10]) == null);
        Assert.assertTrue((arr[11]).equals("val2"));
        Assert.assertTrue(((Integer) arr1[9]) == 9);
        Assert.assertTrue((arr1[10]) == null);
        Assert.assertTrue((arr1[11]).equals("val2"));
    }


    @Test
    public void testGetEntries() {
        populateKva();
        Object[] entries = kva.getEntries();
        MutableInteger idx = new MutableInteger(-1);
        Arrays.stream(entries).forEach(
         o -> {
            Assert.assertTrue(isEqual(((Object[]) o)[0], "key"+idx.increment()));
           }
        );
        idx.value(-1);
        Arrays.stream(entries).forEach(
         o -> {
            Assert.assertTrue(isEqual(((Object[]) o)[1], idx.increment()));
          }
        );
    }


// ----------------------------------------------------------------------------------------------------------



    @SuppressWarnings("unchecked")
    private static void populateKva() {
        kva = KeyValueArray.getInstance();
        Object[] entry;
        for(int i = 0; i < 10; i++) {
          entry = new Object[2];
          entry[0] = "key" + i;
          entry[1] = i;
          kva.add(entry[0],entry[1]);
        }
    }


    /**
     * Checks for equality between the supplied objects.  If by == or .equals() comparison the two objects are equal,
     * it returns true, false if not.
     *
     * @param o
     * @param o1
     */
    private boolean isEqual(Object o, Object o1) {
        return o == o1 || (o != null && o1 != null && o.equals(o1));
    }

    private static void p(String s) {
        System.out.println(s);
    }

    private class MutableInteger {
        private int value = 0;

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
}
