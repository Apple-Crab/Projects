import java.util.*;

public class Node {
    public int id;
    public Map<Integer, Integer> neighbors; // neighbor -> cost
    public Map<Integer, Integer> distanceVector; // destination -> cost
    public Map<Integer, Integer> nextHop; // destination -> next hop

    public Node(int id) {
        this.id = id;
        neighbors = new HashMap<>();
        distanceVector = new HashMap<>();
        nextHop = new HashMap<>();
    }

    public void initialize(Set<Integer> allNodes) {
        for (int node : allNodes) {
            if (node == id) {
                distanceVector.put(node, 0);
            } else if (neighbors.containsKey(node)) {
                distanceVector.put(node, neighbors.get(node));
                nextHop.put(node, node);
            } else {
                distanceVector.put(node, 16); // infinity
            }
        }
    }

    public boolean updateDV(Map<Integer, Node> network) {
        boolean updated = false;

        for (int dest : distanceVector.keySet()) {
            if (dest == id) continue;

            int minCost = distanceVector.get(dest);
            int bestHop = nextHop.getOrDefault(dest, -1);

            for (int neighbor : neighbors.keySet()) {
                Node neighborNode = network.get(neighbor);
                int costToNeighbor = neighbors.get(neighbor);
                int neighborCostToDest = neighborNode.distanceVector.get(dest);

                int newCost = costToNeighbor + neighborCostToDest;
                if (newCost > 16) newCost = 16;

                if (newCost < minCost) {
                    minCost = newCost;
                    bestHop = neighbor;
                    updated = true;
                }
            }

            distanceVector.put(dest, minCost);
            nextHop.put(dest, bestHop);
        }

        return updated;
    }

    public void printTable() {
        System.out.println("Node " + id + " DV Table:");
        System.out.println("Dest | Cost | Next Hop");
        for (int dest : distanceVector.keySet()) {
            System.out.printf("%4d | %4d | %4d\n",
                    dest,
                    distanceVector.get(dest),
                    nextHop.getOrDefault(dest, -1));
        }
        System.out.println();
    }
}