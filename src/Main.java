import org.dreambot.api.Client;
import org.dreambot.api.data.GameState;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.SkillTracker;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.script.listener.ExperienceListener;
import org.dreambot.api.script.listener.PaintListener;
import org.dreambot.api.utilities.Logger;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.Random;


@ScriptManifest(category = Category.WOODCUTTING, name = "TestBot", author = "KeesOG", version = 1.0f)
public class Main extends AbstractScript implements PaintListener, ExperienceListener {
    Random random = new Random();
    CowBot cowBot = new CowBot();

    Font xpFont = new Font("Arial", Font.BOLD, 24);
    Color xpColor = new Color(224, 141, 24);
    BufferedImage logoImage = ImageIO.read(getClass().getResource("logo.jpg"));
    boolean isRunning = false;

    public Main() throws IOException {
    }

    @Override
    public void onStart(){

        Logger.log("Started.");
    }
    @Override
    public int onLoop(){
        if(!isRunning){
            GameState state = org.dreambot.api.Client.getGameState();
            Logger.log(state);
            if(state == GameState.LOGGED_IN) {
                SkillTracker.start();
                Logger.log("Logged in");
                isRunning = true;
                return 1000;
            }
            else{
                Logger.log("Not logged in.");
                return 1000;
            }
        }

        cowBot.StartScript();

        return 500 + random.nextInt(1000);
    }
    @Override
    public void onExit(){
        Logger.log("Quiting.");
    }

    @Override
    public void onPaint(Graphics2D g){

        g.setColor(Color.black);
        g.fillRect(0, 338, 520 ,162);
        g.setFont(xpFont);
        g.setColor(xpColor);
        g.drawString(String.format("%d", SkillTracker.getGainedExperiencePerHour(Skill.ATTACK)), 280, 380);
        g.drawString("Attack xp/hr: ", 50, 380);
        g.drawString(String.format("%d", SkillTracker.getGainedExperiencePerHour(Skill.STRENGTH)), 280, 410);
        g.drawString("Strength xp/hr: ", 50, 410);
        g.drawString(String.format("%d", SkillTracker.getGainedExperiencePerHour(Skill.DEFENCE)), 280, 440);
        g.drawString("Defence xp/hr: ", 50, 440);
        g.drawImage(logoImage, 345, 338, 170, 156, null);

    }
}