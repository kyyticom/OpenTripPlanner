package org.opentripplanner.ext.flex.template;

import org.opentripplanner.ext.flex.edgetype.FlexTransferEdge;
import org.opentripplanner.ext.flex.FlexServiceDate;
import org.opentripplanner.ext.flex.edgetype.FlexTripEdge;
import org.opentripplanner.ext.flex.flexpathcalculator.FlexPathCalculator;
import org.opentripplanner.ext.flex.trip.FlexTrip;
import org.opentripplanner.model.SimpleTransfer;
import org.opentripplanner.model.Stop;
import org.opentripplanner.model.StopLocation;
import org.opentripplanner.model.Transfer;
import org.opentripplanner.model.TransferType;
import org.opentripplanner.model.plan.Itinerary;
import org.opentripplanner.routing.algorithm.mapping.GraphPathToItineraryMapper;
import org.opentripplanner.routing.core.State;
import org.opentripplanner.routing.graph.Edge;
import org.opentripplanner.routing.graph.Graph;
import org.opentripplanner.routing.graph.Vertex;
import org.opentripplanner.routing.graphfinder.NearbyStop;
import org.opentripplanner.routing.spt.GraphPath;
import org.opentripplanner.routing.vertextype.TransitStopVertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class FlexAccessTemplate<T> extends FlexAccessEgressTemplate<T> {
  private static final Logger LOG = LoggerFactory.getLogger(FlexAccessTemplate.class);

  public FlexAccessTemplate(
      NearbyStop accessEgress, FlexTrip<T> trip, T fromStopIndex, T toStopIndex,
      StopLocation transferStop, FlexServiceDate date, FlexPathCalculator<T> calculator
  ) {
    super(accessEgress, trip, fromStopIndex, toStopIndex, transferStop, date, calculator);
  }

  public Itinerary createDirectItinerary(
      NearbyStop egress, boolean arriveBy, int departureTime, ZonedDateTime startOfTime
  ) {
    List<Edge> egressEdges = egress.edges;

    Vertex flexToVertex = egressEdges.get(0).getFromVertex();

    if (!isRouteable(flexToVertex)) {
      return null;
    }

    FlexTripEdge<T> flexEdge = getFlexEdge(flexToVertex, egress.stop);

    State state = flexEdge.traverse(accessEgress.state);

    for (Edge e : egressEdges) {
      state = e.traverse(state);
      if (state == null) {
        LOG.warn("State is null after traversing {}", e);
        return null;
      }
    }

    int[] flexTimes = getFlexTimes(flexEdge, state);

    int preFlexTime = flexTimes[0];
    int flexTime = flexTimes[1];
    int postFlexTime = flexTimes[2];

    int timeShift;

    if (arriveBy) {
      int lastStopArrivalTime = departureTime - postFlexTime - secondsFromStartOfTime;
      int latestArrivalTime = trip.latestArrivalTime(
          lastStopArrivalTime,
          fromStopIndex,
          toStopIndex,
          flexTime
      );
      if (latestArrivalTime == -1) {
        return null;
      }

      // Shift from departing at departureTime to arriving at departureTime
      timeShift = secondsFromStartOfTime + latestArrivalTime - flexTime - preFlexTime;

      // If we would need to arrive after the search time
      if (timeShift > departureTime) {
        return null;
      }
    } else {
      int firstStopDepartureTime = departureTime + preFlexTime - secondsFromStartOfTime;
      int earliestDepartureTime = trip.earliestDepartureTime(
          firstStopDepartureTime,
          fromStopIndex,
          toStopIndex,
          flexTime
      );
      if (earliestDepartureTime == -1) {
        return null;
      }

      timeShift = secondsFromStartOfTime + earliestDepartureTime - preFlexTime;

      // If we would need to depart before the search time
      if (timeShift < departureTime) {
        return null;
      }
    }

    Itinerary itinerary = GraphPathToItineraryMapper.generateItinerary(
        new GraphPath(state, false),
        Locale.ENGLISH
    );

    ZonedDateTime zdt = startOfTime.plusSeconds(timeShift);
    Calendar c = Calendar.getInstance(TimeZone.getTimeZone(zdt.getZone()));
    c.setTimeInMillis(zdt.toInstant().toEpochMilli());
    itinerary.timeShiftToStartAt(c);
    return itinerary;
  }

  public Itinerary getTransferItinerary(
      Transfer transfer, FlexEgressTemplate<?> template, boolean arriveBy, int departureTime,
      ZonedDateTime startOfTime, Map<Stop, TransitStopVertex> stopVertexForStop
  ) {
    if (transfer.getFromStop() != transfer.getToStop()) {
      // TODO: Handle walking between legs
      return null;
    }

    boolean isMinTimeTransfer = transfer.getTransferType() == TransferType.MIN_TIME;
    boolean isGuaranteedTransfer = transfer.getTransferType() == TransferType.GUARANTEED;

    if (!isMinTimeTransfer && !isGuaranteedTransfer) {
      // TODO: Handle other types of transfers
      return null;
    }

    TransitStopVertex transferFromVertex = stopVertexForStop.get(transfer.getFromStop());
    TransitStopVertex transferToVertex = stopVertexForStop.get(transfer.getToStop());

    if (!this.isRouteable(transferFromVertex) || !template.isRouteable(transferToVertex)) {
      return null;
    }

    FlexTripEdge<T> firstFlexEdge = this.getFlexEdge(transferFromVertex, transfer.getFromStop());
    FlexTripEdge<?> secondFlexEdge = template.getFlexEdge(transferToVertex, transfer.getToStop());

    List<Edge> egressEdges = template.accessEgress.edges;

    State state = this.accessEgress.state;

    state = firstFlexEdge.traverse(state);

    // TODO: Remove this and modify state directly
    if (isMinTimeTransfer) {
      FlexTransferEdge legSwitchEdge = new FlexTransferEdge(transferFromVertex, transferToVertex, transfer.getMinTransferTimeSeconds());
      state = legSwitchEdge.traverse(state);
    }
    
    state = secondFlexEdge.traverse(state);

    for (Edge e : egressEdges) {
      state = e.traverse(state);
    }

    // TODO: Filtering of invalid itineraries

    Itinerary itinerary = GraphPathToItineraryMapper.generateItinerary(
        new GraphPath(state, false),
        Locale.ENGLISH
    );

    // TODO: Timeshift

    return itinerary;
  }

  protected List<Edge> getTransferEdges(SimpleTransfer simpleTransfer) {
    return simpleTransfer.getEdges();
  }

  protected Stop getFinalStop(SimpleTransfer simpleTransfer) {
    return simpleTransfer.to instanceof Stop ? (Stop) simpleTransfer.to : null;
  }

  protected Collection<SimpleTransfer> getTransfersFromTransferStop(Graph graph) {
    return graph.transfersByStop.get(transferStop);
  }

  protected Vertex getFlexVertex(Edge edge) {
    return edge.getFromVertex();
  }

  protected boolean isRouteable(Vertex flexVertex) {
    if (accessEgress.state.getVertex() == flexVertex) {
      return false;
    } else
      return calculator.calculateFlexPath(accessEgress.state.getVertex(),
          flexVertex,
          fromStopIndex,
          toStopIndex
      ) != null;
  }

  protected int[] getFlexTimes(FlexTripEdge<T> flexEdge, State state) {
    int preFlexTime = (int) accessEgress.state.getElapsedTimeSeconds();
    int edgeTimeInSeconds = flexEdge.getTimeInSeconds();
    int postFlexTime = (int) state.getElapsedTimeSeconds() - preFlexTime - edgeTimeInSeconds;
    return new int[]{ preFlexTime, edgeTimeInSeconds, postFlexTime };
  }

  protected FlexTripEdge<T> getFlexEdge(Vertex flexToVertex, StopLocation transferStop) {
    return new FlexTripEdge<>(
        accessEgress.state.getVertex(),
        flexToVertex,
        accessEgress.stop,
        transferStop,
        trip,
        this,
        calculator
    );
  }
}
