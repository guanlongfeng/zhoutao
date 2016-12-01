package com.hy.data;

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import com.hy.bean.MqttUtil;
import com.hy.pojo.CdhData;
import com.hy.pojo.Data_Acq;
import com.hy.pojo.Device;

public class THD_TWO extends Thread {
	static MqttUtil mqttut = new MqttUtil();
	private Socket sck;

	THD_TWO(Socket sck) {
		this.sck = sck;
	}

	public void run() {
		boolean isrun = true;
		System.out.println("开启监听设备状态线程线程......");
		while (isrun) {
			List<Device> devicelist = new ArrayList<Device>();
			//获取变流器列表信息
			devicelist = mqttut.startDataAcq();
			try {
				THD_TWO.sleep(3000);
				for (int num = 0; num < devicelist.size(); num++) {
					Device devone = devicelist.get(num);
					//获取设备采集信息
					Data_Acq data = mqttut.getDevice_data(devone);
					System.out.println("监听设备状态中.....设备ID="+devone.getDevice_id());
					//发送运行数据
					String tmpreq=mqttut.sendrundatacode(devone, 2, sck, null, data);
					if(tmpreq.equals("error")){
						isrun=false;
						System.out.println("MQTT出现故障，停止监听设备状态进程......");
					}
				};
			} catch (Exception e) {
				isrun = false;
				e.printStackTrace();
				new THD_TWO(sck).start();
			}
		}

	}
}
