package com.hy.servlet;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.hy.bean.JDBConnection;

/**
 * Servlet implementation class ChangeStateServlet
 */
@WebServlet("/ChangeStateServlet")
public class ChangeStateServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public ChangeStateServlet() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		String id = request.getParameter("id");
		String state = request.getParameter("state");
		try {
			JDBConnection jdbc_trd = new JDBConnection();
			Connection conn_trd = jdbc_trd.connection;
			Statement st_trd = conn_trd
					.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,
							ResultSet.CONCUR_UPDATABLE);
			String sql = "update mqtt_device set run_state='" + state
					+ "' where id='" + id + "' ";
			st_trd.executeUpdate(sql);
			st_trd.close();
			conn_trd.close();
			request.getRequestDispatcher("/DeviceServlet").forward(request,
					response);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		this.doGet(request, response);
	}

}
