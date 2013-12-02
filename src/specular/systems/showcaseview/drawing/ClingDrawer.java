package specular.systems.showcaseview.drawing;

import android.graphics.Canvas;

import specular.systems.showcaseview.utils.ShowcaseAreaCalculator;

/**
 * Created by curraa01 on 13/10/2013.
 */
public interface ClingDrawer extends ShowcaseAreaCalculator {

    void drawShowcase(Canvas canvas, float x, float y, float scaleMultiplier, float radius);

    int getShowcaseWidth();

    int getShowcaseHeight();

}
