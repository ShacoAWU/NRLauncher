package org.nr.launcher;

import java.awt.*;
import java.awt.image.BufferStrategy;

final class Screen extends Canvas {

    private static final Font font = new Font("Monospaced", 0, 12);

    private final Launcher launcher;

    protected Screen(Launcher launcher) {
        this.launcher = launcher;
        setPreferredSize(new Dimension(350, 70));
        setSize(getPreferredSize());
    }

    protected final void draw() {
        BufferStrategy bufferStrategy;
        if ((bufferStrategy = getBufferStrategy()) == null) {
            createBufferStrategy(3);
            return;
        }
        Graphics canvasGraphics = bufferStrategy.getDrawGraphics();
        canvasGraphics.setFont(font);
        FontMetrics fontMetrics = canvasGraphics.getFontMetrics();
        int progress = this.launcher.getProgress();
        String downloadSize = this.launcher.getText();
        String percentage = "- " + progress + "% -";
        int width = getWidth();
        int height = getHeight();
        canvasGraphics.setColor(getBackground());
        canvasGraphics.fillRect(0, 0, width, height);
        canvasGraphics.setColor(Color.BLACK);
        canvasGraphics.drawString(downloadSize, (width >> 1) - (fontMetrics.stringWidth(downloadSize) >> 1), 16);
        canvasGraphics.drawString(percentage, (width >> 1) - (fontMetrics.stringWidth(percentage) >> 1), 16 + fontMetrics.getHeight());
        canvasGraphics.drawRect(width - 200 >> 1, 40, width - ((width - 200) / 2 << 1), 16);
        canvasGraphics.fillRect(width - 200 >> 1, 41, progress << 1, 15);
        canvasGraphics.dispose();
        bufferStrategy.show();
    }

}
