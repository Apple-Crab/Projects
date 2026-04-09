import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // Make GUI appear on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            DVGui gui = new DVGui();
            gui.setVisible(true);
        });
    }
}