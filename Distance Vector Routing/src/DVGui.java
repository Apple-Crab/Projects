import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Distance Vector Routing GUI
 * Features:
 * - Select input file
 * - Step through DV algorithm
 * - Run automatically
 * - Change link cost dynamically
 * - Reset / load new network
 * - Detect stable state and display cycles/time
 */
public class DVGui extends JFrame {

    private Network network;               // Holds all nodes and links
    private Simulation simulation;         // Handles DV algorithm steps
    private Map<Integer, DefaultTableModel> tableModels; // Node DV table models
    private JLabel statusLabel;            // Displays cycle / stable state info

    public DVGui() {
        tableModels = new HashMap<>();
        setupGui();
        promptAndLoadNetwork();
    }

    /**
     * Setup the main GUI window
     */
    private void setupGui() {
        setTitle("Distance Vector Routing Simulation");
        setSize(1000, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10)); // spacing between regions
        ((JComponent) getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Status label
        statusLabel = new JLabel("No network loaded");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        add(statusLabel, BorderLayout.NORTH);
    }

    /**
     * Prompt user to enter a network file name and load it
     */
    private void promptAndLoadNetwork() {
        String filename = JOptionPane.showInputDialog(this, "Enter network file name:", "input.txt");
        if (filename == null || filename.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "No file entered. Exiting.");
            System.exit(0);
        }

        try {
            network = new Network();
            network.loadFromFile(filename.trim());
            simulation = new Simulation(network);

            setupTables();
            setupControls();
            refreshTables();
            statusLabel.setText("Cycle: 0");

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading file: " + e.getMessage());
            System.exit(0);
        }
    }

    /**
     * Setup tables for all nodes
     */
    private void setupTables() {
        JPanel tablesPanel = new JPanel();
        tablesPanel.setLayout(new GridLayout(0, 2, 10, 10)); // 2 columns, gaps between tables
        tableModels.clear();

        for (Node node : network.nodes.values()) {
            tablesPanel.add(createNodePanel(node));
        }

        add(tablesPanel, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    /**
     * Setup control buttons: Step, Run Auto, Change Link Cost, Reset
     */
    private void setupControls() {
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));

        JButton stepButton = new JButton("Step");
        JButton autoButton = new JButton("Run Auto");
        JButton changeLinkButton = new JButton("Change Link Cost");
        JButton resetButton = new JButton("Reset / New File");

        stepButton.addActionListener(this::handleStep);
        autoButton.addActionListener(this::handleAuto);
        changeLinkButton.addActionListener(e -> handleChangeLink());
        resetButton.addActionListener(e -> resetSimulation());

        controlPanel.add(stepButton);
        controlPanel.add(autoButton);
        controlPanel.add(changeLinkButton);
        controlPanel.add(resetButton);

        add(controlPanel, BorderLayout.SOUTH);
        revalidate();
        repaint();
    }

    /**
     * Create a panel containing a table for a node's DV table
     */
    private JPanel createNodePanel(Node node) {
        String[] columns = {"Dest", "Cost", "Next Hop"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        tableModels.put(node.id, model);

        JTable table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Node " + node.id));
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Refresh all DV tables in the GUI
     */
    private void refreshTables() {
        if (network == null) return;

        for (Node node : network.nodes.values()) {
            DefaultTableModel model = tableModels.get(node.id);
            model.setRowCount(0);

            for (int dest : node.distanceVector.keySet()) {
                model.addRow(new Object[]{
                        dest,
                        node.distanceVector.get(dest),
                        node.nextHop.getOrDefault(dest, 0)
                });
            }
        }
    }

    /**
     * Handle single-step execution
     */
    private void handleStep(ActionEvent e) {
        if (simulation == null) return;

        boolean updated = simulation.step();
        refreshTables();
        statusLabel.setText("Cycle: " + simulation.getCycles());

        if (!updated) {
            statusLabel.setText("Stable state reached in " + simulation.getCycles() + " cycles");
        }
    }

    /**
     * Handle automatic run until stable state
     */
    private void handleAuto(ActionEvent e) {
        if (simulation == null) return;

        new Thread(() -> {
            long start = System.currentTimeMillis();
            boolean updated;
            do {
                updated = simulation.step();

                SwingUtilities.invokeLater(this::refreshTables);

                try {
                    Thread.sleep(500); // slow down for visibility
                } catch (InterruptedException ignored) {}

            } while (updated);

            long end = System.currentTimeMillis();
            SwingUtilities.invokeLater(() ->
                    statusLabel.setText("Stable state reached in " + simulation.getCycles() +
                            " cycles (Total time: " + (end - start) + " ms)")
            );
        }).start();
    }

    /**
     * Handle changing a link cost dynamically
     */
    private void handleChangeLink() {
        String input = JOptionPane.showInputDialog(this,
                "Enter: node1 node2 newCost (16 = infinity)");
        if (input == null) return;

        try {
            String[] parts = input.trim().split("\\s+");
            int n1 = Integer.parseInt(parts[0]);
            int n2 = Integer.parseInt(parts[1]);
            int cost = Integer.parseInt(parts[2]);

            network.updateLink(n1, n2, cost);
            refreshTables();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Invalid input. Format: node1 node2 cost");
        }
    }

    /**
     * Reset simulation and prompt for new network file
     */
    private void resetSimulation() {
        getContentPane().removeAll();
        tableModels.clear();
        setupGui();
        promptAndLoadNetwork();
    }
}