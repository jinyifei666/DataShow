package com.yrish.datashow;

import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import lecho.lib.hellocharts.gesture.ContainerScrollType;
import lecho.lib.hellocharts.gesture.ZoomType;
import lecho.lib.hellocharts.listener.LineChartOnValueSelectListener;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.ValueShape;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.view.LineChartView;

public class DataShowActivity extends AppCompatActivity {

    private SwipeRefreshLayout swiperefreshlayout;//下拉刷新空间
    private Handler handler;
    private LineChartView chart;
    private List<PointValue> mPointValues;
    private List<AxisValue> mAxisXValues;
    private int length;
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_show);
        handler=new Handler();
        initView();
        getdata();
    }

    private void initView() {
        textView=(TextView)findViewById(R.id.text);
        swiperefreshlayout=(SwipeRefreshLayout)findViewById(R.id.swipelayout);
        swiperefreshlayout.setProgressBackgroundColorSchemeColor(getResources().getColor(R.color.white));

        //设置刷新时动画的颜色，可以设置4个
        swiperefreshlayout.setColorSchemeResources(android.R.color.holo_blue_light,
                android.R.color.holo_red_light,android.R.color.holo_orange_light,
                android.R.color.holo_green_light);
        swiperefreshlayout.setRefreshing(true);
        swiperefreshlayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swiperefreshlayout.setRefreshing(true);
                getdata();
            }
        });
        chart = (LineChartView) findViewById(R.id.linechart);
        chart.setOnValueTouchListener(new ValueTouchListener());
    }


    public void getdata(){
        AsyncHttpClient client=new AsyncHttpClient();
        RequestParams params=new RequestParams();
        params.put("deviceId",1);
        params.put("num",20);
        PersistentCookieStore myCookieStore = new PersistentCookieStore(this);
        client.setCookieStore(myCookieStore);
        client.get("http://hopes.yrish.com/getdata",params,new JsonHttpResponseHandler(){
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.w("rp",response.toString());
                String state=null;
                try {
                    state=response.getString("state");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (state.equals("0")|state.equals("1")){
                    textView.setVisibility(View.GONE);
                    chart.setVisibility(View.VISIBLE);
                    try {
                        JSONArray array=response.getJSONArray("result");
                        length=array.length();
                        mPointValues = new ArrayList<PointValue>();
                        mAxisXValues = new ArrayList<AxisValue>();
                        for (int i=0;i<length;i++){
                            JSONObject obj=array.getJSONObject(i);
                            mAxisXValues.add(new AxisValue(i).setLabel(String.valueOf(i)));
                            mPointValues.add(new PointValue(i, (float) obj.getDouble("gasAmount")));
                            initLineChart();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
                else {
                    chart.setVisibility(View.GONE);
                    textView.setText("暂时没有数据");
                    textView.setVisibility(View.VISIBLE);
                }
            }
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.w("JsonHttpRH", responseString);
            }
        });
    }





    private void initLineChart() {
        Line line = new Line(mPointValues).setColor(Color.parseColor("#33B5E5"));  //折线的颜色
        List<Line> lines = new ArrayList<Line>();
        line.setShape(ValueShape.CIRCLE);//折线图上每个数据点的形状  这里是圆形 （有三种 ：ValueShape.SQUARE  ValueShape.CIRCLE  ValueShape.DIAMOND）
        line.setCubic(true);//曲线是否平滑，即是曲线还是折线
        line.setFilled(false);//是否填充曲线的面积
        line.setHasLabels(true);//曲线的数据坐标是否加上备注
//      line.setHasLabelsOnlyForSelected(true);//点击数据坐标提示数据（设置了这个line.setHasLabels(true);就无效）
        line.setHasLines(true);//是否用线显示。如果为false 则没有曲线只有点显示
        line.setHasPoints(true);//是否显示圆点 如果为false 则没有原点只有点显示（每个数据点都是个大的圆点）
        lines.add(line);
        LineChartData data = new LineChartData();
        data.setLines(lines);

        //坐标轴
        Axis axisX = new Axis(); //X轴
        axisX.setHasTiltedLabels(false);  //X坐标轴字体是斜的显示还是直的，true是斜的显示
        axisX.setTextColor(Color.GRAY);  //设置字体颜色
        axisX.setName("气体监测");  //表格名称
        axisX.setTextSize(10);//设置字体大小
        axisX.setMaxLabelChars(1); //最多几个X轴坐标，意思就是你的缩放让X轴上数据的个数7<=x<=mAxisXValues.length
        axisX.setValues(mAxisXValues);  //填充X轴的坐标名称
        data.setAxisXBottom(axisX); //x 轴在底部
        //data.setAxisXTop(axisX);  //x 轴在顶部
        axisX.setHasLines(true); //x 轴分割线

        // Y轴是根据数据的大小自动设置Y轴上限(在下面我会给出固定Y轴数据个数的解决方案)
        Axis axisY = new Axis();  //Y轴
        //axisY.setHasLines(true);//设置网格线
        List<AxisValue> values = new ArrayList<>();
        for (int i = 0; i < 100; i += 10) {
            AxisValue value = new AxisValue(i);
            String label = i + "";
            value.setLabel(label);
            values.add(value);
        }
        axisY.setValues(values);//设置Y轴坐标
        axisY.setName("gas含量");//y轴标注
        axisY.setTextColor(Color.GRAY);
        axisY.setTextSize(10);//设置字体大小
        data.setAxisYLeft(axisY);  //Y轴设置在左边
        //data.setAxisYRight(axisY);  //y轴设置在右边

        //设置行为属性，支持缩放、滑动以及平移
        chart.setInteractive(true);
        chart.setZoomType(ZoomType.HORIZONTAL);//设置放大缩小方向
        chart.setMaxZoom((float) 2);//最大放大比例
        chart.setContainerScrollEnabled(false, ContainerScrollType.HORIZONTAL);
        chart.setLineChartData(data);
        chart.setVisibility(View.VISIBLE);
        /**
         * 当时是为了解决X轴固定数据个数。见（http://forum.xda-developers.com/tools/programming/library-hellocharts-charting-library-t2904456/page2）;
         * 设置X轴坐标个数（right）
         */
        Viewport v = new Viewport(chart.getMaximumViewport());
        v.left = 0;
        v.right = length;
        chart.setCurrentViewport(v);
        swiperefreshlayout.setRefreshing(false);
    }

    private class ValueTouchListener implements LineChartOnValueSelectListener {
        @Override
        public void onValueSelected(int lineIndex, int pointIndex, PointValue value) {
            Toast.makeText(DataShowActivity.this, "Selected: " + value, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onValueDeselected() {
        }
    }
}
