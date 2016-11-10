package com.xuweather.app.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.xuweather.R;
import com.xuweather.app.model.City;
import com.xuweather.app.model.County;
import com.xuweather.app.model.Province;
import com.xuweather.app.model.XuWeatherDB;
import com.xuweather.app.util.HttpUtil;
import com.xuweather.app.util.Utility;
import com.xuweather.app.util.HttpUtil.HttpCallbackListener;

public class ChooseAreaActvity extends Activity {
	
	public static final int LEVEL_PROVINCE = 0;
	public static final int LEVEL_CITY = 1;
	public static final int LEVEL_COUNTY =2;
	
	private ProgressDialog progressDialog;
	private TextView titleText;
	private ListView listView;
	private ArrayAdapter<String> adapter;
	private XuWeatherDB xuWeatherDB;
	private List<String> datalist = new ArrayList<String>();
	/**
	 * ʡ�б�
	 */
	private List<Province> provinceList;
	/**
	 * ��
	 */
	private List<City> cityList;
	/**
	 * ��
	 */
	private List<County> countyList;
	/**
	 * ѡ�е�ʡ��
	 */
	private Province selectedProvince;
	/**
	 * ѡ�еĳ���
	 */
	private City selectedCity;
	/**
	 * ��ǰѡ�е� ����
	 */
	private int currentLevel;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.choose_area);
		listView = (ListView) findViewById(R.id.list_view);
		titleText = (TextView) findViewById(R.id.title_text);
		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,datalist);
		listView.setAdapter(adapter);
		xuWeatherDB = XuWeatherDB.getInstance(this);
		listView.setOnItemClickListener(new OnItemClickListener()  {

			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int index,
					long arg3) {
				// TODO Auto-generated method stub
				if (currentLevel == LEVEL_PROVINCE) {
					selectedProvince = provinceList.get(index);
					queryCities();
				} else if (currentLevel == LEVEL_CITY) {
					selectedCity = cityList.get(index);
					queryCounties();
				}
			}	
		});
		queryProvinces(); //����ʡ�����ݡ�
	}

	private void queryProvinces() {
		provinceList = xuWeatherDB.loadProvince();
		if (provinceList.size() >0) {
			datalist.clear();
			for(Province province:provinceList) {
				datalist.add(province.getProvinceName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText("�й�");
			currentLevel = LEVEL_PROVINCE;
		}else {
			queryFromServer(null,"province");
		}
	}
		/**
		 * ��ѯ��
		 */

		private void queryCities() {
				// TODO Auto-generated method stub
				cityList = xuWeatherDB.loadCities(selectedProvince.getId());
				if (cityList.size() >0) {
					datalist.clear();
					for(City city:cityList) {
						datalist.add(city.getCityName());
					}
					adapter.notifyDataSetChanged();
					listView.setSelection(0);
					titleText.setText(selectedProvince.getProvinceName());
					currentLevel = LEVEL_CITY;
				} else {
					queryFromServer(selectedProvince.getProvinceCode(),"city");
				}
			}
	private void queryCounties() {
				// TODO Auto-generated method stub
		countyList = xuWeatherDB.loadCounties(selectedCity.getId());
		if (countyList.size() >0) {
			datalist.clear();
			for(County county:countyList) {
				datalist.add(county.getCountyName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText(selectedCity.getCityName());
			currentLevel = LEVEL_COUNTY;
		} else {
			queryFromServer(selectedCity.getCityCode(),"county");
		}
			}
	/**
	 * �ӷ�������ѯʡ��������
	 * @param cityCode
	 * @param string
	 */
		private void queryFromServer(final String code, final String type) {
			String address;
			if (!TextUtils.isEmpty(code)) {
				address = "http://www.weather.com.cn/data/list3/city"+ code +".xml";
			}else {
				address = "http://http://www.weather.com.cn/data/list3/city.xml";
			}
			showProgressDialog();
			HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
				
				@Override
				public void onFinsh(String response) {
					boolean result = false;
					if ("province".equals(type)) {
						result = Utility.handleProvincesResponse(xuWeatherDB, response);
					}else if ("city".equals(type)) {
						result = Utility.handleCitiesResponse(xuWeatherDB,
								response, selectedProvince.getId());
					}else if ("county".equals(type)) {
						result = Utility.handleCountiesResponse(xuWeatherDB,
								response, selectedCity.getId());
					}
					if (result) {
						// ͨ��runOnUiThread���������ص����̴߳����߼�
						runOnUiThread(new Runnable() {
							
							@Override
							public void run() {
								// TODO Auto-generated method stub
								closeProgressDialog();
								if ("province".equals(type)) {
									queryProvinces();
								}else if("city".equals(type)) {
									queryCities();
								}else if ("county".equals(type)) {
									queryCounties();
								}
							}
						});
					}
					
				}
				
				@Override
				public void onError(Exception e) {
					// TODO Auto-generated method stub
					runOnUiThread(new Runnable() {

						@Override
						public void run() {
							// TODO Auto-generated method stub
							closeProgressDialog();
							Toast.makeText(ChooseAreaActvity.this, "����ʧ��", Toast.LENGTH_SHORT).show();
						}
						
					});
				}
			});
			
	}

	private void showProgressDialog() {
		if (progressDialog == null) {
			progressDialog = new ProgressDialog(this);
			progressDialog.setMessage("���ڼ��ء�����");
			progressDialog.setCanceledOnTouchOutside(false);
		}
			progressDialog.show();
		
	}
	private void closeProgressDialog() {
		if(progressDialog != null) {
			progressDialog.dismiss();
		}
	}
	@Override
	public void onBackPressed() {
		if (currentLevel == LEVEL_COUNTY) {
			queryCities();
		} else if (currentLevel == LEVEL_CITY) {
			queryProvinces();
		} else {
			finish();
		}
	}

}
