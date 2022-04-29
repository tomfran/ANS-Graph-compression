package it.tomfran.thesis.graph;

import it.tomfran.thesis.ans.AnsEncoder;
import it.tomfran.thesis.ans.AnsModel;
import it.tomfran.thesis.ans.SymbolStats;
import it.tomfran.thesis.io.LongWordBitReader;
import it.tomfran.thesis.io.LongWordOutputBitStream;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongBigArrayBigList;
import it.unimi.dsi.io.InputBitStream;
import it.unimi.dsi.io.OutputBitStream;
import it.unimi.dsi.webgraph.ImmutableGraph;
import it.unimi.dsi.webgraph.LazyIntIterator;
import it.unimi.dsi.webgraph.NodeIterator;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Properties;

import static it.unimi.dsi.webgraph.EFGraph.loadLongBigList;
import static java.lang.Math.max;

public class AnsGraph extends ImmutableGraph {

    private static final boolean DEBUG = true;
    private static final boolean ANS_DEBUG = false;

    public static final String GRAPH_EXTENSION = ".graph";
    public static final String OFFSETS_EXTENSION = ".offset";
    public static final String MODEL_EXTENSION = ".model";
    public static final String PROPERTIES_EXTENSION = ".properties";

    protected int numNodes;
    protected AnsModel[] ansModels;
    public LongArrayList offsets;
    protected LongBigArrayBigList graph;
    protected LongWordBitReader outdegreeLongWordBitReader;

    public static void store(ImmutableGraph graph, CharSequence basename) throws IOException {

        ByteOrder byteOrder = ByteOrder.nativeOrder();

        // graph sream
        final FileOutputStream graphOs = new FileOutputStream(basename + GRAPH_EXTENSION);
        final FileChannel graphChannel = graphOs.getChannel();
        final LongWordOutputBitStream graphStream = new LongWordOutputBitStream(graphChannel, byteOrder);

        // model stream
        final FileOutputStream modelOs = new FileOutputStream(basename + MODEL_EXTENSION);
        final FileChannel modelChannel = modelOs.getChannel();
        final LongWordOutputBitStream modelStream = new LongWordOutputBitStream(modelChannel, byteOrder);

        //offsets stream
        final OutputBitStream offsets = new OutputBitStream(basename + OFFSETS_EXTENSION);

        long outdegreeBits, stateBits, modelBits, numArcs, numStates, maxStates;
        outdegreeBits = stateBits = modelBits = numArcs = numStates = maxStates = 0;
        offsets.writeLongDelta(0);

        int N = graph.numNodes();

        int i = 0;
        if (DEBUG) {
            System.out.println("Started Encoding");
        }
        // the model id for now is the index of the node
        int modelNum = 0;
        for (final NodeIterator nodeIterator = graph.nodeIterator(); nodeIterator.hasNext(); ) {
//        for (int i = 0; i < N; i++) {
            if (DEBUG) {
                if ((i++ % 10000) == 0)
                    System.out.println("Node num: " + i);
            }
            nodeIterator.nextInt();
            int outdegree = nodeIterator.outdegree();
            numArcs += outdegree;
            // write outdegree, if zero do not compute the rest
            outdegreeBits += graphStream.writeGamma(outdegree);
            if (outdegree > 0) {
                int[] succ = computeGaps(nodeIterator.successorArray(), outdegree);
                // build symbol stats
                SymbolStats symStats = new SymbolStats(succ, outdegree, 10);
                // build models
                AnsModel m = new AnsModel(symStats);
                AnsEncoder e = new AnsEncoder(m);
                e.encodeAll(succ, outdegree);
                if (ANS_DEBUG) {
                    System.out.println("\nModel num: " + modelNum);
                    e.debugPrint();
                }
                // model id, state count, statelist to .graph file
                stateBits += e.dump(graphStream, modelNum);
                modelNum++;
                // write model to .model file
                modelBits += m.dump(modelStream);

                numStates += e.stateList.size();
                maxStates = max(maxStates, e.stateList.size());

            }
            // write offsets
            offsets.writeLongDelta(outdegreeBits + stateBits);
        }

        graphStream.close();
        modelStream.close();
        offsets.close();

        final DecimalFormat format = new java.text.DecimalFormat("0.###");

        long writtenBits = outdegreeBits + stateBits + modelBits;

        final Properties properties = new Properties();
        properties.setProperty("nodes", String.valueOf(N));
        properties.setProperty("arcs", String.valueOf(numArcs));
        properties.setProperty("byteorder", byteOrder.toString());
        properties.setProperty("bitsformodels", String.valueOf(modelBits));
        properties.setProperty("bitsforoutdegrees", String.valueOf(outdegreeBits));
        properties.setProperty("bitsforstates", String.valueOf(stateBits));
        properties.setProperty("bitsperlink", format.format((double) writtenBits / numArcs));
        properties.setProperty("avgbitsforstates", format.format((double) stateBits / N));
        properties.setProperty("avgbitsformodels", format.format((double) modelBits / modelNum));
        properties.setProperty("avgbitsforoutdegrees", format.format((double) outdegreeBits / N));
        properties.setProperty("avgnumberofstates", format.format((double) numStates / N));
        properties.setProperty("maxnumberofstates", format.format(maxStates));
        properties.setProperty("numberofmodels", format.format(modelNum));
        properties.setProperty(ImmutableGraph.GRAPHCLASS_PROPERTY_KEY, AnsGraph.class.getName());

        final FileOutputStream propertyFile = new FileOutputStream(basename + PROPERTIES_EXTENSION);
        properties.store(propertyFile, "AnsGraph properties");
        propertyFile.close();

        if (DEBUG) System.out.println("Compression completed");
    }

