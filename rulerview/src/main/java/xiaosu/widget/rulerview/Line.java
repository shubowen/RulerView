package xiaosu.widget.rulerview;

import android.graphics.Color;

/**
 * 疏博文 新建于 16/11/12.
 * 邮箱：shubw@icloud.com
 * 描述：请添加此文件的描述
 */

public class Line {
    public float width;
    public float height;
    public int color = Color.BLACK;
    public String desc;
    public float textSize = 12;
    public int textColor = Color.BLACK;

    public Line(float width, float height, int color) {
        this.width = width;
        this.height = height;
        this.color = color;
    }

    public Line(float width, float height, int color, String lineDesc) {
        this.width = width;
        this.height = height;
        this.color = color;
        this.desc = lineDesc;
    }

    public Line(float width, float height, int color, String lineDesc, float textSize) {
        this.width = width;
        this.height = height;
        this.color = color;
        this.desc = lineDesc;
        this.textSize = textSize;
    }

    public Line(float width, float height, int color, String lineDesc, float textSize, int textColor) {
        this.width = width;
        this.height = height;
        this.color = color;
        this.desc = lineDesc;
        this.textSize = textSize;
        this.textColor = textColor;
    }
}
