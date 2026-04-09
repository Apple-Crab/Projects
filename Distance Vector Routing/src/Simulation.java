public class Simulation {
    private Network network;
    private int cycles = 0;

    public Simulation(Network network) {
        this.network = network;
    }

    public boolean step() {
        boolean updated = false;

        for (Node node : network.nodes.values()) {
            if (node.updateDV(network.nodes)) {
                updated = true;
            }
        }

        cycles++;
        return updated;
    }

    public void runStepByStep() {
        boolean updated;
        do {
            System.out.println("Cycle: " + cycles);
            network.printAllTables();
            updated = step();

            try {
                System.out.println("Press Enter for next step...");
                System.in.read();
            } catch (Exception e) {}
        } while (updated);

        System.out.println("Stable state reached in " + cycles + " cycles.");
    }

    public void runAutomatic() {
        long start = System.currentTimeMillis();

        boolean updated;
        do {
            updated = step();
        } while (updated);

        long end = System.currentTimeMillis();

        network.printAllTables();
        System.out.println("Stable in " + cycles + " cycles.");
        System.out.println("Time: " + (end - start) + " ms");
    }

    public int getCycles() {
        return cycles;
    }
}