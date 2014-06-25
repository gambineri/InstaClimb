package com.imdp.instaclimb;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.widget.ImageView;

/**
 * Created by massimo on 6/25/14.
 */
public class ClimbInfoView extends ImageView {

  Paint m_Paint = null;

  public ClimbInfoView(Context context) {
    super(context);

    m_Paint = new Paint(Paint.ANTI_ALIAS_FLAG);
  }

  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);

    m_Paint.setColor(Color.WHITE);
    m_Paint.setTextSize(72);

    m_Paint.setTypeface(Typeface.create("Verdana", Typeface.BOLD_ITALIC));

    canvas.drawText("Feels like 7b+", 100, 100, m_Paint);
  }

}