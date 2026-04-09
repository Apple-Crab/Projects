import java.io.*;
import java.util.*;

public class Network {
    public Map<Integer, Node> nodes;

    public Network() {
        nodes = new HashMap<>();
    }

    public void loadFromFile(String filename) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(filename));
        String line;

        while ((line = br.readLine()) != null) {
            String[] parts = line.split(" ");
            int n1 = Integer.parseInt(parts[0]);
            int n2 = Integer.parseInt(parts[1]);
            int cost = Integer.parseInt(parts[2]);

            nodes.putIfAbsent(n1, new Node(n1));
            nodes.putIfAbsent(n2, new Node(n2));

            nodes.get(n1).neighbors.put(n2, cost);
            nodes.get(n2).neighbors.put(n1, cost);
        }
        br.close();

        Set<Integer> allNodes = nodes.keySet();
        for (Node node : nodes.values()) {
            node.initialize(allNodes);
        }
    }

    public void printAllTables() {
        for (Node node : nodes.values()) {
            node.printTable();
        }
    }

    public void updateLink(int n1, int n2, int cost) {
        nodes.get(n1).neighbors.put(n2, cost);
        nodes.get(n2).neighbors.put(n1, cost);
    }
}