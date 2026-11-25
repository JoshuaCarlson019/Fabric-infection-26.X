package net.hmmer.infection.screen;

import net.hmmer.infection.border.BorderSettings;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public class BorderShrinkSizeSlider extends AbstractSliderButton {

    private static final int STEP = 10;

    public BorderShrinkSizeSlider(int x, int y, int width, int height, double currentSize) {
        super(x, y, width, height, Component.empty(), toSliderValue(currentSize));
        this.value = toSliderValue(getSize());
        this.updateMessage();
    }

    // Border size → 0..1
    private static double toSliderValue(double size) {
        return Mth.clamp((size - BorderSettings.MIN_SHRINK_SIZE)
                / (BorderSettings.MAX_SHRINK_SIZE - BorderSettings.MIN_SHRINK_SIZE), 0.0, 1.0);
    }

    // 0..1 → border size, rounded to the nearest STEP
    public int getSize() {
        double raw = BorderSettings.MIN_SHRINK_SIZE + this.value *
                (BorderSettings.MAX_SHRINK_SIZE - BorderSettings.MIN_SHRINK_SIZE);
        return (int) (Math.round(raw / STEP) * STEP);
    }

    @Override
    protected void updateMessage() {
        this.setMessage(Component.literal("Border shrink size: " + getSize() + " blocks"));
    }

    @Override
    protected void applyValue() {
        this.value = toSliderValue(getSize());
    }
}
