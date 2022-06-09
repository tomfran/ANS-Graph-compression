package it.tomfran.thesis.graph;

import it.tomfran.thesis.ans.AnsEncoder;
import it.tomfran.thesis.ans.AnsModel;
import it.tomfran.thesis.ans.SymbolStats;
import it.tomfran.thesis.clustering.DatapointHistogram;
import it.tomfran.thesis.clustering.KmeansHistogram;
import it.tomfran.thesis.io.LongWordBitReader;
import it.tomfran.thesis.io.LongWordOutputBitStream;
import it.unimi.dsi.fastutil.longs.LongBigList;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.io.InputBitStream;
import it.unimi.dsi.io.OutputBitStream;
import it.unimi.dsi.sux4j.util.EliasFanoMonotoneLongBigList;
import it.unimi.dsi.webgraph.ImmutableGraph;
import it.unimi.dsi.webgraph.LazyIntIterator;
import it.unimi.dsi.webgraph.NodeIterator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Properties;

import static it.unimi.dsi.webgraph.EFGraph.loadLongBigList;
import static java.lang.Math.max;

public class AnsGraph extends ImmutableGraph {

    public static final String GRAPH_EXTENSION = ".graph";
    public static final String OFFSETS_EXTENSION = ".offset";
    public static final String MODEL_EXTENSION = ".model";
    public static final String PROPERTIES_EXTENSION = ".properties";
    public static final int P_RANGE = 10;
    private static final boolean PROGRESS = false;
    public static final int ESCAPE_THRESHOLD_PERCENTAGE = 25;
    public static final int ESCAPE_FREQUENCY = 25;

    /** Elias fano sequence for the offsets. */
    public LongBigList offsets;
    /** Number of nodes. */
    protected int numNodes;
    /** Array with the And models used in the graph. */
    protected AnsModel[] ansModels;
    /** Long big list containing the ecnoded graph. */
    protected LongBigList graph;
    /** LongWordBitReader to read outdegrees.*/
    protected LongWordBitReader outdegreeLongWordBitReader;

    public int escapeBits;


    public AnsGraph(int numNodes, AnsModel[] ansModels, LongBigList offsets, LongBigList graph, LongWordBitReader outdegreeLongWordBitReader, int escapeBits) {
        this.numNodes = numNodes;
        this.ansModels = ansModels;
        this.offsets = offsets;
        this.graph = graph;
        this.outdegreeLongWordBitReader = outdegreeLongWordBitReader;
        this.escapeBits = escapeBits;
    }

    public static void store(ImmutableGraph graph, CharSequence basename, int clusters, int iterations) throws IOException {
        // build the kmeans data points for this graph

        // run Kmeans with the given number of clusters
        KmeansHistogram model = computeClusters(graph, clusters, iterations);
        // store internal with the clustering model
        storeInternal(graph, basename, "cluster", model, 0, 0);

    }

    public static void storeEscape(ImmutableGraph graph, CharSequence basename, int escapePercentage, int escapeFrequency) throws IOException {
        storeInternal(graph, basename, "optimal", null, escapePercentage, escapeFrequency);
    }

    public static void store(ImmutableGraph graph, CharSequence basename) throws IOException {
        storeInternal(graph, basename, "optimal", null, 0, 0);
    }

