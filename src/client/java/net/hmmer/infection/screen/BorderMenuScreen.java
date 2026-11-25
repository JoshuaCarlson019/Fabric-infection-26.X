package net.hmmer.infection.screen;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class BorderMenuScreen extends InfectionScreen{

    private final Screen parent;
    private final int borderSize;
    private final int borderShrinkSize;
    private final int timeTill;
    private final int time;

    private static int CURRENT_Y; // Starting Y position

    public BorderSizeSlider borderSizeSlider;
    public BorderShrinkSizeSlider borderShrinkSizeSlider;
    public EditBox timeTillShrinkBorder;
    public EditBox timeForBorderToShrink;

    protected BorderMenuScreen(Screen parent, int borderSize, int borderShrinkSize, int timeTill, int time) {
        super(Component.literal("Border Settings Menu"));
        this.parent = parent;
        this.borderSize = borderSize;
        this.borderShrinkSize = borderShrinkSize;
        this.time = time;
        this.timeTill = timeTill;
        CURRENT_Y = 4;
    }

    @Override
    protected void init() {
        if (this.minecraft.getConnection() == null || this.minecraft.player == null) return;
        CURRENT_Y = 4; // resets screen start height

        int centerX = this.width / 2;

        addText("Modify the Border Properties", CURRENT_Y, ChatFormatting.GREEN);

        CURRENT_Y += VERTICAL_SPACING + TEXT_HEIGHT;

        // Basic slider setting
        int sliderWidth = 200;
        int sliderHeight = 20;

        borderSizeSlider = addBorderSizeSlider(CURRENT_Y, sliderWidth, sliderHeight, borderSize);

        CURRENT_Y += VERTICAL_SPACING + sliderHeight;

        borderShrinkSizeSlider = addBorderShrinkSizeSlider(CURRENT_Y,
                sliderWidth, sliderHeight, borderShrinkSize);

        CURRENT_Y += VERTICAL_SPACING + sliderHeight;

        addText("How long to wait before border shrinks (minutes) (0 for wait before shrinking)",
                CURRENT_Y, ChatFormatting.WHITE);

        CURRENT_Y += VERTICAL_SPACING + TEXT_HEIGHT;

        timeTillShrinkBorder = addEditBox("Minutes", CURRENT_Y, BOX_WIDTH, timeTill);
        timeTillShrinkBorder.setMaxLength(2);

        CURRENT_Y += VERTICAL_SPACING + BOX_HEIGHT;

        addText("How long it takes for the border to shrink (minutes) (0 for it to shrink instantly)",
                CURRENT_Y, ChatFormatting.WHITE);

        CURRENT_Y += VERTICAL_SPACING + TEXT_HEIGHT;

        timeForBorderToShrink = addEditBox("Minutes", CURRENT_Y, BOX_WIDTH, time);

        CURRENT_Y += VERTICAL_SPACING + BOX_HEIGHT;

        addButton("Go Back", CURRENT_Y, BOX_WIDTH, button -> {

            InfectMenuScreen.borderSize = borderSizeSlider.getSize();
            InfectMenuScreen.borderShrinkSize = borderShrinkSizeSlider.getSize();
            InfectMenuScreen.timeTill = Integer.parseInt(timeTillShrinkBorder.getValue());
            InfectMenuScreen.time = Integer.parseInt(timeForBorderToShrink.getValue());
            this.minecraft.setScreenAndShow(parent);
        });

    }
}

