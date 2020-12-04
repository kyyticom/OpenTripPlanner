package org.opentripplanner.gtfs.mapping;

import org.onebusaway.gtfs.model.Location;
import org.onebusaway.gtfs.model.LocationGroup;
import org.onebusaway.gtfs.model.Stop;
import org.opentripplanner.model.StopTime;
import org.opentripplanner.util.MapUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Responsible for mapping GTFS StopTime into the OTP Transit model.
 */
class StopTimeMapper {
    private final StopMapper stopMapper;

    private final LocationMapper locationMapper;

    private final LocationGroupMapper locationGroupMapper;

    private final TripMapper tripMapper;

    private final Map<org.onebusaway.gtfs.model.StopTime, StopTime> mappedStopTimes = new HashMap<>();

    StopTimeMapper(
        StopMapper stopMapper,
        LocationMapper locationMapper,
        LocationGroupMapper locationGroupMapper,
        TripMapper tripMapper
    ) {
        this.stopMapper = stopMapper;
        this.locationMapper = locationMapper;
        this.locationGroupMapper = locationGroupMapper;
        this.tripMapper = tripMapper;
    }

    Collection<StopTime> map(Collection<org.onebusaway.gtfs.model.StopTime> times) {
        return MapUtils.mapToList(times, this::map);
    }

    /** Map from GTFS to OTP model, {@code null} safe.  */
    StopTime map(org.onebusaway.gtfs.model.StopTime orginal) {
        return orginal == null ? null : mappedStopTimes.computeIfAbsent(orginal, this::doMap);
    }

    private StopTime doMap(org.onebusaway.gtfs.model.StopTime rhs) {
        StopTime lhs = new StopTime();

        lhs.setTrip(tripMapper.map(rhs.getTrip()));
        if (rhs.getStop() instanceof Stop){
            lhs.setStop(stopMapper.map((Stop) rhs.getStop()));
        } else if (rhs.getStop() instanceof Location) {
            lhs.setStop(locationMapper.map((Location) rhs.getStop()));
        } else if (rhs.getStop() instanceof LocationGroup) {
            lhs.setStop(locationGroupMapper.map((LocationGroup) rhs.getStop()));
        }
        lhs.setArrivalTime(rhs.getArrivalTime());
        lhs.setDepartureTime(rhs.getDepartureTime());
        lhs.setTimepoint(rhs.getTimepoint());
        lhs.setStopSequence(rhs.getStopSequence());
        lhs.setStopHeadsign(rhs.getStopHeadsign());
        lhs.setRouteShortName(rhs.getRouteShortName());
        lhs.setPickupType(rhs.getPickupType());
        lhs.setDropOffType(rhs.getDropOffType());
        lhs.setShapeDistTraveled(rhs.getShapeDistTraveled());
        lhs.setFarePeriodId(rhs.getFarePeriodId());
        if (rhs.getStartPickupDropOffWindow() != StopTime.MISSING_VALUE){
            lhs.setFlexWindowStart(rhs.getStartPickupDropOffWindow());  // New field name
        } else {
            lhs.setFlexWindowStart(rhs.getMinArrivalTime());            // Old field name
        }
        if (rhs.getEndPickupDropOffWindow() != StopTime.MISSING_VALUE){
            lhs.setFlexWindowEnd(rhs.getEndPickupDropOffWindow());      // New field name
        } else {
            lhs.setFlexWindowEnd(rhs.getMaxDepartureTime());            // Old field name
        }
        lhs.setFlexContinuousPickup(rhs.getContinuousPickup());
        lhs.setFlexContinuousDropOff(rhs.getContinuousDropOff());

        // Skip mapping of proxy
        // private transient StopTimeProxy proxy;
        if (rhs.getProxy() != null) {
            throw new IllegalStateException("Did not expect proxy to be set!");
        }

        return lhs;
    }
}