    public static void storeInternal(ImmutableGraph graph, CharSequence basename, CharSequence method, KmeansHistogram model, int escapePercentage, int escapeFrequency) throws IOException {

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

        long outdegreeBits, stateBits, modelBits, numArcs, numStates, maxStates, numEscapes, maxEscapes;
        outdegreeBits = stateBits = modelBits = numArcs = numStates = maxStates = numEscapes = maxEscapes = 0;
        offsets.writeLongDelta(0);

        int N = graph.numNodes();
        int escapeBits = (int)(Math.log(N) / Math.log(2) + 1);

        long current = 0;
        long prev = 0;
        int modelNum = 0;
        int i;
        if (PROGRESS) i = 0;

        long lostBits = 0;

        // if clustering is selected, write all models to the stream
        if (method == "cluster"){
            for (int j = 0; j < model.K; j++)
                modelBits += new AnsModel(model.centroid[j]).dump(modelStream);
        }

        for (final NodeIterator nodeIterator = graph.nodeIterator(); nodeIterator.hasNext(); ) {
            if (PROGRESS) {
                if ((i % 10000) == 0) {
                    System.out.println("ANS graph compression: node -> " + i);
                }
                i++;
            }
            nodeIterator.nextInt();
            int outdegree = nodeIterator.outdegree();
            numArcs += outdegree;
            // write outdegree, if zero do not compute the rest
            outdegreeBits += graphStream.writeGamma(outdegree);
            if (outdegree > 0) {

                int[] succ = computeGaps(nodeIterator.successorArray(), outdegree);
                // build model according to method
                AnsModel m = null;
                if (method == "optimal") {
                    SymbolStats symStats = new SymbolStats(succ, outdegree, P_RANGE, escapePercentage, escapeFrequency);
                    m = new AnsModel(symStats);
                } else if (method == "cluster") {
                    int clusterPos = model.getClusterIndex(modelNum);
                    m = new AnsModel(model.centroid[clusterPos]);
                }
                AnsEncoder e = new AnsEncoder(m);
                // compute lost bits writing escapes

                e.encodeAll(succ, outdegree);
                for (int k : e.escapedSymbolList) {
                    lostBits += (escapeBits - Math.max(0, ((int) (Math.log(k) / Math.log(2) + 1))));
                }
                // model id, state count, statelist to .graph file
                if (method =="optimal") {
                    stateBits += e.dump(graphStream, modelNum, escapeBits);
                    modelBits += m.dump(modelStream);
                } else if(method =="cluster") {
                    stateBits += e.dump(graphStream, model.getClusterIndex(modelNum), escapeBits);
                }
                modelNum++;
                // write model to .model file
                numStates += e.stateList.size();
                maxStates = max(maxStates, e.stateList.size());
                numEscapes += e.escapedSymbolList.size();
                maxEscapes = max(maxEscapes, e.escapedSymbolList.size());
            }
            // write offsets
            current = outdegreeBits + stateBits;
            offsets.writeLongDelta(current - prev);
            prev = outdegreeBits + stateBits;
        }


        graphStream.close();
        modelStream.close();
        offsets.close();

        final DecimalFormat format = new java.text.DecimalFormat("0.###");

        if (method == "cluster") modelNum = model.K;

        final long writtenBits = new File(basename + GRAPH_EXTENSION).length() * 8 +
                new File(basename + MODEL_EXTENSION).length() * 8;

        final Properties properties = new Properties();
        properties.setProperty("nodes", String.valueOf(N));
        properties.setProperty("arcs", String.valueOf(numArcs));
        properties.setProperty("byteorder", byteOrder.toString());
        properties.setProperty("escapeBits", String.valueOf(escapeBits));
        properties.setProperty("bitsformodels", String.valueOf(modelBits));
        properties.setProperty("bitsforoutdegrees", String.valueOf(outdegreeBits));
        properties.setProperty("bitsforstates", String.valueOf(stateBits - numEscapes * escapeBits));
        properties.setProperty("bitsforescapes", format.format(numEscapes * escapeBits));
        properties.setProperty("lostbitsforescapes", String.valueOf(lostBits));
        properties.setProperty("avglostbitsforescapes", format.format((double)lostBits / N));
        properties.setProperty("writtenbits", String.valueOf(writtenBits));
        properties.setProperty("bitsperlink", format.format((double) writtenBits / numArcs));
//        properties.setProperty("bitsperlink", format.format((double) writtenBits / numArcs));
//        properties.setProperty("avgbitsforstates", format.format((double) stateBits / N));
//        properties.setProperty("avgbitsformodels", format.format((double) modelBits / modelNum));
//        properties.setProperty("avgbitsforoutdegrees", format.format((double) outdegreeBits / N));
        // states and escapes
        properties.setProperty("avgnumberofstates", format.format((double) numStates / N));
        properties.setProperty("maxnumberofstates", format.format(maxStates));
        properties.setProperty("avgnumberofescapes", format.format((double) numEscapes / N));
        properties.setProperty("maxnumberofescapes", format.format(maxEscapes));

        properties.setProperty("numberofmodels", format.format(modelNum));
        properties.setProperty("method", method.toString());
        properties.setProperty(ImmutableGraph.GRAPHCLASS_PROPERTY_KEY, AnsGraph.class.getName());

        final FileOutputStream propertyFile = new FileOutputStream(basename + PROPERTIES_EXTENSION);
        properties.store(propertyFile, "AnsGraph properties");
        propertyFile.close();

    }

