package code;
import java.util.Locale;

public class Main {
    public static void main(String[] args) {
        
        Locale userLocale = Locale.getDefault();
        String language = userLocale.getLanguage();

        new Window(language);

    }
}