    private static int[] computeGaps(int[] arr, long length) {
        int n = (int) length;
        int[] ret = Arrays.copyOf(arr, n);

        Arrays.sort(ret);
        //
        int tmp;
        // compute gaps
        for (int i = n - 1; i >= 1; i--) {
            ret[i] -= ret[i - 1];
        }
        // reverse order
        for (int i = 0; i < n / 2; i++) {
            tmp = ret[i];
            ret[i] = ret[n - i - 1];
            ret[n - i - 1] = tmp;
        }
        return ret;
    }

    public static AnsGraph load(CharSequence basename) throws IOException {
        return new AnsGraph().loadInternal(basename);
    }

    protected AnsGraph loadInternal(CharSequence basename) throws IOException {

        // open properties file
        final FileInputStream propertyFile = new FileInputStream(basename + PROPERTIES_EXTENSION);
        final Properties properties = new Properties();
        properties.load(propertyFile);
        propertyFile.close();

        // num nodes
        final int nodes = Integer.parseInt(properties.getProperty("nodes"));

        // byte order
        ByteOrder byteOrder = ByteOrder.BIG_ENDIAN;
//        if (properties.get("byteorder").equals(ByteOrder.BIG_ENDIAN.toString())) byteOrder = ByteOrder.BIG_ENDIAN;
        if (properties.get("byteorder").equals(ByteOrder.LITTLE_ENDIAN.toString())) byteOrder = ByteOrder.LITTLE_ENDIAN;

        // loading models form .models file
        final int numModels = Integer.parseInt(properties.getProperty("numberofmodels"));
        ansModels = new AnsModel[numModels];

        LongBigArrayBigList modelsLongList = loadLongBigList(basename + MODEL_EXTENSION, byteOrder);

        int l = 0; // TODO: check this
        LongWordBitReader modelLongWordReader = new LongWordBitReader(modelsLongList, l);
        // each model is rebuilt reading the necessary info from the file
        for (int i = 0; i < numModels; i++) {
            ansModels[i] = AnsModel.rebuildModel(modelLongWordReader);
        }
        // load the offsets
        // for now load to memory, TODO: fix this
        final InputBitStream ioffsets = new InputBitStream(basename + OFFSETS_EXTENSION);
        offsets = new LongArrayList();
        for (int i = 0; i < nodes; i++)
            offsets.add(ioffsets.readLongDelta());

        LongBigArrayBigList graph = loadLongBigList(basename + GRAPH_EXTENSION, byteOrder);


        AnsGraph g = new AnsGraph();
        g.numNodes = nodes;
        g.offsets = offsets;
        g.ansModels = ansModels;
        g.graph = graph;
        g.outdegreeLongWordBitReader = new LongWordBitReader(graph, 0);

        return g;

    }

    public LazyIntIterator successors(int node) {
        return new AnsSuccessorsReader(outdegree(node), getModel(node), graph, offsets.getLong(node));
    }

    public int[] successorsArray(int node) {
        return super.successorArray(node);
    }

    @Override
    public int numNodes() {
        return numNodes;
    }

    @Override
    public boolean randomAccess() {
        return true;
    }

    @Override
    public int outdegree(int i) {
        return (int) outdegreeLongWordBitReader.position(offsets.getLong(i)).readGamma();
    }

    private AnsModel getModel(int i) {
        return ansModels[modelId(i)].copy();
    }

    public int modelId(int i) {
        // read the outdegree, then read the model id written in gamma
        outdegreeLongWordBitReader.position(offsets.getLong(i)).readGamma();
        return (int) outdegreeLongWordBitReader.readGamma();
    }

    @Override
    public ImmutableGraph copy() {
        return null;
    }
}
