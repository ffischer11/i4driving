package org.opentrafficsim.i4driving.sim0mq;

import java.util.List;

import org.djutils.exceptions.Throw;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.i4driving.messages.adapters.ScalarAsListAdapter;
import org.opentrafficsim.road.network.RoadNetwork;

import com.google.gson.annotations.JsonAdapter;

public class RoutesJson
{

    /** Routes. */
    private List<RouteJson> routes;

    /**
     * Route definition.
     */
    private final class RouteJson
    {
        /** Route id. */
        private String id;

        /** Object ids in the route. */
        @JsonAdapter(ScalarAsListAdapter.class)
        private List<String> objects;
    }

    /**
     * Create routes, which stored in the network.
     * @param network road network
     * @param gtuType GTU type for which all routes will be stored
     * @param simulation simulation
     * @throws NetworkException if a route is not correctly defined
     */
    public void createRoutes(final RoadNetwork network, final GtuType gtuType, final Sim0mqSimulation simulation)
            throws NetworkException
    {
        for (RouteJson routeJson : this.routes)
        {
        	Route route = null;
        	if(routeJson.objects.size() == 2) {
        		// Simplified route construction by just the first and last link of the route
        		Link first = network.getLink(routeJson.objects.getFirst());
        		Node startNode = first.getStartNode();
        		Link last = network.getLink(routeJson.objects.getLast());
        		Node endNode = last.getEndNode();
        		
        		Route temp = network.getShortestRouteBetween(gtuType, startNode, endNode); 
                Throw.when(temp == null || temp.size() == 0, NetworkException.class,
                        "Unable to find nodes for route defined by first and last links: %s.", routeJson.objects);
                route = new Route(routeJson.id, gtuType,  temp.getNodes());
        	}
        	else {
	            List<Node> nodes = simulation.getRouteObjectType().getNodes(network, routeJson.objects);
	            Throw.when(nodes == null || nodes.isEmpty(), NetworkException.class,
	                    "Unable to find nodes for route defined by objects %s.", routeJson.objects);
	            route = new Route(routeJson.id, gtuType, nodes);
        	}
    		network.addRoute(gtuType, route);
        }
    }

}
