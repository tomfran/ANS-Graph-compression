package it.tomfran.thesis.graph;

import it.tomfran.thesis.ans.AnsDecoder;
import it.tomfran.thesis.ans.AnsEncoder;
import it.tomfran.thesis.ans.SymbolStats;
import it.tomfran.thesis.io.LongInputStream;
import it.tomfran.thesis.io.LongOutputStream;
import it.unimi.dsi.webgraph.ImmutableGraph;
import it.unimi.dsi.webgraph.LazyIntIterator;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class AnsGraph extends ImmutableGraph {

    protected int numNodes;
    protected int[] outdegree;
    protected AnsDecoder[] nodeDecoder;
    public static final String GRAPH_EXTENSION = ".graph";

    public static void store(ImmutableGraph g, CharSequence basename) throws IOException {

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        LongOutputStream los = new LongOutputStream(os);

        // for each node, get the outdegree and put it on the stream
        // get his successors,
        // encode the list reversed by gap,
        // flush the encoder to the stream,
        // avoid flushing stream buffer until last encoding.
        int nodes = g.numNodes();

        los.writeInt(nodes, 31);

        int outdegree;
        for (int i = 0; i < nodes; i++) {
            outdegree = g.outdegree(i);
            los.writeInt(outdegree, 31);
            if (outdegree == 0)
                continue;
            int[] succ = g.successorArray(i);
            computeGaps(succ, outdegree);
            SymbolStats s = new SymbolStats(succ, outdegree, 10);
            AnsEncoder ans = new AnsEncoder(s, los);
            ans.encodeAll(succ, outdegree);
            ans.flush((i == (nodes-1)));
        }

        Files.write(Paths.get(basename + GRAPH_EXTENSION), os.toByteArray());
    }

    private static void computeGaps(int[] arr, int length){
        int n = length;
        int tmp;
        // compute gaps
        for (int i = n-1; i >= 1; i--) {
            arr[i] -= arr[i-1];
        }
        // reverse order
        for (int i = 0; i < n/2; i++){
            tmp = arr[i];
            arr[i] = arr[n-i-1];
            arr[n-i-1] = tmp;
        }
    }

    public static AnsGraph load(CharSequence basename) throws IOException {
        return new AnsGraph().loadInternal(basename);
    }

    protected AnsGraph loadInternal(CharSequence basename) throws IOException {
        byte[] fileContent = Files.readAllBytes(Paths.get(basename + GRAPH_EXTENSION));
        ByteArrayInputStream is = new ByteArrayInputStream(fileContent);
        LongInputStream lis = new LongInputStream(is);

        AnsGraph g = new AnsGraph();
        int nodes = lis.readInt(31);
        g.numNodes = nodes;
        g.outdegree = new int[nodes];
        g.nodeDecoder = new AnsDecoder[nodes];

        for (int i = 0; i < nodes; i++) {
            g.outdegree[i] = lis.readInt(31);
            if (g.outdegree[i] != 0)
                g.nodeDecoder[i] = new AnsDecoder(lis);
        }

        return g;

    }

    public LazyIntIterator successors(int node) {
        return new AnsSuccessorsReader(outdegree[node], nodeDecoder[node]);
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
        return false;
    }

    @Override
    public int outdegree(int i) {
        return outdegree[i];
    }

    @Override
    public ImmutableGraph copy() {
        return null;
    }
}
