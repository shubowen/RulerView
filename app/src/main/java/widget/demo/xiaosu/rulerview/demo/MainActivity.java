package widget.demo.xiaosu.rulerview.demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.LinearLayout;
import android.widget.TextView;

import xiaosu.widget.rulerview.RulerView;

public class MainActivity extends AppCompatActivity implements RulerView.OnValueChangedListener {

    RulerView mRulerView;
    TextView mTvValue;
    LinearLayout mActivityMain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findView();
        mRulerView.setOnValueChangedListener(this);
    }

    private void findView() {
        mRulerView = (RulerView) findViewById(R.id.rulerView);
        mTvValue = (TextView) findViewById(R.id.tv_value);
        mActivityMain = (LinearLayout) findViewById(R.id.activity_main);
    }

    @Override
    public void onValueChanged(float value) {
        mTvValue.setText(Float.toString(value));
    }
}
