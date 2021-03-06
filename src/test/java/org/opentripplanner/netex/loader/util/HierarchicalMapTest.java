package org.opentripplanner.netex.loader.util;

import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.opentripplanner.netex.loader.util.E.EASTWOOD;
import static org.opentripplanner.netex.loader.util.E.REAGAN;
import static org.opentripplanner.netex.loader.util.E.SCHWARZENEGGER;
import static org.opentripplanner.netex.loader.util.SetSupport.listOf;
import static org.opentripplanner.netex.loader.util.SetSupport.setOf;
import static org.opentripplanner.netex.loader.util.SetSupport.sort;

/**
 * To test the hierarchical map we construct a hierarchy of:
 * <pre>
 *     Country(root) -> State -> City(leaf)
 * </pre>
 * We add <em>president</em> <b>Regan</b> to <b>country</b>, a <em>governor</em>
 * <b>Schwarzenegger</b> to <b>state</b> and <em>mayor</em> <b>Eastwood</b> to <b>City</b>.
 * <p>
 * We also add an <em>actor</em> for each level, but this time <b>Eastwood</b> is added to the
 * <b>country</b> level (clearly the best actor ;-). <b>Schwarzenegger</b> is
 * added to the <b>state</b> level, and <b>Reagan</b> to <b>City</b>.
 * <pre>
 *            |                        R o l e                         |
 * Level      | President | Governor       | Mayor    | Actor          |
 * -----------|-----------|----------------|----------|----------------|
 *  Country   | Reagan    |                |          | Eastwood       |
 *  State     |           | Schwarzenegger |          | Schwarzenegger |
 *  City      |           |                | Eastwood | Reagan         |
 * </pre>
 * Now we use this test setup to test the {@link HierarchicalMap} and
 * {@link AbstractHierarchicalMap} class.
 */
public class HierarchicalMapTest {
    private static final String PRESIDENT = "president";
    private static final String GOVERNOR = "governor";
    private static final String MAYOR = "mayor";
    private static final String ACTOR = "actor";

    private final HierarchicalMap<String, E> country = new HierarchicalMap<>();
    private final HierarchicalMap<String, E> state = new HierarchicalMap<>(country);
    private final HierarchicalMap<String, E> city = new HierarchicalMap<>(state);


    @Before
    public void setup() {
        country.add(PRESIDENT, REAGAN);
        state.add(GOVERNOR, SCHWARZENEGGER);
        city.add(MAYOR, EASTWOOD);

        country.add(ACTOR, EASTWOOD);
        state.add(ACTOR, SCHWARZENEGGER);
        city.add(ACTOR, REAGAN);
    }


    @Test public void addAndLookup() {
        // Given added elements in the setup method

        // Then expect Reagen to be president at all levels
        assertEquals(REAGAN, country.lookup(PRESIDENT));
        assertEquals(REAGAN, state.lookup(PRESIDENT));
        assertEquals(REAGAN, city.lookup(PRESIDENT));

        // And then the right actor for each level
        assertEquals(EASTWOOD, country.lookup(ACTOR));
        assertEquals(SCHWARZENEGGER, state.lookup(ACTOR));
        assertEquals(REAGAN, city.lookup(ACTOR));

        // And no governor at country and state level
        assertNull(country.lookup(MAYOR));
        assertNull(state.lookup(MAYOR));
    }

    @Test public void testAddAllAndLocalMethods() {
        // Given
        Map<String, E> map = new HashMap<>();
        map.put("A", EASTWOOD);
        map.put("B", SCHWARZENEGGER);

        // When
        HierarchicalMap<String, E> hmap = new HierarchicalMap<>();
        hmap.addAll(map);

        // Then
        assertEquals(setOf("A", "B"),  hmap.localKeys());
        assertEquals(EASTWOOD,  hmap.localGet("A"));
        assertEquals(sort(listOf(EASTWOOD, SCHWARZENEGGER)),  sort(hmap.localValues()));
        assertTrue(hmap.localContainsKey("B"));
    }

    @Test public void localValues() {
        // Given added elements in the setup method
        assertEqElements(listOf(EASTWOOD, REAGAN),  country.localValues());
        assertEqElements(listOf(SCHWARZENEGGER, SCHWARZENEGGER), state.localValues());
        assertEqElements(listOf(EASTWOOD, REAGAN), city.localValues());
    }

    @Test public void localGet() {
        // Given added elements in the setup method

        // Then expect null when using a missing key at a given hierarchy level
        assertNull(country.localGet(MAYOR));
        assertNull(state.localGet(MAYOR));
        assertNull(state.localGet(PRESIDENT));
        assertNull(city.localGet(PRESIDENT));

        // Then expect to get the right object for the given hierarchy level
        assertEquals(EASTWOOD, country.localGet(ACTOR));
        assertEquals(SCHWARZENEGGER, state.localGet(GOVERNOR));
        assertEquals(REAGAN, city.localGet(ACTOR));
    }

    @Test public void containsKey() {
        // Given added elements in the setup method

        // At country level expect president present, not governor and mayor
        assertTrue(country.containsKey(PRESIDENT));
        assertFalse(country.containsKey(GOVERNOR));
        assertFalse(country.containsKey(MAYOR));

        // At state level expect president and governor to be present, not mayor
        assertTrue(state.containsKey(PRESIDENT));
        assertTrue(state.containsKey(GOVERNOR));
        assertFalse(state.containsKey(MAYOR));

        // At country level expect all keys to be present: president, governor and mayor
        assertTrue(city.containsKey(PRESIDENT));
        assertTrue(city.containsKey(GOVERNOR));
        assertTrue(city.containsKey(MAYOR));
    }

    @Test public void localContainsKey() {
        // Given added elements in the setup method

        // At country level expect president and actor to be present, not mayor
        assertTrue(country.localContainsKey(PRESIDENT));
        assertTrue(country.localContainsKey(ACTOR));
        assertFalse(country.localContainsKey(MAYOR));

        // At state level expect governor and actor to be present, not president
        assertTrue(state.localContainsKey(GOVERNOR));
        assertTrue(state.localContainsKey(ACTOR));
        assertFalse(state.localContainsKey(PRESIDENT));

        // At country level expect mayor and actor to be present, not governor
        assertTrue(city.localContainsKey(MAYOR));
        assertTrue(city.localContainsKey(ACTOR));
        assertFalse(city.localContainsKey(GOVERNOR));
    }

    private void assertEqElements(Collection<E> expected, Collection<E> actual) {
        assertEquals(sort(expected), sort(actual));
    }

}