import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.interactive.NPCs;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.item.GroundItems;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.NPC;
import org.dreambot.api.wrappers.items.GroundItem;
import sun.rmi.runtime.Log;

import java.util.Arrays;
import java.util.Objects;

import static org.dreambot.api.utilities.Sleep.sleepUntil;

public class CowBot {
    private static final Area COW_AREA = new Area(3265, 3296, 3242, 3283);
    private static final Area BANK_AREA = new Area(3210, 3220, 3207, 3217, 2);
    private static final String[] FOOD_NAMES = {"Trout", "Salmon", "Swordfish", "Tuna", "Lobster"};
    private static final int MIN_START_FOOD = 8;
    private static final int MIN_RUN_ENERGY = 20;
    private static final int MIN_HEALTH_PERCENTAGE = 20;
    private boolean isRunning = false;
    private boolean isBanking = false;
    private boolean isLooting = false;
    private int foodLeft = 0;

    public void StartScript()
    {
        if(!isRunning)
        {
            //check food before script start
            CheckFood();
            //if less food than MIN_START_FOOD and player isn't at the cows yet, than go bank for food.
            if(foodLeft < MIN_START_FOOD && !COW_AREA.contains(Players.getLocal()))
            {
                isBanking = true;
            }
            //set script to running.
            isRunning = true;
        }

        //Check health each game update.
        CheckHealth();
        ToggleRun();

        //If isBanking is true. Go grab food.
        if(isBanking)
        {
            Logger.log("GrabFoodBank()");
            GrabFoodBank();
        }
        if(!Players.getLocal().isAnimating() && !Inventory.isFull() && checkCowhidesGround() && !isLooting)
        {
            Logger.log(checkCowhidesGround());
            Logger.log("GrabCowhides()");
        }

        //If player isn't banking. Attack cow.
        if(!isBanking && !isLooting)
        {
            Logger.log("AttackCow()");
            AttackCow();
        }


    }

    // This method makes the player attack a cow or a cow calf in the COW_AREA
    public void AttackCow()
    {
        // If the player is not in the COW_AREA, walk to a random tile in it and return
        if(!COW_AREA.contains(Players.getLocal()))
        {
            Walking.walk(COW_AREA.getRandomTile());
            return;
        }

        // Find the closest cow or cow calf that is not in combat
        NPC cow = NPCs.closest(n -> (n.getName().equals("Cow") || n.getName().equals("Cow calf"))
                && !n.isInCombat() && COW_AREA.contains(n) && n.getHealthPercent() != 0);

        // If the player is not in combat and not moving, interact with the cow
        if (!Players.getLocal().isInCombat())
        {
            cow.interact("Attack");
            Sleep.sleepUntil(() -> Players.getLocal().isInCombat(), 2000);
        }
    }

    // This method checks the player's health and eats food if needed
    private void CheckHealth()
    {
        // Get the current health percentage of the player
        int health = Players.getLocal().getHealthPercent();

        // If the health is less than or equal to 20%, eat food if available
        if(health <= MIN_HEALTH_PERCENTAGE)
        {
            // If there is food left in the inventory, eat it
            if(foodLeft > 0)
            {
                // Interact with any item that matches the FOOD_NAMES array
                Inventory.interact(item -> Arrays.stream(FOOD_NAMES).anyMatch(item.getName()::equals));
                // Wait until the inventory contains a twisted bow or 1000 milliseconds have passed
                sleepUntil(()-> Inventory.contains("Twisted bow"), 2000);
                // Check the food left in the inventory
                CheckFood();
            }
        }
    }

    //Counts the food items in the inventory by matching their names with FOOD_NAMES
    //Uses stream operations: filter, anyMatch, and count
    void CheckFood()
    {
        foodLeft = (int) Inventory.all().stream()
                .filter(Objects::nonNull)
                .filter(item -> Arrays.stream(FOOD_NAMES).anyMatch(food -> food.equals(item.getName())))
                .count();

        if(foodLeft == 0)
        {
            isBanking = true;
        }
        Logger.log("Food left: " + foodLeft);
    }

    void GrabFoodBank()
    {
        // If the player is not in the bank area, walk to it
        if(!BANK_AREA.contains(Players.getLocal()))
        {
            Walking.walk(BANK_AREA);
        }
        // If the player is in the bank area, open the bank and deposit all items
        if(BANK_AREA.contains(Players.getLocal()))
        {
            Bank.open();
            Bank.depositAllItems();
            // If the bank has any food items, withdraw 28 of them
            if(Bank.contains(item -> Arrays.stream(FOOD_NAMES).anyMatch(item.getName()::equals)))
            {
                Bank.withdraw(item -> Arrays.stream(FOOD_NAMES).anyMatch(item.getName()::equals), 28);
            }
            else{
                Sleep.sleep(10000);
                Logger.log("No more food left.");
            }
            // Check the food amount and set the banking flag to false
            CheckFood();
            isBanking = false;
        }
    }

    boolean checkCowhidesGround(){
        GroundItem item = GroundItems.closest(groundItem -> Players.getLocal().getSurroundingArea(2)
                .contains(groundItem) && groundItem.getName()
                .equals("Cowhide"));
        if(item != null)
        {
            isLooting = true;
            Logger.log("Not Null");
            GrabCowhides(item);
            return true;
        }
        else
        {
            return false;
        }
    }

    void GrabCowhides(GroundItem cowHide)
    {
        // Find the closest cowhide within 2 tiles of the player

        // If there is a cowhide, interact with it to take it
        if(cowHide != null)
        {
            cowHide.interact("Take");
            Sleep.sleepUntil(Inventory::isFull, 2000);
            isLooting = false;
        }

    }

    void ToggleRun()
    {
        if(Walking.getRunEnergy() >= MIN_RUN_ENERGY && !Walking.isRunEnabled())
        {
            Walking.toggleRun();
        }
    }

}
