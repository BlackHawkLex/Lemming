package org.aksw.simba.lemming.metrics.dist;

import java.io.InputStream;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.colour.ColourPalette;
import org.aksw.simba.lemming.creation.GraphCreator;
import org.aksw.simba.lemming.metrics.dist.VertexColourDistribution;
import org.apache.commons.io.IOUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.junit.Test;

import com.carrotsearch.hppc.BitSet;
import com.carrotsearch.hppc.ObjectDoubleOpenHashMap;

import junit.framework.Assert;

public class VertexColourDistributionTest {

    private static final String EXPECTED_VERTEX_CLASSES[] = new String[] { "http://example.org/class1", "http://example.org/class1|http://example.org/class2" };
    private static final int EXPECTED_VERTEX_COUNTS[] = new int[] { 1, 1 };
    private static final int EXPECTED_VERTEXES_WITHOUT_COLOURS = 3;
    private static final String GRAPH_FILE = "graph1.n3";

    @Test
    public void test() {
        Model model = ModelFactory.createDefaultModel();
        InputStream is = this.getClass().getClassLoader().getResourceAsStream(GRAPH_FILE);
        model.read(is, null, "N3");
        IOUtils.closeQuietly(is);

        GraphCreator creator = new GraphCreator();
        ColouredGraph graph = creator.processModel(model);

        VertexColourDistribution distribution = new VertexColourDistribution();
        distribution.apply(graph);

        ObjectDoubleOpenHashMap<BitSet> expectedCounts = new ObjectDoubleOpenHashMap<BitSet>();
        ColourPalette palette = graph.getVertexPalette();
        BitSet colour;
        for (int i = 0; i < EXPECTED_VERTEX_CLASSES.length; ++i) {
            if (EXPECTED_VERTEX_CLASSES[i].contains("|")) {
                colour = palette.getColourMixture(EXPECTED_VERTEX_CLASSES[i].split("\\|"));
            } else {
                colour = palette.getColour(EXPECTED_VERTEX_CLASSES[i]);
            }
            Assert.assertTrue("There is no colour for " + EXPECTED_VERTEX_CLASSES[i], colour.cardinality() > 0);
            expectedCounts.put(colour, EXPECTED_VERTEX_COUNTS[i]);
        }
        expectedCounts.put(new BitSet(), EXPECTED_VERTEXES_WITHOUT_COLOURS);

        Object sampleSpace[] = distribution.getSampleSpace();
        for (int i = 0; i < sampleSpace.length; ++i) {
            Assert.assertTrue(expectedCounts.containsKey((BitSet) sampleSpace[i]));
            Assert.assertEquals(expectedCounts.get((BitSet) sampleSpace[i]), distribution.getDistribution()[i]);
        }
        Assert.assertEquals(expectedCounts.size(), sampleSpace.length);
    }
}
