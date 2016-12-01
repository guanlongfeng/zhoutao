package com.hy.data;

import static com.google.common.util.concurrent.Service.State.NEW;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.google.common.util.concurrent.AbstractService;
import com.google.common.util.concurrent.Service;
import com.google.common.util.concurrent.Uninterruptibles;
import com.hy.bean.AESUtils;
import com.hy.bean.CRCUtils;
import com.hy.bean.JDBConnection;
import com.hy.bean.MqttUtil;
import com.hy.bean.SunModbusTcpbyIp;
import com.hy.pojo.Device;

public class SungrowService extends AbstractService implements Service {
	private State state = NEW;
	private Socket sck;
	private Listener listener;
	 static MqttUtil mqttut=new MqttUtil();
	SungrowService(Socket sck) {
		this.sck = sck;
	}

	public State stopAndWait() {
		return state;
	}

	@Override
	protected void doStart() {
		new Thread() {
			@SuppressWarnings("deprecation")
			@Override
			public void run() {
				notifyStarted();
				boolean isrun = true;
				int numct=0;
				while (isrun) {
					List<Device> devicelist = new ArrayList<Device>();
					devicelist=mqttut.startDataAcq();
					//每一分钟发送一次心跳。每5分钟发送一次运行数据
					Uninterruptibles.sleepUninterruptibly(2000, TimeUnit.MILLISECONDS);
					if(numct==5){
						for (int num = 0; num < devicelist.size(); num++) {
							Device devone = devicelist.get(num);
							mqttut.startthreadother(devone,sck);
						};
						numct=0;
					}else{
						//发送心跳
						String[] xthfcode=mqttut.sendxtcode(sck);
						if(xthfcode==null){
							//关闭线程
							isrun=false;
							System.out.println("MQTT服务器出现故障，断开连接......");
							new THD_TWO(sck).stop();
							new THD_ONE().start();;
							
						}
						numct++;
					}
				}
			}

			

		}.start();
	}
	@Override
	protected void doStop() {
		notifyStopped();
		new Thread() {
			@Override
			public void run() {
				Uninterruptibles.sleepUninterruptibly(1000, TimeUnit.MILLISECONDS);
				notifyStopped();
			}
		}.start();
	}

}