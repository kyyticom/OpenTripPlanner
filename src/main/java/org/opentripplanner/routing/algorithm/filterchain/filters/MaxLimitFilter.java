package org.opentripplanner.routing.algorithm.filterchain.filters;

import org.opentripplanner.model.plan.Itinerary;
import org.opentripplanner.routing.algorithm.filterchain.ItineraryFilter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;


/**
 * Remove all itineraries after the provided limit. This filter remove the itineraries at the
 * end of the list, so the list should be sorted on the desired key before this filter is applied.
 * <p>
 * The filter can also report the first itinerary in the list it will remove. The subscriber
 * is optional.
 */
public class MaxLimitFilter implements ItineraryFilter {
    private static final Consumer<Itinerary> IGNORE_SUBSCRIBER = (i) -> {};

    private final String name;
    private final int maxLimit;
    private final long latestDepartureTimeLimitMs;
    private final Consumer<Itinerary> changedSubscriber;

    public MaxLimitFilter(String name, int maxLimit, Instant latestDepartureTimeLimit) {
        this(name, maxLimit, latestDepartureTimeLimit, null);
    }

    public MaxLimitFilter(String name, int maxLimit, Instant latestDepartureTimeLimit, Consumer<Itinerary> changedSubscriber) {
        this.name = name;
        this.maxLimit = maxLimit;
        this.latestDepartureTimeLimitMs = latestDepartureTimeLimit != null ? latestDepartureTimeLimit.toEpochMilli() : Long.MAX_VALUE;
        this.changedSubscriber = changedSubscriber == null ? IGNORE_SUBSCRIBER : changedSubscriber;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public List<Itinerary> filter(final List<Itinerary> itineraries) {
        List<Itinerary> list = new ArrayList<>();
        for (int i = 0; i < itineraries.size(); i++) {
            Itinerary itinerary = itineraries.get(i);
            if (i >= maxLimit || (
                (i > 0) && (itinerary.startTime().getTimeInMillis() > latestDepartureTimeLimitMs)
            )) {
                changedSubscriber.accept(itinerary);
                break;
            }
            list.add(itinerary);
        }
        return list;
    }

    @Override
    public boolean removeItineraries() {
        return true;
    }
}
