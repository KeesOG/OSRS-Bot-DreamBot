import org.dreambot.api.data.GameState;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.SkillTracker;
import org.dreambot.api.randoms.BreakSolver;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.script.listener.ExperienceListener;
import org.dreambot.api.script.listener.PaintListener;
import org.dreambot.api.utilities.Logger;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;
import java.util.Random;

@ScriptManifest(category = Category.WOODCUTTING, name = "TestBot", author = "KeesOG", version = 1.0f)
public class Main extends AbstractScript implements PaintListener, ExperienceListener {
    Random random = new Random();
    CowBot cowBot = new CowBot();
    private static final Font xpFont = new Font("Arial", Font.BOLD, 24);
    private static final Color xpColor = new Color(224, 141, 24);
    private static final Color xpColor2 = new Color(133, 112, 255);
    private static final BasicStroke bs = new BasicStroke(4);
    private boolean isRunning = false;
    private int randomNumber;
    private final BufferedImage logoImage;
    {
        try
        {
            logoImage = ImageIO.read(Objects.requireNonNull(getClass().getResource("logo.jpg")));
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onStart()
    {
        randomNumber = 5000 + random.nextInt(15000);
        Logger.log("Started.");
        Logger.log("Random number: " + randomNumber);
    }

    @Override
    public int onLoop()
    {
        if(!isRunning)
        {
            GameState state = org.dreambot.api.Client.getGameState();
            Logger.log(state);
            if(state == GameState.LOGGED_IN)
            {
                SkillTracker.start();
                Logger.log("Logged in");
                isRunning = true;
            }
            else
            {
                Logger.log("Not logged in.");
            }
            return 1000;
        }
        if(SkillTracker.getGainedExperience(Skill.ATTACK) >= randomNumber)
        {
            cowBot.isBanking = true;
            SkillTracker.reset(Skill.ATTACK);
            randomNumber = 5000 + random.nextInt(15000);
        }
        cowBot.StartScript();
        return 500 + random.nextInt(1000);
    }

    @Override
    public void onExit()
    {
        Logger.log("Quiting.");
    }

    @Override
    public void onPaint(Graphics2D g){
        g.setColor(xpColor2);
        g.setStroke(bs);
        g.drawRect(0, 336, 520 ,164);
        g.setColor(Color.black);
        g.fillRect(3, 339, 517 ,160);
        g.setFont(xpFont);
        g.setColor(xpColor2);
        g.drawString(String.format("%d", SkillTracker.getGainedExperiencePerHour(Skill.ATTACK)), 280, 380);
        g.drawString("Attack xp/hr: ", 50, 380);
        g.drawString(String.format("%d", SkillTracker.getGainedExperiencePerHour(Skill.STRENGTH)), 280, 410);
        g.drawString("Strength xp/hr: ", 50, 410);
        g.drawString(String.format("%d", SkillTracker.getGainedExperiencePerHour(Skill.DEFENCE)), 280, 440);
        g.drawString("Defence xp/hr: ", 50, 440);
        g.drawString(String.format("%d", SkillTracker.getGainedExperience(Skill.ATTACK)), 280, 470);
        g.drawString("Xp gained: ", 50, 470);
        g.drawImage(logoImage, 345, 338, 170, 156, null);
    }


}