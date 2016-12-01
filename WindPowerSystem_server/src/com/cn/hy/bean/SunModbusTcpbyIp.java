package com.cn.hy.bean;

import java.io.BufferedReader;
/**
* @author 关龙锋 
* @date : 2016年7月11日 下午3:42:57
*/
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class SunModbusTcpbyIp {

	private static final Log LOGGER = LogFactory.getLog(SunModbusTcpbyIp.class);
	

	public static void main(String[] args) {
		// 测试地址,0,1表示非标，2表示海上风电，modbuslength为读取字节长度
		String ip = "192.168.68.231";
		int modbuslength = 3000;
		// 起始和场长度都是字的长度 22个开始 就是 查到23-， 长度不包括 报文 解析说明字。
		ReadSunModbusTcpStrAll(ip,502,0,82, 686,686);
	}

	/**
	 * 读取设备通讯协议中数据
	 * @param ip
	 * @param port
	 * @param d_type
	 * @param startaddr
	 * @param length
	 * @param modbuslength
	 * @return
	 */
	public static String ReadSunModbusTcpStrAll(String ip, int port, int d_type, int startaddr, int length,
			int modbuslength) {
		String bString = "";
		String shortstr = "";
		int num = (int) Math.ceil(modbuslength / length);
		int numy = (int) Math.ceil(modbuslength % length);
		if (numy != 0) {
			num++;
		}
		try {
			String server = ip; // 服务器IP
			int servPort = port;// 端口
			// 发送的数据
			// 参数说明：前6个00是标准modbus协议，简称标准位，
			// 01：为tcp协议的标示，在串口中起作用
			// 04标示0x03/04指令，可以读
			// 00 00读取的起始位置
			// 02AE读取的长度 686 双馈：684,全功率：682
			String[] strox = new String[12];
			// 97 79 00 00 00 06 04 03 31 F5 00 64每次读取最多读取256个字节
			// 标准的modbus,为了方便我们每次读取100个字即h64
			if (d_type == 0 || d_type == 1) {
				String[] strox01 = { "00", "00", "00", "00", "00", "00", "01", "04", "00", "00", "02", "AE" };
				strox = strox01;
			} else if (d_type == 2) {
				String[] strox02 = { "97", "79", "00", "00", "00", "06", "04", "03", "31", "F5", "00", "64" };
				strox = strox02;
			} else {
				return "没有该协议解析方式。";
			}

			// 初始化协议起始地址
			String startaddrstr = Integer.toHexString(startaddr) + "";
			strox[8] = get4HexString(startaddrstr).split(",")[0];
			strox[9] = get4HexString(startaddrstr).split(",")[1];

			// 初始化长度赋值
			String lengthstr = Integer.toHexString(length) + "";
			strox[10] = get4HexString(lengthstr).split(",")[0];
			strox[11] = get4HexString(lengthstr).split(",")[1];
			int reclength = length * 2;
			for (int j = 0; j < num; j++) {
				Socket socket = new Socket(ip, servPort);
				// LOGGER.info("连接IP为：" + ip + " 上位机成功！");
				// 更新起始地址信息
				int newstart = startaddr + j * length;
				String newstarthall = Integer.toHexString(newstart);
				strox[8] = get4HexString(newstarthall).split(",")[0];
				strox[9] = get4HexString(newstarthall).split(",")[1];
				// 更新读取长度
				if (j == num - 1) {
					int newlength = modbuslength - length * j;
					String newlengthstr = Integer.toHexString(newlength) + "";
					strox[10] = get4HexString(newlengthstr).split(",")[0];
					strox[11] = get4HexString(newlengthstr).split(",")[1];
					reclength = newlength * 2;
				}
				String strj = "";
				byte data[] = new byte[12];

				for (int i = 0; i < strox.length; i++) {
					byte b = Integer.valueOf(strox[i], 16).byteValue();
					data[i] = b;
				}
				OutputStream out = socket.getOutputStream();
				out.write(data); // 发送
				BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				String msg = null;
				while ((msg = br.readLine()) != null)
					System.out.println(msg);
				socket.close();
			}	
			LOGGER.info("返回字总和为：" + shortstr);
			System.out.println(shortstr);
		} catch (Exception e) {
			System.out.println("与ip：" + ip + "通讯失败！");
		}
		return shortstr;
	}

	/**
	 * 写指令，发送参数
	 * @param ip
	 * @param port
	 * @param d_type
	 * @param addr
	 * @param paramestr
	 * @param ids
	 * @return
	 */
    public static String WriteSunModbusTcpStrAll(String ip, int port, int d_type,String addr,String paramestr, String ids) {
			String strj = "";
			try {
				System.out.println("发送主机："+ip);
				System.out.println("发送地址："+addr);
				System.out.println("发送参数："+paramestr);
				if(ids.equals("8")&&!addr.equals("0000")){
					String  parameper[] =paramestr.split(",");
					String newpsr="";
					for(int i=0;i<parameper.length;i++){
						//切分
						String str= IntTounshort(Integer.parseInt(parameper[i]),0);	
						if(i==parameper.length){
							newpsr+=str;
						}else{
							newpsr+=str+","	;
						}
						
					}
					paramestr=newpsr;
					
					System.out.println("下发电机参数高低拆分后："+paramestr);
					
				}
				
				String server = ip; // 服务器IP
				int servPort = port;// 端口
				String  parame[] =paramestr.split(",");
				int paramesize=parame.length;
				//System.out.println("发送参数16进制："+parame[0]);
				if(paramesize>0){
					for(int j=0;j<paramesize;j++){
						String value=parame[j];
						if(value.contains("H")){
							value=value.substring(0, value.length()-1);
							gethex4(value);
							parame[j]=gethex4(value);
						}else{
						String hex=Integer.toHexString(Integer.parseInt(value));
						gethex4(hex);
						parame[j]=gethex4(hex);
						}
						System.out.println("发送参数16进制："+parame[j]);
					}
					
				}
				int lent=12+paramesize*2;
				if(d_type==2){
					 lent=13+paramesize*2;
				}
				String[] strox = new String[lent];
//				//获取地址位置
				// 97 79 00 00 00 06 04 03 31 F5 00 64每次读取最多读取256个字节
				// 标准的modbus,为了方便我们每次读取100个字即h64
				if (d_type == 0 || d_type == 1) {
					//使用默认长度0001
					String[] strox01 = { "00", "00", "00", "00", "00", "00", "01", "10", "00", "00", "00", "01" };
					
					
					String instr=Integer.toHexString(paramesize);
					if(paramesize>016){
						strox01[11]=instr;
					}else{
					    strox01[11]="0"+instr;
					}
					for(int i=0;i<strox01.length;i++){
						strox[i]=strox01[i];
					}
					
					if(addr!=""){
						strox[8]=addr.substring(0, 2);
						strox[9]=addr.substring(2, 4);
					}
					
					
					for(int i=0;i<parame.length;i++){
						int index=12+i*2;
						strox[index]=parame[i].substring(0, 2);
						strox[index+1]=parame[i].substring(2, 4);
					}
					
				} else if (d_type == 2) {
					String[] strox02 = { "97", "79", "00", "00", "00", "09", "04", "10", "31", "F4", "00", "01","02" };
					
					String instr=Integer.toHexString(paramesize*2);
					//参数个数    不超过
					if(paramesize>16){
						strox02[12]=instr;
					}else{
						strox02[12]="0"+instr;
					}
					
					//本地到最后一个字节数
					if(paramesize<4.5){
						strox02[5]="0"+Integer.toHexString(7+paramesize*2);
					}else{
						strox02[5]=Integer.toHexString(7+paramesize*2);
					}
					//strox02[5]=Integer.toHexString(7+paramesize*2);
					
					//基本协议参数
					for(int i=0;i<strox02.length;i++){
						strox[i]=strox02[i];
					}
					
					//地址
					if(addr!=""){
						strox[8]=addr.substring(0, 2);
						strox[9]=addr.substring(2, 4);
					}
					
					//参数
					for(int i=0;i<parame.length;i++){
						int index=13+i*2;
						strox[index]=parame[i].substring(0, 2);
						strox[index+1]=parame[i].substring(2, 4);
					}
					
					
				} else {
					return "没有该协议解析方式。";
				}
					// 设置tcp接收长度
					System.out.println("连接IP为：" + ip + "上位机中,请等待......！");
					Socket socket = new Socket(server, servPort);
					System.out.println("连接IP为：" + ip + " 上位机成功！");
					byte data[] = new byte[lent];

					for (int i = 0; i < strox.length; i++) {
						byte b = Integer.valueOf(strox[i], 16).byteValue();
						data[i] = b;
					}
					// byte的大小为8bits而int的大小为32bits 协议接收长度为查询长度的2倍
					byte[] recData = new byte[3000];// 接收数据缓冲 海上风电1191个字
					InputStream in = socket.getInputStream();
					OutputStream out = socket.getOutputStream();
					//设置接收数据时间
					socket.setSoTimeout(30000);
					System.out.println("发送修改指令！");
					out.write(data); // 发送
					// 接收数据 初始化 数据接收长度
					int totalBytesRcvd = 3000; // Total bytes received so far
					totalBytesRcvd = in.read(recData);// 接收
					//LOGGER.info("第" + j + "次返回字节长度为：" + totalBytesRcvd);
					for (int i = 0; i < totalBytesRcvd; i++) {
						byte str = recData[i];
						if (i == totalBytesRcvd - 1) {
							strj += str;
						} else {
							strj += str + ",";
						}

					}
					String shortstrone = getshortstr(recData);
                    if(totalBytesRcvd>0){
                    	System.out.println("指令修改成功");
                    }else{
                    	System.out.println("指令修改失败");
                    }
					socket.close();
					System.out.println("返回信息："+strj);
					return strj;
							
			} catch (Exception e) {
				String ee=e.getMessage();
				if(ee.equals("Connection timed out: connect")){
					return "connect";
				}
			LOGGER.info("与ip：" + ip + "通讯失败！");
			}

			
		
			
			return strj;
		}	
    
    public static String IntTounshort(int a, int off) {  
         short aa =(short)(a & 0xFFFF);  
         short bb=(short)((a>>16) & 0xFFFF);
         return aa+","+bb;  
     } 


	/**
	 * 字符组成16进制的字符
	 * 
	 * @param HexString
	 * @return
	 */
	public static String get4HexString(String HexString) {
		String hx = "";
		if (HexString.length() < 2) {
			hx = "00,0" + HexString;
		} else if (HexString.length() < 3) {
			hx = "00," + HexString;
		} else if (HexString.length() < 4) {
			hx = "0" + HexString.substring(0, 1) + "," + HexString.substring(1, 3);
			;
		} else {
			hx = HexString;
		}
		return hx;
	}

	/**
	 * 字符组成16进制的字符
	 * 
	 * @param HexString
	 * @return
	 */
	public static String gethex4(String HexString) {
		String hx = "";
		if (HexString.length() < 2) {
			hx = "000" + HexString;
		} else if (HexString.length() < 3) {
			hx = "00" + HexString;
		} else if (HexString.length() < 4) {
			hx = "0" +  HexString;
			;
		} else if(HexString.length()>4){
			hx = HexString.substring(4, HexString.length());
		}else{
			hx = HexString;
		}
		return hx;
	}
	/**
	 * byte位转换成short类的字符串
	 * @param newmodbusstr
	 * @return
	 */
	public static String getshortstr(byte[] newmodbusstr) {
		byte[] array = Arrays.copyOfRange(newmodbusstr, 10, newmodbusstr.length);
		// LOGGER.info("0位"+array[0] );
		int len = array.length / 2;
		// LOGGER.info("len="+len);
		short[] ArrayData = new short[len];
		byte[] arraytmp = new byte[2];
		for (short i = 0; i <= len - 1; i++) {
			arraytmp[0] = array[2 * i];
			arraytmp[1] = array[2 * i + 1];
			ArrayData[i] = byte4ToInt(arraytmp, 0);
			// LOGGER.info(2*i+"位"+array[2*i]);
			// LOGGER.info(2*i+1+"位"+array[2*i+1]);
			// LOGGER.info("合并位"+ArrayData[i] );
		}
		String bString = "";
		for (int i = 0; i < ArrayData.length; i++) {
			short str = ArrayData[i];
			if (i == ArrayData.length - 1) {
				bString += str;
			} else {
				bString += str + ",";
			}
		}

		return bString;
	}

	/**
	 * //合并字节位换成一个16位的整形数,short类
	 * 
	 * @param bytes
	 * @param off
	 * @return
	 */
	public static short byte4ToInt(byte[] bytes, int off) {
		int b0 = bytes[off] & 0xFF;
		int b1 = bytes[off + 1] & 0xFF;
		short ii = (short) ((b0 << 8) | b1);
		return ii;
	}

}
