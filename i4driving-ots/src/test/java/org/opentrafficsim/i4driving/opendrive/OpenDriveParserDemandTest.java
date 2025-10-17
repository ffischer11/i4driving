package org.opentrafficsim.i4driving.opendrive;

import java.util.ArrayList;
import java.util.List;

import org.djunits.unit.FrequencyUnit;
import org.djunits.value.vdouble.vector.FrequencyVector;
import org.djunits.value.vdouble.vector.TimeVector;
import org.djutils.cli.CliUtil;
import org.opentrafficsim.core.definitions.DefaultsNl;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.road.definitions.DefaultsRoadNl;
import org.opentrafficsim.road.gtu.generator.characteristics.DefaultLaneBasedGtuCharacteristicsGeneratorOd;
import org.opentrafficsim.road.gtu.generator.characteristics.LaneBasedGtuCharacteristicsGeneratorOd;
import org.opentrafficsim.road.network.RoadNetwork;
import org.opentrafficsim.road.od.Categorization;
import org.opentrafficsim.road.od.Category;
import org.opentrafficsim.road.od.Interpolation;
import org.opentrafficsim.road.od.OdApplier;
import org.opentrafficsim.road.od.OdMatrix;
import org.opentrafficsim.road.od.OdOptions;
import org.opentrafficsim.swing.script.AbstractSimulationScript;

import picocli.CommandLine.Option;

/**
 * Test class for OpenDRIVE networks within the project. This class loads a OpenDRIVE network and places demand on a specific OD
 * combination. Models are default base models, as this class only tests the network and the parsing thereof.
 * @author wjschakel
 */
public class OpenDriveParserDemandTest extends AbstractSimulationScript
{

    /** */
    private static final long serialVersionUID = 20251016L;

    /** Network file. */
    @Option(names = "--networkFile", description = "OpenDRIVE network file")
    private String networkFile;

    /** Origin node. */
    @Option(names = "--origin", description = "Origin node", defaultValue = "c1_6")
    private String origin;

    /** Destination node. */
    @Option(names = "--destination", description = "Destination node", defaultValue = "agent_end_c3_1")
    private String destination;

    /*-
     * For rr_test_scenarios_repeated.xodr:
     * start_a, end_a_2 WORKS
     * agent_start_a1_1, end_a_2 WORKS
     * agent_start_a2_1, end_a_2 WORKS
     * agent_start_a3_1, end_a_2 WORKS
     * start_b, end_b WORKS
     * start_c, end_c WORKS
     * c1_6, agent_end_c3_1 WORKS
     */

    /**
     * Main method.
     * @param args arguments
     * @throws Exception
     */
    public static void main(final String[] args) throws Exception
    {
        OpenDriveParserDemandTest vtiTest = new OpenDriveParserDemandTest();
        CliUtil.execute(vtiTest, args);
        vtiTest.start();
    }

    protected OpenDriveParserDemandTest()
    {
        super("OpenDRIVE test", "Test of OpenDRIVE network and parser");
    }

    @Override
    protected RoadNetwork setupSimulation(final OtsSimulatorInterface sim) throws Exception
    {
        RoadNetwork network = new RoadNetwork("OpenDRIVE network", sim);
        OpenDriveParser parser = OpenDriveParser.parseXodr(this.networkFile).setUseRoadName(true);
        parser.build(network);

        Node o = parser.getOrigin(this.origin, true);
        Node d = parser.getDestination(this.destination, true);

        List<Node> origins = new ArrayList<>();
        List<Node> destinations = new ArrayList<>();
        origins.add(o);
        destinations.add(d);
        TimeVector globalTime = new TimeVector(new double[] {0.0, 3600.0});
        OdMatrix odMatrix =
                new OdMatrix("OdMatrix", origins, destinations, Categorization.UNCATEGORIZED, globalTime, Interpolation.LINEAR);
        odMatrix.putDemandVector(o, d, Category.UNCATEGORIZED,
                new FrequencyVector(new double[] {200.0, 200.0}, FrequencyUnit.PER_HOUR));
        OdOptions odOptions = new OdOptions();
        LaneBasedGtuCharacteristicsGeneratorOd factory = new DefaultLaneBasedGtuCharacteristicsGeneratorOd.Factory(
                DefaultLaneBasedGtuCharacteristicsGeneratorOd.defaultLmrs(sim.getModel().getStream("generation"))).create();
        odOptions.set(OdOptions.GTU_TYPE, factory);
        OdApplier.applyOd(network, odMatrix, odOptions, DefaultsRoadNl.ROAD_USERS);

        GtuType.registerTemplateSupplier(DefaultsNl.CAR, DefaultsNl.NL);

        return network;
    }

}
