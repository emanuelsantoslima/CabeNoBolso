import view.MainFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.util.Locale;

public class Main {
    public static void main(String[] args) {
        // os valores aparecem sempre em PT/BR
        Locale.setDefault(Locale.forLanguageTag("pt-BR"));

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}