    public static int[] computeGaps(int[] arr, long length) {
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

    public static KmeansHistogram computeClusters(ImmutableGraph g, int k, int iterations){

        int n = 0;
        for (final NodeIterator nodeIterator = g.nodeIterator(); nodeIterator.hasNext(); ) {
            nodeIterator.nextInt();
            if (nodeIterator.outdegree() > 0) n++;
        }
        DatapointHistogram[] data = new DatapointHistogram[n];

        int pos = 0;
        for (final NodeIterator nodeIterator = g.nodeIterator(); nodeIterator.hasNext(); ) {
            nodeIterator.nextInt();
            int outdegree = nodeIterator.outdegree();
            if (outdegree > 0){
                int[] succ = computeGaps(nodeIterator.successorArray(), outdegree);
                data[pos++] = new DatapointHistogram(new SymbolStats(succ, outdegree, P_RANGE, ESCAPE_THRESHOLD_PERCENTAGE, ESCAPE_FREQUENCY));
            }
        }

        KmeansHistogram clusteringModel = new KmeansHistogram(k, iterations, data);
        clusteringModel.fit();
//        clusteringModel.lazyFit();
//
//        for (DatapointHistogram c : clusteringModel.centroid)
//            System.out.println(c);

        return clusteringModel;
    }


    /**
     * Load an ans encoded graph.
     *
     * @param basename Path to the file.
     * @return Loaded AnsGraph.
     * @throws IOException
     */
    public static AnsGraph load(CharSequence basename) throws IOException {
        return loadInternal(basename);
    }

    protected static AnsGraph loadInternal(CharSequence basename) throws IOException {

        // open properties file
        final FileInputStream propertyFile = new FileInputStream(basename + PROPERTIES_EXTENSION);
        final Properties properties = new Properties();
        properties.load(propertyFile);
        propertyFile.close();

        // num nodes
        final int nodes = Integer.parseInt(properties.getProperty("nodes"));
        final int escapeBits = Integer.parseInt(properties.getProperty("escapeBits"));


        // byte order
        ByteOrder byteOrder = ByteOrder.BIG_ENDIAN;
        if (properties.get("byteorder").equals(ByteOrder.LITTLE_ENDIAN.toString())) byteOrder = ByteOrder.LITTLE_ENDIAN;

        // loading models from .models file
        final int numModels = Integer.parseInt(properties.getProperty("numberofmodels"));
        AnsModel[] ansModels = new AnsModel[numModels];

        LongBigList modelsLongList = loadLongBigList(basename + MODEL_EXTENSION, byteOrder);

        int l = 0;
        LongWordBitReader modelLongWordReader = new LongWordBitReader(modelsLongList, l);

        String method = properties.getProperty("method");
//        System.out.println("method: " + method);
        // each model is rebuilt reading the necessary info from the file
        for (int i = 0; i < numModels; i++) {
//            if (method.equals("optimal"))
            ansModels[i] = AnsModel.rebuildModel(modelLongWordReader);
//            else if (method.equals("orderStatistic"))
//                ansModels[i] = AnsModelOrderStatistic.rebuildModel(modelLongWordReader);
        }
        LongBigList graph = loadLongBigList(basename + GRAPH_EXTENSION, byteOrder);

        // load offsets
        final InputBitStream offsetIbs = new InputBitStream(basename + OFFSETS_EXTENSION);

        LongBigList offsets = new EliasFanoMonotoneLongBigList(nodes + 1, graph.size64() * (Long.SIZE + 1), new OffsetsLongIterator(offsetIbs, nodes));
        offsetIbs.close();

        return new AnsGraph(nodes, ansModels, offsets, graph, new LongWordBitReader(graph, 0), escapeBits);
    }

    @Override
    public LazyIntIterator successors(int node) {
        AnsModel a = getModel(node);
        if (a == null)
            return new EmptyAnsSuccessorsReader();
        return new AnsSuccessorsReader(outdegree(node), a, graph, offsets.getLong(node), escapeBits);
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
        int id = modelId(i);
        if (id == -1)
            return null;
        return ansModels[id].copy();
    }

    public int modelId(int i) {
        // read the outdegree, then read the model id written in gamma
        long od = outdegreeLongWordBitReader.position(offsets.getLong(i)).readGamma();
        if (od == 0) return -1;

        return (int) outdegreeLongWordBitReader.readGamma();
    }

    @Override
    public ImmutableGraph copy() {
        return null;
    }

    /**
     * Utility class to read a list of delta encoded longs.
     */
    private final static class OffsetsLongIterator implements LongIterator {
        private final InputBitStream offsetIbs;
        private final long n;
        private long offset;
        private long i;

        private OffsetsLongIterator(final InputBitStream offsetIbs, final long n) {
            this.offsetIbs = offsetIbs;
            this.n = n;
            offset = 0;
        }

        @Override
        public boolean hasNext() {
            return i <= n;
        }

        @Override
        public long nextLong() {
            if (!hasNext()) throw new NoSuchElementException();
            i++;
            try {
                return offset += offsetIbs.readLongDelta();
//                return offsetIbs.readLongDelta();

            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
