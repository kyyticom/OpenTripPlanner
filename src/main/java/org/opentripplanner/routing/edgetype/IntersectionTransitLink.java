package org.opentripplanner.routing.edgetype;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.opentripplanner.common.geometry.GeometryUtils;
import org.opentripplanner.routing.api.request.RoutingRequest;
import org.opentripplanner.routing.core.State;
import org.opentripplanner.routing.core.StateEditor;
import org.opentripplanner.routing.core.TraverseMode;
import org.opentripplanner.routing.graph.Edge;
import org.opentripplanner.routing.vertextype.OsmVertex;
import org.opentripplanner.routing.vertextype.TransitStopVertex;

import java.util.Locale;

/**
 * This links transit stops to OSM vertices (rather than splitter vertices). It apears to only be
 * used in org.opentripplanner.graph_builder.linking.SampleStopLinker which is itself unused at this point.
 */
public class IntersectionTransitLink extends Edge {
    private static final long serialVersionUID = 1L;

    private int length_mm;

    public IntersectionTransitLink(TransitStopVertex tstop, OsmVertex intersection, int lengthMeters) {
        super(tstop, intersection);

        length_mm = lengthMeters * 1000;
    }

    public IntersectionTransitLink(OsmVertex intersection, TransitStopVertex tstop, int lengthMeters) {
        super(intersection, tstop);

        length_mm = lengthMeters * 1000;
    }

    @Override
    public State traverse(State s0) {
        RoutingRequest options = s0.getOptions();
        StateEditor s1 = s0.edit(this);

        boolean cycling = options.streetSubRequestModes.contains(TraverseMode.BICYCLE) && !options.walkingBike;
        double speed = cycling ? options.bikeSpeed : options.walkSpeed;

        // speed in m/s, length in mm, so we get milliseconds
        s1.incrementTimeInMilliseconds((long) (length_mm / speed));

        if (!cycling)
            s1.incrementWalkDistance(length_mm / 1000d);

        return s1.makeState();
    }

    @Override
    public String getName() {
        return "Intersection transit link: " + fromv.getName() + " -> " + tov.getName();
    }

    @Override
    public LineString getGeometry () {
        GeometryFactory gf = GeometryUtils.getGeometryFactory();
        // TODO hack so these appear in paths and the visualizer.
        return gf.createLineString(new Coordinate[] { fromv.getCoordinate(), tov.getCoordinate()});
    }


    @Override
    public String getName(Locale locale) {
        return this.getName();
    }
}
