package com.imdp.instaclimb;

import android.content.Context;
import android.graphics.*;
import android.widget.ImageView;

/**
 * Created by massimo on 6/25/14.
 */
public class ClimbInfoView extends ImageView {

  private Paint   m_Paint = null;
  private String  m_AscentName = "";
  private String  m_Location = "";
  private RectF   m_FeelsLikeRect = new RectF(0, 0, 0, 0);
  private int     m_SquareSide = 0;
  private boolean m_Measured = false;

  public boolean isMeasured() {
    return m_Measured;
  }

  public int getSquareSide() { return m_SquareSide; }

  public void setSquareSide(int ss) {
//    setLeft(left);
//    setTop(top);
//    setRight(right);
//    setBottom(bottom);

    m_SquareSide = ss;
    m_FeelsLikeRect.set(20, ss-120, ss-20, ss-20);
    m_Measured = true;
  }

  public void setAscentName(String m_AscentName) {
    this.m_AscentName = m_AscentName;
  }

  public void setLocation(String m_Location) {
    this.m_Location = m_Location;
  }

  public ClimbInfoView(Context context) {
    super(context);
    m_Paint = new Paint(Paint.ANTI_ALIAS_FLAG);
  }

  public ClimbInfoView(Context context, String ascentName, String location) {
    super(context);

    m_AscentName = ascentName;
    m_Location = location;
    m_Paint = new Paint(Paint.ANTI_ALIAS_FLAG);
  }

  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);

    if (!m_Measured)
      return;

    m_Paint.setColor(Color.GRAY);
    m_Paint.setAlpha(128);
    canvas.drawRoundRect(m_FeelsLikeRect, 10, 10, m_Paint);

    m_Paint.setColor(Color.WHITE);
    m_Paint.setTextSize(64);

    m_Paint.setTypeface(Typeface.create("sans-serif", Typeface.BOLD_ITALIC));
    m_Paint.setShadowLayer(5f, 5f, 5f, Color.BLACK);

    canvas.drawText(m_AscentName, 100, 100, m_Paint);
    canvas.drawText("Feels like 7b+", m_FeelsLikeRect.left + 10, m_FeelsLikeRect.top + 100, m_Paint);
  }

}