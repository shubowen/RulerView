package xiaosu.widget.rulerview;

/**
 * 疏博文 新建于 16/11/12.
 * 邮箱：shubw@icloud.com
 * 描述：实现该接口来自定义刻度线样式
 */

public interface ILineCreator {

    Line getLine(int index, int parentHeight, float density);

}
