package net.hmmer.infection.screen;


import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.hmmer.infection.border.BorderRecord;
import net.hmmer.infection.border.BorderSettings;
import net.hmmer.infection.network.InfectPlayerPayload;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;


public class InfectMenuScreen extends InfectionScreen {

    public static int borderSize = BorderSettings.INITIAL_BORDER_SIZE;
    public static int borderShrinkSize = BorderSettings.INITIAL_BORDER_SHRINK_SIZE;
    public static int timeTill = BorderSettings.INITIAL_BORDER_TIME_TILL;
    public static int time = BorderSettings.INITIAL_BORDER_TIME_TO_SHRINK;

    private static int CURRENT_Y; // Starting Y position

    private EditBox xBox, yBox, zBox;
    private StringWidget coordinateText;
    private Button start;
    private Button cancel;
    private BorderSizeSlider slider;
    private Button selectedButton = null;
    private PlayerInfo selectedPlayer = null;

    private int x,y,z;
    private boolean initializedXYZ = false;

    private boolean initializedBorderMenu = false;

    private Button enableZombies = null;

    private UUID playerInfo = null;
    private boolean zombies = false; // false by default, for zombie mode

    public InfectMenuScreen() {
        super(Component.literal("Choose who to infect?"));
        CURRENT_Y = 4;
    }

