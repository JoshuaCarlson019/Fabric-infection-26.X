package net.hmmer.infection.screen;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public abstract class InfectionScreen extends Screen {

    protected InfectionScreen(Component title) {
        super(title);
    }

    /**
     * Creates text
     * @param x the horizontal position
     * @param y the vertical position
     * @param label what the text says
     * @param color the color of the text
     * @return this StringWidget
     */
    protected StringWidget addText(String label, int x, int y, ChatFormatting color) {
        return this.addRenderableWidget(new StringWidget(x, y, this.width, 12,
                Component.literal(label).withStyle(color), this.font));
    }

    /**
     * Creates text, centered
     * @param y the vertical position
     * @param label what the text says
     * @param color the color of the text
     * @return this StringWidget
     */
    protected StringWidget addText(String label, int y, ChatFormatting color) {
        return this.addRenderableWidget(new StringWidget(getCenterX(this.width, label),
                y, this.width, 12, Component.literal(label).withStyle(color), this.font));
    }

    /**
     * Creates error text in the middle of the screen, starts off empty
     * @param x the horizontal position
     * @param y the vertical position
     * @return this StringWidget
     */
    protected StringWidget addErrorText(int x, int y) {
        return this.addRenderableWidget(new StringWidget(x, y, this.width, 12,
                Component.empty(), this.font));
    }

    /**
     * Creates an editBox which can be edited
     * @param label this what the editBox will be named
     * @param x the horizontal position
     * @param y the vertical position
     * @param startValue this is the initial value the editBox will start with
     * @return this EditBox
     */
    protected EditBox addEditBox(String label, int x, int y, int width, int startValue) {
        EditBox box = new EditBox(this.font, x, y, width, 20, Component.literal(label));
        box.setMaxLength(9);
        box.setValue(String.valueOf(startValue));
        box.setHint(Component.literal(label).withStyle(ChatFormatting.DARK_GRAY));
        box.setResponder(text -> {
            String cleaned =
                    text.replaceAll("[^0-9-]", ""); // remove anything that isn't numbers
            if (!cleaned.equals(text)) box.setValue(cleaned);
        });
        return this.addRenderableWidget(box);
    }

    /**
     * Creates a new BorderSizeSlider which is a slider for the border size
     * @param x the horizontal position
     * @param y the vertical position
     * @param sliderWidth width of the slider
     * @param sliderHeight height of the slider
     * @param startingValue initial value the slider starts at, typically 500
     * @return the new slider widget
     */
    protected BorderSizeSlider addBorderSizeSlider(int x,int y, int sliderWidth,
                                                   int sliderHeight, int startingValue) {
        return this.addRenderableWidget(new BorderSizeSlider(x, y, sliderWidth,
                sliderHeight, startingValue));
    }

    /**
     * Creates a new BorderSizeSlider which is a slider for the border size, centered
     * @param y the vertical position
     * @param sliderWidth width of the slider
     * @param sliderHeight height of the slider
     * @param startingValue initial value the slider starts at, typically 500
     * @return the new slider widget
     */
    protected BorderSizeSlider addBorderSizeSlider(int y, int sliderWidth,
                                                   int sliderHeight, int startingValue) {
        return this.addRenderableWidget(
                new BorderSizeSlider(getCenterX(this.width, sliderWidth),
                y, sliderWidth, sliderHeight, startingValue));
    }

    /**
     * Creates a simple button
     * @param label this what the button will be named
     * @param x the horizontal position
     * @param y the vertical position
     * @param width width of the button
     * @param onPress defines what happens when pressed
     * @return the button
     */
    protected Button addButton(String label, int x, int y, int width, Button.OnPress onPress) {
        return this.addRenderableWidget(Button.builder(Component.literal(label),
                onPress)
                .bounds(x, y, width, 20)
                .build());
    }

    /**
     * Creates a simple button, centered
     * @param label this what the button will be named
     * @param y the vertical position
     * @param width width of the button
     * @param onPress defines what happens when pressed
     * @return the button
     */
    protected Button addButton(String label, int y, int width, Button.OnPress onPress) {
        return this.addRenderableWidget(Button.builder(Component.literal(label),
                        onPress)
                .bounds(getCenterX(this.width, width), y, width, 20)
                .build());
    }

    /**
     * Puts the ui in the center of the screen also formatted to the center
     * @param width width of the screen
     * @param length length of the ui item
     * @return the position
     */
    private static int getCenterX(int width, int length) {
        return (width / 2) - (length / 2);
    }


    /**
     * getCenterX, gets the center of the screen and subtracks the width of the font of the text to get the true center
     * @param width width of the screen
     * @param label the label, the string for the text or something like that
     * @return the true center
     */
    private int getCenterX(int width, String label) {
        return (width / 2) - (this.minecraft.font.width(Component.literal(label)) / 2);
    }

    // These screens aren't pause screens
    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
