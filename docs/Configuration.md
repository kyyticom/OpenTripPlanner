# Configuring OpenTripPlanner

## Base Directory

On the OTP2 command line you must always specify a single directory after all the switches. This tells OTP2 where to look for any configuration files, as well as the input files to build a graph (GTFS, OSM, elevation, and base street graphs) or the `graph.obj` file to load when starting a server. 

A typical OTP2 directory for a New York City graph might include the following:

```
otp-config.json
build-config.json
router-config.json
new-york-city-no-buildings.osm.pbf
nyc-elevation.tiff
long-island-rail-road.gtfs.zip
mta-new-york-city-transit.gtfs.zip
port-authority-of-new-york-new-jersey.gtfs.zip
graph.obj
```

You could have more than one of these directories if you are building separate graphs for separate regions. Each one should contain one or more GTFS feeds (which are just zip files full of comma-separated text tables), a PBF OpenStreetMap file, some JSON configuration files, and any output files such as `graph.obj`. For convenience, especially if you work with only one graph at a time, you may want to place your OTP2 JAR file in this same directory.

## Three Scopes of Configuration

OTP is configured via three configuration JSON files which are read from the directory specified on its command line. We try to provide sensible defaults for every option, so all three of these files are optional, as are all the options within each file. Each configuration file corresponds to options that are relevant at a particular phase of OTP usage. 

Options and parameters that are taken into account during the graph building process will be "baked into" the graph, and cannot be changed later in a running server. These are specified in `build-config.json`. Other details of OTP operation can be modified without rebuilding the graph. These run-time configuration options are found in `router-config.json`. Finally, `otp-config.json` contains simple switches that enable or disable system-wide features. 

# System-wide Configuration

Using the file `otp-config.json` you can enable or disable different APIs and experimental [Sandbox Extensions](SandboxExtension.md). By default, all supported APIs are enabled and all sandbox features are disabled, so for most OTP2 use cases it is not necessary to create this file. Features that can be toggled in this file are generally only affect the routing phase of OTP2 usage, but for consistency all such "feature flags", even those that would affect graph building, are managed in this one file. See the OTPFeature Java class for an enumeration of all available features and their default settings. Here is an example:

```JSON
// otp-config.json
{
    otpFeatures : {
        APIBikeRental : false,
        SandboxExampleAPIGraphStatistics : true
    }
}
```

# Graph Build Configuration

This table lists the possible settings that can be defined in a `build-config.json` file. These will be stored in the graph itself, and affect any server that subsequently loads that graph. Sections follow that describe particular settings in more depth.