    @Override
    protected void init() {
        if (this.minecraft.getConnection() == null || this.minecraft.player == null) return; // check player
        CURRENT_Y = 4; // resets screen start height

        int centerX = this.width / 2;

        addText("Start Infection", CURRENT_Y, ChatFormatting.GREEN);

        CURRENT_Y += VERTICAL_SPACING + TEXT_HEIGHT;

        addText("Choose where to start the infection (x, y, z)", CURRENT_Y, ChatFormatting.WHITE);

        CURRENT_Y += VERTICAL_SPACING + TEXT_HEIGHT;

        // Coordinates boxes, prefilled with your current position
        BlockPos pos = this.minecraft.player.blockPosition();
        if (!initializedXYZ) {
            x = pos.getX();
            y = pos.getY();
            z = pos.getZ();
            initializedXYZ = true;
        }

        int startX = centerX - (BOX_WIDTH * 3 + 8) / 2;

        xBox = addEditBox("X", startX, CURRENT_Y, BOX_WIDTH, x);
        yBox = addEditBox("Y", startX + BOX_WIDTH + 4, CURRENT_Y, BOX_WIDTH, y);
        zBox = addEditBox("Z", startX + (BOX_WIDTH + 4) * 2, CURRENT_Y, BOX_WIDTH, z);

        CURRENT_Y += VERTICAL_SPACING + BOX_HEIGHT;

        // button to access border menu

        addButton("Border options", CURRENT_Y, BUTTON_WIDTH, button -> {
            x = Integer.parseInt(xBox.getValue());
            y = Integer.parseInt(yBox.getValue());
            z = Integer.parseInt(zBox.getValue());
            if (!initializedBorderMenu) {
                this.minecraft.setScreenAndShow(new BorderMenuScreen(this, borderSize,
                        borderShrinkSize, timeTill, time));
                initializedBorderMenu = true;
            } else {
                this.minecraft.setScreenAndShow(new BorderMenuScreen(this, borderSize,
                        borderShrinkSize, timeTill, time));
            }
        });

        CURRENT_Y += VERTICAL_SPACING + BUTTON_HEIGHT;

        // Add text for if you want zombies or not
        addText("Enable zombie mode?",  CURRENT_Y, ChatFormatting.WHITE);

        CURRENT_Y += VERTICAL_SPACING + TEXT_HEIGHT;

        enableZombies = addButton("Enable?", CURRENT_Y, BUTTON_WIDTH, (button) -> {
            if (!zombies) {
                enableZombies.setMessage(Component.literal("Disable?"));
                zombies = true;
            } else {
                enableZombies.setMessage(Component.literal("Enable?"));
                zombies = false;
            }
        });

        // Resets enableZombies button
        if (zombies) {
            enableZombies.setMessage(Component.literal("Disable?"));
        } else {
            enableZombies.setMessage(Component.literal("Enable?"));
        }

        CURRENT_Y += VERTICAL_SPACING + BUTTON_HEIGHT;

        // Add text for choosing player
        addText("Choose who the infected player will be?", CURRENT_Y, ChatFormatting.WHITE);

        CURRENT_Y += VERTICAL_SPACING + TEXT_HEIGHT;

        // Buttons
        Minecraft mc = Minecraft.getInstance();
        if (mc.getConnection() == null) return;

        List<PlayerInfo> players = new ArrayList<>(mc.getConnection().getOnlinePlayers());
        if (players.isEmpty()) return;

        players.sort(Comparator.comparing(info -> info.getProfile().name()));

        int amount = players.size();
        if (amount > 3) {
            amount = 3;
        }
        int buttonStartX = centerX - ((BUTTON_WIDTH * amount) + (4 * (amount - 1))) / 2;
        int xPosition = buttonStartX;

        int countForLoop = 0;
        for (PlayerInfo info : players) {
            String name = info.getProfile().name();

            if (countForLoop >= 3) {
                xPosition = buttonStartX;
                CURRENT_Y += SPACING;
            }


            Button btn = addButton(name, xPosition, CURRENT_Y, BUTTON_WIDTH, button -> {

                // Re-enable the previously selected button
                if (selectedButton != null) {
                    selectedButton.active = true;
                }

                // Selected button
                button.active = false;
                selectedButton = button;
                playerInfo = info.getProfile().id();
                selectedPlayer = info;
            });

            if (info.equals(selectedPlayer)) {
                btn.active = false;
                selectedButton = btn;
            }

            if (countForLoop >= 3) { // Important for something, I forgot, yeah
                countForLoop = 0;
            } else {
                xPosition += BUTTON_WIDTH + 4;
            }
            countForLoop++;
        }

        CURRENT_Y += VERTICAL_SPACING + BUTTON_HEIGHT;

        coordinateText = addErrorText(centerX, CURRENT_Y);

        // Start Button ==============================================================
        start = addButton("Start?", CURRENT_Y, BUTTON_WIDTH, button -> {
            BlockPos positon = readCoords();
            if (positon == null) return;

            // sends client info to the server
            // If playerInfo is null it will choose a random player to be infected, this will be right here,
            // can't send a null playerInfo in the packet, the game won't recognize that.

            // gets random player on the server if no one is selected
            if (playerInfo == null) {
                PlayerInfo random = players.get(mc.level.getRandom().nextInt(players.size()));
                playerInfo = random.getProfile().id();
            }

            ClientPlayNetworking.send(new InfectPlayerPayload(playerInfo, positon,
                    new BorderRecord(borderSize, borderShrinkSize, timeTill, time)
                    , zombies));
            this.onClose();
        });

        CURRENT_Y += VERTICAL_SPACING + BUTTON_HEIGHT;

        // Cancel Button ==============================================================
        cancel = addButton("Cancel", CURRENT_Y, BUTTON_WIDTH, button -> {
            this.onClose();
        });
    }

    private BlockPos readCoords() {
        int centerX = this.width / 2;
        try {
            int x = Integer.parseInt(xBox.getValue());
            int y = Integer.parseInt(yBox.getValue());
            int z = Integer.parseInt(zBox.getValue());
            return new BlockPos(x, y, z);
        } catch (NumberFormatException e) {
            setErrorMessage("Enter whole numbers for the infection location (X, Y, Z)");
            return null;
        }
    }

    private void setErrorMessage(String message) {
        int centerX = this.width / 2;

        if (coordinateText.getMessage().equals(Component.empty())) {
            CURRENT_Y -= BUTTON_HEIGHT;
            CURRENT_Y += TEXT_HEIGHT;
            start.setY(CURRENT_Y);
            CURRENT_Y += VERTICAL_SPACING + BUTTON_HEIGHT;
            cancel.setY(CURRENT_Y);
        }

        coordinateText.setMessage(Component.
                literal(message).withStyle(ChatFormatting.RED));
        coordinateText.setX(centerX - (this.minecraft.font.width(coordinateText.getMessage()) / 2));

    }
}
