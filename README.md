# RulerView
Android自定义直尺  

![image](https://github.com/shubowen/RulerView/blob/master/image/ruler.gif)

自定义属性：

    <declare-styleable name="RulerView">
            <!--刻度线之间间距的个数-->
            <attr name="spaceCount" format="integer"/>
            <!--刻度线之间间距的宽-->
            <attr name="spaceWidth" format="dimension"/>
            <!--文字和刻度线之间的距离-->
            <attr name="gapBetweenLineAndText" format="dimension"/>
            <!--中间指针的颜色-->
            <attr name="pointerColor" format="color"/>
            <!--中间指针的宽-->
            <attr name="pointerWidth" format="dimension"/>
            <!--自定义ILineCreator全路径-->
            <attr name="lineCreator" format="string"/>
            <!--默认显示刻度的角标位置-->
            <attr name="startIndex" format="float"/>
            <!--滑动结束是否停止在最近刻度线上-->
            <attr name="pauseOnNearestIndex" format="boolean"/>
    </declare-styleable>

**已解耦刻度线样式**
    
    如需自定义刻度线和刻度线上的文字，请仿RulerView$DefaultLineCreator实现ILineCreator