config key | description | value type | value default | notes
---------- | ----------- | ---------- | ------------- | -----
`` |  TODO | boolean | false |
`areaVisibility` | Perform visibility calculations. If this is `true` OTP attempts to calculate a path straight through an OSM area using the shortest way rather than around the edge of it. (These calculations can be time consuming). | boolean | false |
`banDiscouragedWalking` | should walking should be allowed on OSM ways tagged with `foot=discouraged"` | boolean | false | 
`banDiscouragedBiking` | should walking should be allowed on OSM ways tagged with `bicycle=discouraged"` | boolean | false | 
`dataImportReport` |  Generate nice HTML report of Graph errors/warnings | boolean | false |
`distanceBetweenElevationSamples` | TODO OTP2 | double | 10 |
`elevationBucket` | If specified, download NED elevation tiles from the given AWS S3 bucket | object | null | provide an object with `accessKey`, `secretKey`, and `bucketName` for AWS S3
`elevationUnitMultiplier` | Specify a multiplier to convert elevation units from source to meters | double | 1.0 | see [Elevation unit conversion](#elevation-unit-conversion)
`embedRouterConfig` | Embed the Router config in the graph, which allows it to be sent to a server fully configured over the wire | boolean | true |
`extraEdgesStopPlatformLink` | add extra edges when linking a stop to a platform, to prevent detours along the platform edge | boolean | false | 
`fares` | A specific fares service to use | object | null | see [fares configuration](#fares-configuration)
`fetchElevationUS` | Download US NED elevation data and apply it to the graph | boolean | false |
`islandWithStopsMaxSize` | Pruning threshold for islands with stops. Any such island under this size will be pruned | int | 5 | 
`islandWithoutStopsMaxSize` | Pruning threshold for islands without stops. Any such island under this size will be pruned | int | 40 | 
`matchBusRoutesToStreets` | Based on GTFS shape data, guess which OSM streets each bus runs on to improve stop linking | boolean | false |
`maxDataImportIssuesPerFile` | If number of data import issues is larger then specified maximum number of issues the report will be split in multiple files | int | 1,000 | 
`maxInterlineDistance` | Maximal distance between stops in meters that will connect consecutive trips that are made with same vehicle | int | 200 | units: meters
`maxTransferDistance` | Transfers up to this length in meters will be pre-calculated and included in the Graph | double | 2,000 | units: meters
`multiThreadElevationCalculations` | If true, the elevation module will use multi-threading during elevation calculations. | boolean | false | see [Elevation Data Calculation Optimizations](#elevation-data-calculation-optimizations)
`osmNaming` | A custom OSM namer to use | object | null | see [custom naming](#custom-naming)
`osmWayPropertySet` | Custom OSM way properties | string | `default` | options: `default`, `finland`, `norway`, `uk`
`parentStopLinking` | Link GTFS stops to their parent stops | boolean | false |
`platformEntriesLinking` | Link unconnected entries to public transport platforms | boolean | false |
`readCachedElevations` | If true, reads in pre-calculated elevation data. | boolean | true | see [Elevation Data Calculation Optimizations](#elevation-data-calculation-optimizations)
`staticBikeParkAndRide` | Whether we should create bike P+R stations from OSM data | boolean | false | 
`staticBikeRental` | Whether bike rental stations should be loaded from OSM, rather than periodically dynamically pulled from APIs | boolean | false | 
`staticParkAndRide` | Whether we should create car P+R stations from OSM data | boolean | true | 
`stationTransfers` | Create direct transfers between the constituent stops of each parent station | boolean | false |
`streets` | Include street input files (OSM/PBF) | boolean | true | 
`storage` | Configure access to data sources like GRAPH/OSM/DEM/GTFS/NETEX/ISSUE-REPORT. | object | null | 
`subwayAccessTime` | Minutes necessary to reach stops served by trips on routes of `route_type=1` (subway) from the street | double | 2.0 | units: minutes
`transit` | Include all transit input files (GTFS) from scanned directory | boolean | true |
`transitServiceStart` | Limit the import of transit services to the given *start* date. *Inclusive*. Use an absolute date or a period relative to the day the graph is build. To specify a week before the build date use a negative period like `-P1W`. | Date or Period (ISO 8601) | `-P1Y` | `2020-01-01`, `-P1M3D`, `-P3W`
`transitServiceEnd` | Limit the import of transit services to the given *end* date. *Inclusive*. Use an absolute date or a period relative to the day the graph is build. | Date or Period (ISO 8601) | `P3Y` | `2022-12-31`, `P1Y6M10D`, `P12W`
`useTransfersTxt` | Create direct transfer edges from transfers.txt in GTFS, instead of based on distance | boolean | false |
`writeCachedElevations` | If true, writes the calculated elevation data. | boolean | false | see [Elevation Data Calculation Optimizations](#elevation-data-calculation-optimizations)

This list of parameters in defined in the [BuildConfig.java](https://github.com/opentripplanner/OpenTripPlanner/blob/dev-2.x/src/main/java/org/opentripplanner/standalone/config/BuildConfig.java).


## Storage
Nested inside `storage {...}` in `build-config.json`.

Using other data-sources than the local file system is new in OTP2. This allow for access to cloud 
based storage as well as using local disk. If you run OTP in the cloud you might get faster start-up 
and build times if you use the cloud storage instead of copying the files, it also simplefy the 
deplyment. Nested `storage` build-config. 

See (StorageConfig.java)[https://github.com/opentripplanner/OpenTripPlanner/blob/dev-2.x/src/main/java/org/opentripplanner/standalone/config/StorageConfig.java] 
for up-to-date detailed description of each config parameter. Here is an overview:

config key | description | value type | value default
---------- | ----------- | ---------- | -------------
`gsCredentials` | Use an environment variable to point to the Google Cloud credentials: `"${MY_GOC_SERVICE}"`. | string | `null`
`graph` | Absolute path to the graph file. | URI | `null`
`streetGraph` | Absolute path to the street-graph file. | URI | `null`
`osm` | List of absolute paths of Open Street Map files to build. | URI array | `null`
`dem` | List of absolute paths of Elevation DEM files to build. | URI array | `null`
`gtfs` | List of GTFS transit data files to build. | URI array | `null`
`netex` | List of NeTEx transit data files to build. | URI array | `null`
`buildReportDir` | Path to directory for the build issue report generated by OTP. | URI | `null`
`localFileNamePatterns` | Patterns to use for auto resolving local filenames to input data types. | object | `null`

### Local Filename Patterns
Nested inside `storage : { localFileNamePatterns : { ... } }` in `build-config.json`.

config key | description | value type | value default
---------- | ----------- | ---------- | -------------
`osm` | Pattern used to match Open Street Map files on local disk | Regexp Pattern | `(?i)(\.pbf)`|\.osm|\.osm\.xml)$` 
`dem` | Pattern used to match Elevation DEM files on local disk | Regexp Pattern | `(?i)\.tiff?$` 
`gtfs` | Pattern used to match GTFS files on local disk | Regexp Pattern | `(?i)gtfs` 
`netex` | Pattern used to match NeTEx files on local disk | Regexp Pattern | `(?i)netex` 

### Storage example:
```
storage : {
  // Use the GCS_SERVICE_CREDENTIALS environment variable to locate GCS credentials
  gsCredentials: "${GCS_SERVICE_CREDENTIALS}",
  streetGraph: "file:///Users/kelvin/otp/streetGraph.obj",
  osm: ["gs://bucket-name/shared-osm-file.pbf"]
  localFileNamePatterns: {
    // All filenames that start with "g-" and end with ".zip" is imported as a GTFS file.
    gtfs : "^g-.*\.zip$", 
  }
}
```


## Limit the transit service period

The properties `transitServiceStart` and `transitServiceEnd` can be used to limit the service dates. This affects both GTFS service calendars and dates. The service calendar is reduced and dates outside the period are dropped. OTP2 will compute a transit schedule for every day for which it can find at least one trip running. On the other hand, OTP will waste resources if a service end date is *unbounded* or very large (`9999-12-31`). To avoid this, limit the OTP service period. Also, if you provide a service with multiple feeds they may have different service end dates. To avoid inconsistent results, the period can be limited, so all feeds have data for the entire period. The default is to use a period of 1 year before, and 3 years after the day the graph is built. Limiting the period will *not* improve the search performance, but OTP will build faster and load faster in most cases.

The `transitServiceStart` and `transitServiceEnd` parameters are set using an absolute date like `2020-12-31` or a period like `P1Y6M5D` relative to the graph build date. Negative periods is used to specify dates in the past. The period is computed using the system time-zone, not the feed time-zone. Also, remember that the service day might be more than 24 hours. So be sure to include enough slack to account for the this. Setting the limits too wide have very little impact and is in general better than trying to be exact. The period and date format follow the ISO 8601 standard.

## Reaching a subway platform

The ride locations for some modes of transport such as subways and airplanes can be slow to reach from the street.
When planning a trip, we need to allow additional time to reach these locations to properly inform the passenger. For
example, this helps avoid suggesting short bus rides between two subway rides as a way to improve travel time. You can
specify how long it takes to reach a subway platform

```JSON
// build-config.json
{
  "subwayAccessTime": 2.5
}
```

Stops in GTFS do not necessarily serve a single transit mode, but in practice this is usually the case. This additional
access time will be added to any stop that is visited by trips on subway routes (GTFS route_type = 1).

This setting does not generalize well to airplanes because you often need much longer to check in to a flight (2-3 hours
for international flights) than to alight and exit the airport (perhaps 1 hour). Therefore there is currently no
per-mode access time, it is subway-specific.

## Transferring within stations

Subway systems tend to exist in their own layer of the city separate from the surface, though there are exceptions where
tracks lie right below the street and transfers happen via the surface. In systems where the subway is quite deep
and transfers happen via tunnels, the time required for an in-station transfer is often less than that for a
surface transfer. A proposal was made to provide detailed station pathways in GTFS but it is not in common use.

One way to resolve this problem is by ensuring that the GTFS feed codes each platform as a separate stop, then
micro-mapping stations in OSM. When OSM data contains a detailed description of walkways, stairs, and platforms within
a station, GTFS stops can be linked to the nearest platform and transfers will happen via the OSM ways, which should
yield very realistic transfer time expectations. This works particularly well in above-ground train stations where
the layering of non-intersecting ways is less prevalent. Here's an example in the Netherlands:

<iframe width="425" height="350" frameborder="0" scrolling="no" marginheight="0" marginwidth="0" src="http://www.openstreetmap.org/export/embed.html?bbox=4.70502644777298%2C52.01675028000761%2C4.7070810198783875%2C52.01813190694357&amp;layer=mapnik" style="border: 1px solid black"></iframe><small><a href="http://www.openstreetmap.org/#map=19/52.01744/4.70605">View Larger Map</a></small>
When such micro-mapping data is not available, we need to rely on information from GTFS including how stops are grouped
into stations and a table of transfer timings where available. During the graph build, OTP can create preferential
connections between each pair of stops in the same station to favor in-station transfers:

```JSON
// build-config.json
{
  "stationTransfers": true
}
```

Note that this method is at odds with micro-mapping and might make some transfers artificially short.


## Elevation data

OpenTripPlanner can "drape" the OSM street network over a digital elevation model (DEM).
This allows OTP to draw an elevation profile for the on-street portion of itineraries, and helps provide better
routing for bicyclists. It even helps avoid hills for walking itineraries. DEMs are usually supplied as rasters
(regular grids of numbers) stored in image formats such as GeoTIFF.

### U.S. National Elevation Dataset

In the United States, a high resolution [National Elevation Dataset](http://ned.usgs.gov/) is available for the entire
territory. The US Geological Survey (USGS) delivers this dataset in tiles via a somewhat awkward heavyweight web-based GIS
which generates and emails you download links. OpenTripPlanner contains a module which will automatically contact this
service and download the proper tiles to completely cover your transit and street network area. This process is rather
slow (download is around 1.5 hours, then setting elevation for streets takes about 5 minutes for the Portland, Oregon region),
but once the tiles are downloaded OTP will keep them in local cache for the next graph build operation.

To auto-download NED tiles when building your graph, add the following line to `build-config.json` in your router
directory:

```JSON
// build-config.json
{
  "fetchElevationUS": true
}
```

You may also want to add the `--cache <directory>` command line parameter to specify a custom NED tile cache location.

NED downloads take quite a long time and slow down the graph building process. The USGS will also deliver the
whole dataset in bulk if you [send them a hard drive](http://ned.usgs.gov/faq.html#DATA). OpenTripPlanner contains
another module that will then automatically fetch data in this format from an Amazon S3 copy of your bulk data.
You can configure it as follows in `build-config.json`:

```JSON
{
    "elevationBucket" : {
        "accessKey" : "your-aws-access-key",
        "secretKey" : "corresponding-aws-secret-key",
        "bucketName" : "ned13"
    }
}
```

### Geoid Difference

With some elevation data, the elevation values are specified as relative to the a geoid (irregular estimate of mean sea level). See [issue #2301](https://github.com/opentripplanner/OpenTripPlanner/issues/2301) for detailed discussion of this. In these cases, it is necessary to also add this geoid value onto the elevation value to get the correct result. OTP can automatically calculate these values in one of two ways. 

The first way is to use the geoid difference value that is calculated once at the center of the graph. This value is returned in each trip plan response in the [ElevationMetadata](http://dev.opentripplanner.org/apidoc/1.4.0/json_ElevationMetadata.html) field. Using a single value can be sufficient for smaller OTP deployments, but might result in incorrect values at the edges of larger OTP deployments. If your OTP instance uses this, it is recommended to set a default request value in the `router-config.json` file as follows:

```JSON
// router-config.json
{
    "routingDefaults": {
        "geoidElevation ": true   
    }
}
```

The second way is to precompute these geoid difference values at a more granular level and include them when calculating elevations for each sampled point along each street edge. In order to speed up calculations, the geoid difference values are calculated and cached using only 2 significant digits of GPS coordinates. This is more than enough detail for most regions of the world and should result in less than one meter of difference in areas that have large changes in geoid difference values. To enable this, include the following in the `build-config.json` file: 

```JSON
// build-config.json
{
  "includeEllipsoidToGeoidDifference": true
}
```

If the geoid difference values are precomputed, be careful to not set the routing resource value of `geoidElevation` to true in order to avoid having the graph-wide geoid added again to all elevation values in the relevant street edges in responses.

### Other raster elevation data

For other parts of the world you will need a GeoTIFF file containing the elevation data. These are often available from
national geographic surveys, or you can always fall back on the worldwide
[Space Shuttle Radar Topography Mission](http://www2.jpl.nasa.gov/srtm/) (SRTM) data. This not particularly high resolution
(roughly 30 meters horizontally) but it can give acceptable results.

Simply place the elevation data file in the directory with the other graph builder inputs, alongside the GTFS and OSM data.
Make sure the file has a `.tiff` or `.tif` extension, and the graph builder should detect its presence and apply
the elevation data to the streets.

OTP should automatically handle DEM GeoTIFFs in most common projections. You may want to check for elevation-related
error messages during the graph build process to make sure OTP has properly discovered the projection. If you are using
a DEM in unprojected coordinates make sure that the axis order is (longitude, latitude) rather than
(latitude, longitude). Unfortunately there is no reliable standard for WGS84 axis order, so OTP uses the same axis
order as the above-mentioned SRTM data, which is also the default for the popular Proj4 library.

DEM files(USGS DEM) is not supported by OTP, but can be converted to GeoTIFF with tools like [GDAL](http://www.gdal.org/). 
Use `gdal_merge.py -o merged.tiff *.dem` to merge a set of `dem` files into one `tif` file.

See Interline [PlanetUtils](https://github.com/interline-io/planetutils) for a set of scripts to download, merge, and resample [Mapzen/Amazon Terrain Tiles](https://registry.opendata.aws/terrain-tiles/).

### Elevation unit conversion

By default, OTP expects the elevation data to use metres. However, by setting `elevationUnitMultiplier` in `build-config.json`,
it is possible to define a multiplier that converts the elevation values from some other unit to metres.

```JSON
// build-config.json
{
  // Correct conversation multiplier when source data uses decimetres instead of metres
  "elevationUnitMultiplier": 0.1
}
```

### Elevation Data Calculation Optimizations

Calculating elevations on all StreetEdges can take a dramatically long time. In a very large graph build for multiple Northeast US states, the time it took to download the elevation data and calculate all of the elevations took 5,509 seconds (roughly 1.5 hours).

If you are using cloud computing for your OTP instances, it is recommended to create prebuilt images that contain the elevation data you need. This will save time because all of the data won't need to be downloaded.

However, the bulk of the time will still be spent calculating elevations for all of the street edges. Therefore, a further optimization can be done to calculate and save the elevation data during a graph build and then save it for future use.

#### Reusing elevation data from previous builds

In order to write out the precalculated elevation data, add this to your `build-config.json` file:

```JSON
// build-config.json
{  
  "writeCachedElevations": true
}
```

After building the graph, a file called `cached_elevations.obj` will be written to the cache directory. By default, this file is not written during graph builds. There is also a graph build parameter called `readCachedElevations` which is set to `true` by default.

In graph builds, the elevation module will attempt to read the `cached_elevations.obj` file from the cache directory. The cache directory defaults to `/var/otp/cache`, but this can be overriden via the CLI argument `--cache <directory>`. For the same graph build for multiple Northeast US states, the time it took with using this predownloaded and precalculated data became 543.7 seconds (roughly 9 minutes).

The cached data is a lookup table where the coordinate sequences of respective street edges are used as keys for calculated data. It is assumed that all of the other input data except for the OpenStreetMap data remains the same between graph builds. Therefore, if the underlying elevation data is changed, or different configuration values for `elevationUnitMultiplier` or `includeEllipsoidToGeoidDifference` are used, then this data becomes invalid and all elevation data should be recalculated. Over time, various edits to OpenStreetMap will cause this cached data to become stale and not include new OSM ways. Therefore, periodic update of this cached data is recommended.

#### Configuring multi-threading during elevation calculations

For unknown reasons that seem to depend on data and machine settings, it might be faster to use a single processor. For this reason, multi-threading of elevation calculations is only done if `multiThreadElevationCalculations` is set to true. To enable multi-threading in the elevation module, add the following to the `build-config.json` file:
                                                               
```JSON
// build-config.json
{  
  "multiThreadElevationCalculations": true
}
```

## Fares configuration

By default OTP will compute fares according to the GTFS specification if fare data is provided in
your GTFS input. It is possible to turn off this by setting the fare to "off". For more complex 
scenarios or to handle bike rental fares, it is necessary to manually configure fares using the
`fares` section in `build-config.json`. You can combine different fares (for example transit and
bike-rental) by defining a `combinationStrategy` parameter, and a list of sub-fares to combine 
(all fields starting with `fare` are considered to be sub-fares).

```JSON
// build-config.json
{
  // Select the custom fare "seattle"
  "fares": "seattle",
  // OR this alternative form that could allow additional configuration
  "fares": {
	"type": "seattle"
  }
}
```

```JSON
// build-config.json
{
  "fares": {
    // Combine two fares by simply adding them
    "combinationStrategy": "additive",
    // First fare to combine
    "fare0": "new-york",
    // Second fare to combine
    "fare1": {
      "type": "bike-rental-time-based",
      "currency": "USD",
      "prices": {
          // For trip shorter than 30', $4 fare
          "30":   4.00,
          // For trip shorter than 1h, $6 fare
          "1:00": 6.00
      }
    }
    // We could also add fareFoo, fareBar...
  }
}
```

Turning the fare service _off_, this will ignore any fare data in the provided GTFS data.
```JSON
// build-config.json
{
  "fares": "off"
}
```


The current list of custom fare type is:

- `bike-rental-time-based` - accepting the following parameters:
    - `currency` - the ISO 4217 currency code to use, such as `"EUR"` or `"USD"`,
    - `prices` - a list of {time, price}. The resulting cost is the smallest cost where the elapsed time of bike rental is lower than the defined time.
- `san-francisco` (no parameters)
- `new-york` (no parameters)
- `seattle` (no parameters)
- `off` (no parameters)

The current list of `combinationStrategy` is:

- `additive` - simply adds all sub-fares.

## OSM / OpenStreetMap configuration

It is possible to adjust how OSM data is interpreted by OpenTripPlanner when building the road part of the routing graph.

### Way property sets

OSM tags have different meanings in different countries, and how the roads in a particular country or region are tagged affects routing. As an example are roads tagged with `highway=trunk (mainly) walkable in Norway, but forbidden in some other countries. This might lead to OTP being unable to snap stops to these roads, or by giving you poor routing results for walking and biking.
You can adjust which road types that are accessible by foot, car & bicycle as well as speed limits, suitability for biking and walking.

There are currently following wayPropertySets defined;

- `default` which is based on California/US mapping standard
- `finland` which is adjusted to rules and speeds in Finland
- `norway` which is adjusted to rules and speeds in Norway
- `uk` which is adjusted to rules and speed in the UK

To add your own custom property set have a look at `org.opentripplanner.graph_builder.module.osm.NorwayWayPropertySet` and `org.opentripplanner.graph_builder.module.osm.DefaultWayPropertySet`. If you choose to mainly rely on the default rules, make sure you add your own rules first before applying the default ones. The mechanism is that for any two identical tags, OTP will use the first one.

```JSON
// build-config.json
{
  osmWayPropertySet: "norway"
}
```


### Custom naming

You can define a custom naming scheme for elements drawn from OSM by defining an `osmNaming` field in `build-config.json`,
such as:

```JSON
// build-config.json
{
  "osmNaming": "portland"
}
```

There is currently only one custom naming module called `portland` (which has no parameters).


# Router configuration

This section covers all options that can be set for each router using the `router-config.json` file.
These options can be applied by the OTP server without rebuilding the graph.

config key | description | value type | value default | notes
---------- | ----------- | ---------- | ------------- | -----
`routingDefaults` | Default routing parameters, which will be applied to every request | object |  | see [routing defaults](#routing-defaults)
`streetRoutingTimeout` | maximum time limit for street route queries | double | null | units: seconds; see [timeout](#timeout)
`requestLogFile` | Path to a plain-text file where requests will be logged | string | null | see [logging incoming requests](#logging-incoming-requests)
`transit` | Transit tuning parameters | `TransitRoutingConfig` |  | see [Tuning transit routing](#Tuning-transit-routing)
`updaters` | configure real-time updaters, such as GTFS-realtime feeds | object | null | see [configuring real-time updaters](#configuring-real-time-updaters)
`transmodelApi` | configure Entur Transmodel API (**Sandbox**) | object | null | See the code for parameters, no doc provided.


## Routing defaults

There are many trip planning options used in the OTP web API, and more exist
internally that are not exposed via the API. You may want to change the default value for some of these parameters,
i.e. the value which will be applied unless it is overridden in a web API request.

A full list of them can be found in the RoutingRequest class
[in the Javadoc](http://dev.opentripplanner.org/javadoc/1.4.0/org/opentripplanner/routing/core/RoutingRequest.html).
Any public field or setter method in this class can be given a default value using the routingDefaults section of
`router-config.json` as follows:

```JSON
{
    "routingDefaults": {
        "walkSpeed": 2.0,
        "stairsReluctance": 4.0,
        "carDropoffTime": 240
    }
}
```

## Tuning itinerary filtering
Nested inside `routingDefaults {...}` in `router-config.json`.

OTP2 may produce numerous _pareto-optimal_ results when using `time`, `number-of-transfers` and `generalized-cost` as criteria. Use the parameters listed here to reduce/filter the itineraries return by the search engine before returning the results to client.

config key | description | value type | value default
---------- | ----------- | ---------- | -------------
`debugItineraryFilter` | Enable this to attach a system notice to itineraries instead of removing them. Some filters are not configurable, byt will show up in the system-notice if debugging is enabled. | boolean | `false`
`groupBySimilarityKeepOne` | Pick ONE itinerary from each group after putting itineraries that is 85% similar together. | double | `0.85` (85%)
`groupBySimilarityKeepNumOfItineraries` | Reduce the number of itineraries to the requested number by reducing each group of itineraries grouped by 68% similarity. | double | `0.68` (68%)
`transitGeneralizedCostLimit` | A relative maximum limit for the generalized cost for transit itineraries. The limit is a linear function of the minimum generalized-cost. The function is used to calculate a max-limit. The max-limit is then used to to filter by generalized-cost. Transit itineraries with a cost higher than the max-limit is dropped from the result set. None transit itineraries is excluded from the filter. To set a filter to be 1 hour plus 2 times the best cost use: `3600 + 2.0 x`. To set an absolute value(3000) use: `3000 + 0x`  | `DoubleFunction` | `null`


### Group-by-filters

The group-by-filter is a bit complex, but should be simple to use. Set `debugItineraryFilter=true` 
and experiment with `searchWindow` and the two group-by parameters(`debugItineraryFilter` and 
`groupBySimilarityKeepNumOfItineraries`). 

The group-by-filter work by grouping itineraries together and then reducing the number of 
itineraries in each group, keeping the itinerary/itineraries with the best _generalized-cost_. The 
group-by function first pick all transit legs that account for more than N% of the itinerary based 
on distance traveled. This become the group-key. To keys are the same if all legs in one of the keys
also exist in the other. Note, one key may have a lager set of legs than the other, but they can 
still be the same. When comparing to legs we compare the `tripId` and make sure the legs overlap in
place and time. Two legs are the same if both legs ride at least a common subsection of the same 
trip. The `groupBySimilarityKeepOne` filter will keep ONE itinerary in each group. The 
`groupBySimilarityKeepNumOfItineraries` is a bit more complex, because it uses the 
`numOfItineraries` request parameter to estimate a maxLimit for each group. For example, if the 
`numOfItineraries` is 5 elements and there is 3 groups, we set the _max-limit_ for each group
to 2, returning between 4 and 6 elements depending on the distribution. The _max-limit_ can never 
be less than 1.


## Routing modes

TODO OTP2 - This need to be updated. Why is this even here, does it make sence to configure this and is it possible?
          - Is this API documentation? Move to proper place.

The routing request parameter `mode` determines which transport modalities should be considered when calculating the list
of routes.

Some modes (mostly bicycle and car) also have optional qualifiers `RENT` and `PARK` to specify if vehicles are to be parked at a station or rented. In theory
this can also apply to other modes but makes sense only in select cases which are listed below.

Whether a transport mode is available highly depends on the input feeds (GTFS, OSM, bike sharing feeds) and the graph building options supplied to OTP.

The complete list of modes are:

- `WALK`: Walking some or all of the route.

- `TRANSIT`: General catch-all for all public transport modes.

- `BICYCLE`: Cycling for the entirety of the route or taking a bicycle onto the public transport and cycling from the arrival station to the destination.

- `BICYCLE_RENT`: Taking a rented, shared-mobility bike for part or the entirety of the route.  

    _Prerequisite:_ Vehicle positions need to be added to OTP either as static stations or dynamic data feeds. 

    For dynamic bike positions configure an input feed. See [Configuring real-time updaters](Configuration.md#configuring-real-time-updaters).

    For static stations check the graph building documentation for the property `staticBikeRental`.

- `BICYCLE_PARK`: Leaving the bicycle at the departure station and walking from the arrival station to the destination.

    This mode needs to be combined with at least one transit mode (or `TRANSIT`) otherwise it behaves like an ordinary bicycle journey.

    _Prerequisite:_ Bicycle parking stations present in the OSM file and visible to OTP by enabling the property `staticBikeParkAndRide` during graph build.

- `CAR`: Driving your own car the entirety of the route. 

    If this is combined with `TRANSIT` it will return routes with a 
    [Kiss & Ride](https://en.wikipedia.org/wiki/Park_and_ride#Kiss_and_ride_/_kiss_and_fly) component. This means that the car is not parked in a permanent
    parking area but rather the passenger is dropped off (for example, at an airport) and the driver continues driving the car away from the drop off
    location.

- `CAR_PARK`: Driving a car to the park-and-ride facilities near a station and taking public transport.

    This mode needs to be combined with at least one transit mode (or `TRANSIT`) otherwise it behaves like an ordinary car journey.

    _Prerequisite:_ Park-and-ride areas near the station need to be present in the OSM input file.


The following modes are 1-to-1 mappings from the [GTFS `route_type`](https://developers.google.com/transit/gtfs/reference/#routestxt):

- `TRAM`: Tram, streetcar, or light rail. Used for any light rail or street-level system within a metropolitan area.

- `SUBWAY`: Subway or metro. Used for any underground rail system within a metropolitan area.

- `RAIL`: Used for intercity or long-distance travel.

- `BUS`: Used for short- and long-distance bus routes.

- `FERRY`: Ferry. Used for short- and long-distance boat service.

- `CABLE_CAR`: Cable car. Used for street-level cable cars where the cable runs beneath the car.

- `GONDOLA`: Gondola or suspended cable car. Typically used for aerial cable cars where the car is suspended from the cable.

- `FUNICULAR`: Funicular. Used for any rail system that moves on steep inclines with a cable traction system.

Lastly, this mode is part of the [Extended GTFS route types](https://developers.google.com/transit/gtfs/reference/extended-route-types):

- `AIRPLANE`: Taking an airplane.

Note that there are conceptual overlaps between `TRAM`, `SUBWAY` and `RAIL` and some transport providers categorize their routes differently to others.
In other words, what is considered a `SUBWAY` in one city might be of type `RAIL` in another. Study your input GTFS feed carefully to 
find out the appropriate mapping in your region.

### Drive-to-transit routing defaults

When using the "park and ride" or "kiss and ride" modes (drive to transit), the initial driving time to reach a transit
stop or park and ride facility is constrained. You can set a drive time limit in seconds by adding a line like
`maxPreTransitTime = 1200` to the routingDefaults section. If the limit is too high on a very large street graph, routing
performance may suffer.


## Boarding and alighting times

Sometimes there is a need to configure a longer ride or alighting times for specific modes, such as airplanes or ferries,
where the check-in process needs to be done in good time before ride. The ride time is added to the time when going
from the stop (offboard) vertex to the onboard vertex, and the alight time is added vice versa. The times are configured as
seconds needed for the ride and alighting processes in `router-config.json` as follows:

```JSON
{
  "boardTimes": {
    "AIRPLANE": 2700
  },
  "alightTimes": {
    "AIRPLANE": 1200
  }
}
```

## Timeout

In OTP1 path searches sometimes toke a long time to complete. With the new Raptor algorithm this not
the case anymore. The street part of the routing may still take a long time if searching very long
distances. You can set the street routing timeout to avoid tying up server resources on pointless
searches and ensure that your users receive a timely response. You can also limit the max distance
to search for WALK, BIKE and CAR. When a search times out, a WARN level log entry is made with
information that can help identify problematic searches and improve our routing methods. There are 
no timeouts for the transit part of the routing search, instead configure a reasonable dynamic 
search-window. To set the street routing timeout use the following config:

```JSON
// router-config.json
{
  "streetRoutingTimeout": 5.5
}
```

This specifies a timeout in (optionally fractional) seconds. The search abort after this many seconds and any paths found are returned to the client. 

## Logging incoming requests

You can log some characteristics of trip planning requests in a file for later analysis. Some transit agencies and
operators find this information useful for identifying existing or unmet transportation demand. Logging will be
performed only if you specify a log file name in the router config:

```JSON
// router-config.json
{
  "requestLogFile": "/var/otp/request.log"
}
```

Each line in the resulting log file will look like this:

`2016-04-19T18:23:13.486 0:0:0:0:0:0:0:1 ARRIVE 2016-04-07T00:17 WALK,BUS,CABLE_CAR,TRANSIT,BUSISH 45.559737193889966 -122.64999389648438 45.525592487765635 -122.39044189453124 6095 3 5864 3 6215 3`

The fields separated by whitespace are (in order):

1. Date and time the request was received
2. IP address of the user
3. Arrive or depart search
4. The arrival or departure time
5. A comma-separated list of all transport modes selected
6. Origin latitude and longitude
7. Destination latitude and longitude

Finally, for each itinerary returned to the user, there is a travel duration in seconds and the number of transit vehicles used in that itinerary.


## Tuning transit routing
Nested inside `transit {...}` in `router-config.json`.

Some of these parameters for tuning transit routing is only available through configuration and cannot be set in the routing request. These parameters work together with the default routing request and the actual routing request.

config key | description | value type | value default
---------- | ----------- | ---------- | -------------
`maxNumberOfTransfers` | Use this parameter to allocate enough space for Raptor. Set it to the maximum number of transfers for any given itinerary expected to be found within the entire transit network. The memory overhead of setting this higher than the maximum number of transfers is very little so it is better to set it too high then to low. | int | `12`
`scheduledTripBinarySearchThreshold` | The threshold is used to determine when to perform a binary trip schedule search to reduce the number of trips departure time lookups and comparisons. When testing with data from Entur and all of Norway as a Graph, the optimal value was around 50. Changing this may improve the performance with just a few percent. | int | `50`
`iterationDepartureStepInSeconds` | Step for departure times between each RangeRaptor iterations. A transit network usually uses minute resolution for its depature and arrival times. To match that, set this variable to 60 seconds. | int | `60`
`searchThreadPoolSize` | Split a travel search in smaller jobs and run them in parallel to improve performance. Use this parameter to set the total number of executable threads available across all searches. Multiple searches can run in parallel - this parameter have no effect with regard to that. If 0, no extra threads are started and the search is done in one thread. | int | `0`
`dynamicSearchWindow` | The dynamic search window coefficients used to calculate the EDT(earliest-departure-time), LAT(latest-arrival-time) and SW(raptor-search-window) using heuristics. | object | `null`
`stopTransferCost` | Use this to set a stop transfer cost for the given `TransferPriority`. The cost is applied to boarding and alighting at all stops. All stops have a transfer cost priority set, the default is `ALLOWED`. The `stopTransferCost` parameter is optional, but if listed all values must be set. | object | `null`

### Tuning transit routing - Dynamic search window
Nested inside `transit : { dynamicSearchWindow : { ... } }` in `router-config.json`.

config key | description | value type | value default
---------- | ----------- | ---------- | -------------
`minTripTimeCoefficient` | The coefficient to multiply with minimum travel time found using a heuristic search. This value is added to the `minWinTimeMinutes`. A value between `0.0` to `3.0` is expected to give ok results. | double | `0.75`
`minWinTimeMinutes` | The constant minimum number of minutes for a raptor search window. Use a value between 20-180 minutes in a normal deployment. | int | `40`
`maxWinTimeMinutes` | Set an upper limit to the calculation of the dynamic search window to prevent exceptionable cases to cause very long search windows. Long search windows consumes a lot of resources and may take a long time. Use this parameter to tune the desired maximum search time. | int | `180` (3 hours)
`stepMinutes` | The search window is rounded of to the closest multiplication of N minutes. If N=10 minutes, the search-window can be 10, 20, 30 ... minutes. It the computed search-window is 5 minutes and 17 seconds it will be rounded up to 10 minutes. | int | `10`


### Tuning transit routing - Stop transfer cost
Nested inside `transit : { stopTransferCost : { ... } }` in `router-config.json`.

This _cost_ is in addition to other costs like `boardCost` and indirect cost from waiting (board-/alight-/transfer slack). You should account for this when you tune the routing search parameters.

If not set the `stopTransferCost` is ignored. This is only available for NeTEx imported Stops. 

The cost is a scalar, but is equivalent to the felt cost of riding a transit trip for 1 second.

config key | description | value type 
---------- | ----------- | ---------- 
`DISCOURAGED` | Use a very high cost like `72 000` to eliminate transfers ath the stop if not the only option. | int
`ALLOWED` | Allowed, but not recommended. Use something like `150`. | int 
`RECOMMENDED` | Use a small cost penalty like `60`. | int
`PREFERRED` | The best place to do transfers. Should be set to `0`(zero). | int 

Use values in a range from `0` to `100 000`. **All key/value pairs are required if the `stopTransferCost` is listed.** 


### Transit example section from router-config.json
```
{
    transit: {
        maxNumberOfTransfers: 12,
        scheduledTripBinarySearchThreshold: 50,
        iterationDepartureStepInSeconds: 60,
        searchThreadPoolSize: 0,
        dynamicSearchWindow: {
            minTripTimeCoefficient: 0.4,
            minTripTimeCoefficient: 0.3,
            minTimeMinutes: 30,
            maxLengthMinutes : 360,
            stepMinutes: 10
        },
        stopTransferCost: {
            DISCOURAGED: 72000,
            ALLOWED:       150,
            RECOMMENDED:    60,
            PREFERRED:       0
        }
    }
}
```

## Real-time data

GTFS feeds contain *schedule* data that is is published by an agency or operator in advance. The feed does not account
 for unexpected service changes or traffic disruptions that occur from day to day. Thus, this kind of data is also
 referred to as 'static' data or 'theoretical' arrival and departure times.

### GTFS-Realtime

The [GTFS-RT spec](https://developers.google.com/transit/gtfs-realtime/) complements GTFS with three additional kinds of
feeds. In contrast to the base GTFS schedule feed, they provide *real-time* updates (*'dynamic'* data) and are are
updated from minute to minute.

- **Alerts** are text messages attached to GTFS objects, informing riders of disruptions and changes.

- **TripUpdates** report on the status of scheduled trips as they happen, providing observed and predicted arrival and
departure times for the remainder of the trip.

- **VehiclePositions** give the location of some or all vehicles currently in service, in terms of geographic coordinates
or position relative to their scheduled stops.

### Bicycle rental systems

Besides GTFS-RT transit data, OTP can also fetch real-time data about bicycle rental networks including the number
of bikes and free parking spaces at each station. We support bike rental systems from JCDecaux, BCycle, VCub, Keolis,
Bixi, the Dutch OVFiets system, ShareBike, GBFS and a generic KML format.
It is straightforward to extend OTP to support any bike rental system that
exposes a JSON API or provides KML place markers, though it requires writing a little code.

The generic KML needs to be in format like

```XML
<?xml version="1.0" encoding="utf-8" ?>
<kml xmlns="http://www.opengis.net/kml/2.2">
<Document id="root_doc">
<Schema name="citybikes" id="citybikes">
    <SimpleField name="ID" type="int"></SimpleField>
</Schema>
  <Placemark>
    <name>A Bike Station</name>
    <ExtendedData><SchemaData schemaUrl="#citybikes">
        <SimpleData name="ID">0</SimpleData>
    </SchemaData></ExtendedData>
      <Point><coordinates>24.950682884886643,60.155923430488102</coordinates></Point>
  </Placemark>
</Document></kml>
```

### Configuring real-time updaters

Real-time data can be provided using either a pull or push system. In a pull configuration, the GTFS-RT consumer polls the
real-time provider over HTTP. That is to say, OTP fetches a file from a web server every few minutes. In the push
configuration, the consumer opens a persistent connection to the GTFS-RT provider, which then sends incremental updates
immediately as they become available. OTP can use both approaches. The [OneBusAway GTFS-realtime exporter project](https://github.com/OneBusAway/onebusaway-gtfs-realtime-exporter) provides this kind of streaming, incremental updates over a websocket rather than a single large file.

Real-time data sources are configured in `router-config.json`. The `updaters` section is an array of JSON objects, each
of which has a `type` field and other configuration fields specific to that type. Common to all updater entries that
connect to a network resource is the `url` field.

```JSON
// router-config.json
{
    // Routing defaults are any public field or setter in the Java class
    // org.opentripplanner.routing.api.request.RoutingRequest
    "routingDefaults": {
        "numItineraries": 6,
        "walkSpeed": 2.0,
        "stairsReluctance": 4.0,
        "carDropoffTime": 240
    },

    "updaters": [

        // GTFS-RT service alerts (frequent polling)
        {
            "type": "real-time-alerts",
            "frequencySec": 30,
            "url": "http://developer.trimet.org/ws/V1/FeedSpecAlerts/appID/0123456789ABCDEF",
            "feedId": "TriMet"
        },

        // Polling bike rental updater.
        // sourceType can be: jcdecaux, b-cycle, bixi, keolis-rennes, ov-fiets,
        // city-bikes, citi-bike-nyc, next-bike, vcub, kml
        {
            "type": "bike-rental",
            "frequencySec": 300,
            "sourceType": "city-bikes",
            "url": "http://host.domain.tld"
        },

        //<!--- San Francisco Bay Area bike share -->
        {
          "type": "bike-rental",
          "frequencySec": 300,
          "sourceType": "sf-bay-area",
          "url": "http://www.bayareabikeshare.com/stations/json"
        },

        //<!--- Tampa Area bike share -->
        {
          "type": "bike-rental",
          "frequencySec": 300,
          "sourceType": "gbfs",
          "url": "http://coast.socialbicycles.com/opendata/"
        },


        // Polling bike rental updater for DC bikeshare (a Bixi system)
        // Negative update frequency means to run once and then stop updating (essentially static data)
        {
            "type": "bike-rental",
            "sourceType": "bixi",
            "url": "https://www.capitalbikeshare.com/data/stations/bikeStations.xml",
            "frequencySec": -1
		},

        // Bike parking availability
        {
            "type": "bike-park"
        },

        // Polling for GTFS-RT TripUpdates)
        {
            "type": "stop-time-updater",
            "frequencySec": 60,
            // this is either http or file... shouldn't it default to http or guess from the presence of a URL?
            "sourceType": "gtfs-http",
            "url": "http://developer.trimet.org/ws/V1/TripUpdate/appID/0123456789ABCDEF",
            "feedId": "TriMet"
        },

        // Streaming differential GTFS-RT TripUpdates over websockets
        {
            "type": "websocket-gtfs-rt-updater"
        }
    ]
}
```
#### GBFS Configuration

Steps to add a GBFS feed to a router:

- Add one entry in the `updater` field of `router-config.json` in the format

```JSON
{
     "type": "bike-rental",
     "frequencySec": 60,
     "sourceType": "gbfs",
     "url": "http://coast.socialbicycles.com/opendata/"
}
```

- Follow these instructions to fill these fields:

```
type: "bike-rental"
frequencySec: frequency in seconds in which the GBFS service will be polled
sourceType: "gbfs"
url: the URL of the GBFS feed (do not include the gbfs.json at the end) *
```
\* For a list of known GBFS feeds see the [list of known GBFS feeds](https://github.com/NABSA/gbfs/blob/master/systems.csv)

#### Bike Rental Service Directory configuration (sandbox feature)

To configure and url for the [BikeRentalServiceDirectory](sandbox/BikeRentalServiceDirectory.md).

```JSON
// router-config.json
{
  "bikeRentalServiceDirectoryUrl": "https://api.dev.entur.io/mobility/v1/bikes"
}
```

# Configure using command-line arguments

Certain settings can be provided on the command line, when starting OpenTripPlanner. See the `CommandLineParameters` class for [a full list of arguments](http://dev.opentripplanner.org/javadoc/1.4.0/org/opentripplanner/standalone/CommandLineParameters.html).
