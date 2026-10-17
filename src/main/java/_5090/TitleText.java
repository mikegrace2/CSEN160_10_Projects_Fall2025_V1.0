package _5090;

import javafx.animation.FadeTransition;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;

public class TitleText {

    private Text text; // JavaFX text

    public TitleText(String content, double width, double height) {
        text = new Text(content);
        text.setFill(Color.YELLOW);
        text.setFont(new Font(60));
        text.setTranslateX(width / 2 - 350);
        text.setTranslateY(height / 2);
        text.setEffect(new DropShadow(5, Color.rgb(255, 255, 127, 0.9))); // Glow effect
        text.setOpacity(0); // start invisible
    }

    public Text get() {
        return text;
    } // needed to add into group

    // fade in
    public FadeTransition fadeIn(double durationSec) {
        FadeTransition fade = new FadeTransition(Duration.seconds(durationSec), text);
        fade.setFromValue(0.0);
        fade.setToValue(1.0);
        return fade;
    }

    // stay
    public FadeTransition stay(double durationSec) {
        FadeTransition fade = new FadeTransition(Duration.seconds(durationSec), text);
        fade.setFromValue(1.0);
        fade.setToValue(1.0);
        return fade;
    }

    // fade out
    public FadeTransition fadeOut(double durationSec) {
        FadeTransition fade = new FadeTransition(Duration.seconds(durationSec), text);
        fade.setFromValue(1.0);
        fade.setToValue(0.0);
        return fade;
    }
}
