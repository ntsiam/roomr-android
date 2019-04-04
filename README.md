# roomr-android Routing Application

This application enables routing and navigation inside buildings, using a map of the floor topology to calculate the routing and visualize the path and a tile server that serves the indoor maps as .png images, which are then visualized on the screen, as a tile overlay on top of google maps. 
As a tile server, we use a manually built OSM tile server. A tutorial can be found [here](https://switch2osm.org/manually-building-a-tile-server-16-04-2-lts/).

As users navigate themselves inside one of the available buildings, their position is updated according to their movement and WiFi data is collected from the surrounding environment that later enable on the fly localization.

# How to create and add new maps
To create new maps for routing, we use [JOSM](https://josm.openstreetmap.de/). For routing in a new indoor location, we need two files: An .osm file that contains the geometry of the indoor location and a .geojson file that contains the floor topology. The geometry is loaded on the tile server (example [here](https://www.youtube.com/watch?v=mqWl_7PN3lc)), while the topology is loaded in the application (example [here](https://www.youtube.com/watch?v=lnJENrE9vvg).

