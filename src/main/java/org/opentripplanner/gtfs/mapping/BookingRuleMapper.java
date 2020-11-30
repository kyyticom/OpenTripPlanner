package org.opentripplanner.gtfs.mapping;

import org.opentripplanner.model.BookingRule;
import org.opentripplanner.util.MapUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/** Responsible for mapping GTFS BookingRule into the OTP model. */
class BookingRuleMapper {

    private Map<org.onebusaway.gtfs.model.BookingRule, BookingRule> mappedBookingRules = new HashMap<>();

    BookingRuleMapper(AgencyAndIdMapper agencyAndIdMapper) {
        this.agencyAndIdMapper = agencyAndIdMapper;
    }

    Collection<BookingRule> map(Collection<org.onebusaway.gtfs.model.BookingRule> allBookingRules) {
        return MapUtils.mapToList(allBookingRules, this::map);
    }

    /**
     * Map from GTFS to OTP model, {@code null} safe.
     */
    BookingRule map(org.onebusaway.gtfs.model.BookingRule orginal) {
        return orginal == null ? null : mappedBookingRules.computeIfAbsent(orginal, this::doMap);
    }

    private BookingRule doMap(org.onebusaway.gtfs.model.BookingRule rhs) {
        BookingRule lhs = new BookingRule();

        lhs.setId(agencyAndIdMapper.map(rhs.getId()));
        lhs.setType(rhs.getType());
        lhs.setPriorNoticeDurationMin(rhs.getPriorNoticeDurationMin());
        lhs.setPriorNoticeDurationMax(rhs.getPriorNoticeDurationMax());
        lhs.setPriorNoticeLastDay(rhs.getPriorNoticeLastDay());
        lhs.setPriorNoticeLastTime(rhs.getPriorNoticeLastTime());
        lhs.setPriorNoticeStartDay(rhs.getPriorNoticeStartDay());
        lhs.setPriorNoticeStartTime(rhs.getPriorNoticeStartTime());
        lhs.setPriorNoticeServiceId(agencyAndIdMapper.map(rhs.getPriorNoticeServiceId()));
        lhs.setMessage(rhs.getMessage());
        lhs.setPickupMessage(rhs.getPickupMessage());
        lhs.setDropOffMessage(rhs.getDropOffMessage());
        lhs.setPhoneNumber(rhs.getPhoneNumber());
        lhs.setInfoUrl(rhs.getInfoUrl());
        lhs.setUrl(rhs.getUrl());

        return lhs;
    }
}
