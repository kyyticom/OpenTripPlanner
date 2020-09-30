package org.opentripplanner.ext.transmodelapi.model.stop;

import graphql.Scalars;
import graphql.relay.Relay;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLNonNull;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLOutputType;
import org.opentripplanner.ext.transmodelapi.mapping.TransitIdMapper;
import org.opentripplanner.model.FeedScopedId;
import org.opentripplanner.model.TransitEntity;
import org.opentripplanner.routing.graphfinder.NearbyStop;

public class QuayAtDistanceType {

  public static GraphQLObjectType createQD(GraphQLOutputType quayType, Relay relay) {
    return GraphQLObjectType.newObject()
            .name("QuayAtDistance")
            .field(GraphQLFieldDefinition.newFieldDefinition()
                    .name("id")
                    .type(new GraphQLNonNull(Scalars.GraphQLID))
                    .dataFetcher(environment -> relay.toGlobalId("QAD",
                        ((NearbyStop) environment.getSource()).distance + ";" +
                            TransitIdMapper.mapEntityIDToApi((TransitEntity<FeedScopedId>) ((NearbyStop) environment.getSource()).stop)
                    ))
                    .build())
            .field(GraphQLFieldDefinition.newFieldDefinition()
                    .name("quay")
                    .type(quayType)
                    .dataFetcher(environment -> ((NearbyStop) environment.getSource()).stop)
                    .build())
            .field(GraphQLFieldDefinition.newFieldDefinition()
                    .name("distance")
                    .type(Scalars.GraphQLFloat)
                    .description("The distance in meters to the given quay.")
                    .dataFetcher(environment -> ((NearbyStop) environment.getSource()).distance)
                    .build())
            .build();
  }
}